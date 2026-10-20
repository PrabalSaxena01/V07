"""
parts_panels.py
Contains the 5 Core System Pillars:
1. Cockpit (Sanctuary Cockpit with Integrated Focus Chronometer, Obligations, and Active Plugin Trays)
2. Analytics (High-Clarity Data & Stats, Telemetry Matrix, Pacing & Focus Charts)
3. Ledger (Sovereign Chronological Event Stream with Fast Capture & Filtering)
4. Plugins Hub (Modular Plugin Catalog, Permissions Inspector, Sandboxing & State Management)
5. Vault (Dual Engine Diagnostics, SQLite vs Firestore Sync, JSON Export/Import)
Follows Impeccable UX (44px+ touch targets, clean typography, tactile feedback) and Taste (Zero AI slop, Dark slate palette).
"""

PANELS_HTML = r'''
      <!-- ======================================================================
           PILLAR 1: SANCTUARY COCKPIT (Compact High-Density Cockpit)
           Zero-Scroll Ergonomics on Desktop & Tablets; Adaptive Segmented View on Mobile
           ====================================================================== -->
      <section id="panel-cockpit" class="tab-panel flex flex-col gap-2.5 sm:gap-3 fade-in">
        <!-- 1. Unified Executive Cockpit Bar (High Density, Zero Layout Shift) -->
        <div class="px-3 py-2 sm:px-4 sm:py-2 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-wrap md:flex-nowrap items-center justify-between gap-2">
          <!-- Left: Identity & Date Badge -->
          <div class="flex items-center gap-2 min-w-0 shrink-0">
            <div class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full bg-[var(--emerald)] pulse-online"></span>
              <span class="text-xs sm:text-sm font-bold tracking-tight text-[var(--text)]">Home Sanctuary</span>
            </div>
            <span class="text-[var(--border)] hidden sm:inline">|</span>
            <span id="dashTodayPill" class="text-xs font-mono font-medium text-[var(--text-secondary)] tabular-nums shrink-0"></span>
          </div>

          <!-- Center: 5 Micro KPI Badges in a Sleek Cluster -->
          <div class="flex items-center gap-1.5 sm:gap-2 overflow-x-auto py-0.5 no-scrollbar shrink min-w-0">
            <!-- Focus KPI Pill -->
            <div onclick="selectCockpitFocus()" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--emerald)] cursor-pointer transition-colors btn-press shrink-0" title="Focus time today">
              <span class="material-symbols-outlined text-[14px] text-[var(--emerald)]">timer</span>
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase hidden sm:inline">Focus:</span>
              <span id="dashFocusTotal" class="text-xs font-mono font-bold text-[var(--emerald)] tabular-nums">0m</span>
              <span id="dashFocusSub" class="hidden">0 sessions</span>
            </div>

            <!-- Tasks KPI Pill -->
            <div onclick="selectCockpitTasks()" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--primary)] cursor-pointer transition-colors btn-press shrink-0" title="Tasks completed today">
              <span class="material-symbols-outlined text-[14px] text-[var(--text-secondary)]">checklist</span>
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase hidden sm:inline">Tasks:</span>
              <span id="dashTasksTotal" class="text-xs font-mono font-bold text-[var(--text)] tabular-nums">0 / 0</span>
              <div class="hidden"><div id="dashTasksBar" style="width: 0%"></div></div>
            </div>

            <!-- Habits KPI Pill -->
            <div onclick="launchPluginSandbox('com.v07.habits')" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--emerald)] cursor-pointer transition-colors btn-press shrink-0" title="Active habit streak">
              <span class="material-symbols-outlined text-[14px] text-[var(--text-secondary)]">local_fire_department</span>
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase hidden sm:inline">Habits:</span>
              <span id="dashHabitsStreak" class="text-xs font-mono font-bold text-[var(--text)] tabular-nums">0 Days</span>
              <span id="dashHabitsToday" class="hidden">0</span>
            </div>

            <!-- Screen Time KPI Pill (Integrated Digital Vitals) -->
            <div id="dashScreenTimePill" onclick="selectCockpitScreentime()" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--emerald)] cursor-pointer transition-colors btn-press shrink-0" title="Screen time telemetry today">
              <span class="material-symbols-outlined text-[14px] text-[var(--text-muted)]">smartphone</span>
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase hidden sm:inline">Screen:</span>
              <span id="dashScreenTimeTotal" class="text-xs font-mono font-bold text-[var(--text)] tabular-nums">--</span>
              <span id="dashScreenTimeSub" class="hidden">No Data</span>
            </div>

            <!-- Rank KPI Pill -->
            <div onclick="launchPluginSandbox('com.v07.gamify')" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--border-focus)] cursor-pointer transition-colors btn-press shrink-0" title="Player rank and level">
              <span class="material-symbols-outlined text-[14px] text-[var(--text-muted)]">military_tech</span>
              <span id="dashLevelTitle" class="text-xs font-mono font-bold text-[var(--text)]">LVL 1</span>
              <span id="dashRankBadge" class="text-[9px] font-mono font-bold px-1.5 py-0.2 rounded bg-[var(--surface)] text-[var(--text-muted)] border border-[var(--border)] hidden md:inline">INITIATE</span>
              <span id="dashXpLabel" class="hidden">0 XP</span>
            </div>
          </div>

          <!-- Right: Compact Fast Action Triggers -->
          <div class="flex items-center gap-1.5 shrink-0">
            <button onclick="openNewTaskModal()" class="px-2.5 h-7 sm:h-8 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text)] hover:text-[var(--primary-text)] hover:bg-[var(--primary)] text-xs font-semibold flex items-center gap-1 transition-colors btn-press" title="New Obligation">
              <span class="material-symbols-outlined text-[15px]">add_task</span>
              <span class="hidden sm:inline">Task</span>
            </button>
            <button onclick="openNewThoughtModal()" class="px-2.5 h-7 sm:h-8 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text)] hover:border-[var(--emerald)] text-xs font-semibold flex items-center gap-1 transition-colors btn-press" title="Record Sovereign Thought">
              <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">psychology</span>
              <span class="hidden sm:inline">Thought</span>
            </button>
            <button onclick="switchTab('analytics')" class="px-2.5 h-7 sm:h-8 rounded-lg bg-[var(--primary-dim)] text-[var(--primary)] border border-[var(--primary)]/20 hover:bg-[var(--primary)] hover:text-[var(--primary-text)] text-xs font-bold flex items-center gap-1 transition-all btn-press" title="View Full Analytics">
              <span class="material-symbols-outlined text-[15px]">insights</span>
              <span>Stats</span>
            </button>
          </div>
        </div>

        <!-- Mobile Segmented View Switcher (< 1024px: Zero-Scroll Adaptive Cockpit) -->
        <div class="lg:hidden flex items-center bg-[var(--surface)] p-1 rounded-xl border border-[var(--border)] gap-1 shadow-xs">
          <button onclick="setMobileCockpitTab('focus')" id="mobileCockpitTab-focus" class="flex-1 py-1.5 px-2 rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-all bg-[var(--surface-alt)] text-[var(--text)] border border-[var(--border)] shadow-xs btn-press">
            <span class="material-symbols-outlined text-[15px] text-[var(--emerald)]">timer</span>
            <span>Focus</span>
          </button>
          <button onclick="setMobileCockpitTab('obligations')" id="mobileCockpitTab-obligations" class="flex-1 py-1.5 px-2 rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-all text-[var(--text-secondary)] hover:text-[var(--text)] border border-transparent btn-press">
            <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">checklist</span>
            <span>Tasks</span>
            <span id="mobileTasksBadge" class="px-1.5 py-0.2 rounded text-[9px] font-mono bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]">0</span>
          </button>
          <button onclick="setMobileCockpitTab('tools')" id="mobileCockpitTab-tools" class="flex-1 py-1.5 px-2 rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-all text-[var(--text-secondary)] hover:text-[var(--text)] border border-transparent btn-press">
            <span class="material-symbols-outlined text-[15px] text-[var(--text-secondary)]">grid_view</span>
            <span>Tools</span>
          </button>
          <button onclick="setMobileCockpitTab('all')" id="mobileCockpitTab-all" class="py-1.5 px-2.5 rounded-lg text-xs font-semibold flex items-center justify-center gap-1 transition-all text-[var(--text-muted)] hover:text-[var(--text)] border border-transparent btn-press" title="View all sections stacked">
            <span class="material-symbols-outlined text-[15px]">view_agenda</span>
            <span class="text-[10px] hidden sm:inline">All</span>
          </button>
        </div>

        <!-- 2. High-Efficiency 3-Column Command Workspace (Zero-Scroll on Desktop & High-Density on Mobile) -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-2.5 sm:gap-3 items-stretch">
          
          <!-- Column 1 (lg:col-span-5 xl:col-span-4): Integrated Focus Chronometer -->
          <div id="chronoSection" class="mobile-cockpit-section flex lg:!flex lg:col-span-5 xl:col-span-4 p-3 sm:p-4 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex-col items-center justify-between text-center gap-2 min-h-[340px] max-h-[385px]">
            <!-- Header & Target Switcher -->
            <div class="w-full flex items-center justify-between">
              <div class="flex items-center gap-1.5">
                <span class="w-2 h-2 rounded-full bg-[var(--emerald)] pulse-online"></span>
                <span class="text-xs font-bold tracking-tight text-[var(--text)]">Focus Chronometer</span>
              </div>
              <!-- Presets -->
              <div class="flex items-center bg-[var(--surface-alt)] border border-[var(--border)] rounded-md p-0.5 text-[10px] sm:text-[11px] font-mono font-semibold gap-0.5">
                <button onclick="setChronoTargetMinutes(0)" id="chronoBtn0" class="chrono-tgt-btn px-1.5 sm:px-2 py-0.5 rounded bg-[var(--primary)] text-white btn-press">Flow</button>
                <button onclick="setChronoTargetMinutes(15)" id="chronoBtn15" class="chrono-tgt-btn px-1.5 py-0.5 rounded text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">15m</button>
                <button onclick="setChronoTargetMinutes(25)" id="chronoBtn25" class="chrono-tgt-btn px-1.5 py-0.5 rounded text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">25m</button>
                <button onclick="setChronoTargetMinutes(45)" id="chronoBtn45" class="chrono-tgt-btn px-1.5 py-0.5 rounded text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">45m</button>
                <button onclick="setChronoTargetMinutes(60)" id="chronoBtn60" class="chrono-tgt-btn px-1.5 py-0.5 rounded text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">60m</button>
                <input type="number" id="chronoCustomMinutes" min="1" max="180" placeholder="Min" class="w-9 px-1 py-0.5 text-[10px] font-mono rounded bg-[var(--surface)] border border-[var(--border)] text-[var(--text)] text-center focus:outline-none" onkeydown="if(event.key==='Enter') applyCustomChronoMinutes()">
              </div>
            </div>

            <!-- Compact Radial Dial Gauge -->
            <div class="relative w-28 h-28 sm:w-32 sm:h-32 flex items-center justify-center my-0.5">
              <svg class="w-full h-full transform -rotate-90" viewBox="0 0 240 240">
                <circle cx="120" cy="120" r="102" stroke="var(--border)" stroke-width="7" fill="none" />
                <circle id="chronoCircleProgress" cx="120" cy="120" r="102" stroke="var(--emerald)" stroke-width="7" stroke-linecap="round" fill="none" stroke-dasharray="640.88" stroke-dashoffset="0" class="transition-all duration-300" />
              </svg>
              <div class="absolute flex flex-col items-center justify-center px-2">
                <span id="chronoDisplayDigits" class="text-2xl sm:text-3xl font-mono font-bold tabular-nums tracking-tight text-[var(--text)]">00:00</span>
                <span id="chronoTargetDisplayLabel" class="text-[9px] sm:text-[10px] font-mono font-semibold uppercase tracking-wider text-[var(--emerald)] mt-0.5">Open Flow</span>
                <span id="chronoStatusLabel" class="text-[9px] text-[var(--text-muted)] font-mono">Ready</span>
              </div>
            </div>

            <!-- Activity Selection Pills (Anti-Slop Clean Typography & Vector Accents) -->
            <div class="flex items-center justify-center gap-1 flex-wrap w-full">
              <button onclick="selectChronoActivity('deepwork')" data-act-btn="deepwork" class="act-pill px-2 py-0.5 rounded-md bg-[var(--surface-alt)] border border-[var(--emerald)] text-[var(--emerald)] text-[10px] sm:text-[11px] font-semibold btn-press">Deep Work</button>
              <button onclick="selectChronoActivity('coding')" data-act-btn="coding" class="act-pill px-2 py-0.5 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] text-[10px] sm:text-[11px] font-semibold btn-press">Code</button>
              <button onclick="selectChronoActivity('reading')" data-act-btn="reading" class="act-pill px-2 py-0.5 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] text-[10px] sm:text-[11px] font-semibold btn-press">Read</button>
              <button onclick="selectChronoActivity('writing')" data-act-btn="writing" class="act-pill px-2 py-0.5 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] text-[10px] sm:text-[11px] font-semibold btn-press">Write</button>
              <button onclick="selectChronoActivity('meditation')" data-act-btn="meditation" class="act-pill px-2 py-0.5 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] text-[10px] sm:text-[11px] font-semibold btn-press">Stillness</button>
            </div>

            <!-- Optional Focus Note -->
            <input type="text" id="chronoSessionNotes" placeholder="What are you focusing on? (optional)" class="w-full px-2.5 py-1 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-xs text-[var(--text)] placeholder-[var(--text-muted)] focus:outline-none focus:border-[var(--primary)] text-center transition-colors">

            <!-- Primary Control Buttons (min 44px ergonomics) -->
            <div class="flex items-center gap-2 w-full justify-center">
              <button onclick="toggleChronoStartPause()" id="chronoMainBtn" class="flex-1 h-8 sm:h-9 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold flex items-center justify-center gap-1.5 hover:bg-[var(--primary-hover)] shadow-xs btn-press">
                <span id="chronoMainBtnIcon" class="material-symbols-outlined text-[17px]">play_arrow</span>
                <span id="chronoMainBtnText">Start Focus</span>
              </button>
              <button onclick="finishChronoSession()" id="chronoFinishBtn" class="hidden px-3 h-8 sm:h-9 rounded-lg bg-[var(--emerald)] text-[var(--text-inverse)] text-xs font-bold items-center gap-1 hover:opacity-90 btn-press">
                <span class="material-symbols-outlined text-[16px]">check</span>
                <span>Log</span>
              </button>
              <button onclick="resetChronoSession()" id="chronoResetBtn" class="w-8 h-8 sm:w-9 sm:h-9 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--rose)] hover:border-[var(--rose)] flex items-center justify-center btn-press shrink-0" title="Reset">
                <span class="material-symbols-outlined text-[16px]">refresh</span>
              </button>
            </div>
          </div>

          <!-- Column 2 (lg:col-span-4 xl:col-span-5): Today's Directives & Obligations Checklist -->
          <div id="obligationsSection" class="mobile-cockpit-section hidden lg:!flex lg:col-span-4 xl:col-span-5 p-3 sm:p-4 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex-col justify-between gap-2 min-h-[340px] max-h-[385px]">
            <div>
              <div class="flex items-center justify-between pb-1.5 border-b border-[var(--border)]">
                <div class="flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-[17px] text-[var(--primary)]">checklist</span>
                  <h3 class="text-xs font-bold uppercase tracking-wider text-[var(--text)]">Today's Obligations</h3>
                </div>
                <button onclick="openNewTaskModal()" class="text-[11px] font-bold text-[var(--primary)] hover:underline flex items-center gap-0.5">
                  <span class="material-symbols-outlined text-[13px]">add</span>
                  <span>New</span>
                </button>
              </div>

              <!-- Quick Task Inline Input -->
              <div class="flex items-center gap-1.5 p-1 bg-[var(--surface-alt)] rounded-lg border border-[var(--border)] mt-2">
                <input type="text" id="quickTaskInput" placeholder="Add task for today..." class="flex-1 px-2 py-1 text-xs bg-transparent border-none text-[var(--text)] placeholder-[var(--text-muted)] focus:outline-none" onkeydown="if(event.key==='Enter') addQuickTask()">
                <select id="quickTaskPrio" class="bg-[var(--surface)] text-[10px] font-mono text-[var(--text-secondary)] rounded px-1.5 py-1 border border-[var(--border)] focus:outline-none">
                  <option value="medium">MED</option>
                  <option value="high">HIGH</option>
                  <option value="low">LOW</option>
                </select>
                <button onclick="addQuickTask()" class="px-2.5 py-1 bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold rounded hover:bg-[var(--primary-hover)] btn-press">
                  +
                </button>
              </div>

              <!-- Scrollable Tasks List (Compact max-height prevents viewport blowout) -->
              <div id="dashTasksList" class="flex flex-col gap-1.5 mt-2 max-h-[185px] sm:max-h-[210px] overflow-y-auto pr-1 custom-scrollbar">
                <!-- Dynamically populated by renderDashboard() -->
              </div>
            </div>

            <!-- Footer Task Summary Ratio -->
            <div class="pt-2 border-t border-[var(--border)] flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)]">
              <span>Remaining Focus:</span>
              <button onclick="switchTab('ledger')" class="text-[var(--primary)] hover:underline font-sans font-semibold text-xs">View Ledger History →</button>
            </div>
          </div>

          <!-- Column 3 (lg:col-span-3 xl:col-span-3): Digital Vitals (Screen Time Card) + Installed Utilities -->
          <div id="toolsSection" class="mobile-cockpit-section hidden lg:!flex lg:col-span-3 xl:col-span-3 flex-col justify-between gap-2.5 min-h-[340px] max-h-[385px]">
            
            <!-- Box 1: Digital Vitals & Device Screentime Card (Dedicated Missing State per /taste) -->
            <div id="cardScreentimeContainer" class="p-3 sm:p-3.5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between min-h-[160px] max-h-[175px]">
              <div class="flex items-center justify-between pb-1 border-b border-[var(--border)]">
                <div class="flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-[16px] text-[var(--primary)]">smartphone</span>
                  <h3 class="text-xs font-bold uppercase tracking-wider text-[var(--text)]">Screen Time & Vitals</h3>
                </div>
                <span id="screenTimeStatusBadge" class="text-[9px] font-mono font-semibold px-1.5 py-0.5 rounded bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]">AWAITING SYNC</span>
              </div>

              <!-- Missing Screentime State (Primary user requirement) -->
              <div id="screenTimeMissingState" class="flex flex-col justify-between flex-1 gap-2 pt-1.5">
                <div class="flex items-start gap-2.5">
                  <div class="w-8 h-8 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex items-center justify-center shrink-0 text-[var(--text-muted)]">
                    <span class="material-symbols-outlined text-[17px]">hourglass_disabled</span>
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="text-xs font-semibold text-[var(--text)]">No Screen Time Data</div>
                    <p class="text-[11px] text-[var(--text-muted)] leading-snug mt-0.5">
                      Device usage stats not recorded for today. Grant 'Usage Access' on Android or sync your mobile companion.
                    </p>
                  </div>
                </div>
                <div class="flex items-center gap-2 pt-1 border-t border-[var(--border)]">
                  <button onclick="triggerManualSync()" class="flex-1 h-7 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--emerald)] text-[11px] font-semibold text-[var(--text)] flex items-center justify-center gap-1 transition-colors btn-press" title="Sync from phone">
                    <span class="material-symbols-outlined text-[13px]">sync</span>
                    <span>Sync Device</span>
                  </button>
                  <button onclick="openLogScreenTimeModal()" class="flex-1 h-7 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--primary)] text-[11px] font-semibold text-[var(--text)] flex items-center justify-center gap-1 transition-colors btn-press" title="Manually enter screentime">
                    <span class="material-symbols-outlined text-[13px]">add</span>
                    <span>Log Manually</span>
                  </button>
                </div>
              </div>

              <!-- Active / Synced Screentime State (Revealed when screentime data exists) -->
              <div id="screenTimeActiveState" class="hidden flex flex-col justify-between flex-1 gap-2 pt-1.5">
                <div class="flex items-center justify-between">
                  <div>
                    <div id="screenTimeActiveTotal" class="text-xl font-mono font-bold text-[var(--text)] tabular-nums">0h 0m</div>
                    <div id="screenTimeActiveSub" class="text-[10px] font-mono text-[var(--text-muted)]">Top: Android System</div>
                  </div>
                  <div class="text-right">
                    <div id="screenTimeRatio" class="text-xs font-mono font-bold text-[var(--emerald)]">--</div>
                    <div class="text-[9px] font-mono text-[var(--text-muted)]">Focus vs Screen</div>
                  </div>
                </div>
                <div class="flex items-center justify-between pt-1 border-t border-[var(--border)]">
                  <span id="screenTimeDeviceModel" class="text-[10px] font-mono text-[var(--text-muted)] truncate">Android Companion</span>
                  <button onclick="openLogScreenTimeModal()" class="text-[10px] font-bold text-[var(--primary)] hover:underline">Edit Entry →</button>
                </div>
              </div>
            </div>

            <!-- Box 2: Installed Utilities Shelf + Integrated Soundscape -->
            <div id="cardUtilitiesShelf" class="p-3 sm:p-3.5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between flex-1 min-h-[175px] max-h-[195px]">
              <div>
                <div class="flex items-center justify-between pb-1 border-b border-[var(--border)]">
                  <div class="flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[16px] text-[var(--text-muted)]">grid_view</span>
                    <h3 class="text-xs font-bold uppercase tracking-wider text-[var(--text)]">Utilities & Tools</h3>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <button id="dashRefreshPluginsBtn" onclick="refreshPluginsRegistry()" class="p-1 rounded-md text-[var(--text-muted)] hover:text-[var(--text)] hover:bg-[var(--surface-alt)] transition-colors btn-press" title="Sync & Refresh Plugins with App">
                      <span class="material-symbols-outlined text-[15px]" id="dashRefreshIcon">refresh</span>
                    </button>
                    <button onclick="switchTab('plugins')" class="text-[11px] font-bold text-[var(--primary)] hover:underline">Hub →</button>
                  </div>
                </div>

                <!-- Compact Plugin Launcher Tiles Container -->
                <div id="dashPluginCardsGrid" class="flex flex-col gap-1.5 mt-1.5 overflow-y-auto max-h-[90px] pr-1 custom-scrollbar">
                  <!-- Dynamically populated by renderDashboardPlugins() -->
                </div>
              </div>

              <!-- Integrated Ambient Soundscape Bar -->
              <div class="pt-1.5 border-t border-[var(--border)] flex items-center justify-between gap-2">
                <div class="flex items-center gap-1.5 min-w-0">
                  <span class="material-symbols-outlined text-[15px] text-[var(--text-muted)]">graphic_eq</span>
                  <span class="text-xs font-semibold text-[var(--text)] truncate">Soundscape</span>
                  <span id="soundscapeStatusBadge" class="text-[9px] font-mono px-1 rounded bg-[var(--surface-alt)] text-[var(--text-muted)] border border-[var(--border)]">OFF</span>
                </div>
                <button onclick="toggleSoundscape()" id="soundscapeBtn" class="px-2.5 py-0.5 rounded-md bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--primary)] text-[11px] font-semibold text-[var(--text)] flex items-center gap-1 btn-press transition-colors shrink-0">
                  <span id="soundscapeIcon" class="material-symbols-outlined text-[13px]">play_arrow</span>
                  <span id="soundscapeLabel">Ambient</span>
                </button>
              </div>

              <!-- Hidden preservation of compatibility DOM IDs -->
              <div class="hidden">
                <span id="dashVerseId">Gita 2.47</span>
                <p id="dashVerseText">Preserved verse text</p>
                <div id="dashTimelineStream"></div>
              </div>
            </div>

          </div>

        </div>
      </section>

      <!-- ======================================================================
           PILLAR 2: DATA & ANALYTICS (Intuitive & Easy to Analyze Telemetry)
           ====================================================================== -->
      <section id="panel-analytics" class="tab-panel hidden flex-col gap-6 fade-in">
        <!-- Analytics Header & Horizon Time Selector -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs">
          <div>
            <h2 class="text-lg font-bold text-[var(--text)]">Discipline & Telemetry Analytics</h2>
            <p class="text-xs text-[var(--text-muted)] mt-0.5">Track your focus volume, device screen time, execution velocity, and consistency across time horizons.</p>
          </div>

          <!-- Time Horizon Range Switcher -->
          <div class="flex items-center bg-[var(--surface-alt)] border border-[var(--border)] rounded-lg p-1 text-xs font-mono font-semibold gap-1 self-start sm:self-auto">
            <button onclick="setAnalyticsRange(7)" id="rangeBtn7" class="range-btn px-3 py-1.5 rounded-md text-[var(--text-secondary)] hover:text-[var(--text)] transition-all btn-press">Last 7d</button>
            <button onclick="setAnalyticsRange(14)" id="rangeBtn14" class="range-btn active px-3 py-1.5 rounded-md bg-[var(--primary)] text-white transition-all btn-press">Last 14d</button>
            <button onclick="setAnalyticsRange(30)" id="rangeBtn30" class="range-btn px-3 py-1.5 rounded-md text-[var(--text-secondary)] hover:text-[var(--text)] transition-all btn-press">Last 30d</button>
          </div>
        </div>

        <!-- 5 Comparative Analytical KPI Scorecards (Clean Anti-Slop Bento) -->
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2.5 sm:gap-3">
          <!-- Total Focus Volume -->
          <div class="p-3.5 rounded-lg bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[var(--text-secondary)]">Focus Volume</span>
              <span class="material-symbols-outlined text-[16px] text-[var(--emerald)]">timer</span>
            </div>
            <div class="mt-2.5">
              <div id="analyticsTotalFocus" class="text-lg sm:text-xl font-bold font-mono text-[var(--text)] tabular-nums">0h 0m</div>
              <div class="flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)] mt-1">
                <span>Daily Avg:</span>
                <span id="analyticsAvgFocus" class="text-[var(--emerald)] font-bold">0m/day</span>
              </div>
            </div>
          </div>

          <!-- Digital Screen Time (Dedicated Telemetry Scorecard) -->
          <div class="p-3.5 rounded-lg bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[var(--text-secondary)]">Screen Time</span>
              <span class="material-symbols-outlined text-[16px] text-[var(--text-muted)]">smartphone</span>
            </div>
            <div class="mt-2.5">
              <div id="analyticsTotalScreenTime" class="text-lg sm:text-xl font-bold font-mono text-[var(--text)] tabular-nums">--</div>
              <div class="flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)] mt-1">
                <span>Daily Avg:</span>
                <span id="analyticsAvgScreenTime" class="text-[var(--text-secondary)] font-bold">--</span>
              </div>
            </div>
          </div>

          <!-- Task Execution Velocity -->
          <div class="p-3.5 rounded-lg bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[var(--text-secondary)]">Task Velocity</span>
              <span class="material-symbols-outlined text-[16px] text-[var(--primary)]">speed</span>
            </div>
            <div class="mt-2.5">
              <div id="analyticsTaskVelocity" class="text-lg sm:text-xl font-bold font-mono text-[var(--text)] tabular-nums">0%</div>
              <div class="flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)] mt-1">
                <span>Completed:</span>
                <span id="analyticsTasksDoneRatio" class="text-[var(--primary)] font-bold">0 / 0</span>
              </div>
            </div>
          </div>

          <!-- Habit Consistency Score -->
          <div class="p-3.5 rounded-lg bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[var(--text-secondary)]">Habit Adherence</span>
              <span class="material-symbols-outlined text-[16px] text-[var(--text-secondary)]">local_fire_department</span>
            </div>
            <div class="mt-2.5">
              <div id="analyticsHabitAdherence" class="text-lg sm:text-xl font-bold font-mono text-[var(--text)] tabular-nums">0%</div>
              <div class="flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)] mt-1">
                <span>Streak:</span>
                <span id="analyticsActiveStreaks" class="text-[var(--text-secondary)] font-bold">0 days</span>
              </div>
            </div>
          </div>

          <!-- Sovereign Events Logged -->
          <div class="p-3.5 rounded-lg bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col justify-between col-span-2 sm:col-span-1">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[var(--text-secondary)]">Sovereign Entries</span>
              <span class="material-symbols-outlined text-[16px] text-[var(--text-muted)]">database</span>
            </div>
            <div class="mt-2.5">
              <div id="analyticsTotalEntries" class="text-lg sm:text-xl font-bold font-mono text-[var(--text)] tabular-nums">0</div>
              <div class="flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)] mt-1">
                <span>Top Activity:</span>
                <span id="analyticsTopCategory" class="text-[var(--text-secondary)] font-bold truncate">Focus</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Visual Analytics Charts (High-Density Dual Canvas) -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Focus Minutes Daily Volume Chart -->
          <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-[var(--emerald)]"></span>
                <h3 class="text-xs font-bold text-[var(--text)]">Daily Focus Distribution (Minutes)</h3>
              </div>
              <span class="text-[10px] font-mono text-[var(--text-muted)]">Target: 90m / day</span>
            </div>
            <div class="h-64 w-full relative">
              <canvas id="chartFocusVolume"></canvas>
              <!-- Hidden fallback canvas for legacy scripts -->
              <canvas id="chartSessions" class="hidden"></canvas>
            </div>
          </div>

          <!-- Activity Volume & Consistency Trend Chart -->
          <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-[var(--primary)]"></span>
                <h3 class="text-xs font-bold text-[var(--text)]">Discipline & Ledger Pacing Trend</h3>
              </div>
              <span class="text-[10px] font-mono text-[var(--text-muted)]">Volume per Day</span>
            </div>
            <div class="h-64 w-full relative">
              <canvas id="chartPacingTrend"></canvas>
              <!-- Hidden fallback canvas for legacy scripts -->
              <canvas id="chartDaily" class="hidden"></canvas>
            </div>
          </div>
        </div>

        <!-- Telemetry Breakdown Matrix Table (Makes Data Easy to Analyze) -->
        <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-4">
          <div class="flex items-center justify-between flex-wrap gap-2">
            <div>
              <h3 class="text-sm font-bold text-[var(--text)]">Telemetry Comparison Matrix</h3>
              <p class="text-xs text-[var(--text-muted)] mt-0.5">Day-by-day precision breakdown of your focus time, screen time, completed tasks, and sovereign entries.</p>
            </div>
            <span class="text-[11px] font-mono text-[var(--text-muted)]">Tabular Precision</span>
          </div>

          <div class="overflow-x-auto border border-[var(--border)] rounded-lg">
            <table class="w-full text-left border-collapse text-xs">
              <thead>
                <tr class="bg-[var(--surface-alt)] border-b border-[var(--border)] text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
                  <th class="py-2.5 px-3">Date</th>
                  <th class="py-2.5 px-3">Focus Time</th>
                  <th class="py-2.5 px-3">Screen Time</th>
                  <th class="py-2.5 px-3">Tasks Completed</th>
                  <th class="py-2.5 px-3">Habits Maintained</th>
                  <th class="py-2.5 px-3">Sovereign Logs</th>
                  <th class="py-2.5 px-3 text-right">Flow Rating</th>
                </tr>
              </thead>
              <tbody id="analyticsMatrixBody" class="divide-y divide-[var(--border)] font-mono">
                <!-- Dynamically generated rows -->
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- ======================================================================
           PILLAR 3: SOVEREIGN LEDGER (Events, Thoughts, and Reflections Stream)
           ====================================================================== -->
      <section id="panel-ledger" class="tab-panel hidden flex-col gap-6 fade-in max-w-5xl mx-auto w-full">
        <!-- Ledger Header & Inline Quick Capture -->
        <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-4">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <div class="flex items-center gap-2">
                <span class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--violet)] px-2 py-0.5 rounded bg-[var(--violet-dim)] border border-[var(--violet)]/20">Sovereign Audit Trail</span>
                <span id="navLedgerCount" class="text-xs font-mono font-bold text-[var(--text-muted)]">0 Entries</span>
              </div>
              <h2 class="text-lg font-bold text-[var(--text)] mt-1">Sovereign Ledger Stream</h2>
              <p class="text-xs text-[var(--text-muted)]">Every insight, session, vital metric, and completed obligation preserved forever.</p>
            </div>
            <div class="flex items-center gap-2">
              <button onclick="openNewThoughtModal()" class="px-3.5 h-9 rounded-lg bg-[var(--violet-dim)] text-[var(--violet)] border border-[var(--violet)]/20 hover:bg-[var(--violet)] hover:text-white text-xs font-bold flex items-center gap-1.5 transition-colors btn-press">
                <span class="material-symbols-outlined text-[16px]">psychology</span>
                <span>Record Thought</span>
              </button>
              <button onclick="openNewEventModal()" class="px-3.5 h-9 rounded-lg bg-[var(--primary)] text-white text-xs font-bold flex items-center gap-1.5 hover:bg-[var(--primary-hover)] transition-colors btn-press">
                <span class="material-symbols-outlined text-[16px]">add</span>
                <span>Log Event</span>
              </button>
            </div>
          </div>

          <!-- Inline Quick Thought / Note Box -->
          <div class="flex flex-col gap-2 p-3 bg-[var(--surface-alt)] rounded-lg border border-[var(--border)]">
            <textarea id="quickThoughtInlineInput" rows="2" placeholder="Record an immediate thought, insight, or reflection..." class="w-full text-xs bg-transparent border-none text-[var(--text)] placeholder-[var(--text-muted)] focus:outline-none resize-none"></textarea>
            <div class="flex items-center justify-between pt-1 border-t border-[var(--border)]">
              <div class="flex items-center gap-2">
                <span class="text-[10px] font-mono text-[var(--text-muted)]">Category:</span>
                <select id="quickThoughtCategorySelect" class="bg-[var(--surface)] text-[11px] font-mono text-[var(--text-secondary)] rounded px-2 py-1 border border-[var(--border)] focus:outline-none">
                  <option value="thoughts">Thought / Insight</option>
                  <option value="session">Focus Session</option>
                  <option value="mood">Mood & Vitals</option>
                  <option value="general">General</option>
                </select>
              </div>
              <button onclick="submitInlineThought()" class="px-3 py-1 bg-[var(--violet)] text-white text-xs font-bold rounded hover:opacity-90 btn-press">
                Save to Ledger
              </button>
            </div>
          </div>
        </div>

        <!-- Filter & Search Controls Bar -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs">
          <!-- Category Filter Chips -->
          <div class="flex items-center gap-1.5 flex-wrap">
            <button onclick="setTimelineFilter('all')" data-timeline-filter="all" class="tl-chip active px-3 py-1 rounded-md text-xs font-semibold bg-[var(--primary)] text-white transition-all btn-press">All</button>
            <button onclick="setTimelineFilter('thoughts')" data-timeline-filter="thoughts" class="tl-chip px-3 py-1 rounded-md text-xs font-semibold bg-[var(--surface-alt)] text-[var(--text-secondary)] hover:text-[var(--text)] border border-[var(--border)] transition-all btn-press">Thoughts</button>
            <button onclick="setTimelineFilter('sessions')" data-timeline-filter="sessions" class="tl-chip px-3 py-1 rounded-md text-xs font-semibold bg-[var(--surface-alt)] text-[var(--text-secondary)] hover:text-[var(--text)] border border-[var(--border)] transition-all btn-press">Focus</button>
            <button onclick="setTimelineFilter('tasks')" data-timeline-filter="tasks" class="tl-chip px-3 py-1 rounded-md text-xs font-semibold bg-[var(--surface-alt)] text-[var(--text-secondary)] hover:text-[var(--text)] border border-[var(--border)] transition-all btn-press">Tasks</button>
            <button onclick="setTimelineFilter('habits')" data-timeline-filter="habits" class="tl-chip px-3 py-1 rounded-md text-xs font-semibold bg-[var(--surface-alt)] text-[var(--text-secondary)] hover:text-[var(--text)] border border-[var(--border)] transition-all btn-press">Habits</button>
            <button onclick="setTimelineFilter('vitals')" data-timeline-filter="vitals" class="tl-chip px-3 py-1 rounded-md text-xs font-semibold bg-[var(--surface-alt)] text-[var(--text-secondary)] hover:text-[var(--text)] border border-[var(--border)] transition-all btn-press">Vitals</button>
          </div>

          <!-- Scope Switcher & Search Bar -->
          <div class="flex items-center gap-2">
            <!-- Scope Switcher: All History vs Selected Date -->
            <div class="flex items-center bg-[var(--surface-alt)] border border-[var(--border)] rounded-md p-0.5 text-xs font-mono">
              <button onclick="setTimelineScope('date')" id="timelineScopeDate" class="px-2.5 py-1 rounded text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Date</button>
              <button onclick="setTimelineScope('all')" id="timelineScopeAll" class="px-2.5 py-1 rounded bg-[var(--primary)] text-white font-bold btn-press">All History</button>
            </div>

            <!-- Search Input -->
            <div class="relative">
              <input type="text" placeholder="Search ledger..." oninput="onTimelineSearch(this.value)" class="w-36 sm:w-48 px-2.5 py-1 text-xs rounded-md bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text)] placeholder-[var(--text-muted)] focus:outline-none focus:border-[var(--primary)]">
            </div>
          </div>
        </div>

        <!-- Ledger Events Container (Populated dynamically) -->
        <div id="timelineEventsContainer" class="flex flex-col gap-3 min-h-[200px]">
          <!-- Dynamically populated by renderTimelineView() -->
        </div>
      </section>

      <!-- ======================================================================
           PILLAR 4: PLUGINS HUB (Platform Engine, Modular Catalog & Sandbox)
           ====================================================================== -->
      <section id="panel-plugins" class="tab-panel hidden flex-col gap-6 fade-in max-w-6xl mx-auto w-full">
        <!-- Platform Header & Core vs Plugin Architecture Notice -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs">
          <div class="flex flex-col gap-1">
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--amber)] px-2 py-0.5 rounded bg-[var(--amber-dim)] border border-[var(--amber)]/20">V07 Plugin Platform</span>
              <span class="text-xs font-mono text-[var(--text-muted)]">Specification v3.0</span>
            </div>
            <h2 class="text-lg font-bold text-[var(--text)] mt-1">Modular Plugins Hub</h2>
            <p class="text-xs text-[var(--text-muted)]">First-class extensibility: Core runtime isolates plugins with strict sandboxing and sovereign sync.</p>
          </div>
          <div class="flex items-center gap-2">
            <button id="btnRefreshPlugins" onclick="refreshPluginsRegistry()" class="px-3.5 h-10 rounded-lg bg-[var(--surface-alt)] hover:bg-[var(--surface-hover)] border border-[var(--border)] text-[var(--text)] text-xs font-bold flex items-center gap-1.5 transition-colors btn-press" title="Sync & Refresh Plugins from App & Cloud">
              <span class="material-symbols-outlined text-[17px]" id="refreshPluginsIcon">refresh</span>
              <span>Refresh</span>
            </button>
            <button onclick="openModal('modalInstallPlugin')" class="px-4 h-10 rounded-lg bg-[var(--primary)] text-[var(--primary-text)] text-xs font-bold flex items-center gap-1.5 hover:bg-[var(--primary-hover)] shadow-xs transition-colors btn-press">
              <span class="material-symbols-outlined text-[17px]">add_box</span>
              <span>Install Plugin</span>
            </button>
          </div>
        </div>

        <!-- Plugin Catalog Filter Tabs -->
        <div class="flex items-center justify-between gap-3 border-b border-[var(--border)] pb-3">
          <div class="flex items-center gap-2">
            <button id="pluginFilterAllBtn" onclick="filterPluginsGrid('all')" class="plugin-filter-btn px-3 py-1.5 rounded-lg text-xs font-semibold bg-[var(--surface-alt)] text-[var(--text)] border border-[var(--border)] btn-press">All Plugins (<span id="countFilterAll">9</span>)</button>
            <button id="pluginFilterActiveBtn" onclick="filterPluginsGrid('active')" class="plugin-filter-btn px-3 py-1.5 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Active (<span id="countFilterActive">9</span>)</button>
            <button id="pluginFilterBuiltinBtn" onclick="filterPluginsGrid('builtin')" class="plugin-filter-btn px-3 py-1.5 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Starter Pack (<span id="countFilterBuiltin">9</span>)</button>
            <button id="pluginFilterCustomBtn" onclick="filterPluginsGrid('custom')" class="plugin-filter-btn px-3 py-1.5 rounded-lg text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">Custom (<span id="countFilterCustom">0</span>)</button>
          </div>
          <span class="text-[11px] font-mono text-[var(--text-muted)] hidden sm:inline" id="pluginSyncStatusLabel">Sanctuary Sync Ready</span>
        </div>

        <!-- Plugins Grid Container -->
        <div id="pluginsGridContainer" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <!-- Dynamically populated by renderPluginsGrid() -->
        </div>

        <!-- Plugin Specification Architecture Blueprint Card -->
        <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-3">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px] text-[var(--text-muted)]">code</span>
            <h4 class="text-xs font-bold text-[var(--text)]">V07 Plugin SDK & Zero-Trust Sandbox</h4>
          </div>
          <p class="text-xs text-[var(--text-muted)] leading-relaxed">
            Every plugin executes inside an isolated iframe sandbox communicating through <code class="font-mono text-[var(--text-secondary)]">window.parent.nativeInvoke(method, params)</code>. Plugins have dedicated storage namespaces (<code class="font-mono text-[var(--text-secondary)]">users/{uid}/plugin_storage/{pluginId}</code>) and cannot compromise other modules.
          </p>
        </div>
      </section>

      <!-- ======================================================================
           PILLAR 5: SOVEREIGN VAULT (Dual Engine Diagnostics & Data Portability)
           ====================================================================== -->
      <section id="panel-vault" class="tab-panel hidden flex-col gap-6 fade-in max-w-4xl mx-auto w-full">
        <!-- Vault Header & Engine Beacon -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs">
          <div>
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--emerald)] px-2 py-0.5 rounded bg-[var(--emerald-dim)] border border-[var(--emerald)]/20">Data Sovereignty</span>
              <span class="text-xs font-mono text-[var(--text-muted)]">Zero Lock-in</span>
            </div>
            <h2 class="text-lg font-bold text-[var(--text)] mt-1">Sovereign Vault & Storage Engine</h2>
            <p class="text-xs text-[var(--text-muted)]">Real-time diagnostics of dual-database storage (SQLite on Android, Firestore on Web, and Local Cache).</p>
          </div>
          <button onclick="triggerManualSync()" class="px-3.5 h-9 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] text-[var(--text)] hover:border-[var(--primary)] text-xs font-bold flex items-center gap-2 transition-colors btn-press">
            <span class="material-symbols-outlined text-[16px]">sync</span>
            <span>Trigger Sync</span>
          </button>
        </div>

        <!-- Engine Status Diagnostics Card -->
        <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-4">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-[var(--text)]">Active Storage Engine</span>
            <span id="vaultActiveEngine" class="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-[var(--surface-alt)] text-[var(--emerald)] border border-[var(--border)]">INITIALIZING</span>
          </div>
          <p id="vaultEngineDetail" class="text-xs text-[var(--text-muted)] font-mono">Examining database drivers and network bridges...</p>

          <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-2 border-t border-[var(--border)]">
            <div class="p-3 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col">
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase">Events Cached</span>
              <span id="vaultEventsCount" class="text-lg font-bold font-mono text-[var(--text)] mt-1">0</span>
            </div>
            <div class="p-3 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col">
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase">Tombstones Guarded</span>
              <span id="vaultTombstonesCount" class="text-lg font-bold font-mono text-[var(--text)] mt-1">0</span>
            </div>
            <div class="p-3 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col col-span-2 sm:col-span-1">
              <span class="text-[10px] font-mono text-[var(--text-muted)] uppercase">Cloud Sync</span>
              <span id="vaultCloudStatus" class="text-xs font-bold font-mono text-[var(--emerald)] mt-1 truncate">Synchronized</span>
            </div>
          </div>
        </div>

        <!-- Firestore Architecture & Partitioned Migration Card -->
        <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-4">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <div class="flex items-center gap-2">
                <span class="text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--primary)] px-2 py-0.5 rounded bg-[var(--primary-glow)] border border-[var(--primary)]/20">V3 Architecture</span>
                <span id="migrationStatusBadge" class="text-xs font-mono text-[var(--emerald)] font-bold">Partitioned</span>
              </div>
              <h3 class="text-sm font-bold text-[var(--text)] mt-1">Firestore Partitioning & Migration Engine</h3>
              <p class="text-xs text-[var(--text-muted)]">Segregated collections for Core Modules (sessions, tasks, thoughts), Sandboxed Plugins, and Sovereign Ledger.</p>
            </div>
            <div class="flex items-center gap-2">
              <button id="btnRunMigration" onclick="triggerPartitionMigration()" class="px-3.5 h-9 rounded-lg bg-[var(--primary)] text-white hover:bg-[var(--primary-hover)] text-xs font-bold flex items-center gap-2 transition-colors btn-press">
                <span class="material-symbols-outlined text-[16px]">transform</span>
                <span id="btnRunMigrationText">Run Migration</span>
              </button>
            </div>
          </div>

          <!-- Partition Stats Grid -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2 border-t border-[var(--border)]">
            <div class="p-2.5 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col">
              <span class="text-[9px] font-mono text-[var(--text-muted)] uppercase">Core Sessions</span>
              <span id="partSessionsCount" class="text-base font-bold font-mono text-[var(--text)] mt-0.5">0</span>
            </div>
            <div class="p-2.5 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col">
              <span class="text-[9px] font-mono text-[var(--text-muted)] uppercase">Core Tasks</span>
              <span id="partTasksCount" class="text-base font-bold font-mono text-[var(--text)] mt-0.5">0</span>
            </div>
            <div class="p-2.5 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col">
              <span class="text-[9px] font-mono text-[var(--text-muted)] uppercase">Core Thoughts</span>
              <span id="partThoughtsCount" class="text-base font-bold font-mono text-[var(--text)] mt-0.5">0</span>
            </div>
            <div class="p-2.5 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] flex flex-col">
              <span class="text-[9px] font-mono text-[var(--text-muted)] uppercase">System Ledger</span>
              <span id="partLedgerCount" class="text-base font-bold font-mono text-[var(--text)] mt-0.5">0</span>
            </div>
          </div>

          <!-- Migration Progress & Audit Results (Hidden by default) -->
          <div id="migrationAuditCard" class="hidden p-3.5 rounded-lg bg-[var(--surface-alt)] border border-[var(--emerald)]/30 flex flex-col gap-2">
            <div class="flex items-center justify-between text-xs">
              <span class="font-bold text-[var(--emerald)] flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[16px]">verified</span>
                <span id="migrationAuditTitle">Migration Audit Verified</span>
              </span>
              <span id="migrationAuditChecksum" class="font-mono text-[10px] text-[var(--text-muted)]">Legacy: 0 | Migrated: 0</span>
            </div>
            <p id="migrationAuditDetail" class="text-[11px] text-[var(--text-muted)]">Non-destructive fan-out completed with zero data loss. Legacy records preserved for rollback.</p>
          </div>
        </div>

        <!-- Data Portability & JSON Export/Import Card -->
        <div class="p-5 rounded-xl bg-[var(--surface)] border border-[var(--border)] shadow-xs flex flex-col gap-4">
          <div>
            <h3 class="text-sm font-bold text-[var(--text)]">Data Sovereignty & Portability</h3>
            <p class="text-xs text-[var(--text-muted)] mt-0.5">Export an unencrypted JSON backup of your entire ledger, tasks, and plugin storage, or restore from a previous backup.</p>
          </div>

          <div class="flex items-center gap-3 flex-wrap">
            <button onclick="exportAllDataJson()" class="px-4 h-10 rounded-lg bg-[var(--primary)] text-white text-xs font-bold flex items-center gap-2 hover:bg-[var(--primary-hover)] btn-press">
              <span class="material-symbols-outlined text-[17px]">download</span>
              <span>Export Sovereign JSON</span>
            </button>
            <label class="px-4 h-10 rounded-lg bg-[var(--surface-alt)] border border-[var(--border)] hover:border-[var(--primary)] text-xs font-bold text-[var(--text)] flex items-center gap-2 cursor-pointer btn-press">
              <span class="material-symbols-outlined text-[17px]">upload</span>
              <span>Import JSON Backup</span>
              <input type="file" id="importJsonFileInput" accept=".json" class="hidden" onchange="importDataJson(event)">
            </label>
          </div>
        </div>
      </section>

      <!-- Hidden Legacy DOM Elements for Seamless Compatibility with Legacy Plugin Utilities -->
      <div id="legacyCompatibilityElements" class="hidden">
        <select id="gitaChapterSelect"></select>
        <select id="gitaVerseSelect"></select>
        <div id="gitaVerseIdHeading"></div>
        <div id="gitaHindi"></div>
        <div id="gitaEnglish"></div>
        <div id="gitaSanskrit"></div>
        <textarea id="gitaReflectionInput"></textarea>
        <div id="tasksListContainer"></div>
        <div id="tasksProgressBar"></div>
        <div id="tasksPercentText"></div>
        <div id="habitsListContainer"></div>
        <div id="habitsSummaryRatio"></div>
        <div id="habitsDateSub"></div>
        <div id="habitsHeatmapGrid"></div>
        <div id="gamifyRankTitle"></div>
        <div id="gamifyBadgeLvl"></div>
        <div id="gamifyTotalXpDisplay"></div>
        <div id="gamifyLevelBar"></div>
        <div id="gamifyLevelRatio"></div>
        <div id="gamifyQuestsContainer"></div>
        <div id="chronoSelectedActivityLabel"></div>
        <div id="chronoHistoryList"></div>
        <input type="range" id="synthVolume" value="50">
      </div>

    </main>
  </div>

  <!-- ==========================================================================
       MOBILE FLOATING DOCK NAVIGATION (Matches V07 Android Native Floating Dock)
       Visible on < 1024px screens. Floating pill dock with rounded-full geometry.
       ========================================================================== -->
  <nav class="lg:hidden fixed bottom-0 left-0 right-0 z-40 pb-safe flex justify-center px-3 mb-2.5 pointer-events-none select-none">
    <div class="pointer-events-auto w-full max-w-[360px] h-14 rounded-full bg-[var(--surface-glass)] backdrop-blur-xl border border-[var(--border)] floating-dock-pill px-1 flex items-center justify-around">
      <button onclick="switchTab('cockpit')" data-mobile-tab="cockpit" class="mobile-nav-btn active flex-1 h-full min-w-0 flex flex-col items-center justify-center text-[var(--primary)] btn-press">
        <span class="material-symbols-outlined text-[19px]">spa</span>
        <span class="text-[9px] font-medium tracking-tight truncate">Home</span>
      </button>
      <button onclick="switchTab('plugins')" data-mobile-tab="plugins" class="mobile-nav-btn flex-1 h-full min-w-0 flex flex-col items-center justify-center text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">
        <span class="material-symbols-outlined text-[19px]">grid_view</span>
        <span class="text-[9px] font-medium tracking-tight truncate">Tools</span>
      </button>
      <button onclick="switchTab('ledger')" data-mobile-tab="ledger" class="mobile-nav-btn flex-1 h-full min-w-0 flex flex-col items-center justify-center text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">
        <span class="material-symbols-outlined text-[19px]">history_edu</span>
        <span class="text-[9px] font-medium tracking-tight truncate">Ledger</span>
      </button>
      <button onclick="switchTab('analytics')" data-mobile-tab="analytics" class="mobile-nav-btn flex-1 h-full min-w-0 flex flex-col items-center justify-center text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">
        <span class="material-symbols-outlined text-[19px]">insights</span>
        <span class="text-[9px] font-medium tracking-tight truncate">Day</span>
      </button>
      <button onclick="openMobileDrawer()" class="flex-1 h-full min-w-0 flex flex-col items-center justify-center text-[var(--text-secondary)] hover:text-[var(--text)] btn-press">
        <span class="material-symbols-outlined text-[19px]">settings</span>
        <span class="text-[9px] font-medium tracking-tight truncate">Settings</span>
      </button>
    </div>
  </nav>
'''
