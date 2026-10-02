package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.GeneratedCertificate
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfPrintHelper {

    fun printCertificate(context: Context, pdfFile: File, certTitle: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "প্রিন্ট সেবা উপলব্ধ নয়", Toast.LENGTH_SHORT).show()
                return
            }

            val jobName = "ইউপি সনদ - $certTitle"
            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder("sonod.pdf")
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: android.os.ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                val buf = ByteArray(1024)
                                var bytesRead: Int
                                while (input.read(buf).also { bytesRead = it } > 0) {
                                    output.write(buf, 0, bytesRead)
                                }
                            }
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()

            printManager.print(jobName, printAdapter, printAttributes)
        } catch (e: Exception) {
            Toast.makeText(context, "প্রিন্ট শুরু করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareCertificate(context: Context, pdfFile: File, certTitle: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "ইউনিয়ন পরিষদ সনদপত্র - $certTitle")
                putExtra(Intent.EXTRA_TEXT, "সংযুক্ত ফাইলে ইউনিয়ন পরিষদ কর্তৃক প্রস্তুতকৃত $certTitle পত্রটি রয়েছে।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "সনদপত্রটি শেয়ার করুন"))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCertificate(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "পিডিএফ ফাইল খোলার জন্য উপযুক্ত অ্যাপ নেই", Toast.LENGTH_SHORT).show()
        }
    }

    fun savePdfToDownloads(context: Context, sourcePdf: File, filename: String): Uri? {
        val cleanName = filename.replace("/", "_").replace(" ", "_") + ".pdf"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/UP_Sonod")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourcePdf).use { input ->
                            input.copyTo(out)
                        }
                    }
                }
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "UP_Sonod")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, cleanName)
                FileInputStream(sourcePdf).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            null
        }
    }
}
