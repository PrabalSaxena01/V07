import re

path = r'D:\software\V07-Android\scratch\build_perfect_v3.py'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update body tag to include in-compose-shell
content = content.replace(
    '<body class="bg-surface font-body-md text-body-md text-on-surface antialiased flex flex-col min-h-screen select-none">',
    '<body class="bg-surface font-body-md text-body-md text-on-surface antialiased flex flex-col min-h-screen select-none in-compose-shell">'
)

# 2. Update Tools tab header and add Core Plugins section wrapper
old_tools_header = '''      <section class="flex flex-col gap-y-space-xs pt-space-xs">
        <div class="inline-flex items-center gap-space-xs text-primary font-label-md text-label-md">
          <span class="material-symbols-outlined text-[18px]">widgets</span>
          <span>Daily companions</span>
        </div>
        <h2 class="font-headline-lg text-headline-lg text-on-surface font-semibold tracking-tight">Your tools</h2>
        <p class="font-body-md text-body-md text-on-surface-variant">Small things that help you through the day.</p>
      </section>

      <!-- 1. Reading Companion Card -->'''

new_tools_header = '''      <section class="flex flex-col gap-y-space-xs pt-space-xs">
        <div class="inline-flex items-center gap-space-xs text-primary font-label-md text-label-md">
          <span class="material-symbols-outlined text-[18px]">widgets</span>
          <span>Daily companions</span>
        </div>
        <h2 class="font-headline-lg text-headline-lg text-on-surface font-semibold tracking-tight">Your tools & plugins</h2>
        <p class="font-body-md text-body-md text-on-surface-variant">Core sanctuary companions and extensible modular plugins.</p>
      </section>

      <!-- SECTION 1: CORE PLUGINS (STARTER PACK) -->
      <section class="flex flex-col space-y-space-md">
        <div class="flex items-center justify-between pb-1.5 border-b border-surface-container-high/40">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-[20px] material-symbols-filled">spa</span>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-base font-semibold text-on-surface">Core Companions</h3>
              <span class="font-label-sm text-xs text-on-surface-variant">Native offline engines included with Sanctuary</span>
            </div>
          </div>
          <span class="px-2.5 py-1 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold">7 Core Active</span>
        </div>

        <div class="flex flex-col space-y-space-md">
      <!-- 1. Reading Companion Card -->'''

assert old_tools_header in content, "old_tools_header not found"
content = content.replace(old_tools_header, new_tools_header)

# 3. Close the Core Plugins section and replace the old Companion Plugins section with the complete Add-on Plugins section
old_companion_section = '''      <!-- 8. Platform Companion Plugins (Installed / Ecosystem) -->
      <section class="flex flex-col space-y-space-sm pt-2">
        <div class="flex items-center justify-between px-space-xs">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant font-medium">Companion Plugins</h3>
          <span class="font-label-sm text-label-sm text-primary font-medium bg-surface-container-low px-2.5 py-0.5 rounded-full">Active & Ready</span>
        </div>

        <!-- Chronosynth / Weekly Digest -->
        <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex items-center justify-between gap-space-md" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-xl bg-surface-container text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">psychology</span>
            </div>
            <div class="flex flex-col min-w-0">
              <div class="flex items-center gap-2">
                <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Weekly Digest</h4>
                <span class="text-[10px] font-mono text-outline">v1.0</span>
              </div>
              <p class="font-body-sm text-xs text-on-surface-variant truncate">Executive Sunday briefing & focus intelligence</p>
            </div>
          </div>
          <button onclick="openPlugin('com.v07.chronosynth')" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
          </button>
        </article>

        <!-- Gamify Quests -->
        <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex items-center justify-between gap-space-md" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-xl bg-surface-container text-secondary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">military_tech</span>
            </div>
            <div class="flex flex-col min-w-0">
              <div class="flex items-center gap-2">
                <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Gamify Quests</h4>
                <span class="text-[10px] font-mono text-outline">v1.0</span>
              </div>
              <p class="font-body-sm text-xs text-on-surface-variant truncate">Daily mindful rituals & progression experience</p>
            </div>
          </div>
          <button onclick="openPlugin('com.v07.gamify')" class="h-8 px-3 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
          </button>
        </article>

        <!-- Body Rhythm & Vitals -->
        <article class="bg-surface-container-lowest rounded-2xl p-space-md shadow-sm border border-surface-container-high/40 flex items-center justify-between gap-space-md" style="box-shadow: var(--card-shadow);">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-xl bg-surface-container text-primary flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">monitor_heart</span>
            </div>
            <div class="flex flex-col min-w-0">
              <div class="flex items-center gap-2">
                <h4 class="font-headline-sm text-sm font-semibold text-on-surface truncate">Body Rhythm & Vitals</h4>
                <span class="text-[10px] font-mono text-outline">v1.0</span>
              </div>
              <p class="font-body-sm text-xs text-on-surface-variant truncate">Physical presence, resting pulse, and calm metrics</p>
            </div>
          </div>
          <button onclick="openPlugin('com.v07.vitals')" class="h-8 px-3 rounded-full bg-surface-container-low text-primary font-label-sm text-xs font-semibold hover:bg-surface-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer active:scale-95">
            <span>Open</span>
            <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
          </button>
        </article>
      </section>

      <div class="py-space-md text-center flex flex-col items-center justify-center gap-1 text-on-surface-variant">
        <div class="w-6 h-1 rounded-full bg-surface-container-highest"></div>
        <span class="font-body-sm text-body-sm pt-space-xs">Each tool operates privately and quietly on your device.</span>
      </div>'''

new_companion_section = '''        </div>
      </section>

      <!-- SECTION 2: ADD-ON PLUGINS (ECOSYSTEM EXTENSIONS) -->
      <section class="flex flex-col space-y-space-md pt-2">
        <div class="flex items-center justify-between pb-1.5 border-b border-surface-container-high/40">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-secondary text-[20px]">extension</span>
            <div class="flex flex-col">
              <h3 class="font-headline-sm text-base font-semibold text-on-surface">Modular Add-on Plugins</h3>
              <span class="font-label-sm text-xs text-on-surface-variant">Isolated extensions adhering to V3 Plugin Specification</span>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button onclick="pickAndInstallZip()" class="h-7 px-2.5 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold flex items-center gap-1 cursor-pointer hover:opacity-90 active:scale-95 shadow-xs" title="Install custom plugin from ZIP">
              <span class="material-symbols-outlined text-[14px]">add</span>
              <span>Install ZIP</span>
            </button>
            <span class="px-2.5 py-1 rounded-full bg-surface-container-low text-secondary font-label-sm text-xs font-semibold">5 Installed</span>
          </div>
        </div>

        <div class="flex flex-col space-y-3">
          <!-- 1. Chronosynth / Weekly Digest -->
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

          <!-- 2. Gamify Quests -->
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

          <!-- 3. Sovereign Tasks -->
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

          <!-- 4. Body Rhythm & Vitals -->
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

          <!-- 5. Athletic Rhythm & Running -->
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
        </div>
      </section>

      <!-- Architecture & Security Footnote -->
      <section class="rounded-2xl bg-surface-container-low p-4 flex items-start gap-3 border border-surface-container-high/30">
        <span class="material-symbols-outlined text-primary text-[20px] shrink-0 mt-0.5">verified_user</span>
        <div class="flex flex-col">
          <h4 class="font-label-md text-xs font-semibold text-on-surface">Sandboxed & Sovereign Architecture</h4>
          <p class="font-body-sm text-xs text-on-surface-variant mt-0.5 leading-relaxed">
            Every modular add-on runs within an isolated DOM viewport with explicit capability permissions (AGENTS.md §§ 15, 16, 21). Data remains strictly on-device without remote telemetry leaks.
          </p>
        </div>
      </section>'''

assert old_companion_section in content, "old_companion_section not found"
content = content.replace(old_companion_section, new_companion_section)

# 4. Update openPlugin and add pickAndInstallZip
old_open_plugin = '''    // Sandboxed Plugin Host Launcher
    function openPlugin(pluginId) {
      if (pluginId === 'gita' || pluginId === 'com.v07.gita') {
        openGitaModal();
        return;
      }
      if (pluginId === 'focus' || pluginId === 'com.v07.focus') {
        openFocusModal();
        return;
      }
      if (pluginId === 'habits' || pluginId === 'com.v07.habits') {
        openHabitsModal();
        return;
      }
      if (pluginId === 'reading' || pluginId === 'com.v07.reader') {
        openReadingModal();
        return;
      }
      if (pluginId === 'screentime' || pluginId === 'com.v07.timetracker') {
        openScreenTimeModal();
        return;
      }
      if (pluginId === 'mood') {
        openMoodModal();
        return;
      }

      const modal = document.getElementById('modal-plugin-host');
      const iframe = document.getElementById('plugin-viewport-iframe');
      const title = document.getElementById('plugin-viewport-title');
      const subtitle = document.getElementById('plugin-viewport-subtitle');
      if (modal && iframe) {
        const cleanName = pluginId.replace('com.v07.', '').replace(/^[a-z]/, char => char.toUpperCase());
        if (title) title.textContent = cleanName;
        if (subtitle) subtitle.textContent = pluginId;
        iframe.src = `/plugins/${pluginId}/index.html`;
        modal.showModal();
      }
    }'''

new_open_plugin = '''    // Sandboxed Plugin Host Launcher & Installer
    function pickAndInstallZip() {
      if (window.V3NativeBridge && typeof window.V3NativeBridge.invoke === 'function') {
        try {
          window.V3NativeBridge.invoke('core', 'plugin:importZip', '{}');
        } catch (e) {
          alert('Could not launch plugin installer: ' + e.message);
        }
      } else {
        alert('Plugin ZIP installation is available when running inside native V07 Android shell.');
      }
    }

    function openPlugin(pluginId) {
      if (pluginId === 'gita' || pluginId === 'com.v07.gita') {
        openGitaModal();
        return;
      }
      if (pluginId === 'focus' || pluginId === 'com.v07.focus') {
        openFocusModal();
        return;
      }
      if (pluginId === 'habits' || pluginId === 'com.v07.habits') {
        openHabitsModal();
        return;
      }
      if (pluginId === 'reading' || pluginId === 'com.v07.reader') {
        openReadingModal();
        return;
      }
      if (pluginId === 'screentime' || pluginId === 'com.v07.timetracker') {
        openScreenTimeModal();
        return;
      }
      if (pluginId === 'mood') {
        openMoodModal();
        return;
      }

      const modal = document.getElementById('modal-plugin-host');
      const iframe = document.getElementById('plugin-viewport-iframe');
      const title = document.getElementById('plugin-viewport-title');
      const subtitle = document.getElementById('plugin-viewport-subtitle');
      if (modal && iframe) {
        const pluginNames = {
          'com.v07.chronosynth': 'Weekly Digest',
          'com.v07.gamify': 'Gamify Quests',
          'com.v07.tasks': 'Sovereign Tasks',
          'com.v07.vitals': 'Body Rhythm & Vitals',
          'com.v07.running': 'Athletic Rhythm & Running'
        };
        const cleanName = pluginNames[pluginId] || pluginId.replace('com.v07.', '').replace(/^[a-z]/, char => char.toUpperCase());
        if (title) title.textContent = cleanName;
        if (subtitle) subtitle.textContent = pluginId;
        iframe.src = `/plugins/${pluginId}/index.html`;
        modal.showModal();
      }
    }'''

assert old_open_plugin in content, "old_open_plugin not found"
content = content.replace(old_open_plugin, new_open_plugin)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Successfully updated build_perfect_v3.py")
