missing_fns = """
    // ==========================================
    // NOTIFICATIONS & DRAWER HELPERS
    // ==========================================
    function openProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
    }

    function clearNotifications() {
      const list = document.getElementById('notifications-list');
      if (list) {
        list.innerHTML = `
          <div class="py-8 text-center text-xs text-on-surface-variant flex flex-col items-center gap-1.5">
            <span class="material-symbols-outlined text-[24px] opacity-40">notifications_off</span>
            <span>All caught up. No unread notifications.</span>
          </div>
        `;
      }
      if (typeof showToast === 'function') {
        showToast('Notifications Cleared', 'All notices marked as read', 'info');
      }
    }

    function triggerTestNotification() {
      const list = document.getElementById('notifications-list');
      if (list) {
        const item = document.createElement('div');
        item.className = 'p-3 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex items-start gap-3';
        item.innerHTML = `
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">notifications_active</span>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-on-surface">Sanctuary Reminder</span>
              <span class="text-[10px] text-primary font-semibold">Just now</span>
            </div>
            <p class="text-xs text-on-surface-variant mt-0.5">Stay centered. A mindful breath restores clear presence.</p>
          </div>
        `;
        list.insertBefore(item, list.firstChild);
      }
      if (typeof showToast === 'function') {
        showToast('Notification Dispatched', 'Calm mindfulness prompt delivered', 'success');
      }
    }

    function setCatalogFilter(filter) {
      if (typeof catalogFilter !== 'undefined') {
        catalogFilter = filter;
      }
      document.querySelectorAll('.cat-filter-btn').forEach(btn => {
        if (btn.getAttribute('data-filter') === filter) {
          btn.className = 'cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary transition-all cursor-pointer';
        } else {
          btn.className = 'cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-surface-container text-on-surface-variant transition-all cursor-pointer';
        }
      });
      if (typeof renderPluginCatalog === 'function') {
        renderPluginCatalog();
      }
    }

    function selectWeek(weekKey, btn) {
      document.querySelectorAll('.week-chip').forEach(chip => {
        chip.className = 'week-chip px-3.5 py-1.5 rounded-full text-xs font-semibold bg-surface-container text-on-surface-variant hover:bg-surface-container-high shrink-0 cursor-pointer';
      });
      if (btn) {
        btn.className = 'week-chip px-3.5 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary shadow-xs shrink-0 cursor-pointer';
      }
      if (typeof showToast === 'function') {
        showToast('Week Selected', 'Viewing analytics for ' + weekKey, 'info');
      }
    }
"""

targets = [
    'app/src/main/assets/v3/index.html',
    'app/src/main/assets/index.html'
]

for target in targets:
    with open(target, 'r', encoding='utf-8') as f:
        content = f.read()

    # Insert right before function closeProfileDrawer()
    marker = "function closeProfileDrawer()"
    if marker in content and "function openProfileDrawer()" not in content:
        content = content.replace(marker, missing_fns + "\n    " + marker, 1)
        with open(target, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Added missing functions to {target}")
    else:
        print(f"Already present or marker not found in {target}")
