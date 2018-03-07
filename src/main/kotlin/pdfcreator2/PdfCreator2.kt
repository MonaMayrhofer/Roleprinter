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

package pdfcreator2

import com.itextpdf.io.font.FontConstants
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.element.Table
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Rectangle
import items.ItemTrank
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.text.StyleConstants

fun cmToPt(cm: Float): Float {
    return 0.393701f*cm*72f
}

fun document(content: PdfDocument.()->Unit){
    val doc = PdfDocument(PdfWriter("out.pdf"))
    doc.content()
    doc.close()
}

fun PdfDocument.page(content: PdfCanvas.()->Unit){

    val w = pdfcreator2.cmToPt(6.35f)
    val h = pdfcreator2.cmToPt(8.89f)
    val margin = pdfcreator2.cmToPt(0.3f)



    val page = addNewPage(PageSize(w, h))

    val canvas = PdfCanvas(page)

    canvas.content()

}

class PdfCreator2(itemListFileName: Path) {

    val itemsJob: ItemsJob = ItemsJob(itemListFileName)

    val cards: List<Card>

    init {
        ItemManager.registerLoader("Trank", ItemTrank)

        cards = itemsJob.itemJobs.flatMap {job ->
            val fields = job.itemName.split("/",limit=2)
            Array(1){
                Card(ItemManager[ItemDescriptor(fields.getOrNull(1)?:fields[0],if(fields.size > 1) fields[0] else "" )])
            }.asIterable()
        }


        val doc = Document()


        val str = "Hallo, 1 bims!".repeat(30)

        document {
            cards.forEach {
                page {
                }
            }
        }
    }
}