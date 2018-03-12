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

package items

import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import pdfcreator2.*

data class ItemTrank(val spell: Spell, val level: Int, val degree: Int, override val filename: String): Item(
        spell["Trank"].name ?: spell["Spell"].name!!) { //TODO Make better Trank-Names

    override fun genPdfDesc(document: Document) {
        with(document){
            propertyTable(
                    "Zauberstufe" to level.toString(),
                    "Zaubergrad" to degree.toString(),
                    "Dauer" to spell.duration
            )
            add(Paragraph(spell["Trank"].text).setFontSize(9f).setFont("fondamento").setFixedLeading(10f))
        }
    }

    companion object : ItemFactory<ItemTrank>() {
        override fun load(itemDescriptor: ItemDescriptor): ItemTrank {
            val trankDescrptor = itemDescriptor as ItemTrankDescriptor
            val zaubername = itemDescriptor.name.substringAfterLast(") ").replace(" \\d+ GM".toRegex(), "")
            return ItemTrank(SpellManager[zaubername], trankDescrptor.level, trankDescrptor.degree, "Zauber: $zaubername")
        }
    }

    class ItemTrankDescriptor(name: String, category: String,
                              @ItemProperty("ZS") val level: Int,
                              @ItemProperty("Grad") val degree: Int): ItemDescriptor(name, category)
}