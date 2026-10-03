package com.v07.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings

import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var setupView: View
    private lateinit var signinView: View
    private lateinit var signinEmail: android.widget.EditText
    private lateinit var signinPass: android.widget.EditText
    private lateinit var signinBtn: View
    private lateinit var signinMode: View
    private lateinit var signinSkip: View
    private lateinit var signinStatus: android.widget.TextView
    private var signinCreateMode = false
    private var signinSkipped = false


    private val notificationPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted && android.os.Build.VERSION.SDK_INT >= 33) {
            Toast.makeText(this, "Notifications disabled. Enable in Settings for alerts.", Toast.LENGTH_LONG).show()
        }
    }

    private val vaultPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val path = try {
                val docId = android.provider.DocumentsContract.getTreeDocumentId(uri)
                val idx = docId.indexOf("primary:")
                if (idx >= 0) "/storage/emulated/0/" + docId.substring(idx + 8) else null
            } catch (_: Exception) { null } ?: "/storage/emulated/0/V07Data"
            VaultConfig.setVaultPath(path)
            initVaultAndProceed(path)
        }
    }

    /** SAF picker that installs a plugin from a user-selected .zip/.v07zip bundle. */
    private val zipPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val result = if (uri == null) {
            mapOf("ok" to false, "error" to "No file selected")
        } else {
            installPluginFromZip(uri)
        }
        PluginZipImporter.complete(result)
    }

    private var fileChooserCallback: android.webkit.ValueCallback<Array<android.net.Uri>>? = null
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uris = android.webkit.WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileChooserCallback?.onReceiveValue(uris)
        fileChooserCallback = null
    }

    companion object {
        private val SERVER_BASE_URL get() = V07ServerConfig.baseUrl
        var isInForeground = false
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val initialColor = if (isNightMode) "#111318" else "#F4F7F5"
        val parsedColor = android.graphics.Color.parseColor(initialColor)
        window.navigationBarColor = parsedColor
        window.statusBarColor = parsedColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var flags = window.decorView.systemUiVisibility
            flags = if (!isNightMode) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = flags
        }

        VaultConfig.init(this)
        GeminiManager.init(this)
        FirebaseApp.initializeApp(this)

        requestNotificationPermission()
        requestStoragePermission()
        requestUsageStatsPermission()
        requestAccessibilityPermission()

        webView = findViewById(R.id.webview)
        setupView = findViewById(R.id.setupView)
        signinView = findViewById(R.id.signinView)
        signinEmail = findViewById(R.id.signinEmail)
        signinPass = findViewById(R.id.signinPass)
        signinBtn = findViewById(R.id.signinBtn)
        signinMode = findViewById(R.id.signinMode)
        signinSkip = findViewById(R.id.signinSkip)
        signinStatus = findViewById(R.id.signinStatus)

        applySystemBarInsets(findViewById(R.id.coordinator))

        setupWebView()
        setupSetupScreen()
        setupSigninScreen()

        copyBundleToInternal("index.html")
        copyBundleToInternal("companion.html")
        copyBundleToInternal("admin.html")
        seedBuiltinPlugins()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, V07ServerService::class.java))
        } else {
            startService(Intent(this, V07ServerService::class.java))
        }

if (VaultConfig.isVaultConfigured()) {
            setupView.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.postDelayed({
                webView.evaluateJavascript("(async function(){try{var regs=await navigator.serviceWorker.getRegistrations();for(var r of regs)await r.unregister();var keys=await caches.keys();for(var k of keys)await caches.delete(k)}catch(e){}})()", null)
                webView.loadUrl("$SERVER_BASE_URL/?v=${System.currentTimeMillis()}")
                handleGitaIntent(intent)
                handleTasksIntent(intent)
                handlePluginIntent(intent)
                handleWidgetTypeIntent(intent)
            }, 500)
            maybeShowSigninOrSync()
        } else {
            setupView.visibility = View.VISIBLE
            webView.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleGitaIntent(intent)
        handleTasksIntent(intent)
        handlePluginIntent(intent)
        handleWidgetTypeIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    private fun handleGitaIntent(intent: Intent?) {
        val action = intent?.getStringExtra("action") ?: return
        if (action == "open_gita") {
            webView.postDelayed({
                webView.evaluateJavascript(
                    "(function(){fetch('/gita/today').then(function(r){return r.json()}).then(function(v){if(v.done){return}v.already_read=v.already_read||false;renderGita(v);openGitaPage()})})();",
                    null
                )
            }, 2000)
        }
        intent.removeExtra("action")
    }

    private fun handleTasksIntent(intent: Intent?) {
        val action = intent?.getStringExtra("action") ?: return
        if (action == "open_tasks_overdue") {
            webView.postDelayed({
                webView.evaluateJavascript(
                    "switchTab('log');setTimeout(function(){openTasksPage('overdue')},300)",
                    null
                )
            }, 2000)
        }
        intent.removeExtra("action")
    }

    private fun handlePluginIntent(intent: Intent?) {
        val pluginId = intent?.getStringExtra("openPlugin") ?: return
        webView.postDelayed({
            webView.evaluateJavascript(
                "(function(){if(typeof openPluginPage==='function'){openPluginPage('$pluginId')}else{window.addEventListener('DOMContentLoaded',function(){openPluginPage('$pluginId')})}})();",
                null
            )
        }, 1500)
        intent.removeExtra("openPlugin")
    }

    private fun handleWidgetTypeIntent(intent: Intent?) {
        val widgetType = intent?.getStringExtra("v07_widget_type") ?: return
        webView.postDelayed({
            val js = when (widgetType.lowercase()) {
                "reader" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.reader')}else{switchTab('home')}"
                "habits" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.habits')}else{switchTab('home')}"
                "gamify" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.gamify')}else{switchTab('home')}"
                "vitals" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.vitals')}else{switchTab('home')}"
                "daytracker" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.timetracker')}else{switchTab('home')}"
                "checklist" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.tasks')}else{switchTab('home')}"
                "weekly" -> "if(typeof openPluginPage==='function'){openPluginPage('com.v07.chronosynth')}else{switchTab('home')}"
                "focus" -> "if(typeof openSessionsPage==='function'){openSessionsPage()}else{switchTab('home')}"
                "run" -> "if(typeof openRunningPage==='function'){openRunningPage()}else{switchTab('home')}"
                else -> "switchTab('home')"
            }
            webView.evaluateJavascript("(function(){ $js })();", null)
        }, 1500)
        intent.removeExtra("v07_widget_type")
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) return
            notificationPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestUsageStatsPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return
        if (isUsageStatsGranted()) return
        AlertDialog.Builder(this)
            .setTitle("App Usage Access Needed")
            .setMessage("V07 needs usage access permission to track your app usage and screen time. This powers the Screen Time section of your dashboard.\n\nAfter tapping Grant, find V07 in the list and toggle on \"Allow usage access\".")
            .setPositiveButton("Grant") { _, _ ->
                try { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) } catch (_: Exception) {}
            }
            .setNegativeButton("Not Now", null)
            .show()
    }

    private fun isUsageStatsGranted(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return true
        return try {
            val appOps = getSystemService(android.app.AppOpsManager::class.java)
            val mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) { false }
    }

    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager()) {
            AlertDialog.Builder(this)
                .setTitle("Storage Access Needed")
                .setMessage("V07 needs \"Allow access to manage all files\" to read your dashboard data from the Battery folder. Without this, your data cannot be loaded or saved.\n\nAfter tapping Grant, enable \"Allow access to manage all files\" for V07.")
                .setPositiveButton("Grant") { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    } catch (_: Exception) {}
                }
                .setNegativeButton("Not Now", null)
                .show()
        }
    }

    private fun requestAccessibilityPermission() {
        if (isAccessibilityServiceEnabled()) return
        val prefs = getSharedPreferences("v07_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("accessibility_prompt_done", false)) return
        AlertDialog.Builder(this)
            .setTitle("Foreground App Detection")
            .setMessage("Enable the V07 accessibility service for instant, real-time app switching detection in the overlay timer.\n\nAfter tapping Grant, find \"V07 Dashboard\" in the list and toggle it on.")
            .setPositiveButton("Grant") { _, _ ->
                prefs.edit().putBoolean("accessibility_prompt_done", true).apply()
                try { startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)) } catch (_: Exception) {}
            }
            .setNegativeButton("Not Now") { _, _ ->
                prefs.edit().putBoolean("accessibility_prompt_done", true).apply()
            }
            .show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val am = getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
        } catch (_: Exception) { false }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = false
        webView.settings.allowUniversalAccessFromFileURLs = false
        webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            webView.settings.isAlgorithmicDarkeningAllowed = false
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val targetUrl = request?.url?.toString() ?: return false
                if (targetUrl.startsWith("http://127.0.0.1:") || targetUrl.startsWith("http://localhost:")) {
                    return false
                }
                try {
                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                    startActivity(intent)
                } catch (_: Exception) {}
                return true
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    view?.postDelayed({ view?.loadUrl("$SERVER_BASE_URL/") }, 1000)
                }
            }
        }
        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback
                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null
                    false
                }
            }
            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                val dialog = AlertDialog.Builder(android.view.ContextThemeWrapper(this@MainActivity, android.R.style.Theme_DeviceDefault_Dialog_Alert))
                    .setTitle("V07")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> result?.confirm() }
                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.parseColor("#4F8DF5"))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                }
                dialog.show()
                return true
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                val dialog = AlertDialog.Builder(android.view.ContextThemeWrapper(this@MainActivity, android.R.style.Theme_DeviceDefault_Dialog_Alert))
                    .setTitle("V07")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> result?.confirm() }
                    .setOnCancelListener { result?.cancel() }
                    .create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.parseColor("#4F8DF5"))
                }
                dialog.show()
                return true
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: android.webkit.JsPromptResult?): Boolean {
                val input = android.widget.EditText(this@MainActivity).apply {
                    setText(defaultValue ?: "")
                    setTextColor(android.graphics.Color.parseColor("#F0F6FC"))
                    setHintTextColor(android.graphics.Color.parseColor("#94A3B8"))
                    setBackgroundColor(android.graphics.Color.parseColor("#1C222B"))
                    typeface = android.graphics.Typeface.SANS_SERIF
                    setPadding(32, 24, 32, 24)
                    setSingleLine(true)
                }
                val container = android.widget.FrameLayout(this@MainActivity).apply {
                    setPadding(48, 16, 48, 16)
                    addView(input)
                }
                val dialog = AlertDialog.Builder(android.view.ContextThemeWrapper(this@MainActivity, android.R.style.Theme_DeviceDefault_Dialog_Alert))
                    .setTitle("V07")
                    .setMessage(message)
                    .setView(container)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> result?.confirm(input.text.toString()) }
                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.parseColor("#4F8DF5"))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                }
                dialog.show()
                return true
            }
        }
        WebView.setWebContentsDebuggingEnabled(true)
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun openVaultPicker() {
                runOnUiThread { vaultPicker.launch(null) }
            }

            @android.webkit.JavascriptInterface
            fun setSystemTheme(isLight: Boolean) {
                val colorHex = if (isLight) "#FAF6F9" else "#0B111E"
                setSystemThemeColor(colorHex, isLight)
            }

            @android.webkit.JavascriptInterface
            fun setSystemThemeColor(colorHex: String, isLight: Boolean) {
                runOnUiThread {
                    try {
                        val parsedColor = android.graphics.Color.parseColor(colorHex)
                        window.navigationBarColor = parsedColor
                        window.statusBarColor = parsedColor
                        findViewById<View>(R.id.coordinator)?.setBackgroundColor(parsedColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            var flags = window.decorView.systemUiVisibility
                            flags = if (isLight) {
                                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            } else {
                                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                            }
                            window.decorView.systemUiVisibility = flags
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "setSystemThemeColor failed: ${e.message}")
                    }
                }
            }
        }, "Android")
        // Separate JS interface for the in-app plugin system.
        webView.addJavascriptInterface(V07Bridge(this), "AndroidV07")
        // Wire the SAF zip picker into the importPlugin bridge call (async, no JS blocking).
        PluginZipImporter.bind {
            runOnUiThread {
                zipPicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*"))
            }
        }
        // Push the import result into the page when the picker finishes.
        PluginZipImporter.bindPoster { json ->
            runOnUiThread {
                webView.evaluateJavascript(
                    "(function(){try{var f=window._v07PluginImportDone;if(f)f($json)}catch(e){}})()", null
                )
            }
        }
    }

    private fun installPluginFromZip(uri: Uri): Map<String, Any?> {
        val max = 2 * 1024 * 1024L
        return try {
            val size = contentResolver.query(
                uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null
            )?.use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else -1L } ?: -1L
            if (size > max) return mapOf("ok" to false, "error" to "Bundle exceeds 2 MB limit")
            val stream = contentResolver.openInputStream(uri) ?: return mapOf("ok" to false, "error" to "Cannot read file")
            val bytes = readBounded(stream, max.toInt()) ?: return mapOf("ok" to false, "error" to "Bundle exceeds 2 MB limit")
            when (val res = PluginManager.installFromZip(bytes)) {
                is PluginManager.Result.Ok -> mapOf("ok" to true)
                is PluginManager.Result.Fail -> mapOf("ok" to false, "error" to res.message)
            }
        } catch (e: Exception) {
            mapOf("ok" to false, "error" to (e.message ?: "import failed"))
        }
    }

    private fun readBounded(input: java.io.InputStream, max: Int): ByteArray? {
        return try {
            val out = java.io.ByteArrayOutputStream()
            val buf = ByteArray(8192)
            var total = 0
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                total += n
                if (total > max) { input.close(); return null }
                out.write(buf, 0, n)
            }
            out.toByteArray()
        } catch (_: Exception) { null }
    }

    private fun copyBundleToInternal(filename: String) {
        try {
            val inputStream = assets.open(filename)
            val outFile = File(filesDir, filename)
            inputStream.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Seed built-in plugins shipped inside the APK's assets/plugins directory.
     * Each subdirectory is copied to {filesDir}/v07-plugins/installed/{id}/ unless
     * it already exists, and registered as a builtin plugin in the registry.
     */
    private fun seedBuiltinPlugins() {
        try {
            for (pluginId in PluginManager.BUILTIN_PLUGIN_IDS) {
                val target = PluginManager.pluginDir(pluginId)
                copyAssetDir("plugins/$pluginId", target)
                PluginManager.registerBuiltin(pluginId)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "seedBuiltinPlugins failed: ${e.message}")
        }
    }

    private fun copyAssetDir(assetPath: String, target: File): Boolean {
        return try {
            val list = assets.list(assetPath) ?: return false
            target.mkdirs()
            for (name in list) {
                val childAsset = "$assetPath/$name"
                if (assets.list(childAsset)?.isNotEmpty() == true) {
                    copyAssetDir(childAsset, File(target, name))
                } else {
                    assets.open(childAsset).use { input ->
                        File(target, name).outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
            true
        } catch (_: Exception) { false }
    }

    private fun applySystemBarInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun setupSetupScreen() {
        val statusText = setupView.findViewById<TextView>(R.id.setupStatus)
        setupView.findViewById<TextView>(R.id.btnSelectFolder).setOnClickListener {
            vaultPicker.launch(null)
        }
        setupView.findViewById<TextView>(R.id.btnUseDefault).setOnClickListener {
            statusText.visibility = View.VISIBLE
            statusText.text = "Creating vault..."
            Thread {
                val path = "/storage/emulated/0/V07Data"
                val dir = File(path)
                dir.mkdirs()
                VaultConfig.setVaultPath(path)
                initVaultViaServer()
                VaultConfig.setVaultConfigured(true)
                runOnUiThread {
                    setupView.visibility = View.GONE
                    webView.visibility = View.VISIBLE
                    webView.postDelayed({
                        webView.evaluateJavascript("(async function(){try{var regs=await navigator.serviceWorker.getRegistrations();for(var r of regs)await r.unregister();var keys=await caches.keys();for(var k of keys)await caches.delete(k)}catch(e){}})()", null)
                        webView.loadUrl("$SERVER_BASE_URL/?v=${System.currentTimeMillis()}")
                        handleGitaIntent(intent)
                    }, 500)
                    maybeShowSigninOrSync()
                }
            }.start()
        }
    }

    private fun initVaultAndProceed(path: String) {
        setupView.findViewById<TextView>(R.id.setupStatus)?.let {
            it.visibility = View.VISIBLE
            it.text = "Creating vault..."
        }
        Thread {
            initVaultViaServer()
            VaultConfig.setVaultConfigured(true)
            runOnUiThread {
                setupView.visibility = View.GONE
                webView.visibility = View.VISIBLE
                webView.postDelayed({
                    webView.evaluateJavascript("(async function(){try{var regs=await navigator.serviceWorker.getRegistrations();for(var r of regs)await r.unregister();var keys=await caches.keys();for(var k of keys)await caches.delete(k)}catch(e){}})()", null)
                    webView.loadUrl("$SERVER_BASE_URL/?v=${System.currentTimeMillis()}")
                }, 500)
                maybeShowSigninOrSync()
            }
        }.start()
    }

    private fun initVaultViaServer() {
        try {
            val url = URL("$SERVER_BASE_URL/vault/init")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {}
    }

    private fun setupSigninScreen() {
        signinBtn.setOnClickListener { handleSignin() }
        signinMode.setOnClickListener {
            signinCreateMode = !signinCreateMode
            setSigninModeText()
        }
        signinSkip.setOnClickListener {
            signinSkipped = true
            signinView.visibility = View.GONE
        }
    }

    private fun setSigninModeText() {
        (signinBtn as TextView).text = if (signinCreateMode) "CREATE ACCOUNT" else "SIGN IN"
        (signinMode as TextView).text = if (signinCreateMode) "Have an account?  SIGN IN" else "New here?  CREATE ACCOUNT"
        signinStatus.visibility = android.view.View.INVISIBLE
    }

    private fun maybeShowSigninOrSync() {
        val cfg = VaultConfig.isVaultConfigured()
        val user = FirebaseAuth.getInstance().currentUser
        android.util.Log.w("V07Sync", "maybeShowSigninOrSync cfg=$cfg user=${user?.email ?: "null"} skipped=$signinSkipped")
        if (cfg && user == null && !signinSkipped) {
            signinView.visibility = View.VISIBLE
        } else if (user != null) {
            runSyncNow()
        }
    }

    private fun runSyncNow() {
        syncAllToFirestore()
        pullEventsFromFirestore()
    }

    private fun handleSignin() {
        val email = signinEmail.text.toString().trim()
        val pass = signinPass.text.toString()
        if (email.isEmpty() || pass.length < 6) {
            signinStatus.text = "Enter a valid email + password (6+ chars)"
            signinStatus.visibility = View.VISIBLE
            return
        }
        signinStatus.text = "Working..."
        signinStatus.visibility = View.VISIBLE
        val auth = FirebaseAuth.getInstance()
        val task = if (signinCreateMode) {
            auth.createUserWithEmailAndPassword(email, pass)
        } else {
            auth.signInWithEmailAndPassword(email, pass)
        }
        task.addOnCompleteListener { t ->
            if (t.isSuccessful) {
                signinStatus.visibility = android.view.View.INVISIBLE
                signinView.visibility = View.GONE
                runSyncNow()
            } else {
                val msg = t.exception?.message ?: "Sign-in failed"
                if (!signinCreateMode && msg.contains("user-not-found", ignoreCase = true)) {
                    signinCreateMode = true
                    setSigninModeText()
                    signinStatus.text = "Account not found.  Tap CREATE ACCOUNT above."
                } else if (signinCreateMode && msg.contains("email-already-in-use", ignoreCase = true)) {
                    signinCreateMode = false
                    setSigninModeText()
                    signinStatus.text = "Account exists.  Switch to SIGN IN."
                } else {
                    signinStatus.text = msg.replace(Regex("(.*Firebase): (.*)"), "$2")
                        .ifBlank { msg }
                }
                signinStatus.visibility = View.VISIBLE
            }
        }
    }

    private fun pullEventsFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        Thread {
            try {
                val eventsDir = VaultConfig.eventsDir
                if (!eventsDir.exists()) return@Thread
                val db = FirebaseFirestore.getInstance()
                val col = db.collection("users").document(user.uid).collection("events")
                val snap = com.google.android.gms.tasks.Tasks.await(col.get())
                val existingIds = java.util.HashSet<String>()
                eventsDir.listFiles { f -> f.name.endsWith(".jsonl") }?.forEach { f ->
                    f.readLines().forEach { line ->
                        if (line.isBlank()) return@forEach
                        try { existingIds.add(org.json.JSONObject(line).optString("id")) } catch (_: Exception) {}
                    }
                }
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US)
                for (doc in snap.documents) {
                    val data = doc.data ?: continue
                    val category = data["category"] as? String ?: continue
                    val id = (data["id"] as? String) ?: doc.id
                    if (id.isEmpty() || existingIds.contains(id)) continue
                    val e = org.json.JSONObject()
                    for ((k, v) in data) {
                        when (k) {
                            "category", "_syncedAt" -> {}
                            "ts" -> if (v is Number) e.put("ts", fmt.format(java.util.Date(v.toLong()))) else e.put("ts", v ?: "")
                            else -> when (v) {
                                is String -> e.put(k, v)
                                is Number -> e.put(k, v)
                                is Boolean -> e.put(k, v)
                                null -> e.put(k, org.json.JSONObject.NULL)
                                else -> {}
                            }
                        }
                    }
                    if (!e.has("id")) e.put("id", id)
                    if (!e.has("date")) e.put("date", java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()))
                    if (!e.has("ts")) e.put("ts", fmt.format(java.util.Date()))
                    File(eventsDir, "$category.jsonl").appendText(e.toString() + "\n")
                    existingIds.add(id)
                }
            } catch (_: Exception) {}
        }.start()
    }

    /**
     * Push local vault events to Firestore for cloud sync.
     * Called after vault init or can be triggered manually.
     */
    fun syncEventsToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        Thread {
            try {
                val eventsDir = VaultConfig.eventsDir
                if (!eventsDir.exists()) return@Thread
                val db = FirebaseFirestore.getInstance()
                val col = db.collection("users").document(user.uid).collection("events")
                val files = eventsDir.listFiles { f -> f.name.endsWith(".jsonl") } ?: return@Thread
                for (file in files) {
                    val category = file.nameWithoutExtension
                    val lines = file.readLines()
                    for (line in lines) {
                        if (line.isBlank()) continue
                        try {
                            val jsonString = line
                            val event = org.json.JSONObject(jsonString)
                            event.put("category", category)
                            val map = HashMap<String, Any>()
                            val keys = event.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                map[key] = event.get(key) as Any
                            }
                            map["_syncedAt"] = com.google.firebase.Timestamp.now()
                            val docId = event.optString("id", event.toString().hashCode().toString())
                            col.document(docId).set(map, SetOptions.merge())
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }.start()
    }

    fun syncAllToFirestore() {
        syncEventsToFirestore()
        syncExtendedToFirestore()
    }

    private fun jsonValue(v: Any?): Any? {
        if (v == null || v == org.json.JSONObject.NULL) return null
        return when (v) {
            is org.json.JSONObject -> jsonObjectMap(v)
            is org.json.JSONArray -> jsonArrayToList(v)
            else -> v
        }
    }

    private fun jsonArrayToList(arr: org.json.JSONArray): ArrayList<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until arr.length()) {
            val v = jsonValue(arr.get(i))
            if (v != null) list.add(v)
        }
        return list
    }

    private fun jsonObjectMap(event: org.json.JSONObject): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        val keys = event.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val v = jsonValue(event.get(key))
            if (v != null) map[key] = v
        }
        return map
    }

    private fun pushJsonlCollection(file: File, collection: String, db: FirebaseFirestore, uid: String) {
        if (!file.exists()) return
        val col = db.collection("users").document(uid).collection(collection)
        for (line in file.readLines()) {
            if (line.isBlank()) continue
            try {
                val obj = org.json.JSONObject(line)
                val map = jsonObjectMap(obj)
                map["_syncedAt"] = com.google.firebase.Timestamp.now()
                val docId = obj.optString("id", line.hashCode().toString())
                col.document(docId).set(map, SetOptions.merge())
            } catch (_: Exception) {}
        }
    }

    /**
     * Push non-event vault data (app usage, day tracker, Gita reads/reviews)
     * to Firestore so the companion can review almost all data.
     */
    private fun syncExtendedToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        Thread {
            try {
                val db = FirebaseFirestore.getInstance()
                val uid = user.uid
                try {
                    val usageDir = VaultConfig.appUsageDir
                    if (usageDir.exists()) {
                        val col = db.collection("users").document(uid).collection("appusage")
                        val files = usageDir.listFiles { f -> f.name.endsWith(".json") }
                        files?.takeLast(60)?.forEach { f ->
                            try {
                                val obj = org.json.JSONObject(f.readText())
                                val date = obj.optString("date", f.nameWithoutExtension)
                                val map = jsonObjectMap(obj)
                                map["_syncedAt"] = com.google.firebase.Timestamp.now()
                                col.document(date).set(map, SetOptions.merge())
                            } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}
                pushJsonlCollection(VaultConfig.dayTrackerFile, "daytracker", db, uid)
                pushJsonlCollection(VaultConfig.gitaReadFile(), "gita_read", db, uid)
                pushJsonlCollection(VaultConfig.gitaReviewsFile(), "gita_reviews", db, uid)
            } catch (_: Exception) {}
        }.start()
    }

    private var _pendingOverlayClose = false

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (_pendingOverlayClose) { _pendingOverlayClose = false; super.onBackPressed(); return }
        webView.evaluateJavascript(
            "(function(){var p=document.querySelectorAll('.page-overlay.show');if(p.length>0){p[p.length-1].classList.remove('show');return '1'}" +
            "var sb=document.querySelector('.gita-sidebar.open');if(sb){sb.classList.remove('open');document.getElementById('gitaSidebarOverlay').classList.remove('show');return '1'}" +
            "var psm=document.getElementById('pluginSettingsModal');if(psm&&psm.style.display==='flex'){closePluginSettings();return '1'}" +
            "var mo=document.getElementById('modalOverlay');if(mo&&mo.style.display==='flex'){mo.style.display='none';return '1'}" +
            "return '0'})()"
        ) { value ->
            val closed = value?.removeSurrounding("\"") == "1"
            if (!closed) {
                runOnUiThread {
                    if (webView.canGoBack()) webView.goBack()
                    else { _pendingOverlayClose = true; onBackPressed() }
                }
            }
        }
    }
}
