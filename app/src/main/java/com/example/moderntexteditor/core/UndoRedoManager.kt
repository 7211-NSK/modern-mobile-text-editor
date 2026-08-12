package com.example.moderntexteditor.core

import java.util.Stack

class UndoRedoManager {
    private val undoStack = Stack<String>()
    private val redoStack = Stack<String>()

    fun pushState(currentState: String) {
        undoStack.push(currentState)
        redoStack.clear()
    }

    fun undo(currentState: String): String? {
        if (undoStack.isEmpty()) return null
        redoStack.push(currentState)
        return undoStack.pop()
    }

    fun redo(currentState: String): String? {
        if (redoStack.isEmpty()) return null
        undoStack.push(currentState)
        return redoStack.pop()
    }

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
}
