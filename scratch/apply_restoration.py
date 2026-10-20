import os
import re

def build_tools_html():
    return """    <div id="tab-tools" class="page-tab flex-col w-full pb-6 space-y-space-xl">
      <section class="flex flex-col gap-y-space-xs pt-space-xs">
        <div class="inline-flex items-center gap-space-xs text-primary font-label-md text-label-md">
          <span class="material-symbols-outlined text-[18px]">widgets</span>
          <span>System Utilities & Plugins</span>
        </div>
        <h2 class="font-headline-lg text-headline-lg text-on-surface font-semibold tracking-tight">Your tools & companions</h2>
        <p class="font-body-md text-body-md text-on-surface-variant">Core sanctuary utilities and isolated modular extensions (ADR-006 & ADR-007).</p>
      </section>

      <!-- SECTION 1: CORE SYSTEM UTILITIES & ENGINES -->
      <section class="flex flex-col space-y-space-md">
        <div class="flex items-center justify-between pb-1.5 border-b border-surface-container-high/40">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-[20px] material-symbols-filled">shield</span>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-base font-semibold text-on-surface">Core System Engines</h3>
              <span class="font-label-sm text-xs text-on-surface-variant">Embedded native services & system utilities</span>
            </div>
          </div>
          <span class="px-2.5 py-1 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold">7 Core Active</span>
        </div>

        <div class="flex flex-col space-y-space-md">

          <!-- Core 1: Focus Sanctuary -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">hourglass_top</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Focus Sanctuary</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">Uninterrupted deep work sessions & countdown rhythm.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-label-sm text-label-sm font-medium">Core Timer</span>
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
              <div class="font-headline-sm text-headline-sm text-secondary tracking-tight font-semibold" id="tools-focus-time-preview">25:00</div>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">check_circle</span>
                Foreground service active
              </span>
              <button onclick="openFocusModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Launch</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Core 2: Digital Stillness (Screen Time) -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">hourglass_bottom</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Digital Stillness</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">Mindful app boundaries and device usage telemetry.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-primary font-label-sm text-label-sm font-medium">Core Telemetry</span>
            </div>
            <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
              <div class="flex items-center gap-space-sm">
                <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-primary">
                  <span class="material-symbols-outlined text-[18px]">smartphone</span>
                </div>
                <div class="flex flex-col">
                  <span class="font-label-sm text-label-sm text-on-surface font-medium" id="tools-screen-time-label">Screen time active</span>
                  <span class="font-body-sm text-body-sm text-on-surface-variant">UsageStatsManager live collection</span>
                </div>
              </div>
              <span class="font-label-sm text-label-sm text-primary font-semibold">Active</span>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">analytics</span>
                Live app breakdown
              </span>
              <button onclick="openScreenTimeModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Inspect</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Core 3: Floating Bubble HUD Overlay -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-surface-container-low text-secondary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">bubble_chart</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Floating Bubble HUD</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">System overlay pill over other apps with focus timer & app presence.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-secondary font-label-sm text-label-sm font-medium" id="hud-status-badge">System Overlay</span>
            </div>
            <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
              <div class="flex items-center gap-space-sm">
                <div class="w-8 h-8 rounded-full bg-surface-container-lowest flex items-center justify-center text-secondary">
                  <span class="material-symbols-outlined text-[18px]">layers</span>
                </div>
                <div class="flex flex-col">
                  <span class="font-label-sm text-label-sm text-on-surface font-medium">Android Overlay Pill</span>
                  <span class="font-body-sm text-body-sm text-on-surface-variant">Tap to show/hide bubble overlay</span>
                </div>
              </div>
              <button onclick="toggleOverlayFromJs()" class="h-8 px-3 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold cursor-pointer active:scale-95" id="tools-overlay-toggle-btn">
                Toggle HUD
              </button>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">check_circle</span>
                SYSTEM_ALERT_WINDOW
              </span>
              <button onclick="toggleOverlayFromJs()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Switch Pill</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">refresh</span>
              </button>
            </div>
          </article>

          <!-- Core 4: Daily Rhythm & Timeline -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">calendar_today</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Day Timeline & Cadence</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">Chronological ledger, morning intent & evening reflection.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-primary font-label-sm text-label-sm font-medium">Core Cadence</span>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">wb_sunny</span>
                Daily life OS
              </span>
              <button onclick="switchTab('day')" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Open Day</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Core 5: Thoughts Vault -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">edit_note</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Thoughts & Reflections</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">On-device markdown notes with local SQLite encryption.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-primary font-label-sm text-label-sm font-medium">Core Vault</span>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">lock</span>
                Zero cloud leaks
              </span>
              <button onclick="switchTab('journal')" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Open Journal</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Core 6: Database Admin Cockpit (/admin) -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-surface-container-low text-tertiary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">database</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">SQLite Database Cockpit</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">Low-level inspection of SQLite tables, profiles & raw records.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-tertiary font-label-sm text-label-sm font-medium">Core Utility</span>
            </div>
            <div class="bg-surface-container-low rounded-xl p-space-sm flex items-center justify-between">
              <span class="font-label-sm text-xs font-mono text-on-surface-variant">/admin · WAL Mode Enabled</span>
              <span class="font-label-sm text-xs text-primary font-semibold">Direct Access</span>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">query_stats</span>
                Tables, Vacuum & Profiles
              </span>
              <button onclick="openAdminCockpit()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Inspect Cockpit</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Core 7: Recycle Bin & Recovery -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-lg shadow-sm border border-surface-container-high/40 flex flex-col gap-y-space-md transition-all active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-space-md">
                <div class="w-12 h-12 rounded-xl bg-surface-container-low text-outline flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[24px]">delete_outline</span>
                </div>
                <div class="flex flex-col">
                  <h3 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Recycle Bin & Recovery</h3>
                  <p class="font-body-sm text-body-sm text-on-surface-variant">Restore accidentally deleted journal reflections and tasks.</p>
                </div>
              </div>
              <span class="px-space-xs py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-label-sm text-label-sm font-medium">Core Utility</span>
            </div>
            <div class="flex items-center justify-between pt-space-xs">
              <span class="font-label-md text-label-md text-on-surface-variant flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px] text-tertiary">restore</span>
                Non-destructive deletes
              </span>
              <button onclick="openRecycleBinModal()" class="h-9 px-space-md rounded-full bg-surface-container-low text-primary font-label-md text-label-md hover:bg-surface-container transition-colors flex items-center gap-1.5 group cursor-pointer active:scale-95">
                <span>Open Bin</span>
                <span class="material-symbols-outlined text-[16px] transition-transform group-hover:translate-x-0.5">arrow_forward</span>
              </button>
            </div>
          </article>

        </div>
      </section>

      <!-- SECTION 2: ADD-ON PLUGINS (ECOSYSTEM EXTENSIONS) -->
      <section class="flex flex-col space-y-space-md pt-2">
        <div class="flex items-center justify-between pb-1.5 border-b border-surface-container-high/40">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-secondary text-[20px]">extension</span>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-base font-semibold text-on-surface">Modular Add-on Plugins</h3>
              <span class="font-label-sm text-xs text-on-surface-variant">Sandboxed extensions adhering to V3 Plugin Specification</span>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button onclick="pickAndInstallZip()" class="h-7 px-2.5 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold flex items-center gap-1 cursor-pointer hover:opacity-90 active:scale-95 shadow-xs" title="Install custom plugin from ZIP">
              <span class="material-symbols-outlined text-[14px]">add</span>
              <span>Install ZIP</span>
            </button>
            <span class="px-2.5 py-1 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold">10 Installed</span>
          </div>
        </div>

        <div class="flex flex-col space-y-3">

          <!-- Plugin 1: Habits Matrix -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">cycle</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Habits Matrix</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.habits</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Gentle daily rituals, cadence tracking & streaks</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-primary-container/40 text-primary font-label-sm text-[11px] font-semibold shrink-0">Plugin</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openHabitsModal()" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 2: Book Reader -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">menu_book</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Book Reader</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.reader</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Offline reading companion with page milestones</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-primary-container/40 text-primary font-label-sm text-[11px] font-semibold shrink-0">Plugin</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openReadingModal()" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 3: Bhagavad Gita -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-secondary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">bookmark_heart</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Gita Wisdom</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.gita</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Daily contemplative verses, Sanskrit recitation & commentary</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-secondary-container/40 text-secondary font-label-sm text-[11px] font-semibold shrink-0">Plugin</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openGitaModal()" class="h-8 px-3 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 4: Mood & Energy -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-secondary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">favorite</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Mood & Energy Check-in</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Equilibrium check-in, panic vault grounding & somatic awareness</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-secondary-container/40 text-secondary font-label-sm text-[11px] font-semibold shrink-0">Plugin</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openMoodModal()" class="h-8 px-3 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 5: Chronosynth Weekly Digest -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">psychology</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Weekly Digest</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.chronosynth</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Executive Sunday briefing & focus intelligence</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-primary-container/40 text-primary font-label-sm text-[11px] font-semibold shrink-0">Sandboxed</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">events:read</span>
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openPlugin('com.v07.chronosynth')" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 6: Gamify Quests -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-secondary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">military_tech</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Gamify Quests</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.gamify</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Daily mindful rituals & progressive quest rewards</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-secondary-container/40 text-secondary font-label-sm text-[11px] font-semibold shrink-0">Sandboxed</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">notifications:post</span>
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openPlugin('com.v07.gamify')" class="h-8 px-3 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 7: Sovereign Tasks -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">checklist</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Sovereign Tasks</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.tasks</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Daily tasks, priority checklist, progress bar & deadlines</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-primary-container/40 text-primary font-label-sm text-[11px] font-semibold shrink-0">Sandboxed</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">calendar:read</span>
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openPlugin('com.v07.tasks')" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 8: Body Rhythm & Vitals -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">monitor_heart</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Body Rhythm & Vitals</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.vitals</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Physical presence, resting pulse & circadian metrics</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-primary-container/40 text-primary font-label-sm text-[11px] font-semibold shrink-0">Sandboxed</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">sensors:body</span>
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openPlugin('com.v07.vitals')" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 9: Athletic Rhythm & Running -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-secondary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">directions_run</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Athletic Rhythm & Running</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v3.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.running</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Cadence-guided pacing, GPS routes & recovery analytics</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-secondary-container/40 text-secondary font-label-sm text-[11px] font-semibold shrink-0">Sandboxed</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">location:fine</span>
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openPlugin('com.v07.running')" class="h-8 px-3 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

          <!-- Plugin 10: Modular Time Tracker -->
          <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col gap-y-2.5 transition-all" style="box-shadow: var(--card-shadow);">
            <div class="flex items-start justify-between gap-space-md">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-11 h-11 rounded-xl bg-surface-container-low text-primary flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[22px]">schedule</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Modular Time Tracker</h4>
                    <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-surface-container text-outline">v1.0.0</span>
                    <span class="text-[10px] text-tertiary">com.v07.timetracker</span>
                  </div>
                  <p class="font-body-sm text-xs text-on-surface-variant line-clamp-1">Time accounting, category logging & focus distribution</p>
                </div>
              </div>
              <span class="px-2 py-0.5 rounded-full bg-primary-container/40 text-primary font-label-sm text-[11px] font-semibold shrink-0">Sandboxed</span>
            </div>
            <div class="flex items-center justify-between pt-1 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5">
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">telemetry:read</span>
                <span class="text-[10px] font-mono text-on-surface-variant bg-surface-container-low px-1.5 py-0.5 rounded">storage:isolated</span>
              </div>
              <button onclick="openPlugin('com.v07.timetracker')" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>
          </article>

        </div>
      </section>

      <!-- Footnote -->
      <section class="rounded-2xl bg-surface-container-low p-4 flex items-start gap-3 border border-surface-container-high/30">
        <span class="material-symbols-outlined text-primary text-[20px] shrink-0 mt-0.5">verified_user</span>
        <div class="flex flex-col">
          <h4 class="font-label-md text-xs font-semibold text-on-surface">Architectural Boundary Enforcement</h4>
          <p class="font-body-sm text-xs text-on-surface-variant mt-0.5 leading-relaxed">
            Core utilities are embedded into the native runtime (ADR-006). Modular extensions operate sandboxed through v07-sdk.js with declared capabilities (AGENTS.md §§ 15, 16, 21).
          </p>
        </div>
      </section>
    </div>"""

def build_settings_html():
    return """    <div id="tab-settings" class="page-tab flex-col w-full pb-10 space-y-space-xl">
      <!-- Introductory Framing -->
      <div class="flex flex-col space-y-space-xs pt-space-xs">
        <div class="inline-flex items-center gap-space-xs text-primary font-label-md font-medium">
          <span class="material-symbols-outlined text-[16px]">settings</span>
          <span>Sovereign Configuration</span>
        </div>
        <h2 class="text-2xl sm:text-3xl font-semibold text-on-surface tracking-tight">Settings</h2>
        <p class="font-body-md text-body-md text-on-surface-variant">Profile, Android device integration, and core utility controls.</p>
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
                <span class="font-headline-sm text-headline-sm text-on-surface truncate font-semibold" id="profile-name">Sanctuary User</span>
                <span class="material-symbols-outlined text-primary text-[18px] material-symbols-filled">verified</span>
              </div>
              <p class="font-body-sm text-body-sm text-on-surface-variant truncate" id="profile-email">Local SQLite Database Active</p>
              <div class="mt-1 flex items-center gap-1.5 text-tertiary font-label-sm text-label-sm">
                <span class="material-symbols-outlined text-[13px] text-primary">cloud_done</span>
                <span class="truncate">All records stored locally · No remote telemetry</span>
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

      <!-- Section 2: Android Device Integration (Floating Pill & Usage Access) -->
      <section class="flex flex-col space-y-space-sm">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Android device & system</h3>
          <span class="material-symbols-outlined text-secondary text-[18px]">layers</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl shadow-sm border border-surface-container-high/40 p-space-md flex flex-col space-y-space-md" style="box-shadow: var(--card-shadow);">
          <!-- Item 1: Floating Bubble Pill Overlay -->
          <div class="flex items-center justify-between gap-space-md py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">bubble_chart</span>
              </div>
              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-label-lg text-label-lg text-on-surface font-medium">Floating Bubble HUD</span>
                  <span class="px-2 py-0.2 rounded-full text-[10px] font-semibold bg-surface-container text-secondary" id="settings-hud-status">Standby</span>
                </div>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Floating pill visible across other apps showing active app duration & focus timer</span>
              </div>
            </div>
            <button id="btn-toggle-overlay-settings" aria-checked="false" class="toggle-switch shrink-0 w-12 h-7 rounded-full bg-surface-container-high p-0.5 transition-colors relative flex items-center cursor-pointer" onclick="toggleOverlayFromJs()" role="switch" type="button">
              <div id="toggle-overlay-thumb" class="w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-0 transition-transform flex items-center justify-center">
                <span class="material-symbols-outlined text-[12px] text-outline opacity-0" id="toggle-overlay-icon">check</span>
              </div>
            </button>
          </div>

          <div class="h-[1px] bg-surface-container-low w-full"></div>

          <!-- Item 2: Digital Wellbeing & App Usage Access -->
          <div class="flex items-center justify-between gap-space-md py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-surface-container text-primary flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">hourglass_bottom</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-label-lg text-label-lg text-on-surface font-medium">App Usage Telemetry</span>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Permits local calculation of screen stillness and active app tracking</span>
              </div>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <span class="px-2.5 py-1 rounded-full bg-primary/10 text-primary font-label-sm text-xs font-semibold" id="usage-perm-badge">Granted</span>
              <button onclick="openUsagePermissionFromJs()" class="h-8 px-2.5 rounded-full bg-surface-container text-on-surface font-label-sm text-xs font-medium cursor-pointer hover:bg-surface-container-high active:scale-95" type="button">
                Configure
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Section 3: Core Utilities & Storage Suite -->
      <section class="flex flex-col space-y-space-sm">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Core Utilities & Data Administration</h3>
          <span class="material-symbols-outlined text-primary text-[18px]">admin_panel_settings</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl shadow-sm border border-surface-container-high/40 p-space-md flex flex-col space-y-space-md" style="box-shadow: var(--card-shadow);">
          <!-- Admin Cockpit -->
          <div class="flex items-center justify-between py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-surface-container text-tertiary flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">database</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-label-lg text-label-lg text-on-surface font-medium">SQLite Database Cockpit</span>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Inspect SQLite tables, run WAL vacuum, switch profiles & manage sync</span>
              </div>
            </div>
            <button onclick="openAdminCockpit()" class="px-3.5 py-1.5 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold hover:opacity-95 transition-all shrink-0 cursor-pointer active:scale-95" type="button">
              Launch /admin
            </button>
          </div>

          <div class="h-[1px] bg-surface-container-low w-full"></div>

          <!-- Recycle Bin -->
          <div class="flex items-center justify-between py-1">
            <div class="flex items-start gap-space-sm min-w-0">
              <div class="w-9 h-9 rounded-full bg-surface-container text-outline flex items-center justify-center shrink-0 mt-0.5">
                <span class="material-symbols-outlined text-[18px]">delete_outline</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-label-lg text-label-lg text-on-surface font-medium">Recycle Bin & Data Recovery</span>
                <span class="font-body-sm text-body-sm text-on-surface-variant">Review soft-deleted entries, restore items or permanently purge</span>
              </div>
            </div>
            <button onclick="openRecycleBinModal()" class="px-3.5 py-1.5 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors shrink-0 cursor-pointer active:scale-95" type="button">
              View Bin
            </button>
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

      <!-- Section 5: Appearance & Theme -->
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

      <!-- Section 6: Maintenance & Caution Zone -->
      <section class="flex flex-col space-y-space-sm pt-space-xs">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-tertiary font-medium">Maintenance</h3>
          <span class="font-label-sm text-label-sm text-tertiary">Cache & Reset</span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex flex-col space-y-space-sm" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center justify-between py-1">
            <div class="flex flex-col min-w-0">
              <span class="font-label-md text-label-md text-on-surface font-medium">Clear local cache</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Purge ephemeral assets and temporary WebView cache</span>
            </div>
            <button onclick="clearCacheAction()" class="px-3 py-1.5 rounded-full bg-surface-container-low text-on-surface-variant font-label-sm text-label-sm hover:bg-surface-container transition-colors shrink-0 cursor-pointer font-medium" type="button">
              Clear Cache
            </button>
          </div>
          <div class="h-[1px] bg-surface-container-low w-full"></div>
          <div class="flex items-center justify-between py-1">
            <div class="flex flex-col min-w-0">
              <span class="font-label-md text-label-md text-secondary font-medium">Reset application</span>
              <span class="font-body-sm text-body-sm text-on-surface-variant">Erase local setup and reset to defaults</span>
            </div>
            <button onclick="resetAppAction()" class="px-3 py-1.5 rounded-full bg-error-container text-on-error-container font-label-sm text-label-sm hover:opacity-90 transition-all shrink-0 cursor-pointer font-medium" type="button">
              Reset
            </button>
          </div>
        </div>
      </section>

      <!-- App Sign-off note -->
      <div class="flex flex-col items-center justify-center text-center space-y-1 pt-space-xs text-on-surface-variant">
        <div class="flex items-center gap-1 text-primary">
          <span class="material-symbols-outlined text-[14px]">spa</span>
          <span class="font-label-sm text-label-sm font-semibold">V07 Sanctuary</span>
        </div>
        <p class="font-body-sm text-body-sm text-tertiary">Crafted for mindful presence · Version 3.0.0 (Build 90)</p>
      </div>
    </div>"""

def build_recycle_bin_modal_html():
    return """  <!-- 8. Recycle Bin & Data Recovery Modal -->
  <dialog id="modal-recycle-bin" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[85vh] overflow-y-auto bg-surface rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.22)] p-0 outline-none border border-surface-container-high/50">
    <div class="p-4 border-b border-surface-container-high/40 flex items-center justify-between bg-surface-container-lowest sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-outline text-[22px]">delete_outline</span>
        <div>
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Recycle Bin</h2>
          <p class="font-label-sm text-label-sm text-on-surface-variant">Non-destructive soft-deleted records</p>
        </div>
      </div>
      <button onclick="document.getElementById('modal-recycle-bin').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close recycle bin">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>
    <div class="p-4 space-y-4">
      <div class="flex items-center justify-between">
        <span class="font-label-sm text-on-surface-variant" id="recycle-bin-count">Loading deleted items...</span>
        <button onclick="emptyRecycleBin()" class="h-7 px-3 rounded-full bg-error-container text-on-error-container text-xs font-semibold cursor-pointer hover:opacity-90 active:scale-95">
          Empty Trash
        </button>
      </div>
      <div class="flex flex-col space-y-2" id="recycle-bin-list">
        <!-- Rendered dynamically -->
      </div>
    </div>
  </dialog>"""

def build_screentime_modal_html():
    return """  <!-- 6. Digital Stillness (Screen Time) Modal -->
  <dialog id="modal-screentime" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[85vh] overflow-y-auto bg-surface rounded-2xl shadow-[0_24px_48px_rgba(21,29,25,0.22)] p-0 outline-none border border-surface-container-high/50">
    <div class="p-4 border-b border-surface-container-high/40 flex items-center justify-between bg-surface-container-lowest sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-primary text-[22px]">hourglass_bottom</span>
        <div>
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Digital Stillness</h2>
          <p class="font-label-sm text-label-sm text-on-surface-variant">Live App Usage Telemetry & Presence</p>
        </div>
      </div>
      <button onclick="document.getElementById('modal-screentime').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close screen time">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>
    <div class="p-4 space-y-4">
      <div class="bg-surface-container-lowest rounded-xl p-4 border border-surface-container-high/40 flex items-center justify-between">
        <div>
          <span class="font-label-sm text-label-sm text-on-surface-variant">Total screen time today</span>
          <div class="font-display-lg-mobile text-headline-lg font-bold text-on-surface mt-0.5" id="screentime-modal-total">Calculating...</div>
        </div>
        <div class="text-right flex flex-col items-end gap-1">
          <span class="font-label-sm text-label-sm text-primary font-medium bg-surface-container-low px-2.5 py-1 rounded-full" id="screentime-status-chip">Monitored</span>
          <button onclick="toggleOverlayFromJs()" class="text-xs font-semibold text-secondary flex items-center gap-1 hover:underline cursor-pointer pt-1">
            <span class="material-symbols-outlined text-[14px]">layers</span>
            <span>Launch HUD Pill</span>
          </button>
        </div>
      </div>
      <div class="space-y-2">
        <div class="flex items-center justify-between">
          <span class="font-label-lg font-semibold text-on-surface">Top application activity</span>
          <span class="text-xs font-mono text-on-surface-variant" id="screentime-app-count">0 apps</span>
        </div>
        <div class="flex flex-col bg-surface-container-lowest rounded-xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden" id="screentime-apps-list">
          <div class="p-4 text-center text-on-surface-variant text-sm">Querying Android UsageStatsManager...</div>
        </div>
      </div>
    </div>
  </dialog>"""

def build_extra_js():
    return """
    // ====================================================
    // V3 EXTENDED NATIVE BRIDGE & UTILITY FUNCTIONS
    // ====================================================

    function openAdminCockpit() {
      // Direct navigation to /admin or loopback admin console
      window.location.href = '/admin';
    }

    async function openRecycleBinModal() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        modal.showModal();
        await loadRecycleBinData();
      }
    }

    async function loadRecycleBinData() {
      const listEl = document.getElementById('recycle-bin-list');
      const countEl = document.getElementById('recycle-bin-count');
      if (!listEl) return;
      listEl.innerHTML = '<div class=\"p-4 text-center text-on-surface-variant text-xs\">Loading...</div>';

      try {
        const res = await apiV3('/api/v3/recyclebin');
        if (res.ok) {
          const data = await res.json();
          const items = data.items || [];
          if (countEl) countEl.textContent = `${items.length} items in recycle bin`;
          if (items.length === 0) {
            listEl.innerHTML = '<div class=\"p-6 text-center text-on-surface-variant text-sm font-medium\">Recycle bin is clean. No deleted items.</div>';
            return;
          }
          listEl.innerHTML = items.map(item => {
            const raw = item.data_json || '{}';
            let title = 'Deleted item';
            try {
              const parsed = JSON.parse(raw);
              title = parsed.content || parsed.title || parsed.text || parsed.topic || item.category || 'Deleted item';
            } catch(e) {}
            return `
              <div class=\"p-3 rounded-xl bg-surface-container-lowest border border-surface-container-high/30 flex items-center justify-between gap-3\">
                <div class=\"flex flex-col min-w-0\">
                  <span class=\"font-label-sm text-xs font-semibold text-primary uppercase\">${cleanText(item.category)}</span>
                  <span class=\"font-body-sm text-sm text-on-surface truncate\">${cleanText(title)}</span>
                  <span class=\"font-label-sm text-[10px] text-on-surface-variant\">${item.deleted_at || 'Recently deleted'}</span>
                </div>
                <button onclick=\"restoreRecycleItem('${item.recycle_id}')\" class=\"h-8 px-3 rounded-full bg-surface-container text-primary font-label-sm text-xs font-semibold hover:bg-surface-container-high cursor-pointer active:scale-95 shrink-0 flex items-center gap-1\">
                  <span class=\"material-symbols-outlined text-[14px]\">restore</span>
                  <span>Restore</span>
                </button>
              </div>
            `;
          }).join('');
        }
      } catch(e) {
        if (listEl) listEl.innerHTML = '<div class=\"p-4 text-center text-error text-xs\">Failed to load recycle bin records.</div>';
      }
    }

    async function restoreRecycleItem(recycleId) {
      try {
        const res = await apiV3('/api/v3/recyclebin', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ recycle_id: recycleId })
        });
        if (res.ok) {
          await loadRecycleBinData();
          syncBackendData();
        }
      } catch(e) {}
    }

    async function emptyRecycleBin() {
      if (!confirm('Permanently delete all items from Recycle Bin? This cannot be undone.')) return;
      try {
        const res = await apiV3('/api/v3/recyclebin', { method: 'DELETE' });
        if (res.ok) {
          await loadRecycleBinData();
        }
      } catch(e) {}
    }

    function toggleOverlayFromJs() {
      if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
        try {
          const resStr = window.V07Platform.invoke('core', 'overlay:toggle', '{}');
          const res = JSON.parse(resStr);
          if (res && res.error === 'OVERLAY_PERMISSION_REQUIRED') {
            window.V07Platform.invoke('core', 'overlay:requestPermission', '{}');
          } else {
            refreshOverlayState();
          }
        } catch(e) {
          console.error('Error toggling overlay:', e);
        }
      }
    }

    function openUsagePermissionFromJs() {
      if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
        try {
          window.V07Platform.invoke('core', 'telemetry:requestPermission', '{}');
        } catch(e) {
          console.error('Error requesting usage permission:', e);
        }
      }
    }

    function refreshOverlayState() {
      if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
        try {
          const resStr = window.V07Platform.invoke('core', 'overlay:getStatus', '{}');
          const res = JSON.parse(resStr);
          if (res && res.success && res.result) {
            const running = res.result.running === true;
            const hasUsage = res.result.hasUsagePermission === true;

            // Update Tools badge & button
            const hudBadge = document.getElementById('hud-status-badge');
            if (hudBadge) {
              hudBadge.textContent = running ? 'HUD Active' : 'HUD Standby';
              hudBadge.className = running 
                ? 'px-space-xs py-0.5 rounded-full bg-primary/20 text-primary font-label-sm text-label-sm font-semibold'
                : 'px-space-xs py-0.5 rounded-full bg-surface-container-low text-secondary font-label-sm text-label-sm font-medium';
            }
            const toggleBtn = document.getElementById('tools-overlay-toggle-btn');
            if (toggleBtn) {
              toggleBtn.textContent = running ? 'Stop HUD' : 'Toggle HUD';
              toggleBtn.className = running
                ? 'h-8 px-3 rounded-full bg-secondary text-on-secondary font-label-sm text-xs font-semibold cursor-pointer active:scale-95'
                : 'h-8 px-3 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold cursor-pointer active:scale-95';
            }

            // Update Settings switch
            const settingsBadge = document.getElementById('settings-hud-status');
            if (settingsBadge) {
              settingsBadge.textContent = running ? 'Active' : 'Standby';
              settingsBadge.className = running 
                ? 'px-2 py-0.2 rounded-full text-[10px] font-semibold bg-primary/20 text-primary'
                : 'px-2 py-0.2 rounded-full text-[10px] font-semibold bg-surface-container text-secondary';
            }
            const switchBtn = document.getElementById('btn-toggle-overlay-settings');
            const switchThumb = document.getElementById('toggle-overlay-thumb');
            const switchIcon = document.getElementById('toggle-overlay-icon');
            if (switchBtn && switchThumb) {
              if (running) {
                switchBtn.className = 'toggle-switch shrink-0 w-12 h-7 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer';
                switchThumb.className = 'w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center';
                if (switchIcon) {
                  switchIcon.className = 'material-symbols-outlined text-[12px] text-primary opacity-100';
                }
              } else {
                switchBtn.className = 'toggle-switch shrink-0 w-12 h-7 rounded-full bg-surface-container-high p-0.5 transition-colors relative flex items-center cursor-pointer';
                switchThumb.className = 'w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-0 transition-transform flex items-center justify-center';
                if (switchIcon) {
                  switchIcon.className = 'material-symbols-outlined text-[12px] text-outline opacity-0';
                }
              }
            }

            // Update Usage Access badge
            const usageBadge = document.getElementById('usage-perm-badge');
            if (usageBadge) {
              usageBadge.textContent = hasUsage ? 'Granted' : 'Permission Required';
              usageBadge.className = hasUsage 
                ? 'px-2.5 py-1 rounded-full bg-primary/10 text-primary font-label-sm text-xs font-semibold'
                : 'px-2.5 py-1 rounded-full bg-error-container text-on-error-container font-label-sm text-xs font-semibold';
            }
          }
        } catch(e) {}
      }
    }

    async function loadScreenTimeLive() {
      try {
        const res = await apiV3('/api/v3/screentime');
        if (res.ok) {
          const data = await res.json();
          const totalStr = data.total_duration_formatted || '0h 0m';
          const apps = data.apps || [];

          const totalEl = document.getElementById('screentime-modal-total');
          if (totalEl) totalEl.textContent = totalStr;

          const statScreenEl = document.getElementById('stat-screen-time');
          if (statScreenEl) statScreenEl.textContent = totalStr;

          const dayScreenEl = document.getElementById('day-screen-time');
          if (dayScreenEl) dayScreenEl.textContent = totalStr;

          const countEl = document.getElementById('screentime-app-count');
          if (countEl) countEl.textContent = `${apps.length} applications`;

          const listEl = document.getElementById('screentime-apps-list');
          if (listEl) {
            if (apps.length === 0) {
              listEl.innerHTML = '<div class=\"p-4 text-center text-on-surface-variant text-sm font-medium\">No application usage logged yet today.</div>';
            } else {
              listEl.innerHTML = apps.map(app => {
                const initial = (app.app_name || app.package_name || 'A').charAt(0).toUpperCase();
                const durationMin = Math.round((app.duration_sec || 0) / 60);
                const durationStr = durationMin >= 60 ? `${Math.floor(durationMin / 60)}h ${durationMin % 60}m` : `${durationMin}m`;
                const pct = app.percentage ? `${app.percentage.toFixed(1)}%` : '';
                return `
                  <div class=\"p-3 flex items-center justify-between\">
                    <div class=\"flex items-center gap-3 min-w-0\">
                      <div class=\"w-9 h-9 rounded-xl bg-surface-container-low flex items-center justify-center text-primary font-bold text-sm shrink-0 border border-surface-container-high/40\">
                        ${initial}
                      </div>
                      <div class=\"flex flex-col min-w-0\">
                        <span class=\"font-body-md font-semibold text-on-surface text-[14px] truncate\">${cleanText(app.app_name || app.package_name)}</span>
                        <span class=\"font-label-sm text-[11px] text-on-surface-variant truncate\">${cleanText(app.category || 'Application')} · ${pct}</span>
                      </div>
                    </div>
                    <span class=\"font-label-md text-on-surface font-semibold shrink-0\">${durationStr}</span>
                  </div>
                `;
              }).join('');
            }
          }
        }
      } catch(e) {}
    }

    // Wrap openScreenTimeModal to load live data
    const originalOpenScreenTime = window.openScreenTimeModal;
    window.openScreenTimeModal = function() {
      const modal = document.getElementById('modal-screentime');
      if (modal) modal.showModal();
      loadScreenTimeLive();
    };

    window.openAdminCockpit = openAdminCockpit;
    window.openRecycleBinModal = openRecycleBinModal;
    window.restoreRecycleItem = restoreRecycleItem;
    window.emptyRecycleBin = emptyRecycleBin;
    window.toggleOverlayFromJs = toggleOverlayFromJs;
    window.refreshOverlayState = refreshOverlayState;
    window.openUsagePermissionFromJs = openUsagePermissionFromJs;
    window.loadScreenTimeLive = loadScreenTimeLive;
"""

print("Building transformation...")
def apply_transforms(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        html = f.read()

    print(f"Original size of {file_path}: {len(html)} bytes")

    # 1. Update switchTab
    # Find pages map
    pages_old = """      const pages = {
        home: document.getElementById('tab-home'),
        tools: document.getElementById('tab-tools'),
        journal: document.getElementById('tab-journal'),
        day: document.getElementById('tab-day'),
        you: document.getElementById('tab-you')
      };"""
    pages_new = """      const pages = {
        home: document.getElementById('tab-home'),
        tools: document.getElementById('tab-tools'),
        journal: document.getElementById('tab-journal'),
        day: document.getElementById('tab-day'),
        you: document.getElementById('tab-settings') || document.getElementById('tab-you'),
        settings: document.getElementById('tab-settings') || document.getElementById('tab-you')
      };"""
    if pages_old in html:
        html = html.replace(pages_old, pages_new)
        print("Replaced pages map")
    else:
        print("Warning: pages_old not found!")

    titles_old = """      const titles = {
        home: 'Home',
        tools: 'Tools',
        journal: 'Journal',
        day: 'Day',
        you: 'You'
      };"""
    titles_new = """      const titles = {
        home: 'Home',
        tools: 'Tools',
        journal: 'Journal',
        day: 'Day',
        you: 'Settings',
        settings: 'Settings'
      };"""
    if titles_old in html:
        html = html.replace(titles_old, titles_new)
        print("Replaced titles map")
    else:
        print("Warning: titles_old not found!")

    icons_old = """      const icons = {
        home: 'spa',
        tools: 'grid_view',
        journal: 'edit_note',
        day: 'calendar_today',
        you: 'person_outline'
      };"""
    icons_new = """      const icons = {
        home: 'spa',
        tools: 'grid_view',
        journal: 'edit_note',
        day: 'calendar_today',
        you: 'settings',
        settings: 'settings'
      };"""
    if icons_old in html:
        html = html.replace(icons_old, icons_new)
        print("Replaced icons map")
    else:
        print("Warning: icons_old not found!")

    # Update nav item active check in switchTab
    nav_check_old = """      const navItems = document.querySelectorAll('.nav-item');
      navItems.forEach(item => {
        if (item.getAttribute('data-tab') === tab) {
          item.classList.add('nav-active');
          item.classList.remove('text-on-surface-variant');
        } else {
          item.classList.remove('nav-active');
          item.classList.add('text-on-surface-variant');
        }
      });"""

    nav_check_new = """      const navItems = document.querySelectorAll('.nav-item');
      navItems.forEach(item => {
        const itemTab = item.getAttribute('data-tab');
        if (itemTab === tab || (tab === 'settings' && itemTab === 'you') || (tab === 'you' && itemTab === 'settings')) {
          item.classList.add('nav-active');
          item.classList.remove('text-on-surface-variant');
        } else {
          item.classList.remove('nav-active');
          item.classList.add('text-on-surface-variant');
        }
      });"""
    if nav_check_old in html:
        html = html.replace(nav_check_old, nav_check_new)
        print("Replaced nav_check in switchTab")
    else:
        print("Warning: nav_check_old not found!")

    # 2. Update bottomNav 5th item
    nav_you_old = """      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="you" href="javascript:void(0)" onclick="switchTab('you')">
        <span class="material-symbols-outlined text-[20px]">person_outline</span>
        <span class="font-label-sm text-label-sm">You</span>
      </a>"""
    nav_settings_new = """      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="settings" href="javascript:void(0)" onclick="switchTab('settings')">
        <span class="material-symbols-outlined text-[20px]">settings</span>
        <span class="font-label-sm text-label-sm">Settings</span>
      </a>"""
    if nav_you_old in html:
        html = html.replace(nav_you_old, nav_settings_new)
        print("Replaced bottomNav 5th item")
    else:
        print("Warning: nav_you_old not found!")

    # 3. Replace tab-tools block using regex or string boundaries
    tools_start = html.find('<div id="tab-tools"')
    tools_end = html.find('<div id="tab-journal"')
    if tools_start != -1 and tools_end != -1:
        # We need to find the closing div of tab-tools before tab-journal
        # Look backwards from tools_end
        idx_before_journal = html.rfind('</div>', tools_start, tools_end)
        if idx_before_journal != -1:
            # Check what's between idx_before_journal and tools_end (comments/newlines)
            old_tools_block = html[tools_start:idx_before_journal + 6]
            html = html[:tools_start] + build_tools_html() + html[idx_before_journal + 6:]
            print("Successfully replaced tab-tools block!")
        else:
            print("Error: could not find end of tab-tools")
    else:
        print(f"Error: tools_start={tools_start}, tools_end={tools_end}")

    # 4. Replace tab-you block with build_settings_html()
    you_start = html.find('<div id="tab-you"')
    main_end = html.find('</main>')
    if you_start != -1 and main_end != -1:
        idx_before_main = html.rfind('</div>', you_start, main_end)
        if idx_before_main != -1:
            old_you_block = html[you_start:idx_before_main + 6]
            html = html[:you_start] + build_settings_html() + html[idx_before_main + 6:]
            print("Successfully replaced tab-you block with tab-settings!")
        else:
            print("Error: could not find end of tab-you")
    else:
        print(f"Error: you_start={you_start}, main_end={main_end}")

    # 5. Replace modal-screentime block
    st_start = html.find('<dialog id="modal-screentime"')
    if st_start != -1:
        st_end = html.find('</dialog>', st_start)
        if st_end != -1:
            old_st_block = html[st_start:st_end + 9]
            combined_modals = build_screentime_modal_html() + "\n\n" + build_recycle_bin_modal_html()
            html = html[:st_start] + combined_modals + html[st_end + 9:]
            print("Successfully replaced modal-screentime and inserted modal-recycle-bin!")
        else:
            print("Error: could not find end of modal-screentime")
    else:
        print("Warning: modal-screentime start not found")

    # 6. Add extra JS functions before closing </script>
    script_end = html.rfind('</script>')
    if script_end != -1:
        html = html[:script_end] + "\n" + build_extra_js() + "\n" + html[script_end:]
        print("Successfully injected extra JS bridge & helpers!")

    # 7. Add refreshOverlayState() to DOMContentLoaded
    dom_loaded = "renderHeatmap();"
    if dom_loaded in html:
        html = html.replace(dom_loaded, dom_loaded + "\n      refreshOverlayState();\n      loadScreenTimeLive();")
        print("Injected refreshOverlayState & loadScreenTimeLive to DOMContentLoaded")

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(html)

    print(f"Saved updated {file_path}: {len(html)} bytes")

# Execute
apply_transforms('D:/software/V07-Android/app/src/main/assets/v3/index.html')
# Also mirror to app/src/main/assets/index.html
apply_transforms('D:/software/V07-Android/app/src/main/assets/index.html')
