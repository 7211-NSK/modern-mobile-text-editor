package com.example.moderntexteditor.core

import com.github.difflib.DiffUtils
import com.github.difflib.patch.Patch

object DiffManager {

    fun generateDelta(oldText: String, newText: String): String {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        val patch: Patch<String> = DiffUtils.diff(oldLines, newLines)
        

        return patch.deltas.joinToString("\n") { it.toString() }
    }


    fun applyDelta(oldText: String, delta: String): String {

        return oldText
    }
}
