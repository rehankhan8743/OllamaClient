package com.rehan.ollamaclient.ui.screens.serverform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServerFormViewModel = viewModel()
) {
    val name by viewModel.name.collectAsState()
    val type by viewModel.type.collectAsState()
    val url by viewModel.url.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val customHeaders by viewModel.customHeaders.collectAsState()
    val connectTimeout by viewModel.connectTimeout.collectAsState()
    val readTimeout by viewModel.readTimeout.collectAsState()
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(saved) {
        if (saved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Server" else "Add Server") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Server Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Server Type", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == "ollama",
                    onClick = { viewModel.setType("ollama") },
                    label = { Text("Ollama (Local)") }
                )
                FilterChip(
                    selected = type == "cloud",
                    onClick = { viewModel.setType("cloud") },
                    label = { Text("Cloud (OpenAI-compat)") }
                )
            }

            OutlinedTextField(
                value = url,
                onValueChange = { viewModel.setUrl(it) },
                label = { Text(if (type == "ollama") "URL (e.g. http://192.168.1.10:11434)" else "Base URL (e.g. https://api.example.com/v1)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (type == "cloud") {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.setApiKey(it) },
                    label = { Text("API Key (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customHeaders,
                    onValueChange = { viewModel.setCustomHeaders(it) },
                    label = { Text("Custom Headers (JSON, optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("{\"X-Custom\":\"value\"}") }
                )
            }

            Text("Timeouts", style = MaterialTheme.typography.titleSmall)
            Text("Connect: ${connectTimeout}s", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = connectTimeout.toFloat(),
                onValueChange = { viewModel.setConnectTimeout(it.toInt()) },
                valueRange = 5f..120f
            )
            Text("Read: ${readTimeout}s", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = readTimeout.toFloat(),
                onValueChange = { viewModel.setReadTimeout(it.toInt()) },
                valueRange = 30f..600f
            )

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = url.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
