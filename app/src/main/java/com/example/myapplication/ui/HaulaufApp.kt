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
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("presets") {
            PresetLibraryScreen(
                savedPresets = savedPresets,
                allMoves = allMoves,
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
                customMoves = allMoves.filter { it.id.startsWith("custom_") },
                ttsSpeechRate = ttsSpeechRate,
                onTtsSpeechRateChange = viewModel::saveTtsSpeechRate,
                uiLanguage = uiLanguage,
                onUiLanguageChange = viewModel::saveUiLanguage,
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
                onSaveMoveInfo = { moveId, description, imagePath ->
                    viewModel.saveMoveInfo(moveId, description, imagePath)
                },
                onTestMove = viewModel::testMoveAudio,
                onOpenMoveDetails = { id -> navController.navigate("move_audio/$id") },
                onBack = { navController.popBackStack() }
            )
        }
    }
    }
}
