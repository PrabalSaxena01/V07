package com.v07.v3.core.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.core.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * V3CompanionSyncManager
 *
 * Provides bidirectional live cloud synchronization between V3 SQLite storage
 * and the V07 Companion Web Dashboard (v07-dashboard) via Firebase Firestore.
 * Handles schema migration, category normalization, timestamp standardization,
 * and format reconciliation across all modules.
 */
class V3CompanionSyncManager private constructor(
    private val context: Context,
    private val repository: EventRepository,
    private val screenTimeTracker: ScreenTimeTracker
) {
    companion object {
        private const val TAG = "V3CompanionSync"
        private const val PREFS_NAME = "v07_v3_sync_prefs"
        private const val KEY_LAST_SYNCED = "last_synced_timestamp"
        const val DEFAULT_COMPANION_UID = "5fwVgpra4pRzB1QqMWb9LSZSqrp2"

        @Volatile
        private var instance: V3CompanionSyncManager? = null

        fun getInstance(context: Context, repository: EventRepository, screenTimeTracker: ScreenTimeTracker): V3CompanionSyncManager {
            return instance ?: synchronized(this) {
                instance ?: V3CompanionSyncManager(
                    context.applicationContext,
                    repository,
                    screenTimeTracker
                ).also { instance = it }
            }
        }

        fun getInstance(context: Context, repository: EventRepository): V3CompanionSyncManager {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val tracker = ScreenTimeTracker(context.applicationContext, repository)
                    V3CompanionSyncManager(
                        context.applicationContext,
                        repository,
                        tracker
                    ).also { instance = it }
                }
            }
        }

        fun getInstance(context: Context): V3CompanionSyncManager {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val repo = EventRepository.getInstance(context.applicationContext)
                    val tracker = ScreenTimeTracker(context.applicationContext, repo)
                    V3CompanionSyncManager(
                        context.applicationContext,
                        repo,
                        tracker
                    ).also { instance = it }
                }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val isSyncing = AtomicBoolean(false)
    private val syncScope = CoroutineScope(Dispatchers.IO)
    private val realtimeRegistrations = mutableListOf<ListenerRegistration>()
    private var heartbeatJob: kotlinx.coroutines.Job? = null

    fun isCloudSyncEnabled(): Boolean {
        return getSafeAuth()?.currentUser != null
    }

    fun getAuthenticatedUser(): com.google.firebase.auth.FirebaseUser? = getSafeAuth()?.currentUser

    fun getAuthenticatedEmail(): String? = getSafeAuth()?.currentUser?.email

    fun isAnonymous(): Boolean = getSafeAuth()?.currentUser?.isAnonymous == true

    fun getTargetUid(): String {
        val user = getSafeAuth()?.currentUser
        if (user != null) return user.uid
        val custom = prefs.getString("custom_companion_uid", null)?.trim()
        if (!custom.isNullOrBlank()) return custom
        var guestUuid = prefs.getString("guest_device_uuid", null)
        if (guestUuid.isNullOrBlank()) {
            guestUuid = "guest_" + java.util.UUID.randomUUID().toString().replace("-", "")
            prefs.edit().putString("guest_device_uuid", guestUuid).apply()
        }
        return guestUuid
    }

    fun setTargetUid(uid: String) {
        val clean = uid.trim()
        if (clean.isNotBlank()) {
            prefs.edit().putString("custom_companion_uid", clean).apply()
            startRealtimeSync()
            syncNow { _, _ -> }
        }
    }

    fun getCustomCompanionUid(): String? {
        return prefs.getString("custom_companion_uid", null)?.trim()?.ifBlank { null }
    }

    fun enableCloudSync(uid: String? = null) {
        val target = uid?.trim()?.ifBlank { null }
            ?: getCustomCompanionUid()
            ?: DEFAULT_COMPANION_UID
        prefs.edit().putString("custom_companion_uid", target).apply()
        startRealtimeSync()
        syncNow { _, _ -> }
    }

    fun disableCloudSync() {
        prefs.edit().remove("custom_companion_uid").apply()
        stopRealtimeSync()
    }

    private fun ensureFirebaseInitialized(): Boolean {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBw_23Rap_Ja9-CKfDEce6F8ZYgBgjNgsc")
                    .setApplicationId("1:689369760342:android:1f03ec126b581e8916f1f7")
                    .setProjectId("v07-dashboard")
                    .setStorageBucket("v07-dashboard.firebasestorage.app")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(context, options)
                Log.i(TAG, "FirebaseApp programmatically initialized with options")
            }
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to ensure FirebaseApp: ${e.message}")
            return false
        }
    }

    private fun getSafeAuth(): FirebaseAuth? = try {
        ensureFirebaseInitialized()
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
        null
    }

    private fun getSafeFirestore(): FirebaseFirestore? = try {
        ensureFirebaseInitialized()
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "FirebaseFirestore unavailable: ${e.message}")
        null
    }

    fun getSyncStatus(): JSONObject {
        val auth = getSafeAuth()
        val user = auth?.currentUser
        val lastSynced = prefs.getLong(KEY_LAST_SYNCED, 0L)
        val activeUid = getTargetUid()

        return JSONObject().apply {
            put("authenticated", user != null)
            put("user_email", user?.email ?: "companion-user@v07-dashboard")
            put("uid", activeUid)
            put("active_profile", V07Database.getCurrentProfileId())
            put("available_profiles", JSONArray(V07Database.listProfiles(context)))
            put("last_synced_ms", lastSynced)
            put("last_synced_formatted", if (lastSynced > 0) formatRelativeTime(lastSynced) else "Never")
            put("is_syncing", isSyncing.get())
            put("realtime_active", realtimeRegistrations.isNotEmpty())
        }
    }

    fun switchProfile(profileId: String): Boolean {
        // Enforce strictly single-profile sovereign vault
        return true
    }

    fun listProfiles(): List<String> = listOf(V07Database.DEFAULT_PROFILE_ID)
    fun getActiveProfileId(): String = V07Database.DEFAULT_PROFILE_ID

    fun signIn(email: String, pass: String, createMode: Boolean, callback: (Boolean, String?) -> Unit) {
        val auth = getSafeAuth()
        if (auth == null) {
            callback(false, "Firebase authentication is not available")
            return
        }
        val task = if (createMode) {
            auth.createUserWithEmailAndPassword(email.trim(), pass)
        } else {
            auth.signInWithEmailAndPassword(email.trim(), pass)
        }

        task.addOnCompleteListener { t ->
            if (t.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    prefs.edit().putString("custom_companion_uid", user.uid).apply()
                    // Retain single default SQLite vault; zero profile switching
                    startRealtimeSync()
                    syncNow { _, _ -> }
                }
                callback(true, null)
            } else {
                callback(false, t.exception?.message ?: "Authentication failed")
            }
        }
    }

    fun signInAnonymously(callback: (Boolean, String?) -> Unit) {
        val auth = getSafeAuth()
        if (auth == null) {
            callback(false, "Firebase authentication is not available")
            return
        }
        auth.signInAnonymously().addOnCompleteListener { t ->
            if (t.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    prefs.edit().putString("custom_companion_uid", user.uid).apply()
                    // Retain single default SQLite vault; zero profile switching
                    startRealtimeSync()
                    syncNow { _, _ -> }
                }
                callback(true, null)
            } else {
                callback(false, t.exception?.message ?: "Anonymous sign-in failed")
            }
        }
    }

    fun signOut(onComplete: (() -> Unit)? = null) {
        stopRealtimeSync()
        try {
            getSafeAuth()?.signOut()
        } catch (_: Exception) {}
        prefs.edit().remove("custom_companion_uid").apply()
        try {
            val fs = getSafeFirestore()
            fs?.terminate()?.addOnCompleteListener {
                try {
                    fs.clearPersistence()
                } catch (_: Exception) {}
                onComplete?.invoke()
            } ?: run { onComplete?.invoke() }
        } catch (_: Exception) {
            onComplete?.invoke()
        }
        // Single local vault remains intact; zero data wiped or hidden
    }

    private val canonicalHabitsMap = mapOf(
        "hbt_ssb" to "⚔️ SSB Practice",
        "hbt_running" to "🏃 Running",
        "hbt_dsa" to "💻 DSA Problem Solving",
        "hbt_chess" to "♟️ Chess Training",
        "hbt_content_creation" to "✍️ Content Creation",
        "hbt_development" to "⚡ Development",
        "hbt_meditation" to "🧘 Meditation",
        "hmudx8p39" to "💪 Pushups",
        "hmud3qf88" to "🌿 Mindfulness",
        "hmud3qz2i" to "📚 Deep Study",
        "hmudmiwt3" to "✨ Daily Routine"
    )

    fun getHabitName(habitId: String): String {
        return canonicalHabitsMap[habitId] ?: habitId.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    /**
     * Pushes a single habit check-in/out to Firestore companion collection:
     * users/{uid}/plugins/com.v07.habits/events/hlog_{habitId}_{compactDate}
     */
    fun pushHabitLog(habitId: String, dateStr: String, isCompleted: Boolean) {
        if (!isCloudSyncEnabled()) return
        val compactDate = dateStr.replace("-", "")
        val docId = "hlog_${habitId}_$compactDate"
        val hbtEventId = "hbt_${habitId}_$dateStr"
        val now = System.currentTimeMillis()
        val habitName = getHabitName(habitId)

        syncScope.launch {
            try {
                val db = getSafeFirestore() ?: return@launch
                val uid = getTargetUid()
                val habitsCol = db.collection("users").document(uid)
                    .collection("plugins").document("com.v07.habits").collection("events")

                if (isCompleted) {
                    val map = hashMapOf<String, Any>(
                        "id" to docId,
                        "habit_id" to habitId,
                        "habit_name" to habitName,
                        "date" to dateStr,
                        "completed" to true,
                        "ts" to now,
                        "category" to "habits",
                        "updated_at" to now,
                        "_syncedAt" to Timestamp.now()
                    )
                    habitsCol.document(docId).set(map, SetOptions.merge())
                    Log.d(TAG, "Pushed habit check-in to Firestore: $docId")
                } else {
                    habitsCol.document(docId).delete()
                    deleteRemoteEvent(hbtEventId)
                    deleteRemoteEvent(docId)
                    Log.d(TAG, "Removed habit check-in from Firestore: $docId")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push habit log $docId: ${e.message}")
            }
        }
    }

    /**
     * Reconciles a habit document from Firestore into SQLite plugin_storage and events table.
     */
    fun reconcileHabitDoc(docId: String, data: Map<String, Any?>) {
        try {
            val habitId = data["habit_id"] as? String
                ?: data["habit"] as? String
                ?: (if (docId.startsWith("hlog_")) {
                    val rest = docId.removePrefix("hlog_")
                    val idx = rest.lastIndexOf('_')
                    if (idx > 0) rest.substring(0, idx) else rest
                } else null)
                ?: return

            val dateStr = data["date"] as? String
                ?: (if (docId.startsWith("hlog_")) {
                    val rest = docId.removePrefix("hlog_")
                    val idx = rest.lastIndexOf('_')
                    if (idx > 0 && rest.length - idx - 1 == 8) {
                        val raw = rest.substring(idx + 1)
                        "${raw.substring(0, 4)}-${raw.substring(4, 6)}-${raw.substring(6, 8)}"
                    } else null
                } else null)
                ?: return

            val isDone = data["completed"] as? Boolean
                ?: (data["on"] as? Boolean
                ?: (data["completed"]?.toString()?.equals("true", ignoreCase = true) ?: true))

            val (epoch, _) = parseTimestamp(data["ts"] ?: data["timestamp"] ?: data["updated_at"], dateStr)
            val habitName = data["habit_name"] as? String
                ?: data["title"] as? String
                ?: getHabitName(habitId)

            val currentLogsStr = repository.getPluginValue("com.v07.habits", "logs")
            val logsArr = if (!currentLogsStr.isNullOrBlank()) JSONArray(currentLogsStr) else JSONArray()
            val newArr = JSONArray()

            for (i in 0 until logsArr.length()) {
                val item = logsArr.getJSONObject(i)
                val hid = item.optString("habit", item.optString("habit_id", ""))
                val d = item.optString("date", "")
                if (!(hid == habitId && d == dateStr)) {
                    newArr.put(item)
                }
            }

            val hbtEventId = "hbt_${habitId}_$dateStr"
            if (isDone) {
                newArr.put(JSONObject().apply {
                    put("habit", habitId)
                    put("date", dateStr)
                    put("on", true)
                    put("created", epoch)
                })
                repository.setPluginValue("com.v07.habits", "logs", newArr.toString())

                // Insert into V3Event
                val event = V3Event(
                    id = hbtEventId,
                    category = "habit",
                    date = dateStr,
                    ts = "${dateStr}T12:00:00Z",
                    timestamp = epoch,
                    title = habitName,
                    content = "Completed $habitName",
                    score = 10,
                    metadataJson = JSONObject().put("habit_id", habitId).put("completed", true).toString()
                )
                repository.insertEvent(event)

                // Insert into V3PluginEvent
                repository.insertPluginEvent(
                    V3PluginEvent(
                        id = docId,
                        pluginId = "com.v07.habits",
                        category = "habits",
                        payloadJson = JSONObject(data).toString(),
                        timestamp = epoch,
                        date = dateStr
                    )
                )
            } else {
                repository.setPluginValue("com.v07.habits", "logs", newArr.toString())
                repository.deleteEntityEverywhere(hbtEventId)
                repository.deleteEntityEverywhere(docId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to reconcile habit doc $docId: ${e.message}")
        }
    }

    /**
     * Deletes a habit document locally when removed remotely.
     */
    fun deleteHabitDocLocally(docId: String) {
        try {
            repository.deleteEntityEverywhere(docId)
            if (docId.startsWith("hlog_")) {
                val rest = docId.removePrefix("hlog_")
                val idx = rest.lastIndexOf('_')
                if (idx > 0) {
                    val habitId = rest.substring(0, idx)
                    val rawDate = rest.substring(idx + 1)
                    val dateStr = if (rawDate.length == 8) {
                        "${rawDate.substring(0, 4)}-${rawDate.substring(4, 6)}-${rawDate.substring(6, 8)}"
                    } else rawDate

                    val hbtEventId = "hbt_${habitId}_$dateStr"
                    repository.deleteEntityEverywhere(hbtEventId)

                    val currentLogsStr = repository.getPluginValue("com.v07.habits", "logs")
                    if (!currentLogsStr.isNullOrBlank()) {
                        val logsArr = JSONArray(currentLogsStr)
                        val newArr = JSONArray()
                        for (i in 0 until logsArr.length()) {
                            val item = logsArr.getJSONObject(i)
                            val hid = item.optString("habit", item.optString("habit_id", ""))
                            val d = item.optString("date", "")
                            if (!(hid == habitId && (d == dateStr || d.replace("-", "") == rawDate))) {
                                newArr.put(item)
                            }
                        }
                        repository.setPluginValue("com.v07.habits", "logs", newArr.toString())
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete habit doc locally: ${e.message}")
        }
    }

    /**
     * Deletes a remote document across all potential collections asynchronously.
     * Prevents deleted/purged items from ever resurrecting across companion sync cycles.
     */
    fun deleteRemoteEvent(id: String) {
        if (id.isBlank()) return
        syncScope.launch {
            try {
                val db = getSafeFirestore() ?: return@launch
                val uid = getTargetUid()
                val userDoc = db.collection("users").document(uid)
                userDoc.collection("tombstones").document(id).set(mapOf("id" to id, "deleted_at" to System.currentTimeMillis()), SetOptions.merge())
                val cols = listOf("events", "sessions", "tasks", "thoughts", "daytracker", "ledger")
                for (col in cols) {
                    try { userDoc.collection(col).document(id).delete() } catch (_: Exception) {}
                }
                val knownPlugins = listOf("com.v07.habits", "com.v07.vitals", "com.v07.fitness", "com.v07.gita", "com.v07.reader", "com.v07.tasks", "com.v07.timetracker")
                for (pid in knownPlugins) {
                    try { userDoc.collection("plugins").document(pid).collection("events").document(id).delete() } catch (_: Exception) {}
                }
                if (id.startsWith("hbt_")) {
                    val parts = id.removePrefix("hbt_")
                    val lastUnderscore = parts.lastIndexOf('_')
                    if (lastUnderscore > 0) {
                        val hId = parts.substring(0, lastUnderscore)
                        val dStr = parts.substring(lastUnderscore + 1).replace("-", "")
                        val hlogId = "hlog_${hId}_$dStr"
                        try { userDoc.collection("plugins").document("com.v07.habits").collection("events").document(hlogId).delete() } catch (_: Exception) {}
                        userDoc.collection("tombstones").document(hlogId).set(mapOf("id" to hlogId, "deleted_at" to System.currentTimeMillis()), SetOptions.merge())
                    }
                }
                Log.d(TAG, "Deleted remote Firestore event doc: $id across all collections")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete remote event $id: ${e.message}")
            }
        }
    }

    /**
     * Starts persistent realtime listeners on Firestore collections.
     * Web companion edits reflect instantly in Android SQLite without manual sync.
     */
    @Synchronized
    fun startRealtimeSync() {
        stopRealtimeSync()
        val authUser = getSafeAuth()?.currentUser
        if (authUser == null) {
            Log.i(TAG, "startRealtimeSync skipped: Offline Sanctuary Mode active (Sign in to sync with cloud)")
            return
        }
        val db = getSafeFirestore() ?: return
        val uid = authUser.uid
        val userDoc = db.collection("users").document(uid)

        Log.i(TAG, "Starting realtime Firestore sync for UID: $uid")

        val handleListenerError: (FirebaseFirestoreException?) -> Boolean = { e ->
            if (e != null) {
                if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    Log.w(TAG, "Snapshot listener permission denied; halting realtime sync listeners.")
                    stopRealtimeSync()
                }
                true
            } else false
        }

        try {
            // Sessions
            realtimeRegistrations.add(
                userDoc.collection("sessions").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                repository.deleteEntityEverywhere(doc.id)
                            } else {
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val d = doc.data ?: continue
                                val (epoch, _) = parseTimestamp(d["startTime"] ?: d["ts"] ?: d["timestamp"], null)
                                val durSec = (d["durationSec"] as? Number)?.toLong() ?: ((d["duration_sec"] as? Number)?.toLong() ?: 0L)
                                repository.insertSession(
                                    V3Session(
                                        id = doc.id,
                                        activity = d["activity"] as? String ?: d["title"] as? String ?: "Focus Session",
                                        startTime = epoch,
                                        endTime = (d["endTime"] as? Number)?.toLong(),
                                        durationSec = durSec,
                                        notes = d["notes"] as? String
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Tasks
            realtimeRegistrations.add(
                userDoc.collection("tasks").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                repository.deleteEntityEverywhere(doc.id)
                            } else {
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val d = doc.data ?: continue
                                val (epoch, _) = parseTimestamp(d["createdAt"] ?: d["ts"] ?: d["timestamp"], null)
                                repository.insertTask(
                                    V3Task(
                                        id = doc.id,
                                        title = d["title"] as? String ?: "Task",
                                        priority = d["priority"] as? String ?: "MEDIUM",
                                        status = d["status"] as? String ?: "pending",
                                        createdAt = epoch,
                                        completedAt = (d["completedAt"] as? Number)?.toLong(),
                                        deadline = d["deadline"] as? String
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Thoughts
            realtimeRegistrations.add(
                userDoc.collection("thoughts").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                repository.deleteEntityEverywhere(doc.id)
                            } else {
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val d = doc.data ?: continue
                                val (epoch, dateStr) = parseTimestamp(d["timestamp"] ?: d["ts"], d["date"] as? String)
                                repository.insertThought(
                                    V3Thought(
                                        id = doc.id,
                                        timestamp = epoch,
                                        date = dateStr,
                                        topic = d["topic"] as? String,
                                        content = d["content"] as? String ?: d["text"] as? String ?: "",
                                        pinned = d["pinned"] as? Boolean ?: false,
                                        tags = d["tags"] as? String
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Daytracker
            realtimeRegistrations.add(
                userDoc.collection("daytracker").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                repository.deleteEntityEverywhere(doc.id)
                                repository.deleteEntityEverywhere("dt_${doc.id}")
                            } else {
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val d = doc.data ?: continue
                                val (epoch, dateStr) = parseTimestamp(d["createdAt"] ?: d["ts"], d["targetDate"] as? String ?: d["date"] as? String)
                                repository.insertDayTrackerItem(
                                    V3DayTrackerItem(
                                        id = doc.id,
                                        title = d["title"] as? String ?: "Milestone",
                                        targetDate = dateStr,
                                        hour = (d["hour"] as? Number)?.toInt() ?: 0,
                                        pinned = d["pinned"] as? Boolean ?: false,
                                        createdAt = epoch
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Ledger
            realtimeRegistrations.add(
                userDoc.collection("ledger").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                repository.deleteEntityEverywhere(doc.id)
                            } else {
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val d = doc.data ?: continue
                                val (epoch, dateStr) = parseTimestamp(d["timestamp"] ?: d["ts"], d["date"] as? String)
                                val isoTs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(epoch))
                                repository.insertEvent(
                                    V3Event(
                                        id = doc.id,
                                        category = d["category"] as? String ?: "general",
                                        date = dateStr,
                                        ts = isoTs,
                                        timestamp = epoch,
                                        title = d["title"] as? String,
                                        content = d["content"] as? String ?: d["text"] as? String,
                                        score = (d["score"] as? Number)?.toInt() ?: 0
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Tombstones
            realtimeRegistrations.add(
                userDoc.collection("tombstones").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            repository.deleteEntityEverywhere(doc.id)
                        }
                    }
                }
            )

            // System Plugins Catalog
            realtimeRegistrations.add(
                userDoc.collection("system").document("plugins").addSnapshotListener { snapshot, e ->
                    if (handleListenerError(e) || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    syncScope.launch {
                        try {
                            val data = snapshot.data ?: return@launch
                            @Suppress("UNCHECKED_CAST")
                            val list = data["plugins"] as? List<Map<String, Any?>> ?: return@launch
                            val registry = com.v07.v3.core.plugin.PluginRegistry.getInstance(context)
                            for (pMap in list) {
                                val pid = pMap["id"] as? String ?: continue
                                val enabled = pMap["enabled"] as? Boolean ?: true
                                val existing = registry.getPlugin(pid)
                                if (existing != null) {
                                    if (enabled && existing.state != com.v07.v3.core.plugin.PluginState.ENABLED && existing.state != com.v07.v3.core.plugin.PluginState.RUNNING) {
                                        registry.enablePlugin(pid)
                                    } else if (!enabled && existing.state != com.v07.v3.core.plugin.PluginState.DISABLED) {
                                        registry.disablePlugin(pid)
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            )

            // Habits subcollection with real-time bidirectional synchronization
            realtimeRegistrations.add(
                userDoc.collection("plugins").document("com.v07.habits").collection("events").addSnapshotListener { snapshots, e ->
                    if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                    syncScope.launch {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                deleteHabitDocLocally(doc.id)
                            } else {
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val d = doc.data ?: continue
                                reconcileHabitDoc(doc.id, d)
                            }
                        }
                    }
                }
            )

            // Other Known Plugins Subcollections
            val otherPlugins = listOf("com.v07.vitals", "com.v07.fitness", "com.v07.gita", "com.v07.reader", "com.v07.tasks", "com.v07.timetracker")
            for (pid in otherPlugins) {
                realtimeRegistrations.add(
                    userDoc.collection("plugins").document(pid).collection("events").addSnapshotListener { snapshots, e ->
                        if (handleListenerError(e) || snapshots == null) return@addSnapshotListener
                        syncScope.launch {
                            for (change in snapshots.documentChanges) {
                                val doc = change.document
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                    repository.deleteEntityEverywhere(doc.id)
                                } else {
                                    if (repository.isDeletedOrTombstoned(doc.id)) continue
                                    val d = doc.data ?: continue
                                    val (epoch, dateStr) = parseTimestamp(d["timestamp"] ?: d["ts"], d["date"] as? String)
                                    val cat = d["category"] as? String ?: "plugin"
                                    val payloadJson = d["payloadJson"] as? String ?: JSONObject(d).toString()
                                    repository.insertPluginEvent(
                                        V3PluginEvent(
                                            id = doc.id,
                                            pluginId = pid,
                                            category = cat,
                                            payloadJson = payloadJson,
                                            timestamp = epoch,
                                            date = dateStr
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // Periodic heartbeat every 60s
            heartbeatJob?.cancel()
            heartbeatJob = syncScope.launch {
                while (true) {
                    kotlinx.coroutines.delay(60_000L)
                    try {
                        syncNow { _, _ -> }
                    } catch (_: Exception) {}
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Failed to start realtime sync: ${e.message}")
        }
    }

    @Synchronized
    fun stopRealtimeSync() {
        for (reg in realtimeRegistrations) {
            try { reg.remove() } catch (_: Exception) {}
        }
        realtimeRegistrations.clear()
        heartbeatJob?.cancel()
        heartbeatJob = null
        Log.i(TAG, "Stopped realtime Firestore sync")
    }

    /**
     * Parses diverse Firestore and companion timestamp representations into (epochMs, yyyy-MM-dd)
     */
    private fun parseTimestamp(rawTs: Any?, rawDate: String?): Pair<Long, String> {
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        if (rawTs is Number) {
            val epoch = rawTs.toLong()
            val dateStr = if (!rawDate.isNullOrBlank()) rawDate else dateFmt.format(Date(epoch))
            return Pair(epoch, dateStr)
        }
        if (rawTs is String && rawTs.isNotBlank()) {
            val patterns = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )
            for (p in patterns) {
                try {
                    val sdf = SimpleDateFormat(p, Locale.US)
                    val d = sdf.parse(rawTs)
                    if (d != null) {
                        val epoch = d.time
                        val dateStr = if (!rawDate.isNullOrBlank()) rawDate else dateFmt.format(d)
                        return Pair(epoch, dateStr)
                    }
                } catch (_: Exception) {}
            }
        }
        val now = System.currentTimeMillis()
        val dateStr = if (!rawDate.isNullOrBlank()) rawDate else dateFmt.format(Date(now))
        return Pair(now, dateStr)
    }

    /**
     * Normalizes and migrates a single event document from web companion into V3 SQLite.
     * Accurately updates sessions, tasks, thoughts, and unified event ledger.
     */
    private fun migrateSingleEvent(docId: String, data: Map<String, Any?>): Boolean {
        if (repository.isDeletedOrTombstoned(docId)) {
            Log.d(TAG, "Skipping migrateSingleEvent for tombstoned/deleted ID: $docId")
            return false
        }
        try {
            val rawCat = (data["category"] as? String ?: "unknown").lowercase().trim()
            val (epoch, dateStr) = parseTimestamp(
                data["ts"] ?: data["timestamp"] ?: data["started_at"] ?: data["time"],
                data["date"] as? String
            )
            val isoTs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(epoch))

            when (rawCat) {
                "sessions", "session" -> {
                    val act = data["activity"] as? String ?: data["title"] as? String ?: "Focus Session"
                    val durMin = (data["duration"] as? Number)?.toLong()
                        ?: (data["duration_min"] as? Number)?.toLong()
                        ?: (data["duration_minutes"] as? Number)?.toLong() ?: 0L
                    val durSec = (data["duration_sec"] as? Number)?.toLong() ?: (durMin * 60L)
                    val notes = data["notes"] as? String ?: data["custom_label"] as? String
                    val startTime = (data["started_at"] as? Number)?.toLong() ?: epoch
                    val endTime = (data["ended_at"] as? Number)?.toLong() ?: (startTime + (durSec * 1000L))

                    val session = V3Session(
                        id = docId,
                        activity = act,
                        startTime = startTime,
                        endTime = endTime,
                        durationSec = durSec,
                        notes = notes
                    )
                    repository.insertSession(session)

                    val cleanAct = act.replace("_", " ").replaceFirstChar { it.uppercase() }
                    val content = "${durSec / 60}m focus session" + if (!notes.isNullOrBlank()) " • $notes" else ""
                    val event = V3Event(
                        id = docId,
                        category = "session",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = cleanAct,
                        content = content,
                        score = 0,
                        metadataJson = JSONObject().put("durationSec", durSec).put("activity", act).toString()
                    )
                    repository.insertEvent(event)
                }

                "tasks", "tasks_ui" -> {
                    val title = data["title"] as? String ?: data["task_id"] as? String ?: "Task"
                    val statusStr = data["new_status"] as? String ?: data["status"] as? String ?: ""
                    val action = data["action"] as? String ?: ""
                    val isDone = statusStr.equals("done", true) ||
                            statusStr.equals("completed", true) ||
                            action.equals("completed", true)
                    val prio = data["priority"] as? String ?: "MEDIUM"
                    val deadline = data["deadline"] as? String

                    val task = V3Task(
                        id = docId,
                        title = title,
                        priority = prio,
                        status = if (isDone) "completed" else "pending",
                        createdAt = epoch,
                        completedAt = if (isDone) epoch else null,
                        deadline = deadline
                    )
                    repository.insertTask(task)

                    val event = V3Event(
                        id = docId,
                        category = "task",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = title,
                        content = "Priority: $prio" + if (!deadline.isNullOrBlank()) " • Due: $deadline" else "",
                        score = if (isDone) 1 else 0
                    )
                    repository.insertEvent(event)
                }

                "checklist" -> {
                    val label = data["label"] as? String ?: "Checklist item"
                    val isDone = data["done"] as? Boolean ?: false
                    val recurring = data["recurring"] as? Boolean ?: true
                    val freq = data["frequency"] as? String ?: "D"

                    val task = V3Task(
                        id = docId,
                        title = label,
                        priority = "MEDIUM",
                        status = if (isDone) "completed" else "pending",
                        createdAt = epoch,
                        completedAt = if (isDone) epoch else null,
                        deadline = dateStr
                    )
                    repository.insertTask(task)

                    val event = V3Event(
                        id = docId,
                        category = "task",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = label,
                        content = "${if (recurring) (if (freq == "D") "Daily" else "Recurring") else "Single"} Checklist • ${if (isDone) "Done ✓" else "Pending"}",
                        score = if (isDone) 1 else 0
                    )
                    repository.insertEvent(event)
                }

                "thought", "thought_ui" -> {
                    val text = data["text"] as? String ?: data["content"] as? String ?: ""
                    val topic = data["topic"] as? String ?: "Reflection"
                    val info = data["info"] as? String

                    val thought = V3Thought(
                        id = docId,
                        timestamp = epoch,
                        date = dateStr,
                        topic = topic,
                        content = text,
                        tags = info
                    )
                    repository.insertThought(thought)

                    val cleanTopic = topic.replace("_", " ").replaceFirstChar { it.uppercase() }
                    val event = V3Event(
                        id = docId,
                        category = "thought",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = cleanTopic,
                        content = text
                    )
                    repository.insertEvent(event)
                }

                "shares" -> {
                    val title = data["title"] as? String ?: "Saved Link"
                    val text = data["text"] as? String ?: data["info"] as? String ?: ""
                    val tags = data["tags"] as? String ?: "Link"

                    val thought = V3Thought(
                        id = docId,
                        timestamp = epoch,
                        date = dateStr,
                        topic = "Link / Share",
                        content = "$title\n$text",
                        tags = tags
                    )
                    repository.insertThought(thought)

                    val event = V3Event(
                        id = docId,
                        category = "thought",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "Saved: $title",
                        content = text
                    )
                    repository.insertEvent(event)
                }

                "dsa" -> {
                    val problem = data["problem"] as? String ?: "Problem"
                    val topic = data["topic"] as? String ?: "Algorithm"
                    val desc = data["description"] as? String ?: ""
                    val diff = data["difficulty"] as? String ?: "medium"
                    val done = data["completed"] as? Boolean ?: false

                    val task = V3Task(
                        id = docId,
                        title = "DSA: $problem ($topic)",
                        priority = if (diff.equals("hard", true)) "HIGH" else "MEDIUM",
                        status = if (done) "completed" else "pending",
                        createdAt = epoch,
                        completedAt = if (done) epoch else null,
                        deadline = dateStr
                    )
                    repository.insertTask(task)

                    val event = V3Event(
                        id = docId,
                        category = "task",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "DSA: $problem",
                        content = "$topic • ${diff.replaceFirstChar { it.uppercase() }} • $desc"
                    )
                    repository.insertEvent(event)
                }

                "mood", "mood_ui" -> {
                    val mood = data["mood"] as? String ?: "calm"
                    val score = (data["mood_score"] as? Number)?.toInt() ?: 3
                    val note = data["note"] as? String ?: ""
                    val title = "Mood: ${mood.replaceFirstChar { it.uppercase() }}"
                    val content = if (note.isNotBlank()) note else "Intensity: $score/5"

                    val event = V3Event(
                        id = docId,
                        category = "mood",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = title,
                        content = content,
                        score = score,
                        metadataJson = JSONObject().put("mood", mood).put("score", score).toString()
                    )
                    repository.insertEvent(event)
                }

                "energy" -> {
                    val energy = data["energy"] as? String ?: "balanced"
                    val score = (data["energy_score"] as? Number)?.toInt() ?: 3
                    val note = data["note"] as? String ?: ""
                    val title = "Energy: ${energy.replaceFirstChar { it.uppercase() }}"
                    val content = if (note.isNotBlank()) note else "Intensity: $score/5"

                    val event = V3Event(
                        id = docId,
                        category = "energy",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = title,
                        content = content,
                        score = score,
                        metadataJson = JSONObject().put("energy", energy).put("score", score).toString()
                    )
                    repository.insertEvent(event)
                }

                "running" -> {
                    val km = (data["distance_km"] as? Number)?.toFloat() ?: 0.0f
                    val durMin = (data["duration_min"] as? Number)?.toInt()
                        ?: (data["duration_minutes"] as? Number)?.toInt()
                        ?: (data["minutes"] as? Number)?.toInt() ?: 0
                    val pace = data["pace"] as? String ?: ""
                    val notes = data["notes"] as? String ?: ""

                    val content = "${durMin}m" +
                            (if (pace.isNotBlank()) " • Pace: $pace" else "") +
                            (if (notes.isNotBlank()) " • $notes" else "")

                    val event = V3Event(
                        id = docId,
                        category = "run",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "Run: ${String.format(Locale.US, "%.2f", km)} km",
                        content = content,
                        metadataJson = JSONObject().put("distance_km", km).put("duration_min", durMin).put("pace", pace).toString()
                    )
                    repository.insertEvent(event)
                }

                "habits", "habits_ui", "hbt" -> {
                    val act = data["activity"] as? String ?: data["habit"] as? String ?: "Daily Habit"
                    val isOn = data["on"] as? Boolean ?: (data["on"] == "true")
                    val event = V3Event(
                        id = docId,
                        category = "habit",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "Habit: ${act.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        content = if (isOn) "Completed ✓" else "Missed ✗",
                        score = if (isOn) 1 else 0
                    )
                    repository.insertEvent(event)
                }

                "metrics", "metrics_ui" -> {
                    val mType = data["type"] as? String ?: "metrics"
                    val note = data["note"] as? String ?: ""
                    val pushups = (data["pushups"] as? Number)?.toInt() ?: 0
                    val doomScroll = (data["doom_scroll"] as? Number)?.toInt() ?: 0

                    val event = when {
                        mType == "checkin" -> V3Event(
                            id = docId,
                            category = "checkin",
                            date = dateStr,
                            ts = isoTs,
                            timestamp = epoch,
                            title = "Morning Check-In",
                            content = if (note.isNotBlank()) note else "Started the day in presence"
                        )
                        mType == "checkout" -> V3Event(
                            id = docId,
                            category = "checkout",
                            date = dateStr,
                            ts = isoTs,
                            timestamp = epoch,
                            title = "Evening Check-Out",
                            content = if (note.isNotBlank()) note else "Day concluded peacefully"
                        )
                        pushups > 0 -> V3Event(
                            id = docId,
                            category = "pushups",
                            date = dateStr,
                            ts = isoTs,
                            timestamp = epoch,
                            title = "Pushups: $pushups",
                            content = "$pushups repetitions completed",
                            score = pushups
                        )
                        doomScroll > 0 -> V3Event(
                            id = docId,
                            category = "doom_scroll",
                            date = dateStr,
                            ts = isoTs,
                            timestamp = epoch,
                            title = "Doom Scroll: ${doomScroll}m",
                            content = "$doomScroll minutes recorded"
                        )
                        else -> V3Event(
                            id = docId,
                            category = "metrics",
                            date = dateStr,
                            ts = isoTs,
                            timestamp = epoch,
                            title = mType.replaceFirstChar { it.uppercase() },
                            content = note
                        )
                    }
                    repository.insertEvent(event)
                }

                "chess" -> {
                    val opp = data["opponent"] as? String ?: "Opponent"
                    val res = data["result"] as? String ?: "played"
                    val col = data["color"] as? String ?: "white"
                    val notes = data["notes"] as? String ?: ""
                    val event = V3Event(
                        id = docId,
                        category = "session",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "Chess vs $opp ($res)",
                        content = "Played as $col.${if (notes.isNotBlank()) " $notes" else ""}".trim()
                    )
                    repository.insertEvent(event)
                }

                "panics" -> {
                    val note = data["note"] as? String ?: ""
                    val interval = data["time_since_last"] as? String ?: ""
                    val event = V3Event(
                        id = docId,
                        category = "panic",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "Mindful Pause (Panic Anchor)",
                        content = note + if (interval.isNotBlank()) " (Interval: $interval)" else ""
                    )
                    repository.insertEvent(event)
                }

                "gita" -> {
                    val vId = data["verse_id"] as? String ?: "${data["chapter"] ?: 1}.${data["verse"] ?: 1}"
                    val event = V3Event(
                        id = docId,
                        category = "gita",
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = "Gita Verse $vId",
                        content = "Read and reflected on Bhagavad Gita $vId"
                    )
                    repository.insertEvent(event)
                }

                else -> {
                    val title = (data["title"] as? String ?: rawCat).replaceFirstChar { it.uppercase() }
                    val content = data["text"] as? String ?: data["content"] as? String ?: data["note"] as? String ?: ""
                    val event = V3Event(
                        id = docId,
                        category = rawCat,
                        date = dateStr,
                        ts = isoTs,
                        timestamp = epoch,
                        title = title,
                        content = content
                    )
                    repository.insertEvent(event)
                }
            }
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Error migrating single event $docId: ${e.message}")
            return false
        }
    }

    /**
     * Comprehensive migration engine: Pulls all documents from Firestore users/{uid}
     * for events, daytracker, appusage, gita_reviews, and transforms them into V3 SQLite.
     * Falls back seamlessly to bundled migration asset if offline or on network failure.
     */
    fun migrateWebCompanionData(targetUid: String? = null, onComplete: (Boolean, String?, JSONObject) -> Unit) {
        syncScope.launch {
            val stats = JSONObject()
            var eventsMigrated = 0
            var sessionsMigrated = 0
            var tasksMigrated = 0
            var thoughtsMigrated = 0
            var appUsageDays = 0
            var dayTrackerCount = 0

            val auth = getSafeAuth()
            val targetDocUid = auth?.currentUser?.uid ?: (targetUid?.ifBlank { null } ?: getTargetUid())

            Log.i(TAG, "Starting full Web Companion migration for UID: $targetDocUid")

            try {
                val db = getSafeFirestore()
                if (db == null) {
                    Log.w(TAG, "FirebaseFirestore unavailable. Network connection required for companion sync.")
                    onComplete(false, "FirebaseFirestore unavailable", stats)
                    return@launch
                }
                if (auth?.currentUser == null) {
                    Log.w(TAG, "Migration skipped: Cloud account not authenticated")
                    onComplete(false, "Authentication required: Please sign in to your Cloud Account", stats)
                    return@launch
                }
                val userDoc = db.collection("users").document(targetDocUid)

                // 1. PULL ALL EVENTS (No 200 limit)
                val eventsSnap = Tasks.await(userDoc.collection("events").get())
                Log.i(TAG, "Fetched ${eventsSnap.size()} events from Firestore")
                val tombstones = repository.getAllTombstoneIds()

                for (doc in eventsSnap.documents) {
                    val data = doc.data ?: continue
                    if (tombstones.contains(doc.id) || repository.isDeletedOrTombstoned(doc.id)) {
                        try {
                            doc.reference.delete()
                            Log.d(TAG, "Pruned remote tombstoned document during migration: ${doc.id}")
                        } catch (_: Exception) {}
                        continue
                    }
                    if (migrateSingleEvent(doc.id, data)) {
                        eventsMigrated++
                        val cat = (data["category"] as? String ?: "").lowercase()
                        when {
                            cat.contains("session") -> sessionsMigrated++
                            cat.contains("task") || cat == "checklist" || cat == "dsa" -> tasksMigrated++
                            cat.contains("thought") || cat == "shares" -> thoughtsMigrated++
                        }
                    }
                }

                // 2. PULL ALL DAY TRACKER ITEMS
                try {
                    val daySnap = Tasks.await(userDoc.collection("daytracker").get())
                    for (doc in daySnap.documents) {
                        val d = doc.data ?: continue
                        if (tombstones.contains(doc.id) || repository.isDeletedOrTombstoned(doc.id) ||
                            tombstones.contains("dt_${doc.id}") || repository.isDeletedOrTombstoned("dt_${doc.id}")) {
                            try { doc.reference.delete() } catch (_: Exception) {}
                            continue
                        }
                        val title = d["title"] as? String ?: "Milestone"
                        val date = d["date"] as? String ?: ""
                        val hour = (d["hour"] as? Number)?.toInt() ?: 0
                        val pinned = d["pinned"] as? Boolean ?: false
                        val (epoch, _) = parseTimestamp(d["ts"], date)

                        repository.insertDayTrackerItem(
                            V3DayTrackerItem(
                                id = doc.id,
                                title = title,
                                targetDate = date,
                                hour = hour,
                                pinned = pinned,
                                createdAt = epoch
                            )
                        )
                        repository.insertEvent(
                            V3Event(
                                id = "dt_${doc.id}",
                                category = "milestone",
                                date = date,
                                ts = "",
                                timestamp = epoch,
                                title = title,
                                content = "Target Date: $date"
                            )
                        )
                        dayTrackerCount++
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Daytracker pull warning: ${e.message}")
                }

                // 3. PULL ALL APP USAGE (65+ days telemetry)
                try {
                    val usageSnap = Tasks.await(userDoc.collection("appusage").get())
                    val cal = Calendar.getInstance()
                    for (doc in usageSnap.documents) {
                        val d = doc.data ?: continue
                        val date = d["date"] as? String ?: doc.id
                        val dDate = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) } catch (_: Exception) { null }
                        val startMs = dDate?.let {
                            cal.time = it
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.timeInMillis
                        } ?: System.currentTimeMillis()
                        val endMs = startMs + 86400000L - 1000L

                        @Suppress("UNCHECKED_CAST")
                        val usageList = d["usage"] as? List<Map<String, Any?>>
                        if (usageList != null) {
                            for (u in usageList) {
                                val pName = u["package"] as? String ?: "unknown"
                                val aName = u["app"] as? String ?: pName
                                val durSec = (u["duration_sec"] as? Number)?.toLong() ?: 0L
                                repository.insertAppUsage(
                                    V3AppUsage(
                                        id = "u_${date}_${pName}",
                                        packageName = pName,
                                        appName = aName,
                                        startTime = startMs,
                                        endTime = endMs,
                                        durationMs = durSec * 1000L,
                                        category = "Neutral"
                                    )
                                )
                            }
                            appUsageDays++
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Appusage pull warning: ${e.message}")
                }

                // 4. PULL GITA REVIEWS & READS
                try {
                    val gitaSnap = Tasks.await(userDoc.collection("gita_reviews").get())
                    for (doc in gitaSnap.documents) {
                        val d = doc.data ?: continue
                        if (tombstones.contains(doc.id) || repository.isDeletedOrTombstoned(doc.id)) {
                            try { doc.reference.delete() } catch (_: Exception) {}
                            continue
                        }
                        val text = d["text"] as? String ?: continue
                        val vId = d["verse_id"] as? String ?: "1.1"
                        val (epoch, dateStr) = parseTimestamp(d["ts"], d["date"] as? String)

                        repository.insertThought(
                            V3Thought(
                                id = doc.id,
                                timestamp = epoch,
                                date = dateStr,
                                topic = "Gita Reflection",
                                content = "Gita Verse $vId:\n$text",
                                tags = "gita"
                            )
                        )
                        repository.insertEvent(
                            V3Event(
                                id = doc.id,
                                category = "gita",
                                date = dateStr,
                                ts = "",
                                timestamp = epoch,
                                title = "Gita Reflection (Verse $vId)",
                                content = text
                            )
                        )
                        thoughtsMigrated++
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Gita pull warning: ${e.message}")
                }

                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_SYNCED, now).apply()

                stats.put("source", "Live Firestore (v07-dashboard)")
                stats.put("uid", targetDocUid)
                stats.put("eventsCount", eventsMigrated)
                stats.put("sessionsCount", sessionsMigrated)
                stats.put("tasksCount", tasksMigrated)
                stats.put("thoughtsCount", thoughtsMigrated)
                stats.put("appUsageDaysCount", appUsageDays)
                stats.put("dayTrackerCount", dayTrackerCount)

                Log.i(TAG, "Live migration succeeded: $stats")
                onComplete(true, null, stats)

            } catch (e: Exception) {
                Log.w(TAG, "Live Firestore migration encountered error: ${e.message}")
                val friendlyMsg = if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    "Cloud permission denied. Please sign in to your cloud account."
                } else {
                    e.message ?: "Firestore migration error"
                }
                onComplete(false, friendlyMsg, stats)
            }
        }
    }

    /**
     * Bidirectional live sync with v07-dashboard.
     * PUSHES V3 local events/sessions/tasks/thoughts/usage in web companion format,
     * then PULLS all remote items from Firestore.
     */
    fun syncNow(onComplete: (Boolean, String?) -> Unit) {
        if (!isCloudSyncEnabled()) {
            Log.i(TAG, "syncNow skipped: Offline Guest Mode active")
            onComplete(true, "Offline Guest Mode active")
            return
        }
        val uid = getTargetUid()

        if (isSyncing.getAndSet(true)) {
            onComplete(false, "Sync already in progress")
            return
        }

        syncScope.launch {
            try {
                val db = getSafeFirestore()
                if (db == null) {
                    isSyncing.set(false)
                    Log.i(TAG, "Sync skipped: Firebase Firestore not available")
                    onComplete(false, "Firebase service not available")
                    return@launch
                }

                val auth = getSafeAuth()
                val currentAuthUser = auth?.currentUser
                if (currentAuthUser == null) {
                    isSyncing.set(false)
                    Log.i(TAG, "Cloud sync skipped: Offline Sanctuary Mode active (Sign in to sync with companion website)")
                    onComplete(false, "Authentication required: Please sign in to your Cloud Account")
                    return@launch
                }

                val targetDocUid = currentAuthUser.uid
                val userDoc = db.collection("users").document(targetDocUid)
                val sessionsCol = userDoc.collection("sessions")
                val tasksCol = userDoc.collection("tasks")
                val thoughtsCol = userDoc.collection("thoughts")
                val daytrackerCol = userDoc.collection("daytracker")
                val ledgerCol = userDoc.collection("ledger")
                val usageCol = userDoc.collection("appusage")
                val tombstonesCol = userDoc.collection("tombstones")

                val now = System.currentTimeMillis()

                // ─── 1. PUSH SESSIONS ──────────────────────────────────────────
                val sessions = repository.getRecentSessions(100)
                for (s in sessions) {
                    val map = HashMap<String, Any>()
                    val durMin = (s.durationSec / 60).toInt()
                    map["id"] = s.id
                    map["activity"] = s.activity
                    map["startTime"] = s.startTime
                    s.endTime?.let { map["endTime"] = it }
                    map["durationSec"] = s.durationSec
                    map["duration_sec"] = s.durationSec
                    map["duration_min"] = durMin
                    s.notes?.let { map["notes"] = it }
                    map["ts"] = s.startTime
                    map["date"] = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(s.startTime))
                    map["updated_at"] = now
                    map["_syncedAt"] = Timestamp.now()
                    sessionsCol.document(s.id).set(map, SetOptions.merge())
                }

                // ─── 2. PUSH TASKS ─────────────────────────────────────────────
                val tasks = repository.getTasks(null)
                for (t in tasks) {
                    val map = HashMap<String, Any>()
                    map["id"] = t.id
                    map["title"] = t.title
                    map["priority"] = t.priority
                    map["status"] = t.status
                    map["createdAt"] = t.createdAt
                    t.completedAt?.let { map["completedAt"] = it }
                    t.deadline?.let { map["deadline"] = it }
                    map["updated_at"] = now
                    map["_syncedAt"] = Timestamp.now()
                    tasksCol.document(t.id).set(map, SetOptions.merge())
                }

                // ─── 3. PUSH THOUGHTS ──────────────────────────────────────────
                val thoughts = repository.getRecentThoughts(100)
                for (th in thoughts) {
                    val map = HashMap<String, Any>()
                    map["id"] = th.id
                    map["timestamp"] = th.timestamp
                    map["ts"] = th.timestamp
                    map["date"] = th.date
                    th.topic?.let { map["topic"] = it }
                    map["content"] = th.content
                    map["pinned"] = th.pinned
                    th.tags?.let { map["tags"] = it }
                    map["updated_at"] = now
                    map["_syncedAt"] = Timestamp.now()
                    thoughtsCol.document(th.id).set(map, SetOptions.merge())
                }

                // ─── 4. PUSH DAY TRACKER ───────────────────────────────────────
                val dayItems = repository.getAllDayTrackerItems()
                for (dt in dayItems) {
                    val map = HashMap<String, Any>()
                    map["id"] = dt.id
                    map["title"] = dt.title
                    map["targetDate"] = dt.targetDate
                    map["date"] = dt.targetDate
                    map["hour"] = dt.hour
                    map["pinned"] = dt.pinned
                    map["createdAt"] = dt.createdAt
                    map["updated_at"] = now
                    map["_syncedAt"] = Timestamp.now()
                    daytrackerCol.document(dt.id).set(map, SetOptions.merge())
                }

                // ─── 5. PUSH HABITS FROM PLUGIN STORAGE ─────────────────────
                try {
                    val logsStr = repository.getPluginValue("com.v07.habits", "logs")
                    if (!logsStr.isNullOrBlank()) {
                        val arr = JSONArray(logsStr)
                        val habitsCol = userDoc.collection("plugins").document("com.v07.habits").collection("events")
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            val hid = item.optString("habit", item.optString("habit_id", ""))
                            val d = item.optString("date", "")
                            val on = item.optBoolean("on", item.optBoolean("completed", true))
                            if (hid.isNotBlank() && d.isNotBlank()) {
                                val compactDate = d.replace("-", "")
                                val docId = "hlog_${hid}_$compactDate"
                                if (on) {
                                    val map = hashMapOf<String, Any>(
                                        "id" to docId,
                                        "habit_id" to hid,
                                        "habit_name" to getHabitName(hid),
                                        "date" to d,
                                        "completed" to true,
                                        "ts" to item.optLong("created", now),
                                        "category" to "habits",
                                        "updated_at" to now,
                                        "_syncedAt" to Timestamp.now()
                                    )
                                    habitsCol.document(docId).set(map, SetOptions.merge())
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed pushing habits to cloud: ${e.message}")
                }

                // ─── 5b. PUSH PLUGIN DOMAIN EVENTS ──────────────────────────────
                val pluginEvents = repository.getAllPluginEventsForSync()
                for (pe in pluginEvents) {
                    val map = HashMap<String, Any>()
                    map["id"] = pe.id
                    map["pluginId"] = pe.pluginId
                    map["category"] = pe.category
                    map["payloadJson"] = pe.payloadJson
                    map["timestamp"] = pe.timestamp
                    map["date"] = pe.date
                    map["updated_at"] = now
                    map["_syncedAt"] = Timestamp.now()
                    try {
                        val parsed = JSONObject(pe.payloadJson)
                        val keys = parsed.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            if (!map.containsKey(k)) {
                                val v = jsonToPlainJava(parsed.opt(k))
                                if (v != null) map[k] = v
                            }
                        }
                    } catch (_: Exception) {}
                    userDoc.collection("plugins").document(pe.pluginId).collection("events").document(pe.id).set(map, SetOptions.merge())
                }

                // ─── 5b. PUSH PLUGIN REGISTRY & STATES ──────────────────────
                try {
                    val registry = com.v07.v3.core.plugin.PluginRegistry.getInstance(context)
                    val pluginsList = registry.getAllPlugins().map { p ->
                        mapOf(
                            "id" to p.manifest.id,
                            "name" to p.manifest.name,
                            "version" to p.manifest.version,
                            "author" to p.manifest.publisher,
                            "description" to p.manifest.description,
                            "icon" to p.manifest.navIcon,
                            "color" to p.manifest.navColor,
                            "state" to p.state.name,
                            "enabled" to (p.state == com.v07.v3.core.plugin.PluginState.ENABLED || p.state == com.v07.v3.core.plugin.PluginState.RUNNING),
                            "isBuiltIn" to p.isBuiltIn,
                            "updated_at" to now
                        )
                    }
                    userDoc.collection("system").document("plugins").set(
                        mapOf(
                            "plugins" to pluginsList,
                            "updated_at" to now,
                            "_syncedAt" to Timestamp.now()
                        ),
                        SetOptions.merge()
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to push plugin catalog to Firestore: ${e.message}")
                }

                // ─── 6. PUSH LEDGER (LIFE OS CADENCE & CAPTURES) ────────────────
                val localEvents = repository.getAllEventsForSync()
                for (e in localEvents) {
                    val map = HashMap<String, Any>()
                    map["id"] = e.id
                    map["category"] = e.category
                    map["date"] = e.date
                    map["ts"] = e.timestamp
                    map["timestamp"] = e.timestamp
                    e.title?.let { map["title"] = it }
                    e.content?.let { map["content"] = it; map["text"] = it }
                    if (e.score > 0) map["score"] = e.score
                    map["updated_at"] = now
                    map["_syncedAt"] = Timestamp.now()

                    if (!e.metadataJson.isNullOrBlank()) {
                        try {
                            val meta = JSONObject(e.metadataJson)
                            val keys = meta.keys()
                            while (keys.hasNext()) {
                                val k = keys.next()
                                val v = jsonToPlainJava(meta.opt(k))
                                if (v != null) {
                                    map["meta_$k"] = v
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    ledgerCol.document(e.id).set(map, SetOptions.merge())
                }

                // ─── 7. PUSH TOMBSTONES ─────────────────────────────────────────
                val tombstones = repository.getAllTombstoneIds()
                for (tId in tombstones) {
                    val map = HashMap<String, Any>()
                    map["id"] = tId
                    map["deleted_at"] = now
                    tombstonesCol.document(tId).set(map, SetOptions.merge())
                }

                // ─── 8. PUSH SCREEN TIME ───────────────────────────────────────
                if (screenTimeTracker.hasUsageStatsPermission()) {
                    val summary = screenTimeTracker.collectNow()
                    val usageDoc = HashMap<String, Any>()
                    usageDoc["date"] = summary.date

                    val summaryMap = HashMap<String, Any>()
                    summaryMap["total_screen_time_sec"] = summary.totalDurationMs / 1000
                    summaryMap["unique_apps_used"] = summary.appCount
                    summaryMap["session_count"] = summary.appCount
                    summaryMap["app_switches"] = 0

                    val topApp = summary.topApps.firstOrNull()
                    if (topApp != null) {
                        val topMap = HashMap<String, Any>()
                        topMap["app"] = topApp.appName
                        topMap["package"] = topApp.packageName
                        topMap["duration_sec"] = topApp.durationMs / 1000
                        topMap["percentage"] = topApp.percentage
                        summaryMap["top_app"] = topMap
                    }
                    usageDoc["summary"] = summaryMap

                    val usageList = summary.topApps.map { stat ->
                        val uMap = HashMap<String, Any>()
                        uMap["app"] = stat.appName
                        uMap["package"] = stat.packageName
                        uMap["duration_sec"] = stat.durationMs / 1000
                        uMap["total_duration_sec"] = stat.durationMs / 1000
                        uMap
                    }
                    usageDoc["usage"] = usageList
                    usageDoc["_syncedAt"] = Timestamp.now()
                    usageDoc["updated_at"] = now
                    usageCol.document(summary.date).set(usageDoc, SetOptions.merge())
                }

                // ─── 9. PULL REMOTE TOMBSTONES ──────────────────────────────────
                try {
                    val remoteTombs = Tasks.await(tombstonesCol.get())
                    for (td in remoteTombs.documents) {
                        repository.deleteEntityEverywhere(td.id)
                    }
                } catch (_: Exception) {}

                // ─── 10. PULL PARTITIONED DATA (OR DUAL-READ FALLBACK) ──────────
                val partSessions = Tasks.await(sessionsCol.get())
                val partTasks = Tasks.await(tasksCol.get())
                val partThoughts = Tasks.await(thoughtsCol.get())
                val partDay = Tasks.await(daytrackerCol.get())
                val partLedger = Tasks.await(ledgerCol.get())

                val hasPartitionedData = !partSessions.isEmpty || !partTasks.isEmpty || !partThoughts.isEmpty || !partDay.isEmpty || !partLedger.isEmpty

                if (hasPartitionedData) {
                    // Pull Sessions
                    for (doc in partSessions.documents) {
                        val d = doc.data ?: continue
                        if (repository.isDeletedOrTombstoned(doc.id)) continue
                        val (epoch, _) = parseTimestamp(d["startTime"] ?: d["ts"] ?: d["timestamp"], null)
                        val durSec = (d["durationSec"] as? Number)?.toLong() ?: ((d["duration_sec"] as? Number)?.toLong() ?: 0L)
                        repository.insertSession(
                            V3Session(
                                id = doc.id,
                                activity = d["activity"] as? String ?: d["title"] as? String ?: "Focus Session",
                                startTime = epoch,
                                endTime = (d["endTime"] as? Number)?.toLong(),
                                durationSec = durSec,
                                notes = d["notes"] as? String
                            )
                        )
                    }

                    // Pull Tasks
                    for (doc in partTasks.documents) {
                        val d = doc.data ?: continue
                        if (repository.isDeletedOrTombstoned(doc.id)) continue
                        val (epoch, _) = parseTimestamp(d["createdAt"] ?: d["ts"] ?: d["timestamp"], null)
                        repository.insertTask(
                            V3Task(
                                id = doc.id,
                                title = d["title"] as? String ?: "Task",
                                priority = d["priority"] as? String ?: "MEDIUM",
                                status = d["status"] as? String ?: "pending",
                                createdAt = epoch,
                                completedAt = (d["completedAt"] as? Number)?.toLong(),
                                deadline = d["deadline"] as? String
                            )
                        )
                    }

                    // Pull Thoughts
                    for (doc in partThoughts.documents) {
                        val d = doc.data ?: continue
                        if (repository.isDeletedOrTombstoned(doc.id)) continue
                        val (epoch, dateStr) = parseTimestamp(d["timestamp"] ?: d["ts"], d["date"] as? String)
                        repository.insertThought(
                            V3Thought(
                                id = doc.id,
                                timestamp = epoch,
                                date = dateStr,
                                topic = d["topic"] as? String,
                                content = d["content"] as? String ?: d["text"] as? String ?: "",
                                pinned = d["pinned"] as? Boolean ?: false,
                                tags = d["tags"] as? String
                            )
                        )
                    }

                    // Pull Day Tracker
                    for (doc in partDay.documents) {
                        val d = doc.data ?: continue
                        if (repository.isDeletedOrTombstoned(doc.id)) continue
                        val (epoch, dateStr) = parseTimestamp(d["createdAt"] ?: d["ts"], d["targetDate"] as? String ?: d["date"] as? String)
                        repository.insertDayTrackerItem(
                            V3DayTrackerItem(
                                id = doc.id,
                                title = d["title"] as? String ?: "Milestone",
                                targetDate = dateStr,
                                hour = (d["hour"] as? Number)?.toInt() ?: 0,
                                pinned = d["pinned"] as? Boolean ?: false,
                                createdAt = epoch
                            )
                        )
                    }

                    // Pull Ledger (Life OS events)
                    for (doc in partLedger.documents) {
                        val d = doc.data ?: continue
                        if (repository.isDeletedOrTombstoned(doc.id)) continue
                        val (epoch, dateStr) = parseTimestamp(d["timestamp"] ?: d["ts"], d["date"] as? String)
                        val isoTs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(epoch))
                        repository.insertEvent(
                            V3Event(
                                id = doc.id,
                                category = d["category"] as? String ?: "general",
                                date = dateStr,
                                ts = isoTs,
                                timestamp = epoch,
                                title = d["title"] as? String,
                                content = d["content"] as? String ?: d["text"] as? String,
                                score = (d["score"] as? Number)?.toInt() ?: 0
                            )
                        )
                    }

                    // Pull Habits Domain Events & reconcile locally
                    try {
                        val hSnap = Tasks.await(userDoc.collection("plugins").document("com.v07.habits").collection("events").get())
                        for (doc in hSnap.documents) {
                            val d = doc.data ?: continue
                            if (repository.isDeletedOrTombstoned(doc.id)) continue
                            reconcileHabitDoc(doc.id, d)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed pulling habits from cloud: ${e.message}")
                    }

                    // Pull Other Known Plugins Domain Events
                    val knownPlugins = listOf(
                        "com.v07.vitals",
                        "com.v07.fitness",
                        "com.v07.gita",
                        "com.v07.running",
                        "com.v07.reader",
                        "com.v07.tasks",
                        "com.v07.timetracker",
                        "com.v07.gamify",
                        "com.v07.chronosynth"
                    )
                    for (pid in knownPlugins) {
                        try {
                            val pSnap = Tasks.await(userDoc.collection("plugins").document(pid).collection("events").get())
                            for (doc in pSnap.documents) {
                                val d = doc.data ?: continue
                                if (repository.isDeletedOrTombstoned(doc.id)) continue
                                val (epoch, dateStr) = parseTimestamp(d["timestamp"] ?: d["ts"], d["date"] as? String)
                                val cat = d["category"] as? String ?: "plugin"
                                val payloadJson = d["payloadJson"] as? String ?: JSONObject(d).toString()
                                repository.insertPluginEvent(
                                    V3PluginEvent(
                                        id = doc.id,
                                        pluginId = pid,
                                        category = cat,
                                        payloadJson = payloadJson,
                                        timestamp = epoch,
                                        date = dateStr
                                    )
                                )
                            }
                        } catch (_: Exception) {}
                    }

                    // Pull Remote Plugin States & Custom Plugins
                    try {
                        val pluginsDoc = Tasks.await(userDoc.collection("system").document("plugins").get())
                        if (pluginsDoc.exists()) {
                            val remoteList = pluginsDoc.get("plugins") as? List<Map<String, Any>>
                            if (remoteList != null) {
                                val registry = com.v07.v3.core.plugin.PluginRegistry.getInstance(context)
                                for (pMap in remoteList) {
                                    val pid = pMap["id"] as? String ?: continue
                                    val remoteEnabled = pMap["enabled"] as? Boolean ?: true
                                    if (remoteEnabled) registry.enablePlugin(pid) else registry.disablePlugin(pid)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                } else {
                    // DUAL-READ FALLBACK: Legacy users/{uid}/events
                    Log.i(TAG, "Partitioned collections empty, executing Dual-Read fallback from users/{uid}/events")
                    val legacyEventsCol = userDoc.collection("events")
                    val snap = Tasks.await(legacyEventsCol.get())
                    val localTombs = repository.getAllTombstoneIds()
                    for (doc in snap.documents) {
                        val data = doc.data ?: continue
                        if (localTombs.contains(doc.id) || repository.isDeletedOrTombstoned(doc.id)) {
                            try { doc.reference.delete() } catch (_: Exception) {}
                            continue
                        }
                        migrateSingleEvent(doc.id, data)
                    }
                }

                // Record sync watermarks
                repository.setSyncWatermark("sessions", now)
                repository.setSyncWatermark("tasks", now)
                repository.setSyncWatermark("thoughts", now)
                repository.setSyncWatermark("daytracker", now)
                repository.setSyncWatermark("ledger", now)

                prefs.edit().putLong(KEY_LAST_SYNCED, now).apply()
                isSyncing.set(false)
                Log.i(TAG, "V3 Companion bidirectional cloud sync completed successfully.")
                onComplete(true, null)
            } catch (e: Exception) {
                isSyncing.set(false)
                Log.e(TAG, "V3 Companion cloud sync error: ${e.message}", e)
                val friendlyMsg = if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    "Cloud sync permission denied. Please sign in to your cloud account."
                } else {
                    e.message ?: "Sync failed"
                }
                onComplete(false, friendlyMsg)
            }
        }
    }

    private fun formatRelativeTime(ts: Long): String {
        val diffSec = ((System.currentTimeMillis() - ts) / 1000).coerceAtLeast(0)
        return when {
            diffSec < 60 -> "Just now"
            diffSec < 3600 -> "${diffSec / 60}m ago"
            diffSec < 86400 -> "${diffSec / 3600}h ago"
            else -> "${diffSec / 86400}d ago"
        }
    }

    private fun jsonToPlainJava(obj: Any?): Any? {
        return when (obj) {
            null, JSONObject.NULL -> null
            is JSONObject -> {
                val map = HashMap<String, Any?>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = jsonToPlainJava(obj.opt(k))
                }
                map
            }
            is JSONArray -> {
                val list = ArrayList<Any?>()
                for (i in 0 until obj.length()) {
                    list.add(jsonToPlainJava(obj.opt(i)))
                }
                list
            }
            is Number, is Boolean, is String -> obj
            else -> obj.toString()
        }
    }
}
