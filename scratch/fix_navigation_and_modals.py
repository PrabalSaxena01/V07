import os
import re

def main():
    v3_html_path = 'D:/software/V07-Android/app/src/main/assets/v3/index.html'
    root_html_path = 'D:/software/V07-Android/app/src/main/assets/index.html'
    
    with open(v3_html_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Fix renderRecycleBin call in openRecycleBinModal
    old_rc = """    function openRecycleBinModal() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
        renderRecycleBin();
      }
    }"""
    
    new_rc = """    function openRecycleBinModal() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
        if (typeof loadRecycleBinData === 'function') {
          loadRecycleBinData();
        }
      }
    }"""

    # Normalize newlines for matching
    content = content.replace('\r\n', '\n')
    old_rc = old_rc.replace('\r\n', '\n')
    new_rc = new_rc.replace('\r\n', '\n')
    
    if old_rc in content:
        content = content.replace(old_rc, new_rc, 1)
        print("Fixed openRecycleBinModal")
    else:
        print("Warning: old_rc not found")

    # 2. Window exports for modals
    old_exports = """    window.closeFocusModal = closeFocusModal;
    window.closeScreenTimeModal = closeScreenTimeModal;
    window.closeRecycleBinModal = closeRecycleBinModal;
    window.openRecycleBinModal = openRecycleBinModal;
    window.closeAnyFullView = closeAnyFullView;"""

    new_exports = """    window.openFocusModal = openFocusModal;
    window.closeFocusModal = closeFocusModal;
    window.openScreenTimeModal = openScreenTimeModal;
    window.closeScreenTimeModal = closeScreenTimeModal;
    window.openRecycleBinModal = openRecycleBinModal;
    window.closeRecycleBinModal = closeRecycleBinModal;
    window.openHabitsModal = openHabitsModal;
    window.openReadingModal = openReadingModal;
    window.openGitaModal = openGitaModal;
    window.openMoodModal = openMoodModal;
    window.closeAnyFullView = closeAnyFullView;"""

    if old_exports in content:
        content = content.replace(old_exports, new_exports, 1)
        print("Fixed window modal exports")
    else:
        print("Warning: old_exports not found")

    # 3. Guard renderFocusModal and related
    old_focus = """    function renderFocusModal() {
      const mins = Math.floor(focusModalSeconds / 60);
      const secs = focusModalSeconds % 60;
      const timeStr = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
      document.getElementById('focus-modal-time').textContent = timeStr;
      document.getElementById('tools-focus-time-preview').textContent = timeStr;

      // Update ring
      const ring = document.getElementById('focus-progress-ring');
      if (ring) {
        const perimeter = 2 * Math.PI * 100; // ~628.31
        const frac = focusModalTotalSeconds > 0 ? (focusModalSeconds / focusModalTotalSeconds) : 0;
        const offset = perimeter * (1 - frac);
        ring.style.strokeDashoffset = offset;
      }
    }"""

    new_focus = """    function renderFocusModal() {
      try {
        const mins = Math.floor(focusModalSeconds / 60);
        const secs = focusModalSeconds % 60;
        const timeStr = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
        const modalTime = document.getElementById('focus-modal-time');
        if (modalTime) modalTime.textContent = timeStr;
        const preview = document.getElementById('tools-focus-time-preview');
        if (preview) preview.textContent = timeStr;
        const homeTimer = document.getElementById('home-timer-display');
        if (homeTimer) homeTimer.textContent = timeStr;

        // Update ring
        const ring = document.getElementById('focus-progress-ring');
        if (ring) {
          const perimeter = 2 * Math.PI * 100; // ~628.31
          const frac = focusModalTotalSeconds > 0 ? (focusModalSeconds / focusModalTotalSeconds) : 0;
          const offset = perimeter * (1 - frac);
          ring.style.strokeDashoffset = offset;
        }
      } catch (e) {
        console.warn('renderFocusModal notice:', e);
      }
    }"""

    if old_focus in content:
        content = content.replace(old_focus, new_focus, 1)
        print("Fixed renderFocusModal")
    else:
        print("Warning: old_focus not found")

    # 4. Habits, reading, gita modals -> openPlugin
    old_tool_modals = """    // Tool Modals
    function openHabitsModal() {
      const modal = document.getElementById('modal-habits');
      if (modal) modal.showModal();
    }

    function openReadingModal() {
      const modal = document.getElementById('modal-reading');
      if (modal) modal.showModal();
    }"""

    new_tool_modals = """    // Tool Modals
    function openHabitsModal() {
      openPlugin('com.v07.habits');
    }

    function openReadingModal() {
      openPlugin('com.v07.reader');
    }"""

    if old_tool_modals in content:
        content = content.replace(old_tool_modals, new_tool_modals, 1)
        print("Fixed openHabitsModal and openReadingModal")
    else:
        print("Warning: old_tool_modals not found")

    old_gita_modal = """    function openGitaModal() {
      const modal = document.getElementById('modal-gita');
      if (modal) {
        modal.showModal();
        loadGitaData();
      }
    }"""

    new_gita_modal = """    function openGitaModal() {
      openPlugin('com.v07.gita');
    }"""

    if old_gita_modal in content:
        content = content.replace(old_gita_modal, new_gita_modal, 1)
        print("Fixed openGitaModal")
    else:
        print("Warning: old_gita_modal not found")

    # 5. openMoodModal
    old_mood_modal = """    function openMoodModal() {
      const modal = document.getElementById('modal-mood');
      if (modal) modal.showModal();
    }"""

    new_mood_modal = """    function openMoodModal() {
      openPlugin('com.v07.vitals');
    }"""

    if old_mood_modal in content:
        content = content.replace(old_mood_modal, new_mood_modal, 1)
        print("Fixed openMoodModal")
    else:
        print("Warning: old_mood_modal not found")

    # 6. openPlugin redirect check & pluginMeta
    old_open_plugin_head = """    function openPlugin(pluginId) {
      if (pluginId === 'mood' || pluginId === 'com.v07.mood') {
        openMoodModal();
        return;
      }"""

    new_open_plugin_head = """    function openPlugin(pluginId) {"""

    if old_open_plugin_head in content:
        content = content.replace(old_open_plugin_head, new_open_plugin_head, 1)
        print("Fixed openPlugin head")
    else:
        print("Warning: old_open_plugin_head not found")

    old_meta_vitals = """        'com.v07.vitals': { name: 'Body Rhythm & Vitals', desc: 'Circadian presence & somatic metrics' },"""
    new_meta_vitals = """        'com.v07.vitals': { name: 'Body Rhythm & Vitals', desc: 'Circadian presence & somatic metrics' },
        'mood': { name: 'Body Rhythm & Vitals', desc: 'Circadian presence & somatic metrics' },
        'com.v07.mood': { name: 'Body Rhythm & Vitals', desc: 'Circadian presence & somatic metrics' },"""

    if old_meta_vitals in content:
        content = content.replace(old_meta_vitals, new_meta_vitals, 1)
        print("Fixed pluginMeta vitals/mood entries")
    else:
        print("Warning: old_meta_vitals not found")

    # 7. Remove duplicate openRecycleBinModal on line ~3546
    old_dup_recycle = """    async function openRecycleBinModal() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        modal.showModal();
        await loadRecycleBinData();
      }
    }"""

    if old_dup_recycle in content:
        content = content.replace(old_dup_recycle, "", 1)
        print("Removed duplicate openRecycleBinModal")
    else:
        print("Warning: old_dup_recycle not found")

    # Write back CRLF formatted
    final_content = content.replace('\n', '\r\n')
    with open(v3_html_path, 'wb') as f:
        f.write(final_content.encode('utf-8'))
    print(f"Updated {v3_html_path}")

    with open(root_html_path, 'wb') as f:
        f.write(final_content.encode('utf-8'))
    print(f"Synced {root_html_path}")

if __name__ == '__main__':
    main()
