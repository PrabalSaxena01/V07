package com.v07.v3.core.data

import android.content.Context
import com.v07.v3.core.data.repo.*

/**
 * Authoritative Repository Facade for the 4 Core V3 Modules + Plugin Platform Storage.
 * Delegates domain operations to specialized repositories in com.v07.v3.core.data.repo.*
 */
class EventRepository private constructor(private val appContext: Context) {

    val dbHelper: V07Database
        get() = V07Database.getInstance(appContext)

    init {
        try {
            val db = dbHelper.writableDatabase
            V07Database.ensureAllTables(db)
        } catch (e: Exception) {
            android.util.Log.w("EventRepository", "Failed to ensure schema at repository init: ${e.message}")
        }
    }

    // Domain Repositories
    val sessions = SessionRepository(dbHelper)
    val tasks = TaskRepository(dbHelper)
    val thoughts = ThoughtRepository(dbHelper)
    val screenTime = ScreenTimeRepository(dbHelper)
    val dayTracker = DayTrackerRepository(dbHelper)
    val recycleBin = RecycleBinRepository(dbHelper)
    val pluginStorage = PluginStorageRepository(dbHelper)
    val pluginEvents = PluginEventRepository(dbHelper)
    val cadenceLedger = CadenceLedgerRepository(dbHelper, recycleBin)
    val dataExplorer = DataExplorerRepository(dbHelper, pluginStorage)

    fun getSafeExcludeClause(db: android.database.sqlite.SQLiteDatabase? = null): String {
        return recycleBin.getSafeExcludeClause(db)
    }

    companion object {
        const val SAFE_EXCLUDE_CLAUSE = BaseRepository.SAFE_EXCLUDE_CLAUSE

        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(context: Context): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ─── CORE MODULE 1: SESSION TIMER ────────────────────────────────────────
    fun insertSession(session: V3Session): Boolean = sessions.insertSession(session)
    fun getActiveSession(): V3Session? = sessions.getActiveSession()
    fun stopActiveSession(endTime: Long, notes: String? = null): Boolean = sessions.stopActiveSession(endTime, notes)
    fun getTodaySessions(todayStartMs: Long): List<V3Session> = sessions.getTodaySessions(todayStartMs)
    fun getRecentSessions(limit: Int = 50, offset: Int = 0): List<V3Session> = sessions.getRecentSessions(limit, offset)
    fun deleteSession(id: String): Boolean = sessions.deleteSession(id)
    fun updateSession(session: V3Session): Boolean = sessions.updateSession(session)

    // ─── CORE MODULE 2: TASKS DISCIPLINE ────────────────────────────────────
    fun getTasks(status: String? = null): List<V3Task> = tasks.getTasks(status)
    fun insertTask(task: V3Task): Boolean = tasks.insertTask(task)
    fun updateTask(task: V3Task): Boolean = tasks.updateTask(task)
    fun updateTaskDetails(id: String, title: String? = null, priority: String? = null, deadline: String? = null): Boolean =
        tasks.updateTaskDetails(id, title, priority, deadline)
    fun setTaskStatus(id: String, status: String): Boolean = tasks.setTaskStatus(id, status)
    fun toggleTaskStatus(id: String): Boolean = tasks.toggleTaskStatus(id)
    fun deleteTask(id: String): Boolean = tasks.deleteTask(id)

    // ─── CORE MODULE 3: THOUGHTS & REFLECTIONS ──────────────────────────────
    fun insertThought(thought: V3Thought): Boolean = thoughts.insertThought(thought)
    fun getRecentThoughts(limit: Int = 50): List<V3Thought> = thoughts.getRecentThoughts(limit)
    fun getPinnedThoughts(): List<V3Thought> = thoughts.getPinnedThoughts()
    fun toggleThoughtPinned(id: String): Boolean = thoughts.toggleThoughtPinned(id)
    fun deleteThought(id: String): Boolean = thoughts.deleteThought(id)
    fun updateThought(thought: V3Thought): Boolean = thoughts.updateThought(thought)

    // ─── CORE MODULE 4: SCREEN TIME TELEMETRY ────────────────────────────────
    fun insertAppUsage(usage: V3AppUsage): Boolean = screenTime.insertAppUsage(usage)
    fun getAppUsageForDate(startMs: Long, endMs: Long): List<V3AppUsage> = screenTime.getAppUsageForDate(startMs, endMs)
    fun getAppUsageForDate(dateStr: String, startMs: Long = 0L, endMs: Long = 0L): List<V3AppUsage> =
        screenTime.getAppUsageForDate(dateStr, startMs, endMs)
    fun getAppUsageLogs(limit: Int = 200, offset: Int = 0): List<V3AppUsage> = screenTime.getAppUsageLogs(limit, offset)
    fun deleteAppUsage(id: String? = null): Boolean = screenTime.deleteAppUsage(id)

    // ─── CORE UTILITY 1: DAY TRACKER ────────────────────────────────────────
    fun insertDayTrackerItem(item: V3DayTrackerItem): Boolean = dayTracker.insertDayTrackerItem(item)
    fun getAllDayTrackerItems(): List<V3DayTrackerItem> = dayTracker.getAllDayTrackerItems()
    fun getPinnedDayTrackerItems(): List<V3DayTrackerItem> = dayTracker.getPinnedDayTrackerItems()
    fun toggleDayTrackerPinned(id: String): Boolean = dayTracker.toggleDayTrackerPinned(id)
    fun deleteDayTrackerItem(id: String): Boolean = dayTracker.deleteDayTrackerItem(id)
    fun updateDayTrackerItem(item: V3DayTrackerItem): Boolean = dayTracker.updateDayTrackerItem(item)

    // ─── CORE UTILITY 2: RECYCLE BIN & TOMBSTONES ───────────────────────────
    fun recycleItem(originalId: String, category: String, dataJson: String): Boolean =
        recycleBin.recycleItem(originalId, category, dataJson)
    fun isDeletedOrTombstoned(id: String): Boolean = recycleBin.isDeletedOrTombstoned(id)
    fun getAllTombstoneIds(): Set<String> = recycleBin.getAllTombstoneIds()
    fun recordTombstone(id: String, category: String = "general"): Boolean = recycleBin.recordTombstone(id, category)
    fun clearTombstone(id: String): Boolean = recycleBin.clearTombstone(id)
    fun getRecycleBinItems(limit: Int = 100): List<V3RecycleItem> = recycleBin.getRecycleBinItems(limit)
    fun restoreRecycleItem(recycleId: String): V3RecycleItem? = recycleBin.restoreRecycleItem(recycleId)
    fun purgeRecycleItem(recycleId: String): String? = recycleBin.purgeRecycleItem(recycleId)
    fun emptyRecycleBin(): List<String> = recycleBin.emptyRecycleBin()

    // ─── PLUGIN PLATFORM STORAGE ────────────────────────────────────────────
    fun getPluginValue(pluginId: String, key: String): String? = pluginStorage.getPluginValue(pluginId, key)
    fun setPluginValue(pluginId: String, key: String, valueJson: String): Boolean =
        pluginStorage.setPluginValue(pluginId, key, valueJson)
    fun deletePluginValue(pluginId: String, key: String): Boolean = pluginStorage.deletePluginValue(pluginId, key)
    fun getAllPluginKeys(pluginId: String): List<String> = pluginStorage.getAllPluginKeys(pluginId)

    // ─── PLUGIN PLATFORM DOMAIN EVENTS ──────────────────────────────────────
    fun insertPluginEvent(event: V3PluginEvent): Boolean = pluginEvents.insertPluginEvent(event)
    fun getPluginEvents(pluginId: String, limit: Int = 100): List<V3PluginEvent> =
        pluginEvents.getPluginEvents(pluginId, limit)
    fun getPluginEventsByCategory(pluginId: String, category: String, date: String? = null): List<V3PluginEvent> =
        pluginEvents.getPluginEventsByCategory(pluginId, category, date)
    fun deletePluginEvent(id: String): Boolean = pluginEvents.deletePluginEvent(id)
    fun getAllPluginEventsForSync(sinceTs: Long = 0L): List<V3PluginEvent> =
        pluginEvents.getAllPluginEventsForSync(sinceTs)

    // ─── SYNC WATERMARKS ────────────────────────────────────────────────────
    fun getSyncWatermark(collectionName: String): Long = pluginStorage.getSyncWatermark(collectionName)
    fun setSyncWatermark(collectionName: String, timestamp: Long): Boolean =
        pluginStorage.setSyncWatermark(collectionName, timestamp)

    // ─── DAILY TIMELINE ─────────────────────────────────────────────────────
    fun getDayTimeline(dateStr: String): V3DayTimeline = cadenceLedger.getDayTimeline(dateStr)

    // ─── CORE CADENCE & LIFE OS EVENTS ──────────────────────────────────────
    fun insertEvent(event: V3Event): Boolean = cadenceLedger.insertEvent(event)
    fun deleteEvent(id: String): Boolean = cadenceLedger.deleteEvent(id)
    fun deleteEntityEverywhere(id: String, category: String? = null): Boolean =
        cadenceLedger.deleteEntityEverywhere(id, category)
    fun batchDeleteEntities(ids: List<String>): Int = cadenceLedger.batchDeleteEntities(ids)
    fun batchRecycleEntities(items: List<Triple<String, String, String>>): Int =
        cadenceLedger.batchRecycleEntities(items)
    fun updateEvent(
        id: String,
        title: String? = null,
        content: String? = null,
        category: String? = null,
        metadataJson: String? = null
    ): Boolean = cadenceLedger.updateEvent(id, title, content, category, metadataJson)
    fun getEvents(category: String? = null, date: String? = null, limit: Int = 100): List<V3Event> =
        cadenceLedger.getEvents(category, date, limit)
    fun getEventById(id: String): V3Event? = cadenceLedger.getEventById(id)
    fun getAllEventsForSync(): List<V3Event> = cadenceLedger.getAllEventsForSync()

    // ─── SANCTUARY LEDGER ENGINE ────────────────────────────────────────────
    fun queryLedgerEntries(
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        query: String? = null,
        limit: Int = 200
    ): List<V3Event> = cadenceLedger.queryLedgerEntries(category, startDate, endDate, query, limit)

    // ─── UNIFIED DATA EXPLORER & PURGE ──────────────────────────────────────
    fun queryUnifiedEntries(
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        query: String? = null,
        limit: Int = 200
    ): List<V3UnifiedEntry> = dataExplorer.queryUnifiedEntries(category, startDate, endDate, query, limit)
    fun purgeAllData(keepPluginConfigs: Boolean = true): Boolean = dataExplorer.purgeAllData(keepPluginConfigs)
}
