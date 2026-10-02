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
        targetFile: File? = null
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawCertificateOnCanvas(context, canvas, certificate, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat())

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
        height: Float
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
        val emblemRadius = 22f
        val emblemCenterX = width / 2f
        val emblemCenterY = margin + 34f
        drawGovernmentEmblem(canvas, emblemCenterX, emblemCenterY, emblemRadius)

        var curY = emblemCenterY + emblemRadius + 14f

        val govtPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = notoBengali
            isAntiAlias = true
        }
        canvas.drawText("গণপ্রজাতন্ত্রী বাংলাদেশ সরকার", width / 2f, curY, govtPaint)

        curY += 16f
        val unionPaint = Paint().apply {
            color = Color.rgb(0, 95, 65)
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

        // Draw Passport Photo Box on Top Right
        val photoWidth = 72f
        val photoHeight = 86f
        val photoX = width - margin - photoWidth - 14f
        val photoY = margin + 14f
        drawApplicantPhoto(context, canvas, cert.applicantPhotoUri, photoX, photoY, photoWidth, photoHeight, notoBengali)

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

        // 5. Formal Bangla Body Text
        curY += 16f
        val bodyPaint = TextPaint().apply {
            color = Color.rgb(20, 20, 20)
            textSize = 12.5f
            typeface = notoBengali
            isAntiAlias = true
        }

        val textWidth = (contentWidth - 36f).toInt()
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(cert.generatedBodyText, 0, cert.generatedBodyText.length, bodyPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(5f, 1.15f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                cert.generatedBodyText,
                bodyPaint,
                textWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                5f,
                true
            )
        }

        canvas.save()
        canvas.translate(margin + 18f, curY)
        staticLayout.draw(canvas)
        canvas.restore()

        curY += staticLayout.height + 12f

        // 6. Succession Heirs Table (if applicable)
        val heirs = cert.heirsJson?.let { BanglaHelper.parseHeirsJson(it) } ?: emptyList()
        if (heirs.isNotEmpty()) {
            curY = drawHeirsTable(canvas, heirs, margin + 18f, curY, textWidth.toFloat(), notoBengali)
        }

        // 7. Official Bottom Footer Section (Chairman signature, Seal, Ward Member)
        drawBottomSignaturesAndSeal(canvas, cert, margin, width, height, notoBengali)
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

    private fun drawGovernmentEmblem(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val greenPaint = Paint().apply {
            color = Color.rgb(0, 106, 78)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        val innerCirclePaint = Paint().apply {
            color = Color.rgb(244, 42, 65)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val starPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        canvas.drawCircle(cx, cy, r, greenPaint)
        canvas.drawCircle(cx, cy, r * 0.72f, innerCirclePaint)

        // Draw star inside red circle
        val s = r * 0.35f
        val path = android.graphics.Path().apply {
            moveTo(cx, cy - s)
            lineTo(cx + s * 0.25f, cy - s * 0.25f)
            lineTo(cx + s, cy)
            lineTo(cx + s * 0.35f, cy + s * 0.4f)
            lineTo(cx + s * 0.6f, cy + s)
            lineTo(cx, cy + s * 0.55f)
            lineTo(cx - s * 0.6f, cy + s)
            lineTo(cx - s * 0.35f, cy + s * 0.4f)
            lineTo(cx - s, cy)
            lineTo(cx - s * 0.25f, cy - s * 0.25f)
            close()
        }
        canvas.drawPath(path, starPaint)
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
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
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
        typeface: android.graphics.Typeface?
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
        val headerTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
            this.typeface = typeface
            isAntiAlias = true
        }
        val rowTextPaint = Paint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = 9.5f
            this.typeface = typeface
            isAntiAlias = true
        }

        // Column widths
        val colSl = 32f
        val colRel = 80f
        val colAge = 55f
        val colRem = 85f
        val colName = totalWidth - (colSl + colRel + colAge + colRem)

        val rowHeight = 20f

        // Draw Table Header
        canvas.drawRect(x, curY, x + totalWidth, curY + rowHeight, headerBg)
        canvas.drawRect(x, curY, x + totalWidth, curY + rowHeight, tablePaint)

        // Vertical lines for header
        canvas.drawLine(x + colSl, curY, x + colSl, curY + rowHeight, tablePaint)
        canvas.drawLine(x + colSl + colName, curY, x + colSl + colName, curY + rowHeight, tablePaint)
        canvas.drawLine(x + colSl + colName + colRel, curY, x + colSl + colName + colRel, curY + rowHeight, tablePaint)
        canvas.drawLine(x + colSl + colName + colRel + colAge, curY, x + colSl + colName + colRel + colAge, curY + rowHeight, tablePaint)

        // Header titles
        canvas.drawText("ক্র.নং", x + 5f, curY + 14f, headerTextPaint)
        canvas.drawText("ওয়ারিশগণের নাম", x + colSl + 6f, curY + 14f, headerTextPaint)
        canvas.drawText("সম্পর্ক", x + colSl + colName + 6f, curY + 14f, headerTextPaint)
        canvas.drawText("বয়স", x + colSl + colName + colRel + 6f, curY + 14f, headerTextPaint)
        canvas.drawText("মন্তব্য", x + colSl + colName + colRel + colAge + 6f, curY + 14f, headerTextPaint)

        curY += rowHeight

        // Draw Rows
        for (i in heirs.indices) {
            val h = heirs[i]
            canvas.drawRect(x, curY, x + totalWidth, curY + rowHeight, tablePaint)

            canvas.drawLine(x + colSl, curY, x + colSl, curY + rowHeight, tablePaint)
            canvas.drawLine(x + colSl + colName, curY, x + colSl + colName, curY + rowHeight, tablePaint)
            canvas.drawLine(x + colSl + colName + colRel, curY, x + colSl + colName + colRel, curY + rowHeight, tablePaint)
            canvas.drawLine(x + colSl + colName + colRel + colAge, curY, x + colSl + colName + colRel + colAge, curY + rowHeight, tablePaint)

            val slText = BanglaHelper.toBanglaDigits((i + 1).toString())
            canvas.drawText(slText, x + 8f, curY + 14f, rowTextPaint)
            canvas.drawText(h.name.take(28), x + colSl + 6f, curY + 14f, rowTextPaint)
            canvas.drawText(h.relation.take(12), x + colSl + colName + 6f, curY + 14f, rowTextPaint)
            canvas.drawText(BanglaHelper.toBanglaDigits(h.age), x + colSl + colName + colRel + 6f, curY + 14f, rowTextPaint)
            canvas.drawText(h.remarks.take(15), x + colSl + colName + colRel + colAge + 6f, curY + 14f, rowTextPaint)

            curY += rowHeight
        }

        return curY + 10f
    }

    private fun drawBottomSignaturesAndSeal(
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
        val sigLineLength = 120f
        canvas.drawLine(leftX, footerBaseY - 32f, leftX + sigLineLength, footerBaseY - 32f, linePaint)
        canvas.drawText("সত্যায়নকারী ইউপি সদস্য", leftX + 4f, footerBaseY - 18f, boldPaint)
        canvas.drawText("স্বাক্ষর ও সীলমোহর", leftX + 16f, footerBaseY - 4f, textPaint)

        // 2. Center: Official Seal Circle Placeholder
        val sealCenterX = width / 2f
        val sealCenterY = footerBaseY - 24f
        val sealRadius = 32f

        val sealPaint = Paint().apply {
            color = Color.rgb(180, 70, 70) // Soft maroon/red seal ring
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            pathEffect = DashPathEffect(floatArrayOf(5f, 3f), 0f)
            isAntiAlias = true
        }
        canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, sealPaint)

        val sealTextPaint = Paint().apply {
            color = Color.rgb(160, 60, 60)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            this.typeface = typeface
            isAntiAlias = true
        }
        canvas.drawText("ইউনিয়ন পরিষদ", sealCenterX, sealCenterY - 4f, sealTextPaint)
        canvas.drawText("গোল সীলমোহর", sealCenterX, sealCenterY + 8f, sealTextPaint)

        // 3. Right side: Chairman signature & title
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
