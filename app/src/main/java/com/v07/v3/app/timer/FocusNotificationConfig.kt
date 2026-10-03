package com.v07.v3.app.timer

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * FocusNotificationConfig
 *
 * Manages user preferences for the active focus session notification, including:
 * - Dynamic vs custom titles
 * - Subtitle styles (Time/status, Bhagavad Gita shloka, Mindful prompt, or custom motto)
 * - Native progress bar display
 * - Configurable action buttons (Pause/Resume, Reset, Finish, +5m)
 * - Sound and vibration on countdown completion
 */
class FocusNotificationConfig private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "v3_session_notif_prefs"

        const val KEY_TITLE_MODE = "title_mode" // "topic", "sanctuary", "custom"
        const val KEY_CUSTOM_TITLE = "custom_title"
        const val KEY_SUBTITLE_MODE = "subtitle_mode" // "time", "gita", "mindful", "custom"
        const val KEY_CUSTOM_SUBTITLE = "custom_subtitle"
        const val KEY_SHOW_PROGRESS_BAR = "show_progress_bar"
        const val KEY_SHOW_PAUSE_ACTION = "show_pause_action"
        const val KEY_SHOW_RESET_ACTION = "show_reset_action"
        const val KEY_SHOW_FINISH_ACTION = "show_finish_action"
        const val KEY_SHOW_ADD_5M_ACTION = "show_add_5m_action"
        const val KEY_CHIME_ON_COMPLETE = "chime_on_complete"
        const val KEY_VIBRATE_ON_COMPLETE = "vibrate_on_complete"

        @Volatile
        private var instance: FocusNotificationConfig? = null

        fun getInstance(context: Context): FocusNotificationConfig {
            return instance ?: synchronized(this) {
                instance ?: FocusNotificationConfig(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var titleMode: String
        get() = prefs.getString(KEY_TITLE_MODE, "topic") ?: "topic"
        set(value) = prefs.edit().putString(KEY_TITLE_MODE, value).apply()

    var customTitle: String
        get() = prefs.getString(KEY_CUSTOM_TITLE, "Focus Sanctuary") ?: "Focus Sanctuary"
        set(value) = prefs.edit().putString(KEY_CUSTOM_TITLE, value).apply()

    var subtitleMode: String
        get() = prefs.getString(KEY_SUBTITLE_MODE, "time") ?: "time"
        set(value) = prefs.edit().putString(KEY_SUBTITLE_MODE, value).apply()

    var customSubtitle: String
        get() = prefs.getString(KEY_CUSTOM_SUBTITLE, "One mindful breath at a time.") ?: "One mindful breath at a time."
        set(value) = prefs.edit().putString(KEY_CUSTOM_SUBTITLE, value).apply()

    var showProgressBar: Boolean
        get() = prefs.getBoolean(KEY_SHOW_PROGRESS_BAR, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_PROGRESS_BAR, value).apply()

    var showPauseAction: Boolean
        get() = prefs.getBoolean(KEY_SHOW_PAUSE_ACTION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_PAUSE_ACTION, value).apply()

    var showResetAction: Boolean
        get() = prefs.getBoolean(KEY_SHOW_RESET_ACTION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_RESET_ACTION, value).apply()

    var showFinishAction: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FINISH_ACTION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FINISH_ACTION, value).apply()

    var showAdd5mAction: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ADD_5M_ACTION, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ADD_5M_ACTION, value).apply()

    var chimeOnComplete: Boolean
        get() = prefs.getBoolean(KEY_CHIME_ON_COMPLETE, true)
        set(value) = prefs.edit().putBoolean(KEY_CHIME_ON_COMPLETE, value).apply()

    var vibrateOnComplete: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ON_COMPLETE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE_ON_COMPLETE, value).apply()

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("titleMode", titleMode)
            put("customTitle", customTitle)
            put("subtitleMode", subtitleMode)
            put("customSubtitle", customSubtitle)
            put("showProgressBar", showProgressBar)
            put("showPauseAction", showPauseAction)
            put("showResetAction", showResetAction)
            put("showFinishAction", showFinishAction)
            put("showAdd5mAction", showAdd5mAction)
            put("chimeOnComplete", chimeOnComplete)
            put("vibrateOnComplete", vibrateOnComplete)
        }
    }

    fun updateFromJson(json: JSONObject) {
        val editor = prefs.edit()
        if (json.has("titleMode")) editor.putString(KEY_TITLE_MODE, json.getString("titleMode"))
        if (json.has("customTitle")) editor.putString(KEY_CUSTOM_TITLE, json.getString("customTitle"))
        if (json.has("subtitleMode")) editor.putString(KEY_SUBTITLE_MODE, json.getString("subtitleMode"))
        if (json.has("customSubtitle")) editor.putString(KEY_CUSTOM_SUBTITLE, json.getString("customSubtitle"))
        if (json.has("showProgressBar")) editor.putBoolean(KEY_SHOW_PROGRESS_BAR, json.getBoolean("showProgressBar"))
        if (json.has("showPauseAction")) editor.putBoolean(KEY_SHOW_PAUSE_ACTION, json.getBoolean("showPauseAction"))
        if (json.has("showResetAction")) editor.putBoolean(KEY_SHOW_RESET_ACTION, json.getBoolean("showResetAction"))
        if (json.has("showFinishAction")) editor.putBoolean(KEY_SHOW_FINISH_ACTION, json.getBoolean("showFinishAction"))
        if (json.has("showAdd5mAction")) editor.putBoolean(KEY_SHOW_ADD_5M_ACTION, json.getBoolean("showAdd5mAction"))
        if (json.has("chimeOnComplete")) editor.putBoolean(KEY_CHIME_ON_COMPLETE, json.getBoolean("chimeOnComplete"))
        if (json.has("vibrateOnComplete")) editor.putBoolean(KEY_VIBRATE_ON_COMPLETE, json.getBoolean("vibrateOnComplete"))
        editor.apply()
    }
}
