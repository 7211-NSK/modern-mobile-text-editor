package com.example.moderntexteditor.ui.screens.editor

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moderntexteditor.core.DiffManager
import com.example.moderntexteditor.core.UndoRedoManager
import com.example.moderntexteditor.data.AppDatabase
import com.example.moderntexteditor.data.DocumentEntity
import com.example.moderntexteditor.data.VersionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).documentDao()
    private val undoRedoManager = UndoRedoManager()
    
    var text by mutableStateOf("")
        private set

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    var isReadOnly by mutableStateOf(false)
        private set

    var documentTitle by mutableStateOf("Untitled")
        private set

    var documentEncoding by mutableStateOf("UTF-8")
        private set


    var isWordWrapEnabled by mutableStateOf(true)
        private set
    var isMarkdownPreviewEnabled by mutableStateOf(false)
        private set
    var isSearchEnabled by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var replaceQuery by mutableStateOf("")
        private set

    var historyList by mutableStateOf<List<VersionEntity>>(emptyList())
        private set

    val recentDocuments = dao.getRecentDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var currentDocId: Long = -1
    private var saveJob: Job? = null

    init {
        createNewDocument()
    }

    fun createNewDocument() {
        viewModelScope.launch {
            val id = dao.insertDocument(DocumentEntity(title = "Untitled", content = ""))
            loadDocument(id)
        }
    }

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            val doc = dao.getDocumentById(id)
            if (doc != null) {
                currentDocId = doc.id
                text = doc.content
                documentTitle = doc.title
                loadHistory()
                updateStatus()
            }
        }
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch {
            val doc = dao.getDocumentById(id)
            if (doc != null) {
                dao.deleteDocument(doc)

                if (currentDocId == id) {
                    createNewDocument()
                }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            historyList = dao.getVersionsForDocument(currentDocId)
        }
    }

    fun restoreVersion(version: VersionEntity) {

        loadHistory() 
    }

    fun updateMetadata(newTitle: String, newEncoding: String = "UTF-8") {
        documentTitle = newTitle
        documentEncoding = newEncoding
        viewModelScope.launch {
            val oldDoc = dao.getDocumentById(currentDocId)
            if (oldDoc != null) {
                dao.updateDocument(oldDoc.copy(title = newTitle, lastModified = System.currentTimeMillis()))
            }
        }
    }

    fun onTextChanged(newText: String) {
        if (newText != text && !isReadOnly) {
            undoRedoManager.pushState(text)
            text = newText
            updateStatus()
            scheduleAutoSave()
        }
    }

    fun onUndo() {
        if (isReadOnly) return
        undoRedoManager.undo(text)?.let {
            text = it
            updateStatus()
            scheduleAutoSave()
        }
    }

    fun onRedo() {
        if (isReadOnly) return
        undoRedoManager.redo(text)?.let {
            text = it
            updateStatus()
            scheduleAutoSave()
        }
    }

    fun toggleReadOnly() { isReadOnly = !isReadOnly }
    fun toggleWordWrap() { isWordWrapEnabled = !isWordWrapEnabled }
    fun toggleMarkdownPreview() { isMarkdownPreviewEnabled = !isMarkdownPreviewEnabled }
    fun toggleSearch() { isSearchEnabled = !isSearchEnabled }

    fun updateSearchQuery(query: String) { searchQuery = query }
    fun updateReplaceQuery(query: String) { replaceQuery = query }

    fun performReplace() {
        if (searchQuery.isNotEmpty()) {
            val newText = text.replace(searchQuery, replaceQuery)
            onTextChanged(newText)
        }
    }

    fun performReplaceAll() {
        if (searchQuery.isNotEmpty()) {
            val newText = text.replace(searchQuery, replaceQuery)
            onTextChanged(newText)
        }
    }

    private fun updateStatus() {
        canUndo = undoRedoManager.canUndo()
        canRedo = undoRedoManager.canRedo()
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000)
            onSave()
        }
    }

    fun onSave() {
        viewModelScope.launch {
            val oldDoc = dao.getDocumentById(currentDocId)
            if (oldDoc != null) {
                val delta = DiffManager.generateDelta(oldDoc.content, text)
                if (delta.isNotEmpty()) {
                    dao.insertVersion(VersionEntity(documentId = currentDocId, delta = delta))
                }
                dao.updateDocument(oldDoc.copy(content = text, lastModified = System.currentTimeMillis()))
            }
        }
    }
}
