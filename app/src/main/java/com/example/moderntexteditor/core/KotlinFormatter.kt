package com.example.moderntexteditor.core

object KotlinFormatter {
    fun formatCode(code: String): String {
        var indentLevel = 0
        val indentString = "    "
        val lines = code.lines()
        val formatted = StringBuilder()

        for (line in lines) {
            val trimmedLine = line.trim()
            

            if (trimmedLine.isEmpty()) {
                formatted.append("\n")
                continue
            }


            if (trimmedLine.startsWith("}")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            

            for (i in 0 until indentLevel) {
                formatted.append(indentString)
            }
            formatted.append(trimmedLine).append("\n")
            

            if (trimmedLine.endsWith("{")) {
                indentLevel++
            }
        }
        return formatted.toString().trimEnd()
    }
}
