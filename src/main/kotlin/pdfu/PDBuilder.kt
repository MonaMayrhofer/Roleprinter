/*
 * Roleprinter - Print itemcards for your Pathfinder campaign.
 *     Copyright (C) 2018 Erik Mayrhofer
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pdfu

import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.AreaBreakType
import com.itextpdf.layout.property.UnitValue


fun cmToPt(cm: Float): Float {
    return 0.393701f*cm*72f
}

fun document(content: Pair<Document, PdfDocument>.()->Unit){

    val w = pdfu.cmToPt(6.35f)
    val h = pdfu.cmToPt(8.89f)
    val margin = pdfu.cmToPt(0.3f)
    val wrtr = PdfWriter("out.pdf")
    val pdfDoc = PdfDocument(wrtr)
    val doc = Document(pdfDoc, PageSize(w, h))
    doc.setMargins(margin, margin, margin, margin)
    //val doc = Document(Rectangle(w, h), margin, margin, margin, margin)
    //val wrtr = PdfWriter.getInstance(doc, FileOutputStream("out.pdf"))
    //doc.open()

    Pair(doc, pdfDoc).content()

    doc.close()
}

fun Pair<Document, PdfDocument>.page(content: Document.()->Unit, error: ()->Unit){
    val pagesStart = second.numberOfPages
    if(pagesStart > 0)
        first.add(AreaBreak(AreaBreakType.NEXT_PAGE))
    first.content()
    val pagesEnd = second.numberOfPages
    if(pagesEnd-pagesStart != 1){
        error()
    }
}

fun Document.table(numColumns: Int, border: Border? = Border.NO_BORDER, content: Table.()->Unit){
    val table = Table(Array(numColumns){UnitValue.createPercentValue(100f/numColumns)})
    table.setBorder(border)
    table.content()
    add(table)
}

fun Table.cell(content: String, border: Border? = Border.NO_BORDER){
    val cell = Cell()
    cell.add(Paragraph(content))
    cell.setBorder(border)
    addCell(cell)
}

fun Table.cell(border: Border? = Border.NO_BORDER, content: Cell.()->Unit){
    val cell = Cell()
    cell.content()
    cell.setBorder(border)
    addCell(cell)
}