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



import com.itextpdf.text.pdf.BaseFont
import org.xhtmlrenderer.pdf.ITextRenderer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors


data class ItemPos(val amount: Int, val name: String, val props: List<Pair<String, String>>, val description: String)
data class Settings(val titleFont: String, val titleSize: Int, val propertyFont: String, val propertySize: Int, val propertySpacing: Int,
                    val descriptionFont: String, val descriptionSize: Int)
data class PropertyTemplate(val name: String, val needed: Boolean, val default: String?)
data class ItemTemplate(val name: String, val props: Map<String, PropertyTemplate>)

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

class PdfCreator {

    fun run(inFileName: String, outFileName: String){
        val settings = Settings("Metamorphous-Regular", 14,
                "BadScript-Regular", 11, 9,
                "ArimaMadurai-Regular", 9
        )

        //------------- LOAD ITEMS -------------
        val itemListFile = Paths.get(inFileName)

        val templates = getItemTemplates(Paths.get("templates"))
        val inputs = createHTML(getItemPositions(itemListFile, Paths.get("items"), templates), settings)
        createPdf(inputs, Paths.get(outFileName))
        println("Transformed $inFileName into $outFileName")
    }

    private fun getItemTemplates(templateDirectory: Path): Map<String, ItemTemplate> {
        return templateDirectory.toFile().traverseFiles().filter { it.extension == "tmpl" }.map {
            val props = it.readLines().map {line ->
                val short = line.substringBefore("=").trim()
                var name = line.substringAfter("=").trim()
                val needed = line.endsWith('*')
                name = name.substringBefore('*').trim()
                var default : String? = null
                if(name.contains(":")){
                    default = name.substringAfter(":")
                    name = name.substringBefore(":")
                }
                short to PropertyTemplate(name, needed, default)
            }.toMap()
            it.nameWithoutExtension to ItemTemplate(it.name, props)
        }.toMap()
    }

    /**
     * Loads the Item-Positions from the File
     */
    private fun getItemPositions(itemListFile: Path, itemDirectory: Path, templates: Map<String, ItemTemplate>): List<ItemPos>{
        return Files.lines(itemListFile).map {
            // val fields = it.split("x")
            val amt = it.substringBefore("x").toInt()
            val itemName = it.substringAfter("x").trim()
            println("Parsing: $it -> $itemName")
            val lines = Files.readAllLines(itemDirectory.resolve("$itemName.item")).iterator()
            var name = lines.nextNotEmpty()
            var template: ItemTemplate? = null
            if(name.startsWith("<") && name.endsWith(">")){
                val templatename = name.trim('<','>')
                if(!templates.containsKey(templatename)){
                    throw IllegalStateException("Template couldn't be found: $templatename")
                }
                template = templates[templatename]
                name=lines.nextNotEmpty()
            }
            val defaultFields = template?.props?.values?.filter { it.default != null }?.map { it.name to it.default!! }
            val neededFields = template?.props?.values?.filter { it.needed }?.map { it.name }?.toMutableList()
            val props: MutableList<Pair<String, String>> = ArrayList()

            var description = ""
            while(lines.hasNext()){
                val currLine = lines.next()
                if(!currLine.contains(":")){
                    description += currLine
                    break
                }
                val lineFields = currLine.split(":")
                var propname = lineFields[0]
                if(template?.props?.containsKey(propname) == true){
                    propname = template.props[propname]!!.name
                }
                neededFields?.remove(propname)
                props.add(propname to lineFields[1])
            }

            if(neededFields != null && !neededFields.isEmpty()){
                throw IllegalStateException("Not all needed Fields of ${template?.name} are satisfied: $neededFields in $itemName")
            }

            defaultFields?.forEach { (defaultField, defaultValue) ->
                if(props.none { it.first == defaultField }){
                    props.add(defaultField to defaultValue)
                }
            }

            props.sortBy { it.first }
            while (lines.hasNext())
                description += "<br/>" + lines.next()
            ItemPos(amt, name, props, description)
        }.collect(Collectors.toList())
    }

    private fun createHTML(itemPositions: List<ItemPos>, settings: Settings): List<Pair<ItemPos, String>>{
        return itemPositions.flatMap {pos ->
            Array(pos.amount) {pos to newPageHtml(pos, settings)}.toList()
        }
    }

    // --------------- PDF STUFF -------------
    private fun createPdf(inputs: List<Pair<ItemPos, String>>, outputFile: Path){
        val os = outputFile.toFile().outputStream()
        os.use {
            val renderer = ITextRenderer()

            //----------- LOAD Fonts --------------
            println("Loading Fonts from \"${Paths.get("font/").toFile().absolutePath}\": ")
            Paths.get("font/").toFile().traverseFiles().filter {
                arrayOf("otf", "ttf", "ttc").contains(it.extension)
            }.forEach{
                renderer.fontResolver.addFont(it.absolutePath, it.nameWithoutExtension, BaseFont.CP1252, true, null)
                println(" - ${it.name}")
            }


            var prevPageCount = 0

            println("Writing Items:")
            for (i in 0 until inputs.size) {
                println(" - ${(100*i/inputs.size).toString().padStart(3)}% ${inputs[i].first.name}")

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

    private fun getPropertySection(item: ItemPos): StringBuilder {
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
}