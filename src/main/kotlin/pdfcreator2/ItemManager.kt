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

import items.Item
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

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

object ItemManager : LazyManager<ItemDescriptor, Item>() {

    private val loaders: HashMap<String, ItemFactory<Item>> = HashMap()

    fun registerLoader(category: String, loader: ItemFactory<Item>){
        loaders[category] = loader
    }

    override fun load(name: ItemDescriptor): Item {

        if(!loaders.containsKey(name.category)){
            reflectLoader(name.category)
        }

        if(loaders.containsKey(name.category)) {
            return loaders[name.category]!!.load(name)
        }else{
            throw IOException("Unknown Category: ${name.category}")
        }
/*
        val files = Paths.get("items/").toFile().walkTopDown().filter { it.name == "$name.txt" }
        if(files.count() > 1){
            throw IOException("$name is ambiguously given")
        }else if(files.count() < 1){
            throw IOException("$name was not found")
        }
        val file = files.first()
        return loadItem(file)*/
    }

    fun reflectLoader(category: String){
        val loaderClass = Class.forName("items.Item${category}")
        registerLoader(category, loaderClass.kotlin.companionObjectInstance as ItemFactory<Item>)

    }
}