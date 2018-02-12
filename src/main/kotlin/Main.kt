import com.beust.klaxon.Parser
import org.xhtmlrenderer.pdf.ITextRenderer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


data class ItemPos(val amount: Int, val name: String, val props: Map<String, String>, val description: String)

@Throws(Exception::class)
fun main(args: Array<String>) {

    val itemListFile = Paths.get("itemlist.txt")
    val parser = Parser()
    val posses = Files.lines(itemListFile).map {
        val fields = it.split("x")
        val amt = fields[0].toInt()
        val itemname = fields[1].trim()

        val lines = Files.readAllLines(Paths.get("items/$itemname.json"))
        val name = lines[0]
        val props: MutableMap<String, String> = HashMap()

        var descIndex = 0
        for(i in 0..lines.size){
            if(!lines[i].contains(":")){
                descIndex = i
                break
            }
            val linefields = lines[i].split(":")
            props[fields[0]] = fields[1]
        }

        val description = lines.drop(descIndex).joinToString()

        ItemPos(amt, name, props, description)
    }.collect(Collectors.toList())

    val inputs = posses.flatMap {pos ->
        Array(pos.amount) {pos}.toList()
    }.map {
                newPageHtml(it)
            }

    val path = Paths.get("out.pdf")
    val os = path.toFile().outputStream()
    os.use {
        val renderer = ITextRenderer()

        renderer.setDocumentFromString(inputs[0])
        renderer.layout()
        renderer.createPDF(os, false)

        for (i in 1 until inputs.size) {
            renderer.setDocumentFromString(inputs[i])
            renderer.layout()
            renderer.writeNextDocument()
        }
        renderer.finishPDF()

        println("Sample file with " + inputs.size + " documents rendered as PDF to " + os)
    }
}

private fun newPageHtml(item: ItemPos): String {
    return "<html>" +
            "    <h1>" + item.name +
            "</h1></html>"
}