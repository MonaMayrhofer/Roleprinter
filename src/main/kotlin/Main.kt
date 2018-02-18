import com.itextpdf.text.pdf.BaseFont
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors


data class ItemPos(val amount: Int, val name: String, val props: List<Pair<String, String>>, val description: String)
data class Settings(val titleFont: String, val titleSize: Int, val propertyFont: String, val propertySize: Int, val propertySpacing: Int,
                    val descriptionFont: String, val descriptionSize: Int)


fun main(args: Array<String>) {
    val settings = Settings("Metamorphous-Regular", 14,
            "BadScript-Regular", 11, 9,
            "ArimaMadurai-Regular", 9
            )

    //------------- LOAD ITEMS -------------
    val itemListFile = Paths.get("itemlist.txt")

    val inputs = createHTML(getItemPositions(itemListFile, Paths.get("items")), settings)

    createPdf(inputs, Paths.get("out.pdf"))
}

/**
 * Recursively loop through all children
 */
fun File.traverseFiles(filter: (File) -> Boolean, callback: (File) -> Unit) {
    if(this.isDirectory){
        if(filter(this))
            callback(this)
    }else {
        this.list().forEach {
            val child = Paths.get("${this.absolutePath}/$it").toFile()
            child.traverseFiles(filter, callback)
        }
    }
}

/**
 * Loads the Item-Positions from the File
 */
fun getItemPositions(itemListFile: Path, itemDirectory: Path): List<ItemPos>{
    return Files.lines(itemListFile).map {
        val fields = it.split("x")
        val amt = fields[0].toInt()
        val itemName = fields[1].trim()
        val lines = Files.readAllLines(itemDirectory.resolve("$itemName.item"))
        val name = lines[0]
        val props: MutableList<Pair<String, String>> = ArrayList()

        var descIndex = 0
        for(i in 1..lines.size){
            if(!lines[i].contains(":")){
                descIndex = i
                break
            }
            val lineFields = lines[i].split(":")
            props.add(lineFields[0] to lineFields[1])
        }
        props.sortBy { it.first }

        val description = lines.drop(descIndex).joinToString()
        ItemPos(amt, name, props, description)
    }.collect(Collectors.toList())
}

fun createHTML(itemPositions: List<ItemPos>, settings: Settings): List<Pair<ItemPos, String>>{
    return itemPositions.flatMap {pos ->
        Array(pos.amount) {pos to newPageHtml(pos, settings)}.toList()
    }
}

// --------------- PDF STUFF -------------
fun createPdf(inputs: List<Pair<ItemPos, String>>, outputFile: Path){
    val os = outputFile.toFile().outputStream()
    os.use {
        val renderer = ITextRenderer()

        //----------- LOAD Fonts --------------
        Paths.get("font/").toFile().traverseFiles({
                    arrayOf("otf", "ttf", "ttc").contains(it.extension)
                }, {
                    renderer.fontResolver.addFont(it.absolutePath, it.nameWithoutExtension, BaseFont.CP1252, true, null)
                }
        )

        var prevPageCount = 0

        for (i in 0 until inputs.size) {
            println("Writing item ${inputs[i].first.name}")

            //------- Write PFD -----
            renderer.setDocumentFromString(inputs[i].second)
            renderer.layout()
            if(i == 0)
                renderer.createPDF(os, false)
            else
                renderer.writeNextDocument()

            //--------- Check -------------
            val currPageCount = renderer.rootBox.layer.pages.size
            if(currPageCount-prevPageCount > 1)
                throw Exception("Item is too long: ${inputs[i].first}")
            prevPageCount = currPageCount
        }

        renderer.finishPDF()
    }
}

//------------------- HTML STUFF --------------------

private var debugStringInt = 0
private var debug = true
private fun getDebugBorderString(): String{
    debugStringInt+=1
    val r = debugStringInt*88%255
    val g = debugStringInt*55%255
    val b = debugStringInt*22%255
    return if(!debug) "" else "border: 1px solid rgb($r,$g,$b)"
}

private fun newPageHtml(item: ItemPos, settings: Settings): String {
    val bld = StringBuilder()
    bld.append("""
        <html>
            <head>
                <style>
                    @page{
                        size: 63.5mm 88.9mm;
                        margin: 3mm;
                    }
                    h1{
                        padding: 0px;
                        margin: 0px;
                        font-family: ${settings.titleFont};
                        font-size: ${settings.titleSize}pt;
                        text-align: center;
                    }
                    div.prop-holder{
                        display: block;
                        position: relative;
                    }
                    div.properties{
                        display: table;
                        width: 100%;
                    }
                    div.property{
                        display: table-row;
                        padding: 0px;
                        margin: 0px;
                    }
                    div.propitem{
                        line-height: ${settings.propertySpacing}pt;
                        width: 50%;
                        display: table-cell;
                    }
                    div.propval{
                        font-family: ${settings.propertyFont};
                        font-size: ${settings.propertySize}pt;
                        vertical-align: bottom;
                        display: inline;
                    }
                    div.key{
                        padding-right: 5px;
                        text-align: right;
                    }
                    div.value{
                        padding-left: 5px;
                        text-align: left;
                    }
                    p.description{
                        font-family: ${settings.descriptionFont};
                        font-size: ${settings.descriptionSize}pt;
                        padding-top: 10px;
                        margin: 0px;
                    }
                </style>
            </head>
            <body>
                <h1>
                ${item.name}
                </h1>
                <div class="prop-holder">
                <div class="properties">
                """.trimIndent())
    bld.append(getPropertySection(item))
    bld.append(
     """
                </div> <!--Table-->
                </div>
                <p class="description">
                    ${item.description}
                </p>
            </body>
        </html>
    """.trimIndent())

    return bld.toString()
}

fun getPropertySection(item: ItemPos): StringBuilder {
    val bld = StringBuilder()
            for (a in item.props) {
                bld.append(
                """
                    <div class="property">
                        <div class="propitem key">
                            <div class="propval">
                            ${a.first}
                            </div>
                        </div>
                        <div class="propitem value">
                            <div class="propval">
                            ${a.second}
                            </div>
                        </div>
                    </div>
                """)
            }
    return bld
}
