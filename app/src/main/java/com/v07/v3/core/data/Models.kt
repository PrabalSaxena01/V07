package com.v07.v3.core.data

import kotlinx.serialization.Serializable

// ─── 4 CORE MODULES ──────────────────────────────────────────────────────────

/**
 * Core Module 1: Session Timer
 */
@Serializable
data class V3Session(
    val id: String,
    val activity: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSec: Long = 0,
    val notes: String? = null
)

/**
 * Core Module 2: Tasks Discipline
 */
@Serializable
data class V3Task(
    val id: String,
    val title: String,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val status: String = "pending",   // pending, completed
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val deadline: String? = null
)

/**
 * Core Module 3: Thoughts & Reflections
 */
@Serializable
data class V3Thought(
    val id: String,
    val timestamp: Long,
    val date: String,
    val topic: String? = null,
    val content: String,
    val pinned: Boolean = false,
    val tags: String? = null
)

/**
 * Core Module 4: Screen Time & Passive Attention Telemetry
 */
@Serializable
data class V3AppUsage(
    val id: String,
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val category: String             // Productive, Neutral, Distracting
)

// ─── CORE UTILITIES ─────────────────────────────────────────────────────────

/**
 * Core Utility 1: Day Tracker (Milestones, Countdowns & Date Targets)
 */
@Serializable
data class V3DayTrackerItem(
    val id: String,
    val title: String,
    val targetDate: String,           // YYYY-MM-DD
    val hour: Int = 0,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Core Utility 2: Recycle Bin (Soft-Delete & Data Recovery)
 */
@Serializable
data class V3RecycleItem(
    val recycleId: String,
    val originalId: String,
    val category: String,             // 'tasks', 'thoughts', 'sessions', 'day_tracker'
    val dataJson: String,
    val deletedAt: Long = System.currentTimeMillis()
)

/**
 * Core Utility 3: Database Administration & Health Metrics
 */
@Serializable
data class V3DatabaseStats(
    val dbPath: String,
    val fileSizeBytes: Long,
    val tableCounts: Map<String, Long>,
    val isWalEnabled: Boolean
)

/**
 * Core Timeline Event (Chronological Day Stream)
 */
@Serializable
data class V3TimelineEvent(
    val id: String,
    val timestamp: Long,
    val timeFormatted: String,        // HH:mm
    val category: String,             // 'sessions', 'tasks', 'thoughts', 'app_usage', 'plugin'
    val title: String,
    val subtitle: String = "",
    val durationSec: Long = 0L,
    val isHighlight: Boolean = false
)

/**
 * Full Day Timeline Aggregate (Equivalent to V2 Day Tracker / Timeline)
 */
@Serializable
data class V3DayTimeline(
    val date: String,                 // YYYY-MM-DD
    val totalFocusSec: Long,
    val sessionCount: Int,
    val tasksCompletedCount: Int,
    val thoughtsCount: Int,
    val totalScreenTimeMs: Long,
    val events: List<V3TimelineEvent>
)

/**
 * Core Event / Micro-Capture (Cadence, Wins, Lessons, Check-In, Check-Out, Habits, Vitals, Pushups)
 */
@Serializable
data class V3Event(
    val id: String,
    val category: String,             // checkin, checkout, win, lesson, thought, pushups, habit, screentime, focus
    val date: String,                 // YYYY-MM-DD
    val ts: String,                   // ISO-8601
    val timestamp: Long,              // epoch ms
    val title: String? = null,
    val content: String? = null,
    val score: Int = 0,
    val metadataJson: String? = null
)

/**
 * Unified Entry model for the Unified Data Explorer
 */
@Serializable
data class V3UnifiedEntry(
    val id: String,
    val category: String,             // checkin, checkout, win, lesson, thought, session, task, habit, pushups, screentime
    val displayCategory: String,      // Human-readable: "Check-In", "Win", "Lesson", "Thought", "Focus Session", "Task", etc.
    val title: String,
    val subtitle: String = "",
    val content: String = "",
    val date: String,                 // YYYY-MM-DD
    val timeFormatted: String,        // HH:mm
    val timestamp: Long,
    val score: Int? = null,
    val durationSec: Long? = null,
    val tags: String? = null,
    val rawJson: String = ""
)

/**
 * Isolated Plugin Domain Event (Habits, Vitals, Fitness, Gita, Quests, etc.)
 */
@Serializable
data class V3PluginEvent(
    val id: String,
    val pluginId: String,
    val category: String,
    val payloadJson: String,
    val timestamp: Long,
    val date: String
)


