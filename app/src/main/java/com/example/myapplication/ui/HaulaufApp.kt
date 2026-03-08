package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
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
    val savedPresets by viewModel.savedPresets.collectAsState()
    val uiLanguage by viewModel.uiLanguage.collectAsState()
    val ttsSpeechRate by viewModel.ttsSpeechRate.collectAsState()

    val strings = remember(uiLanguage) {
        if (uiLanguage == "en") StringsEn else StringsDe
    }

    CompositionLocalProvider(LocalStrings provides strings) {
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
                onEditCurrentSession = { navController.navigate("setup") },
                onOpenMoveDetails = { navController.navigate("move_audio_list") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenPresets = { navController.navigate("presets") }
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
                onSavePreset = { name ->
                    viewModel.savePreset(name)
                    navController.navigate("presets?saved=true") {
                        popUpTo("setup") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "presets?saved={saved}",
            arguments = listOf(navArgument("saved") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val showSavedFeedback = backStackEntry.arguments?.getBoolean("saved") ?: false
            PresetLibraryScreen(
                savedPresets = savedPresets,
                allMoves = allMoves,
                showSavedFeedback = showSavedFeedback,
                onCreateNew = { navController.navigate("setup") },
                onStartPreset = { savedPreset ->
                    viewModel.loadPreset(savedPreset)
                    viewModel.savePresetAndStart { navController.navigate("training") }
                },
                onEditPreset = { savedPreset ->
                    viewModel.loadPreset(savedPreset)
                    navController.navigate("setup")
                },
                onDeletePreset = { savedPreset ->
                    viewModel.deletePreset(savedPreset.id)
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
                onSaveMoveInfo = { id, description, imagePath ->
                    viewModel.saveMoveInfo(id, description, imagePath)
                },
                onDeleteMove = viewModel::removeCustomMove,
                onTestMove = viewModel::testMoveAudio,
                onBack = { navController.popBackStack() }
            )
        }
        composable("training") {
            TrainingScreen(
                preset = preset,
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
                ttsSpeechRate = ttsSpeechRate,
                onTtsSpeechRateChange = viewModel::saveTtsSpeechRate,
                uiLanguage = uiLanguage,
                onUiLanguageChange = viewModel::saveUiLanguage,
                onPresetChange = viewModel::updatePreset,
                onClose = { navController.popBackStack() }
            )
        }

        composable("move_audio_list") {
            MoveAudioListScreen(
                allMoves = allMoves,
                moveOverrides = moveOverrides,
                onAddMove = viewModel::addCustomMove,
                onTestMove = viewModel::testMoveAudio,
                onOpenMoveDetails = { id -> navController.navigate("move_audio/$id") },
                onBack = { navController.popBackStack() }
            )
        }
    }
    }
}
