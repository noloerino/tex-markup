import com.jhshi.markup.*
import java.io.File
import java.util.Arrays

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
    var tokens = wordsToTokens(File("example.xml"))
    var tagmd = StringBuilder()
    var outs = StringBuilder()
    for (token in tokens) {
        tagmd.append(token.toString())
    }
    initConfig("./config.json")
    outs.append(buildHeader())
    while (tokens.size != 0) {
    	val token = tokens[0]
    	outs.append(token.eval(tokens, ParseEnv.LITERAL))
    }
    File("tagmd.txt").bufferedWriter().use {
        o -> o.write(tagmd.toString())
    }
    File("out.tex").writeText(outs.toString(), Charsets.US_ASCII) // needs to be utf-8 encoded
}
