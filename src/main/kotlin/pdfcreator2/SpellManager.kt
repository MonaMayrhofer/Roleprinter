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

import java.io.File

object SpellManager : FromFileManager<String, Spell>("zauber") {
    override fun accept(descriptor: String, file: File): Boolean {
        return descriptor.equals(file.nameWithoutExtension, true)
    }

    override fun parse(lines: Sequence<String>, name: String): Spell {
        val expected = listOf(
                "Schule",
                "Grad",
                "Zeitaufwand",
                "Komponenten",
                "Reichweite",
                "Ziel",
                "Wirkungsdauer",
                "Rettungswurf",
                "Zauberresistenz")

        val (parts, description) = PropertyDescParser.parsePropertyDesc(lines, expected, name)

        return Spell(
                name,
                parts["Schule"]!!,
                "TODO",
                parts["Grad"]!!,
                parts["Zeitaufwand"]!!,
                parts["Komponenten"]!!,
                parts["Reichweite"]!!,
                parts["Ziel"]!!,
                parts["Wirkungsdauer"]!!,
                parts["Rettungswurf"]!!,
                parts["Zauberresistenz"]!!,
                description)
    }
}