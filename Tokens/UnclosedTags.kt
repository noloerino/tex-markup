package com.jhshi.markup
import java.util.Arrays

val unclosedTags = hashMapOf("!DOCTYPE" to DocType.Companion::create,
							"pbr" to PageBreak.Companion::create,
							 "pdf" to IncludePDF.Companion::create,
							 "img" to Image.Companion::create,
							 "frac" to Fraction.Companion::create)

open class UnclosedTag(id: String, flags: Array<String>, properties: HashMap<String, String>) : Tag(id, flags, properties) {
	constructor(id: String) : this(id, arrayOf(), hashMapOf()) {}

	override fun eval(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		if (tokens.size == 0) {
			return ""
		}
		assert(tokens[0] === this)
        tokens.removeAt(0)
        return evalHelper(mutableListOf(), currEnv)
	}

	override fun toString(): String {
		return ":UNCLOSED TAG $id:${if (flags.size > 0) ("\n\t:flags:" + Arrays.toString(flags)) else ""}${if (properties.size > 0) ("\n\t:props:" + properties.toString()) else ""}\n"
	}
}

class DocType: UnclosedTag("!DOCTYPE") {
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		return ""
	}

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return DocType()
		}
	}
}

class PageBreak : UnclosedTag("pbr") {
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		return "\\clearpage\n"
	}

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return PageBreak()
		}
	}
}

class IncludePDF(flags: Array<String>, properties: HashMap<String, String>) : UnclosedTag("pdf", flags, properties) {
	override val validProperties = arrayOf("pgs", "src")

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val sb = StringBuilder("\\includepdf[pages=")
		if ("pgs" !in properties.keys) {
			sb.append("-")
		}
		else {
			sb.append(properties["pgs"])
		}
		if ("src" in properties.keys) {
			sb.append("]{${properties["pgs"]}}")
		}
		else {
			sb.append("{}")
		}
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return IncludePDF(flags, properties)
        }
    }
}

class Image(flags: Array<String>, properties: HashMap<String, String>) : UnclosedTag("img", flags, properties) {
	override val validProperties = arrayOf("scale", "src")

	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		var sb = StringBuilder("\\includegraphics[scale=0.7]{3b.png}")
		if ("scale" in properties.keys) {
			sb.append("[scale=${properties["scale"]}]")
		}
		if ("src" in properties.keys) {
			sb.append("{${properties["src"]}}")
		}
		else {
			sb.append("{}")
		}
		return sb.toString()
	}

	companion object {
        fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
            return Image(flags, properties)
        }
    }
}

// The following tags will wrap themselves in the appropriate math environment if found outside a math environment
class Fraction(flags: Array<String>, properties: HashMap<String, String>) : UnclosedTag("frac", flags, properties) {
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		if(currEnv != ParseEnv.MATH_LITERAL) {
			return wrapInMath(true, mutableListOf(this), currEnv)
		}
		var sb = StringBuilder("\\frac")
		for (flag in flags) {
			sb.append("{$flag}")
		}
		return sb.toString()
	}

	companion object {
		fun create(flags: Array<String>, properties: HashMap<String, String>): Tag {
			return Fraction(flags, properties)
		}
	}
}
