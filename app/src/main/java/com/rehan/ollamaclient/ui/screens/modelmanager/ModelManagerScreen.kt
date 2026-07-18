package com.rehan.ollamaclient.ui.screens.modelmanager

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ModelManagerViewModel = viewModel()
) {
    val models by viewModel.models.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPulling by viewModel.isPulling.collectAsState()
    val pullStatus by viewModel.pullStatus.collectAsState()
    val pullProgress by viewModel.pullProgress.collectAsState()
    val pullModelName by viewModel.pullModelName.collectAsState()
    val error by viewModel.error.collectAsState()
    var modelToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Manager") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadModels() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            OutlinedTextField(
                value = pullModelName,
                onValueChange = { viewModel.setPullModelName(it) },
                label = { Text("Model name to pull (e.g. llama3)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPulling
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.pullModel() },
                enabled = pullModelName.isNotBlank() && !isPulling,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isPulling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(pullStatus)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pull Model")
                }
            }

            if (isPulling) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = pullProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(pullStatus, style = MaterialTheme.typography.bodySmall)
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Installed Models", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                }
                models.isEmpty() -> {
                    Text("No models found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(models, key = { it.name }) { model ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(model.name, style = MaterialTheme.typography.bodyMedium)
                                        if (model.size.isNotBlank()) {
                                            Text(model.size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (model.details.isNotBlank()) {
                                            Text(model.details, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { modelToDelete = model.name }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        modelToDelete?.let { name ->
            AlertDialog(
                onDismissRequest = { modelToDelete = null },
                title = { Text("Delete Model") },
                text = { Text("Delete \"$name\"? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteModel(name)
                        modelToDelete = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { modelToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
