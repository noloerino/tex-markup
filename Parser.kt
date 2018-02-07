package texmarkup

import java.io.File

fun parse(src: String) {
    var tokens = wordsToTokens(File("$src.xml"))
    var outs = StringBuilder()
    /*for (token in tokens) {
        tagmd.append(token.toString())
    }*/
    initConfig("./config.json")
    outs.append(buildHeader())
    while (tokens.size != 0) {
        val token = tokens[0]
        outs.append(token.eval(tokens, ParseEnv.LITERAL))
    }
    /*File("tagmd.txt").bufferedWriter().use {
        o -> o.write(tagmd.toString())
    }*/
    File("out.tex").writeText(outs.toString())
}