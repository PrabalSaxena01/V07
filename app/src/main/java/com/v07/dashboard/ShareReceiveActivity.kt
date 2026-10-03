package com.v07.dashboard

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ShareReceiveActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private var sharedText: String? = null
    private var sharedImageUri: Uri? = null
    private var imageSavedPath: String? = null
    private var contentType: String = "text"
    private var availableTags: List<String> = emptyList()
    private val selectedTags: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_receive)

        val type = intent?.type
        val action = intent?.action

        if (action == Intent.ACTION_SEND) {
            when {
                type?.startsWith("image/") == true -> {
                    sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    contentType = "image"
                }
                type?.startsWith("video/") == true -> {
                    sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    contentType = "video"
                }
                type?.startsWith("audio/") == true -> {
                    sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    contentType = "audio"
                }
                type == "application/pdf" -> {
                    sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    contentType = "pdf"
                }
                type == "text/plain" -> {
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    contentType = if (sharedText != null && isValidUrl(sharedText!!)) "url" else "text"
                }
            }
        }

        setupUI()
    }

    private fun setupUI() {
        val typeLabel = findViewById<TextView>(R.id.content_type_label)
        val imagePreview = findViewById<ImageView>(R.id.image_preview)
        val textPreview = findViewById<TextView>(R.id.text_preview)
        val titleInput = findViewById<EditText>(R.id.share_title)
        val infoInput = findViewById<EditText>(R.id.share_info)
        val cancelBtn = findViewById<TextView>(R.id.btn_cancel)
        val saveBtn = findViewById<TextView>(R.id.btn_save)

        when (contentType) {
            "image" -> {
                typeLabel.text = "Image"
                sharedImageUri?.let { uri ->
                    imagePreview.visibility = android.view.View.VISIBLE
                    try {
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            imagePreview.setImageBitmap(bitmap)
                        }
                    } catch (_: Exception) {}

                    val name = getFileName(uri)
                    if (name != null) titleInput.setText(name)
                }
            }
            "video", "audio", "pdf" -> {
                typeLabel.text = contentType.uppercase()
                sharedImageUri?.let { uri ->
                    textPreview.visibility = android.view.View.VISIBLE
                    textPreview.text = getFileName(uri) ?: uri.lastPathSegment ?: contentType.uppercase()
                    val name = getFileName(uri)
                    if (name != null) titleInput.setText(name)
                }
            }
            "url" -> {
                typeLabel.text = "URL"
                textPreview.visibility = android.view.View.VISIBLE
                textPreview.text = sharedText
                titleInput.setText(sharedText ?: "")
            }
            "text" -> {
                typeLabel.text = "Text"
                textPreview.visibility = android.view.View.VISIBLE
                textPreview.text = sharedText
            }
        }

        cancelBtn.setOnClickListener { finish() }
        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val info = infoInput.text.toString().trim()
            saveToVault(title, info)
        }

        fetchTags { tags ->
            availableTags = tags
            handler.post {
                val tagsContainer = findViewById<LinearLayout>(R.id.share_tags_container)
                renderTagChips(tagsContainer)
            }
        }
    }

    private fun renderTagChips(container: LinearLayout) {
        container.removeAllViews()
        for (tag in availableTags) {
            val chip = TextView(this).apply {
                text = tag
                textSize = 12f
                setTextColor(if (selectedTags.contains(tag)) Color.parseColor("#7c5bf5") else Color.WHITE)
                background = getDrawable(if (selectedTags.contains(tag)) R.drawable.overlay_tag_selected_bg else R.drawable.overlay_tag_bg)
                setPadding(dp(12), dp(6), dp(12), dp(6))
                setOnClickListener {
                    if (selectedTags.contains(tag)) selectedTags.remove(tag) else selectedTags.add(tag)
                    renderTagChips(container)
                }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(8); bottomMargin = dp(6) }
            container.addView(chip, lp)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun fetchTags(callback: (List<String>) -> Unit) {
        Thread {
            try {
                val url = URL("http://127.0.0.1:8000/tags")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val json = org.json.JSONObject(body)
                    val arr = json.getJSONArray("tags")
                    val tags = (0 until arr.length()).map { arr.getJSONObject(it).getString("name") }
                    callback(tags)
                } else {
                    conn.disconnect()
                    callback(emptyList())
                }
            } catch (_: Exception) {
                callback(emptyList())
            }
        }.start()
    }

    private fun saveToVault(title: String, info: String) {
        Thread {
            try {
                if (contentType in listOf("image", "video", "audio", "pdf") && sharedImageUri != null) {
                    saveMediaToVault(sharedImageUri!!)
                }

                val now = LocalDateTime.now()
                val capturedTime = now.format(DateTimeFormatter.ofPattern("HH:mm"))
                val capturedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val locationStr = getLocationString()

                val params = mutableListOf("title=$title", "type=$contentType")
                if (info.isNotEmpty()) params.add("info=$info")
                if (sharedText != null) params.add("content=${URLEncoder.encode(sharedText, "UTF-8")}")
                if (imageSavedPath != null) params.add("file_path=${URLEncoder.encode(imageSavedPath, "UTF-8")}")
                if (selectedTags.isNotEmpty()) params.add("tags=${URLEncoder.encode(selectedTags.joinToString(","), "UTF-8")}")
                if (capturedTime.isNotEmpty()) params.add("captured_time=${URLEncoder.encode(capturedTime, "UTF-8")}")
                if (locationStr.isNotEmpty()) params.add("location=${URLEncoder.encode(locationStr, "UTF-8")}")

                val url = URL("http://127.0.0.1:8000/log/share?${params.joinToString("&")}")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val code = conn.responseCode
                conn.disconnect()

                handler.post {
                    if (code == 200) {
                        Toast.makeText(this, "Saved to time capsule", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $code", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }

    @Suppress("MissingPermission")
    private fun getLocationString(): String {
        return try {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                String.format("%.4f, %.4f", loc.latitude, loc.longitude)
            } else ""
        } catch (_: Exception) { "" }
    }

    private fun saveMediaToVault(uri: Uri) {
        try {
            val mediaDir = File(VaultConfig.getVaultPath(), "media")
            if (!mediaDir.exists()) mediaDir.mkdirs()

            val ext = getFileName(uri)?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() } ?: when (contentType) {
                "video" -> "mp4"
                "audio" -> "mp3"
                "pdf" -> "pdf"
                else -> "jpg"
            }
            val filename = "share_${System.currentTimeMillis()}.$ext"
            val outFile = File(mediaDir, filename)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }

            imageSavedPath = outFile.absolutePath
        } catch (e: Exception) {
        }
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) return cursor.getString(idx)
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun isValidUrl(text: String): Boolean {
        return text.startsWith("http://") || text.startsWith("https://")
    }
}
