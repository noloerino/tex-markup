package markup.tokens

enum class ParseEnv {
    // Used when specifying meta information (like the name)
    HEADER,
    MATH_LITERAL,
    SPECIAL,
    LITERAL,
    // Used in the declaration of a tag
    TAG_DECL
}

abstract class Token {
    abstract fun eval(currEnv: ParseEnv): String
}

// Characters that are directly replaced
val csubstitutions: HashMap<Char, String> = hashMapOf('`' to "&", '\n' to "\\\\", '~' to "\\sim")

class Literal(var content: Array<String>) : Token() {
    override fun eval(currEnv: ParseEnv): String {
        var sb = StringBuilder()
        for (token in content) {
            for (c in token) {
                if (c in csubstitutions.keys){
                    sb.append(csubstitutions[c])
                }
                else{
                    sb.append(token)
                }
            }
        }
        return sb.toString()
    }
}

abstract class Tag(val id: String, var content: Array<Token>, var flags: Array<String>, var properties: Array<String>) : Token() {
    
    constructor(id: String, content: Array<Token>) : this(id, content, arrayOf(), arrayOf()) { }

    open val validFlags: Array<String> = arrayOf()
    open val validProperties: Array<String> = arrayOf()

    open override fun eval(currEnv: ParseEnv): String {
        var sb: StringBuilder = StringBuilder()
        for (token in content) {
            sb.append(token.eval(currEnv))
        }
        return sb.toString()
    }
}

// When in doubt, <tag> will be converted to \begin{tag} and </tag> will be converted to \end{tag}
open class UnknownTag(id: String, content: String) : Tag(id, arrayOf<Token>(Literal(arrayOf(content)))) {
    open override fun eval(currEnv: ParseEnv): String {
        var sb: StringBuilder = StringBuilder("\\begin{$id}\n\t")
        sb.append(super.eval(currEnv))
        sb.append("\\end{$id}\n\t")
        return sb.toString()
    }
}

// The following tags do not create a new environment, and take as content everything in the line following
val lineTags: List<String> = listOf("item", "node")
abstract class LineTag(id: String, content: String) : UnknownTag(id, content) {
    open override fun eval(currEnv: ParseEnv): String {
        var sb: StringBuilder = StringBuilder("\\$id ")
        for (token in content) {
            sb.append(token.eval(currEnv))
        }
        return sb.toString()
    }
}
