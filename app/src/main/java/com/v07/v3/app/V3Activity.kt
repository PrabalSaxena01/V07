package com.v07.v3.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.v07.v3.core.plugin.PlatformJsBridge
import com.v07.v3.core.plugin.PluginManifestParser
import com.v07.v3.core.plugin.PluginRegistry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * V3Activity
 *
 * Dedicated, isolated native Android Activity for the V3 platform.
 * Hosts the full-screen V3 Web UI Shell inside an optimized hardware-accelerated WebView,
 * injects the native V3 RPC bridge (V3NativeBridge), and manages platform lifecycle.
 */
class V3Activity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var zipCallback: ((JSONObject) -> Unit)? = null
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    private val notificationPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted && Build.VERSION.SDK_INT >= 33) {
            android.widget.Toast.makeText(this, "Notifications disabled. Enable in Settings for alarms & focus alerts.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) return
            notificationPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null) {
            fileChooserCallback?.onReceiveValue(arrayOf(uri))
        } else {
            fileChooserCallback?.onReceiveValue(null)
        }
        fileChooserCallback = null
    }

    private val zipPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            Thread {
                try {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        val res = installPluginFromZip(stream)
                        runOnUiThread {
                            zipCallback?.invoke(res)
                            zipCallback = null
                        }
                    } ?: run {
                        val err = JSONObject().put("success", false).put("error", "Could not open selected file")
                        runOnUiThread {
                            zipCallback?.invoke(err)
                            zipCallback = null
                        }
                    }
                } catch (e: Exception) {
                    val err = JSONObject().put("success", false).put("error", "Failed to extract zip: ${e.message}")
                    runOnUiThread {
                        zipCallback?.invoke(err)
                        zipCallback = null
                    }
                }
            }.start()
        } else {
            val cancelled = JSONObject().put("success", false).put("error", "File selection cancelled")
            zipCallback?.invoke(cancelled)
            zipCallback = null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePrefs = getSharedPreferences("v07_theme_prefs", Context.MODE_PRIVATE)
        val defaultIsLight = !((resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
        val isLight = themePrefs.getBoolean("is_light", defaultIsLight)
        val defaultColor = if (isLight) "#FAF6F9" else "#0B111E"
        val initialColor = themePrefs.getString("theme_bg_color", defaultColor) ?: defaultColor
        val parsedColor = Color.parseColor(initialColor)
        window.statusBarColor = parsedColor
        window.navigationBarColor = parsedColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var flags = window.decorView.systemUiVisibility
            flags = if (isLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = flags
        }

        // Ensure notification channels exist & request Android 13+ POST_NOTIFICATIONS permission
        com.v07.dashboard.PluginReminderReceiver.ensureNotificationChannel(this)
        requestNotificationPermission()

        // Initialize and boot V3 platform subsystems
        V3PlatformHost.init(this)
        V3PlatformHost.platformJsBridge.activity = this
        val baseUrl = V3PlatformHost.startServices()

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(parsedColor)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(parsedColor)
            isFocusable = true
            isFocusableInTouchMode = true
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            isLongClickable = true
        }
        rootLayout.addView(webView)
        setContentView(rootLayout)

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun setSystemTheme(isLight: Boolean) {
                val colorHex = if (isLight) "#FAF6F9" else "#0B111E"
                setSystemThemeColor(colorHex, isLight)
            }

            @JavascriptInterface
            fun setSystemThemeColor(colorHex: String, isLight: Boolean) {
                themePrefs.edit()
                    .putBoolean("is_light", isLight)
                    .putString("theme_bg_color", colorHex)
                    .apply()
                runOnUiThread {
                    try {
                        val color = Color.parseColor(colorHex)
                        window.statusBarColor = color
                        window.navigationBarColor = color
                        rootLayout.setBackgroundColor(color)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            var f = window.decorView.systemUiVisibility
                            f = if (isLight) {
                                f or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            } else {
                                f and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                            }
                            window.decorView.systemUiVisibility = f
                        }
                    } catch (e: Exception) {
                        Log.e("V3Activity", "setSystemThemeColor failed: ${e.message}")
                    }
                }
            }
        }, "AndroidTheme")

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            rootLayout.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupWebView(baseUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(baseUrl: String) {
        WebView.setWebContentsDebuggingEnabled(true)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Attach native platform RPC bridge
        webView.addJavascriptInterface(V3PlatformHost.platformJsBridge, "V3NativeBridge")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    android.util.Log.d("V3WebView", "[${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                }
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                if (isFinishing || isDestroyed) {
                    result?.cancel()
                    return true
                }
                AlertDialog.Builder(this@V3Activity)
                    .setTitle("V07 Sanctuary")
                    .setMessage(message ?: "")
                    .setPositiveButton("OK") { _, _ -> result?.confirm() }
                    .setOnCancelListener { result?.confirm() }
                    .setCancelable(true)
                    .show()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                if (isFinishing || isDestroyed) {
                    result?.cancel()
                    return true
                }
                AlertDialog.Builder(this@V3Activity)
                    .setTitle("V07 Sanctuary")
                    .setMessage(message ?: "")
                    .setPositiveButton("Confirm") { _, _ -> result?.confirm() }
                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .setCancelable(true)
                    .show()
                return true
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                if (isFinishing || isDestroyed) {
                    result?.cancel()
                    return true
                }
                val input = EditText(this@V3Activity).apply {
                    setText(defaultValue ?: "")
                    setSingleLine()
                    val pad = (16 * resources.displayMetrics.density).toInt()
                    setPadding(pad, pad / 2, pad, pad / 2)
                }
                val container = FrameLayout(this@V3Activity).apply {
                    val horizPad = (20 * resources.displayMetrics.density).toInt()
                    setPadding(horizPad, (8 * resources.displayMetrics.density).toInt(), horizPad, 0)
                    addView(input)
                }
                AlertDialog.Builder(this@V3Activity)
                    .setTitle(if (!message.isNullOrBlank()) message else "V07 Sanctuary")
                    .setView(container)
                    .setPositiveButton("Save") { _, _ -> result?.confirm(input.text.toString()) }
                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .setCancelable(true)
                    .show()
                return true
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback
                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null
                    return false
                }
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val token = V3PlatformHost.sessionToken
                val targetUrl = url ?: ""
                val pluginId = if (targetUrl.contains("/plugins/")) {
                    targetUrl.substringAfter("/plugins/").substringBefore('/')
                } else {
                    "com.v07.core"
                }
                val script = PlatformJsBridge.generatePluginCompatibilityScript(pluginId, baseUrl, token)
                view?.evaluateJavascript(script, null)

                val openPlugin = intent?.getStringExtra("openPlugin")
                if (!openPlugin.isNullOrBlank()) {
                    view?.evaluateJavascript("setTimeout(function() { if (typeof openPluginFullPage === 'function') openPluginFullPage('$openPlugin'); }, 400);", null)
                }
            }
        }

        // Load V3 Shell with session token and persisted theme for zero-trust bootstrap
        val token = V3PlatformHost.sessionToken
        val themePrefs = getSharedPreferences("v07_theme_prefs", Context.MODE_PRIVATE)
        val defaultIsLight = !((resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
        val isLight = themePrefs.getBoolean("is_light", defaultIsLight)
        val currentTheme = if (isLight) "light" else "dark"
        val startUrl = "$baseUrl/index.html?token=$token&theme=$currentTheme&v=${System.currentTimeMillis()}"
        webView.loadUrl(startUrl)

        // Observe native TimerManager state to keep WebView UI perfectly in sync with notification actions
        lifecycleScope.launch {
            val repository = com.v07.v3.core.data.EventRepository.getInstance(this@V3Activity)
            val tm = com.v07.v3.app.timer.TimerManager.getInstance(this@V3Activity, repository)
            tm.timerState.collect { state ->
                dispatchTimerStateToWebView(state)
            }
        }
    }

    fun dispatchTimerStateToWebView(state: com.v07.v3.app.timer.ActiveSessionState) {
        val json = JSONObject().apply {
            put("status", state.status.name)
            put("topic", state.topic)
            put("category", state.category)
            put("mode", state.mode.name)
            put("elapsed_ms", state.getElapsedMs())
            put("remaining_ms", state.getRemainingMs())
            put("formatted", state.getFormattedTime())
        }
        runOnUiThread {
            webView.evaluateJavascript("""
                try {
                    if (typeof window.onNativeTimerStateChange === 'function') {
                        window.onNativeTimerStateChange($json);
                    }
                    window.dispatchEvent(new CustomEvent('v07:timer-state', { detail: $json }));
                } catch(e) {}
            """.trimIndent(), null)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val openPlugin = intent?.getStringExtra("openPlugin")
        if (!openPlugin.isNullOrBlank()) {
            webView.evaluateJavascript("if (typeof openPluginFullPage === 'function') openPluginFullPage('$openPlugin');", null)
        }
        val openTab = intent?.getStringExtra("v3_open_tab")
        if (openTab == "timer") {
            webView.evaluateJavascript("if (typeof navigateToFocus === 'function') navigateToFocus(); else if (typeof openFocusModal === 'function') openFocusModal();", null)
        }
    }

    fun launchZipPicker(callback: (JSONObject) -> Unit) {
        this.zipCallback = callback
        try {
            zipPickerLauncher.launch(arrayOf(
                "application/zip",
                "application/x-zip-compressed",
                "application/x-zip",
                "application/octet-stream",
                "*/*"
            ))
        } catch (e: Exception) {
            val err = JSONObject().put("success", false).put("error", "Unable to launch file picker: ${e.message}")
            callback(err)
            zipCallback = null
        }
    }

    fun installPluginFromZipBytes(bytes: ByteArray): JSONObject {
        return installPluginFromZip(java.io.ByteArrayInputStream(bytes))
    }

    fun installPluginFromZip(inputStream: InputStream): JSONObject {
        val tempDir = File(cacheDir, "plugin_unzip_${System.currentTimeMillis()}_${(1000..9999).random()}")
        try {
            tempDir.mkdirs()
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            val destCanonicalPath = tempDir.canonicalPath
            while (entry != null) {
                val newFile = File(tempDir, entry.name)
                if (!newFile.canonicalPath.startsWith(destCanonicalPath + File.separator) && newFile.canonicalPath != destCanonicalPath) {
                    tempDir.deleteRecursively()
                    return JSONObject().put("success", false).put("error", "Zip slip detected in entry: ${entry.name}")
                }
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    newFile.outputStream().use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }

            // Locate manifest.json
            var pluginContentDir = tempDir
            var manifestFile = File(tempDir, "manifest.json")
            if (!manifestFile.exists()) {
                val subDirs = tempDir.listFiles { f -> f.isDirectory } ?: emptyArray()
                for (sub in subDirs) {
                    val mf = File(sub, "manifest.json")
                    if (mf.exists()) {
                        manifestFile = mf
                        pluginContentDir = sub
                        break
                    }
                }
            }

            if (!manifestFile.exists()) {
                tempDir.deleteRecursively()
                return JSONObject().put("success", false).put("error", "No manifest.json found in .zip archive root or subfolder")
            }

            val manifestJson = manifestFile.readText(Charsets.UTF_8)
            val manifest = PluginManifestParser.parse(manifestJson)
                ?: return JSONObject().put("success", false).put("error", "Failed to parse manifest.json: missing ID or invalid JSON")

            val errors = PluginManifestParser.validate(manifest)
            if (errors.isNotEmpty()) {
                tempDir.deleteRecursively()
                return JSONObject().put("success", false).put("error", "Manifest validation failed: ${errors.joinToString(", ")}")
            }

            val pluginsDir = File(filesDir, "plugins")
            if (!pluginsDir.exists()) pluginsDir.mkdirs()
            val targetDir = File(pluginsDir, manifest.id)
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            targetDir.mkdirs()

            pluginContentDir.copyRecursively(targetDir, overwrite = true)
            tempDir.deleteRecursively()

            val reg = PluginRegistry.getInstance(this)
            reg.discoverAll()
            reg.enablePlugin(manifest.id)

            val res = JSONObject().apply {
                put("success", true)
                put("plugin", JSONObject().apply {
                    put("id", manifest.id)
                    put("name", manifest.name)
                    put("version", manifest.version)
                    put("description", manifest.description)
                    put("icon", manifest.navIcon)
                    put("color", manifest.navColor)
                    put("category", if (manifest.navLabel.isNotBlank()) manifest.navLabel else "Custom Plugin")
                    put("isBuiltIn", false)
                    put("enabled", true)
                    put("permissions", JSONArray(manifest.permissions))
                })
            }

            runOnUiThread {
                val escaped = res.toString().replace("\\", "\\\\").replace("'", "\\'")
                webView.evaluateJavascript("if (typeof window.onPluginZipInstalled === 'function') { window.onPluginZipInstalled(JSON.parse('$escaped')); }", null)
            }

            return res
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            return JSONObject().put("success", false).put("error", "Error installing plugin: ${e.message}")
        }
    }

    override fun onBackPressed() {
        webView.evaluateJavascript(
            "(function() { if (typeof window.handleBackPress === 'function') { return window.handleBackPress() ? 'handled' : 'none'; } return 'none'; })()"
        ) { result ->
            if (result != "\"handled\"") {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.evaluateJavascript(
                "if (typeof window.onNativeResume === 'function') { window.onNativeResume(); }",
                null
            )
        }
        try {
            val repository = com.v07.v3.core.data.EventRepository.getInstance(this)
            val tm = com.v07.v3.app.timer.TimerManager.getInstance(this, repository)
            dispatchTimerStateToWebView(tm.timerState.value)
        } catch (_: Exception) {}
        if (com.v07.v3.app.overlay.V3BubbleOverlayService.isOverlayEnabled(this) &&
            !com.v07.v3.app.overlay.V3BubbleOverlayService.isRunning &&
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) android.provider.Settings.canDrawOverlays(this) else true)
        ) {
            com.v07.v3.app.overlay.V3BubbleOverlayService.start(this)
        }
    }

    override fun onDestroy() {
        V3PlatformHost.platformJsBridge.activity = null
        webView.destroy()
        super.onDestroy()
    }
}
