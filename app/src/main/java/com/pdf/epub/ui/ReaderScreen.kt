package com.pdf.epub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(epubFile: File, onBack: () -> Unit) {
    var contentText by remember { mutableStateOf("Loading EPUB contents...") }

    LaunchedEffect(epubFile) {
        try {
            // Unzip or parse basic text contents for reading view
            val zipFile = java.util.zip.ZipFile(epubFile)
            val entry = zipFile.getEntry("OEBPS/chapter1.xhtml")
            if (entry != null) {
                val inputStream = zipFile.getInputStream(entry)
                val text = inputStream.bufferedReader().use { it.readText() }
                // Strip simple HTML tags for text display
                contentText = text.replace(Regex("<[^>]*>"), "")
            } else {
                contentText = "Chapter not found in EPUB container."
            }
            zipFile.close()
        } catch (e: Exception) {
            contentText = "Error reading EPUB: ${e.localizedMessage}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EPUB Reader") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
