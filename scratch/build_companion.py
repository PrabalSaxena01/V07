"""
build_companion.py
Builds the overhauled V07 Companion Website HTML adhering to Impeccable and Taste standards.
"""
import os

HTML_CONTENT = r'''<!DOCTYPE html>
<html lang="en" class="">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
  <title>V07 Sanctuary Companion</title>

  <!-- Google Fonts & Material Symbols -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@600;700&family=JetBrains+Mono:wght@400;500;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />

  <!-- Tailwind CSS & Fallback -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    if (typeof tailwind === 'undefined') {
      document.write('<script src="tailwind.min.js"><\/script>');
    }
  </script>

  <!-- Firebase Compat SDKs (10.12.0) -->
  <script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js"></script>
  <script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-auth-compat.js"></script>
  <script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore-compat.js"></script>

  <!-- Chart.js 4.4.4 -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>

  <style>
    /* Local & Offline Font-Face Fallbacks */
    @font-face {
      font-family: 'Plus Jakarta Sans';
      font-style: normal;
      font-weight: 400 700;
      font-display: swap;
      src: url('fonts/plus-jakarta-sans-latin.woff2') format('woff2'),
           url('fonts/plus-jakarta-sans-variable.ttf') format('truetype');
    }
    @font-face {
      font-family: 'JetBrains Mono';
      font-style: normal;
      font-weight: 400 700;
      font-display: swap;
      src: url('fonts/JetBrainsMono-Regular.ttf') format('truetype'),
           url('fonts/JetBrainsMono-Medium.ttf') format('truetype');
    }
    @font-face {
      font-family: 'Material Symbols Outlined';
      font-style: normal;
      font-weight: 100 700;
      font-display: block;
      src: url('material-symbols.woff2') format('woff2'),
           url('fonts/material-symbols.woff2') format('woff2');
    }

    /* CSS Reset & Variables */
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    
    :root {
      --bg: #090d16;
      --surface: #0f172a;
      --surface-container: #17223b;
      --surface-container-low: #0f172a;
      --surface-container-high: #1e2e4f;
      --surface-container-highest: #283d64;
      --primary: #38bdf8;
      --primary-hover: #0284c7;
      --primary-container: rgba(56, 189, 248, 0.12);
      --on-primary: #04101c;
      --text: #f8fafc;
      --text-inactive: #94a3b8;
      --muted: #64748b;
      --border: rgba(255, 255, 255, 0.08);
      --border-highlight: rgba(56, 189, 248, 0.35);
      --divider: rgba(255, 255, 255, 0.05);
      --green: #34d399;
      --yellow: #fbbf24;
      --red: #f87171;
      --purple: #a78bfa;
      --accent-orange: #fb923c;
      --radius-sm: 6px;
      --radius-md: 10px;
      --radius-lg: 14px;
      --radius-xl: 18px;
      --card-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.25), 0 1px 2px -1px rgba(0, 0, 0, 0.25);
    }

    :root[data-theme="light"] {
      --bg: #f8fafc;
      --surface: #ffffff;
      --surface-container: #f1f5f9;
      --surface-container-low: #ffffff;
      --surface-container-high: #e2e8f0;
      --surface-container-highest: #cbd5e1;
      --primary: #0284c7;
      --primary-hover: #0369a1;
      --primary-container: rgba(2, 132, 199, 0.1);
      --on-primary: #ffffff;
      --text: #0f172a;
      --text-inactive: #64748b;
      --muted: #94a3b8;
      --border: rgba(0, 0, 0, 0.08);
      --border-highlight: rgba(2, 132, 199, 0.35);
      --divider: rgba(0, 0, 0, 0.05);
      --green: #059669;
      --yellow: #d97706;
      --red: #dc2626;
      --purple: #7c3aed;
      --accent-orange: #ea580c;
      --card-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.06), 0 1px 2px -1px rgba(0, 0, 0, 0.06);
    }

    body {
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      -webkit-font-smoothing: antialiased;
      -webkit-tap-highlight-color: transparent;
      user-select: none;
    }

    /* Impeccable Typography & Truncation Standards */
    .font-mono { font-family: 'JetBrains Mono', monospace !important; }
    .font-gita { font-family: 'Cinzel', serif !important; }

    .truncate-single {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .truncate-multi {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .truncate-multi-3 {
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    /* Ergonomic Touch Targets */
    .touch-target {
      min-width: 44px;
      min-height: 44px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .touch-target-sm {
      min-width: 36px;
      min-height: 36px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }

    /* Tactile Press Feedback */
    button, .btn, .card-press, .nav-item, .chip {
      transition: transform 0.08s ease, background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
      cursor: pointer;
    }
    button:active, .btn:active, .card-press:active, .nav-item:active, .chip:active {
      transform: scale(0.96);
    }

    /* Material Symbols Styling */
    .material-symbols-outlined {
      font-family: 'Material Symbols Outlined' !important;
      font-weight: normal;
      font-style: normal;
      font-size: 20px;
      line-height: 1;
      letter-spacing: normal;
      text-transform: none;
      display: inline-block;
      white-space: nowrap;
      word-wrap: normal;
      direction: ltr;
      -webkit-font-smoothing: antialiased;
    }

    /* Custom Scrollbars */
    ::-webkit-scrollbar { width: 6px; height: 6px; }
    ::-webkit-scrollbar-track { background: transparent; }
    ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 4px; }
    ::-webkit-scrollbar-thumb:hover { background: var(--text-inactive); }

    /* Animated Tickers & Indicators */
    @keyframes pulse-subtle {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.55; transform: scale(0.94); }
    }
    .pulse-dot { animation: pulse-subtle 1.8s infinite ease-in-out; }

    @keyframes pulse-pause {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.35; }
    }
    .pulse-pause { animation: pulse-pause 1.2s infinite ease-in-out; }

    /* Navigation Panel Transitions */
    .panel { display: none; }
    .panel.active { display: block; animation: panelFade 0.16s ease-out; }
    @keyframes panelFade { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }

    /* Heatmap Grid */
    .heatmap-row { display: flex; gap: 3px; margin-bottom: 3px; }
    .heatmap-cell { width: 13px; height: 13px; border-radius: 2px; background: var(--surface-container-high); transition: transform 0.1s; }
    .heatmap-cell:hover { transform: scale(1.2); }
    .heatmap-cell.l1 { background: rgba(56, 189, 248, 0.25); }
    .heatmap-cell.l2 { background: rgba(56, 189, 248, 0.50); }
    .heatmap-cell.l3 { background: rgba(56, 189, 248, 0.75); }
    .heatmap-cell.l4 { background: rgba(56, 189, 248, 1.00); }

    /* Keyboard Shortcut Tag */
    kbd {
      background: var(--surface-container-high);
      border: 1px solid var(--border);
      border-radius: 4px;
      padding: 2px 6px;
      font-family: 'JetBrains Mono', monospace;
      font-size: 11px;
      font-weight: 700;
      color: var(--primary);
    }
  </style>
</head>
<body class="bg-[var(--bg)] text-[var(--text)] antialiased min-h-screen">

  <!-- ========================================================================= -->
  <!-- 1. AUTH SCREEN (SIGN IN / SIGN UP / LOCAL DEMO MODE)                      -->
  <!-- ========================================================================= -->
  <div id="authScreen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[var(--bg)]">
    <div class="w-full max-w-[420px] bg-[var(--surface)] border border-[var(--border)] rounded-2xl p-6 sm:p-8 shadow-xl space-y-6" style="box-shadow: var(--card-shadow);">
      <!-- Logo & Title -->
      <div class="text-center space-y-2">
        <div class="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-[var(--primary-container)] border border-[var(--border-highlight)] text-[var(--primary)] mb-1">
          <span class="material-symbols-outlined text-[30px]">spa</span>
        </div>
        <h1 class="text-2xl font-extrabold tracking-tight text-[var(--text)]">V07 Sanctuary</h1>
        <p class="text-xs text-[var(--text-inactive)] tracking-wider uppercase font-semibold">Web Companion &amp; Reflection OS</p>
      </div>

      <!-- Auth Form -->
      <div id="authForm" class="space-y-4 text-left">
        <div class="space-y-1">
          <label class="text-[11px] font-bold text-[var(--text-inactive)] uppercase tracking-wider">Email Address</label>
          <input type="email" id="authEmail" placeholder="you@example.com" autocomplete="email"
                 class="w-full px-3.5 py-2.5 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-sm text-[var(--text)] placeholder-[var(--muted)] focus:outline-none focus:border-[var(--primary)] font-medium transition-colors" />
        </div>

        <div class="space-y-1">
          <div class="flex items-center justify-between">
            <label class="text-[11px] font-bold text-[var(--text-inactive)] uppercase tracking-wider">Password</label>
            <button type="button" onclick="toggleAuthPasswordVisibility()" id="authPassToggle" class="text-[11px] font-semibold text-[var(--primary)] hover:underline">Show</button>
          </div>
          <input type="password" id="authPass" placeholder="••••••••" autocomplete="current-password"
                 class="w-full px-3.5 py-2.5 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-sm text-[var(--text)] placeholder-[var(--muted)] focus:outline-none focus:border-[var(--primary)] font-medium transition-colors" />
        </div>

        <div id="authError" class="text-xs text-[var(--red)] min-h-[18px] font-medium"></div>

        <div class="space-y-2.5 pt-1">
          <button type="button" id="authSubmitBtn" onclick="handleAuth()"
                  class="w-full py-3 rounded-xl bg-[var(--primary)] hover:bg-[var(--primary-hover)] text-[var(--on-primary)] font-bold text-sm tracking-wide flex items-center justify-center gap-2 shadow-sm active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[18px]">login</span>
            <span>Sign In</span>
          </button>

          <button type="button" onclick="launchLocalDemoMode()"
                  class="w-full py-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-[var(--text)] font-semibold text-xs tracking-wide flex items-center justify-center gap-2 active:scale-95 transition-all"
                  title="Explore local companion without signing in">
            <span class="material-symbols-outlined text-[16px] text-[var(--primary)]">offline_bolt</span>
            <span>Explore Offline Demo Mode</span>
          </button>
        </div>

        <div class="pt-2 text-center">
          <button type="button" id="authToggle" onclick="toggleAuthMode()" class="text-xs text-[var(--text-inactive)] hover:text-[var(--text)] transition-colors">
            Don't have an account? <span class="text-[var(--primary)] font-bold">Sign Up</span>
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- ========================================================================= -->
  <!-- 2. MAIN APPLICATION SHELL (STICKY HEADER + DESKTOP RAIL + BOTTOM BAR)     -->
  <!-- ========================================================================= -->
  <div id="app" class="hidden min-h-screen flex flex-col">
    <!-- Top Modern App Bar -->
    <header class="sticky top-0 z-40 h-14 bg-[var(--surface)]/95 backdrop-blur-md border-b border-[var(--border)] flex items-center justify-between px-3 sm:px-6 shadow-xs">
      <div class="flex items-center gap-2.5 min-w-0">
        <!-- Logo Button -->
        <button type="button" onclick="switchTab('dashboard')" class="flex items-center gap-2 px-2.5 py-1 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-[var(--primary)] hover:border-[var(--primary)]/40 transition-colors">
          <span class="material-symbols-outlined text-[20px]">spa</span>
          <span class="font-extrabold text-sm tracking-tight text-[var(--text)]">V07</span>
          <span class="text-[10px] font-bold px-1.5 py-0.2 rounded bg-[var(--primary-container)] text-[var(--primary)] font-mono">v3</span>
        </button>

        <span class="text-xs font-bold text-[var(--text-inactive)] uppercase tracking-wider hidden sm:inline" id="pageTitle">COMPANION</span>

        <!-- Active Focus Timer Pill in Header -->
        <div id="topTimerPill" onclick="switchTab('dashboard')" style="display:none;"
             class="items-center gap-1.5 px-2.5 py-1 rounded-full bg-[var(--primary-container)] border border-[var(--border-highlight)] text-[var(--primary)] text-xs font-mono font-bold cursor-pointer active:scale-95 transition-transform"
             title="Active Chronometer — click to jump">
          <span class="w-2 h-2 rounded-full bg-[var(--primary)] pulse-dot"></span>
          <span id="topTimerText">00:00</span>
        </div>
      </div>

      <!-- Right Header Actions -->
      <div class="flex items-center gap-1.5 sm:gap-2">
        <!-- Date Selector Preset Bar -->
        <div class="flex items-center gap-1 bg-[var(--surface-container)] border border-[var(--border)] rounded-xl px-1.5 py-0.5 text-xs">
          <button type="button" onclick="shiftViewDate(-1)" class="w-7 h-7 rounded-lg flex items-center justify-center text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container-high)]" title="Previous Day ([)">
            <span class="material-symbols-outlined text-[16px]">chevron_left</span>
          </button>
          <button type="button" onclick="openDatePickerModal()" id="topDateLabel" class="px-2 py-0.5 font-mono text-[11px] font-bold text-[var(--text)] hover:text-[var(--primary)]" title="Click to pick date">
            Today
          </button>
          <button type="button" onclick="shiftViewDate(1)" class="w-7 h-7 rounded-lg flex items-center justify-center text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container-high)]" title="Next Day (])">
            <span class="material-symbols-outlined text-[16px]">chevron_right</span>
          </button>
        </div>

        <!-- Sync Dot Badge -->
        <div class="flex items-center gap-1.5 px-2 py-1 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-[11px]" title="Cloud Sync Status">
          <span id="syncDot" class="w-2 h-2 rounded-full bg-[var(--green)] pulse-dot"></span>
          <span id="syncStatusLabel" class="hidden md:inline font-mono text-[10px] text-[var(--text-inactive)] font-medium">Synced</span>
        </div>

        <!-- Theme Toggle -->
        <button type="button" onclick="toggleTheme()" class="touch-target-sm rounded-xl text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] border border-[var(--border)]" title="Toggle Light / Dark Theme">
          <span class="material-symbols-outlined text-[18px]" id="themeIcon">dark_mode</span>
        </button>

        <!-- Shortcuts Modal Button -->
        <button type="button" onclick="showShortcutsModal()" class="touch-target-sm rounded-xl text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] border border-[var(--border)] hidden sm:inline-flex" title="Keyboard Shortcuts (?)">
          <span class="material-symbols-outlined text-[18px]">keyboard</span>
        </button>

        <!-- Logout Button -->
        <button type="button" onclick="logoutUser()" class="touch-target-sm rounded-xl text-[var(--red)] hover:bg-[var(--red)]/10 border border-[var(--border)]" title="Sign Out">
          <span class="material-symbols-outlined text-[18px]">logout</span>
        </button>
      </div>
    </header>

    <!-- App Body with Desktop Sidebar Rail -->
    <div class="flex-1 flex max-w-full overflow-x-hidden">
      <!-- Desktop Left Rail (>=920px) -->
      <aside class="hidden lg:flex flex-col w-[240px] shrink-0 sticky top-14 h-[calc(100vh-56px)] bg-[var(--surface)] border-r border-[var(--border)] p-3 space-y-1 overflow-y-auto">
        <div class="px-3 py-2 text-[10px] font-bold text-[var(--text-inactive)] uppercase tracking-wider">Navigation</div>
        
        <nav class="space-y-1 flex-1">
          <button type="button" data-tab="dashboard" onclick="switchTab('dashboard')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--primary)]">dashboard</span>
            <span>Home</span>
          </button>
          <button type="button" data-tab="events" onclick="switchTab('events')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--purple)]">receipt_long</span>
            <span>Events &amp; Journal</span>
          </button>
          <button type="button" data-tab="analytics" onclick="switchTab('analytics')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--primary)]">monitoring</span>
            <span>Analytics &amp; Stats</span>
          </button>
          <button type="button" data-tab="tasks" onclick="switchTab('tasks')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--green)]">check_circle</span>
            <span>Task Discipline</span>
          </button>
          <button type="button" data-tab="runs" onclick="switchTab('runs')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--green)]">directions_run</span>
            <span>Runtime Tracker</span>
          </button>
          <button type="button" data-tab="day" onclick="switchTab('day')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--yellow)]">calendar_today</span>
            <span>Daily Review</span>
          </button>
          <button type="button" data-tab="usage" onclick="switchTab('usage')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--accent-orange)]">hourglass_empty</span>
            <span>Screen Stillness</span>
          </button>
          <button type="button" data-tab="gita" onclick="switchTab('gita')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--yellow)]">menu_book</span>
            <span>Bhagavad Gita</span>
          </button>
          <button type="button" data-tab="settings" onclick="switchTab('settings')" class="desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all">
            <span class="material-symbols-outlined text-[20px] text-[var(--muted)]">settings</span>
            <span>Settings &amp; Vault</span>
          </button>
        </nav>

        <!-- Rail Footer -->
        <div class="pt-3 border-t border-[var(--border)] text-[11px] text-[var(--text-inactive)] space-y-1 px-1">
          <div class="font-mono text-[10px] truncate text-[var(--primary)]" id="railUserEmail">Sovereign Mode</div>
          <div class="text-[10px]">Press <kbd>?</kbd> for shortcuts</div>
        </div>
      </aside>

      <!-- Main Content Panels -->
      <main class="flex-1 p-3 sm:p-6 pb-24 lg:pb-12 max-w-[1100px] mx-auto w-full overflow-x-hidden">
        <div id="panelDashboard" class="panel active space-y-4"></div>
        <div id="panelEvents" class="panel space-y-4"></div>
        <div id="panelAnalytics" class="panel space-y-4"></div>
        <div id="panelTasks" class="panel space-y-4"></div>
        <div id="panelRuns" class="panel space-y-4"></div>
        <div id="panelDay" class="panel space-y-4"></div>
        <div id="panelUsage" class="panel space-y-4"></div>
        <div id="panelGita" class="panel space-y-4"></div>
        <div id="panelSettings" class="panel space-y-4"></div>
      </main>
    </div>

    <!-- Mobile Bottom Navigation Bar (<920px) -->
    <nav id="bottomNav" class="lg:hidden fixed bottom-0 left-0 right-0 z-40 h-16 bg-[var(--surface)]/95 backdrop-blur-md border-t border-[var(--border)] flex items-center justify-around px-1 overflow-x-auto no-scrollbar shadow-lg">
      <button type="button" data-tab="dashboard" onclick="switchTab('dashboard')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">dashboard</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Home</span>
      </button>
      <button type="button" data-tab="events" onclick="switchTab('events')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">receipt_long</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Events</span>
      </button>
      <button type="button" data-tab="analytics" onclick="switchTab('analytics')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">monitoring</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Stats</span>
      </button>
      <button type="button" data-tab="tasks" onclick="switchTab('tasks')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">check_circle</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Tasks</span>
      </button>
      <button type="button" data-tab="runs" onclick="switchTab('runs')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">directions_run</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Runs</span>
      </button>
      <button type="button" data-tab="day" onclick="switchTab('day')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">calendar_today</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Days</span>
      </button>
      <button type="button" data-tab="usage" onclick="switchTab('usage')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">hourglass_empty</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Screen</span>
      </button>
      <button type="button" data-tab="gita" onclick="switchTab('gita')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">menu_book</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Gita</span>
      </button>
      <button type="button" data-tab="settings" onclick="switchTab('settings')" class="mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all">
        <span class="material-symbols-outlined text-[22px]">settings</span>
        <span class="text-[9px] font-bold tracking-tight mt-0.5">Vault</span>
      </button>
    </nav>
  </div>

  <!-- ========================================================================= -->
  <!-- 3. DETAIL OVERLAY SLIDE-OVER SHEET                                        -->
  <!-- ========================================================================= -->
  <div id="detailOverlay" class="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm hidden flex justify-end transition-opacity">
    <div class="w-full max-w-[560px] h-full bg-[var(--surface)] border-l border-[var(--border)] flex flex-col overflow-hidden shadow-2xl animate-fade-in">
      <div class="h-14 px-4 border-b border-[var(--border)] flex items-center justify-between shrink-0 bg-[var(--surface-container-low)]">
        <div class="flex items-center gap-2 min-w-0">
          <button type="button" onclick="closeDetail()" class="touch-target-sm rounded-xl text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)]">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
          <h3 id="detailTitle" class="text-sm font-bold text-[var(--text)] truncate">Detail View</h3>
        </div>
      </div>
      <div class="flex-1 p-4 overflow-y-auto space-y-4" id="detailBody"></div>
    </div>
  </div>

  <!-- ========================================================================= -->
  <!-- 4. MODAL DIALOG CONTAINER                                                 -->
  <!-- ========================================================================= -->
  <div id="modalOverlay" class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm hidden items-center justify-center p-4">
    <div class="w-full max-w-[460px] bg-[var(--surface)] border border-[var(--border)] rounded-2xl p-5 sm:p-6 shadow-2xl space-y-4 max-h-[85vh] overflow-y-auto" style="box-shadow: var(--card-shadow);">
      <div class="flex items-center justify-between pb-2 border-b border-[var(--border)]">
        <h3 id="modalTitle" class="text-base font-bold text-[var(--text)]">Modal Action</h3>
        <button type="button" onclick="closeModal()" class="touch-target-sm rounded-lg text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)]">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
      <div id="modalFields" class="space-y-3.5 text-left"></div>
      <div class="flex items-center justify-end gap-2 pt-2 border-t border-[var(--border)]">
        <button type="button" onclick="closeModal()" class="px-4 py-2 rounded-xl text-xs font-bold text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)]">Cancel</button>
        <button type="button" id="modalSubmit" class="px-5 py-2 rounded-xl bg-[var(--primary)] hover:bg-[var(--primary-hover)] text-[var(--on-primary)] text-xs font-bold shadow-xs active:scale-95 transition-all">Submit</button>
      </div>
    </div>
  </div>

  <!-- Toast Notification -->
  <div id="toast" class="fixed bottom-20 sm:bottom-8 left-1/2 -translate-x-1/2 z-50 px-4 py-2.5 rounded-xl bg-[var(--surface-container-highest)] border border-[var(--border-highlight)] text-[var(--text)] text-xs font-bold shadow-xl opacity-0 pointer-events-none transition-all flex items-center gap-2"></div>

  <!-- ========================================================================= -->
  <!-- 5. COMPANION LOGIC & APPLICATION ENGINE                                   -->
  <!-- ========================================================================= -->
  <script>
    // --- ZERO BROKEN TEXT SANITIZER (IMPECCABLE MANDATORY IMPLEMENTATION) ---
    function cleanText(input) {
      if (input == null) return '';
      if (typeof input === 'object') {
        if (input.value) input = input.value;
        else if (Array.isArray(input)) input = input.join(', ');
        else input = '';
      }
      var str = String(input).replace(/<[^>]+>/g, ' ');
      var txt = document.createElement('textarea');
      txt.innerHTML = str;
      return txt.value.replace(/\s+/g, ' ').trim();
    }

    function esc(s) {
      return String(s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    // --- DATE & TIME HELPERS ---
    function localDate() {
      var d = new Date();
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
    }
    function fmtDate(d) {
      if (!d) return '';
      var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
      var p = d.split('-');
      if (p.length < 3) return d;
      return p[2] + ' ' + months[parseInt(p[1]) - 1];
    }
    function fmtTime(ts) {
      if (!ts) return '';
      var d = new Date(ts);
      var h = d.getHours();
      var m = String(d.getMinutes()).padStart(2, '0');
      var ampm = h >= 12 ? 'pm' : 'am';
      h = h % 12 || 12;
      return h + ':' + m + ampm;
    }
    function fmtTs(ts) {
      if (!ts) return '';
      var d = new Date(ts);
      return fmtTime(ts) + ' · ' + fmtDate(d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'));
    }
    function eTs(e) { return new Date(e.ts || 0).getTime() || 0; }
    function fmtSecs(s) {
      var sec = s || 0;
      var h = Math.floor(sec / 3600);
      var m = Math.floor((sec % 3600) / 60);
      if (h > 0) return h + 'h ' + m + 'm';
      return m + 'm' + (sec % 60 ? ' ' + Math.floor(sec % 60) + 's' : '');
    }

    // --- TOAST CONTROLLER ---
    var _toastTimer = null;
    function toast(msg) {
      var el = document.getElementById('toast');
      if (!el) return;
      clearTimeout(_toastTimer);
      el.textContent = cleanText(msg);
      el.classList.remove('opacity-0', 'pointer-events-none');
      el.classList.add('opacity-100', 'translate-y-[-4px]');
      _toastTimer = setTimeout(function() {
        el.classList.remove('opacity-100', 'translate-y-[-4px]');
        el.classList.add('opacity-0', 'pointer-events-none');
      }, 2200);
    }

    // --- SYNTHESIZED AUDIO CHIMES (WEB AUDIO API - ZERO ASSET DEPENDENCY) ---
    function playChime(type) {
      try {
        var AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        var ctx = new AudioCtx();
        var osc = ctx.createOscillator();
        var gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        var now = ctx.currentTime;
        if (type === 'complete') {
          osc.type = 'triangle';
          osc.frequency.setValueAtTime(587.33, now);
          osc.frequency.setValueAtTime(880, now + 0.12);
          gain.gain.setValueAtTime(0.25, now);
          gain.gain.exponentialRampToValueAtTime(0.001, now + 0.55);
          osc.start(now);
          osc.stop(now + 0.55);
        } else {
          osc.type = 'sine';
          osc.frequency.setValueAtTime(659.25, now);
          gain.gain.setValueAtTime(0.12, now);
          gain.gain.exponentialRampToValueAtTime(0.001, now + 0.2);
          osc.start(now);
          osc.stop(now + 0.2);
        }
      } catch (_) {}
    }

    // --- FIREBASE INITIALIZATION ---
    var firebaseConfig = {
      apiKey: "AIzaSyCiKGZ_J8p-XnvsANNnmkrUxEZQX0KUVUc",
      authDomain: "v07-dashboard.firebaseapp.com",
      projectId: "v07-dashboard",
      storageBucket: "v07-dashboard.firebasestorage.app",
      messagingSenderId: "689369760342",
      appId: "1:689369760342:web:e0debabebc08d6f016f1f7"
    };

    var db = null;
    var _currentUser = null;
    var _isLocalDemo = false;
    var _isSignUp = false;

    try {
      firebase.initializeApp(firebaseConfig);
      db = firebase.firestore();
      try { firebase.auth().setPersistence((firebase.auth.Auth && firebase.auth.Auth.Persistence && firebase.auth.Auth.Persistence.LOCAL) || 'local'); } catch (_) {}
      db.enablePersistence({ synchronizeTabs: true }).catch(function() {});
    } catch (e) {
      console.warn('Firebase initialization note:', e);
    }

    // --- APPLICATION STATE CACHES ---
    var _eventsCache = [];
    var _usageCache = [];
    var _dayCache = [];
    var _gitaReadCache = [];
    var _gitaReviewsCache = [];
    var _chartInstances = {};
    var _listeners = [];
    var _currentTab = 'dashboard';
    var _viewDate = localDate();
    var _taskFilter = 'all';
    var _eventsFilter = { category: 'all', search: '' };
    var _usageLoaded = false;
    var _dayLoaded = false;

    // --- TIMER ACTIVITIES ---
    var TIMER_ACTIVITIES = [
      { id: 'deepwork', label: 'Deep Work', icon: 'psychology' },
      { id: 'dsa', label: 'DSA Practice', icon: 'code' },
      { id: 'reading', label: 'Reading', icon: 'menu_book' },
      { id: 'writing', label: 'Writing & Reflection', icon: 'edit_note' },
      { id: 'coding', label: 'System Build', icon: 'terminal' },
      { id: 'workout', label: 'Workout', icon: 'fitness_center' }
    ];

    // --- CATEGORY VISUAL TOKENS ---
    var CATEGORY_ICONS = {
      session: 'timer', sessions: 'timer',
      task: 'check_circle', tasks: 'check_circle',
      mood: 'mood', energy: 'bolt',
      running: 'directions_run', habits: 'auto_awesome', habit: 'auto_awesome',
      thought: 'psychology', journal: 'edit_note',
      screentime: 'hourglass_empty', appusage: 'hourglass_empty',
      gita: 'menu_book', metrics: 'analytics', dsa: 'code'
    };

    var CATEGORY_COLORS = {
      session: 'border-l-blue-500 bg-blue-500/10 text-blue-400',
      sessions: 'border-l-blue-500 bg-blue-500/10 text-blue-400',
      task: 'border-l-emerald-500 bg-emerald-500/10 text-emerald-400',
      tasks: 'border-l-emerald-500 bg-emerald-500/10 text-emerald-400',
      running: 'border-l-emerald-400 bg-emerald-500/10 text-emerald-400',
      habits: 'border-l-orange-500 bg-orange-500/10 text-orange-400',
      habit: 'border-l-orange-500 bg-orange-500/10 text-orange-400',
      thought: 'border-l-violet-500 bg-violet-500/10 text-violet-400',
      journal: 'border-l-teal-500 bg-teal-500/10 text-teal-400',
      mood: 'border-l-rose-500 bg-rose-500/10 text-rose-400',
      energy: 'border-l-amber-500 bg-amber-500/10 text-amber-400',
      screentime: 'border-l-amber-500 bg-amber-500/10 text-amber-400',
      gita: 'border-l-amber-400 bg-amber-500/10 text-amber-400',
      metrics: 'border-l-indigo-500 bg-indigo-500/10 text-indigo-400',
      dsa: 'border-l-cyan-500 bg-cyan-500/10 text-cyan-400'
    };

    var MOOD_LABELS = {
      happy: 'Peak Energy & Flow', energetic: 'Vibrant Cadence',
      motivated: 'Clear Purpose', stressed: 'Tense & Overwhelmed',
      anxious: 'Restless Focus', tired: 'Low Energy',
      confused: 'Diffused Attention', relaxed: 'Serene & Calm',
      frustrated: 'Friction', hopeful: 'Centered'
    };

    var ENERGY_LABELS = {
      high: '⚡ High Momentum', moderate: '✨ Clear & Steady',
      balanced: '🌱 Neutral Cadence', low: '🌧️ Low Vitality',
      exhausted: '⚠️ Exhausted'
    };

    // --- DATE NAVIGATION ---
    function getViewDate() { return _viewDate || localDate(); }
    function setViewDate(d) {
      _viewDate = d;
      updateHeaderDateDisplay();
      if (_currentTab === 'dashboard') renderDashboardData();
      else if (_currentTab === 'events') renderEventsData();
      else if (_currentTab === 'tasks') renderTasksData();
      else if (_currentTab === 'runs') renderRunsData();
      else if (_currentTab === 'usage') renderUsageData();
      else if (_currentTab === 'day') renderDayData();
    }
    function shiftViewDate(delta) {
      var parts = getViewDate().split('-').map(Number);
      var dt = new Date(parts[0], parts[1] - 1, parts[2]);
      dt.setDate(dt.getDate() + delta);
      var y = dt.getFullYear();
      var m = String(dt.getMonth() + 1).padStart(2, '0');
      var d = String(dt.getDate()).padStart(2, '0');
      setViewDate(y + '-' + m + '-' + d);
    }
    function resetToToday() { setViewDate(localDate()); }
    function updateHeaderDateDisplay() {
      var el = document.getElementById('topDateLabel');
      if (!el) return;
      var cur = getViewDate();
      var isToday = (cur === localDate());
      el.textContent = isToday ? 'Today' : fmtDate(cur);
    }
    function openDatePickerModal() {
      showModal('Navigate to Date', [
        { key: 'targetDate', label: 'Select Target Date', type: 'date', value: getViewDate() }
      ], function(d) {
        if (d.targetDate) setViewDate(d.targetDate);
      });
    }

    // --- AUTHENTICATION FLOW ---
    function toggleAuthPasswordVisibility() {
      var inp = document.getElementById('authPass');
      var btn = document.getElementById('authPassToggle');
      if (!inp || !btn) return;
      if (inp.type === 'password') {
        inp.type = 'text';
        btn.textContent = 'Hide';
      } else {
        inp.type = 'password';
        btn.textContent = 'Show';
      }
    }

    function toggleAuthMode() {
      _isSignUp = !_isSignUp;
      var btn = document.getElementById('authSubmitBtn');
      var toggle = document.getElementById('authToggle');
      var err = document.getElementById('authError');
      if (btn) btn.innerHTML = _isSignUp ? '<span class="material-symbols-outlined text-[18px]">person_add</span><span>Sign Up</span>' : '<span class="material-symbols-outlined text-[18px]">login</span><span>Sign In</span>';
      if (toggle) toggle.innerHTML = _isSignUp ? 'Already have an account? <span class="text-[var(--primary)] font-bold">Sign In</span>' : 'Don\'t have an account? <span class="text-[var(--primary)] font-bold">Sign Up</span>';
      if (err) err.textContent = '';
    }

    function handleAuth() {
      var email = document.getElementById('authEmail')?.value.trim();
      var pass = document.getElementById('authPass')?.value;
      var err = document.getElementById('authError');
      if (!email || !pass) {
        if (err) err.textContent = 'Email and password are required';
        return;
      }
      if (err) err.textContent = '';
      if (_isSignUp) {
        firebase.auth().createUserWithEmailAndPassword(email, pass).catch(function(e) {
          if (err) err.textContent = e.message;
        });
      } else {
        firebase.auth().signInWithEmailAndPassword(email, pass).catch(function(e) {
          if (err) err.textContent = e.message;
        });
      }
    }

    function launchLocalDemoMode() {
      _isLocalDemo = true;
      _currentUser = { uid: 'sovereign_local', email: 'sovereign@v07.local', isAnonymous: true };
      localStorage.setItem('v07_logged_in', 'true');
      document.getElementById('authScreen').style.display = 'none';
      document.getElementById('app').classList.remove('hidden');
      document.getElementById('railUserEmail').textContent = 'Offline Demo User';
      updateSyncDot('online');
      populateInitialDemoState();
      switchTab('dashboard');
      toast('Welcome to Sovereign Companion Mode!');
    }

    function logoutUser() {
      if (confirm('Sign out of V07 Sanctuary Companion?')) {
        localStorage.removeItem('v07_logged_in');
        if (!_isLocalDemo && firebase.auth()) {
          firebase.auth().signOut().catch(function(_) {});
        }
        location.reload();
      }
    }

    if (firebase.auth) {
      firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
          _currentUser = user;
          _isLocalDemo = false;
          localStorage.setItem('v07_logged_in', 'true');
          document.getElementById('authScreen').style.display = 'none';
          document.getElementById('app').classList.remove('hidden');
          var emailEl = document.getElementById('railUserEmail');
          if (emailEl) emailEl.textContent = user.email || 'Cloud User';
          startListeners();
          sessionEnsureTicker();
          var lastTab = localStorage.getItem('v07_active_tab') || 'dashboard';
          switchTab(lastTab);
        } else if (!_isLocalDemo) {
          _currentUser = null;
          localStorage.removeItem('v07_logged_in');
          document.getElementById('authScreen').style.display = 'flex';
          document.getElementById('app').classList.add('hidden');
          stopListeners();
        }
      });
    }

    // --- FIRESTORE HELPERS ---
    function userCol() {
      if (_isLocalDemo || !db || !_currentUser) return null;
      return db.collection('users').doc(_currentUser.uid).collection('events');
    }
    function userSub(col) {
      if (_isLocalDemo || !db || !_currentUser) return null;
      return db.collection('users').doc(_currentUser.uid).collection(col);
    }

    function updateSyncDot(state) {
      var dot = document.getElementById('syncDot');
      var lbl = document.getElementById('syncStatusLabel');
      if (!dot) return;
      if (state === 'offline') {
        dot.className = 'w-2 h-2 rounded-full bg-[var(--red)]';
        if (lbl) lbl.textContent = 'Offline';
      } else if (state === 'syncing') {
        dot.className = 'w-2 h-2 rounded-full bg-[var(--yellow)] pulse-dot';
        if (lbl) lbl.textContent = 'Syncing...';
      } else {
        dot.className = 'w-2 h-2 rounded-full bg-[var(--green)] pulse-dot';
        if (lbl) lbl.textContent = 'Synced';
      }
    }

    function startListeners() {
      stopListeners();
      if (!userCol()) return;
      var q = userCol().orderBy('ts', 'desc').limit(3000);
      _listeners.push(q.onSnapshot(function(snap) {
        _eventsCache = [];
        snap.forEach(function(doc) {
          _eventsCache.push(Object.assign({ docId: doc.id }, doc.data()));
        });
        updateSyncDot('online');
        refreshActivePanel();
      }, function() {
        updateSyncDot('offline');
      }));

      var uCol = userSub('appusage');
      if (uCol) {
        _listeners.push(uCol.onSnapshot(function(snap) {
          _usageCache = [];
          snap.forEach(function(d) {
            _usageCache.push(Object.assign({ docId: d.id }, d.data()));
          });
          _usageLoaded = true;
          if (_currentTab === 'usage' || _currentTab === 'dashboard') refreshActivePanel();
        }, function(e) {
          console.warn('Appusage sync error:', e);
        }));
      }
    }
    function stopListeners() {
      _listeners.forEach(function(u) { try { u(); } catch (_) {} });
      _listeners = [];
    }

    function eventsFor(cat, date) {
      return _eventsCache.filter(function(e) {
        if (cat && cat !== 'all' && e.category !== cat) return false;
        if (date && e.date !== date) return false;
        return true;
      });
    }

    function refreshActivePanel() {
      if (_currentTab === 'dashboard') renderDashboardData();
      else if (_currentTab === 'events') renderEventsData();
      else if (_currentTab === 'tasks') renderTasksData();
      else if (_currentTab === 'runs') renderRunsData();
      else if (_currentTab === 'analytics') renderAnalytics();
      else if (_currentTab === 'usage') renderUsageData();
      else if (_currentTab === 'day') renderDayData();
    }

    async function writeEvent(category, data) {
      var targetDate = (data && data.date) || getViewDate() || localDate();
      var payload = Object.assign({
        id: Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
        category: category,
        date: targetDate,
        ts: Date.now()
      }, data || {});

      if (_isLocalDemo) {
        _eventsCache.unshift(Object.assign({ docId: payload.id }, payload));
        refreshActivePanel();
        return;
      }
      var col = userCol();
      if (col) {
        updateSyncDot('syncing');
        await col.add(payload);
        updateSyncDot('online');
      }
    }

    // --- TAB SWITCHING ENGINE ---
    function switchTab(tab) {
      _currentTab = tab;
      try { localStorage.setItem('v07_active_tab', tab); } catch (_) {}
      
      document.querySelectorAll('.panel').forEach(function(p) { p.classList.remove('active'); });
      var panel = document.getElementById('panel' + tab.charAt(0).toUpperCase() + tab.slice(1));
      if (panel) panel.classList.add('active');

      document.querySelectorAll('#bottomNav button, aside nav button').forEach(function(btn) {
        var isTarget = btn.getAttribute('data-tab') === tab;
        if (btn.classList.contains('desktop-nav-btn')) {
          if (isTarget) {
            btn.className = 'desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 bg-[var(--primary-container)] text-[var(--primary)] border border-[var(--border-highlight)] active:scale-95 transition-all';
          } else {
            btn.className = 'desktop-nav-btn w-full h-10 px-3 rounded-xl text-xs font-bold flex items-center gap-3 text-[var(--text-inactive)] hover:text-[var(--text)] hover:bg-[var(--surface-container)] active:scale-95 transition-all';
          }
        } else if (btn.classList.contains('mobile-nav-btn')) {
          if (isTarget) {
            btn.className = 'mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--primary)] font-bold active:scale-95 transition-all';
          } else {
            btn.className = 'mobile-nav-btn flex flex-col items-center justify-center flex-1 h-full py-1 text-[var(--text-inactive)] hover:text-[var(--text)] active:scale-95 transition-all';
          }
        }
      });

      var titleMap = {
        dashboard: 'HOME DASHBOARD', events: 'JOURNAL & EVENTS',
        analytics: 'STATS & PROGRESSION', tasks: 'TASK DISCIPLINE',
        runs: 'RUNTIME TRACKER', day: 'DAILY REVIEW',
        usage: 'DIGITAL STILLNESS', gita: 'BHAGAVAD GITA', settings: 'VAULT & SETTINGS'
      };
      var pTitle = document.getElementById('pageTitle');
      if (pTitle) pTitle.textContent = titleMap[tab] || 'COMPANION';

      window.scrollTo(0, 0);

      if (tab === 'dashboard') loadDashboard();
      else if (tab === 'events') loadEvents();
      else if (tab === 'analytics') loadAnalytics();
      else if (tab === 'tasks') loadTasks();
      else if (tab === 'runs') loadRuns();
      else if (tab === 'day') loadDay();
      else if (tab === 'usage') loadUsage();
      else if (tab === 'gita') loadGita();
      else if (tab === 'settings') loadSettings();
    }

    // --- MODAL DIALOGS CONTROLLER ---
    function showModal(title, fields, onSubmit) {
      var tEl = document.getElementById('modalTitle');
      var fEl = document.getElementById('modalFields');
      if (tEl) tEl.textContent = title;
      if (!fEl) return;
      fEl.innerHTML = '';

      fields.forEach(function(f) {
        var group = document.createElement('div');
        group.className = 'space-y-1.5 text-left';
        var lbl = document.createElement('label');
        lbl.className = 'text-[11px] font-bold text-[var(--text-inactive)] uppercase tracking-wider block';
        lbl.textContent = f.label;
        group.appendChild(lbl);

        var el;
        if (f.type === 'select') {
          el = document.createElement('select');
          el.className = 'w-full px-3 py-2 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] focus:outline-none focus:border-[var(--primary)] cursor-pointer';
          (f.options || []).forEach(function(o) {
            var opt = document.createElement('option');
            opt.value = typeof o === 'object' ? o.value : o;
            opt.textContent = typeof o === 'object' ? o.label : o;
            el.appendChild(opt);
          });
        } else if (f.type === 'textarea') {
          el = document.createElement('textarea');
          el.rows = 3;
          el.className = 'w-full px-3 py-2 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-xs text-[var(--text)] focus:outline-none focus:border-[var(--primary)] resize-none shadow-xs';
        } else {
          el = document.createElement('input');
          el.type = f.type || 'text';
          el.className = 'w-full px-3 py-2 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] focus:outline-none focus:border-[var(--primary)] shadow-xs';
        }

        if (f.value !== undefined) el.value = f.value;
        if (f.placeholder) el.placeholder = f.placeholder;
        el.dataset.key = f.key;
        group.appendChild(el);

        if (f.chips && Array.isArray(f.chips)) {
          var chipRow = document.createElement('div');
          chipRow.className = 'flex flex-wrap gap-1.5 pt-1';
          f.chips.forEach(function(ch) {
            var cBtn = document.createElement('button');
            cBtn.type = 'button';
            cBtn.className = 'px-2.5 py-1 rounded-lg bg-[var(--surface-container)] border border-[var(--border)] text-[11px] font-semibold text-[var(--text-inactive)] hover:text-[var(--text)] cursor-pointer active:scale-95 transition-all';
            cBtn.textContent = ch.label || ch.value || ch;
            cBtn.onclick = function() {
              el.value = ch.value || ch;
              chipRow.querySelectorAll('button').forEach(function(b) { b.classList.remove('bg-[var(--primary-container)]', 'text-[var(--primary)]', 'border-[var(--border-highlight)]'); });
              cBtn.classList.add('bg-[var(--primary-container)]', 'text-[var(--primary)]', 'border-[var(--border-highlight)]');
            };
            chipRow.appendChild(cBtn);
          });
          group.appendChild(chipRow);
        }

        fEl.appendChild(group);
      });

      var subBtn = document.getElementById('modalSubmit');
      if (subBtn) {
        subBtn.onclick = function() {
          var data = {};
          fEl.querySelectorAll('input, select, textarea').forEach(function(inp) {
            if (inp.dataset.key) data[inp.dataset.key] = inp.value;
          });
          closeModal();
          onSubmit(data);
        };
      }

      var modal = document.getElementById('modalOverlay');
      if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
      }
    }
    function closeModal() {
      var modal = document.getElementById('modalOverlay');
      if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
      }
    }

    function openDetail(title, html) {
      var t = document.getElementById('detailTitle');
      var b = document.getElementById('detailBody');
      var o = document.getElementById('detailOverlay');
      if (t) t.textContent = title;
      if (b) b.innerHTML = html;
      if (o) o.classList.remove('hidden');
    }
    function closeDetail() {
      var o = document.getElementById('detailOverlay');
      if (o) o.classList.add('hidden');
    }

    // --- SESSION CHRONOMETER ENGINE ---
    var _sessionTimerInterval = null;
    function sessionState() {
      try {
        var s = JSON.parse(localStorage.getItem('v07_session_timer') || 'null');
        if (s && typeof s === 'object') {
          return Object.assign({
            active: false, paused: false, mode: 'stopwatch',
            targetMin: 25, startTs: 0, resumeTs: 0,
            accumulatedMs: 0, activity: 'deepwork', notified: false
          }, s);
        }
      } catch (_) {}
      return { active: false, paused: false, mode: 'stopwatch', targetMin: 25, startTs: 0, resumeTs: 0, accumulatedMs: 0, activity: 'deepwork', notified: false };
    }
    function sessionSave(s) {
      try { localStorage.setItem('v07_session_timer', JSON.stringify(s)); } catch (_) {}
    }
    function sessionElapsedMs(s) {
      if (!s.active) return 0;
      var running = s.paused ? 0 : (Date.now() - (s.resumeTs || s.startTs));
      return (s.accumulatedMs || 0) + Math.max(0, running);
    }
    function sessionDisplay(ms) {
      var sec = Math.floor(ms / 1000);
      var h = Math.floor(sec / 3600);
      var m = Math.floor((sec % 3600) / 60);
      var s = sec % 60;
      var ss = String(s).padStart(2, '0');
      var mm = String(m).padStart(2, '0');
      return h > 0 ? (h + ':' + mm + ':' + ss) : (mm + ':' + ss);
    }

    function selectTimerActivity(actId) {
      var s = sessionState();
      s.activity = actId;
      sessionSave(s);
      playChime();
      var actObj = TIMER_ACTIVITIES.find(function(a) { return a.id === actId; });
      toast('Focus Topic: ' + (actObj ? actObj.label : actId));
      renderSessionTimer();
      sessionTick();
    }

    function selectTimerMode(mode, targetMin) {
      var s = sessionState();
      if (s.active) {
        if (s.mode === 'pomodoro' && mode === 'pomodoro' && targetMin) {
          s.targetMin = targetMin;
          sessionSave(s);
          playChime();
          toast('Pomodoro target set to ' + targetMin + 'm');
          renderSessionTimer();
          sessionTick();
          return;
        }
        return toast('Please stop active session first to switch mode');
      }
      s.mode = mode;
      if (targetMin) s.targetMin = targetMin;
      sessionSave(s);
      playChime();
      toast('Mode: ' + (mode === 'pomodoro' ? targetMin + 'm Countdown' : 'Count-Up Stopwatch'));
      renderSessionTimer();
      sessionTick();
    }

    function updateTopTimer(s, elapsed, remMs) {
      var pill = document.getElementById('topTimerPill');
      var txt = document.getElementById('topTimerText');
      if (!pill || !txt) return;
      if (s && s.active) {
        pill.style.display = 'inline-flex';
        var isPomo = s.mode === 'pomodoro';
        var disp = isPomo ? sessionDisplay(remMs) : sessionDisplay(elapsed);
        var actObj = TIMER_ACTIVITIES.find(function(a) { return a.id === s.activity; });
        var actLabel = actObj ? actObj.label : (s.activity || 'Focus');
        txt.textContent = (s.paused ? '⏸ ' : '') + disp + ' (' + actLabel + ')';
      } else {
        pill.style.display = 'none';
      }
    }

    function sessionEnsureTicker() {
      if (!_sessionTimerInterval) {
        _sessionTimerInterval = setInterval(sessionTick, 1000);
      }
      sessionTick();
    }

    function renderSessionTimer() {
      var card = document.getElementById('sessionTimerCard');
      if (!card) return;
      var s = sessionState();
      var viewD = getViewDate();
      var ses = eventsFor('sessions', viewD).filter(function(e) { return e.type === 'end'; });
      var totalMin = ses.reduce(function(a, e) {
        return a + (parseInt(e.duration) || parseInt(e.duration_min) || 0);
      }, 0);
      var hStr = totalMin >= 60 ? Math.floor(totalMin / 60) + 'h ' + (totalMin % 60) + 'm' : totalMin + 'm';

      var elapsed = sessionElapsedMs(s);
      var isPomo = s.mode === 'pomodoro';
      var targetMs = (s.targetMin || 25) * 60000;
      var remMs = Math.max(0, targetMs - elapsed);
      var display = s.active ? (isPomo ? sessionDisplay(remMs) : sessionDisplay(elapsed)) : (isPomo ? sessionDisplay(targetMs) : '00:00');
      var statusText = s.active ? (s.paused ? 'PAUSED' : (isPomo ? 'COUNTDOWN (' + s.targetMin + 'm)' : 'ACTIVE FLOW')) : (isPomo ? 'READY (' + s.targetMin + 'm)' : 'READY (0-TO-∞)');

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-4 shadow-sm" style="box-shadow: var(--card-shadow);">
          <!-- Top Row: Mode & Target Selector -->
          <div class="flex items-center justify-between gap-2 flex-wrap pb-2 border-b border-[var(--divider)]">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-[20px] text-[var(--primary)]">timer</span>
              <span class="text-xs font-bold text-[var(--text)] uppercase tracking-wider">Focus Sanctuary</span>
            </div>
            <div class="flex items-center gap-1.5 flex-wrap">
              <button type="button" onclick="selectTimerMode('stopwatch')" class="px-2.5 py-1 rounded-lg text-xs font-bold ${s.mode === 'stopwatch' ? 'bg-[var(--primary)] text-[var(--on-primary)] shadow-xs' : 'bg-[var(--surface-container)] text-[var(--text-inactive)] hover:text-[var(--text)]'} transition-all cursor-pointer">
                ⏱ Count-Up
              </button>
              <button type="button" onclick="selectTimerMode('pomodoro', 25)" class="px-2.5 py-1 rounded-lg text-xs font-bold ${s.mode === 'pomodoro' && s.targetMin === 25 ? 'bg-[var(--primary)] text-[var(--on-primary)] shadow-xs' : 'bg-[var(--surface-container)] text-[var(--text-inactive)] hover:text-[var(--text)]'} transition-all cursor-pointer">
                ⏳ 25m
              </button>
              <button type="button" onclick="selectTimerMode('pomodoro', 50)" class="px-2.5 py-1 rounded-lg text-xs font-bold ${s.mode === 'pomodoro' && s.targetMin === 50 ? 'bg-[var(--primary)] text-[var(--on-primary)] shadow-xs' : 'bg-[var(--surface-container)] text-[var(--text-inactive)] hover:text-[var(--text)]'} transition-all cursor-pointer">
                ⏳ 50m
              </button>
            </div>
          </div>

          <!-- Activity Topic Pills -->
          <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            ${TIMER_ACTIVITIES.map(function(act) {
              var isSel = (s.activity || 'deepwork') === act.id;
              return `
                <button type="button" onclick="selectTimerActivity('${act.id}')"
                        class="h-8 px-2.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 shrink-0 transition-all ${isSel ? 'bg-[var(--primary-container)] border border-[var(--border-highlight)] text-[var(--primary)] font-bold' : 'bg-[var(--surface-container)] border border-[var(--border)] text-[var(--text-inactive)] hover:text-[var(--text)]'} active:scale-95 cursor-pointer">
                  <span class="material-symbols-outlined text-[15px]">${act.icon}</span>
                  <span>${act.label}</span>
                </button>
              `;
            }).join('')}
          </div>

          <!-- Display & Controls Main Hero -->
          <div class="flex items-center justify-between gap-4 flex-wrap pt-1">
            <div class="flex flex-col">
              <div id="sessionTimerDisplay" class="font-mono text-3xl sm:text-4xl font-extrabold tracking-tight ${s.paused ? 'pulse-pause text-[var(--yellow)]' : 'text-[var(--primary)]'}">
                ${display}
              </div>
              <div class="text-[11px] font-mono text-[var(--text-inactive)] flex items-center gap-1.5 mt-1">
                <span class="w-2 h-2 rounded-full ${s.active ? (s.paused ? 'bg-[var(--yellow)]' : 'bg-[var(--green)] pulse-dot') : 'bg-[var(--muted)]'}"></span>
                <span>${statusText}</span>
                <span>·</span>
                <span>${fmtDate(viewD)} (${ses.length} ses, ${hStr})</span>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex items-center gap-2">
              ${!s.active ? `
                <button type="button" onclick="sessionStart()" class="h-10 px-5 rounded-xl bg-[var(--primary)] hover:bg-[var(--primary-hover)] text-[var(--on-primary)] font-bold text-xs flex items-center gap-1.5 shadow-sm active:scale-95 transition-all cursor-pointer">
                  <span class="material-symbols-outlined text-[18px]">play_arrow</span>
                  <span>Start Flow</span>
                </button>
              ` : `
                ${!s.paused ? `
                  <button type="button" onclick="sessionPause()" class="h-10 px-4 rounded-xl bg-[var(--yellow)]/15 border border-[var(--yellow)]/40 hover:bg-[var(--yellow)]/25 text-[var(--yellow)] font-bold text-xs flex items-center gap-1.5 active:scale-95 transition-all cursor-pointer">
                    <span class="material-symbols-outlined text-[18px]">pause</span>
                    <span>Pause</span>
                  </button>
                ` : `
                  <button type="button" onclick="sessionResume()" class="h-10 px-4 rounded-xl bg-[var(--green)] hover:bg-[var(--green)]/90 text-black font-bold text-xs flex items-center gap-1.5 active:scale-95 transition-all cursor-pointer">
                    <span class="material-symbols-outlined text-[18px]">play_arrow</span>
                    <span>Resume</span>
                  </button>
                `}
                <button type="button" onclick="sessionStop()" class="h-10 px-4 rounded-xl bg-[var(--red)]/15 border border-[var(--red)]/40 hover:bg-[var(--red)]/25 text-[var(--red)] font-bold text-xs flex items-center gap-1.5 active:scale-95 transition-all cursor-pointer">
                  <span class="material-symbols-outlined text-[18px]">stop</span>
                  <span>Stop &amp; Log</span>
                </button>
              `}
            </div>
          </div>
        </div>
      `;

      card.innerHTML = html;
      sessionEnsureTicker();
    }

    function sessionTick() {
      var s = sessionState();
      var d = document.getElementById('sessionTimerDisplay');
      var elapsed = sessionElapsedMs(s);
      var isPomo = s.mode === 'pomodoro';
      var targetMs = (s.targetMin || 25) * 60000;
      var remMs = Math.max(0, targetMs - elapsed);

      updateTopTimer(s, elapsed, remMs);

      if (!s.active) {
        if (d) d.textContent = isPomo ? sessionDisplay(targetMs) : '00:00';
        return;
      }

      if (isPomo) {
        if (d) d.textContent = sessionDisplay(remMs);
        if (remMs <= 0 && !s.notified) {
          s.notified = true;
          sessionSave(s);
          playChime('complete');
          toast('🎉 Pomodoro target complete (' + s.targetMin + 'm)!');
        }
      } else {
        if (d && !s.paused) d.textContent = sessionDisplay(elapsed);
      }
    }

    function sessionStart() {
      var s = sessionState();
      if (s.active) return;
      var now = Date.now();
      var mode = s.mode || 'stopwatch';
      var targetMin = s.targetMin || 25;
      var act = s.activity || 'deepwork';
      writeEvent('sessions', { type: 'start', activity: act, mode: mode, target_min: mode === 'pomodoro' ? targetMin : null });
      sessionSave({ active: true, paused: false, mode: mode, targetMin: targetMin, startTs: now, resumeTs: now, accumulatedMs: 0, activity: act, notified: false });
      playChime();
      sessionEnsureTicker();
      renderSessionTimer();
      toast('Focus session started: ' + act);
    }

    function sessionPause() {
      var s = sessionState();
      if (!s.active || s.paused) return;
      var now = Date.now();
      var running = now - (s.resumeTs || s.startTs);
      s.accumulatedMs = (s.accumulatedMs || 0) + Math.max(0, running);
      s.paused = true;
      s.pauseTs = now;
      sessionSave(s);
      playChime();
      sessionTick();
      renderSessionTimer();
      toast('Session paused');
    }

    function sessionResume() {
      var s = sessionState();
      if (!s.active || !s.paused) return;
      s.paused = false;
      s.resumeTs = Date.now();
      sessionSave(s);
      playChime();
      sessionEnsureTicker();
      renderSessionTimer();
      toast('Session resumed');
    }

    function sessionStop() {
      var s = sessionState();
      if (!s.active) return;
      var totalMs = sessionElapsedMs(s);
      var minutes = Math.max(1, Math.round(totalMs / 60000));
      var viewD = getViewDate();
      writeEvent('sessions', {
        type: 'end',
        activity: s.activity || 'deepwork',
        mode: s.mode || 'stopwatch',
        start_ts: new Date(s.startTs).toISOString(),
        duration: minutes,
        duration_min: minutes,
        target_min: s.mode === 'pomodoro' ? s.targetMin : null,
        date: viewD
      });
      sessionSave({ active: false, paused: false, mode: s.mode || 'stopwatch', targetMin: s.targetMin || 25, startTs: 0, resumeTs: 0, accumulatedMs: 0, activity: s.activity || 'deepwork', notified: false });
      updateTopTimer(sessionState(), 0, 0);
      playChime('complete');
      renderSessionTimer();
      toast('Logged ' + minutes + 'm focus session!');
    }

    // --- QUICK ACTIONS ---
    function quickPushups(count) {
      var n = parseInt(count) || 10;
      writeEvent('metrics', { pushups: n, type: 'pushups', date: getViewDate() });
      playChime();
      toast('+' + n + ' Pushups recorded!');
    }
    function quickDoom(min) {
      var m = parseInt(min) || 15;
      writeEvent('metrics', { doom_scroll: m, type: 'doom_scroll', date: getViewDate() });
      playChime();
      toast('+' + m + 'm Doom scroll noted');
    }
    function quickCheckin() {
      writeEvent('metrics', { type: 'checkin', note: 'Morning Check-in', date: getViewDate() });
      playChime();
      toast('Morning Check-in logged!');
    }
    function quickCheckout() {
      writeEvent('metrics', { type: 'checkout', note: 'Evening Check-out', date: getViewDate() });
      playChime();
      toast('Evening Check-out logged!');
    }
    function toggleHabitQuick(activity, willBeOn) {
      writeEvent('habits', { activity: activity, on: willBeOn, date: getViewDate() });
      playChime();
      toast(activity + ': ' + (willBeOn ? 'Done ✓' : 'Reset'));
    }

    // --- MODAL TRIGGERS ---
    function openMoodModal() {
      showModal('Log Mood Reflection', [
        { key: 'mood', label: 'Cadence & Mood', type: 'select',
          options: [
            { label: '✨ Peak Energy & Flow', value: 'happy' },
            { label: '⚡ Vibrant Cadence', value: 'energetic' },
            { label: '🌱 Centered Calm', value: 'relaxed' },
            { label: '🌧️ Low Energy', value: 'tired' },
            { label: '⚠️ Stressed / Friction', value: 'stressed' }
          ],
          chips: [
            { label: '✨ Flow', value: 'happy' },
            { label: '⚡ Vibrant', value: 'energetic' },
            { label: '🌱 Calm', value: 'relaxed' },
            { label: '🌧️ Low', value: 'tired' }
          ]
        },
        { key: 'note', label: 'Reflection Notes', type: 'textarea', placeholder: 'What contributed to this state?' }
      ], function(d) {
        if (!d.mood) return;
        writeEvent('mood', { mood: d.mood, note: d.note });
        toast('Mood reflection logged!');
      });
    }

    function openEnergyModal() {
      showModal('Log Vitality & Energy', [
        { key: 'energy', label: 'Vitality Level', type: 'select',
          options: [
            { label: '🔥 High Momentum (5/5)', value: 'high' },
            { label: '⚡ Clear & Steady (4/5)', value: 'moderate' },
            { label: '⚖️ Neutral Cadence (3/5)', value: 'balanced' },
            { label: '🪵 Low Vitality (2/5)', value: 'low' },
            { label: '💀 Exhausted (1/5)', value: 'exhausted' }
          ]
        },
        { key: 'note', label: 'Notes', type: 'textarea' }
      ], function(d) {
        if (!d.energy) return;
        writeEvent('energy', { energy: d.energy, note: d.note });
        toast('Energy rating logged!');
      });
    }

    function openHabitModal() {
      showModal('Log Habit Discipline', [
        { key: 'activity', label: 'Habit Name', type: 'text', placeholder: 'e.g. Reading, Meditation, Pushups' },
        { key: 'on', label: 'Status', type: 'select', options: [{ label: 'Completed ✓', value: 'true' }, { label: 'Incomplete', value: 'false' }] },
        { key: 'notes', label: 'Notes', type: 'textarea' }
      ], function(d) {
        if (!d.activity) return toast('Habit name required');
        writeEvent('habits', { activity: d.activity, on: d.on, notes: d.notes });
        toast('Habit discipline logged!');
      });
    }

    function openWinModal() {
      showModal('Log Daily Win', [
        { key: 'text', label: 'What was your victory today?', type: 'textarea', placeholder: 'Completed deep work milestone, resisted doom-scrolling...' }
      ], function(d) {
        if (!d.text) return toast('Victory description required');
        writeEvent('journal', { subtype: 'win', text: d.text });
        toast('Daily win celebrated! 🏆');
      });
    }

    function openLessonModal() {
      showModal('Log Lesson Learned', [
        { key: 'text', label: 'Insight or Principle', type: 'textarea', placeholder: 'Systems beat goals. Avoid context switching before noon...' }
      ], function(d) {
        if (!d.text) return toast('Lesson text required');
        writeEvent('journal', { subtype: 'lesson', text: d.text });
        toast('Insight recorded! 💡');
      });
    }

    function openJournalModal() {
      showModal('Freeform Reflection', [
        { key: 'text', label: 'Journal Entry', type: 'textarea', placeholder: 'Reflect on your day, thoughts, or ideas...' }
      ], function(d) {
        if (!d.text) return toast('Entry cannot be blank');
        writeEvent('journal', { subtype: 'journal', text: d.text });
        toast('Journal entry saved!');
      });
    }

    function openThinkModal() {
      showModal('Capture Spark / Thought', [
        { key: 'text', label: 'Thought', type: 'textarea', placeholder: 'What idea or intuition crossed your mind?' },
        { key: 'topic', label: 'Topic / Hashtag', type: 'text', placeholder: 'architecture, philosophy, system...' }
      ], function(d) {
        if (!d.text) return toast('Thought text required');
        writeEvent('thought', { text: d.text, topic: d.topic });
        toast('Thought preserved in vault! 💭');
      });
    }

    function openRunModal() {
      showModal('Log Runtime Session', [
        { key: 'distance', label: 'Distance (Kilometers)', type: 'number', placeholder: '5.2', value: '5.0' },
        { key: 'duration', label: 'Duration (Minutes)', type: 'number', placeholder: '28', value: '25' },
        { key: 'notes', label: 'Route & Cadence Notes', type: 'textarea', placeholder: 'Morning park route, cadence 165...' }
      ], function(d) {
        var dist = parseFloat(d.distance) || 0;
        var m = parseInt(d.duration) || 0;
        if (dist <= 0 || m <= 0) return toast('Valid distance and time required');
        writeEvent('running', {
          distance_km: dist,
          duration_min: m,
          duration_sec: m * 60,
          notes: d.notes
        });
        toast('Run session recorded! 🏃');
      });
    }

    function openCreateTaskModal() {
      showModal('Discipline Task Target', [
        { key: 'title', label: 'Task Title', type: 'text', placeholder: 'Clear core API migration, review PR...' },
        { key: 'deadline', label: 'Target Deadline', type: 'date', value: getViewDate() },
        { key: 'deadline_time', label: 'Target Hour (Optional)', type: 'time' },
        { key: 'priority', label: 'Priority', type: 'select',
          options: [
            { label: '🔴 Critical High', value: 'high' },
            { label: '🟡 Medium Cadence', value: 'medium' },
            { label: '🟢 Low Cadence', value: 'low' }
          ]
        }
      ], function(d) {
        if (!d.title) return toast('Task title required');
        writeEvent('tasks', {
          title: d.title,
          deadline: d.deadline,
          deadline_time: d.deadline_time,
          priority: d.priority || 'medium',
          status: 'pending'
        });
        toast('Task discipline committed!');
      });
    }

    // --- DASHBOARD PANEL RENDERER ---
    function loadDashboard() {
      renderDashboardData();
    }

    function renderDashboardData() {
      var viewDate = getViewDate();
      var isToday = (viewDate === localDate());
      var dayEvents = eventsFor(null, viewDate);

      var moods = dayEvents.filter(function(e) { return e.category === 'mood'; }).sort(function(a,b) { return eTs(b) - eTs(a); });
      var energies = dayEvents.filter(function(e) { return e.category === 'energy'; }).sort(function(a,b) { return eTs(b) - eTs(a); });
      var habits = dayEvents.filter(function(e) { return e.category === 'habits' || e.category === 'habit'; });
      var tasks = eventsFor('tasks');
      var pendingTasks = tasks.filter(function(t) { return t.status !== 'done'; });
      var completedTodayTasks = tasks.filter(function(t) { return t.status === 'done' && t.date === viewDate; });
      var sessions = dayEvents.filter(function(e) { return (e.category === 'session' || e.category === 'sessions') && e.type === 'end'; });
      var totalFocusMins = sessions.reduce(function(a, e) { return a + (parseInt(e.duration_min) || parseInt(e.duration) || 0); }, 0);
      var pushups = dayEvents.filter(function(e) { return e.pushups; }).reduce(function(s, e) { return s + (parseInt(e.pushups) || 0); }, 0);

      var latestMood = moods[0];
      var latestEnergy = energies[0];

      // Screen time for viewDate
      var usageDoc = _usageCache.find(function(d) { return (d.date || d.docId) === viewDate; });
      var screenSec = (usageDoc && usageDoc.summary && usageDoc.summary.total_screen_time_sec) || 0;

      var html = `
        <!-- Date Context Header Banner if viewing past date -->
        ${!isToday ? `
          <div class="px-4 py-2.5 rounded-xl bg-[var(--yellow)]/10 border border-[var(--yellow)]/30 text-xs font-bold text-[var(--yellow)] flex items-center justify-between shadow-2xs">
            <span class="flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[16px]">history</span>
              <span>Viewing Historical Date: ${viewDate} (logs will commit to ${viewDate})</span>
            </span>
            <button type="button" onclick="resetToToday()" class="underline hover:text-[var(--text)]">Jump to Today</button>
          </div>
        ` : ''}

        <!-- 4-Stat Metric Deck -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <!-- Focus Sanctuary -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 shadow-xs flex flex-col justify-between" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between text-[var(--text-inactive)]">
              <span class="text-[10px] font-bold uppercase tracking-wider">Deep Focus</span>
              <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">timer</span>
            </div>
            <div class="mt-2">
              <div class="font-mono text-2xl font-extrabold text-[var(--text)]">${totalFocusMins}<span class="text-sm font-normal text-[var(--text-inactive)]">m</span></div>
              <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">${sessions.length} completed session(s)</div>
            </div>
          </div>

          <!-- Tasks Completed -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 shadow-xs flex flex-col justify-between" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between text-[var(--text-inactive)]">
              <span class="text-[10px] font-bold uppercase tracking-wider">Tasks</span>
              <span class="material-symbols-outlined text-[18px] text-[var(--green)]">check_circle</span>
            </div>
            <div class="mt-2">
              <div class="font-mono text-2xl font-extrabold text-[var(--text)]">${completedTodayTasks.length}<span class="text-sm font-normal text-[var(--text-inactive)]">/${tasks.length}</span></div>
              <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">${pendingTasks.length} pending target(s)</div>
            </div>
          </div>

          <!-- Mood & Cadence -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 shadow-xs flex flex-col justify-between" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between text-[var(--text-inactive)]">
              <span class="text-[10px] font-bold uppercase tracking-wider">Cadence</span>
              <span class="material-symbols-outlined text-[18px] text-[var(--rose)]">vital_signs</span>
            </div>
            <div class="mt-2">
              <div class="text-base font-extrabold text-[var(--text)] truncate">${latestMood ? (MOOD_LABELS[latestMood.mood] || latestMood.mood) : 'Not Logged'}</div>
              <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">${latestEnergy ? (ENERGY_LABELS[latestEnergy.energy] || latestEnergy.energy) : 'Tap Quick Log below'}</div>
            </div>
          </div>

          <!-- Digital Stillness -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 shadow-xs flex flex-col justify-between" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between text-[var(--text-inactive)]">
              <span class="text-[10px] font-bold uppercase tracking-wider">Screen Stillness</span>
              <span class="material-symbols-outlined text-[18px] text-[var(--accent-orange)]">hourglass_empty</span>
            </div>
            <div class="mt-2">
              <div class="font-mono text-2xl font-extrabold text-[var(--text)]">${fmtSecs(screenSec)}</div>
              <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">${usageDoc ? 'Recorded from phone' : 'No telemetry synced'}</div>
            </div>
          </div>
        </div>

        <!-- Session Chronometer Card Container -->
        <div id="sessionTimerCard"></div>

        <!-- Quick Log Action Grid -->
        <section class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <h3 class="text-xs font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[16px] text-[var(--primary)]">bolt</span>
              <span>Quick Action Command</span>
            </h3>
            <span class="text-[10px] font-mono text-[var(--text-inactive)] font-semibold">1-Tap Prompts</span>
          </div>

          <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2">
            <button type="button" onclick="openMoodModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-rose-400">mood</span>
              <span>Log Mood</span>
            </button>
            <button type="button" onclick="openEnergyModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-amber-400">bolt</span>
              <span>Log Energy</span>
            </button>
            <button type="button" onclick="openHabitModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-emerald-400">auto_awesome</span>
              <span>Log Habit</span>
            </button>
            <button type="button" onclick="openWinModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-purple-400">trophy</span>
              <span>Log Win</span>
            </button>
            <button type="button" onclick="openLessonModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-amber-400">lightbulb</span>
              <span>Lesson</span>
            </button>
            <button type="button" onclick="openThinkModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-violet-400">psychology</span>
              <span>Spark</span>
            </button>
            <button type="button" onclick="openRunModal()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-emerald-400">directions_run</span>
              <span>Log Run</span>
            </button>
            <button type="button" onclick="quickPushups(10)" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-orange-400">fitness_center</span>
              <span>+10 Pushups</span>
            </button>
            <button type="button" onclick="quickCheckin()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-emerald-400">wb_sunny</span>
              <span>Check-in</span>
            </button>
            <button type="button" onclick="quickCheckout()" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-violet-400">bedtime</span>
              <span>Check-out</span>
            </button>
            <button type="button" onclick="quickDoom(15)" class="h-10 px-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-semibold text-[var(--text)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px] text-rose-400">warning</span>
              <span>+15m Doom</span>
            </button>
            <button type="button" onclick="openCreateTaskModal()" class="h-10 px-2.5 rounded-xl bg-[var(--primary-container)] hover:bg-[var(--primary)]/20 border border-[var(--border-highlight)] text-xs font-bold text-[var(--primary)] flex items-center gap-2 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">add_task</span>
              <span>New Task</span>
            </button>
          </div>
        </section>

        <!-- Two Column Layout: Habits & Recent Timeline -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <!-- Gentle Habits Card -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between">
              <h3 class="text-xs font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[16px] text-orange-400">auto_awesome</span>
                <span>Gentle Habits</span>
              </h3>
              <button type="button" onclick="openHabitModal()" class="text-xs font-bold text-[var(--primary)] hover:underline flex items-center gap-0.5">
                <span class="material-symbols-outlined text-[14px]">add</span>
                <span>Add</span>
              </button>
            </div>

            <div class="space-y-1.5">
              ${habits.length === 0 ? `
                <div class="py-6 text-center text-xs text-[var(--text-inactive)]">
                  No habits logged for ${viewDate}. Tap "Add" above to track a discipline.
                </div>
              ` : habits.map(function(h) {
                var isOn = h.on === 'true' || h.on === true;
                return `
                  <div onclick="toggleHabitQuick('${esc(h.activity)}', ${!isOn})" class="flex items-center justify-between p-2.5 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] cursor-pointer active:scale-98 transition-all">
                    <div class="flex items-center gap-2.5 min-w-0">
                      <span class="material-symbols-outlined text-[18px] ${isOn ? 'text-[var(--green)]' : 'text-[var(--text-inactive)]'}">
                        ${isOn ? 'check_circle' : 'radio_button_unchecked'}
                      </span>
                      <span class="text-xs font-bold text-[var(--text)] ${isOn ? 'line-through opacity-60' : ''} truncate">${cleanText(h.activity)}</span>
                    </div>
                    <span class="text-[10px] font-mono font-bold px-2 py-0.5 rounded-md ${isOn ? 'bg-[var(--green)]/15 text-[var(--green)]' : 'bg-[var(--surface-container-high)] text-[var(--text-inactive)]'}">
                      ${isOn ? 'DONE ✓' : 'TAP TO COMPLETE'}
                    </span>
                  </div>
                `;
              }).join('')}
            </div>
          </div>

          <!-- Recent Day Activity Feed -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between">
              <h3 class="text-xs font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[16px] text-[var(--primary)]">schedule</span>
                <span>Day Activity Feed</span>
              </h3>
              <button type="button" onclick="switchTab('events')" class="text-xs font-bold text-[var(--primary)] hover:underline">View All ›</button>
            </div>

            <div class="space-y-2">
              ${dayEvents.slice(0, 6).map(function(e) {
                var iconName = CATEGORY_ICONS[e.category] || 'receipt_long';
                var colorClass = CATEGORY_COLORS[e.category] || 'border-l-primary/40 text-primary';
                return `
                  <div class="flex items-start gap-2.5 p-2 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] border-l-4 ${colorClass.split(' ')[0]}">
                    <span class="material-symbols-outlined text-[16px] shrink-0 mt-0.5">${iconName}</span>
                    <div class="flex-1 min-w-0 text-left">
                      <div class="flex items-center justify-between text-[10px] font-mono text-[var(--text-inactive)]">
                        <span class="uppercase font-bold">${cleanText(e.category)}</span>
                        <span>${fmtTime(e.ts)}</span>
                      </div>
                      <p class="text-xs font-semibold text-[var(--text)] mt-0.5 truncate-multi">${cleanText(formatEventText(e))}</p>
                    </div>
                  </div>
                `;
              }).join('') || '<div class="py-6 text-center text-xs text-[var(--text-inactive)]">No events logged yet for this date.</div>'}
            </div>
          </div>
        </div>
      `;

      var p = document.getElementById('panelDashboard');
      if (p) p.innerHTML = html;
      renderSessionTimer();
    }

    // --- EVENTS / JOURNAL VAULT RENDERER ---
    function loadEvents() {
      renderEventsData();
    }

    function renderEventsData() {
      var categories = ['all', 'session', 'tasks', 'thought', 'running', 'habits', 'mood', 'energy', 'journal', 'metrics'];
      var activeCat = _eventsFilter.category || 'all';

      var filtered = eventsFor(activeCat === 'all' ? null : activeCat);
      if (_eventsFilter.search) {
        var q = _eventsFilter.search.toLowerCase();
        filtered = filtered.filter(function(e) {
          return JSON.stringify(e).toLowerCase().indexOf(q) !== -1;
        });
      }
      filtered.sort(function(a,b) { return eTs(b) - eTs(a); });

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-3 shadow-xs" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between gap-2 flex-wrap">
            <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">receipt_long</span>
              <span>Sovereign Activity Journal</span>
            </h2>
            <div class="flex items-center gap-2">
              <span class="text-xs font-mono text-[var(--text-inactive)] font-bold">${filtered.length} entries</span>
              <button type="button" onclick="exportData()" class="h-8 px-2.5 rounded-lg bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-bold text-[var(--text)] flex items-center gap-1 active:scale-95 transition-all">
                <span class="material-symbols-outlined text-[14px]">download</span>
                <span>Export JSON</span>
              </button>
            </div>
          </div>

          <!-- Search Input -->
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-2.5 text-[var(--text-inactive)] text-[18px]">search</span>
            <input type="text" value="${esc(_eventsFilter.search)}" placeholder="Filter by keyword, title, tag, or note..."
                   oninput="_eventsFilter.search=this.value; renderEventsData();"
                   class="w-full pl-9 pr-4 py-2 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] text-xs text-[var(--text)] placeholder-[var(--muted)] focus:outline-none focus:border-[var(--primary)] font-medium" />
          </div>

          <!-- Category Chips -->
          <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            ${categories.map(function(c) {
              var isSel = activeCat === c;
              return `
                <button type="button" onclick="_eventsFilter.category='${c}'; renderEventsData();"
                        class="h-7 px-2.5 rounded-lg text-xs font-bold shrink-0 transition-all ${isSel ? 'bg-[var(--primary)] text-[var(--on-primary)] shadow-xs' : 'bg-[var(--surface-container)] text-[var(--text-inactive)] hover:text-[var(--text)] border border-[var(--border)]'} active:scale-95">
                  ${c.toUpperCase()}
                </button>
              `;
            }).join('')}
          </div>
        </div>

        <!-- Event Cards Stream -->
        <div class="space-y-2.5">
          ${filtered.length === 0 ? `
            <div class="p-10 text-center bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl space-y-2">
              <span class="material-symbols-outlined text-3xl text-[var(--muted)]">filter_alt_off</span>
              <h4 class="text-sm font-bold text-[var(--text)]">No Matching Events Found</h4>
              <p class="text-xs text-[var(--text-inactive)]">Try clearing your search keyword or switching category pills.</p>
            </div>
          ` : filtered.slice(0, 150).map(function(e) {
            var iconName = CATEGORY_ICONS[e.category] || 'category';
            var colorStyles = CATEGORY_COLORS[e.category] || 'border-l-primary/40 bg-primary/10 text-primary';
            var borderClass = colorStyles.split(' ')[0];
            return `
              <div class="bg-[var(--surface-container-low)] border border-[var(--border)] border-l-4 ${borderClass} rounded-2xl p-3.5 sm:p-4 shadow-xs flex items-start justify-between gap-3 hover:border-[var(--primary)]/30 transition-all text-left">
                <div class="flex items-start gap-3 min-w-0 flex-1">
                  <div class="w-8 h-8 rounded-lg bg-[var(--surface-container)] flex items-center justify-center shrink-0 mt-0.5 text-[var(--primary)]">
                    <span class="material-symbols-outlined text-[18px]">${iconName}</span>
                  </div>
                  <div class="min-w-0 flex-1 space-y-1">
                    <div class="flex items-center gap-2">
                      <span class="text-[10px] font-bold px-2 py-0.5 rounded-md bg-[var(--surface-container)] text-[var(--text-inactive)] uppercase font-mono">${cleanText(e.category)}</span>
                      <span class="text-[11px] font-mono text-[var(--text-inactive)]">${fmtTs(e.ts)}</span>
                    </div>
                    <p class="text-xs font-semibold text-[var(--text)] leading-relaxed truncate-multi">${cleanText(formatEventText(e))}</p>
                  </div>
                </div>
                <div class="flex items-center gap-1 shrink-0">
                  <button type="button" onclick="editEvent('${e.docId}')" class="touch-target-sm rounded-lg text-[var(--text-inactive)] hover:text-[var(--primary)] hover:bg-[var(--surface-container)]" title="Edit entry">
                    <span class="material-symbols-outlined text-[16px]">edit</span>
                  </button>
                  <button type="button" onclick="deleteEvent('${e.docId}')" class="touch-target-sm rounded-lg text-[var(--text-inactive)] hover:text-[var(--red)] hover:bg-[var(--red)]/10" title="Delete entry">
                    <span class="material-symbols-outlined text-[16px]">delete</span>
                  </button>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `;

      var p = document.getElementById('panelEvents');
      if (p) p.innerHTML = html;
    }

    function formatEventText(e) {
      if (e.category === 'mood') return (MOOD_LABELS[e.mood] || e.mood || '') + (e.note ? ' — ' + e.note : '');
      if (e.category === 'energy') return (ENERGY_LABELS[e.energy] || e.energy || '') + (e.note ? ' — ' + e.note : '');
      if (e.category === 'habits' || e.category === 'habit') return (e.activity || 'Habit') + (e.on === 'true' || e.on === true ? ' (Done ✓)' : ' (Not Done)') + (e.notes ? ' — ' + e.notes : '');
      if (e.category === 'journal') return (e.subtype === 'win' ? 'WIN: ' : e.subtype === 'lesson' ? 'LESSON: ' : '') + (e.text || e.win || '');
      if (e.category === 'thought') return (e.text || '') + (e.topic ? ' (' + e.topic + ')' : '');
      if (e.category === 'running') return (e.distance_km || '?') + 'km in ' + (e.duration_formatted || ((e.duration_min || 0) + 'min')) + (e.notes ? ' — ' + e.notes : '');
      if (e.category === 'sessions' || e.category === 'session') return (e.activity || 'Focus Session') + (e.mode === 'pomodoro' ? ' [Pomodoro]' : '') + ((e.duration_min || e.duration) ? ' — ' + (e.duration_min || e.duration) + 'm' : '');
      if (e.category === 'tasks') return (e.title || '') + (e.deadline ? ' due ' + e.deadline : '') + (e.status === 'done' ? ' (Done ✓)' : '');
      if (e.category === 'metrics') {
        if (e.type === 'checkin') return 'Morning Check-in' + (e.note ? ' — ' + e.note : '');
        if (e.type === 'checkout') return 'Evening Check-out' + (e.note ? ' — ' + e.note : '');
        if (e.pushups) return e.pushups + ' Pushups completed';
        if (e.doom_scroll) return e.doom_scroll + 'm Doom scrolling';
        return JSON.stringify(e);
      }
      return e.text || e.note || e.title || JSON.stringify(e).slice(0, 80);
    }

    async function deleteEvent(docId) {
      if (!confirm('Permanently delete this activity?')) return;
      if (_isLocalDemo) {
        _eventsCache = _eventsCache.filter(function(x) { return x.docId !== docId; });
        refreshActivePanel();
        toast('Activity removed');
        return;
      }
      var col = userCol();
      if (col) {
        await col.doc(docId).delete();
        toast('Activity removed from vault');
      }
    }

    function editEvent(docId) {
      var e = _eventsCache.find(function(x) { return x.docId === docId; });
      if (!e) return toast('Event not found');
      var fields = [];
      if (e.category === 'mood') {
        fields.push({ key: 'mood', label: 'Mood State', type: 'select', value: e.mood || 'happy', options: Object.keys(MOOD_LABELS).map(function(k) { return { label: MOOD_LABELS[k], value: k }; }) });
        fields.push({ key: 'note', label: 'Reflection Note', type: 'textarea', value: e.note || '' });
      } else if (e.category === 'habits' || e.category === 'habit') {
        fields.push({ key: 'activity', label: 'Habit Name', type: 'text', value: e.activity || '' });
        fields.push({ key: 'on', label: 'Completed Status', type: 'select', value: String(e.on === true || e.on === 'true'), options: [{ label: 'Done ✓', value: 'true' }, { label: 'Incomplete', value: 'false' }] });
      } else if (e.category === 'thought') {
        fields.push({ key: 'text', label: 'Thought Spark', type: 'textarea', value: e.text || '' });
        fields.push({ key: 'topic', label: 'Topic / Hashtag', type: 'text', value: e.topic || '' });
      } else if (e.category === 'running') {
        fields.push({ key: 'distance_km', label: 'Distance (km)', type: 'number', value: e.distance_km || 0 });
        fields.push({ key: 'duration_min', label: 'Duration (min)', type: 'number', value: e.duration_min || 0 });
        fields.push({ key: 'notes', label: 'Route Notes', type: 'textarea', value: e.notes || '' });
      } else if (e.category === 'tasks') {
        fields.push({ key: 'title', label: 'Task Title', type: 'text', value: e.title || '' });
        fields.push({ key: 'deadline', label: 'Deadline', type: 'date', value: e.deadline || '' });
        fields.push({ key: 'priority', label: 'Priority', type: 'select', value: e.priority || 'medium', options: [{ label: '🔴 High', value: 'high' }, { label: '🟡 Medium', value: 'medium' }, { label: '🟢 Low', value: 'low' }] });
      } else {
        fields.push({ key: 'text', label: 'Content Notes', type: 'textarea', value: e.text || e.note || e.title || '' });
      }

      showModal('Edit ' + (e.category || 'Event').toUpperCase(), fields, async function(d) {
        if (_isLocalDemo) {
          Object.assign(e, d);
          refreshActivePanel();
          toast('Changes saved locally');
          return;
        }
        var col = userCol();
        if (col) {
          await col.doc(docId).update(d);
          toast('Changes saved to Firestore');
        }
      });
    }

    // --- TASK DISCIPLINE PANEL RENDERER ---
    function loadTasks() {
      renderTasksData();
    }

    function renderTasksData() {
      var tasks = eventsFor('tasks');
      var today = localDate();
      var filtered = tasks;

      if (_taskFilter === 'today') filtered = tasks.filter(function(t) { return t.deadline === today && t.status !== 'done'; });
      else if (_taskFilter === 'upcoming') filtered = tasks.filter(function(t) { return t.deadline > today && t.status !== 'done'; });
      else if (_taskFilter === 'overdue') filtered = tasks.filter(function(t) { return t.deadline && t.deadline < today && t.status !== 'done'; });
      else if (_taskFilter === 'done') filtered = tasks.filter(function(t) { return t.status === 'done'; });

      filtered.sort(function(a,b) { return (a.deadline || 'z').localeCompare(b.deadline || 'z'); });
      var total = tasks.length;
      var done = tasks.filter(function(t) { return t.status === 'done'; }).length;
      var pct = total > 0 ? Math.round(done / total * 100) : 0;

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-4 shadow-xs" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[18px] text-[var(--green)]">check_circle</span>
                <span>Task Discipline Dashboard</span>
              </h2>
              <div class="text-[11px] font-mono text-[var(--text-inactive)] mt-0.5">${done} of ${total} finished (${pct}%)</div>
            </div>
            <button type="button" onclick="openCreateTaskModal()" class="h-9 px-3.5 rounded-xl bg-[var(--primary)] hover:bg-[var(--primary-hover)] text-[var(--on-primary)] text-xs font-bold flex items-center gap-1.5 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">add</span>
              <span>New Target</span>
            </button>
          </div>

          <!-- Progress Bar -->
          <div class="w-full h-2 rounded-full bg-[var(--surface-container)] overflow-hidden">
            <div class="h-full bg-[var(--green)] rounded-full transition-all duration-300" style="width: ${pct}%"></div>
          </div>

          <!-- Filter Chips -->
          <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            ${['all', 'today', 'upcoming', 'overdue', 'done'].map(function(f) {
              var isSel = _taskFilter === f;
              return `
                <button type="button" onclick="_taskFilter='${f}'; renderTasksData();"
                        class="h-7 px-2.5 rounded-lg text-xs font-bold shrink-0 transition-all ${isSel ? 'bg-[var(--primary)] text-[var(--on-primary)] shadow-xs' : 'bg-[var(--surface-container)] text-[var(--text-inactive)] hover:text-[var(--text)] border border-[var(--border)]'} active:scale-95">
                  ${f.toUpperCase()}
                </button>
              `;
            }).join('')}
          </div>
        </div>

        <!-- Task Items -->
        <div class="space-y-2">
          ${filtered.length === 0 ? `
            <div class="p-8 text-center bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl text-xs text-[var(--text-inactive)]">
              No tasks match filter "${_taskFilter}". Tap "+ New Target" to create one.
            </div>
          ` : filtered.map(function(t) {
            var isDone = t.status === 'done';
            var overdue = t.deadline && t.deadline < today && !isDone;
            var prioColor = t.priority === 'high' ? 'bg-red-500/15 text-red-400' : (t.priority === 'medium' ? 'bg-amber-500/15 text-amber-400' : 'bg-emerald-500/15 text-emerald-400');
            return `
              <div class="flex items-center justify-between gap-3 p-3.5 rounded-2xl bg-[var(--surface-container-low)] border border-[var(--border)] hover:border-[var(--primary)]/30 transition-all text-left">
                <div class="flex items-center gap-3 min-w-0 flex-1">
                  <button type="button" onclick="toggleTask('${t.id || t.docId}', ${isDone})" class="shrink-0 w-8 h-8 rounded-lg flex items-center justify-center text-[var(--text-inactive)] hover:text-[var(--green)] active:scale-90 transition-transform">
                    <span class="material-symbols-outlined text-[22px] ${isDone ? 'text-[var(--green)]' : 'text-[var(--text-inactive)]'}">
                      ${isDone ? 'check_circle' : 'radio_button_unchecked'}
                    </span>
                  </button>
                  <div class="min-w-0 flex-1">
                    <h4 class="text-xs font-bold text-[var(--text)] ${isDone ? 'line-through opacity-60' : ''} truncate">${cleanText(t.title)}</h4>
                    <div class="flex items-center gap-2 mt-1 text-[10px] font-mono text-[var(--text-inactive)]">
                      <span class="px-1.5 py-0.2 rounded font-bold uppercase ${prioColor}">${t.priority || 'medium'}</span>
                      ${t.deadline ? `<span class="${overdue ? 'text-[var(--red)] font-bold' : ''}">Due: ${t.deadline}</span>` : ''}
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-1 shrink-0">
                  <button type="button" onclick="deleteEvent('${t.docId}')" class="touch-target-sm rounded-lg text-[var(--text-inactive)] hover:text-[var(--red)] hover:bg-[var(--red)]/10">
                    <span class="material-symbols-outlined text-[16px]">delete</span>
                  </button>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `;

      var p = document.getElementById('panelTasks');
      if (p) p.innerHTML = html;
    }

    async function toggleTask(id, wasDone) {
      var doc = _eventsCache.find(function(e) { return e.id === id || e.docId === id; });
      if (!doc) return;
      if (_isLocalDemo) {
        doc.status = wasDone ? 'pending' : 'done';
        refreshActivePanel();
        playChime(wasDone ? 'step' : 'complete');
        return;
      }
      var col = userCol();
      if (col && doc.docId) {
        playChime(wasDone ? 'step' : 'complete');
        await col.doc(doc.docId).update({
          status: wasDone ? 'pending' : 'done',
          completed_at: wasDone ? null : new Date().toISOString()
        });
      }
    }

    // --- RUNTIME TRACKER PANEL RENDERER ---
    function loadRuns() {
      renderRunsData();
    }

    function fmtPace(distKm, durMin) {
      var dist = parseFloat(distKm);
      var dur = parseFloat(durMin);
      if (!dist || dist <= 0 || !dur || dur <= 0) return '--';
      var paceDec = dur / dist;
      var pMin = Math.floor(paceDec);
      var pSec = Math.round((paceDec - pMin) * 60);
      if (pSec === 60) { pMin++; pSec = 0; }
      return pMin + "'" + String(pSec).padStart(2, '0') + '" /km';
    }

    function renderRunsData() {
      var runs = _eventsCache.filter(function(e) { return e.category === 'running'; }).sort(function(a,b) { return eTs(b) - eTs(a); });
      var totalRuns = runs.length;
      var totalDist = runs.reduce(function(acc, r) { return acc + (parseFloat(r.distance_km) || 0); }, 0);
      var totalDur = runs.reduce(function(acc, r) { return acc + (parseInt(r.duration_min) || 0); }, 0);
      var overallPace = fmtPace(totalDist, totalDur);

      var nowMs = Date.now();
      var d7Ago = new Date(nowMs - 7 * 86400000).toISOString().slice(0, 10);
      var d30Ago = new Date(nowMs - 30 * 86400000).toISOString().slice(0, 10);
      var runs7d = runs.filter(function(r) { return (r.date || '') >= d7Ago; });
      var runs30d = runs.filter(function(r) { return (r.date || '') >= d30Ago; });
      var dist7d = runs7d.reduce(function(a, r) { return a + (parseFloat(r.distance_km) || 0); }, 0);
      var dist30d = runs30d.reduce(function(a, r) { return a + (parseFloat(r.distance_km) || 0); }, 0);

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-4 shadow-xs" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[18px] text-[var(--green)]">directions_run</span>
              <span>Runtime Tracker &amp; Pace</span>
            </h2>
            <button type="button" onclick="openRunModal()" class="h-9 px-3.5 rounded-xl bg-[var(--green)] hover:bg-[var(--green)]/90 text-black text-xs font-bold flex items-center gap-1.5 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">add</span>
              <span>Log Run</span>
            </button>
          </div>

          <!-- Stats Deck -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Total Dist</div>
              <div class="text-xl font-mono font-extrabold text-[var(--green)] mt-1">${totalDist.toFixed(1)} <span class="text-xs">km</span></div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">${totalRuns} total run(s)</div>
            </div>
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Avg Pace</div>
              <div class="text-xl font-mono font-extrabold text-[var(--text)] mt-1">${overallPace}</div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">overall endurance</div>
            </div>
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Past 7d</div>
              <div class="text-xl font-mono font-extrabold text-[var(--primary)] mt-1">${dist7d.toFixed(1)} <span class="text-xs">km</span></div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">${runs7d.length} run(s)</div>
            </div>
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Past 30d</div>
              <div class="text-xl font-mono font-extrabold text-[var(--yellow)] mt-1">${dist30d.toFixed(1)} <span class="text-xs">km</span></div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">${runs30d.length} run(s)</div>
            </div>
          </div>

          <!-- Distance Bar Chart Container -->
          ${totalRuns > 0 ? `
            <div class="pt-2">
              <div class="text-[11px] uppercase font-bold text-[var(--text-inactive)] tracking-wider mb-2">Distance History (Last 14 Runs)</div>
              <div class="h-44 w-full"><canvas id="chartRuns"></canvas></div>
            </div>
          ` : ''}
        </div>

        <!-- Run Logs Stream -->
        <div class="space-y-2">
          ${runs.length === 0 ? `
            <div class="p-8 text-center bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl text-xs text-[var(--text-inactive)]">
              No road runs logged yet. Tap "Log Run" to begin your streak.
            </div>
          ` : runs.map(function(r) {
            var dist = parseFloat(r.distance_km) || 0;
            var dur = parseInt(r.duration_min) || 0;
            var pace = fmtPace(dist, dur);
            return `
              <div class="flex items-center justify-between gap-3 p-3.5 rounded-2xl bg-[var(--surface-container-low)] border border-[var(--border)] border-l-4 border-l-emerald-500 hover:border-[var(--primary)]/30 transition-all text-left">
                <div class="min-w-0 flex-1">
                  <div class="flex items-baseline gap-2">
                    <span class="text-base font-extrabold font-mono text-[var(--green)]">${dist.toFixed(2)} km</span>
                    <span class="text-xs font-mono text-[var(--text)]">${dur}m</span>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-[var(--surface-container)] text-[var(--text-inactive)]">${pace}</span>
                  </div>
                  <div class="text-[11px] font-mono text-[var(--text-inactive)] mt-1">${fmtDate(r.date)} · ${fmtTime(r.ts)}</div>
                  ${r.notes ? `<p class="text-xs text-[var(--text)] mt-1 font-medium bg-[var(--surface-container)] px-2.5 py-1.5 rounded-lg">${cleanText(r.notes)}</p>` : ''}
                </div>
                <button type="button" onclick="deleteEvent('${r.docId}')" class="touch-target-sm rounded-lg text-[var(--text-inactive)] hover:text-[var(--red)] hover:bg-[var(--red)]/10">
                  <span class="material-symbols-outlined text-[16px]">delete</span>
                </button>
              </div>
            `;
          }).join('')}
        </div>
      `;

      var p = document.getElementById('panelRuns');
      if (p) {
        p.innerHTML = html;
        if (totalRuns > 0) renderRunsChart(runs);
      }
    }

    function renderRunsChart(runs) {
      if (typeof Chart === 'undefined') return;
      if (_chartInstances.runs) _chartInstances.runs.destroy();
      var canvas = document.getElementById('chartRuns');
      if (!canvas) return;
      var chrono = runs.slice().reverse().slice(-14);
      var labels = chrono.map(function(r) { return (r.date || '').slice(5) || fmtTime(r.ts); });
      var data = chrono.map(function(r) { return parseFloat(r.distance_km) || 0; });

      _chartInstances.runs = new Chart(canvas, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: 'Distance (km)',
            data: data,
            backgroundColor: 'rgba(52, 211, 153, 0.45)',
            borderColor: 'rgba(52, 211, 153, 1)',
            borderWidth: 1.5,
            borderRadius: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            x: { ticks: { color: '#94a3b8', font: { family: 'JetBrains Mono', size: 10 } }, grid: { color: 'rgba(255,255,255,0.05)' } },
            y: { ticks: { color: '#94a3b8', font: { family: 'JetBrains Mono', size: 10 } }, grid: { color: 'rgba(255,255,255,0.05)' } }
          }
        }
      });
    }

    // --- ANALYTICS PANEL RENDERER ---
    function loadAnalytics() {
      var html = `
        <div class="space-y-4">
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 shadow-xs space-y-4" style="box-shadow: var(--card-shadow);">
            <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">monitoring</span>
              <span>Cadence Analytics &amp; Visual Progression</span>
            </h2>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <div class="p-3.5 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
                <div class="text-[11px] font-bold text-[var(--text-inactive)] uppercase tracking-wider mb-2">14-Day Activity Cadence</div>
                <div class="h-44 w-full"><canvas id="chartDaily"></canvas></div>
              </div>
              <div class="p-3.5 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
                <div class="text-[11px] font-bold text-[var(--text-inactive)] uppercase tracking-wider mb-2">Focus Minutes Flow</div>
                <div class="h-44 w-full"><canvas id="chartSessions"></canvas></div>
              </div>
            </div>
          </div>

          <!-- Habit Heatmap Card -->
          <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between">
              <h3 class="text-xs font-bold text-[var(--text)] uppercase tracking-wider">26-Week Consistency Matrix</h3>
              <div class="flex items-center gap-1.5 text-[10px] font-mono text-[var(--text-inactive)]">
                <span>Less</span>
                <span class="w-2.5 h-2.5 rounded-xs bg-[var(--surface-container-high)]"></span>
                <span class="w-2.5 h-2.5 rounded-xs bg-sky-500/25"></span>
                <span class="w-2.5 h-2.5 rounded-xs bg-sky-500/60"></span>
                <span class="w-2.5 h-2.5 rounded-xs bg-sky-500"></span>
                <span>More</span>
              </div>
            </div>
            <div id="heatmapContainer" class="overflow-x-auto py-2"></div>
          </div>
        </div>
      `;

      var p = document.getElementById('panelAnalytics');
      if (p) {
        p.innerHTML = html;
        renderAnalyticsCharts();
        renderHeatmap();
      }
    }

    function renderAnalyticsCharts() {
      if (typeof Chart === 'undefined') return;
      // 1. Daily Activity Bar Chart
      var byDate = {};
      _eventsCache.forEach(function(e) { byDate[e.date] = (byDate[e.date] || 0) + 1; });
      var today = new Date();
      var labels = [];
      var data = [];
      for (var i = 13; i >= 0; i--) {
        var d = new Date(today);
        d.setDate(d.getDate() - i);
        var ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
        labels.push(ds.slice(5));
        data.push(byDate[ds] || 0);
      }

      if (_chartInstances.daily) _chartInstances.daily.destroy();
      var dCanvas = document.getElementById('chartDaily');
      if (dCanvas) {
        _chartInstances.daily = new Chart(dCanvas, {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: 'Events',
              data: data,
              backgroundColor: 'rgba(56, 189, 248, 0.5)',
              borderColor: 'rgba(56, 189, 248, 1)',
              borderWidth: 1,
              borderRadius: 4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              x: { ticks: { color: '#94a3b8', font: { family: 'JetBrains Mono', size: 9 } }, grid: { color: 'rgba(255,255,255,0.05)' } },
              y: { ticks: { color: '#94a3b8', font: { family: 'JetBrains Mono', size: 9 } }, grid: { color: 'rgba(255,255,255,0.05)' } }
            }
          }
        });
      }

      // 2. Session Minutes Line Chart
      var sesByDate = {};
      _eventsCache.filter(function(e) { return (e.category === 'session' || e.category === 'sessions') && e.duration_min; }).forEach(function(e) {
        sesByDate[e.date] = (sesByDate[e.date] || 0) + (parseInt(e.duration_min) || 0);
      });
      var sLabels = labels;
      var sData = labels.map(function(lbl) {
        var fullDate = Object.keys(sesByDate).find(function(k) { return k.endsWith(lbl); });
        return fullDate ? sesByDate[fullDate] : 0;
      });

      if (_chartInstances.sessions) _chartInstances.sessions.destroy();
      var sCanvas = document.getElementById('chartSessions');
      if (sCanvas) {
        _chartInstances.sessions = new Chart(sCanvas, {
          type: 'line',
          data: {
            labels: sLabels,
            datasets: [{
              label: 'Minutes',
              data: sData,
              borderColor: '#38bdf8',
              backgroundColor: 'rgba(56, 189, 248, 0.1)',
              fill: true,
              tension: 0.35,
              borderWidth: 2
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              x: { ticks: { color: '#94a3b8', font: { family: 'JetBrains Mono', size: 9 } }, grid: { color: 'rgba(255,255,255,0.05)' } },
              y: { ticks: { color: '#94a3b8', font: { family: 'JetBrains Mono', size: 9 } }, grid: { color: 'rgba(255,255,255,0.05)' } }
            }
          }
        });
      }
    }

    function renderHeatmap() {
      var container = document.getElementById('heatmapContainer');
      if (!container) return;
      var today = new Date();
      var habitDates = {};
      _eventsCache.filter(function(e) { return (e.category === 'habits' || e.category === 'habit') && (e.on === 'true' || e.on === true); }).forEach(function(e) {
        habitDates[e.date] = (habitDates[e.date] || 0) + 1;
      });

      var html = '<div class="flex gap-1">';
      for (var w = 25; w >= 0; w--) {
        html += '<div class="flex flex-col gap-1">';
        for (var d = 0; d < 7; d++) {
          var date = new Date(today);
          date.setDate(date.getDate() - ((w * 7) + d));
          var ds = date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
          var count = habitDates[ds] || 0;
          var lvl = count === 0 ? '' : (count === 1 ? 'l1' : (count === 2 ? 'l2' : (count <= 3 ? 'l3' : 'l4')));
          html += '<div class="heatmap-cell ' + lvl + '" title="' + ds + ': ' + count + ' habit(s) complete"></div>';
        }
        html += '</div>';
      }
      html += '</div>';
      container.innerHTML = html;
    }

    // --- DAILY REVIEW PANEL RENDERER ---
    function loadDay() {
      renderDayData();
    }
    function renderDayData() {
      var days = {};
      _eventsCache.forEach(function(e) {
        days[e.date] = (days[e.date] || { total: 0, cats: {} });
        days[e.date].total++;
        days[e.date].cats[e.category] = (days[e.date].cats[e.category] || 0) + 1;
      });
      var dates = Object.keys(days).sort().reverse();

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-3 shadow-xs" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[18px] text-[var(--yellow)]">calendar_today</span>
              <span>Daily Review Spine (${dates.length} Days Tracked)</span>
            </h2>
          </div>
          <p class="text-xs text-[var(--text-inactive)]">Click any day to open its chronological inspection timeline.</p>
        </div>

        <div class="space-y-2">
          ${dates.slice(0, 60).map(function(d) {
            var info = days[d];
            var top = Object.keys(info.cats).sort(function(a, b) { return info.cats[b] - info.cats[a]; })[0] || 'general';
            return `
              <div onclick="openDetail('${d}', dayDetailHtml('${d}'))"
                   class="flex items-center justify-between p-3.5 rounded-2xl bg-[var(--surface-container-low)] border border-[var(--border)] hover:border-[var(--primary)]/40 cursor-pointer active:scale-98 transition-all text-left">
                <div>
                  <h4 class="text-xs font-bold text-[var(--text)] font-mono">${fmtDate(d)} (${d})</h4>
                  <p class="text-[11px] text-[var(--text-inactive)] mt-0.5">${info.total} activity record(s) · top discipline: <span class="font-bold text-[var(--primary)] font-mono">${top.toUpperCase()}</span></p>
                </div>
                <span class="material-symbols-outlined text-[18px] text-[var(--text-inactive)]">chevron_right</span>
              </div>
            `;
          }).join('') || '<div class="p-8 text-center text-xs text-[var(--text-inactive)]">No dates logged yet.</div>'}
        </div>
      `;

      var p = document.getElementById('panelDay');
      if (p) p.innerHTML = html;
    }

    function dayDetailHtml(date) {
      var rows = _eventsCache.filter(function(e) { return e.date === date; }).sort(function(a, b) { return eTs(b) - eTs(a); });
      var html = '<div class="space-y-2 text-left">';
      rows.forEach(function(e) {
        var iconName = CATEGORY_ICONS[e.category] || 'category';
        html += `
          <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)] space-y-1">
            <div class="flex items-center justify-between text-[10px] font-mono text-[var(--text-inactive)]">
              <span class="uppercase font-bold text-[var(--primary)]">${cleanText(e.category)}</span>
              <span>${fmtTime(e.ts)}</span>
            </div>
            <p class="text-xs font-semibold text-[var(--text)]">${cleanText(formatEventText(e))}</p>
          </div>
        `;
      });
      html += '</div>';
      return html;
    }

    // --- SCREEN TIME PANEL RENDERER ---
    function loadUsage() {
      renderUsageData();
    }
    function renderUsageData() {
      var docs = _usageCache.slice().sort(function(a, b) { return (a.date || '').localeCompare(b.date || ''); }).reverse();
      var viewD = getViewDate();
      var selDoc = docs.find(function(d) { return (d.date || d.docId) === viewD; });
      var sum = (selDoc && selDoc.summary) || {};

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-4 shadow-xs" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[18px] text-[var(--accent-orange)]">hourglass_empty</span>
              <span>Digital Stillness &amp; Screen Time</span>
            </h2>
            <button type="button" onclick="document.getElementById('usageFileInput').click()" class="h-8 px-2.5 rounded-lg bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-bold text-[var(--text)] flex items-center gap-1">
              <span class="material-symbols-outlined text-[15px]">upload_file</span>
              <span>Import JSON</span>
            </button>
          </div>
          <input type="file" id="usageFileInput" accept=".json" style="display:none" onchange="handleUsageFileUpload(event)"/>

          <!-- Selected Day Screen Breakdown -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Screen Time</div>
              <div class="text-xl font-mono font-extrabold text-[var(--accent-orange)] mt-1">${fmtSecs(sum.total_screen_time_sec)}</div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">for ${viewD}</div>
            </div>
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Sessions</div>
              <div class="text-xl font-mono font-extrabold text-[var(--text)] mt-1">${sum.session_count || 0}</div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">pickups / unlocks</div>
            </div>
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">App Switches</div>
              <div class="text-xl font-mono font-extrabold text-[var(--text)] mt-1">${sum.app_switches || 0}</div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">task context shifts</div>
            </div>
            <div class="p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Apps Used</div>
              <div class="text-xl font-mono font-extrabold text-[var(--text)] mt-1">${sum.unique_apps_used || (selDoc && (selDoc.usage || []).length) || 0}</div>
              <div class="text-[10px] text-[var(--text-inactive)] mt-0.5">unique applications</div>
            </div>
          </div>

          <!-- Apps Progress Distribution -->
          ${selDoc && (selDoc.usage || []).length > 0 ? `
            <div class="space-y-2 pt-2">
              <div class="text-[11px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">Top Application Stillness</div>
              <div class="space-y-2">
                ${selDoc.usage.slice(0, 10).map(function(u) {
                  var dur = u.total_duration_sec || u.duration_sec || 0;
                  var pct = Math.min(100, Math.max(4, Math.round(dur / Math.max(1, sum.total_screen_time_sec) * 100)));
                  return `
                    <div class="space-y-1">
                      <div class="flex items-center justify-between text-xs font-semibold">
                        <span class="text-[var(--text)] truncate max-w-[200px]">${cleanText(u.app || u.package)}</span>
                        <span class="font-mono text-[var(--text-inactive)]">${fmtSecs(dur)} (${pct}%)</span>
                      </div>
                      <div class="w-full h-1.5 rounded-full bg-[var(--surface-container)] overflow-hidden">
                        <div class="h-full bg-[var(--accent-orange)] rounded-full" style="width: ${pct}%"></div>
                      </div>
                    </div>
                  `;
                }).join('')}
              </div>
            </div>
          ` : `
            <div class="p-6 text-center text-xs text-[var(--text-inactive)] bg-[var(--surface-container)] rounded-xl">
              No granular app breakdown available for ${viewD}.
            </div>
          `}
        </div>
      `;

      var p = document.getElementById('panelUsage');
      if (p) p.innerHTML = html;
    }

    function handleUsageFileUpload(event) {
      var file = event.target.files && event.target.files[0];
      if (!file) return;
      var reader = new FileReader();
      reader.onload = function(e) {
        try {
          var json = JSON.parse(e.target.result);
          var date = json.date || file.name.replace('.json', '');
          toast('Usage JSON loaded for ' + date);
          _usageCache.unshift(Object.assign({ docId: date, date: date }, json));
          renderUsageData();
        } catch (_) {
          toast('Failed to parse JSON file');
        }
      };
      reader.readAsText(file);
    }

    // --- BHAGAVAD GITA CONTEMPLATION ENGINE ---
    var _gitaData = null;
    var _gitaIndex = 0;
    var _gitaLoading = false;
    var GITA_CHAPTERS = [
      { ch: 1, name: 'Arjuna Vishada Yoga', verses: 47 },
      { ch: 2, name: 'Sankhya Yoga', verses: 72 },
      { ch: 3, name: 'Karma Yoga', verses: 43 },
      { ch: 4, name: 'Jnana Karma Sanyasa Yoga', verses: 42 },
      { ch: 5, name: 'Karma Sanyasa Yoga', verses: 29 },
      { ch: 6, name: 'Dhyana Yoga', verses: 47 },
      { ch: 7, name: 'Jnana Vijnana Yoga', verses: 30 },
      { ch: 8, name: 'Akshara Brahma Yoga', verses: 28 },
      { ch: 9, name: 'Raja Vidya Raja Guhya Yoga', verses: 34 },
      { ch: 10, name: 'Vibhuti Yoga', verses: 42 },
      { ch: 11, name: 'Vishwarupa Darshana Yoga', verses: 55 },
      { ch: 12, name: 'Bhakti Yoga', verses: 20 },
      { ch: 13, name: 'Kshetra Kshetragna Vibhaga Yoga', verses: 35 },
      { ch: 14, name: 'Gunatraya Vibhaga Yoga', verses: 27 },
      { ch: 15, name: 'Purushottama Yoga', verses: 20 },
      { ch: 16, name: 'Daivasura Sampad Vibhaga Yoga', verses: 24 },
      { ch: 17, name: 'Shraddhatraya Vibhaga Yoga', verses: 28 },
      { ch: 18, name: 'Moksha Sanyasa Yoga', verses: 78 }
    ];

    function loadGita() {
      if (!_gitaData) {
        if (!_gitaLoading) {
          _gitaLoading = true;
          document.getElementById('panelGita').innerHTML = `
            <div class="p-12 text-center bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl space-y-3">
              <span class="material-symbols-outlined text-4xl text-[var(--yellow)] animate-spin">sync</span>
              <h4 class="text-sm font-bold text-[var(--text)]">Loading Bhagavad Gita...</h4>
              <p class="text-xs text-[var(--text-inactive)]">Retrieving 701 sacred Sanskrit shlokas and commentaries</p>
            </div>
          `;
          fetch('gita.json').then(function(res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
          }).then(function(data) {
            _gitaData = data;
            _gitaLoading = false;
            var savedIdx = parseInt(localStorage.getItem('v07_gita_index'));
            _gitaIndex = (!isNaN(savedIdx) && savedIdx >= 0 && savedIdx < _gitaData.length) ? savedIdx : 0;
            renderGitaPanel();
          }).catch(function(err) {
            _gitaLoading = false;
            document.getElementById('panelGita').innerHTML = `
              <div class="p-10 text-center bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl space-y-3">
                <span class="material-symbols-outlined text-3xl text-[var(--red)]">error</span>
                <h4 class="text-sm font-bold text-[var(--text)]">Failed to load gita.json</h4>
                <p class="text-xs text-[var(--text-inactive)]">${esc(err.message || String(err))}</p>
                <button type="button" onclick="loadGita()" class="px-4 py-2 rounded-xl bg-[var(--primary)] text-[var(--on-primary)] text-xs font-bold">Retry</button>
              </div>
            `;
          });
        }
        return;
      }
      renderGitaPanel();
    }

    function renderGitaPanel() {
      if (!_gitaData || !_gitaData.length) return;
      var v = _gitaData[_gitaIndex] || _gitaData[0];
      var chNum = parseInt(v.chapter);
      var vsNum = parseInt(v.verse);
      var chMeta = GITA_CHAPTERS.find(function(c) { return c.ch === chNum; }) || { ch: chNum, name: 'Chapter ' + chNum, verses: 47 };

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-6 space-y-5 shadow-xs text-left" style="box-shadow: var(--card-shadow);">
          <!-- Chapter & Verse Selector Bar -->
          <div class="flex items-center justify-between gap-3 flex-wrap pb-3 border-b border-[var(--divider)]">
            <div class="flex items-center gap-2">
              <span class="font-gita text-xl font-bold text-[var(--yellow)]">🕉 श्रीमद्भगवद्गीता</span>
            </div>
            <div class="flex items-center gap-2 flex-wrap">
              <select onchange="jumpGitaChapter(this.value)" class="px-2.5 py-1.5 rounded-lg bg-[var(--surface-container)] border border-[var(--border)] text-xs font-bold text-[var(--text)] cursor-pointer">
                ${GITA_CHAPTERS.map(function(c) {
                  return `<option value="${c.ch}" ${c.ch === chNum ? 'selected' : ''}>Ch ${c.ch}: ${c.name}</option>`;
                }).join('')}
              </select>
              <select onchange="jumpGitaVerse(${chNum}, this.value)" class="px-2.5 py-1.5 rounded-lg bg-[var(--surface-container)] border border-[var(--border)] text-xs font-bold text-[var(--text)] cursor-pointer">
                ${Array.from({ length: chMeta.verses }, function(_, i) {
                  var vn = i + 1;
                  return `<option value="${vn}" ${vn === vsNum ? 'selected' : ''}>Verse ${vn} / ${chMeta.verses}</option>`;
                }).join('')}
              </select>
            </div>
          </div>

          <!-- Chapter Badge -->
          <div class="text-[11px] font-mono text-[var(--yellow)] uppercase font-bold tracking-wider">
            CHAPTER ${chNum}: ${cleanText(chMeta.name)} · VERSE ${vsNum}
          </div>

          <!-- Sacred Sanskrit Shloka Box -->
          ${v.shloka && v.shloka.text ? `
            <div class="p-5 rounded-2xl bg-[var(--surface-container)] border border-[var(--border)] text-center leading-loose font-gita text-base sm:text-lg text-[var(--text)] whitespace-pre-line shadow-xs">
              ${cleanText(v.shloka.text)}
            </div>
          ` : ''}

          <!-- English Translation -->
          ${v.english_sivananda && v.english_sivananda.text ? `
            <div class="space-y-1.5 pt-1">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">English Translation (Swami Sivananda)</div>
              <p class="text-sm text-[var(--text)] leading-relaxed font-sans">${cleanText(v.english_sivananda.text)}</p>
            </div>
          ` : ''}

          <!-- Hindi Translation -->
          ${v.hindi_translation && v.hindi_translation.text ? `
            <div class="space-y-1.5 pt-1">
              <div class="text-[10px] uppercase font-bold text-[var(--text-inactive)] tracking-wider">हिन्दी अनुवाद</div>
              <p class="text-sm text-[var(--text)] leading-relaxed font-sans">${cleanText(v.hindi_translation.text)}</p>
            </div>
          ` : ''}

          <!-- Collapsible Commentaries -->
          ${v.english_gambirananda && v.english_gambirananda.text ? `
            <details class="group space-y-2 pt-2 border-t border-[var(--divider)] text-xs">
              <summary class="flex items-center justify-between cursor-pointer py-1 font-bold text-[11px] text-[var(--text-inactive)] group-open:text-[var(--primary)]">
                <span>📜 Sri Shankaracharya Commentary</span>
                <span class="material-symbols-outlined text-[16px] group-open:rotate-180 transition-transform">expand_more</span>
              </summary>
              <div class="p-3 rounded-xl bg-[var(--surface-container)] text-[var(--text-inactive)] leading-relaxed whitespace-pre-line">
                ${cleanText(v.english_gambirananda.text)}
              </div>
            </details>
          ` : ''}

          <!-- Navigation & Action Steppers -->
          <div class="flex items-center gap-2 pt-2 flex-wrap">
            <button type="button" onclick="navGita('prev')" class="h-10 px-4 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-bold flex items-center gap-1 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">chevron_left</span>
              <span>Prev Verse</span>
            </button>
            <button type="button" onclick="markGitaRead()" class="h-10 px-5 rounded-xl bg-[var(--yellow)] hover:bg-[var(--yellow)]/90 text-black text-xs font-bold flex items-center gap-1.5 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">check</span>
              <span>Mark as Read</span>
            </button>
            <button type="button" onclick="openGitaReflectModal(${chNum}, ${vsNum})" class="h-10 px-4 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-bold text-[var(--primary)] flex items-center gap-1.5 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">edit_note</span>
              <span>Reflect</span>
            </button>
            <button type="button" onclick="navGita('next')" class="h-10 px-4 rounded-xl bg-[var(--surface-container)] hover:bg-[var(--surface-container-high)] border border-[var(--border)] text-xs font-bold flex items-center gap-1 ml-auto active:scale-95 transition-all">
              <span>Next Verse</span>
              <span class="material-symbols-outlined text-[16px]">chevron_right</span>
            </button>
          </div>
        </div>
      `;

      var p = document.getElementById('panelGita');
      if (p) p.innerHTML = html;
    }

    function jumpGitaChapter(ch) {
      if (!_gitaData) return;
      var idx = _gitaData.findIndex(function(v) { return parseInt(v.chapter) === parseInt(ch); });
      if (idx >= 0) {
        _gitaIndex = idx;
        localStorage.setItem('v07_gita_index', _gitaIndex);
        renderGitaPanel();
      }
    }
    function jumpGitaVerse(ch, vs) {
      if (!_gitaData) return;
      var idx = _gitaData.findIndex(function(v) { return parseInt(v.chapter) === parseInt(ch) && parseInt(v.verse) === parseInt(vs); });
      if (idx >= 0) {
        _gitaIndex = idx;
        localStorage.setItem('v07_gita_index', _gitaIndex);
        renderGitaPanel();
      }
    }
    function navGita(dir) {
      if (!_gitaData || !_gitaData.length) return;
      if (dir === 'next') {
        if (_gitaIndex < _gitaData.length - 1) _gitaIndex++;
        else toast('You have reached the end of Bhagavad Gita! 🕉');
      } else {
        if (_gitaIndex > 0) _gitaIndex--;
        else toast('You are at Chapter 1, Verse 1');
      }
      localStorage.setItem('v07_gita_index', _gitaIndex);
      renderGitaPanel();
    }
    function markGitaRead() {
      if (!_gitaData || !_gitaData[_gitaIndex]) return;
      var v = _gitaData[_gitaIndex];
      writeEvent('gita', { chapter: parseInt(v.chapter), verse: parseInt(v.verse), date: getViewDate() });
      toast('Verse ' + v.chapter + '.' + v.verse + ' marked as completed! 🕉');
    }
    function openGitaReflectModal(ch, vs) {
      showModal('Reflect on Ch ' + ch + ', Verse ' + vs, [
        { key: 'thought', label: 'Reflection or Life Principle', type: 'textarea', placeholder: 'What wisdom applies to your life today?' }
      ], function(d) {
        if (!d.thought) return;
        writeEvent('thought', { text: 'Gita Ch ' + ch + ' Vs ' + vs + ': ' + d.thought, chapter: ch, verse: vs });
        toast('Reflection preserved in sanctuary!');
      });
    }

    // --- SETTINGS PANEL RENDERER ---
    function loadSettings() {
      var isDark = document.documentElement.getAttribute('data-theme') !== 'light';
      var user = _currentUser;

      var html = `
        <div class="bg-[var(--surface-container-low)] border border-[var(--border)] rounded-2xl p-4 sm:p-5 space-y-4 shadow-xs text-left" style="box-shadow: var(--card-shadow);">
          <h2 class="text-sm font-bold text-[var(--text)] uppercase tracking-wider flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[18px] text-[var(--primary)]">settings</span>
            <span>Sanctuary Settings &amp; Vault Sync</span>
          </h2>

          <div class="space-y-3 pt-1">
            <div class="flex items-center justify-between p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div>
                <div class="text-xs font-bold text-[var(--text)]">Authenticated Sovereign Account</div>
                <div class="text-[11px] font-mono text-[var(--text-inactive)] mt-0.5">${esc(user ? (user.email || 'Local Demo Account') : 'Not Signed In')}</div>
              </div>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[var(--green)]/15 text-[var(--green)] font-mono">Linked</span>
            </div>

            <div class="flex items-center justify-between p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div>
                <div class="text-xs font-bold text-[var(--text)]">Color Theme</div>
                <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">Switch between Calm Dark and Crisp Light modes</div>
              </div>
              <button type="button" onclick="toggleTheme()" class="h-8 px-3 rounded-xl bg-[var(--surface-container-high)] text-xs font-bold text-[var(--text)] flex items-center gap-1.5 active:scale-95 transition-all">
                <span class="material-symbols-outlined text-[16px]">${isDark ? 'light_mode' : 'dark_mode'}</span>
                <span>${isDark ? 'Switch to Light' : 'Switch to Dark'}</span>
              </button>
            </div>

            <div class="flex items-center justify-between p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div>
                <div class="text-xs font-bold text-[var(--text)]">Export Full Sanctuary JSON</div>
                <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">Backup all events, habits, tasks, runs &amp; reflections</div>
              </div>
              <button type="button" onclick="exportData()" class="h-8 px-3.5 rounded-xl bg-[var(--primary)] hover:bg-[var(--primary-hover)] text-[var(--on-primary)] text-xs font-bold flex items-center gap-1.5 active:scale-95 transition-all">
                <span class="material-symbols-outlined text-[16px]">download</span>
                <span>Export</span>
              </button>
            </div>

            <div class="flex items-center justify-between p-3 rounded-xl bg-[var(--surface-container)] border border-[var(--border)]">
              <div>
                <div class="text-xs font-bold text-[var(--text)]">Keyboard Shortcuts Guide</div>
                <div class="text-[11px] text-[var(--text-inactive)] mt-0.5">Quick single-key workflow shortcuts</div>
              </div>
              <button type="button" onclick="showShortcutsModal()" class="h-8 px-3 rounded-xl bg-[var(--surface-container-high)] text-xs font-bold text-[var(--text)] flex items-center gap-1.5 active:scale-95 transition-all">
                <span class="material-symbols-outlined text-[16px]">keyboard</span>
                <span>View Keys</span>
              </button>
            </div>
          </div>

          <div class="pt-2">
            <button type="button" onclick="logoutUser()" class="w-full py-2.5 rounded-xl bg-[var(--red)]/15 border border-[var(--red)]/30 hover:bg-[var(--red)]/25 text-[var(--red)] font-bold text-xs flex items-center justify-center gap-1.5 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[16px]">logout</span>
              <span>Sign Out of Companion</span>
            </button>
          </div>
        </div>
      `;

      var p = document.getElementById('panelSettings');
      if (p) p.innerHTML = html;
    }

    function toggleTheme() {
      var cur = document.documentElement.getAttribute('data-theme');
      var next = cur === 'light' ? 'dark' : 'light';
      if (next === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
      } else {
        document.documentElement.removeAttribute('data-theme');
      }
      try { localStorage.setItem('v07_theme', next); } catch (_) {}
      var icon = document.getElementById('themeIcon');
      if (icon) icon.textContent = next === 'light' ? 'light_mode' : 'dark_mode';
      if (_currentTab === 'settings') loadSettings();
    }

    function exportData() {
      var all = {
        exported_at: new Date().toISOString(),
        events: _eventsCache,
        usage: _usageCache,
        days: _dayCache,
        gita_read: _gitaReadCache,
        gita_reviews: _gitaReviewsCache
      };
      var jsonStr = JSON.stringify(all, null, 2);
      var blob = new Blob([jsonStr], { type: 'application/json' });
      var a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = 'v07_companion_export_' + localDate() + '.json';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      toast('Full sanctuary backup exported!');
    }

    function showShortcutsModal() {
      var html = `
        <div class="space-y-3 text-left">
          <div class="space-y-1.5">
            <div class="text-[10px] font-bold text-[var(--primary)] uppercase tracking-wider font-mono">Navigation Tabs</div>
            <div class="flex items-center justify-between text-xs py-1 border-b border-[var(--divider)]">
              <span><kbd>1</kbd> to <kbd>9</kbd></span>
              <span class="text-[var(--text-inactive)]">Switch Tabs (Home, Events, Stats, Tasks, Runs, Days, Screen, Gita, Settings)</span>
            </div>
          </div>

          <div class="space-y-1.5">
            <div class="text-[10px] font-bold text-[var(--primary)] uppercase tracking-wider font-mono">Focus Chronometer</div>
            <div class="flex items-center justify-between text-xs py-1 border-b border-[var(--divider)]">
              <span><kbd>Space</kbd></span>
              <span class="text-[var(--text-inactive)]">Start / Pause / Resume Flow</span>
            </div>
            <div class="flex items-center justify-between text-xs py-1 border-b border-[var(--divider)]">
              <span><kbd>s</kbd></span>
              <span class="text-[var(--text-inactive)]">Stop &amp; Log Active Session</span>
            </div>
          </div>

          <div class="space-y-1.5">
            <div class="text-[10px] font-bold text-[var(--primary)] uppercase tracking-wider font-mono">Dates &amp; Reading</div>
            <div class="flex items-center justify-between text-xs py-1 border-b border-[var(--divider)]">
              <span><kbd>[</kbd> / <kbd>]</kbd></span>
              <span class="text-[var(--text-inactive)]">Previous / Next Day or Verse</span>
            </div>
            <div class="flex items-center justify-between text-xs py-1 border-b border-[var(--divider)]">
              <span><kbd>t</kbd></span>
              <span class="text-[var(--text-inactive)]">Reset Date to Today</span>
            </div>
            <div class="flex items-center justify-between text-xs py-1 border-b border-[var(--divider)]">
              <span><kbd>d</kbd></span>
              <span class="text-[var(--text-inactive)]">Open Date Picker</span>
            </div>
          </div>

          <div class="space-y-1.5">
            <div class="text-[10px] font-bold text-[var(--primary)] uppercase tracking-wider font-mono">Quick Logs</div>
            <div class="grid grid-cols-2 gap-2 text-xs">
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>m</kbd></span><span class="text-[var(--text-inactive)]">Mood</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>e</kbd></span><span class="text-[var(--text-inactive)]">Energy</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>h</kbd></span><span class="text-[var(--text-inactive)]">Habit</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>w</kbd></span><span class="text-[var(--text-inactive)]">Win</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>l</kbd></span><span class="text-[var(--text-inactive)]">Lesson</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>j</kbd></span><span class="text-[var(--text-inactive)]">Journal</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>r</kbd></span><span class="text-[var(--text-inactive)]">Run</span></div>
              <div class="flex items-center justify-between py-1 border-b border-[var(--divider)]"><span><kbd>p</kbd></span><span class="text-[var(--text-inactive)]">Pushups</span></div>
            </div>
          </div>
        </div>
      `;
      openDetail('Keyboard Shortcuts Guide', html);
    }

    // Keyboard Shortcuts Listener
    document.addEventListener('keydown', function(e) {
      var tag = (e.target && e.target.tagName) ? e.target.tagName.toLowerCase() : '';
      var isInput = tag === 'input' || tag === 'textarea' || tag === 'select' || (e.target && e.target.isContentEditable);

      if (e.key === 'Escape') {
        if (document.getElementById('modalOverlay') && !document.getElementById('modalOverlay').classList.contains('hidden')) {
          closeModal();
          e.preventDefault();
          return;
        }
        if (document.getElementById('detailOverlay') && !document.getElementById('detailOverlay').classList.contains('hidden')) {
          closeDetail();
          e.preventDefault();
          return;
        }
        if (isInput && e.target) {
          e.target.blur();
          e.preventDefault();
          return;
        }
      }

      if (isInput || e.ctrlKey || e.metaKey || e.altKey) return;
      var k = e.key.toLowerCase();
      var tabs = ['dashboard', 'events', 'analytics', 'tasks', 'runs', 'day', 'usage', 'gita', 'settings'];
      var num = parseInt(e.key);
      if (!isNaN(num) && num >= 1 && num <= 9) {
        switchTab(tabs[num - 1]);
        e.preventDefault();
        return;
      }

      if (e.key === '[' || e.key === 'ArrowLeft') {
        if (_currentTab === 'gita') navGita('prev');
        else shiftViewDate(-1);
        e.preventDefault();
        return;
      }
      if (e.key === ']' || e.key === 'ArrowRight') {
        if (_currentTab === 'gita') navGita('next');
        else shiftViewDate(1);
        e.preventDefault();
        return;
      }
      if (k === 't') { resetToToday(); toast('Date reset to Today'); e.preventDefault(); return; }
      if (k === 'd') { openDatePickerModal(); e.preventDefault(); return; }
      if (e.code === 'Space' || e.key === ' ') {
        e.preventDefault();
        var s = sessionState();
        if (!s.active) sessionStart();
        else if (s.paused) sessionResume();
        else sessionPause();
        return;
      }
      if (k === 's') {
        var s = sessionState();
        if (s.active) { sessionStop(); e.preventDefault(); return; }
      }
      if (k === 'm') { openMoodModal(); e.preventDefault(); return; }
      if (k === 'e') { openEnergyModal(); e.preventDefault(); return; }
      if (k === 'h') { openHabitModal(); e.preventDefault(); return; }
      if (k === 'w') { openWinModal(); e.preventDefault(); return; }
      if (k === 'l') { openLessonModal(); e.preventDefault(); return; }
      if (k === 'j') { openJournalModal(); e.preventDefault(); return; }
      if (k === 'r') { openRunModal(); e.preventDefault(); return; }
      if (k === 'p') { quickPushups(10); e.preventDefault(); return; }
      if (k === 'k') { quickCheckin(); e.preventDefault(); return; }
      if (k === 'o') { quickCheckout(); e.preventDefault(); return; }
      if (e.key === '?') { showShortcutsModal(); e.preventDefault(); return; }
    });

    // --- DEMO SEED STATE FOR LOCAL EXPLORATION ---
    function populateInitialDemoState() {
      if (_eventsCache.length > 0) return;
      var today = localDate();
      _eventsCache = [
        { docId: 'demo_1', id: 'demo_1', category: 'session', activity: 'deepwork', duration_min: 45, date: today, ts: Date.now() - 3600000, type: 'end' },
        { docId: 'demo_2', id: 'demo_2', category: 'tasks', title: 'Refactor Companion UI to Impeccable Standards', priority: 'high', status: 'pending', deadline: today, date: today, ts: Date.now() - 7200000 },
        { docId: 'demo_3', id: 'demo_3', category: 'tasks', title: 'Verify SQLite schema and deleted_tombstones', priority: 'medium', status: 'done', deadline: today, date: today, ts: Date.now() - 10800000 },
        { docId: 'demo_4', id: 'demo_4', category: 'mood', mood: 'happy', note: 'Calm morning cadence and smooth flow', date: today, ts: Date.now() - 14400000 },
        { docId: 'demo_5', id: 'demo_5', category: 'running', distance_km: 5.2, duration_min: 27, notes: 'Morning road pace', date: today, ts: Date.now() - 18000000 },
        { docId: 'demo_6', id: 'demo_6', category: 'habits', activity: 'Meditation & Breathwork', on: true, date: today, ts: Date.now() - 21600000 }
      ];
    }

    // --- ON DOM READY INITIALIZATION ---
    window.addEventListener('DOMContentLoaded', function() {
      var savedTheme = localStorage.getItem('v07_theme');
      if (savedTheme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
      }
      var icon = document.getElementById('themeIcon');
      if (icon) icon.textContent = savedTheme === 'light' ? 'light_mode' : 'dark_mode';

      if (localStorage.getItem('v07_logged_in') === 'true') {
        document.getElementById('authScreen').style.display = 'none';
        document.getElementById('app').classList.remove('hidden');
        var lastTab = localStorage.getItem('v07_active_tab') || 'dashboard';
        switchTab(lastTab);
      }
      sessionEnsureTicker();
    });
  </script>
</body>
</html>
'''

def main():
    target_path = r'D:\software\V07-Android\app\src\main\assets\companion.html'
    v3_target_path = r'D:\software\V07-Android\app\src\main\assets\v3\companion.html'
    
    with open(target_path, 'w', encoding='utf-8') as f:
        f.write(HTML_CONTENT.strip() + '\n')
    print(f"Overhauled companion written to {target_path}")

    # Also sync to v3 assets so LoopbackServer serves it seamlessly
    os.makedirs(os.path.dirname(v3_target_path), exist_ok=True)
    with open(v3_target_path, 'w', encoding='utf-8') as f:
        f.write(HTML_CONTENT.strip() + '\n')
    print(f"Overhauled companion also written to {v3_target_path}")

if __name__ == '__main__':
    main()
