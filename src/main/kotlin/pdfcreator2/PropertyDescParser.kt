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

data class Description(val text: String, val name: String?)

object PropertyDescParser {
    fun parsePropertyDesc(lines: Sequence<String>, expected: List<String>, name: String, standardDescription: String): Pair<Map<String, String>, Map<String, Description>> {
        val startMillis = System.currentTimeMillis()

        val headerSplitRegex = "\\n(?= *--- *\\w+(:? ?\\[.*\\])? *--- *)".toRegex()
        val headerRegex = " *--- *\\w+(:? ?\\[.*\\])? *--- *".toRegex()
        val headerNameSplitRegex = "\\]?( *--- *)|:? *\\[".toRegex()

        val intermediate = lines.filterNot { it.trim().startsWith("#") }.flatMap { line ->
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
        var standardDesc = ""
        val descriptionMap = intermediate.second.joinToString(separator = "\n") { it.first.trim() }
                .split(headerSplitRegex)
                .map {
                    val descLines = it.split("\n")
                    val desc = descLines.subList(1, descLines.size).joinToString(" ")
                    val firstLine = descLines[0]
                    if(!firstLine.matches(headerRegex)) {
                            standardDesc = desc
                        standardDescription to Description(desc, name)
                    }else {
                        val headerFields = firstLine.split(headerNameSplitRegex).filterNot { it.isEmpty() }
                        val descCategory = headerFields[0]
                        val descName = headerFields.getOrNull(1).takeUnless { it?.trim()?.isEmpty() ?: false }
                        descCategory to Description(desc, descName)
                    }
        }.map { (name, desc) ->
                    if(desc.text.isEmpty())
                        name to Description(standardDesc, desc.name)
                    else
                    name to desc
                }.toMap()


    //TODO WHY IS THIS NOT WORKING!
        val leftFields = expected.filter { descriptionMap.containsKey(it) }
        if(leftFields.isNotEmpty()){
            throw Exception("Expected fields [$leftFields] were not filled out in '$name'")
        }

        println("PropertyDescParser parsed input '$name' in: ${System.currentTimeMillis()-startMillis}ms")
        return Pair(parts, descriptionMap)
    }
}