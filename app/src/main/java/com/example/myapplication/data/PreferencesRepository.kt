package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "haulauf_prefs")

class PreferencesRepository(private val context: Context) {
    private val minMovePriority = 1
    private val maxMovePriority = 3

    val lastUsedPreset: Flow<TrainingPreset?> = context.dataStore.data.map { prefs ->
        prefs[ROUNDS]?.let { rounds ->
            TrainingPreset(
                rounds = rounds,
                unitsPerRound = prefs[UNITS_PER_ROUND] ?: 25,
                reactionIntervalMs = prefs[REACTION_INTERVAL_MS] ?: 1200L,
                reactionIntervalMinMs = prefs[REACTION_MIN_MS],
                reactionIntervalMaxMs = prefs[REACTION_MAX_MS],
                pauseBetweenRoundsMs = prefs[PAUSE_MS] ?: 10000L,
                selectedMoveIds = prefs[SELECTED_MOVES] ?: DEFAULT_MOVES.map { it.id }.toSet(),
                endBeepEnabled = prefs[END_BEEP_ENABLED] ?: true,
                metronomeEnabled = prefs[METRONOME_ENABLED] ?: false,
                metronomeBeatIntervalMs = normalizeMetronomeBeats(prefs[METRONOME_INTERVAL_MS]),
                noImmediateRepetition = prefs[NO_IMMEDIATE_REP] ?: false,
                initialCountdownMs = prefs[INITIAL_COUNTDOWN_MS] ?: 5000L,
                movePriorities = parseMovePriorities(prefs[MOVE_PRIORITIES]),
                volumeCall = prefs[VOLUME_CALL] ?: 1f,
                volumeBeep = prefs[VOLUME_BEEP] ?: 0.75f,
                volumeTick = prefs[VOLUME_TICK] ?: 0.25f,
            )
        }
    }

    val customMoves: Flow<List<Move>> = context.dataStore.data.map { prefs ->
        val moveInfoMap = prefs[MOVE_INFO_KEY]?.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 2) {
                val description = if (parts.size >= 2 && parts[1].isNotEmpty()) parts[1] else null
                val imagePath = if (parts.size >= 3 && parts[2].isNotEmpty()) parts[2] else null
                parts[0] to (description to imagePath)
            } else null
        }?.toMap() ?: emptyMap()

        prefs[CUSTOM_MOVES_KEY]?.mapNotNull { entry ->
            val parts = entry.split("|")
            when {
                parts.size >= 3 -> {
                    val cat = try { MoveCategory.valueOf(parts[2]) } catch (_: Exception) { MoveCategory.HAU }
                    val info = moveInfoMap[parts[0]]
                    Move(parts[0], if (parts[1].startsWith("v2:")) android.net.Uri.decode(parts[1].removePrefix("v2:")) else parts[1], cat, info?.first, info?.second)
                }
                parts.size == 2 -> {
                    val info = moveInfoMap[parts[0]]
                    Move(parts[0], parts[1], MoveCategory.HAU, info?.first, info?.second)
                }
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
            prefs[METRONOME_INTERVAL_MS] = normalizeMetronomeBeats(preset.metronomeBeatIntervalMs)
            prefs[NO_IMMEDIATE_REP] = preset.noImmediateRepetition
            prefs[INITIAL_COUNTDOWN_MS] = preset.initialCountdownMs.coerceIn(1000L, 30000L)
            val priorities = preset.movePriorities
                .filterKeys { it in preset.selectedMoveIds }
                .mapValues { (_, priority) -> priority.coerceIn(minMovePriority, maxMovePriority) }
            if (priorities.isNotEmpty()) {
                prefs[MOVE_PRIORITIES] = priorities.map { (id, priority) -> "$id|$priority" }.toSet()
            } else {
                prefs.remove(MOVE_PRIORITIES)
            }
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
            current.add("${move.id}|v2:${android.net.Uri.encode(move.displayName)}|${move.category.name}")
            prefs[CUSTOM_MOVES_KEY] = current
        }
        return move
    }

    suspend fun removeCustomMove(moveId: String) {
        require(moveId.startsWith("custom_"))
        val obsolete = mutableSetOf<String>()
        context.dataStore.edit { prefs ->
            obsolete.addAll(ownedAttachmentPaths(JSONObject(prefs[MOVE_DETAILS_JSON] ?: "{}").optJSONObject(moveId)))
            obsolete.addAll(ownedAudioPaths(prefs[STRING_SET_KEY].orEmpty().filter { it.startsWith("$moveId|") }))
            for (key in listOf(CUSTOM_MOVES_KEY, MOVE_INFO_KEY, STRING_SET_KEY)) {
                prefs[key] = prefs[key].orEmpty().filterNot { it.startsWith("$moveId|") }.toSet()
            }
            val details = JSONObject(prefs[MOVE_DETAILS_JSON] ?: "{}")
            details.remove(moveId)
            prefs[MOVE_DETAILS_JSON] = details.toString()
            excludeReferencedPaths(obsolete, details)
            obsolete.removeAll(ownedAudioPaths(prefs[STRING_SET_KEY].orEmpty()).toSet())
            prefs[SELECTED_MOVES]?.let { prefs[SELECTED_MOVES] = it - moveId }
            prefs[MOVE_PRIORITIES] = prefs[MOVE_PRIORITIES].orEmpty().filterNot { it.startsWith("$moveId|") }.toSet()
            val templates = JSONArray(prefs[SAVED_PRESETS_KEY] ?: "[]")
            for (i in 0 until templates.length()) {
                val template = templates.getJSONObject(i).getJSONObject("preset")
                val ids = template.getJSONArray("selectedMoveIds")
                template.put("selectedMoveIds", JSONArray((0 until ids.length()).map { ids.getString(it) }.filterNot { it == moveId }))
                template.optJSONObject("movePriorities")?.remove(moveId)
            }
            prefs[SAVED_PRESETS_KEY] = templates.toString()
        }
        deleteOwnedAttachments(obsolete)
    }

    /** One transaction: readers never observe half-saved exercise details. */
    suspend fun saveExercise(move: Move, audioUri: String?) {
        require(move.displayName.isNotBlank())
        val obsolete = mutableSetOf<String>()
        context.dataStore.edit { prefs ->
            if (move.id.startsWith("custom_")) {
                val custom = prefs[CUSTOM_MOVES_KEY].orEmpty().filterNot { it.startsWith("${move.id}|") }.toMutableSet()
                custom.add("${move.id}|v2:${android.net.Uri.encode(move.displayName)}|${move.category.name}")
                prefs[CUSTOM_MOVES_KEY] = custom
            }
            val details = JSONObject(prefs[MOVE_DETAILS_JSON] ?: "{}")
            obsolete.addAll(ownedAttachmentPaths(details.optJSONObject(move.id)))
            obsolete.addAll(ownedAudioPaths(prefs[STRING_SET_KEY].orEmpty().filter { it.startsWith("${move.id}|") }))
            details.put(move.id, JSONObject().apply {
                put("description", (DEFAULT_MOVES.find { it.id == move.id } ?: move).description ?: JSONObject.NULL)
                put("imagePath", move.imagePath ?: JSONObject.NULL)
                put("notes", move.notes)
                put("media", JSONArray().apply {
                    move.media.forEach { item -> put(JSONObject().put("path", item.path).put("kind", item.kind.name)) }
                })
            })
            prefs[MOVE_DETAILS_JSON] = details.toString()
            excludeReferencedPaths(obsolete, details)
            val audio = prefs[STRING_SET_KEY].orEmpty().filterNot { it.startsWith("${move.id}|") }.toMutableSet()
            audioUri?.let { audio.add("${move.id}|$it") }
            prefs[STRING_SET_KEY] = audio
            obsolete.removeAll(ownedAudioPaths(audio).toSet())
        }
        deleteOwnedAttachments(obsolete)
    }

    private fun ownedAudioPaths(entries: Collection<String>): List<String> = entries.mapNotNull { entry ->
        val uri = android.net.Uri.parse(entry.substringAfter('|', ""))
        if (uri.scheme == "file") uri.path else null
    }

    private fun ownedAttachmentPaths(item: JSONObject?): List<String> {
        if (item == null) return emptyList()
        val media = item.optJSONArray("media") ?: JSONArray()
        return listOfNotNull(item.optString("imagePath", "").takeIf { it.isNotBlank() }) +
            (0 until media.length()).mapNotNull { media.optJSONObject(it)?.optString("path") }
    }

    private fun excludeReferencedPaths(obsolete: MutableSet<String>, details: JSONObject) {
        details.keys().forEach { id -> obsolete.removeAll(ownedAttachmentPaths(details.optJSONObject(id)).toSet()) }
    }

    private suspend fun deleteOwnedAttachments(paths: Set<String>) = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, "exercise-media").canonicalFile
        paths.forEach { path ->
            // Never remove external legacy documents or files outside the app's media directory.
            runCatching { File(path).canonicalFile.takeIf { it.parentFile == directory }?.delete() }
        }
    }

    val exerciseAdditions: Flow<Map<String, ExerciseAdditions>> = context.dataStore.data.map { prefs ->
        val details = JSONObject(prefs[MOVE_DETAILS_JSON] ?: "{}")
        buildMap {
            details.keys().forEach { id ->
                val item = details.getJSONObject(id)
                // Missing means legacy data: retain its old description and image through migration.
                if (item.has("notes")) {
                    val media = item.optJSONArray("media") ?: JSONArray()
                    put(id, ExerciseAdditions(item.optString("notes", ""), (0 until media.length()).mapNotNull { index ->
                        val attachment = media.optJSONObject(index) ?: return@mapNotNull null
                        val path = attachment.optString("path", "")
                        val kind = runCatching { ExerciseMediaKind.valueOf(attachment.optString("kind", "IMAGE")) }.getOrNull()
                        if (path.isBlank() || kind == null) null else ExerciseMedia(path, kind)
                    }))
                }
            }
        }
    }

    val globalAudio: Flow<AudioLevels?> = context.dataStore.data.map { prefs ->
        prefs[GLOBAL_AUDIO]?.let { raw ->
            val obj = JSONObject(raw)
            AudioLevels(obj.getDouble("call").toFloat(), obj.getDouble("beep").toFloat(), obj.getDouble("tick").toFloat())
        }
    }

    suspend fun saveGlobalAudio(levels: AudioLevels?) {
        context.dataStore.edit { prefs ->
            if (levels == null) prefs.remove(GLOBAL_AUDIO)
            else prefs[GLOBAL_AUDIO] = JSONObject().apply {
                put("call", levels.call.toDouble()); put("beep", levels.beep.toDouble()); put("tick", levels.tick.toDouble())
            }.toString()
        }
    }

    suspend fun updateCustomMoveTitle(moveId: String, newTitle: String) {
        val trimmedTitle = newTitle.trim()
        if (trimmedTitle.isEmpty()) return

        context.dataStore.edit { prefs ->
            val current = prefs[CUSTOM_MOVES_KEY] ?: emptySet()
            val updated = current.map { entry ->
                val parts = entry.split("|")
                if (parts.isNotEmpty() && parts[0] == moveId) {
                    val category = if (parts.size >= 3) parts[2] else MoveCategory.HAU.name
                    "$moveId|$trimmedTitle|$category"
                } else {
                    entry
                }
            }.toSet()
            prefs[CUSTOM_MOVES_KEY] = updated
        }
    }

    suspend fun getLastUsedPreset(): TrainingPreset? = lastUsedPreset.first()
    suspend fun getMoveAudioOverrides(): Map<String, String> = moveAudioOverrides.first()

    val savedPresets: Flow<List<SavedPreset>> = context.dataStore.data.map { prefs ->
        prefs[SAVED_PRESETS_KEY]?.let { jsonString ->
            try {
                val jsonArray = JSONArray(jsonString)
                (0 until jsonArray.length()).mapNotNull { i ->
                    val obj = jsonArray.getJSONObject(i)
                    val presetJson = obj.getJSONObject("preset")
                    SavedPreset(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        preset = TrainingPreset(
                            rounds = presetJson.getInt("rounds"),
                            unitsPerRound = presetJson.getInt("unitsPerRound"),
                            reactionIntervalMs = presetJson.getLong("reactionIntervalMs"),
                            reactionIntervalMinMs = if (presetJson.has("reactionIntervalMinMs") && !presetJson.isNull("reactionIntervalMinMs")) presetJson.getLong("reactionIntervalMinMs") else null,
                            reactionIntervalMaxMs = if (presetJson.has("reactionIntervalMaxMs") && !presetJson.isNull("reactionIntervalMaxMs")) presetJson.getLong("reactionIntervalMaxMs") else null,
                            pauseBetweenRoundsMs = presetJson.getLong("pauseBetweenRoundsMs"),
                            selectedMoveIds = presetJson.getJSONArray("selectedMoveIds").let { arr ->
                                (0 until arr.length()).map { arr.getString(it) }.toSet()
                            },
                            endBeepEnabled = presetJson.getBoolean("endBeepEnabled"),
                            metronomeEnabled = presetJson.getBoolean("metronomeEnabled"),
                            metronomeBeatIntervalMs = normalizeMetronomeBeats(presetJson.getLong("metronomeBeatIntervalMs")),
                            noImmediateRepetition = presetJson.getBoolean("noImmediateRepetition"),
                            initialCountdownMs = if (presetJson.has("initialCountdownMs") && !presetJson.isNull("initialCountdownMs")) presetJson.getLong("initialCountdownMs") else 5000L,
                            movePriorities = if (presetJson.has("movePriorities") && !presetJson.isNull("movePriorities")) {
                                val prioritiesJson = presetJson.getJSONObject("movePriorities")
                                val parsed = mutableMapOf<String, Int>()
                                val keys = prioritiesJson.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    parsed[key] = prioritiesJson.optInt(key, minMovePriority)
                                        .coerceIn(minMovePriority, maxMovePriority)
                                }
                                parsed
                            } else {
                                emptyMap()
                            },
                            volumeCall = presetJson.getDouble("volumeCall").toFloat(),
                            volumeBeep = presetJson.getDouble("volumeBeep").toFloat(),
                            volumeTick = presetJson.getDouble("volumeTick").toFloat(),
                        )
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    val ttsSpeechRate: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[TTS_SPEECH_RATE_KEY] ?: 1.4f
    }

    val ttsVoiceName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TTS_VOICE_NAME_KEY]
    }

    val uiLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[UI_LANGUAGE_KEY] ?: "de"
    }

    val moveInfo: Flow<Map<String, Pair<String?, String?>>> = context.dataStore.data.map { prefs ->
        prefs[MOVE_INFO_KEY]?.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 2) {
                val description = if (parts.size >= 2 && parts[1].isNotEmpty()) parts[1] else null
                val imagePath = if (parts.size >= 3 && parts[2].isNotEmpty()) parts[2] else null
                parts[0] to (description to imagePath)
            } else null
        }?.toMap().orEmpty().toMutableMap().apply {
            val details = JSONObject(prefs[MOVE_DETAILS_JSON] ?: "{}")
            details.keys().forEach { id ->
                val item = details.getJSONObject(id)
                put(id, (if (item.isNull("description")) null else item.getString("description")) to
                    (if (item.isNull("imagePath")) null else item.getString("imagePath")))
            }
        }
    }

    suspend fun savePreset(savedPreset: SavedPreset) {
        context.dataStore.edit { prefs ->
            val current = prefs[SAVED_PRESETS_KEY]?.let { jsonString ->
                try {
                    JSONArray(jsonString)
                } catch (e: Exception) {
                    JSONArray()
                }
            } ?: JSONArray()

            // Remove existing preset with same ID if exists
            val newArray = JSONArray()
            for (i in 0 until current.length()) {
                val obj = current.getJSONObject(i)
                if (obj.getString("id") != savedPreset.id) {
                    newArray.put(obj)
                }
            }

            // Add/update preset
            val presetJson = JSONObject().apply {
                put("rounds", savedPreset.preset.rounds)
                put("unitsPerRound", savedPreset.preset.unitsPerRound)
                put("reactionIntervalMs", savedPreset.preset.reactionIntervalMs)
                put("reactionIntervalMinMs", savedPreset.preset.reactionIntervalMinMs ?: JSONObject.NULL)
                put("reactionIntervalMaxMs", savedPreset.preset.reactionIntervalMaxMs ?: JSONObject.NULL)
                put("pauseBetweenRoundsMs", savedPreset.preset.pauseBetweenRoundsMs)
                put("selectedMoveIds", JSONArray(savedPreset.preset.selectedMoveIds.toList()))
                put("endBeepEnabled", savedPreset.preset.endBeepEnabled)
                put("metronomeEnabled", savedPreset.preset.metronomeEnabled)
                put("metronomeBeatIntervalMs", normalizeMetronomeBeats(savedPreset.preset.metronomeBeatIntervalMs))
                put("noImmediateRepetition", savedPreset.preset.noImmediateRepetition)
                put("initialCountdownMs", savedPreset.preset.initialCountdownMs.coerceIn(1000L, 30000L))
                put("movePriorities", JSONObject().apply {
                    savedPreset.preset.movePriorities
                        .filterKeys { it in savedPreset.preset.selectedMoveIds }
                        .forEach { (id, priority) ->
                            put(id, priority.coerceIn(minMovePriority, maxMovePriority))
                        }
                })
                put("volumeCall", savedPreset.preset.volumeCall.toDouble())
                put("volumeBeep", savedPreset.preset.volumeBeep.toDouble())
                put("volumeTick", savedPreset.preset.volumeTick.toDouble())
            }

            val savedPresetJson = JSONObject().apply {
                put("id", savedPreset.id)
                put("name", savedPreset.name)
                put("preset", presetJson)
            }
            newArray.put(savedPresetJson)
            prefs[SAVED_PRESETS_KEY] = newArray.toString()
        }
    }

    suspend fun renamePreset(presetId: String, name: String) {
        require(name.isNotBlank())
        context.dataStore.edit { prefs ->
            val current = JSONArray(prefs[SAVED_PRESETS_KEY] ?: "[]")
            val item = (0 until current.length()).map { current.getJSONObject(it) }
                .firstOrNull { it.getString("id") == presetId }
                ?: error("Training no longer exists")
            item.put("name", name.trim())
            prefs[SAVED_PRESETS_KEY] = current.toString()
        }
    }

    suspend fun deletePreset(presetId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SAVED_PRESETS_KEY]?.let { jsonString ->
                try {
                    JSONArray(jsonString)
                } catch (e: Exception) {
                    JSONArray()
                }
            } ?: JSONArray()

            val newArray = JSONArray()
            for (i in 0 until current.length()) {
                val obj = current.getJSONObject(i)
                if (obj.getString("id") != presetId) {
                    newArray.put(obj)
                }
            }
            prefs[SAVED_PRESETS_KEY] = newArray.toString()
        }
    }

    suspend fun saveMoveInfo(moveId: String, description: String?, imagePath: String?) {
        context.dataStore.edit { prefs ->
            val current = prefs[MOVE_INFO_KEY]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$moveId|") }
            val entry = "$moveId|${description ?: ""}|${imagePath ?: ""}"
            current.add(entry)
            prefs[MOVE_INFO_KEY] = current
        }
    }

    suspend fun getSavedPresets(): List<SavedPreset> = savedPresets.first()
    suspend fun getMoveInfo(): Map<String, Pair<String?, String?>> = moveInfo.first()

    suspend fun getTtsSpeechRate(): Float = ttsSpeechRate.first()
    suspend fun getTtsVoiceName(): String? = ttsVoiceName.first()
    suspend fun getUiLanguage(): String = uiLanguage.first()

    suspend fun saveTtsSpeechRate(rate: Float) {
        context.dataStore.edit { prefs ->
            prefs[TTS_SPEECH_RATE_KEY] = rate.coerceIn(0.5f, 2f)
        }
    }

    suspend fun saveTtsVoiceName(name: String?) {
        context.dataStore.edit { prefs ->
            if (name.isNullOrBlank()) prefs.remove(TTS_VOICE_NAME_KEY)
            else prefs[TTS_VOICE_NAME_KEY] = name
        }
    }

    suspend fun saveUiLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[UI_LANGUAGE_KEY] = if (lang in listOf("de", "en")) lang else "de"
        }
    }

    companion object {
        private val MOVE_DETAILS_JSON = stringPreferencesKey("move_details_v2")
        private val GLOBAL_AUDIO = stringPreferencesKey("global_audio_v1")
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
        private val INITIAL_COUNTDOWN_MS = longPreferencesKey("initial_countdown_ms")
        private val MOVE_PRIORITIES = stringSetPreferencesKey("move_priorities")
        private val VOLUME_CALL = floatPreferencesKey("volume_call")
        private val VOLUME_BEEP = floatPreferencesKey("volume_beep")
        private val VOLUME_TICK = floatPreferencesKey("volume_tick")
        private val STRING_SET_KEY = stringSetPreferencesKey("move_audio_overrides")
        private val CUSTOM_MOVES_KEY = stringSetPreferencesKey("custom_moves")
        private val SAVED_PRESETS_KEY = stringPreferencesKey("saved_presets")
        private val MOVE_INFO_KEY = stringSetPreferencesKey("move_info")
        private val TTS_SPEECH_RATE_KEY = floatPreferencesKey("tts_speech_rate")
        private val TTS_VOICE_NAME_KEY = stringPreferencesKey("tts_voice_name")
        private val UI_LANGUAGE_KEY = stringPreferencesKey("ui_language")
    }

    private fun parseMovePriorities(entries: Set<String>?): Map<String, Int> {
        return entries?.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size != 2) return@mapNotNull null
            val moveId = parts[0].trim()
            val priority = parts[1].toIntOrNull()
                ?.coerceIn(minMovePriority, maxMovePriority) ?: return@mapNotNull null
            moveId to priority
        }?.toMap() ?: emptyMap()
    }

    /**
     * Migration-safe normalization:
     * - current format stores beat count in [1, 32]
     * - legacy format stored milliseconds (e.g. 500, 1000, 2000)
     */
    private fun normalizeMetronomeBeats(raw: Long?): Long {
        val value = raw ?: 2L
        return when {
            value < 1L -> 2L
            value <= 32L -> value
            else -> (value / 1000L).coerceIn(1L, 32L)
        }
    }
}
