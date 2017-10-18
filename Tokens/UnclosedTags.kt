package markup.tokens

val unclosedTags: Array<String> = arrayOf() // TODO

open class UnclosedTag(id: String, flags: Array<String>, properties: Array<String>) : Tag(id, arrayOf<Token>(Literal(arrayOf<String>())), flags, properties) {
	constructor(id: String) : this(id, arrayOf(), arrayOf()) {}
}

class PageBreak : UnclosedTag("pbr") { }

class IncludePDF : UnclosedTag("pdf") {
	override val validProperties = arrayOf("pgs", "src") // TODO
}

class Image : UnclosedTag("img") {
	override val validProperties = arrayOf("scale", "src") // TODO
}