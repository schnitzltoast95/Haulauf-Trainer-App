package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

data class MoveStat(val id: String, val name: String, val category: MoveCategory, val count: Int, val deleted: Boolean)
data class StatisticsSnapshot(
    val totalCalls: Int = 0,
    val totalCuts: Int = 0,
    val yearlyCuts: Int = 0,
    val finishedTrainings: Int = 0,
    val moves: List<MoveStat> = emptyList(),
    val badgeCounts: Map<String, Int> = emptyMap(),
    val challenge: WeeklyChallenge = WeeklyChallenge(0, 0, 30),
)

/** One row per completed reaction window. Session and unit form an idempotency key. */
class StatisticsRepository(context: Context) : SQLiteOpenHelper(context, "training_statistics.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE sessions (id TEXT PRIMARY KEY, started_at INTEGER NOT NULL, status TEXT NOT NULL)")
        db.execSQL("CREATE TABLE calls (session_id TEXT NOT NULL, unit_index INTEGER NOT NULL, move_id TEXT NOT NULL, move_name TEXT NOT NULL, category TEXT NOT NULL, base_move_id TEXT, completed_at INTEGER NOT NULL, PRIMARY KEY(session_id, unit_index))")
        db.execSQL("CREATE INDEX calls_time ON calls(completed_at)")
        db.execSQL("CREATE INDEX calls_move ON calls(move_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    suspend fun beginSession(): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        writableDatabase.insertOrThrow("sessions", null, ContentValues().apply {
            put("id", id)
            put("started_at", System.currentTimeMillis())
            put("status", "running")
        })
        id
    }

    suspend fun recordCall(sessionId: String, unit: Int, move: Move) = withContext(Dispatchers.IO) {
        writableDatabase.insertWithOnConflict("calls", null, ContentValues().apply {
            put("session_id", sessionId)
            put("unit_index", unit)
            put("move_id", move.id)
            put("move_name", move.trainingName)
            put("category", move.category.name)
            put("base_move_id", move.identity.baseMoveId)
            put("completed_at", System.currentTimeMillis())
        }, SQLiteDatabase.CONFLICT_IGNORE)
        Unit
    }

    suspend fun finishSession(sessionId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        writableDatabase.execSQL(
            "UPDATE sessions SET status = ? WHERE id = ? AND status = 'running'",
            arrayOf(if (completed) "finished" else "stopped", sessionId)
        )
    }

    suspend fun snapshot(activeMoves: List<Move>): StatisticsSnapshot = withContext(Dispatchers.IO) {
        val now = Calendar.getInstance()
        val yearStart = (now.clone() as Calendar).apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val weekStart = (now.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nextWeek = (weekStart.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, 1) }.timeInMillis
        val weekNumber = (weekStart.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }.get(Calendar.WEEK_OF_YEAR)
        val challengeType = Math.floorMod(weekStart.get(Calendar.YEAR) * 53 + weekNumber, 3)

        val records = mutableListOf<CallRecord>()
        readableDatabase.rawQuery(
            "SELECT move_id, move_name, category, base_move_id, completed_at FROM calls ORDER BY completed_at, session_id, unit_index",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) records += CallRecord(
                cursor.getString(0), cursor.getString(1),
                runCatching { MoveCategory.valueOf(cursor.getString(2)) }.getOrDefault(MoveCategory.HAU),
                cursor.getString(3), cursor.getLong(4)
            )
        }
        val activeById = activeMoves.associateBy { it.id }
        val counts = records.groupingBy { it.moveId }.eachCount()
        val badgeCounts = counts.toMutableMap()
        records.forEach { record ->
            record.baseMoveId?.takeIf { it != record.moveId && it in activeById }?.let { baseId ->
                badgeCounts[baseId] = (badgeCounts[baseId] ?: 0) + 1
            }
        }
        val names = records.associate { it.moveId to it.moveName }
        val categories = records.associate { it.moveId to it.category }
        val moves = (activeMoves.map { it.id } + counts.keys).distinct().map { id ->
            val active = activeById[id]
            MoveStat(id, active?.trainingName ?: names[id].orEmpty(),
                active?.category ?: categories[id] ?: MoveCategory.HAU,
                counts[id] ?: 0, active == null)
        }.sortedWith(compareByDescending<MoveStat> { it.count }.thenBy { it.name })

        val thisWeek = records.filter { it.completedAt >= weekStart.timeInMillis && it.completedAt < nextWeek }
        val progress = when (challengeType) {
            0 -> thisWeek.count { it.category == MoveCategory.HAU }
            1 -> thisWeek.map { record ->
                (now.clone() as Calendar).apply { timeInMillis = record.completedAt }.let {
                    "${it.get(Calendar.YEAR)}-${it.get(Calendar.DAY_OF_YEAR)}"
                }
            }.distinct().size
            else -> thisWeek.filter { it.category == MoveCategory.HAU }.map { it.moveId }.distinct().size
        }
        val target = when (challengeType) { 0 -> 30; 1 -> 3; else -> 5 }
        val finishedTrainings = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM sessions WHERE status = 'finished'", null
        ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) }
        StatisticsSnapshot(
            totalCalls = records.size,
            totalCuts = records.count { it.category == MoveCategory.HAU },
            yearlyCuts = records.count { it.category == MoveCategory.HAU && it.completedAt >= yearStart },
            finishedTrainings = finishedTrainings,
            moves = moves,
            badgeCounts = badgeCounts,
            challenge = WeeklyChallenge(challengeType, progress, target),
        )
    }

    private data class CallRecord(
        val moveId: String, val moveName: String, val category: MoveCategory,
        val baseMoveId: String?, val completedAt: Long,
    )
}

data class WeeklyChallenge(val type: Int, val progress: Int, val target: Int)
