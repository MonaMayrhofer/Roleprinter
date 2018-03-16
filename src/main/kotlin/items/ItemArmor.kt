package items

import com.itextpdf.layout.Document
import pdfcreator2.ItemDescriptor

class ItemArmor(override val filename: String) : Item("TODO") {
    override fun genPdfDesc(document: Document) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class ItemArmorDescriptor(name: String, category: String) : ItemDescriptor(name, category)
}