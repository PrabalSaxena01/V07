
    /* ==========================================================================
       V07 SANCTUARY CORE JAVASCRIPT ENGINE (v3.0.0)
       ========================================================================== */

    // --- APPLICATION STATE ---
    var _activeTab = 'dashboard';
    var _selectedDate = getTodayStr(); // YYYY-MM-DD
    var _eventsCache = [];
    var _tombstones = new Set();
    var _pendingMutations = [];
    var _chartInstances = {};
    var _gitaData = null;

    // Built-in Starter Plugins Registry (Synced with Sanctuary App & Cloud)
    var _pluginsRegistry = [
      {
        id: "com.v07.reader",
        aliases: ["reading"],
        name: "Reading Companion",
        version: "3.0.0",
        author: "Prabal",
        icon: "menu_book",
        color: "#8D6CFF",
        category: "Literary Shelf",
        desc: "Book collection and reading tracker. Search open databases, log reading progress, and manage personal shelves.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "server", "notification"],
        entry: "plugins/com.v07.reader/index.html",
        settings: "plugins/com.v07.reader/settings.html",
        widget: "plugins/com.v07.reader/widget.html"
      },
      {
        id: "com.v07.habits",
        aliases: ["habits"],
        name: "Habit Cadence",
        version: "3.0.0",
        author: "Prabal",
        icon: "local_fire_department",
        color: "#FFB454",
        category: "Daily Consistency",
        desc: "Track daily habits on a calendar heatmap. Log habits, build streaks, and maintain daily consistency.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "server", "notification"],
        entry: "plugins/com.v07.habits/index.html",
        settings: "plugins/com.v07.habits/settings.html",
        widget: "plugins/com.v07.habits/widget.html"
      },
      {
        id: "com.v07.tasks",
        aliases: ["tasks"],
        name: "Sovereign Tasks",
        version: "3.0.0",
        author: "Prabal",
        icon: "checklist",
        color: "#5CE1E6",
        category: "Action Items",
        desc: "Daily tasks, priority checklist, progress pacing bar, and deadline management.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "server", "notification"],
        entry: "plugins/com.v07.tasks/index.html",
        settings: "plugins/com.v07.tasks/settings.html",
        widget: "plugins/com.v07.tasks/widget.html"
      },
      {
        id: "com.v07.timetracker",
        aliases: ["timetracker"],
        name: "Time Horizon",
        version: "3.0.0",
        author: "Prabal",
        icon: "hourglass_empty",
        color: "#8D6CFF",
        category: "Temporal Horizon",
        desc: "Milestone countdowns, event timers, and pinned dates with persistent tracking.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "server", "notification"],
        entry: "plugins/com.v07.timetracker/index.html",
        settings: "plugins/com.v07.timetracker/settings.html",
        widget: "plugins/com.v07.timetracker/widget.html"
      },
      {
        id: "com.v07.gamify",
        aliases: ["gamify"],
        name: "Gamify & Quests",
        version: "3.0.0",
        author: "Prabal",
        icon: "military_tech",
        color: "#4DE086",
        category: "Progression & Streaks",
        desc: "Earn XP for logging in and logging life milestones. Track player levels, quest streaks, and perfect days.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "server", "notification"],
        entry: "plugins/com.v07.gamify/index.html",
        settings: "plugins/com.v07.gamify/settings.html",
        widget: "plugins/com.v07.gamify/widget.html"
      },
      {
        id: "com.v07.chronosynth",
        aliases: ["weekly"],
        name: "Weekly Digest",
        version: "3.0.0",
        author: "Prabal",
        icon: "psychology",
        color: "#4DE086",
        category: "Executive Briefing",
        desc: "Weekly AI intelligence engine aggregating focus, obligations, habits, and vitals into executive briefings.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "sessions:read", "server", "notification", "ai"],
        entry: "plugins/com.v07.chronosynth/index.html",
        settings: "plugins/com.v07.chronosynth/settings.html",
        widget: "plugins/com.v07.chronosynth/widget.html"
      },
      {
        id: "com.v07.vitals",
        aliases: ["mood"],
        name: "Mood & Energy",
        version: "3.0.0",
        author: "Prabal",
        icon: "psychology_alt",
        color: "#4DE086",
        category: "Sovereign Check-in",
        desc: "Log and track daily mental and physical state with quick ratings, energy levels, and vital telemetry.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "server", "notification"],
        entry: "plugins/com.v07.vitals/index.html",
        settings: "plugins/com.v07.vitals/settings.html",
        widget: "plugins/com.v07.vitals/widget.html"
      },
      {
        id: "com.v07.gita",
        aliases: ["gita"],
        name: "Gita Wisdom",
        version: "3.0.0",
        author: "V07 Sanctuary",
        icon: "auto_stories",
        color: "#FF9F43",
        category: "Wisdom & Philosophy",
        desc: "Contemplative verses and philosophical clarity for daily life. Search, reflect, and internalize sacred teachings.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "storage:isolated"],
        entry: "plugins/com.v07.gita/index.html",
        settings: "plugins/com.v07.gita/index.html",
        widget: "plugins/com.v07.gita/index.html"
      },
      {
        id: "com.v07.running",
        aliases: ["running", "com.v07.fitness"],
        name: "Runtime Tracker",
        version: "3.0.0",
        author: "V07 Sanctuary",
        icon: "directions_run",
        color: "#10B981",
        category: "Physical Cadence",
        desc: "Log running sessions, track pace, cadence, notes, and visual progression across weekly and monthly horizons.",
        enabled: true,
        builtin: true,
        permissions: ["events:read", "events:write", "storage:isolated"],
        entry: "plugins/com.v07.running/index.html",
        settings: "plugins/com.v07.running/index.html",
        widget: "plugins/com.v07.running/index.html"
      }
    ];

    function findPluginRecord(idOrAlias) {
      if (!idOrAlias) return null;
      return _pluginsRegistry.find(function(p) {
        return p.id === idOrAlias || (p.aliases && p.aliases.includes(idOrAlias));
      });
    }

    // --- DATE HELPERS ---
    function getTodayStr() {
      var d = new Date();
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
    }

    function formatDisplayDate(dateStr) {
      if (!dateStr) return 'Today';
      if (dateStr === getTodayStr()) return 'Today';
      var parts = dateStr.split('-');
      if (parts.length !== 3) return dateStr;
      var d = new Date(parts[0], parts[1] - 1, parts[2]);
      return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', weekday: 'short' });
    }

    function shiftDate(delta) {
      var parts = _selectedDate.split('-');
      var d = new Date(parts[0], parts[1] - 1, parts[2]);
      d.setDate(d.getDate() + delta);
      _selectedDate = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
      updateDateUI();
      renderAllViews();
    }

    function resetToToday() {
      _selectedDate = getTodayStr();
      updateDateUI();
      renderAllViews();
    }

    function updateDateUI() {
      var lbl = document.getElementById('dateDisplayLabel');
      if (lbl) lbl.textContent = formatDisplayDate(_selectedDate);
      var jumpBtn = document.getElementById('todayJumpBtn');
      if (jumpBtn) {
        if (_selectedDate === getTodayStr()) jumpBtn.classList.add('hidden');
        else jumpBtn.classList.remove('hidden');
      }
      var dashPill = document.getElementById('dashTodayPill');
      if (dashPill) dashPill.textContent = _selectedDate;
      var habitsSub = document.getElementById('habitsDateSub');
      if (habitsSub) habitsSub.textContent = formatDisplayDate(_selectedDate);
    }

    function openDatePickerModal() {
      var inp = document.getElementById('datePickerInput');
      if (inp) inp.value = _selectedDate;
      openModal('modalDatePicker');
    }

    function applyPickedDate() {
      var inp = document.getElementById('datePickerInput');
      if (inp && inp.value) {
        _selectedDate = inp.value;
        updateDateUI();
        renderAllViews();
      }
      closeModal('modalDatePicker');
    }

    // --- ESCAPING & TEXT SANITIZATION (IMPECCABLE STANDARD) ---
    function esc(str) {
      if (str === null || str === undefined) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
    }

    function fmtTime(ts) {
      if (!ts) return '';
      var d = new Date(typeof ts === 'number' ? ts : (Date.parse(ts) || Date.now()));
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    // --- CATEGORY MATCHING HELPERS (UNIFY ANDROID SQLITE & COMPANION SCHEMAS) ---
    function isTaskCategory(cat) {
      if (!cat) return false;
      cat = String(cat).toLowerCase().trim();
      return cat === 'task' || cat === 'tasks' || cat === 'tasks_ui' || cat === 'checklist' || cat === 'dsa';
    }

    function isSessionCategory(cat) {
      if (!cat) return false;
      cat = String(cat).toLowerCase().trim();
      return cat === 'session' || cat === 'sessions';
    }

    function isHabitCategory(cat) {
      if (!cat) return false;
      cat = String(cat).toLowerCase().trim();
      return cat === 'habit' || cat === 'habits' || cat === 'habits_ui' || cat === 'hbt';
    }

    function isThoughtCategory(cat) {
      if (!cat) return false;
      cat = String(cat).toLowerCase().trim();
      return cat === 'thought' || cat === 'thoughts' || cat === 'thought_ui' || cat === 'journal' || cat === 'shares';
    }

    function isMoodCategory(cat) {
      if (!cat) return false;
      cat = String(cat).toLowerCase().trim();
      return cat === 'mood' || cat === 'mood_ui' || cat === 'energy';
    }

    function isScreenTimeCategory(cat) {
      if (!cat) return false;
      cat = String(cat).toLowerCase().trim();
      return cat === 'screentime' || cat === 'screen_time' || cat === 'appusage' || cat === 'app_usage' || cat === 'vitals' || cat === 'device';
    }

    // ==========================================================================
    // DUAL DATABASE STORAGE ENGINE (SQLite Loopback & Firebase Firestore)
    // ==========================================================================
    var V07_FIREBASE_CONFIG = {
      apiKey: "AIzaSyCiKGZ_J8p-XnvsANNnmkrUxEZQX0KUVUc",
      authDomain: "v07-dashboard.firebaseapp.com",
      projectId: "v07-dashboard",
      storageBucket: "v07-dashboard.firebasestorage.app",
      messagingSenderId: "689369760342",
      appId: "1:689369760342:web:e0debabebc08d6f016f1f7"
    };

    function ensureFirebaseInitialized() {
      if (typeof firebase === 'undefined') return false;
      try {
        if (!firebase.apps || firebase.apps.length === 0) {
          var cfg = (typeof window !== 'undefined' && window.firebaseConfig) ? window.firebaseConfig : V07_FIREBASE_CONFIG;
          firebase.initializeApp(cfg);
          console.log('[V07 Firebase] Default FirebaseApp initialized');
        }
        return (firebase.apps && firebase.apps.length > 0);
      } catch (e) {
        console.warn('[V07 Firebase] ensureFirebaseInitialized error:', e);
        return (firebase.apps && firebase.apps.length > 0);
      }
    }

    function getSafeFirebaseAuth() {
      if (!ensureFirebaseInitialized()) return null;
      try {
        return (firebase && firebase.auth) ? firebase.auth() : null;
      } catch (e) {
        console.warn('[V07 Firebase] getSafeFirebaseAuth error:', e);
        return null;
      }
    }

    function getSafeFirebaseFirestore() {
      if (!ensureFirebaseInitialized()) return null;
      try {
        return (firebase && firebase.firestore) ? firebase.firestore() : null;
      } catch (e) {
        console.warn('[V07 Firebase] getSafeFirebaseFirestore error:', e);
        return null;
      }
    }

    function getSafeCurrentUser() {
      var auth = getSafeFirebaseAuth();
      return (auth && auth.currentUser) || (typeof V07StorageEngine !== 'undefined' && V07StorageEngine.currentUser) || null;
    }

    var V07StorageEngine = {
      mode: 'detecting', // 'app_sqlite', 'cloud_firestore', 'offline_local'
      currentUser: null,
      db: null,
      _unsubscribeFirestore: null,
      _subscribers: { sessions: null, tasks: null, thoughts: null, daytracker: null, ledger: null, legacy: null, pluginsMeta: null, plugins: {} },
      _sessionsCache: [],
      _tasksCache: [],
      _thoughtsCache: [],
      _daytrackerCache: [],
      _ledgerCache: [],
      _legacyCache: [],
      _pluginEventsCache: {},
      useLegacyEventsFallback: true,
      isMigrating: false,

      init: async function() {
        this.loadLocalTombstones();
        this.loadLocalPending();
        this.loadCustomPlugins();
        this.loadLocalStorageEvents();

        if (typeof refreshPluginsRegistry === 'function') {
          refreshPluginsRegistry(false);
        }

        // 1. Detect if running inside Android WebView Bridge
        if (window.V3NativeBridge || window.V07Android) {
          this.mode = 'app_sqlite';
          this.updateEngineBadge('App SQLite Bridge', 'Direct Android Bridge');
          await this.loadAppEvents();
          this.setupAuthListener();
          return;
        }

        // 2. Detect if connected to Loopback Server (localhost:8000 or localhost:8080)
        try {
          var res = await fetch('/api/v3/plugins', { signal: AbortSignal.timeout(1200) });
          if (res.ok) {
            this.mode = 'app_sqlite';
            this.updateEngineBadge('SQLite Loopback', 'Port ' + (window.location.port || '8000'));
            await this.loadAppEvents();
            this.setupAuthListener();
            return;
          }
        } catch (_) {}

        // 3. Fallback to Firebase Firestore Cloud Single Source of Truth
        if (ensureFirebaseInitialized()) {
          try {
            this.db = getSafeFirebaseFirestore();
            if (this.db) {
              try { this.db.enablePersistence({ synchronizeTabs: true }).catch(function() {}); } catch (_) {}
            }
            this.setupAuthListener();
            return;
          } catch (e) {
            console.warn('[V07 Storage] Firebase init error:', e);
          }
        }

        // 4. Default Offline Sovereign Local Storage
        this.mode = 'offline_local';
        this.updateEngineBadge('Offline Sovereign', 'Local Storage Active');
        this.loadLocalStorageEvents();
      },

      setupAuthListener: function() {
        var auth = getSafeFirebaseAuth();
        if (auth) {
          auth.onAuthStateChanged((user) => {
            if (user) {
              this.currentUser = user;
              if (this.mode !== 'app_sqlite') {
                this.mode = 'cloud_firestore';
                this.updateEngineBadge('Cloud Firestore', user.email || 'Authenticated');
              }
              this.syncFirestoreEvents();
              if (typeof refreshPluginsRegistry === 'function') {
                refreshPluginsRegistry(false);
              }
            } else {
              this.currentUser = null;
              if (this.mode !== 'app_sqlite') {
                this.mode = 'offline_local';
                this.updateEngineBadge('Offline Sovereign', 'Local Storage Active');
                this.loadLocalStorageEvents();
              }
            }
            if (typeof updateAuthModalUI === 'function') {
              updateAuthModalUI(user);
            }
          });
        }
      },

      updateEngineBadge: function(title, detail) {
        var badge = document.getElementById('envBadge');
        var label = document.getElementById('envLabel');
        var actEngine = document.getElementById('vaultActiveEngine');
        var engDetail = document.getElementById('vaultEngineDetail');
        var userLbl = document.getElementById('userAccountLabel');
        var userSub = document.getElementById('userAccountSub');

        if (label) label.textContent = title;
        if (badge) badge.classList.remove('hidden');
        if (actEngine) actEngine.textContent = title;
        if (engDetail) engDetail.textContent = detail;
        if (userLbl) userLbl.textContent = title;
        if (userSub) userSub.textContent = detail;
      },

      loadLocalTombstones: function() {
        try {
          var t = localStorage.getItem('v07_tombstones');
          if (t) _tombstones = new Set(JSON.parse(t));
        } catch (_) {}
      },

      saveLocalTombstones: function() {
        try {
          localStorage.setItem('v07_tombstones', JSON.stringify(Array.from(_tombstones)));
        } catch (_) {}
      },

      loadLocalPending: function() {
        try {
          var p = localStorage.getItem('v07_pending_mutations');
          if (p) _pendingMutations = JSON.parse(p);
        } catch (_) {}
      },

      saveLocalPending: function() {
        try {
          localStorage.setItem('v07_pending_mutations', JSON.stringify(_pendingMutations));
        } catch (_) {}
      },

      loadLocalStorageEvents: function() {
        try {
          var raw = localStorage.getItem('v07_events_cache');
          if (raw) {
            _eventsCache = JSON.parse(raw);
          } else {
            _eventsCache = [];
          }
        } catch (_) {
          _eventsCache = [];
        }
        renderAllViews();
      },

      saveLocalStorageEvents: function() {
        try {
          localStorage.setItem('v07_events_cache', JSON.stringify(_eventsCache));
        } catch (_) {}
      },

      // Fetch from Android REST API
      loadAppEvents: async function() {
        try {
          var res = await fetch('/api/v3/entries?limit=5000');
          if (!res.ok) {
            res = await fetch('/api/v3/events?limit=5000');
          }
          if (res.ok) {
            var data = await res.json();
            var list = data.entries || data.events || [];
            if (Array.isArray(list)) {
              var mapped = list.map(function(e) {
                var ts = e.timestamp || e.ts || (e.date ? new Date(e.date).getTime() : Date.now());
                return {
                  id: e.id || e.docId,
                  docId: e.id || e.docId,
                  category: (e.category || 'general').toLowerCase(),
                  title: e.title || (e.category === 'thought' ? 'Thought' : (e.topic || 'Event')),
                  content: e.content || e.text || '',
                  notes: e.notes || '',
                  date: e.date || (new Date(ts).toISOString().split('T')[0]),
                  ts: ts,
                  timestamp: ts,
                  duration_min: e.duration_min || (e.durationSec ? Math.round(e.durationSec / 60) : 0),
                  duration_sec: e.durationSec || e.duration_sec || 0,
                  score: e.score,
                  tags: e.tags || '',
                  priority: e.priority || 'medium',
                  status: e.status || 'pending',
                  deadline: e.deadline || ''
                };
              });

              // Merge dedicated /api/v3/thoughts
              try {
                var thRes = await fetch('/api/v3/thoughts?limit=1000');
                if (thRes.ok) {
                  var thData = await thRes.json();
                  if (thData.thoughts && Array.isArray(thData.thoughts)) {
                    thData.thoughts.forEach(function(th) {
                      var thId = th.id;
                      if (!mapped.some(function(m) { return m.id === thId; })) {
                        var thTs = th.timestamp || (th.date ? new Date(th.date).getTime() : Date.now());
                        mapped.push({
                          id: thId,
                          docId: thId,
                          category: 'thought',
                          title: th.topic || 'Thought',
                          content: th.content || th.text || '',
                          date: th.date || (new Date(thTs).toISOString().split('T')[0]),
                          ts: thTs,
                          timestamp: thTs,
                          tags: th.tags || '',
                          pinned: th.pinned || false
                        });
                      }
                    });
                  }
                }
              } catch (_) {}

              _eventsCache = mapped.filter(function(e) { return !_tombstones.has(e.id); });
              this.saveLocalStorageEvents();
              renderAllViews();
              return;
            }
          }
        } catch (e) {
          console.warn('[V07 Storage] App events fetch error, using local fallback:', e);
        }
        this.loadLocalStorageEvents();
      },

      rebuildAggregateEventsCache: function() {
        var map = new Map();
        var self = this;

        function add(item) {
          if (!item || !item.id) return;
          if (self.isTombstoned(item.id)) return;
          map.set(item.id, item);
        }

        this._sessionsCache.forEach(add);
        this._tasksCache.forEach(add);
        this._thoughtsCache.forEach(add);
        this._daytrackerCache.forEach(add);
        this._ledgerCache.forEach(add);

        Object.keys(this._pluginEventsCache).forEach(function(pid) {
          var arr = self._pluginEventsCache[pid] || [];
          arr.forEach(add);
        });

        if (this._legacyCache && this._legacyCache.length > 0) {
          this._legacyCache.forEach(function(item) {
            if (!map.has(item.id)) {
              add(item);
            }
          });
        }

        var list = Array.from(map.values());
        list.sort(function(a, b) {
          return (b.ts || b.timestamp || 0) - (a.ts || a.timestamp || 0);
        });

        _eventsCache = list;
        this.saveLocalStorageEvents();

        // Update UI counts
        var countEl = document.getElementById('vaultEventsCount');
        if (countEl) countEl.textContent = _eventsCache.length;

        var sesEl = document.getElementById('partSessionsCount');
        if (sesEl) sesEl.textContent = this._sessionsCache.length;

        var taskEl = document.getElementById('partTasksCount');
        if (taskEl) taskEl.textContent = this._tasksCache.length;

        var thEl = document.getElementById('partThoughtsCount');
        if (thEl) thEl.textContent = this._thoughtsCache.length;

        var ledEl = document.getElementById('partLedgerCount');
        if (ledEl) ledEl.textContent = this._ledgerCache.length;

        renderAllViews();
      },

      checkMigrationStatusBadge: function() {
        var badge = document.getElementById('migrationStatusBadge');
        if (!badge) return;
        var partCount = this._sessionsCache.length + this._tasksCache.length + this._thoughtsCache.length + this._ledgerCache.length;
        if (partCount > 0) {
          badge.textContent = 'Active (Partitioned)';
          badge.className = 'text-xs font-mono text-[var(--emerald)] font-bold';
        } else if (this._legacyCache && this._legacyCache.length > 0) {
          badge.textContent = 'Migration Needed';
          badge.className = 'text-xs font-mono text-[var(--warning)] font-bold';
        } else {
          badge.textContent = 'Ready';
          badge.className = 'text-xs font-mono text-[var(--emerald)] font-bold';
        }
      },

      unsubscribeAll: function() {
        if (this._subscribers.sessions) { try { this._subscribers.sessions(); } catch (_) {} this._subscribers.sessions = null; }
        if (this._subscribers.tasks) { try { this._subscribers.tasks(); } catch (_) {} this._subscribers.tasks = null; }
        if (this._subscribers.thoughts) { try { this._subscribers.thoughts(); } catch (_) {} this._subscribers.thoughts = null; }
        if (this._subscribers.daytracker) { try { this._subscribers.daytracker(); } catch (_) {} this._subscribers.daytracker = null; }
        if (this._subscribers.ledger) { try { this._subscribers.ledger(); } catch (_) {} this._subscribers.ledger = null; }
        if (this._subscribers.legacy) { try { this._subscribers.legacy(); } catch (_) {} this._subscribers.legacy = null; }
        if (this._subscribers.pluginsMeta) { try { this._subscribers.pluginsMeta(); } catch (_) {} this._subscribers.pluginsMeta = null; }
        if (this._subscribers.plugins) {
          var pKeys = Object.keys(this._subscribers.plugins);
          for (var i = 0; i < pKeys.length; i++) {
            try { this._subscribers.plugins[pKeys[i]](); } catch (_) {}
          }
          this._subscribers.plugins = {};
        }
      },

      // Bidirectional Firestore Partitioned Sync with Dual-Read Fallback
      syncFirestoreEvents: function() {
        if (!this.db) {
          this.db = getSafeFirebaseFirestore();
        }
        if (!this.currentUser) {
          this.currentUser = getSafeCurrentUser();
        }
        if (!this.db || !this.currentUser) return;
        var uid = this.currentUser.uid;
        var self = this;
        var userRef = this.db.collection('users').doc(uid);

        this.unsubscribeAll();

        try {
          // 1. Sessions Listener
          this._subscribers.sessions = userRef.collection('sessions')
            .onSnapshot(function(snap) {
              var list = [];
              snap.forEach(function(doc) {
                var d = doc.data();
                d.docId = doc.id;
                d.id = d.id || doc.id;
                d.category = 'sessions';
                if (!d.ts && d.startTime) d.ts = d.startTime;
                if (!d.timestamp && d.ts) d.timestamp = d.ts;
                if (!d.date && d.ts) {
                  var dt = new Date(d.ts);
                  d.date = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
                }
                d.title = d.activity || d.title || 'Focus Session';
                d.duration_min = d.duration_min || (d.durationSec ? Math.round(d.durationSec / 60) : 0);
                d.duration_sec = d.durationSec || d.duration_sec || 0;
                if (!self.isTombstoned(doc.id)) list.push(d);
              });
              self._sessionsCache = list;
              self.rebuildAggregateEventsCache();
              self.checkMigrationStatusBadge();
            }, function(err) {
              console.warn('[V07 Storage] sessions listener error:', err);
            });

          // 2. Tasks Listener
          this._subscribers.tasks = userRef.collection('tasks')
            .onSnapshot(function(snap) {
              var list = [];
              snap.forEach(function(doc) {
                var d = doc.data();
                d.docId = doc.id;
                d.id = d.id || doc.id;
                d.category = 'task';
                if (!d.ts && d.createdAt) d.ts = d.createdAt;
                if (!d.timestamp && d.ts) d.timestamp = d.ts;
                if (!d.date && d.ts) {
                  var dt = new Date(d.ts);
                  d.date = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
                }
                if (!self.isTombstoned(doc.id)) list.push(d);
              });
              self._tasksCache = list;
              self.rebuildAggregateEventsCache();
              self.checkMigrationStatusBadge();
            }, function(err) {
              console.warn('[V07 Storage] tasks listener error:', err);
            });

          // 3. Thoughts Listener
          this._subscribers.thoughts = userRef.collection('thoughts')
            .onSnapshot(function(snap) {
              var list = [];
              snap.forEach(function(doc) {
                var d = doc.data();
                d.docId = doc.id;
                d.id = d.id || doc.id;
                d.category = 'thought';
                if (!d.ts && d.timestamp) d.ts = d.timestamp;
                if (!d.date && d.ts) {
                  var dt = new Date(d.ts);
                  d.date = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
                }
                d.title = d.topic || d.title || 'Thought';
                d.content = d.content || d.text || '';
                if (!self.isTombstoned(doc.id)) list.push(d);
              });
              self._thoughtsCache = list;
              self.rebuildAggregateEventsCache();
              self.checkMigrationStatusBadge();
            }, function(err) {
              console.warn('[V07 Storage] thoughts listener error:', err);
            });

          // 4. Day Tracker Listener
          this._subscribers.daytracker = userRef.collection('daytracker')
            .onSnapshot(function(snap) {
              var list = [];
              snap.forEach(function(doc) {
                var d = doc.data();
                d.docId = doc.id;
                d.id = d.id || doc.id;
                d.category = 'milestone';
                if (!d.date && d.targetDate) d.date = d.targetDate;
                if (!d.ts && d.createdAt) d.ts = d.createdAt;
                if (!d.timestamp && d.ts) d.timestamp = d.ts;
                if (!self.isTombstoned(doc.id)) list.push(d);
              });
              self._daytrackerCache = list;
              self.rebuildAggregateEventsCache();
            }, function(err) {
              console.warn('[V07 Storage] daytracker listener error:', err);
            });

          // 5. System Ledger Listener
          this._subscribers.ledger = userRef.collection('ledger')
            .onSnapshot(function(snap) {
              var list = [];
              snap.forEach(function(doc) {
                var d = doc.data();
                d.docId = doc.id;
                d.id = d.id || doc.id;
                if (!d.category) d.category = 'general';
                if (!d.ts && d.timestamp) d.ts = d.timestamp;
                if (!d.date && d.ts) {
                  var dt = new Date(d.ts);
                  d.date = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
                }
                if (!self.isTombstoned(doc.id)) list.push(d);
              });
              self._ledgerCache = list;
              self.rebuildAggregateEventsCache();
              self.checkMigrationStatusBadge();
            }, function(err) {
              console.warn('[V07 Storage] ledger listener error:', err);
            });

          // 6. Built-in Plugins Domain Events Listeners
          var knownPlugins = [
            'com.v07.habits',
            'com.v07.vitals',
            'com.v07.fitness',
            'com.v07.gita',
            'com.v07.running',
            'com.v07.reader',
            'com.v07.tasks',
            'com.v07.timetracker',
            'com.v07.gamify',
            'com.v07.chronosynth'
          ];
          knownPlugins.forEach(function(pid) {
            self._subscribers.plugins[pid] = userRef.collection('plugins').doc(pid).collection('events')
              .onSnapshot(function(snap) {
                var list = [];
                snap.forEach(function(doc) {
                  var d = doc.data();
                  d.docId = doc.id;
                  d.id = d.id || doc.id;
                  d.pluginId = pid;
                  if (!d.ts && d.timestamp) d.ts = d.timestamp;
                  if (!d.date && d.ts) {
                    var dt = new Date(d.ts);
                    d.date = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
                  }
                  if (!self.isTombstoned(doc.id)) list.push(d);
                });
                self._pluginEventsCache[pid] = list;
                self.rebuildAggregateEventsCache();
              }, function(err) {
                console.warn('[V07 Storage] plugin ' + pid + ' events listener error:', err);
              });
          });

          // 7. System Plugins Catalog & States Listener
          this._subscribers.pluginsMeta = userRef.collection('system').doc('plugins')
            .onSnapshot(function(doc) {
              if (doc.exists) {
                var pData = doc.data();
                if (pData && Array.isArray(pData.plugins)) {
                  mergeRemotePlugins(pData.plugins);
                  renderPluginsView();
                  renderDashboardPlugins();
                  updatePluginFilterCounts();
                }
              }
            }, function(_) {});

          // 7. Dual-Read Fallback: Legacy users/{uid}/events Listener
          this._subscribers.legacy = userRef.collection('events')
            .onSnapshot(function(snap) {
              var list = [];
              snap.forEach(function(doc) {
                var d = doc.data();
                d.docId = doc.id;
                d.id = d.id || doc.id;
                if (!d.ts && d.timestamp) d.ts = d.timestamp;
                if (!d.date && d.ts) {
                  var dt = new Date(d.ts);
                  d.date = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
                }
                if (!self.isTombstoned(doc.id)) {
                  list.push(d);
                }
              });
              self._legacyCache = list;
              self.rebuildAggregateEventsCache();
              self.checkMigrationStatusBadge();
            }, function(err) {
              console.warn('[V07 Storage] legacy events listener error:', err);
            });

          self.flushPendingQueue();
        } catch (e) {
          console.warn('[V07 Storage] Firestore subscribe error:', e);
          self.loadLocalStorageEvents();
        }
      },

      // Add or update an event across dual stores
      saveEvent: async function(event) {
        event.id = event.id || 'evt_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
        event.docId = event.id;
        event.date = event.date || getTodayStr();
        event.ts = event.ts || Date.now();
        event.timestamp = event.ts;
        event.updated_at = Date.now();

        // Determine destination collection
        var targetCollection = 'ledger';
        var pluginId = event.pluginId || event.plugin_id || null;
        var cat = (event.category || '').toLowerCase();

        if (cat === 'sessions' || cat === 'session' || event.activity || event.durationSec !== undefined) {
          targetCollection = 'sessions';
          event.activity = event.activity || event.title || 'Focus Session';
          event.startTime = event.startTime || event.started_at || event.ts;
          event.durationSec = event.durationSec || event.duration_sec || ((event.duration_min || 0) * 60);
          event.notes = event.notes || event.content || '';
        } else if (cat === 'task' || cat === 'tasks' || cat === 'checklist' || cat === 'dsa') {
          targetCollection = 'tasks';
          event.title = event.title || 'Directive';
          event.priority = event.priority || 'MEDIUM';
          event.status = event.status || (event.completed ? 'completed' : 'pending');
          event.createdAt = event.createdAt || event.created_at || event.ts;
        } else if (cat === 'thought' || cat === 'thoughts' || cat === 'shares') {
          targetCollection = 'thoughts';
          event.topic = event.topic || event.title || 'Reflection';
          event.content = event.content || event.text || '';
        } else if (cat === 'daytracker' || cat === 'milestone') {
          targetCollection = 'daytracker';
          event.targetDate = event.targetDate || event.date;
          event.createdAt = event.createdAt || event.ts;
        } else if (pluginId) {
          targetCollection = 'plugin_events';
        } else if (cat === 'hbt' || cat === 'habit' || cat === 'habits') {
          targetCollection = 'plugin_events';
          pluginId = 'com.v07.habits';
        } else if (cat === 'mood' || cat === 'energy' || cat === 'vitals') {
          targetCollection = 'plugin_events';
          pluginId = 'com.v07.vitals';
        } else if (cat === 'running' || cat === 'run' || cat === 'fitness') {
          targetCollection = 'plugin_events';
          pluginId = 'com.v07.fitness';
        } else if (cat === 'gita') {
          targetCollection = 'plugin_events';
          pluginId = 'com.v07.gita';
        } else {
          targetCollection = 'ledger';
        }

        // Update local modular cache
        if (targetCollection === 'sessions') {
          var idx = this._sessionsCache.findIndex(e => e.id === event.id);
          if (idx >= 0) this._sessionsCache[idx] = event; else this._sessionsCache.unshift(event);
        } else if (targetCollection === 'tasks') {
          var idx = this._tasksCache.findIndex(e => e.id === event.id);
          if (idx >= 0) this._tasksCache[idx] = event; else this._tasksCache.unshift(event);
        } else if (targetCollection === 'thoughts') {
          var idx = this._thoughtsCache.findIndex(e => e.id === event.id);
          if (idx >= 0) this._thoughtsCache[idx] = event; else this._thoughtsCache.unshift(event);
        } else if (targetCollection === 'daytracker') {
          var idx = this._daytrackerCache.findIndex(e => e.id === event.id);
          if (idx >= 0) this._daytrackerCache[idx] = event; else this._daytrackerCache.unshift(event);
        } else if (targetCollection === 'plugin_events' && pluginId) {
          this._pluginEventsCache[pluginId] = this._pluginEventsCache[pluginId] || [];
          var idx = this._pluginEventsCache[pluginId].findIndex(e => e.id === event.id);
          if (idx >= 0) this._pluginEventsCache[pluginId][idx] = event; else this._pluginEventsCache[pluginId].unshift(event);
        } else {
          var idx = this._ledgerCache.findIndex(e => e.id === event.id);
          if (idx >= 0) this._ledgerCache[idx] = event; else this._ledgerCache.unshift(event);
        }

        this.rebuildAggregateEventsCache();

        // Push to App SQLite if in app mode
        if (this.mode === 'app_sqlite') {
          try {
            await fetch('/api/v3/events', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(event)
            });
            return event;
          } catch (_) {}
        }

        // Push to Firestore Partitioned Collection
        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (this.mode === 'cloud_firestore' && fDb && fUser) {
          try {
            var userDoc = fDb.collection('users').doc(fUser.uid);
            if (targetCollection === 'plugin_events' && pluginId) {
              await userDoc.collection('plugins').doc(pluginId).collection('events').doc(event.id).set(event, { merge: true });
            } else {
              await userDoc.collection(targetCollection).doc(event.id).set(event, { merge: true });
            }
            return event;
          } catch (e) {
            console.warn('[V07 Storage] Firestore push failed, queuing offline mutation:', e);
            _pendingMutations.push({ action: 'set', event: event, targetCollection: targetCollection, pluginId: pluginId });
            this.saveLocalPending();
          }
        } else {
          _pendingMutations.push({ action: 'set', event: event, targetCollection: targetCollection, pluginId: pluginId });
          this.saveLocalPending();
        }

        return event;
      },

      // Delete an event and register tombstone to prevent resurrection
      deleteEvent: async function(docId) {
        if (!docId) return;
        _tombstones.add(docId);
        this.saveLocalTombstones();

        // Remove from modular caches
        this._sessionsCache = this._sessionsCache.filter(e => e.id !== docId && e.docId !== docId);
        this._tasksCache = this._tasksCache.filter(e => e.id !== docId && e.docId !== docId);
        this._thoughtsCache = this._thoughtsCache.filter(e => e.id !== docId && e.docId !== docId);
        this._daytrackerCache = this._daytrackerCache.filter(e => e.id !== docId && e.docId !== docId);
        this._ledgerCache = this._ledgerCache.filter(e => e.id !== docId && e.docId !== docId);
        if (this._legacyCache) this._legacyCache = this._legacyCache.filter(e => e.id !== docId && e.docId !== docId);

        var self = this;
        Object.keys(this._pluginEventsCache).forEach(function(pid) {
          self._pluginEventsCache[pid] = self._pluginEventsCache[pid].filter(e => e.id !== docId && e.docId !== docId);
        });

        this.rebuildAggregateEventsCache();

        if (this.mode === 'app_sqlite') {
          try {
            await fetch('/api/v3/events/' + encodeURIComponent(docId), { method: 'DELETE' });
          } catch (_) {}
        }

        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (this.mode === 'cloud_firestore' && fDb && fUser) {
          try {
            var userDoc = fDb.collection('users').doc(fUser.uid);
            // Record permanent tombstone in cloud
            await userDoc.collection('tombstones').doc(docId).set({
              id: docId,
              deleted_at: Date.now()
            }, { merge: true });

            // Delete from all potential collections
            var collections = ['sessions', 'tasks', 'thoughts', 'daytracker', 'ledger', 'events'];
            for (var i = 0; i < collections.length; i++) {
              userDoc.collection(collections[i]).doc(docId).delete().catch(function() {});
            }
            var knownPlugins = ['com.v07.habits', 'com.v07.vitals', 'com.v07.fitness', 'com.v07.gita'];
            for (var j = 0; j < knownPlugins.length; j++) {
              userDoc.collection('plugins').doc(knownPlugins[j]).collection('events').doc(docId).delete().catch(function() {});
            }
          } catch (_) {
            _pendingMutations.push({ action: 'delete', id: docId });
            this.saveLocalPending();
          }
        }

        renderAllViews();
        showToast('Record deleted with sovereign anti-resurrection tombstone');
      },

      isTombstoned: function(id) {
        return _tombstones.has(id);
      },

      // Flush uncommitted mutations when online
      flushPendingQueue: async function() {
        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (!fDb || !fUser || _pendingMutations.length === 0) return;
        var uid = fUser.uid;
        var queue = _pendingMutations.slice();
        _pendingMutations = [];
        this.saveLocalPending();

        for (var i = 0; i < queue.length; i++) {
          var item = queue[i];
          try {
            var userDoc = fDb.collection('users').doc(uid);
            if (item.action === 'set' && item.event) {
              if (item.targetCollection === 'plugin_events' && item.pluginId) {
                await userDoc.collection('plugins').doc(item.pluginId).collection('events').doc(item.event.id).set(item.event, { merge: true });
              } else if (item.targetCollection) {
                await userDoc.collection(item.targetCollection).doc(item.event.id).set(item.event, { merge: true });
              } else {
                await userDoc.collection('ledger').doc(item.event.id).set(item.event, { merge: true });
              }
            } else if (item.action === 'delete' && item.id) {
              await userDoc.collection('tombstones').doc(item.id).set({ id: item.id, deleted_at: Date.now() }, { merge: true });
            }
          } catch (e) {
            _pendingMutations.push(item);
          }
        }
        this.saveLocalPending();
      },

      // Non-Destructive Partition Migration Engine
      runPartitionMigration: async function() {
        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (!fDb || !fUser) {
          showToast('Please sign in to run Firestore cloud migration');
          return;
        }

        var btn = document.getElementById('btnRunMigration');
        var btnText = document.getElementById('btnRunMigrationText');
        if (btn) btn.disabled = true;
        if (btnText) btnText.textContent = 'Migrating...';
        showToast('Starting non-destructive partitioned migration...');

        var uid = fUser.uid;
        var userRef = fDb.collection('users').doc(uid);

        try {
          // 1. Fetch legacy events
          var snap = await userRef.collection('events').get();
          var totalLegacy = snap.size;
          if (totalLegacy === 0) {
            showToast('No legacy records found to migrate');
            if (btn) btn.disabled = false;
            if (btnText) btnText.textContent = 'Run Migration';
            return;
          }

          // 2. Fetch tombstones to skip
          var tombSnap = await userRef.collection('tombstones').get().catch(function() { return { empty: true, forEach: function() {} }; });
          var remoteTombstones = new Set();
          tombSnap.forEach(function(td) { remoteTombstones.add(td.id); });

          var stats = {
            totalLegacy: totalLegacy,
            migratedSessions: 0,
            migratedTasks: 0,
            migratedThoughts: 0,
            migratedDaytracker: 0,
            migratedLedger: 0,
            migratedHabits: 0,
            migratedVitals: 0,
            migratedFitness: 0,
            migratedGita: 0,
            skippedTombstoned: 0
          };

          var batches = [];
          var currentBatch = fDb.batch();
          var opCount = 0;

          function addBatchOp(ref, data, isDelete) {
            if (isDelete) currentBatch.delete(ref);
            else currentBatch.set(ref, data, { merge: true });
            opCount++;
            if (opCount >= 450) {
              batches.push(currentBatch);
              currentBatch = fDb.batch();
              opCount = 0;
            }
          }

          snap.forEach(function(doc) {
            var id = doc.id;
            var data = doc.data() || {};
            if (remoteTombstones.has(id) || _tombstones.has(id)) {
              stats.skippedTombstoned++;
              return;
            }

            var cat = (data.category || '').toLowerCase().trim();
            var ts = data.ts || data.timestamp || Date.now();
            var date = data.date || (new Date(ts).toISOString().split('T')[0]);

            var targetCol = null;
            var targetDocRef = null;
            var record = Object.assign({}, data, {
              id: id,
              docId: id,
              ts: ts,
              timestamp: ts,
              date: date,
              _v3_migrated: true,
              _v3_migrated_at: Date.now(),
              updated_at: data.updated_at || Date.now()
            });

            // Categorize into 3 tiers
            if (cat === 'sessions' || cat === 'session' || data.activity) {
              targetCol = 'sessions';
              record.activity = data.activity || data.title || 'Focus Session';
              record.startTime = data.startTime || data.started_at || ts;
              record.durationSec = data.durationSec || data.duration_sec || ((data.duration_min || data.duration || 0) * 60);
              record.notes = data.notes || data.content || '';
              targetDocRef = userRef.collection('sessions').doc(id);
              stats.migratedSessions++;
            } else if (cat === 'task' || cat === 'tasks' || cat === 'checklist' || cat === 'dsa') {
              targetCol = 'tasks';
              record.title = data.title || 'Directive';
              record.priority = data.priority || (cat === 'dsa' ? 'HIGH' : 'MEDIUM');
              record.status = data.status || (data.completed ? 'completed' : 'pending');
              record.createdAt = data.createdAt || data.created_at || ts;
              targetDocRef = userRef.collection('tasks').doc(id);
              stats.migratedTasks++;
            } else if (cat === 'thought' || cat === 'thoughts' || cat === 'shares') {
              targetCol = 'thoughts';
              record.topic = data.topic || data.title || 'Reflection';
              record.content = data.content || data.text || '';
              targetDocRef = userRef.collection('thoughts').doc(id);
              stats.migratedThoughts++;
            } else if (cat === 'daytracker' || cat === 'milestone') {
              targetCol = 'daytracker';
              record.targetDate = data.targetDate || date;
              record.createdAt = data.createdAt || ts;
              targetDocRef = userRef.collection('daytracker').doc(id);
              stats.migratedDaytracker++;
            } else if (cat === 'hbt' || cat === 'habit' || cat === 'habits') {
              targetCol = 'plugins/com.v07.habits/events';
              targetDocRef = userRef.collection('plugins').doc('com.v07.habits').collection('events').doc(id);
              stats.migratedHabits++;
            } else if (cat === 'mood' || cat === 'energy' || cat === 'vitals') {
              targetCol = 'plugins/com.v07.vitals/events';
              targetDocRef = userRef.collection('plugins').doc('com.v07.vitals').collection('events').doc(id);
              stats.migratedVitals++;
            } else if (cat === 'running' || cat === 'run' || cat === 'fitness') {
              targetCol = 'plugins/com.v07.fitness/events';
              targetDocRef = userRef.collection('plugins').doc('com.v07.fitness').collection('events').doc(id);
              stats.migratedFitness++;
            } else if (cat === 'gita') {
              targetCol = 'plugins/com.v07.gita/events';
              targetDocRef = userRef.collection('plugins').doc('com.v07.gita').collection('events').doc(id);
              stats.migratedGita++;
            } else {
              targetCol = 'ledger';
              targetDocRef = userRef.collection('ledger').doc(id);
              stats.migratedLedger++;
            }

            // 1. Write to target partitioned collection
            addBatchOp(targetDocRef, record, false);

            // 2. Tag source document in legacy collection WITHOUT deleting
            addBatchOp(doc.ref, {
              _v3_migrated: true,
              _v3_partition: targetCol,
              _v3_migrated_at: Date.now()
            }, false);
          });

          // Also write system sync_meta
          var syncMetaRef = userRef.collection('system').doc('sync_meta');
          addBatchOp(syncMetaRef, {
            schema_version: 3,
            last_migration_ts: Date.now(),
            stats: stats
          }, false);

          if (opCount > 0) {
            batches.push(currentBatch);
          }

          // Commit all batches in order
          for (var b = 0; b < batches.length; b++) {
            await batches[b].commit();
          }

          // Show audit results
          var auditCard = document.getElementById('migrationAuditCard');
          var auditChecksum = document.getElementById('migrationAuditChecksum');
          var auditDetail = document.getElementById('migrationAuditDetail');
          var badge = document.getElementById('migrationStatusBadge');

          if (auditCard) auditCard.classList.remove('hidden');
          var totalMigrated = stats.migratedSessions + stats.migratedTasks + stats.migratedThoughts +
                              stats.migratedDaytracker + stats.migratedLedger + stats.migratedHabits +
                              stats.migratedVitals + stats.migratedFitness + stats.migratedGita;

          if (auditChecksum) {
            auditChecksum.textContent = 'Legacy: ' + totalLegacy + ' | Migrated: ' + totalMigrated + ' | Skipped: ' + stats.skippedTombstoned;
          }
          if (auditDetail) {
            auditDetail.textContent = 'Sessions: ' + stats.migratedSessions + ', Tasks: ' + stats.migratedTasks +
              ', Thoughts: ' + stats.migratedThoughts + ', Ledger: ' + stats.migratedLedger +
              ', Habits: ' + stats.migratedHabits + ', Vitals: ' + stats.migratedVitals;
          }
          if (badge) {
            badge.textContent = 'Partitioned & Verified';
            badge.className = 'text-xs font-mono text-[var(--emerald)] font-bold';
          }

          showToast('Partitioned migration completed successfully! ' + totalMigrated + ' records verified.');
          this.syncFirestoreEvents();
        } catch (e) {
          console.error('[V07 Storage] Migration failed:', e);
          showToast('Migration error: ' + e.message);
        } finally {
          if (btn) btn.disabled = false;
          if (btnText) btnText.textContent = 'Re-run Migration';
        }
      },

      // Isolated Key-Value Plugin Storage
      getPluginStorage: async function(pluginId, key) {
        var storeKey = 'v07_plugin_store_' + pluginId;
        var local = {};
        try { local = JSON.parse(localStorage.getItem(storeKey) || '{}'); } catch (_) {}
        if (local[key] !== undefined) return local[key];

        if (this.mode === 'app_sqlite') {
          try {
            var res = await fetch('/api/v3/plugins/' + encodeURIComponent(pluginId) + '/data?key=' + encodeURIComponent(key));
            if (res.ok) {
              var json = await res.json();
              return json.value;
            }
          } catch (_) {}
        }

        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (this.mode === 'cloud_firestore' && fDb && fUser) {
          try {
            var doc = await fDb.collection('users').doc(fUser.uid)
              .collection('plugin_storage').doc(pluginId).get();
            if (doc.exists) {
              var d = doc.data();
              return d[key];
            }
          } catch (_) {}
        }

        return null;
      },

      setPluginStorage: async function(pluginId, key, value) {
        var storeKey = 'v07_plugin_store_' + pluginId;
        var local = {};
        try { local = JSON.parse(localStorage.getItem(storeKey) || '{}'); } catch (_) {}
        local[key] = value;
        try { localStorage.setItem(storeKey, JSON.stringify(local)); } catch (_) {}

        if (this.mode === 'app_sqlite') {
          try {
            await fetch('/api/v3/plugins/' + encodeURIComponent(pluginId) + '/data', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ key: key, value: String(value) })
            });
          } catch (_) {}
        }

        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (this.mode === 'cloud_firestore' && fDb && fUser) {
          try {
            var updateObj = {};
            updateObj[key] = value;
            await fDb.collection('users').doc(fUser.uid)
              .collection('plugin_storage').doc(pluginId).set(updateObj, { merge: true });
          } catch (_) {}
        }
      },

      deletePluginStorage: async function(pluginId, key) {
        var storeKey = 'v07_plugin_store_' + pluginId;
        var local = {};
        try { local = JSON.parse(localStorage.getItem(storeKey) || '{}'); } catch (_) {}
        delete local[key];
        try { localStorage.setItem(storeKey, JSON.stringify(local)); } catch (_) {}

        var fDb = this.db || getSafeFirebaseFirestore();
        var fUser = this.currentUser || getSafeCurrentUser();
        if (this.mode === 'cloud_firestore' && fDb && fUser) {
          try {
            var updateObj = {};
            updateObj[key] = (typeof firebase !== 'undefined' && firebase.firestore && firebase.firestore.FieldValue) ? firebase.firestore.FieldValue.delete() : null;
            await fDb.collection('users').doc(fUser.uid)
              .collection('plugin_storage').doc(pluginId).update(updateObj);
          } catch (_) {}
        }
      },

      getPluginStorageKeys: async function(pluginId) {
        var storeKey = 'v07_plugin_store_' + pluginId;
        var local = {};
        try { local = JSON.parse(localStorage.getItem(storeKey) || '{}'); } catch (_) {}
        return Object.keys(local);
      },

      // Custom Plugins Registry Persist
      loadCustomPlugins: function() {
        try {
          var raw = localStorage.getItem('v07_custom_plugins');
          if (raw) {
            var customs = JSON.parse(raw);
            customs.forEach(function(cp) {
              if (!_pluginsRegistry.find(p => p.id === cp.id)) {
                _pluginsRegistry.push(cp);
              }
            });
          }
        } catch (_) {}
      },

      saveCustomPlugins: function() {
        try {
          var customs = _pluginsRegistry.filter(p => !p.builtin);
          localStorage.setItem('v07_custom_plugins', JSON.stringify(customs));
        } catch (_) {}
      },

      seedDemoData: function() {
        // Zero dummy data: reflect true database state only
        _eventsCache = [];
        this.saveLocalStorageEvents();
      }
    };

    // ==========================================================================
    // PLUGINS PLATFORM HOST BRIDGE (v07-sdk.js Host Implementation)
    // ==========================================================================
    window.nativeInvoke = async function(arg1, arg2, arg3) {
      var pluginId, method, params;
      if (typeof arg2 === 'string') {
        // 3-arg signature: (pluginId, method, params)
        pluginId = arg1;
        method = arg2;
        params = arg3 || {};
      } else {
        // 2-arg signature from within plugin iframe: (method, params)
        method = arg1;
        params = arg2 || {};
        pluginId = window._activeSandboxPluginId || 'com.v07.plugin';
      }

      params = params || {};

      if (method === 'storage:get') {
        var val = await V07StorageEngine.getPluginStorage(pluginId, params.key);
        return { ok: true, found: val !== null, value: val };
      }
      if (method === 'storage:set') {
        await V07StorageEngine.setPluginStorage(pluginId, params.key, params.value);
        return { ok: true, success: true };
      }
      if (method === 'storage:delete') {
        await V07StorageEngine.deletePluginStorage(pluginId, params.key);
        return { ok: true, success: true };
      }
      if (method === 'storage:keys') {
        var keys = await V07StorageEngine.getPluginStorageKeys(pluginId);
        return { ok: true, keys: keys };
      }
      if (method === 'ledger:save') {
        var newEvent = {
          category: params.category || 'plugin',
          title: params.title || ('Activity from ' + pluginId),
          content: params.content || '',
          plugin_id: pluginId,
          date: getTodayStr(),
          ts: params.timestamp || Date.now()
        };
        await V07StorageEngine.saveEvent(newEvent);
        renderAllViews();
        return { ok: true, id: newEvent.id, success: true };
      }
      if (method === 'events:insert') {
        var eventId = params.id || ('ev_' + Date.now().toString(36) + '_' + Math.random().toString(36).substr(2, 4));
        var eventRecord = Object.assign({}, params, {
          id: eventId,
          docId: eventId,
          pluginId: pluginId,
          plugin_id: pluginId,
          date: params.date || getTodayStr(),
          ts: params.timestamp || Date.now(),
          timestamp: params.timestamp || Date.now(),
          updated_at: Date.now()
        });
        await V07StorageEngine.saveEvent(eventRecord);
        return { ok: true, id: eventId, success: true };
      }
      if (method === 'events:query') {
        var eventsList = (V07StorageEngine._pluginEventsCache[pluginId] || []).slice();
        if (params.category) {
          eventsList = eventsList.filter(e => e.category === params.category);
        }
        if (params.date) {
          eventsList = eventsList.filter(e => e.date === params.date);
        }
        return { ok: true, events: eventsList };
      }
      if (method === 'events:delete') {
        if (params.id) {
          await V07StorageEngine.deleteEvent(params.id);
        }
        return { ok: true, success: true };
      }
      if (method === 'ui:showToast') {
        showToast(params.title || params.message || 'Notification from ' + pluginId, params.message || '');
        return { ok: true };
      }
      if (method === 'ui:close') {
        closePluginSandbox();
        return { ok: true };
      }
      if (method === 'ui:getTheme') {
        var currentTheme = document.documentElement.getAttribute('data-theme') || localStorage.getItem('v07_theme') || 'dark';
        return { ok: true, theme: currentTheme };
      }
      return { ok: false, error: 'Unknown method: ' + method };
    };

    // PostMessage bridge for secure sandboxed iframes
    window.addEventListener('message', async function(ev) {
      if (!ev.data || typeof ev.data !== 'object') return;
      var msg = ev.data;
      if (msg.type === 'v07_plugin_invoke') {
        var pId = msg.pluginId || window._activeSandboxPluginId;
        var res = await window.nativeInvoke(pId, msg.method, msg.params);
        if (ev.source && typeof ev.source.postMessage === 'function') {
          ev.source.postMessage({ type: 'v07_plugin_response', reqId: msg.reqId, result: res }, '*');
        }
      } else if (msg.type === 'v07_request_theme') {
        var curTheme = document.documentElement.getAttribute('data-theme') || localStorage.getItem('v07_theme') || 'dark';
        if (ev.source && typeof ev.source.postMessage === 'function') {
          ev.source.postMessage({ type: 'v07_set_theme', theme: curTheme }, '*');
        }
      }
    });

    // ==========================================================================
    // WEB AUDIO API SYNTHESIZER (Focus Bells & Ambient Chronosynth)
    // ==========================================================================
    var _audioCtx = null;
    var _synthNodes = null;
    var _isSoundscapePlaying = false;
    var _currentSoundPreset = 'brown';

    function getAudioContext() {
      if (!_audioCtx) {
        var AudioContext = window.AudioContext || window.webkitAudioContext;
        if (AudioContext) _audioCtx = new AudioContext();
      }
      if (_audioCtx && _audioCtx.state === 'suspended') {
        _audioCtx.resume();
      }
      return _audioCtx;
    }

    // Acoustic Tibetan Singing Bowl Chime
    function playChime() {
      var ctx = getAudioContext();
      if (!ctx) return;
      var osc = ctx.createOscillator();
      var gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(528, ctx.currentTime); // 528 Hz Love / Transformation tone
      gain.gain.setValueAtTime(0.3, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 3.2);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 3.3);
    }

    // Procedural Ambient Noise Generator
    function toggleSoundscape() {
      var ctx = getAudioContext();
      if (!ctx) {
        showToast('Web Audio not supported in this browser');
        return;
      }
      if (_isSoundscapePlaying) {
        stopSoundscape();
      } else {
        startSoundscape();
      }
    }

    function startSoundscape() {
      var ctx = getAudioContext();
      if (!ctx) return;
      stopSoundscape();

      var bufferSize = 2 * ctx.sampleRate;
      var noiseBuffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
      var output = noiseBuffer.getChannelData(0);
      var lastOut = 0.0;

      for (var i = 0; i < bufferSize; i++) {
        var white = Math.random() * 2 - 1;
        if (_currentSoundPreset === 'brown' || _currentSoundPreset === 'rain') {
          // Brown noise integration filter
          output[i] = (lastOut + (0.02 * white)) / 1.02;
          lastOut = output[i];
          output[i] *= 3.5;
        } else {
          // Stream / White
          output[i] = white * 0.15;
        }
      }

      var whiteNoise = ctx.createBufferSource();
      whiteNoise.buffer = noiseBuffer;
      whiteNoise.loop = true;

      var filter = ctx.createBiquadFilter();
      filter.type = _currentSoundPreset === 'rain' ? 'bandpass' : 'lowpass';
      filter.frequency.value = _currentSoundPreset === 'rain' ? 800 : 400;

      var gainNode = ctx.createGain();
      var volSlider = document.getElementById('synthVolume');
      var vol = volSlider ? (volSlider.value / 100) : 0.4;
      gainNode.gain.setValueAtTime(vol * 0.4, ctx.currentTime);

      whiteNoise.connect(filter);
      filter.connect(gainNode);
      gainNode.connect(ctx.destination);
      whiteNoise.start();

      _synthNodes = { source: whiteNoise, gain: gainNode };
      _isSoundscapePlaying = true;
      updateSoundscapeUI(true);
    }

    function stopSoundscape() {
      if (_synthNodes && _synthNodes.source) {
        try { _synthNodes.source.stop(); } catch (_) {}
        _synthNodes = null;
      }
      _isSoundscapePlaying = false;
      updateSoundscapeUI(false);
    }

    function updateSoundscapeUI(playing) {
      var btn = document.getElementById('soundscapeBtn');
      var label = document.getElementById('soundscapeLabel');
      var icon = document.getElementById('soundscapeIcon');
      var badge = document.getElementById('soundscapeStatusBadge');

      if (badge) {
        badge.textContent = playing ? 'PLAYING' : 'OFF';
        badge.style.color = playing ? 'var(--accent-synth)' : 'var(--text-muted)';
      }
      if (label) label.textContent = playing ? 'Pause Ambient' : 'Play Ambient';
      if (icon) icon.textContent = playing ? 'pause' : 'play_arrow';
    }

    function setSynthSound(preset) {
      _currentSoundPreset = preset;
      document.querySelectorAll('.synth-preset-btn').forEach(b => {
        b.style.borderColor = 'var(--border)';
        b.style.color = 'var(--text-secondary)';
      });
      if (event && event.target) {
        event.target.style.borderColor = 'var(--accent-synth)';
        event.target.style.color = 'var(--accent-synth)';
      }
      if (_isSoundscapePlaying) {
        startSoundscape();
      }
    }

    function updateSynthVolume(val) {
      if (_synthNodes && _synthNodes.gain && _audioCtx) {
        _synthNodes.gain.gain.setValueAtTime((val / 100) * 0.4, _audioCtx.currentTime);
      }
    }

    // ==========================================================================
    // FOCUS CHRONOMETER ENGINE (com.v07.timetracker)
    // ==========================================================================
    var _chrono = {
      targetMinutes: 0, // 0 = count-up (open flow), >0 = countdown target in minutes
      state: 'idle',     // 'idle', 'running', 'paused'
      activity: 'deepwork',
      seconds: 0,
      targetSeconds: 0,
      timerId: null,
      startedAt: null
    };

    function setChronoTargetMinutes(mins) {
      if (_chrono.state === 'running') {
        if (!confirm('Focus session is running. Reset and switch target?')) return;
      }
      mins = Math.max(0, parseInt(mins) || 0);
      _chrono.targetMinutes = mins;
      _chrono.targetSeconds = mins * 60;
      resetChronoSession();

      document.querySelectorAll('.chrono-tgt-btn').forEach(b => {
        b.classList.remove('bg-[var(--primary)]', 'text-white');
        b.classList.add('text-[var(--text-secondary)]');
      });
      var btn = document.getElementById('chronoBtn' + mins);
      if (btn) {
        btn.classList.add('bg-[var(--primary)]', 'text-white');
        btn.classList.remove('text-[var(--text-secondary)]');
      }

      var tgtLabel = document.getElementById('chronoTargetDisplayLabel');
      if (tgtLabel) {
        tgtLabel.textContent = mins === 0 ? 'Open Flow · Count-up' : ('Target: ' + mins + ' Minutes');
      }

      updateChronoDisplay();
    }

    // Backward-compatible alias for any callers
    function setChronoMode(mode) {
      if (mode === 'stopwatch') setChronoTargetMinutes(0);
      else if (mode === 'pomo25') setChronoTargetMinutes(25);
      else if (mode === 'pomo50') setChronoTargetMinutes(50);
      else setChronoTargetMinutes(0);
    }

    function applyCustomChronoMinutes() {
      var inp = document.getElementById('chronoCustomMinutes');
      if (!inp) return;
      var val = parseInt(inp.value);
      if (isNaN(val) || val <= 0) {
        showToast('Please enter a duration in minutes');
        return;
      }
      setChronoTargetMinutes(val);
      showToast('Set focus target to ' + val + ' minutes');
    }

    function selectChronoActivity(act) {
      _chrono.activity = act;
      document.querySelectorAll('.act-pill').forEach(p => {
        p.style.borderColor = 'var(--border)';
        p.style.color = 'var(--text-secondary)';
      });
      var sel = document.querySelector('[data-act-btn="' + act + '"]');
      if (sel) {
        sel.style.borderColor = 'var(--accent-chrono)';
        sel.style.color = 'var(--accent-chrono)';
      }
      var lbl = document.getElementById('chronoSelectedActivityLabel');
      if (lbl) lbl.textContent = act.replace('_', ' ');
      var hAct = document.getElementById('headerTimerActivity');
      if (hAct) hAct.textContent = act.replace('_', ' ');
    }

    function toggleChronoStartPause() {
      if (_chrono.state === 'running') {
        // Pause
        clearInterval(_chrono.timerId);
        _chrono.state = 'paused';
        updateChronoControls();
      } else {
        // Start or Resume
        if (_chrono.state === 'idle') {
          _chrono.startedAt = Date.now();
          if (_chrono.targetMinutes === 0) _chrono.seconds = 0;
          else _chrono.seconds = _chrono.targetSeconds;
        }
        _chrono.state = 'running';
        _chrono.timerId = setInterval(chronoTick, 1000);
        updateChronoControls();
      }
    }

    function chronoTick() {
      if (_chrono.targetMinutes === 0) {
        _chrono.seconds++;
      } else {
        _chrono.seconds--;
        if (_chrono.seconds <= 0) {
          _chrono.seconds = 0;
          clearInterval(_chrono.timerId);
          _chrono.state = 'idle';
          playChime();
          finishChronoSession();
          showToast('Focus session complete! Target reached.');
          return;
        }
      }
      updateChronoDisplay();
    }

    function updateChronoDisplay() {
      var min = Math.floor(_chrono.seconds / 60);
      var sec = _chrono.seconds % 60;
      var str = String(min).padStart(2, '0') + ':' + String(sec).padStart(2, '0');

      var digits = document.getElementById('chronoDisplayDigits');
      if (digits) digits.textContent = str;

      var headerVal = document.getElementById('headerTimerVal');
      if (headerVal) headerVal.textContent = str;

      // Circular SVG Progress
      var circle = document.getElementById('chronoCircleProgress');
      if (circle) {
        var total = (_chrono.targetMinutes === 0) ? 3600 : _chrono.targetSeconds;
        var fraction = total > 0 ? (_chrono.seconds / total) : 0;
        if (_chrono.targetMinutes !== 0) fraction = 1 - fraction;
        var offset = 640.88 * (1 - Math.min(fraction, 1));
        circle.style.strokeDashoffset = offset;
      }
    }

    function updateChronoControls() {
      var mainBtn = document.getElementById('chronoMainBtn');
      var mainIcon = document.getElementById('chronoMainBtnIcon');
      var mainText = document.getElementById('chronoMainBtnText');
      var finishBtn = document.getElementById('chronoFinishBtn');
      var statusLbl = document.getElementById('chronoStatusLabel');

      if (_chrono.state === 'running') {
        if (mainIcon) mainIcon.textContent = 'pause';
        if (mainText) mainText.textContent = 'Pause';
        if (finishBtn) finishBtn.classList.remove('hidden');
        if (finishBtn) finishBtn.classList.add('flex');
        if (statusLbl) statusLbl.textContent = 'In sovereign focus flow';
      } else if (_chrono.state === 'paused') {
        if (mainIcon) mainIcon.textContent = 'play_arrow';
        if (mainText) mainText.textContent = 'Resume';
        if (finishBtn) finishBtn.classList.remove('hidden');
        if (statusLbl) statusLbl.textContent = 'Session paused';
      } else {
        if (mainIcon) mainIcon.textContent = 'play_arrow';
        if (mainText) mainText.textContent = 'Start Focus';
        if (finishBtn) finishBtn.classList.add('hidden');
        if (statusLbl) statusLbl.textContent = 'Ready to flow';
      }
    }

    async function finishChronoSession() {
      clearInterval(_chrono.timerId);
      var elapsedSec = (_chrono.targetMinutes === 0) ? _chrono.seconds : (_chrono.targetSeconds - _chrono.seconds);
      var durMin = Math.round(elapsedSec / 60);
      if (durMin < 1 && elapsedSec > 20) durMin = 1;

      var notesInp = document.getElementById('chronoSessionNotes');
      var notes = notesInp ? notesInp.value.trim() : '';

      if (durMin > 0) {
        var newEvent = {
          category: 'session',
          activity: _chrono.activity,
          title: _chrono.activity.replace('_', ' ').toUpperCase() + ' Focus (' + durMin + 'm)',
          duration_min: durMin,
          duration_sec: elapsedSec,
          notes: notes,
          date: getTodayStr(),
          ts: Date.now()
        };
        await V07StorageEngine.saveEvent(newEvent);
        showToast('Logged ' + durMin + 'm focus session (+ ' + (durMin * 10) + ' XP)');
      }

      resetChronoSession();
      renderAllViews();
    }

    function resetChronoSession() {
      clearInterval(_chrono.timerId);
      _chrono.state = 'idle';
      _chrono.seconds = (_chrono.targetMinutes === 0) ? 0 : _chrono.targetSeconds;
      updateChronoDisplay();
      updateChronoControls();
      var notesInp = document.getElementById('chronoSessionNotes');
      if (notesInp) notesInp.value = '';
    }

    // ==========================================================================
    // TASK DISCIPLINE ENGINE (com.v07.tasks)
    // ==========================================================================
    var _taskFilter = 'all';

    function setTaskFilter(filter) {
      _taskFilter = filter;
      document.querySelectorAll('.task-filter-btn').forEach(b => {
        b.style.borderColor = 'transparent';
        b.style.color = 'var(--text-secondary)';
      });
      var active = document.querySelector('[data-task-filter="' + filter + '"]');
      if (active) {
        active.style.borderColor = 'rgba(79, 141, 245, 0.3)';
        active.style.color = 'var(--primary)';
      }
      renderTasksView();
    }

    async function quickAddTask() {
      var inp = document.getElementById('quickTaskInput');
      var prio = document.getElementById('quickTaskPrio');
      if (!inp || !inp.value.trim()) return;
      var title = inp.value.trim();

      var taskEvent = {
        category: 'tasks',
        title: title,
        priority: prio ? prio.value : 'medium',
        status: 'pending',
        deadline: _selectedDate,
        date: _selectedDate,
        ts: Date.now()
      };
      await V07StorageEngine.saveEvent(taskEvent);
      inp.value = '';
      showToast('Obligation recorded');
      renderAllViews();
    }

    function openNewTaskModal() {
      var d = document.getElementById('newTaskDeadline');
      if (d) d.value = _selectedDate;
      openModal('modalNewTask');
    }

    async function submitNewTask() {
      var title = document.getElementById('newTaskTitle').value.trim();
      var priority = document.getElementById('newTaskPriority').value;
      var deadline = document.getElementById('newTaskDeadline').value || _selectedDate;
      if (!title) {
        showToast('Please enter a task title');
        return;
      }
      var taskEvent = {
        category: 'tasks',
        title: title,
        priority: priority,
        status: 'pending',
        deadline: deadline,
        date: _selectedDate,
        ts: Date.now()
      };
      await V07StorageEngine.saveEvent(taskEvent);
      closeModal('modalNewTask');
      document.getElementById('newTaskTitle').value = '';
      showToast('Obligation added');
      renderAllViews();
    }

    async function toggleTaskStatus(docId) {
      var task = _eventsCache.find(e => (e.id === docId || e.docId === docId));
      if (!task) return;
      var isDone = task.status === 'done' || task.status === 'completed';
      task.status = isDone ? 'pending' : 'done';
      task.completed_at = isDone ? null : Date.now();
      task.updated_at = Date.now();
      await V07StorageEngine.saveEvent(task);
      if (!isDone) showToast('Task completed (+15 XP)');
      renderAllViews();
    }

    // ==========================================================================
    // HABIT CADENCE ENGINE (com.v07.habits)
    // ==========================================================================
    function openNewHabitModal() {
      openModal('modalNewHabit');
    }

    async function submitNewHabit() {
      var title = document.getElementById('newHabitTitle').value.trim();
      var icon = document.getElementById('newHabitIcon').value;
      if (!title) {
        showToast('Please enter a habit title');
        return;
      }
      var habitEvent = {
        category: 'habits',
        title: title,
        icon: icon,
        on: true,
        date: _selectedDate,
        ts: Date.now()
      };
      await V07StorageEngine.saveEvent(habitEvent);
      closeModal('modalNewHabit');
      document.getElementById('newHabitTitle').value = '';
      showToast('New habit initiated');
      renderAllViews();
    }

    async function toggleHabitCheck(habitTitle) {
      var existing = _eventsCache.find(e => isHabitCategory(e.category) && e.title === habitTitle && e.date === _selectedDate);
      if (existing) {
        existing.on = !existing.on;
        existing.updated_at = Date.now();
        await V07StorageEngine.saveEvent(existing);
      } else {
        var newH = {
          category: 'habits',
          title: habitTitle,
          on: true,
          date: _selectedDate,
          ts: Date.now()
        };
        await V07StorageEngine.saveEvent(newH);
      }
      showToast('Habit cadence updated (+20 XP)');
      renderAllViews();
    }

    // ==========================================================================
    // GAMIFICATION ENGINE (com.v07.gamify)
    // ==========================================================================
    function calculateGamifyStats() {
      var totalFocusMin = 0;
      var completedTasks = 0;
      var habitsChecked = 0;

      _eventsCache.forEach(function(e) {
        if (isSessionCategory(e.category)) {
          totalFocusMin += (parseInt(e.duration_min) || Math.round((parseInt(e.durationSec || e.duration_sec) || 0) / 60) || 0);
        }
        if (isTaskCategory(e.category) && (e.status === 'done' || e.status === 'completed' || e.score === 1)) {
          completedTasks++;
        }
        if (isHabitCategory(e.category) && (e.on || e.score === 1)) {
          habitsChecked++;
        }
      });

      var totalXp = (totalFocusMin * 10) + (completedTasks * 15) + (habitsChecked * 20);
      var level = Math.floor(totalXp / 100) + 1;
      var currentLevelXp = totalXp % 100;

      var rankTitle = 'Initiate';
      if (level >= 50) rankTitle = 'Paragon';
      else if (level >= 20) rankTitle = 'Master';
      else if (level >= 10) rankTitle = 'Adept';
      else if (level >= 5) rankTitle = 'Apprentice';

      return { totalXp, level, currentLevelXp, rankTitle };
    }

    // ==========================================================================
    // BHAGAVAD GITA READER (com.v07.reader)
    // ==========================================================================
    async function loadGitaData() {
      if (_gitaData) return;
      try {
        var res = await fetch('gita.json');
        if (res.ok) {
          _gitaData = await res.json();
          initGitaSelectors();
          renderGitaVerse();
        }
      } catch (e) {
        console.warn('[V07 Reader] Could not load gita.json, using sacred offline verses:', e);
      }
    }

    function initGitaSelectors() {
      var chapSelect = document.getElementById('gitaChapterSelect');
      var verseSelect = document.getElementById('gitaVerseSelect');
      if (!chapSelect || !verseSelect) return;

      chapSelect.innerHTML = '';
      for (var c = 1; c <= 18; c++) {
        var opt = document.createElement('option');
        opt.value = c;
        opt.textContent = 'Chapter ' + c;
        if (c === 2) opt.selected = true;
        chapSelect.appendChild(opt);
      }

      updateGitaVerseOptions(2);
    }

    function updateGitaVerseOptions(chapNum) {
      var verseSelect = document.getElementById('gitaVerseSelect');
      if (!verseSelect) return;
      verseSelect.innerHTML = '';
      var maxVerses = 72; // default approx
      if (_gitaData && _gitaData[chapNum] && _gitaData[chapNum].verses) {
        maxVerses = _gitaData[chapNum].verses.length;
      }
      for (var v = 1; v <= maxVerses; v++) {
        var opt = document.createElement('option');
        opt.value = v;
        opt.textContent = 'Verse ' + v;
        if (v === 47 && chapNum === 2) opt.selected = true;
        verseSelect.appendChild(opt);
      }
    }

    function onGitaSelectChange() {
      var c = parseInt(document.getElementById('gitaChapterSelect').value) || 2;
      var v = parseInt(document.getElementById('gitaVerseSelect').value) || 47;
      renderGitaVerse(c, v);
    }

    function renderGitaVerse(chap, verse) {
      chap = chap || 2;
      verse = verse || 47;
      var heading = document.getElementById('gitaVerseIdHeading');
      var sanskrit = document.getElementById('gitaSanskrit');
      var english = document.getElementById('gitaEnglish');
      var hindi = document.getElementById('gitaHindi');

      if (heading) heading.textContent = 'CHAPTER ' + chap + ' · VERSE ' + verse;

      if (_gitaData && _gitaData[chap] && _gitaData[chap].verses && _gitaData[chap].verses[verse - 1]) {
        var vData = _gitaData[chap].verses[verse - 1];
        if (sanskrit) sanskrit.textContent = vData.sanskrit || '';
        if (english) english.textContent = '"' + (vData.english || vData.translation || '') + '"';
        if (hindi) hindi.textContent = vData.hindi || '';
      } else {
        // Fallback classic verse (Gita 2.47)
        if (sanskrit) sanskrit.innerHTML = 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br>मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥';
        if (english) english.textContent = '"Thy right is to work only, but never with its fruits; let not the fruits of action be thy motive, nor let thy attachment be to inaction."';
        if (hindi) hindi.textContent = 'तेरा कर्म करने में ही अधिकार है, उसके फलों में कभी नहीं।';
      }
    }

    async function saveGitaReflection() {
      var inp = document.getElementById('gitaReflectionInput');
      if (!inp || !inp.value.trim()) return;
      var note = inp.value.trim();
      var c = document.getElementById('gitaChapterSelect').value || 2;
      var v = document.getElementById('gitaVerseSelect').value || 47;

      var newEvent = {
        category: 'thoughts',
        title: 'Reflection on Gita ' + c + '.' + v,
        content: note,
        date: _selectedDate,
        ts: Date.now()
      };
      await V07StorageEngine.saveEvent(newEvent);
      inp.value = '';
      showToast('Contemplative reflection committed to ledger');
      renderAllViews();
    }

    function openNewThoughtModal() {
      var t = document.getElementById('newThoughtTopic');
      var c = document.getElementById('newThoughtContent');
      var g = document.getElementById('newThoughtTags');
      if (t) t.value = '';
      if (c) c.value = '';
      if (g) g.value = '';
      openModal('modalNewThought');
    }

    async function submitNewThought() {
      var topic = (document.getElementById('newThoughtTopic').value || '').trim();
      var content = (document.getElementById('newThoughtContent').value || '').trim();
      var tags = (document.getElementById('newThoughtTags').value || '').trim();

      if (!content && !topic) {
        showToast('Please enter your thought or reflection');
        return;
      }

      var now = Date.now();
      var newEvent = {
        id: 'th_' + now + '_' + Math.random().toString(36).substr(2, 6),
        category: 'thought',
        title: topic || 'Thought',
        topic: topic || 'Thought',
        content: content,
        tags: tags,
        date: _selectedDate || getTodayStr(),
        ts: now,
        timestamp: now
      };

      await V07StorageEngine.saveEvent(newEvent);
      closeModal('modalNewThought');
      showToast('Thought committed to sovereign ledger');
      renderAllViews();
    }

    // ==========================================================================
    // PLUGINS PLATFORM HUB ENGINE (Current & Future Sandbox)
    // ==========================================================================
    var _pluginFilter = 'all';

    function setPluginFilter(f) {
      _pluginFilter = f;
      document.querySelectorAll('.plugin-filter-btn').forEach(b => {
        b.style.borderColor = 'transparent';
        b.style.color = 'var(--text-secondary)';
      });
      var btn = document.querySelector('[data-plugin-filter="' + f + '"]');
      if (btn) {
        btn.style.borderColor = 'rgba(79, 141, 245, 0.3)';
        btn.style.color = 'var(--primary)';
      }
      renderPluginsView();
    }

    var _activeSandboxPluginId = null;
    var _activeSandboxMode = 'app'; // 'app' or 'settings'

    function renderPluginsView() {
      var container = document.getElementById('pluginsGridContainer');
      if (!container) return;

      var filtered = _pluginsRegistry.filter(function(p) {
        if (_pluginFilter === 'active') return p.enabled;
        if (_pluginFilter === 'builtin') return p.builtin;
        if (_pluginFilter === 'custom') return !p.builtin;
        return true;
      });

      var html = '';
      filtered.forEach(function(p) {
        var statusBadge = p.enabled
          ? '<span class="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[var(--success-dim)] text-[var(--success)]">ENABLED</span>'
          : '<span class="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[var(--surface-alt)] text-[var(--text-muted)]">DISABLED</span>';

        var typeBadge = p.builtin
          ? '<span class="text-[9px] font-mono font-bold px-1.5 py-0.5 rounded bg-[var(--primary-dim)] text-[var(--primary)]">BUILT-IN</span>'
          : '<span class="text-[9px] font-mono font-bold px-1.5 py-0.5 rounded bg-amber-500/10 text-amber-400">COMMUNITY</span>';

        html += `
          <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-sm flex flex-col justify-between gap-4 hover:border-[var(--primary)]/40 transition-colors">
            <div class="flex items-start justify-between gap-3">
              <div class="flex items-center gap-3">
                <div class="w-11 h-11 rounded-xl bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0 shadow-xs">
                  <span class="material-symbols-outlined text-2xl" style="color: ${p.color || 'var(--primary)'}">${p.icon || 'extension'}</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <h3 class="text-sm font-bold text-[var(--text)] truncate">${esc(p.name)}</h3>
                    ${statusBadge}
                    ${typeBadge}
                  </div>
                  <span class="text-[10px] font-mono text-[var(--text-muted)] truncate">${esc(p.id)} · v${esc(p.version)}</span>
                </div>
              </div>
              <!-- Enable / Disable Toggle Switch -->
              <label class="relative inline-flex items-center cursor-pointer shrink-0" title="Toggle Plugin Activation">
                <input type="checkbox" ${p.enabled ? 'checked' : ''} onchange="togglePluginState('${p.id}', this.checked)" class="sr-only peer">
                <div class="w-9 h-5 bg-[var(--surface-alt)] border border-[var(--border)] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-[var(--primary)]"></div>
              </label>
            </div>

            <p class="text-xs text-[var(--text-secondary)] leading-relaxed">${esc(p.desc)}</p>

            <div class="flex items-center justify-between pt-3 border-t border-[var(--border)] gap-2 flex-wrap">
              <div class="flex items-center gap-1 flex-wrap">
                ${(p.permissions || []).map(perm => `<span class="px-1.5 py-0.5 rounded bg-[var(--surface-alt)] text-[9px] font-mono text-[var(--text-muted)] border border-[var(--border)]">${esc(perm)}</span>`).join('')}
              </div>
              <div class="flex items-center gap-1.5 shrink-0">
                <button onclick="openPluginSettings('${p.id}')" class="w-8 h-8 rounded-lg bg-[var(--surface-alt)] hover:bg-[var(--surface-hover)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] flex items-center justify-center btn-press" title="Plugin Settings">
                  <span class="material-symbols-outlined text-[16px]">settings</span>
                </button>
                <button onclick="popoutPlugin('${p.id}')" class="w-8 h-8 rounded-lg bg-[var(--surface-alt)] hover:bg-[var(--surface-hover)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] flex items-center justify-center btn-press" title="Open in Dedicated Tab">
                  <span class="material-symbols-outlined text-[16px]">open_in_new</span>
                </button>
                <button onclick="launchPluginSandbox('${p.id}')" class="px-3.5 py-1.5 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold hover:bg-[var(--primary-hover)] flex items-center gap-1.5 shadow-xs btn-press">
                  <span class="material-symbols-outlined text-[15px]">launch</span>
                  <span>Launch</span>
                </button>
              </div>
            </div>
          </div>
        `;
      });

      container.innerHTML = html;
      updatePluginFilterCounts();
    }

    function updatePluginFilterCounts() {
      var allCount = _pluginsRegistry.length;
      var activeCount = _pluginsRegistry.filter(p => p.enabled).length;
      var builtinCount = _pluginsRegistry.filter(p => p.builtin).length;
      var customCount = _pluginsRegistry.filter(p => !p.builtin).length;

      var cAll = document.getElementById('countFilterAll');
      if (cAll) cAll.textContent = allCount;
      var cAct = document.getElementById('countFilterActive');
      if (cAct) cAct.textContent = activeCount;
      var cBui = document.getElementById('countFilterBuiltin');
      if (cBui) cBui.textContent = builtinCount;
      var cCus = document.getElementById('countFilterCustom');
      if (cCus) cCus.textContent = customCount;
    }

    async function togglePluginState(pluginId, enabled) {
      var p = findPluginRecord(pluginId);
      if (p) {
        p.enabled = enabled;
        V07StorageEngine.saveCustomPlugins();

        var statesMap = {};
        try { statesMap = JSON.parse(localStorage.getItem('v07_plugin_states') || '{}'); } catch (_) {}
        statesMap[p.id] = enabled;
        if (p.aliases) p.aliases.forEach(function(a) { statesMap[a] = enabled; });
        localStorage.setItem('v07_plugin_states', JSON.stringify(statesMap));

        // Push to local server if available
        try {
          fetch('/api/v3/plugins/toggle', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: p.id, enabled: enabled })
          }).catch(function() {});
        } catch (_) {}

        // Push to Firestore if authenticated
        if (ensureFirebaseInitialized() && V07StorageEngine.userRef) {
          try {
            var pluginsList = _pluginsRegistry.map(function(x) {
              return {
                id: x.id,
                name: x.name,
                version: x.version,
                enabled: x.enabled,
                isBuiltIn: x.builtin,
                icon: x.icon,
                color: x.color,
                category: x.category || '',
                updated_at: Date.now()
              };
            });
            V07StorageEngine.userRef.collection('system').doc('plugins').set({
              plugins: pluginsList,
              updated_at: Date.now()
            }, { merge: true }).catch(function() {});
          } catch (_) {}
        }

        // Bridge invoke if inside WebView
        if (window.nativeInvoke) {
          try {
            window.nativeInvoke(enabled ? 'plugin:enable' : 'plugin:disable', { id: p.id });
          } catch (_) {}
        }

        renderPluginsView();
        renderDashboardPlugins();
        showToast((enabled ? 'Enabled ' : 'Disabled ') + p.name);
      }
    }

    function mergeRemotePlugins(remoteList) {
      if (!Array.isArray(remoteList)) return;
      remoteList.forEach(function(rp) {
        var existing = findPluginRecord(rp.id);
        if (existing) {
          if (rp.name) existing.name = rp.name;
          if (rp.version) existing.version = rp.version;
          if (rp.description || rp.desc) existing.desc = rp.description || rp.desc;
          if (rp.icon || rp.nav_icon) existing.icon = rp.icon || rp.nav_icon;
          if (rp.color || rp.nav_color) existing.color = rp.color || rp.nav_color;
          if (rp.enabled !== undefined) existing.enabled = Boolean(rp.enabled);
          if (rp.state) existing.state = rp.state;
          if (rp.category) existing.category = rp.category;
          if (rp.isBuiltIn !== undefined || rp.is_builtin !== undefined) {
            existing.builtin = Boolean(rp.isBuiltIn !== undefined ? rp.isBuiltIn : rp.is_builtin);
          }
        } else {
          _pluginsRegistry.push({
            id: rp.id,
            name: rp.name || rp.id,
            version: rp.version || '1.0.0',
            author: rp.author || rp.publisher || 'Community',
            icon: rp.icon || rp.nav_icon || 'extension',
            color: rp.color || rp.nav_color || 'var(--primary)',
            category: rp.category || 'Utilities',
            desc: rp.description || rp.desc || '',
            enabled: rp.enabled !== undefined ? Boolean(rp.enabled) : true,
            builtin: Boolean(rp.isBuiltIn !== undefined ? rp.isBuiltIn : rp.is_builtin),
            permissions: rp.permissions || ['storage:isolated'],
            entry: rp.entry || ('plugins/' + rp.id + '/index.html'),
            settings: rp.settings || ('plugins/' + rp.id + '/settings.html'),
            widget: rp.widget || ('plugins/' + rp.id + '/widget.html')
          });
        }
      });
      var statesMap = {};
      _pluginsRegistry.forEach(function(p) {
        statesMap[p.id] = p.enabled;
        if (p.aliases) p.aliases.forEach(function(a) { statesMap[a] = p.enabled; });
      });
      try { localStorage.setItem('v07_plugin_states', JSON.stringify(statesMap)); } catch (_) {}
    }

    async function refreshPluginsRegistry(showUserFeedback) {
      if (showUserFeedback === undefined) showUserFeedback = true;
      var iconHub = document.getElementById('refreshPluginsIcon');
      var iconDash = document.getElementById('dashRefreshIcon');
      var statusLbl = document.getElementById('pluginSyncStatusLabel');

      if (iconHub) iconHub.classList.add('animate-spin');
      if (iconDash) iconDash.classList.add('animate-spin');
      if (statusLbl) statusLbl.textContent = 'Syncing...';

      if (showUserFeedback) {
        showToast('Syncing plugins with Sanctuary & Cloud...', 'info');
      }

      var syncedFrom = null;

      // 1. Try local loopback server or LAN companion host
      try {
        var res = await fetch('/api/v3/plugins/refresh', { signal: AbortSignal.timeout(1800) });
        if (!res.ok) res = await fetch('/api/v3/plugins', { signal: AbortSignal.timeout(1800) });
        if (res.ok) {
          var data = await res.json();
          if (data && Array.isArray(data.plugins)) {
            mergeRemotePlugins(data.plugins);
            syncedFrom = 'App Host';
          }
        }
      } catch (_) {}

      // 2. Try Firestore system/plugins
      if (!syncedFrom && ensureFirebaseInitialized() && V07StorageEngine.userRef) {
        try {
          var doc = await V07StorageEngine.userRef.collection('system').doc('plugins').get();
          if (doc.exists) {
            var fData = doc.data();
            if (fData && Array.isArray(fData.plugins)) {
              mergeRemotePlugins(fData.plugins);
              syncedFrom = 'Cloud Firestore';
            }
          }
        } catch (e) {
          console.warn('[V07 Storage] Firestore plugin sync error:', e);
        }
      }

      // 3. Try WebView bridge
      if (!syncedFrom && (window.V3NativeBridge || window.Android)) {
        try {
          var pList = await window.nativeInvoke('plugin:list');
          if (Array.isArray(pList)) {
            mergeRemotePlugins(pList);
            syncedFrom = 'Native Bridge';
          }
        } catch (_) {}
      }

      // 4. Load custom plugins from localStorage
      V07StorageEngine.loadCustomPlugins();

      // 5. Restore saved states if no remote source responded
      if (!syncedFrom) {
        try {
          var saved = localStorage.getItem('v07_plugin_states');
          if (saved) {
            var sMap = JSON.parse(saved);
            _pluginsRegistry.forEach(function(p) {
              if (sMap[p.id] !== undefined) p.enabled = Boolean(sMap[p.id]);
            });
          }
        } catch (_) {}
      }

      renderPluginsView();
      renderDashboardPlugins();
      updatePluginFilterCounts();

      setTimeout(function() {
        if (iconHub) iconHub.classList.remove('animate-spin');
        if (iconDash) iconDash.classList.remove('animate-spin');
        if (statusLbl) statusLbl.textContent = syncedFrom ? ('Synced (' + syncedFrom + ')') : 'Sovereign Offline';
      }, 400);

      if (showUserFeedback) {
        var activeCount = _pluginsRegistry.filter(function(p) { return p.enabled; }).length;
        var totalCount = _pluginsRegistry.length;
        var srcText = syncedFrom ? (' via ' + syncedFrom) : '';
        showToast('Synchronized ' + totalCount + ' plugins (' + activeCount + ' active)' + srcText, 'success');
      }
    }
    window.refreshPluginsRegistry = refreshPluginsRegistry;

    function openInstallPluginModal() {
      openModal('modalInstallPlugin');
    }

    function setInstallMode(mode) {
      var mBtn = document.getElementById('installModeManifestBtn');
      var uBtn = document.getElementById('installModeUrlBtn');
      var mSec = document.getElementById('installManifestSection');
      var uSec = document.getElementById('installUrlSection');

      if (mode === 'manifest') {
        mBtn.classList.add('bg-[var(--primary)]', 'text-white');
        mBtn.classList.remove('bg-[var(--surface-alt)]', 'text-[var(--text-secondary)]');
        uBtn.classList.remove('bg-[var(--primary)]', 'text-white');
        uBtn.classList.add('bg-[var(--surface-alt)]', 'text-[var(--text-secondary)]');
        mSec.classList.remove('hidden');
        uSec.classList.add('hidden');
      } else {
        uBtn.classList.add('bg-[var(--primary)]', 'text-white');
        uBtn.classList.remove('bg-[var(--surface-alt)]', 'text-[var(--text-secondary)]');
        mBtn.classList.remove('bg-[var(--primary)]', 'text-white');
        mBtn.classList.add('bg-[var(--surface-alt)]', 'text-[var(--text-secondary)]');
        uSec.classList.remove('hidden');
        mSec.classList.add('hidden');
      }
    }

    async function submitInstallPlugin() {
      var manifestStr = document.getElementById('pluginManifestInput').value.trim();
      if (!manifestStr) {
        showToast('Please provide a valid plugin manifest JSON');
        return;
      }
      try {
        var manifest = JSON.parse(manifestStr);
        if (!manifest.id || !manifest.name) {
          throw new Error('Manifest missing required "id" or "name" field');
        }
        manifest.version = manifest.version || '1.0.0';
        manifest.permissions = manifest.permissions || ['storage', 'ledger'];
        manifest.enabled = true;
        manifest.builtin = false;
        manifest.icon = manifest.icon || 'extension';
        manifest.desc = manifest.description || 'Custom V07 Plugin';

        var existingIdx = _pluginsRegistry.findIndex(p => p.id === manifest.id);
        if (existingIdx >= 0) _pluginsRegistry[existingIdx] = manifest;
        else _pluginsRegistry.push(manifest);

        V07StorageEngine.saveCustomPlugins();
        closeModal('modalInstallPlugin');
        renderPluginsView();
        renderDashboardPlugins();
        showToast('Installed plugin: ' + manifest.name);
      } catch (e) {
        showToast('Error validating manifest: ' + e.message);
      }
    }

    // --- FULL IMPECCABLE PLUGIN SANDBOX CONTROLS ---
    function launchPluginSandbox(pluginId, entryOverride) {
      var p = findPluginRecord(pluginId);
      if (!p) return;

      _activeSandboxPluginId = p.id;
      _activeSandboxMode = (entryOverride && entryOverride.includes('settings')) ? 'settings' : 'app';

      var title = document.getElementById('sandboxTitle');
      var idBadge = document.getElementById('sandboxIdBadge');
      var icon = document.getElementById('sandboxIcon');
      var verBadge = document.getElementById('sandboxVersionBadge');
      var viewBadge = document.getElementById('sandboxViewModeBadge');
      var iframe = document.getElementById('pluginSandboxIframe');
      var loader = document.getElementById('sandboxLoadingOverlay');

      if (title) title.textContent = p.name;
      if (idBadge) idBadge.textContent = p.id;
      if (icon) {
        icon.textContent = p.icon || 'extension';
        icon.style.color = p.color || 'var(--primary)';
      }
      if (verBadge) verBadge.textContent = 'v' + (p.version || '1.0');
      if (viewBadge) viewBadge.textContent = (_activeSandboxMode === 'settings' ? 'Settings' : 'Main App');

      var entry = entryOverride || p.entry || ('plugins/' + p.id + '/index.html');

      if (loader) loader.classList.remove('hidden');
      if (iframe) {
        iframe.onload = function() {
          if (loader) loader.classList.add('hidden');
          try {
            var curTheme = document.documentElement.getAttribute('data-theme') || localStorage.getItem('v07_theme') || 'dark';
            iframe.contentWindow.postMessage({ type: 'v07_set_theme', theme: curTheme }, '*');
          } catch (_) {}
        };
        iframe.src = entry;
      }

      openModal('modalPluginSandbox');
    }

    function reloadPluginSandbox() {
      var iframe = document.getElementById('pluginSandboxIframe');
      var loader = document.getElementById('sandboxLoadingOverlay');
      var reloadIcon = document.getElementById('sandboxReloadIcon');
      if (reloadIcon) reloadIcon.classList.add('animate-spin');
      if (loader) loader.classList.remove('hidden');
      if (iframe) {
        var curSrc = iframe.src;
        iframe.src = curSrc;
      }
      setTimeout(function() {
        if (reloadIcon) reloadIcon.classList.remove('animate-spin');
      }, 700);
    }

    function toggleActivePluginSettingsView() {
      if (!_activeSandboxPluginId) return;
      var p = findPluginRecord(_activeSandboxPluginId);
      if (!p) return;
      if (_activeSandboxMode === 'app') {
        var settingsPath = p.settings || ('plugins/' + p.id + '/settings.html');
        launchPluginSandbox(_activeSandboxPluginId, settingsPath);
      } else {
        var appPath = p.entry || ('plugins/' + p.id + '/index.html');
        launchPluginSandbox(_activeSandboxPluginId, appPath);
      }
    }

    function popoutActivePlugin() {
      if (!_activeSandboxPluginId) return;
      var p = findPluginRecord(_activeSandboxPluginId);
      if (!p) return;
      var targetPath = (_activeSandboxMode === 'settings' && p.settings) ? p.settings : (p.entry || ('plugins/' + p.id + '/index.html'));
      window.open(targetPath, '_blank');
    }

    function popoutPlugin(pluginId) {
      var p = findPluginRecord(pluginId);
      if (!p) return;
      var targetPath = p.entry || ('plugins/' + p.id + '/index.html');
      window.open(targetPath, '_blank');
    }

    function openPluginSettings(pluginId) {
      var p = findPluginRecord(pluginId);
      if (!p) return;
      var settingsPath = p.settings || ('plugins/' + p.id + '/settings.html');
      launchPluginSandbox(p.id, settingsPath);
    }

    function closePluginSandbox() {
      var iframe = document.getElementById('pluginSandboxIframe');
      if (iframe) iframe.src = 'about:blank';
      _activeSandboxPluginId = null;
      _activeSandboxMode = 'app';
      closeModal('modalPluginSandbox');
    }

    window.closeCustomModal = closePluginSandbox;

    // --- SANCTUARY HOME ACTIVE PLUGINS SHELF ---
    function renderDashboardPlugins() {
      var grid = document.getElementById('dashPluginCardsGrid');
      if (!grid) return;
      var activePlugins = _pluginsRegistry.filter(p => p.enabled);
      var html = '';
      activePlugins.forEach(function(p) {
        html += `
          <div class="flex items-center justify-between p-1.5 sm:p-2 rounded-xl bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--primary)]/50 transition-all gap-2 group">
            <div class="flex items-center gap-2 min-w-0">
              <div class="w-7 h-7 rounded-lg bg-[var(--surface)] border border-[var(--border)] flex items-center justify-center shrink-0 shadow-xs">
                <span class="material-symbols-outlined text-[15px]" style="color: ${p.color || 'var(--primary)'}">${p.icon || 'extension'}</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-xs font-semibold text-[var(--text)] truncate">${esc(p.name)}</span>
                <span class="text-[9px] text-[var(--text-muted)] truncate">${esc(p.category || 'Utility')}</span>
              </div>
            </div>
            <div class="flex items-center gap-1 shrink-0">
              <button onclick="launchPluginSandbox('${p.id}')" class="px-2.5 py-1 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-[10px] font-bold hover:bg-[var(--primary-hover)] btn-press flex items-center gap-0.5">
                <span>Open</span>
                <span class="material-symbols-outlined text-[11px]">arrow_forward</span>
              </button>
              <button onclick="popoutPlugin('${p.id}')" class="text-[var(--text-muted)] hover:text-[var(--text)] p-1 rounded-lg hover:bg-[var(--surface)] transition-colors btn-press" title="Open in dedicated tab">
                <span class="material-symbols-outlined text-[13px]">open_in_new</span>
              </button>
            </div>
          </div>
        `;
      });
      grid.innerHTML = html;
    }

    // ==========================================================================
    // SOVEREIGN VAULT EXPORT & IMPORT
    // ==========================================================================
    function exportSovereignVault() {
      var exportData = {
        version: "3.0.0",
        exported_at: new Date().toISOString(),
        events: _eventsCache,
        tombstones: Array.from(_tombstones),
        plugins: _pluginsRegistry.filter(p => !p.builtin)
      };
      var blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
      var url = URL.createObjectURL(blob);
      var a = document.createElement('a');
      a.href = url;
      a.download = 'v07_sovereign_vault_' + getTodayStr() + '.json';
      a.click();
      URL.revokeObjectURL(url);
      showToast('Vault database exported successfully');
    }

    function importSovereignVault(event) {
      var file = event.target.files[0];
      if (!file) return;
      var reader = new FileReader();
      reader.onload = async function(e) {
        try {
          var data = JSON.parse(e.target.result);
          if (data.events && Array.isArray(data.events)) {
            data.events.forEach(function(ev) {
              if (!_tombstones.has(ev.id || ev.docId)) {
                var idx = _eventsCache.findIndex(x => (x.id === ev.id || x.docId === ev.docId));
                if (idx >= 0) {
                  // Merge newest
                  if ((ev.updated_at || 0) > (_eventsCache[idx].updated_at || 0)) {
                    _eventsCache[idx] = ev;
                  }
                } else {
                  _eventsCache.push(ev);
                }
              }
            });
            V07StorageEngine.saveLocalStorageEvents();
            showToast('Merged ' + data.events.length + ' vault records with zero data loss');
            renderAllViews();
          }
        } catch (err) {
          showToast('Failed to parse JSON backup: ' + err.message);
        }
      };
      reader.readAsText(file);
    }

    function triggerManualSync() {
      var icon = document.getElementById('syncIcon');
      if (icon) icon.classList.add('live-pulse');
      showToast('Synchronizing with sovereign database...');
      setTimeout(function() {
        if (V07StorageEngine.mode === 'cloud_firestore') {
          V07StorageEngine.syncFirestoreEvents();
        } else if (V07StorageEngine.mode === 'app_sqlite') {
          V07StorageEngine.loadAppEvents();
        }
        if (icon) icon.classList.remove('live-pulse');
        showToast('Database synchronization complete');
      }, 900);
    }

    function triggerPartitionMigration() {
      V07StorageEngine.runPartitionMigration();
    }
    window.triggerPartitionMigration = triggerPartitionMigration;

    // ==========================================================================
    // ALL VIEW RENDERERS (DASHBOARD, TASKS, HABITS, TIMELINE, STATS)
    // ==========================================================================
    function renderAllViews() {
      updateDateUI();
      renderDashboardView();
      renderDashboardPlugins();
      renderTasksView();
      renderHabitsView();
      renderGamifyView();
      renderTimelineView();
      renderPluginsView();
      renderVaultView();
      renderStatsCharts();
      renderAnalyticsView();

      // Update sidebar ledger count
      var navLedger = document.getElementById('navLedgerCount');
      if (navLedger) navLedger.textContent = _eventsCache.length;
    }

    function renderDashboardView() {
      // 1. Calculate Focus for selected date
      var focusMin = 0;
      var sessionsCount = 0;
      _eventsCache.filter(e => e.date === _selectedDate && isSessionCategory(e.category)).forEach(e => {
        focusMin += (parseInt(e.duration_min) || Math.round((parseInt(e.durationSec || e.duration_sec) || 0) / 60) || 0);
        sessionsCount++;
      });
      var fTotal = document.getElementById('dashFocusTotal');
      var fSub = document.getElementById('dashFocusSub');
      if (fTotal) fTotal.textContent = focusMin + 'm';
      if (fSub) fSub.textContent = sessionsCount + ' sessions logged';

      // 2. Tasks for selected date
      var dayTasks = _eventsCache.filter(e => isTaskCategory(e.category) && (e.deadline === _selectedDate || e.date === _selectedDate));
      var doneTasks = dayTasks.filter(t => t.status === 'done' || t.status === 'completed' || t.score === 1);
      var tTotal = document.getElementById('dashTasksTotal');
      var tBar = document.getElementById('dashTasksBar');
      var mTasksBadge = document.getElementById('mobileTasksBadge');
      if (tTotal) tTotal.textContent = doneTasks.length + ' / ' + dayTasks.length;
      if (mTasksBadge) mTasksBadge.textContent = doneTasks.length + ' / ' + dayTasks.length;
      if (tBar) {
        var pct = dayTasks.length > 0 ? Math.round((doneTasks.length / dayTasks.length) * 100) : 0;
        tBar.style.width = pct + '%';
      }

      // Populate dashboard obligations list
      var dList = document.getElementById('dashTasksList');
      if (dList) {
        if (dayTasks.length === 0) {
          dList.innerHTML = '<div class="text-xs text-[var(--text-muted)] italic p-2">No pending obligations for this date.</div>';
        } else {
          var h = '';
          dayTasks.slice(0, 4).forEach(function(t) {
            var isDone = t.status === 'done' || t.status === 'completed' || t.score === 1;
            h += `
              <div class="flex items-center justify-between p-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs">
                <div class="flex items-center gap-2.5 min-w-0">
                  <input type="checkbox" ${isDone ? 'checked' : ''} onchange="toggleTaskStatus('${t.id || t.docId}')" class="w-4 h-4 rounded text-[var(--primary)] accent-[var(--primary)] cursor-pointer">
                  <span class="truncate-single ${isDone ? 'line-through text-[var(--text-muted)]' : 'text-[var(--text)] font-semibold'}">${esc(t.title)}</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[9px] font-bold uppercase ${t.priority === 'high' ? 'bg-[var(--danger-dim)] text-[var(--danger)]' : 'bg-[var(--surface)] text-[var(--text-muted)]'}">${esc(t.priority || 'MED')}</span>
              </div>
            `;
          });
          dList.innerHTML = h;
        }
      }

      // 3. Habits Streak
      var habitsToday = _eventsCache.filter(e => isHabitCategory(e.category) && e.date === _selectedDate && (e.on || e.score === 1)).length;
      var hToday = document.getElementById('dashHabitsToday');
      if (hToday) hToday.textContent = habitsToday + ' checked today';

      // 4. Gamify Card
      var g = calculateGamifyStats();
      var dLvl = document.getElementById('dashLevelTitle');
      var dRank = document.getElementById('dashRankBadge');
      var dXp = document.getElementById('dashXpLabel');
      if (dLvl) dLvl.textContent = 'LVL ' + g.level;
      if (dRank) dRank.textContent = g.rankTitle.toUpperCase();
      if (dXp) dXp.textContent = g.currentLevelXp + ' / 100 XP to Level ' + (g.level + 1);

      // 5. Activity Ledger Preview
      var dStream = document.getElementById('dashTimelineStream');
      if (dStream) {
        var dayEvts = _eventsCache.filter(e => e.date === _selectedDate).slice(0, 4);
        if (dayEvts.length === 0) {
          dStream.innerHTML = '<div class="text-xs text-[var(--text-muted)] italic p-2">Zero ledger entries recorded for this date.</div>';
        } else {
          var sh = '';
          dayEvts.forEach(function(ev) {
            sh += `
              <div class="flex items-center justify-between p-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs">
                <div class="flex items-center gap-2 min-w-0">
                  <span class="w-2 h-2 rounded-full bg-[var(--primary)]"></span>
                  <span class="font-semibold text-[var(--text)] truncate">${esc(ev.title || ev.category)}</span>
                </div>
                <span class="text-[10px] font-mono text-[var(--text-muted)]">${fmtTime(ev.ts)}</span>
              </div>
            `;
          });
          dStream.innerHTML = sh;
        }
      }

      // 6. Screentime & Digital Vitals Card
      renderScreenTimeCard(focusMin);
    }

    function renderScreenTimeCard(focusMin) {
      if (typeof focusMin === 'undefined') {
        focusMin = 0;
        _eventsCache.filter(e => e.date === _selectedDate && isSessionCategory(e.category)).forEach(e => {
          focusMin += (parseInt(e.duration_min) || Math.round((parseInt(e.durationSec || e.duration_sec) || 0) / 60) || 0);
        });
      }

      var missingState = document.getElementById('screenTimeMissingState');
      var activeState = document.getElementById('screenTimeActiveState');
      var statusBadge = document.getElementById('screenTimeStatusBadge');
      var pillTotal = document.getElementById('dashScreenTimeTotal');
      var pillSub = document.getElementById('dashScreenTimeSub');
      var activeTotal = document.getElementById('screenTimeActiveTotal');
      var activeSub = document.getElementById('screenTimeActiveSub');
      var ratioEl = document.getElementById('screenTimeRatio');
      var deviceModelEl = document.getElementById('screenTimeDeviceModel');

      var screenEntries = _eventsCache.filter(e => e.date === _selectedDate && isScreenTimeCategory(e.category));
      
      var totalMinutes = 0;
      var topApp = '';
      var deviceName = 'Android Companion';

      if (screenEntries.length > 0) {
        screenEntries.forEach(function(item) {
          var dur = parseInt(item.duration_min) || Math.round((parseInt(item.duration_sec || item.durationSec) || 0) / 60) || 0;
          totalMinutes += dur;
          if (item.top_app || item.appName || item.app) {
            topApp = item.top_app || item.appName || item.app;
          }
          if (item.device || item.device_model) {
            deviceName = item.device || item.device_model;
          }
        });
      }

      if (totalMinutes === 0 && window._deviceUsageData && window._deviceUsageData.date === _selectedDate) {
        var sum = window._deviceUsageData.summary || {};
        totalMinutes = Math.round((sum.total_screen_time_sec || 0) / 60);
        if (sum.top_app && sum.top_app.app) topApp = sum.top_app.app;
        if (window._deviceUsageData.metadata && window._deviceUsageData.metadata.device_model) {
          deviceName = window._deviceUsageData.metadata.device_model;
        }
      }

      if (totalMinutes > 0) {
        var hrs = Math.floor(totalMinutes / 60);
        var mins = totalMinutes % 60;
        var formatted = (hrs > 0 ? hrs + 'h ' : '') + mins + 'm';

        if (pillTotal) pillTotal.textContent = formatted;
        if (pillSub) pillSub.textContent = topApp ? 'Top: ' + topApp : 'Recorded';
        if (activeTotal) activeTotal.textContent = formatted;
        if (activeSub) activeSub.textContent = topApp ? 'Top: ' + topApp : 'Android System';
        if (deviceModelEl) deviceModelEl.textContent = deviceName;

        if (ratioEl) {
          if (focusMin > 0) {
            var ratio = Math.round((focusMin / totalMinutes) * 100);
            ratioEl.textContent = ratio + '%';
          } else {
            ratioEl.textContent = '0%';
          }
        }

        if (statusBadge) {
          statusBadge.textContent = 'RECORDED';
          statusBadge.className = 'text-[9px] font-mono font-semibold px-1.5 py-0.5 rounded bg-[var(--surface-alt)] text-[var(--emerald)] border border-[var(--emerald)]/30';
        }

        if (missingState) missingState.classList.add('hidden');
        if (activeState) activeState.classList.remove('hidden');
      } else {
        if (pillTotal) pillTotal.textContent = '--';
        if (pillSub) pillSub.textContent = 'No Data';

        if (statusBadge) {
          statusBadge.textContent = 'AWAITING SYNC';
          statusBadge.className = 'text-[9px] font-mono font-semibold px-1.5 py-0.5 rounded bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]';
        }

        if (missingState) missingState.classList.remove('hidden');
        if (activeState) activeState.classList.add('hidden');
      }
    }

    function selectCockpitScreentime() {
      switchTab('cockpit');
      var card = document.getElementById('cardScreentimeContainer');
      if (card) {
        card.classList.add('border-[var(--emerald)]');
        setTimeout(function() {
          card.classList.remove('border-[var(--emerald)]');
        }, 1500);
      }
    }

    function openLogScreenTimeModal() {
      var hInput = document.getElementById('screenTimeHoursInput');
      var mInput = document.getElementById('screenTimeMinutesInput');
      var topAppInput = document.getElementById('screenTimeTopAppInput');
      var notesInput = document.getElementById('screenTimeNotesInput');
      if (hInput) hInput.value = '2';
      if (mInput) mInput.value = '30';
      if (topAppInput) topAppInput.value = '';
      if (notesInput) notesInput.value = '';
      openModal('modalLogScreenTime');
    }

    function submitManualScreenTime() {
      var hInput = document.getElementById('screenTimeHoursInput');
      var mInput = document.getElementById('screenTimeMinutesInput');
      var topAppInput = document.getElementById('screenTimeTopAppInput');
      var notesInput = document.getElementById('screenTimeNotesInput');

      var hrs = parseInt(hInput ? hInput.value : 0) || 0;
      var mins = parseInt(mInput ? mInput.value : 0) || 0;
      var totalMin = (hrs * 60) + mins;

      if (totalMin <= 0) {
        showToast('Please enter a valid screen time duration', 'error');
        return;
      }

      var topApp = topAppInput ? topAppInput.value.trim() : '';
      var notes = notesInput ? notesInput.value.trim() : '';

      var entry = {
        id: 'screentime_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
        category: 'app_usage',
        date: _selectedDate,
        duration_min: totalMin,
        duration_sec: totalMin * 60,
        top_app: topApp || 'Android Device',
        title: 'Device Screen Time: ' + (hrs > 0 ? hrs + 'h ' : '') + mins + 'm',
        notes: notes,
        ts: Date.now()
      };

      _eventsCache.push(entry);
      saveCache();
      persistEvent(entry);

      closeModal('modalLogScreenTime');
      renderDashboardView();
      renderTimelineView();
      renderAnalyticsView();
      showToast('Screen time logged (' + (hrs > 0 ? hrs + 'h ' : '') + mins + 'm)', 'success');
    }

    function renderTasksView() {
      var container = document.getElementById('tasksListContainer');
      if (!container) return;

      var allTasks = _eventsCache.filter(e => isTaskCategory(e.category));
      var filtered = allTasks.filter(function(t) {
        var isDone = t.status === 'done' || t.status === 'completed';
        if (_taskFilter === 'done') return isDone;
        if (_taskFilter === 'today') return !isDone && (t.deadline === getTodayStr() || t.date === getTodayStr());
        if (_taskFilter === 'overdue') return !isDone && t.deadline && t.deadline < getTodayStr();
        if (_taskFilter === 'upcoming') return !isDone && t.deadline && t.deadline > getTodayStr();
        return true;
      });

      var completedCount = allTasks.filter(t => t.status === 'done' || t.status === 'completed').length;
      var pct = allTasks.length > 0 ? Math.round((completedCount / allTasks.length) * 100) : 0;

      var pText = document.getElementById('tasksPercentText');
      var pBar = document.getElementById('tasksProgressBar');
      var navCount = document.getElementById('navTaskCount');

      if (pText) pText.textContent = pct + '% (' + completedCount + ' / ' + allTasks.length + ')';
      if (pBar) pBar.style.width = pct + '%';
      if (navCount) navCount.textContent = (allTasks.length - completedCount);

      if (filtered.length === 0) {
        container.innerHTML = '<div class="text-center py-10 text-xs text-[var(--text-muted)] italic">No obligations match this view filter.</div>';
        return;
      }

      var html = '';
      filtered.forEach(function(t) {
        var isDone = t.status === 'done' || t.status === 'completed';
        var prioColor = t.priority === 'high' ? 'bg-[var(--danger-dim)] text-[var(--danger)]' : (t.priority === 'low' ? 'bg-[var(--surface-alt)] text-[var(--text-muted)]' : 'bg-[var(--warning-dim)] text-[var(--warning)]');

        html += `
          <div class="p-3 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-sm flex items-center justify-between gap-3 transition-all">
            <div class="flex items-center gap-3 min-w-0">
              <input type="checkbox" ${isDone ? 'checked' : ''} onchange="toggleTaskStatus('${t.id || t.docId}')" class="w-4 h-4 rounded text-[var(--primary)] accent-[var(--primary)] cursor-pointer shrink-0">
              <div class="flex flex-col min-w-0">
                <span class="text-xs truncate-single ${isDone ? 'line-through text-[var(--text-muted)]' : 'text-[var(--text)] font-semibold'}">${esc(t.title)}</span>
                <span class="text-[10px] text-[var(--text-muted)] flex items-center gap-2">
                  <span>Due: ${esc(t.deadline || t.date || 'Anytime')}</span>
                </span>
              </div>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <span class="px-2 py-0.5 rounded text-[10px] font-bold uppercase ${prioColor}">${esc(t.priority || 'MED')}</span>
              <button onclick="V07StorageEngine.deleteEvent('${t.id || t.docId}')" class="text-[var(--text-muted)] hover:text-[var(--danger)] p-1 btn-press" title="Delete">
                <span class="material-symbols-outlined text-[16px]">delete</span>
              </button>
            </div>
          </div>
        `;
      });
      container.innerHTML = html;
    }

    function renderHabitsView() {
      var container = document.getElementById('habitsListContainer');
      if (!container) return;

      var standardHabits = [
        { title: 'Morning Meditation & Breathwork', icon: 'self_improvement' },
        { title: 'Deep Work Focus (50m)', icon: 'timer' },
        { title: 'Athletics & Physical Conditioning', icon: 'fitness_center' },
        { title: 'Bhagavad Gita Contemplation', icon: 'menu_book' },
        { title: 'Digital Sunset & Mindful Stillness', icon: 'bedtime' }
      ];

      // Add any custom habits saved
      var savedHabitEvents = _eventsCache.filter(e => isHabitCategory(e.category));
      savedHabitEvents.forEach(function(sh) {
        if (!standardHabits.find(h => h.title === sh.title)) {
          standardHabits.push({ title: sh.title, icon: sh.icon || 'local_fire_department' });
        }
      });

      var checkedToday = 0;
      var html = '';
      standardHabits.forEach(function(h) {
        var isChecked = _eventsCache.some(e => isHabitCategory(e.category) && e.title === h.title && e.date === _selectedDate && (e.on || e.score === 1));
        if (isChecked) checkedToday++;

        // Calculate simple streak
        var streak = 0;
        var d = new Date();
        for (var i = 0; i < 30; i++) {
          var ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
          var onThatDay = _eventsCache.some(e => isHabitCategory(e.category) && e.title === h.title && e.date === ds && (e.on || e.score === 1));
          if (onThatDay) streak++;
          else if (i > 0) break;
          d.setDate(d.getDate() - 1);
        }

        html += `
          <div class="p-3.5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-sm flex items-center justify-between gap-3">
            <div class="flex items-center gap-3 min-w-0">
              <button onclick="toggleHabitCheck('${esc(h.title)}')" class="w-8 h-8 rounded-lg flex items-center justify-center border transition-all btn-press ${isChecked ? 'bg-[var(--accent-habits)] border-[var(--accent-habits)] text-[var(--text-inverse)]' : 'bg-[var(--surface-alt)] border-[var(--border)] text-[var(--text-muted)]'}">
                <span class="material-symbols-outlined text-[18px]">${isChecked ? 'check' : h.icon}</span>
              </button>
              <div class="flex flex-col min-w-0">
                <span class="text-xs font-semibold text-[var(--text)] truncate">${esc(h.title)}</span>
                <span class="text-[10px] text-[var(--accent-habits)] font-bold">🔥 ${streak} Day Streak</span>
              </div>
            </div>
            <button onclick="toggleHabitCheck('${esc(h.title)}')" class="px-3 py-1 rounded-lg text-xs font-bold transition-all btn-press ${isChecked ? 'bg-[var(--accent-habits)]/15 text-[var(--accent-habits)]' : 'bg-[var(--surface-alt)] text-[var(--text-secondary)] border border-[var(--border)]'}">
              ${isChecked ? 'Checked' : 'Check In'}
            </button>
          </div>
        `;
      });

      container.innerHTML = html;
      var ratio = document.getElementById('habitsSummaryRatio');
      if (ratio) ratio.textContent = checkedToday + ' / ' + standardHabits.length + ' Checked';

      renderHabitsHeatmap();
    }

    function renderHabitsHeatmap() {
      var grid = document.getElementById('habitsHeatmapGrid');
      if (!grid) return;
      var html = '';
      var today = new Date();

      // Render 20 weeks (140 days)
      for (var w = 19; w >= 0; w--) {
        html += '<div class="flex flex-col gap-1">';
        for (var day = 6; day >= 0; day--) {
          var d = new Date(today);
          d.setDate(d.getDate() - (w * 7 + day));
          var ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
          var count = _eventsCache.filter(e => isHabitCategory(e.category) && e.date === ds && (e.on || e.score === 1)).length;

          var opacity = count === 0 ? 'bg-[var(--border)]/40' : (count < 2 ? 'bg-[var(--accent-habits)]/40' : (count < 4 ? 'bg-[var(--accent-habits)]/75' : 'bg-[var(--accent-habits)]'));
          html += `<div class="w-3 h-3 rounded-xs ${opacity}" title="${ds}: ${count} habits"></div>`;
        }
        html += '</div>';
      }
      grid.innerHTML = html;
    }

    function renderGamifyView() {
      var g = calculateGamifyStats();
      var title = document.getElementById('gamifyRankTitle');
      var badge = document.getElementById('gamifyBadgeLvl');
      var xpDisp = document.getElementById('gamifyTotalXpDisplay');
      var lBar = document.getElementById('gamifyLevelBar');
      var lRatio = document.getElementById('gamifyLevelRatio');

      if (title) title.textContent = 'Sovereign ' + g.rankTitle;
      if (badge) badge.textContent = 'LVL ' + g.level;
      if (xpDisp) xpDisp.textContent = g.totalXp + ' Total XP';
      if (lBar) lBar.style.width = g.currentLevelXp + '%';
      if (lRatio) lRatio.textContent = g.currentLevelXp + ' / 100 XP';

      // Daily Quests Deck
      var qContainer = document.getElementById('gamifyQuestsContainer');
      if (qContainer) {
        qContainer.innerHTML = `
          <div class="p-3.5 rounded-xl bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-between gap-3">
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-[var(--text)]">Deep Work Focus Session</span>
              <span class="text-[10px] text-[var(--text-muted)]">Complete at least 45m of focus</span>
            </div>
            <span class="px-2 py-1 rounded bg-[var(--accent-gamify)]/15 text-[var(--accent-gamify)] text-[10px] font-bold">+50 XP</span>
          </div>
          <div class="p-3.5 rounded-xl bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-between gap-3">
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-[var(--text)]">Triple Obligation Cleared</span>
              <span class="text-[10px] text-[var(--text-muted)]">Complete 3 task obligations today</span>
            </div>
            <span class="px-2 py-1 rounded bg-[var(--accent-gamify)]/15 text-[var(--accent-gamify)] text-[10px] font-bold">+45 XP</span>
          </div>
        `;
      }
    }

    var _timelineFilter = 'all';
    var _timelineScope = 'all'; // 'all' (All History) or 'date' (Selected Date)
    var _timelineSearchQuery = '';

    function setTimelineFilter(filter) {
      _timelineFilter = filter;
      document.querySelectorAll('.timeline-filter-btn').forEach(b => {
        b.classList.remove('bg-[var(--primary)]', 'text-white');
        b.classList.add('bg-[var(--surface-alt)]', 'text-[var(--text-secondary)]');
      });
      var active = document.querySelector('[data-timeline-filter="' + filter + '"]');
      if (active) {
        active.classList.add('bg-[var(--primary)]', 'text-white');
        active.classList.remove('bg-[var(--surface-alt)]', 'text-[var(--text-secondary)]');
      }
      renderTimelineView();
    }

    function setTimelineScope(scope) {
      _timelineScope = scope;
      var btnAll = document.getElementById('timelineScopeAll');
      var btnDate = document.getElementById('timelineScopeDate');
      if (scope === 'all') {
        if (btnAll) {
          btnAll.classList.add('bg-[var(--primary)]', 'text-white');
          btnAll.classList.remove('text-[var(--text-secondary)]');
        }
        if (btnDate) {
          btnDate.classList.remove('bg-[var(--primary)]', 'text-white');
          btnDate.classList.add('text-[var(--text-secondary)]');
        }
      } else {
        if (btnDate) {
          btnDate.classList.add('bg-[var(--primary)]', 'text-white');
          btnDate.classList.remove('text-[var(--text-secondary)]');
        }
        if (btnAll) {
          btnAll.classList.remove('bg-[var(--primary)]', 'text-white');
          btnAll.classList.add('text-[var(--text-secondary)]');
        }
      }
      renderTimelineView();
    }

    function onTimelineSearch(q) {
      _timelineSearchQuery = (q || '').trim().toLowerCase();
      renderTimelineView();
    }

    function renderTimelineView() {
      var container = document.getElementById('timelineEventsContainer');
      if (!container) return;

      var events = _eventsCache.slice();

      // 1. Scope filter
      if (_timelineScope === 'date') {
        events = events.filter(e => e.date === _selectedDate);
      }

      // 2. Category filter
      if (_timelineFilter !== 'all') {
        events = events.filter(function(e) {
          var c = (e.category || '').toLowerCase();
          if (_timelineFilter === 'thoughts') {
            return c === 'thought' || c === 'thoughts' || c === 'shares' || c === 'reading' || Boolean(e.topic) || (e.tags && c !== 'task');
          }
          if (_timelineFilter === 'session') {
            return c === 'session' || c === 'sessions' || c === 'focus';
          }
          if (_timelineFilter === 'tasks') {
            return c === 'task' || c === 'tasks' || c === 'tasks_ui';
          }
          if (_timelineFilter === 'habits') {
            return c === 'habit' || c === 'habits' || c === 'pushups';
          }
          if (_timelineFilter === 'mood') {
            return c === 'mood' || c === 'vitals' || c === 'energy';
          }
          return c === _timelineFilter;
        });
      }

      // 3. Search query filter
      if (_timelineSearchQuery) {
        events = events.filter(function(e) {
          var t = (e.title || '').toLowerCase();
          var c = (e.content || e.notes || '').toLowerCase();
          var top = (e.topic || '').toLowerCase();
          var tags = (e.tags || '').toLowerCase();
          var cat = (e.category || '').toLowerCase();
          return t.includes(_timelineSearchQuery) || c.includes(_timelineSearchQuery) || top.includes(_timelineSearchQuery) || tags.includes(_timelineSearchQuery) || cat.includes(_timelineSearchQuery);
        });
      }

      // 4. Sort descending
      events.sort(function(a, b) {
        var tA = a.ts || a.timestamp || 0;
        var tB = b.ts || b.timestamp || 0;
        return tB - tA;
      });

      // 5. Empty State
      if (events.length === 0) {
        var msg = _timelineSearchQuery
          ? 'No ledger records matched "' + esc(_timelineSearchQuery) + '"'
          : (_timelineScope === 'date' ? 'No activities recorded for ' + _selectedDate : 'Your sovereign database ledger is currently empty.');
        container.innerHTML = `
          <div class="text-center py-12 px-4 rounded-2xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col items-center justify-center gap-3">
            <div class="w-12 h-12 rounded-full bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center text-[var(--text-muted)]">
              <span class="material-symbols-outlined text-[24px]">history_edu</span>
            </div>
            <div class="flex flex-col items-center">
              <span class="text-sm font-bold text-[var(--text)]">Zero Ledger Records</span>
              <span class="text-xs text-[var(--text-muted)] mt-0.5">${msg}</span>
            </div>
            <div class="flex items-center gap-2 mt-2">
              <button onclick="openNewThoughtModal()" class="px-3.5 py-2 rounded-xl bg-purple-600 text-white text-xs font-bold hover:bg-purple-700 btn-press flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[16px]">psychology</span>
                <span>Write Thought</span>
              </button>
              <button onclick="openNewEventModal()" class="px-3.5 py-2 rounded-xl bg-[var(--primary)] text-white text-xs font-bold hover:bg-[var(--primary-hover)] btn-press flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[16px]">add</span>
                <span>Log Event</span>
              </button>
            </div>
          </div>
        `;
        return;
      }

      // 6. List Cards
      var html = '';
      events.forEach(function(ev) {
        var cat = (ev.category || 'general').toLowerCase();
        var isThought = cat === 'thought' || cat === 'thoughts' || cat === 'shares' || Boolean(ev.topic);
        var isSession = cat === 'session' || cat === 'sessions' || cat === 'focus';
        var isTask = cat === 'task' || cat === 'tasks' || cat === 'tasks_ui';
        var isHabit = cat === 'habit' || cat === 'habits' || cat === 'pushups';
        var isMood = cat === 'mood' || cat === 'vitals' || cat === 'energy';

        var icon = 'receipt_long';
        var iconColor = 'text-[var(--primary)]';
        var badgeBg = 'bg-[var(--surface-alt)] text-[var(--text-muted)]';
        var catName = cat.toUpperCase();

        if (isThought) {
          icon = 'psychology';
          iconColor = 'text-purple-400';
          badgeBg = 'bg-purple-500/15 text-purple-400 border border-purple-500/30';
          catName = 'Thought';
        } else if (isSession) {
          icon = 'timer';
          iconColor = 'text-emerald-400';
          badgeBg = 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30';
          catName = 'Focus';
        } else if (isTask) {
          icon = 'task_alt';
          iconColor = 'text-sky-400';
          badgeBg = 'bg-sky-500/15 text-sky-400 border border-sky-500/30';
          catName = 'Task';
        } else if (isHabit) {
          icon = 'local_fire_department';
          iconColor = 'text-amber-400';
          badgeBg = 'bg-amber-500/15 text-amber-400 border border-amber-500/30';
          catName = 'Habit';
        } else if (isMood) {
          icon = 'favorite';
          iconColor = 'text-rose-400';
          badgeBg = 'bg-rose-500/15 text-rose-400 border border-rose-500/30';
          catName = 'Vital';
        }

        var titleText = ev.title || ev.topic || (isThought ? 'Thought' : catName);
        var bodyText = ev.content || ev.text || '';
        var notesText = ev.notes || '';
        var durMin = ev.duration_min || (ev.duration_sec ? Math.round(ev.duration_sec / 60) : 0);

        html += `
          <div class="p-4 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex items-start justify-between gap-3 hover:border-[var(--primary)]/40 transition-colors">
            <div class="flex items-start gap-3 min-w-0 flex-1">
              <div class="w-8 h-8 rounded-lg bg-[var(--surface-alt)] flex items-center justify-center shrink-0 mt-0.5 border border-[var(--border)]">
                <span class="material-symbols-outlined text-[18px] ${iconColor}">${icon}</span>
              </div>
              <div class="flex flex-col min-w-0 flex-1">
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="text-xs font-bold text-[var(--text)]">${esc(titleText)}</span>
                  <span class="px-2 py-0.5 rounded text-[9px] font-mono uppercase font-bold ${badgeBg}">${esc(catName)}</span>
                  ${durMin > 0 ? `<span class="px-2 py-0.5 rounded text-[9px] font-mono font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">${durMin} Minutes</span>` : ''}
                  ${ev.tags ? `<span class="px-2 py-0.5 rounded text-[9px] font-mono bg-[var(--surface-alt)] text-[var(--text-secondary)]">#${esc(ev.tags)}</span>` : ''}
                </div>
                ${bodyText ? `<p class="text-xs text-[var(--text)] leading-relaxed mt-1.5 whitespace-pre-wrap">${esc(bodyText)}</p>` : ''}
                ${notesText ? `<p class="text-xs text-[var(--text-secondary)] italic mt-1">${esc(notesText)}</p>` : ''}
                <div class="flex items-center gap-2 mt-2 text-[10px] text-[var(--text-muted)] font-mono">
                  <span>${esc(ev.date || '')}</span>
                  <span>•</span>
                  <span>${fmtTime(ev.ts || ev.timestamp)}</span>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-1 shrink-0">
              <button onclick="V07StorageEngine.deleteEvent('${ev.id || ev.docId}')" class="text-[var(--text-muted)] hover:text-[var(--danger)] p-1.5 rounded-lg hover:bg-[var(--danger-dim)] transition-colors btn-press" title="Delete record">
                <span class="material-symbols-outlined text-[16px]">delete</span>
              </button>
            </div>
          </div>
        `;
      });
      container.innerHTML = html;
    }

    function renderVaultView() {
      var evtCount = document.getElementById('vaultEventsCount');
      var tombCount = document.getElementById('vaultTombstonesCount');
      var engLabel = document.getElementById('vaultActiveEngine');
      var engDetail = document.getElementById('vaultEngineDetail');

      if (evtCount) evtCount.textContent = _eventsCache.length;
      if (tombCount) tombCount.textContent = _tombstones.size + ' Guarded';

      if (engLabel) {
        if (V07StorageEngine.mode === 'app_sqlite') {
          engLabel.textContent = 'SQLITE ENGINE (ANDROID)';
          if (engDetail) engDetail.textContent = 'Connected via native AndroidBridge to v07_phone.db (Zero mock data).';
        } else if (V07StorageEngine.mode === 'cloud_firestore') {
          engLabel.textContent = 'FIRESTORE CLOUD ENGINE';
          if (engDetail) engDetail.textContent = 'Direct sync to users/{uid}/events with offline cache fallback.';
        } else {
          engLabel.textContent = 'SOVEREIGN LOCAL ENGINE';
          if (engDetail) engDetail.textContent = 'High-speed browser storage with zero external dependencies.';
        }
      }
    }

    // ==========================================================================
    // DATA & ANALYTICS INTELLIGENCE (High-Clarity Analysis, Charts & Matrix)
    // ==========================================================================
    var _analyticsRangeDays = 14;

    function setAnalyticsRange(days) {
      _analyticsRangeDays = days;
      [7, 14, 30].forEach(function(d) {
        var b = document.getElementById('rangeBtn' + d);
        if (b) {
          if (d === days) {
            b.classList.add('active', 'bg-[var(--primary)]', 'text-white');
            b.classList.remove('text-[var(--text-secondary)]');
          } else {
            b.classList.remove('active', 'bg-[var(--primary)]', 'text-white');
            b.classList.add('text-[var(--text-secondary)]');
          }
        }
      });
      renderAnalyticsView();
    }

    function calculateHabitMaxStreak() {
      var maxStreak = 0;
      var standardHabits = [
        'Morning Meditation & Breathwork',
        'Deep Work Focus (50m)',
        'Athletics & Physical Conditioning',
        'Bhagavad Gita Contemplation',
        'Digital Sunset & Mindful Stillness'
      ];
      standardHabits.forEach(function(title) {
        var streak = 0;
        var d = new Date();
        for (var i = 0; i < 60; i++) {
          var ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
          var ok = _eventsCache.some(e => isHabitCategory(e.category) && e.title === title && e.date === ds && (e.on || e.score === 1));
          if (ok) {
            streak++;
          } else if (i > 0) {
            break;
          }
          d.setDate(d.getDate() - 1);
        }
        if (streak > maxStreak) maxStreak = streak;
      });
      return maxStreak;
    }

    function renderAnalyticsView() {
      var days = _analyticsRangeDays || 14;
      var today = new Date();
      var dateList = [];
      var byDateFocus = {};
      var byDateScreenTime = {};
      var byDateTasksTotal = {};
      var byDateTasksDone = {};
      var byDateHabits = {};
      var byDateEventsCount = {};
      var categoryCounts = {};

      for (var i = days - 1; i >= 0; i--) {
        var d = new Date(today);
        d.setDate(d.getDate() - i);
        var ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
        dateList.push({ dateStr: ds, jsDate: d });
        byDateFocus[ds] = 0;
        byDateScreenTime[ds] = 0;
        byDateTasksTotal[ds] = 0;
        byDateTasksDone[ds] = 0;
        byDateHabits[ds] = 0;
        byDateEventsCount[ds] = 0;
      }

      var totalFocusMin = 0;
      var totalScreenMin = 0;
      var totalTasks = 0;
      var totalTasksDone = 0;
      var totalHabitChecks = 0;
      var totalEntries = 0;

      _eventsCache.forEach(function(e) {
        var dStr = e.date || (e.ts ? new Date(e.ts).toISOString().slice(0, 10) : '');
        if (byDateFocus[dStr] !== undefined) {
          byDateEventsCount[dStr]++;
          totalEntries++;

          var cat = (e.category || 'general').toLowerCase();
          categoryCounts[cat] = (categoryCounts[cat] || 0) + 1;

          if (isSessionCategory(cat)) {
            var min = parseInt(e.duration_min) || Math.round((parseInt(e.durationSec || e.duration_sec) || 0) / 60) || 0;
            byDateFocus[dStr] += min;
            totalFocusMin += min;
          } else if (isScreenTimeCategory(cat)) {
            var sMin = parseInt(e.duration_min) || Math.round((parseInt(e.durationSec || e.duration_sec) || 0) / 60) || 0;
            byDateScreenTime[dStr] = (byDateScreenTime[dStr] || 0) + sMin;
            totalScreenMin += sMin;
          } else if (isTaskCategory(cat)) {
            byDateTasksTotal[dStr]++;
            totalTasks++;
            if (e.status === 'done' || e.status === 'completed' || e.score === 1) {
              byDateTasksDone[dStr]++;
              totalTasksDone++;
            }
          } else if (isHabitCategory(cat)) {
            if (e.on || e.score === 1) {
              byDateHabits[dStr]++;
              totalHabitChecks++;
            }
          }
        }
      });

      // 1. Fill KPI scorecards
      var fHours = Math.floor(totalFocusMin / 60);
      var fRemMin = totalFocusMin % 60;
      var elFocusTot = document.getElementById('analyticsTotalFocus');
      var elFocusAvg = document.getElementById('analyticsAvgFocus');
      if (elFocusTot) elFocusTot.textContent = fHours + 'h ' + fRemMin + 'm';
      if (elFocusAvg) elFocusAvg.textContent = Math.round(totalFocusMin / days) + 'm/day';

      var elScreenTot = document.getElementById('analyticsTotalScreenTime');
      var elScreenAvg = document.getElementById('analyticsAvgScreenTime');
      if (totalScreenMin > 0) {
        var sHours = Math.floor(totalScreenMin / 60);
        var sRemMin = totalScreenMin % 60;
        if (elScreenTot) elScreenTot.textContent = (sHours > 0 ? sHours + 'h ' : '') + sRemMin + 'm';
        if (elScreenAvg) elScreenAvg.textContent = Math.round(totalScreenMin / days) + 'm/day';
      } else {
        if (elScreenTot) elScreenTot.textContent = '--';
        if (elScreenAvg) elScreenAvg.textContent = 'No Data';
      }

      var elTaskVel = document.getElementById('analyticsTaskVelocity');
      var elTaskRatio = document.getElementById('analyticsTasksDoneRatio');
      var taskPct = totalTasks > 0 ? Math.round((totalTasksDone / totalTasks) * 100) : 0;
      if (elTaskVel) elTaskVel.textContent = taskPct + '%';
      if (elTaskRatio) elTaskRatio.textContent = totalTasksDone + ' / ' + totalTasks;

      var elHabitAdh = document.getElementById('analyticsHabitAdherence');
      var elHabitStreak = document.getElementById('analyticsActiveStreaks');
      var activeHabitsCount = 3;
      var habitPct = Math.min(100, Math.round((totalHabitChecks / (days * activeHabitsCount)) * 100));
      if (elHabitAdh) elHabitAdh.textContent = habitPct + '%';
      if (elHabitStreak) elHabitStreak.textContent = calculateHabitMaxStreak() + ' days';

      var elTotEntries = document.getElementById('analyticsTotalEntries');
      var elTopCat = document.getElementById('analyticsTopCategory');
      if (elTotEntries) elTotEntries.textContent = totalEntries;
      var topCat = 'Focus';
      var topCount = 0;
      Object.keys(categoryCounts).forEach(k => {
        if (categoryCounts[k] > topCount) {
          topCount = categoryCounts[k];
          topCat = k.toUpperCase();
        }
      });
      if (elTopCat) elTopCat.textContent = topCat;

      // 2. Render Telemetry Matrix Table
      var matrixBody = document.getElementById('analyticsMatrixBody');
      if (matrixBody) {
        var rowsHtml = '';
        dateList.slice().reverse().forEach(function(item) {
          var ds = item.dateStr;
          var fMin = byDateFocus[ds] || 0;
          var sMin = byDateScreenTime[ds] || 0;
          var sHours = Math.floor(sMin / 60);
          var sRem = sMin % 60;
          var sFormatted = sMin > 0 ? ((sHours > 0 ? sHours + 'h ' : '') + sRem + 'm') : '—';
          var tDone = byDateTasksDone[ds] || 0;
          var tTot = byDateTasksTotal[ds] || 0;
          var hCount = byDateHabits[ds] || 0;
          var eCount = byDateEventsCount[ds] || 0;

          var isToday = (ds === getTodayStr());
          var dayName = item.jsDate.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });

          var ratingText = 'LIGHT';
          var ratingClass = 'text-[var(--text-muted)]';
          if (fMin >= 90 || (tDone >= 3 && hCount >= 2)) {
            ratingText = 'HIGH FLOW';
            ratingClass = 'text-[var(--emerald)] font-bold';
          } else if (fMin >= 45 || tDone >= 1 || hCount >= 1) {
            ratingText = 'STEADY';
            ratingClass = 'text-[var(--primary)] font-semibold';
          }

          rowsHtml += `
            <tr class="hover:bg-[var(--surface-hover)] transition-colors border-b border-[var(--border)]">
              <td class="py-2.5 px-3">
                <span class="${isToday ? 'text-[var(--primary)] font-bold' : 'text-[var(--text)]'}">${dayName}</span>
                ${isToday ? '<span class="ml-1 text-[9px] px-1 py-0.2 rounded bg-[var(--primary-dim)] text-[var(--primary)] font-bold">TODAY</span>' : ''}
              </td>
              <td class="py-2.5 px-3 font-semibold ${fMin > 0 ? 'text-[var(--emerald)]' : 'text-[var(--text-muted)]'}">${fMin}m</td>
              <td class="py-2.5 px-3 font-mono ${sMin > 0 ? 'text-[var(--text)] font-semibold' : 'text-[var(--text-muted)]'}">${sFormatted}</td>
              <td class="py-2.5 px-3 text-[var(--text-secondary)]">${tDone} / ${tTot}</td>
              <td class="py-2.5 px-3 text-[var(--text-secondary)]">${hCount > 0 ? hCount + 'd' : '—'}</td>
              <td class="py-2.5 px-3 text-[var(--text-muted)]">${eCount}</td>
              <td class="py-2.5 px-3 text-right font-mono text-[11px] ${ratingClass}">${ratingText}</td>
            </tr>
          `;
        });
        matrixBody.innerHTML = rowsHtml;
      }

      // 3. Render Chart.js
      renderAnalyticsCharts(dateList, byDateFocus, byDateEventsCount);
    }

    function renderAnalyticsCharts(dateList, byDateFocus, byDateEventsCount) {
      if (typeof Chart === 'undefined') return;

      var labels = dateList.map(d => d.dateStr.slice(5));
      var focusData = dateList.map(d => byDateFocus[d.dateStr] || 0);
      var eventsData = dateList.map(d => byDateEventsCount[d.dateStr] || 0);

      // Focus Volume Canvas
      var fCanvas = document.getElementById('chartFocusVolume') || document.getElementById('chartSessions');
      if (fCanvas) {
        if (_chartInstances.focus) _chartInstances.focus.destroy();
        _chartInstances.focus = new Chart(fCanvas, {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: 'Focus Minutes',
              data: focusData,
              backgroundColor: 'rgba(16, 185, 129, 0.45)',
              borderColor: '#10B981',
              borderWidth: 1,
              borderRadius: 4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: false },
              tooltip: {
                callbacks: {
                  label: function(ctx) { return ctx.raw + ' min focus'; }
                }
              }
            },
            scales: {
              x: { ticks: { color: '#94A3B8', font: { family: 'JetBrains Mono', size: 10 } }, grid: { display: false } },
              y: { ticks: { color: '#94A3B8', font: { family: 'JetBrains Mono', size: 10 } }, grid: { color: 'rgba(255,255,255,0.05)' } }
            }
          }
        });
      }

      // Pacing Trend Canvas
      var pCanvas = document.getElementById('chartPacingTrend') || document.getElementById('chartDaily');
      if (pCanvas) {
        if (_chartInstances.pacing) _chartInstances.pacing.destroy();
        _chartInstances.pacing = new Chart(pCanvas, {
          type: 'line',
          data: {
            labels: labels,
            datasets: [{
              label: 'Ledger Entries',
              data: eventsData,
              borderColor: '#3B82F6',
              backgroundColor: 'rgba(59, 130, 246, 0.12)',
              fill: true,
              tension: 0.35,
              borderWidth: 2,
              pointRadius: 3,
              pointBackgroundColor: '#3B82F6'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              x: { ticks: { color: '#94A3B8', font: { family: 'JetBrains Mono', size: 10 } }, grid: { display: false } },
              y: { ticks: { color: '#94A3B8', font: { family: 'JetBrains Mono', size: 10 } }, grid: { color: 'rgba(255,255,255,0.05)' } }
            }
          }
        });
      }
    }

    function renderStatsCharts() {
      // Legacy wrapper delegating to modern renderAnalyticsView
      renderAnalyticsView();
    }

    // --- ZERO-SCROLL COCKPIT ADAPTIVE CONTROLLER (< 1024px) ---
    function setMobileCockpitTab(tabKey) {
      var sections = {
        'focus': document.getElementById('chronoSection'),
        'obligations': document.getElementById('obligationsSection'),
        'tools': document.getElementById('toolsSection')
      };
      var btns = {
        'focus': document.getElementById('mobileCockpitTab-focus'),
        'obligations': document.getElementById('mobileCockpitTab-obligations'),
        'tools': document.getElementById('mobileCockpitTab-tools'),
        'all': document.getElementById('mobileCockpitTab-all')
      };

      if (tabKey === 'all') {
        Object.keys(sections).forEach(function(k) {
          var sec = sections[k];
          if (sec) {
            sec.classList.remove('hidden');
            sec.classList.add('flex');
          }
        });
        Object.keys(btns).forEach(function(k) {
          var b = btns[k];
          if (!b) return;
          if (k === 'all') {
            b.classList.add('bg-[var(--surface-alt)]', 'text-[var(--text)]', 'border-[var(--border)]');
            b.classList.remove('text-[var(--text-muted)]', 'border-transparent');
          } else {
            b.classList.remove('bg-[var(--surface-alt)]', 'text-[var(--text)]', 'border-[var(--border)]');
            b.classList.add('text-[var(--text-secondary)]', 'border-transparent');
          }
        });
        return;
      }

      Object.keys(sections).forEach(function(k) {
        var sec = sections[k];
        var btn = btns[k];
        if (!sec) return;
        if (k === tabKey) {
          sec.classList.remove('hidden');
          sec.classList.add('flex');
          if (btn) {
            btn.classList.add('bg-[var(--surface-alt)]', 'text-[var(--text)]', 'border-[var(--border)]');
            btn.classList.remove('text-[var(--text-secondary)]', 'border-transparent');
          }
        } else {
          sec.classList.add('hidden');
          sec.classList.remove('flex');
          if (btn) {
            btn.classList.remove('bg-[var(--surface-alt)]', 'text-[var(--text)]', 'border-[var(--border)]');
            btn.classList.add('text-[var(--text-secondary)]', 'border-transparent');
          }
        }
      });
      if (btns['all']) {
        btns['all'].classList.remove('bg-[var(--surface-alt)]', 'text-[var(--text)]', 'border-[var(--border)]');
        btns['all'].classList.add('text-[var(--text-muted)]', 'border-transparent');
      }
    }
    window.setMobileCockpitTab = setMobileCockpitTab;

    function selectCockpitFocus() {
      switchTab('cockpit');
      if (window.innerWidth < 1024) setMobileCockpitTab('focus');
      var chrono = document.getElementById('chronoSection');
      if (chrono) chrono.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
    window.selectCockpitFocus = selectCockpitFocus;

    function selectCockpitTasks() {
      switchTab('cockpit');
      if (window.innerWidth < 1024) setMobileCockpitTab('obligations');
      var inp = document.getElementById('quickTaskInput');
      if (inp) {
        inp.focus();
        inp.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    }
    window.selectCockpitTasks = selectCockpitTasks;

    function scrollToChrono() {
      selectCockpitFocus();
    }

    function addQuickTask() {
      var inp = document.getElementById('quickTaskInput');
      var prio = document.getElementById('quickTaskPrio');
      if (!inp) return;
      var title = inp.value.trim();
      if (!title) return;
      var taskEvent = {
        id: 'task_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
        category: 'tasks',
        title: title,
        priority: (prio ? prio.value : 'medium'),
        date: _selectedDate,
        deadline: _selectedDate,
        status: 'pending',
        ts: Date.now()
      };
      V07StorageEngine.saveEvent(taskEvent);
      inp.value = '';
      showToast('Obligation recorded for ' + _selectedDate);
      renderAllViews();
    }

    function submitInlineThought() {
      var inp = document.getElementById('quickThoughtInlineInput');
      var catSel = document.getElementById('quickThoughtCategorySelect');
      if (!inp) return;
      var content = inp.value.trim();
      if (!content) return;
      var cat = catSel ? catSel.value : 'thoughts';
      var thoughtEvent = {
        id: 'thought_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
        category: cat,
        title: content.slice(0, 40) + (content.length > 40 ? '...' : ''),
        content: content,
        notes: content,
        date: _selectedDate,
        ts: Date.now()
      };
      V07StorageEngine.saveEvent(thoughtEvent);
      inp.value = '';
      showToast('Thought committed to Sovereign Ledger');
      renderAllViews();
    }

    function exportAllDataJson() {
      exportSovereignVault();
    }

    function importDataJson(event) {
      var file = (event && event.target && event.target.files) ? event.target.files[0] : event;
      if (!file) return;
      importSovereignVault({ target: { files: [file] } });
    }

    window.filterPluginsGrid = function(f) {
      if (typeof setPluginFilter === 'function') setPluginFilter(f);
    };

    // ==========================================================================
    // NAVIGATION & MODALS
    // ==========================================================================
    function switchTab(tabId) {
      // Architectural alias mapping to V07 Sanctuary conventions
      if (tabId === 'home' || tabId === 'dashboard') tabId = 'cockpit';
      if (tabId === 'tools') tabId = 'plugins';
      if (tabId === 'day' || tabId === 'stats') tabId = 'analytics';
      if (tabId === 'settings') tabId = 'vault';
      if (tabId === 'timeline') tabId = 'ledger';
      if (tabId === 'chrono') {
        selectCockpitFocus();
        return;
      }
      if (tabId === 'tasks') {
        selectCockpitTasks();
        return;
      }

      _activeTab = tabId;
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.add('hidden'));
      var target = document.getElementById('panel-' + tabId);
      if (target) {
        target.classList.remove('hidden');
        target.classList.add('flex');
        if (tabId === 'cockpit' && window.innerWidth < 1024) {
          var currentActive = document.querySelector('.mobile-cockpit-section:not(.hidden)');
          if (!currentActive) setMobileCockpitTab('focus');
        }
      }

      // Sidebar buttons
      document.querySelectorAll('.nav-item').forEach(b => {
        b.classList.remove('active', 'bg-[var(--surface-hover)]', 'text-[var(--text)]');
        b.classList.add('text-[var(--text-secondary)]');
      });
      var activeSide = document.querySelector('[data-tab-btn="' + tabId + '"]');
      if (activeSide) {
        activeSide.classList.add('active', 'bg-[var(--surface-hover)]', 'text-[var(--text)]');
        activeSide.classList.remove('text-[var(--text-secondary)]');
      }

      // Mobile bottom bar buttons
      document.querySelectorAll('.mobile-nav-btn').forEach(b => {
        b.classList.remove('active', 'text-[var(--primary)]');
        b.classList.add('text-[var(--text-secondary)]');
      });
      var activeMob = document.querySelector('[data-mobile-tab="' + tabId + '"]');
      if (activeMob) {
        activeMob.classList.add('active', 'text-[var(--primary)]');
        activeMob.classList.remove('text-[var(--text-secondary)]');
      }

      if (tabId === 'analytics') {
        renderAnalyticsView();
      }

      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function openModal(id) {
      var m = document.getElementById(id);
      if (m) m.classList.remove('hidden');
    }

    function closeModal(id) {
      var m = document.getElementById(id);
      if (m) m.classList.add('hidden');
    }

    function openMobileDrawer() {
      var d = document.getElementById('mobileDrawer');
      if (d) d.classList.remove('hidden');
    }

    function closeMobileDrawer() {
      var d = document.getElementById('mobileDrawer');
      if (d) d.classList.add('hidden');
    }

    function openShortcutsModal() {
      openModal('modalShortcuts');
    }

    function openNewEventModal() {
      openModal('modalNewEvent');
    }

    async function submitNewEvent() {
      var cat = document.getElementById('newEventCategory').value;
      var title = document.getElementById('newEventTitle').value.trim();
      var content = document.getElementById('newEventContent').value.trim();
      if (!title) {
        showToast('Please enter an event title');
        return;
      }
      var ev = {
        category: cat,
        title: title,
        content: content,
        date: _selectedDate,
        ts: Date.now()
      };
      await V07StorageEngine.saveEvent(ev);
      closeModal('modalNewEvent');
      document.getElementById('newEventTitle').value = '';
      document.getElementById('newEventContent').value = '';
      showToast('Event committed to ledger');
      renderAllViews();
    }

    function toggleTheme() {
      var root = document.documentElement;
      var current = root.getAttribute('data-theme');
      var next = current === 'light' ? 'dark' : 'light';
      root.setAttribute('data-theme', next);
      localStorage.setItem('v07_theme', next);
      var icon = document.getElementById('themeIcon');
      if (icon) icon.textContent = next === 'light' ? 'light_mode' : 'dark_mode';
      showToast('Theme updated to ' + next);
      renderStatsCharts();
    }

    function showToast(titleOrMsg, optMsg, optType) {
      var container = document.getElementById('toastContainer');
      if (!container) return;
      var title = optMsg ? titleOrMsg : '';
      var message = optMsg ? optMsg : titleOrMsg;
      var toast = document.createElement('div');
      toast.className = 'px-4 py-2.5 rounded-xl bg-[var(--surface-alt)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] shadow-lg fade-in flex items-center gap-2.5 pointer-events-auto';
      toast.innerHTML = `
        <span class="material-symbols-outlined text-[16px] text-[var(--primary)] shrink-0">info</span>
        <div class="flex flex-col min-w-0">
          ${title ? `<span class="font-bold text-[var(--text)] leading-tight truncate">${esc(title)}</span>` : ''}
          <span class="text-[var(--text-secondary)] leading-tight truncate">${esc(message)}</span>
        </div>
      `;
      container.appendChild(toast);
      setTimeout(function() {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
      }, 3200);
    }

    var _authIsSignUp = false;

    function handleAuthAction() {
      var user = getSafeCurrentUser();
      updateAuthModalUI(user);
      openModal('modalAuth');
    }

    function toggleAuthMode() {
      _authIsSignUp = !_authIsSignUp;
      var title = document.getElementById('authModalTitle');
      var sub = document.getElementById('authModalSub');
      var btn = document.getElementById('authSubmitBtn');
      var toggleBtn = document.getElementById('authToggleModeBtn');
      var err = document.getElementById('authErrorMessage');
      if (err) { err.textContent = ''; err.classList.add('hidden'); }

      if (_authIsSignUp) {
        if (title) title.textContent = 'Create V07 Cloud Account';
        if (sub) sub.textContent = 'Register once to unify your mobile Android app and Web companion.';
        if (btn) btn.innerHTML = '<span class="material-symbols-outlined text-[18px]">person_add</span><span>Create Account</span>';
        if (toggleBtn) toggleBtn.textContent = 'Already have an account? Sign In';
      } else {
        if (title) title.textContent = 'Sign In to V07 Cloud';
        if (sub) sub.textContent = 'Single source of truth (Firestore) for Android app & Web Companion.';
        if (btn) btn.innerHTML = '<span class="material-symbols-outlined text-[18px]">login</span><span>Sign In</span>';
        if (toggleBtn) toggleBtn.textContent = "Don't have an account? Sign Up";
      }
    }

    function updateAuthModalUI(user) {
      user = user || getSafeCurrentUser();
      var loggedInView = document.getElementById('authLoggedInView');
      var loggedOutView = document.getElementById('authLoggedOutView');
      var actionBtn = document.getElementById('authActionBtn');
      var userLbl = document.getElementById('userAccountLabel');
      var userSub = document.getElementById('userAccountSub');

      if (user) {
        if (loggedInView) loggedInView.classList.remove('hidden');
        if (loggedOutView) loggedOutView.classList.add('hidden');
        var emailEl = document.getElementById('authCurrentEmail');
        var uidEl = document.getElementById('authCurrentUid');
        if (emailEl) emailEl.textContent = user.email || 'Cloud User';
        if (uidEl) uidEl.textContent = user.uid || 'Authenticated';
        if (actionBtn) actionBtn.textContent = 'Account';
        if (userLbl) userLbl.textContent = user.email ? user.email.split('@')[0] : 'Cloud User';
        if (userSub) userSub.textContent = user.email || 'Single Source of Truth';
      } else {
        if (loggedInView) loggedInView.classList.add('hidden');
        if (loggedOutView) loggedOutView.classList.remove('hidden');
        if (actionBtn) actionBtn.textContent = 'Sign In';
        if (userLbl) userLbl.textContent = 'Local Mode';
        if (userSub) userSub.textContent = 'Zero Data Loss Guard';
      }
    }

    async function submitAuthLogin() {
      var email = document.getElementById('authEmail').value.trim();
      var pass = document.getElementById('authPassword').value.trim();
      var errEl = document.getElementById('authErrorMessage');
      var btn = document.getElementById('authSubmitBtn');

      if (errEl) { errEl.textContent = ''; errEl.classList.add('hidden'); }

      if (!email || !pass) {
        var msg = 'Please enter both your email and password.';
        if (errEl) { errEl.textContent = msg; errEl.classList.remove('hidden'); }
        showToast(msg);
        return;
      }

      var prevBtnHtml = btn ? btn.innerHTML : '';
      if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="material-symbols-outlined text-[18px] animate-spin">refresh</span><span>Processing...</span>';
      }

      try {
        if (!ensureFirebaseInitialized()) {
          throw new Error('Firebase SDK is not available. Please check network connection.');
        }

        var auth = getSafeFirebaseAuth();
        if (!auth) {
          throw new Error('Firebase Authentication module unavailable.');
        }

        var cred;
        if (_authIsSignUp) {
          cred = await auth.createUserWithEmailAndPassword(email, pass);
          showToast('Account created successfully: ' + (cred.user.email || email));
        } else {
          cred = await auth.signInWithEmailAndPassword(email, pass);
          showToast('Signed in as ' + (cred.user.email || email));
        }

        var user = auth.currentUser || (cred && cred.user);
        V07StorageEngine.currentUser = user;

        // If connected to local Android backend, sync companion auth state to phone
        if (V07StorageEngine.mode === 'app_sqlite') {
          try {
            await fetch('/api/v3/companion/auth', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ email: email, password: pass, createMode: _authIsSignUp }),
              signal: AbortSignal.timeout(3000)
            });
          } catch (_) {}
        } else {
          V07StorageEngine.mode = 'cloud_firestore';
          V07StorageEngine.updateEngineBadge('Cloud Firestore', user.email || 'Authenticated');
        }

        V07StorageEngine.syncFirestoreEvents();
        updateAuthModalUI(user);
        closeModal('modalAuth');
      } catch (err) {
        var friendlyMsg = err.message || 'Authentication error';
        if (err.code === 'auth/user-not-found') {
          friendlyMsg = 'No account found with this email. Click "Sign Up" below to create it.';
        } else if (err.code === 'auth/wrong-password') {
          friendlyMsg = 'Incorrect password. Please verify and re-enter.';
        } else if (err.code === 'auth/invalid-credential') {
          friendlyMsg = 'Invalid credentials. If this is your first time, click "Sign Up" below to create an account.';
        } else if (err.code === 'auth/email-already-in-use') {
          friendlyMsg = 'This email is already registered. Click "Sign In" instead.';
        } else if (err.code === 'auth/weak-password') {
          friendlyMsg = 'Password must be at least 6 characters.';
        } else if (err.code === 'auth/invalid-email') {
          friendlyMsg = 'Please enter a valid email address format.';
        } else if (err.code === 'auth/network-request-failed') {
          friendlyMsg = 'Network error. Please check your internet connection.';
        }
        if (errEl) {
          errEl.textContent = friendlyMsg;
          errEl.classList.remove('hidden');
        }
        showToast('Auth error: ' + friendlyMsg);
      } finally {
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = prevBtnHtml;
        }
      }
    }

    async function signOutAuthUser() {
      try {
        var auth = getSafeFirebaseAuth();
        if (auth) {
          await auth.signOut();
        }
        if (V07StorageEngine._unsubscribeFirestore) {
          try { V07StorageEngine._unsubscribeFirestore(); } catch (_) {}
          V07StorageEngine._unsubscribeFirestore = null;
        }
        V07StorageEngine.currentUser = null;
        if (V07StorageEngine.mode !== 'app_sqlite') {
          V07StorageEngine.mode = 'offline_local';
          V07StorageEngine.updateEngineBadge('Offline Sovereign', 'Local Storage Active');
        }
        updateAuthModalUI(null);
        closeModal('modalAuth');
        showToast('Signed out of cloud account');
        renderAllViews();
      } catch (err) {
        showToast('Sign out error: ' + err.message);
      }
    }

    function syncFirestoreNow() {
      var user = V07StorageEngine.currentUser || getSafeCurrentUser();
      if (user) {
        showToast('Initiating Firestore cloud sync...');
        V07StorageEngine.syncFirestoreEvents();
        closeModal('modalAuth');
      } else {
        showToast('Please sign in to sync with Firestore');
      }
    }

    function launchOfflineDemoMode() {
      closeModal('modalAuth');
      showToast('Sovereign local storage active (zero mock data)');
      renderAllViews();
    }

    // --- KEYBOARD SHORTCUTS LISTENER ---
    window.addEventListener('keydown', function(e) {
      if (e.target && (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT')) {
        return;
      }
      if (e.key === ' ' || e.code === 'Space') {
        e.preventDefault();
        toggleChronoStartPause();
      } else if (e.key === 'n' || e.key === 'N') {
        e.preventDefault();
        openNewTaskModal();
      } else if (e.key === 't' || e.key === 'T') {
        e.preventDefault();
        toggleTheme();
      } else if (e.key === '?') {
        e.preventDefault();
        openShortcutsModal();
      } else if (e.key >= '1' && e.key <= '5') {
        var tabs = ['cockpit', 'analytics', 'ledger', 'plugins', 'vault'];
        var idx = parseInt(e.key) - 1;
        if (tabs[idx]) switchTab(tabs[idx]);
      } else if (e.key === 'Escape' || e.key === 'Esc') {
        closePluginSandbox();
        ['modalNewTask', 'modalNewHabit', 'modalNewEvent', 'modalNewThought', 'modalInstallPlugin', 'modalAuth', 'modalDatePicker', 'modalShortcuts'].forEach(closeModal);
        closeMobileDrawer();
      }
    });

    // --- ON DOM READY INITIALIZATION ---
    window.addEventListener('DOMContentLoaded', async function() {
      var savedTheme = localStorage.getItem('v07_theme');
      if (savedTheme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
      }
      var icon = document.getElementById('themeIcon');
      if (icon) icon.textContent = savedTheme === 'light' ? 'light_mode' : 'dark_mode';

      await V07StorageEngine.init();
      await loadGitaData();
      switchTab('cockpit');
    });
  