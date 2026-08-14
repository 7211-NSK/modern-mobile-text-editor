package com.example.moderntexteditor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "versions")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val delta: String, // Stored as a JSON or serialized patch
    val timestamp: Long = System.currentTimeMillis()
)
