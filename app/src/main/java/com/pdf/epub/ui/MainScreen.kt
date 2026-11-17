package com.pdf.epub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pdf.epub.converter.PdfToEpubConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onOpenReader: (File) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Ready to convert PDF to EPUB") }
    var isConverting by remember { mutableStateOf(false) }
    var convertedFile by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF to EPUB Converter") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isConverting = true
                    statusText = "Preparing sample PDF..."
                    scope.launch {
                        val success = withContext(Dispatchers.IO) {
                            try {
                                // Create a dummy sample PDF file for demonstration
                                val samplePdf = File(context.cacheDir, "sample.pdf")
                                samplePdf.writeText("Sample PDF Header\n\nThis is a converted PDF document demonstrating heading preservation and formatting.\n\nSub-heading content follows here with clean typography.")

                                val outputEpub = File(context.getExternalFilesDir(null), "converted_book.epub")
                                val converter = PdfToEpubConverter(context)
                                converter.convertPdfToEpub(samplePdf, outputEpub) { progress ->
                                    statusText = progress
                                }
                            } catch (e: Exception) {
                                false
                            }
                        }
                        isConverting = false
                        if (success) {
                            convertedFile = File(context.getExternalFilesDir(null), "converted_book.epub")
                            statusText = "Conversion Successful!"
                        } else {
                            statusText = "Conversion Failed."
                        }
                    }
                },
                enabled = !isConverting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isConverting) "Converting..." else "Convert Sample PDF to EPUB")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (convertedFile != null) {
                Button(
                    onClick = { onOpenReader(convertedFile!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Book, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Read EPUB in Reader")
                }
            }
        }
    }
}
