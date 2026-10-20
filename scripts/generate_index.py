import os

html_content = r'''<!DOCTYPE html>
<html lang="en" class="">
<head>
  <meta charset="utf-8"/>
  <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover" name="viewport"/>
  <meta content="mobile_tab" name="shell-type"/>
  <title>V07 Sanctuary</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet"/>
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet"/>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    :root {
      --surface: #f3fbf5;
      --surface-dim: #d3dcd6;
      --surface-bright: #f3fbf5;
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
      --nav-shadow: 0 12px 32px -4px rgba(32, 40, 36, 0.06), 0 4px 12px -2px rgba(32, 40, 36, 0.03);
      --card-shadow: 0 2px 12px -2px rgba(32, 40, 36, 0.04), 0 1px 3px 0 rgba(32, 40, 36, 0.02);
    }

    html.dark {
      --surface: #171B19;
      --surface-dim: #101412;
      --surface-bright: #272D29;
      --surface-container-lowest: #202522;
      --surface-container-low: #202522;
      --surface-container: #202522;
      --surface-container-subtle: #272D29;
      --surface-container-high: #272D29;
      --surface-container-highest: #343C37;
      --on-surface: #E9EEE9;
      --on-surface-variant: #AAB5AE;
      --on-surface-muted: #7D8982;
      --inverse-surface: #E9EEE9;
      --inverse-on-surface: #171B19;
      --outline: #343C37;
      --outline-variant: #272D29;
      --surface-tint: #75B7A1;
      --primary: #75B7A1;
      --on-primary: #101412;
      --primary-container: #75B7A1;
      --on-primary-container: #101412;
      --inverse-primary: #1c6150;
      --secondary: #D09A8C;
      --on-secondary: #171B19;
      --secondary-container: #3A2B28;
      --on-secondary-container: #D09A8C;
      --secondary-fixed: #3A2B28;
      --secondary-fixed-dim: #4B271D;
      --on-secondary-fixed: #D09A8C;
      --tertiary: #AAB5AE;
      --tertiary-container: #272D29;
      --tertiary-fixed: #272D29;
      --tertiary-fixed-dim: #202522;
      --error: #FFB4AB;
      --on-error: #690005;
      --error-container: #93000A;
      --on-error-container: #FFDAD6;
      --primary-fixed: #263D35;
      --primary-fixed-dim: #75B7A1;
      --on-primary-fixed: #E9EEE9;
      --surface-variant: #272D29;
      --nav-shadow: 0 12px 32px -4px rgba(0, 0, 0, 0.35), 0 4px 12px -2px rgba(0, 0, 0, 0.2);
      --card-shadow: 0 2px 12px -2px rgba(0, 0, 0, 0.25), 0 1px 3px 0 rgba(0, 0, 0, 0.15);
    }

    @layer base {
      html, body {
        width: 100vw;
        margin: 0;
        padding: 0;
        background-color: var(--surface);
        color: var(--on-surface);
        font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
        overscroll-behavior: none;
      }
      .pb-safe { padding-bottom: max(env(safe-area-inset-bottom, 0px), 12px); }
      .pt-safe { padding-top: max(env(safe-area-inset-top, 0px), 8px); }
      main > :first-child { margin-top: 0 !important; }
      main > :last-child { margin-bottom: 0 !important; }
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
      opacity: 0;
      transition: opacity 0.18s ease-in-out;
    }
    .page-tab.active {
      display: flex;
      opacity: 1;
    }

    /* Modal dialog styling */
    dialog::backdrop {
      background: rgba(16, 20, 18, 0.45);
      backdrop-filter: blur(4px);
    }
  </style>
  <script id="tailwind-config">
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "surface": "var(--surface)",
            "background": "var(--surface)",
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
            "outline": "var(--outline)",
            "outline-variant": "var(--outline-variant)",
            "surface-tint": "var(--surface-tint)",
            "primary": "var(--primary)",
            "primary-container": "var(--primary-container)",
            "on-primary": "var(--on-primary)",
            "on-primary-container": "var(--on-primary-container)",
            "inverse-primary": "var(--inverse-primary)",
            "secondary": "var(--secondary)",
            "secondary-container": "var(--secondary-container)",
            "on-secondary": "var(--on-secondary)",
            "on-secondary-container": "var(--on-secondary-container)",
            "secondary-fixed": "var(--secondary-fixed)",
            "secondary-fixed-dim": "var(--secondary-fixed-dim)",
            "on-secondary-fixed": "var(--on-secondary-fixed)",
            "primary-fixed": "var(--primary-fixed)",
            "primary-fixed-dim": "var(--primary-fixed-dim)",
            "on-primary-fixed": "var(--on-primary-fixed)",
            "tertiary": "var(--tertiary)",
            "tertiary-container": "var(--tertiary-container)",
            "tertiary-fixed": "var(--tertiary-fixed)",
            "tertiary-fixed-dim": "var(--tertiary-fixed-dim)",
            "error-container": "var(--error-container)",
            "on-error-container": "var(--on-error-container)",
            "error": "var(--error)",
            "on-error": "var(--on-error)",
            "surface-variant": "var(--surface-variant)",
            "inverse-surface": "var(--inverse-surface)",
            "inverse-on-surface": "var(--inverse-on-surface)"
          },
          borderRadius: {
            "DEFAULT": "0.25rem",
            "sm": "0.25rem",
            "md": "0.5rem",
            "lg": "0.75rem",
            "xl": "1rem",
            "2xl": "1.25rem",
            "full": "9999px"
          },
          spacing: {
            "space-xs": "0.5rem",
            "space-sm": "0.75rem",
            "space-md": "1rem",
            "space-lg": "1.25rem",
            "space-xl": "1.5rem",
            "space-2xl": "2rem",
            "margin": "1.25rem",
            "gutter": "1.25rem"
          },
          fontFamily: {
            "body-sm": ["Plus Jakarta Sans", "sans-serif"],
            "label-lg": ["Plus Jakarta Sans", "sans-serif"],
            "display-lg": ["Plus Jakarta Sans", "sans-serif"],
            "label-md": ["Plus Jakarta Sans", "sans-serif"],
            "body-lg": ["Plus Jakarta Sans", "sans-serif"],
            "display-lg-mobile": ["Plus Jakarta Sans", "sans-serif"],
            "headline-md": ["Plus Jakarta Sans", "sans-serif"],
            "headline-sm": ["Plus Jakarta Sans", "sans-serif"],
            "label-sm": ["Plus Jakarta Sans", "sans-serif"],
            "headline-lg": ["Plus Jakarta Sans", "sans-serif"],
            "body-md": ["Plus Jakarta Sans", "sans-serif"]
          },
          fontSize: {
            "body-sm": ["13px", { "lineHeight": "20px", "fontWeight": "400" }],
            "label-lg": ["14px", { "lineHeight": "20px", "fontWeight": "500" }],
            "display-lg": ["40px", { "lineHeight": "52px", "letterSpacing": "-0.02em", "fontWeight": "600" }],
            "label-md": ["12px", { "lineHeight": "16px", "fontWeight": "500" }],
            "body-lg": ["17px", { "lineHeight": "28px", "fontWeight": "400" }],
            "display-lg-mobile": ["30px", { "lineHeight": "38px", "letterSpacing": "-0.015em", "fontWeight": "600" }],
            "headline-md": ["22px", { "lineHeight": "30px", "letterSpacing": "-0.01em", "fontWeight": "600" }],
            "headline-sm": ["18px", { "lineHeight": "26px", "letterSpacing": "-0.005em", "fontWeight": "600" }],
            "label-sm": ["11px", { "lineHeight": "14px", "letterSpacing": "0.02em", "fontWeight": "500" }],
            "headline-lg": ["28px", { "lineHeight": "38px", "letterSpacing": "-0.015em", "fontWeight": "600" }],
            "body-md": ["15px", { "lineHeight": "24px", "fontWeight": "400" }]
          }
        }
      }
    };
  </script>
</head>
<body class="bg-surface font-body-md text-body-md text-on-surface antialiased flex flex-col min-h-screen select-none">

  <!-- Fixed Top Header -->
  <header class="fixed top-0 w-full z-40 pt-safe bg-surface/85 backdrop-blur-xl border-b border-surface-container-high/40 shadow-[0_1px_8px_rgba(0,0,0,0.02)]">
    <div class="h-14 px-margin flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-primary text-[20px]" id="header-icon">spa</span>
        <h1 class="font-headline-sm text-headline-sm text-on-surface font-semibold tracking-tight" id="header-title">Home</h1>
      </div>
      <button onclick="switchTab('you')" class="w-8 h-8 rounded-full bg-primary flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" aria-label="Your space">
        <span class="material-symbols-outlined text-on-primary text-[18px]">person</span>
      </button>
    </div>
  </header>

  <!-- Main View Container -->
  <main class="flex flex-col relative w-full pt-16 pb-28 bg-surface px-margin min-h-screen">

    <!-- ========================================== -->
    <!-- 1. HOME TAB                                -->
    <!-- ========================================== -->
    <div id="tab-home" class="page-tab active flex-col w-full pb-6 space-y-space-xl">
      <!-- Warm Greeting -->
      <section class="flex flex-col pt-space-xs">
        <div class="flex items-center gap-space-xs mb-1.5">
          <span class="material-symbols-outlined text-primary text-[20px] material-symbols-filled">eco</span>
          <span class="font-label-md text-label-md text-primary font-medium tracking-wide" id="home-greeting-day">Today's sanctuary</span>
        </div>
        <h2 class="font-headline-lg text-headline-lg text-on-surface font-semibold tracking-tight" id="home-greeting-title">Good morning, Sarah</h2>
        <p class="font-body-md text-body-md text-on-surface-variant mt-0.5">A little space for today.</p>
      </section>

      <!-- Today's Quiet Snapshot -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between pb-space-sm mb-space-sm border-b border-surface-container-low">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-primary-container animate-pulse"></span>
            <span class="font-label-md text-label-md text-on-surface-variant font-medium">The morning flow</span>
          </div>
          <span class="font-label-sm text-label-sm text-outline" id="snapshot-updated-time">Updated moments ago</span>
        </div>
        <div class="grid grid-cols-3 gap-space-xs text-left">
          <div class="flex flex-col pr-1">
            <span class="font-headline-sm text-headline-sm text-on-surface font-semibold" id="stat-screen-time">2h 15m</span>
            <span class="font-label-sm text-label-sm text-on-surface-variant mt-0.5">screen time today</span>
          </div>
          <div class="flex flex-col px-1">
            <span class="font-headline-sm text-headline-sm text-primary font-semibold" id="stat-focused-time">50 min</span>
            <span class="font-label-sm text-label-sm text-on-surface-variant mt-0.5">focused time</span>
          </div>
          <div class="flex flex-col pl-1">
            <span class="font-headline-sm text-headline-sm text-secondary font-semibold" id="stat-habits-met">4 of 5</span>
            <span class="font-label-sm text-label-sm text-on-surface-variant mt-0.5">habits completed</span>
          </div>
        </div>
        <!-- Soft Breath Indicator Bar -->
        <div class="w-full bg-surface-container-low h-1.5 rounded-full mt-space-md overflow-hidden flex">
          <div class="bg-primary h-full rounded-full transition-all duration-700" id="snapshot-progress-bar" style="width: 72%;"></div>
        </div>
      </section>

      <!-- Focus Section -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-space-xl shadow-sm border border-surface-container-high/40 flex flex-col items-center text-center relative overflow-hidden" style="box-shadow: var(--card-shadow);">
        <div class="w-full flex items-center justify-between mb-space-md text-left">
          <div>
            <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Take a little time for yourself</h3>
            <p class="font-body-sm text-body-sm text-on-surface-variant">Quiet intervals for calm presence</p>
          </div>
          <div class="w-9 h-9 rounded-full bg-surface-container flex items-center justify-center shrink-0 text-primary">
            <span class="material-symbols-outlined text-[20px]">hourglass_empty</span>
          </div>
        </div>
        <!-- Timer Visual -->
        <div class="my-space-sm flex flex-col items-center">
          <div class="font-display-lg-mobile text-display-lg-mobile font-semibold text-on-surface tracking-tight" id="home-timer-display">
            25:00
          </div>
          <p class="font-label-md text-label-md text-on-surface-variant mt-1.5 flex items-center gap-1.5" id="home-timer-subtext">
            <span class="inline-block w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
            Gentle bell chime at completion
          </p>
        </div>
        <div class="w-full flex items-center justify-center gap-space-sm pt-space-xs">
          <button class="w-full max-w-[260px] py-3 px-6 rounded-xl bg-primary text-on-primary font-label-lg text-label-lg font-medium shadow-sm active:scale-[0.98] transition-all flex items-center justify-center gap-2 cursor-pointer hover:opacity-95" id="home-timer-btn" onclick="toggleHomeTimer()">
            <span class="material-symbols-outlined text-[20px]">play_arrow</span>
            <span id="home-timer-btn-text">Start focus</span>
          </button>
        </div>
      </section>

      <!-- Tasks Section: A few things for today -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between mb-space-sm">
          <div class="flex flex-col">
            <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">A few things for today</h3>
            <span class="font-label-sm text-label-sm text-on-surface-variant">Unrushed steps to take</span>
          </div>
          <span class="font-label-sm text-label-sm text-primary font-medium bg-surface-container-low px-2.5 py-1 rounded-full" id="tasks-done-badge">1 done</span>
        </div>
        <div class="flex flex-col divide-y-0 space-y-space-xs mt-1" id="home-tasks-list">
          <!-- Dynamically Populated Tasks -->
        </div>
        <!-- Soft Action Prompt -->
        <button onclick="promptAddTask()" class="mt-space-sm w-full py-2.5 px-space-sm rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface-variant font-label-md text-label-md flex items-center justify-center gap-1.5 transition-colors cursor-pointer active:scale-[0.99]">
          <span class="material-symbols-outlined text-[18px] text-primary">add</span>
          <span>Add a gentle note</span>
        </button>
      </section>

      <!-- Reading Section: Currently Reading -->
      <section class="w-full bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col cursor-pointer active:scale-[0.99] transition-transform" onclick="openReadingModal()" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between mb-space-sm">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-secondary text-[18px]">menu_book</span>
            <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Currently reading</h3>
          </div>
          <span class="font-label-sm text-label-sm text-secondary font-medium">Kindle sync</span>
        </div>
        <div class="flex items-center gap-space-md">
          <div class="w-14 h-20 rounded-lg bg-surface-container overflow-hidden shrink-0 shadow-sm relative">
            <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAC8ZEhBwRLi2Zw1vYfkmOLOq8XGczjyeYaKmsgpaG8K8zDX67k3LhXl1KV2qYTw8FokeGfo1CYRF4XW89O0ZpUq0ewq0zAxpKvWtePvSDH53KQeYTn86hGaK5zEW-UDtUgCFuwk3QSN_JoaVRvJe7XG4JCcGQHW6l6JymXW1YkEoXVCHFOKwoLR1kNdxTEyVlkAW8CRPZfP7wi9OP2QFkjBFk6wBr43bZvGFhWpUYSIT0yqRxFtPCZ" alt="Book cover"/>
          </div>
          <div class="flex flex-col flex-1 min-w-0">
            <h4 class="font-headline-sm text-headline-sm text-on-surface truncate font-semibold" id="home-book-title">Quiet: The Power of Introverts</h4>
            <p class="font-body-sm text-body-sm text-on-surface-variant mt-0.5" id="home-book-author">Susan Cain</p>
            <div class="mt-space-xs flex items-center justify-between text-on-surface-variant font-label-sm text-label-sm">
              <span id="home-book-chapter">Chapter 4</span>
              <span class="text-primary font-medium" id="home-book-percent">42% complete</span>
            </div>
            <!-- Mint Progress Line -->
            <div class="w-full bg-surface-container-high h-1.5 rounded-full mt-1.5 overflow-hidden">
              <div class="bg-primary-container h-full rounded-full transition-all duration-500" id="home-book-bar" style="width: 42%;"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- Atmospheric Daily Quote -->
      <section class="w-full rounded-2xl bg-surface-container-low p-space-lg flex items-start gap-space-sm border border-surface-container-high/30">
        <span class="material-symbols-outlined text-primary text-[22px] shrink-0 mt-0.5 material-symbols-filled">format_quote</span>
        <div class="flex flex-col">
          <p class="font-body-sm text-body-sm text-on-surface italic leading-relaxed" id="daily-quote-text">
            "Almost everything will work again if you unplug it for a few minutes, including you."
          </p>
          <span class="font-label-sm text-label-sm text-outline mt-1" id="daily-quote-author">— Anne Lamott</span>
        </div>
      </section>
    </div>

    <!-- ========================================== -->
    <!-- 2. TOOLS TAB                               -->
    <!-- ========================================== -->
    <div id="tab-tools" class="page-tab flex-col w-full pb-6 space-y-space-xl">
      <section class="flex flex-col gap-y-space-xs pt-space-xs">
        <div class="inline-flex items-center gap-space-xs text-primary font-label-md text-label-md">
          <span class="material-symbols-outlined text-[18px]">widgets</span>
          <span>Daily companions</span>
        </div>
        <h2 class="font-headline-lg text-headline-lg text-on-surface font-semibold tracking-tight">Your tools</h2>
        <p class="font-body-md text-body-md text-on-surface-variant">Small things that help you through the day.</p>
      </section>

      <!-- Reading Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between gap-space-md">
          <div class="flex items-center gap-space-md">
            <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[24px]">menu_book</span>
            </div>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Reading</h3>
              <p class="font-body-sm text-body-sm text-on-surface-variant">Keep track of what you're reading.</p>
            </div>
          </div>
          <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-primary font-label-sm text-label-sm font-medium">Active</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-space-sm flex flex-col gap-y-space-xs">
          <div class="flex items-center justify-between text-on-surface-variant font-label-sm text-label-sm">
            <span class="truncate max-w-[190px] font-medium text-on-surface">The Summer Book • Tove Jansson</span>
            <span class="text-primary font-semibold">68%</span>
          </div>
          <div class="w-full bg-surface-variant rounded-full h-1.5 overflow-hidden">
            <div class="bg-primary h-full rounded-full transition-all duration-500" style="width: 68%;"></div>
          </div>
        </div>
        <div class="flex items-center justify-between pt-space-xs">
          <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px] text-tertiary">bookmark_added</span>
            18 pages left today
          </span>
          <button onclick="openReadingModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- Habits Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between gap-space-md">
          <div class="flex items-center gap-space-md">
            <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[24px]">cycle</span>
            </div>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Habits</h3>
              <p class="font-body-sm text-body-sm text-on-surface-variant">Notice the things you keep coming back to.</p>
            </div>
          </div>
          <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-secondary font-label-sm text-label-sm font-medium" id="tools-habits-streak">5 day streak</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
          <div class="flex flex-col">
            <span class="font-label-sm text-label-sm text-on-surface font-medium">Gentle cadence</span>
            <span class="font-body-sm text-body-sm text-on-surface-variant" id="tools-habits-cadence">4 of 5 gentle rituals met</span>
          </div>
          <div class="flex items-center gap-1.5" id="tools-habits-dots">
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary">
              <span class="material-symbols-outlined text-[13px]">check</span>
            </span>
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary">
              <span class="material-symbols-outlined text-[13px]">check</span>
            </span>
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary">
              <span class="material-symbols-outlined text-[13px]">check</span>
            </span>
            <span class="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-on-primary">
              <span class="material-symbols-outlined text-[13px]">check</span>
            </span>
            <span class="w-5 h-5 rounded-full bg-surface-container-high"></span>
          </div>
        </div>
        <div class="flex items-center justify-between pt-space-xs">
          <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px] text-tertiary">wb_sunny</span>
            Morning rituals in rhythm
          </span>
          <button onclick="openHabitsModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- Focus Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between gap-space-md">
          <div class="flex items-center gap-space-md">
            <div class="w-12 h-12 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[24px]">hourglass_top</span>
            </div>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Focus</h3>
              <p class="font-body-sm text-body-sm text-on-surface-variant">Create a little uninterrupted time.</p>
            </div>
          </div>
          <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-label-sm text-label-sm font-medium">Quiet space</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
          <div class="flex items-center gap-space-sm">
            <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-secondary">
              <span class="material-symbols-outlined text-[18px]">nature</span>
            </div>
            <div class="flex flex-col">
              <span class="font-label-sm text-label-sm text-on-surface font-medium">Forest soundscape</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Default 25 minutes</span>
            </div>
          </div>
          <div class="font-headline-sm text-headline-sm text-secondary tracking-tight font-semibold">25:00</div>
        </div>
        <div class="flex items-center justify-between pt-space-xs">
          <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px] text-tertiary">check_circle</span>
            2 deep sessions logged
          </span>
          <button onclick="switchTab('home')" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- Gita Wisdom Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between gap-space-md">
          <div class="flex items-center gap-space-md">
            <div class="w-12 h-12 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[24px]">bookmark_heart</span>
            </div>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Gita Wisdom</h3>
              <p class="font-body-sm text-body-sm text-on-surface-variant">Read, reflect, and return to a verse.</p>
            </div>
          </div>
          <span class="px-space-xs py-0.5 rounded-full bg-secondary-fixed text-on-secondary-fixed font-label-sm text-label-sm font-medium">Chapter 2</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-space-sm flex flex-col gap-y-1 relative overflow-hidden">
          <div class="absolute -right-2 -bottom-3 text-surface-variant opacity-40 select-none pointer-events-none">
            <span class="material-symbols-outlined text-[54px]">format_quote</span>
          </div>
          <span class="font-label-sm text-label-sm text-secondary font-medium">Verse 2.47 • Nishkama Karma</span>
          <p class="font-body-sm text-body-sm text-on-surface-variant line-clamp-2 italic pr-space-md">
            "You have a right to your actions, but never to your action's fruits..."
          </p>
        </div>
        <div class="flex items-center justify-between pt-space-xs">
          <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px] text-tertiary">edit_note</span>
            1 saved reflection
          </span>
          <button onclick="openGitaModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- Digital Stillness (Screen Time) Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between gap-space-md">
          <div class="flex items-center gap-space-md">
            <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[24px]">hourglass_bottom</span>
            </div>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Digital Stillness</h3>
              <p class="font-body-sm text-body-sm text-on-surface-variant">Mindful screen boundaries and app rhythm.</p>
            </div>
          </div>
          <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-primary font-label-sm text-label-sm font-medium">Monitored</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
          <div class="flex items-center gap-space-sm">
            <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-primary">
              <span class="material-symbols-outlined text-[18px]">smartphone</span>
            </div>
            <div class="flex flex-col">
              <span class="font-label-sm text-label-sm text-on-surface font-medium" id="tools-screen-time-label">2h 15m screen time</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">32 pickups today</span>
            </div>
          </div>
          <span class="font-label-sm text-label-sm text-primary font-semibold">Within limit</span>
        </div>
        <div class="flex items-center justify-between pt-space-xs">
          <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px] text-tertiary">trending_down</span>
            18% less than yesterday
          </span>
          <button onclick="openScreenTimeModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
          </button>
        </div>
      </article>

      <!-- Journal Vault Tool Card -->
      <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
        <div class="flex items-start justify-between gap-space-md">
          <div class="flex items-center gap-space-md">
            <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[24px]">folder_special</span>
            </div>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Journal Vault</h3>
              <p class="font-body-sm text-body-sm text-on-surface-variant">Offline backup and export of your reflections.</p>
            </div>
          </div>
          <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-primary font-label-sm text-label-sm font-medium">Secured</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
          <div class="flex items-center gap-space-sm">
            <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-primary">
              <span class="material-symbols-outlined text-[18px]">verified_user</span>
            </div>
            <div class="flex flex-col">
              <span class="font-label-sm text-label-sm text-on-surface font-medium">On-device copy</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Export ready: Plain text • Markdown</span>
            </div>
          </div>
          <span class="material-symbols-outlined text-primary text-[20px]">cloud_done</span>
        </div>
        <div class="flex items-center justify-between pt-space-xs">
          <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px] text-tertiary">schedule</span>
            Backed up today
          </span>
          <button onclick="switchTab('you')" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
            <span>Manage</span>
            <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
          </button>
        </div>
      </article>

      <div class="py-space-md text-center flex flex-col items-center justify-center gap-1 text-on-surface-variant">
        <div class="w-6 h-1 rounded-full bg-surface-container-highest"></div>
        <span class="font-body-sm text-body-sm pt-space-xs">Each tool operates privately and quietly on your device.</span>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 3. JOURNAL TAB                             -->
    <!-- ========================================== -->
    <div id="tab-journal" class="page-tab flex-col w-full max-w-[680px] mx-auto pb-12">
      <!-- Sanctuary Header -->
      <header class="flex items-end justify-between pt-2 pb-space-lg">
        <div class="flex flex-col gap-1">
          <h2 class="font-display-lg-mobile text-display-lg-mobile text-on-surface tracking-tight font-semibold">Journal</h2>
          <p class="font-body-md text-body-md text-on-surface-variant">Somewhere to put your thoughts.</p>
        </div>
        <button onclick="openWriteModal()" class="flex items-center gap-space-xs px-space-md py-2.5 bg-primary text-on-primary rounded-full shadow-[0_4px_16px_rgba(28,97,80,0.18)] active:scale-[0.98] transition-transform cursor-pointer hover:opacity-95" id="write-button">
          <span class="material-symbols-outlined text-[18px]">stylus</span>
          <span class="font-label-lg text-label-lg font-medium">Write</span>
        </button>
      </header>

      <!-- Tactile Search & Filters -->
      <section class="flex flex-col gap-space-sm mb-space-xl">
        <div class="relative w-full flex items-center">
          <span class="material-symbols-outlined absolute left-3.5 text-outline text-[20px] pointer-events-none select-none">search</span>
          <input class="w-full bg-surface-container-lowest text-on-surface placeholder:text-outline pl-11 pr-4 py-3 rounded-xl shadow-sm border border-surface-container-high/40 outline-none focus:bg-surface-container-low transition-colors font-body-md text-body-md" id="journal-search" placeholder="Search your thoughts..." type="text" oninput="applyJournalFilters()"/>
        </div>
        <!-- Soft Horizontal Tag Rail -->
        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar py-1 -mx-margin px-margin">
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-primary text-on-primary shrink-0 transition-colors shadow-sm cursor-pointer" data-tag="all" onclick="selectJournalTag('all', this)">
            All
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="gratitude" onclick="selectJournalTag('gratitude', this)">
            #gratitude
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="focus" onclick="selectJournalTag('focus', this)">
            #focus
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="calm" onclick="selectJournalTag('calm', this)">
            #calm
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="morning" onclick="selectJournalTag('morning', this)">
            #morning
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="reading" onclick="selectJournalTag('reading', this)">
            #reading
          </button>
        </div>
      </section>

      <!-- Lined Linen Notebook Canvas -->
      <div class="relative w-full rounded-2xl bg-surface-container-lowest p-6 shadow-sm border border-surface-container-high/40" style="box-shadow: var(--card-shadow);">
        <!-- Faint Notebook Linen Accent Bookmark -->
        <div class="absolute -top-1.5 left-10 w-6 h-3 bg-secondary-fixed rounded-b-sm shadow-sm opacity-80 pointer-events-none"></div>

        <div class="flex flex-col gap-space-2xl" id="journal-entries-container">
          <!-- Populated dynamically with Today, Yesterday, earlier thoughts -->
        </div>

        <!-- Reflection Prompt Card -->
        <div class="mt-space-2xl pt-space-lg flex flex-col items-center text-center bg-surface-container-low rounded-xl p-space-lg border border-surface-container-high/30">
          <div class="w-8 h-8 rounded-full bg-surface-container flex items-center justify-center text-secondary mb-2">
            <span class="material-symbols-outlined text-[18px]">nature</span>
          </div>
          <p class="font-body-md text-body-md text-on-surface font-medium">How was your evening pause?</p>
          <p class="font-body-sm text-body-sm text-on-surface-variant mt-0.5">Capture a gentle thought before the sun sets.</p>
          <button onclick="openWriteModal()" class="mt-3 px-4 py-1.5 bg-surface-container-lowest text-primary rounded-full font-label-md text-label-md shadow-sm hover:bg-surface-bright transition-colors cursor-pointer active:scale-95 font-medium border border-surface-container-high/40">
            Reflect now
          </button>
        </div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 4. DAY TAB                                 -->
    <!-- ========================================== -->
    <div id="tab-day" class="page-tab flex-col w-full max-w-[680px] mx-auto pb-6 space-y-space-lg">
      <!-- Date Navigation Bar -->
      <div class="flex items-center justify-between px-space-xs py-space-xs rounded-full bg-surface-container-low shadow-sm border border-surface-container-high/40">
        <button onclick="changeDayOffset(-1)" aria-label="Previous day" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container transition-colors active:scale-95 cursor-pointer" type="button">
          <span class="material-symbols-outlined text-[20px]">chevron_left</span>
        </button>
        <div class="flex items-center gap-space-xs cursor-pointer select-none" onclick="resetToToday()">
          <span class="material-symbols-outlined text-primary text-[18px]">calendar_today</span>
          <span class="font-headline-sm text-headline-sm text-on-surface font-semibold" id="day-nav-label">Today</span>
        </div>
        <button onclick="changeDayOffset(1)" aria-label="Next day" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container transition-colors active:scale-95 cursor-pointer" type="button">
          <span class="material-symbols-outlined text-[20px]">chevron_right</span>
        </button>
      </div>

      <!-- Page Header Brief -->
      <div class="px-space-xs pt-space-xs">
        <h2 class="font-display-lg-mobile text-display-lg-mobile text-on-surface font-semibold tracking-tight">Your day</h2>
        <p class="font-body-md text-body-md text-on-surface-variant mt-1">A quiet look at how your time unfolded.</p>
      </div>

      <!-- Today Summary Card -->
      <div class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-space-lg" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between">
          <span class="font-label-lg text-label-lg text-primary font-semibold tracking-wide" id="day-card-label">Today</span>
          <span class="font-label-sm text-label-sm text-on-surface-variant bg-surface-container-low px-space-sm py-1 rounded-full font-medium">Gentle rhythm</span>
        </div>
        <!-- Metrics Row -->
        <div class="grid grid-cols-3 gap-space-xs text-center py-space-xs">
          <div class="flex flex-col items-center justify-center py-space-xs">
            <span class="font-headline-md text-headline-md text-on-surface font-semibold text-center" id="day-screen-time">2h 15m</span>
            <span class="font-label-md text-label-md text-on-surface-variant mt-0.5 text-center">screen time</span>
          </div>
          <div class="flex flex-col items-center justify-center bg-surface-container-low/60 rounded-xl py-space-xs px-space-xs">
            <span class="font-headline-md text-headline-md text-primary font-semibold text-center" id="day-focused-time">50 min</span>
            <span class="font-label-md text-label-md text-on-surface-variant mt-0.5 text-center">focused</span>
          </div>
          <div class="flex flex-col items-center justify-center py-space-xs">
            <span class="font-headline-md text-headline-md text-secondary font-semibold text-center" id="day-habits-count">4</span>
            <span class="font-label-md text-label-md text-on-surface-variant mt-0.5 text-center">habits met</span>
          </div>
        </div>
        <!-- Serene Pace Visual Curve (Custom SVG) -->
        <div class="w-full pt-space-xs flex flex-col gap-2">
          <div class="flex items-center justify-between font-label-sm text-label-sm text-on-surface-variant px-1 font-medium">
            <span class="text-left">Dawn</span>
            <span class="text-center">Noon</span>
            <span class="text-right">Dusk</span>
          </div>
          <div class="relative w-full h-16 rounded-xl bg-surface-container-low/50 p-2 flex items-center justify-center overflow-hidden border border-surface-container-high/20">
            <svg class="w-full h-full stroke-current" preserveAspectRatio="none" viewBox="0 0 320 48">
              <defs>
                <linearGradient id="curveGradient" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stop-color="var(--primary)" stop-opacity="0.3"></stop>
                  <stop offset="100%" stop-color="var(--primary)" stop-opacity="0"></stop>
                </linearGradient>
              </defs>
              <path d="M0,38 C40,36 70,12 110,14 C150,16 170,36 210,34 C250,32 280,18 320,24 L320,48 L0,48 Z" fill="url(#curveGradient)"></path>
              <path d="M0,38 C40,36 70,12 110,14 C150,16 170,36 210,34 C250,32 280,18 320,24" fill="none" stroke="var(--primary)" stroke-linecap="round" stroke-width="2.5"></path>
              <circle cx="110" cy="14" r="3.5" fill="var(--surface-container-lowest)" stroke="var(--primary)" stroke-width="2"></circle>
              <circle cx="210" cy="34" r="3" fill="var(--surface-container-lowest)" stroke="var(--secondary)" stroke-width="1.5"></circle>
            </svg>
          </div>
        </div>
      </div>

      <!-- Timeline Flow Section -->
      <div class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40" style="box-shadow: var(--card-shadow);">
        <div class="flex items-center justify-between pb-space-md border-b border-surface-container-low">
          <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Earlier</h3>
          <span class="font-label-sm text-label-sm text-primary font-medium">Chronological</span>
        </div>
        <div class="relative pl-6 space-y-space-lg pt-4" id="day-timeline-events">
          <!-- Vertical spine line -->
          <div class="absolute left-2.5 top-5 bottom-3 w-[1.5px] bg-surface-container-high"></div>

          <!-- Item 1: Morning Light -->
          <div class="relative flex items-start gap-space-md group">
            <div class="absolute -left-6 top-1 w-5 h-5 rounded-full bg-surface-container-lowest flex items-center justify-center">
              <div class="w-2.5 h-2.5 rounded-full bg-primary ring-4 ring-surface-container-lowest"></div>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-baseline justify-between gap-space-xs">
                <h4 class="font-body-md text-body-md text-on-surface font-medium truncate">Morning light completed</h4>
                <span class="font-body-sm text-body-sm text-on-surface-variant shrink-0">09:30 AM</span>
              </div>
              <div class="flex items-center gap-space-xs mt-1">
                <span class="material-symbols-outlined text-[15px] text-primary">nature_people</span>
                <p class="font-body-sm text-body-sm text-on-surface-variant">Movement · 15m outdoor walk</p>
              </div>
            </div>
          </div>

          <!-- Item 2: Focus Session -->
          <div class="relative flex items-start gap-space-md group">
            <div class="absolute -left-6 top-1 w-5 h-5 rounded-full bg-surface-container-lowest flex items-center justify-center">
              <div class="w-2.5 h-2.5 rounded-full bg-primary-container ring-4 ring-surface-container-lowest"></div>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-baseline justify-between gap-space-xs">
                <h4 class="font-body-md text-body-md text-on-surface font-medium truncate">25 minutes of focus completed</h4>
                <span class="font-body-sm text-body-sm text-on-surface-variant shrink-0">09:05 AM</span>
              </div>
              <div class="flex items-center gap-space-xs mt-1">
                <span class="material-symbols-outlined text-[15px] text-primary">target</span>
                <p class="font-body-sm text-body-sm text-on-surface-variant">Focus · Quiet flow</p>
              </div>
            </div>
          </div>

          <!-- Item 3: Reading Session -->
          <div class="relative flex items-start gap-space-md group">
            <div class="absolute -left-6 top-1 w-5 h-5 rounded-full bg-surface-container-lowest flex items-center justify-center">
              <div class="w-2.5 h-2.5 rounded-full bg-secondary ring-4 ring-surface-container-lowest"></div>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-baseline justify-between gap-space-xs">
                <h4 class="font-body-md text-body-md text-on-surface font-medium truncate">Reading session</h4>
                <span class="font-body-sm text-body-sm text-on-surface-variant shrink-0">08:35 AM</span>
              </div>
              <div class="flex items-center gap-space-xs mt-1">
                <span class="material-symbols-outlined text-[15px] text-secondary">menu_book</span>
                <p class="font-body-sm text-body-sm text-on-surface-variant">Reading · Quiet: The Power of Introverts (30m)</p>
              </div>
            </div>
          </div>

          <!-- Item 4: Rest / Wake up -->
          <div class="relative flex items-start gap-space-md group">
            <div class="absolute -left-6 top-1 w-5 h-5 rounded-full bg-surface-container-lowest flex items-center justify-center">
              <div class="w-2.5 h-2.5 rounded-full bg-tertiary-container ring-4 ring-surface-container-lowest"></div>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-baseline justify-between gap-space-xs">
                <h4 class="font-body-md text-body-md text-on-surface font-medium truncate">Woke up rested</h4>
                <span class="font-body-sm text-body-sm text-on-surface-variant shrink-0">08:00 AM</span>
              </div>
              <div class="flex items-center gap-space-xs mt-1">
                <span class="material-symbols-outlined text-[15px] text-tertiary">bedtime</span>
                <p class="font-body-sm text-body-sm text-on-surface-variant">Rest · 7h 48m gentle sleep</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Soft Add Note / Interaction Trigger -->
      <div class="flex justify-center pt-space-xs">
        <button onclick="openWriteModal()" class="inline-flex items-center gap-space-xs px-space-lg py-space-sm bg-surface-container-low text-primary rounded-full font-label-lg text-label-lg hover:bg-surface-container transition-all active:scale-95 font-medium cursor-pointer border border-surface-container-high/30" type="button">
          <span class="material-symbols-outlined text-[18px]">add</span>
          <span>Add quiet reflection</span>
        </button>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 5. YOU TAB (PROFILE & SETTINGS)            -->
    <!-- ========================================== -->
    <div id="tab-you" class="page-tab flex-col w-full pb-10 space-y-space-xl">
      <!-- Introductory Framing -->
      <div class="flex flex-col space-y-space-xs pt-space-xs">
        <div class="inline-flex items-center gap-space-xs text-primary font-label-md font-medium">
          <span class="material-symbols-outlined text-[16px]">nature_people</span>
          <span>Sanctuary settings</span>
        </div>
        <h2 class="font-headline-md text-headline-md text-on-surface tracking-tight font-semibold">Your space</h2>
        <p class="font-body-md text-body-md text-on-surface-variant">Make V07 feel like yours.</p>
      </div>

      <!-- Section 1: Your Profile -->
      <section class="flex flex-col space-y-space-sm">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Your profile</h3>
          <span class="font-label-sm text-label-sm text-primary flex items-center gap-1 font-medium">
            <span class="w-1.5 h-1.5 rounded-full bg-primary inline-block"></span>
            Encrypted & private
          </span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-space-md" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center gap-space-md">
            <div class="relative shrink-0">
              <div class="w-16 h-16 rounded-full bg-surface-container flex items-center justify-center text-primary shadow-sm overflow-hidden">
                <span class="material-symbols-outlined text-[36px]">face</span>
              </div>
              <div class="absolute bottom-0 right-0 w-5 h-5 rounded-full bg-primary text-on-primary flex items-center justify-center shadow-sm">
                <span class="material-symbols-outlined text-[12px]">favorite</span>
              </div>
            </div>
            <div class="flex flex-col min-w-0 flex-1">
              <div class="flex items-center gap-space-xs">
                <span class="font-headline-sm text-headline-sm text-on-surface truncate font-semibold" id="profile-name">Sarah Jenkins</span>
                <span class="material-symbols-outlined text-primary text-[18px] material-symbols-filled">verified</span>
              </div>
              <p class="font-body-sm text-body-sm text-on-surface-variant truncate" id="profile-email">sarah@example.com</p>
              <div class="mt-1 flex items-center gap-1.5 text-tertiary font-label-sm text-label-sm">
                <span class="material-symbols-outlined text-[13px] text-primary">cloud_done</span>
                <span class="truncate">Local journal active · Backed up securely</span>
              </div>
            </div>
          </div>
          <div class="pt-space-xs flex items-center justify-between gap-space-sm">
            <button onclick="promptEditProfile()" class="w-full py-2.5 px-space-md rounded-full bg-surface-container-low text-primary hover:bg-surface-container text-label-md font-label-md font-medium transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 cursor-pointer" type="button">
              <span class="material-symbols-outlined text-[16px]">edit</span>
              <span>Edit profile</span>
            </button>
            <button onclick="shareSanctuaryStatus()" class="w-full py-2.5 px-space-md rounded-full bg-surface-container-low text-on-surface-variant hover:bg-surface-container text-label-md font-label-md font-medium transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 cursor-pointer" type="button">
              <span class="material-symbols-outlined text-[16px]">share</span>
              <span>Share status</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 2: Daily Rhythm & Notifications -->
      <section class="flex flex-col space-y-space-sm">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Daily rhythm & notifications</h3>
          <span class="material-symbols-outlined text-tertiary text-[18px]">bedtime</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl shadow-sm border border-surface-container-high/40 p-space-md flex flex-col space-y-space-md" style="box-shadow: var(--card-shadow);">
          <!-- Item 1: Morning prompt -->
          <div class="flex items-center justify-between gap-space-md py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">wb_sunny</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-label-lg text-label-lg text-on-surface font-medium">Morning prompt</span>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Gentle reminder at 8:30 AM to reflect on quiet intent</span>
              </div>
            </div>
            <button aria-checked="true" class="toggle-switch shrink-0 w-12 h-7 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleSwitch(this, 'morning_prompt')" role="switch" type="button">
              <div class="w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[12px] text-primary">check</span>
              </div>
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>

          <!-- Item 2: Focus chime -->
          <div class="flex items-center justify-between gap-space-md py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-primary-fixed text-on-primary-fixed flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">music_note</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-label-lg text-label-lg text-on-surface font-medium">Focus chime & vibration</span>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Soft wooden singing bowl chime when writing sessions start</span>
              </div>
            </div>
            <button aria-checked="true" class="toggle-switch shrink-0 w-12 h-7 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleSwitch(this, 'focus_chime')" role="switch" type="button">
              <div class="w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[12px] text-primary">check</span>
              </div>
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>

          <!-- Item 3: Evening pause notification -->
          <div class="flex items-center justify-between gap-space-md py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-surface-container text-tertiary flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">nights_stay</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-label-lg text-label-lg text-on-surface font-medium">Evening pause notification</span>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Warm reminder at 9:15 PM to release today's thoughts</span>
              </div>
            </div>
            <button aria-checked="false" class="toggle-switch shrink-0 w-12 h-7 rounded-full bg-surface-container-high p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleSwitch(this, 'evening_pause')" role="switch" type="button">
              <div class="w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-0 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[12px] text-outline opacity-0">check</span>
              </div>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 3: Appearance & Theme -->
      <section class="flex flex-col space-y-space-sm">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Appearance & theme</h3>
          <span class="font-label-sm text-label-sm text-tertiary">Editorial palette</span>
        </div>
        <div class="grid grid-cols-2 gap-space-sm">
          <!-- Warm Paper (Light) Option -->
          <div class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 relative cursor-pointer flex flex-col justify-between transition-all group hover:bg-surface-bright" id="theme-card-light" onclick="setAppTheme('light')">
            <div class="flex items-center justify-between mb-space-sm">
              <div class="w-6 h-6 rounded-full bg-surface-container-low flex items-center justify-center">
                <span class="material-symbols-outlined text-primary text-[14px]">wb_sunny</span>
              </div>
              <div class="w-5 h-5 rounded-full bg-primary text-on-primary flex items-center justify-center" id="check-light">
                <span class="material-symbols-outlined text-[13px]">check</span>
              </div>
            </div>
            <div class="h-16 w-full rounded-xl bg-[#F3FBF5] p-2 flex flex-col justify-between mb-space-sm border border-[#E2EAE4]">
              <div class="flex gap-1.5 items-center">
                <div class="w-2.5 h-2.5 rounded-full bg-[#1C6150]"></div>
                <div class="w-12 h-1.5 rounded-full bg-[#BFC9C4]"></div>
              </div>
              <div class="space-y-1">
                <div class="w-full h-1.5 rounded-full bg-[#BFC9C4]"></div>
                <div class="w-3/4 h-1.5 rounded-full bg-[#BFC9C4]"></div>
              </div>
            </div>
            <div class="flex flex-col">
              <span class="font-label-md text-label-md text-on-surface font-semibold">Warm Paper</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Calm, natural daylight</span>
            </div>
          </div>

          <!-- Night Ink (Dark) Option -->
          <div class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 relative cursor-pointer flex flex-col justify-between transition-all group opacity-85 hover:opacity-100" id="theme-card-dark" onclick="setAppTheme('dark')">
            <div class="flex items-center justify-between mb-space-sm">
              <div class="w-6 h-6 rounded-full bg-surface-container flex items-center justify-center">
                <span class="material-symbols-outlined text-on-surface text-[14px]">dark_mode</span>
              </div>
              <div class="w-5 h-5 rounded-full bg-surface-container text-tertiary flex items-center justify-center opacity-0" id="check-dark">
                <span class="material-symbols-outlined text-[13px]">check</span>
              </div>
            </div>
            <div class="h-16 w-full rounded-xl bg-[#171B19] p-2 flex flex-col justify-between mb-space-sm border border-[#272D29]">
              <div class="flex gap-1.5 items-center">
                <div class="w-2.5 h-2.5 rounded-full bg-[#75B7A1]"></div>
                <div class="w-12 h-1.5 rounded-full bg-[#343C37]"></div>
              </div>
              <div class="space-y-1">
                <div class="w-full h-1.5 rounded-full bg-[#343C37]"></div>
                <div class="w-3/4 h-1.5 rounded-full bg-[#343C37]"></div>
              </div>
            </div>
            <div class="flex flex-col">
              <span class="font-label-md text-label-md text-on-surface font-semibold">Night Ink</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Deep charcoal resting</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Section 4: Storage & Vault -->
      <section class="flex flex-col space-y-space-sm">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Storage & vault</h3>
          <span class="font-label-sm text-label-sm text-primary flex items-center gap-1 font-medium">
            <span class="material-symbols-outlined text-[14px]">lock</span>
            Local sovereignty
          </span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col space-y-space-md" style="box-shadow: var(--card-shadow);">
          <div class="flex items-start gap-space-sm">
            <div class="w-10 h-10 rounded-full bg-primary-container text-on-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">folder_special</span>
            </div>
            <div class="flex flex-col min-w-0">
              <p class="font-label-lg text-label-lg text-on-surface font-semibold">Your words stay on your device.</p>
              <p class="font-body-sm text-body-sm text-on-surface-variant mt-0.5">V07 never parses, tracks, or shares your personal journal entries. Data is held in open formats you control completely.</p>
            </div>
          </div>
          <div class="bg-surface-container-low rounded-xl p-space-sm flex flex-col space-y-1">
            <span class="font-label-sm text-label-sm text-on-surface-variant font-medium">Vault directory path</span>
            <div class="flex items-center justify-between gap-space-xs overflow-hidden">
              <code class="font-mono text-[12px] text-primary truncate select-all" id="vault-path-text">/storage/emulated/0/V07Data</code>
              <button class="text-on-surface-variant hover:text-primary transition-colors p-1 rounded-md shrink-0 flex items-center cursor-pointer" onclick="copyVaultPath(this)" title="Copy path" type="button">
                <span class="material-symbols-outlined text-[18px]">content_copy</span>
              </button>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-space-sm pt-space-xs">
            <button onclick="pickVaultFolder()" class="py-2.5 px-space-sm rounded-full bg-surface-container text-on-surface text-label-md font-label-md hover:bg-surface-container-high transition-colors active:scale-[0.98] flex items-center justify-center gap-1.5 truncate font-medium cursor-pointer" type="button">
              <span class="material-symbols-outlined text-[16px]">folder_open</span>
              <span>Choose folder</span>
            </button>
            <button onclick="exportVaultBackup()" class="py-2.5 px-space-sm rounded-full bg-primary text-on-primary text-label-md font-label-md hover:opacity-95 transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 shadow-sm truncate font-medium cursor-pointer" type="button">
              <span class="material-symbols-outlined text-[16px]">cloud_download</span>
              <span>Export backup</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 5: Advanced -->
      <section class="flex flex-col space-y-space-sm pt-space-xs">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-tertiary font-medium">Advanced</h3>
          <span class="font-label-sm text-label-sm text-tertiary">Inspection & cache</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col space-y-space-sm" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between py-1">
            <div class="flex flex-col min-w-0">
              <span class="font-label-md text-label-md text-on-surface font-medium">Inspect local database</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Review indexed events and storage files</span>
            </div>
            <button onclick="inspectDatabase()" class="px-3 py-1.5 rounded-full bg-surface-container-low text-on-surface-variant font-label-sm text-label-sm hover:bg-surface-container transition-colors shrink-0 cursor-pointer font-medium" type="button">
              View raw
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>
          <div class="flex items-center justify-between py-1">
            <div class="flex flex-col min-w-0">
              <span class="font-label-md text-label-md text-on-surface font-medium">Diagnostics & logs</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Export sync timestamps and device logs</span>
            </div>
            <button onclick="downloadDiagnostics()" class="px-3 py-1.5 rounded-full bg-surface-container-low text-on-surface-variant font-label-sm text-label-sm hover:bg-surface-container transition-colors shrink-0 cursor-pointer font-medium" type="button">
              Download
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>
          <!-- Caution Zone -->
          <div class="pt-space-xs flex flex-col space-y-space-sm">
            <div class="flex items-center gap-1.5 text-secondary font-label-md font-medium">
              <span class="material-symbols-outlined text-[16px]">warning</span>
              <span>Caution zone</span>
            </div>
            <div class="flex flex-col gap-2">
              <button onclick="clearCacheAction()" class="w-full py-2.5 px-space-md rounded-full bg-error-container text-on-error-container text-label-md font-label-md hover:opacity-90 transition-all active:scale-[0.98] flex items-center justify-between cursor-pointer font-medium" type="button">
                <span class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-[16px]">cached</span>
                  <span>Clear local cache</span>
                </span>
                <span class="font-label-sm text-label-sm opacity-80">14.8 MB</span>
              </button>
              <button onclick="resetAppAction()" class="w-full py-2.5 px-space-md rounded-full bg-surface-container-low text-secondary text-label-md font-label-md hover:bg-error-container hover:text-on-error-container transition-all active:scale-[0.98] flex items-center justify-between cursor-pointer font-medium" type="button">
                <span class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-[16px]">delete_forever</span>
                  <span>Reset application</span>
                </span>
                <span class="font-label-sm text-label-sm">Erase setup</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- App Sign-off note -->
      <div class="flex flex-col items-center justify-center text-center space-y-1 pt-space-xs text-on-surface-variant">
        <div class="flex items-center gap-1 text-primary">
          <span class="material-symbols-outlined text-[14px]">spa</span>
          <span class="font-label-sm text-label-sm font-semibold">V07 Notebook</span>
        </div>
        <p class="font-body-sm text-body-sm text-tertiary">Crafted for mindful presence · Version 0.7.4 (Build 89)</p>
      </div>
    </div>
  </main>

  <!-- ========================================== -->
  <!-- FLOATING 5-NODE BOTTOM NAVIGATION DOCK     -->
  <!-- ========================================== -->
  <nav class="fixed bottom-0 w-full z-40 pb-safe pointer-events-none flex justify-center px-margin mb-space-xs">
    <div class="pointer-events-auto w-full max-w-[400px] h-16 rounded-full bg-surface-container-lowest shadow-[var(--nav-shadow)] px-space-xs flex items-center justify-between border border-surface-container-high/30" id="bottomNav">
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full transition-colors cursor-pointer nav-active" data-tab="home" href="javascript:void(0)" onclick="switchTab('home')">
        <span class="material-symbols-outlined text-[20px]">spa</span>
        <span class="font-label-sm text-label-sm">Home</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="tools" href="javascript:void(0)" onclick="switchTab('tools')">
        <span class="material-symbols-outlined text-[20px]">grid_view</span>
        <span class="font-label-sm text-label-sm">Tools</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="journal" href="javascript:void(0)" onclick="switchTab('journal')">
        <span class="material-symbols-outlined text-[20px]">edit_note</span>
        <span class="font-label-sm text-label-sm">Journal</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="day" href="javascript:void(0)" onclick="switchTab('day')">
        <span class="material-symbols-outlined text-[20px]">calendar_today</span>
        <span class="font-label-sm text-label-sm">Day</span>
      </a>
      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="you" href="javascript:void(0)" onclick="switchTab('you')">
        <span class="material-symbols-outlined text-[20px]">person_outline</span>
        <span class="font-label-sm text-label-sm">You</span>
      </a>
    </div>
  </nav>

  <!-- ========================================== -->
  <!-- MODALS & DETAIL DRAWERS                     -->
  <!-- ========================================== -->

  <!-- 1. Journal Write Modal -->
  <dialog id="write-modal" class="fixed inset-0 m-auto w-[92%] max-w-[520px] bg-surface-container-lowest rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.18)] p-6 outline-none border border-surface-container-high/50">
    <form class="flex flex-col gap-4" method="dialog" onsubmit="handleSaveThought(event)">
      <div class="flex items-center justify-between pb-2 border-b border-surface-container-low">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-primary text-[20px]">edit_note</span>
          <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">New Reflection</h3>
        </div>
        <button class="text-outline hover:text-on-surface transition-colors cursor-pointer p-1" onclick="document.getElementById('write-modal').close()" type="button">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>
      <textarea id="thought-input" class="w-full bg-surface-container-low p-3.5 rounded-xl text-on-surface placeholder:text-outline font-body-md text-body-md leading-relaxed resize-none outline-none focus:bg-surface-container transition-colors border border-surface-container-high/30" placeholder="What brought you ease or insight today?" rows="5" required></textarea>
      <div class="flex items-center justify-between pt-1">
        <div class="flex items-center gap-1.5 text-outline bg-surface-container-low px-2.5 py-1 rounded-lg border border-surface-container-high/20">
          <span class="material-symbols-outlined text-[16px]">label</span>
          <input id="thought-tag-input" class="w-28 bg-transparent text-label-md font-label-md text-on-surface placeholder:text-outline outline-none" placeholder="#calm #morning" type="text"/>
        </div>
        <div class="flex items-center gap-2">
          <button class="px-3.5 py-1.5 rounded-full font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" onclick="document.getElementById('write-modal').close()" type="button">
            Quietly discard
          </button>
          <button class="px-4 py-1.5 rounded-full font-label-md text-label-md bg-primary text-on-primary shadow-sm hover:opacity-95 transition-opacity cursor-pointer font-medium" type="submit">
            Preserve
          </button>
        </div>
      </div>
    </form>
  </dialog>

  <!-- 2. Habits Full Sheet Modal -->
  <dialog id="modal-habits" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[85vh] overflow-y-auto bg-surface rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.22)] p-0 outline-none border border-surface-container-high/50">
    <div class="p-4 border-b border-surface-container-high/40 flex items-center justify-between bg-surface-container-lowest sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-primary text-[22px]">cycle</span>
        <div>
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Habits</h2>
          <p class="font-label-sm text-label-sm text-on-surface-variant">Daily cadence and rituals</p>
        </div>
      </div>
      <button onclick="document.getElementById('modal-habits').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>
    <div class="p-4 space-y-4">
      <div class="flex items-center justify-between">
        <span class="font-label-lg text-label-lg font-semibold text-on-surface">Today's rituals</span>
        <span class="font-label-sm text-label-sm text-primary font-medium bg-surface-container-low px-2 py-0.5 rounded-full" id="habits-ratio-badge">3 of 4 done</span>
      </div>
      <div class="space-y-2.5" id="habits-modal-list">
        <!-- Habit item 1 -->
        <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs">
          <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
            <div class="habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px]">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title font-body-md text-on-surface line-through opacity-70 font-medium">Morning stillness</span>
              <span class="font-label-sm text-[11px] text-secondary font-medium">🔥 14 days streak</span>
            </div>
          </div>
        </div>
        <!-- Habit item 2 -->
        <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs">
          <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
            <div class="habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px]">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title font-body-md text-on-surface line-through opacity-70 font-medium">Hydrate with herbal infusion</span>
              <span class="font-label-sm text-[11px] text-secondary font-medium">🔥 21 days streak</span>
            </div>
          </div>
        </div>
        <!-- Habit item 3 -->
        <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs">
          <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
            <div class="habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px]">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title font-body-md text-on-surface line-through opacity-70 font-medium">Read 20 pages</span>
              <span class="font-label-sm text-[11px] text-secondary font-medium">🔥 6 days streak</span>
            </div>
          </div>
        </div>
        <!-- Habit item 4 -->
        <div class="habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs">
          <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
            <div class="habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title font-body-md text-on-surface font-medium">Evening reflection</span>
              <span class="font-label-sm text-[11px] text-on-surface-variant">Scheduled at 9:15 PM</span>
            </div>
          </div>
        </div>
      </div>
      <button onclick="promptAddHabit()" class="w-full py-3 rounded-xl border border-dashed border-outline-variant hover:border-primary text-primary font-label-md flex items-center justify-center gap-2 cursor-pointer active:scale-95 transition-colors">
        <span class="material-symbols-outlined text-[18px]">add</span>
        <span>Add ritual habit</span>
      </button>

      <!-- Monthly Consistency Heatmap Grid -->
      <div class="p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 space-y-2.5">
        <div class="flex items-center justify-between">
          <span class="font-label-md font-semibold text-on-surface">Consistency</span>
          <span class="font-label-sm text-primary font-medium">September · 86%</span>
        </div>
        <div class="grid grid-cols-7 gap-1 text-center font-label-sm text-[10px] text-on-surface-variant">
          <span>M</span><span>T</span><span>W</span><span>T</span><span>F</span><span>S</span><span>S</span>
        </div>
        <div class="grid grid-cols-7 gap-1 justify-items-center" id="heatmap-grid">
          <!-- Dynamically populated or static representation -->
        </div>
      </div>
    </div>
  </dialog>

  <!-- 3. Reading / Books Full Sheet Modal -->
  <dialog id="modal-reading" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[85vh] overflow-y-auto bg-surface rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.22)] p-0 outline-none border border-surface-container-high/50">
    <div class="p-4 border-b border-surface-container-high/40 flex items-center justify-between bg-surface-container-lowest sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-secondary text-[22px]">menu_book</span>
        <div>
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Your Books</h2>
          <p class="font-label-sm text-label-sm text-on-surface-variant">Keep track of what you're reading</p>
        </div>
      </div>
      <button onclick="document.getElementById('modal-reading').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>
    <div class="p-4 space-y-4">
      <!-- Active Book Card -->
      <div class="bg-surface-container-lowest rounded-xl p-3.5 shadow-sm border border-surface-container-high/40">
        <div class="flex gap-3.5 items-start">
          <div class="w-16 h-24 rounded-lg overflow-hidden shrink-0 shadow-sm bg-surface-container">
            <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAC8ZEhBwRLi2Zw1vYfkmOLOq8XGczjyeYaKmsgpaG8K8zDX67k3LhXl1KV2qYTw8FokeGfo1CYRF4XW89O0ZpUq0ewq0zAxpKvWtePvSDH53KQeYTn86hGaK5zEW-UDtUgCFuwk3QSN_JoaVRvJe7XG4JCcGQHW6l6JymXW1YkEoXVCHFOKwoLR1kNdxTEyVlkAW8CRPZfP7wi9OP2QFkjBFk6wBr43bZvGFhWpUYSIT0yqRxFtPCZ" alt="Book cover"/>
          </div>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-start justify-between gap-1">
              <h3 class="font-headline-sm text-[16px] text-on-surface font-semibold truncate leading-tight">Quiet: The Power of Introverts</h3>
              <span class="font-label-sm text-[12px] font-semibold text-primary shrink-0" id="reading-percent-label">42% complete</span>
            </div>
            <p class="font-body-sm text-[13px] text-on-surface-variant mt-0.5">Susan Cain</p>
            <p class="font-label-sm text-[11px] text-tertiary mt-1">Page 142 of 338</p>
            <div class="w-full h-1.5 bg-surface-container rounded-full overflow-hidden mt-2">
              <div class="h-full bg-primary rounded-full transition-all duration-300" id="reading-modal-bar" style="width: 42%;"></div>
            </div>
            <div class="flex items-center gap-2 mt-3">
              <button onclick="adjustBookPages(-5)" class="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg font-label-sm text-[12px] transition-colors active:scale-95 cursor-pointer">
                -5 p
              </button>
              <button onclick="adjustBookPages(10)" class="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg font-label-sm text-[12px] transition-colors active:scale-95 cursor-pointer">
                +10 p
              </button>
              <button onclick="promptUpdatePage()" class="ml-auto flex items-center gap-1 px-3 py-1 bg-primary text-on-primary rounded-lg font-label-sm text-[12px] transition-opacity hover:opacity-95 cursor-pointer">
                <span class="material-symbols-outlined text-[14px]">edit</span>
                <span>Update</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Shelf List -->
      <div class="space-y-2">
        <h4 class="font-label-lg font-semibold text-on-surface">Your shelf</h4>
        <div class="flex flex-col bg-surface-container-lowest rounded-xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden">
          <div class="p-3 flex items-center gap-3">
            <div class="w-10 h-14 rounded overflow-hidden shrink-0 bg-surface-container shadow-xs">
              <img class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCjgMVcp30Hjld1NvpUVWqCnq-nNf2PO3yMf0bejUgHXyOap3xKCRN8CUE56l0NgH_GeWH7LqOGZ5YIbPXHhbODPm6vlEDfh2uWG0WMm54z3Mt9EV_TRcup4rWKnFP99F0AKpVpCDTE8gW9D3oRzVSeyOZSowp6hqn_9piDrnmpdRRg5HI3GOmaoVmnDlIspa4a_4zft7R52t7nXbar9amI7IyJN-TKhqRko3BCqqN7HKq-Ya9pQCN_" alt="The Summer Book"/>
            </div>
            <div class="flex flex-col flex-1 min-w-0">
              <h5 class="font-body-md text-[14px] font-medium text-on-surface truncate">The Summer Book</h5>
              <p class="font-body-sm text-[12px] text-on-surface-variant">Tove Jansson</p>
              <span class="font-label-sm text-[11px] text-primary font-medium mt-0.5">68% · In progress</span>
            </div>
          </div>
          <div class="p-3 flex items-center gap-3">
            <div class="w-10 h-14 rounded overflow-hidden shrink-0 bg-surface-container shadow-xs flex items-center justify-center text-tertiary">
              <span class="material-symbols-outlined text-[20px]">book</span>
            </div>
            <div class="flex flex-col flex-1 min-w-0">
              <h5 class="font-body-md text-[14px] font-medium text-on-surface truncate">Atomic Habits</h5>
              <p class="font-body-sm text-[12px] text-on-surface-variant">James Clear</p>
              <span class="font-label-sm text-[11px] text-secondary font-medium mt-0.5">Completed last month</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </dialog>

  <!-- 4. Gita Wisdom Full Sheet Modal -->
  <dialog id="modal-gita" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[85vh] overflow-y-auto bg-surface rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.22)] p-0 outline-none border border-surface-container-high/50">
    <div class="p-4 border-b border-surface-container-high/40 flex items-center justify-between bg-surface-container-lowest sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-secondary text-[22px]">bookmark_heart</span>
        <div>
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Bhagavad Gita</h2>
          <p class="font-label-sm text-label-sm text-on-surface-variant">Daily contemplative verse</p>
        </div>
      </div>
      <button onclick="document.getElementById('modal-gita').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>
    <div class="p-5 space-y-4">
      <div class="flex items-center justify-between text-secondary font-label-md font-medium">
        <span id="gita-chapter-verse">Chapter 2 • Verse 47</span>
        <span class="px-2.5 py-0.5 bg-secondary-fixed text-on-secondary-fixed rounded-full text-label-sm">Nishkama Karma</span>
      </div>
      <!-- Sanskrit sloka -->
      <div class="p-4 rounded-xl bg-surface-container-low border border-surface-container-high/30 text-center">
        <p class="font-body-lg text-body-lg text-on-surface font-medium leading-relaxed" id="gita-sanskrit">
          कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br/>
          मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥
        </p>
      </div>
      <!-- Translation -->
      <div class="space-y-1.5">
        <span class="font-label-sm text-label-sm text-on-surface-variant font-medium">Translation</span>
        <p class="font-body-md text-body-md text-on-surface leading-relaxed" id="gita-translation">
          "You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty."
        </p>
      </div>
      <!-- Contemplation -->
      <div class="p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 space-y-2">
        <span class="font-label-sm text-label-sm text-primary font-medium flex items-center gap-1">
          <span class="material-symbols-outlined text-[16px]">psychology</span>
          Sanctuary Prompt
        </span>
        <p class="font-body-sm text-body-sm text-on-surface-variant">
          Where in your day can you release attachment to outcomes and simply give complete, quiet attention to the act itself?
        </p>
      </div>
    </div>
  </dialog>

  <!-- 5. Screen Time / Digital Stillness Modal -->
  <dialog id="modal-screentime" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[85vh] overflow-y-auto bg-surface rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.22)] p-0 outline-none border border-surface-container-high/50">
    <div class="p-4 border-b border-surface-container-high/40 flex items-center justify-between bg-surface-container-lowest sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-primary text-[22px]">hourglass_bottom</span>
        <div>
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Digital Stillness</h2>
          <p class="font-label-sm text-label-sm text-on-surface-variant">App usage & presence overview</p>
        </div>
      </div>
      <button onclick="document.getElementById('modal-screentime').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>
    <div class="p-4 space-y-4">
      <div class="bg-surface-container-lowest rounded-xl p-4 border border-surface-container-high/40 flex items-center justify-between">
        <div>
          <span class="font-label-sm text-label-sm text-on-surface-variant">Total screen time today</span>
          <div class="font-display-lg-mobile text-headline-lg font-bold text-on-surface mt-0.5" id="screentime-modal-total">2h 15m</div>
        </div>
        <div class="text-right">
          <span class="font-label-sm text-label-sm text-primary font-medium bg-surface-container-low px-2.5 py-1 rounded-full">Quiet day</span>
        </div>
      </div>
      <div class="space-y-2">
        <span class="font-label-lg font-semibold text-on-surface">Top application activity</span>
        <div class="flex flex-col bg-surface-container-lowest rounded-xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden" id="screentime-apps-list">
          <div class="p-3 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg bg-surface-container-low flex items-center justify-center text-primary font-bold text-sm">V</div>
              <div class="flex flex-col">
                <span class="font-body-md font-medium text-on-surface text-[14px]">V07 Notebook</span>
                <span class="font-label-sm text-[11px] text-on-surface-variant">Reading & focus</span>
              </div>
            </div>
            <span class="font-label-md text-on-surface font-semibold">48m</span>
          </div>
          <div class="p-3 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg bg-surface-container-low flex items-center justify-center text-secondary font-bold text-sm">K</div>
              <div class="flex flex-col">
                <span class="font-body-md font-medium text-on-surface text-[14px]">Kindle</span>
                <span class="font-label-sm text-[11px] text-on-surface-variant">Books & reflection</span>
              </div>
            </div>
            <span class="font-label-md text-on-surface font-semibold">35m</span>
          </div>
          <div class="p-3 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg bg-surface-container-low flex items-center justify-center text-outline font-bold text-sm">M</div>
              <div class="flex flex-col">
                <span class="font-body-md font-medium text-on-surface text-[14px]">Maps & Walks</span>
                <span class="font-label-sm text-[11px] text-on-surface-variant">Outdoor navigation</span>
              </div>
            </div>
            <span class="font-label-md text-on-surface font-semibold">22m</span>
          </div>
        </div>
      </div>
    </div>
  </dialog>

  <!-- ========================================== -->
  <!-- APPLICATION JAVASCRIPT                     -->
  <!-- ========================================== -->
  <script>
    // Global State
    let currentTab = 'home';
    let currentDayOffset = 0;
    let homeTimerRunning = false;
    let homeTimerSeconds = 25 * 60;
    let homeTimerInterval = null;
    let activeTag = 'all';
    let currentReadingPage = 142;
    const totalReadingPages = 338;

    // Default Sanctuary Tasks
    let tasks = [
      { id: '1', title: 'Review morning pages', time: '7:45 am', done: true },
      { id: '2', title: 'Water the plants', time: 'Ferns and monstera by window', done: false },
      { id: '3', title: 'Read for 10 minutes', time: 'Balcony tea time', done: false }
    ];

    // Default Journal Thoughts
    let thoughts = [
      {
        id: 't1',
        text: 'Morning clarity after the green tea. Finished the chapter on solitude. The quiet hours of early dawn bring an unmatched spaciousness.',
        time: '09:30 AM',
        date: 'Today',
        tags: ['calm', 'morning'],
        mood: 'wb_twilight'
      },
      {
        id: 't2',
        text: 'Thoughts on deep focus without artificial urgency. When we remove notifications, attention naturally deepens into slow flow.',
        time: '10:15 PM',
        date: 'Yesterday',
        tags: ['reading', 'focus'],
        mood: 'nightlight'
      },
      {
        id: 't3',
        text: 'Walk through the pine canopy before dusk. The air smelled of damp cedar. Gratitude for quiet evenings with no alarms.',
        time: '06:40 PM',
        date: 'Yesterday',
        tags: ['gratitude', 'calm'],
        mood: 'park'
      }
    ];

    // Tab Switching
    function switchTab(tab) {
      currentTab = tab;
      const pages = {
        home: document.getElementById('tab-home'),
        tools: document.getElementById('tab-tools'),
        journal: document.getElementById('tab-journal'),
        day: document.getElementById('tab-day'),
        you: document.getElementById('tab-you')
      };

      const titles = {
        home: 'Home',
        tools: 'Tools',
        journal: 'Journal',
        day: 'Day',
        you: 'You'
      };

      const icons = {
        home: 'spa',
        tools: 'grid_view',
        journal: 'edit_note',
        day: 'calendar_today',
        you: 'person_outline'
      };

      // Toggle pages
      Object.keys(pages).forEach(k => {
        if (pages[k]) {
          if (k === tab) {
            pages[k].classList.add('active');
          } else {
            pages[k].classList.remove('active');
          }
        }
      });

      // Update header
      document.getElementById('header-title').textContent = titles[tab] || 'Home';
      document.getElementById('header-icon').textContent = icons[tab] || 'spa';

      // Update bottom nav dock
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

    // Dynamic Greeting
    function updateGreeting() {
      const now = new Date();
      const hour = now.getHours();
      const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
      const dayName = days[now.getDay()];

      const dayChip = document.getElementById('home-greeting-day');
      if (dayChip) dayChip.textContent = `${dayName} sanctuary`;

      let greeting = 'Good morning, Sarah';
      if (hour >= 12 && hour < 17) {
        greeting = 'Good afternoon, Sarah';
      } else if (hour >= 17) {
        greeting = 'Good evening, Sarah';
      }

      const title = document.getElementById('home-greeting-title');
      if (title) title.textContent = greeting;
    }

    // Home Focus Timer
    function toggleHomeTimer() {
      const btn = document.getElementById('home-timer-btn');
      const text = document.getElementById('home-timer-btn-text');
      const icon = btn.querySelector('.material-symbols-outlined');

      if (homeTimerRunning) {
        // Pause timer
        clearInterval(homeTimerInterval);
        homeTimerRunning = false;
        icon.textContent = 'play_arrow';
        text.textContent = 'Resume focus';
        btn.classList.remove('bg-secondary');
        btn.classList.add('bg-primary');
        try { fetch('/session/pause', { method: 'POST' }); } catch(e) {}
      } else {
        // Start or resume timer
        homeTimerRunning = true;
        icon.textContent = 'pause';
        text.textContent = 'Pause focus';
        btn.classList.remove('bg-primary');
        btn.classList.add('bg-secondary');
        try {
          fetch('/session/start', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ activity: 'focus', timer_mode: 'countdown', timer_seconds: homeTimerSeconds })
          });
        } catch(e) {}

        homeTimerInterval = setInterval(() => {
          if (homeTimerSeconds > 0) {
            homeTimerSeconds--;
            renderHomeTimer();
          } else {
            clearInterval(homeTimerInterval);
            homeTimerRunning = false;
            homeTimerSeconds = 25 * 60;
            renderHomeTimer();
            icon.textContent = 'check_circle';
            text.textContent = 'Session complete';
            btn.classList.remove('bg-secondary');
            btn.classList.add('bg-primary');
            try { fetch('/session/stop', { method: 'POST' }); } catch(e) {}
          }
        }, 1000);
      }
    }

    function renderHomeTimer() {
      const mins = Math.floor(homeTimerSeconds / 60);
      const secs = homeTimerSeconds % 60;
      const str = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
      const disp = document.getElementById('home-timer-display');
      if (disp) disp.textContent = str;
    }

    // Tasks Management
    function renderTasks() {
      const list = document.getElementById('home-tasks-list');
      if (!list) return;
      list.innerHTML = '';

      let doneCount = 0;
      tasks.forEach(t => {
        if (t.done) doneCount++;
        const item = document.createElement('label');
        item.className = 'group flex items-center gap-space-sm p-space-xs rounded-xl hover:bg-surface-container-low transition-colors cursor-pointer select-none';
        item.innerHTML = `
          <input type="checkbox" ${t.done ? 'checked' : ''} onchange="toggleTask('${t.id}')" class="peer sr-only"/>
          <div class="w-5 h-5 rounded-md ${t.done ? 'bg-primary text-on-primary' : 'border-2 border-outline-variant bg-transparent'} flex items-center justify-center transition-all shrink-0">
            <span class="material-symbols-outlined text-[15px] ${t.done ? '' : 'opacity-0'}">check</span>
          </div>
          <div class="flex flex-col min-w-0 flex-1">
            <span class="font-body-md text-body-md ${t.done ? 'line-through text-on-surface-variant opacity-70' : 'text-on-surface'} transition-colors truncate font-medium">${t.title}</span>
            <span class="font-label-sm text-label-sm text-outline truncate">${t.time}</span>
          </div>
        `;
        list.appendChild(item);
      });

      const badge = document.getElementById('tasks-done-badge');
      if (badge) badge.textContent = `${doneCount} done`;
    }

    function toggleTask(id) {
      const t = tasks.find(x => x.id === id);
      if (t) {
        t.done = !t.done;
        renderTasks();
        try {
          fetch(`/tasks/${id}/${t.done ? 'done' : 'reopen'}`, { method: 'POST' });
        } catch(e) {}
      }
    }

    function promptAddTask() {
      const title = prompt('Enter a gentle task for today:');
      if (title && title.trim()) {
        const newTask = {
          id: String(Date.now()),
          title: title.trim(),
          time: 'Added moments ago',
          done: false
        };
        tasks.push(newTask);
        renderTasks();
        try {
          fetch('/task', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ title: title.trim(), deadline: new Date().toISOString().split('T')[0] })
          });
        } catch(e) {}
      }
    }

    // Journal Management
    function renderJournalEntries() {
      const container = document.getElementById('journal-entries-container');
      if (!container) return;
      container.innerHTML = '';

      const query = (document.getElementById('journal-search')?.value || '').toLowerCase().trim();

      const filtered = thoughts.filter(t => {
        const matchesTag = activeTag === 'all' || t.tags.includes(activeTag);
        const matchesQuery = query === '' || t.text.toLowerCase().includes(query);
        return matchesTag && matchesQuery;
      });

      // Group by date
      const groups = {};
      filtered.forEach(t => {
        const d = t.date || 'Earlier';
        if (!groups[d]) groups[d] = [];
        groups[d].push(t);
      });

      const dateKeys = Object.keys(groups);
      if (dateKeys.length === 0) {
        container.innerHTML = `
          <div class="py-8 text-center text-on-surface-variant font-body-md">
            No reflections found for this filter.
          </div>
        `;
        return;
      }

      dateKeys.forEach((d, idx) => {
        const groupEl = document.createElement('section');
        groupEl.className = 'journal-group flex flex-col gap-space-md';

        let dateFormatted = d === 'Today' ? 'May 14' : (d === 'Yesterday' ? 'May 13' : d);
        groupEl.innerHTML = `
          <div class="flex items-center gap-space-sm">
            <span class="font-headline-sm text-headline-sm text-on-surface font-semibold">${d}</span>
            <div class="h-[1px] flex-1 bg-surface-container-high"></div>
            <span class="font-label-sm text-label-sm text-outline">${dateFormatted}</span>
          </div>
        `;

        groups[d].forEach(entry => {
          const entryEl = document.createElement('article');
          entryEl.className = 'journal-entry flex flex-col gap-3 py-2 border-b border-surface-container-low/60 last:border-b-0';
          const tagBadges = (entry.tags || []).map(tg =>
            `<span class="px-2.5 py-0.5 rounded-full font-label-sm text-label-sm bg-secondary-fixed text-on-secondary-fixed font-medium">#${tg}</span>`
          ).join('');

          entryEl.innerHTML = `
            <div class="flex items-center justify-between text-outline">
              <span class="font-label-md text-label-md tracking-wider">${entry.time}</span>
              <div class="flex items-center gap-1 text-tertiary">
                <span class="material-symbols-outlined text-[15px] opacity-75">${entry.mood || 'wb_twilight'}</span>
                <span class="font-label-sm text-label-sm">quiet hour</span>
              </div>
            </div>
            <p class="font-body-lg text-body-lg text-on-surface leading-[1.65] select-text">
              ${entry.text}
            </p>
            <div class="flex items-center justify-between pt-1">
              <div class="flex items-center gap-2 flex-wrap">
                ${tagBadges}
              </div>
              <div class="flex items-center gap-4 text-outline font-label-sm text-label-sm">
                <button class="hover:text-primary transition-colors cursor-pointer py-1 font-medium" onclick="editThought('${entry.id}')">Edit</button>
                <button class="hover:text-error transition-colors cursor-pointer py-1 font-medium" onclick="deleteThought('${entry.id}')">Delete</button>
              </div>
            </div>
          `;
          groupEl.appendChild(entryEl);
        });

        container.appendChild(groupEl);

        if (idx < dateKeys.length - 1) {
          const divDots = document.createElement('div');
          divDots.className = 'w-full flex items-center justify-center gap-2 py-1 select-none';
          divDots.innerHTML = `
            <span class="w-1.5 h-1.5 rounded-full bg-surface-container-highest"></span>
            <span class="w-1.5 h-1.5 rounded-full bg-surface-container-highest"></span>
            <span class="w-1.5 h-1.5 rounded-full bg-surface-container-highest"></span>
          `;
          container.appendChild(divDots);
        }
      });
    }

    function selectJournalTag(tag, el) {
      activeTag = tag;
      document.querySelectorAll('.journal-filter-chip').forEach(c => {
        c.classList.remove('bg-primary', 'text-on-primary', 'shadow-sm');
        c.classList.add('bg-surface-container-low', 'text-on-surface-variant');
      });
      el.classList.remove('bg-surface-container-low', 'text-on-surface-variant');
      el.classList.add('bg-primary', 'text-on-primary', 'shadow-sm');
      renderJournalEntries();
    }

    function applyJournalFilters() {
      renderJournalEntries();
    }

    function openWriteModal() {
      const modal = document.getElementById('write-modal');
      const input = document.getElementById('thought-input');
      const tagInput = document.getElementById('thought-tag-input');
      if (input) input.value = '';
      if (tagInput) tagInput.value = '';
      if (modal) modal.showModal();
    }

    function handleSaveThought(e) {
      e.preventDefault();
      const input = document.getElementById('thought-input');
      const tagInput = document.getElementById('thought-tag-input');
      const text = input ? input.value.trim() : '';
      if (!text) return;

      const rawTags = (tagInput ? tagInput.value : '').split(/[\s,]+/).filter(Boolean).map(x => x.replace('#', ''));
      const newThought = {
        id: 't_' + Date.now(),
        text: text,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        date: 'Today',
        tags: rawTags.length > 0 ? rawTags : ['calm'],
        mood: 'wb_twilight'
      };

      thoughts.unshift(newThought);
      renderJournalEntries();
      document.getElementById('write-modal').close();

      try {
        fetch('/event/thought', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ text: text, topic: rawTags[0] || 'journal' })
        });
      } catch(e) {}
    }

    function editThought(id) {
      const entry = thoughts.find(t => t.id === id);
      if (!entry) return;
      const updated = prompt('Edit your reflection:', entry.text);
      if (updated && updated.trim()) {
        entry.text = updated.trim();
        renderJournalEntries();
      }
    }

    function deleteThought(id) {
      if (confirm('Gently release this reflection?')) {
        thoughts = thoughts.filter(t => t.id !== id);
        renderJournalEntries();
        try {
          fetch(`/event/thought/${id}`, { method: 'DELETE' });
        } catch(e) {}
      }
    }

    // Day Offset Navigation
    function changeDayOffset(delta) {
      currentDayOffset += delta;
      renderDayView();
    }

    function resetToToday() {
      currentDayOffset = 0;
      renderDayView();
    }

    function renderDayView() {
      const d = new Date();
      d.setDate(d.getDate() + currentDayOffset);
      const options = { weekday: 'long', month: 'short', day: 'numeric' };
      const dateStr = d.toLocaleDateString('en-US', options);

      const navLabel = document.getElementById('day-nav-label');
      const cardLabel = document.getElementById('day-card-label');
      if (navLabel) {
        navLabel.textContent = currentDayOffset === 0 ? 'Today' : dateStr;
      }
      if (cardLabel) {
        cardLabel.textContent = currentDayOffset === 0 ? 'Today' : dateStr;
      }
    }

    // Profile & Settings
    function toggleSwitch(btn, settingKey) {
      const isChecked = btn.getAttribute('aria-checked') === 'true';
      const thumb = btn.querySelector('div');
      const icon = thumb.querySelector('span');

      if (isChecked) {
        btn.setAttribute('aria-checked', 'false');
        btn.classList.remove('bg-primary');
        btn.classList.add('bg-surface-container-high');
        thumb.classList.remove('translate-x-5');
        thumb.classList.add('translate-x-0');
        icon.classList.add('opacity-0');
        localStorage.setItem(`v07_${settingKey}`, 'false');
      } else {
        btn.setAttribute('aria-checked', 'true');
        btn.classList.remove('bg-surface-container-high');
        btn.classList.add('bg-primary');
        thumb.classList.remove('translate-x-0');
        thumb.classList.add('translate-x-5');
        icon.classList.remove('opacity-0');
        icon.classList.add('text-primary');
        localStorage.setItem(`v07_${settingKey}`, 'true');
      }
    }

    function setAppTheme(theme) {
      const checkLight = document.getElementById('check-light');
      const checkDark = document.getElementById('check-dark');
      const cardLight = document.getElementById('theme-card-light');
      const cardDark = document.getElementById('theme-card-dark');

      if (theme === 'dark') {
        document.documentElement.classList.add('dark');
        if (checkDark) checkDark.classList.remove('opacity-0');
        if (checkDark) checkDark.classList.add('bg-primary', 'text-on-primary');
        if (checkLight) checkLight.classList.add('opacity-0');
        if (cardDark) cardDark.classList.remove('opacity-85');
        if (cardLight) cardLight.classList.add('opacity-85');
        localStorage.setItem('v07_theme', 'dark');
        if (window.Android && window.Android.setSystemTheme) {
          window.Android.setSystemTheme(false);
        }
      } else {
        document.documentElement.classList.remove('dark');
        if (checkLight) checkLight.classList.remove('opacity-0');
        if (checkLight) checkLight.classList.add('bg-primary', 'text-on-primary');
        if (checkDark) checkDark.classList.add('opacity-0');
        if (cardLight) cardLight.classList.remove('opacity-85');
        if (cardDark) cardDark.classList.add('opacity-85');
        localStorage.setItem('v07_theme', 'light');
        if (window.Android && window.Android.setSystemTheme) {
          window.Android.setSystemTheme(true);
        }
      }
    }

    function initTheme() {
      const saved = localStorage.getItem('v07_theme') || 'light';
      setAppTheme(saved);
    }

    function promptEditProfile() {
      const name = prompt('Update sanctuary profile name:', document.getElementById('profile-name').textContent);
      if (name && name.trim()) {
        document.getElementById('profile-name').textContent = name.trim();
        localStorage.setItem('v07_profile_name', name.trim());
      }
    }

    function shareSanctuaryStatus() {
      if (navigator.share) {
        navigator.share({
          title: 'V07 Sanctuary',
          text: 'Deep in quiet flow with V07 Notebook.'
        }).catch(() => {});
      } else {
        alert('Status shared privately.');
      }
    }

    function copyVaultPath(button) {
      const icon = button.querySelector('.material-symbols-outlined');
      const path = document.getElementById('vault-path-text')?.textContent || '/storage/emulated/0/V07Data';
      navigator.clipboard?.writeText(path);
      const prevText = icon.textContent;
      icon.textContent = 'check';
      button.classList.add('text-primary');
      setTimeout(() => {
        icon.textContent = prevText;
        button.classList.remove('text-primary');
      }, 1800);
    }

    function pickVaultFolder() {
      if (window.Android && window.Android.openVaultPicker) {
        window.Android.openVaultPicker();
      } else {
        alert('Vault folder selection is managed by system storage.');
      }
    }

    function exportVaultBackup() {
      const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify({ thoughts, tasks, date: new Date().toISOString() }));
      const a = document.createElement('a');
      a.setAttribute('href', dataStr);
      a.setAttribute('download', `v07_sanctuary_backup_${new Date().toISOString().split('T')[0]}.json`);
      document.body.appendChild(a);
      a.click();
      a.remove();
    }

    function inspectDatabase() {
      alert(`Local Sanctuary Database Status:\n- Thoughts: ${thoughts.length} records\n- Tasks: ${tasks.length} records\n- Vault Storage: SQLite & JSONL synced`);
    }

    function downloadDiagnostics() {
      alert('Diagnostics bundle generated: all services nominal.');
    }

    function clearCacheAction() {
      if (confirm('Clear local web view temporary cache?')) {
        alert('Cache cleared.');
      }
    }

    function resetAppAction() {
      if (confirm('Are you sure you want to reset all sanctuary local settings?')) {
        localStorage.clear();
        location.reload();
      }
    }

    // Tool Modals
    function openHabitsModal() {
      const modal = document.getElementById('modal-habits');
      if (modal) modal.showModal();
    }

    function openReadingModal() {
      const modal = document.getElementById('modal-reading');
      if (modal) modal.showModal();
    }

    function openGitaModal() {
      const modal = document.getElementById('modal-gita');
      if (modal) modal.showModal();
    }

    function openScreenTimeModal() {
      const modal = document.getElementById('modal-screentime');
      if (modal) modal.showModal();
    }

    function toggleHabitItem(wrapper) {
      const check = wrapper.querySelector('.habit-check');
      const title = wrapper.querySelector('.habit-title');
      const icon = check.querySelector('span');
      const isDone = check.classList.contains('bg-primary');

      if (isDone) {
        check.className = 'habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0';
        icon.classList.add('opacity-0');
        title.classList.remove('line-through', 'opacity-70');
      } else {
        check.className = 'habit-check w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0';
        icon.classList.remove('opacity-0');
        title.classList.add('line-through', 'opacity-70');
      }
    }

    function promptAddHabit() {
      const habit = prompt('Enter a new gentle daily ritual:');
      if (habit && habit.trim()) {
        const list = document.getElementById('habits-modal-list');
        const div = document.createElement('div');
        div.className = 'habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs';
        div.innerHTML = `
          <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
            <div class="habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title font-body-md text-on-surface font-medium">${habit.trim()}</span>
              <span class="font-label-sm text-[11px] text-on-surface-variant">Scheduled daily</span>
            </div>
          </div>
        `;
        list.appendChild(div);
      }
    }

    function adjustBookPages(delta) {
      currentReadingPage = Math.max(1, Math.min(totalReadingPages, currentReadingPage + delta));
      const pct = Math.round((currentReadingPage / totalReadingPages) * 100);
      document.getElementById('reading-percent-label').textContent = `${pct}% complete`;
      document.getElementById('home-book-percent').textContent = `${pct}% complete`;
      document.getElementById('reading-modal-bar').style.width = `${pct}%`;
      document.getElementById('home-book-bar').style.width = `${pct}%`;
    }

    function promptUpdatePage() {
      const p = prompt('Enter current page number:', currentReadingPage);
      if (p && !isNaN(parseInt(p))) {
        currentReadingPage = parseInt(p);
        adjustBookPages(0);
      }
    }

    // Heatmap rendering
    function renderHeatmap() {
      const grid = document.getElementById('heatmap-grid');
      if (!grid) return;
      grid.innerHTML = '';
      for (let i = 1; i <= 30; i++) {
        const span = document.createElement('span');
        span.className = 'w-5 h-5 rounded-md flex items-center justify-center text-[10px] font-medium ';
        if (i % 5 === 0) {
          span.className += 'bg-primary text-on-primary';
        } else if (i % 3 === 0) {
          span.className += 'bg-primary-container text-on-primary';
        } else if (i % 2 === 0) {
          span.className += 'bg-surface-container-high text-on-surface';
        } else {
          span.className += 'bg-surface-container-low text-on-surface-variant';
        }
        span.textContent = i;
        grid.appendChild(span);
      }
    }

    // Fetch live data from backend server if available
    async function syncBackendData() {
      try {
        const rUsage = await fetch('/app-usage/today');
        if (rUsage.ok) {
          const u = await rUsage.json();
          if (u.screen_time_formatted) {
            document.getElementById('stat-screen-time').textContent = u.screen_time_formatted;
            document.getElementById('day-screen-time').textContent = u.screen_time_formatted;
            document.getElementById('screentime-modal-total').textContent = u.screen_time_formatted;
            document.getElementById('tools-screen-time-label').textContent = `${u.screen_time_formatted} screen time`;
          }
        }
      } catch(e) {}

      try {
        const rThoughts = await fetch('/thoughts');
        if (rThoughts.ok) {
          const j = await rThoughts.json();
          if (j.thoughts && j.thoughts.length > 0) {
            thoughts = j.thoughts.map(item => ({
              id: item.id,
              text: item.text,
              time: item.ts ? new Date(item.ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '09:00 AM',
              date: 'Today',
              tags: item.topic ? [item.topic] : ['calm'],
              mood: 'wb_twilight'
            }));
            renderJournalEntries();
          }
        }
      } catch(e) {}

      try {
        const rTasks = await fetch('/tasks?filter=today');
        if (rTasks.ok) {
          const j = await rTasks.json();
          if (j.tasks && j.tasks.length > 0) {
            tasks = j.tasks.map(t => ({
              id: t.id,
              title: t.title,
              time: t.deadline || 'Today',
              done: t.status === 'done'
            }));
            renderTasks();
          }
        }
      } catch(e) {}
    }

    // Initialization on DOMContentLoaded
    window.addEventListener('DOMContentLoaded', () => {
      initTheme();
      updateGreeting();
      renderTasks();
      renderJournalEntries();
      renderHeatmap();
      syncBackendData();

      // Restore profile name if saved
      const savedName = localStorage.getItem('v07_profile_name');
      if (savedName) {
        const nameEl = document.getElementById('profile-name');
        if (nameEl) nameEl.textContent = savedName;
        const greetingTitle = document.getElementById('home-greeting-title');
        if (greetingTitle) greetingTitle.textContent = `Good morning, ${savedName.split(' ')[0]}`;
      }
    });
  </script>
</body>
</html>
'''

output_path = r'D:\software\V07-Android\app\src\main\assets\index.html'
with open(output_path, 'w', encoding='utf-8') as f:
    f.write(html_content)

print(f"Successfully generated {output_path} with length {len(html_content)} characters.")
