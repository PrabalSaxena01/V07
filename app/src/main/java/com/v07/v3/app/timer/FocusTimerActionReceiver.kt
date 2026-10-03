package com.v07.v3.app.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.v07.v3.core.data.EventRepository

/**
 * FocusTimerActionReceiver
 *
 * Dedicated BroadcastReceiver for interactive notification action buttons.
 * Guarantees instantaneous, zero-latency execution on Android 8 through Android 15
 * without being blocked by background service start restrictions.
 */
class FocusTimerActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FocusTimerActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "Notification action received: $action")

        val repository = EventRepository.getInstance(context)
        val timerManager = TimerManager.getInstance(context, repository)

        when (action) {
            FocusTimerService.ACTION_PAUSE -> {
                Log.d(TAG, "Executing notification PAUSE")
                timerManager.pauseSession()
                FocusTimerService.updateNotification(context)
            }
            FocusTimerService.ACTION_RESUME -> {
                Log.d(TAG, "Executing notification RESUME")
                timerManager.resumeSession()
                FocusTimerService.updateNotification(context)
            }
            FocusTimerService.ACTION_RESET, FocusTimerService.ACTION_DISCARD -> {
                Log.d(TAG, "Executing notification RESET/DISCARD")
                timerManager.discardSession()
                FocusTimerService.stop(context)
            }
            FocusTimerService.ACTION_FINISH -> {
                Log.d(TAG, "Executing notification FINISH")
                timerManager.finishSession("Completed via notification action")
                FocusTimerService.stop(context)
            }
            FocusTimerService.ACTION_ADD_5M -> {
                Log.d(TAG, "Executing notification ADD_5M (+5 min)")
                timerManager.addMinutes(5)
                FocusTimerService.updateNotification(context)
            }
            else -> {
                Log.w(TAG, "Unrecognized notification action: $action")
            }
        }
    }
}
