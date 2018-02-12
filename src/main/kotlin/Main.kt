import org.xhtmlrenderer.pdf.ITextRenderer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


data class ItemPos(val amount: Int, val name: String, val props: Map<String, String>, val description: String)

@Throws(Exception::class)
fun main(args: Array<String>) {

    val itemListFile = Paths.get("itemlist.txt")
    val posses = Files.lines(itemListFile).map {
        val fields = it.split("x")
        val amt = fields[0].toInt()
        val itemname = fields[1].trim()

        val lines = Files.readAllLines(Paths.get("items/$itemname.json"))
        val name = lines[0]
        val props: MutableMap<String, String> = HashMap()

        var descIndex = 0
        for(i in 1..lines.size){
            if(!lines[i].contains(":")){
                descIndex = i
                break
            }
            val linefields = lines[i].split(":")
            props[linefields[0]] = linefields[1]
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
    }
}

private fun newPageHtml(item: ItemPos): String {
    val bld = StringBuilder()
    bld.append("""
        <html>
            <head>
                <style>
                    @page{
                        size: 63.5mm 88.9mm;
                        margin: 0;
                    }
                    h1{
                        width: 100%;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <h1>${item.name}</h1>
                <table> """)
            for (a in item.props) {
                bld.append(
                """
                <tr>
                    <td>
                        ${a.key}
                    </td>
                    <td>
                        ${a.value}
                    </td>
                </tr>
                """)
            }
    bld.append(
     """
            </table>
            <p>
                ${item.description}
            </p>
        </body>
    </html>
    """)

    return bld.toString()
}