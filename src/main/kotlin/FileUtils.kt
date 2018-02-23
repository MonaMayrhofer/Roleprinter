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


import java.io.File
import java.nio.file.Paths

/**
 * Recursively loop through all children
 */
fun File.traverseFiles(filter: (File) -> Boolean, callback: (File) -> Unit) {
    if(!this.isDirectory){
        if(filter(this))
            callback(this)
    }else {
        this.list().forEach {
            val child = Paths.get("${this.absolutePath}/$it").toFile()
            child.traverseFiles(filter, callback)
        }
    }
}

class FileStackEntry(val file: File): Iterator<File>{
    var index = 0

    override fun hasNext(): Boolean =
        file.listFiles() != null && index < file.listFiles().size

    override fun next(): File =
        file.listFiles()[index++]
}

class RecursiveFileChildrenIterator(parentFile: File) : Iterator<File>{

    val fileStack = ArrayList<FileStackEntry>()

    init {
        fileStack.add(FileStackEntry(parentFile))
    }

    override fun hasNext(): Boolean {
        return fileStack.count() > 0 && fileStack.any { it.hasNext() }
    }

    override fun next(): File {
        while(!fileStack.last().hasNext()){
            fileStack.removeAt(fileStack.lastIndex)
        }
        val file = fileStack.last().next()
        if(file.isDirectory){
            fileStack.add(FileStackEntry(file))
        }
        return file
    }
}

fun File.traverseFiles() : Iterable<File>{
    return Iterable {
        RecursiveFileChildrenIterator(this)
    }
}