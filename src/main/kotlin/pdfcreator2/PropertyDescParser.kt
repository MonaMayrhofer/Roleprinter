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

object PropertyDescParser {
    fun parsePropertyDesc(lines: Sequence<String>, expected: List<String>, name: String): Pair<Map<String, String>, Map<String, SpellDescription>> {
        val startMillis = System.currentTimeMillis()

        val headerSplitRegex = "\\n(?= *--- *\\w+(:? ?\\[.*\\])? *--- *\\n)".toRegex()
        val headerRegex = " *--- *\\w+(:? ?\\[.*\\])? *--- *".toRegex()
        val headerNameSplitRegex = "\\]?( *--- *)|:? *\\[".toRegex()

        println("\n---Trank---\n".contains(headerSplitRegex))

        val intermediate = lines.flatMap { line ->
            line.split(";").filter { !it.trim().isEmpty() }.map { field ->
                val arr = field.split(":", limit = 2)
                when {
                    expected.contains(arr[0].trim()) -> arr[0].trim() to arr[1].trim()
                    arr.size > 1 -> arr[0] + ":" + arr[1] to null
                    else -> arr[0] to null
                }
            }.asSequence()
        }.partition { it.second != null }
        val parts = intermediate.first.
                mapNotNull { if(it.second==null) null else it.first to it.second!! }.toMap()
        val descriptionMap = intermediate.second.joinToString(separator = "\n") { it.first.trim() }
                .split(headerSplitRegex)
                .map {
                    println("ONEMAP!!!")
                    val descLines = it.split("\n")
                    val desc = descLines.subList(1, descLines.size).joinToString(" ")
                    val firstLine = descLines[0]
                    println("FirstLine: $firstLine")
                    if(!firstLine.matches(headerRegex)) {
                        println("Generic Description")
                        "Spell" to SpellDescription(desc, name)
                    }else {
                        val headerFields = firstLine.split(headerNameSplitRegex).filterNot { it.isEmpty() }
                        val descCategory = headerFields[0]
                        val descName = headerFields.getOrNull(1).takeUnless { it?.trim()?.isEmpty() ?: false }
                        println("Got: $descName of $descCategory")
                        descCategory to SpellDescription(desc, descName)
                    }
        }.toMap()

        println("PropertyDescParser parsed file in: ${System.currentTimeMillis()-startMillis}ms")
        return Pair(parts, descriptionMap)
    }
}