package texmarkup

val aliasTags = hashMapOf("math" to Math.Companion::create,
						  "ilmath" to InlineMath.Companion::create,
						  "part" to ProbPart.Companion::create,
						  "i" to Italic.Companion::create,
						  "b" to Bold.Companion::create,
						  "body" to DocumentT.Companion::create,
						  "box" to MDFramed.Companion::create)

abstract class AliasTag(id: String, flags: Array<String>, properties: HashMap<String, String>) : Tag(id, flags, properties) {
    constructor(id: String) : this(id, arrayOf(), hashMapOf()) { }
    abstract val texId: String

    override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        var sb = StringBuilder("\\begin{$texId}\n\t")
        evalChildren(sb, tokens, currEnv)
        sb.append("\\end{$texId}\n\t")
        return sb.toString()
    }
}

class Math() : AliasTag("math") {
	override val texId = "align*"
	
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val sb = StringBuilder("\\begin{$texId}")
        evalChildren(sb, tokens, ParseEnv.MATH_LITERAL)
        if (sb.toString().endsWith("\\\\ ")) {
        	sb.delete(sb.length - 3, sb.length)
        }
        sb.append("\\end{$texId}")
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return Math()
        }
    }
}

class InlineMath() : AliasTag("ilmath") {
	override val texId = "$"

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		var sb = StringBuilder(texId)
        evalChildren(sb, tokens, ParseEnv.MATH_LITERAL)
		sb.append(texId)
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return InlineMath()
        }
    }
}

class ProbPart(flags: Array<String>, properties: HashMap<String, String>) : AliasTag("part", flags, properties) {
	override val texId = "item"

	override val validFlags = arrayOf("pbr, nobox")
	override val validProperties = arrayOf("name")

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		var sb = StringBuilder("\\$texId ")
		sb.append(if ("name" in properties) Literal(listOf(properties["name"]!!)).eval(mutableListOf(), currEnv) else "\\\\")
		if ("nobox" !in flags) {
			sb.append("\\begin{mdframed}\\textbf{Solution}")
			if (tokens.size > 0 && !nxtEatsPrNewLine(tokens[1])) {
				sb.append("\\\\")
			}
		}
		evalChildren(sb, tokens, currEnv)
		if ("nobox" !in flags) {
			sb.append("\\end{mdframed}")
		}
		if ("pbr" in flags) {
			sb.append("\\clearpage")
		}
		return sb.toString()
	}

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return ProbPart(flags, properties)
		}
	}
}

class Italic() : AliasTag("i") {
	override val texId = "textit"

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		var sb = StringBuilder("\\$texId{")
		evalChildren(sb, tokens, currEnv)
		sb.append("}")
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return Italic()
        }
    }
}

class Bold() : AliasTag("b") {
	override val texId = "textbf"

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val _texId = when (currEnv) {
			ParseEnv.MATH_LITERAL -> "mathbf"
			else -> "textbf"
		}
		var sb  = StringBuilder("\\$_texId{")
		evalChildren(sb, tokens, currEnv)
		sb.append("}")
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return Bold()
        }
    }
}

class DocumentT() : AliasTag("body") {
	override val texId = "document"

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return DocumentT()
        }
	}
}

class MDFramed() : AliasTag("box") {
	override val texId = "mdframed"

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return MDFramed()
        }
	}
}