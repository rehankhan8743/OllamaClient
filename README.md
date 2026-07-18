# Ollama Client

A modern Android client app for **Ollama** servers with offline (local/LAN) model support and **cloud model support** (OpenAI-compatible APIs).

## Features

- **Theming**: System / Light / Dark theme modes, Material You dynamic color on Android 12+
- **Multiple Server Connections**: Add Ollama (local/LAN) or cloud (OpenAI-compatible) servers
- **Model Manager**: List, pull, and delete models on Ollama servers with live progress
- **Chat**: Token-by-token streaming, Markdown rendering, code block copy, stop generation
- **Per-chat Parameters**: System prompt, temperature, top-p, top-k, context length
- **Settings**: Theme, dynamic color, default server/model, clear all history
- **Full persistence**: Room database for servers, sessions, messages; DataStore for preferences

## How to Build

1. Open the project folder in **Android Studio Hedgehog (2023.1)** or later
2. Let Gradle sync (requires internet for first build)
3. Click Run or Build > Build APK(s)

**Requirements:**
- Android Studio with Kotlin and Compose plugins
- JDK 17
- Android SDK 34

## Connecting to a Local Ollama Server

By default, Ollama only binds to `localhost` and is not reachable over LAN. To fix this:

### Linux / macOS

```bash
OLLAMA_HOST=0.0.0.0 ollama serve
```

### Windows

```cmd
set OLLAMA_HOST=0.0.0.0
ollama serve
```

Then in the app:
1. Go to **Servers** (top-right icon)
2. Tap **+** to add a server
3. Select **Ollama (Local)**
4. Enter the URL: `http://<your-lan-ip>:11434` (e.g. `http://192.168.1.10:11434`)
5. Save

## Adding a Cloud Endpoint

1. Go to **Servers**
2. Tap **+**
3. Select **Cloud (OpenAI-compat)**
4. Enter the base URL (e.g. `https://api.openai.com/v1`)
5. Optionally enter an API key
6. Optionally add custom HTTP headers as JSON
7. Save

## Generating a Chat

1. Tap **+** on the home screen
2. Pick a server
3. Pick a model (the app will fetch available models from the server)
4. Start chatting

## Architecture

- **Jetpack Compose** + **Material 3** for UI
- **MVVM**: Compose → ViewModel → Repository → Room / OkHttp
- **Room** for local persistence
- **OkHttp** for streaming (raw NDJSON / SSE parsing via `callbackFlow`)
- **DataStore Preferences** for settings
- **kotlinx.serialization** for all JSON parsing
- Manual service locator for dependency injection
