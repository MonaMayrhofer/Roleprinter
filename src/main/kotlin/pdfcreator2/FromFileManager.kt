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
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

abstract class FromFileManager<K, T>(private val startDirectory: Path) : LazyManager<K, T>() {

    constructor(startDirectory: String) : this(Paths.get(startDirectory))

    final override fun load(name: K): T {
        return findFile(name).toFile().useLines{
            parse(it)
        }
    }

    open fun findFile(descriptor: K): Path{
        val candidates = startDirectory.toFile().walkTopDown().filter {
            accept(descriptor, it)
        }
        if(candidates.count() > 1){
            throw IOException("File $descriptor is ambiguously defined")
        }
        if(candidates.count() < 1){
            throw IOException("File $descriptor couldn't be found")
        }
        return candidates.first().toPath()
    }

    abstract fun accept(descriptor: K, file: File): Boolean

    abstract fun parse(lines: Sequence<String>): T
}