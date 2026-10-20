import os

# generate_v3_full_pages.py
# Complete Impeccable V07 V3 Generator
# - Eliminates empty upper space (compact 52px header, tight safe area geometry)
# - Replaces ALL pop-up modals with full-bleed dedicated views (Focus, Reading, Habits, Screen Time, Mood & Panic, Gita, Write)
# - Connects to native Android platform bridge (Timer, Screen Time Telemetry, SQLite Tasks & Thoughts, KV Storage)
# - Implements hardware back navigation handling (window.handleBackPress)

html_content = r'''<!DOCTYPE html>
<html lang="en" class="">
<head>
  <meta charset="utf-8"/>
  <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover" name="viewport"/>
  <meta content="mobile_tab" name="shell-type"/>
  <title>V07 V3 Sanctuary</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet"/>
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet"/>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    :root {
      /* Warm Paper (Light Mode) Palette - Calm Space System */
      --surface: #f3fbf5;
      --surface-dim: #d3dcd6;
      --surface-bright: #ffffff;
      --surface-container-lowest: #ffffff;
      --surface-container-low: #edf6ef;
      --surface-container: #e7f0e9;
      --surface-container-subtle: #edf6ef;
      --surface-container-high: #e2eae4;
      --surface-container-highest: #dce5de;
      --on-surface: #151d19;
      --on-surface-variant: #3f4945;
      --on-surface-muted: #6f7975;
      --inverse-surface: #2a322e;
      --inverse-on-surface: #eaf3ec;
      --outline: #6f7975;
      --outline-variant: #bfc9c4;
      --surface-tint: #266958;
      --primary: #1c6150;
      --on-primary: #ffffff;
      --primary-container: #397a68;
      --on-primary-container: #c6ffeb;
      --inverse-primary: #92d4be;
      --secondary: #855144;
      --on-secondary: #ffffff;
      --secondary-container: #fdb9a9;
      --on-secondary-container: #79473b;
      --secondary-fixed: #ffdad2;
      --secondary-fixed-dim: #fab6a6;
      --on-secondary-fixed: #341007;
      --tertiary: #535855;
      --tertiary-container: #6b706d;
      --tertiary-fixed: #dfe3e0;
      --tertiary-fixed-dim: #c3c7c4;
      --error: #ba1a1a;
      --on-error: #ffffff;
      --error-container: #ffdad6;
      --on-error-container: #93000a;
      --primary-fixed: #adf0da;
      --primary-fixed-dim: #92d4be;
      --on-primary-fixed: #002019;
      --surface-variant: #dce5de;
      --nav-shadow: 0 8px 24px -2px rgba(32, 40, 36, 0.08), 0 2px 6px -1px rgba(32, 40, 36, 0.04);
      --card-shadow: 0 1px 6px -1px rgba(32, 40, 36, 0.05), 0 1px 2px 0 rgba(32, 40, 36, 0.03);
    }

    html.dark {
      /* Night Ink (Dark Mode) Palette - Paper & Ink After Sunset */
      --surface: #171b19;
      --surface-dim: #101412;
      --surface-bright: #272d29;
      --surface-container-lowest: #202522;
      --surface-container-low: #202522;
      --surface-container: #202522;
      --surface-container-subtle: #272d29;
      --surface-container-high: #272d29;
      --surface-container-highest: #343c37;
      --on-surface: #e9eee9;
      --on-surface-variant: #aab5ae;
      --on-surface-muted: #7d8982;
      --inverse-surface: #e9eee9;
      --inverse-on-surface: #171b19;
      --outline: #343c37;
      --outline-variant: #272d29;
      --surface-tint: #75b7a1;
      --primary: #75b7a1;
      --on-primary: #101412;
      --primary-container: #75b7a1;
      --on-primary-container: #101412;
      --inverse-primary: #1c6150;
      --secondary: #d09a8c;
      --on-secondary: #171b19;
      --secondary-container: #3a2b28;
      --on-secondary-container: #d09a8c;
      --secondary-fixed: #3a2b28;
      --secondary-fixed-dim: #4b271d;
      --on-secondary-fixed: #d09a8c;
      --tertiary: #aab5ae;
      --tertiary-container: #272d29;
      --tertiary-fixed: #272d29;
      --tertiary-fixed-dim: #202522;
      --error: #ffb4ab;
      --on-error: #690005;
      --error-container: #93000a;
      --on-error-container: #ffdad6;
      --primary-fixed: #263d35;
      --primary-fixed-dim: #75b7a1;
      --on-primary-fixed: #e9eee9;
      --surface-variant: #272d29;
      --nav-shadow: 0 8px 24px -2px rgba(0, 0, 0, 0.4), 0 2px 6px -1px rgba(0, 0, 0, 0.25);
      --card-shadow: 0 1px 6px -1px rgba(0, 0, 0, 0.25), 0 1px 2px 0 rgba(0, 0, 0, 0.15);
    }

    @layer base {
      html, body {
        width: 100vw;
        height: 100vh;
        margin: 0;
        padding: 0;
        background-color: var(--surface);
        color: var(--on-surface);
        font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
        overscroll-behavior: none;
        -webkit-tap-highlight-color: transparent;
        overflow-x: hidden;
      }
    }
    ::-webkit-scrollbar { display: none; }
    .no-scrollbar::-webkit-scrollbar { display: none; }
    .no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }

    .material-symbols-outlined {
      font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
      line-height: 1;
      user-select: none;
    }
    .material-symbols-filled {
      font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
      line-height: 1;
      user-select: none;
    }

    .nav-active {
      background-color: var(--surface-container-low);
      color: var(--primary) !important;
    }
    .nav-active .material-symbols-outlined {
      font-variation-settings: 'FILL' 1;
      color: var(--primary);
    }

    .page-tab {
      display: none;
    }
    .page-tab.active {
      display: flex;
    }

    /* Full Page Tool Views */
    .tool-view {
      position: fixed;
      inset: 0;
      z-index: 50;
      background-color: var(--surface);
      display: flex;
      flex-direction: column;
      overflow-y: auto;
      overscroll-behavior-y: contain;
    }
    .tool-view.hidden {
      display: none !important;
    }

    /* Box Breathing Animation */
    @keyframes breathePulse {
      0%, 100% { transform: scale(0.85); opacity: 0.7; }
      50% { transform: scale(1.18); opacity: 1; }
    }
    .animate-breathe {
      animation: breathePulse 8s ease-in-out infinite;
    }
  </style>
  <script id="tailwind-config">
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "surface": "var(--surface)",
            "surface-dim": "var(--surface-dim)",
            "surface-bright": "var(--surface-bright)",
            "surface-container-lowest": "var(--surface-container-lowest)",
            "surface-container-low": "var(--surface-container-low)",
            "surface-container": "var(--surface-container)",
            "surface-container-subtle": "var(--surface-container-subtle)",
            "surface-container-high": "var(--surface-container-high)",
            "surface-container-highest": "var(--surface-container-highest)",
            "on-surface": "var(--on-surface)",
            "on-surface-variant": "var(--on-surface-variant)",
            "on-surface-muted": "var(--on-surface-muted)",
            "inverse-surface": "var(--inverse-surface)",
            "inverse-on-surface": "var(--inverse-on-surface)",
            "outline": "var(--outline)",
            "outline-variant": "var(--outline-variant)",
            "surface-tint": "var(--surface-tint)",
            "primary": "var(--primary)",
            "on-primary": "var(--on-primary)",
            "primary-container": "var(--primary-container)",
            "on-primary-container": "var(--on-primary-container)",
            "inverse-primary": "var(--inverse-primary)",
            "secondary": "var(--secondary)",
            "on-secondary": "var(--on-secondary)",
            "secondary-container": "var(--secondary-container)",
            "on-secondary-container": "var(--on-secondary-container)",
            "secondary-fixed": "var(--secondary-fixed)",
            "secondary-fixed-dim": "var(--secondary-fixed-dim)",
            "on-secondary-fixed": "var(--on-secondary-fixed)",
            "tertiary": "var(--tertiary)",
            "tertiary-container": "var(--tertiary-container)",
            "tertiary-fixed": "var(--tertiary-fixed)",
            "tertiary-fixed-dim": "var(--tertiary-fixed-dim)",
            "error": "var(--error)",
            "on-error": "var(--on-error)",
            "error-container": "var(--error-container)",
            "on-error-container": "var(--on-error-container)",
            "primary-fixed": "var(--primary-fixed)",
            "primary-fixed-dim": "var(--primary-fixed-dim)",
            "on-primary-fixed": "var(--on-primary-fixed)",
            "surface-variant": "var(--surface-variant)"
          },
          fontFamily: {
            sans: ["Plus Jakarta Sans", "system-ui", "-apple-system", "sans-serif"],
            mono: ["JetBrains Mono", "ui-monospace", "SFMono-Regular", "monospace"]
          }
        }
      }
    };
  </script>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen select-none">

  <!-- ========================================== -->
  <!-- TOP PERSISTENT EDITORIAL SANCTUARY HEADER  -->
  <!-- ========================================== -->
  <header id="app-top-header" class="fixed top-0 left-0 right-0 z-40 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 h-13 px-4 flex items-center justify-between shadow-xs">
    <div class="flex items-center gap-2">
      <span class="material-symbols-outlined text-primary text-[22px] material-symbols-filled" id="header-icon">spa</span>
      <h1 class="text-[17px] font-semibold tracking-tight text-on-surface" id="header-title">Home</h1>
    </div>
    <div class="flex items-center gap-2">
      <button onclick="switchTab('you')" class="w-9 h-9 rounded-full bg-surface-container-low border border-surface-container-high/60 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" title="Profile & Settings">
        <span class="material-symbols-outlined text-primary text-[19px]">person</span>
      </button>
    </div>
  </header>

  <!-- ========================================== -->
  <!-- MAIN SCROLLABLE CONTENT VIEWPORT           -->
  <!-- ========================================== -->
  <main id="app-main" class="flex flex-col relative w-full bg-surface px-4 pt-14 pb-24 min-h-screen">

    <!-- ========================================== -->
    <!-- 1. HOME TAB                                -->
    <!-- ========================================== -->
    <div id="tab-home" class="page-tab active flex-col w-full space-y-4">
      <!-- Snug Warm Greeting (Zero wasted upper space) -->
      <section class="flex flex-col pt-1">
        <div class="flex items-center gap-1.5 mb-0.5">
          <span class="material-symbols-outlined text-primary text-[18px] material-symbols-filled">eco</span>
          <span class="text-xs font-semibold text-primary tracking-wide" id="home-greeting-day">Today's sanctuary</span>
        </div>
        <h2 class="text-2xl font-bold text-on-surface tracking-tight" id="home-greeting-title">Good morning, Sarah</h2>
        <p class="text-sm text-on-surface-variant mt-0.5">A quiet space for today.</p>
      </section>

      <!-- Today's Quiet Snapshot (Directly links to Full Page Tools) -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between pb-2 mb-2 border-b border-surface-container-low">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-primary"></span>
            <span class="text-xs font-medium text-on-surface-variant">The morning flow</span>
          </div>
          <span class="text-[11px] text-outline" id="snapshot-updated-time">Live sync active</span>
        </div>
        <div class="grid grid-cols-3 gap-2 text-left">
          <div class="flex flex-col pr-1 cursor-pointer active:opacity-75" onclick="navigateTo('screentime')">
            <span class="text-lg font-bold text-on-surface" id="stat-screen-time">2h 15m</span>
            <span class="text-[11px] text-on-surface-variant mt-0.5">screen time</span>
          </div>
          <div class="flex flex-col px-1 cursor-pointer active:opacity-75" onclick="navigateTo('focus')">
            <span class="text-lg font-bold text-primary" id="stat-focused-time">25 min</span>
            <span class="text-[11px] text-on-surface-variant mt-0.5">focus sanctuary</span>
          </div>
          <div class="flex flex-col pl-1 cursor-pointer active:opacity-75" onclick="navigateTo('habits')">
            <span class="text-lg font-bold text-secondary" id="stat-habits-met">4 of 5</span>
            <span class="text-[11px] text-on-surface-variant mt-0.5">rituals met</span>
          </div>
        </div>
        <!-- Soft Progress Bar -->
        <div class="w-full bg-surface-container-low h-1.5 rounded-full mt-3 overflow-hidden flex">
          <div class="bg-primary h-full rounded-full transition-all duration-700" id="snapshot-progress-bar" style="width: 75%;"></div>
        </div>
      </section>

      <!-- Focus Sanctuary Card -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col items-center text-center relative overflow-hidden" style="box-shadow: var(--card-shadow);">
        <div class="w-full flex items-center justify-between mb-2 text-left">
          <div>
            <h3 class="text-base font-semibold text-on-surface">Take a little time for yourself</h3>
            <p class="text-xs text-on-surface-variant">Quiet intervals for calm presence</p>
          </div>
          <button onclick="navigateTo('focus')" class="w-9 h-9 rounded-full bg-surface-container flex items-center justify-center shrink-0 text-primary cursor-pointer active:scale-95" title="Open Focus Sanctuary">
            <span class="material-symbols-outlined text-[20px]">hourglass_empty</span>
          </button>
        </div>
        <!-- Timer Visual -->
        <div class="my-2 flex flex-col items-center cursor-pointer" onclick="navigateTo('focus')">
          <div class="font-mono text-4xl font-bold text-on-surface tracking-tight" id="home-timer-display">
            25:00
          </div>
          <p class="text-xs text-on-surface-variant mt-1.5 flex items-center gap-1.5" id="home-timer-subtext">
            <span class="inline-block w-1.5 h-1.5 rounded-full bg-primary"></span>
            Foreground focus service ready
          </p>
        </div>
        <div class="w-full flex items-center justify-center gap-2 pt-1">
          <button class="w-full max-w-[240px] py-2.5 px-5 rounded-xl bg-primary text-on-primary text-sm font-semibold shadow-sm active:scale-[0.98] transition-all flex items-center justify-center gap-2 cursor-pointer hover:opacity-95" id="home-timer-btn" onclick="toggleHomeTimer()">
            <span class="material-symbols-outlined text-[19px]">play_arrow</span>
            <span id="home-timer-btn-text">Start focus</span>
          </button>
        </div>
      </section>

      <!-- Tasks Section: A few things for today -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between mb-2">
          <div class="flex flex-col">
            <h3 class="text-base font-semibold text-on-surface">A few things for today</h3>
            <span class="text-xs text-on-surface-variant">Unrushed steps to take</span>
          </div>
          <span class="text-xs text-primary font-semibold bg-surface-container-low px-2.5 py-0.5 rounded-full" id="tasks-done-badge">1 done</span>
        </div>
        <div class="flex flex-col space-y-2 mt-1" id="home-tasks-list">
          <!-- Dynamically Populated Tasks from SQLite -->
        </div>
        <!-- Soft Action Prompt -->
        <button onclick="promptAddTask()" class="mt-3 w-full py-2.5 px-3 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface-variant text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer active:scale-[0.99]">
          <span class="material-symbols-outlined text-[17px] text-primary">add</span>
          <span>Add a gentle note</span>
        </button>
      </section>

      <!-- Reading Section: Currently Reading -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col cursor-pointer active:scale-[0.99] transition-transform" onclick="navigateTo('reading')" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-secondary text-[18px]">menu_book</span>
            <h3 class="text-base font-semibold text-on-surface">Currently reading</h3>
          </div>
          <span class="text-xs text-secondary font-semibold">Reading Companion</span>
        </div>
        <div class="flex items-center gap-3">
          <div class="w-13 h-18 rounded-lg bg-surface-container overflow-hidden shrink-0 shadow-sm relative">
            <img class="w-full h-full object-cover" id="home-book-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAC8ZEhBwRLi2Zw1vYfkmOLOq8XGczjyeYaKmsgpaG8K8zDX67k3LhXl1KV2qYTw8FokeGfo1CYRF4XW89O0ZpUq0ewq0zAxpKvWtePvSDH53KQeYTn86hGaK5zEW-UDtUgCFuwk3QSN_JoaVRvJe7XG4JCcGQHW6l6JymXW1YkEoXVCHFOKwoLR1kNdxTEyVlkAW8CRPZfP7wi9OP2QFkjBFk6wBr43bZvGFhWpUYSIT0yqRxFtPCZ" alt="Book cover" onerror="this.onerror=null; this.src=FALLBACK_BOOK_SVG;"/>
          </div>
          <div class="flex flex-col flex-1 min-w-0">
            <h4 class="text-sm font-semibold text-on-surface truncate" id="home-book-title">Quiet: The Power of Introverts</h4>
            <p class="text-xs text-on-surface-variant mt-0.5 truncate" id="home-book-author">Susan Cain</p>
            <div class="mt-1 flex items-center justify-between text-xs text-on-surface-variant">
              <span id="home-book-chapter">Chapter 4</span>
              <span class="text-primary font-semibold" id="home-book-percent">42% complete</span>
            </div>
            <!-- Progress Line -->
            <div class="w-full bg-surface-container-high h-1.5 rounded-full mt-1.5 overflow-hidden">
              <div class="bg-primary h-full rounded-full transition-all duration-500" id="home-book-bar" style="width: 42%;"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- Atmospheric Daily Quote / Gita Wisdom Card -->
      <section class="w-full rounded-2xl bg-surface-container-low p-4 flex items-start gap-2.5 border border-surface-container-high/30 cursor-pointer active:scale-[0.99] transition-transform" onclick="navigateTo('gita')">
        <span class="material-symbols-outlined text-primary text-[20px] shrink-0 mt-0.5 material-symbols-filled">format_quote</span>
        <div class="flex flex-col">
          <p class="text-xs italic text-on-surface leading-relaxed" id="daily-quote-text">
            "Almost everything will work again if you unplug it for a few minutes, including you."
          </p>
          <span class="text-[11px] text-outline mt-1 font-medium" id="daily-quote-author">— Anne Lamott · Tap for Gita Wisdom</span>
        </div>
      </section>
    </div>

    <!-- ========================================== -->
    <!-- 2. TOOLS TAB (DEDICATED FULL-PAGE DOORS)   -->
    <!-- ========================================== -->
    <div id="tab-tools" class="page-tab flex-col w-full space-y-4">
      <section class="flex flex-col pt-1">
        <div class="inline-flex items-center gap-1.5 text-primary text-xs font-semibold">
          <span class="material-symbols-outlined text-[17px]">widgets</span>
          <span>Daily companions</span>
        </div>
        <h2 class="text-2xl font-bold text-on-surface tracking-tight">Your tools</h2>
        <p class="text-sm text-on-surface-variant">Dedicated spaces designed for quiet presence.</p>
      </section>

      <!-- 1. Reading Companion Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">menu_book</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Reading</h3>
              <p class="text-xs text-on-surface-variant">Keep track of what you're reading.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-surface-container-low text-primary text-xs font-medium">Active</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-3 flex flex-col gap-1.5">
          <div class="flex items-center justify-between text-xs text-on-surface-variant">
            <span class="truncate max-w-[200px] font-medium text-on-surface">Quiet: The Power of Introverts</span>
            <span class="text-primary font-semibold" id="tools-reading-pct">42%</span>
          </div>
          <div class="w-full bg-surface-variant rounded-full h-1.5 overflow-hidden">
            <div class="bg-primary h-full rounded-full transition-all duration-500" style="width: 42%;"></div>
          </div>
        </div>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-tertiary">bookmark_added</span>
            18 pages left today
          </span>
          <button onclick="navigateTo('reading')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 2. Habits Companion Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">cycle</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Gentle Habits</h3>
              <p class="text-xs text-on-surface-variant">Notice the rituals you keep coming back to.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-surface-container-low text-secondary text-xs font-semibold" id="tools-habits-streak">5 day streak</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-3 flex items-center justify-between">
          <div class="flex flex-col">
            <span class="text-xs font-semibold text-on-surface">Daily cadence</span>
            <span class="text-xs text-on-surface-variant" id="tools-habits-cadence">4 of 5 rituals met today</span>
          </div>
          <div class="flex items-center gap-1.5" id="tools-habits-dots">
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary text-[11px] font-bold">✓</span>
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary text-[11px] font-bold">✓</span>
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary text-[11px] font-bold">✓</span>
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary text-[11px] font-bold">✓</span>
            <span class="w-5 h-5 rounded-full bg-surface-container-high"></span>
          </div>
        </div>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-tertiary">wb_sunny</span>
            Morning rituals in rhythm
          </span>
          <button onclick="navigateTo('habits')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 3. Focus Sanctuary Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">hourglass_top</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Focus Sanctuary</h3>
              <p class="text-xs text-on-surface-variant">Foreground timer with soundscapes.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant text-xs font-medium">Quiet space</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-3 flex items-center justify-between">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-secondary">
              <span class="material-symbols-outlined text-[17px]">nature</span>
            </div>
            <div class="flex flex-col">
              <span class="text-xs font-semibold text-on-surface">Forest soundscape</span>
              <span class="text-[11px] text-on-surface-variant">Default 25 minutes block</span>
            </div>
          </div>
          <div class="font-mono text-base font-bold text-secondary" id="tools-focus-time-preview">25:00</div>
        </div>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-tertiary">check_circle</span>
            Native background service
          </span>
          <button onclick="navigateTo('focus')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 4. Digital Stillness (Screen Time) Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">hourglass_bottom</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Digital Stillness</h3>
              <p class="text-xs text-on-surface-variant">App telemetry and mindful limits.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-surface-container-low text-primary text-xs font-medium">Monitored</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-3 flex items-center justify-between">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-primary">
              <span class="material-symbols-outlined text-[17px]">smartphone</span>
            </div>
            <div class="flex flex-col">
              <span class="text-xs font-semibold text-on-surface" id="tools-screen-time-label">2h 15m screen time</span>
              <span class="text-[11px] text-on-surface-variant">Direct Android telemetry</span>
            </div>
          </div>
          <span class="text-xs text-primary font-semibold">Live data</span>
        </div>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-tertiary">trending_down</span>
            UsageStatsManager
          </span>
          <button onclick="navigateTo('screentime')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 5. Mood & Energy Check-in Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-secondary-fixed-dim text-on-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">favorite</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Mood & Energy</h3>
              <p class="text-xs text-on-surface-variant">Self check-in & panic grounding vault.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-secondary-fixed text-on-secondary-fixed text-xs font-medium">Equilibrium</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-3 flex items-center justify-between">
          <div class="flex items-center gap-2.5">
            <span class="text-xl">🙂</span>
            <div class="flex flex-col">
              <span class="text-xs font-semibold text-on-surface" id="tools-mood-preview">Calm & Centered</span>
              <span class="text-[11px] text-on-surface-variant" id="tools-energy-preview">Energy level: 7/10</span>
            </div>
          </div>
          <span class="material-symbols-outlined text-secondary text-[19px]">spa</span>
        </div>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-tertiary">psychology</span>
            Box breathing ready
          </span>
          <button onclick="navigateTo('mood')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 6. Gita Wisdom Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">bookmark_heart</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Gita Wisdom</h3>
              <p class="text-xs text-on-surface-variant">Daily contemplative verse & Sanskrit text.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-secondary-fixed text-on-secondary-fixed text-xs font-medium">Chapter 2</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-3 flex flex-col gap-1">
          <span class="text-xs font-semibold text-secondary">Verse 2.47 • Nishkama Karma</span>
          <p class="text-xs text-on-surface-variant line-clamp-2 italic">
            "You have a right to your actions, but never to your action's fruits..."
          </p>
        </div>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-tertiary">edit_note</span>
            Contemplative prompts
          </span>
          <button onclick="navigateTo('gita')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>
    </div>

    <!-- ========================================== -->
    <!-- 3. JOURNAL TAB                             -->
    <!-- ========================================== -->
    <div id="tab-journal" class="page-tab flex-col w-full max-w-[680px] mx-auto space-y-4">
      <!-- Sanctuary Header -->
      <div class="flex items-end justify-between pt-1">
        <div class="flex flex-col gap-0.5">
          <h2 class="text-2xl font-bold text-on-surface tracking-tight">Journal</h2>
          <p class="text-sm text-on-surface-variant">Somewhere to put your thoughts.</p>
        </div>
        <button onclick="navigateTo('write')" class="flex items-center gap-1.5 px-4 py-2 bg-primary text-on-primary rounded-full shadow-sm active:scale-[0.98] transition-transform cursor-pointer hover:opacity-95" id="write-button">
          <span class="material-symbols-outlined text-[18px]">stylus</span>
          <span class="text-xs font-semibold">Write</span>
        </button>
      </div>

      <!-- Tactile Search & Filters -->
      <section class="flex flex-col gap-2">
        <div class="relative w-full flex items-center">
          <span class="material-symbols-outlined absolute left-3.5 text-outline text-[19px] pointer-events-none select-none">search</span>
          <input class="w-full bg-surface-container-lowest text-on-surface placeholder:text-outline pl-10 pr-4 py-2.5 rounded-xl shadow-sm border border-surface-container-high/40 outline-none focus:bg-surface-container-low transition-colors text-sm" id="journal-search" placeholder="Search your reflections..." type="text" oninput="applyJournalFilters()"/>
        </div>
        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
          <button class="journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-primary text-on-primary shrink-0 transition-colors shadow-sm cursor-pointer" data-tag="all" onclick="selectJournalTag('all', this)">
            All
          </button>
          <button class="journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="gratitude" onclick="selectJournalTag('gratitude', this)">
            #gratitude
          </button>
          <button class="journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="focus" onclick="selectJournalTag('focus', this)">
            #focus
          </button>
          <button class="journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="calm" onclick="selectJournalTag('calm', this)">
            #calm
          </button>
          <button class="journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="morning" onclick="selectJournalTag('morning', this)">
            #morning
          </button>
        </div>
      </section>

      <!-- Entries Stream -->
      <div class="flex flex-col space-y-3" id="journal-entries-stream">
        <!-- Dynamically Populated from SQLite via PlatformJsBridge -->
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 4. DAY TAB                                 -->
    <!-- ========================================== -->
    <div id="tab-day" class="page-tab flex-col w-full space-y-4">
      <div class="pt-1">
        <h2 class="text-2xl font-bold text-on-surface tracking-tight">Your day</h2>
        <p class="text-sm text-on-surface-variant">A quiet look at how your time unfolded.</p>
      </div>

      <!-- Today Summary Card -->
      <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between">
          <span class="text-sm font-semibold text-primary" id="day-card-label">Today</span>
          <span class="text-xs text-on-surface-variant bg-surface-container-low px-2.5 py-0.5 rounded-full font-medium">Gentle rhythm</span>
        </div>
        <div class="grid grid-cols-3 gap-2 text-center py-1">
          <div class="flex flex-col items-center justify-center cursor-pointer active:opacity-75" onclick="navigateTo('screentime')">
            <span class="text-lg font-bold text-on-surface" id="day-screen-time">2h 15m</span>
            <span class="text-xs text-on-surface-variant mt-0.5">screen time</span>
          </div>
          <div class="flex flex-col items-center justify-center bg-surface-container-low/60 rounded-xl py-1 px-1 cursor-pointer active:opacity-75" onclick="navigateTo('focus')">
            <span class="text-lg font-bold text-primary" id="day-focused-time">25 min</span>
            <span class="text-xs text-on-surface-variant mt-0.5">focused</span>
          </div>
          <div class="flex flex-col items-center justify-center cursor-pointer active:opacity-75" onclick="navigateTo('habits')">
            <span class="text-lg font-bold text-secondary" id="day-habits-count">4</span>
            <span class="text-xs text-on-surface-variant mt-0.5">habits met</span>
          </div>
        </div>
      </div>

      <!-- Timeline Events -->
      <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-3" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between pb-2 border-b border-surface-container-low">
          <h3 class="text-sm font-semibold text-on-surface">Earlier today</h3>
          <span class="text-xs text-primary font-medium">Chronological</span>
        </div>
        <div class="relative pl-5 space-y-4 pt-1" id="day-timeline-events">
          <div class="absolute left-2 top-3 bottom-2 w-[1.5px] bg-surface-container-high"></div>
          <div class="relative flex items-start gap-3">
            <div class="absolute -left-5 top-1 w-4 h-4 rounded-full bg-primary ring-4 ring-surface-container-lowest"></div>
            <div>
              <span class="text-xs font-semibold text-on-surface">Focus sanctuary session</span>
              <p class="text-xs text-on-surface-variant">25 minutes of quiet presence</p>
            </div>
          </div>
          <div class="relative flex items-start gap-3">
            <div class="absolute -left-5 top-1 w-4 h-4 rounded-full bg-secondary ring-4 ring-surface-container-lowest"></div>
            <div>
              <span class="text-xs font-semibold text-on-surface">Reading session</span>
              <p class="text-xs text-on-surface-variant">Quiet: The Power of Introverts</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 5. YOU TAB (SETTINGS & THEME)             -->
    <!-- ========================================== -->
    <div id="tab-you" class="page-tab flex-col w-full space-y-4 pb-10">
      <div class="pt-1">
        <h2 class="text-2xl font-bold text-on-surface tracking-tight">Your space</h2>
        <p class="text-sm text-on-surface-variant">Theme, profile & local vault.</p>
      </div>

      <!-- Theme Switcher -->
      <section class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-3" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-semibold text-on-surface">Appearance & Theme</h3>
          <span class="text-xs text-tertiary">Editorial Palette</span>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div class="p-3 rounded-xl border border-surface-container-high flex flex-col items-center justify-center gap-1.5 cursor-pointer active:scale-95 transition-all" id="theme-card-light" onclick="setAppTheme('light')">
            <span class="material-symbols-outlined text-primary text-[24px]">wb_sunny</span>
            <span class="text-xs font-semibold text-on-surface">Warm Paper</span>
            <span class="text-[10px] text-on-surface-variant">Natural daylight</span>
          </div>
          <div class="p-3 rounded-xl border border-surface-container-high flex flex-col items-center justify-center gap-1.5 cursor-pointer active:scale-95 transition-all" id="theme-card-dark" onclick="setAppTheme('dark')">
            <span class="material-symbols-outlined text-primary text-[24px]">dark_mode</span>
            <span class="text-xs font-semibold text-on-surface">Night Ink</span>
            <span class="text-[10px] text-on-surface-variant">Deep charcoal</span>
          </div>
        </div>
      </section>

      <!-- Profile Card -->
      <section class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex items-center gap-3" style="box-shadow: var(--card-shadow);">
        <div class="w-12 h-12 rounded-full bg-surface-container-low text-primary flex items-center justify-center font-bold text-lg">
          S
        </div>
        <div class="flex flex-col flex-1 min-w-0">
          <span class="text-sm font-semibold text-on-surface truncate" id="profile-name">Sarah Jenkins</span>
          <span class="text-xs text-on-surface-variant truncate" id="profile-email">sarah@sanctuary.internal</span>
          <span class="text-[11px] text-primary mt-0.5">SQLite Sovereignty · 100% On-device</span>
        </div>
      </section>
    </div>
  </main>

  <!-- ========================================== -->
  <!-- FLOATING 5-NODE BOTTOM NAVIGATION DOCK     -->
  <!-- ========================================== -->
  <nav id="app-bottom-nav" class="fixed bottom-0 left-0 right-0 z-40 pointer-events-none flex justify-center px-4 pb-3">
    <div class="pointer-events-auto w-full max-w-[400px] h-14 rounded-full bg-surface-container-lowest shadow-[var(--nav-shadow)] px-2 flex items-center justify-between border border-surface-container-high/40" id="bottomNav">
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-11 rounded-full transition-colors cursor-pointer nav-active" data-tab="home" href="javascript:void(0)" onclick="switchTab('home')">
        <span class="material-symbols-outlined text-[20px]">spa</span>
        <span class="text-[10px] font-medium mt-0.5">Home</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-11 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="tools" href="javascript:void(0)" onclick="switchTab('tools')">
        <span class="material-symbols-outlined text-[20px]">grid_view</span>
        <span class="text-[10px] font-medium mt-0.5">Tools</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-11 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="journal" href="javascript:void(0)" onclick="switchTab('journal')">
        <span class="material-symbols-outlined text-[20px]">edit_note</span>
        <span class="text-[10px] font-medium mt-0.5">Journal</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-11 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="day" href="javascript:void(0)" onclick="switchTab('day')">
        <span class="material-symbols-outlined text-[20px]">calendar_today</span>
        <span class="text-[10px] font-medium mt-0.5">Day</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-11 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="you" href="javascript:void(0)" onclick="switchTab('you')">
        <span class="material-symbols-outlined text-[20px]">person_outline</span>
        <span class="text-[10px] font-medium mt-0.5">You</span>
      </a>
    </div>
  </nav>

  <!-- ========================================== -->
  <!-- DEDICATED FULL-PAGE COMPANION VIEWS        -->
  <!-- (Zero popups; Full bleed with back header) -->
  <!-- ========================================== -->
  <div id="tool-views-container">

    <!-- 1. FOCUS SANCTUARY FULL PAGE -->
    <div id="view-focus" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">Focus Sanctuary</h2>
            <p class="text-[11px] text-on-surface-variant">Quiet intervals for deep presence</p>
          </div>
        </div>
        <button onclick="resetFocusTimer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Reset">
          <span class="material-symbols-outlined text-[19px]">restart_alt</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Mode Switcher -->
        <div class="w-full p-1 bg-surface-container-low rounded-xl flex items-center justify-between border border-surface-container-high/40">
          <button class="flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer" id="focus-mode-pomodoro" onclick="setFocusMode('pomodoro')">
            <span class="material-symbols-outlined text-[15px]">timelapse</span>
            <span>Pomodoro (25m)</span>
          </button>
          <button class="flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer" id="focus-mode-timer" onclick="setFocusMode('timer')">
            <span class="material-symbols-outlined text-[15px]">hourglass_empty</span>
            <span>Open Timer</span>
          </button>
        </div>

        <!-- Chronometer Card -->
        <div class="w-full bg-surface-container-lowest rounded-2xl p-6 border border-surface-container-high/40 flex flex-col items-center relative overflow-hidden" style="box-shadow: var(--card-shadow);">
          <div class="w-full flex items-center justify-between text-xs mb-2">
            <div class="flex items-center gap-1.5 px-3 py-1 rounded-full bg-surface-container-low text-primary font-semibold">
              <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
              <span id="focus-cycle-badge">Focus • Cycle 1/4</span>
            </div>
            <span class="text-on-surface-variant font-medium flex items-center gap-1">
              <span class="material-symbols-outlined text-[15px] text-secondary">flag</span>
              <span id="focus-target-duration">25m Block</span>
            </span>
          </div>

          <!-- Circular Dial Ring -->
          <div class="relative w-52 h-52 flex items-center justify-center my-2">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 240 240">
              <circle cx="120" cy="120" r="100" fill="transparent" stroke="var(--surface-container-high)" stroke-width="8"></circle>
              <circle id="focus-progress-ring" cx="120" cy="120" r="100" fill="transparent" stroke="var(--primary)" stroke-width="8" stroke-dasharray="628.31" stroke-dashoffset="0" stroke-linecap="round" class="transition-all duration-1000 ease-linear"></circle>
            </svg>
            <div class="absolute flex flex-col items-center justify-center text-center select-none">
              <span class="font-mono text-4xl font-bold text-on-surface tracking-tight" id="focus-modal-time">25:00</span>
              <span class="text-xs font-semibold text-primary flex items-center gap-1 mt-1 uppercase" id="focus-modal-phase">
                <span class="material-symbols-outlined text-[14px]">lock_clock</span>
                <span>Ready</span>
              </span>
            </div>
          </div>

          <!-- Interval Adjustment -->
          <div class="flex items-center gap-3 mt-1">
            <button onclick="adjustFocusMinutes(-5)" class="h-8 px-3 rounded-lg bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant text-xs font-mono font-medium transition-all flex items-center gap-1 cursor-pointer">
              <span class="material-symbols-outlined text-[14px]">remove</span> 5m
            </button>
            <button onclick="adjustFocusMinutes(5)" class="h-8 px-3 rounded-lg bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant text-xs font-mono font-medium transition-all flex items-center gap-1 cursor-pointer">
              <span class="material-symbols-outlined text-[14px]">add</span> 5m
            </button>
          </div>

          <!-- Action Buttons -->
          <div class="flex items-center gap-3 mt-4">
            <button onclick="resetFocusTimer()" class="h-11 px-4 rounded-xl bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant flex items-center gap-1.5 font-semibold text-xs border border-surface-container-high/40 cursor-pointer">
              <span class="material-symbols-outlined text-[17px]">restart_alt</span>
              <span>Reset</span>
            </button>
            <button onclick="toggleFocusTimer()" id="focus-modal-toggle-btn" class="h-11 px-7 rounded-xl bg-primary hover:opacity-95 text-on-primary active:scale-95 transition-all flex items-center gap-2 font-bold text-sm shadow-md cursor-pointer">
              <span class="material-symbols-outlined text-[19px]" id="focus-modal-play-icon">play_arrow</span>
              <span id="focus-modal-play-text">Start focus</span>
            </button>
            <button onclick="skipFocusCycle()" class="h-11 w-11 rounded-xl bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant flex items-center justify-center border border-surface-container-high/40 cursor-pointer" title="Skip Cycle">
              <span class="material-symbols-outlined text-[17px]">skip_next</span>
            </button>
          </div>
        </div>

        <!-- Presets -->
        <div class="w-full flex items-center justify-between text-xs text-on-surface-variant px-1">
          <span class="font-medium">Preset intervals:</span>
          <div class="flex items-center gap-1.5">
            <button onclick="setFocusPreset(15)" class="px-2.5 py-1 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface font-mono font-medium cursor-pointer">15m</button>
            <button onclick="setFocusPreset(25)" class="px-2.5 py-1 rounded-lg bg-primary text-on-primary font-mono font-medium cursor-pointer">25m</button>
            <button onclick="setFocusPreset(45)" class="px-2.5 py-1 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface font-mono font-medium cursor-pointer">45m</button>
            <button onclick="setFocusPreset(60)" class="px-2.5 py-1 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface font-mono font-medium cursor-pointer">60m</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. READING COMPANION FULL PAGE -->
    <div id="view-reading" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">Reading Companion</h2>
            <p class="text-[11px] text-on-surface-variant">Your library & bookmarks</p>
          </div>
        </div>
        <button onclick="promptUpdatePage()" class="px-3 py-1 bg-surface-container-low hover:bg-surface-container text-primary rounded-full text-xs font-semibold flex items-center gap-1 cursor-pointer">
          <span class="material-symbols-outlined text-[15px]">edit</span>
          <span>Update page</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Book Progress Card -->
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex gap-3.5 items-start">
            <div class="w-16 h-24 rounded-lg overflow-hidden shrink-0 shadow-sm bg-surface-container">
              <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAC8ZEhBwRLi2Zw1vYfkmOLOq8XGczjyeYaKmsgpaG8K8zDX67k3LhXl1KV2qYTw8FokeGfo1CYRF4XW89O0ZpUq0ewq0zAxpKvWtePvSDH53KQeYTn86hGaK5zEW-UDtUgCFuwk3QSN_JoaVRvJe7XG4JCcGQHW6l6JymXW1YkEoXVCHFOKwoLR1kNdxTEyVlkAW8CRPZfP7wi9OP2QFkjBFk6wBr43bZvGFhWpUYSIT0yqRxFtPCZ" alt="Book cover" onerror="this.onerror=null; this.src=FALLBACK_BOOK_SVG;"/>
            </div>
            <div class="flex flex-col flex-1 min-w-0">
              <div class="flex items-start justify-between gap-1">
                <h3 class="text-base font-semibold text-on-surface truncate">Quiet: The Power of Introverts</h3>
                <span class="text-xs font-semibold text-primary shrink-0" id="reading-percent-label">42% complete</span>
              </div>
              <p class="text-xs text-on-surface-variant mt-0.5 truncate">Susan Cain</p>
              <p class="text-[11px] text-tertiary mt-1" id="reading-page-info">Page 142 of 338</p>
              <div class="w-full h-1.5 bg-surface-container rounded-full overflow-hidden mt-2">
                <div class="h-full bg-primary rounded-full transition-all duration-300" id="reading-modal-bar" style="width: 42%;"></div>
              </div>
              <div class="flex items-center gap-2 mt-3">
                <button onclick="adjustBookPages(-5)" class="px-3 py-1.5 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg text-xs font-semibold active:scale-95 cursor-pointer">
                  -5 pages
                </button>
                <button onclick="adjustBookPages(10)" class="px-3 py-1.5 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg text-xs font-semibold active:scale-95 cursor-pointer">
                  +10 pages
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Shelf -->
        <div class="space-y-2">
          <h4 class="text-xs font-semibold text-on-surface uppercase tracking-wider">Your Shelf</h4>
          <div class="flex flex-col bg-surface-container-lowest rounded-xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden">
            <div class="p-3 flex items-center gap-3">
              <div class="w-9 h-13 rounded overflow-hidden shrink-0 bg-surface-container shadow-xs">
                <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCjgMVcp30Hjld1NvpUVWqCnq-nNf2PO3yMf0bejUgHXyOap3xKCRN8CUE56l0NgH_GeWH7LqOGZ5YIbPXHhbODPm6vlEDfh2uWG0WMm54z3Mt9EV_TRcup4rWKnFP99F0AKpVpCDTE8gW9D3oRzVSeyOZSowp6hqn_9piDrnmpdRRg5HI3GOmaoVmnDlIspa4a_4zft7R52t7nXbar9amI7IyJN-TKhqRko3BCqqN7HKq-Ya9pQCN_" alt="The Summer Book" onerror="this.onerror=null; this.src=FALLBACK_BOOK_SVG;"/>
              </div>
              <div class="flex flex-col flex-1 min-w-0">
                <h5 class="text-sm font-semibold text-on-surface truncate">The Summer Book</h5>
                <p class="text-xs text-on-surface-variant truncate">Tove Jansson</p>
                <span class="text-[11px] text-primary font-medium mt-0.5">68% · In progress</span>
              </div>
            </div>
            <div class="p-3 flex items-center gap-3">
              <div class="w-9 h-13 rounded overflow-hidden shrink-0 bg-surface-container shadow-xs flex items-center justify-center text-tertiary">
                <span class="material-symbols-outlined text-[18px]">book</span>
              </div>
              <div class="flex flex-col flex-1 min-w-0">
                <h5 class="text-sm font-semibold text-on-surface truncate">Atomic Habits</h5>
                <p class="text-xs text-on-surface-variant truncate">James Clear</p>
                <span class="text-[11px] text-secondary font-medium mt-0.5">Completed</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. GENTLE HABITS FULL PAGE -->
    <div id="view-habits" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">Gentle Habits</h2>
            <p class="text-[11px] text-on-surface-variant">Daily cadence and rituals</p>
          </div>
        </div>
        <button onclick="promptAddHabit()" class="w-8 h-8 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Add Habit">
          <span class="material-symbols-outlined text-[20px]">add</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <div class="flex items-center justify-between">
          <span class="text-sm font-semibold text-on-surface">Today's rituals</span>
          <span class="text-xs text-primary font-semibold bg-surface-container-low px-2.5 py-0.5 rounded-full" id="habits-ratio-badge">4 of 5 done</span>
        </div>

        <div class="space-y-2.5" id="habits-modal-list">
          <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs cursor-pointer active:scale-[0.99]" onclick="toggleHabitItem(this)">
            <div class="flex items-center gap-3 flex-1">
              <div class="habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[16px]">check</span>
              </div>
              <div class="flex flex-col">
                <span class="habit-title text-sm text-on-surface line-through opacity-70 font-medium">Morning stillness</span>
                <span class="text-[11px] text-secondary font-medium">🔥 14 days streak</span>
              </div>
            </div>
          </div>
          <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs cursor-pointer active:scale-[0.99]" onclick="toggleHabitItem(this)">
            <div class="flex items-center gap-3 flex-1">
              <div class="habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[16px]">check</span>
              </div>
              <div class="flex flex-col">
                <span class="habit-title text-sm text-on-surface line-through opacity-70 font-medium">Hydrate with herbal infusion</span>
                <span class="text-[11px] text-secondary font-medium">🔥 21 days streak</span>
              </div>
            </div>
          </div>
          <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs cursor-pointer active:scale-[0.99]" onclick="toggleHabitItem(this)">
            <div class="flex items-center gap-3 flex-1">
              <div class="habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[16px]">check</span>
              </div>
              <div class="flex flex-col">
                <span class="habit-title text-sm text-on-surface line-through opacity-70 font-medium">Read 20 pages</span>
                <span class="text-[11px] text-secondary font-medium">🔥 6 days streak</span>
              </div>
            </div>
          </div>
          <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs cursor-pointer active:scale-[0.99]" onclick="toggleHabitItem(this)">
            <div class="flex items-center gap-3 flex-1">
              <div class="habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
              </div>
              <div class="flex flex-col">
                <span class="habit-title text-sm text-on-surface font-medium">Evening reflection</span>
                <span class="text-[11px] text-on-surface-variant">Scheduled at 9:15 PM</span>
              </div>
            </div>
          </div>
        </div>

        <button onclick="promptAddHabit()" class="w-full py-3 rounded-xl border border-dashed border-outline-variant hover:border-primary text-primary text-xs font-semibold flex items-center justify-center gap-2 cursor-pointer active:scale-95 transition-colors">
          <span class="material-symbols-outlined text-[17px]">add</span>
          <span>Add ritual habit</span>
        </button>

        <!-- Heatmap Grid -->
        <div class="p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 space-y-2">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-on-surface">Consistency Grid</span>
            <span class="text-xs text-primary font-medium">30 days rhythm</span>
          </div>
          <div class="grid grid-cols-7 gap-1 justify-items-center" id="heatmap-grid"></div>
        </div>
      </div>
    </div>

    <!-- 4. DIGITAL STILLNESS (SCREEN TIME) FULL PAGE -->
    <div id="view-screentime" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">Digital Stillness</h2>
            <p class="text-[11px] text-on-surface-variant">Telemetry & usage boundaries</p>
          </div>
        </div>
        <button onclick="refreshScreenTime()" class="w-8 h-8 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Refresh">
          <span class="material-symbols-outlined text-[19px]">sync</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Permission Banner -->
        <div id="screentime-perm-banner" class="hidden p-4 rounded-2xl bg-secondary-fixed text-on-secondary-fixed space-y-2 border border-secondary-fixed-dim">
          <div class="flex items-center gap-2 font-semibold text-sm">
            <span class="material-symbols-outlined text-[20px]">security</span>
            <span>Usage Access Permission Required</span>
          </div>
          <p class="text-xs leading-relaxed opacity-90">
            To view detailed app breakdown and daily presence metrics on your device, please grant Android usage stats access.
          </p>
          <button onclick="requestUsagePermission()" class="mt-1 px-4 py-2 bg-on-secondary-fixed text-secondary-fixed rounded-xl text-xs font-bold active:scale-95 transition-all cursor-pointer">
            Grant Permission in Settings
          </button>
        </div>

        <!-- Total Screen Time Card -->
        <div class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 flex items-center justify-between shadow-xs">
          <div>
            <span class="text-xs text-on-surface-variant font-medium">Total screen time today</span>
            <div class="text-3xl font-bold text-on-surface mt-0.5" id="screentime-modal-total">2h 15m</div>
          </div>
          <div class="text-right">
            <span class="text-xs text-primary font-semibold bg-surface-container-low px-2.5 py-1 rounded-full">Quiet day</span>
          </div>
        </div>

        <!-- Top Apps List -->
        <div class="space-y-2">
          <span class="text-xs font-semibold text-on-surface uppercase tracking-wider">Top Application Activity</span>
          <div class="flex flex-col bg-surface-container-lowest rounded-2xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden shadow-xs" id="screentime-apps-list">
            <!-- Populated dynamically from ScreenTimeTracker -->
          </div>
        </div>
      </div>
    </div>

    <!-- 5. MOOD & GROUNDING FULL PAGE -->
    <div id="view-mood" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">Mood & Grounding</h2>
            <p class="text-[11px] text-on-surface-variant">Self check-in & panic vault</p>
          </div>
        </div>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Subtab Switcher -->
        <div class="w-full p-1 bg-surface-container-low rounded-xl flex items-center justify-between border border-surface-container-high/40">
          <button class="flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer" id="tab-btn-mood-checkin" onclick="switchMoodSubtab('checkin')">
            <span class="material-symbols-outlined text-[15px]">spa</span>
            <span>Check-in</span>
          </button>
          <button class="flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer" id="tab-btn-panic-vault" onclick="switchMoodSubtab('panic')">
            <span class="material-symbols-outlined text-[15px] text-secondary">lock</span>
            <span>Panic Vault</span>
          </button>
        </div>

        <!-- Check-in Form View -->
        <div id="mood-view-checkin" class="space-y-4">
          <div class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 space-y-4 shadow-xs">
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-semibold text-on-surface">How are you feeling?</span>
                <span class="text-xs text-primary font-medium bg-surface-container-low px-2 py-0.5 rounded-full" id="mood-selected-label">Calm & Centered</span>
              </div>
              <div class="grid grid-cols-5 gap-1.5" id="mood-pill-grid">
                <button onclick="selectMood('heavy', 'Heavy', '😞')" class="mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface transition-all cursor-pointer">
                  <span class="text-2xl">😞</span>
                  <span class="text-[11px] text-on-surface-variant mt-1">Heavy</span>
                </button>
                <button onclick="selectMood('uneasy', 'Uneasy', '😕')" class="mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface transition-all cursor-pointer">
                  <span class="text-2xl">😕</span>
                  <span class="text-[11px] text-on-surface-variant mt-1">Uneasy</span>
                </button>
                <button onclick="selectMood('steady', 'Steady', '😐')" class="mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface transition-all cursor-pointer">
                  <span class="text-2xl">😐</span>
                  <span class="text-[11px] text-on-surface-variant mt-1">Steady</span>
                </button>
                <button onclick="selectMood('calm', 'Calm & Centered', '🙂')" class="mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-primary text-on-primary shadow-xs transition-all cursor-pointer">
                  <span class="text-2xl">🙂</span>
                  <span class="text-[11px] font-semibold mt-1">Calm</span>
                </button>
                <button onclick="selectMood('vibrant', 'Vibrant', '😄')" class="mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface transition-all cursor-pointer">
                  <span class="text-2xl">😄</span>
                  <span class="text-[11px] text-on-surface-variant mt-1">Vibrant</span>
                </button>
              </div>
            </div>

            <div class="pt-2 border-t border-surface-container-high/40">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-semibold text-on-surface flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-[17px] text-primary">bolt</span>
                  <span>Energy level</span>
                </span>
                <span class="text-sm text-primary font-bold" id="mood-energy-display">7 / 10</span>
              </div>
              <input type="range" min="1" max="10" value="7" class="w-full accent-primary cursor-pointer" id="mood-energy-slider" oninput="updateEnergyDisplay(this.value)"/>
            </div>

            <div class="pt-2 border-t border-surface-container-high/40">
              <label class="block text-xs font-semibold text-on-surface mb-1">Quiet note</label>
              <textarea id="mood-reflection-input" class="w-full rounded-xl bg-surface-container-low text-on-surface placeholder:text-outline p-3 text-xs leading-relaxed resize-none outline-none focus:bg-surface-container transition-colors border border-surface-container-high/30" placeholder="A line of gratitude, the sound of the wind, or thoughts resting lightly..." rows="3"></textarea>
            </div>

            <button onclick="saveMoodCheckin()" class="w-full py-2.5 bg-primary hover:opacity-95 text-on-primary rounded-xl text-xs font-semibold transition-all shadow-sm flex items-center justify-center gap-2 cursor-pointer active:scale-98">
              <span class="material-symbols-outlined text-[17px]">check</span>
              <span>Log check-in</span>
            </button>
          </div>
        </div>

        <!-- Panic Grounding View -->
        <div id="mood-view-panic" class="space-y-4 hidden">
          <div class="bg-surface-container-lowest rounded-2xl p-5 border border-surface-container-high/40 text-center space-y-4 shadow-xs">
            <div class="w-12 h-12 rounded-full bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center mx-auto">
              <span class="material-symbols-outlined text-[24px]">air</span>
            </div>
            <div>
              <h3 class="text-base font-semibold text-on-surface">Box Breathing (4-4-4-4)</h3>
              <p class="text-xs text-on-surface-variant mt-1">Inhale for 4s, hold for 4s, exhale for 4s, hold for 4s.</p>
            </div>
            <!-- Breathing Pulse Circle -->
            <div class="py-4 flex flex-col items-center justify-center">
              <div class="w-28 h-28 rounded-full bg-primary/20 border-2 border-primary flex items-center justify-center animate-breathe" id="breathing-circle">
                <span class="text-xs text-primary font-bold" id="breathing-label">Breathe In</span>
              </div>
            </div>
            <div class="text-xs text-on-surface-variant italic">
              "You are right here, safe and anchored in this breath."
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 6. BHAGAVAD GITA WISDOM FULL PAGE -->
    <div id="view-gita" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">Bhagavad Gita</h2>
            <p class="text-[11px] text-on-surface-variant">Daily contemplative verse</p>
          </div>
        </div>
        <button onclick="nextGitaVerse()" class="w-8 h-8 rounded-full flex items-center justify-center text-secondary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Next Verse">
          <span class="material-symbols-outlined text-[19px]">shuffle</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <div class="flex items-center justify-between text-secondary text-xs font-semibold">
          <span id="gita-chapter-verse">Chapter 2 • Verse 47</span>
          <span class="px-2.5 py-0.5 bg-secondary-fixed text-on-secondary-fixed rounded-full text-[10px]">Nishkama Karma</span>
        </div>
        <!-- Sanskrit Card -->
        <div class="p-5 rounded-2xl bg-surface-container-low border border-surface-container-high/30 text-center shadow-xs">
          <p class="text-base text-on-surface font-semibold leading-relaxed" id="gita-sanskrit">
            कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br/>
            मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥
          </p>
        </div>
        <!-- Translation -->
        <div class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 space-y-2 shadow-xs">
          <span class="text-xs font-semibold text-on-surface-variant">Translation</span>
          <p class="text-sm text-on-surface leading-relaxed" id="gita-translation">
            "You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty."
          </p>
        </div>
        <!-- Sanctuary Prompt -->
        <div class="p-4 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 space-y-2 shadow-xs">
          <span class="text-xs text-primary font-semibold flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px]">psychology</span>
            Sanctuary Prompt
          </span>
          <p class="text-xs text-on-surface-variant leading-relaxed">
            Where in your day can you release attachment to outcomes and simply give complete, quiet attention to the act itself?
          </p>
          <button onclick="saveGitaReflection()" class="mt-2 w-full py-2 bg-surface-container-low hover:bg-surface-container text-primary rounded-xl text-xs font-semibold active:scale-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer">
            <span class="material-symbols-outlined text-[16px]">bookmark_add</span>
            <span>Save verse to Journal</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 7. JOURNAL REFLECTION WRITER FULL PAGE -->
    <div id="view-write" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-13 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2">
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[22px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-semibold text-on-surface">New Reflection</h2>
            <p class="text-[11px] text-on-surface-variant">Preserve your thoughts</p>
          </div>
        </div>
        <button onclick="handleSaveThought(event)" class="px-4 py-1.5 rounded-full text-xs font-semibold bg-primary text-on-primary shadow-sm hover:opacity-95 transition-opacity cursor-pointer">
          Preserve
        </button>
      </header>

      <div class="flex-1 p-4 pb-12 max-w-[500px] mx-auto w-full space-y-4">
        <div class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 space-y-3 shadow-xs">
          <textarea id="thought-input" class="w-full bg-surface-container-low p-3.5 rounded-xl text-on-surface placeholder:text-outline text-sm leading-relaxed resize-none outline-none focus:bg-surface-container transition-colors border border-surface-container-high/30" placeholder="What brought you ease or insight today?" rows="6" required></textarea>
          <div class="flex items-center justify-between pt-1">
            <div class="flex items-center gap-1.5 text-outline bg-surface-container-low px-2.5 py-1 rounded-lg border border-surface-container-high/20">
              <span class="material-symbols-outlined text-[15px]">label</span>
              <input id="thought-tag-input" class="w-32 bg-transparent text-xs text-on-surface placeholder:text-outline outline-none" placeholder="#calm #morning" type="text"/>
            </div>
            <button class="px-3.5 py-1.5 rounded-full text-xs text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" onclick="navigateBack()" type="button">
              Discard
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>

  <!-- ========================================== -->
  <!-- JAVASCRIPT: NATIVE BRIDGE & FULL PAGE NAV  -->
  <!-- ========================================== -->
  <script>
    // SVG Fallbacks
    const FALLBACK_BOOK_SVG = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 140" fill="%23202522"><rect width="100" height="140" rx="6" stroke="%23343C37"/><text x="50" y="70" fill="%2375B7A1" font-size="10" font-family="sans-serif" text-anchor="middle">BOOK SHELF</text></svg>';

    // Text Sanitizer
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

    // ==========================================
    // NATIVE BRIDGE RPC HELPER
    // ==========================================
    function nativeInvoke(method, params) {
      try {
        if (window.V3NativeBridge && typeof window.V3NativeBridge.invoke === 'function') {
          var payload = params ? JSON.stringify(params) : '{}';
          var resStr = window.V3NativeBridge.invoke('com.v07.core', method, payload);
          var res = JSON.parse(resStr);
          if (res && (res.ok === true || res.status === 'success')) {
            return res.result || res.data || {};
          } else {
            console.warn('[V3Bridge error]', method, res ? (res.error || res.message) : 'Unknown');
            return null;
          }
        }
      } catch (e) {
        console.error('[V3Bridge exception]', method, e);
      }
      return null;
    }

    // Navigation Stack
    let navHistory = ['home'];
    let currentTab = 'home';

    function switchTab(tab) {
      currentTab = tab;
      const pages = {
        home: document.getElementById('tab-home'),
        tools: document.getElementById('tab-tools'),
        journal: document.getElementById('tab-journal'),
        day: document.getElementById('tab-day'),
        you: document.getElementById('tab-you')
      };
      const titles = { home: 'Home', tools: 'Tools', journal: 'Journal', day: 'Day', you: 'You' };
      const icons = { home: 'spa', tools: 'grid_view', journal: 'edit_note', day: 'calendar_today', you: 'person_outline' };

      Object.keys(pages).forEach(k => {
        if (pages[k]) {
          if (k === tab) pages[k].classList.add('active');
          else pages[k].classList.remove('active');
        }
      });

      document.getElementById('header-title').textContent = titles[tab] || 'Home';
      document.getElementById('header-icon').textContent = icons[tab] || 'spa';

      const navItems = document.querySelectorAll('.nav-item');
      navItems.forEach(item => {
        if (item.getAttribute('data-tab') === tab) {
          item.classList.add('nav-active');
          item.classList.remove('text-on-surface-variant');
        } else {
          item.classList.remove('nav-active');
          item.classList.add('text-on-surface-variant');
        }
      });
      window.scrollTo({ top: 0, behavior: 'instant' });
    }

    // ==========================================
    // FULL-PAGE VIEW TRANSITIONS (ZERO POPUPS)
    // ==========================================
    function navigateTo(toolId) {
      // Hide all tool views
      document.querySelectorAll('.tool-view').forEach(el => el.classList.add('hidden'));

      const target = document.getElementById('view-' + toolId);
      if (target) {
        target.classList.remove('hidden');
        // Hide floating bottom nav dock for full-screen focus
        const nav = document.getElementById('app-bottom-nav');
        if (nav) nav.classList.add('hidden');
        navHistory.push(toolId);

        // Refresh view data
        if (toolId === 'screentime') refreshScreenTime();
        if (toolId === 'focus') syncFocusState();
        if (toolId === 'habits') renderHeatmap();
      }
    }

    function navigateBack() {
      if (navHistory.length > 1) {
        const current = navHistory.pop();
        const currentEl = document.getElementById('view-' + current);
        if (currentEl) currentEl.classList.add('hidden');

        const prev = navHistory[navHistory.length - 1];
        const prevTool = document.getElementById('view-' + prev);
        if (prevTool) {
          prevTool.classList.remove('hidden');
        } else {
          // Returned to main tab
          const nav = document.getElementById('app-bottom-nav');
          if (nav) nav.classList.remove('hidden');
          switchTab(prev || 'home');
        }
      } else {
        const nav = document.getElementById('app-bottom-nav');
        if (nav) nav.classList.remove('hidden');
        switchTab('home');
      }
    }

    // Hardware Back Button Integration
    window.handleBackPress = function() {
      // 1. If currently inside a tool-view full page:
      const activeTool = document.querySelector('.tool-view:not(.hidden)');
      if (activeTool) {
        navigateBack();
        return true; // Handled!
      }
      // 2. If on a secondary tab, navigate back to home:
      const activeTab = document.querySelector('.page-tab.active');
      if (activeTab && activeTab.id !== 'tab-home') {
        switchTab('home');
        return true; // Handled!
      }
      // 3. On home tab: let Android handle back press (minimize)
      return false;
    };

    // Aliases so old calls smoothly route to full pages
    window.openFocusModal = () => navigateTo('focus');
    window.openReadingModal = () => navigateTo('reading');
    window.openHabitsModal = () => navigateTo('habits');
    window.openScreenTimeModal = () => navigateTo('screentime');
    window.openMoodModal = () => navigateTo('mood');
    window.openGitaModal = () => navigateTo('gita');
    window.openWriteModal = () => navigateTo('write');

    // ==========================================
    // FOCUS SANCTUARY NATIVE TIMER
    // ==========================================
    let focusMinutes = 25;
    let focusSecondsRemaining = 25 * 60;
    let focusIsRunning = false;

    function formatTime(totalSec) {
      const m = Math.floor(totalSec / 60);
      const s = totalSec % 60;
      return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    }

    function syncFocusState() {
      const state = nativeInvoke('timer:getState');
      if (state) {
        if (state.status === 'RUNNING') {
          focusSecondsRemaining = Math.max(0, Math.floor(state.remaining_ms / 1000));
          focusIsRunning = true;
        } else if (state.status === 'PAUSED') {
          focusSecondsRemaining = Math.max(0, Math.floor(state.remaining_ms / 1000));
          focusIsRunning = false;
        } else {
          focusIsRunning = false;
        }
      }
      updateTimerUI();
    }

    function updateTimerUI() {
      const timeStr = formatTime(focusSecondsRemaining);
      const homeDisplay = document.getElementById('home-timer-display');
      if (homeDisplay) homeDisplay.textContent = timeStr;
      const modalTime = document.getElementById('focus-modal-time');
      if (modalTime) modalTime.textContent = timeStr;

      // Update ring
      const totalBlockSec = focusMinutes * 60;
      const pct = Math.max(0, Math.min(1, focusSecondsRemaining / totalBlockSec));
      const ring = document.getElementById('focus-progress-ring');
      if (ring) {
        const offset = 628.31 * (1 - pct);
        ring.style.strokeDashoffset = offset;
      }

      // Update buttons
      const playIcon = document.getElementById('focus-modal-play-icon');
      const playText = document.getElementById('focus-modal-play-text');
      const homeBtnText = document.getElementById('home-timer-btn-text');
      if (playIcon) playIcon.textContent = focusIsRunning ? 'pause' : 'play_arrow';
      if (playText) playText.textContent = focusIsRunning ? 'Pause focus' : 'Start focus';
      if (homeBtnText) homeBtnText.textContent = focusIsRunning ? 'Pause focus' : 'Start focus';
    }

    function toggleFocusTimer() {
      if (focusIsRunning) {
        nativeInvoke('timer:pause');
        focusIsRunning = false;
      } else {
        nativeInvoke('timer:start', {
          topic: 'Focus Sanctuary',
          category: 'Sanctuary',
          mode: 'COUNTDOWN',
          targetMinutes: focusMinutes
        });
        focusIsRunning = true;
      }
      updateTimerUI();
    }

    function toggleHomeTimer() {
      toggleFocusTimer();
    }

    function resetFocusTimer() {
      nativeInvoke('timer:finish', { notes: '' });
      focusIsRunning = false;
      focusSecondsRemaining = focusMinutes * 60;
      updateTimerUI();
    }

    function adjustFocusMinutes(delta) {
      focusMinutes = Math.max(5, Math.min(120, focusMinutes + delta));
      focusSecondsRemaining = focusMinutes * 60;
      document.getElementById('focus-target-duration').textContent = `${focusMinutes}m Block`;
      updateTimerUI();
    }

    function setFocusPreset(mins) {
      focusMinutes = mins;
      focusSecondsRemaining = mins * 60;
      document.getElementById('focus-target-duration').textContent = `${focusMinutes}m Block`;
      updateTimerUI();
    }

    function skipFocusCycle() {
      resetFocusTimer();
    }

    function setFocusMode(mode) {
      const pomoBtn = document.getElementById('focus-mode-pomodoro');
      const timerBtn = document.getElementById('focus-mode-timer');
      if (mode === 'pomodoro') {
        pomoBtn.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        timerBtn.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
      } else {
        timerBtn.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        pomoBtn.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
      }
    }

    // Periodic sync with native timer service
    setInterval(() => {
      const state = nativeInvoke('timer:getState');
      if (state && state.status === 'RUNNING') {
        focusSecondsRemaining = Math.max(0, Math.floor(state.remaining_ms / 1000));
        focusIsRunning = true;
        updateTimerUI();
      } else if (state && state.status === 'PAUSED') {
        focusIsRunning = false;
        updateTimerUI();
      }
    }, 1000);

    // ==========================================
    // SCREEN TIME NATIVE TELEMETRY
    // ==========================================
    function refreshScreenTime() {
      const res = nativeInvoke('telemetry:getDetailedScreenTime');
      if (res) {
        const banner = document.getElementById('screentime-perm-banner');
        if (!res.hasPermission) {
          if (banner) banner.classList.remove('hidden');
        } else {
          if (banner) banner.classList.add('hidden');
        }

        const totalStr = res.formattedTotal || '0m';
        document.getElementById('stat-screen-time').textContent = totalStr;
        document.getElementById('day-screen-time').textContent = totalStr;
        document.getElementById('tools-screen-time-label').textContent = `${totalStr} screen time`;
        document.getElementById('screentime-modal-total').textContent = totalStr;

        const list = document.getElementById('screentime-apps-list');
        if (list && res.topApps && res.topApps.length > 0) {
          list.innerHTML = '';
          res.topApps.forEach(app => {
            const div = document.createElement('div');
            div.className = 'p-3.5 flex items-center justify-between border-b border-surface-container-low last:border-b-0';
            const initial = (app.appName || 'A').charAt(0).toUpperCase();
            const pct = Math.round(app.percentage || 0);
            div.innerHTML = `
              <div class="flex items-center gap-3 min-w-0 flex-1 pr-2">
                <div class="w-9 h-9 rounded-xl bg-surface-container-low flex items-center justify-center text-primary font-bold text-sm shrink-0">
                  ${initial}
                </div>
                <div class="flex flex-col min-w-0 flex-1">
                  <div class="flex items-center justify-between">
                    <span class="text-sm font-semibold text-on-surface truncate">${cleanText(app.appName)}</span>
                    <span class="text-xs text-on-surface font-semibold ml-2">${app.formatted}</span>
                  </div>
                  <div class="flex items-center justify-between mt-1">
                    <span class="text-[11px] text-on-surface-variant truncate">${cleanText(app.category || 'App')}</span>
                    <span class="text-[10px] text-primary font-medium">${pct}%</span>
                  </div>
                  <div class="w-full h-1 bg-surface-container-low rounded-full mt-1.5 overflow-hidden">
                    <div class="h-full bg-primary rounded-full" style="width: ${Math.max(4, pct)}%"></div>
                  </div>
                </div>
              </div>
            `;
            list.appendChild(div);
          });
        }
      }
    }

    function requestUsagePermission() {
      nativeInvoke('telemetry:requestPermission');
    }

    // ==========================================
    // SQLITE PERSISTENT TASKS
    // ==========================================
    let tasks = [];

    function renderTasks() {
      const container = document.getElementById('home-tasks-list');
      if (!container) return;
      container.innerHTML = '';

      const doneCount = tasks.filter(t => t.done).length;
      document.getElementById('tasks-done-badge').textContent = `${doneCount} done`;

      tasks.forEach(t => {
        const item = document.createElement('div');
        item.className = 'flex items-center justify-between p-3 rounded-xl bg-surface-container-low/70 border border-surface-container-high/30 cursor-pointer active:scale-[0.99] transition-all';
        item.onclick = () => toggleTask(t.id);
        item.innerHTML = `
          <div class="flex items-center gap-2.5 min-w-0 flex-1">
            <div class="w-5 h-5 rounded-md flex items-center justify-center shrink-0 ${t.done ? 'bg-primary text-on-primary' : 'border border-outline-variant'}">
              <span class="material-symbols-outlined text-[15px] ${t.done ? '' : 'opacity-0'}">check</span>
            </div>
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-semibold text-on-surface truncate ${t.done ? 'line-through opacity-60' : ''}">${cleanText(t.title)}</span>
              <span class="text-[10px] text-on-surface-variant">${cleanText(t.time || 'Today')}</span>
            </div>
          </div>
          <button onclick="event.stopPropagation(); deleteTask('${t.id}')" class="text-outline hover:text-error p-1 rounded-full active:scale-90">
            <span class="material-symbols-outlined text-[16px]">close</span>
          </button>
        `;
        container.appendChild(item);
      });
    }

    function loadTasks() {
      const res = nativeInvoke('tasks:list');
      if (res && res.tasks && res.tasks.length > 0) {
        tasks = res.tasks.map(t => ({
          id: t.id,
          title: t.title,
          time: t.deadline || 'Today',
          done: t.done === true || t.status === 'completed'
        }));
      } else {
        // Seed default starter tasks into SQLite
        const starter = [
          { title: 'Review morning pages', priority: 'MEDIUM' },
          { title: 'Water the plants by window', priority: 'LOW' },
          { title: 'Read 20 pages in evening light', priority: 'MEDIUM' }
        ];
        starter.forEach(s => nativeInvoke('tasks:add', s));
        const res2 = nativeInvoke('tasks:list');
        if (res2 && res2.tasks) {
          tasks = res2.tasks.map(t => ({
            id: t.id,
            title: t.title,
            time: t.deadline || 'Today',
            done: t.done === true || t.status === 'completed'
          }));
        }
      }
      renderTasks();
    }

    function toggleTask(id) {
      nativeInvoke('tasks:toggle', { id: id });
      const t = tasks.find(x => x.id === id);
      if (t) t.done = !t.done;
      renderTasks();
    }

    function deleteTask(id) {
      nativeInvoke('tasks:delete', { id: id });
      tasks = tasks.filter(x => x.id !== id);
      renderTasks();
    }

    function promptAddTask() {
      const title = prompt('Enter a gentle task for today:');
      if (title && title.trim()) {
        const res = nativeInvoke('tasks:add', { title: title.trim(), priority: 'MEDIUM' });
        if (res && res.id) {
          tasks.push({ id: res.id, title: cleanText(title.trim()), time: 'Today', done: false });
          renderTasks();
        }
      }
    }

    // ==========================================
    // SQLITE PERSISTENT THOUGHTS & JOURNAL
    // ==========================================
    let thoughts = [];
    let activeTag = 'all';

    function renderJournalEntries() {
      const container = document.getElementById('journal-entries-stream');
      if (!container) return;
      container.innerHTML = '';

      const filtered = activeTag === 'all'
        ? thoughts
        : thoughts.filter(t => t.tags && t.tags.includes(activeTag));

      if (filtered.length === 0) {
        container.innerHTML = `
          <div class="py-8 text-center text-on-surface-variant text-xs">
            No reflections found. Tap Write to preserve a quiet thought.
          </div>
        `;
        return;
      }

      filtered.forEach(th => {
        const div = document.createElement('div');
        div.className = 'bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col space-y-2';
        div.style.boxShadow = 'var(--card-shadow)';
        const tagBadges = (th.tags || []).map(tag => `<span class="px-2 py-0.5 rounded-full bg-surface-container-low text-[10px] text-primary font-medium">#${cleanText(tag)}</span>`).join(' ');
        div.innerHTML = `
          <div class="flex items-center justify-between text-xs text-on-surface-variant">
            <span class="font-medium">${cleanText(th.date)} · ${cleanText(th.time)}</span>
            <button onclick="deleteThought('${th.id}')" class="text-outline hover:text-error p-1 rounded-full active:scale-90" title="Delete">
              <span class="material-symbols-outlined text-[16px]">close</span>
            </button>
          </div>
          <p class="text-xs text-on-surface leading-relaxed">${cleanText(th.text)}</p>
          <div class="flex items-center gap-1.5 pt-1">
            ${tagBadges}
          </div>
        `;
        container.appendChild(div);
      });
    }

    function loadThoughts() {
      const res = nativeInvoke('thoughts:list');
      if (res && res.thoughts && res.thoughts.length > 0) {
        thoughts = res.thoughts.map(t => ({
          id: t.id,
          text: t.content,
          date: t.date || 'Today',
          time: new Date(t.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          tags: t.tags ? t.tags.split(' ').map(x => x.replace('#', '')) : ['calm']
        }));
      } else {
        const starter = [
          { content: 'Morning clarity after green tea. Finished the chapter on solitude. The quiet hours of early dawn bring an unmatched spaciousness.', tags: '#calm #morning' },
          { content: 'Thoughts on deep focus without artificial urgency. When we remove notifications, attention naturally deepens into slow flow.', tags: '#reading #focus' },
          { content: 'Walk through the pine canopy before dusk. The air smelled of damp cedar. Gratitude for quiet evenings with no alarms.', tags: '#gratitude #calm' }
        ];
        starter.forEach(s => nativeInvoke('thoughts:add', { content: s.content, topic: 'Sanctuary', tags: s.tags }));
        const res2 = nativeInvoke('thoughts:list');
        if (res2 && res2.thoughts) {
          thoughts = res2.thoughts.map(t => ({
            id: t.id,
            text: t.content,
            date: t.date || 'Today',
            time: new Date(t.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            tags: t.tags ? t.tags.split(' ').map(x => x.replace('#', '')) : ['calm']
          }));
        }
      }
      renderJournalEntries();
    }

    function handleSaveThought(e) {
      if (e) e.preventDefault();
      const input = document.getElementById('thought-input');
      const tagInput = document.getElementById('thought-tag-input');
      if (!input || !input.value.trim()) return;

      const text = input.value.trim();
      const tags = (tagInput && tagInput.value.trim()) || '#calm';

      const res = nativeInvoke('thoughts:add', { content: text, topic: 'Sanctuary', tags: tags });
      if (res && res.id) {
        thoughts.unshift({
          id: res.id,
          text: cleanText(text),
          date: 'Today',
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          tags: tags.split(' ').map(x => x.replace('#', ''))
        });
        input.value = '';
        renderJournalEntries();
        navigateBack();
      }
    }

    function deleteThought(id) {
      nativeInvoke('thoughts:delete', { id: id });
      thoughts = thoughts.filter(x => x.id !== id);
      renderJournalEntries();
    }

    function selectJournalTag(tag, btn) {
      activeTag = tag;
      document.querySelectorAll('.journal-filter-chip').forEach(c => {
        c.className = 'journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer';
      });
      if (btn) btn.className = 'journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-primary text-on-primary shrink-0 transition-colors shadow-sm cursor-pointer';
      renderJournalEntries();
    }

    function applyJournalFilters() {
      const q = (document.getElementById('journal-search').value || '').toLowerCase();
      const container = document.getElementById('journal-entries-stream');
      if (!container) return;
      container.innerHTML = '';

      const filtered = thoughts.filter(t => {
        const matchesTag = activeTag === 'all' || (t.tags && t.tags.includes(activeTag));
        const matchesQuery = !q || t.text.toLowerCase().includes(q);
        return matchesTag && matchesQuery;
      });

      if (filtered.length === 0) {
        container.innerHTML = `<div class="py-8 text-center text-on-surface-variant text-xs">No matching thoughts found.</div>`;
        return;
      }

      filtered.forEach(th => {
        const div = document.createElement('div');
        div.className = 'bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col space-y-2';
        const tagBadges = (th.tags || []).map(tag => `<span class="px-2 py-0.5 rounded-full bg-surface-container-low text-[10px] text-primary font-medium">#${cleanText(tag)}</span>`).join(' ');
        div.innerHTML = `
          <div class="flex items-center justify-between text-xs text-on-surface-variant">
            <span class="font-medium">${cleanText(th.date)} · ${cleanText(th.time)}</span>
            <button onclick="deleteThought('${th.id}')" class="text-outline hover:text-error p-1 rounded-full active:scale-90" title="Delete">
              <span class="material-symbols-outlined text-[16px]">close</span>
            </button>
          </div>
          <p class="text-xs text-on-surface leading-relaxed">${cleanText(th.text)}</p>
          <div class="flex items-center gap-1.5 pt-1">${tagBadges}</div>
        `;
        container.appendChild(div);
      });
    }

    // ==========================================
    // READING COMPANION STATE
    // ==========================================
    let currentReadingPage = 142;
    const totalReadingPages = 338;

    function adjustBookPages(delta) {
      currentReadingPage = Math.max(1, Math.min(totalReadingPages, currentReadingPage + delta));
      const pct = Math.round((currentReadingPage / totalReadingPages) * 100);
      document.getElementById('reading-percent-label').textContent = `${pct}% complete`;
      document.getElementById('home-book-percent').textContent = `${pct}% complete`;
      document.getElementById('tools-reading-pct').textContent = `${pct}%`;
      document.getElementById('reading-modal-bar').style.width = `${pct}%`;
      document.getElementById('home-book-bar').style.width = `${pct}%`;
      document.getElementById('reading-page-info').textContent = `Page ${currentReadingPage} of ${totalReadingPages}`;
      nativeInvoke('storage:set', { key: 'reading_page', value: String(currentReadingPage) });
    }

    function promptUpdatePage() {
      const p = prompt('Enter current page number:', currentReadingPage);
      if (p && !isNaN(parseInt(p))) {
        currentReadingPage = parseInt(p);
        adjustBookPages(0);
      }
    }

    // ==========================================
    // GENTLE HABITS STATE
    // ==========================================
    function toggleHabitItem(card) {
      const check = card.querySelector('.habit-check');
      const title = card.querySelector('.habit-title');
      const icon = check.querySelector('.material-symbols-outlined');
      const isDone = check.classList.contains('bg-primary');

      if (isDone) {
        check.classList.remove('bg-primary', 'text-on-primary');
        check.classList.add('border-2', 'border-outline-variant', 'bg-transparent');
        icon.classList.add('opacity-0');
        title.classList.remove('line-through', 'opacity-70');
      } else {
        check.classList.remove('border-2', 'border-outline-variant', 'bg-transparent');
        check.classList.add('bg-primary', 'text-on-primary');
        icon.classList.remove('opacity-0');
        title.classList.add('line-through', 'opacity-70');
      }

      // Update count
      const doneItems = document.querySelectorAll('#habits-modal-list .habit-check.bg-primary').length;
      const totalItems = document.querySelectorAll('#habits-modal-list .habit-check').length;
      document.getElementById('habits-ratio-badge').textContent = `${doneItems} of ${totalItems} done`;
      document.getElementById('stat-habits-met').textContent = `${doneItems} of ${totalItems}`;
      document.getElementById('day-habits-count').textContent = doneItems;
      document.getElementById('tools-habits-cadence').textContent = `${doneItems} of ${totalItems} rituals met today`;
    }

    function promptAddHabit() {
      const h = prompt('Enter a new daily ritual:');
      if (h && h.trim()) {
        const list = document.getElementById('habits-modal-list');
        const div = document.createElement('div');
        div.className = 'habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs cursor-pointer active:scale-[0.99]';
        div.onclick = function() { toggleHabitItem(this); };
        div.innerHTML = `
          <div class="flex items-center gap-3 flex-1">
            <div class="habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title text-sm text-on-surface font-medium">${cleanText(h.trim())}</span>
              <span class="text-[11px] text-on-surface-variant">Scheduled daily</span>
            </div>
          </div>
        `;
        list.appendChild(div);
      }
    }

    function renderHeatmap() {
      const grid = document.getElementById('heatmap-grid');
      if (!grid) return;
      grid.innerHTML = '';
      for (let i = 1; i <= 28; i++) {
        const span = document.createElement('span');
        span.className = 'w-6 h-6 rounded-md flex items-center justify-center text-[10px] font-medium ';
        if (i % 5 === 0) span.className += 'bg-primary text-on-primary';
        else if (i % 3 === 0) span.className += 'bg-primary-container text-on-primary';
        else if (i % 2 === 0) span.className += 'bg-surface-container-high text-on-surface';
        else span.className += 'bg-surface-container-low text-on-surface-variant';
        span.textContent = i;
        grid.appendChild(span);
      }
    }

    // ==========================================
    // MOOD & ENERGY STATE
    // ==========================================
    function switchMoodSubtab(tab) {
      const checkinView = document.getElementById('mood-view-checkin');
      const panicView = document.getElementById('mood-view-panic');
      const btnCheckin = document.getElementById('tab-btn-mood-checkin');
      const btnPanic = document.getElementById('tab-btn-panic-vault');

      if (tab === 'checkin') {
        checkinView.classList.remove('hidden');
        panicView.classList.add('hidden');
        btnCheckin.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        btnPanic.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
      } else {
        panicView.classList.remove('hidden');
        checkinView.classList.add('hidden');
        btnPanic.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        btnCheckin.className = 'flex-1 py-1.5 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
      }
    }

    let selectedMoodKey = 'calm';
    let selectedMoodLabel = 'Calm & Centered';

    function selectMood(key, label, emoji) {
      selectedMoodKey = key;
      selectedMoodLabel = label;
      document.getElementById('mood-selected-label').textContent = label;
      document.getElementById('tools-mood-preview').textContent = label;
      document.querySelectorAll('#mood-pill-grid .mood-opt').forEach(opt => {
        opt.className = 'mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface transition-all cursor-pointer';
      });
      event.currentTarget.className = 'mood-opt flex flex-col items-center justify-center py-2.5 px-1 rounded-xl bg-primary text-on-primary shadow-xs transition-all cursor-pointer';
    }

    function updateEnergyDisplay(val) {
      document.getElementById('mood-energy-display').textContent = `${val} / 10`;
      document.getElementById('tools-energy-preview').textContent = `Energy level: ${val}/10`;
    }

    function saveMoodCheckin() {
      const slider = document.getElementById('mood-energy-slider');
      const note = document.getElementById('mood-reflection-input');
      const text = note ? note.value.trim() : '';

      if (text) {
        nativeInvoke('thoughts:add', {
          content: `[Mood: ${selectedMoodLabel}, Energy: ${slider.value}/10] ${text}`,
          topic: 'Mood',
          tags: '#mood #checkin'
        });
        loadThoughts();
      }
      navigateBack();
    }

    // ==========================================
    // BHAGAVAD GITA WISDOM VERSES
    // ==========================================
    const gitaVerses = [
      {
        ref: 'Chapter 2 • Verse 47',
        concept: 'Nishkama Karma',
        sanskrit: 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥',
        translation: 'You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty.'
      },
      {
        ref: 'Chapter 6 • Verse 5',
        concept: 'Self-Elevation',
        sanskrit: 'उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।\nआत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः॥',
        translation: 'Elevate yourself through the power of your mind, and do not degrade yourself, for the mind can be the friend and also the enemy of the self.'
      },
      {
        ref: 'Chapter 2 • Verse 70',
        concept: 'Ocean of Peace',
        sanskrit: 'आपूर्यमाणमचलप्रतिष्ठं समुद्रमापः प्रविशन्ति यद्वत्।\nतद्वत्कामा यं प्रविशन्ति सर्वे स शान्तिमाप्नोति न कामकामी॥',
        translation: 'As the ocean remains calm and undisturbed even as rivers pour water into it, so the person who remains peaceful despite endless streams of desires achieves tranquility.'
      }
    ];
    let gitaIndex = 0;

    function nextGitaVerse() {
      gitaIndex = (gitaIndex + 1) % gitaVerses.length;
      const v = gitaVerses[gitaIndex];
      document.getElementById('gita-chapter-verse').textContent = v.ref;
      document.getElementById('gita-sanskrit').innerHTML = v.sanskrit.replace('\n', '<br/>');
      document.getElementById('gita-translation').textContent = `"${v.translation}"`;
    }

    function saveGitaReflection() {
      const v = gitaVerses[gitaIndex];
      nativeInvoke('thoughts:add', {
        content: `[Gita ${v.ref}] "${v.translation}"`,
        topic: 'Gita',
        tags: '#gita #wisdom'
      });
      loadThoughts();
      navigateBack();
    }

    // ==========================================
    // THEME HANDLING
    // ==========================================
    function setAppTheme(theme) {
      if (theme === 'dark') {
        document.documentElement.classList.add('dark');
        localStorage.setItem('v07_theme', 'dark');
      } else {
        document.documentElement.classList.remove('dark');
        localStorage.setItem('v07_theme', 'light');
      }
    }

    function initTheme() {
      const saved = localStorage.getItem('v07_theme') || 'dark';
      setAppTheme(saved);
    }

    // Greeting
    function updateGreeting() {
      const now = new Date();
      const hour = now.getHours();
      const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
      const dayName = days[now.getDay()];

      const dayChip = document.getElementById('home-greeting-day');
      if (dayChip) dayChip.textContent = `${dayName} sanctuary`;

      let greeting = 'Good morning, Sarah';
      if (hour >= 12 && hour < 17) greeting = 'Good afternoon, Sarah';
      else if (hour >= 17) greeting = 'Good evening, Sarah';

      const title = document.getElementById('home-greeting-title');
      if (title) title.textContent = greeting;
    }

    // ==========================================
    // INITIALIZATION
    // ==========================================
    window.addEventListener('DOMContentLoaded', () => {
      initTheme();
      updateGreeting();
      loadTasks();
      loadThoughts();
      refreshScreenTime();
      renderHeatmap();
      syncFocusState();

      // Read stored page if available
      const storedPage = nativeInvoke('storage:get', { key: 'reading_page' });
      if (storedPage && storedPage.value) {
        currentReadingPage = parseInt(storedPage.value) || 142;
        adjustBookPages(0);
      }
    });
  </script>
</body>
</html>
'''

# Write to target files
targets = [
    r'D:\software\V07-Android\app\src\main\assets\v3\index.html',
    r'D:\software\V07-Android\app\src\main\assets\index.html'
]

for t in targets:
    os.makedirs(os.path.dirname(t), exist_ok=True)
    with open(t, 'w', encoding='utf-8') as f:
        f.write(html_content)
    print(f"Successfully generated {len(html_content)} bytes to {t}")
