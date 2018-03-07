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

package util

fun <T> Iterator<T>.next(predicate: (T)->Boolean): T? {
    while(hasNext()) {
        val curr = next()
        if(predicate(curr))
            return curr
    }
    return null
}

fun Iterator<String>.nextTrimmed(): String{
    return next().trim()
}

fun Iterator<String>.nextNotEmpty(): String{
    do{
        val line = nextTrimmed()
        if(!line.isEmpty()){
            return line
        }
    }while(true)
}