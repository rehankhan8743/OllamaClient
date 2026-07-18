package com.rehan.ollamaclient.ui.screens.newchat

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rehan.ollamaclient.data.local.entities.ServerConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onNavigateBack: () -> Unit,
    onChatCreated: (Long) -> Unit,
    viewModel: NewChatViewModel = viewModel()
) {
    val servers by viewModel.servers.collectAsState()
    var selectedServer by remember { mutableStateOf<ServerConfig?>(null) }
    val ollamaModels by viewModel.ollamaModels.collectAsState()
    val cloudModels by viewModel.cloudModels.collectAsState()
    val isLoadingModels by viewModel.isLoadingModels.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(servers) {
        if (servers.isNotEmpty() && selectedServer == null) {
            selectedServer = servers.firstOrNull { it.isDefault } ?: servers.firstOrNull()
        }
    }

    val models = if (selectedServer?.type == "ollama") ollamaModels else cloudModels

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Chat") },
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
        ) {
            Text("Select Server", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (servers.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "No servers configured. Add a server first.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedServer?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Server") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Icon(
                                if (selectedServer?.type == "ollama") Icons.Default.Computer else Icons.Default.Cloud,
                                contentDescription = null
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        servers.forEach { server ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(server.name)
                                        Text(
                                            server.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedServer = server
                                    expanded = false
                                    viewModel.loadModels(server)
                                },
                                leadingIcon = {
                                    Icon(
                                        if (server.type == "ollama") Icons.Default.Computer else Icons.Default.Cloud,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Model", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoadingModels -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
                error != null -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                models.isEmpty() && selectedServer != null -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No models available. Pull or download models first.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    var selectedModel by remember { mutableStateOf<String?>(null) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(models) { model ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedModel = model },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedModel == model)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = model,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedServer?.let { server ->
                                selectedModel?.let { model ->
                                    viewModel.createSession(server, model, onChatCreated)
                                }
                            }
                        },
                        enabled = selectedServer != null && selectedModel != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Chat")
                    }
                }
            }
        }
    }
}
