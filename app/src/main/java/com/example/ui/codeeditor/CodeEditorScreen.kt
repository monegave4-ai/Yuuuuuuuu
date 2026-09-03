package com.example.ui.codeeditor

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val workspace = remember { File(context.filesDir, "workspace").apply { mkdirs() } }

    // Setup default files if empty
    LaunchedEffect(Unit) {
        val indexHtml = File(workspace, "index.html")
        if (!indexHtml.exists()) {
            indexHtml.writeText("<!DOCTYPE html>\n<html>\n<head>\n  <link rel=\"stylesheet\" href=\"style.css\">\n</head>\n<body>\n  <h1>Hello Acode!</h1>\n  <script src=\"main.js\"></script>\n</body>\n</html>")
            File(workspace, "style.css").writeText("body {\n  background-color: #1e1e1e;\n  color: #fff;\n  font-family: sans-serif;\n  text-align: center;\n  padding-top: 50px;\n}")
            File(workspace, "main.js").writeText("console.log('App started!');")
        }
    }

    var files by remember { mutableStateOf(workspace.listFiles()?.toList()?.sortedBy { it.name } ?: emptyList()) }
    var activeFile by remember { mutableStateOf<File?>(files.firstOrNull { it.name == "index.html" } ?: files.firstOrNull()) }
    var fileContent by remember { mutableStateOf(activeFile?.readText() ?: "") }
    var showPreview by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun loadFile(file: File) {
        activeFile?.writeText(fileContent) // Save current before switching
        activeFile = file
        fileContent = file.readText()
        showPreview = false
    }

    fun refreshFiles() {
        files = workspace.listFiles()?.toList()?.sortedBy { it.name } ?: emptyList()
        if (activeFile == null) activeFile = files.firstOrNull()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Spacer(Modifier.height(12.dp))
                Text("Workspace", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(files) { file ->
                        val isSelected = activeFile == file
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    loadFile(file)
                                    scope.launch { drawerState.close() }
                                }
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    file.name.endsWith(".html") -> Icons.Filled.Language
                                    file.name.endsWith(".css") -> Icons.Filled.Style
                                    file.name.endsWith(".js") -> Icons.Filled.Code
                                    else -> Icons.Filled.InsertDriveFile
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = file.name,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                HorizontalDivider()
                TextButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("New File")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(activeFile?.name ?: "No file") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, "Menu")
                        }
                    },
                    actions = {
                        if (activeFile != null) {
                            IconButton(onClick = { activeFile?.writeText(fileContent) }) {
                                Icon(Icons.Filled.Save, "Save")
                            }
                        }
                        IconButton(onClick = {
                            activeFile?.writeText(fileContent)
                            showPreview = !showPreview
                        }) {
                            Icon(if (showPreview) Icons.Filled.Code else Icons.Filled.PlayArrow, "Toggle Preview")
                        }
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.Close, "Close App")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (showPreview) {
                    val indexHtml = File(workspace, "index.html")
                    val baseUrl = "file://${workspace.absolutePath}/"
                    val htmlData = if (indexHtml.exists()) indexHtml.readText() else "<h1>No index.html found</h1>"
                    
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.allowFileAccess = true
                                settings.allowFileAccessFromFileURLs = true
                                settings.allowUniversalAccessFromFileURLs = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(baseUrl, htmlData, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    TextField(
                        value = fileContent,
                        onValueChange = { fileContent = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File") },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("File Name (e.g., script.js)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFileName.isNotBlank()) {
                        val newFile = File(workspace, newFileName)
                        if (!newFile.exists()) {
                            newFile.writeText("")
                            refreshFiles()
                            loadFile(newFile)
                        }
                        showNewFileDialog = false
                        newFileName = ""
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
