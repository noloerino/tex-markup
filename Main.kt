import markup.parser.*
import java.io.File

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

fun main(args: Array<String>) {
    var x = getWords(File("example.xml"))
    var words = StringBuilder()
    for (word in x) {
        words.append(":START:${word.replace("\n", "\\n\n")}:END:")
    }
    File("out.txt").bufferedWriter().use {
        o -> o.write(words.toString())
    }
    var fields = getMeta("./config.json", getWords(File("example.xml")))
}
