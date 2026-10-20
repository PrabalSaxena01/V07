import re

def polish_v3():
    files = [
        r"D:\software\V07-Android\app\src\main\assets\v3\index.html",
        r"D:\software\V07-Android\app\src\main\assets\index.html"
    ]

    for fpath in files:
        with open(fpath, "r", encoding="utf-8") as f:
            content = f.read()

        # 1. Fix Floating Bubble HUD title truncation
        old_hud = '''              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-semibold text-on-surface truncate">Floating Bubble HUD</span>
                  <span class="text-[10px] font-semibold px-1.5 py-0.2 rounded-full bg-surface-container text-secondary" id="hud-status-badge">Standby</span>
                </div>
                <span class="text-xs text-on-surface-variant truncate">Overlay pill over external apps</span>
              </div>'''

        new_hud = '''              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-1.5 min-w-0">
                  <span class="text-sm font-semibold text-on-surface">Floating HUD</span>
                  <span class="text-[10px] font-semibold px-1.5 py-0.5 rounded-full bg-surface-container text-secondary shrink-0" id="hud-status-badge">Standby</span>
                </div>
                <span class="text-xs text-on-surface-variant truncate">Overlay pill over external apps</span>
              </div>'''

        if old_hud in content:
            content = content.replace(old_hud, new_hud)
            print(f"Replaced Floating HUD in {fpath}")
        else:
            print(f"Warning: old_hud not found in {fpath}")

        # 2. Fix Database Cockpit title truncation
        old_cockpit = '''              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-semibold text-on-surface truncate">Database Cockpit</span>
                  <span class="text-[10px] font-semibold px-1.5 py-0.2 rounded-full bg-surface-container text-outline" id="cockpit-table-count">SQLite</span>
                </div>
                <span class="text-xs text-on-surface-variant truncate">Inspect SQLite tables & storage size</span>
              </div>'''

        new_cockpit = '''              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-1.5 min-w-0">
                  <span class="text-sm font-semibold text-on-surface">Database Cockpit</span>
                  <span class="text-[10px] font-semibold px-1.5 py-0.5 rounded-full bg-surface-container text-outline shrink-0" id="cockpit-table-count">SQLite</span>
                </div>
                <span class="text-xs text-on-surface-variant truncate">Inspect SQLite tables & storage size</span>
              </div>'''

        if old_cockpit in content:
            content = content.replace(old_cockpit, new_cockpit)
            print(f"Replaced Database Cockpit in {fpath}")
        else:
            print(f"Warning: old_cockpit not found in {fpath}")

        # 3. In openPlugin, notify AndroidTheme of fullscreen mode
        old_open = '''      // Full View slide-up transition
      host.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
      host.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
    }'''

        new_open = '''      // Full View slide-up transition
      host.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
      host.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');

      try {
        if (window.AndroidTheme && typeof window.AndroidTheme.setPluginFullscreen === 'function') {
          window.AndroidTheme.setPluginFullscreen(true);
        }
      } catch (e) {
        console.warn('AndroidTheme bridge call failed', e);
      }
    }'''

        if old_open in content:
            content = content.replace(old_open, new_open)
            print(f"Updated openPlugin bridge notification in {fpath}")
        else:
            print(f"Warning: old_open not found in {fpath}")

        # 4. In closePluginModal, notify AndroidTheme of fullscreen exit
        old_close = '''      if (host) {
        host.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        host.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
        if (iframe) iframe.src = 'about:blank';
      }'''

        new_close = '''      if (host) {
        host.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        host.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
        if (iframe) iframe.src = 'about:blank';

        try {
          if (window.AndroidTheme && typeof window.AndroidTheme.setPluginFullscreen === 'function') {
            window.AndroidTheme.setPluginFullscreen(false);
          }
        } catch (e) {
          console.warn('AndroidTheme bridge call failed', e);
        }
      }'''

        if old_close in content:
            content = content.replace(old_close, new_close)
            print(f"Updated closePluginModal bridge notification in {fpath}")
        else:
            print(f"Warning: old_close not found in {fpath}")

        # 5. Map com.v07.mood in openPlugin routing
        old_routing = "      iframe.src = `/plugins/${pluginId}/index.html`;"
        new_routing = """      const targetDir = (pluginId === 'com.v07.mood' || pluginId === 'mood') ? 'com.v07.vitals' : pluginId;
      iframe.src = `/plugins/${targetDir}/index.html`;"""

        if old_routing in content:
            content = content.replace(old_routing, new_routing)
            print(f"Updated plugin routing in {fpath}")
        else:
            print(f"Warning: old_routing not found in {fpath}")

        # 6. Change Mood & Energy card onclick to openPlugin('com.v07.mood')
        old_mood_btn = 'onclick="openMoodModal()"'
        new_mood_btn = 'onclick="openPlugin(\'com.v07.mood\')"'
        if old_mood_btn in content:
            content = content.replace(old_mood_btn, new_mood_btn)
            print(f"Updated Mood & Energy button onclick in {fpath}")

        # 7. Simplify handleBackPress
        old_back = '''    // Android Hardware & Gesture Back Button Handler
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
    };'''

        new_back = '''    // Android Hardware & Gesture Back Button Handler
    window.handleBackPress = function() {
      const host = document.getElementById('modal-plugin-host');
      if (host && !host.classList.contains('translate-y-full')) {
        closePluginModal();
        return true;
      }
      const openDialog = document.querySelector('dialog[open]');
      if (openDialog) {
        openDialog.close();
        return true;
      }
      return false;
    };'''

        if old_back in content:
            content = content.replace(old_back, new_back)
            print(f"Updated handleBackPress in {fpath}")
        else:
            print(f"Warning: old_back not found in {fpath}")

        with open(fpath, "w", encoding="utf-8") as f:
            f.write(content)

    print("HTML files successfully polished.")

if __name__ == "__main__":
    polish_v3()
