package com.brunoshiroma.url_expander

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    sharedText: String?,
    actions: PlatformActions,
    client: GoUrlExpanderClient = remember { GoUrlExpanderClient() },
) {
    MaterialTheme {
        var input by remember { mutableStateOf(GoUrlExpanderClient.NO_DATA) }
        var expanded by remember { mutableStateOf("") }
        var showWeb by remember { mutableStateOf(false) }
        var trigger by remember { mutableStateOf(0) }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // Every new shared text (or manual request) resets the state and expands again.
        LaunchedEffect(sharedText) {
            if (sharedText != null) {
                input = sharedText
                trigger++
            }
        }
        LaunchedEffect(trigger) {
            expanded = ""
            if (trigger > 0) {
                expanded = runCatching { client.getUrlExpanded(input) }.getOrNull().orEmpty()
            }
        }

        val enabled = expanded.isNotEmpty()

        Scaffold(
            modifier = Modifier.safeDrawingPadding(),
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = { Text("URL Expander") },
                    navigationIcon = {
                        if (showWeb) TextButton(onClick = { showWeb = false }) { Text("Back") }
                    },
                )
            },
        ) { padding ->
            if (showWeb) {
                PlatformWebView(expanded, Modifier.padding(padding).fillMaxSize())
            } else {
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("URL") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { trigger++ }) { Text("Expand") }
                    SelectionContainer {
                        Text(
                            text = expanded,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Button(enabled = enabled, onClick = {
                            actions.copyToClipboard(expanded)
                            scope.launch { snackbar.showSnackbar("Copied") }
                        }) { Text("Copy") }
                        Button(enabled = enabled, onClick = { actions.share(expanded) }) {
                            Text("Share")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Button(enabled = enabled, onClick = { actions.openExternal(expanded) }) {
                            Text("Open external")
                        }
                        Button(enabled = enabled, onClick = { showWeb = true }) {
                            Text("Open internal")
                        }
                    }
                }
            }
        }
    }
}
