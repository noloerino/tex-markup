package com.jhshi.markup

val aliasTags = hashMapOf("math" to Math.Companion::create,
						  "ilmath" to InlineMath.Companion::create, 
						  "i" to Italic.Companion::create,
						  "b" to Bold.Companion::create)

abstract class AliasTag(id: String, flags: Array<String>, properties: HashMap<String, String>) : Tag(id, flags, properties) {
    constructor(id: String) : this(id, arrayOf(), hashMapOf()) { }
    abstract val texId: String

    override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        var sb = StringBuilder("\\begin{$texId}\n\t")
        while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, currEnv))
        }
        sb.append("\\end{$texId}\n\t")
        return sb.toString()
    }
}

class Math() : AliasTag("math") {
	override val texId = "align*"
	
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val sb = StringBuilder("\\begin{$texId}")
		while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, ParseEnv.MATH_LITERAL))
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
		while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, ParseEnv.MATH_LITERAL))
        }
		sb.append(texId)
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return InlineMath()
        }
    }
}

class Italic() : AliasTag("i") {
	override val texId = "textit"

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		var sb = StringBuilder("\\$texId{")
		while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, currEnv))
        }
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
		while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, currEnv))
        }
		sb.append("}")
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return Bold()
        }
    }
}