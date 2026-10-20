#!/usr/bin/env python3
"""
Replace raw prompt() and alert() in app/src/main/assets/v3/index.html
with Calm Space Editorial openPromptModal() and showToast().
"""

import os

INDEX_PATH = r"D:\software\V07-Android\app\src\main\assets\v3\index.html"

with open(INDEX_PATH, "r", encoding="utf-8") as f:
    c = f.read()

# 1. Add openPromptModal right next to showToast
old_toast_anchor = """    window.showToast = showToast;"""

new_toast_and_prompt = """    window.showToast = showToast;

    function openPromptModal(title, defaultValue, onConfirm) {
      let modal = document.getElementById('v07-prompt-modal');
      if (!modal) {
        modal = document.createElement('div');
        modal.id = 'v07-prompt-modal';
        modal.className = 'fixed inset-0 z-[4000] bg-black/40 backdrop-blur-xs flex items-center justify-center p-4';
        modal.innerHTML = `
          <div class="bg-surface rounded-2xl p-5 max-w-xs w-full shadow-2xl border border-outline-variant/40">
            <h3 id="v07-prompt-title" class="font-headline-sm text-sm font-bold text-on-surface mb-2"></h3>
            <input type="text" id="v07-prompt-input" class="w-full px-3.5 py-2.5 rounded-xl border border-outline-variant bg-surface-container-lowest text-on-surface text-sm outline-none focus:border-primary mb-4">
            <div class="flex justify-end gap-2">
              <button id="v07-prompt-cancel" class="px-3.5 py-1.5 rounded-full text-xs font-semibold text-on-surface-variant hover:bg-surface-container">Cancel</button>
              <button id="v07-prompt-ok" class="px-4 py-1.5 rounded-full text-xs font-semibold bg-primary text-on-primary shadow-xs">Confirm</button>
            </div>
          </div>
        `;
        document.body.appendChild(modal);
      }
      document.getElementById('v07-prompt-title').textContent = title;
      const input = document.getElementById('v07-prompt-input');
      input.value = defaultValue || '';
      modal.style.display = 'flex';
      setTimeout(() => input.focus(), 60);

      const close = () => { modal.style.display = 'none'; };
      document.getElementById('v07-prompt-cancel').onclick = close;
      const handleOk = () => {
        const val = input.value;
        close();
        if (onConfirm && val) onConfirm(val);
      };
      document.getElementById('v07-prompt-ok').onclick = handleOk;
      input.onkeydown = (e) => {
        if (e.key === 'Enter') handleOk();
        if (e.key === 'Escape') close();
      };
    }
    window.openPromptModal = openPromptModal;"""

c = c.replace(old_toast_anchor, new_toast_and_prompt)

# 2. Replace promptAddTask
old_promptAddTask = """    function promptAddTask() {
      const title = prompt('Enter a gentle task for today:');
      if (title && title.trim()) {
        const clean = cleanText(title);
        const newTask = {
          id: 'task_' + Date.now(),
          title: clean,
          time: 'Added moments ago',
          done: false
        };
        tasks.push(newTask);
        renderTasks();
        apiV3('/api/v3/tasks', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ title: clean, priority: 'medium', due_date: new Date().toISOString().split('T')[0] })
        }).catch(() => {});
      }
    }"""

new_promptAddTask = """    function promptAddTask() {
      openPromptModal('Enter a gentle task for today:', '', (title) => {
        if (title && title.trim()) {
          const clean = cleanText(title);
          const newTask = {
            id: 'task_' + Date.now(),
            title: clean,
            time: 'Added moments ago',
            done: false
          };
          tasks.push(newTask);
          renderTasks();
          showToast('Added task: ' + clean, 'success');
          apiV3('/api/v3/tasks', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ title: clean, priority: 'medium', due_date: new Date().toISOString().split('T')[0] })
          }).catch(() => {});
        }
      });
    }"""

c = c.replace(old_promptAddTask.replace('\r\n', '\n'), new_promptAddTask.replace('\r\n', '\n'))

# 3. Replace editThought
old_editThought = """    function editThought(id) {
      const entry = thoughts.find(t => t.id === id);
      if (!entry) return;
      const updated = prompt('Edit your reflection:', entry.text);
      if (updated && updated.trim()) {
        entry.text = cleanText(updated);
        renderJournalEntries();
      }
    }"""

new_editThought = """    function editThought(id) {
      const entry = thoughts.find(t => t.id === id);
      if (!entry) return;
      openPromptModal('Edit your reflection:', entry.text, (updated) => {
        if (updated && updated.trim()) {
          entry.text = cleanText(updated);
          renderJournalEntries();
          showToast('Reflection updated', 'success');
        }
      });
    }"""

c = c.replace(old_editThought.replace('\r\n', '\n'), new_editThought.replace('\r\n', '\n'))

# 4. Replace promptEditProfile
old_promptEditProfile = """    function promptEditProfile() {
      const name = prompt('Update sanctuary profile name:', document.getElementById('profile-name').textContent);
      if (name && name.trim()) {
        const clean = cleanText(name);
        document.getElementById('profile-name').textContent = clean;
        localStorage.setItem('v07_profile_name', clean);
      }
    }"""

new_promptEditProfile = """    function promptEditProfile() {
      openPromptModal('Update sanctuary profile name:', document.getElementById('profile-name').textContent, (name) => {
        if (name && name.trim()) {
          const clean = cleanText(name);
          document.getElementById('profile-name').textContent = clean;
          localStorage.setItem('v07_profile_name', clean);
          showToast('Profile name updated', 'success');
        }
      });
    }"""

c = c.replace(old_promptEditProfile.replace('\r\n', '\n'), new_promptEditProfile.replace('\r\n', '\n'))

# 5. Replace shareSanctuaryStatus alert
c = c.replace("alert('Status shared privately.');", "showToast('Status shared privately.', 'success');")

# 6. Replace pickVaultFolder alert
c = c.replace("alert('Vault directory is secured at /storage/emulated/0/V07Data');", "showToast('Vault directory is secured at /storage/emulated/0/V07Data', 'info');")

# 7. Replace inspectDatabase alert
old_inspect = """    function inspectDatabase() {
      const thoughtsCount = (typeof thoughts !== 'undefined' ? thoughts.length : 3);
      const tasksCount = (typeof tasks !== 'undefined' ? tasks.length : 4);
      const habitsCount = (typeof habits !== 'undefined' ? habits.length : 5);
      const statsMsg = "V07 Sovereign SQLite Engine:\\n" +
                       "• Architecture: V3 Sandboxed Platform\\n" +
                       "• Journal Entries: " + thoughtsCount + " indexed\\n" +
                       "• Gentle Steps: " + tasksCount + " active\\n" +
                       "• Daily Habits: " + habitsCount + " monitored\\n" +
                       "• Storage Integrity: Verified SQLite WAL & In-Memory Cache\\n" +
                       "• Schema: V3 Normalized Event Model";
      alert(statsMsg);
    }"""

new_inspect = """    function inspectDatabase() {
      const thoughtsCount = (typeof thoughts !== 'undefined' ? thoughts.length : 3);
      const tasksCount = (typeof tasks !== 'undefined' ? tasks.length : 4);
      showToast('SQLite WAL Verified: ' + thoughtsCount + ' reflections, ' + tasksCount + ' tasks', 'info');
    }"""

c = c.replace(old_inspect.replace('\r\n', '\n'), new_inspect.replace('\r\n', '\n'))

# 8. Replace downloadDiagnostics alert
old_diag = """      alert("Diagnostics Bundle:\\n" + JSON.stringify(diagData, null, 2));"""
new_diag = """      if (navigator.clipboard) navigator.clipboard.writeText(JSON.stringify(diagData, null, 2));
      showToast('Diagnostics bundle copied to clipboard', 'success');"""
c = c.replace(old_diag, new_diag)

# 9. Replace clearCacheAction alert
c = c.replace('alert("Cache cleared successfully.");', 'showToast("Cache cleared successfully.", "success");')

# 10. Replace installPluginZip alert
c = c.replace("alert('Could not launch plugin installer: ' + e.message);", "showToast('Could not launch plugin installer: ' + e.message, 'error');")
c = c.replace("alert('Plugin ZIP installation is available when running inside native V07 Android shell.');", "showToast('Plugin ZIP installation is available inside Android shell.', 'info');")

# 11. Replace promptAddHabit
old_promptAddHabit = """    function promptAddHabit() {
      const habit = prompt('Enter a new gentle daily ritual:');
      if (habit && habit.trim()) {
        const clean = cleanText(habit);
        const list = document.getElementById('habits-modal-list');
        const div = document.createElement('div');
        div.className = 'habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs';
        div.innerHTML = `
          <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
            <div class="habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
            </div>
            <div class="flex flex-col">
              <span class="habit-title font-body-md text-on-surface font-medium">${clean}</span>
              <span class="font-label-sm text-[11px] text-on-surface-variant">Scheduled daily</span>
            </div>
          </div>
        `;
        list.appendChild(div);
      }
    }"""

new_promptAddHabit = """    function promptAddHabit() {
      openPromptModal('Enter a new gentle daily ritual:', '', (habit) => {
        if (habit && habit.trim()) {
          const clean = cleanText(habit);
          const list = document.getElementById('habits-modal-list');
          const div = document.createElement('div');
          div.className = 'habit-item flex items-center justify-between p-3.5 rounded-xl bg-surface-container-lowest border border-surface-container-high/40 shadow-xs';
          div.innerHTML = `
            <div class="flex items-center gap-3 cursor-pointer flex-1" onclick="toggleHabitItem(this)">
              <div class="habit-check w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>
              </div>
              <div class="flex flex-col">
                <span class="habit-title font-body-md text-on-surface font-medium">${clean}</span>
                <span class="font-label-sm text-[11px] text-on-surface-variant">Scheduled daily</span>
              </div>
            </div>
          `;
          list.appendChild(div);
          showToast('Added daily ritual: ' + clean, 'success');
        }
      });
    }"""

c = c.replace(old_promptAddHabit.replace('\r\n', '\n'), new_promptAddHabit.replace('\r\n', '\n'))

# 12. Replace promptUpdatePage
old_promptUpdatePage = """    function promptUpdatePage() {
      const p = prompt('Enter current page number:', currentReadingPage);
      if (p && !isNaN(parseInt(p))) {
        currentReadingPage = parseInt(p);
        adjustBookPages(0);
      }
    }"""

new_promptUpdatePage = """    function promptUpdatePage() {
      openPromptModal('Enter current page number:', currentReadingPage, (p) => {
        if (p && !isNaN(parseInt(p))) {
          currentReadingPage = parseInt(p);
          adjustBookPages(0);
          showToast('Updated reading page to ' + currentReadingPage, 'success');
        }
      });
    }"""

c = c.replace(old_promptUpdatePage.replace('\r\n', '\n'), new_promptUpdatePage.replace('\r\n', '\n'))

with open(INDEX_PATH, "w", encoding="utf-8", newline="\n") as f:
    f.write(c)

print("Updated app/src/main/assets/v3/index.html cleanly!")
