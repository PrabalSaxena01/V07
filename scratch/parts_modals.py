"""
parts_modals.py
Contains all interactive modals:
1. New Task Modal (Task Obligations)
2. New Habit Modal (Habit Cadence)
3. New Event Modal (Ledger Entries)
4. New Thought Modal (Contemplation & Insight Capture)
5. Install Plugin Modal (V3 Plugin Platform Extensibility)
6. Sandboxed Plugin Runner Modal (Iframe Viewport with Controls & Settings)
7. Auth & Cloud Sync Modal (Firebase Auth, Account State, Firestore Sync)
8. Date Picker Modal (Day tuner)
9. Keyboard Shortcuts Modal (?)
10. Mobile Drawer (Slide-out navigation for mobile screens)
11. Toast Notification Container
"""

MODALS_HTML = r'''
  <!-- ==========================================================================
       MODAL 1: NEW TASK MODAL
       ========================================================================== -->
  <div id="modalNewTask" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">add_task</span>
          <h3 class="text-sm font-bold text-[var(--text)]">New Task Obligation</h3>
        </div>
        <button onclick="closeModal('modalNewTask')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <div class="flex flex-col gap-3">
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Task Title</label>
          <input type="text" id="newTaskTitle" placeholder="Describe the obligation..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Priority</label>
            <select id="newTaskPriority" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none">
              <option value="high">High (Urgent)</option>
              <option value="medium" selected>Medium</option>
              <option value="low">Low</option>
            </select>
          </div>
          <div>
            <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Target Date</label>
            <input type="date" id="newTaskDeadline" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs font-mono text-[var(--text)] focus:outline-none">
          </div>
        </div>
      </div>
      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="closeModal('modalNewTask')" class="px-3.5 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Cancel</button>
        <button onclick="submitNewTask()" class="px-4 py-1.5 rounded-lg bg-[var(--primary)] text-xs font-bold text-[var(--primary-text)] hover:bg-[var(--primary-hover)] btn-press">Save Obligation</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 2: NEW HABIT MODAL
       ========================================================================== -->
  <div id="modalNewHabit" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px] text-[var(--amber)]">local_fire_department</span>
          <h3 class="text-sm font-bold text-[var(--text)]">Add Habit Cadence</h3>
        </div>
        <button onclick="closeModal('modalNewHabit')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <div class="flex flex-col gap-3">
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Habit Name</label>
          <input type="text" id="newHabitTitle" placeholder="e.g. 20m Morning Meditation, 5km Run..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--amber)]">
        </div>
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Icon Glyph</label>
          <select id="newHabitIcon" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none">
            <option value="local_fire_department">🔥 Fire / Consistency</option>
            <option value="fitness_center">🏋️ Fitness / Athletics</option>
            <option value="menu_book">📖 Reading / Contemplation</option>
            <option value="self_improvement">🧘 Meditation / Mindfulness</option>
            <option value="code">💻 Coding / Craft</option>
            <option value="water_drop">💧 Hydration / Health</option>
          </select>
        </div>
      </div>
      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="closeModal('modalNewHabit')" class="px-3.5 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Cancel</button>
        <button onclick="submitNewHabit()" class="px-4 py-1.5 rounded-lg bg-[var(--primary)] text-xs font-bold text-[var(--primary-text)] hover:bg-[var(--primary-hover)] btn-press">Save Habit</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 2B: LOG SCREEN TIME & DIGITAL VITALS MODAL
       ========================================================================== -->
  <div id="modalLogScreenTime" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">smartphone</span>
          <h3 class="text-sm font-bold text-[var(--text)]">Log Screen Time & Vitals</h3>
        </div>
        <button onclick="closeModal('modalLogScreenTime')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <p class="text-xs text-[var(--text-muted)] leading-relaxed">
        Record today's device screen time manually when offline or awaiting background sync from Android companion.
      </p>
      <div class="flex flex-col gap-3">
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs font-semibold text-[var(--text-secondary)]">Hours</label>
            <input type="number" id="screenTimeHoursInput" min="0" max="24" value="2" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs font-mono text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
          </div>
          <div>
            <label class="text-xs font-semibold text-[var(--text-secondary)]">Minutes</label>
            <input type="number" id="screenTimeMinutesInput" min="0" max="59" value="30" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs font-mono text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold text-[var(--text-secondary)]">Top Application (Optional)</label>
          <input type="text" id="screenTimeTopAppInput" placeholder="e.g. Chrome, YouTube, V07..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
        </div>
        <div>
          <label class="text-xs font-semibold text-[var(--text-secondary)]">Device / Notes (Optional)</label>
          <input type="text" id="screenTimeNotesInput" placeholder="e.g. Android Phone, Tablet..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
        </div>
      </div>
      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="closeModal('modalLogScreenTime')" class="px-3.5 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Cancel</button>
        <button onclick="submitManualScreenTime()" class="px-4 py-1.5 rounded-lg bg-[var(--primary)] text-xs font-bold text-[var(--primary-text)] hover:bg-[var(--primary-hover)] btn-press">Save Telemetry</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 3: NEW EVENT / LOG MODAL
       ========================================================================== -->
  <div id="modalNewEvent" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">history_edu</span>
          <h3 class="text-sm font-bold text-[var(--text)]">Log Activity to Sovereign Ledger</h3>
        </div>
        <button onclick="closeModal('modalNewEvent')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <div class="flex flex-col gap-3">
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Category</label>
          <select id="newEventCategory" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none">
            <option value="session">Focus Session</option>
            <option value="tasks">Task Completion</option>
            <option value="mood">Mood / Vitals</option>
            <option value="running">Athletics / Run</option>
            <option value="thoughts">Insight / Reflection</option>
            <option value="plugin">Custom Plugin Ledger</option>
          </select>
        </div>
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Title / Summary</label>
          <input type="text" id="newEventTitle" placeholder="Summary of event..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
        </div>
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Extended Notes (Optional)</label>
          <textarea id="newEventContent" rows="3" placeholder="Context, thoughts, or telemetry..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]"></textarea>
        </div>
      </div>
      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="closeModal('modalNewEvent')" class="px-3.5 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Cancel</button>
        <button onclick="submitNewEvent()" class="px-4 py-1.5 rounded-lg bg-[var(--primary)] text-xs font-bold text-[var(--primary-text)] hover:bg-[var(--primary-hover)] btn-press">Commit to Ledger</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 3B: NEW THOUGHT / REFLECTION MODAL
       ========================================================================== -->
  <div id="modalNewThought" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px] text-[var(--violet)]">psychology</span>
          <h3 class="text-sm font-bold text-[var(--text)]">Capture Sovereign Thought</h3>
        </div>
        <button onclick="closeModal('modalNewThought')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <div class="flex flex-col gap-3">
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Topic / Keyword</label>
          <input type="text" id="newThoughtTopic" placeholder="e.g. Architectural Principle, Deep Reflection..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--violet)]">
        </div>
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Thought Content</label>
          <textarea id="newThoughtContent" rows="4" placeholder="Write your contemplative thought..." class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--violet)]"></textarea>
        </div>
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Tags (Optional)</label>
          <input type="text" id="newThoughtTags" placeholder="e.g. wisdom, architecture, clarity" class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none">
        </div>
      </div>
      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="closeModal('modalNewThought')" class="px-3.5 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Cancel</button>
        <button onclick="submitNewThought()" class="px-4 py-1.5 rounded-lg bg-[var(--violet)] text-xs font-bold text-white hover:opacity-90 btn-press">Commit Thought</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 4: INSTALL CUSTOM PLUGIN (Platform Extensibility)
       ========================================================================== -->
  <div id="modalInstallPlugin" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-lg bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 sm:p-6 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px] text-[var(--amber)]">extension</span>
          <h3 class="text-sm font-bold text-[var(--text)]">Install Plugin Package</h3>
        </div>
        <button onclick="closeModal('modalInstallPlugin')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <p class="text-xs text-[var(--text-muted)]">
        Install any plugin adhering to the V07 Platform specification (v3.0). Paste the manifest JSON below or provide an endpoint URL.
      </p>

      <div class="flex flex-col gap-3">
        <div>
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Source Format</label>
          <div class="flex gap-2 mt-1">
            <button onclick="setInstallMode('manifest')" id="installModeManifestBtn" class="flex-1 py-1.5 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold btn-press">Manifest JSON</button>
            <button onclick="setInstallMode('url')" id="installModeUrlBtn" class="flex-1 py-1.5 rounded-lg bg-[var(--surface-alt)] text-[var(--text-secondary)] text-xs font-bold btn-press">Hosted URL</button>
          </div>
        </div>

        <div id="installManifestSection" class="flex flex-col gap-1">
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Manifest JSON Specification</label>
          <textarea id="pluginManifestInput" rows="7" placeholder='{
  "id": "com.custom.counter",
  "name": "Focus Counter",
  "version": "1.0.0",
  "author": "Community",
  "description": "Custom focus counter with persistent SQLite/Firestore sync",
  "permissions": ["storage", "ledger"],
  "entry": "plugins/v07-template/index.html"
}' class="w-full px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] font-mono text-[11px] text-[var(--text)] focus:outline-none focus:border-[var(--primary)]"></textarea>
        </div>

        <div id="installUrlSection" class="hidden flex-col gap-1">
          <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Plugin Bundle URL</label>
          <input type="url" id="pluginUrlInput" placeholder="https://example.com/plugins/my-plugin/manifest.json" class="w-full px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
        </div>
      </div>

      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="closeModal('modalInstallPlugin')" class="px-3.5 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Cancel</button>
        <button onclick="submitInstallPlugin()" class="px-4 py-1.5 rounded-lg bg-[var(--primary)] text-xs font-bold text-[var(--primary-text)] hover:bg-[var(--primary-hover)] btn-press">Validate & Install</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 5: SANDBOXED PLUGIN RUNNER (Impeccable Plugin Viewport)
       ========================================================================== -->
  <div id="modalPluginSandbox" class="hidden fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-2 sm:p-4">
    <div class="w-full max-w-5xl h-[92vh] bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-2xl flex flex-col overflow-hidden fade-in">
      <!-- Impeccable Viewport Header -->
      <div class="h-13 px-4 sm:px-6 border-b border-[var(--border)] flex items-center justify-between bg-[var(--surface-alt)] shrink-0 gap-3">
        <!-- Left: Plugin Identity -->
        <div class="flex items-center gap-3 min-w-0">
          <div class="w-7 h-7 rounded-md bg-[var(--surface)] border border-[var(--border)] flex items-center justify-center shrink-0 shadow-xs">
            <span id="sandboxIcon" class="material-symbols-outlined text-[17px] text-[var(--primary)]">extension</span>
          </div>
          <div class="flex flex-col min-w-0">
            <div class="flex items-center gap-2">
              <span id="sandboxTitle" class="text-xs sm:text-sm font-bold text-[var(--text)] truncate">Sandboxed Plugin</span>
              <span id="sandboxVersionBadge" class="hidden sm:inline-flex px-1.5 py-0.2 rounded text-[9px] font-mono font-bold bg-[var(--primary-dim)] text-[var(--primary)]">v3.0</span>
              <span id="sandboxViewModeBadge" class="px-1.5 py-0.2 rounded text-[9px] font-mono uppercase bg-[var(--surface)] text-[var(--text-muted)] border border-[var(--border)]">Main App</span>
            </div>
            <span id="sandboxIdBadge" class="text-[10px] font-mono text-[var(--text-muted)] truncate">com.v07.plugin</span>
          </div>
        </div>

        <!-- Right: Viewport Controls -->
        <div class="flex items-center gap-1.5 shrink-0">
          <!-- Reload Plugin Frame -->
          <button onclick="reloadPluginSandbox()" class="w-8 h-8 flex items-center justify-center rounded-lg bg-[var(--surface)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] hover:border-[var(--primary)] transition-colors btn-press" title="Reload Plugin">
            <span id="sandboxReloadIcon" class="material-symbols-outlined text-[16px]">refresh</span>
          </button>

          <!-- Toggle Settings / App View -->
          <button onclick="toggleActivePluginSettingsView()" id="sandboxSettingsBtn" class="w-8 h-8 flex items-center justify-center rounded-lg bg-[var(--surface)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] hover:border-[var(--amber)] transition-colors btn-press" title="Plugin Settings">
            <span class="material-symbols-outlined text-[16px]">settings</span>
          </button>

          <!-- Pop out to dedicated new tab -->
          <button onclick="popoutActivePlugin()" class="w-8 h-8 flex items-center justify-center rounded-lg bg-[var(--surface)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] hover:border-[var(--primary)] transition-colors btn-press" title="Open in Dedicated Tab">
            <span class="material-symbols-outlined text-[16px]">open_in_new</span>
          </button>

          <!-- Close Viewport -->
          <button onclick="closePluginSandbox()" class="w-8 h-8 flex items-center justify-center rounded-lg bg-[var(--surface)] border border-[var(--border)] text-[var(--text-muted)] hover:text-[var(--rose)] hover:bg-[var(--rose-dim)] transition-colors btn-press ml-1" title="Close Viewport">
            <span class="material-symbols-outlined text-[17px]">close</span>
          </button>
        </div>
      </div>

      <!-- Sandbox Iframe Container with Loading Indicator -->
      <div class="flex-1 w-full relative bg-[var(--bg)] overflow-hidden">
        <div id="sandboxLoadingOverlay" class="hidden absolute inset-0 bg-[var(--bg)]/90 backdrop-blur-xs z-10 flex flex-col items-center justify-center gap-2">
          <div class="w-5 h-5 border-2 border-[var(--primary)] border-t-transparent rounded-full animate-spin"></div>
          <span class="text-xs font-mono text-[var(--text-muted)]">Loading sandboxed environment...</span>
        </div>
        <iframe id="pluginSandboxIframe" sandbox="allow-scripts allow-forms allow-same-origin allow-modals allow-popups allow-downloads" class="w-full h-full border-none" src="about:blank"></iframe>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 6: AUTH & CLOUD SYNC SCREEN
       ========================================================================== -->
  <div id="modalAuth" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-md flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-2xl p-6 sm:p-7 flex flex-col gap-5 text-center fade-in">
      
      <!-- LOGGED OUT VIEW (Sign In / Sign Up) -->
      <div id="authLoggedOutView" class="flex flex-col gap-5">
        <div class="w-11 h-11 rounded-lg bg-[var(--primary-dim)] text-[var(--primary)] flex items-center justify-center mx-auto border border-[var(--primary)]/20 shadow-xs">
          <span class="material-symbols-outlined text-[22px]">shield</span>
        </div>
        <div>
          <h2 id="authModalTitle" class="text-base font-bold text-[var(--text)]">Sign In to V07 Cloud</h2>
          <p id="authModalSub" class="text-xs text-[var(--text-muted)] mt-1">Single source of truth (Firestore) for Android app & Web Companion.</p>
        </div>

        <div id="authErrorMessage" class="hidden text-xs p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 text-left font-medium"></div>

        <form onsubmit="event.preventDefault(); submitAuthLogin();" class="flex flex-col gap-3 text-left">
          <div>
            <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Email Address</label>
            <input type="email" id="authEmail" placeholder="prabal.pks@gmail.com" autocomplete="email" required class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
          </div>
          <div>
            <label class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Password</label>
            <input type="password" id="authPassword" placeholder="••••••••••••" autocomplete="current-password" required class="w-full mt-1 px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)]">
          </div>

          <div class="flex flex-col gap-2 pt-2 text-center">
            <button type="submit" id="authSubmitBtn" class="w-full py-2.5 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold hover:bg-[var(--primary-hover)] shadow-xs btn-press flex items-center justify-center gap-2">
              <span class="material-symbols-outlined text-[17px]">login</span>
              <span>Sign In</span>
            </button>
            <button type="button" id="authToggleModeBtn" onclick="toggleAuthMode()" class="text-xs text-[var(--primary)] hover:underline font-semibold py-1">
              Don't have an account? Sign Up
            </button>
            <button type="button" onclick="launchOfflineDemoMode()" class="w-full py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs font-semibold text-[var(--text-muted)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press mt-1">
              Continue Sovereign Offline Store
            </button>
          </div>
        </form>
        <button type="button" onclick="closeModal('modalAuth')" class="text-xs text-[var(--text-muted)] hover:underline">Continue as Guest</button>
      </div>

      <!-- LOGGED IN VIEW (Active Account / Sync / Sign Out) -->
      <div id="authLoggedInView" class="hidden flex flex-col gap-5">
        <div class="w-11 h-11 rounded-lg bg-[var(--emerald-dim)] text-[var(--emerald)] flex items-center justify-center mx-auto border border-[var(--emerald)]/20 shadow-xs">
          <span class="material-symbols-outlined text-[22px]">verified_user</span>
        </div>
        <div>
          <h2 class="text-base font-bold text-[var(--text)]">Account Connected</h2>
          <p class="text-xs text-[var(--text-muted)] mt-1">Single source of truth active across Android app and Web companion.</p>
        </div>

        <div class="p-3.5 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-left flex flex-col gap-2">
          <div class="flex items-center justify-between">
            <span class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">Signed In Email</span>
            <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[var(--emerald-dim)] text-[var(--emerald)] font-bold border border-[var(--emerald)]/20">Active</span>
          </div>
          <div id="authCurrentEmail" class="text-xs font-mono font-bold text-[var(--text)] truncate">user@v07-dashboard.firebaseapp.com</div>
          <div class="text-[10px] text-[var(--text-muted)] font-mono truncate">UID: <span id="authCurrentUid">...</span></div>
          <div class="text-[10px] text-[var(--emerald)] flex items-center gap-1.5 pt-1">
            <span class="w-1.5 h-1.5 rounded-full bg-[var(--emerald)] animate-pulse"></span>
            <span>Firestore <code class="font-mono">users/{uid}/events</code> synced</span>
          </div>
        </div>

        <div class="flex flex-col gap-2">
          <button type="button" onclick="syncFirestoreNow()" class="w-full py-2.5 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold hover:bg-[var(--primary-hover)] shadow-xs btn-press flex items-center justify-center gap-2">
            <span class="material-symbols-outlined text-[17px]">sync</span>
            <span>Sync Firestore Now</span>
          </button>
          <button type="button" onclick="signOutAuthUser()" class="w-full py-2 rounded-lg bg-[var(--rose-dim)] border border-[var(--rose)]/20 text-[var(--rose)] hover:bg-[var(--rose)] hover:text-white text-xs font-semibold btn-press flex items-center justify-center gap-2 transition-colors">
            <span class="material-symbols-outlined text-[17px]">logout</span>
            <span>Sign Out of Account</span>
          </button>
        </div>
        <button type="button" onclick="closeModal('modalAuth')" class="text-xs text-[var(--text-muted)] hover:underline">Close</button>
      </div>

    </div>
  </div>

  <!-- ==========================================================================
       MODAL 7: DATE PICKER MODAL
       ========================================================================== -->
  <div id="modalDatePicker" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-xs bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <h3 class="text-xs font-bold text-[var(--text)] font-mono uppercase">Jump to Date</h3>
        <button onclick="closeModal('modalDatePicker')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[17px]">close</span>
        </button>
      </div>
      <input type="date" id="datePickerInput" class="w-full px-3 py-2 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs font-mono text-[var(--text)] focus:outline-none">
      <div class="flex justify-between gap-2 pt-2 border-t border-[var(--border)]">
        <button onclick="resetToToday(); closeModal('modalDatePicker')" class="px-3 py-1.5 rounded-lg bg-[var(--surface-alt)] text-xs font-semibold text-[var(--primary)] btn-press">Today</button>
        <button onclick="applyPickedDate()" class="px-4 py-1.5 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold btn-press">Select</button>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 8: KEYBOARD SHORTCUTS MODAL
       ========================================================================== -->
  <div id="modalShortcuts" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="w-full max-w-sm bg-[var(--surface)] border border-[var(--border)] rounded-xl shadow-xl p-5 flex flex-col gap-4 fade-in">
      <div class="flex items-center justify-between">
        <h3 class="text-xs font-bold text-[var(--text)] font-mono uppercase">Keyboard Shortcuts</h3>
        <button onclick="closeModal('modalShortcuts')" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
          <span class="material-symbols-outlined text-[17px]">close</span>
        </button>
      </div>
      <div class="flex flex-col gap-2 text-xs">
        <div class="flex justify-between py-1.5 border-b border-[var(--border)]">
          <span class="text-[var(--text-secondary)]">Start / Pause Focus</span>
          <kbd class="px-1.5 py-0.5 rounded bg-[var(--surface-alt)] border border-[var(--border)] font-mono text-[10px]">Space</kbd>
        </div>
        <div class="flex justify-between py-1.5 border-b border-[var(--border)]">
          <span class="text-[var(--text-secondary)]">New Task Obligation</span>
          <kbd class="px-1.5 py-0.5 rounded bg-[var(--surface-alt)] border border-[var(--border)] font-mono text-[10px]">N</kbd>
        </div>
        <div class="flex justify-between py-1.5 border-b border-[var(--border)]">
          <span class="text-[var(--text-secondary)]">Toggle Theme (Dark / Light)</span>
          <kbd class="px-1.5 py-0.5 rounded bg-[var(--surface-alt)] border border-[var(--border)] font-mono text-[10px]">T</kbd>
        </div>
        <div class="flex justify-between py-1.5 border-b border-[var(--border)]">
          <span class="text-[var(--text-secondary)]">Jump to Core Pillars</span>
          <kbd class="px-1.5 py-0.5 rounded bg-[var(--surface-alt)] border border-[var(--border)] font-mono text-[10px]">1 - 5</kbd>
        </div>
        <div class="flex justify-between py-1.5">
          <span class="text-[var(--text-secondary)]">Close Active Modal / Sandbox</span>
          <kbd class="px-1.5 py-0.5 rounded bg-[var(--surface-alt)] border border-[var(--border)] font-mono text-[10px]">Esc</kbd>
        </div>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       MODAL 9: MOBILE TOOLS DRAWER (Touch-Ergonomic Drawer)
       ========================================================================== -->
  <div id="mobileDrawer" class="hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex justify-end">
    <div class="w-72 h-full bg-[var(--surface)] border-l border-[var(--border)] p-4 flex flex-col justify-between overflow-y-auto fade-in">
      <div class="flex flex-col gap-4">
        <div class="flex items-center justify-between pb-3 border-b border-[var(--border)]">
          <span class="text-xs font-mono font-bold uppercase text-[var(--text)]">Navigation Sanctuary</span>
          <button onclick="closeMobileDrawer()" class="text-[var(--text-muted)] hover:text-[var(--text)] btn-press">
            <span class="material-symbols-outlined text-[19px]">close</span>
          </button>
        </div>
        <div class="flex flex-col gap-4">
          <!-- Core System Pillars -->
          <div class="flex flex-col gap-1">
            <span class="text-[10px] font-mono font-bold tracking-wider uppercase text-[var(--text-muted)] px-3 mb-1">Core Foundations</span>
            <button onclick="switchTab('cockpit'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[19px] text-[var(--primary)]">spa</span>
              <span>Home Sanctuary</span>
            </button>
            <button onclick="switchTab('plugins'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[19px] text-[var(--amber)]">grid_view</span>
              <span>Tools & Modules</span>
            </button>
            <button onclick="switchTab('ledger'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[19px] text-[var(--violet)]">history_edu</span>
              <span>Sovereign Ledger</span>
            </button>
            <button onclick="switchTab('analytics'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[19px] text-[var(--cyan)]">insights</span>
              <span>Data & Analytics</span>
            </button>
            <button onclick="switchTab('vault'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[19px] text-[var(--text-muted)]">settings</span>
              <span>Settings & Vault</span>
            </button>
          </div>

          <!-- Installed Tools & Plugins -->
          <div class="flex flex-col gap-1 border-t border-[var(--border)] pt-3">
            <span class="text-[10px] font-mono font-bold tracking-wider uppercase text-[var(--text-muted)] px-3 mb-1">Installed Utilities</span>
            <button onclick="launchPluginSandbox('com.v07.reader'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[#A78BFA]">menu_book</span>
              <span>Reading Companion</span>
            </button>
            <button onclick="launchPluginSandbox('com.v07.habits'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[#F59E0B]">local_fire_department</span>
              <span>Habit Cadence</span>
            </button>
            <button onclick="launchPluginSandbox('com.v07.tasks'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[#38BDF8]">checklist</span>
              <span>Tasks Directives</span>
            </button>
            <button onclick="launchPluginSandbox('com.v07.timetracker'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[var(--emerald)]">hourglass_empty</span>
              <span>Time Tracker</span>
            </button>
            <button onclick="launchPluginSandbox('com.v07.gamify'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[var(--amber)]">military_tech</span>
              <span>Gamify & Quests</span>
            </button>
            <button onclick="launchPluginSandbox('com.v07.chronosynth'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[#4ADE80]">insights</span>
              <span>Weekly Digest</span>
            </button>
            <button onclick="launchPluginSandbox('com.v07.vitals'); closeMobileDrawer()" class="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px] text-[var(--rose)]">psychology</span>
              <span>Mood & Vitals</span>
            </button>
          </div>
        </div>
      </div>
      <div class="pt-3 border-t border-[var(--border)] text-center">
        <span class="text-[10px] font-mono text-[var(--text-muted)]">V07 Sanctuary · Sovereign v3</span>
      </div>
    </div>
  </div>

  <!-- ==========================================================================
       TOAST NOTIFICATION ENGINE (Ergonomic Float)
       ========================================================================== -->
  <div id="toastContainer" class="fixed bottom-16 lg:bottom-6 right-4 z-50 flex flex-col gap-2 pointer-events-none">
    <!-- Populated by showToast() -->
  </div>
'''
