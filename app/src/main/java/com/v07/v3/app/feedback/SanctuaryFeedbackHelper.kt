package com.v07.v3.app.feedback

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * SanctuaryFeedbackHelper
 *
 * Central tactile haptic and audio feedback controller.
 * Provides intentional, responsive haptics and subtle click sounds across all Sanctuary actions.
 * Fully configurable and toggleable via main Sanctuary Settings.
 */
object SanctuaryFeedbackHelper {

    private const val PREFS_NAME = "v07_prefs"
    private const val KEY_HAPTIC_ENABLED = "sanctuary_haptic_feedback_enabled"
    private const val KEY_SOUND_ENABLED = "sanctuary_sound_feedback_enabled"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isHapticEnabled: Boolean
        get() = prefs?.getBoolean(KEY_HAPTIC_ENABLED, true) ?: true
        set(value) {
            prefs?.edit()?.putBoolean(KEY_HAPTIC_ENABLED, value)?.apply()
        }

    var isSoundEnabled: Boolean
        get() = prefs?.getBoolean(KEY_SOUND_ENABLED, true) ?: true
        set(value) {
            prefs?.edit()?.putBoolean(KEY_SOUND_ENABLED, value)?.apply()
        }

    fun isHapticEnabled(context: Context): Boolean {
        val sp = prefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_HAPTIC_ENABLED, true)
    }

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        val sp = prefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    fun isSoundEnabled(context: Context): Boolean {
        val sp = prefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        val sp = prefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    /**
     * Tactile click / tap feedback for standard buttons, chips, and switches.
     */
    fun performClick(context: Context, view: View? = null) {
        if (isHapticEnabled(context)) {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } else {
                vibrate(context, 14, VibrationEffect.EFFECT_CLICK)
            }
        }
        if (isSoundEnabled(context)) {
            playSystemClick(context)
        }
    }

    /**
     * Satisfying confirmation feedback for task completion, ritual log, or session finish.
     */
    fun performSuccess(context: Context) {
        if (isHapticEnabled(context)) {
            vibratePattern(context, longArrayOf(0, 30, 40, 50))
        }
        if (isSoundEnabled(context)) {
            playGentleTone(ToneGenerator.TONE_PROP_BEEP2, 100)
        }
    }

    /**
     * Warning or error haptic pulse for deletions, resets, or threshold alerts.
     */
    fun performWarning(context: Context) {
        if (isHapticEnabled(context)) {
            vibrate(context, 65, VibrationEffect.EFFECT_HEAVY_CLICK)
        }
        if (isSoundEnabled(context)) {
            playGentleTone(ToneGenerator.TONE_PROP_NACK, 120)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun vibrate(context: Context, durationMs: Long, effectType: Int) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    vibrator.vibrate(VibrationEffect.createPredefined(effectType))
                } catch (_: Exception) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(context: Context, timings: LongArray) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    private fun playSystemClick(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        } catch (_: Exception) {}
    }

    private fun playGentleTone(toneType: Int, durationMs: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 65)
            toneGen.startTone(toneType, durationMs)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try { toneGen.release() } catch (_: Exception) {}
            }, (durationMs + 80).toLong())
        } catch (_: Exception) {}
    }
}
