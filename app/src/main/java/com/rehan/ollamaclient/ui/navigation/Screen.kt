package com.rehan.ollamaclient.ui.navigation

sealed class Screen(val route: String) {
    object SessionList : Screen("session_list")
    object NewChat : Screen("new_chat")
    object Chat : Screen("chat/{sessionId}") {
        fun createRoute(sessionId: Long) = "chat/$sessionId"
    }
    object ChatParams : Screen("chat_params/{sessionId}") {
        fun createRoute(sessionId: Long) = "chat_params/$sessionId"
    }
    object ServerList : Screen("server_list")
    object ServerForm : Screen("server_form?serverId={serverId}") {
        fun createRoute(serverId: Long? = null) = if (serverId != null) "server_form?serverId=$serverId" else "server_form"
    }
    object ModelManager : Screen("model_manager/{serverId}") {
        fun createRoute(serverId: Long) = "model_manager/$serverId"
    }
    object Settings : Screen("settings")
}
