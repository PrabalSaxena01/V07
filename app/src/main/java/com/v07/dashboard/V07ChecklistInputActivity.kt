package com.v07.dashboard

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class V07ChecklistInputActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist_input)

        val input = findViewById<EditText>(R.id.checklistInputEdit)
        val saveBtn = findViewById<TextView>(R.id.checklistInputSave)
        val cancelBtn = findViewById<TextView>(R.id.checklistInputCancel)
        val widgetId = intent.getIntExtra("widget_id", -1)

        input.requestFocus()

        saveBtn.setOnClickListener {
            val label = input.text.toString().trim()
            if (label.isEmpty()) {
                input.error = "Required"
                return@setOnClickListener
            }
            addChecklistItem(label)
            refreshWidget()
            finish()
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun addChecklistItem(label: String) {
        val file = File(VaultConfig.getVaultPath(), "08 - System/Data/events/checklist.jsonl")
        file.parentFile?.mkdirs()
        val id = "cl_${kotlin.random.Random.nextLong().toString(36).take(8)}"
        val item = mapOf(
            "id" to id,
            "date" to LocalDate.now().toString(),
            "label" to label,
            "done" to false,
            "recurring" to false,
            "ts" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        val jsonStr = toJson(item)
        file.appendText(jsonStr + "\n")
    }

    private fun refreshWidget() {
        val mgr = AppWidgetManager.getInstance(this)
        val ids = mgr.getAppWidgetIds(android.content.ComponentName(this, V07ChecklistWidget::class.java))
        for (id in ids) {
            V07ChecklistWidget.updateWidget(this, mgr, id)
        }
    }
}
