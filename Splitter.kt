package markup.parser

import markup.tokens.*
import java.io.File
import java.io.BufferedReader

enum class TagEnvironment {
    // A token found immediately after the < sign
    TAG_NAME,
    // A token found within the body of a tag declaration, either a flag or a property
    TAG_DECL,
    // A token (such as <item> or <node>) that signals the rest of the line should be included in this token
    LINE_TAG,
    // A token found within the body of a tag, to be interpreted literally by the LaTeX compiler
    LITERAL,
    // A comment. Should be dropped after the tokenization process.
    COMMENT
}

// If the end of a line is a backslash character, joins this line to the next
private fun parseLineJoins(words: List<String>): List<String> {
    var newWords: MutableList<String> = mutableListOf()
    var i = 0
    while (i < words.size) {
        var word = words[i]
        if (word.trim().endsWith("\\")) {
            try {
                newWords.add(word + words[i + 1])
            }
            catch(e: IndexOutOfBoundsException) {
                println("Illegal line join character at line $i")
                throw e
            }
        }
        else {
            newWords.add(word)
        }
        i++
    }
    return newWords
}

private fun getWords(file: File): List<String> {
    val br = file.bufferedReader()
    var text = parseLineJoins(br.readLines())
    val TAG_OPEN = '<'
    val TAG_CLOSE = '>'
    var tokens: MutableList<String> = mutableListOf()
    var currEnv: TagEnvironment = TagEnvironment.LITERAL
    var token: StringBuilder = StringBuilder()
    var tagName: StringBuilder = StringBuilder()
    fun addToken() {
        if (!token.toString().isBlank()) {
            tokens.add(token.toString())
            token = StringBuilder()
        }
    }
    for (rawLine in text) {
        var line = rawLine.trim()
        reader@ for (c in line) {
            when (currEnv) {
                TagEnvironment.LINE_TAG -> {
                    token.append(c)
                }
                TagEnvironment.COMMENT -> {
                    token.append(c)
                    if (c == TAG_CLOSE && token.endsWith("--" + TAG_CLOSE)) {
                        token = StringBuilder()
                        currEnv = TagEnvironment.LITERAL
                    }
                }
                TagEnvironment.TAG_NAME, TagEnvironment.TAG_DECL -> {
                    token.append(c)
                    if (token.toString().trim().startsWith(TAG_OPEN + "!--")) {
                        currEnv = TagEnvironment.COMMENT
                        continue@reader
                    }
                    if (currEnv == TagEnvironment.TAG_NAME) {
                        tagName.append(c)
                    }
                    when (c) {
                        ' ' -> {
                            currEnv = TagEnvironment.TAG_DECL
                        }
                        TAG_CLOSE -> {
                            if (tagName.toString().substring(1, tagName.length - 1) in lineTags) {
                                currEnv = TagEnvironment.LINE_TAG
                            }
                            else {
                                currEnv = TagEnvironment.LITERAL
                                addToken()
                            }
                            tagName = StringBuilder()
                        }
                        TAG_OPEN -> {
                            throw IllegalArgumentException("Cannot nest tag declarations")
                        }
                        else -> {}
                    }
                }
                else -> {
                    if (c == TAG_OPEN) {
                        if (token.length != 0) {
                            addToken()
                        }
                        tagName = StringBuilder(TAG_OPEN.toString())
                        currEnv = TagEnvironment.TAG_NAME
                    }
                    token.append(c)
                }
            } 
        }
        if (currEnv == TagEnvironment.LINE_TAG) {
            currEnv = TagEnvironment.LITERAL
            tagName = StringBuilder()
        }
        if (currEnv == TagEnvironment.LITERAL && !token.isBlank()) {
            token.append("\n")
        }
        addToken()
    }
    return tokens
}

fun wordsToTokens(file: File): List<Token> {
	val TOKEN_EXPR = Regex("<*>")

	return listOf()
}