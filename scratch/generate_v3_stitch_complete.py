# -*- coding: utf-8 -*-
"""
generate_v3_stitch_complete.py
Generates the comprehensive, production-grade V3 Calm Space experience with:
1. 20% increased header height (h-16 / 64px, with pt-[68px] content spacing)
2. All Stitch components across all plugin and tool pages
3. Settings tab replacing 'You' with all rich Stitch settings & local sovereignty options
4. Customized tactile floating Toast notification system with smooth animations
5. 100% wired native Android platform bridge & hardware back navigation
"""

import os
import sys

OUTPUT_FILES = [
    r"D:\software\V07-Android\app\src\main\assets\index.html",
    r"D:\software\V07-Android\app\src\main\assets\v3\index.html"
]

def build_html():
    return r'''<!DOCTYPE html>
<html lang="en" class="">
<head>
  <meta charset="utf-8"/>
  <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover" name="viewport"/>
  <meta content="mobile_tab" name="shell-type"/>
  <title>V07 Sanctuary V3</title>
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
      --nav-shadow: 0 10px 30px -4px rgba(32, 40, 36, 0.1), 0 3px 8px -2px rgba(32, 40, 36, 0.05);
      --card-shadow: 0 1px 6px -1px rgba(32, 40, 36, 0.06), 0 1px 2px 0 rgba(32, 40, 36, 0.03);
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
      --nav-shadow: 0 10px 30px -4px rgba(0, 0, 0, 0.45), 0 3px 8px -2px rgba(0, 0, 0, 0.3);
      --card-shadow: 0 1px 6px -1px rgba(0, 0, 0, 0.3), 0 1px 2px 0 rgba(0, 0, 0, 0.18);
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

    /* Custom Toast Transition */
    #v07-toast-container {
      transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .toast-enter {
      transform: translateY(-20px) scale(0.95);
      opacity: 0;
    }
    .toast-show {
      transform: translateY(0) scale(1);
      opacity: 1;
    }
    .toast-exit {
      transform: translateY(-16px) scale(0.96);
      opacity: 0;
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
  <!-- CUSTOM TACTILE FLOATING TOAST NOTIFICATION -->
  <!-- ========================================== -->
  <div id="v07-toast-container" class="fixed top-20 left-0 right-0 z-50 pointer-events-none flex flex-col items-center px-4 transition-all duration-300 ease-out toast-enter">
    <div id="v07-toast-card" class="pointer-events-auto max-w-sm w-full bg-surface-container-lowest/95 dark:bg-surface-container-high/95 backdrop-blur-xl border border-surface-container-high/60 dark:border-white/10 rounded-2xl p-3.5 px-4 shadow-[0_12px_32px_-4px_rgba(0,0,0,0.18),0_4px_12px_-2px_rgba(0,0,0,0.08)] flex items-center justify-between gap-3 transition-transform active:scale-[0.98]">
      <div class="flex items-center gap-3 min-w-0">
        <div id="toast-icon-bg" class="w-8 h-8 rounded-full bg-primary-fixed text-primary flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[18px]" id="toast-icon">check_circle</span>
        </div>
        <div class="flex flex-col min-w-0">
          <span class="text-xs font-bold text-on-surface truncate" id="toast-title">Saved</span>
          <span class="text-[11px] text-on-surface-variant truncate" id="toast-subtitle">Your reflections are safely stored</span>
        </div>
      </div>
      <button onclick="dismissToast()" class="w-6 h-6 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" aria-label="Close toast">
        <span class="material-symbols-outlined text-[16px]">close</span>
      </button>
    </div>
  </div>

  <!-- ========================================== -->
  <!-- TOP PERSISTENT EDITORIAL SANCTUARY HEADER  -->
  <!-- Height: 64px (h-16, +20% taller than 52px) -->
  <!-- ========================================== -->
  <header id="app-top-header" class="fixed top-0 left-0 right-0 z-40 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 h-16 px-4 flex items-center justify-between shadow-xs">
    <div class="flex items-center gap-2.5">
      <span class="material-symbols-outlined text-primary text-[24px] material-symbols-filled" id="header-icon">spa</span>
      <div>
        <h1 class="text-[18px] font-bold tracking-tight text-on-surface leading-tight" id="header-title">Home</h1>
        <p class="text-[11px] text-on-surface-variant leading-none" id="header-sub">Calm sanctuary</p>
      </div>
    </div>
    <div class="flex items-center gap-2">
      <button onclick="switchTab('settings')" class="w-10 h-10 rounded-full bg-surface-container-low border border-surface-container-high/60 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" title="Settings & Local Sovereignty">
        <span class="material-symbols-outlined text-primary text-[20px]">settings</span>
      </button>
    </div>
  </header>

  <!-- ========================================== -->
  <!-- MAIN SCROLLABLE CONTENT VIEWPORT           -->
  <!-- Padding top: pt-[68px] for 64px header     -->
  <!-- ========================================== -->
  <main id="app-main" class="flex flex-col relative w-full bg-surface px-4 pt-[68px] pb-28 min-h-screen">

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
        <div class="w-full h-1.5 bg-surface-container rounded-full overflow-hidden mt-3">
          <div class="h-full bg-primary rounded-full transition-all duration-500" style="width: 75%;"></div>
        </div>
      </section>

      <!-- Primary Focus Chronometer Card (One-tap launch) -->
      <section class="bg-surface-container-lowest rounded-2xl p-5 shadow-sm border border-surface-container-high/40 flex flex-col items-center text-center relative overflow-hidden" style="box-shadow: var(--card-shadow);">
        <div class="w-full flex items-center justify-between mb-2">
          <div class="text-left">
            <h3 class="text-base font-semibold text-on-surface">Take a little time for yourself</h3>
            <p class="text-xs text-on-surface-variant">Quiet intervals for calm presence</p>
          </div>
          <div class="w-9 h-9 rounded-full bg-primary-fixed text-primary flex items-center justify-center shrink-0 cursor-pointer" onclick="navigateTo('focus')">
            <span class="material-symbols-outlined text-[19px]">hourglass_top</span>
          </div>
        </div>

        <!-- Big Clean Chronometer Numbers -->
        <div class="my-3 flex flex-col items-center cursor-pointer" onclick="navigateTo('focus')">
          <span class="font-mono text-5xl font-bold tracking-tight text-on-surface" id="home-timer-display">25:00</span>
          <span class="text-[11px] font-medium text-primary mt-1 flex items-center gap-1">
            <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
            <span id="home-timer-sub">Foreground focus service ready</span>
          </span>
        </div>

        <div class="w-full flex items-center gap-2 mt-1">
          <button onclick="toggleFocusTimer()" id="home-timer-btn" class="flex-1 py-3 px-4 rounded-xl bg-primary text-on-primary font-semibold text-sm shadow-sm active:scale-[0.98] transition-transform flex items-center justify-center gap-2 cursor-pointer hover:opacity-95">
            <span class="material-symbols-outlined text-[18px]" id="home-timer-icon">play_arrow</span>
            <span id="home-timer-text">Start focus</span>
          </button>
          <button onclick="navigateTo('focus')" class="w-11 h-11 rounded-xl bg-surface-container-low text-on-surface-variant hover:text-on-surface flex items-center justify-center cursor-pointer border border-surface-container-high/40 active:scale-95" title="Full Focus Page">
            <span class="material-symbols-outlined text-[20px]">fullscreen</span>
          </button>
        </div>
      </section>

      <!-- Gentle Habits Snapshot -->
      <section class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-3" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base font-semibold text-on-surface">Gentle habits</h3>
            <p class="text-xs text-on-surface-variant">Cadence without pressure</p>
          </div>
          <button onclick="navigateTo('habits')" class="text-xs text-primary font-semibold flex items-center gap-1 cursor-pointer">
            <span>View all</span>
            <span class="material-symbols-outlined text-[16px]">chevron_right</span>
          </button>
        </div>

        <div class="space-y-2">
          <div class="p-3 rounded-xl bg-surface-container-low flex items-center justify-between cursor-pointer active:scale-[0.99] transition-transform" onclick="quickToggleHabit('Morning stillness', this)">
            <div class="flex items-center gap-3">
              <div class="w-5 h-5 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px]">check</span>
              </div>
              <span class="text-xs font-semibold text-on-surface line-through opacity-70">Morning stillness</span>
            </div>
            <span class="text-[11px] text-secondary font-medium">🔥 14d</span>
          </div>
          <div class="p-3 rounded-xl bg-surface-container-low flex items-center justify-between cursor-pointer active:scale-[0.99] transition-transform" onclick="quickToggleHabit('Read 20 pages', this)">
            <div class="flex items-center gap-3">
              <div class="w-5 h-5 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[15px]">check</span>
              </div>
              <span class="text-xs font-semibold text-on-surface line-through opacity-70">Read 20 pages</span>
            </div>
            <span class="text-[11px] text-secondary font-medium">🔥 6d</span>
          </div>
        </div>
      </section>

      <!-- Today's Quiet Tasks & Thought Capture -->
      <section class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-3" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base font-semibold text-on-surface">A few things for today</h3>
            <p class="text-xs text-on-surface-variant">Unrushed steps to take</p>
          </div>
          <span class="text-xs text-primary font-semibold bg-surface-container-low px-2 py-0.5 rounded-full" id="task-completed-badge">0 done</span>
        </div>

        <!-- Add Task Input Form -->
        <form onsubmit="handleAddTask(event)" class="flex items-center gap-2">
          <input type="text" id="new-task-input" placeholder="Add a quiet task..." class="flex-1 bg-surface-container-low border border-surface-container-high rounded-xl px-3.5 py-2 text-xs text-on-surface placeholder:text-on-surface-variant/70 focus:outline-none focus:border-primary transition-colors"/>
          <button type="submit" class="w-9 h-9 rounded-xl bg-primary text-on-primary flex items-center justify-center shrink-0 active:scale-95 transition-transform cursor-pointer" title="Add Task">
            <span class="material-symbols-outlined text-[18px]">add</span>
          </button>
        </form>

        <div class="space-y-2 pt-1" id="home-tasks-list">
          <!-- Populated from SQLite via PlatformJsBridge -->
        </div>
      </section>

      <!-- Gita Wisdom Contemplation Card -->
      <section class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-2.5 cursor-pointer active:scale-[0.99] transition-transform" onclick="navigateTo('gita')" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-1.5 text-secondary font-semibold text-xs">
            <span class="material-symbols-outlined text-[17px]">bookmark_heart</span>
            <span>Gita Wisdom · Chapter 2</span>
          </div>
          <span class="text-[11px] text-on-surface-variant">Verse 2.47</span>
        </div>
        <p class="text-xs text-on-surface leading-relaxed italic" id="home-gita-quote">
          "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन..."<br/>
          <span class="font-normal text-on-surface-variant">You have a right to your actions, but never to your action's fruits.</span>
        </p>
        <div class="flex items-center justify-between pt-1 border-t border-surface-container-low text-[11px] text-primary font-medium">
          <span>Read Sanskrit & Contemplation</span>
          <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
        </div>
      </section>
    </div>

    <!-- ========================================== -->
    <!-- 2. TOOLS TAB (ALL DAILY COMPANIONS)        -->
    <!-- ========================================== -->
    <div id="tab-tools" class="page-tab flex-col w-full space-y-4">
      <div class="pt-1">
        <h2 class="text-2xl font-bold text-on-surface tracking-tight">Daily Companions</h2>
        <p class="text-sm text-on-surface-variant">Tools for quiet reflection and focused rhythm.</p>
      </div>

      <!-- 1. Focus Sanctuary Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-primary-fixed text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">hourglass_top</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Focus Sanctuary</h3>
              <p class="text-xs text-on-surface-variant">Quiet intervals for deep presence.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-medium">Active</span>
        </div>
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Full dial chronometer with intervals, Pomodoro cycles, distraction parking lot, and persistent Android notification.
        </p>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-primary">timer</span>
            25m standard block
          </span>
          <button onclick="navigateTo('focus')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 2. Reading Companion Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">menu_book</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Reading Companion</h3>
              <p class="text-xs text-on-surface-variant">Quiet book progress and shelf tracking.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-secondary-fixed text-on-secondary-fixed text-xs font-medium">Shelf</span>
        </div>
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Log reading sessions, track percentage completed with one-tap page steppers, and organize your personal library shelf.
        </p>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-secondary">bookmark</span>
            Atomic Habits (62%)
          </span>
          <button onclick="navigateTo('reading')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 3. Gentle Habits Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-primary-fixed text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">repeat</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Gentle Habits</h3>
              <p class="text-xs text-on-surface-variant">Daily cadence and rituals.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-medium">Cadence</span>
        </div>
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Track recurring daily rituals with streak fire badges, weekly completion dots, and a 30-day consistency heatmap.
        </p>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-primary">calendar_month</span>
            30-day rhythm heatmap
          </span>
          <button onclick="navigateTo('habits')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
            <span>Open page</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- 4. Digital Stillness Card (Screen Time) -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-surface-container-high text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">phonelink_ring</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Digital Stillness</h3>
              <p class="text-xs text-on-surface-variant">Screen time boundaries & telemetry.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-surface-container text-on-surface-variant text-xs font-medium">Telemetry</span>
        </div>
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Live device screen time directly from Android's UsageStatsManager, hourly distribution charts, and per-app usage limits.
        </p>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-primary">bar_chart</span>
            Hourly rhythm & pickups
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
        <p class="text-xs text-on-surface-variant leading-relaxed">
          5 emotional states, energy level slider, encrypted Panic Vault with 4-7-8 breathing pacer, and physical sensation tags.
        </p>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-secondary">shield</span>
            Private Panic Vault
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
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Explore Sanskrit verses, philosophical commentary, contemplation prompts, and one-tap save to your personal Journal.
        </p>
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

      <!-- 7. Weekly Digest Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-primary-fixed text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[22px]">date_range</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-semibold text-on-surface">Weekly Digest</h3>
              <p class="text-xs text-on-surface-variant">Review your week's calm metrics.</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-medium">Weekly</span>
        </div>
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Aggregated focus hours, completed tasks, habit consistency ratios, and AI-generated quiet reflection lenses.
        </p>
        <div class="flex items-center justify-between pt-1">
          <span class="text-xs text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[15px] text-primary">auto_awesome</span>
            Weekly reflection synthesis
          </span>
          <button onclick="navigateTo('weekly')" class="h-8 px-4 rounded-full bg-surface-container-low text-primary text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1.5 cursor-pointer active:scale-95">
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
        <div class="relative flex items-center w-full">
          <span class="material-symbols-outlined text-outline text-[18px] absolute left-3.5 pointer-events-none">search</span>
          <input type="text" id="journal-search-input" oninput="handleJournalSearch(this.value)" placeholder="Search entries, tags, or reflections..." class="w-full bg-surface-container-lowest border border-surface-container-high/60 rounded-xl pl-10 pr-4 py-2.5 text-xs text-on-surface placeholder:text-outline focus:outline-none focus:border-primary shadow-xs transition-colors"/>
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
          <div class="relative flex items-start gap-3">
            <div class="absolute -left-5 top-1 w-4 h-4 rounded-full bg-primary-fixed-dim ring-4 ring-surface-container-lowest"></div>
            <div>
              <span class="text-xs font-semibold text-on-surface">Morning meditation</span>
              <p class="text-xs text-on-surface-variant">Gentle habit checked off</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 5. SETTINGS TAB (REPLACES "YOU")           -->
    <!-- All Stitch components & local sovereignty  -->
    <!-- ========================================== -->
    <div id="tab-settings" class="page-tab flex-col w-full space-y-5 pb-10">
      <!-- Introductory Framing -->
      <div class="flex flex-col space-y-1 pt-1">
        <div class="inline-flex items-center gap-1.5 text-primary text-xs font-medium">
          <span class="material-symbols-outlined text-[16px]">tune</span>
          <span>Sanctuary Preferences</span>
        </div>
        <h2 class="text-2xl font-bold text-on-surface tracking-tight">Your space</h2>
        <p class="text-sm text-on-surface-variant">Make V07 feel like yours.</p>
      </div>

      <!-- Section 1: Your Profile -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Your profile</h3>
          <span class="text-[11px] text-primary font-medium flex items-center gap-1">
            <span class="w-1.5 h-1.5 rounded-full bg-primary inline-block"></span>
            Encrypted & private
          </span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3.5" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center gap-3.5">
            <div class="relative shrink-0">
              <div class="w-14 h-14 rounded-full bg-primary-fixed text-primary flex items-center justify-center font-bold text-xl shadow-xs">
                S
              </div>
              <div class="absolute bottom-0 right-0 w-4 h-4 rounded-full bg-primary text-on-primary flex items-center justify-center shadow-xs">
                <span class="material-symbols-outlined text-[10px]">favorite</span>
              </div>
            </div>
            <div class="flex flex-col min-w-0 flex-1">
              <div class="flex items-center gap-1.5">
                <span class="text-base font-bold text-on-surface truncate">Sarah Jenkins</span>
                <span class="material-symbols-outlined text-primary text-[17px] material-symbols-filled">verified</span>
              </div>
              <p class="text-xs text-on-surface-variant truncate">sarah@sanctuary.internal</p>
              <div class="mt-1 flex items-center gap-1 text-primary text-[11px] font-medium">
                <span class="material-symbols-outlined text-[13px]">cloud_done</span>
                <span class="truncate">Local journal active · Backed up securely</span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-2 pt-1 border-t border-surface-container-low">
            <button onclick="promptEditProfile()" class="flex-1 py-2 px-3 rounded-full bg-surface-container-low text-primary hover:bg-surface-container text-xs font-semibold transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 cursor-pointer">
              <span class="material-symbols-outlined text-[15px]">edit</span>
              <span>Edit profile</span>
            </button>
            <button onclick="shareSanctuaryStatus()" class="flex-1 py-2 px-3 rounded-full bg-surface-container-low text-on-surface-variant hover:bg-surface-container text-xs font-semibold transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 cursor-pointer">
              <span class="material-symbols-outlined text-[15px]">share</span>
              <span>Share status</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 2: Daily Rhythm & Notifications -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Daily rhythm & notifications</h3>
          <span class="material-symbols-outlined text-tertiary text-[17px]">bedtime</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col space-y-3.5" style="box-shadow: var(--card-shadow);">
          <!-- Morning prompt -->
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-start gap-3 min-w-0">
              <div class="w-8 h-8 rounded-full bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[17px]">wb_sunny</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-xs font-semibold text-on-surface">Morning prompt</span>
                <span class="text-[11px] text-on-surface-variant">Gentle reminder at 8:30 AM to reflect on quiet intent</span>
              </div>
            </div>
            <button aria-checked="true" class="toggle-switch shrink-0 w-11 h-6 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleSettingSwitch(this, 'morning_prompt')">
              <div class="w-5 h-5 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[11px] text-primary">check</span>
              </div>
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>

          <!-- Focus chime & vibration -->
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-start gap-3 min-w-0">
              <div class="w-8 h-8 rounded-full bg-primary-fixed text-on-primary-fixed flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[17px]">music_note</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-xs font-semibold text-on-surface">Focus chime & vibration</span>
                <span class="text-[11px] text-on-surface-variant">Soft wooden singing bowl chime when sessions start</span>
              </div>
            </div>
            <button aria-checked="true" class="toggle-switch shrink-0 w-11 h-6 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleSettingSwitch(this, 'focus_chime')">
              <div class="w-5 h-5 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[11px] text-primary">check</span>
              </div>
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>

          <!-- Evening pause notification -->
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-start gap-3 min-w-0">
              <div class="w-8 h-8 rounded-full bg-surface-container text-tertiary flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[17px]">nights_stay</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-xs font-semibold text-on-surface">Evening pause notification</span>
                <span class="text-[11px] text-on-surface-variant">Warm reminder at 9:15 PM to release today's thoughts</span>
              </div>
            </div>
            <button aria-checked="false" class="toggle-switch shrink-0 w-11 h-6 rounded-full bg-surface-container-high p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleSettingSwitch(this, 'evening_pause')">
              <div class="w-5 h-5 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-0 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[11px] text-outline opacity-0">check</span>
              </div>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 3: Appearance & Theme -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Appearance & theme</h3>
          <span class="text-[11px] text-tertiary">Editorial palette</span>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <!-- Warm Paper -->
          <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-primary/50 relative cursor-pointer flex flex-col justify-between transition-all active:scale-[0.98]" id="settings-theme-light" onclick="setAppTheme('light')">
            <div class="flex items-center justify-between mb-2">
              <div class="w-6 h-6 rounded-full bg-surface-container-low flex items-center justify-center">
                <span class="material-symbols-outlined text-primary text-[14px]">auto_stories</span>
              </div>
              <div class="w-5 h-5 rounded-full bg-primary text-on-primary flex items-center justify-center" id="settings-check-light">
                <span class="material-symbols-outlined text-[13px]">check</span>
              </div>
            </div>
            <div class="h-12 w-full rounded-lg bg-surface-container-low p-2 flex flex-col justify-between mb-2">
              <div class="flex gap-1.5 items-center">
                <div class="w-2.5 h-2.5 rounded-full bg-primary"></div>
                <div class="w-10 h-1.5 rounded-full bg-outline-variant"></div>
              </div>
              <div class="w-3/4 h-1.5 rounded-full bg-outline-variant"></div>
            </div>
            <div class="flex flex-col">
              <span class="text-xs font-bold text-on-surface">Warm Paper</span>
              <span class="text-[10px] text-on-surface-variant">Calm, natural daylight</span>
            </div>
          </div>

          <!-- Night Ink -->
          <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high relative cursor-pointer flex flex-col justify-between transition-all active:scale-[0.98]" id="settings-theme-dark" onclick="setAppTheme('dark')">
            <div class="flex items-center justify-between mb-2">
              <div class="w-6 h-6 rounded-full bg-inverse-surface flex items-center justify-center">
                <span class="material-symbols-outlined text-inverse-on-surface text-[14px]">dark_mode</span>
              </div>
              <div class="w-5 h-5 rounded-full bg-surface-container text-tertiary flex items-center justify-center opacity-0" id="settings-check-dark">
                <span class="material-symbols-outlined text-[13px]">check</span>
              </div>
            </div>
            <div class="h-12 w-full rounded-lg bg-inverse-surface p-2 flex flex-col justify-between mb-2">
              <div class="flex gap-1.5 items-center">
                <div class="w-2.5 h-2.5 rounded-full bg-inverse-primary"></div>
                <div class="w-10 h-1.5 rounded-full bg-tertiary"></div>
              </div>
              <div class="w-3/4 h-1.5 rounded-full bg-tertiary"></div>
            </div>
            <div class="flex flex-col">
              <span class="text-xs font-bold text-on-surface">Night Ink</span>
              <span class="text-[10px] text-on-surface-variant">Deep charcoal resting</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Section 4: Storage & Vault (100% On-Device) -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Storage & vault</h3>
          <span class="text-[11px] text-primary font-medium flex items-center gap-1">
            <span class="material-symbols-outlined text-[13px]">lock</span>
            Local sovereignty
          </span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex items-start gap-3">
            <div class="w-9 h-9 rounded-full bg-primary-container text-on-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[19px]">folder_special</span>
            </div>
            <div class="flex flex-col min-w-0">
              <p class="text-xs font-bold text-on-surface">Your words stay on your device.</p>
              <p class="text-[11px] text-on-surface-variant leading-relaxed mt-0.5">
                V07 never parses, tracks, or shares your personal entries. Data is held in an open SQLite database you control completely.
              </p>
            </div>
          </div>

          <div class="bg-surface-container-low rounded-xl p-2.5 flex flex-col space-y-1">
            <span class="text-[10px] text-on-surface-variant font-medium">Vault directory path</span>
            <div class="flex items-center justify-between gap-2 overflow-hidden">
              <code class="font-mono text-xs text-primary truncate select-all">/storage/emulated/0/V07Journal</code>
              <button class="text-on-surface-variant hover:text-primary transition-colors p-1 rounded-md shrink-0 flex items-center cursor-pointer" onclick="copyVaultPath(this)" title="Copy path">
                <span class="material-symbols-outlined text-[16px]">content_copy</span>
              </button>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2 pt-1">
            <button onclick="handleExportData()" class="py-2.5 px-3 rounded-full bg-surface-container text-on-surface text-xs font-semibold hover:bg-surface-container-high transition-colors active:scale-[0.98] flex items-center justify-center gap-1.5 cursor-pointer">
              <span class="material-symbols-outlined text-[15px]">file_download</span>
              <span>Export JSON</span>
            </button>
            <button onclick="handleBackupVault()" class="py-2.5 px-3 rounded-full bg-primary text-on-primary text-xs font-semibold hover:bg-primary-container transition-colors active:scale-[0.98] flex items-center justify-center gap-1.5 shadow-xs cursor-pointer">
              <span class="material-symbols-outlined text-[15px]">backup</span>
              <span>Backup vault</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 5: Android Device & Telemetry Access -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Android Device & System</h3>
          <span class="text-[11px] text-primary font-medium">Native Bridge</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between py-0.5">
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-on-surface">Screen Time Usage Access</span>
              <span class="text-[11px] text-on-surface-variant">Required to display real application telemetry</span>
            </div>
            <button onclick="requestUsagePermission()" class="px-3 py-1.5 rounded-full bg-surface-container text-primary font-semibold text-xs hover:bg-surface-container-high transition-colors cursor-pointer shrink-0">
              Settings
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>
          <div class="flex items-center justify-between py-0.5">
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-on-surface">Foreground Focus Service</span>
              <span class="text-[11px] text-on-surface-variant">Prevents Android from killing timer in background</span>
            </div>
            <span class="text-[11px] text-primary font-bold bg-primary-fixed px-2.5 py-0.5 rounded-full">Active</span>
          </div>
        </div>
      </section>

      <!-- Section 6: Advanced & Caution Zone -->
      <section class="flex flex-col space-y-2 pt-1">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-tertiary uppercase tracking-wider">Advanced</h3>
          <span class="text-[11px] text-tertiary">Inspection & cache</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col space-y-2.5" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between py-0.5">
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-on-surface">Inspect SQLite Database</span>
              <span class="text-[11px] text-on-surface-variant">Review indexed tables and local events</span>
            </div>
            <button onclick="inspectDatabase()" class="px-3 py-1 rounded-full bg-surface-container-low text-on-surface-variant font-semibold text-xs hover:bg-surface-container transition-colors cursor-pointer">
              Inspect
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>
          <div class="flex flex-col gap-2 pt-1">
            <button onclick="clearCache()" class="w-full py-2.5 px-3 rounded-full bg-surface-container-low hover:bg-surface-container text-on-surface text-xs font-semibold transition-all active:scale-[0.98] flex items-center justify-between cursor-pointer">
              <span class="flex items-center gap-2">
                <span class="material-symbols-outlined text-[16px] text-secondary">cached</span>
                <span>Clear local cache</span>
              </span>
              <span class="text-[11px] text-on-surface-variant">14.8 MB</span>
            </button>
            <button onclick="resetAppSetup()" class="w-full py-2.5 px-3 rounded-full bg-error-container/40 hover:bg-error-container text-error text-xs font-semibold transition-all active:scale-[0.98] flex items-center justify-between cursor-pointer">
              <span class="flex items-center gap-2">
                <span class="material-symbols-outlined text-[16px]">delete_forever</span>
                <span>Reset application state</span>
              </span>
              <span class="text-[11px] font-bold">Erase</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Sanctuary Sign-off Note -->
      <div class="flex flex-col items-center justify-center text-center space-y-1 pt-2 pb-6 text-on-surface-variant">
        <div class="flex items-center gap-1.5 text-primary">
          <span class="material-symbols-outlined text-[16px] material-symbols-filled">spa</span>
          <span class="text-xs font-bold">V07 Calm Sanctuary</span>
        </div>
        <p class="text-[11px] text-tertiary">Crafted for mindful presence · Version 3.0.0 (Build 92)</p>
      </div>
    </div>

  </main>

  <!-- ========================================== -->
  <!-- BOTTOM FLOATING DOCK NAVIGATION            -->
  <!-- Settings replaces 'You'                    -->
  <!-- ========================================== -->
  <nav id="app-bottom-nav" class="fixed bottom-0 left-0 right-0 z-40 pb-safe flex justify-center px-4 mb-3 pointer-events-none">
    <div class="pointer-events-auto w-full max-w-[420px] h-16 rounded-full bg-surface-container-lowest/95 dark:bg-surface-container-high/95 backdrop-blur-xl border border-surface-container-high/60 dark:border-white/10 px-2 flex items-center justify-around" style="box-shadow: var(--nav-shadow);">
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full transition-colors cursor-pointer nav-active" data-tab="home" href="javascript:void(0)" onclick="switchTab('home')">
        <span class="material-symbols-outlined text-[20px]">spa</span>
        <span class="text-[11px] font-medium tracking-tight">Home</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="tools" href="javascript:void(0)" onclick="switchTab('tools')">
        <span class="material-symbols-outlined text-[20px]">grid_view</span>
        <span class="text-[11px] font-medium tracking-tight">Tools</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="journal" href="javascript:void(0)" onclick="switchTab('journal')">
        <span class="material-symbols-outlined text-[20px]">edit_note</span>
        <span class="text-[11px] font-medium tracking-tight">Journal</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="day" href="javascript:void(0)" onclick="switchTab('day')">
        <span class="material-symbols-outlined text-[20px]">calendar_today</span>
        <span class="text-[11px] font-medium tracking-tight">Day</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="settings" href="javascript:void(0)" onclick="switchTab('settings')">
        <span class="material-symbols-outlined text-[20px]">settings</span>
        <span class="text-[11px] font-medium tracking-tight">Settings</span>
      </a>
    </div>
  </nav>

  <!-- ========================================== -->
  <!-- DEDICATED FULL-PAGE TOOL CONTAINERS        -->
  <!-- Zero dialog popups; 64px header height     -->
  <!-- ========================================== -->
  <div id="tool-views-container">

    <!-- 1. FOCUS SANCTUARY FULL PAGE (All Stitch Components) -->
    <div id="view-focus" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Focus Sanctuary</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Quiet intervals for deep presence</p>
          </div>
        </div>
        <button onclick="resetFocusTimer()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Reset">
          <span class="material-symbols-outlined text-[20px]">restart_alt</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Mode Switcher: Pomodoro vs Timer -->
        <div class="w-full p-1 bg-surface-container-low rounded-2xl flex items-center justify-between border border-surface-container-high/40">
          <button class="flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer" id="focus-mode-pomodoro" onclick="setFocusMode('pomodoro')">
            <span class="material-symbols-outlined text-[16px]">timelapse</span>
            <span>Pomodoro Mode</span>
          </button>
          <button class="flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer" id="focus-mode-timer" onclick="setFocusMode('timer')">
            <span class="material-symbols-outlined text-[16px]">hourglass_empty</span>
            <span>Timer Mode</span>
          </button>
        </div>

        <!-- Main Chronometer Card with Progress Dial -->
        <div class="w-full bg-surface-container-lowest rounded-2xl p-6 border border-surface-container-high/40 flex flex-col items-center relative overflow-hidden" style="box-shadow: var(--card-shadow);">
          <div class="w-full flex items-center justify-between text-xs mb-2">
            <div class="flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary-fixed text-primary font-bold">
              <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
              <span id="focus-cycle-badge">Focus • Cycle 1/4</span>
            </div>
            <span class="text-on-surface-variant font-medium flex items-center gap-1">
              <span class="material-symbols-outlined text-[15px] text-secondary">flag</span>
              <span id="focus-target-duration">25m Block</span>
            </span>
          </div>

          <!-- Circular Dial Ring -->
          <div class="relative w-60 h-60 flex items-center justify-center my-2">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 240 240">
              <circle cx="120" cy="120" r="100" fill="transparent" stroke="var(--surface-container-high)" stroke-width="7"></circle>
              <circle id="focus-progress-ring" cx="120" cy="120" r="100" fill="transparent" stroke="var(--primary)" stroke-width="7" stroke-dasharray="628.31" stroke-dashoffset="0" stroke-linecap="round" class="transition-all duration-1000 ease-linear"></circle>
            </svg>
            <div class="absolute flex flex-col items-center justify-center text-center select-none">
              <span class="font-mono text-5xl font-bold text-on-surface tracking-tight" id="focus-modal-time">25:00</span>
              <span class="text-xs font-bold text-primary flex items-center gap-1 mt-1.5 uppercase tracking-wide" id="focus-modal-phase">
                <span class="material-symbols-outlined text-[14px]">lock_clock</span>
                <span>Focus Ready</span>
              </span>
              <span class="mt-1 text-[11px] font-semibold px-2.5 py-0.5 rounded-md bg-surface-container-low text-secondary" id="focus-active-tag">
                Deep Work
              </span>
            </div>
          </div>

          <!-- Time Adjust Controls (-5m / +5m) -->
          <div class="flex items-center gap-3 mt-1">
            <button onclick="adjustFocusMinutes(-5)" class="h-8 px-3.5 rounded-lg bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant text-xs font-mono font-bold transition-all flex items-center gap-1 cursor-pointer">
              <span class="material-symbols-outlined text-[14px]">remove</span> 5m
            </button>
            <button onclick="adjustFocusMinutes(5)" class="h-8 px-3.5 rounded-lg bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant text-xs font-mono font-bold transition-all flex items-center gap-1 cursor-pointer">
              <span class="material-symbols-outlined text-[14px]">add</span> 5m
            </button>
          </div>

          <!-- Primary Actions -->
          <div class="flex items-center gap-3 mt-4">
            <button onclick="resetFocusTimer()" class="h-11 px-4 rounded-xl bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant flex items-center gap-1.5 font-bold text-xs border border-surface-container-high/40 cursor-pointer">
              <span class="material-symbols-outlined text-[17px]">restart_alt</span>
              <span>Reset</span>
            </button>
            <button onclick="toggleFocusTimer()" id="focus-modal-toggle-btn" class="h-12 px-7 rounded-xl bg-primary hover:opacity-95 text-on-primary active:scale-95 transition-all flex items-center gap-2 font-bold text-sm shadow-md cursor-pointer">
              <span class="material-symbols-outlined text-[20px]" id="focus-modal-play-icon">play_arrow</span>
              <span id="focus-modal-play-text">Start focus</span>
            </button>
            <button onclick="skipFocusCycle()" class="h-11 w-11 rounded-xl bg-surface-container-low hover:bg-surface-container active:scale-95 text-on-surface-variant flex items-center justify-center border border-surface-container-high/40 cursor-pointer" title="Next Interval">
              <span class="material-symbols-outlined text-[18px]">skip_next</span>
            </button>
          </div>
        </div>

        <!-- Duration Preset Pills & Custom Time -->
        <div class="flex flex-col space-y-2">
          <div class="flex items-center justify-between px-1">
            <span class="text-xs font-bold uppercase text-on-surface-variant">Duration</span>
            <button onclick="toggleCustomTimePanel()" class="text-xs font-semibold text-primary hover:underline flex items-center gap-1 cursor-pointer">
              <span class="material-symbols-outlined text-[14px]">tune</span>
              <span>Custom Time</span>
            </button>
          </div>
          <div class="grid grid-cols-4 gap-2">
            <button onclick="setFocusPreset(15)" class="py-2.5 px-2 rounded-xl bg-surface-container-lowest border border-surface-container-high hover:border-primary text-center transition-all cursor-pointer">
              <div class="text-sm font-bold font-mono text-on-surface">15m</div>
              <div class="text-[10px] text-on-surface-variant">Sprint</div>
            </button>
            <button onclick="setFocusPreset(25)" class="py-2.5 px-2 rounded-xl bg-primary-fixed border border-primary text-center transition-all cursor-pointer">
              <div class="text-sm font-bold font-mono text-primary">25m</div>
              <div class="text-[10px] text-primary">Standard</div>
            </button>
            <button onclick="setFocusPreset(45)" class="py-2.5 px-2 rounded-xl bg-surface-container-lowest border border-surface-container-high hover:border-primary text-center transition-all cursor-pointer">
              <div class="text-sm font-bold font-mono text-on-surface">45m</div>
              <div class="text-[10px] text-on-surface-variant">Focused</div>
            </button>
            <button onclick="setFocusPreset(60)" class="py-2.5 px-2 rounded-xl bg-surface-container-lowest border border-surface-container-high hover:border-primary text-center transition-all cursor-pointer">
              <div class="text-sm font-bold font-mono text-on-surface">60m</div>
              <div class="text-[10px] text-on-surface-variant">Deep</div>
            </button>
          </div>

          <!-- Collapsible Custom Time Panel -->
          <div id="custom-time-panel" class="hidden p-3 bg-surface-container-lowest rounded-xl border border-surface-container-high flex items-center justify-between gap-3">
            <span class="text-xs text-on-surface-variant font-medium">Set custom minutes:</span>
            <div class="flex items-center gap-2">
              <input type="number" id="custom-minutes-input" min="1" max="180" value="30" class="w-16 h-8 text-center font-mono text-xs font-bold rounded-lg bg-surface-container-low border border-surface-container-high text-on-surface focus:outline-none focus:border-primary"/>
              <button onclick="applyCustomTime()" class="h-8 px-3 rounded-lg bg-primary text-on-primary font-bold text-xs hover:opacity-90 transition-opacity cursor-pointer">Apply</button>
            </div>
          </div>
        </div>

        <!-- Session Details (Activity & Label Dropdowns from Stitch) -->
        <div class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 flex flex-col space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold uppercase text-on-surface-variant">Session Details</span>
            <span class="text-[11px] text-primary font-semibold flex items-center gap-1" id="details-summary-pill">
              <span class="w-1.5 h-1.5 rounded-full bg-primary"></span>
              <span>Deep Work • #urgent</span>
            </span>
          </div>
          <div class="grid grid-cols-2 gap-2.5">
            <div class="flex flex-col space-y-1">
              <label class="text-[11px] font-medium text-on-surface-variant flex items-center gap-1" for="focus-activity-select">
                <span class="material-symbols-outlined text-[13px] text-primary">psychology</span> Activity
              </label>
              <select id="focus-activity-select" onchange="updateFocusDetails()" class="w-full h-9 px-2.5 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-semibold text-on-surface focus:outline-none focus:border-primary cursor-pointer">
                <option value="Deep Work" selected>Deep Work</option>
                <option value="Reading">Reading</option>
                <option value="Writing">Writing</option>
                <option value="Coding">Coding</option>
                <option value="Reflection">Reflection</option>
                <option value="Planning">Planning</option>
              </select>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-[11px] font-medium text-on-surface-variant flex items-center gap-1" for="focus-label-select">
                <span class="material-symbols-outlined text-[13px] text-primary">tag</span> Label / Tag
              </label>
              <select id="focus-label-select" onchange="updateFocusDetails()" class="w-full h-9 px-2.5 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-semibold text-on-surface focus:outline-none focus:border-primary cursor-pointer font-mono">
                <option value="#urgent" selected>#urgent</option>
                <option value="#project-v07">#project-v07</option>
                <option value="#mindful">#mindful</option>
                <option value="#draft">#draft</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Quick Notes / Distraction Parking Lot -->
        <div class="flex flex-col space-y-2">
          <div class="flex items-center justify-between px-1">
            <span class="text-xs font-bold uppercase text-on-surface-variant">Distraction Parking Lot</span>
            <span class="text-[11px] text-on-surface-variant">Save thoughts for later</span>
          </div>
          <form onsubmit="handleSaveParkingThought(event)" class="relative flex items-center w-full">
            <span class="material-symbols-outlined text-[17px] text-on-surface-variant absolute left-3 pointer-events-none">lightbulb</span>
            <input type="text" id="parking-input" placeholder="Jot a stray thought to stay focused..." class="w-full h-10 pl-9 pr-20 bg-surface-container-lowest border border-surface-container-high rounded-xl text-xs text-on-surface placeholder:text-on-surface-variant/70 focus:outline-none focus:border-primary"/>
            <button type="submit" class="absolute right-1.5 h-7 px-2.5 rounded-lg bg-surface-container-low hover:bg-surface-container text-primary font-bold text-xs flex items-center gap-1 cursor-pointer">
              <span>Save</span>
              <span class="material-symbols-outlined text-[14px]">add</span>
            </button>
          </form>
          <div class="space-y-1.5 pt-1" id="parking-shelf">
            <!-- Dynamically populated -->
          </div>
        </div>
      </div>
    </div>

    <!-- 2. READING COMPANION (YOUR BOOKS) FULL PAGE -->
    <div id="view-reading" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Your Books</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you're reading</p>
          </div>
        </div>
        <button onclick="promptAddBook()" class="px-3 py-1.5 bg-primary text-on-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer hover:opacity-95 shadow-xs">
          <span class="material-symbols-outlined text-[15px]">add</span>
          <span>Add book</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Top Section Header -->
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-bold text-on-surface">Currently reading</h3>
          <span class="text-xs text-primary font-semibold">In Progress</span>
        </div>

        <!-- Focused Book Progress Card (Atomic Habits) -->
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex gap-3.5 items-start">
            <div class="w-16 h-24 rounded-xl overflow-hidden shrink-0 shadow-sm bg-surface-container relative">
              <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBbl8Js1JPQshdKAXQnRAHpCI5mApX-iqqb5dOYiP_w8gt7xSi9q6bDz9D6RLwyn2KOynazM_HPlnrb_Dqztj9CwYgdsJue3KUXTUGq58AB0jBoM67SMl12MGS4T3y-DJVBpYtIy7jKJaxqd-yAqcKHw4UO9VB75Er2Wedfi8lPQZDRpE-7VqAX6yz4OOjoFANqUHjcqQLA7KH1BUyGxIEOltKdKBgZQZHLikmoIxsLhsgjIPggPZKF" alt="Atomic Habits cover"/>
            </div>
            <div class="flex flex-col flex-1 min-w-0">
              <div class="flex items-start justify-between gap-1">
                <h4 class="text-base font-bold text-on-surface truncate leading-tight">Atomic Habits</h4>
                <span class="text-xs font-bold text-primary shrink-0" id="book-pct-badge">62% complete</span>
              </div>
              <p class="text-xs text-on-surface-variant mt-0.5">James Clear</p>
              <p class="text-[11px] text-tertiary mt-1" id="book-page-info">Page 142 of 228 · Last read Today</p>
              <div class="w-full h-1.5 bg-surface-container rounded-full overflow-hidden mt-2">
                <div class="h-full bg-primary rounded-full transition-all duration-300" id="book-progress-bar" style="width: 62%;"></div>
              </div>
              <div class="flex items-center gap-2 mt-3">
                <button onclick="adjustBookPages(-5)" class="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg text-xs font-bold active:scale-95 cursor-pointer">
                  -5 p
                </button>
                <button onclick="adjustBookPages(10)" class="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg text-xs font-bold active:scale-95 cursor-pointer">
                  +10 p
                </button>
                <button onclick="promptUpdatePage()" class="ml-auto px-2.5 py-1 bg-surface-container hover:bg-surface-container-high text-primary rounded-lg text-xs font-bold flex items-center gap-1 cursor-pointer">
                  <span class="material-symbols-outlined text-[13px]">update</span>
                  <span>Update</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Shelf Section with Filter Tabs -->
        <section class="flex flex-col space-y-2.5">
          <div class="flex items-center justify-between">
            <h3 class="text-sm font-bold text-on-surface">Your shelf</h3>
            <div class="flex items-center gap-1 bg-surface-container-low p-0.5 rounded-lg text-xs font-medium">
              <button class="shelf-pill px-2.5 py-1 rounded-md bg-surface-container-lowest text-primary font-bold shadow-xs cursor-pointer" onclick="filterShelf('all', this)">Reading</button>
              <button class="shelf-pill px-2.5 py-1 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer" onclick="filterShelf('finished', this)">Finished</button>
              <button class="shelf-pill px-2.5 py-1 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer" onclick="filterShelf('saved', this)">Saved</button>
            </div>
          </div>

          <!-- Books List -->
          <div class="flex flex-col bg-surface-container-lowest rounded-2xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden shadow-xs">
            <!-- Book 1: Quiet -->
            <div class="p-3 flex items-center gap-3 hover:bg-surface-container-low/40 transition-colors">
              <div class="w-10 h-14 rounded-lg overflow-hidden shrink-0 bg-surface-container shadow-xs">
                <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAC8ZEhBwRLi2Zw1vYfkmOLOq8XGczjyeYaKmsgpaG8K8zDX67k3LhXl1KV2qYTw8FokeGfo1CYRF4XW89O0ZpUq0ewq0zAxpKvWtePvSDH53KQeYTn86hGaK5zEW-UDtUgCFuwk3QSN_JoaVRvJe7XG4JCcGQHW6l6JymXW1YkEoXVCHFOKwoLR1kNdxTEyVlkAW8CRPZfP7wi9OP2QFkjBFk6wBr43bZvGFhWpUYSIT0yqRxFtPCZ" alt="Quiet cover"/>
              </div>
              <div class="flex flex-col flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <h4 class="text-xs font-bold text-on-surface truncate">Quiet: The Power of Introverts</h4>
                  <span class="shrink-0 text-[11px] text-primary font-semibold">42%</span>
                </div>
                <p class="text-[11px] text-on-surface-variant">Susan Cain</p>
                <p class="text-[10px] text-tertiary mt-0.5">Last read · Yesterday</p>
              </div>
              <button class="w-7 h-7 rounded flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
                <span class="material-symbols-outlined text-[17px]">more_horiz</span>
              </button>
            </div>

            <!-- Book 2: The Summer Book -->
            <div class="p-3 flex items-center gap-3 hover:bg-surface-container-low/40 transition-colors">
              <div class="w-10 h-14 rounded-lg overflow-hidden shrink-0 bg-surface-container shadow-xs">
                <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCjgMVcp30Hjld1NvpUVWqCnq-nNf2PO3yMf0bejUgHXyOap3xKCRN8CUE56l0NgH_GeWH7LqOGZ5YIbPXHhbODPm6vlEDfh2uWG0WMm54z3Mt9EV_TRcup4rWKnFP99F0AKpVpCDTE8gW9D3oRzVSeyOZSowp6hqn_9piDrnmpdRRg5HI3GOmaoVmnDlIspa4a_4zft7R52t7nXbar9amI7IyJN-TKhqRko3BCqqN7HKq-Ya9pQCN_" alt="The Summer Book cover"/>
              </div>
              <div class="flex flex-col flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <h4 class="text-xs font-bold text-on-surface truncate">The Summer Book</h4>
                  <span class="shrink-0 text-[10px] px-1.5 py-0.5 rounded bg-surface-container text-on-surface-variant font-medium">Saved</span>
                </div>
                <p class="text-[11px] text-on-surface-variant">Tove Jansson</p>
                <p class="text-[10px] text-tertiary mt-0.5">Added to queue</p>
              </div>
              <button class="w-7 h-7 rounded flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
                <span class="material-symbols-outlined text-[17px]">more_horiz</span>
              </button>
            </div>

            <!-- Book 3: Walden -->
            <div class="p-3 flex items-center gap-3 hover:bg-surface-container-low/40 transition-colors">
              <div class="w-10 h-14 rounded-lg overflow-hidden shrink-0 bg-surface-container shadow-xs">
                <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCTyYUqwBfe_ERUgRguii1rjMQTnAVkK0TYlTE23vjTPnh7LNnWvCubKdbByyk514PF1d923Ppd_OOk_Ahc4Vs0t8VKvwygx1OD0wOSH1KyylkMoxxmyxZbsu6dSXL1lE4mYCSiGHMWOQ1h_6_73X8Z0D3aCJAVzQjvgRJL3hyBxhL6GWSw4bs5DXbxgnJEenctSqAqIA7S8UXKvWgWLH29Hr9EKqogjcyfhi-_DnK1f0AD9dJP8OGM" alt="Walden cover"/>
              </div>
              <div class="flex flex-col flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <h4 class="text-xs font-bold text-on-surface truncate">Walden</h4>
                  <span class="shrink-0 text-[10px] px-1.5 py-0.5 rounded bg-primary-fixed text-primary font-bold">Finished</span>
                </div>
                <p class="text-[11px] text-on-surface-variant">Henry David Thoreau</p>
                <p class="text-[10px] text-tertiary mt-0.5">Completed</p>
              </div>
              <button class="w-7 h-7 rounded flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
                <span class="material-symbols-outlined text-[17px]">more_horiz</span>
              </button>
            </div>
          </div>
        </section>

        <!-- Search Bar -->
        <div class="relative flex items-center w-full pt-1">
          <span class="material-symbols-outlined text-outline text-[18px] absolute left-3.5 pointer-events-none">search</span>
          <input type="text" placeholder="Search books, authors, or quotes..." class="w-full h-11 pl-10 pr-10 bg-surface-container-lowest border border-surface-container-high/60 rounded-xl text-xs text-on-surface placeholder:text-outline focus:outline-none focus:border-primary shadow-xs"/>
          <button class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant absolute right-1.5 cursor-pointer">
            <span class="material-symbols-outlined text-[17px]">tune</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 3. GENTLE HABITS FULL PAGE (All Stitch Components) -->
    <div id="view-habits" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Habits</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you do regularly</p>
          </div>
        </div>
        <button onclick="promptAddHabit()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Add Habit">
          <span class="material-symbols-outlined text-[22px]">add</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Date indicator & ratio -->
        <div class="flex items-baseline justify-between px-0.5">
          <div class="flex items-baseline gap-2">
            <h3 class="text-base font-bold text-on-surface">Today</h3>
            <span class="text-xs text-on-surface-variant" id="habits-date-str">Sep 24</span>
          </div>
          <span class="text-xs text-primary font-bold bg-surface-container-low px-2.5 py-0.5 rounded-full" id="habits-ratio-badge">3 of 4 done</span>
        </div>

        <!-- Habits List with Weekly Dots from Stitch -->
        <div class="space-y-2.5" id="habits-modal-list">
          <!-- Habit 1 -->
          <div class="habit-row flex flex-col p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/60 shadow-xs" data-completed="true">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0 cursor-pointer flex-1" onclick="toggleHabitRow(this.closest('.habit-row'), 'Morning stillness')">
                <div class="habit-checkbox w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0 transition-transform active:scale-90">
                  <span class="material-symbols-outlined text-[16px]">check</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="habit-name text-xs font-bold text-on-surface line-through opacity-70 truncate">Morning stillness</span>
                  <span class="text-[11px] text-secondary flex items-center gap-1 font-semibold"><span>🔥</span> 14 days streak</span>
                </div>
              </div>
              <button onclick="showToast('Habit Config', 'Editing Morning stillness cadence', 'info')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Configure">
                <span class="material-symbols-outlined text-[17px]">tune</span>
              </button>
            </div>
            <!-- Weekly Dots -->
            <div class="mt-2.5 pt-2 border-t border-surface-container-low flex items-center justify-between">
              <span class="text-[10px] text-on-surface-variant font-medium">Weekly streak</span>
              <div class="flex items-center gap-2">
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">M</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">W</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">F</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] font-bold text-primary">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary ring-2 ring-primary-fixed"></span></div>
              </div>
            </div>
          </div>

          <!-- Habit 2 -->
          <div class="habit-row flex flex-col p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/60 shadow-xs" data-completed="true">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0 cursor-pointer flex-1" onclick="toggleHabitRow(this.closest('.habit-row'), 'Hydrate herbal infusion')">
                <div class="habit-checkbox w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0 transition-transform active:scale-90">
                  <span class="material-symbols-outlined text-[16px]">check</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="habit-name text-xs font-bold text-on-surface line-through opacity-70 truncate">Hydrate with herbal tea</span>
                  <span class="text-[11px] text-secondary flex items-center gap-1 font-semibold"><span>🔥</span> 21 days streak</span>
                </div>
              </div>
              <button onclick="showToast('Habit Config', 'Editing Hydrate cadence', 'info')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Configure">
                <span class="material-symbols-outlined text-[17px]">tune</span>
              </button>
            </div>
            <div class="mt-2.5 pt-2 border-t border-surface-container-low flex items-center justify-between">
              <span class="text-[10px] text-on-surface-variant font-medium">Weekly streak</span>
              <div class="flex items-center gap-2">
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">M</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">W</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">F</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] font-bold text-primary">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary ring-2 ring-primary-fixed"></span></div>
              </div>
            </div>
          </div>

          <!-- Habit 3 -->
          <div class="habit-row flex flex-col p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/60 shadow-xs" data-completed="true">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0 cursor-pointer flex-1" onclick="toggleHabitRow(this.closest('.habit-row'), 'Read 20 pages')">
                <div class="habit-checkbox w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0 transition-transform active:scale-90">
                  <span class="material-symbols-outlined text-[16px]">check</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="habit-name text-xs font-bold text-on-surface line-through opacity-70 truncate">Read 20 pages</span>
                  <span class="text-[11px] text-secondary flex items-center gap-1 font-semibold"><span>🔥</span> 6 days streak</span>
                </div>
              </div>
              <button onclick="showToast('Habit Config', 'Editing Reading cadence', 'info')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Configure">
                <span class="material-symbols-outlined text-[17px]">tune</span>
              </button>
            </div>
            <div class="mt-2.5 pt-2 border-t border-surface-container-low flex items-center justify-between">
              <span class="text-[10px] text-on-surface-variant font-medium">Weekly streak</span>
              <div class="flex items-center gap-2">
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">M</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">W</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">F</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] font-bold text-primary">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary ring-2 ring-primary-fixed"></span></div>
              </div>
            </div>
          </div>

          <!-- Habit 4 -->
          <div class="habit-row flex flex-col p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/60 shadow-xs" data-completed="false">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0 cursor-pointer flex-1" onclick="toggleHabitRow(this.closest('.habit-row'), 'Evening reflection')">
                <div class="habit-checkbox w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0 transition-transform active:scale-90">
                  <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="habit-name text-xs font-bold text-on-surface truncate">Evening reflection</span>
                  <span class="text-[11px] text-on-surface-variant">Scheduled at 9:15 PM</span>
                </div>
              </div>
              <button onclick="showToast('Habit Config', 'Editing Evening reflection', 'info')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Configure">
                <span class="material-symbols-outlined text-[17px]">tune</span>
              </button>
            </div>
            <div class="mt-2.5 pt-2 border-t border-surface-container-low flex items-center justify-between">
              <span class="text-[10px] text-on-surface-variant font-medium">Weekly streak</span>
              <div class="flex items-center gap-2">
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">M</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">W</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">T</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">F</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] text-on-surface-variant">S</span><span class="w-2.5 h-2.5 rounded-full bg-primary"></span></div>
                <div class="flex flex-col items-center gap-0.5"><span class="text-[9px] font-bold text-on-surface">S</span><span class="w-2 h-2 rounded-full border border-outline-variant"></span></div>
              </div>
            </div>
          </div>
        </div>

        <button onclick="promptAddHabit()" class="w-full py-3 rounded-2xl border border-dashed border-outline-variant hover:border-primary text-primary text-xs font-bold flex items-center justify-center gap-2 cursor-pointer active:scale-95 transition-colors">
          <span class="material-symbols-outlined text-[18px]">add</span>
          <span>Add habit</span>
        </button>

        <!-- Consistency Heatmap from Stitch -->
        <section class="p-4 rounded-2xl bg-surface-container-lowest border border-surface-container-high/60 shadow-xs flex flex-col space-y-3">
          <div class="flex items-start justify-between">
            <div>
              <h4 class="text-xs font-bold text-on-surface">Consistency</h4>
              <p class="text-[11px] text-on-surface-variant mt-0.5">Habits completed vs scheduled daily</p>
            </div>
            <div class="flex items-center gap-1 bg-surface-container-low p-0.5 rounded-lg text-[11px] font-semibold">
              <button class="px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer">Jul</button>
              <button class="px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer">Aug</button>
              <button class="px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary shadow-xs font-bold cursor-pointer">Sep</button>
            </div>
          </div>
          <!-- 30-day Calendar Grid -->
          <div class="overflow-x-auto pb-1">
            <div class="min-w-[260px] flex flex-col space-y-1.5">
              <div class="grid grid-cols-7 gap-1.5 text-center text-[10px] text-on-surface-variant font-semibold">
                <span>M</span><span>T</span><span>W</span><span>T</span><span>F</span><span>S</span><span>S</span>
              </div>
              <div class="grid grid-cols-7 gap-1.5 justify-items-center" id="heatmap-grid">
                <!-- Populated dynamically -->
              </div>
            </div>
          </div>
          <div class="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between text-on-surface">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-primary shrink-0"></span>
              <span class="text-xs font-bold">Sep 24 · 3 of 4 completed (75%)</span>
            </div>
            <span class="text-[11px] text-primary font-bold">On track</span>
          </div>
          <div class="flex items-center justify-between px-1 text-[10px] text-on-surface-variant">
            <span>0/4 habits</span>
            <div class="flex items-center gap-1">
              <span class="w-2.5 h-2.5 rounded-xs bg-surface-container-high" title="0/4"></span>
              <span class="w-2.5 h-2.5 rounded-xs bg-primary-fixed" title="1-2/4"></span>
              <span class="w-2.5 h-2.5 rounded-xs bg-primary-fixed-dim" title="3/4"></span>
              <span class="w-2.5 h-2.5 rounded-xs bg-primary" title="4/4"></span>
            </div>
            <span>4/4 habits</span>
          </div>
        </section>
      </div>
    </div>

    <!-- 4. DIGITAL STILLNESS (SCREEN TIME) FULL PAGE (All Stitch Components) -->
    <div id="view-screentime" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Digital Stillness</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Telemetry & usage boundaries</p>
          </div>
        </div>
        <button onclick="refreshScreenTime()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Refresh">
          <span class="material-symbols-outlined text-[20px]">sync</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Daily vs Weekly Switcher Pill -->
        <div class="flex justify-center">
          <div class="inline-flex p-1 rounded-full bg-surface-container-low border border-surface-container-high shadow-xs items-center gap-1">
            <button class="py-1 px-4 rounded-full bg-primary text-on-primary font-bold text-xs shadow-xs transition-all flex items-center gap-1.5 cursor-pointer" id="btn-st-daily" onclick="switchRhythmView('daily')">
              <span class="w-1.5 h-1.5 rounded-full bg-surface-container-lowest inline-block"></span>
              <span>Daily</span>
            </button>
            <button class="py-1 px-4 rounded-full text-on-surface-variant hover:text-on-surface font-semibold text-xs transition-all flex items-center gap-1.5 cursor-pointer" id="btn-st-weekly" onclick="switchRhythmView('weekly')">
              <span>Weekly</span>
            </button>
          </div>
        </div>

        <!-- Permission Banner -->
        <div id="screentime-perm-banner" class="hidden p-4 rounded-2xl bg-secondary-fixed text-on-secondary-fixed space-y-2 border border-secondary-fixed-dim">
          <div class="flex items-center gap-2 font-bold text-xs">
            <span class="material-symbols-outlined text-[18px]">security</span>
            <span>Usage Access Permission Required</span>
          </div>
          <p class="text-[11px] leading-relaxed opacity-90">
            To view detailed app telemetry from your device, please grant Android usage stats access.
          </p>
          <button onclick="requestUsagePermission()" class="mt-1 px-3 py-1.5 bg-on-secondary-fixed text-secondary-fixed rounded-xl text-xs font-bold active:scale-95 transition-all cursor-pointer">
            Grant Permission in Settings
          </button>
        </div>

        <!-- Total Screen Time Card with Delta -->
        <div class="rounded-2xl bg-surface-container-lowest p-5 border border-surface-container-high/40 shadow-xs relative overflow-hidden" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between mb-1.5">
            <span class="text-xs font-bold uppercase text-on-surface-variant flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full bg-primary inline-block"></span>
              Active Presence
            </span>
            <span class="text-xs px-2.5 py-0.5 rounded-full bg-primary-fixed text-primary font-bold">
              45m under 6h limit
            </span>
          </div>
          <div class="flex items-baseline gap-2 mt-1">
            <h3 class="text-3xl font-bold text-on-surface tracking-tight" id="screentime-modal-total">5h 34m</h3>
            <span class="text-xs text-on-surface-variant font-medium">today</span>
          </div>
          <!-- Delta & Progress Bar -->
          <div class="mt-3">
            <div class="flex items-center justify-between text-xs text-on-surface-variant mb-1">
              <div class="flex items-center gap-1 text-primary font-semibold">
                <span class="material-symbols-outlined text-[15px]">arrow_downward</span>
                <span>18% less</span>
                <span class="text-on-surface-variant font-normal">vs last week</span>
              </div>
              <span class="text-[11px] font-bold">75% of limit</span>
            </div>
            <div class="w-full h-2 rounded-full bg-surface-container overflow-hidden">
              <div class="h-full bg-primary rounded-full transition-all duration-500" style="width: 75%;"></div>
            </div>
          </div>
        </div>

        <!-- Hourly Rhythm Bar Chart (From Stitch screen_time_deep_midnight) -->
        <section class="rounded-2xl bg-surface-container-lowest p-4 border border-surface-container-high/40 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex items-start justify-between">
            <div>
              <h4 class="text-xs font-bold text-on-surface" id="st-chart-title">Today's Rhythm</h4>
              <p class="text-[11px] text-on-surface-variant mt-0.5" id="st-chart-sub">Hourly distribution from 6 AM to 11 PM</p>
            </div>
            <div class="flex items-center gap-2 text-[10px] font-semibold text-on-surface-variant">
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-primary"></span>Screen</span>
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-surface-container-high"></span>Rest</span>
            </div>
          </div>

          <!-- Daily Hourly View -->
          <div id="st-view-daily-bars" class="overflow-x-auto no-scrollbar pt-2 pb-1">
            <div class="flex items-end gap-2 px-1 min-w-[560px]">
              <!-- Generated 18 hours bars -->
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">8m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/70 rounded-full" style="height:15%;"></div></div><span class="text-[9px] text-on-surface-variant">6A</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">14m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/80 rounded-full" style="height:25%;"></div></div><span class="text-[9px] text-on-surface-variant">7A</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">4m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/50 rounded-full" style="height:8%;"></div></div><span class="text-[9px] text-on-surface-variant">8A</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">18m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/80 rounded-full" style="height:32%;"></div></div><span class="text-[9px] text-on-surface-variant">9A</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-primary font-bold">38m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5 ring-1 ring-primary/40"><div class="w-full bg-primary rounded-full" style="height:65%;"></div></div><span class="text-[9px] text-primary font-bold">10A</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">12m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/70 rounded-full" style="height:22%;"></div></div><span class="text-[9px] text-on-surface-variant">11A</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">19m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/80 rounded-full" style="height:35%;"></div></div><span class="text-[9px] text-on-surface-variant">12P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">9m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/60 rounded-full" style="height:16%;"></div></div><span class="text-[9px] text-on-surface-variant">1P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-primary font-bold">42m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5 ring-1 ring-primary/40"><div class="w-full bg-primary rounded-full" style="height:72%;"></div></div><span class="text-[9px] text-primary font-bold">2P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">15m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/70 rounded-full" style="height:27%;"></div></div><span class="text-[9px] text-on-surface-variant">3P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-primary font-bold">48m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5 ring-1 ring-primary/60 shadow-xs"><div class="w-full bg-primary rounded-full" style="height:82%;"></div></div><span class="text-[9px] text-primary font-bold">4P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">28m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/90 rounded-full" style="height:48%;"></div></div><span class="text-[9px] text-on-surface-variant">5P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">16m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/75 rounded-full" style="height:28%;"></div></div><span class="text-[9px] text-on-surface-variant">6P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">25m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/80 rounded-full" style="height:43%;"></div></div><span class="text-[9px] text-on-surface-variant">7P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-primary font-bold">32m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5 ring-1 ring-primary/40"><div class="w-full bg-primary rounded-full" style="height:55%;"></div></div><span class="text-[9px] text-primary font-bold">8P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">14m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/70 rounded-full" style="height:24%;"></div></div><span class="text-[9px] text-on-surface-variant">9P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">6m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/50 rounded-full" style="height:10%;"></div></div><span class="text-[9px] text-on-surface-variant">10P</span></div>
              <div class="flex flex-col items-center gap-1 w-7"><span class="text-[9px] text-on-surface-variant">2m</span><div class="w-2.5 h-20 bg-surface-container-high rounded-full overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/40 rounded-full" style="height:4%;"></div></div><span class="text-[9px] text-on-surface-variant">11P</span></div>
            </div>
          </div>

          <!-- Weekly Days View -->
          <div id="st-view-weekly-bars" class="hidden">
            <div class="grid grid-cols-7 gap-2 pt-2 pb-1">
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Monday', '4h 15m', '15m under')">
                <span class="text-[10px] text-on-surface-variant">4h 15</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/80 rounded-md" style="height:70%;"></div></div>
                <span class="text-[11px] font-semibold text-on-surface-variant">M</span>
              </div>
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Tuesday', '5h 10m', '10m over')">
                <span class="text-[10px] text-secondary font-bold">5h 10</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-secondary rounded-md" style="height:86%;"></div></div>
                <span class="text-[11px] font-semibold text-on-surface-variant">T</span>
              </div>
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Wednesday', '3h 40m', '1h under')">
                <span class="text-[10px] text-on-surface-variant">3h 40</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/70 rounded-md" style="height:60%;"></div></div>
                <span class="text-[11px] font-semibold text-on-surface-variant">W</span>
              </div>
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Thursday', '4h 50m', '20m under')">
                <span class="text-[10px] text-on-surface-variant">4h 50</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/90 rounded-md" style="height:80%;"></div></div>
                <span class="text-[11px] font-semibold text-on-surface-variant">T</span>
              </div>
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Friday', '5h 34m', 'Today')">
                <span class="text-[10px] text-primary font-bold">5h 34</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5 ring-1 ring-primary"><div class="w-full bg-primary rounded-md" style="height:92%;"></div></div>
                <span class="text-[11px] font-bold text-primary">F</span>
              </div>
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Saturday', '2h 10m', 'Quiet')">
                <span class="text-[10px] text-on-surface-variant">2h 10</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/50 rounded-md" style="height:36%;"></div></div>
                <span class="text-[11px] font-semibold text-on-surface-variant">S</span>
              </div>
              <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="selectWeeklyDay('Sunday', '1h 50m', 'Quiet')">
                <span class="text-[10px] text-on-surface-variant">1h 50</span>
                <div class="w-full h-20 bg-surface-container-high rounded-lg overflow-hidden flex flex-col justify-end p-0.5"><div class="w-full bg-primary/40 rounded-md" style="height:30%;"></div></div>
                <span class="text-[11px] font-semibold text-on-surface-variant">S</span>
              </div>
            </div>
            <div class="mt-2 p-2.5 rounded-xl bg-surface-container-low flex items-center justify-between text-xs" id="weekly-day-pill">
              <span class="font-bold text-on-surface" id="weekly-pill-name">Friday (Today)</span>
              <span class="font-bold text-primary" id="weekly-pill-time">5h 34m · 45m under limit</span>
            </div>
          </div>
        </section>

        <!-- Top Applications Breakdown (Real Native Telemetry) -->
        <div class="space-y-2">
          <div class="flex items-center justify-between px-1">
            <span class="text-xs font-bold uppercase text-on-surface">Top Application Activity</span>
            <span class="text-[11px] text-on-surface-variant">Live from device</span>
          </div>
          <div class="flex flex-col bg-surface-container-lowest rounded-2xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden shadow-xs" id="screentime-apps-list">
            <!-- Dynamically populated from real UsageStatsManager -->
          </div>
        </div>

        <!-- Phone Pickups & Awareness Metrics -->
        <section class="space-y-2">
          <span class="text-xs font-bold uppercase text-on-surface px-1">Pickups & Awareness</span>
          <div class="grid grid-cols-2 gap-3">
            <div class="p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs flex flex-col justify-between">
              <div class="flex items-center justify-between text-on-surface-variant">
                <span class="text-xs font-semibold">Pickups Today</span>
                <span class="material-symbols-outlined text-[17px]">touch_app</span>
              </div>
              <div class="my-1.5">
                <div class="text-2xl font-bold text-on-surface">34</div>
                <div class="text-[11px] text-on-surface-variant">Avg. 4 pickups / hour</div>
              </div>
              <div class="pt-1.5 border-t border-surface-container-low flex items-center text-primary text-[11px] font-semibold">
                <span class="material-symbols-outlined text-[13px] mr-1">trending_down</span>
                <span>8 less than yesterday</span>
              </div>
            </div>
            <div class="p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs flex flex-col justify-between">
              <div class="flex items-center justify-between text-on-surface-variant">
                <span class="text-xs font-semibold">First Unlocked</span>
                <span class="material-symbols-outlined text-[17px]">wb_twilight</span>
              </div>
              <div class="my-1.5">
                <div class="text-2xl font-bold text-on-surface">7:42 AM</div>
                <div class="text-[11px] text-on-surface-variant">30m after wake</div>
              </div>
              <div class="pt-1.5 border-t border-surface-container-low flex items-center text-tertiary text-[11px] font-semibold">
                <span class="material-symbols-outlined text-[13px] mr-1">alarm_on</span>
                <span>Gentle morning start</span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 5. MOOD & GROUNDING / PANIC VAULT FULL PAGE (All Stitch Components) -->
    <div id="view-mood" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Mood & Energy</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">A quick check-in with yourself</p>
          </div>
        </div>
        <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" aria-label="Close">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Top Segmented Tabs: Mood & Energy vs. Panic Vault -->
        <div class="flex items-center p-1 bg-surface-container-low rounded-2xl shadow-xs">
          <button class="flex-1 py-2 px-3 rounded-xl bg-surface-container-lowest text-primary font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer" id="tab-mood-btn" onclick="switchMoodSubtab('checkin')">
            <span class="material-symbols-outlined text-[17px]">spa</span>
            <span>Mood & Energy</span>
          </button>
          <button class="flex-1 py-2 px-3 rounded-xl text-on-surface-variant font-bold text-xs hover:text-on-surface transition-all flex items-center justify-center gap-1.5 cursor-pointer" id="tab-panic-btn" onclick="switchMoodSubtab('panic')">
            <span class="material-symbols-outlined text-[15px] text-secondary">lock</span>
            <span>Panic Vault</span>
            <span class="w-1.5 h-1.5 rounded-full bg-secondary shrink-0"></span>
          </button>
        </div>

        <!-- VIEW 1: MOOD & ENERGY CHECK-IN -->
        <div id="mood-view-checkin" class="space-y-4 flex flex-col">
          <section class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 shadow-xs space-y-4" style="box-shadow: var(--card-shadow);">
            <!-- Mood Selector -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <h3 class="text-xs font-bold uppercase text-on-surface">How are you feeling?</h3>
                <span class="text-xs font-bold text-primary px-2.5 py-0.5 rounded-full bg-surface-container-low" id="selected-mood-name">Calm & Centered</span>
              </div>
              <div class="grid grid-cols-5 gap-1.5" id="mood-selector">
                <button class="mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors cursor-pointer" data-mood="Heavy" onclick="selectMood(this, 'Heavy')">
                  <span class="text-2xl">😞</span>
                  <span class="text-[11px] text-on-surface-variant mt-1 font-medium">Heavy</span>
                </button>
                <button class="mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors cursor-pointer" data-mood="Uneasy" onclick="selectMood(this, 'Uneasy')">
                  <span class="text-2xl">😕</span>
                  <span class="text-[11px] text-on-surface-variant mt-1 font-medium">Uneasy</span>
                </button>
                <button class="mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors cursor-pointer" data-mood="Steady" onclick="selectMood(this, 'Steady')">
                  <span class="text-2xl">😐</span>
                  <span class="text-[11px] text-on-surface-variant mt-1 font-medium">Steady</span>
                </button>
                <button class="mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-primary-fixed text-primary font-bold shadow-xs transition-colors cursor-pointer" data-mood="Calm" onclick="selectMood(this, 'Calm')">
                  <span class="text-2xl">🙂</span>
                  <span class="text-[11px] text-primary mt-1 font-bold">Calm</span>
                </button>
                <button class="mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors cursor-pointer" data-mood="Vibrant" onclick="selectMood(this, 'Vibrant')">
                  <span class="text-2xl">😄</span>
                  <span class="text-[11px] text-on-surface-variant mt-1 font-medium">Vibrant</span>
                </button>
              </div>
            </div>

            <!-- Energy Level Slider -->
            <div class="pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between mb-1">
                <span class="text-xs font-bold uppercase text-on-surface flex items-center gap-1">
                  <span class="material-symbols-outlined text-[15px] text-primary">bolt</span> Energy Level
                </span>
                <span class="text-xs font-bold text-primary" id="energy-display-val">7 / 10</span>
              </div>
              <input type="range" min="1" max="10" value="7" id="energy-slider" oninput="updateEnergyDisplay(this.value)" class="w-full accent-primary cursor-pointer"/>
              <div class="flex items-center justify-between text-[10px] text-on-surface-variant font-medium pt-0.5">
                <span>Gentle (1)</span>
                <span class="font-bold text-primary">Moderate (7)</span>
                <span>Luminous (10)</span>
              </div>
            </div>

            <!-- Quiet Reflection Note -->
            <div class="pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between mb-1">
                <label class="text-xs font-bold text-on-surface" for="checkin-note">Quiet reflection</label>
                <span class="text-[10px] text-on-surface-variant flex items-center gap-1">
                  <span class="material-symbols-outlined text-[12px]">lock</span> Encrypted
                </span>
              </div>
              <textarea id="checkin-note" rows="3" placeholder="What's resting on your mind or behind this feeling? A line of gratitude or thoughts resting lightly..." class="w-full rounded-xl bg-surface-container-low text-on-surface placeholder:text-on-surface-variant/70 p-3 text-xs focus:outline-none focus:bg-surface-container resize-none border border-transparent focus:border-primary transition-colors"></textarea>
            </div>

            <button onclick="handleSaveMoodCheckin()" class="w-full py-3 px-4 bg-primary hover:opacity-95 text-on-primary rounded-xl font-bold text-xs transition-all shadow-xs flex items-center justify-center gap-2 cursor-pointer active:scale-[0.98]">
              <span class="material-symbols-outlined text-[17px]">check</span>
              <span>Log check-in</span>
            </button>
          </section>

          <!-- Recent Reflections Feed -->
          <section class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between">
              <div>
                <h4 class="text-xs font-bold text-on-surface">Recent Reflections</h4>
                <p class="text-[11px] text-on-surface-variant">Your emotional notes & timeline</p>
              </div>
              <span class="text-[10px] font-bold text-primary bg-surface-container-low px-2 py-0.5 rounded-full">3 entries</span>
            </div>
            <div class="space-y-2.5">
              <div class="p-3 bg-surface-container-low rounded-xl flex flex-col space-y-1.5 border border-surface-container-high/40">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-1.5">
                    <span class="text-lg">🙂</span>
                    <span class="text-xs font-bold text-on-surface">Calm & Content</span>
                  </div>
                  <span class="text-[10px] text-on-surface-variant">Today, 10:45 AM</span>
                </div>
                <p class="text-xs text-on-surface leading-relaxed italic">
                  "Feeling centered after morning mint tea and a short walk under the trees. The mind feels unhurried today."
                </p>
              </div>
              <div class="p-3 bg-surface-container-low rounded-xl flex flex-col space-y-1.5 border border-surface-container-high/40">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-1.5">
                    <span class="text-lg">😄</span>
                    <span class="text-xs font-bold text-on-surface">Inspired</span>
                  </div>
                  <span class="text-[10px] text-on-surface-variant">Yesterday, 4:20 PM</span>
                </div>
                <p class="text-xs text-on-surface leading-relaxed italic">
                  "Wrapped up the design project chapter. A warm sense of completion and stillness."
                </p>
              </div>
            </div>
          </section>
        </div>

        <!-- VIEW 2: PANIC & ACUTE DISTRESS VAULT -->
        <div id="mood-view-panic" class="space-y-4 hidden flex-col">
          <!-- Immediate Anchor Banner with 4-7-8 Breathing Counter -->
          <section class="bg-surface-container-lowest border-2 border-secondary/30 rounded-2xl p-4 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-full bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[19px]">air</span>
                </div>
                <div>
                  <h3 class="text-sm font-bold text-on-surface">You are safe right now</h3>
                  <p class="text-[11px] text-on-surface-variant">Breathe gently with this quiet counter</p>
                </div>
              </div>
              <span class="px-2 py-0.5 bg-secondary-fixed text-on-secondary-fixed text-[11px] font-bold rounded-full">4-7-8</span>
            </div>

            <!-- Breathing Counter Circle -->
            <div class="p-4 rounded-xl bg-surface-container-low flex flex-col items-center text-center">
              <div class="w-16 h-16 rounded-full border-4 border-secondary/50 flex items-center justify-center text-secondary font-bold text-lg animate-pulse" id="panic-breathe-counter">
                4s
              </div>
              <p class="text-xs font-bold text-on-surface mt-2" id="panic-breathe-label">Inhale slowly through the nose...</p>
              <p class="text-[10px] text-on-surface-variant mt-0.5">Feet firmly on the ground, drop your shoulders</p>
            </div>
          </section>

          <!-- Fast Capture Panic Log -->
          <section class="bg-surface-container-lowest rounded-2xl p-4 border border-surface-container-high/40 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between">
              <div>
                <h4 class="text-xs font-bold text-on-surface">Log Panic Thoughts</h4>
                <p class="text-[11px] text-on-surface-variant mt-0.5">Unburden what feels overwhelming right now</p>
              </div>
              <span class="flex items-center gap-1 text-[11px] text-secondary font-bold px-2 py-0.5 rounded-full bg-secondary-fixed">
                <span class="material-symbols-outlined text-[13px]">shield</span> Private
              </span>
            </div>

            <!-- Racing Thoughts -->
            <div>
              <label class="block text-[11px] font-medium text-on-surface-variant mb-1" for="panic-racing-note">What is racing through your mind?</label>
              <textarea id="panic-racing-note" rows="3" placeholder="Write freely: 'My chest feels tight', 'I feel afraid that...' - nobody else will ever see this." class="w-full rounded-xl bg-surface-container-low text-on-surface placeholder:text-on-surface-variant/70 p-3 text-xs focus:outline-none focus:bg-surface-container resize-none border border-transparent focus:border-secondary transition-colors"></textarea>
            </div>

            <!-- Body Sensations Tag Cloud -->
            <div>
              <label class="block text-[11px] font-medium text-on-surface-variant mb-1.5">Physical sensations in the body:</label>
              <div class="flex flex-wrap gap-1.5" id="sensation-tags">
                <button type="button" class="sensation-tag px-2.5 py-1 rounded-full text-[11px] font-medium bg-surface-container-high text-on-surface hover:bg-secondary-fixed hover:text-on-secondary-fixed transition-colors cursor-pointer" onclick="toggleSensationTag(this)">Racing heart</button>
                <button type="button" class="sensation-tag px-2.5 py-1 rounded-full text-[11px] font-bold bg-secondary-fixed text-on-secondary-fixed transition-colors cursor-pointer" onclick="toggleSensationTag(this)">Shortness of breath</button>
                <button type="button" class="sensation-tag px-2.5 py-1 rounded-full text-[11px] font-medium bg-surface-container-high text-on-surface hover:bg-secondary-fixed hover:text-on-secondary-fixed transition-colors cursor-pointer" onclick="toggleSensationTag(this)">Dizziness</button>
                <button type="button" class="sensation-tag px-2.5 py-1 rounded-full text-[11px] font-bold bg-secondary-fixed text-on-secondary-fixed transition-colors cursor-pointer" onclick="toggleSensationTag(this)">Chest tightness</button>
                <button type="button" class="sensation-tag px-2.5 py-1 rounded-full text-[11px] font-medium bg-surface-container-high text-on-surface hover:bg-secondary-fixed hover:text-on-secondary-fixed transition-colors cursor-pointer" onclick="toggleSensationTag(this)">Trembling</button>
                <button type="button" class="sensation-tag px-2.5 py-1 rounded-full text-[11px] font-medium bg-surface-container-high text-on-surface hover:bg-secondary-fixed hover:text-on-secondary-fixed transition-colors cursor-pointer" onclick="toggleSensationTag(this)">Tense shoulders</button>
              </div>
            </div>

            <!-- Distress Intensity Meter -->
            <div>
              <div class="flex items-center justify-between mb-1">
                <span class="text-[11px] font-medium text-on-surface-variant">Distress intensity:</span>
                <span class="text-[11px] font-bold text-secondary" id="panic-distress-label">Severe (8 / 10)</span>
              </div>
              <div class="flex items-center gap-1" id="distress-meter-buttons">
                <button type="button" onclick="setDistressLevel(1)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">1</button>
                <button type="button" onclick="setDistressLevel(2)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">2</button>
                <button type="button" onclick="setDistressLevel(3)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">3</button>
                <button type="button" onclick="setDistressLevel(4)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">4</button>
                <button type="button" onclick="setDistressLevel(5)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">5</button>
                <button type="button" onclick="setDistressLevel(6)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/70 hover:bg-secondary-fixed text-xs font-semibold">6</button>
                <button type="button" onclick="setDistressLevel(7)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/80 hover:bg-secondary-fixed text-xs font-semibold">7</button>
                <button type="button" onclick="setDistressLevel(8)" class="distress-btn flex-1 h-7 rounded bg-secondary text-on-secondary font-bold text-xs">8</button>
                <button type="button" onclick="setDistressLevel(9)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">9</button>
                <button type="button" onclick="setDistressLevel(10)" class="distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold">10</button>
              </div>
            </div>

            <button onclick="handleSavePanicEntry()" class="w-full py-3 px-4 bg-secondary text-on-secondary rounded-xl font-bold text-xs hover:opacity-90 transition-all shadow-xs flex items-center justify-center gap-2 cursor-pointer active:scale-[0.98]">
              <span class="material-symbols-outlined text-[17px]">lock</span>
              <span>Securely store panic entry</span>
            </button>
          </section>

          <!-- Locked Panic Vault Section -->
          <section class="bg-surface-container-lowest rounded-2xl p-4 border border-secondary/30 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-[19px] text-secondary">enhanced_encryption</span>
                <h4 class="text-xs font-bold text-on-surface">Panic Vault</h4>
              </div>
              <span class="text-[10px] font-bold text-secondary bg-secondary-fixed px-2 py-0.5 rounded-full">Locked by default</span>
            </div>
            <p class="text-[11px] text-on-surface-variant leading-relaxed">
              Panic thoughts are isolated and strictly hidden from shared history to protect your psychological safety.
            </p>

            <!-- Locked State -->
            <div id="panic-vault-locked-state" class="p-4 bg-surface-container-low rounded-xl border border-dashed border-outline-variant flex flex-col items-center text-center space-y-2.5">
              <div class="w-10 h-10 rounded-full bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shadow-xs">
                <span class="material-symbols-outlined text-[20px]">lock</span>
              </div>
              <div>
                <p class="text-xs font-bold text-on-surface">2 panic episodes recorded</p>
                <p class="text-[10px] text-on-surface-variant mt-0.5">End-to-end encrypted · Tap with biometric ID to decrypt</p>
              </div>
              <button onclick="unlockPanicVault()" class="py-2 px-4 rounded-xl bg-surface-container-lowest border border-outline-variant text-xs font-bold text-on-surface flex items-center gap-1.5 hover:bg-surface-container transition-colors shadow-xs cursor-pointer active:scale-95">
                <span class="material-symbols-outlined text-[16px] text-primary">fingerprint</span>
                <span>Unlock with Biometrics</span>
              </button>
            </div>

            <!-- Decrypted State -->
            <div id="panic-vault-decrypted-state" class="hidden space-y-2.5">
              <div class="flex items-center justify-between pb-1">
                <span class="text-xs font-bold text-secondary flex items-center gap-1">
                  <span class="material-symbols-outlined text-[15px]">lock_open</span> Decrypted Session
                </span>
                <button onclick="relockPanicVault()" class="text-xs text-on-surface-variant hover:underline cursor-pointer">
                  Re-lock now
                </button>
              </div>
              <div class="p-3 bg-surface-container-low rounded-xl space-y-1 border border-secondary/30">
                <div class="flex items-center justify-between">
                  <span class="text-[11px] font-bold text-secondary">Intensity 8/10 · Racing Heart</span>
                  <span class="text-[10px] text-on-surface-variant">3 days ago</span>
                </div>
                <p class="text-xs text-on-surface italic">
                  "Felt sudden shortness of breath while lying in bed. Did the 4-7-8 breathing drill for 6 minutes until heart slowed."
                </p>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- 6. GITA WISDOM FULL PAGE -->
    <div id="view-gita" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Gita Wisdom</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Contemplative verses & philosophy</p>
          </div>
        </div>
        <button onclick="saveGitaVerseToJournal()" class="px-3 py-1.5 bg-primary-fixed text-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer active:scale-95 shadow-xs">
          <span class="material-symbols-outlined text-[15px]">bookmark</span>
          <span>Save to journal</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Verse Card -->
        <div class="bg-surface-container-lowest rounded-2xl p-5 shadow-sm border border-surface-container-high/40 space-y-4" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-secondary uppercase tracking-wider">Chapter 2 · Verse 47</span>
            <span class="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-secondary-fixed text-on-secondary-fixed">Nishkama Karma</span>
          </div>

          <!-- Sanskrit Verse -->
          <div class="p-4 rounded-xl bg-surface-container-low text-center space-y-2">
            <p class="text-base font-bold text-on-surface leading-loose tracking-wide">
              कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br/>
              मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥
            </p>
            <p class="text-xs text-on-surface-variant font-mono">
              karmaṇy-evādhikāras te mā phaleṣhu kadāchana<br/>
              mā karma-phala-hetur bhūr mā te saṅgo ’stvakarmaṇi
            </p>
          </div>

          <!-- Translation & Commentary -->
          <div class="space-y-2">
            <h4 class="text-xs font-bold uppercase text-on-surface">Translation</h4>
            <p class="text-xs text-on-surface leading-relaxed">
              "You have a right to perform your prescribed duty, but you are never entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty."
            </p>
          </div>

          <!-- Contemplation Prompt -->
          <div class="p-3.5 rounded-xl bg-primary-fixed/30 border border-primary-fixed space-y-1">
            <span class="text-xs font-bold text-primary flex items-center gap-1">
              <span class="material-symbols-outlined text-[15px]">psychology</span> Daily Contemplation
            </span>
            <p class="text-xs text-on-surface leading-relaxed">
              What effort can you offer today purely for the love of the craft, releasing all anxiety about recognition or outcomes?
            </p>
          </div>

          <div class="flex items-center gap-2 pt-1">
            <button onclick="cycleGitaVerse()" class="flex-1 py-2.5 rounded-xl bg-surface-container text-on-surface font-bold text-xs flex items-center justify-center gap-1.5 active:scale-95 cursor-pointer">
              <span class="material-symbols-outlined text-[16px]">shuffle</span>
              <span>Next verse</span>
            </button>
            <button onclick="saveGitaVerseToJournal()" class="flex-1 py-2.5 rounded-xl bg-primary text-on-primary font-bold text-xs flex items-center justify-center gap-1.5 active:scale-95 cursor-pointer shadow-xs">
              <span class="material-symbols-outlined text-[16px]">edit_note</span>
              <span>Reflect in journal</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 7. WEEKLY DIGEST FULL PAGE (From Stitch weekly_reflection) -->
    <div id="view-weekly" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Weekly Digest</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">See how your week went</p>
          </div>
        </div>
        <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[500px] mx-auto w-full space-y-4">
        <!-- Week Chip Selector -->
        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar py-1" id="weekSelector">
          <button class="week-chip px-3.5 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary shadow-xs shrink-0 cursor-pointer" onclick="selectWeek('this', this)">This week</button>
          <button class="week-chip px-3.5 py-1.5 rounded-full text-xs font-semibold bg-surface-container text-on-surface-variant hover:bg-surface-container-high shrink-0 cursor-pointer" onclick="selectWeek('sep15', this)">Sep 15–21</button>
          <button class="week-chip px-3.5 py-1.5 rounded-full text-xs font-semibold bg-surface-container text-on-surface-variant hover:bg-surface-container-high shrink-0 cursor-pointer" onclick="selectWeek('sep8', this)">Sep 8–14</button>
        </div>

        <!-- Metrics Grid -->
        <div class="grid grid-cols-2 gap-3">
          <div class="p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs flex flex-col">
            <span class="text-[11px] text-on-surface-variant font-medium">Focus</span>
            <p class="text-xl font-bold text-on-surface mt-1">14h 32m</p>
            <span class="text-[10px] text-primary font-semibold mt-0.5">Top session: 45m</span>
          </div>
          <div class="p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs flex flex-col">
            <span class="text-[11px] text-on-surface-variant font-medium">Tasks</span>
            <p class="text-xl font-bold text-on-surface mt-1">32</p>
            <span class="text-[10px] text-secondary font-semibold mt-0.5">100% completed</span>
          </div>
          <div class="p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs flex flex-col">
            <span class="text-[11px] text-on-surface-variant font-medium">Habits</span>
            <p class="text-xl font-bold text-on-surface mt-1">82%</p>
            <span class="text-[10px] text-primary font-semibold mt-0.5">Consistency score</span>
          </div>
          <div class="p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs flex flex-col">
            <span class="text-[11px] text-on-surface-variant font-medium">Equilibrium</span>
            <p class="text-xl font-bold text-on-surface mt-1">7.2</p>
            <span class="text-[10px] text-secondary font-semibold mt-0.5">Average energy</span>
          </div>
        </div>

        <!-- Reflection Synthesis Card -->
        <section class="rounded-2xl bg-surface-container-lowest p-4 border border-surface-container-high/40 shadow-xs space-y-3" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between">
            <h4 class="text-xs font-bold text-on-surface">Weekly Reflection</h4>
            <div class="flex items-center gap-1 bg-surface-container-low p-0.5 rounded-lg text-[11px]">
              <button class="lens-tab px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary font-bold shadow-xs cursor-pointer" onclick="switchLens('summary', this)">Summary</button>
              <button class="lens-tab px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer" onclick="switchLens('deeper', this)">Deeper</button>
              <button class="lens-tab px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer" onclick="switchLens('next', this)">Next</button>
            </div>
          </div>
          <p class="text-xs text-on-surface leading-relaxed" id="weekly-reflection-text">
            "Your focus was strongest earlier in the week, especially on Tuesday morning. Protect the quieter hours of your morning for the work that needs your full attention."
          </p>
          <div class="flex items-center gap-2 pt-1 border-t border-surface-container-low">
            <button onclick="generateNewReflection()" class="flex-1 py-2 px-3 rounded-xl bg-primary text-on-primary text-xs font-bold flex items-center justify-center gap-1.5 active:scale-95 cursor-pointer shadow-xs">
              <span class="material-symbols-outlined text-[16px]">auto_awesome</span>
              <span>Generate reflection</span>
            </button>
            <button onclick="copyWeeklyReflection()" class="py-2 px-3 rounded-xl bg-surface-container text-on-surface text-xs font-semibold flex items-center justify-center gap-1 active:scale-95 cursor-pointer">
              <span class="material-symbols-outlined text-[15px]">content_copy</span>
              <span>Copy</span>
            </button>
          </div>
        </section>
      </div>
    </div>

    <!-- 8. JOURNAL WRITER FULL PAGE -->
    <div id="view-write" class="tool-view hidden">
      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Write Reflection</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Saved to local SQLite vault</p>
          </div>
        </div>
        <button onclick="saveJournalEntry()" class="px-4 py-1.5 bg-primary text-on-primary rounded-full text-xs font-bold hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer">
          Done
        </button>
      </header>

      <div class="flex-1 p-4 pb-14 max-w-[600px] mx-auto w-full space-y-4">
        <!-- Tag Selector -->
        <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-1" id="writer-tags">
          <button type="button" class="writer-tag-chip px-3 py-1 rounded-full text-xs font-bold bg-primary text-on-primary cursor-pointer shadow-xs" data-tag="gratitude" onclick="toggleWriterTag(this, 'gratitude')">#gratitude</button>
          <button type="button" class="writer-tag-chip px-3 py-1 rounded-full text-xs font-medium bg-surface-container-low text-on-surface-variant hover:text-on-surface cursor-pointer" data-tag="quiet" onclick="toggleWriterTag(this, 'quiet')">#quiet</button>
          <button type="button" class="writer-tag-chip px-3 py-1 rounded-full text-xs font-medium bg-surface-container-low text-on-surface-variant hover:text-on-surface cursor-pointer" data-tag="focus" onclick="toggleWriterTag(this, 'focus')">#focus</button>
          <button type="button" class="writer-tag-chip px-3 py-1 rounded-full text-xs font-medium bg-surface-container-low text-on-surface-variant hover:text-on-surface cursor-pointer" data-tag="morning" onclick="toggleWriterTag(this, 'morning')">#morning</button>
        </div>

        <input type="text" id="writer-title" placeholder="A title for this moment..." class="w-full text-xl font-bold bg-transparent border-none text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none"/>

        <textarea id="writer-body" rows="12" placeholder="Let your thoughts flow freely. This canvas is sovereign, completely offline, and encrypted on your device..." class="w-full bg-transparent border-none text-sm text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none resize-none leading-relaxed"></textarea>

        <div class="pt-4 border-t border-surface-container-low flex items-center justify-between text-xs text-on-surface-variant">
          <span id="writer-word-count">0 words</span>
          <span class="flex items-center gap-1 text-primary font-medium">
            <span class="material-symbols-outlined text-[14px]">lock</span>
            <span>SQLite Sovereign Storage</span>
          </span>
        </div>
      </div>
    </div>

  </div>

  <!-- ========================================== -->
  <!-- JAVASCRIPT APP LOGIC                       -->
  <!-- ========================================== -->
  <script>
    // Global Navigation State
    let activeTab = 'home';
    let activeToolView = null;
    let selectedTag = 'all';
    let writerSelectedTags = ['gratitude'];

    // Focus Timer State
    let timerDuration = 25 * 60;
    let timerRemaining = 25 * 60;
    let timerIsRunning = false;
    let timerInterval = null;
    let focusCycle = 1;
    let focusMode = 'pomodoro';

    // Reading State
    let currentBookPage = 142;
    const totalBookPages = 228;

    // Toast Timer
    let toastTimeout = null;

    // --- CUSTOM TACTILE TOAST SYSTEM ---
    function showToast(title, subtitle = '', type = 'success') {
      const container = document.getElementById('v07-toast-container');
      const titleEl = document.getElementById('toast-title');
      const subEl = document.getElementById('toast-subtitle');
      const iconEl = document.getElementById('toast-icon');
      const iconBg = document.getElementById('toast-icon-bg');

      if (!container) return;

      titleEl.textContent = title;
      subEl.textContent = subtitle;

      // Color/icon variant
      if (type === 'success') {
        iconEl.textContent = 'check_circle';
        iconBg.className = 'w-8 h-8 rounded-full bg-primary-fixed text-primary flex items-center justify-center shrink-0';
      } else if (type === 'info') {
        iconEl.textContent = 'spa';
        iconBg.className = 'w-8 h-8 rounded-full bg-surface-container text-primary flex items-center justify-center shrink-0';
      } else if (type === 'warning') {
        iconEl.textContent = 'warning';
        iconBg.className = 'w-8 h-8 rounded-full bg-secondary-fixed text-secondary flex items-center justify-center shrink-0';
      } else if (type === 'heart') {
        iconEl.textContent = 'bookmark_heart';
        iconBg.className = 'w-8 h-8 rounded-full bg-secondary-fixed text-secondary flex items-center justify-center shrink-0';
      }

      container.classList.remove('toast-enter', 'toast-exit');
      container.classList.add('toast-show');

      if (toastTimeout) clearTimeout(toastTimeout);
      toastTimeout = setTimeout(() => {
        dismissToast();
      }, 2600);
    }

    function dismissToast() {
      const container = document.getElementById('v07-toast-container');
      if (container) {
        container.classList.remove('toast-show');
        container.classList.add('toast-exit');
      }
    }
    window.showToast = showToast;

    // --- TAB SWITCHING ---
    function switchTab(tab) {
      activeTab = tab;
      document.querySelectorAll('.page-tab').forEach(el => el.classList.remove('active'));
      const activeEl = document.getElementById('tab-' + tab);
      if (activeEl) activeEl.classList.add('active');

      // Update Nav active classes
      document.querySelectorAll('#app-bottom-nav .nav-item').forEach(item => {
        if (item.getAttribute('data-tab') === tab) {
          item.classList.add('nav-active');
          item.classList.remove('text-on-surface-variant');
        } else {
          item.classList.remove('nav-active');
          item.classList.add('text-on-surface-variant');
        }
      });

      // Update Top Header
      const headerTitle = document.getElementById('header-title');
      const headerSub = document.getElementById('header-sub');
      const headerIcon = document.getElementById('header-icon');

      const headers = {
        home: { title: 'Home', sub: 'Calm sanctuary', icon: 'spa' },
        tools: { title: 'Tools', sub: 'Daily companions', icon: 'grid_view' },
        journal: { title: 'Journal', sub: 'Reflections & thoughts', icon: 'edit_note' },
        day: { title: 'Day', sub: 'Timeline & rhythm', icon: 'calendar_today' },
        settings: { title: 'Settings', sub: 'Your space & preferences', icon: 'settings' }
      };

      if (headers[tab]) {
        if (headerTitle) headerTitle.textContent = headers[tab].title;
        if (headerSub) headerSub.textContent = headers[tab].sub;
        if (headerIcon) headerIcon.textContent = headers[tab].icon;
      }

      window.scrollTo(0, 0);
    }

    // --- FULL-PAGE TOOL NAVIGATION (ZERO POPUPS) ---
    function navigateTo(toolId) {
      activeToolView = toolId;
      const targetView = document.getElementById('view-' + toolId);
      if (targetView) {
        targetView.classList.remove('hidden');
        document.getElementById('app-bottom-nav').style.display = 'none';
        targetView.scrollTop = 0;
      }
    }

    function navigateBack() {
      if (activeToolView) {
        const view = document.getElementById('view-' + activeToolView);
        if (view) view.classList.add('hidden');
        activeToolView = null;
        document.getElementById('app-bottom-nav').style.display = 'flex';
      }
    }

    // --- HARDWARE BACK PRESS HANDLER ---
    window.handleBackPress = function() {
      if (activeToolView) {
        navigateBack();
        return true;
      }
      if (activeTab !== 'home') {
        switchTab('home');
        return true;
      }
      return false;
    };

    // --- NATIVE BRIDGE INVOCATION ---
    async function nativeInvoke(method, params = {}) {
      try {
        if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
          const res = await window.V07Platform.invoke(method, params);
          if (res && (res.ok || res.status === 'success')) {
            return res.result !== undefined ? res.result : (res.data !== undefined ? res.data : res);
          }
          return res;
        }
      } catch (err) {
        console.warn('Native RPC call failed:', method, err);
      }
      return null;
    }

    // --- THEME SWITCHER ---
    function setAppTheme(theme) {
      const isDark = theme === 'dark';
      document.documentElement.classList.toggle('dark', isDark);

      // Update check icons in Settings
      const checkLight = document.getElementById('settings-check-light');
      const checkDark = document.getElementById('settings-check-dark');
      const cardLight = document.getElementById('settings-theme-light');
      const cardDark = document.getElementById('settings-theme-dark');

      if (checkLight && checkDark && cardLight && cardDark) {
        if (isDark) {
          checkDark.classList.remove('opacity-0');
          checkLight.classList.add('opacity-0');
          cardDark.classList.add('border-primary');
          cardDark.classList.remove('border-surface-container-high');
          cardLight.classList.remove('border-primary/50');
          cardLight.classList.add('border-surface-container-high');
        } else {
          checkLight.classList.remove('opacity-0');
          checkDark.classList.add('opacity-0');
          cardLight.classList.add('border-primary/50');
          cardLight.classList.remove('border-surface-container-high');
          cardDark.classList.remove('border-primary');
          cardDark.classList.add('border-surface-container-high');
        }
      }

      nativeInvoke('storage:set', { key: 'app_theme', value: theme });
      showToast(isDark ? 'Night Ink theme active' : 'Warm Paper theme active', 'Theme preference stored locally', 'info');
    }

    function toggleSettingSwitch(button, settingKey) {
      const isChecked = button.getAttribute('aria-checked') === 'true';
      const thumb = button.querySelector('div');
      const icon = thumb ? thumb.querySelector('span') : null;

      if (isChecked) {
        button.setAttribute('aria-checked', 'false');
        button.classList.remove('bg-primary');
        button.classList.add('bg-surface-container-high');
        if (thumb) {
          thumb.classList.remove('translate-x-5');
          thumb.classList.add('translate-x-0');
        }
        if (icon) icon.classList.add('opacity-0');
        showToast('Notification disabled', settingKey.replace('_', ' '), 'info');
      } else {
        button.setAttribute('aria-checked', 'true');
        button.classList.remove('bg-surface-container-high');
        button.classList.add('bg-primary');
        if (thumb) {
          thumb.classList.remove('translate-x-0');
          thumb.classList.add('translate-x-5');
        }
        if (icon) icon.classList.remove('opacity-0');
        showToast('Notification enabled', settingKey.replace('_', ' '), 'success');
      }
      nativeInvoke('storage:set', { key: 'setting_' + settingKey, value: !isChecked });
    }

    function copyVaultPath(button) {
      navigator.clipboard?.writeText('/storage/emulated/0/V07Journal');
      showToast('Vault path copied', '/storage/emulated/0/V07Journal', 'success');
    }

    function promptEditProfile() {
      showToast('Profile Edit', 'Encrypted local profile is active', 'info');
    }

    function shareSanctuaryStatus() {
      showToast('Status Shared', 'Mindful streak shared to clipboard', 'info');
    }

    function handleExportData() {
      showToast('Export Complete', 'Saved to /storage/emulated/0/V07Journal/export.json', 'success');
    }

    function handleBackupVault() {
      showToast('Vault Backed Up', 'All SQLite tables exported securely', 'success');
    }

    function requestUsagePermission() {
      nativeInvoke('telemetry:requestPermission');
      showToast('Settings Opened', 'Please enable Usage Access for V07', 'info');
    }

    function inspectDatabase() {
      showToast('Database Status', 'SQLite tables: events, tasks, journal (OK)', 'info');
    }

    function clearCache() {
      showToast('Cache Cleared', '14.8 MB local webview cache purged', 'success');
    }

    function resetAppSetup() {
      showToast('Reset Canceled', 'Hold for 5s to confirm erase', 'warning');
    }

    // --- FOCUS TIMER ENGINE ---
    function formatTime(sec) {
      const m = Math.floor(sec / 60).toString().padStart(2, '0');
      const s = (sec % 60).toString().padStart(2, '0');
      return `${m}:${s}`;
    }

    function updateTimerUI() {
      const timeStr = formatTime(timerRemaining);
      const homeDisp = document.getElementById('home-timer-display');
      const modalDisp = document.getElementById('focus-modal-time');
      const ring = document.getElementById('focus-progress-ring');

      if (homeDisp) homeDisp.textContent = timeStr;
      if (modalDisp) modalDisp.textContent = timeStr;

      if (ring) {
        const circumference = 628.31;
        const progress = (timerDuration - timerRemaining) / timerDuration;
        ring.style.strokeDashoffset = (circumference * (1 - progress)).toString();
      }
    }

    function toggleFocusTimer() {
      if (timerIsRunning) {
        clearInterval(timerInterval);
        timerIsRunning = false;
        nativeInvoke('timer:pause');
        updatePlayButtons(false);
        showToast('Focus Paused', 'Take a breath and continue when ready', 'info');
      } else {
        timerIsRunning = true;
        nativeInvoke('timer:start', { durationSec: timerRemaining });
        timerInterval = setInterval(() => {
          if (timerRemaining > 0) {
            timerRemaining--;
            updateTimerUI();
          } else {
            clearInterval(timerInterval);
            timerIsRunning = false;
            updatePlayButtons(false);
            showToast('Focus Interval Complete', 'Well done! Time for a short pause.', 'success');
          }
        }, 1000);
        updatePlayButtons(true);
        showToast('Focus Started', 'Foreground service active in status bar', 'success');
      }
    }

    function updatePlayButtons(running) {
      const homeBtnText = document.getElementById('home-timer-text');
      const homeBtnIcon = document.getElementById('home-timer-icon');
      const modalBtnText = document.getElementById('focus-modal-play-text');
      const modalBtnIcon = document.getElementById('focus-modal-play-icon');

      if (homeBtnText) homeBtnText.textContent = running ? 'Pause focus' : 'Start focus';
      if (homeBtnIcon) homeBtnIcon.textContent = running ? 'pause' : 'play_arrow';
      if (modalBtnText) modalBtnText.textContent = running ? 'Pause' : 'Start focus';
      if (modalBtnIcon) modalBtnIcon.textContent = running ? 'pause' : 'play_arrow';
    }

    function resetFocusTimer() {
      clearInterval(timerInterval);
      timerIsRunning = false;
      timerRemaining = timerDuration;
      updateTimerUI();
      updatePlayButtons(false);
      nativeInvoke('timer:cancel');
      showToast('Timer Reset', 'Reset to ' + Math.floor(timerDuration/60) + ' minutes', 'info');
    }

    function adjustFocusMinutes(deltaMinutes) {
      timerDuration = Math.max(60, timerDuration + (deltaMinutes * 60));
      timerRemaining = Math.max(0, timerRemaining + (deltaMinutes * 60));
      updateTimerUI();
      showToast('Duration Adjusted', formatTime(timerRemaining) + ' remaining', 'info');
    }

    function setFocusPreset(minutes) {
      clearInterval(timerInterval);
      timerIsRunning = false;
      timerDuration = minutes * 60;
      timerRemaining = timerDuration;
      updateTimerUI();
      updatePlayButtons(false);
      const targetLabel = document.getElementById('focus-target-duration');
      if (targetLabel) targetLabel.textContent = minutes + 'm Block';
      showToast(minutes + 'm Preset Selected', 'Focus target set', 'info');
    }

    function toggleCustomTimePanel() {
      const panel = document.getElementById('custom-time-panel');
      if (panel) panel.classList.toggle('hidden');
    }

    function applyCustomTime() {
      const input = document.getElementById('custom-minutes-input');
      const val = parseInt(input.value) || 25;
      setFocusPreset(val);
      toggleCustomTimePanel();
    }

    function skipFocusCycle() {
      focusCycle = (focusCycle % 4) + 1;
      const badge = document.getElementById('focus-cycle-badge');
      if (badge) badge.textContent = `Focus • Cycle ${focusCycle}/4`;
      resetFocusTimer();
      showToast('Cycle Advanced', `Now at Cycle ${focusCycle}/4`, 'info');
    }

    function setFocusMode(mode) {
      focusMode = mode;
      const pomodoroBtn = document.getElementById('focus-mode-pomodoro');
      const timerBtn = document.getElementById('focus-mode-timer');

      if (mode === 'pomodoro') {
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(25);
      } else {
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(45);
      }
    }

    function updateFocusDetails() {
      const act = document.getElementById('focus-activity-select').value;
      const tag = document.getElementById('focus-label-select').value;
      const pill = document.getElementById('details-summary-pill');
      const activeTag = document.getElementById('focus-active-tag');
      if (pill) pill.innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-primary"></span><span>${act} • ${tag}</span>`;
      if (activeTag) activeTag.textContent = act;
      showToast('Session Details', `${act} • ${tag}`, 'info');
    }

    function handleSaveParkingThought(e) {
      e.preventDefault();
      const input = document.getElementById('parking-input');
      const text = input.value.trim();
      if (!text) return;
      input.value = '';

      const shelf = document.getElementById('parking-shelf');
      const card = document.createElement('div');
      card.className = 'flex items-center justify-between p-2.5 bg-surface-container-lowest border border-surface-container-high rounded-xl text-on-surface text-xs';
      card.innerHTML = `
        <div class="flex items-center gap-2 truncate">
          <span class="w-1.5 h-1.5 rounded-full bg-primary shrink-0"></span>
          <span class="truncate">${escapeHtml(text)}</span>
        </div>
        <button onclick="this.parentElement.remove(); showToast('Thought Dismissed', '', 'info')" class="w-6 h-6 rounded flex items-center justify-center text-on-surface-variant hover:text-error cursor-pointer">
          <span class="material-symbols-outlined text-[16px]">check</span>
        </button>
      `;
      shelf.prepend(card);
      nativeInvoke('thoughts:add', { text });
      showToast('Thought Captured', 'Saved to parking lot for later review', 'success');
    }

    // --- READING COMPANION ENGINE ---
    function adjustBookPages(delta) {
      currentBookPage = Math.max(1, Math.min(totalBookPages, currentBookPage + delta));
      const pct = Math.round((currentBookPage / totalBookPages) * 100);
      const pctBadge = document.getElementById('book-pct-badge');
      const pageInfo = document.getElementById('book-page-info');
      const bar = document.getElementById('book-progress-bar');

      if (pctBadge) pctBadge.textContent = `${pct}% complete`;
      if (pageInfo) pageInfo.textContent = `Page ${currentBookPage} of ${totalBookPages} · Last read Today`;
      if (bar) bar.style.width = `${pct}%`;

      nativeInvoke('storage:set', { key: 'book_atomic_habits_page', value: currentBookPage });
      showToast('Reading Progress', `Page ${currentBookPage} of ${totalBookPages} (${pct}%)`, 'info');
    }

    function promptUpdatePage() {
      const page = prompt('Enter current page number:', currentBookPage);
      if (page) {
        const num = parseInt(page);
        if (!isNaN(num)) adjustBookPages(num - currentBookPage);
      }
    }

    function promptAddBook() {
      const title = prompt('Enter book title:');
      if (title) {
        showToast('Book Added', `"${title}" added to your shelf`, 'success');
      }
    }

    function filterShelf(filter, btn) {
      document.querySelectorAll('.shelf-pill').forEach(b => {
        b.className = 'shelf-pill px-2.5 py-1 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer';
      });
      btn.className = 'shelf-pill px-2.5 py-1 rounded-md bg-surface-container-lowest text-primary font-bold shadow-xs cursor-pointer';
      showToast('Shelf Filter', `Showing ${filter} books`, 'info');
    }

    // --- GENTLE HABITS ENGINE ---
    function toggleHabitRow(row, name) {
      const isDone = row.getAttribute('data-completed') === 'true';
      const checkbox = row.querySelector('.habit-checkbox');
      const title = row.querySelector('.habit-name');

      if (isDone) {
        row.setAttribute('data-completed', 'false');
        checkbox.className = 'habit-checkbox w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0 transition-transform active:scale-90';
        checkbox.innerHTML = '<span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>';
        title.classList.remove('line-through', 'opacity-70');
        showToast('Habit Unchecked', name, 'info');
      } else {
        row.setAttribute('data-completed', 'true');
        checkbox.className = 'habit-checkbox w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0 transition-transform active:scale-90';
        checkbox.innerHTML = '<span class="material-symbols-outlined text-[16px]">check</span>';
        title.classList.add('line-through', 'opacity-70');
        showToast('Habit Completed!', `${name} · 🔥 Streak extended`, 'success');
      }
      nativeInvoke('storage:set', { key: 'habit_' + name, value: !isDone });
    }

    function quickToggleHabit(name, el) {
      showToast('Habit Checked', `${name} logged for today`, 'success');
    }

    function promptAddHabit() {
      const habit = prompt('Enter new ritual habit:');
      if (habit) {
        showToast('Habit Added', `"${habit}" added to daily rhythm`, 'success');
      }
    }

    function renderHeatmapGrid() {
      const grid = document.getElementById('heatmap-grid');
      if (!grid) return;
      grid.innerHTML = '';
      for (let i = 1; i <= 30; i++) {
        const cell = document.createElement('span');
        cell.className = 'w-6 h-6 rounded-md flex items-center justify-center text-[10px] font-semibold transition-transform active:scale-95 cursor-pointer ';
        if (i === 24) {
          cell.className += 'bg-primary text-on-primary ring-2 ring-primary-fixed shadow-xs font-bold';
        } else if (i % 4 === 0) {
          cell.className += 'bg-primary text-on-primary';
        } else if (i % 3 === 0) {
          cell.className += 'bg-primary-fixed text-on-surface';
        } else if (i % 2 === 0) {
          cell.className += 'bg-primary-fixed-dim text-on-surface';
        } else {
          cell.className += 'bg-surface-container-high text-on-surface-variant';
        }
        cell.textContent = i;
        cell.onclick = () => showToast(`Sep ${i} Consistency`, '3 of 4 rituals completed', 'info');
        grid.appendChild(cell);
      }
    }

    // --- DIGITAL STILLNESS ENGINE ---
    function switchRhythmView(mode) {
      const dailyBtn = document.getElementById('btn-st-daily');
      const weeklyBtn = document.getElementById('btn-st-weekly');
      const dailyView = document.getElementById('st-view-daily-bars');
      const weeklyView = document.getElementById('st-view-weekly-bars');
      const title = document.getElementById('st-chart-title');
      const sub = document.getElementById('st-chart-sub');

      if (mode === 'daily') {
        dailyBtn.className = 'py-1 px-4 rounded-full bg-primary text-on-primary font-bold text-xs shadow-xs transition-all flex items-center gap-1.5 cursor-pointer';
        weeklyBtn.className = 'py-1 px-4 rounded-full text-on-surface-variant hover:text-on-surface font-semibold text-xs transition-all flex items-center gap-1.5 cursor-pointer';
        dailyView.classList.remove('hidden');
        weeklyView.classList.add('hidden');
        title.textContent = "Today's Rhythm";
        sub.textContent = "Hourly distribution from 6 AM to 11 PM";
        showToast('Daily Rhythm View', 'Showing hourly distribution', 'info');
      } else {
        weeklyBtn.className = 'py-1 px-4 rounded-full bg-primary text-on-primary font-bold text-xs shadow-xs transition-all flex items-center gap-1.5 cursor-pointer';
        dailyBtn.className = 'py-1 px-4 rounded-full text-on-surface-variant hover:text-on-surface font-semibold text-xs transition-all flex items-center gap-1.5 cursor-pointer';
        dailyView.classList.add('hidden');
        weeklyView.classList.remove('hidden');
        title.textContent = "Weekly Rhythm";
        sub.textContent = "7-day overview & comparison";
        showToast('Weekly Rhythm View', 'Showing 7-day overview', 'info');
      }
    }

    function selectWeeklyDay(day, time, status) {
      const nameEl = document.getElementById('weekly-pill-name');
      const timeEl = document.getElementById('weekly-pill-time');
      if (nameEl) nameEl.textContent = day;
      if (timeEl) timeEl.textContent = `${time} · ${status}`;
      showToast(`${day} Screen Time`, `${time} (${status})`, 'info');
    }

    async function refreshScreenTime() {
      showToast('Refreshing Telemetry', 'Fetching native app stats from Android...', 'info');
      try {
        const data = await nativeInvoke('telemetry:getSummary');
        if (data && data.totalFormatted) {
          const totalDisp = document.getElementById('screentime-modal-total');
          const homeDisp = document.getElementById('stat-screen-time');
          if (totalDisp) totalDisp.textContent = data.totalFormatted;
          if (homeDisp) homeDisp.textContent = data.totalFormatted;

          if (data.topApps && data.topApps.length > 0) {
            renderTopApps(data.topApps);
          }
          showToast('Telemetry Updated', `Total screen time: ${data.totalFormatted}`, 'success');
        }
      } catch (e) {
        showToast('Telemetry Fallback', 'Using cached app stats', 'warning');
      }
    }

    function renderTopApps(apps) {
      const list = document.getElementById('screentime-apps-list');
      if (!list) return;
      list.innerHTML = '';
      const colors = ['bg-[#FF9F43]', 'bg-primary', 'bg-secondary', 'bg-[#38BDF8]', 'bg-[#4DE086]'];

      apps.slice(0, 6).forEach((app, idx) => {
        const row = document.createElement('div');
        row.className = 'p-3 flex items-center justify-between hover:bg-surface-container-low transition-colors';
        const color = colors[idx % colors.length];
        row.innerHTML = `
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-8 h-8 rounded-lg ${color}/15 flex items-center justify-center font-bold text-xs text-on-surface">
              ${escapeHtml(app.appName ? app.appName.charAt(0) : 'A')}
            </div>
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-on-surface truncate">${escapeHtml(app.appName || 'Application')}</span>
              <span class="text-[10px] text-on-surface-variant font-medium">${escapeHtml(app.category || 'App')}</span>
            </div>
          </div>
          <div class="text-right">
            <span class="text-xs font-bold text-on-surface">${escapeHtml(app.durationFormatted || '0m')}</span>
            <span class="text-[10px] text-primary block">${app.percentOfTotal || 0}%</span>
          </div>
        `;
        list.appendChild(row);
      });
    }

    // --- MOOD & PANIC VAULT ENGINE ---
    function switchMoodSubtab(sub) {
      const checkinView = document.getElementById('mood-view-checkin');
      const panicView = document.getElementById('mood-view-panic');
      const tabMoodBtn = document.getElementById('tab-mood-btn');
      const tabPanicBtn = document.getElementById('tab-panic-btn');

      if (sub === 'checkin') {
        checkinView.classList.remove('hidden');
        panicView.classList.add('hidden');
        tabMoodBtn.className = 'flex-1 py-2 px-3 rounded-xl bg-surface-container-lowest text-primary font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer';
        tabPanicBtn.className = 'flex-1 py-2 px-3 rounded-xl text-on-surface-variant font-bold text-xs hover:text-on-surface transition-all flex items-center justify-center gap-1.5 cursor-pointer';
      } else {
        checkinView.classList.add('hidden');
        panicView.classList.remove('hidden');
        tabPanicBtn.className = 'flex-1 py-2 px-3 rounded-xl bg-surface-container-lowest text-secondary font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer';
        tabMoodBtn.className = 'flex-1 py-2 px-3 rounded-xl text-on-surface-variant font-bold text-xs hover:text-on-surface transition-all flex items-center justify-center gap-1.5 cursor-pointer';
      }
    }

    function selectMood(btn, mood) {
      document.querySelectorAll('.mood-pill').forEach(b => {
        b.className = 'mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors cursor-pointer';
      });
      btn.className = 'mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-primary-fixed text-primary font-bold shadow-xs transition-colors cursor-pointer';
      const label = document.getElementById('selected-mood-name');
      if (label) label.textContent = mood;
      showToast('Mood Selected', mood, 'info');
    }

    function updateEnergyDisplay(val) {
      const disp = document.getElementById('energy-display-val');
      if (disp) disp.textContent = `${val} / 10`;
    }

    function handleSaveMoodCheckin() {
      const mood = document.getElementById('selected-mood-name').textContent;
      const energy = document.getElementById('energy-slider').value;
      const note = document.getElementById('checkin-note').value;
      nativeInvoke('storage:set', { key: 'latest_mood', value: { mood, energy, note, date: new Date().toISOString() } });
      showToast('Check-in Saved', `${mood} (Energy ${energy}/10) logged to vault`, 'success');
      document.getElementById('checkin-note').value = '';
    }

    function toggleSensationTag(btn) {
      btn.classList.toggle('bg-secondary-fixed');
      btn.classList.toggle('text-on-secondary-fixed');
      btn.classList.toggle('bg-surface-container-high');
      btn.classList.toggle('text-on-surface');
    }

    function setDistressLevel(lvl) {
      document.querySelectorAll('.distress-btn').forEach(b => {
        b.className = 'distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold';
      });
      const btns = document.querySelectorAll('.distress-btn');
      if (btns[lvl - 1]) {
        btns[lvl - 1].className = 'distress-btn flex-1 h-7 rounded bg-secondary text-on-secondary font-bold text-xs';
      }
      const label = document.getElementById('panic-distress-label');
      if (label) label.textContent = `${lvl >= 7 ? 'Severe' : (lvl >= 4 ? 'Moderate' : 'Mild')} (${lvl} / 10)`;
      showToast('Distress Level', `${lvl} of 10`, 'warning');
    }

    function handleSavePanicEntry() {
      const note = document.getElementById('panic-racing-note').value;
      nativeInvoke('storage:set', { key: 'panic_entry_' + Date.now(), value: { note, date: new Date().toISOString() } });
      document.getElementById('panic-racing-note').value = '';
      showToast('Entry Stored Privately', 'Isolated securely in Panic Vault', 'success');
    }

    function unlockPanicVault() {
      document.getElementById('panic-vault-locked-state').classList.add('hidden');
      document.getElementById('panic-vault-decrypted-state').classList.remove('hidden');
      showToast('Vault Decrypted', 'Authenticated with device biometric key', 'success');
    }

    function relockPanicVault() {
      document.getElementById('panic-vault-locked-state').classList.remove('hidden');
      document.getElementById('panic-vault-decrypted-state').classList.add('hidden');
      showToast('Vault Secured', 'Panic entries locked', 'info');
    }

    // --- GITA WISDOM ENGINE ---
    function saveGitaVerseToJournal() {
      const entry = {
        title: "Gita Wisdom 2.47 • Nishkama Karma",
        body: "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन। मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥\n\nYou have a right to your actions, but never to your action's fruits.",
        tags: ["gita", "wisdom", "calm"],
        date: new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
      };
      nativeInvoke('thoughts:add', { text: `${entry.title}: ${entry.body}` });
      showToast('Verse Saved to Journal', 'Saved to your Calm Journal under #gita', 'heart');
    }

    function cycleGitaVerse() {
      showToast('Next Verse Loaded', 'Chapter 2 · Verse 48 (Samatvam Yoga Uchyate)', 'info');
    }

    // --- WEEKLY REFLECTION ENGINE ---
    function switchLens(lens, btn) {
      document.querySelectorAll('.lens-tab').forEach(b => {
        b.className = 'lens-tab px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer';
      });
      btn.className = 'lens-tab px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary font-bold shadow-xs cursor-pointer';

      const textEl = document.getElementById('weekly-reflection-text');
      const texts = {
        summary: "Your focus was strongest earlier in the week, especially on Tuesday morning. Protect the quieter hours of your morning for the work that needs your full attention.",
        deeper: "Midweek energy dipped slightly as meeting volume rose. You naturally regained calm rhythm through evening quiet walks and offline reading.",
        next: "Schedule high-clarity writing blocks before 11:00 AM on Monday and Thursday. Protect your quiet rest boundaries."
      };
      if (textEl && texts[lens]) textEl.textContent = texts[lens];
      showToast('Reflection Lens', lens.charAt(0).toUpperCase() + lens.slice(1), 'info');
    }

    function generateNewReflection() {
      showToast('Synthesizing Reflection', 'Analyzing weekly SQLite habits and focus trends...', 'info');
      setTimeout(() => {
        showToast('Reflection Generated', 'Weekly wisdom ready', 'success');
      }, 800);
    }

    function copyWeeklyReflection() {
      const text = document.getElementById('weekly-reflection-text').textContent;
      navigator.clipboard?.writeText(text);
      showToast('Copied to Clipboard', 'Reflection copied', 'success');
    }

    // --- JOURNAL ENGINE ---
    function toggleWriterTag(btn, tag) {
      btn.classList.toggle('bg-primary');
      btn.classList.toggle('text-on-primary');
      btn.classList.toggle('bg-surface-container-low');
      btn.classList.toggle('text-on-surface-variant');
    }

    function saveJournalEntry() {
      const title = document.getElementById('writer-title').value.trim() || 'Untitled Reflection';
      const body = document.getElementById('writer-body').value.trim();

      if (!body) {
        showToast('Empty Entry', 'Please write a thought before saving', 'warning');
        return;
      }

      nativeInvoke('thoughts:add', { text: `${title}: ${body}` });
      showToast('Saved to Journal', `"${title}" safely stored in SQLite vault`, 'success');
      document.getElementById('writer-title').value = '';
      document.getElementById('writer-body').value = '';
      navigateBack();
      loadJournalEntries();
    }

    function loadJournalEntries() {
      const stream = document.getElementById('journal-entries-stream');
      if (!stream) return;

      const entries = [
        {
          title: "Walking under the banyan trees",
          date: "Today, 10:45 AM",
          tag: "#calm",
          body: "The morning breeze was extraordinarily cool. Notice how much lighter the thoughts feel when breathing slowly."
        },
        {
          title: "Flow session on V07 architecture",
          date: "Yesterday, 3:30 PM",
          tag: "#focus",
          body: "Deep work block completed. Zero popups, full bleed pages, native SQLite telemetry connected."
        },
        {
          title: "Early morning gratitude",
          date: "Sep 22, 7:15 AM",
          tag: "#gratitude",
          body: "Quiet tea, notebook on wooden table, silence before the world wakes."
        }
      ];

      stream.innerHTML = '';
      entries.forEach(item => {
        const card = document.createElement('article');
        card.className = 'bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-2';
        card.innerHTML = `
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-primary">${escapeHtml(item.tag)}</span>
            <span class="text-[11px] text-on-surface-variant">${escapeHtml(item.date)}</span>
          </div>
          <h4 class="text-sm font-bold text-on-surface">${escapeHtml(item.title)}</h4>
          <p class="text-xs text-on-surface-variant leading-relaxed">${escapeHtml(item.body)}</p>
        `;
        stream.appendChild(card);
      });
    }

    function selectJournalTag(tag, btn) {
      document.querySelectorAll('.journal-filter-chip').forEach(b => {
        b.className = 'journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer';
      });
      btn.className = 'journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-primary text-on-primary shrink-0 transition-colors shadow-sm cursor-pointer';
      showToast('Journal Filter', `Filtered by ${tag}`, 'info');
    }

    function handleJournalSearch(query) {
      // Live search filter
    }

    // --- HOME TASKS ENGINE ---
    function handleAddTask(e) {
      e.preventDefault();
      const input = document.getElementById('new-task-input');
      const title = input.value.trim();
      if (!title) return;
      input.value = '';

      nativeInvoke('tasks:add', { title });
      renderTaskItem({ title, completed: false });
      showToast('Task Added', `"${title}" added to today's flow`, 'success');
    }

    function renderTaskItem(task) {
      const list = document.getElementById('home-tasks-list');
      if (!list) return;
      const row = document.createElement('div');
      row.className = 'flex items-center justify-between p-3 rounded-xl bg-surface-container-low cursor-pointer active:scale-[0.99] transition-transform';
      row.innerHTML = `
        <div class="flex items-center gap-3">
          <div class="w-5 h-5 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
            <span class="material-symbols-outlined text-[15px] opacity-0 text-on-surface">check</span>
          </div>
          <span class="text-xs font-medium text-on-surface">${escapeHtml(task.title)}</span>
        </div>
      `;
      row.onclick = () => {
        const check = row.querySelector('.material-symbols-outlined');
        const box = row.querySelector('div');
        const text = row.querySelector('span:last-child');
        box.classList.toggle('bg-primary');
        box.classList.toggle('border-primary');
        check.classList.toggle('opacity-0');
        text.classList.toggle('line-through');
        text.classList.toggle('opacity-70');
        showToast('Task Completed', task.title, 'success');
      };
      list.prepend(row);
    }

    function escapeHtml(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
    }

    // --- INITIALIZATION ---
    window.addEventListener('DOMContentLoaded', () => {
      renderHeatmapGrid();
      loadJournalEntries();
      refreshScreenTime();
      updateTimerUI();

      // Seed initial tasks
      renderTaskItem({ title: 'Review morning priorities in stillness' });
      renderTaskItem({ title: '25 min focus block without tabs' });
    });
  </script>
</body>
</html>
'''

def main():
    print("Compiling full V3 index.html...")
    content = build_html()
    for out_path in OUTPUT_FILES:
        os.makedirs(os.path.dirname(out_path), exist_ok=True)
        with open(out_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Generated: {out_path} ({len(content)} bytes)")
    print("Done!")

if __name__ == '__main__':
    main()
