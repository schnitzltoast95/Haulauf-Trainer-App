package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun HaulaufApp(
    viewModel: HaulaufViewModel
) {
    val navController = rememberNavController()
    val preset by viewModel.preset.collectAsState()
    val moveOverrides by viewModel.moveOverrides.collectAsState()
    val allMoves by viewModel.allMoves.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                preset = preset,
                allMoves = allMoves,
                onQuickstart = {
                    viewModel.startQuickstart { navController.navigate("training") }
                },
                onConfigureTraining = { navController.navigate("setup") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("setup") {
            TrainingSetupScreen(
                preset = preset,
                allMoves = allMoves,
                onPresetChange = viewModel::updatePreset,
                onStart = {
                    viewModel.savePresetAndStart { navController.navigate("training") }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "move_audio/{moveId}",
            arguments = listOf(navArgument("moveId") { type = NavType.StringType })
        ) { backStackEntry ->
            val moveId = backStackEntry.arguments?.getString("moveId") ?: return@composable
            MoveAudioScreen(
                moveId = moveId,
                allMoves = allMoves,
                overrideUri = moveOverrides[moveId],
                onSaveOverride = viewModel::saveMoveOverride,
                onTestMove = viewModel::testMoveAudio,
                onBack = { navController.popBackStack() }
            )
        }
        composable("training") {
            TrainingScreen(
                state = viewModel.trainingState,
                onPause = viewModel::pauseTraining,
                onResume = viewModel::resumeTraining,
                onStop = {
                    viewModel.stopTraining()
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                preset = preset,
                customMoves = allMoves.filter { it.id.startsWith("custom_") },
                onPresetChange = viewModel::updatePreset,
                onOpenCustomAudio = { navController.navigate("move_audio_list") },
                onAddMove = viewModel::addCustomMove,
                onRemoveMove = viewModel::removeCustomMove,
                onClose = { navController.popBackStack() }
            )
        }

        composable("move_audio_list") {
            MoveAudioListScreen(
                allMoves = allMoves,
                moveOverrides = moveOverrides,
                onSaveOverride = viewModel::saveMoveOverride,
                onTestMove = viewModel::testMoveAudio,
                onOpenMoveDetails = { id -> navController.navigate("move_audio/$id") },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
