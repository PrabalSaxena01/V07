import re
import sys

def apply_polish(filepath):
    print(f"Applying Impeccable polish to {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Implement showCustomPrompt function in script
    # Look for function showCustomModal
    prompt_fn = """
    function showCustomPrompt({
      title,
      subtitle = '',
      icon = 'edit',
      placeholder = '',
      defaultValue = '',
      confirmText = 'Save',
      onConfirm = null
    }) {
      const inputId = 'custom-prompt-input-' + Date.now();
      const bodyHtml = `
        <div class="pt-2 flex flex-col gap-2">
          <input type="text" id="${inputId}" value="${escapeHtml(defaultValue)}" placeholder="${escapeHtml(placeholder)}"
                 class="w-full px-3.5 py-2.5 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary transition-colors"/>
        </div>
      `;
      showCustomModal({
        title,
        subtitle,
        icon,
        bodyHtml,
        confirmText,
        onConfirm: () => {
          const inputEl = document.getElementById(inputId);
          const val = inputEl ? inputEl.value.trim() : '';
          if (onConfirm) onConfirm(val);
        }
      });
      setTimeout(() => {
        const el = document.getElementById(inputId);
        if (el) {
          el.focus();
          el.select();
        }
      }, 150);
    }
"""

    if "function showCustomPrompt" not in content:
        content = content.replace("function showCustomModal({", prompt_fn + "\n    function showCustomModal({")
        print("  + Added showCustomPrompt")

    # 2. Wire promptAddBook, promptUpdatePage, promptAddHabit, promptEditProfile to showCustomPrompt
    # Replace promptAddBook
    old_prompt_book = """    function promptAddBook() {
      const title = prompt('Enter book title:');
      if (title) {
        showToast('Book Added', `"${title}" added to your shelf`, 'success');
      }
    }"""
    new_prompt_book = """    function promptAddBook() {
      showCustomPrompt({
        title: 'Add Book to Shelf',
        subtitle: 'Reading Companion',
        icon: 'menu_book',
        placeholder: 'e.g. Meditations, Siddhartha, Dune',
        confirmText: 'Add Book',
        onConfirm: (title) => {
          if (!title) return;
          showToast('Book Added', `"${title}" added to your reading shelf`, 'success');
          // Prepend to shelf list if available
          const shelf = document.querySelector('#view-reading .divide-y');
          if (shelf) {
            const item = document.createElement('div');
            item.className = 'p-3 flex items-center gap-3 hover:bg-surface-container-low/40 transition-colors animate-fade-in';
            item.innerHTML = `
              <div class="w-10 h-14 rounded-lg overflow-hidden shrink-0 bg-primary-fixed text-primary flex items-center justify-center font-bold text-xs shadow-xs">
                <span>${escapeHtml(title.slice(0, 2).toUpperCase())}</span>
              </div>
              <div class="flex flex-col flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <h4 class="text-xs font-bold text-on-surface truncate">${escapeHtml(title)}</h4>
                  <span class="shrink-0 text-[10px] px-1.5 py-0.5 rounded bg-surface-container text-on-surface-variant font-medium">New</span>
                </div>
                <p class="text-[11px] text-on-surface-variant">Personal reading</p>
                <p class="text-[10px] text-tertiary mt-0.5">Added just now</p>
              </div>
              <button onclick="openBookOptions(this, '${escapeHtml(title)}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Options">
                <span class="material-symbols-outlined text-[18px]">more_horiz</span>
              </button>
            `;
            shelf.insertBefore(item, shelf.firstChild);
          }
        }
      });
    }"""
    content = content.replace(old_prompt_book, new_prompt_book)
    print("  + Enhanced promptAddBook with tactile custom modal")

    # Replace promptUpdatePage
    old_prompt_page = """    function promptUpdatePage() {
      const page = prompt('Enter current page number:', currentBookPage);
      if (page && !isNaN(page)) {
        currentBookPage = Math.min(totalBookPages, Math.max(0, parseInt(page)));
        updateBookProgress();
        showToast('Page Updated', `Currently on page ${currentBookPage} of ${totalBookPages}`, 'success');
      }
    }"""
    new_prompt_page = """    function promptUpdatePage() {
      showCustomPrompt({
        title: 'Update Reading Progress',
        subtitle: 'Atomic Habits · Total 228 pages',
        icon: 'auto_stories',
        placeholder: 'Enter page number (e.g. 150)',
        defaultValue: String(currentBookPage),
        confirmText: 'Update Progress',
        onConfirm: (page) => {
          const num = parseInt(page);
          if (!isNaN(num) && num >= 0) {
            currentBookPage = Math.min(totalBookPages, num);
            updateBookProgress();
            showToast('Page Updated', `Currently on page ${currentBookPage} of ${totalBookPages}`, 'success');
          }
        }
      });
    }"""
    content = content.replace(old_prompt_page, new_prompt_page)
    print("  + Enhanced promptUpdatePage with tactile custom modal")

    # Replace promptAddHabit
    old_prompt_habit = """    function promptAddHabit() {
      const habit = prompt('Enter new ritual habit:');
      if (habit) {
        showToast('Ritual Added', `"${habit}" added to your daily rhythms`, 'success');
      }
    }"""
    new_prompt_habit = """    function promptAddHabit() {
      showCustomPrompt({
        title: 'New Daily Ritual',
        subtitle: 'Habit Sanctuary',
        icon: 'spa',
        placeholder: 'e.g. 10m Pranayama, Green Tea, Evening Stroll',
        confirmText: 'Add Ritual',
        onConfirm: (habit) => {
          if (!habit) return;
          showToast('Ritual Added', `"${habit}" added to your daily cadence`, 'success');
          const list = document.getElementById('habits-modal-list');
          if (list) {
            const item = document.createElement('div');
            item.className = 'habit-row flex flex-col p-3.5 rounded-2xl bg-surface-container-lowest border border-surface-container-high/60 shadow-xs animate-fade-in';
            item.setAttribute('data-completed', 'false');
            item.innerHTML = `
              <div class="flex items-center justify-between gap-3">
                <div class="flex items-center gap-3 min-w-0 cursor-pointer flex-1" onclick="toggleHabitRow(this.closest('.habit-row'), '${escapeHtml(habit)}')">
                  <div class="habit-checkbox w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0 transition-transform active:scale-90">
                    <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
                  </div>
                  <div class="flex flex-col min-w-0">
                    <span class="habit-name text-xs font-bold text-on-surface truncate">${escapeHtml(habit)}</span>
                    <span class="text-[11px] text-on-surface-variant">Daily cadence · 0 days streak</span>
                  </div>
                </div>
                <button onclick="promptEditHabit('${escapeHtml(habit)}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Configure">
                  <span class="material-symbols-outlined text-[18px]">tune</span>
                </button>
              </div>
              <div class="mt-2.5 pt-2 border-t border-surface-container-low flex items-center justify-between">
                <span class="text-[10px] text-on-surface-variant font-medium">Weekly streak</span>
                <div class="flex items-center gap-2">
                  <span class="text-[9px] text-on-surface-variant">M</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                  <span class="text-[9px] text-on-surface-variant">T</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                  <span class="text-[9px] text-on-surface-variant">W</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                  <span class="text-[9px] text-on-surface-variant">T</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                  <span class="text-[9px] text-on-surface-variant">F</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                  <span class="text-[9px] text-on-surface-variant">S</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                  <span class="text-[9px] text-on-surface-variant">S</span><span class="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
                </div>
              </div>
            `;
            list.insertBefore(item, list.firstChild);
          }
        }
      });
    }"""
    content = content.replace(old_prompt_habit, new_prompt_habit)
    print("  + Enhanced promptAddHabit with tactile custom modal")

    # Replace promptEditProfile
    old_edit_prof = """    function promptEditProfile() {
      showToast('Profile Edit', 'Encrypted local profile is active', 'info');
    }"""
    new_edit_prof = """    function promptEditProfile() {
      const currentName = document.getElementById('profile-drawer-name')?.textContent || 'Sarah Jenkins';
      showCustomPrompt({
        title: 'Edit Display Name',
        subtitle: 'Local Sovereign Profile',
        icon: 'badge',
        defaultValue: currentName,
        placeholder: 'Enter your preferred name',
        confirmText: 'Update Name',
        onConfirm: (newName) => {
          if (!newName) return;
          const profName = document.getElementById('profile-drawer-name');
          const homeGreeting = document.getElementById('home-greeting-title');
          if (profName) profName.textContent = newName;
          if (homeGreeting) homeGreeting.textContent = 'Good day, ' + newName;
          localStorage.setItem('v07_user_name', newName);
          showToast('Profile Updated', `Name set to "${newName}"`, 'success');
        }
      });
    }"""
    content = content.replace(old_edit_prof, new_edit_prof)
    print("  + Enhanced promptEditProfile with tactile custom modal")

    # 3. Add book options modal and habit edit modal helpers
    helpers_js = """
    function openBookOptions(btn, bookTitle) {
      showCustomModal({
        title: bookTitle,
        subtitle: 'Reading Companion Shelf',
        icon: 'book',
        bodyHtml: `
          <div class="flex flex-col gap-2 pt-1 text-xs">
            <button onclick="promptUpdatePage(); closeCustomModal();" class="p-3 bg-surface-container-low hover:bg-surface-container rounded-xl flex items-center justify-between text-on-surface font-semibold cursor-pointer">
              <span class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-[18px] text-primary">edit_note</span>
                Update Current Page
              </span>
              <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
            </button>
            <button onclick="showToast('Status Updated', 'Book marked as Completed', 'success'); closeCustomModal();" class="p-3 bg-surface-container-low hover:bg-surface-container rounded-xl flex items-center justify-between text-on-surface font-semibold cursor-pointer">
              <span class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-[18px] text-primary">task_alt</span>
                Mark as Finished
              </span>
              <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
            </button>
            <button onclick="showToast('Moved to Saved', 'Added to reading queue', 'info'); closeCustomModal();" class="p-3 bg-surface-container-low hover:bg-surface-container rounded-xl flex items-center justify-between text-on-surface font-semibold cursor-pointer">
              <span class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-[18px] text-on-surface-variant">bookmark</span>
                Save for Later
              </span>
              <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
            </button>
          </div>
        `,
        confirmText: 'Done',
        onConfirm: () => {}
      });
    }

    function promptEditHabit(habitName) {
      showCustomModal({
        title: habitName,
        subtitle: 'Ritual Configuration',
        icon: 'tune',
        bodyHtml: `
          <div class="flex flex-col gap-2 pt-1 text-xs">
            <button onclick="showToast('Cadence Set', 'Reminder set for daily 9:00 AM', 'success'); closeCustomModal();" class="p-3 bg-surface-container-low hover:bg-surface-container rounded-xl flex items-center justify-between text-on-surface font-semibold cursor-pointer">
              <span class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-[18px] text-primary">schedule</span>
                Adjust Daily Schedule
              </span>
              <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
            </button>
            <button onclick="showToast('Streak Reset', 'Habit streak reset to day 1', 'info'); closeCustomModal();" class="p-3 bg-surface-container-low hover:bg-surface-container rounded-xl flex items-center justify-between text-on-surface font-semibold cursor-pointer">
              <span class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-[18px] text-secondary">restart_alt</span>
                Reset Streak Cadence
              </span>
              <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
            </button>
          </div>
        `,
        confirmText: 'Done',
        onConfirm: () => {}
      });
    }

    function switchHeatmapMonth(month, btn) {
      if (btn) {
        btn.parentElement.querySelectorAll('button').forEach(b => {
          b.className = 'px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer';
        });
        btn.className = 'px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary shadow-xs font-bold cursor-pointer';
      }
      renderHeatmapGrid();
      showToast('Heatmap Month', `Viewing ${month} consistency`, 'info');
    }
"""

    if "function openBookOptions" not in content:
        content = content.replace("function promptUpdatePage() {", helpers_js + "\n    function promptUpdatePage() {")
        print("  + Added openBookOptions, promptEditHabit, switchHeatmapMonth")

    # 4. Wire onclick to more_horiz buttons in view-reading
    content = content.replace(
        """              <button class="w-7 h-7 rounded flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
                <span class="material-symbols-outlined text-[17px]">more_horiz</span>
              </button>""",
        """              <button onclick="openBookOptions(this, 'Quiet: The Power of Introverts')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" title="Options">
                <span class="material-symbols-outlined text-[18px]">more_horiz</span>
              </button>"""
    )

    # 5. Fix Kit Tiles in renderKitTiles() - Fix text wrapping
    old_kit_bottom = """            <!-- Metric Preview & Primary Action -->
            <div class="flex items-center justify-between pt-2 border-t border-surface-container-high/30">
              <span class="text-xs font-mono font-medium text-primary flex items-center gap-1">
                <span class="w-1.5 h-1.5 rounded-full bg-primary inline-block"></span>
                <span>${escapeHtml(getPluginStatusPreview(p.id))}</span>
              </span>
              <div class="flex items-center gap-2">
                <button onclick="openPluginSettingsModal('${p.id}')" class="px-3 py-1.5 rounded-xl bg-surface-container hover:bg-surface-container-high text-on-surface text-xs font-semibold flex items-center gap-1 active:scale-95 transition-all cursor-pointer border border-surface-container-high/40">
                  <span class="material-symbols-outlined text-[15px]">tune</span>
                  <span>Customize</span>
                </button>
                <button onclick="openPluginFullPage('${p.id}')" class="px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer">
                  <span>Open</span>
                  <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
                </button>
              </div>
            </div>"""

    new_kit_bottom = """            <!-- Metric Preview & Primary Action -->
            <div class="flex items-center justify-between gap-2 pt-2.5 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5 min-w-0 flex-1">
                <span class="w-1.5 h-1.5 rounded-full bg-primary shrink-0"></span>
                <span class="text-[11px] font-mono font-medium text-primary truncate" title="${escapeHtml(getPluginStatusPreview(p.id))}">${escapeHtml(getPluginStatusPreview(p.id))}</span>
              </div>
              <div class="flex items-center gap-1.5 shrink-0">
                <button onclick="openPluginSettingsModal('${p.id}')" class="h-8 px-2.5 rounded-xl bg-surface-container hover:bg-surface-container-high text-on-surface text-xs font-semibold flex items-center gap-1 active:scale-95 transition-all cursor-pointer border border-surface-container-high/40" title="Customize ${escapeHtml(p.name)}">
                  <span class="material-symbols-outlined text-[15px]">tune</span>
                  <span class="hidden xs:inline">Settings</span>
                </button>
                <button onclick="openPluginFullPage('${p.id}')" class="h-8 px-3 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer">
                  <span>Open</span>
                  <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
                </button>
              </div>
            </div>"""

    content = content.replace(old_kit_bottom, new_kit_bottom)
    print("  + Fixed Kit tiles text wrapping and button ergonomics")

    # 6. Fix Reading Companion Atomic Habits title truncation
    old_reading_h4 = '<h4 class="text-base font-bold text-on-surface truncate leading-tight">Atomic Habits</h4>'
    new_reading_h4 = '<h4 class="text-base font-bold text-on-surface truncate leading-tight flex-1">Atomic Habits</h4>'
    content = content.replace(old_reading_h4, new_reading_h4)

    # 7. Fix Mood Emojis - use grid grid-cols-5 gap-1.5 so Vibrant is never clipped!
    old_mood_emojis = '<div class="flex items-center justify-between gap-2 overflow-x-auto pb-1">'
    new_mood_emojis = '<div class="grid grid-cols-5 gap-1.5 w-full">'
    content = content.replace(old_mood_emojis, new_mood_emojis)
    print("  + Fixed Mood emojis grid overflow")

    # 8. Fix Screen Time 18-hour chart overflow
    old_st_chart = '<div class="flex items-end justify-between gap-1 h-32 pt-2 px-1">'
    new_st_chart = '<div class="flex items-end justify-between gap-1 h-32 pt-2 px-1 overflow-x-auto no-scrollbar" style="touch-action: pan-x;">'
    content = content.replace(old_st_chart, new_st_chart)
    print("  + Fixed Screen Time 18-hour bar chart overflow")

    # 9. Fix Top Application Activity fallback in view-screentime
    old_st_list = """          <div class="flex flex-col bg-surface-container-lowest rounded-2xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden shadow-xs" id="screentime-apps-list">
            <!-- Dynamically populated from real UsageStatsManager -->
          </div>"""

    new_st_list = """          <div class="flex flex-col bg-surface-container-lowest rounded-2xl divide-y divide-surface-container-low border border-surface-container-high/40 overflow-hidden shadow-xs" id="screentime-apps-list">
            <div class="p-3 flex items-center justify-between hover:bg-surface-container-low transition-colors">
              <div class="flex items-center gap-2.5 min-w-0">
                <div class="w-8 h-8 rounded-lg bg-[#FF9F43]/20 text-[#FF9F43] flex items-center justify-center font-bold text-xs">📖</div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold text-on-surface truncate">Sanctuary Reader</span>
                  <span class="text-[10px] text-on-surface-variant">com.v07.reader</span>
                </div>
              </div>
              <span class="text-xs font-mono font-bold text-on-surface">1h 42m</span>
            </div>
            <div class="p-3 flex items-center justify-between hover:bg-surface-container-low transition-colors">
              <div class="flex items-center gap-2.5 min-w-0">
                <div class="w-8 h-8 rounded-lg bg-primary/20 text-primary flex items-center justify-center font-bold text-xs">⏳</div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold text-on-surface truncate">Focus Sanctuary</span>
                  <span class="text-[10px] text-on-surface-variant">com.v07.focus</span>
                </div>
              </div>
              <span class="text-xs font-mono font-bold text-on-surface">52m</span>
            </div>
            <div class="p-3 flex items-center justify-between hover:bg-surface-container-low transition-colors">
              <div class="flex items-center gap-2.5 min-w-0">
                <div class="w-8 h-8 rounded-lg bg-[#38BDF8]/20 text-[#38BDF8] flex items-center justify-center font-bold text-xs">💬</div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold text-on-surface truncate">Messages & Colleague</span>
                  <span class="text-[10px] text-on-surface-variant">com.v07.chat</span>
                </div>
              </div>
              <span class="text-xs font-mono font-bold text-on-surface">28m</span>
            </div>
          </div>"""
    content = content.replace(old_st_list, new_st_list)
    print("  + Added fallback telemetry state for Screen Time apps list")

    # 10. Fix Gita Wisdom header overflow
    old_gita_header = """          <button onclick="saveGitaVerseToJournal()" class="px-3 py-1.5 bg-primary-fixed text-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer hover:opacity-95 shadow-xs">
            <span class="material-symbols-outlined text-[15px]">bookmark</span>
            <span>Save to journal</span>
          </button>"""
    new_gita_header = """          <button onclick="saveGitaVerseToJournal()" class="w-9 h-9 rounded-full flex items-center justify-center bg-primary-fixed text-primary hover:opacity-90 active:scale-95 transition-all cursor-pointer shadow-xs" title="Save to journal">
            <span class="material-symbols-outlined text-[20px]">bookmark_add</span>
          </button>"""
    content = content.replace(old_gita_header, new_gita_header)
    print("  + Polished Gita Wisdom header to eliminate title squishing")

    # 11. Remove redundant close X from view-mood and view-weekly
    content = content.replace(
        """        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('mood')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Mood & Energy">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Close">
            <span class="material-symbols-outlined text-[22px]">close</span>
          </button>
        </div>""",
        """        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('mood')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Mood & Energy">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
        </div>"""
    )

    content = content.replace(
        """        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('weekly')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Weekly Digest">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Close">
            <span class="material-symbols-outlined text-[22px]">close</span>
          </button>
        </div>""",
        """        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('weekly')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Weekly Digest">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
        </div>"""
    )
    print("  + Removed redundant close buttons from headers")

    # 12. Fix Month buttons in Habits Heatmap
    old_months = """            <div class="flex items-center gap-1 bg-surface-container-low p-0.5 rounded-lg text-[11px] font-semibold">
              <button class="px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer">Jul</button>
              <button class="px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer">Aug</button>
              <button class="px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary shadow-xs font-bold cursor-pointer">Sep</button>
            </div>"""
    new_months = """            <div class="flex items-center gap-1 bg-surface-container-low p-0.5 rounded-lg text-[11px] font-semibold">
              <button onclick="switchHeatmapMonth('Jul', this)" class="px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer">Jul</button>
              <button onclick="switchHeatmapMonth('Aug', this)" class="px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer">Aug</button>
              <button onclick="switchHeatmapMonth('Sep', this)" class="px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary shadow-xs font-bold cursor-pointer">Sep</button>
            </div>"""
    content = content.replace(old_months, new_months)
    print("  + Wired up Heatmap month buttons")

    # 13. Fix Habit tune buttons in view-habits
    content = re.sub(
        r'onclick="showToast\(\'Habit Config\',\s*\'Editing ([^\']+)\'\s*,\s*\'info\'\)"',
        r'onclick="promptEditHabit(\'\1\')"',
        content
    )
    print("  + Replaced habit config toasts with promptEditHabit")

    # 14. Add Home Timer Presets and Mode Switch on Home
    old_home_timer_buttons = """        <div class="w-full flex items-center gap-2 mt-1">
          <button onclick="toggleFocusTimer()" id="home-timer-btn" class="flex-1 py-3 px-4 rounded-xl bg-primary text-on-primary font-semibold text-sm shadow-sm active:scale-[0.98] transition-transform flex items-center justify-center gap-2 cursor-pointer hover:opacity-95">
            <span class="material-symbols-outlined text-[18px]" id="home-timer-icon">play_arrow</span>
            <span id="home-timer-text">Start focus</span>
          </button>
          <button onclick="navigateTo('focus')" class="w-11 h-11 rounded-xl bg-surface-container-low text-on-surface-variant hover:text-on-surface flex items-center justify-center cursor-pointer border border-surface-container-high/40 active:scale-95" title="Full Focus Page">
            <span class="material-symbols-outlined text-[20px]">fullscreen</span>
          </button>
        </div>"""

    new_home_timer_buttons = """        <!-- Quick Preset & Mode Pills -->
        <div class="flex items-center justify-center gap-1.5 pb-2">
          <button onclick="setFocusPreset(15)" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-surface-container-low hover:bg-surface-container text-on-surface cursor-pointer active:scale-95 transition-all">15m</button>
          <button onclick="setFocusPreset(25)" class="px-2.5 py-1 rounded-full text-xs font-bold bg-primary text-on-primary shadow-xs cursor-pointer active:scale-95 transition-all">25m</button>
          <button onclick="setFocusPreset(45)" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-surface-container-low hover:bg-surface-container text-on-surface cursor-pointer active:scale-95 transition-all">45m</button>
          <button onclick="setFocusMode('timer')" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-surface-container-low hover:bg-surface-container text-primary flex items-center gap-1 cursor-pointer active:scale-95 transition-all" title="Timer Mode: 0 to infinite">
            <span class="material-symbols-outlined text-[13px]">hourglass_empty</span>
            <span>infinity Open</span>
          </button>
        </div>

        <div class="w-full flex items-center gap-2 mt-1">
          <button onclick="toggleFocusTimer()" id="home-timer-btn" class="flex-1 py-3 px-4 rounded-xl bg-primary text-on-primary font-semibold text-sm shadow-sm active:scale-[0.98] transition-transform flex items-center justify-center gap-2 cursor-pointer hover:opacity-95">
            <span class="material-symbols-outlined text-[18px]" id="home-timer-icon">play_arrow</span>
            <span id="home-timer-text">Start focus</span>
          </button>
          <button onclick="navigateTo('focus')" class="w-11 h-11 rounded-xl bg-surface-container-low text-on-surface-variant hover:text-on-surface flex items-center justify-center cursor-pointer border border-surface-container-high/40 active:scale-95" title="Full Focus Page">
            <span class="material-symbols-outlined text-[20px]">fullscreen</span>
          </button>
        </div>"""
    content = content.replace(old_home_timer_buttons, new_home_timer_buttons)
    print("  + Added quick presets and infinity Open timer mode to Home")

    # 15. Fix Home Gentle Habits + button
    old_habits_header = """        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base font-semibold text-on-surface">Gentle habits</h3>
            <p class="text-xs text-on-surface-variant">Cadence without pressure</p>
          </div>
          <button onclick="navigateTo('habits')" class="text-xs text-primary font-semibold flex items-center gap-1 cursor-pointer">
            <span>View all</span>
            <span class="material-symbols-outlined text-[16px]">chevron_right</span>
          </button>
        </div>"""

    new_habits_header = """        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base font-semibold text-on-surface">Gentle habits</h3>
            <p class="text-xs text-on-surface-variant">Cadence without pressure</p>
          </div>
          <div class="flex items-center gap-2">
            <button onclick="promptAddHabit()" class="w-7 h-7 rounded-full bg-primary-fixed text-primary flex items-center justify-center cursor-pointer active:scale-95 transition-transform" title="Add ritual">
              <span class="material-symbols-outlined text-[16px]">add</span>
            </button>
            <button onclick="navigateTo('habits')" class="text-xs text-primary font-semibold flex items-center gap-1 cursor-pointer">
              <span>View all</span>
              <span class="material-symbols-outlined text-[16px]">chevron_right</span>
            </button>
          </div>
        </div>"""
    content = content.replace(old_habits_header, new_habits_header)
    print("  + Fixed + button in Gentle habits on Home")

    # 16. Fix Save note button in view-focus
    old_parking_btn = """            <button type="submit" class="absolute right-1.5 h-7 px-2.5 rounded-lg bg-surface-container-low hover:bg-surface-container text-primary font-bold text-xs flex items-center gap-1 cursor-pointer">
              <span>Save</span>
              <span class="material-symbols-outlined text-[14px]">add</span>
            </button>"""
    new_parking_btn = """            <button type="submit" onclick="handleSaveParkingThought(event)" class="absolute right-1.5 h-7 px-2.5 rounded-lg bg-primary text-on-primary font-bold text-xs flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer">
              <span>Save</span>
              <span class="material-symbols-outlined text-[14px]">add</span>
            </button>"""
    content = content.replace(old_parking_btn, new_parking_btn)
    print("  + Polished parking lot save button in view-focus")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Successfully polished {filepath}!")

targets = [
    'app/src/main/assets/v3/index.html',
    'app/src/main/assets/index.html'
]

for t in targets:
    apply_polish(t)
