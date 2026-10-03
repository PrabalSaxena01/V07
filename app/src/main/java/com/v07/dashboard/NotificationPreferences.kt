package com.v07.dashboard

import android.content.Context
import android.content.SharedPreferences

object NotificationPreferences {
    private const val PREFS_NAME = "v07_notification_prefs"

    private const val KEY_IDLE_ALERT = "notify_idle_alert"
    private const val KEY_OVERDUE_ALERT = "notify_overdue_alert"
    private const val KEY_DUE_TODAY_ALERT = "notify_due_today_alert"
    private const val KEY_STREAK_REMINDER = "notify_streak_reminder"
    private const val KEY_HABITS_REMINDER = "notify_habits_reminder"
    private const val KEY_MOOD_REMINDER = "notify_mood_reminder"
    private const val KEY_TIME_PRIORITY = "notify_time_priority"
    private const val KEY_CHECKIN_REMINDER = "notify_checkin_reminder"
    private const val KEY_CHECKOUT_REMINDER = "notify_checkout_reminder"
    private const val KEY_CHECKIN_REMINDER_TIME = "notify_checkin_reminder_time"
    private const val KEY_CHECKOUT_REMINDER_TIME = "notify_checkout_reminder_time"
    private const val KEY_GITA_DAILY = "notify_gita_daily"
    private const val KEY_GITA_DAILY_TIME = "notify_gita_daily_time"
    private const val KEY_NOT_LOGGED_IN = "notify_not_logged_in"
    private const val KEY_CHECKLIST_REMINDER = "notify_checklist_reminder"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"

    private const val DEFAULT_ENABLED = true

    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    private fun prefs(): SharedPreferences = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isIdleAlertEnabled(): Boolean = prefs().getBoolean(KEY_IDLE_ALERT, DEFAULT_ENABLED)
    fun setIdleAlertEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_IDLE_ALERT, enabled).apply()

    fun isOverdueAlertEnabled(): Boolean = prefs().getBoolean(KEY_OVERDUE_ALERT, DEFAULT_ENABLED)
    fun setOverdueAlertEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_OVERDUE_ALERT, enabled).apply()

    fun isDueTodayAlertEnabled(): Boolean = prefs().getBoolean(KEY_DUE_TODAY_ALERT, DEFAULT_ENABLED)
    fun setDueTodayAlertEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_DUE_TODAY_ALERT, enabled).apply()

    fun isStreakReminderEnabled(): Boolean = prefs().getBoolean(KEY_STREAK_REMINDER, DEFAULT_ENABLED)
    fun setStreakReminderEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_STREAK_REMINDER, enabled).apply()

    fun isHabitsReminderEnabled(): Boolean = prefs().getBoolean(KEY_HABITS_REMINDER, false)
    fun setHabitsReminderEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_HABITS_REMINDER, enabled).apply()

    fun isMoodReminderEnabled(): Boolean = prefs().getBoolean(KEY_MOOD_REMINDER, false)
    fun setMoodReminderEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_MOOD_REMINDER, enabled).apply()

    fun isTimePriorityAlertEnabled(): Boolean = prefs().getBoolean(KEY_TIME_PRIORITY, DEFAULT_ENABLED)
    fun setTimePriorityAlertEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_TIME_PRIORITY, enabled).apply()

    fun isCheckinReminderEnabled(): Boolean = prefs().getBoolean(KEY_CHECKIN_REMINDER, false)
    fun setCheckinReminderEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_CHECKIN_REMINDER, enabled).apply()

    fun isCheckoutReminderEnabled(): Boolean = prefs().getBoolean(KEY_CHECKOUT_REMINDER, false)
    fun setCheckoutReminderEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_CHECKOUT_REMINDER, enabled).apply()

    fun getCheckinReminderTime(): String = prefs().getString(KEY_CHECKIN_REMINDER_TIME, "") ?: ""
    fun setCheckinReminderTime(time: String) = prefs().edit().putString(KEY_CHECKIN_REMINDER_TIME, time).apply()

    fun getCheckoutReminderTime(): String = prefs().getString(KEY_CHECKOUT_REMINDER_TIME, "") ?: ""
    fun setCheckoutReminderTime(time: String) = prefs().edit().putString(KEY_CHECKOUT_REMINDER_TIME, time).apply()

    fun isGitaDailyEnabled(): Boolean = prefs().getBoolean(KEY_GITA_DAILY, false)
    fun setGitaDailyEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_GITA_DAILY, enabled).apply()

    fun getGitaDailyTime(): String = prefs().getString(KEY_GITA_DAILY_TIME, "07:00") ?: "07:00"
    fun setGitaDailyTime(time: String) = prefs().edit().putString(KEY_GITA_DAILY_TIME, time).apply()

    fun isNotLoggedInEnabled(): Boolean = prefs().getBoolean(KEY_NOT_LOGGED_IN, false)
    fun setNotLoggedInEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_NOT_LOGGED_IN, enabled).apply()

    fun getLastNotLoggedInSent(): Long = prefs().getLong("last_not_logged_in_sent", 0)
    fun setLastNotLoggedInSent(time: Long) = prefs().edit().putLong("last_not_logged_in_sent", time).apply()

    fun isChecklistReminderEnabled(): Boolean = prefs().getBoolean(KEY_CHECKLIST_REMINDER, true)
    fun setChecklistReminderEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_CHECKLIST_REMINDER, enabled).apply()
    fun getLastChecklistReminderSent(): Long = prefs().getLong("last_checklist_reminder_sent", 0)
    fun setLastChecklistReminderSent(time: Long) = prefs().edit().putLong("last_checklist_reminder_sent", time).apply()

    fun isOverlayEnabled(): Boolean = prefs().getBoolean(KEY_OVERLAY_ENABLED, false)
    fun setOverlayEnabled(enabled: Boolean) = prefs().edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
}