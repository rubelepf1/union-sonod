package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.CertificateRegistry
import com.example.data.model.GeneratedCertificate
import com.example.data.model.Heir
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    // A4 dimensions in points (72 points = 1 inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateCertificatePdf(
        context: Context,
        certificate: GeneratedCertificate,
        targetFile: File? = null,
        logoUri: String? = null
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawCertificateOnCanvas(context, canvas, certificate, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), logoUri)

        pdfDoc.finishPage(page)

        val outputFile = targetFile ?: run {
            val dir = File(context.cacheDir, "certificates")
            if (!dir.exists()) dir.mkdirs()
            File(dir, "sonod_${certificate.serialNo.replace("/", "_")}_${System.currentTimeMillis()}.pdf")
        }

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }

    fun drawCertificateOnCanvas(
        context: Context,
        canvas: Canvas,
        cert: GeneratedCertificate,
        width: Float,
        height: Float,
        logoUri: String? = null
    ) {
        val notoBengali = try {
            ResourcesCompat.getFont(context, R.font.noto_sans_bengali)
        } catch (_: Exception) {
            null
        }

        // Fill background white
        canvas.drawColor(Color.WHITE)

        val margin = 26f
        val contentWidth = width - (margin * 2)

        // 1. Draw outer decorative government double-borders
        val borderPaint = Paint().apply {
            color = Color.rgb(20, 70, 45) // Deep national green tone
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRect(margin, margin, width - margin, height - margin, borderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawRect(margin + 4f, margin + 4f, width - margin - 4f, height - margin - 4f, innerBorderPaint)

        // Corner decorative accents
        drawCornerDecorations(canvas, margin + 6f, margin + 6f, width - margin - 6f, height - margin - 6f)

        // 2. Top Emblem & Header
        val emblemRadius = 24f
        val emblemCenterX = width / 2f
        val emblemCenterY = margin + 34f
        drawUnionParishadEmblem(context, canvas, emblemCenterX, emblemCenterY, emblemRadius, logoUri, notoBengali)

        var curY = emblemCenterY + emblemRadius + 14f

        val govtPaint = Paint().apply {
            color = Color.rgb(0, 95, 65)
            textSize = 12.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = notoBengali
            isAntiAlias = true
        }
        canvas.drawText("স্থানীয় সরকার বিভাগ", width / 2f, curY, govtPaint)

        curY += 16f
        val unionPaint = Paint().apply {
            color = Color.BLACK
            textSize = 17f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = notoBengali
            isAntiAlias = true
        }
        canvas.drawText(cert.unionName, width / 2f, curY, unionPaint)

        curY += 15f
        val subHeaderPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11.5f
            textAlign = Paint.Align.CENTER
            typeface = notoBengali
            isAntiAlias = true
        }
        canvas.drawText("উপজেলা: ${cert.upazila}, জেলা: ${cert.district}", width / 2f, curY, subHeaderPaint)

        // Draw Passport Photo Box on Top Right ONLY IF this certificate type actually requires/supports photo
        val certType = CertificateRegistry.findById(cert.certificateTypeId)
        val requiresPhoto = certType?.requiresPhoto == true
        val hasPhoto = !cert.applicantPhotoUri.isNullOrBlank()
        val shouldDrawPhoto = requiresPhoto

        if (shouldDrawPhoto) {
            val photoWidth = 72f
            val photoHeight = 86f
            val photoX = width - margin - photoWidth - 14f
            val photoY = margin + 14f
            drawApplicantPhoto(context, canvas, cert.applicantPhotoUri, photoX, photoY, photoWidth, photoHeight, notoBengali)
        }

        // Header separator line
        curY += 10f
        val linePaint = Paint().apply {
            color = Color.rgb(180, 190, 185)
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawLine(margin + 20f, curY, width - margin - 20f, curY, linePaint)

        // 3. Certificate Title Box
        curY += 18f
        val title = cert.certificateTitle
        val titleTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 15f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = notoBengali
            isAntiAlias = true
        }
        val titleBounds = Rect()
        titleTextPaint.getTextBounds(title, 0, title.length, titleBounds)
        val titleBoxWidth = titleBounds.width() + 36f
        val titleBoxHeight = 24f
        val titleBoxRect = RectF(
            (width - titleBoxWidth) / 2f,
            curY - 16f,
            (width + titleBoxWidth) / 2f,
            curY + 8f
        )

        val titleBoxBgPaint = Paint().apply {
            color = Color.rgb(243, 248, 245)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(titleBoxRect, 4f, 4f, titleBoxBgPaint)

        val titleBoxStrokePaint = Paint().apply {
            color = Color.rgb(0, 95, 65)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(titleBoxRect, 4f, 4f, titleBoxStrokePaint)
        canvas.drawText(title, width / 2f, curY, titleTextPaint)

        // 4. Ref No and Issue Date Row
        curY += 22f
        val metaPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 10.5f
            typeface = notoBengali
            isAntiAlias = true
        }
        canvas.drawText("স্মারক নং: ${cert.serialNo}", margin + 18f, curY, metaPaint)

        val datePaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 10.5f
            textAlign = Paint.Align.RIGHT
            typeface = notoBengali
            isAntiAlias = true
        }
        canvas.drawText("তারিখ: ${cert.issueDateBangla}", width - margin - 18f, curY, datePaint)

        // Underline under ref/date
        curY += 6f
        canvas.drawLine(margin + 18f, curY, width - margin - 18f, curY, linePaint)

        // 5. Formal Bangla Body Text & Heirs Table (with Auto-fit calculation)
        val heirs = cert.heirsJson?.let { BanglaHelper.parseHeirsJson(it) } ?: emptyList()
        val textWidth = (contentWidth - 36f).toInt()

        // Determine if scaling is needed based on heirs count and text length
        val hasManyHeirs = heirs.size > 4
        val isVeryLongText = cert.generatedBodyText.length > 250

        val bodyTextSize = when {
            heirs.size >= 8 || (hasManyHeirs && isVeryLongText) -> 10.5f
            hasManyHeirs || isVeryLongText -> 11.5f
            else -> 12.5f
        }
        val lineSpacingMult = if (hasManyHeirs) 1.08f else 1.15f
        val lineSpacingAdd = if (hasManyHeirs) 2f else 4f

        curY += if (hasManyHeirs) 10f else 16f

        val bodyPaint = TextPaint().apply {
            color = Color.rgb(20, 20, 20)
            textSize = bodyTextSize
            typeface = notoBengali
            isAntiAlias = true
        }

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(cert.generatedBodyText, 0, cert.generatedBodyText.length, bodyPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(lineSpacingAdd, lineSpacingMult)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                cert.generatedBodyText,
                bodyPaint,
                textWidth,
                Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMult,
                lineSpacingAdd,
                true
            )
        }

        canvas.save()
        canvas.translate(margin + 18f, curY)
        staticLayout.draw(canvas)
        canvas.restore()

        curY += staticLayout.height + (if (hasManyHeirs) 8f else 12f)

        // 6. Succession Heirs Table (if applicable)
        if (heirs.isNotEmpty()) {
            val maxRowHeight = when {
                heirs.size >= 8 -> 15.5f
                heirs.size >= 5 -> 17.5f
                else -> 20f
            }
            curY = drawHeirsTable(canvas, heirs, margin + 18f, curY, textWidth.toFloat(), notoBengali, maxRowHeight)
        }

        // 7. Official Bottom Footer Section (Chairman signature, Ward Member)
        drawBottomSignatures(canvas, cert, margin, width, height, notoBengali)
    }

    private fun drawCornerDecorations(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val p = Paint().apply {
            color = Color.rgb(0, 106, 78)
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val s = 10f
        // Top-left
        canvas.drawLine(left, top, left + s, top, p)
        canvas.drawLine(left, top, left, top + s, p)
        // Top-right
        canvas.drawLine(right, top, right - s, top, p)
        canvas.drawLine(right, top, right, top + s, p)
        // Bottom-left
        canvas.drawLine(left, bottom, left + s, bottom, p)
        canvas.drawLine(left, bottom, left, bottom - s, p)
        // Bottom-right
        canvas.drawLine(right, bottom, right - s, bottom, p)
        canvas.drawLine(right, bottom, right, bottom - s, p)
    }

    private fun drawUnionParishadEmblem(
        context: Context,
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        logoUri: String?,
        typeface: android.graphics.Typeface?
    ) {
        val greenColor = Color.rgb(0, 106, 78)
        val greenBorderPaint = Paint().apply {
            color = greenColor
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            isAntiAlias = true
        }

        // 1. Try custom logo first if present
        if (!logoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(logoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        canvas.save()
                        val clipPath = android.graphics.Path().apply {
                            addCircle(cx, cy, r, android.graphics.Path.Direction.CW)
                        }
                        canvas.clipPath(clipPath)
                        val dstRect = RectF(cx - r, cy - r, cx + r, cy + r)
                        canvas.drawBitmap(bmp, Rect(0, 0, bmp.width, bmp.height), dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
                        canvas.restore()
                        canvas.drawCircle(cx, cy, r, greenBorderPaint)
                        return
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Default Authentic Union Parishad Local Government Monogram
        val whiteRingPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val innerDiscPaint = Paint().apply {
            color = Color.rgb(0, 95, 65) // UP Green
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val goldAccentPaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val petalPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val petalBorderPaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        val starPaint = Paint().apply {
            color = Color.rgb(220, 38, 38)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val wavePaint = Paint().apply {
            color = Color.rgb(147, 197, 253) // light blue waves
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        val paddyPaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Golden ears of paddy
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val jutePaint = Paint().apply {
            color = Color.rgb(22, 163, 74) // Green jute leaves
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // 1. Draw outer circle border & white background
        canvas.drawCircle(cx, cy, r, whiteRingPaint)
        canvas.drawCircle(cx, cy, r, greenBorderPaint)

        // 2. 4 Red stars in the outer ring
        val starR = r * 0.83f
        val starSize = r * 0.08f
        drawTinyStar(canvas, cx - starR * 0.70f, cy + starR * 0.40f, starSize, starPaint)
        drawTinyStar(canvas, cx - starR * 0.88f, cy + starR * 0.10f, starSize, starPaint)
        drawTinyStar(canvas, cx + starR * 0.70f, cy + starR * 0.40f, starSize, starPaint)
        drawTinyStar(canvas, cx + starR * 0.88f, cy + starR * 0.10f, starSize, starPaint)

        // 3. Ring Text: "স্থানীয় সরকার বিভাগ" on top, "ইউনিয়ন পরিষদ" on bottom
        val ringTextPaint = Paint().apply {
            color = greenColor
            textSize = r * 0.20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            this.typeface = typeface
            isAntiAlias = true
        }
        val topArcPath = android.graphics.Path().apply {
            addArc(RectF(cx - r * 0.84f, cy - r * 0.84f, cx + r * 0.84f, cy + r * 0.84f), 195f, 150f)
        }
        canvas.drawTextOnPath("স্থানীয় সরকার বিভাগ", topArcPath, 0f, 0f, ringTextPaint)

        val bottomArcPath = android.graphics.Path().apply {
            addArc(RectF(cx - r * 0.84f, cy - r * 0.84f, cx + r * 0.84f, cy + r * 0.84f), 50f, 80f)
        }
        canvas.drawTextOnPath("ইউনিয়ন পরিষদ", bottomArcPath, 0f, 0f, ringTextPaint)

        // 4. Inner Green Disc
        val innerR = r * 0.60f
        canvas.drawCircle(cx, cy, innerR, innerDiscPaint)
        canvas.drawCircle(cx, cy, innerR, goldAccentPaint)

        // 5. Water Waves under Shapla
        canvas.drawLine(cx - innerR * 0.6f, cy + innerR * 0.45f, cx + innerR * 0.6f, cy + innerR * 0.45f, wavePaint)
        canvas.drawLine(cx - innerR * 0.45f, cy + innerR * 0.6f, cx + innerR * 0.45f, cy + innerR * 0.6f, wavePaint)

        // 6. Shapla (Water Lily) Petals in Center
        val centerPetal = android.graphics.Path().apply {
            moveTo(cx, cy - innerR * 0.45f)
            cubicTo(cx - innerR * 0.22f, cy - innerR * 0.2f, cx - innerR * 0.18f, cy + innerR * 0.25f, cx, cy + innerR * 0.35f)
            cubicTo(cx + innerR * 0.18f, cy + innerR * 0.25f, cx + innerR * 0.22f, cy - innerR * 0.2f, cx, cy - innerR * 0.45f)
            close()
        }
        canvas.drawPath(centerPetal, petalPaint)
        canvas.drawPath(centerPetal, petalBorderPaint)

        val leftPetal = android.graphics.Path().apply {
            moveTo(cx - innerR * 0.1f, cy - innerR * 0.35f)
            cubicTo(cx - innerR * 0.45f, cy - innerR * 0.1f, cx - innerR * 0.40f, cy + innerR * 0.25f, cx, cy + innerR * 0.35f)
            cubicTo(cx - innerR * 0.1f, cy + innerR * 0.2f, cx - innerR * 0.05f, cy - innerR * 0.1f, cx - innerR * 0.1f, cy - innerR * 0.35f)
            close()
        }
        canvas.drawPath(leftPetal, petalPaint)
        canvas.drawPath(leftPetal, petalBorderPaint)

        val rightPetal = android.graphics.Path().apply {
            moveTo(cx + innerR * 0.1f, cy - innerR * 0.35f)
            cubicTo(cx + innerR * 0.45f, cy - innerR * 0.1f, cx + innerR * 0.40f, cy + innerR * 0.25f, cx, cy + innerR * 0.35f)
            cubicTo(cx + innerR * 0.1f, cy + innerR * 0.2f, cx + innerR * 0.05f, cy - innerR * 0.1f, cx + innerR * 0.1f, cy - innerR * 0.35f)
            close()
        }
        canvas.drawPath(rightPetal, petalPaint)
        canvas.drawPath(rightPetal, petalBorderPaint)

        // 7. Paddy / Rice grains flanking petals
        canvas.drawCircle(cx - innerR * 0.55f, cy + innerR * 0.1f, innerR * 0.09f, paddyPaint)
        canvas.drawCircle(cx - innerR * 0.50f, cy - innerR * 0.1f, innerR * 0.09f, paddyPaint)
        canvas.drawCircle(cx + innerR * 0.55f, cy + innerR * 0.1f, innerR * 0.09f, paddyPaint)
        canvas.drawCircle(cx + innerR * 0.50f, cy - innerR * 0.1f, innerR * 0.09f, paddyPaint)

        // 8. 3 Jute Leaves at Top
        canvas.drawCircle(cx, cy - innerR * 0.65f, innerR * 0.09f, jutePaint)
        canvas.drawCircle(cx - innerR * 0.18f, cy - innerR * 0.58f, innerR * 0.08f, jutePaint)
        canvas.drawCircle(cx + innerR * 0.18f, cy - innerR * 0.58f, innerR * 0.08f, jutePaint)
    }

    private fun drawTinyStar(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val path = android.graphics.Path().apply {
            moveTo(cx, cy - size)
            lineTo(cx + size * 0.25f, cy - size * 0.25f)
            lineTo(cx + size, cy)
            lineTo(cx + size * 0.35f, cy + size * 0.4f)
            lineTo(cx + size * 0.6f, cy + size)
            lineTo(cx, cy + size * 0.55f)
            lineTo(cx - size * 0.6f, cy + size)
            lineTo(cx - size * 0.35f, cy + size * 0.4f)
            lineTo(cx - size, cy)
            lineTo(cx - size * 0.25f, cy - size * 0.25f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawApplicantPhoto(
        context: Context,
        canvas: Canvas,
        photoUriStr: String?,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        typeface: android.graphics.Typeface?
    ) {
        val rect = RectF(x, y, x + w, y + h)
        var bitmap: Bitmap? = null

        if (!photoUriStr.isNullOrBlank()) {
            try {
                val uri = Uri.parse(photoUriStr)
                // First decode bounds only
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                }

                // Downsample large camera photos to max 300x300
                var sampleSize = 1
                while (boundsOptions.outWidth / sampleSize > 300 || boundsOptions.outHeight / sampleSize > 300) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            } catch (_: Exception) {}
        }

        if (bitmap != null) {
            val src = Rect(0, 0, bitmap!!.width, bitmap!!.height)
            canvas.drawBitmap(bitmap!!, src, rect, Paint(Paint.FILTER_BITMAP_FLAG))
            val borderPaint = Paint().apply {
                color = Color.rgb(100, 100, 100)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRect(rect, borderPaint)
        } else {
            // Draw placeholder box
            val bgPaint = Paint().apply {
                color = Color.rgb(248, 249, 250)
                style = Paint.Style.FILL
            }
            canvas.drawRect(rect, bgPaint)

            val dashPaint = Paint().apply {
                color = Color.rgb(160, 170, 165)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
            }
            canvas.drawRect(rect, dashPaint)

            val p = Paint().apply {
                color = Color.GRAY
                textSize = 9.5f
                textAlign = Paint.Align.CENTER
                this.typeface = typeface
                isAntiAlias = true
            }
            canvas.drawText("আবেদনকারীর", x + (w / 2f), y + (h / 2f) - 4f, p)
            canvas.drawText("পাসপোর্ট ছবি", x + (w / 2f), y + (h / 2f) + 10f, p)
        }
    }

    private fun drawHeirsTable(
        canvas: Canvas,
        heirs: List<Heir>,
        x: Float,
        y: Float,
        totalWidth: Float,
        typeface: android.graphics.Typeface?,
        rowHeight: Float = 20f
    ): Float {
        var curY = y + 6f

        val tablePaint = Paint().apply {
            color = Color.rgb(30, 30, 30)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val headerBg = Paint().apply {
            color = Color.rgb(238, 243, 240)
            style = Paint.Style.FILL
        }
        val headerFontSize = if (rowHeight < 18f) 8.5f else 10f
        val headerTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = headerFontSize
            isFakeBoldText = true
            this.typeface = typeface
            isAntiAlias = true
        }
        val rowFontSize = if (rowHeight < 18f) 8.5f else 9.5f
        val rowTextPaint = Paint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = rowFontSize
            this.typeface = typeface
            isAntiAlias = true
        }

        // Column widths
        val colSl = 32f
        val colRel = 80f
        val colAge = 55f
        val colRem = 85f
        val colName = totalWidth - (colSl + colRel + colAge + colRem)

        val textBaselineY = curY + (rowHeight * 0.7f)

        // Draw Table Header
        canvas.drawRect(x, curY, x + totalWidth, curY + rowHeight, headerBg)
        canvas.drawRect(x, curY, x + totalWidth, curY + rowHeight, tablePaint)

        // Vertical lines for header
        canvas.drawLine(x + colSl, curY, x + colSl, curY + rowHeight, tablePaint)
        canvas.drawLine(x + colSl + colName, curY, x + colSl + colName, curY + rowHeight, tablePaint)
        canvas.drawLine(x + colSl + colName + colRel, curY, x + colSl + colName + colRel, curY + rowHeight, tablePaint)
        canvas.drawLine(x + colSl + colName + colRel + colAge, curY, x + colSl + colName + colRel + colAge, curY + rowHeight, tablePaint)

        // Header titles
        canvas.drawText("ক্র.নং", x + 5f, textBaselineY, headerTextPaint)
        canvas.drawText("ওয়ারিশগণের নাম", x + colSl + 6f, textBaselineY, headerTextPaint)
        canvas.drawText("সম্পর্ক", x + colSl + colName + 6f, textBaselineY, headerTextPaint)
        canvas.drawText("বয়স", x + colSl + colName + colRel + 6f, textBaselineY, headerTextPaint)
        canvas.drawText("মন্তব্য", x + colSl + colName + colRel + colAge + 6f, textBaselineY, headerTextPaint)

        curY += rowHeight

        // Draw Rows
        for (i in heirs.indices) {
            val h = heirs[i]
            val rowBaselineY = curY + (rowHeight * 0.7f)

            canvas.drawRect(x, curY, x + totalWidth, curY + rowHeight, tablePaint)

            canvas.drawLine(x + colSl, curY, x + colSl, curY + rowHeight, tablePaint)
            canvas.drawLine(x + colSl + colName, curY, x + colSl + colName, curY + rowHeight, tablePaint)
            canvas.drawLine(x + colSl + colName + colRel, curY, x + colSl + colName + colRel, curY + rowHeight, tablePaint)
            canvas.drawLine(x + colSl + colName + colRel + colAge, curY, x + colSl + colName + colRel + colAge, curY + rowHeight, tablePaint)

            val slText = BanglaHelper.toBanglaDigits((i + 1).toString())
            canvas.drawText(slText, x + 8f, rowBaselineY, rowTextPaint)
            canvas.drawText(h.name.take(28), x + colSl + 6f, rowBaselineY, rowTextPaint)
            canvas.drawText(h.relation.take(12), x + colSl + colName + 6f, rowBaselineY, rowTextPaint)
            canvas.drawText(BanglaHelper.toBanglaDigits(h.age), x + colSl + colName + colRel + 6f, rowBaselineY, rowTextPaint)
            canvas.drawText(h.remarks.take(15), x + colSl + colName + colRel + colAge + 6f, rowBaselineY, rowTextPaint)

            curY += rowHeight
        }

        return curY + 10f
    }

    private fun drawBottomSignatures(
        canvas: Canvas,
        cert: GeneratedCertificate,
        margin: Float,
        width: Float,
        height: Float,
        typeface: android.graphics.Typeface?
    ) {
        val footerBaseY = height - margin - 35f

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10.5f
            this.typeface = typeface
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
            this.typeface = typeface
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            strokeWidth = 0.8f
        }

        // 1. Left side: Ward Member
        val leftX = margin + 20f
        val sigLineLength = 130f
        canvas.drawLine(leftX, footerBaseY - 32f, leftX + sigLineLength, footerBaseY - 32f, linePaint)
        canvas.drawText("সত্যায়নকারী ইউপি সদস্য", leftX + 8f, footerBaseY - 18f, boldPaint)
        canvas.drawText("স্বাক্ষর", leftX + 46f, footerBaseY - 4f, textPaint)

        // 2. Right side: Chairman signature & title (aligned to the right)
        val rightX = width - margin - 20f
        val rightTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10.5f
            textAlign = Paint.Align.RIGHT
            this.typeface = typeface
            isAntiAlias = true
        }
        val rightBoldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            this.typeface = typeface
            isAntiAlias = true
        }

        canvas.drawLine(rightX - sigLineLength, footerBaseY - 32f, rightX, footerBaseY - 32f, linePaint)
        canvas.drawText(cert.chairmanName, rightX, footerBaseY - 18f, rightBoldPaint)
        canvas.drawText("চেয়ারম্যান", rightX, footerBaseY - 4f, rightBoldPaint)
        canvas.drawText(cert.unionName, rightX, footerBaseY + 10f, rightTextPaint)
        canvas.drawText("${cert.upazila}, ${cert.district}", rightX, footerBaseY + 22f, rightTextPaint)
    }
}
