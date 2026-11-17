package com.pdf.epub.converter

import android.content.Context
import android.graphics.Bitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PdfToEpubConverter(private val context: Context) {

    fun convertPdfToEpub(pdfFile: File, outputEpubFile: File, onProgress: (String) -> Unit): Boolean {
        try {
            onProgress("Initializing PDF Document...")
            com.tom_roush.pdfbox.util.PDFBoxResourceLoader.init(context)

            val document = PDDocument.load(pdfFile)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)

            onProgress("Extracting images and formatting structure...")
            val pdfRenderer = PDFRenderer(document)
            val imageFiles = mutableListOf<File>()

            // Render first few pages as images to preserve diagrams/headings if needed
            val pagesToCapture = minOf(document.numberOfPages, 5)
            for (i in 0 until pagesToCapture) {
                val bitmap = pdfRenderer.renderImageWithDPI(i, 150f, PDFRenderer.RenderType.RGB)
                val imgFile = File(context.cacheDir, "page_${i + 1}.png")
                val fos = FileOutputStream(imgFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
                imageFiles.add(imgFile)
            }

            document.close()

            onProgress("Building EPUB container...")
            createEpubZip(outputEpubFile, text, imageFiles)

            onProgress("Conversion completed successfully!")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress("Error: ${e.localizedMessage}")
            return false
        }
    }

    private fun createEpubZip(epubFile: File, contentText: String, images: List<File>) {
        val zipOut = ZipOutputStream(BufferedOutputStream(FileOutputStream(epubFile)))

        // 1. mimetype (must be first and uncompressed)
        val mimeEntry = ZipEntry("mimetype")
        mimeEntry.method = ZipEntry.STORED
        val mimeBytes = "application/epub+zip".toByteArray(Charsets.US_ASCII)
        mimeEntry.size = mimeBytes.size.toLong()
        val crc = java.util.zip.CRC32()
        crc.update(mimeBytes)
        mimeEntry.crc = crc.value
        zipOut.putNextEntry(mimeEntry)
        zipOut.write(mimeBytes)
        zipOut.closeEntry()

        // 2. META-INF/container.xml
        zipOut.putNextEntry(ZipEntry("META-INF/container.xml"))
        val containerXml = """<?xml version="1.0"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>"""
        zipOut.write(containerXml.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        // 3. OEBPS/content.opf
        zipOut.putNextEntry(ZipEntry("OEBPS/content.opf"))
        val opfContent = """<?xml version="1.0" encoding="utf-8"?>
<package xmlns="http://www.idpf.org/2007/opf" unique-id="bookid" version="2.0">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>Converted PDF Document</dc:title>
    <dc:creator>PDFtoEPUB Converter</dc:creator>
    <dc:language>en</dc:language>
    <dc:identifier id="bookid">urn:uuid:12345-67890-54321</dc:identifier>
  </metadata>
  <manifest>
    <item id="chapter1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
  </manifest>
  <spine toc="ncx">
    <itemref idref="chapter1"/>
  </spine>
</package>"""
        zipOut.write(opfContent.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        // 4. OEBPS/toc.ncx
        zipOut.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
        val ncxContent = """<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
  <head>
    <meta name="dtb:uid" content="urn:uuid:12345-67890-54321"/>
    <meta name="dtb:depth" content="1"/>
    <meta name="dtb:totalPageCount" content="0"/>
    <meta name="dtb:maxPageNumber" content="0"/>
  </head>
  <docTitle><text>Converted PDF Document</text></docTitle>
  <navMap>
    <navPoint id="navPoint-1" playOrder="1">
      <navLabel><text>Start</text></navLabel>
      <content src="chapter1.xhtml"/>
    </navPoint>
  </navMap>
</ncx>"""
        zipOut.write(ncxContent.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        // 5. OEBPS/chapter1.xhtml
        zipOut.putNextEntry(ZipEntry("OEBPS/chapter1.xhtml"))
        val formattedHtml = buildHtmlContent(contentText)
        zipOut.write(formattedHtml.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        zipOut.close()
    }

    private fun buildHtmlContent(text: String): String {
        val escapedText = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val paragraphs = escapedText.split("\n\n").joinToString("\n") { "<p>$it</p>" }
        return """<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Converted Document</title>
<style>
  body { font-family: sans-serif; margin: 1em; line-height: 1.6; color: #333; }
  h1, h2, h3 { color: #1a73e8; margin-top: 1.5em; }
  p { margin-bottom: 1em; text-align: justify; }
</style>
</head>
<body>
<h1>Document Content</h1>
$paragraphs
</body>
</html>"""
    }
}
