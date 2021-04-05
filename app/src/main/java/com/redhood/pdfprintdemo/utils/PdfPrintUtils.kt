package com.redhood.pdfprintdemo.utils

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.view.View
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.PathUtils
import java.io.File
import java.io.FileOutputStream

object PdfPrintUtils {


    fun generatePdfManually(contentView: View) {

        val outFileDir = File(PathUtils.getExternalAppFilesPath(), "/pdf_export").also { dir ->
            if (!dir.exists()) {
                dir.mkdir()
            }
        }
        val outFile = File(outFileDir, "export_document.pdf").also { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        // create a new document
        val document = PdfDocument()
        with(document) {
            drawPage(generatePageInfo(pageNumber = 1)) {
                contentView.draw(it.canvas)
            }
            drawPage(generatePageInfo(pageNumber = 2)) {
                contentView.draw(it.canvas)
            }
            drawPage(generatePageInfo(pageNumber = 3)) {
                val paint = Paint().apply {
                    color = Color.BLACK
                    alpha = 90
                }
                it.canvas.drawCircle(200F, 200F, 200F, paint)
            }
            writeTo(FileOutputStream(outFile))
            close()
        }

    }

    /**
     * create a page description
     * @param pageWidth Int
     * @param pageHeight Int
     * @param pageNumber Int Page number of pdf document that should start from 1.
     * @return PdfDocument.PageInfo
     */
    private fun generatePageInfo(
        pageWidth: Int = ConvertUtils.dp2px(400F),
        pageHeight: Int = ConvertUtils.dp2px(400F),
        pageNumber: Int
    ): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()

    private fun PdfDocument.drawPage(
        pageInfo: PdfDocument.PageInfo,
        onDrawPage: (PdfDocument.Page) -> Unit
    ) {
        val page = startPage(pageInfo)
        onDrawPage(page)
        finishPage(page)
    }


}