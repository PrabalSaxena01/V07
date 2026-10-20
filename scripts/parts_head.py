"""
parts_head.py
HTML Head, Typography, Material Symbols, Tailwind Fallback, Firebase SDKs, Chart.js,
and Design System tokens adhering to taste and impeccable (Zero AI slop, No purple blur blobs, Crisp slate surfaces).
"""

HEAD_HTML = r'''<!DOCTYPE html>
<html lang="en" class="">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
  <meta name="theme-color" content="#0D120F">
  <title>V07 Sanctuary · Sovereign Operating Platform</title>

  <!-- Google Fonts: Plus Jakarta Sans (UI) & JetBrains Mono (Technical Tabular Telemetry) -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />

  <!-- Tailwind CSS & Local Fallback -->
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
  <script>
    window.firebaseConfig = {
      apiKey: "AIzaSyCiKGZ_J8p-XnvsANNnmkrUxEZQX0KUVUc",
      authDomain: "v07-dashboard.firebaseapp.com",
      projectId: "v07-dashboard",
      storageBucket: "v07-dashboard.firebasestorage.app",
      messagingSenderId: "689369760342",
      appId: "1:689369760342:web:e0debabebc08d6f016f1f7"
    };
    var firebaseConfig = window.firebaseConfig;

    window.ensureFirebaseInitialized = function() {
      if (typeof firebase === 'undefined') return false;
      try {
        if (!firebase.apps || firebase.apps.length === 0) {
          firebase.initializeApp(window.firebaseConfig);
        }
        return (firebase.apps && firebase.apps.length > 0);
      } catch (e) {
        console.warn('[V07 Firebase] ensureFirebaseInitialized:', e);
        return (firebase.apps && firebase.apps.length > 0);
      }
    };

    try {
      window.ensureFirebaseInitialized();
      if (typeof firebase !== 'undefined' && firebase.auth) {
        try {
          firebase.auth().setPersistence((firebase.auth.Auth && firebase.auth.Auth.Persistence && firebase.auth.Auth.Persistence.LOCAL) || 'local').catch(function(){});
        } catch (_) {}
      }
    } catch (e) {
      console.warn('[V07 Firebase Init]:', e);
    }
  </script>

  <!-- Chart.js 4.4.4 for high-clarity data analysis -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>

  <style>
    /* Offline Local Font-Face Fallbacks */
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

    /* CSS Reset */
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    /* ==========================================================================
       V07 SANCTUARY DESIGN SYSTEM TOKENS (Matching V07 Android Native Theme)
       Botanical Obsidian Canvas, Radiant Sanctuary Emerald, Vector Precision.
       Strictly NO generic AI slop, NO purple blur blobs, NO unformatted text.
       ========================================================================== */
    :root, .dark {
      /* Deep Sanctuary Obsidian Canvas (Warm botanical dark tone from V07 app) */
      --bg: #0D120F;
      --surface: #151B17;
      --surface-alt: #1C2420;
      --surface-hover: #232E27;
      --surface-active: #2B3A31;
      --surface-glass: rgba(21, 27, 23, 0.95);

      /* Crisp 1px Hairline Borders */
      --border: #233027;
      --border-subtle: rgba(255, 255, 255, 0.08);
      --border-focus: #4ADE80;
      --divider: #1D2821;

      /* High-Contrast Typography (Impeccable WCAG AAA) */
      --text: #FFFFFF;
      --text-secondary: #A3B8AD;
      --text-muted: #6C8275;
      --text-inverse: #081E12;

      /* Radiant Sanctuary Emerald Accents (Matching V07 Android app) */
      --primary: #4ADE80;
      --primary-hover: #3CD072;
      --primary-dim: rgba(74, 222, 128, 0.14);
      --primary-text: #081E12;

      --emerald: #4ADE80;
      --emerald-dim: rgba(74, 222, 128, 0.14);
      --amber: #F59E0B;
      --amber-dim: rgba(245, 158, 11, 0.14);
      --rose: #FB7185;
      --rose-dim: rgba(251, 113, 133, 0.14);
      --violet: #A78BFA;
      --violet-dim: rgba(167, 139, 250, 0.14);
      --cyan: #38BDF8;
      --cyan-dim: rgba(56, 189, 248, 0.14);

      /* Geometric Intentional Radii (Anti-Slop Craft per /taste) */
      --radius-xs: 4px;
      --radius-sm: 6px;
      --radius-md: 8px;
      --radius-lg: 12px;
      --radius-xl: 14px;
      --radius-full: 9999px;

      /* Elevation Shadows */
      --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.4);
      --shadow-md: 0 3px 8px rgba(0, 0, 0, 0.45);
      --shadow-lg: 0 8px 20px rgba(0, 0, 0, 0.55);
      --nav-shadow: 0 8px 24px -4px rgba(3, 7, 18, 0.8), 0 2px 6px -1px rgba(3, 7, 18, 0.5);
    }

    :root[data-theme="light"] {
      /* Warm Porcelain / Sage Sanctuary Canvas (From V07 app light mode) */
      --bg: #F4F8F5;
      --surface: #FFFFFF;
      --surface-alt: #EDF4EE;
      --surface-hover: #E3ECE5;
      --surface-active: #D5E2D8;
      --surface-glass: rgba(255, 255, 255, 0.95);

      --border: #E0ECE3;
      --border-subtle: rgba(0, 0, 0, 0.05);
      --border-focus: #1B4D3E;
      --divider: #EAF2EC;

      --text: #132219;
      --text-secondary: #4D6154;
      --text-muted: #738A7C;
      --text-inverse: #FFFFFF;

      /* Deep Forest Sanctuary Emerald */
      --primary: #1B4D3E;
      --primary-hover: #133D31;
      --primary-dim: rgba(27, 77, 62, 0.10);
      --primary-text: #FFFFFF;

      --emerald: #059669;
      --emerald-dim: rgba(5, 150, 105, 0.10);
      --amber: #D97706;
      --amber-dim: rgba(217, 119, 6, 0.10);
      --rose: #E11D48;
      --rose-dim: rgba(225, 29, 72, 0.10);
      --violet: #7C3AED;
      --violet-dim: rgba(124, 58, 237, 0.10);
      --cyan: #0891B2;
      --cyan-dim: rgba(8, 145, 178, 0.10);

      --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
      --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.07);
      --shadow-lg: 0 12px 28px rgba(0, 0, 0, 0.1);
      --nav-shadow: 0 10px 30px -4px rgba(44, 24, 46, 0.08), 0 3px 8px -2px rgba(44, 24, 46, 0.04);
    }

    .floating-dock-pill {
      box-shadow: var(--nav-shadow);
    }

    html, body {
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      background-color: var(--bg);
      color: var(--text);
      min-height: 100vh;
      max-width: 100vw;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
      overflow-x: hidden;
      touch-action: manipulation;
    }

    /* Tabular Technical Monospace */
    .font-mono, .tabular-nums {
      font-family: 'JetBrains Mono', monospace;
      font-variant-numeric: tabular-nums;
    }

    /* Material Symbols Typography Alignment */
    .material-symbols-outlined {
      font-family: 'Material Symbols Outlined' !important;
      font-weight: normal;
      font-style: normal;
      font-size: 20px;
      line-height: 1;
      display: inline-block;
      white-space: nowrap;
      user-select: none;
      vertical-align: middle;
    }

    /* Tactile Press Physics (scale 0.97 per taste skill) */
    .btn-press {
      transition: transform 0.12s cubic-bezier(0.16, 1, 0.3, 1), background-color 0.12s ease, border-color 0.12s ease;
      touch-action: manipulation;
    }
    .btn-press:active {
      transform: scale(0.97);
    }

    /* Primary buttons contrast guarantee: Emerald background gets dark text in dark mode, white in light mode */
    .bg-\[var\(--primary\)\] {
      color: var(--primary-text) !important;
    }

    /* Zero Broken Text Truncation */
    .truncate-single {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .truncate-multi {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    /* Precision Scrollbars */
    ::-webkit-scrollbar { width: 5px; height: 5px; }
    ::-webkit-scrollbar-track { background: transparent; }
    ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 2px; }
    ::-webkit-scrollbar-thumb:hover { background: var(--text-muted); }

    /* Transitions */
    .fade-in {
      animation: fadeIn 0.18s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(3px); }
      to { opacity: 1; transform: translateY(0); }
    }
    @keyframes pulseOnline {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.35; transform: scale(0.85); }
    }
    .pulse-online {
      animation: pulseOnline 2s infinite ease-in-out;
    }
  </style>
</head>
'''
