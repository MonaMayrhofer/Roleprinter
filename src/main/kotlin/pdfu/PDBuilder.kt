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
import com.itextpdf.layout.element.*
import com.itextpdf.layout.font.FontProvider
import com.itextpdf.layout.font.FontSet
import com.itextpdf.layout.property.AreaBreakType
import com.itextpdf.layout.property.Property
import com.itextpdf.layout.property.UnitValue
import java.nio.file.Paths


fun cmToPt(cm: Float): Float {
    return 0.393701f*cm*72f
}

fun document(fontList: List<String>? = null, content: Pair<Document, PdfDocument>.()->Unit){
    val w = pdfu.cmToPt(6.35f)
    val h = pdfu.cmToPt(8.89f)
    val margin = pdfu.cmToPt(0.3f)
    val pdfDoc = PdfDocument(PdfWriter("out.pdf"))
    val doc = Document(pdfDoc, PageSize(w, h))
    doc.setMargins(margin, margin, margin, margin)

    if(fontList != null){
        doc.fontProvider = FontProvider(FontSet())
        print("Loading fonts: ")
        fontList.forEach {
            print(".")
            doc.fontProvider.addFont(it)
        }
        println()
        println("Fonts: ")
        doc.fontProvider.fontSet.fonts.groupBy { it.descriptor.familyNameLowerCase }.forEach { family, fonts ->
            println(" - '$family':")
            fonts.forEach {
                println("       - ${it.descriptor.fontNameLowerCase}")
            }
        }
    }

    doc.use {
        Pair(doc, pdfDoc).content()
    }
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

fun Table.cell(content: IBlockElement, border: Border? = null): Cell{
    val cell = Cell()
    cell.add(content)
    if(border == null)
        cell.setBorder(this.getProperty(Property.BORDER))
    else
        cell.setBorder(border)
    addCell(cell)
    return cell
}

fun Table.cell(content: String, border: Border? = null): Cell{
    return cell(Paragraph(content), border)
}

fun Table.cell(border: Border? = Border.NO_BORDER, content: Cell.()->Unit): Cell{
    val cell = Cell()
    cell.content()
    cell.setBorder(border)
    addCell(cell)
    return cell
}