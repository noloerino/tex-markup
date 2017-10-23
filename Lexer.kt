package texmarkup

import java.io.File
import java.io.BufferedReader

enum class TagEnvironment {
    // A token found immediately after the < sign
    TAG_NAME,
    // A token found within the body of a tag declaration, either a flag or a property
    TAG_DECL,
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
        if (word.trim().endsWith("\\") && !word.trim().endsWith("\\\\")) {
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

fun getWords(file: File): List<String> {
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
                            currEnv = TagEnvironment.LITERAL
                            addToken()
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
        if (currEnv == TagEnvironment.LITERAL && !token.isBlank()) {
            token.append("\n")
        }
        addToken()
    }
    return tokens
}

// TODO
fun checkIdentifierConflicts() {
// use a set
}

val TOKEN_OPEN = Regex("(?<=<)[^/]\\w*(?=\\s?.*?>)")
val TOKEN_FLAG = Regex("(?<= )(?<!=)\\w*(?=[ >])") // note: does not check that the string is within an html tag
val TOKEN_PROPERTY_STR = Regex("(?<= )\\w+=[\"\'][^\"\']*[\"\'](?=)") // see above
val TOKEN_PROPERTY = Regex("(?<= )\\w*=[^\"]\\S*(?=[ >])")
val TOKEN_CLOSE = Regex("(?<=</)\\w*(?=\\s?.*?>)")

fun wordsToTokens(file: File): MutableList<Token> {
	val tokens: MutableList<Token> = mutableListOf()
	val words = getWords(file)
	// create tokens from words
	// keeps track of what token requires closing, with most recent on the end
	val openTokenStack: MutableList<String> = mutableListOf()
	val emptyCreator = {-> UnknownTag.create("empty", arrayOf(), hashMapOf())}
	var creator = emptyCreator
	var i = 0
	while (i < words.size) {
		var word = words[i]
		if (TOKEN_OPEN.containsMatchIn(word)) {
			val tokenName: String = TOKEN_OPEN.find(word)!!.value
			val flags: Array<String> = TOKEN_FLAG.findAll(word).map { it.value }.toList().toTypedArray()
			val properties: HashMap<String, String> = getProperties(word)
			creator = when (tokenName) {
				in headerTags -> {-> HeaderTag.create(tokenName)}
				in emptyTags -> {-> EmptyTag.create(tokenName, flags, properties)}
				in lineTags -> {-> LineTag.create(tokenName, flags, properties)}
				in unclosedTags -> {-> unclosedTags[tokenName]!!(flags, properties)}
				in aliasTags -> {-> aliasTags[tokenName]!!(flags, properties)}
				in definedTags -> {-> definedTags[tokenName]!!(flags, properties)}
				else -> {-> UnknownTag.create(tokenName, flags, properties)}
			}
			if (tokenName !in unclosedTags) {
				openTokenStack.add(tokenName)
			}
			tokens.add(creator())
		}
		else if (TOKEN_CLOSE.containsMatchIn(word)) {
			val tokenName = TOKEN_CLOSE.find(word)!!.value
			/*if (tokenName != openTokenStack.last()) { // token closed improperly
				throw IllegalArgumentException("Attempted to close ${openTokenStack.last()} with $tokenName on word #$i")
			}*/
			openTokenStack.removeAt(openTokenStack.size - 1)
			tokens.add(ClosingTag(tokenName))
			creator = emptyCreator
		}
		else {
			var literal: MutableList<String> = mutableListOf()
			while (i < words.size && !TOKEN_OPEN.containsMatchIn(words[i]) && !TOKEN_CLOSE.containsMatchIn(words[i])) {
				literal.add(words[i])
				i++
			}
			i--
			tokens.add(Literal(literal))
		}
		i++
	}
	if (openTokenStack.size != 0) {
		//throw IllegalArgumentException("Unclosed token(s) ${openTokenStack.toString()}")
	}
	return tokens
}

fun getProperties(word: String): HashMap<String, String> {
	val props = TOKEN_PROPERTY.findAll(word)
	val propsStr = TOKEN_PROPERTY_STR.findAll(word)
	val map: MutableMap<String, String> = mutableMapOf()
	fun add(item: MatchResult) {
		val split = item.value.split("=", limit=2)
		if (split[1].length >= 2
				&& (split[1].startsWith('\'') || split[1].startsWith('\"')) && (split[1].endsWith('\'') || split[1].endsWith('\"'))) {
			map.put(split[0], split[1].substring(1, split[1].length - 1))
		}
		else {
			map.put(split[0], split[1])
		} //" this is to fix syntax highlighting
	}
	for (item in props) {
		add(item)
	}
	for (item in propsStr) {
		add(item)
	}
	return HashMap(map.toMap())
}
