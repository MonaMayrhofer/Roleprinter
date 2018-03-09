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
import items.ItemTrank
import pdfu.document
import pdfu.page
import java.nio.file.Path


class PdfCreator2(itemListFileName: Path) {

    val itemsJob: ItemsJob = ItemsJob(itemListFileName)

    val cards: List<Card>

    init {
        val startMillis = System.currentTimeMillis()

        ItemManager.registerLoader("Trank", ItemTrank)

        cards = itemsJob.itemJobs.flatMap {job ->
            val fields = job.itemName.split(" \\(|\\) ".toRegex(), limit = 3)
            val itemDescriptor = if(fields[0] == "Trank"){
                val properties = fields[1].replace(".","").split(", ").mapNotNull {
                    val propertyFields = it.split(" ")
                    val value = propertyFields[1].toIntOrNull()
                    if(value == null) null
                    else
                    propertyFields[0] to propertyFields[1].toInt()
                }.toMap()
                ItemTrank.ItemTrankDescriptor(job.itemName, "Trank", properties["ZS"]!!, properties["Grad"]!!)
            }else{
                ItemDescriptor(job.itemName, "")
            }

            val item = ItemManager[itemDescriptor]

            Array(job.itemAmount){
                Card(item)
            }.asIterable()
        }

        document {
            cards.forEach {
                page({
                    it.genPdf(this)
                }, {
                    throw Exception("'${it.item.itemName}' in '${it.item.filename}' takes to much space!")
                })
            }
        }

        println("PdfCreator finished in ${System.currentTimeMillis()-startMillis}ms.")
    }
}