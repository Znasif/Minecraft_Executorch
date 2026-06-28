package com.example.copilotui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.copilotui.navigation.Routes
import com.example.copilotui.ui.screens.*
import com.example.copilotui.ui.theme.CopilotTheme
import com.example.copilotui.ui.viewmodel.ConstructionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CopilotTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val constructionViewModel: ConstructionViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onDone = {
                navController.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeDashboardScreen(
                onNewProject = { navController.navigate(Routes.ENVIRONMENT_SCAN) },
                onContinue = { navController.navigate(Routes.BUILD_TIMELINE) },
                onDemo = { navController.navigate(Routes.AR_CONSTRUCTION) },
                onScanner = { navController.navigate(Routes.ENVIRONMENT_SCAN) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.ENVIRONMENT_SCAN) {
            EnvironmentScanScreen(
                onFinish = { navController.navigate(Routes.AR_CONSTRUCTION) },
            )
        }
        composable(Routes.BUILD_TIMELINE) {
            BuildTimelineScreen(
                onBack = { navController.popBackStack() },
                onResume = { navController.navigate(Routes.AR_CONSTRUCTION) },
            )
        }
        composable(Routes.AR_CONSTRUCTION) {
            ARConstructionScreen(
                viewModel = constructionViewModel,
                onClose = { navController.popBackStack() },
                onVerify = { navController.navigate(Routes.VERIFICATION) },
                onHelp = {},
            )
        }
        composable(Routes.VERIFICATION) {
            VerificationScreen(
                viewModel = constructionViewModel,
                onBack = { navController.popBackStack() },
                onFix = { navController.popBackStack() },
                onProceed = { navController.navigate(Routes.COMPLETION) },
            )
        }
        composable(Routes.COMPLETION) {
            CompletionScreen(
                onShare = {},
                onNewProject = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
