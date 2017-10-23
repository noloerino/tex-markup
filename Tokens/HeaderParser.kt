package texmarkup

import com.beust.klaxon.*
import java.io.File

const val ANSI_BOLD_YELLOW = "\u001B[1;33m"
const val ANSI_BOLD_RED = "\u001B[1;31m"
const val ANSI_RESET = "\u001B[0m"

fun warn(msg: String) {
	println(ANSI_BOLD_YELLOW + "WARN:" + ANSI_RESET + " $msg")
}

fun err(msg: String) {
	println(ANSI_BOLD_RED + "ERROR:" + ANSI_RESET + " $msg")
}
val fields = mutableMapOf(
	"hwnum" to "0",
	"name" to "MISSING",
	"sid" to "0",
	"class" to "MISSING",
	"sem" to "MISSING",
	"instr" to "MISSING",
	"header" to "MISSING")
fun initConfig(fileName: String) {
	val obj = Parser().parse(fileName)/*parse(fileName)*/ as? JsonObject ?: throw NullPointerException("Cannot find file $fileName")

	val meta = obj.obj("meta") as JsonObject
	for (field in meta.keys) {
		if (field == "instr") {
			val value = meta.array<String>(field) as JsonArray<String>
			val sb = StringBuilder()
			for (i in 0..value.size - 1) {
				sb.append(value[i])
				if (i != value.size - 1) {
					sb.append(", ")
				}
			}
			fields[field] = sb.toString()
		}
		else if (field in fields) {
			fields[field] = meta[field].toString()
		}
		else {
			warn("I don't know what to do with field \'$field\', value \'${fields[field]}\', so I'm ignoring it.")
		}
	}
	var header = obj.array<String>("header") as JsonArray
	val sb = StringBuilder()
	for (h in header) {
		sb.append("$h\n")
	}
	fields["header"] = sb.toString()
}

fun buildHeader(): String {
	val sb = StringBuilder()
	sb.append(fields["header"]!!)
	sb.append("\\rfoot{${fields["name"]!!} | ${fields["sid"]!!}}\n")
	sb.append("\\lhead{\\Large\\fontfamily{lmdh}\\selectfont ${fields["class"]!!} \\\\${fields["sem"]} \\tab\\tab ${fields["instr"]!!}}\n")
	sb.append("\\rhead{\\LARGE \\fontfamily{lmdh}\\selectfont Homework ${fields["hwnum"]!!}}\n")
	return sb.toString()
}

val headerTags = arrayOf("head", "hwnum", "name", "sid", "class", "sem", "instr", "header")

class HeaderTag(id: String) : Tag(id) {
	override fun evalHelper(tokens: MutableList<Token>, currEnv: ParseEnv): String {
		val sb = StringBuilder()
		evalChildren(sb, mutableListOf(), ParseEnv.HEADER)
		fields[id] = sb.toString()
		return ""
	}

	companion object {
		fun create(id: String): Tag {
			return HeaderTag(id)
		}
	}
}