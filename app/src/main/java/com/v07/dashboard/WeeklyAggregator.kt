package com.v07.dashboard

import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Backend Aggregation Engine for Weekly Telemetry & AI Synthesis.
 *
 * Gathers and correlates data across all personal life domains:
 * - Focus sessions & deep work
 * - Tasks velocity & backlog
 * - Habits & daily discipline
 * - Mental wellness (mood, energy, emotional notes)
 * - Physical athletics (running, pushups, workouts)
 * - Technical mastery (DSA problem solving)
 * - Strategic gaming (Chess records)
 * - Screen time & digital attention audits (Android usage stats)
 * - Philosophical & journal reflections (Gita, thoughts, journal)
 *
 * Compiles a rich telemetry digest and prompt context for LLM generation.
 */
object WeeklyAggregator {

    /**
     * Resolves start and end dates from explicit parameters or week offset.
     * Offset 0 = Current week (Monday to Sunday)
     * Offset 1 = Last week
     */
    fun resolveDateRange(startStr: String?, endStr: String?, offset: Int? = null): Pair<LocalDate, LocalDate> {
        if (!startStr.isNullOrBlank() && !endStr.isNullOrBlank()) {
            return try {
                val s = LocalDate.parse(startStr.trim())
                val e = LocalDate.parse(endStr.trim())
                if (s.isAfter(e)) Pair(e, s) else Pair(s, e)
            } catch (_: Exception) {
                fallbackWeekRange(offset ?: 0)
            }
        }
        return fallbackWeekRange(offset ?: 0)
    }

    private fun fallbackWeekRange(offset: Int): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val currentMonday = today.minusDays((dayOfWeek - 1).toLong())
        val targetMonday = currentMonday.minusWeeks(offset.coerceAtLeast(0).toLong())
        val targetSunday = targetMonday.plusDays(6)
        return Pair(targetMonday, targetSunday)
    }

    /**
     * Collects and aggregates all logs from the backend for the given period.
     */
    fun aggregate(startStr: String? = null, endStr: String? = null, offset: Int? = null): Map<String, Any?> {
        val (startDate, endDate) = resolveDateRange(startStr, endStr, offset)
        val sDateStr = startDate.toString()
        val eDateStr = endDate.toString()
        val daysCount = (ChronoUnit.DAYS.between(startDate, endDate) + 1).toInt().coerceAtLeast(1)

        val dateList = (0 until daysCount).map { startDate.plusDays(it.toLong()).toString() }

        // 1. Focus Sessions
        val rawSessions = eventsInRangeRaw("sessions", sDateStr, eDateStr)
        val focusAggregation = aggregateFocus(rawSessions, dateList)

        // 2. Tasks
        val rawTasks = eventsInRangeRaw("tasks", sDateStr, eDateStr)
        val tasksAggregation = aggregateTasks(rawTasks, sDateStr, eDateStr)

        // 3. Habits
        val rawHabits = eventsInRangeRaw("habits", sDateStr, eDateStr)
        val habitsAggregation = aggregateHabits(rawHabits, dateList)

        // 4. Mental State & Energy
        val rawMood = eventsInRangeRaw("mood", sDateStr, eDateStr)
        val rawEnergy = eventsInRangeRaw("energy", sDateStr, eDateStr)
        val wellnessAggregation = aggregateWellness(rawMood, rawEnergy)

        // 5. Athletics (Running & Pushups)
        val rawRunning = eventsInRangeRaw("running", sDateStr, eDateStr)
        val rawMetrics = eventsInRangeRaw("metrics", sDateStr, eDateStr)
        val athleticsAggregation = aggregateAthletics(rawRunning, rawMetrics)

        // 6. Thoughts & Brain Dumps
        val rawThoughts = eventsInRangeRaw("thought", sDateStr, eDateStr)
        val thoughtsAggregation = aggregateThoughts(rawThoughts)

        // 7. Screen Time & Digital Wellness
        val screenTimeAggregation = aggregateScreenTime(dateList, rawMetrics)

        // 8. Gita & Philosophical Readings
        val rawGita = eventsInRangeRaw("gita", sDateStr, eDateStr)
        val gitaAggregation = aggregateGita(rawGita)

        // 9. Journal & Daily Notes highlights
        val rawJournal = eventsInRangeRaw("journal", sDateStr, eDateStr)
        val journalAggregation = aggregateJournal(rawJournal, dateList)

        // Top-level summary metrics
        val totalFocusMin = (focusAggregation["total_minutes"] as? Number)?.toInt() ?: 0
        val totalFocusHours = (focusAggregation["total_hours"] as? Number)?.toDouble() ?: 0.0
        val tasksCompleted = (tasksAggregation["completed_count"] as? Number)?.toInt() ?: 0
        val habitConsistencyPct = (habitsAggregation["consistency_pct"] as? Number)?.toInt() ?: 0
        val avgMood = (wellnessAggregation["avg_mood_score"] as? Number)?.toDouble() ?: 0.0
        val avgEnergy = (wellnessAggregation["avg_energy_score"] as? Number)?.toDouble() ?: 0.0
        val totalRunKm = (athleticsAggregation["total_distance_km"] as? Number)?.toDouble() ?: 0.0
        val totalPushups = (athleticsAggregation["total_pushups"] as? Number)?.toInt() ?: 0
        val screenTimeHours = (screenTimeAggregation["total_screen_time_hours"] as? Number)?.toDouble() ?: 0.0
        val doomScrollMin = (screenTimeAggregation["doom_scroll_minutes"] as? Number)?.toInt() ?: 0

        val summaryMetrics = mapOf(
            "start_date" to sDateStr,
            "end_date" to eDateStr,
            "days_count" to daysCount,
            "total_focus_minutes" to totalFocusMin,
            "total_focus_hours" to totalFocusHours,
            "tasks_completed" to tasksCompleted,
            "habit_consistency_pct" to habitConsistencyPct,
            "avg_mood_score" to avgMood,
            "avg_energy_score" to avgEnergy,
            "total_run_km" to totalRunKm,
            "total_pushups" to totalPushups,
            "screen_time_hours" to screenTimeHours,
            "doom_scroll_minutes" to doomScrollMin
        )

        // Construct high-density markdown context for LLM prompt
        val promptContext = buildPromptContext(
            sDateStr, eDateStr, daysCount, summaryMetrics,
            focusAggregation, tasksAggregation, habitsAggregation,
            wellnessAggregation, athleticsAggregation,
            thoughtsAggregation, screenTimeAggregation,
            gitaAggregation, journalAggregation
        )

        return mapOf(
            "ok" to true,
            "range" to mapOf("start" to sDateStr, "end" to eDateStr, "days" to daysCount),
            "summary_metrics" to summaryMetrics,
            "focus" to focusAggregation,
            "tasks" to tasksAggregation,
            "habits" to habitsAggregation,
            "wellness" to wellnessAggregation,
            "athletics" to athleticsAggregation,
            "thoughts" to thoughtsAggregation,
            "screen_time" to screenTimeAggregation,
            "gita" to gitaAggregation,
            "journal" to journalAggregation,
            "prompt_context" to promptContext
        )
    }

    // ─── 1. Focus Sessions Aggregation ──────────────────────────────────────

    private fun aggregateFocus(sessions: List<Map<String, Any?>>, dateList: List<String>): Map<String, Any?> {
        val completed = sessions.filter {
            val t = it["type"] as? String
            val d = getDurationNumber(it)
            (t == "end" || d > 0)
        }

        var totalMinutes = 0
        val byActivity = mutableMapOf<String, MutableMap<String, Any?>>()
        val byDay = dateList.associateWith { 0 }.toMutableMap()
        var peakSession: Map<String, Any?>? = null
        var peakDuration = 0
        val allTitles = mutableSetOf<String>()

        for (s in completed) {
            val d = getDurationNumber(s).roundToInt()
            if (d <= 0) continue
            totalMinutes += d

            val act = (s["activity"] as? String)?.lowercase() ?: "other"
            val title = (s["title"] as? String ?: s["custom_label"] as? String ?: "").trim()
            if (title.isNotEmpty()) allTitles.add(title)

            val date = s["date"] as? String ?: ""
            if (date.isNotEmpty() && byDay.containsKey(date)) {
                byDay[date] = (byDay[date] ?: 0) + d
            }

            if (d > peakDuration) {
                peakDuration = d
                peakSession = mapOf(
                    "activity" to act,
                    "duration_min" to d,
                    "title" to title,
                    "date" to date
                )
            }

            val actEntry = byActivity.getOrPut(act) {
                mutableMapOf(
                    "activity" to act,
                    "minutes" to 0,
                    "sessions_count" to 0,
                    "topics" to mutableListOf<String>()
                )
            }
            actEntry["minutes"] = (actEntry["minutes"] as Int) + d
            actEntry["sessions_count"] = (actEntry["sessions_count"] as Int) + 1
            if (title.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                val topics = actEntry["topics"] as MutableList<String>
                if (!topics.contains(title)) topics.add(title)
            }
        }

        // Calculate percentages & hours for activities
        val actList = byActivity.values.map { entry ->
            val m = entry["minutes"] as Int
            val pct = if (totalMinutes > 0) ((m.toDouble() / totalMinutes) * 1000).roundToInt() / 10.0 else 0.0
            val hrs = (m / 60.0 * 10).roundToInt() / 10.0
            mapOf(
                "activity" to entry["activity"],
                "minutes" to m,
                "hours" to hrs,
                "percentage" to pct,
                "sessions_count" to entry["sessions_count"],
                "topics" to entry["topics"]
            )
        }.sortedByDescending { (it["minutes"] as? Int) ?: 0 }

        val peakDay = byDay.maxByOrNull { it.value }?.key ?: ""
        val totalHours = (totalMinutes / 60.0 * 10).roundToInt() / 10.0

        return mapOf(
            "total_minutes" to totalMinutes,
            "total_hours" to totalHours,
            "session_count" to completed.size,
            "by_activity" to actList,
            "daily_minutes" to byDay,
            "peak_day" to peakDay,
            "peak_session" to peakSession,
            "topics" to allTitles.toList()
        )
    }

    private fun getDurationNumber(m: Map<String, Any?>): Double {
        return (m["duration"] as? Number)?.toDouble()
            ?: (m["duration_min"] as? Number)?.toDouble()
            ?: (m["duration_minutes"] as? Number)?.toDouble()
            ?: (m["minutes"] as? Number)?.toDouble()
            ?: 0.0
    }

    // ─── 2. Tasks Aggregation ────────────────────────────────────────────────

    private fun aggregateTasks(events: List<Map<String, Any?>>, start: String, end: String): Map<String, Any?> {
        // Build task lookup table for task titles
        val allTasks = rebuildTaskState()
        val taskTitleMap = allTasks.associate { it.id to (it.title ?: "Untitled Task") }
        val taskPriorityMap = allTasks.associate { it.id to (it.priority ?: "medium") }

        val completed = mutableListOf<Map<String, Any?>>()
        val created = mutableListOf<Map<String, Any?>>()

        for (e in events) {
            val action = e["action"] as? String ?: continue
            val date = e["date"] as? String ?: ""
            if (action == "completed") {
                val tid = e["task_id"] as? String ?: e["id"] as? String ?: ""
                val title = e["title"] as? String ?: taskTitleMap[tid] ?: "Task ($tid)"
                val priority = e["priority"] as? String ?: taskPriorityMap[tid] ?: "medium"
                completed.add(mapOf("id" to tid, "title" to title, "priority" to priority, "date" to date))
            } else if (action == "created") {
                val tid = e["id"] as? String ?: ""
                val title = e["title"] as? String ?: "Untitled"
                val priority = e["priority"] as? String ?: "medium"
                val deadline = e["deadline"] as? String ?: ""
                created.add(mapOf("id" to tid, "title" to title, "priority" to priority, "deadline" to deadline, "date" to date))
            }
        }

        val pending = allTasks.filter { it.status == "pending" }.map {
            mapOf("id" to it.id, "title" to (it.title ?: "Untitled"), "priority" to (it.priority ?: "medium"), "deadline" to (it.deadline ?: ""))
        }

        val highPriorityCompleted = completed.count { it["priority"] == "high" }
        val velocity = if (created.isNotEmpty()) ((completed.size.toDouble() / created.size) * 100).roundToInt() else 100

        return mapOf(
            "completed_count" to completed.size,
            "created_count" to created.size,
            "pending_count" to pending.size,
            "velocity_pct" to velocity,
            "high_priority_completed" to highPriorityCompleted,
            "completed_tasks" to completed,
            "created_tasks" to created,
            "pending_tasks" to pending.take(10)
        )
    }

    // ─── 3. Habits Aggregation ───────────────────────────────────────────────

    private fun aggregateHabits(habits: List<Map<String, Any?>>, dateList: List<String>): Map<String, Any?> {
        val activeDates = mutableSetOf<String>()
        val habitCounts = mutableMapOf<String, Int>()

        for (h in habits) {
            val date = h["date"] as? String ?: continue
            val on = h["on"] == true || h["done"] == true
            if (on) {
                activeDates.add(date)
                val act = (h["activity"] as? String ?: h["habit"] as? String ?: "habit").lowercase()
                habitCounts[act] = (habitCounts[act] ?: 0) + 1
            }
        }

        val daysCount = dateList.size.coerceAtLeast(1)
        val consistencyPct = ((activeDates.size.toDouble() / daysCount) * 100).roundToInt()

        val breakdown = habitCounts.entries.map { (name, count) ->
            mapOf(
                "name" to name,
                "completed_days" to count,
                "percentage" to ((count.toDouble() / daysCount) * 100).roundToInt()
            )
        }.sortedByDescending { (it["completed_days"] as? Int) ?: 0 }

        return mapOf(
            "active_days" to activeDates.size,
            "total_days" to daysCount,
            "consistency_pct" to consistencyPct,
            "habits_breakdown" to breakdown
        )
    }

    // ─── 4. Wellness / Mood & Energy Aggregation ─────────────────────────────

    private fun aggregateWellness(moods: List<Map<String, Any?>>, energies: List<Map<String, Any?>>): Map<String, Any?> {
        val moodScores = mutableListOf<Int>()
        val moodFreq = mutableMapOf<String, Int>()
        val notes = mutableListOf<String>()

        for (m in moods) {
            val name = (m["mood"] as? String)?.lowercase() ?: continue
            moodFreq[name] = (moodFreq[name] ?: 0) + 1
            val explicitScore = (m["mood_score"] as? Number)?.toInt()
            val score = explicitScore ?: MOOD_SCORES[name] ?: if (name.contains("happy") || name.contains("good")) 8 else 5
            moodScores.add(score)
            val n = (m["note"] as? String ?: m["text"] as? String ?: "").trim()
            if (n.isNotEmpty() && !notes.contains(n)) notes.add(n)
        }

        val energyScores = mutableListOf<Int>()
        val energyFreq = mutableMapOf<String, Int>()
        for (e in energies) {
            val name = (e["energy"] as? String)?.lowercase() ?: continue
            energyFreq[name] = (energyFreq[name] ?: 0) + 1
            val explicitScore = (e["energy_score"] as? Number)?.toInt()
            val score = explicitScore ?: ENERGY_SCORES[name] ?: 3
            energyScores.add(score)
            val n = (e["note"] as? String ?: e["text"] as? String ?: "").trim()
            if (n.isNotEmpty() && !notes.contains(n)) notes.add(n)
        }

        val avgMood = if (moodScores.isNotEmpty()) ((moodScores.average() * 10).roundToInt() / 10.0) else 7.0
        val avgEnergy = if (energyScores.isNotEmpty()) ((energyScores.average() * 10).roundToInt() / 10.0) else 3.5

        val dominantMood = moodFreq.maxByOrNull { it.value }?.key ?: "steady"
        val dominantEnergy = energyFreq.maxByOrNull { it.value }?.key ?: "moderate"

        return mapOf(
            "avg_mood_score" to avgMood,
            "avg_energy_score" to avgEnergy,
            "dominant_mood" to dominantMood,
            "dominant_energy" to dominantEnergy,
            "mood_counts" to moodFreq,
            "energy_counts" to energyFreq,
            "reflections" to notes
        )
    }

    // ─── 5. Athletics (Running & Pushups) Aggregation ─────────────────────────

    private fun aggregateAthletics(runs: List<Map<String, Any?>>, metrics: List<Map<String, Any?>>): Map<String, Any?> {
        var totalKm = 0.0
        var totalRunMin = 0
        val runNotes = mutableListOf<String>()
        val runsList = mutableListOf<Map<String, Any?>>()

        for (r in runs) {
            val km = (r["distance_km"] as? Number)?.toDouble() ?: 0.0
            val rawSec = (r["duration_sec"] as? Number)?.toInt()
            var durMin = if (rawSec != null && rawSec > 0) {
                (rawSec / 60.0).roundToInt()
            } else {
                val raw = (r["duration_min"] as? Number)?.toDouble()
                    ?: (r["duration"] as? Number)?.toDouble()
                    ?: (r["minutes"] as? Number)?.toDouble()
                    ?: (r["duration_min"] as? String)?.toDoubleOrNull()
                    ?: 0.0
                val isSec = if (km > 0) (raw / km > 20.0) else (raw >= 100.0)
                if (isSec) (raw / 60.0).roundToInt() else raw.roundToInt()
            }
            totalKm += km
            totalRunMin += durMin

            val note = (r["notes"] as? String ?: "").trim()
            if (note.isNotEmpty()) runNotes.add(note)

            runsList.add(mapOf(
                "date" to (r["date"] as? String ?: ""),
                "distance_km" to km,
                "duration_min" to durMin,
                "run_type" to (r["run_type"] as? String ?: "run"),
                "notes" to note
            ))
        }

        // Pushups sum across running & metrics
        var pushups = runs.sumOf { (it["pushups"] as? Number)?.toInt() ?: 0 }
        pushups += metrics.sumOf { (it["pushups"] as? Number)?.toInt() ?: 0 }

        val avgPace = if (totalKm > 0.1) {
            val paceVal = totalRunMin / totalKm
            val pMin = paceVal.toInt()
            val pSec = ((paceVal - pMin) * 60).roundToInt()
            String.format("%d:%02d min/km", pMin, pSec)
        } else "N/A"

        return mapOf(
            "total_distance_km" to ((totalKm * 100).roundToInt() / 100.0),
            "total_duration_min" to totalRunMin,
            "runs_count" to runs.size,
            "average_pace" to avgPace,
            "total_pushups" to pushups,
            "runs" to runsList,
            "notes" to runNotes
        )
    }

    // ─── 6. Thoughts & Brain Dumps Aggregation ───────────────────────────────

    private fun aggregateThoughts(thoughts: List<Map<String, Any?>>): List<Map<String, Any?>> {
        return thoughts.mapNotNull { t ->
            val text = (t["text"] as? String ?: t["note"] as? String ?: "").trim()
            if (text.isEmpty()) null
            else mapOf(
                "topic" to (t["topic"] as? String ?: "Insight"),
                "text" to text,
                "date" to (t["date"] as? String ?: "")
            )
        }
    }

    // ─── 9. Screen Time & Digital Attention Aggregation ──────────────────────

    private fun aggregateScreenTime(dateList: List<String>, metrics: List<Map<String, Any?>>): Map<String, Any?> {
        var totalSec = 0L
        val appUsageMap = mutableMapOf<String, MutableMap<String, Any?>>()

        for (date in dateList) {
            val file = VaultConfig.appUsageFile(date)
            if (file.exists()) {
                try {
                    val root = jsonToMap(file.readText())
                    @Suppress("UNCHECKED_CAST")
                    val summary = root["summary"] as? Map<String, Any?>
                    val daySec = (summary?.get("total_screen_time_sec") as? Number)?.toLong() ?: 0L
                    totalSec += daySec

                    @Suppress("UNCHECKED_CAST")
                    val usageList = root["usage"] as? List<Map<String, Any?>> ?: emptyList()
                    for (u in usageList) {
                        val pkg = (u["package"] as? String ?: "").trim()
                        val app = (u["app"] as? String ?: pkg.split('.').lastOrNull() ?: "App").trim()
                        if (pkg.isEmpty()) continue

                        val dur = (u["total_duration_sec"] as? Number)?.toLong() ?: 0L
                        val entry = appUsageMap.getOrPut(pkg) {
                            mutableMapOf("app" to app, "package" to pkg, "duration_sec" to 0L, "launches" to 0)
                        }
                        entry["duration_sec"] = (entry["duration_sec"] as Long) + dur
                        entry["launches"] = (entry["launches"] as Int) + ((u["launch_count"] as? Number)?.toInt() ?: 0)
                    }
                } catch (_: Exception) {}
            }
        }

        val totalHours = (totalSec / 3600.0 * 10).roundToInt() / 10.0
        val dailyAvgSec = if (dateList.isNotEmpty()) totalSec / dateList.size else 0L

        val topApps = appUsageMap.values.map { entry ->
            val sec = entry["duration_sec"] as Long
            val pct = if (totalSec > 0) ((sec.toDouble() / totalSec) * 1000).roundToInt() / 10.0 else 0.0
            val h = sec / 3600
            val m = (sec % 3600) / 60
            val formatted = if (h > 0) "${h}h ${m}m" else "${m}m"
            mapOf(
                "app" to entry["app"],
                "package" to entry["package"],
                "duration_sec" to sec,
                "duration_formatted" to formatted,
                "percentage" to pct,
                "launches" to entry["launches"],
                "icon_url" to "/app-icon?package=${entry["package"]}&size=64"
            )
        }.sortedByDescending { (it["duration_sec"] as? Long) ?: 0L }.take(7)

        val doomScrollMin = metrics.sumOf { (it["doom_scroll"] as? Number)?.toInt() ?: 0 }

        return mapOf(
            "total_screen_time_sec" to totalSec,
            "total_screen_time_hours" to totalHours,
            "total_screen_time_formatted" to formatDurationSec(totalSec),
            "daily_avg_screen_time_formatted" to formatDurationSec(dailyAvgSec),
            "top_apps" to topApps,
            "doom_scroll_minutes" to doomScrollMin
        )
    }

    private fun formatDurationSec(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    // ─── 10. Gita & Reading Aggregation ──────────────────────────────────────

    private fun aggregateGita(gitaEvents: List<Map<String, Any?>>): Map<String, Any?> {
        val verses = gitaEvents.mapNotNull { it["verse_id"] as? String }.distinct()
        val reviews = gitaEvents.filter { it["review_type"] != null }
        return mapOf(
            "verses_studied" to verses.size,
            "verses" to verses,
            "reviews_count" to reviews.size
        )
    }

    // ─── 11. Journal & Daily Notes Aggregation ───────────────────────────────

    private fun aggregateJournal(journalEvents: List<Map<String, Any?>>, dateList: List<String>): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        for (j in journalEvents) {
            val text = (j["text"] as? String ?: j["note"] as? String ?: "").trim()
            val date = j["date"] as? String ?: ""
            if (text.isNotEmpty()) list.add(mapOf("date" to date, "text" to text))
        }

        // Check Obsidian 01 - Daily notes folder for matching date notes
        val dailyDir = File(VaultConfig.vaultDir, "01 - Daily")
        if (dailyDir.exists()) {
            for (d in dateList) {
                val year = d.take(4)
                val candidates = listOf(
                    File(dailyDir, "$year/$d.md"),
                    File(dailyDir, "$d.md")
                )
                for (cand in candidates) {
                    if (cand.exists()) {
                        try {
                            val lines = cand.readLines().filter { it.isNotBlank() && !it.startsWith("---") && !it.startsWith("#") }
                            val snippet = lines.take(3).joinToString(" ").trim()
                            if (snippet.isNotEmpty()) {
                                list.add(mapOf("date" to d, "text" to snippet, "source" to "daily_note"))
                            }
                        } catch (_: Exception) {}
                        break
                    }
                }
            }
        }
        return list.take(10)
    }

    // ─── Prompt Context Builder ──────────────────────────────────────────────

    private fun buildPromptContext(
        startDate: String,
        endDate: String,
        days: Int,
        metrics: Map<String, Any?>,
        focus: Map<String, Any?>,
        tasks: Map<String, Any?>,
        habits: Map<String, Any?>,
        wellness: Map<String, Any?>,
        athletics: Map<String, Any?>,
        thoughts: List<Map<String, Any?>>,
        screenTime: Map<String, Any?>,
        gita: Map<String, Any?>,
        journal: List<Map<String, String>>
    ): String = buildString {
        appendLine("### V07 USER WEEKLY TELEMETRY DIGEST")
        appendLine("**Period**: $startDate to $endDate ($days days)")
        appendLine("**Top-Level Metrics**:")
        appendLine("- Focus: ${metrics["total_focus_hours"]}h (${metrics["total_focus_minutes"]}m)")
        appendLine("- Tasks Completed: ${metrics["tasks_completed"]} | Velocity: ${(tasks["velocity_pct"])}%")
        appendLine("- Habit Consistency: ${metrics["habit_consistency_pct"]}%")
        appendLine("- Avg Mood: ${metrics["avg_mood_score"]}/10 | Dominant: ${wellness["dominant_mood"]}")
        appendLine("- Avg Energy: ${metrics["avg_energy_score"]}/5 | Dominant: ${wellness["dominant_energy"]}")
        appendLine("- Running: ${metrics["total_run_km"]} km | Pushups: ${metrics["total_pushups"]}")
        appendLine("- Screen Time: ${screenTime["total_screen_time_formatted"]} (Avg ${screenTime["daily_avg_screen_time_formatted"]}/day)")
        appendLine("- Doom Scroll: ${metrics["doom_scroll_minutes"]}m")
        appendLine()

        // 1. Focus
        appendLine("#### 1. DEEP WORK & FOCUS BREAKDOWN (${focus["total_hours"]}h across ${focus["session_count"]} sessions)")
        @Suppress("UNCHECKED_CAST")
        val acts = focus["by_activity"] as? List<Map<String, Any?>> ?: emptyList()
        if (acts.isEmpty()) appendLine("- No structured focus sessions recorded.")
        else {
            for (a in acts) {
                @Suppress("UNCHECKED_CAST")
                val topics = (a["topics"] as? List<String>)?.take(3)?.joinToString(", ") ?: ""
                val topicSuffix = if (topics.isNotEmpty()) " (Topics: $topics)" else ""
                appendLine("- ${a["activity"]}: ${a["hours"]}h (${a["percentage"]}%) across ${a["sessions_count"]} sessions$topicSuffix")
            }
        }
        val peak = focus["peak_session"] as? Map<String, Any?>
        if (peak != null) {
            appendLine("- Peak Session: ${peak["activity"]} for ${peak["duration_min"]}m on ${peak["date"]} ('${peak["title"]}')")
        }
        appendLine()

        // 2. Tasks
        appendLine("#### 2. TASK EXECUTION (Completed: ${tasks["completed_count"]}, Created: ${tasks["created_count"]})")
        @Suppress("UNCHECKED_CAST")
        val completedTasks = tasks["completed_tasks"] as? List<Map<String, Any?>> ?: emptyList()
        if (completedTasks.isEmpty()) appendLine("- No tasks marked completed.")
        else {
            for (t in completedTasks.take(8)) {
                appendLine("- [x] ${t["title"]} [${t["priority"]}] (${t["date"]})")
            }
        }
        @Suppress("UNCHECKED_CAST")
        val pendingTasks = tasks["pending_tasks"] as? List<Map<String, Any?>> ?: emptyList()
        if (pendingTasks.isNotEmpty()) {
            appendLine("- Pending Backlog (${tasks["pending_count"]} total): " + pendingTasks.take(5).joinToString("; ") { "${it["title"]} [${it["priority"]}]" })
        }
        appendLine()

        // 3. Habits
        appendLine("#### 3. HABITS & ROUTINES (${habits["active_days"]}/$days active days - ${habits["consistency_pct"]}%)")
        @Suppress("UNCHECKED_CAST")
        val hBreakdown = habits["habits_breakdown"] as? List<Map<String, Any?>> ?: emptyList()
        for (h in hBreakdown) {
            appendLine("- ${h["name"]}: ${h["completed_days"]}/$days days (${h["percentage"]}%)")
        }
        appendLine()

        // 4. Mental State & Energy
        appendLine("#### 4. MENTAL & EMOTIONAL PATTERNS")
        appendLine("- Mood: Avg ${wellness["avg_mood_score"]}/10 | Dominant: ${wellness["dominant_mood"]}")
        appendLine("- Energy: Avg ${wellness["avg_energy_score"]}/5 | Dominant: ${wellness["dominant_energy"]}")
        @Suppress("UNCHECKED_CAST")
        val reflections = wellness["reflections"] as? List<String> ?: emptyList()
        if (reflections.isNotEmpty()) {
            appendLine("- Logged Journal Notes & Reflections:")
            for (r in reflections.take(6)) {
                appendLine("  * \"$r\"")
            }
        }
        appendLine()

        // 5. Athletics
        appendLine("#### 5. PHYSICAL TRAINING & RUNNING")
        appendLine("- Total Distance: ${athletics["total_distance_km"]} km across ${athletics["runs_count"]} runs (Pace: ${athletics["average_pace"]})")
        appendLine("- Pushups Logged: ${athletics["total_pushups"]}")
        @Suppress("UNCHECKED_CAST")
        val rNotes = athletics["notes"] as? List<String> ?: emptyList()
        if (rNotes.isNotEmpty()) {
            appendLine("- Run Notes: " + rNotes.take(3).joinToString("; "))
        }
        appendLine()

        // 6. Screen Time & Distractions
        appendLine("#### 6. DIGITAL ATTENTION & APP USAGE")
        appendLine("- Total Screen Time: ${screenTime["total_screen_time_formatted"]} (Daily Avg: ${screenTime["daily_avg_screen_time_formatted"]})")
        appendLine("- Doom Scrolling Logged: ${screenTime["doom_scroll_minutes"]}m")
        @Suppress("UNCHECKED_CAST")
        val topApps = screenTime["top_apps"] as? List<Map<String, Any?>> ?: emptyList()
        if (topApps.isNotEmpty()) {
            appendLine("- Top Apps:")
            for (app in topApps.take(5)) {
                appendLine("  * ${app["app"]}: ${app["duration_formatted"]} (${app["percentage"]}%)")
            }
        }
        appendLine()

        // 9. Brain Dumps & Thoughts
        if (thoughts.isNotEmpty()) {
            appendLine("#### 9. BRAIN DUMPS & CAPTURED IDEAS")
            for (th in thoughts.take(6)) {
                appendLine("- [${th["topic"]}] ${th["text"]} (${th["date"]})")
            }
            appendLine()
        }

        // 10. Gita
        if ((gita["verses_studied"] as? Int ?: 0) > 0) {
            appendLine("#### 10. PHILOSOPHY & GITA")
            appendLine("- Verses Studied: ${gita["verses_studied"]} (${gita["verses"]})")
            appendLine()
        }

        // 11. Journal
        if (journal.isNotEmpty()) {
            appendLine("#### 11. DAILY NOTES & JOURNAL EXCERPTS")
            for (j in journal.take(5)) {
                appendLine("- (${j["date"]}) ${j["text"]}")
            }
            appendLine()
        }
    }

    /**
     * Saves the synthesized markdown summary directly into the Obsidian Vault.
     * Target directory: {vaultDir}/02 - Journal/Weekly/ or {vaultDir}/Weekly/
     */
    fun saveToVault(startDate: String, endDate: String, markdownContent: String, title: String? = null): File {
        val baseVault = VaultConfig.vaultDir
        val weeklyDir = File(baseVault, "02 - Journal/Weekly").let {
            if (!it.exists()) it.mkdirs()
            if (it.exists() && it.canWrite()) it else File(baseVault, "Weekly").also { d -> d.mkdirs() }
        }

        val safeTitle = (title?.ifBlank { null } ?: "Weekly-Review-${startDate}-to-${endDate}")
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val targetFile = File(weeklyDir, "$safeTitle.md")

        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val fullContent = buildString {
            if (!markdownContent.trimStart().startsWith("---")) {
                appendLine("---")
                appendLine("type: weekly_summary")
                appendLine("title: \"${title ?: "Weekly Review $startDate to $endDate"}\"")
                appendLine("start_date: $startDate")
                appendLine("end_date: $endDate")
                appendLine("generated_at: $timestamp")
                appendLine("synthesizer: Weekly Digest")
                appendLine("tags:")
                appendLine("  - review/weekly")
                appendLine("  - ai/weekly-digest")
                appendLine("---")
                appendLine()
            }
            appendLine(markdownContent.trim())
        }

        targetFile.writeText(fullContent)

        // Also record an event in journal.jsonl
        try {
            appendEvent("journal", mapOf(
                "type" to "weekly_summary",
                "title" to (title ?: "Weekly Review $startDate to $endDate"),
                "date" to endDate,
                "file_path" to targetFile.relativeToOrSelf(baseVault).path
            ))
        } catch (_: Exception) {}

        return targetFile
    }
}
