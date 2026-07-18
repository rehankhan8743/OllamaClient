package com.rehan.ollamaclient.ui.screens.chatparams

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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatParamsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatParamsViewModel = viewModel()
) {
    val session by viewModel.session.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Parameters") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        session?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = s.systemPrompt,
                    onValueChange = { viewModel.updateSystemPrompt(it) },
                    label = { Text("System Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8
                )

                Text("Temperature: ${String.format("%.2f", s.temperature)}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = s.temperature,
                    onValueChange = { viewModel.updateTemperature(it) },
                    valueRange = 0f..2f,
                    steps = 19
                )

                Text("Top P: ${String.format("%.2f", s.topP)}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = s.topP,
                    onValueChange = { viewModel.updateTopP(it) },
                    valueRange = 0f..1f,
                    steps = 9
                )

                Text("Top K: ${s.topK}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = s.topK.toFloat(),
                    onValueChange = { viewModel.updateTopK(it.roundToInt()) },
                    valueRange = 1f..100f,
                    steps = 98
                )

                Text("Context Length (num_ctx): ${s.numCtx}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = s.numCtx.toFloat(),
                    onValueChange = { viewModel.updateNumCtx(it.roundToInt()) },
                    valueRange = 512f..32768f,
                    steps = 63
                )
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
