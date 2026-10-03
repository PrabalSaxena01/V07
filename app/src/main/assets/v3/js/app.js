/**
 * V07 V3 SANCTUARY APP COORDINATOR (SanctuaryApp)
 * 
 * Normative Requirements Addressed:
 * - Impeccable Law 4: Instant tactile theme switching (<16ms) between Warm Paper & Night Ink.
 * - Tab navigation & responsive routing (Desktop sidebar + mobile bottom dock).
 * - Date navigation (Prev, Next, Today).
 * - Modal workflows: Session cancellation, Post-session reflection, Habit editor, Task editor, Vault import/export.
 * - Global keyboard shortcuts (Ctrl+K, Esc, ?, Space).
 */

(function(window) {
  'use strict';

  class SanctuaryApp {
    constructor() {
      this.activeTab = 'cockpit';
      this.currentTheme = localStorage.getItem('v07_app_theme') || 'dark';
    }

    async init() {
      console.log('[SanctuaryApp] Initializing V3 Sanctuary Web Companion...');

      // 1. Apply initial theme
      this.applyTheme(this.currentTheme, false);

      // 2. Initialize Storage Engine
      await window.V07StorageEngine.init();

      // 3. Setup Listeners
      this.initKeyboardShortcuts();
      this.initModals();
      this.initNavigation();

      // 4. Subscribe to Storage and Chrono mutations
      window.V07StorageEngine.subscribe(() => {
        window.SanctuaryViews.renderAll();
      });

      window.FocusChronometer.subscribe(() => {
        window.SanctuaryViews.updateChronoGaugeDisplay();
      });

      // 5. Initial Render
      window.SanctuaryViews.renderAll();
      this.switchTab(this.activeTab);

      console.log('[SanctuaryApp] V3 Sanctuary Web Companion ready.');
    }

    // --- Navigation & Routing ---
    switchTab(tabId) {
      if (!tabId) return;
      this.activeTab = tabId;

      // Update Panel Visibilities
      document.querySelectorAll('.tab-panel').forEach(panel => {
        panel.classList.remove('active');
        panel.style.display = 'none';
      });

      const activePanel = document.getElementById(`panel-${tabId}`);
      if (activePanel) {
        activePanel.classList.add('active');
        activePanel.style.display = 'flex';
      }

      // Update Desktop Sidebar active styling
      document.querySelectorAll('.nav-item').forEach(btn => {
        const isCurrent = btn.getAttribute('data-tab') === tabId;
        btn.classList.toggle('active', isCurrent);
      });

      // Update Mobile Bottom Dock active styling
      document.querySelectorAll('.mobile-nav-btn').forEach(btn => {
        const isCurrent = btn.getAttribute('data-tab') === tabId;
        btn.classList.toggle('active', isCurrent);
      });

      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    initNavigation() {
      // Sidebar & Mobile Nav Clicks
      document.querySelectorAll('[data-tab]').forEach(el => {
        el.addEventListener('click', (e) => {
          e.preventDefault();
          const tab = el.getAttribute('data-tab');
          this.switchTab(tab);
        });
      });

      // Date Switcher Controls
      const prevBtn = document.getElementById('hudPrevDateBtn');
      const nextBtn = document.getElementById('hudNextDateBtn');
      const todayBtn = document.getElementById('hudTodayBtn');

      if (prevBtn) {
        prevBtn.addEventListener('click', () => this.shiftSelectedDate(-1));
      }
      if (nextBtn) {
        nextBtn.addEventListener('click', () => this.shiftSelectedDate(1));
      }
      if (todayBtn) {
        todayBtn.addEventListener('click', () => {
          window.SanctuaryViews.selectedDate = window.SanctuaryViews.getTodayDateString();
          window.SanctuaryViews.renderAll();
        });
      }

      // Chrono Controls
      const toggleChrono = document.getElementById('chronoToggleBtn');
      const resetChrono = document.getElementById('chronoResetBtn');
      if (toggleChrono) {
        toggleChrono.addEventListener('click', () => {
          const chrono = window.FocusChronometer;
          if (chrono.state === 'RUNNING') chrono.pause();
          else chrono.start();
        });
      }
      if (resetChrono) {
        resetChrono.addEventListener('click', () => {
          window.FocusChronometer.requestCancel();
        });
      }
    }

    shiftSelectedDate(daysDelta) {
      const current = new Date(window.SanctuaryViews.selectedDate + 'T00:00:00');
      current.setDate(current.getDate() + daysDelta);
      window.SanctuaryViews.selectedDate = current.toISOString().split('T')[0];
      window.SanctuaryViews.renderAll();
    }

    // --- Tactile Instant Theme Switching (Impeccable Law 4) ---
    toggleTheme() {
      const nextTheme = this.currentTheme === 'dark' ? 'light' : 'dark';
      this.applyTheme(nextTheme, true);
    }

    applyTheme(theme, save = true) {
      this.currentTheme = theme;
      const htmlEl = document.documentElement;
      const themeIcon = document.getElementById('themeToggleIcon');

      if (theme === 'light') {
        htmlEl.setAttribute('data-theme', 'light');
        htmlEl.classList.remove('dark');
        htmlEl.classList.add('light');
        if (themeIcon) themeIcon.textContent = 'dark_mode';
      } else {
        htmlEl.removeAttribute('data-theme');
        htmlEl.classList.remove('light');
        htmlEl.classList.add('dark');
        if (themeIcon) themeIcon.textContent = 'light_mode';
      }

      if (save) localStorage.setItem('v07_app_theme', theme);

      // Dispatch to native Android bridge if connected
      if (window.AndroidTheme && typeof window.AndroidTheme.setSystemTheme === 'function') {
        window.AndroidTheme.setSystemTheme(theme === 'light');
      }
    }

    // --- Global Keyboard Shortcuts ---
    initKeyboardShortcuts() {
      window.addEventListener('keydown', (ev) => {
        // Ignore if user is actively typing in an input or textarea
        const activeTag = document.activeElement ? document.activeElement.tagName.toLowerCase() : '';
        if (activeTag === 'input' || activeTag === 'textarea' || activeTag === 'select') {
          if (ev.key === 'Escape') {
            document.activeElement.blur();
          }
          return;
        }

        if (ev.key === 'Escape') {
          this.closeAllModals();
          window.V07PluginHost.closeRunner();
        } else if (ev.key === '?') {
          ev.preventDefault();
          this.openModal('shortcutsModal');
        } else if (ev.code === 'Space') {
          ev.preventDefault();
          const chrono = window.FocusChronometer;
          if (chrono.state === 'RUNNING') chrono.pause();
          else chrono.start();
        } else if (ev.altKey && ev.key === 't') {
          ev.preventDefault();
          this.toggleTheme();
        }
      });
    }

    // --- Modals Management ---
    initModals() {
      // Close button wiring for all modal overlays
      document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
          if (e.target === overlay) {
            this.closeAllModals();
          }
        });
      });

      document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => this.closeAllModals());
      });

      // Quick Add Task form in Obligations view
      const quickTaskInput = document.getElementById('quickTaskInput');
      const quickTaskBtn = document.getElementById('quickTaskSubmitBtn');
      if (quickTaskBtn && quickTaskInput) {
        quickTaskBtn.addEventListener('click', async () => {
          const val = quickTaskInput.value.trim();
          if (!val) return;
          await window.V07StorageEngine.saveTask({
            title: val,
            priority: 'medium',
            status: 'pending',
            date: window.SanctuaryViews.selectedDate
          });
          quickTaskInput.value = '';
          window.SanctuaryViews.renderTasks();
          window.SanctuaryViews.renderCockpit();
          window.SanctuaryViews.showToast('Obligation committed to sanctuary (+40 XP)');
        });

        quickTaskInput.addEventListener('keydown', (e) => {
          if (e.key === 'Enter') quickTaskBtn.click();
        });
      }

      // Quick Reflection in Journal view
      const quickThoughtInput = document.getElementById('quickThoughtInput');
      const quickThoughtBtn = document.getElementById('quickThoughtSubmitBtn');
      if (quickThoughtBtn && quickThoughtInput) {
        quickThoughtBtn.addEventListener('click', async () => {
          const val = quickThoughtInput.value.trim();
          if (!val) return;
          await window.V07StorageEngine.saveThought({
            title: 'Daily Reflection',
            content: val,
            date: window.SanctuaryViews.selectedDate,
            ts: Date.now()
          });
          quickThoughtInput.value = '';
          window.SanctuaryViews.renderJournal();
          window.SanctuaryViews.renderCockpit();
          window.SanctuaryViews.showToast('Reflection illuminated');
        });
      }

      // Wire Task Editor Save
      const saveTaskEditBtn = document.getElementById('saveTaskEditBtn');
      if (saveTaskEditBtn) {
        saveTaskEditBtn.addEventListener('click', async () => {
          const id = document.getElementById('editTaskId')?.value;
          const title = document.getElementById('editTaskTitle')?.value.trim();
          const priority = document.getElementById('editTaskPriority')?.value || 'medium';
          const deadline = document.getElementById('editTaskDeadline')?.value || '';

          if (!id || !title) return;
          const task = window.V07StorageEngine.tasks.find(t => t.id === id);
          if (task) {
            task.title = title;
            task.priority = priority;
            task.deadline = deadline;
            await window.V07StorageEngine.saveTask(task);
            this.closeAllModals();
            window.SanctuaryViews.renderTasks();
            window.SanctuaryViews.showToast('Obligation updated');
          }
        });
      }

      // Wire Post-Session Modal Submission
      const saveReflectionBtn = document.getElementById('savePostSessionBtn');
      if (saveReflectionBtn) {
        saveReflectionBtn.addEventListener('click', async () => {
          const notes = document.getElementById('postSessionNotes')?.value || '';
          const rating = parseInt(document.querySelector('input[name="focusRating"]:checked')?.value || '5');
          const chrono = window.FocusChronometer;

          await window.V07StorageEngine.saveSession({
            id: 'sess_' + Date.now(),
            activity: chrono.activity,
            title: chrono.title,
            startTime: chrono.startTime || (Date.now() - chrono.elapsedSeconds * 1000),
            endTime: Date.now(),
            durationSec: chrono.elapsedSeconds,
            notes: notes,
            rating: rating,
            category: 'session',
            date: window.SanctuaryViews.selectedDate
          });

          chrono.reset();
          this.closeAllModals();
          window.SanctuaryViews.renderCockpit();
          window.SanctuaryViews.showToast(`Focus session saved with ${rating}★ depth (+50 XP)`);
        });
      }

      // Wire Cancellation Modal
      window.showCancelChronoModal = () => {
        this.openModal('cancelChronoModal');
      };

      const confirmCancelBtn = document.getElementById('confirmCancelChronoBtn');
      if (confirmCancelBtn) {
        confirmCancelBtn.addEventListener('click', () => {
          window.FocusChronometer.reset();
          this.closeAllModals();
          window.SanctuaryViews.showToast('Focus interval released');
        });
      }

      // Wire Post-Session Modal Hook
      window.showPostSessionModal = (meta) => {
        const modal = document.getElementById('postSessionModal');
        const durEl = document.getElementById('postSessionDurationDisplay');
        if (durEl) durEl.textContent = `${Math.round(meta.durationSec / 60)} minutes`;
        if (modal) modal.classList.add('active');
      };

      // Wire Plugin Consent Modal Hook
      window.showPluginConsentModal = (meta, onAllow) => {
        const modal = document.getElementById('pluginConsentModal');
        const titleEl = document.getElementById('consentPluginTitle');
        const allowBtn = document.getElementById('consentAllowBtn');

        if (titleEl) titleEl.textContent = meta.name || meta.id;
        if (allowBtn) {
          allowBtn.onclick = () => {
            this.closeAllModals();
            if (typeof onAllow === 'function') onAllow();
          };
        }
        if (modal) modal.classList.add('active');
      };

      // Wire Vault JSON Export
      const exportBtn = document.getElementById('vaultExportBtn');
      if (exportBtn) {
        exportBtn.addEventListener('click', () => {
          const data = window.V07StorageEngine.generateVaultExport();
          const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `v07_sanctuary_vault_${new Date().toISOString().split('T')[0]}.json`;
          a.click();
          URL.revokeObjectURL(url);
          window.SanctuaryViews.showToast('Sovereign Vault JSON backup generated');
        });
      }

      // Wire Vault JSON Import Validation & Diff Preview
      const importInput = document.getElementById('vaultImportFileInput');
      if (importInput) {
        importInput.addEventListener('change', (e) => {
          const file = e.target.files[0];
          if (!file) return;

          const reader = new FileReader();
          reader.onload = async (ev) => {
            const result = window.V07StorageEngine.validateVaultJson(ev.target.result);
            if (!result.valid) {
              alert('Schema Validation Failed: ' + result.error);
              return;
            }

            // Prompt preview diff
            const confirmMsg = `Vault Import Validation Passed:\n` +
              `- Focus Sessions: ${result.counts.sessions}\n` +
              `- Obligations: ${result.counts.tasks}\n` +
              `- Reflections: ${result.counts.thoughts}\n` +
              `- Habit Logs: ${result.counts.habit_logs}\n\n` +
              `Merge these records into your local sanctuary?`;

            if (confirm(confirmMsg)) {
              await window.V07StorageEngine.importVaultPayload(result.data);
              window.SanctuaryViews.renderAll();
              window.SanctuaryViews.showToast('Sovereign Vault imported successfully');
            }
          };
          reader.readAsText(file);
          importInput.value = '';
        });
      }

      // --- Profile Modal Event Listeners ---
      const switchBtn = document.getElementById('switchProfileBtn');
      if (switchBtn) {
        switchBtn.addEventListener('click', async () => {
          const select = document.getElementById('profileSelectDropdown');
          if (!select) return;
          await window.V07StorageEngine.switchProfile(select.value);
          this.closeAllModals();
          window.SanctuaryViews.renderAll();
          window.SanctuaryViews.showToast(`Switched to profile '${select.value}'`);
        });
      }

      const createBtn = document.getElementById('createProfileBtn');
      if (createBtn) {
        createBtn.addEventListener('click', async () => {
          const input = document.getElementById('newProfileInput');
          const val = input ? input.value.trim() : '';
          if (!val) return;
          await window.V07StorageEngine.switchProfile(val);
          if (input) input.value = '';
          this.closeAllModals();
          window.SanctuaryViews.renderAll();
          window.SanctuaryViews.showToast(`Created & switched to profile '${val}'`);
        });
      }

      // Cloud Auth Wire
      const signInBtn = document.getElementById('cloudSignInBtn');
      if (signInBtn) {
        signInBtn.addEventListener('click', async () => {
          const email = document.getElementById('cloudAuthEmail')?.value.trim();
          const pass = document.getElementById('cloudAuthPassword')?.value;
          if (!email || !pass) {
            alert('Please enter email and password');
            return;
          }
          try {
            await firebase.auth().signInWithEmailAndPassword(email, pass);
            this.closeAllModals();
            window.SanctuaryViews.showToast(`Authenticated as ${email}`);
          } catch (err) {
            alert('Sign In Failed: ' + err.message);
          }
        });
      }

      const signUpBtn = document.getElementById('cloudSignUpBtn');
      if (signUpBtn) {
        signUpBtn.addEventListener('click', async () => {
          const email = document.getElementById('cloudAuthEmail')?.value.trim();
          const pass = document.getElementById('cloudAuthPassword')?.value;
          if (!email || !pass) {
            alert('Please enter email and password');
            return;
          }
          try {
            await firebase.auth().createUserWithEmailAndPassword(email, pass);
            this.closeAllModals();
            window.SanctuaryViews.showToast(`Account created for ${email}`);
          } catch (err) {
            alert('Account Creation Failed: ' + err.message);
          }
        });
      }

      const signOutBtn = document.getElementById('cloudSignOutBtn');
      if (signOutBtn) {
        signOutBtn.addEventListener('click', async () => {
          try {
            await firebase.auth().signOut();
            this.closeAllModals();
            window.SanctuaryViews.showToast('Signed out to Offline Guest mode');
          } catch (err) {
            alert('Sign Out Failed: ' + err.message);
          }
        });
      }

      const syncNowBtn = document.getElementById('cloudSyncNowBtn');
      if (syncNowBtn) {
        syncNowBtn.addEventListener('click', async () => {
          syncNowBtn.disabled = true;
          syncNowBtn.innerHTML = '<span class="material-symbols-outlined text-sm animate-spin">autorenew</span> Syncing...';
          await window.V07StorageEngine.syncPendingToCloud();
          setTimeout(() => {
            syncNowBtn.disabled = false;
            syncNowBtn.innerHTML = '<span class="material-symbols-outlined text-sm">sync</span> Sync Cloud Partition Now';
            window.SanctuaryViews.showToast('Cloud partition synchronized');
          }, 600);
        });
      }
    }

    openProfileModal() {
      const se = window.V07StorageEngine;
      const activeProf = se.getActiveProfileId ? se.getActiveProfileId() : 'guest';

      const labelEl = document.getElementById('activeProfileLabel');
      if (labelEl) labelEl.textContent = activeProf;

      const badgeEl = document.getElementById('profileSyncStatusBadge');
      if (badgeEl) {
        if (se.mode === 'cloud_firestore') {
          badgeEl.className = 'text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[var(--success-dim)] text-[var(--success)]';
          badgeEl.textContent = 'Cloud Synced';
        } else {
          badgeEl.className = 'text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[var(--warning-dim)] text-[var(--warning)]';
          badgeEl.textContent = 'Offline Partition';
        }
      }

      const dropdown = document.getElementById('profileSelectDropdown');
      if (dropdown && typeof se.listProfiles === 'function') {
        const profiles = se.listProfiles();
        dropdown.innerHTML = profiles.map(p => `<option value="${p}" ${p === activeProf ? 'selected' : ''}>${p}</option>`).join('');
      }

      const loggedInView = document.getElementById('authLoggedInView');
      const loggedOutView = document.getElementById('authLoggedOutView');
      if (se.currentUser) {
        if (loggedInView) loggedInView.classList.remove('hidden');
        if (loggedOutView) loggedOutView.classList.add('hidden');
        const emailEl = document.getElementById('loggedInUserEmail');
        const uidEl = document.getElementById('loggedInUserUid');
        if (emailEl) emailEl.textContent = se.currentUser.email || 'Authenticated User';
        if (uidEl) uidEl.textContent = 'uid: ' + se.currentUser.uid;
      } else {
        if (loggedInView) loggedInView.classList.add('hidden');
        if (loggedOutView) loggedOutView.classList.remove('hidden');
      }

      this.openModal('profileModal');
    }

    openModal(modalId) {
      const el = document.getElementById(modalId);
      if (el) el.classList.add('active');
    }

    closeAllModals() {
      document.querySelectorAll('.modal-overlay').forEach(el => el.classList.remove('active'));
    }
  }

  window.SanctuaryApp = new SanctuaryApp();

  // Document Ready Bootstrap
  document.addEventListener('DOMContentLoaded', () => {
    window.SanctuaryApp.init();
  });
})(window);
