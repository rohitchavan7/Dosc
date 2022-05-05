package com.r.dosc.presentation.viewer

import androidx.lifecycle.ViewModel
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class PdfDocViewerViewModel
@Inject constructor(
    @Named("temp") private val tempDirectory: File,
) : ViewModel() {

    init {
        val errorFile = "$tempDirectory/temp_error"
        val errorFileDir = File(errorFile)

        if (!errorFileDir.exists()) {
            errorFileDir.mkdirs()
        }

        createErrorPdfFile(errorFile)

    }


    private fun createErrorPdfFile(path: String) {
        val document = Document()

        val writer: PdfWriter = PdfWriter.getInstance(document, FileOutputStream("$path/error.pdf"))

        document.open()

        val f = Font(Font.FontFamily.COURIER, 22.0f, Font.BOLD)

        val text = Chunk("This document is corrupted or damaged!", f)
        val paragraph = Paragraph(text)
        paragraph.alignment = Element.ALIGN_CENTER

        document.add(paragraph)

        writer.isPageEmpty = false

        document.close()
    }

    fun getErrorFile(): File = File("$tempDirectory/temp_error/error.pdf")

    override fun onCleared() {
        super.onCleared()
        File("$tempDirectory/temp_error").deleteRecursively()
    }

}


private fun addTextToPdf(writer: PdfWriter) {
    try {
        val cb: PdfContentByte = writer.directContent
        val bf = BaseFont.createFont(BaseFont.COURIER_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)

        cb.saveState()
        cb.beginText()
        cb.moveText(writer.pageSize.width/8, writer.pageSize.height/2)
        cb.setFontAndSize(bf, 22f)
        cb.showText("This document is corrupted or damaged!")
        cb.endText()
        cb.restoreState()
    } catch (e: DocumentException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}