package com.example.myapplication.ui

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.data.SavedPreset
import com.example.myapplication.data.AudioLevels
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
    val ttsVoiceName by viewModel.ttsVoiceName.collectAsState()

    val globalAudio by viewModel.globalAudio.collectAsState()
    var editing by rememberSaveable { mutableStateOf<SavedPreset?>(null) }
    var editorInitial by rememberSaveable { mutableStateOf(TrainingPreset()) }
    fun openEditor(value: TrainingPreset, saved: SavedPreset? = null) {
        editing = saved
        editorInitial = value
        navController.navigate("setup")
    }

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
                    if (allMoves.none { it.id in preset.selectedMoveIds }) openEditor(preset)
                    else viewModel.startQuickstart { navController.navigate("training") }
                },
                onConfigureTraining = { openEditor(preset) },
                onEditCurrentSession = { openEditor(preset) },
                onOpenMoveDetails = { navController.navigate("move_audio_list") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenPresets = { navController.navigate("presets") }
            )
        }
        composable("setup") {
            TrainingEditor(
                initial = editorInitial,
                saved = editing,
                allMoves = allMoves,
                onStart = { draft ->
                    viewModel.updatePreset(draft)
                    viewModel.savePresetAndStart { navController.navigate("training") }
                },
                onSave = { name, id, draft, done, failed ->
                    viewModel.savePreset(name, id, draft, done, failed)
                },
                onSaved = {
                    navController.navigate("presets?saved=true") {
                        popUpTo("home")
                        launchSingleTop = true
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
                onCreateNew = { openEditor(TrainingPreset()) },
                onCopyPreset = { source -> openEditor(source.preset) },
                onRenamePreset = viewModel::renamePreset,
                onStartPreset = { savedPreset ->
                    viewModel.loadPreset(savedPreset)
                    viewModel.savePresetAndStart { navController.navigate("training") }
                },
                onEditPreset = { savedPreset ->
                    openEditor(savedPreset.preset, savedPreset)
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
            ExerciseEditor(
                moveId = moveId,
                allMoves = allMoves,
                overrideUri = moveOverrides[moveId],
                savedPresets = savedPresets,
                onSave = viewModel::saveExercise,
                onPreview = viewModel::previewExercise,
                onDelete = viewModel::removeCustomMove,
                onBack = { navController.popBackStack() }
            )
        }

        composable("training") {
            TrainingScreen(
                preset = preset,
                state = viewModel.trainingState,
                onPause = viewModel::pauseTraining,
                onResume = viewModel::resumeTraining,
                onRestart = viewModel::restartTraining,
                onStop = {
                    viewModel.stopTraining()
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                preset = (globalAudio ?: AudioLevels.from(preset)).applyTo(preset),
                legacyAudio = globalAudio == null,
                onUseLegacyAudio = { viewModel.saveGlobalAudio(null) },
                ttsSpeechRate = ttsSpeechRate,
                onTtsSpeechRateChange = viewModel::saveTtsSpeechRate,
                ttsVoiceName = ttsVoiceName,
                onTtsVoiceChange = viewModel::saveTtsVoiceName,
                uiLanguage = uiLanguage,
                onUiLanguageChange = viewModel::saveUiLanguage,
                onPresetChange = { viewModel.saveGlobalAudio(AudioLevels.from(it)) },
                onClose = { navController.popBackStack() }
            )
        }

        composable("move_audio_list") {
            MoveAudioListScreen(
                allMoves = allMoves,
                moveOverrides = moveOverrides,
                onAddMove = { name, category ->
                    viewModel.addCustomMove(name, category) { id -> navController.navigate("move_audio/$id") }
                },
                onTestMove = viewModel::testMoveAudio,
                onOpenMoveDetails = { id -> navController.navigate("move_audio/$id") },
                onBack = { navController.popBackStack() }
            )
        }
    }
    }
}
