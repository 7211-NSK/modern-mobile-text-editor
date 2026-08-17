package com.example.moderntexteditor.core

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch

object DiffManager {

    fun generateDelta(oldText: String, newText: String): String {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        
        // Generate reverse patch to go from NEW to OLD
        val patch: Patch<String> = DiffUtils.diff(newLines, oldLines)
        
        // Serialize patch using UnifiedDiffUtils
        val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
            "newText", 
            "oldText", 
            newLines, 
            patch, 
            0
        )
        return unifiedDiff.joinToString("\n")
    }


    fun applyDelta(currentText: String, delta: String): String {
        if (delta.isBlank()) return currentText
        return try {
            val currentLines = currentText.lines()
            val deltaLines = delta.lines()
            val patch = UnifiedDiffUtils.parseUnifiedDiff(deltaLines)
            val restoredLines = DiffUtils.patch(currentLines, patch)
            restoredLines.joinToString("\n")
        } catch (e: Exception) {
            e.printStackTrace()
            currentText
        }
    }
}
