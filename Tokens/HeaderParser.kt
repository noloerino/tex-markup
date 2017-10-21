package com.jhshi.markup

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

fun parse(name: String) : Any? {
    val cls = Parser::class.java
    return cls.getResourceAsStream(name)?.let { inputStream ->
        return Parser().parse(inputStream)
    }
}

fun getMeta(fileName: String, tokens: MutableList<String>) : String {
	val obj = Parser().parse(fileName)/*parse(fileName)*/ as? JsonObject ?: throw NullPointerException("Cannot find file $fileName")
	val fields = mutableMapOf(
		"hwnum" to "0",
		"name" to "MISSING",
		"sid" to "0",
		"class" to "MISSING",
		"sem" to "MISSING",
		"instr" to "MISSING")
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
	// Gets meta information from split tokens
	var i = 0
	while (i < tokens.size && tokens[i] != "<head>") {
		
	}


	return ""
}