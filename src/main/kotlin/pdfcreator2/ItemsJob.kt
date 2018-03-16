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

import java.nio.file.Path

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

class ItemsJob(itemsJobFile: Path) {

    val itemJobs: List<ItemJob>

    init {

        var currentCategory: String? = null

        val quantifierRegex = "^[0-9]+ ?x .+".toRegex()
        itemJobs = itemsJobFile.toFile().inputStream().bufferedReader().useLines { it.map { rawLine ->
            val line = rawLine.trim()
            if(line.startsWith("--"))
                return@map null

            if(line.isEmpty())
                return@map null

            if(line.endsWith("{")){
                if(currentCategory != null){
                    throw IllegalStateException("Cannot open Brackets inside of brackets!")
                }
                currentCategory = line.substringBefore("{")
                return@map null
            }

            if(line.endsWith("}")){
                if(currentCategory == null){
                    throw IllegalStateException("Cannot close Brackts before opening one.")
                }
                currentCategory = null
                return@map null
            }
            if(line.contains(quantifierRegex)){
                val nbr = line.substringBefore("x ").trim().toInt()
                val name = line.substringAfter("x ").trim()
                if(name.isEmpty())
                    null
                else
                    ItemJob((currentCategory?.plus(" ")?:"") + name, nbr)
            }else{
                ItemJob((currentCategory?.plus(" ")?:"") + line, 1)
            }
        }.filterNotNull().toList() }
    }
}