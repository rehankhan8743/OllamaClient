package com.rehan.ollamaclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.rehan.ollamaclient.di.ServiceLocator
import com.rehan.ollamaclient.ui.navigation.OllamaClientNavHost
import com.rehan.ollamaclient.ui.theme.OllamaClientTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = ServiceLocator.getPreferencesManager(this)

        setContent {
            val themeMode by prefs.themeMode.collectAsState(initial = "system")
            val dynamicColor by prefs.dynamicColor.collectAsState(initial = true)

            OllamaClientTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OllamaClientNavHost()
                }
            }
        }
    }
}
