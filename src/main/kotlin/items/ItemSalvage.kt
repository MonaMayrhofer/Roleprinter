package items

import com.itextpdf.layout.Document
import pdfcreator2.ItemDescriptor
import pdfcreator2.ItemFactory
import pdfcreator2.PRDSalvageStripper
import pdfcreator2.PropertyDescParser
import java.nio.file.Paths

class ItemSalvage(
        val price: String,
        val weight: String,
        val description: String,
        val name: String,
        override val filename: String) : Item(name) {

    override fun genPdfDesc(document: Document) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object : ItemFactory<ItemSalvage>() {
        override fun load(itemDescriptor: ItemDescriptor): ItemSalvage {
            val path = Paths.get("items/").toFile().walkTopDown().firstOrNull { it.nameWithoutExtension == itemDescriptor.name }
            if(path != null) {
                val expectedProperties = listOf(
                        "Preis", "Gewicht"
                )

                path.useLines {
                    val (parts, description) = PropertyDescParser.parsePropertyDesc(it,expectedProperties, itemDescriptor.name, "Armor")
                    return ItemSalvage(
                            parts["Preis"]!!,
                            parts["Gewicht"]!!,
                            description["Armor"]!!.text,
                            itemDescriptor.name,
                            path.absolutePath
                    )
                }
            }else{
                val item = PRDSalvageStripper.strip(itemDescriptor.name)
                PRDSalvageStripper.saveStrippedItem(item)
                return item
            }
        }

    }

    class ItemSalvageDescriptor(name: String, category: String) : ItemDescriptor(name, category)
}