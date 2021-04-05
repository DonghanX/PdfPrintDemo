package com.redhood.pdfprintdemo.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import com.redhood.pdfprintdemo.model.PrintEntity
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.ceil

class CustomPrintAdapter(
    private val context: Context,
    private val printList: List<PrintEntity>?,
    private val exportFileName: String = "diary_export_file_" + System.currentTimeMillis()
) : PrintDocumentAdapter() {

    private var pagesCount: Int = 0

    private var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        pdfDocument = PrintedPdfDocument(context, newAttributes)
        // handle cancellation request
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        // retrieve printed pages
        pagesCount = getPrintPagesCount()

        if (pagesCount > 0) {
            PrintDocumentInfo.Builder(exportFileName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pagesCount)
                .build()
                .apply {
                    // utilize content-change flag to avoid calling onWrite() redundantly
                    callback?.onLayoutFinished(this, true)
                }
        } else {
            callback?.onLayoutFailed("Failed to calculate document pages.")

        }
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {

        if (cancellationSignal?.isCanceled == true) {
            callback?.onWriteCancelled()
            return
        }

        for (i in 0 until pagesCount) {

            pdfDocument?.startPage(i)?.also { page ->
                if (cancellationSignal?.isCanceled == true) {
                    releasePrintProcess(callback)
                    return
                }
                drawPage(page)
            }
        }

        writePdfDocumentToFile(destination, pages, callback)
    }

    private fun writePdfDocumentToFile(
        destination: ParcelFileDescriptor?,
        pages: Array<out PageRange>?,
        callback: WriteResultCallback?
    ) {
        try {
            pdfDocument?.writeTo(FileOutputStream(destination?.fileDescriptor))
        } catch (e: IOException) {
            callback?.onWriteFailed(e.toString())
        } finally {
            closePdfDocumentStream()
        }
        callback?.onWriteFinished(pages)
    }

    private fun drawPage(page: PdfDocument.Page) {
        page.canvas.apply {
            val testTitle = "test"
            // units are in points (1/72 of an inch)
            val titleBaseLine = 72f
            val leftMargin = 54f

            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 36f
            drawText(testTitle, leftMargin, titleBaseLine, paint)

            paint.textSize = 11f
            drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint)

            paint.color = Color.BLUE
            drawRect(100f, 100f, 172f, 172f, paint)
        }

        // finish the last started page after completing drawing
        pdfDocument?.finishPage(page)
    }

    private fun closePdfDocumentStream() {
        pdfDocument?.close()
        pdfDocument = null
    }

    private fun releasePrintProcess(callback: WriteResultCallback?) {
        callback?.onWriteCancelled()
        closePdfDocumentStream()
    }

    private fun getPrintPagesCount(itemsPerPage: Int = 12): Int {
        // utilize portrait print only
        val printItemCount = getPrintItemCount()

        return ceil(printItemCount / itemsPerPage.toDouble()).toInt()
    }

    private fun getPrintItemCount(): Int {
        return printList?.size ?: 0
    }
}