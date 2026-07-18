package com.rehan.ollamaclient.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val defaultServerId by viewModel.defaultServerId.collectAsState()
    val defaultModel by viewModel.defaultModel.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val showClearDialog by viewModel.showClearDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Text("Theme", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                    FilterChip(
                        selected = themeMode == value,
                        onClick = { viewModel.setThemeMode(value) },
                        label = { Text(label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dynamic Color", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Use Material You colors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }

            HorizontalDivider()

            Text("Defaults", style = MaterialTheme.typography.titleMedium)

            Text("Default Server", style = MaterialTheme.typography.bodyMedium)
            var serverExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = serverExpanded,
                onExpandedChange = { serverExpanded = !serverExpanded }
            ) {
                OutlinedTextField(
                    value = servers.find { it.id == defaultServerId }?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Default Server") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(serverExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = serverExpanded,
                    onDismissRequest = { serverExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            viewModel.setDefaultServer(-1L)
                            serverExpanded = false
                        }
                    )
                    servers.forEach { server ->
                        DropdownMenuItem(
                            text = { Text(server.name) },
                            onClick = {
                                viewModel.setDefaultServer(server.id)
                                serverExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Default Model", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = defaultModel,
                onValueChange = { viewModel.setDefaultModel(it) },
                label = { Text("Model name") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text("Data Management", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { viewModel.requestClearHistory() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Chat History")
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissClearDialog() },
                title = { Text("Clear All Chat History") },
                text = { Text("This will permanently delete all chat sessions and messages. This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearAllHistory() }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissClearDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
