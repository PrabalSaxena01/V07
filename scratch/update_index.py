import re

def update_index():
    paths = [
        r"D:\software\V07-Android\app\src\main\assets\v3\index.html",
        r"D:\software\V07-Android\app\src\main\assets\index.html"
    ]

    for p in paths:
        with open(p, "r", encoding="utf-8") as f:
            html = f.read()

        # 1. Define window.V07Platform at top of script
        bridge_init = """    // Platform Native RPC Bridge Initializer
    if (!window.V07Platform) {
      window.V07Platform = {
        version: '3.0.0',
        invoke: function(pluginId, method, paramsJson) {
          if (window.V3NativeBridge && typeof window.V3NativeBridge.invoke === 'function') {
            try {
              const p = typeof paramsJson === 'string' ? paramsJson : JSON.stringify(paramsJson || {});
              return window.V3NativeBridge.invoke(pluginId || 'core', method, p);
            } catch(e) {
              console.error('[V07Platform] invoke error:', e);
              return JSON.stringify({ ok: false, error: e.message });
            }
          }
          console.warn('[V07Platform] Native V3NativeBridge not found on window');
          return JSON.stringify({ ok: false, error: 'NO_NATIVE_BRIDGE' });
        }
      };
    }
    window.v07 = window.V07Platform;"""

        if "if (!window.V07Platform)" not in html:
            html = html.replace("<script>", "<script>\n" + bridge_init)
            print(f"Injected V07Platform bridge in {p}")

        # 2. Convert modal-focus dialog to full-view container
        old_focus_dialog_open = '<dialog id="modal-focus" class="fixed inset-0 m-auto w-[94%] max-w-[500px] max-h-[90vh] overflow-y-auto bg-surface rounded-2xl  p-0 outline-none border border-surface-container-high/50">'
        new_focus_fullview = '''<div id="modal-focus" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">
    <!-- Full-View Navigation Header -->
    <div class="h-14 px-4 bg-surface-container-lowest/90 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shrink-0">
      <div class="flex items-center gap-3">
        <button onclick="closeFocusModal()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Back">
          <span class="material-symbols-outlined text-[22px]">arrow_back</span>
        </button>
        <div class="flex flex-col">
          <span class="text-sm font-semibold text-on-surface leading-tight">Focus Sanctuary</span>
          <span class="text-[11px] text-on-surface-variant leading-tight">Quiet intervals for deep presence</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button onclick="resetFocusTimer()" class="h-8 px-3 rounded-full bg-surface-container-low hover:bg-surface-container text-xs font-semibold text-on-surface-variant flex items-center gap-1 transition-colors cursor-pointer active:scale-95" title="Reset">
          <span class="material-symbols-outlined text-[14px]">restart_alt</span>
          <span>Reset</span>
        </button>
        <button onclick="closeFocusModal()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
    </div>
    <!-- Scrollable Body -->
    <div class="flex-1 overflow-y-auto w-full max-w-[540px] mx-auto p-4 sm:p-6 flex flex-col items-center space-y-5">'''

        # Find closing </dialog> for modal-focus
        # Note: modal-focus has an internal header that we can replace with clean contents
        pattern_focus = r'<dialog id="modal-focus"[^>]*>[\s\S]*?<\/dialog>'
        # Let's inspect the entire modal-focus block
        m = re.search(pattern_focus, html)
        if m:
            focus_block = m.group(0)
            # Extract the inner controls after the old header
            # Old header was: <div class="p-4 border-b ...">...</div>
            # Then: <div class="p-5 flex flex-col items-center space-y-5">...</div>
            body_match = re.search(r'<div class="p-5 flex flex-col items-center space-y-5">([\s\S]*?)<\/dialog>', focus_block)
            if body_match:
                inner_body = body_match.group(1).rstrip()
                # remove any trailing </div> before </dialog>
                if inner_body.endswith("</div>"):
                    inner_body = inner_body[:-6]
                replacement_focus = new_focus_fullview + "\n" + inner_body + "\n    </div>\n  </div>"
                html = html.replace(focus_block, replacement_focus)
                print(f"Replaced modal-focus with Full View in {p}")
            else:
                print(f"Could not parse body in modal-focus for {p}")

        # 3. Convert modal-screentime dialog to full-view container
        pattern_screentime = r'<dialog id="modal-screentime"[^>]*>[\s\S]*?<\/dialog>'
        new_screentime_header = '''<div id="modal-screentime" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">
    <!-- Full-View Navigation Header -->
    <div class="h-14 px-4 bg-surface-container-lowest/90 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shrink-0">
      <div class="flex items-center gap-3">
        <button onclick="closeScreenTimeModal()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Back">
          <span class="material-symbols-outlined text-[22px]">arrow_back</span>
        </button>
        <div class="flex flex-col">
          <span class="text-sm font-semibold text-on-surface leading-tight">Digital Stillness</span>
          <span class="text-[11px] text-on-surface-variant leading-tight">App usage telemetry & limits</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button onclick="openUsagePermissionFromJs()" class="h-8 px-3 rounded-full bg-surface-container-low hover:bg-surface-container text-xs font-semibold text-secondary flex items-center gap-1 transition-colors cursor-pointer active:scale-95" title="Permissions">
          <span class="material-symbols-outlined text-[14px]">tune</span>
          <span>Perms</span>
        </button>
        <button onclick="closeScreenTimeModal()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
    </div>
    <!-- Scrollable Body -->
    <div class="flex-1 overflow-y-auto w-full max-w-[540px] mx-auto p-4 sm:p-6 space-y-4">'''

        m2 = re.search(pattern_screentime, html)
        if m2:
            st_block = m2.group(0)
            body_match2 = re.search(r'<div class="p-4 space-y-4">([\s\S]*?)<\/dialog>', st_block)
            if body_match2:
                inner_body2 = body_match2.group(1).rstrip()
                if inner_body2.endswith("</div>"):
                    inner_body2 = inner_body2[:-6]
                replacement_st = new_screentime_header + "\n" + inner_body2 + "\n    </div>\n  </div>"
                html = html.replace(st_block, replacement_st)
                print(f"Replaced modal-screentime with Full View in {p}")

        # 4. Convert modal-recycle-bin dialog to full-view container
        pattern_recycle = r'<dialog id="modal-recycle-bin"[^>]*>[\s\S]*?<\/dialog>'
        new_recycle_header = '''<div id="modal-recycle-bin" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">
    <!-- Full-View Navigation Header -->
    <div class="h-14 px-4 bg-surface-container-lowest/90 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shrink-0">
      <div class="flex items-center gap-3">
        <button onclick="closeRecycleBinModal()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Back">
          <span class="material-symbols-outlined text-[22px]">arrow_back</span>
        </button>
        <div class="flex flex-col">
          <span class="text-sm font-semibold text-on-surface leading-tight">Recycle Bin</span>
          <span class="text-[11px] text-on-surface-variant leading-tight">Restore or purge soft-deleted items</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button onclick="emptyRecycleBinPrompt()" class="h-8 px-3 rounded-full bg-error-container text-on-error-container text-xs font-semibold hover:opacity-90 transition-opacity flex items-center gap-1 cursor-pointer active:scale-95">
          <span class="material-symbols-outlined text-[14px]">delete_forever</span>
          <span>Empty</span>
        </button>
        <button onclick="closeRecycleBinModal()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>
    </div>
    <!-- Scrollable Body -->
    <div class="flex-1 overflow-y-auto w-full max-w-[540px] mx-auto p-4 sm:p-6 space-y-4">'''

        m3 = re.search(pattern_recycle, html)
        if m3:
            rc_block = m3.group(0)
            body_match3 = re.search(r'<div class="p-4 space-y-4">([\s\S]*?)<\/dialog>', rc_block)
            if body_match3:
                inner_body3 = body_match3.group(1).rstrip()
                if inner_body3.endswith("</div>"):
                    inner_body3 = inner_body3[:-6]
                replacement_rc = new_recycle_header + "\n" + inner_body3 + "\n    </div>\n  </div>"
                html = html.replace(rc_block, replacement_rc)
                print(f"Replaced modal-recycle-bin with Full View in {p}")

        # 5. Remove Category Filter Chips from Plugins Section
        pattern_chips = r'<!-- Filter Chips \(All, Mindfulness, Productivity, Body\) -->\s*<div class="flex items-center gap-1.5 overflow-x-auto pb-0.5 no-scrollbar" id="plugin-filter-chips">[\s\S]*?<\/div>'
        if re.search(pattern_chips, html):
            html = re.sub(pattern_chips, '', html)
            print(f"Removed plugin category filter chips in {p}")
        else:
            print(f"Warning: pattern_chips not found in {p}")

        # 6. Update openFocusModal, closeFocusModal, openScreenTimeModal, closeScreenTimeModal, closeAnyFullView
        js_modals = """    // Full-View Modal Handlers for Core Engines & Tools
    function openFocusModal() {
      const modal = document.getElementById('modal-focus');
      if (modal) {
        renderFocusModal();
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
      }
    }

    function closeFocusModal() {
      const modal = document.getElementById('modal-focus');
      if (modal) {
        modal.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(false);
      }
    }

    function openScreenTimeModal() {
      const modal = document.getElementById('modal-screentime');
      if (modal) {
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
        loadLiveScreenTime();
      }
    }

    function closeScreenTimeModal() {
      const modal = document.getElementById('modal-screentime');
      if (modal) {
        modal.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(false);
      }
    }

    function openRecycleBinModal() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
        renderRecycleBin();
      }
    }

    function closeRecycleBinModal() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        modal.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(false);
      }
    }

    function notifyNativeFullscreen(isOpen) {
      try {
        if (window.AndroidTheme && typeof window.AndroidTheme.setPluginFullscreen === 'function') {
          window.AndroidTheme.setPluginFullscreen(isOpen);
        }
      } catch(e) {
        console.warn('AndroidTheme bridge call failed', e);
      }
    }

    function closeAnyFullView() {
      // 1. Plugin Host
      const pHost = document.getElementById('modal-plugin-host');
      if (pHost && !pHost.classList.contains('translate-y-full')) {
        closePluginModal();
        return true;
      }
      // 2. Focus Sanctuary
      const fModal = document.getElementById('modal-focus');
      if (fModal && !fModal.classList.contains('translate-y-full')) {
        closeFocusModal();
        return true;
      }
      // 3. Digital Stillness
      const stModal = document.getElementById('modal-screentime');
      if (stModal && !stModal.classList.contains('translate-y-full')) {
        closeScreenTimeModal();
        return true;
      }
      // 4. Recycle Bin
      const rcModal = document.getElementById('modal-recycle-bin');
      if (rcModal && !rcModal.classList.contains('translate-y-full')) {
        closeRecycleBinModal();
        return true;
      }
      // 5. Standard open dialog fallback
      const openDialog = document.querySelector('dialog[open]');
      if (openDialog) {
        openDialog.close();
        return true;
      }
      return false;
    }

    window.closeFocusModal = closeFocusModal;
    window.closeScreenTimeModal = closeScreenTimeModal;
    window.closeRecycleBinModal = closeRecycleBinModal;
    window.openRecycleBinModal = openRecycleBinModal;
    window.closeAnyFullView = closeAnyFullView;"""

        # Replace openFocusModal block
        old_open_focus_def = """    function openFocusModal() {
      const modal = document.getElementById('modal-focus');
      if (modal) {
        renderFocusModal();
        modal.showModal();
      }
    }"""
        if old_open_focus_def in html:
            html = html.replace(old_open_focus_def, js_modals)
            print(f"Replaced openFocusModal and added Full View handlers in {p}")
        else:
            print(f"Warning: old_open_focus_def not found in {p}")

        # Update handleBackPress to use closeAnyFullView
        old_hbp = """    window.handleBackPress = function() {
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
    };"""

        new_hbp = """    window.handleBackPress = function() {
      return closeAnyFullView();
    };"""

        if old_hbp in html:
            html = html.replace(old_hbp, new_hbp)
            print(f"Updated window.handleBackPress to closeAnyFullView in {p}")
        else:
            print(f"Warning: old_hbp not found in {p}")

        with open(p, "w", encoding="utf-8") as f:
            f.write(html)

    print("Index HTML files successfully updated.")

if __name__ == "__main__":
    update_index()
