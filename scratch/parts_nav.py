"""
parts_nav.py
Top App Bar HUD, Desktop Navigation Rail, and Mobile Navigation Bar.
Designed from scratch with high information density, clean typography, and 1-click utility access.
"""

NAV_HTML = r'''
<body class="flex flex-col min-h-screen bg-[var(--bg)] text-[var(--text)] antialiased select-none selection:bg-[var(--primary)] selection:text-[var(--primary-text)]">

  <!-- ==========================================================================
       TOP APP BAR HUD (56px Height, Razor 1px Hairline Border, Zero Blur Blobs)
       Matching V07 Android Native Navigation and Iconography
       ========================================================================== -->
  <header class="sticky top-0 z-40 h-14 w-full max-w-full bg-[var(--surface-glass)] backdrop-blur-md border-b border-[var(--border)] px-3 sm:px-6 flex items-center justify-between gap-2 overflow-hidden">
    <!-- Left: Brand Beacon & Environment Status (App Sprout Icon) -->
    <div class="flex items-center gap-2 sm:gap-3 min-w-0">
      <div class="flex items-center gap-2 sm:gap-2.5 min-w-0">
        <div class="w-8 h-8 rounded-xl bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center text-[var(--primary)] font-bold text-xs shadow-xs shrink-0">
          <span class="material-symbols-outlined text-[20px] text-[var(--primary)]">spa</span>
        </div>
        <div class="flex flex-col min-w-0">
          <div class="flex items-center gap-1.5 min-w-0">
            <span class="text-xs sm:text-sm font-bold tracking-tight text-[var(--text)] truncate">V07 Sanctuary</span>
            <!-- Environment Badge: Android SQLite vs Cloud Firestore vs Local -->
            <span id="envBadge" class="max-md:hidden inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono font-semibold uppercase bg-[var(--surface-alt)] text-[var(--text-secondary)] border border-[var(--border)] shrink-0">
              <span id="envBeacon" class="w-1.5 h-1.5 rounded-full bg-[var(--emerald)] pulse-online"></span>
              <span id="envLabel">CONNECTING...</span>
            </span>
          </div>
          <span class="text-[10px] text-[var(--text-muted)] truncate hidden sm:block">Calm sanctuary & sovereign workspace</span>
        </div>
      </div>
    </div>

    <!-- Center: Global Focus Chronometer Ticker (Visible across all tabs) -->
    <div id="headerChronoTicker" onclick="switchTab('cockpit')" class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--emerald)] cursor-pointer transition-colors btn-press shrink-0" title="Open Focus Controls">
      <span class="w-2 h-2 rounded-full bg-[var(--emerald)] pulse-online"></span>
      <span class="text-[11px] font-semibold text-[var(--text-secondary)]">FOCUS:</span>
      <span id="headerTimerVal" class="text-xs font-mono font-bold text-[var(--emerald)] tabular-nums">00:00</span>
      <span id="headerTimerActivity" class="text-[11px] text-[var(--text-muted)] max-w-[80px] truncate hidden md:inline">Deep Work</span>
    </div>

    <!-- Right Controls: Date Tuner, Cloud Sync, Theme Switcher, Notifications & Profile -->
    <div class="flex items-center gap-1.5 sm:gap-2 shrink-0">
      <!-- Date Tuner Bar -->
      <div class="flex items-center bg-[var(--surface-alt)] border border-[var(--border)] rounded-full p-0.5 text-xs shrink-0">
        <button onclick="shiftDate(-1)" class="hidden sm:flex w-7 h-7 items-center justify-center rounded-full text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press" title="Previous Day">
          <span class="material-symbols-outlined text-[16px]">chevron_left</span>
        </button>
        <button onclick="openDatePickerModal()" class="px-2 h-7 flex items-center gap-1 text-[var(--text)] font-semibold hover:bg-[var(--surface-hover)] rounded-full btn-press">
          <span class="material-symbols-outlined text-[14px] text-[var(--text-muted)]">calendar_today</span>
          <span id="dateDisplayLabel" class="tabular-nums font-mono text-xs">Today</span>
        </button>
        <button onclick="shiftDate(1)" class="hidden sm:flex w-7 h-7 items-center justify-center rounded-full text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] btn-press" title="Next Day">
          <span class="material-symbols-outlined text-[16px]">chevron_right</span>
        </button>
      </div>

      <!-- Quick 'Today' Jump Button (Hidden if already today) -->
      <button id="todayJumpBtn" onclick="resetToToday()" class="hidden px-2.5 h-8 items-center rounded-full bg-[var(--primary-dim)] text-[var(--primary)] hover:bg-[var(--primary)] hover:text-[var(--primary-text)] text-xs font-bold border border-[var(--primary)]/20 transition-all btn-press">
        Today
      </button>

      <!-- Sync Button -->
      <button onclick="triggerManualSync()" id="syncBtn" class="w-8 h-8 flex items-center justify-center rounded-full bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] hover:border-[var(--primary)] btn-press" title="Synchronize Database">
        <span id="syncIcon" class="material-symbols-outlined text-[17px]">sync</span>
      </button>

      <!-- Theme Switcher -->
      <button onclick="toggleTheme()" class="w-8 h-8 flex items-center justify-center rounded-full bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] btn-press" title="Toggle Theme (Dark / Light)">
        <span id="themeIcon" class="material-symbols-outlined text-[17px]">dark_mode</span>
      </button>

      <!-- App-Style Profile Avatar Beacon (Matches V07 Android Home Header) -->
      <button onclick="switchTab('vault')" class="relative w-8 h-8 rounded-full border border-[var(--border)] bg-[var(--surface-alt)] hover:border-[var(--primary)] flex items-center justify-center text-xs font-bold text-[var(--text)] font-mono cursor-pointer btn-press shrink-0" title="Account & Sovereign Vault">
        <span>S</span>
        <span class="absolute bottom-0 right-0 w-2 h-2 rounded-full bg-[var(--emerald)] border-2 border-[var(--surface)]"></span>
      </button>

      <!-- Shortcuts (?) -->
      <button onclick="openShortcutsModal()" class="hidden md:flex w-8 h-8 items-center justify-center rounded-full bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-muted)] hover:text-[var(--text)] text-xs font-mono font-bold btn-press" title="Keyboard Shortcuts (?)">
        ?
      </button>
    </div>
  </header>

  <!-- ==========================================================================
       MAIN APP SHELL (Desktop Sidebar + Dynamic Content Viewport)
       ========================================================================== -->
  <div class="flex-1 flex w-full max-w-[1600px] mx-auto overflow-hidden">

    <!-- DESKTOP SIDEBAR NAVIGATION (Width: 240px, Visible >= 1024px) -->
    <aside class="hidden lg:flex flex-col w-[240px] shrink-0 border-r border-[var(--border)] bg-[var(--surface)] p-3 justify-between select-none">
      <div class="flex flex-col gap-5 overflow-y-auto pr-1">

        <!-- Group 1: Core System Pillars (Aligned to V07 App Iconography) -->
        <div class="flex flex-col gap-1">
          <span class="text-[10px] font-semibold tracking-wider uppercase text-[var(--text-muted)] px-3 mb-1 font-mono">Foundations</span>
          
          <button onclick="switchTab('cockpit')" data-tab-btn="cockpit" class="nav-item active flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text)] bg-[var(--surface-hover)] transition-colors btn-press">
            <div class="flex items-center gap-2.5">
              <span class="material-symbols-outlined text-[19px] text-[var(--primary)]">spa</span>
              <span>Home Sanctuary</span>
            </div>
            <span class="w-1.5 h-1.5 rounded-full bg-[var(--primary)]"></span>
          </button>

          <button onclick="switchTab('plugins')" data-tab-btn="plugins" class="nav-item flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press">
            <div class="flex items-center gap-2.5">
              <span class="material-symbols-outlined text-[19px] text-[var(--text-muted)]">grid_view</span>
              <span>Tools & Modules</span>
            </div>
            <span id="navPluginCountBadge" class="px-2 py-0.5 rounded text-[9px] font-mono font-bold bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]">7 Active</span>
          </button>

          <button onclick="switchTab('ledger')" data-tab-btn="ledger" class="nav-item flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press">
            <div class="flex items-center gap-2.5">
              <span class="material-symbols-outlined text-[19px] text-[var(--text-muted)]">history_edu</span>
              <span>Sovereign Ledger</span>
            </div>
            <span id="navLedgerCount" class="text-[10px] font-mono text-[var(--text-muted)]">0</span>
          </button>

          <button onclick="switchTab('analytics')" data-tab-btn="analytics" class="nav-item flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press">
            <div class="flex items-center gap-2.5">
              <span class="material-symbols-outlined text-[19px] text-[var(--text-muted)]">insights</span>
              <span>Data & Analytics</span>
            </div>
            <span class="px-2 py-0.5 rounded text-[9px] font-mono font-bold bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]">Insights</span>
          </button>

          <button onclick="switchTab('vault')" data-tab-btn="vault" class="nav-item flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press">
            <div class="flex items-center gap-2.5">
              <span class="material-symbols-outlined text-[19px] text-[var(--text-muted)]">settings</span>
              <span>Settings & Vault</span>
            </div>
            <span class="material-symbols-outlined text-[14px] text-[var(--text-muted)]">lock</span>
          </button>
        </div>

        <!-- Group 2: Installed Utilities (1-Click Launchers into Sandbox) -->
        <div class="flex flex-col gap-1">
          <div class="flex items-center justify-between px-3 mb-1">
            <span class="text-[10px] font-semibold tracking-wider uppercase text-[var(--text-muted)] font-mono">Utilities</span>
            <button onclick="switchTab('plugins')" class="text-[10px] font-bold text-[var(--primary)] hover:underline">Manage</button>
          </div>

          <button onclick="launchPluginSandbox('com.v07.reader')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Book Collection & Reading Tracker">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">menu_book</span>
              </div>
              <span class="truncate">Reading Companion</span>
            </div>
            <span class="material-symbols-outlined text-[13px] text-[var(--text-muted)] opacity-50">arrow_forward</span>
          </button>

          <button onclick="launchPluginSandbox('com.v07.habits')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Habit Cadence">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">local_fire_department</span>
              </div>
              <span class="truncate">Habit Cadence</span>
            </div>
            <span id="navHabitStreak" class="text-[10px] font-mono font-bold text-[var(--text-secondary)]">0d</span>
          </button>

          <button onclick="launchPluginSandbox('com.v07.tasks')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Tasks Directives">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">checklist</span>
              </div>
              <span class="truncate">Tasks Directives</span>
            </div>
            <span id="navTaskCount" class="px-2 py-0.2 rounded text-[10px] font-mono font-bold bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]">0</span>
          </button>

          <button onclick="launchPluginSandbox('com.v07.timetracker')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Time Tracker & Countdowns">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">hourglass_empty</span>
              </div>
              <span class="truncate">Time Tracker</span>
            </div>
            <span class="material-symbols-outlined text-[13px] text-[var(--text-muted)] opacity-50">arrow_forward</span>
          </button>

          <button onclick="launchPluginSandbox('com.v07.gamify')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Gamify & Quests">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">military_tech</span>
              </div>
              <span class="truncate">Gamify & Quests</span>
            </div>
            <span id="navGamifyLevel" class="px-2 py-0.2 rounded text-[9px] font-mono font-bold bg-[var(--surface-alt)] text-[var(--text-secondary)] border border-[var(--border)]">LVL 1</span>
          </button>

          <button onclick="launchPluginSandbox('com.v07.chronosynth')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Weekly Digest">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">insights</span>
              </div>
              <span class="truncate">Weekly Digest</span>
            </div>
            <span class="material-symbols-outlined text-[13px] text-[var(--text-muted)] opacity-50">arrow_forward</span>
          </button>

          <button onclick="launchPluginSandbox('com.v07.vitals')" class="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--surface-hover)] transition-colors btn-press" title="Open Mood & Vitals">
            <div class="flex items-center gap-2.5 min-w-0">
              <div class="w-6 h-6 rounded bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">psychology</span>
              </div>
              <span class="truncate">Mood & Vitals</span>
            </div>
            <span class="material-symbols-outlined text-[13px] text-[var(--text-muted)] opacity-50">arrow_forward</span>
          </button>
        </div>

      </div>

      <!-- Sidebar Account & Storage Status Footer -->
      <div class="mt-3 pt-3 border-t border-[var(--border)] flex items-center justify-between">
        <div class="flex items-center gap-2 min-w-0">
          <div class="relative w-8 h-8 rounded-full bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center text-[var(--text)] font-mono font-bold text-xs shrink-0">
            <span>S</span>
            <span class="absolute bottom-0 right-0 w-2 h-2 rounded-full bg-[var(--emerald)] border-2 border-[var(--surface)]"></span>
          </div>
          <div class="flex flex-col min-w-0">
            <span id="userAccountLabel" class="text-xs font-bold text-[var(--text)] truncate">Local Mode</span>
            <span id="userAccountSub" class="text-[10px] text-[var(--text-muted)] truncate">Zero Data Loss</span>
          </div>
        </div>
        <button onclick="handleAuthAction()" id="authActionBtn" class="text-[11px] font-bold text-[var(--primary)] hover:underline shrink-0">
          Sign In
        </button>
      </div>
    </aside>

    <!-- MAIN SCROLLABLE CONTENT VIEWPORT (Calibrated for Zero Excessive Scroll) -->
    <main id="contentContainer" class="flex-1 min-w-0 overflow-y-auto px-3 sm:px-5 lg:px-6 py-2.5 sm:py-3 pb-20 lg:pb-3">
'''
