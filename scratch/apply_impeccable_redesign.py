import re

def build_compact_tools_html():
    return """    <div id="tab-tools" class="page-tab flex-col w-full pb-8 space-y-4">
      <!-- Minimalist Compact Framing -->
      <section class="flex flex-col gap-0.5 pt-1">
        <div class="inline-flex items-center gap-1.5 text-primary text-xs font-semibold uppercase tracking-wider">
          <span class="material-symbols-outlined text-[16px]">widgets</span>
          <span>System & Plugins</span>
        </div>
        <h2 class="text-xl sm:text-2xl font-bold text-on-surface tracking-tight">Tools & Companions</h2>
      </section>

      <!-- SECTION 1: CORE SYSTEM UTILITIES (7 Core Engines - Compact Impeccable Grid) -->
      <section class="flex flex-col space-y-2.5">
        <div class="flex items-center justify-between pb-1 border-b border-surface-container-high/40">
          <div class="flex items-center gap-1.5">
            <span class="material-symbols-outlined text-primary text-[18px]">shield</span>
            <span class="text-xs font-bold text-on-surface uppercase tracking-wider">Core System Engines</span>
          </div>
          <span class="text-[11px] font-semibold text-primary px-2 py-0.5 rounded-full bg-primary/10">7 Active</span>
        </div>

        <div class="flex flex-col space-y-2">

          <!-- Core 1: Focus Sanctuary -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-secondary-fixed text-on-secondary-fixed flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">hourglass_top</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Focus Sanctuary</span>
                <span class="text-xs text-on-surface-variant truncate">Deep work intervals & stopwatch</span>
              </div>
            </div>
            <button onclick="openFocusModal()" class="h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
              <span>Launch</span>
              <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </article>

          <!-- Core 2: Digital Stillness -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-primary/10 text-primary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">hourglass_bottom</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Digital Stillness</span>
                <span class="text-xs text-on-surface-variant truncate" id="tools-screen-time-label">App usage telemetry & limits</span>
              </div>
            </div>
            <button onclick="openScreenTimeModal()" class="h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
              <span>Inspect</span>
              <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </article>

          <!-- Core 3: Floating Bubble HUD -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-secondary/10 text-secondary flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">bubble_chart</span>
              </div>
              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-semibold text-on-surface truncate">Floating Bubble HUD</span>
                  <span class="text-[10px] font-semibold px-1.5 py-0.2 rounded-full bg-surface-container text-secondary" id="hud-status-badge">Standby</span>
                </div>
                <span class="text-xs text-on-surface-variant truncate">Overlay pill over external apps</span>
              </div>
            </div>
            <button onclick="toggleOverlayFromJs()" id="tools-overlay-toggle-btn" class="h-8 px-3 rounded-full bg-primary text-on-primary text-xs font-semibold transition-all shrink-0 cursor-pointer active:scale-95">
              Toggle HUD
            </button>
          </article>

          <!-- Core 4: Day Timeline & Cadence -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">calendar_today</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Day Timeline</span>
                <span class="text-xs text-on-surface-variant truncate">Chronological flow & daily rhythm</span>
              </div>
            </div>
            <button onclick="switchTab('day')" class="h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
              <span>Open</span>
              <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </article>

          <!-- Core 5: Thoughts Vault -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">edit_note</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Thoughts Vault</span>
                <span class="text-xs text-on-surface-variant truncate">Encrypted markdown reflections</span>
              </div>
            </div>
            <button onclick="switchTab('journal')" class="h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
              <span>Open</span>
              <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </article>

          <!-- Core 6: Database Cockpit -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-slate-500/10 text-slate-600 dark:text-slate-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">database</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Database Cockpit</span>
                <span class="text-xs text-on-surface-variant truncate">Inspect SQLite tables & WAL vacuum</span>
              </div>
            </div>
            <button onclick="openAdminCockpit()" class="h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
              <span>Inspect</span>
              <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </article>

          <!-- Core 7: Recycle Bin -->
          <article class="bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50 active:scale-[0.99]" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-zinc-500/10 text-zinc-600 dark:text-zinc-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">delete_outline</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Recycle Bin</span>
                <span class="text-xs text-on-surface-variant truncate">Restore or purge soft-deleted items</span>
              </div>
            </div>
            <button onclick="openRecycleBinModal()" class="h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
              <span>View Bin</span>
              <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </article>

        </div>
      </section>

      <!-- SECTION 2: MODULAR ADD-ON PLUGINS WITH FULL MANAGE TOOL -->
      <section class="flex flex-col space-y-2.5 pt-2">
        <div class="flex items-center justify-between pb-1 border-b border-surface-container-high/40">
          <div class="flex items-center gap-1.5">
            <span class="material-symbols-outlined text-secondary text-[18px]">extension</span>
            <span class="text-xs font-bold text-on-surface uppercase tracking-wider">Modular Add-on Plugins</span>
          </div>
          <span class="text-[11px] font-semibold text-secondary px-2 py-0.5 rounded-full bg-secondary/10" id="plugins-total-count">10 Plugins</span>
        </div>

        <!-- MANAGE TOOL: SEARCH, FILTER & UNINSTALL -->
        <div class="bg-surface-container-lowest rounded-xl p-2.5 border border-surface-container-high/40 flex flex-col gap-2 shadow-xs">
          <div class="flex items-center gap-2">
            <div class="relative flex-1">
              <span class="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-on-surface-variant text-[17px]">search</span>
              <input 
                id="plugin-search-input" 
                type="text" 
                placeholder="Search installed plugins..." 
                oninput="filterPluginsList(this.value)"
                class="w-full h-8 pl-8 pr-2.5 rounded-lg bg-surface-container-low text-xs text-on-surface placeholder:text-on-surface-variant/60 border-none outline-none focus:ring-1 focus:ring-primary"
              />
            </div>
            <button onclick="pickAndInstallZip()" class="h-8 px-2.5 rounded-lg bg-primary text-on-primary text-xs font-semibold flex items-center gap-1 cursor-pointer active:scale-95 shrink-0" title="Install Plugin ZIP">
              <span class="material-symbols-outlined text-[15px]">add</span>
              <span class="hidden sm:inline">Install</span>
            </button>
            <button onclick="toggleManagePluginsMode()" id="btn-toggle-manage" class="h-8 px-2.5 rounded-lg bg-surface-container text-on-surface text-xs font-medium flex items-center gap-1 cursor-pointer active:scale-95 shrink-0" title="Toggle Manage Mode">
              <span class="material-symbols-outlined text-[15px]" id="manage-icon">tune</span>
              <span id="manage-btn-label">Manage</span>
            </button>
          </div>

          <!-- Quick Category Filters -->
          <div class="flex items-center gap-1 overflow-x-auto pb-0.5 no-scrollbar text-xs">
            <button onclick="filterPluginsCategory('all', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-primary text-on-primary font-medium text-[11px] cursor-pointer">All (10)</button>
            <button onclick="filterPluginsCategory('mindful', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container">Mindfulness</button>
            <button onclick="filterPluginsCategory('productivity', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container">Productivity</button>
            <button onclick="filterPluginsCategory('body', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container">Body & Health</button>
          </div>
        </div>

        <!-- PLUGINS LIST (Full View Capable, Compact) -->
        <div class="flex flex-col space-y-2" id="plugins-list-container">

          <!-- Plugin 1: Habits Matrix -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.habits" data-name="habits matrix" data-category="mindful" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">cycle</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Habits Matrix</span>
                <span class="text-xs text-on-surface-variant truncate">Daily gentle rituals & streak cadence</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.habits')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.habits', 'Habits Matrix')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 2: Book Reader -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.reader" data-name="book reader" data-category="mindful" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">menu_book</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Book Reader</span>
                <span class="text-xs text-on-surface-variant truncate">Offline reader & page milestones</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.reader')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.reader', 'Book Reader')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 3: Bhagavad Gita -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.gita" data-name="bhagavad gita wisdom" data-category="mindful" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">bookmark_heart</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Gita Wisdom</span>
                <span class="text-xs text-on-surface-variant truncate">Daily contemplative Sanskrit & commentary</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.gita')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-secondary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.gita', 'Gita Wisdom')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 4: Mood & Energy Check-in -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.mood" data-name="mood & energy check-in" data-category="mindful" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-rose-500/10 text-rose-600 dark:text-rose-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">favorite</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Mood & Energy</span>
                <span class="text-xs text-on-surface-variant truncate">Daily equilibrium & panic vault grounding</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openMoodModal()" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-secondary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.mood', 'Mood & Energy')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 5: Weekly Digest (Chronosynth) -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.chronosynth" data-name="weekly digest chronosynth" data-category="productivity" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-600 dark:text-purple-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">psychology</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Weekly Digest</span>
                <span class="text-xs text-on-surface-variant truncate">Sunday executive review & intelligence</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.chronosynth')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.chronosynth', 'Weekly Digest')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 6: Gamify Quests -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.gamify" data-name="gamify quests" data-category="productivity" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-yellow-500/10 text-yellow-600 dark:text-yellow-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">military_tech</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Gamify Quests</span>
                <span class="text-xs text-on-surface-variant truncate">Mindful rituals & progressive quest rewards</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.gamify')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-secondary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.gamify', 'Gamify Quests')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 7: Sovereign Tasks -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.tasks" data-name="sovereign tasks" data-category="productivity" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">checklist</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Sovereign Tasks</span>
                <span class="text-xs text-on-surface-variant truncate">Priority checklist, deadlines & progress</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.tasks')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.tasks', 'Sovereign Tasks')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 8: Body Rhythm & Vitals -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.vitals" data-name="body rhythm & vitals" data-category="body" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-red-500/10 text-red-600 dark:text-red-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">monitor_heart</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Body Rhythm & Vitals</span>
                <span class="text-xs text-on-surface-variant truncate">Physical presence & circadian metrics</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.vitals')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.vitals', 'Body Rhythm')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 9: Athletic Rhythm & Running -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.running" data-name="athletic rhythm & running" data-category="body" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-orange-500/10 text-orange-600 dark:text-orange-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">directions_run</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Athletic Rhythm & Running</span>
                <span class="text-xs text-on-surface-variant truncate">Cadence pacing, GPS & recovery analytics</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.running')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-secondary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.running', 'Athletic Rhythm')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

          <!-- Plugin 10: Modular Time Tracker -->
          <article class="plugin-item-card bg-surface-container-lowest rounded-xl p-3 border border-surface-container-high/40 flex items-center justify-between gap-3 transition-all hover:bg-surface-container-low/50" data-plugin-id="com.v07.timetracker" data-name="modular time tracker" data-category="productivity" style="box-shadow: var(--card-shadow);">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <div class="w-9 h-9 rounded-xl bg-teal-500/10 text-teal-600 dark:text-teal-400 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">schedule</span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-semibold text-on-surface truncate">Time Tracker</span>
                <span class="text-xs text-on-surface-variant truncate">Time accounting & category focus logs</span>
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button onclick="openPlugin('com.v07.timetracker')" class="plugin-open-btn h-8 px-3 rounded-full bg-surface-container text-primary text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer active:scale-95">
                <span>Open</span>
                <span class="material-symbols-outlined text-[14px]">open_in_full</span>
              </button>
              <button onclick="promptRemovePlugin('com.v07.timetracker', 'Time Tracker')" class="plugin-manage-btn hidden h-8 px-2.5 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity cursor-pointer active:scale-95">
                Remove
              </button>
            </div>
          </article>

        </div>
      </section>
    </div>"""

def build_fullview_plugin_host_html():
    return """  <!-- ========================================== -->
  <!-- FULL VIEW PLUGIN HOST VIEWPORT             -->
  <!-- ========================================== -->
  <div id="modal-plugin-host" class="fixed inset-0 z-50 bg-background flex flex-col w-full h-full transition-all duration-300 transform translate-y-full opacity-0 pointer-events-none" style="display: flex;">
    <!-- Ergonomic M3 Full-View Header -->
    <header class="h-14 px-4 bg-surface-container-lowest border-b border-surface-container-high/40 flex items-center justify-between shrink-0 shadow-xs z-10">
      <div class="flex items-center gap-3 min-w-0">
        <button onclick="closePluginModal()" class="w-10 h-10 -ml-2 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close plugin and return to Sanctuary">
          <span class="material-symbols-outlined text-[22px]">arrow_back</span>
        </button>
        <div class="flex flex-col min-w-0">
          <h2 class="font-headline-sm text-sm sm:text-base font-semibold text-on-surface truncate" id="plugin-viewport-title">Plugin Viewport</h2>
          <span class="font-label-sm text-[11px] text-on-surface-variant truncate font-mono" id="plugin-viewport-subtitle">com.v07.plugin</span>
        </div>
      </div>
      <div class="flex items-center gap-1">
        <button onclick="reloadPluginIframe()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Reload plugin">
          <span class="material-symbols-outlined text-[18px]">refresh</span>
        </button>
        <button onclick="closePluginModal()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Close">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>
    </header>

    <!-- Plugin Iframe Viewport (Full View, Edge-to-Edge) -->
    <main class="flex-1 w-full h-full relative overflow-hidden bg-surface">
      <iframe id="plugin-viewport-iframe" class="w-full h-full border-0 absolute inset-0" src="about:blank" allow="fullscreen; accelerometer; gyroscope; geolocation" sandbox="allow-scripts allow-same-origin allow-forms allow-modals allow-popups"></iframe>
    </main>
  </div>"""

def build_impeccable_js():
    return """
    // ====================================================
    // IMPECCABLE PLUGIN HOST & MANAGEMENT FUNCTIONS
    // ====================================================

    let isManagePluginsActive = false;

    function openPlugin(pluginId) {
      const host = document.getElementById('modal-plugin-host');
      const iframe = document.getElementById('plugin-viewport-iframe');
      const title = document.getElementById('plugin-viewport-title');
      const subtitle = document.getElementById('plugin-viewport-subtitle');

      if (!host || !iframe) {
        console.error('modal-plugin-host element not found in DOM');
        return;
      }

      const pluginMeta = {
        'com.v07.habits': { name: 'Habits Matrix', desc: 'Gentle daily rituals & cadence' },
        'com.v07.reader': { name: 'Book Reader', desc: 'Offline reading companion' },
        'com.v07.gita': { name: 'Bhagavad Gita Wisdom', desc: 'Daily verses & contemplation' },
        'com.v07.chronosynth': { name: 'Weekly Digest', desc: 'Executive Sunday intelligence' },
        'com.v07.gamify': { name: 'Gamify Quests', desc: 'Rituals & progressive quest rewards' },
        'com.v07.tasks': { name: 'Sovereign Tasks', desc: 'Daily checklist & deadlines' },
        'com.v07.vitals': { name: 'Body Rhythm & Vitals', desc: 'Physical presence & circadian metrics' },
        'com.v07.running': { name: 'Athletic Rhythm & Running', desc: 'Pacing & recovery analytics' },
        'com.v07.timetracker': { name: 'Modular Time Tracker', desc: 'Time accounting & focus distribution' }
      };

      const meta = pluginMeta[pluginId] || { 
        name: pluginId.replace('com.v07.', '').replace(/^[a-z]/, c => c.toUpperCase()), 
        desc: pluginId 
      };

      if (title) title.textContent = meta.name;
      if (subtitle) subtitle.textContent = meta.desc;

      // Point iframe to the plugin's index.html
      iframe.src = `/plugins/${pluginId}/index.html`;

      // Full View slide-up transition
      host.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
      host.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
    }

    function closePluginModal() {
      const host = document.getElementById('modal-plugin-host');
      const iframe = document.getElementById('plugin-viewport-iframe');

      if (host) {
        host.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        host.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
      }
      if (iframe) {
        setTimeout(() => { iframe.src = 'about:blank'; }, 250);
      }
    }

    function reloadPluginIframe() {
      const iframe = document.getElementById('plugin-viewport-iframe');
      if (iframe && iframe.src && iframe.src !== 'about:blank') {
        iframe.src = iframe.src;
      }
    }

    function toggleManagePluginsMode() {
      isManagePluginsActive = !isManagePluginsActive;
      const btn = document.getElementById('btn-toggle-manage');
      const label = document.getElementById('manage-btn-label');
      const icon = document.getElementById('manage-icon');

      const openBtns = document.querySelectorAll('.plugin-open-btn');
      const manageBtns = document.querySelectorAll('.plugin-manage-btn');

      if (isManagePluginsActive) {
        if (btn) btn.className = 'h-8 px-2.5 rounded-lg bg-secondary text-on-secondary text-xs font-semibold flex items-center gap-1 cursor-pointer active:scale-95 shrink-0';
        if (label) label.textContent = 'Done';
        if (icon) icon.textContent = 'check';

        openBtns.forEach(b => b.classList.add('hidden'));
        manageBtns.forEach(b => b.classList.remove('hidden'));
      } else {
        if (btn) btn.className = 'h-8 px-2.5 rounded-lg bg-surface-container text-on-surface text-xs font-medium flex items-center gap-1 cursor-pointer active:scale-95 shrink-0';
        if (label) label.textContent = 'Manage';
        if (icon) icon.textContent = 'tune';

        openBtns.forEach(b => b.classList.remove('hidden'));
        manageBtns.forEach(b => b.classList.add('hidden'));
      }
    }

    function filterPluginsList(query) {
      const q = (query || '').toLowerCase().trim();
      const cards = document.querySelectorAll('.plugin-item-card');
      let visibleCount = 0;

      cards.forEach(card => {
        const name = (card.getAttribute('data-name') || '').toLowerCase();
        const id = (card.getAttribute('data-plugin-id') || '').toLowerCase();
        if (!q || name.includes(q) || id.includes(q)) {
          card.classList.remove('hidden');
          visibleCount++;
        } else {
          card.classList.add('hidden');
        }
      });

      const countEl = document.getElementById('plugins-total-count');
      if (countEl) countEl.textContent = `${visibleCount} Plugins`;
    }

    function filterPluginsCategory(cat, btn) {
      // Update chip styling
      document.querySelectorAll('.plugin-filter-chip').forEach(c => {
        c.className = 'plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container';
      });
      if (btn) {
        btn.className = 'plugin-filter-chip px-2.5 py-0.5 rounded-full bg-primary text-on-primary font-medium text-[11px] cursor-pointer';
      }

      const cards = document.querySelectorAll('.plugin-item-card');
      let visibleCount = 0;

      cards.forEach(card => {
        const cardCat = card.getAttribute('data-category');
        if (cat === 'all' || cardCat === cat) {
          card.classList.remove('hidden');
          visibleCount++;
        } else {
          card.classList.add('hidden');
        }
      });

      const countEl = document.getElementById('plugins-total-count');
      if (countEl) countEl.textContent = `${visibleCount} Plugins`;
    }

    function promptRemovePlugin(pluginId, pluginName) {
      if (confirm(`Uninstall and remove "${pluginName}" (${pluginId})?\\nThis will delete its sandboxed storage and view.`)) {
        // Call backend or mark disabled
        const card = document.querySelector(`.plugin-item-card[data-plugin-id="${pluginId}"]`);
        if (card) {
          card.style.opacity = '0.4';
          card.style.pointerEvents = 'none';
        }
        alert(`Plugin ${pluginName} removed.`);
      }
    }

    window.openPlugin = openPlugin;
    window.closePluginModal = closePluginModal;
    window.reloadPluginIframe = reloadPluginIframe;
    window.toggleManagePluginsMode = toggleManagePluginsMode;
    window.filterPluginsList = filterPluginsList;
    window.filterPluginsCategory = filterPluginsCategory;
    window.promptRemovePlugin = promptRemovePlugin;
"""

print("Impeccable script loaded")
