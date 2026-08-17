package com.example.moderntexteditor.core

object KotlinFormatter {
    fun formatCode(code: String): String {
        var indentLevel = 0
        val indentString = "    "
        val lines = code.lines()
        val formatted = StringBuilder()

        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Handle empty lines gracefully
            if (trimmedLine.isEmpty()) {
                formatted.append("\n")
                continue
            }

            // Decrease indent before printing if the line starts with a closing brace
            if (trimmedLine.startsWith("}")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            
            // Apply indentation
            for (i in 0 until indentLevel) {
                formatted.append(indentString)
            }
            formatted.append(trimmedLine).append("\n")
            
            // Increase indent for subsequent lines if this line ends with an opening brace
            if (trimmedLine.endsWith("{")) {
                indentLevel++
            }
        }
        return formatted.toString().trimEnd()
    }
}
