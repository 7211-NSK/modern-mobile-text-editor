package com.example.moderntexteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.moderntexteditor.ui.screens.editor.EditorScreen
import com.example.moderntexteditor.ui.theme.ModernTextEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModernTextEditorTheme {
                EditorScreen()
            }
        }
    }
}
