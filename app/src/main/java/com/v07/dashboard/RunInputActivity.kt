package com.v07.dashboard

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class RunInputActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_input)

        val distInput = findViewById<EditText>(R.id.runInputDistance)
        val durInput = findViewById<EditText>(R.id.runInputDuration)
        val titleInput = findViewById<EditText>(R.id.runInputTitle)
        val saveBtn = findViewById<TextView>(R.id.runInputSave)
        val cancelBtn = findViewById<TextView>(R.id.runInputCancel)

        distInput.requestFocus()

        saveBtn.setOnClickListener {
            val distStr = distInput.text.toString().trim()
            val durStr = durInput.text.toString().trim()
            if (distStr.isEmpty()) {
                distInput.error = "Required"
                return@setOnClickListener
            }
            val dist = distStr.toDoubleOrNull()
            if (dist == null || dist <= 0) {
                distInput.error = "Invalid"
                return@setOnClickListener
            }

            var durationSec = 0
            if (durStr.isNotEmpty()) {
                val parts = durStr.split(":")
                if (parts.size == 2) {
                    val mins = parts[0].toIntOrNull() ?: 0
                    val secs = parts[1].toIntOrNull() ?: 0
                    durationSec = mins * 60 + secs
                } else {
                    durationSec = (durStr.toDoubleOrNull()?.times(60))?.toInt() ?: 0
                }
            }

            val title = titleInput.text.toString().trim().ifEmpty { "Run" }
            logRun(dist, durationSec, title)
            refreshWidget()
            finish()
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun logRun(distanceKm: Double, durationSec: Int, title: String) {
        val durationMin = Math.round(durationSec / 60.0).toInt()
        val pace = if (distanceKm > 0) durationSec / distanceKm else 0.0
        val paceMm = (pace / 60).toInt()
        val paceSs = (pace % 60).toInt()
        val paceStr = String.format("%d:%02d/km", paceMm, paceSs)
        val formatted = if (paceSs > 0) "${durationSec / 60}m ${durationSec % 60}s" else "${durationSec / 60}m"

        val entry = mapOf(
            "id" to "r_${kotlin.random.Random.nextLong().toString(36).take(8)}",
            "date" to LocalDate.now().toString(),
            "distance_km" to distanceKm,
            "duration_sec" to durationSec,
            "duration_min" to durationMin,
            "duration_minutes" to durationMin,
            "minutes" to durationMin,
            "duration_formatted" to formatted,
            "title" to title,
            "pace" to paceStr,
            "ts" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )

        val file = java.io.File(VaultConfig.getVaultPath(), "08 - System/Data/events/running.jsonl")
        file.parentFile?.mkdirs()
        file.appendText(toJson(entry) + "\n")
    }

    private fun refreshWidget() {
        val mgr = AppWidgetManager.getInstance(this)
        val ids = mgr.getAppWidgetIds(android.content.ComponentName(this, RunWidget::class.java))
        for (id in ids) {
            RunWidget.updateWidget(this, mgr, id)
        }
    }
}
