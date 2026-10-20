import re
import os

V3_PATH = 'D:/software/V07-Android/app/src/main/assets/v3/index.html'
ROOT_PATH = 'D:/software/V07-Android/app/src/main/assets/index.html'

with open(ROOT_PATH, 'r', encoding='utf-8') as f:
    html = f.read()

print(f"Original index.html: {len(html)} chars")

# 1. Update CSS Tokens in :root and html.dark
css_tokens_light = """      --nav-shadow: 0 12px 32px -4px rgba(32, 40, 36, 0.08), 0 4px 12px -2px rgba(32, 40, 36, 0.04);
      --card-shadow: 0 1px 3px rgba(32, 40, 36, 0.05), 0 1px 2px rgba(32, 40, 36, 0.03);
      --modal-shadow: 0 16px 32px -4px rgba(32, 40, 36, 0.12), 0 4px 12px -2px rgba(32, 40, 36, 0.06);"""

css_tokens_dark = """      --nav-shadow: 0 12px 32px -4px rgba(0, 0, 0, 0.45), 0 4px 12px -2px rgba(0, 0, 0, 0.25);
      --card-shadow: 0 1px 3px rgba(0, 0, 0, 0.3), 0 1px 2px rgba(0, 0, 0, 0.2);
      --modal-shadow: 0 16px 36px -4px rgba(0, 0, 0, 0.55), 0 4px 12px -2px rgba(0, 0, 0, 0.35);"""

old_light_shadow = re.search(r'--nav-shadow:.*?\n\s*--card-shadow:.*?;', html)
if old_light_shadow:
    html = html.replace(old_light_shadow.group(0), css_tokens_light, 1)

old_dark_shadow = re.search(r'html\.dark\s*\{.*?--nav-shadow:.*?\n\s*--card-shadow:.*?;', html, re.DOTALL)
if old_dark_shadow:
    # replace inside dark mode
    dark_part = old_dark_shadow.group(0)
    new_dark_part = re.sub(r'--nav-shadow:.*?\n\s*--card-shadow:.*?;', css_tokens_dark, dark_part)
    html = html.replace(dark_part, new_dark_part, 1)

# 2. Add Typography Scale, Spacing Fallbacks, and Modal styling into <style>
extra_css = """
    /* Impeccable Typography Scale Fallbacks & Resiliency */
    .text-display-lg { font-size: 32px; line-height: 40px; font-weight: 700; letter-spacing: -0.02em; }
    .text-display-lg-mobile { font-size: 26px; line-height: 34px; font-weight: 600; letter-spacing: -0.015em; }
    .text-headline-lg { font-size: 24px; line-height: 32px; font-weight: 600; letter-spacing: -0.015em; }
    .text-headline-md { font-size: 20px; line-height: 28px; font-weight: 600; letter-spacing: -0.01em; }
    .text-headline-sm { font-size: 16px; line-height: 24px; font-weight: 600; letter-spacing: -0.005em; }
    .text-body-lg { font-size: 16px; line-height: 24px; font-weight: 400; }
    .text-body-md { font-size: 14px; line-height: 20px; font-weight: 400; }
    .text-body-sm { font-size: 12px; line-height: 16px; font-weight: 400; }
    .text-label-lg { font-size: 14px; line-height: 20px; font-weight: 500; }
    .text-label-md { font-size: 12px; line-height: 16px; font-weight: 500; }
    .text-label-sm { font-size: 11px; line-height: 14px; font-weight: 500; letter-spacing: 0.02em; }

    /* Spacing Fallbacks */
    .gap-space-xs { gap: 0.5rem; }
    .gap-space-sm { gap: 0.75rem; }
    .gap-space-md { gap: 1rem; }
    .gap-space-lg { gap: 1.25rem; }
    .gap-space-xl { gap: 1.5rem; }
    .p-space-xs { padding: 0.5rem; }
    .p-space-sm { padding: 0.75rem; }
    .p-space-md { padding: 1rem; }
    .p-space-lg { padding: 1.25rem; }
    .p-space-xl { padding: 1.5rem; }
    .space-y-space-xs > :not([hidden]) ~ :not([hidden]) { --tw-space-y-reverse: 0; margin-top: calc(0.5rem * calc(1 - var(--tw-space-y-reverse))); margin-bottom: calc(0.5rem * var(--tw-space-y-reverse)); }
    .space-y-space-sm > :not([hidden]) ~ :not([hidden]) { --tw-space-y-reverse: 0; margin-top: calc(0.75rem * calc(1 - var(--tw-space-y-reverse))); margin-bottom: calc(0.75rem * var(--tw-space-y-reverse)); }
    .space-y-space-md > :not([hidden]) ~ :not([hidden]) { --tw-space-y-reverse: 0; margin-top: calc(1rem * calc(1 - var(--tw-space-y-reverse))); margin-bottom: calc(1rem * var(--tw-space-y-reverse)); }
    .space-y-space-lg > :not([hidden]) ~ :not([hidden]) { --tw-space-y-reverse: 0; margin-top: calc(1.25rem * calc(1 - var(--tw-space-y-reverse))); margin-bottom: calc(1.25rem * var(--tw-space-y-reverse)); }
    .space-y-space-xl > :not([hidden]) ~ :not([hidden]) { --tw-space-y-reverse: 0; margin-top: calc(1.5rem * calc(1 - var(--tw-space-y-reverse))); margin-bottom: calc(1.5rem * var(--tw-space-y-reverse)); }

    /* Dialog Refinement */
    dialog {
      box-shadow: var(--modal-shadow) !important;
      border: 1px solid var(--surface-container-high) !important;
    }
"""

if "/* Impeccable Typography Scale Fallbacks" not in html:
    html = html.replace("</style>", extra_css + "  </style>", 1)

# 3. Clean up broken Unicode in Settings
html = html.replace("All records stored locally  No remote telemetry", "All records stored locally · No remote telemetry")

# 4. Clean dialog shadow attributes
html = re.sub(r'shadow-\[0_24px_48px_rgba\(21,29,25,0\.25\)\]', '', html)

# 5. Compact Tools Tab Replacement
from apply_impeccable_redesign import build_compact_tools_html, build_fullview_plugin_host_html

tab_tools_regex = r'<div id="tab-tools"[^>]*>.*?(?=<!-- ==========================================\s*-->\s*<!-- 4\. DAY TAB)'
compact_tools_markup = build_compact_tools_html() + "\n\n    "

m = re.search(tab_tools_regex, html, re.DOTALL)
if m:
    html = html[:m.start()] + compact_tools_markup + html[m.end():]
    print("Replaced tab-tools successfully")
else:
    print("WARNING: tab-tools regex match failed!")

# 6. Inject Full-View Plugin Host before <script>
if 'id="modal-plugin-host"' not in html:
    fullview_host = build_fullview_plugin_host_html() + "\n\n  "
    script_pos = html.find('  <script>')
    if script_pos == -1:
        script_pos = html.find('<script>')
    if script_pos != -1:
        html = html[:script_pos] + fullview_host + html[script_pos:]
        print("Injected modal-plugin-host successfully")
    else:
        print("WARNING: <script> not found for modal-plugin-host injection!")
else:
    print("modal-plugin-host already present in HTML")

# 7. Replace openPlugin / closePluginModal in script
impeccable_js = """
    // ====================================================
    // IMPECCABLE PLUGIN HOST & MANAGEMENT FUNCTIONS
    // ====================================================

    let isManagePluginsActive = false;

    function openPlugin(pluginId) {
      if (pluginId === 'mood' || pluginId === 'com.v07.mood') {
        openMoodModal();
        return;
      }

      const host = document.getElementById('modal-plugin-host');
      const iframe = document.getElementById('plugin-viewport-iframe');
      const title = document.getElementById('plugin-viewport-title');
      const subtitle = document.getElementById('plugin-viewport-subtitle');

      if (!host || !iframe) {
        console.error('modal-plugin-host element not found in DOM');
        return;
      }

      const pluginMeta = {
        'com.v07.habits': { name: 'Habits Matrix', desc: 'Daily gentle rituals & cadence' },
        'com.v07.reader': { name: 'Book Reader', desc: 'Offline reading companion' },
        'com.v07.gita': { name: 'Bhagavad Gita Wisdom', desc: 'Daily Sanskrit verses & contemplation' },
        'com.v07.chronosynth': { name: 'Weekly Digest', desc: 'Sunday executive review & intelligence' },
        'com.v07.gamify': { name: 'Gamify Quests', desc: 'Mindful rituals & quest milestones' },
        'com.v07.tasks': { name: 'Sovereign Tasks', desc: 'Priority checklist & daily progress' },
        'com.v07.vitals': { name: 'Body Rhythm & Vitals', desc: 'Circadian presence & somatic metrics' },
        'com.v07.running': { name: 'Athletic Rhythm & Running', desc: 'Cadence pacing & recovery analytics' },
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
        setTimeout(() => { iframe.src = 'about:blank'; }, 300);
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
        if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
          try {
            window.V07Platform.invoke('core', 'plugin:uninstall', JSON.stringify({ pluginId: pluginId }));
          } catch(e) {
            console.warn('Backend plugin uninstall invoke warning:', e);
          }
        }
        const card = document.querySelector(`.plugin-item-card[data-plugin-id="${pluginId}"]`);
        if (card) {
          card.style.opacity = '0.35';
          card.style.pointerEvents = 'none';
          const badge = document.createElement('span');
          badge.className = 'text-[10px] text-error font-semibold';
          badge.textContent = 'Uninstalled';
          card.appendChild(badge);
        }
      }
    }

    // Android Hardware & Gesture Back Button Handler
    window.handleBackPress = function() {
      const host = document.getElementById('modal-plugin-host');
      if (host && !host.classList.contains('translate-y-full') && host.classList.contains('opacity-100')) {
        closePluginModal();
        return true;
      }
      const openDialog = document.querySelector('dialog[open]');
      if (openDialog) {
        openDialog.close();
        return true;
      }
      const homeTab = document.getElementById('tab-home');
      if (homeTab && !homeTab.classList.contains('active')) {
        if (typeof switchTab === 'function') {
          switchTab('home');
          return true;
        }
      }
      return false;
    };

    window.openPlugin = openPlugin;
    window.openPluginFullPage = openPlugin;
    window.closePluginModal = closePluginModal;
    window.reloadPluginIframe = reloadPluginIframe;
    window.toggleManagePluginsMode = toggleManagePluginsMode;
    window.filterPluginsList = filterPluginsList;
    window.filterPluginsCategory = filterPluginsCategory;
    window.promptRemovePlugin = promptRemovePlugin;
"""

# Replace the old openPlugin block
old_open_plugin_regex = r'function openPlugin\(pluginId\)\s*\{.*?window\.closePluginModal = closePluginModal;'
m_js = re.search(old_open_plugin_regex, html, re.DOTALL)
if m_js:
    html = html[:m_js.start()] + impeccable_js.strip() + html[m_js.end():]
    print("Replaced openPlugin JS block successfully")
else:
    print("WARNING: old openPlugin JS block not found by regex!")

# Write to both ROOT_PATH and V3_PATH
with open(ROOT_PATH, 'w', encoding='utf-8') as f:
    f.write(html)
with open(V3_PATH, 'w', encoding='utf-8') as f:
    f.write(html)

print(f"Successfully wrote {len(html)} chars to:")
print(f"  {ROOT_PATH}")
print(f"  {V3_PATH}")
