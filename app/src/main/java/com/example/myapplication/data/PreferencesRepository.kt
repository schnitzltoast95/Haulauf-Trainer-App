package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "haulauf_prefs")

class PreferencesRepository(private val context: Context) {

    val lastUsedPreset: Flow<TrainingPreset?> = context.dataStore.data.map { prefs ->
        prefs[ROUNDS]?.let { rounds ->
            TrainingPreset(
                rounds = rounds,
                unitsPerRound = prefs[UNITS_PER_ROUND] ?: 25,
                reactionIntervalMs = prefs[REACTION_INTERVAL_MS] ?: 6000L,
                reactionIntervalMinMs = prefs[REACTION_MIN_MS],
                reactionIntervalMaxMs = prefs[REACTION_MAX_MS],
                pauseBetweenRoundsMs = prefs[PAUSE_MS] ?: 10000L,
                selectedMoveIds = (prefs[SELECTED_MOVES] ?: emptySet()).ifEmpty {
                    DEFAULT_MOVES.map { it.id }.toSet()
                },
                endBeepEnabled = prefs[END_BEEP_ENABLED] ?: true,
                metronomeEnabled = prefs[METRONOME_ENABLED] ?: false,
                metronomeBeatIntervalMs = prefs[METRONOME_INTERVAL_MS] ?: 500L,
                noImmediateRepetition = prefs[NO_IMMEDIATE_REP] ?: false,
                volumeCall = prefs[VOLUME_CALL] ?: 1f,
                volumeBeep = prefs[VOLUME_BEEP] ?: 0.75f,
                volumeTick = prefs[VOLUME_TICK] ?: 0.25f,
            )
        }
    }

    val customMoves: Flow<List<Move>> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_MOVES_KEY]?.mapNotNull { entry ->
            val parts = entry.split("|")
            when {
                parts.size >= 3 -> {
                    val cat = try { MoveCategory.valueOf(parts[2]) } catch (_: Exception) { MoveCategory.HAU }
                    Move(parts[0], parts[1], cat)
                }
                parts.size == 2 -> Move(parts[0], parts[1], MoveCategory.HAU)
                else -> null
            }
        } ?: emptyList()
    }

    val moveAudioOverrides: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        prefs[STRING_SET_KEY]?.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 2) parts[0] to parts[1] else null
        }?.toMap() ?: emptyMap()
    }

    suspend fun saveLastUsedPreset(preset: TrainingPreset) {
        context.dataStore.edit { prefs ->
            prefs[ROUNDS] = preset.rounds
            prefs[UNITS_PER_ROUND] = preset.unitsPerRound
            prefs[REACTION_INTERVAL_MS] = preset.reactionIntervalMs
            if (preset.reactionIntervalMinMs != null) {
                prefs[REACTION_MIN_MS] = preset.reactionIntervalMinMs
            } else {
                prefs.remove(REACTION_MIN_MS)
            }
            if (preset.reactionIntervalMaxMs != null) {
                prefs[REACTION_MAX_MS] = preset.reactionIntervalMaxMs
            } else {
                prefs.remove(REACTION_MAX_MS)
            }
            prefs[PAUSE_MS] = preset.pauseBetweenRoundsMs
            prefs[SELECTED_MOVES] = preset.selectedMoveIds
            prefs[END_BEEP_ENABLED] = preset.endBeepEnabled
            prefs[METRONOME_ENABLED] = preset.metronomeEnabled
            prefs[METRONOME_INTERVAL_MS] = preset.metronomeBeatIntervalMs
            prefs[NO_IMMEDIATE_REP] = preset.noImmediateRepetition
            prefs[VOLUME_CALL] = preset.volumeCall
            prefs[VOLUME_BEEP] = preset.volumeBeep
            prefs[VOLUME_TICK] = preset.volumeTick
        }
    }

    suspend fun saveMoveAudioOverride(moveId: String, uriString: String?) {
        context.dataStore.edit { prefs ->
            val current = prefs[STRING_SET_KEY]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$moveId|") }
            uriString?.let { current.add("$moveId|$it") }
            prefs[STRING_SET_KEY] = current
        }
    }

    suspend fun addCustomMove(displayName: String, category: MoveCategory): Move {
        val id = "custom_${UUID.randomUUID()}"
        val move = Move(id, displayName.trim(), category)
        context.dataStore.edit { prefs ->
            val current = prefs[CUSTOM_MOVES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.add("${move.id}|${move.displayName}|${move.category.name}")
            prefs[CUSTOM_MOVES_KEY] = current
        }
        return move
    }

    suspend fun removeCustomMove(moveId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[CUSTOM_MOVES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$moveId|") }
            prefs[CUSTOM_MOVES_KEY] = current
        }
    }

    suspend fun getLastUsedPreset(): TrainingPreset? = lastUsedPreset.first()
    suspend fun getMoveAudioOverrides(): Map<String, String> = moveAudioOverrides.first()

    companion object {
        private val ROUNDS = intPreferencesKey("rounds")
        private val UNITS_PER_ROUND = intPreferencesKey("units_per_round")
        private val REACTION_INTERVAL_MS = longPreferencesKey("reaction_interval_ms")
        private val REACTION_MIN_MS = longPreferencesKey("reaction_min_ms")
        private val REACTION_MAX_MS = longPreferencesKey("reaction_max_ms")
        private val PAUSE_MS = longPreferencesKey("pause_ms")
        private val SELECTED_MOVES = stringSetPreferencesKey("selected_moves")
        private val END_BEEP_ENABLED = booleanPreferencesKey("end_beep_enabled")
        private val METRONOME_ENABLED = booleanPreferencesKey("metronome_enabled")
        private val METRONOME_INTERVAL_MS = longPreferencesKey("metronome_interval_ms")
        private val NO_IMMEDIATE_REP = booleanPreferencesKey("no_immediate_rep")
        private val VOLUME_CALL = floatPreferencesKey("volume_call")
        private val VOLUME_BEEP = floatPreferencesKey("volume_beep")
        private val VOLUME_TICK = floatPreferencesKey("volume_tick")
        private val STRING_SET_KEY = stringSetPreferencesKey("move_audio_overrides")
        private val CUSTOM_MOVES_KEY = stringSetPreferencesKey("custom_moves")
    }
}
