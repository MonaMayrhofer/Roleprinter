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
        itemJobs = itemsJobFile.toFile().inputStream().bufferedReader().useLines { it.map { line ->
            if(line.startsWith("--"))
                return@map null

            if(line.trim().isEmpty())
                return@map null

            if(line.contains("^[0-9]+x .+".toRegex())){
                val nbr = line.substringBefore("x ").toInt()
                val name = line.substringAfter("x ").trim()
                if(name.isEmpty())
                    null
                else
                    ItemJob(name, nbr)
            }else{
                ItemJob(line.trim(), 1)
            }
        }.filterNotNull().toList() }
    }
}