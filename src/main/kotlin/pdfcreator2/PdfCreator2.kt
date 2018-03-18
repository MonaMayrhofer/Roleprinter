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

    fun getItemDescriptor(category: String, properties: String, rawName: String): ItemDescriptor{
        val priceRegex = "\\d+ [GSKPC][MP]|[gskpc][mp]".toRegex()
        val weightRegex = "[\\d½]+ [Pp]fd.?".toRegex()
        val footNoteRegex = "\\d\$".toRegex()

        val name = rawName.
                replace(priceRegex, "")
                .replace(weightRegex, "")
                .replace(footNoteRegex, "")
                .substringBefore("[").trim()
        //println("$rawName -> $name")
        val propertyMap = properties.replace(".", "").split(", ").mapNotNull {
            val propertyFields = it.split(" ")
            if(propertyFields.size == 1)
                return@mapNotNull "Typ" to propertyFields[0]
            val value = propertyFields[1].toIntOrNull()
            if (value == null) null
            else
                propertyFields[0] to propertyFields[1].toInt()
        }.toMap()

        try {
            val itemClass = Class.forName("items.Item${category}\$Item${category}Descriptor").kotlin
            val constructor = itemClass.constructors.first()
            val paramterPair = constructor.parameters.partition { it.findAnnotation<ItemProperty>() == null }
            val constructorParameters = paramterPair.first.map {
                it to when (it.name) {
                    "name" -> name
                    "category" -> category
                    else -> throw Exception("${itemClass.simpleName}-Constructor has unparsable Parameter ${it.name}")
                }
            }.union(paramterPair.second.map {
                it to propertyMap[it.findAnnotation<ItemProperty>()!!.name]
            }).toMap()

            return constructor.callBy(constructorParameters) as ItemDescriptor
        }catch (e: ClassNotFoundException){
            throw Exception("Now valid ItemDescriptor was found for '$category'. Searched @ '${e.message}'", e)
        }
    }

    init {
        val startMillis = System.currentTimeMillis()

        //Parse itemlist.txt into cards
        cards = itemsJob.itemJobs.flatMap {job ->
            val categorySplit = job.itemName.split(" ", limit = 2)
            val category = categorySplit[0]
            val properties = job.itemName.substringBeforeLast(")").substringAfter("(")
            val rest = categorySplit[1].split("\\(.*\\)".toRegex())

            val itemDescriptor = getItemDescriptor(category, properties, rest.joinToString(separator = ""))
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