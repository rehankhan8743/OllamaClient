package com.rehan.ollamaclient.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rehan.ollamaclient.ui.screens.chat.ChatScreen
import com.rehan.ollamaclient.ui.screens.chatparams.ChatParamsScreen
import com.rehan.ollamaclient.ui.screens.modelmanager.ModelManagerScreen
import com.rehan.ollamaclient.ui.screens.newchat.NewChatScreen
import com.rehan.ollamaclient.ui.screens.serverform.ServerFormScreen
import com.rehan.ollamaclient.ui.screens.serverlist.ServerListScreen
import com.rehan.ollamaclient.ui.screens.sessionlist.SessionListScreen
import com.rehan.ollamaclient.ui.screens.settings.SettingsScreen

@Composable
fun OllamaClientNavHost(navController: NavHostController = androidx.navigation.compose.rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.SessionList.route) {

        composable(Screen.SessionList.route) {
            SessionListScreen(
                onNewChat = { navController.navigate(Screen.NewChat.route) },
                onOpenChat = { id -> navController.navigate(Screen.Chat.createRoute(id)) },
                onOpenServers = { navController.navigate(Screen.ServerList.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.NewChat.route) {
            NewChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onChatCreated = { id ->
                    navController.popBackStack(Screen.SessionList.route, false)
                    navController.navigate(Screen.Chat.createRoute(id))
                }
            )
        }

        composable(
            Screen.Chat.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenParams = { id -> navController.navigate(Screen.ChatParams.createRoute(id)) }
            )
        }

        composable(
            Screen.ChatParams.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            ChatParamsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ServerList.route) {
            ServerListScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddServer = { navController.navigate(Screen.ServerForm.createRoute()) },
                onEditServer = { id -> navController.navigate(Screen.ServerForm.createRoute(id)) },
                onManageModels = { id -> navController.navigate(Screen.ModelManager.createRoute(id)) }
            )
        }

        composable(
            Screen.ServerForm.route,
            arguments = listOf(navArgument("serverId") { type = NavType.LongType; defaultValue = -1L })
        ) {
            ServerFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.ModelManager.route,
            arguments = listOf(navArgument("serverId") { type = NavType.LongType })
        ) {
            ModelManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
