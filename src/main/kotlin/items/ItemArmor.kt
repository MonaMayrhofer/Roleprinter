package items

import com.itextpdf.layout.Document
import pdfcreator2.ItemDescriptor
import pdfcreator2.ItemFactory
import pdfcreator2.PropertyDescParser
import java.nio.file.Paths

class ItemArmor(
        val name: String,
        val type: String,
        val price: String,
        val bonus: String,
        val dexbonus: String,
        val armormalus: String,
        val arcaneFail: String,
        val mov9m: String,
        val mov6m: String,
        val weight: String,
        override val filename: String) : Item("TODO") {
    override fun genPdfDesc(document: Document) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object : ItemFactory<ItemArmor>() {
        override fun load(itemDescriptor: ItemDescriptor): ItemArmor {
            val path = Paths.get("items/").toFile().walkTopDown().firstOrNull { it.nameWithoutExtension == itemDescriptor.name } ?: throw Exception("Could not find Armor file for ${itemDescriptor.name}")

            val expectedProperties = listOf(
                    "Preis", "Art", "Rüstungs-/Schildbonus", "Max. GE-Bonus", "Rüstungsmalus", "Chance für arkane Zauberpatzer", "Bewegungsrate 9m", "Bewegungsrate 6m", "Gewicht"
            )

            path.useLines {
                val (parts, description) = PropertyDescParser.parsePropertyDesc(it,expectedProperties, itemDescriptor.name, "Armor")
                return ItemArmor(
                    itemDescriptor.name,
                        parts["Art"]!!,
                        parts["Preis"]!!,
                        parts["Rüstungs-/Schildbonus"]!!,
                        parts["Max. GE-Bonus"]!!,
                        parts["Rüstungsmalus"]!!,
                        parts["Chance für arkane Zauberpatzer"]!!,
                        parts["Bewegungsrate 9m"]!!,
                        parts["Bewegungsrate 6m"]!!,
                        parts["Gewicht"]!!,
                        path.absolutePath
                )
            }
        }

    }

    class ItemArmorDescriptor(name: String, category: String) : ItemDescriptor(name, category)
}