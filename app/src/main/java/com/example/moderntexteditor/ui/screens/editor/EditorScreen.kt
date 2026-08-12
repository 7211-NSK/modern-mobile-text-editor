package com.example.moderntexteditor.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moderntexteditor.ui.components.EditorTextField
import com.example.moderntexteditor.ui.theme.ModernTextEditorTheme
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(viewModel: EditorViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val recentDocs by viewModel.recentDocuments.collectAsState()
    
    var showRenameDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Enter title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newTitle.isNotBlank()) {
                        viewModel.updateMetadata(newTitle)
                        showRenameDialog = false
                    }
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Modern Editor",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(horizontal = 28.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("New Document", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        viewModel.createNewDocument()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                NavigationDrawerItem(
                    icon = { Icon(if (viewModel.isWordWrapEnabled) Icons.Default.WrapText else Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = null) },
                    label = { Text("Word Wrap: ${if (viewModel.isWordWrapEnabled) "On" else "Off"}", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { viewModel.toggleWordWrap() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp), thickness = 0.5.dp)
                
                Text(
                    "VERSION HISTORY",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(viewModel.historyList) { version ->
                        NavigationDrawerItem(
                            label = { Text("Snapshot ${version.id} - ${java.text.SimpleDateFormat("HH:mm").format(version.timestamp)}") },
                            selected = false,
                            onClick = { viewModel.restoreVersion(version) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp), thickness = 0.5.dp)

                Text(
                    "RECENT",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(recentDocs) { doc ->
                        NavigationDrawerItem(
                            label = { 
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(doc.title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.deleteDocument(doc.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            selected = false,
                            onClick = {
                                viewModel.loadDocument(doc.id)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    EditorTopBar(
                        title = viewModel.documentTitle,
                        canUndo = viewModel.canUndo,
                        canRedo = viewModel.canRedo,
                        isReadOnly = viewModel.isReadOnly,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onTitleClick = { 
                            newTitle = viewModel.documentTitle
                            showRenameDialog = true 
                        },
                        onUndo = viewModel::onUndo,
                        onRedo = viewModel::onRedo,
                        onSave = viewModel::onSave,
                        onToggleReadOnly = viewModel::toggleReadOnly,
                        onToggleSearch = viewModel::toggleSearch
                    )
                    
                    if (viewModel.isSearchEnabled) {
                        Surface(
                            tonalElevation = 8.dp,
                            shadowElevation = 4.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = viewModel.searchQuery,
                                        onValueChange = { viewModel.updateSearchQuery(it) },
                                        placeholder = { Text("Search text...") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.toggleSearch() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = viewModel.replaceQuery,
                                        onValueChange = { viewModel.updateReplaceQuery(it) },
                                        placeholder = { Text("Replace with...") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.performReplace() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Replace")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.toggleMarkdownPreview() },
                    icon = { Icon(if (viewModel.isMarkdownPreviewEnabled) Icons.Default.Edit else Icons.Default.Visibility, null) },
                    text = { Text(if (viewModel.isMarkdownPreviewEnabled) "Editor" else "Preview") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        ) { padding ->
            Row(modifier = Modifier.padding(padding).fillMaxSize()) {
                val scrollState = rememberScrollState()
                
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    EditorTextField(
                        text = viewModel.text,
                        onTextChanged = viewModel::onTextChanged,
                        isReadOnly = viewModel.isReadOnly,
                        modifier = if (viewModel.isWordWrapEnabled) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.fillMaxSize().horizontalScroll(scrollState)
                        }
                    )
                }

                if (viewModel.isMarkdownPreviewEnabled) {
                    VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = viewModel.text,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditorPreview() {
    ModernTextEditorTheme {
        EditorScreen()
    }
}
