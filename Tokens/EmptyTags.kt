package texmarkup

val emptyTags = listOf("texml")

open class EmptyTag(id: String, flags: Array<String>, properties: HashMap<String, String>) : Tag(id, flags, properties) {
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val sb = StringBuilder()
		evalChildren(sb, tokens, currEnv)
		return sb.toString()
	}

	companion object {
		fun create(id: String, flags: Array<String>, properties: HashMap<String, String>): Tag {
			return EmptyTag(id, flags, properties)
		}
	}
}