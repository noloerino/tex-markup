package com.jhshi.markup

val emptyTags = listOf("texml")

open class EmptyTag(id: String, flags: Array<String>, properties: HashMap<String, String>) : Tag(id, flags, properties) {
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val sb = StringBuilder()
		while (tokens.size != 0) {
            sb.append(tokens[0].eval(tokens, currEnv))
        }
		return sb.toString()
	}

	companion object {
		fun create(id: String, flags: Array<String>, properties: HashMap<String, String>): Tag {
			return EmptyTag(id, flags, properties)
		}
	}
}