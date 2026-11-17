package com.pdf.epub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pdf.epub.ui.MainScreen
import com.pdf.epub.ui.ReaderScreen
import com.pdf.epub.ui.theme.PdfToEpubTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfToEpubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                    var activeEpub by remember { mutableStateOf<File?>(null) }

                    when (val screen = currentScreen) {
                        is Screen.Main -> {
                            MainScreen(
                                onOpenReader = { file ->
                                    activeEpub = file
                                    currentScreen = Screen.Reader
                                }
                            )
                        }
                        is Screen.Reader -> {
                            activeEpub?.let { file ->
                                ReaderScreen(
                                    epubFile = file,
                                    onBack = { currentScreen = Screen.Main }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Main : Screen()
    object Reader : Screen()
}
