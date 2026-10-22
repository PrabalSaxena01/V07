package com.v07.v3.ui.compose

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.v07.v3.app.V3PlatformHost

/**
 * Dedicated Sandboxed Plugin Host Screen in Jetpack Compose.
 *
 * Runs any internal or external V3 plugin inside an isolated hardware-accelerated WebView
 * with native Calm Sanctuary chrome (Back, Title, Subtitle, Reload, Settings) and full
 * V3 RPC Native Bridge connectivity.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PluginHostScreen(
    pluginId: String,
    pluginTitle: String,
    pluginSubtitle: String,
    isLight: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var pluginIsLight by remember(isLight) { mutableStateOf(isLight) }

    fun applyTheme(light: Boolean) {
        pluginIsLight = light
        val themeName = if (light) "light" else "dark"
        val bgColor = if (light) 0xFFFAF6F9.toInt() else 0xFF0B111E.toInt()
        webViewRef?.setBackgroundColor(bgColor)
        val js = """
            (function() {
                try {
                    var t = '$themeName';
                    try { localStorage.setItem('v07_theme', t); localStorage.setItem('v07_app_theme', t); } catch(e){}
                    document.documentElement.setAttribute('data-theme', t);
                    if (t === 'light') {
                        document.documentElement.classList.add('light');
                        document.documentElement.classList.remove('dark');
                        if (document.body) {
                            document.body.classList.add('light');
                            document.body.classList.remove('dark');
                        }
                    } else {
                        document.documentElement.classList.add('dark');
                        document.documentElement.classList.remove('light');
                        if (document.body) {
                            document.body.classList.add('dark');
                            document.body.classList.remove('light');
                        }
                    }
                    window.dispatchEvent(new CustomEvent('v07-theme-changed', { detail: { theme: t } }));
                } catch(e){}
            })();
        """.trimIndent()
        webViewRef?.evaluateJavascript(js, null)
    }

    LaunchedEffect(isLight) {
        if (pluginIsLight != isLight) {
            applyTheme(isLight)
        }
    }

    // Intercept back key: allow web view to navigate back, or exit host screen
    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .border(1.dp, if (pluginIsLight) Color(0xFFDDD6DD) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                color = if (pluginIsLight) Color(0xFFFAF6F9) else MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Back to Tool Kit",
                                tint = if (pluginIsLight) Color(0xFF182230) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier.padding(start = 2.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = pluginTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = if (pluginIsLight) Color(0xFF182230) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = pluginSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (pluginIsLight) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { applyTheme(!pluginIsLight) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (pluginIsLight) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = "Toggle Daylight Warm Paper / Night Ink",
                                tint = if (pluginIsLight) Color(0xFFD97706) else Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { webViewRef?.reload() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reload",
                                tint = if (pluginIsLight) Color(0xFF475569) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Plugin Settings",
                                tint = if (pluginIsLight) Color(0xFF0D9488) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        setBackgroundColor(if (pluginIsLight) 0xFFFAF6F9.toInt() else 0xFF0B111E.toInt())
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                        overScrollMode = View.OVER_SCROLL_NEVER

                        val s = settings
                        s.javaScriptEnabled = true
                        s.domStorageEnabled = true
                        s.databaseEnabled = true
                        s.allowFileAccess = true
                        s.allowContentAccess = true
                        s.mediaPlaybackRequiresUserGesture = false
                        s.cacheMode = WebSettings.LOAD_DEFAULT
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        // Attach V3 Native Bridge RPC
                        if (V3PlatformHost.isInitialized) {
                            addJavascriptInterface(V3PlatformHost.platformJsBridge, "V3NativeBridge")
                        }

                        // Attach theme bridge
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun isDark(): Boolean = !pluginIsLight
                        }, "AndroidTheme")

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    android.util.Log.d("PluginWebView", "[${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                                }
                                return super.onConsoleMessage(consoleMessage)
                            }

                            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                AlertDialog.Builder(ctx)
                                    .setTitle(pluginTitle)
                                    .setMessage(message ?: "")
                                    .setPositiveButton("OK") { _, _ -> result?.confirm() }
                                    .setOnCancelListener { result?.confirm() }
                                    .show()
                                return true
                            }

                            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                AlertDialog.Builder(ctx)
                                    .setTitle(pluginTitle)
                                    .setMessage(message ?: "")
                                    .setPositiveButton("Confirm") { _, _ -> result?.confirm() }
                                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                                    .setOnCancelListener { result?.cancel() }
                                    .show()
                                return true
                            }

                            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                                val input = EditText(ctx).apply {
                                    setText(defaultValue ?: "")
                                    setSingleLine()
                                }
                                val container = FrameLayout(ctx).apply {
                                    val pad = (16 * resources.displayMetrics.density).toInt()
                                    setPadding(pad, pad / 2, pad, pad / 2)
                                    addView(input)
                                }
                                AlertDialog.Builder(ctx)
                                    .setTitle(if (!message.isNullOrBlank()) message else pluginTitle)
                                    .setView(container)
                                    .setPositiveButton("Save") { _, _ -> result?.confirm(input.text.toString()) }
                                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                                    .setOnCancelListener { result?.cancel() }
                                    .show()
                                return true
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                val themeName = if (pluginIsLight) "light" else "dark"
                                val earlyJs = """
                                    (function() {
                                        try {
                                            var t = '$themeName';
                                            document.documentElement.setAttribute('data-theme', t);
                                            if (t === 'light') {
                                                document.documentElement.classList.add('light');
                                                document.documentElement.classList.remove('dark');
                                            } else {
                                                document.documentElement.classList.add('dark');
                                                document.documentElement.classList.remove('light');
                                            }
                                        } catch(e){}
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(earlyJs, null)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                applyTheme(pluginIsLight)
                                val insetsJs = """
                                    (function() {
                                        try {
                                            var style = document.getElementById('v07-insets-override');
                                            if (!style) {
                                                style = document.createElement('style');
                                                style.id = 'v07-insets-override';
                                                document.head.appendChild(style);
                                            }
                                            style.innerHTML = 'html, body { box-sizing: border-box !important; overflow-x: hidden !important; max-width: 100vw !important; } body { padding-top: 12px !important; padding-bottom: 96px !important; padding-left: 14px !important; padding-right: 14px !important; } .container, .page, .main-wrap, #app, main { padding-bottom: 80px !important; }';
                                        } catch(e){}
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(insetsJs, null)
                            }
                        }

                        val baseUrl = V3PlatformHost.baseUrl
                        val token = V3PlatformHost.sessionToken
                        val theme = if (pluginIsLight) "light" else "dark"
                        val pluginUrl = "$baseUrl/plugins/$pluginId/index.html?token=$token&theme=$theme&v=${System.currentTimeMillis()}"
                        loadUrl(pluginUrl)
                        webViewRef = this
                    }
                },
                update = { wv ->
                    webViewRef = wv
                }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "$pluginTitle Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Plugin Identifier: $pluginId",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "Architecture: V3 Sandboxed Platform Module",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val baseUrl = V3PlatformHost.baseUrl
                            val token = V3PlatformHost.sessionToken
                            val theme = if (pluginIsLight) "light" else "dark"
                            val settingsUrl = "$baseUrl/plugins/$pluginId/settings.html?token=$token&theme=$theme"
                            webViewRef?.loadUrl(settingsUrl)
                            showSettingsDialog = false
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Extended Settings Page")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
