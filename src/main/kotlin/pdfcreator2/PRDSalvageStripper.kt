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

import com.github.kittinunf.fuel.httpGet
import items.ItemSalvage
import org.jsoup.Jsoup
import java.nio.charset.Charset
import java.nio.file.Paths
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object PRDSalvageStripper {
    fun strip(name: String): ItemSalvage{
        println("Stripping $name")
        val webUrl = "http://prd.5footstep.de/Grundregelwerk/Ausruestung/$name"
        Jsoup.connect(webUrl).get().run {
            val page = select("#page.page")
            val props = page.select("table td").drop(1).map { it.html().trim() }
            val desc = page.select("p").html()
                    .substringAfter("</strong>")
                    .substringBefore("<br>")
                    .replace("&nbsp;"," ")
                    .replace("<a .*\" *>".toRegex(), "")
                    .replace("</a>","")
                    .trim()
            return ItemSalvage(props[0], props[1], desc, name, webUrl)
        }
    }
    fun saveStrippedItem(item: ItemSalvage){
        val path = Paths.get("items/stripped/${item.itemName}.item").toFile()
        println(path)
        if(path.exists())
            throw Exception("File ${path.absolutePath} already exists. Don't saving...")
        Paths.get("items/stripped").toFile().mkdirs()
        path.printWriter().use {
            with(it){
                println("#Generated using PRDSalvageStripper on ${LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} from ${item.filename}")
                println("Preis: ${item.price}")
                println("Gewicht: ${item.weight}")
                println()
                println(item.description)
            }
        }
    }
}