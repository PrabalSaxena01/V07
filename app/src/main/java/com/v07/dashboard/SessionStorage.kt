package com.v07.dashboard

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class ActiveSession(
    val id: String = UUID.randomUUID().toString(),
    val activity: String = "",
    val customLabel: String? = null,
    val mode: String = "countdown",
    val status: String = "running",
    val startedAt: Long = System.currentTimeMillis(),
    val pauseStartedAt: Long? = null,
    val pausedDuration: Long = 0L,
    val targetDuration: Long = 0L,
    val dailyTarget: Int = 30
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("activity", activity)
        put("customLabel", customLabel ?: JSONObject.NULL)
        put("mode", mode)
        put("status", status)
        put("startedAt", startedAt)
        put("pauseStartedAt", pauseStartedAt ?: JSONObject.NULL)
        put("pausedDuration", pausedDuration)
        put("targetDuration", targetDuration)
        put("dailyTarget", dailyTarget)
    }

    companion object {
        fun fromJson(obj: JSONObject): ActiveSession = ActiveSession(
            id = obj.optString("id", UUID.randomUUID().toString()),
            activity = obj.optString("activity", ""),
            customLabel = if (obj.isNull("customLabel")) null else obj.optString("customLabel"),
            mode = obj.optString("mode", "countdown"),
            status = obj.optString("status", "running"),
            startedAt = obj.optLong("startedAt", System.currentTimeMillis()),
            pauseStartedAt = if (obj.isNull("pauseStartedAt")) null else obj.optLong("pauseStartedAt"),
            pausedDuration = obj.optLong("pausedDuration", 0L),
            targetDuration = obj.optLong("targetDuration", 0L),
            dailyTarget = obj.optInt("dailyTarget", 30)
        )
    }
}

data class CompletedSession(
    val id: String,
    val activity: String,
    val customLabel: String? = null,
    val startedAt: Long,
    val endedAt: Long,
    val workedMs: Long,
    val pausedMs: Long,
    val targetDuration: Long,
    val date: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("activity", activity)
        put("customLabel", customLabel ?: JSONObject.NULL)
        put("startedAt", startedAt)
        put("endedAt", endedAt)
        put("workedMs", workedMs)
        put("workedMinutes", (workedMs / 60000).toInt())
        put("pausedMs", pausedMs)
        put("pausedMinutes", (pausedMs / 60000).toInt())
        put("targetDuration", targetDuration)
        put("date", date)
    }

    companion object {
        fun fromJson(obj: JSONObject): CompletedSession = CompletedSession(
            id = obj.optString("id", ""),
            activity = obj.optString("activity", ""),
            customLabel = if (obj.isNull("customLabel")) null else obj.optString("customLabel"),
            startedAt = obj.optLong("startedAt", 0L),
            endedAt = obj.optLong("endedAt", 0L),
            workedMs = obj.optLong("workedMs", 0L),
            pausedMs = obj.optLong("pausedMs", 0L),
            targetDuration = obj.optLong("targetDuration", 0L),
            date = obj.optString("date", "")
        )
    }
}

object SessionStorage {

    private fun dataDir(): File {
        val dir = File(VaultConfig.filesDir, "sessions")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun activeFile() = File(dataDir(), "active_session.json")
    private fun historyFile() = File(dataDir(), "sessions.json")
    private fun dailyStatsFile() = File(dataDir(), "daily_stats.json")
    // ─── Active Session ────────────────────────────────────────────────────

    fun saveActiveSession(session: ActiveSession) {
        activeFile().writeText(session.toJson().toString(2))
    }

    fun loadActiveSession(): ActiveSession? {
        val f = activeFile()
        if (!f.exists()) return null
        return try {
            ActiveSession.fromJson(JSONObject(f.readText()))
        } catch (_: Exception) {
            null
        }
    }

    fun deleteActiveSession() {
        val f = activeFile()
        if (f.exists()) f.delete()
    }

    // ─── Session History ───────────────────────────────────────────────────

    fun appendSession(session: CompletedSession) {
        val arr = loadSessionHistory().toMutableList()
        arr.add(session)
        historyFile().writeText(
            JSONObject().apply {
                put("sessions", org.json.JSONArray().apply {
                    arr.forEach { put(it.toJson()) }
                })
            }.toString(2)
        )
    }

    fun loadSessionHistory(): List<CompletedSession> {
        val f = historyFile()
        if (!f.exists()) return emptyList()
        return try {
            val obj = JSONObject(f.readText())
            val arr = obj.optJSONArray("sessions") ?: return emptyList()
            (0 until arr.length()).mapNotNull { i ->
                try { CompletedSession.fromJson(arr.getJSONObject(i)) } catch (_: Exception) { null }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ─── Daily Stats ───────────────────────────────────────────────────────

    private fun loadDailyStats(): JSONObject {
        val f = dailyStatsFile()
        if (!f.exists()) return JSONObject()
        return try { JSONObject(f.readText()) } catch (_: Exception) { JSONObject() }
    }

    private fun saveDailyStats(stats: JSONObject) {
        dailyStatsFile().writeText(stats.toString(2))
    }

    fun recordSessionComplete(session: CompletedSession) {
        val stats = loadDailyStats()
        val day = session.date.ifEmpty {
            java.time.Instant.ofEpochMilli(session.endedAt)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate().toString()
        }
        val dayObj = stats.optJSONObject(day) ?: JSONObject()
        val actObj = dayObj.optJSONObject(session.activity) ?: JSONObject()
        val prevMin = actObj.optInt("minutes", 0)
        val prevSessions = actObj.optInt("sessions", 0)
        actObj.put("minutes", prevMin + (session.workedMs / 60000).toInt())
        actObj.put("sessions", prevSessions + 1)
        dayObj.put(session.activity, actObj)
        stats.put(day, dayObj)
        saveDailyStats(stats)
    }

    fun getTodayStats(): JSONObject {
        val today = java.time.LocalDate.now().toString()
        val stats = loadDailyStats()
        return stats.optJSONObject(today) ?: JSONObject()
    }

    fun getTodayMinutesForActivity(activity: String): Int {
        val todayStats = getTodayStats()
        return todayStats.optJSONObject(activity)?.optInt("minutes", 0) ?: 0
    }

    fun removeMinutesFromDailyStats(activity: String, minutes: Int, date: String) {
        val stats = loadDailyStats()
        val dayObj = stats.optJSONObject(date) ?: return
        val actObj = dayObj.optJSONObject(activity) ?: return
        val prevMin = actObj.optInt("minutes", 0)
        val prevSessions = actObj.optInt("sessions", 0)
        actObj.put("minutes", (prevMin - minutes).coerceAtLeast(0))
        actObj.put("sessions", (prevSessions - 1).coerceAtLeast(0))
        if (actObj.optInt("minutes", 0) <= 0) dayObj.remove(activity)
        stats.put(date, dayObj)
        saveDailyStats(stats)
    }

    fun addMinutesToDailyStats(activity: String, minutes: Int, date: String) {
        val stats = loadDailyStats()
        val dayObj = stats.optJSONObject(date) ?: JSONObject()
        val actObj = dayObj.optJSONObject(activity) ?: JSONObject()
        val prevMin = actObj.optInt("minutes", 0)
        val prevSessions = actObj.optInt("sessions", 0)
        actObj.put("minutes", prevMin + minutes)
        actObj.put("sessions", prevSessions + 1)
        dayObj.put(activity, actObj)
        stats.put(date, dayObj)
        saveDailyStats(stats)
    }

    fun rebuildDailyStatsForDate(date: String, activityMinutes: Map<String, Int>) {
        val stats = loadDailyStats()
        val dayObj = JSONObject()
        for ((activity, minutes) in activityMinutes) {
            val sessions = minutes / 25 + 1
            val actObj = JSONObject()
            actObj.put("minutes", minutes)
            actObj.put("sessions", sessions)
            dayObj.put(activity, actObj)
        }
        stats.put(date, dayObj)
        saveDailyStats(stats)
    }

    private fun deletedActivitiesFile() = File(dataDir(), "deleted_activities.json")

    private fun loadDeletedActivities(): Set<String> {
        val f = deletedActivitiesFile()
        if (!f.exists()) return emptySet()
        return try {
            val arr = JSONArray(f.readText())
            (0 until arr.length()).map { arr.optString(it) }.toSet()
        } catch (_: Exception) { emptySet() }
    }

    private fun saveDeletedActivities(activities: Set<String>) {
        deletedActivitiesFile().writeText(JSONArray(activities.toList()).toString())
    }

    fun deleteRecentActivity(name: String) {
        val deleted = loadDeletedActivities().toMutableSet()
        deleted.add(name)
        saveDeletedActivities(deleted)
    }

    private fun customActivitiesFile() = File(dataDir(), "custom_activities.json")

    private fun loadCustomActivities(): Set<String> {
        val f = customActivitiesFile()
        if (!f.exists()) return emptySet()
        return try {
            val arr = JSONArray(f.readText())
            (0 until arr.length()).map { arr.optString(it) }.toSet()
        } catch (_: Exception) { emptySet() }
    }

    private fun saveCustomActivities(activities: Set<String>) {
        customActivitiesFile().writeText(JSONArray(activities.toList()).toString())
    }

    fun addCustomActivity(name: String) {
        val custom = loadCustomActivities().toMutableSet()
        custom.add(name)
        saveCustomActivities(custom)
    }

    fun getRecentCustomActivities(limit: Int = 10): List<String> {
        val deleted = loadDeletedActivities()
        val explicitlyAdded = loadCustomActivities()
        val history = loadSessionHistory().asReversed()
        val seen = mutableSetOf<String>()
        val result = mutableListOf<String>()
        for (act in explicitlyAdded) {
            if (act !in deleted && act !in seen) {
                seen.add(act)
                result.add(act)
            }
        }
        for (session in history) {
            val act = session.activity
            if (act != null && act !in deleted && act !in seen) {
                seen.add(act)
                result.add(act)
                if (result.size >= limit) break
            }
        }
        return result
    }

    fun getAllActivities(): List<String> {
        val deleted = loadDeletedActivities()
        val custom = loadCustomActivities()
        val history = loadSessionHistory().asReversed()
        val result = linkedSetOf<String>()
        for (act in custom) {
            if (act !in deleted && act.isNotBlank()) result.add(act)
        }
        for (session in history) {
            val act = session.activity ?: continue
            if (act !in deleted && act.isNotBlank()) result.add(act)
        }
        return result.toList()
    }
}
