package com.v07.v3.core.plugin

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.plugin.handlers.*
import com.v07.v3.core.sync.V3CompanionSyncManager
import org.json.JSONObject

/**
 * PlatformJsBridge
 *
 * Secure native Android JavaScript interface bound to WebViews.
 * Translates window.V07Platform RPC calls to core native services while enforcing
 * manifest-declared permissions for every caller.
 *
 * Decomposed via BridgeHandler registry pattern to ensure high cohesion and modularity.
 */
class PlatformJsBridge(
    private val context: Context,
    private val repository: EventRepository,
    private val timerManager: TimerManager,
    private val screenTimeTracker: ScreenTimeTracker,
    private val registry: PluginRegistry,
    private val syncManager: V3CompanionSyncManager
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    var activity: com.v07.v3.app.V3Activity? = null

    private val handlers: List<BridgeHandler> = listOf(
        StorageBridgeHandler(repository),
        EventsBridgeHandler(repository, syncManager),
        TimerBridgeHandler(context, repository, timerManager, syncManager),
        TasksThoughtsBridgeHandler(repository, syncManager),
        CadenceLedgerBridgeHandler(repository, syncManager),
        TelemetryOverlayBridgeHandler(context, screenTimeTracker, repository, mainHandler) { activity },
        PluginAdminBridgeHandler(context, repository, registry, syncManager, mainHandler) { activity },
        AiGitaBridgeHandler(context, repository)
    )

    @JavascriptInterface
    fun invoke(pluginId: String, method: String, paramsJson: String): String {
        return try {
            val pluginRecord = registry.getPlugin(pluginId)
            val manifest = pluginRecord?.manifest

            // Permission check
            when {
                method.startsWith("storage:") -> {
                    if (manifest != null && !manifest.hasPermission("storage:isolated")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'storage:isolated' permission")
                    }
                }
                method.startsWith("telemetry:") || method.startsWith("timer:") -> {
                    if (manifest != null && !manifest.hasPermission("telemetry:read")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'telemetry:read' permission")
                    }
                }
                method.startsWith("events:") -> {
                    if (manifest != null && !manifest.hasPermission("events:listen") && !manifest.hasPermission("events:emit")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'events:listen' or 'events:emit' permission")
                    }
                }
                method.startsWith("ai:") -> {
                    if (manifest != null && !manifest.hasPermission("ai")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'ai' permission")
                    }
                }
            }

            val params = JSONObject(if (paramsJson.isNotBlank()) paramsJson else "{}")

            for (handler in handlers) {
                if (handler.canHandle(method)) {
                    val result = handler.handle(pluginId, method, params)
                    if (result != null) return result
                }
            }

            errorJson("Unknown bridge method: $method")
        } catch (e: Exception) {
            errorJson("Bridge execution failed: ${e.message}")
        }
    }

    @JavascriptInterface
    fun call(method: String, paramsJson: String): String {
        val pid = try {
            JSONObject(paramsJson).optString("pluginId", "com.v07.core").ifBlank { "com.v07.core" }
        } catch (_: Exception) { "com.v07.core" }
        return invoke(pid, method, paramsJson)
    }

    @JavascriptInterface
    fun nativeInvoke(pluginId: String, method: String, paramsJson: String): String {
        return invoke(pluginId, method, paramsJson)
    }

    @JavascriptInterface
    fun nativeInvoke(method: String, paramsJson: String): String {
        val pid = try {
            JSONObject(paramsJson).optString("pluginId", "com.v07.core").ifBlank { "com.v07.core" }
        } catch (_: Exception) { "com.v07.core" }
        return invoke(pid, method, paramsJson)
    }

    @JavascriptInterface
    fun toast(message: String) {
        mainHandler.post {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun successJson(data: JSONObject): String {
        return JSONObject().apply {
            put("ok", true)
            put("status", "success")
            put("result", data)
            put("data", data)
        }.toString()
    }

    private fun errorJson(errorMessage: String): String {
        return JSONObject().apply {
            put("ok", false)
            put("status", "error")
            put("error", errorMessage)
            put("message", errorMessage)
        }.toString()
    }

    companion object {
        /**
         * Generates the client-side JavaScript injection script defining `window.V07Platform`.
         */
        fun generateInjectionScript(pluginId: String, baseUrl: String, token: String): String {
            return """
            (function() {
                if (window.V07Platform) return;
                
                const PLUGIN_ID = '$pluginId';
                const BASE_URL = '$baseUrl';
                const TOKEN = '$token';
                
                function callNative(method, params) {
                    return new Promise((resolve, reject) => {
                        try {
                            if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                                const respStr = window.V3NativeBridge.invoke(PLUGIN_ID, method, JSON.stringify(params || {}));
                                const resp = JSON.parse(respStr);
                                if (resp.ok) resolve(resp.result);
                                else reject(new Error(resp.error));
                            } else {
                                // Fallback to HTTP API
                                fetch(BASE_URL + '/api/v3/plugins/' + PLUGIN_ID + '/data', {
                                    headers: { 'X-V07-Token': TOKEN }
                                }).then(r => r.json()).then(resolve).catch(reject);
                            }
                        } catch (e) {
                            reject(e);
                        }
                    });
                }
                
                window.V07Platform = {
                    version: '3.0.0',
                    pluginId: PLUGIN_ID,
                    baseUrl: BASE_URL,
                    invoke: callNative,
                    callNative: callNative,
                    
                    storage: {
                        get: async function(key) {
                            const res = await callNative('storage:get', { key: key });
                            if (!res.found || res.value === null) return null;
                            try { return JSON.parse(res.value); } catch(_) { return res.value; }
                        },
                        set: async function(key, value) {
                            const valStr = typeof value === 'string' ? value : JSON.stringify(value);
                            return await callNative('storage:set', { key: key, value: valStr });
                        },
                        delete: async function(key) {
                            return await callNative('storage:delete', { key: key });
                        },
                        keys: async function() {
                            const res = await callNative('storage:keys', {});
                            return res.keys || [];
                        }
                    },
                    
                    telemetry: {
                        getScreenTimeToday: async function() {
                            return await callNative('telemetry:getScreenTime', {});
                        },
                        getDetailedScreenTime: async function() {
                            return await callNative('telemetry:getDetailedScreenTime', {});
                        },
                        hasUsagePermission: async function() {
                            const res = await callNative('telemetry:hasUsagePermission', {});
                            return res.hasPermission || res.granted || false;
                        },
                        requestPermission: async function() {
                            return await callNative('telemetry:requestPermission', {});
                        },
                        getActiveFocusSession: async function() {
                            return await callNative('timer:getState', {});
                        }
                    },

                    cadence: {
                        checkin: async function(data) {
                            return await callNative('cadence:checkin', data || {});
                        },
                        checkout: async function(data) {
                            return await callNative('cadence:checkout', data || {});
                        },
                        getStatus: async function() {
                            return await callNative('cadence:getStatus', {});
                        }
                    },

                    capture: {
                        quick: async function(data) {
                            return await callNative('capture:quick', data || {});
                        }
                    },

                    entries: {
                        query: async function(filters) {
                            const res = await callNative('entries:query', filters || {});
                            return res.entries || [];
                        },
                        delete: async function(id, category) {
                            return await callNative('entries:delete', { id: id, category: category });
                        },
                        recycle: async function(id, category, dataJson) {
                            return await callNative('entries:recycle', { originalId: id, category: category, dataJson: dataJson || '{}' });
                        },
                        update: async function(id, data) {
                            return await callNative('entries:update', Object.assign({ id: id }, data || {}));
                        },
                        batchDelete: async function(ids) {
                            return await callNative('entries:batchDelete', { ids: ids || [] });
                        },
                        batchRecycle: async function(items) {
                            return await callNative('entries:batchRecycle', { items: items || [] });
                        }
                    },
                    sessions: {
                        list: async function(date) {
                            const res = await callNative('sessions:list', { date: date || '' });
                            return res.sessions || [];
                        },
                        add: async function(activity, durationSec, notes) {
                            return await callNative('sessions:add', { activity: activity, durationSec: durationSec, notes: notes || '' });
                        },
                        delete: async function(id) {
                            return await callNative('sessions:delete', { id: id });
                        }
                    },

                    gita: {
                        getDaily: async function() {
                            return await callNative('gita:getDaily', {});
                        },
                        getVerse: async function(chapter, verse) {
                            return await callNative('gita:getVerse', { chapter: chapter, verse: verse });
                        },
                        nextVerse: async function(chapter, verse) {
                            return await callNative('gita:nextVerse', { chapter: chapter, verse: verse });
                        },
                        prevVerse: async function(chapter, verse) {
                            return await callNative('gita:prevVerse', { chapter: chapter, verse: verse });
                        },
                        randomVerse: async function() {
                            return await callNative('gita:randomVerse', {});
                        }
                    },

                    companion: {
                        getStatus: async function() {
                            return await callNative('companion:getStatus', {});
                        },
                        syncNow: async function() {
                            return await callNative('companion:syncNow', {});
                        },
                        signIn: async function(email, password, createMode) {
                            return await callNative('companion:signIn', { email: email, password: password, createMode: !!createMode });
                        },
                        signOut: async function() {
                            return await callNative('companion:signOut', {});
                        },
                        migrate: async function(uid) {
                            return await callNative('companion:migrate', { uid: uid || null });
                        },
                        switchProfile: async function(profileId) {
                            return await callNative('companion:switchProfile', { profileId: profileId });
                        },
                        getProfiles: async function() {
                            return await callNative('companion:getProfiles', {});
                        },
                        getActiveProfile: async function() {
                            return await callNative('companion:getActiveProfile', {});
                        }
                    },
                    
                    ai: {
                        isAvailable: async function() {
                            try {
                                const res = await callNative('ai:status', {});
                                return !!(res && res.configured);
                            } catch(e) { return false; }
                        },
                        getStatus: async function() {
                            return await callNative('ai:status', {});
                        },
                        generateText: async function(opts) {
                            if (typeof opts === 'string') opts = { prompt: opts };
                            return await callNative('ai:generate', opts || {});
                        },
                        generateJson: async function(opts) {
                            opts = opts || {};
                            opts.responseFormat = 'json';
                            return await callNative('ai:generate', opts);
                        },
                        chat: async function(opts) {
                            if (Array.isArray(opts)) opts = { messages: opts };
                            return await callNative('ai:chat', opts || {});
                        }
                    },

                    ui: {
                        toast: function(message) {
                            callNative('ui:toast', { message: message });
                        },
                        requestResize: function(height) {
                            // Notify parent iframe host if running inside dashboard widget
                            window.parent && window.parent.postMessage({ type: 'v3:widget:resize', pluginId: PLUGIN_ID, height: height }, '*');
                        }
                    }
                };
                
                // Dispatch event so plugins know platform is ready
                window.dispatchEvent(new CustomEvent('V07PlatformReady', { detail: window.V07Platform }));
            })();
            """.trimIndent()
        }

        /**
         * Generates a backwards-compatible plugin bridge combining window.V07Platform and window.V07.
         * Ensures all starter pack plugins work synchronously and persist to SQLite plugin storage.
         */
        fun generatePluginCompatibilityScript(pluginId: String, baseUrl: String, token: String): String {
            val v3Script = generateInjectionScript(pluginId, baseUrl, token)
            return """
            $v3Script
            (function() {
                var pid = '$pluginId';
                var baseUrl = '$baseUrl';
                var token = '$token';

                function nativeInvoke(arg1, arg2, arg3) {
                    var pId = pid;
                    var method = arg1;
                    var params = arg2 || {};
                    if (typeof arg2 === 'string') {
                        pId = arg1;
                        method = arg2;
                        params = arg3 || {};
                    }
                    return new Promise(function(resolve, reject) {
                        try {
                            var paramsStr = typeof params === 'string' ? params : JSON.stringify(params);
                            if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                                var resStr = window.V3NativeBridge.invoke(pId, method, paramsStr);
                                var res = JSON.parse(resStr);
                                if (res.ok) resolve(res.result !== undefined ? res.result : (res.data !== undefined ? res.data : res));
                                else reject(new Error(res.error || res.message || 'Bridge invocation failed'));
                            } else {
                                fetch(baseUrl + '/api/v3/plugins/' + pId + '/data?method=' + encodeURIComponent(method), {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
                                    body: paramsStr
                                }).then(function(r) { return r.json(); }).then(resolve).catch(reject);
                            }
                        } catch(e) {
                            reject(e);
                        }
                    });
                }
                window.nativeInvoke = nativeInvoke;
                window.callNative = nativeInvoke;
                try {
                    if (window.parent) {
                        window.parent.nativeInvoke = nativeInvoke;
                        window.parent.callNative = nativeInvoke;
                        if (window.V3NativeBridge) window.parent.V3NativeBridge = window.V3NativeBridge;
                    }
                } catch(e){}

                if (window.V07Platform) {
                    window.V07Platform.callBridge = nativeInvoke;
                    window.V07Platform.invoke = nativeInvoke;
                }
                try {
                    if (window.parent && window.parent.V07Platform) {
                        window.parent.V07Platform.callBridge = nativeInvoke;
                        window.parent.V07Platform.invoke = nativeInvoke;
                    }
                } catch(e){}

                if (!window.AndroidV07) {
                    window.AndroidV07 = {
                        call: function(method, paramsJson) {
                            try {
                                if (window.V3NativeBridge && typeof window.V3NativeBridge.call === 'function') {
                                    return window.V3NativeBridge.call(method, paramsJson);
                                }
                                var p = typeof paramsJson === 'string' ? JSON.parse(paramsJson) : (paramsJson || {});
                                var targetPid = p.pluginId || pid;
                                if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                                    return window.V3NativeBridge.invoke(targetPid, method, typeof paramsJson === 'string' ? paramsJson : JSON.stringify(paramsJson));
                                }
                            } catch(e) {
                                return JSON.stringify({ ok: false, error: e.message });
                            }
                            return JSON.stringify({ ok: true });
                        },
                        toast: function(msg) {
                            try {
                                if (window.V3NativeBridge && window.V3NativeBridge.toast) {
                                    window.V3NativeBridge.toast(msg);
                                } else if (window.V07 && window.V07.toast) {
                                    window.V07.toast(msg);
                                }
                            } catch(e){}
                        }
                    };
                }
                try { if (window.parent) window.parent.AndroidV07 = window.AndroidV07; } catch(e){}

                if (!window.V07StorageEngine) {
                    window.V07StorageEngine = {
                        _eventsCache: [],
                        _sessionsCache: [],
                        _tasksCache: [],
                        _pluginEventsCache: {},
                        setPluginStorage: function(pId, key, val) {
                            try {
                                localStorage.setItem('v07_plugin_' + pId + '_' + key, JSON.stringify(val));
                                nativeInvoke(pId, 'storage:set', { key: key, value: JSON.stringify(val) });
                            } catch(e){}
                        },
                        getPluginStorage: function(pId, key) {
                            try {
                                var v = localStorage.getItem('v07_plugin_' + pId + '_' + key);
                                return v ? JSON.parse(v) : null;
                            } catch(e) { return null; }
                        }
                    };
                }
                try { if (window.parent) window.parent.V07StorageEngine = window.V07StorageEngine; } catch(e){}

                window.openPluginPage = function(p) {
                    window.location.href = baseUrl + '/plugins/' + p + '/index.html?token=' + token;
                };
                window.showToast = function(msg) {
                    try {
                        if (window.V3NativeBridge && window.V3NativeBridge.toast) {
                            window.V3NativeBridge.toast(msg);
                        } else if (window.V07 && window.V07.toast) {
                            window.V07.toast(msg);
                        }
                    } catch(e){}
                };
                window.toast = window.showToast;
                window.closeCustomModal = function() { try { window.history.back(); } catch(e){} };
                try {
                    if (window.parent) {
                        window.parent.openPluginPage = window.openPluginPage;
                        window.parent.showToast = window.showToast;
                        window.parent.toast = window.toast;
                        window.parent.closeCustomModal = window.closeCustomModal;
                    }
                } catch(e){}

                function detectTheme() {
                    try {
                        var params = new URLSearchParams(window.location.search);
                        if (params.has('theme')) return params.get('theme');
                    } catch(e){}
                    try {
                        if (window.AndroidTheme && typeof window.AndroidTheme.isDark === 'function') {
                            return window.AndroidTheme.isDark() ? 'dark' : 'light';
                        }
                    } catch(e){}
                    try {
                        var saved = localStorage.getItem('v07_theme') || localStorage.getItem('v07_app_theme');
                        if (saved) return saved;
                    } catch(e){}
                    return 'dark';
                }
                function applyTheme(t) {
                    var isLight = (t === 'light');
                    var themeName = isLight ? 'light' : 'dark';
                    document.documentElement.setAttribute('data-theme', themeName);
                    if (isLight) {
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
                }
                applyTheme(detectTheme());
                window.addEventListener('DOMContentLoaded', function() { applyTheme(detectTheme()); });
                window.addEventListener('storage', function() { applyTheme(detectTheme()); });
            })();
            (function() {
                if (window.V07) return;
                var pid = '$pluginId';
                var baseUrl = '$baseUrl';
                var token = '$token';
                
                function getLocal(k) {
                    try {
                        var v = localStorage.getItem('v07_' + k + '_events') || localStorage.getItem('v07_plugin_' + k);
                        return v ? JSON.parse(v) : [];
                    } catch(e) { return []; }
                }
                function setLocal(k, evs) {
                    try { 
                        localStorage.setItem('v07_' + k + '_events', JSON.stringify(evs)); 
                    } catch(e) {}
                    try {
                        fetch(baseUrl + '/api/v3/plugins/' + pid + '/data', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
                            body: JSON.stringify({ key: 'events_' + k, value: JSON.stringify(evs) })
                        }).catch(function(){});
                    } catch(e) {}
                }

                window.V07 = {
                    pluginId: pid,
                    features: { notifications: true, ai: true },
                    readEventsAll: function(cat) {
                        if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                            try {
                                var s = window.V3NativeBridge.invoke(pid, 'events:query', JSON.stringify({ category: cat }));
                                var parsed = JSON.parse(s);
                                if (parsed && parsed.ok && parsed.result && Array.isArray(parsed.result.events)) {
                                    return { ok: true, events: parsed.result.events };
                                }
                            } catch(e){}
                        }
                        return { ok: true, events: getLocal(cat) };
                    },
                    readEvents: function(cat, date) {
                        if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                            try {
                                var s = window.V3NativeBridge.invoke(pid, 'events:query', JSON.stringify({ category: cat, date: date || '' }));
                                var parsed = JSON.parse(s);
                                if (parsed && parsed.ok && parsed.result && Array.isArray(parsed.result.events)) {
                                    return { ok: true, events: parsed.result.events };
                                }
                            } catch(e){}
                        }
                        var all = getLocal(cat);
                        var filtered = date ? all.filter(function(e) { return e.date === date; }) : all;
                        return { ok: true, events: filtered };
                    },
                    writeEvent: function(cat, data) {
                        var now = new Date();
                        var ev = Object.assign({
                            id: 'ev_' + Date.now().toString(36) + '_' + Math.random().toString(36).substr(2, 4),
                            date: now.toISOString().slice(0, 10),
                            ts: now.toISOString()
                        }, data || {});
                        if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                            try {
                                window.V3NativeBridge.invoke(pid, 'events:insert', JSON.stringify({
                                    id: ev.id,
                                    category: cat,
                                    date: ev.date,
                                    timestamp: now.getTime(),
                                    payloadJson: JSON.stringify(ev)
                                }));
                            } catch(e){}
                        }
                        var all = getLocal(cat);
                        all.push(ev);
                        setLocal(cat, all);
                        return { ok: true, event: ev };
                    },
                    updateEvent: function(cat, id, fields) {
                        var all = getLocal(cat);
                        all.forEach(function(e) {
                            if (e.id === id) Object.assign(e, fields || {});
                        });
                        setLocal(cat, all);
                        return { ok: true };
                    },
                    deleteEvent: function(cat, id) {
                        var all = getLocal(cat).filter(function(e) { return e.id !== id; });
                        setLocal(cat, all);
                        return { ok: true };
                    },
                    toast: function(msg) {
                        try {
                            if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                                window.V3NativeBridge.invoke(pid, 'ui:toast', JSON.stringify({ message: msg }));
                            } else if (window.parent && window.parent.showToast) {
                                window.parent.showToast(msg, 'info');
                            } else {
                                console.log('[V07 Toast]', msg);
                            }
                        } catch(e) {}
                    },
                    getTheme: function() {
                        var t = 'dark';
                        try {
                            t = (window.parent && window.parent.document.documentElement.dataset.theme) || 
                                document.documentElement.dataset.theme || 'dark';
                        } catch(e) {}
                        return { theme: t };
                    },
                    api: function(method, path, data) {
                        return fetch(baseUrl + path, {
                            method: method || 'GET',
                            headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
                            body: data ? JSON.stringify(data) : undefined
                        }).then(function(r) { return r.json(); });
                    }
                };
                try {
                    if (window.parent) {
                        window.parent.V07 = window.V07;
                    }
                } catch(e){}
            })();
            """.trimIndent()
        }
    }
}
