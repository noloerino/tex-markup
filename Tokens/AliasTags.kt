package markup.tokens

abstract class AliasTag(id: String, content: Array<Token>, flags: Array<String>, properties: Array<String>) : Tag(id, content, flags, properties) {
    constructor(id: String, content: Array<Token>) : this(id, content, arrayOf(), arrayOf()) { }
    abstract val texId: String

    override fun eval(currEnv: ParseEnv): String {
        var sb = StringBuilder("\\begin{$texId}\n\t")
        sb.append(super.eval(currEnv))
        sb.append("\\end{$texId}\n\t")
        return sb.toString()
    }
}

class Math(content: Array<Token>) : AliasTag("math", content) {
	override val texId = "align*"
	
	override fun eval(currEnv: ParseEnv): String {
		return super.eval(ParseEnv.MATH_LITERAL)
	}
}

class InlineMath(content: Array<Token>) : AliasTag("ilmath", content) {
	override val texId = "$"

	override fun eval(currEnv: ParseEnv): String {
		var sb = StringBuilder(texId)
		for (token in content) {
            sb.append(token.eval(ParseEnv.MATH_LITERAL))
        }
		sb.append(texId)
		return sb.toString()
	}
}

class Italic(content: Array<Token>) : AliasTag("i", content) {
	override val texId = "textit"
}

class Bold(content: Array<Token>) : AliasTag("b", content) {
	override val texId = "textbf"

	override fun eval(currEnv: ParseEnv): String {
		val _texId = when (currEnv) {
			ParseEnv.MATH_LITERAL -> "mathbf"
			else -> "textbf"
		}
		var sb  = StringBuilder("\\begin{$_texId}")
		for (token in content) {
			sb.append(token.eval(currEnv))
		}
		sb.append("\\end{$_texId")
		return sb.toString()
	}
}