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

package pdfu

import com.itextpdf.text.DocListener
import com.itextpdf.text.Element
import com.itextpdf.text.Rectangle

open class DocListenerAdapter : DocListener {
    override fun open() = Unit

    override fun setPageCount(pageN: Int) = Unit

    override fun setMargins(marginLeft: Float, marginRight: Float, marginTop: Float, marginBottom: Float): Boolean = true

    override fun setMarginMirroring(marginMirroring: Boolean): Boolean = true

    override fun setMarginMirroringTopBottom(marginMirroringTopBottom: Boolean): Boolean = true

    override fun setPageSize(pageSize: Rectangle?): Boolean = true

    override fun newPage(): Boolean = true

    override fun resetPageCount() = Unit

    override fun add(element: Element?): Boolean = true

    override fun close() = Unit
}