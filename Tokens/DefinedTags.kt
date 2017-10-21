package com.jhshi.markup

val definedTags = hashMapOf("matrix" to MatrixT.Companion::create)

class MatrixT(flags: Array<String>, properties: HashMap<String, String>) : Tag("matrix", flags, properties) {

	override val validFlags = arrayOf("T")
	override val validProperties = arrayOf("augment", "brackets")

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		if (currEnv != ParseEnv.MATH_LITERAL) {
			return wrapInMath(false, tokens, currEnv)
		}
 		var sb = StringBuilder()
		// Does some weird stuff here
		var tagType = "bmatrix"
		var openTag = "\\begin{$tagType}"
		var endTag = "\\end{$tagType}"
		if ("brackets" in properties) {
			tagType = "array"
			openTag = "\\begin{$tagType}"
			endTag = "\\end{$tagType}"
			val paren = listOf(")", "(", "paren", "parentheses")
			val sqbr = listOf("]", "[", "sq", "square")
			val pipe = listOf("|", "det", "pipe")
			var leftChar = when (properties["brackets"]) {
				in paren -> "("
				in pipe -> "|"
				in sqbr -> "["
				else -> "["
			}
			var rightChar = when(properties["brackets"]) {
				in paren -> ")"
				in pipe -> "|"
				in sqbr -> "]"
				else -> "]"
			}
			openTag = "\\left$leftChar" + openTag
			endTag = endTag + "\\right$rightChar"
		}
		if ("augment" in properties) {
			tagType = "array"
		}
		else if (tagType == "array") {

		} // TODO figure out how to figure out proper line length
		sb.append(openTag)
		// Since commas are used as delimiters for ease, 
		for (c in tokens) {
			if (c is Literal) {
				// TODO escaped commas
				c.content = c.content.map { it.replace(',', '`') }
			}
		}
		while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, currEnv))
        }
		sb.append(endTag)
		return sb.toString()
	}

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return MatrixT(flags, properties)
		}
	}
}