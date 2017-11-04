package texmarkup
import java.util.Arrays

enum class ParseEnv {
    // Used when specifying meta information (like the name)
    HEADER,
    MATH_LITERAL,
    LITERAL,
    // Used in the declaration of a tag
    TAG_DECL
}

abstract class Token {
    abstract fun eval(tokens: MutableList<Token>, currEnv: ParseEnv): String
    override fun toString(): String {
        return ":TOKEN:\n"
    }
}

fun evalChildren(sb: StringBuilder, children: MutableList<Token>, currEnv: ParseEnv) {
    while (children.size != 0) {
        sb.append(children[0].eval(children, currEnv))
    }
}

// Characters that are directly replaced
val csubstitutions: HashMap<Char, String> = hashMapOf('`' to "&", '\n' to "\\\\", '~' to "\\sim")

// TODO
// typealias Property = String to String

class Literal(var content: List<String>) : Token() {
    override fun eval(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        assert (tokens.size == 0 || tokens[0] === this)
        var sb = StringBuilder()
        for (token in content) {
            for (c in token) {
                if (c in csubstitutions.keys) {
                    if (tokens.size == 0 ||  (c == '\n' && nxtEatsPrNewLine(tokens))) { }
                    else {
                        sb.append(csubstitutions[c] + " ")
                    }
                }
                else {
                    sb.append(c)
                }
            }
        }
        if (tokens.size != 0) {
            tokens.removeAt(0)
        }
        return sb.toString()
    }

    override fun toString(): String {
        return ":LITERAL:\n\t:content: $content\n"
    }
}

fun wrapInMath(inlin: Boolean, tokens: MutableList<Token>, currEnv: ParseEnv): String {
    if (inlin) {
        return InlineMath().eval(tokens, currEnv)
    }
    else {
        return Math().eval(tokens, currEnv)
    }
}

val noPrecedingNewLineTags = listOf("center", "math", "enumerate", "itemize", "mdframed", "image", "item")
fun nxtEatsPrNewLine(tokens: MutableList<Token>): Boolean {
    if (tokens.size > 1) {
        return tokens[1] is Tag && (tokens[1] as Tag).id in noPrecedingNewLineTags
    }
    else {
        return false;
    }
    /*try {
        var t: Tag = tokens.first{ it is Tag } as Tag
        return t.id in noPrecedingNewLineTags
    }
    catch (e: NoSuchElementException) {
        return false
    }*/
}

abstract class Tag(val id: String, var flags: Array<String>, var properties: HashMap<String, String>) : Token() {
    constructor(id: String) : this(id, arrayOf(), hashMapOf()) {
        for ((prop, value) in properties) {
            if (prop !in validProperties) {
                warn("I was given property $prop=$value for tag $id, and I do not know what to do with it")
            }
        }
        for (flag in flags) {
            if (flag !in validFlags) {
                warn("I was given flag $flag for tag $id, and I do not know what to do with it")
            }
        }
    }

    // SHOULD ALWAYS BE CALLED WITH THIS ELEMENT'S OPEN TAG AS THE FIRST ELEMENT
    override fun eval(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        if (tokens.size == 0) {
            return ""
        }
        assert(tokens[0] === this)
        tokens.removeAt(0)
        var i = 0
        var level = 0 // determines the number of closing tags that need to be seen to stop
        var toEval: MutableList<Token> = mutableListOf()
        while (tokens.size != 0 && !(tokens[i] is ClosingTag && (tokens[i] as Tag).id == id && level == 0)) {
            if (tokens[i] is Tag && (tokens[i] as Tag).id == id) {
                if (tokens[i] is ClosingTag) { // closes a different tag of the same type
                    level--
                }
                else {
                    level++
                }
            }
            toEval.add(tokens[i])
            tokens.removeAt(i)
        }
        if (tokens.size != 0) {
            assert(tokens[0] is ClosingTag && (tokens[0] as Tag).id == id)
            tokens.removeAt(0)
        }
        return evalHelper(toEval, currEnv) + "\n"
    }

    open val validFlags: Array<String> = arrayOf()
    open val validProperties: Array<String> = arrayOf()

    open fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        var sb: StringBuilder = StringBuilder()
        evalChildren(sb, tokens, currEnv)
        return sb.toString()
    }

    override fun toString(): String {
        return ":OPEN TAG $id:${if (flags.size > 0) ("\n\t:flags:" + Arrays.toString(flags)) else ""}${if (properties.size > 0) ("\n\t:props:" + properties.toString()) else ""}\n"
    }
}

class ClosingTag(id: String) : Tag(id) {
    override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        return ""
    }

    override fun toString(): String {
        return ":CLOSING TAG $id:\n"
    }
}

// When in doubt, <tag> will be converted to \begin{tag} and </tag> will be converted to \end{tag}
open class UnknownTag(id: String) : Tag(id) {
    open override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        var sb: StringBuilder = StringBuilder("\\begin{$id}\n")
        evalChildren(sb, tokens, currEnv)
        sb.append("\n\\end{$id}\n")
        return sb.toString()
    }

    override fun toString(): String {
        return super.toString() + ":UNKNOWN:"
    }

    companion object {
        fun create(id: String, flags: Array<String>, properties: HashMap<String, String>): Tag {
            if (flags.size != 0 || properties.size != 0) {
                warn("Tag $id was not specified, dropping flags ${Arrays.toString(flags)} and properties ${properties.toString()}")
            }
            return UnknownTag(id)
        }
    }
}

// The following tags do not create a new environment, and take as content everything in the line following
val lineTags: List<String> = listOf("item", "node")
class LineTag(id: String) : UnknownTag(id) {
    override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
        var sb: StringBuilder = StringBuilder("\\$id ")
        evalChildren(sb, tokens, currEnv)
        return sb.toString()
    }

    companion object {
        fun create(id: String, flags: Array<String>, properties: HashMap<String, String>): Tag {
            if (flags.size != 0 || properties.size != 0) {
                warn("Line tag $id was not specified, dropping flags ${Arrays.toString(flags)} and properties ${properties.toString()}")
            }
            return LineTag(id)
        }
    }
}
