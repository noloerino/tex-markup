package com.jhshi.markup

val definedTags = hashMapOf("matrix" to MatrixT.Companion::create,
							"prob" to Problem.Companion::create)

class Problem(flags: Array<String>, properties: HashMap<String, String>) : Tag("prob", flags, properties) {
	override val validFlags = arrayOf("nobox")
	override val validProperties = arrayOf("name")

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val pnum: Int
		if (flags.size != 0) {
			val flagNum: Int? = flags.filter { it.matches(Regex("(0|[1-9][0-9]*)")) }.map { it.toInt() }.max()
			pnum = if (flagNum == null) Problem.problems else flagNum
		}
		else {
			pnum = Problem.problems
		}
		Problem.problems++
		val name = if ("name" in properties) properties["name"] else ""
		val sb = StringBuilder("\\subsection*{$pnum. $name}")
		val multipart = tokens.any { e -> e is ProbPart }
		if (!multipart) {
			if ("nobox" !in flags) {
				sb.append("\\begin{mdframed}\\textbf{Solution}\\\\")
			}
			evalChildren(sb, tokens, currEnv)
			if ("nobox" !in flags) {
				sb.append("\\end{mdframed}\\clearpage")
			}
		}
		else {
			sb.append("\\begin{enumerate}")
			evalChildren(sb, tokens, currEnv)
			sb.append("\\end{enumerate}\\clearpage")
		}
		return sb.toString()
	}

	companion object {
		var problems = 1
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return Problem(flags, properties)
		}
	}
}

class MatrixT(flags: Array<String>, properties: HashMap<String, String>) : Tag("matrix", flags, properties) {
	override val validFlags = arrayOf("T") // TODO
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
		evalChildren(sb, tokens, currEnv)
		sb.append(endTag)
		return sb.toString()
	}

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return MatrixT(flags, properties)
		}
	}
}