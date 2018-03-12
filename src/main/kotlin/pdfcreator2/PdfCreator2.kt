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
import pdfu.document
import pdfu.page
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.full.findAnnotation


class PdfCreator2(itemListFileName: Path) {

    private val itemsJob: ItemsJob = ItemsJob(itemListFileName)

    private val cards: List<Card>

    fun getItemDescriptor(category: String, properties: String, name: String): ItemDescriptor{
        val propertyMap = properties.replace(".", "").split(", ").mapNotNull {
            val propertyFields = it.split(" ")
            val value = propertyFields[1].toIntOrNull()
            if (value == null) null
            else
                propertyFields[0] to propertyFields[1].toInt()
        }.toMap()

        val itemClass = Class.forName("items.Item${category}\$Item${category}Descriptor").kotlin
        val constructor = itemClass.constructors.first()
        val paramterPair = constructor.parameters.
                partition { it.findAnnotation<ItemProperty>() == null }
        val constructorParameters = paramterPair.first.map { it to when(it.name){
                    "name" -> name
                    "category" -> category
                    else -> throw Exception("${itemClass.simpleName}-Constructor has unparsable Parameter ${it.name}")
        } }.union(paramterPair.second.map {
            it to propertyMap[it.findAnnotation<ItemProperty>()!!.name]
        }).toMap()

        val trankDescriptor = constructor.callBy(constructorParameters) as ItemDescriptor
        return trankDescriptor
    }

    init {
        val startMillis = System.currentTimeMillis()

        //Parse itemlist.txt into cards
        cards = itemsJob.itemJobs.flatMap {job ->
            val fields = job.itemName.split(" \\(|\\) ".toRegex(), limit = 3)
            val itemDescriptor = getItemDescriptor(fields[0], fields[1], fields[2])
            val item = ItemManager[itemDescriptor]

            Array(job.itemAmount){
                Card(item)
            }.asIterable()
        }

        //Create PDF
        val fonts = Paths.get("font").toFile().walkTopDown().filter { it.extension == "ttf" }.map { it.absolutePath }.toList()
        document(fonts) {
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