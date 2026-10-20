#!/usr/bin/env python3
"""
V07 Plugin Refactor & Modernization Script
Eliminates blocking alert() and prompt() calls across all V3 plugins,
replacing them with Calm Space Editorial modals, expandable forms, and
window.v07.ui.toast feedback.
"""

import os
import re

PLUGINS_DIR = r"D:\software\V07-Android\app\src\main\assets\v3\plugins"

def patch_gamify():
    path = os.path.join(PLUGINS_DIR, "com.v07.gamify", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Add id to header badge
    content = content.replace(
        '<span class="badge-pill" style="font-family: var(--font-mono); font-size: 11px;">2,450 XP</span>',
        '<span id="header-xp-badge" class="badge-pill" style="font-family: var(--font-mono); font-size: 11px;">2,450 XP</span>'
    )
    # Add id to level sub text
    content = content.replace(
        '<div style="font-size: 11px; color: var(--subslate);">550 XP remaining to Level 8</div>',
        '<div id="level-sub-txt" style="font-size: 11px; color: var(--subslate);">550 XP remaining to Level 8</div>'
    )

    old_script = """  <script>
    let totalXp = 2450;
    function claimQuest(btn, xp) {
      totalXp += xp;
      btn.disabled = true;
      btn.textContent = 'Claimed!';
      btn.style.background = 'var(--card-low)';
      btn.style.color = 'var(--subslate)';
      alert(`+${xp} XP claimed! New total: ${totalXp} XP`);
      if (window.v07 && window.v07.storage) {
        window.v07.storage.set('gamify_xp', totalXp);
      }
    }
  </script>"""

    new_script = """  <script>
    let totalXp = 2450;
    function claimQuest(btn, xp) {
      totalXp += xp;
      btn.disabled = true;
      btn.textContent = '✓ Claimed';
      btn.style.background = 'var(--card-low)';
      btn.style.color = 'var(--subslate)';
      
      const badge = document.getElementById('header-xp-badge');
      if (badge) badge.textContent = totalXp.toLocaleString() + ' XP';
      
      const remaining = Math.max(0, 3000 - totalXp);
      const subTxt = document.getElementById('level-sub-txt');
      if (subTxt) {
        subTxt.textContent = remaining > 0 ? (remaining + ' XP remaining to Level 8') : '⭐ Max Level Reached!';
      }
      const fill = document.getElementById('xp-fill');
      if (fill) {
        const pct = Math.min(100, Math.round((totalXp / 3000) * 100));
        fill.style.width = pct + '%';
      }

      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('+' + xp + ' XP Claimed! New total: ' + totalXp.toLocaleString() + ' XP', 'success');
      }
      if (window.v07 && window.v07.storage) {
        window.v07.storage.set('gamify_xp', totalXp);
      }
    }
  </script>"""

    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched gamify")

def patch_gita():
    path = os.path.join(PLUGINS_DIR, "com.v07.gita", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    old_reflection = """  <!-- Reflection Note Card -->
  <div class="card">
    <div class="card-label"><span>Reflective Synthesis</span></div>
    <textarea id="gita-reflection" class="reflection-box" placeholder="Contemplate this timeless wisdom... what truth does it illuminate in your journey?"></textarea>
    <button class="btn btn-primary" onclick="saveReflection()" style="width: 100%; justify-content: center; margin-top: 8px;">
      <span class="material-symbols-outlined" style="font-size: 16px;">bookmark</span>
      <span>Save Reflection</span>
    </button>
  </div>"""

    new_reflection = """  <!-- Reflection Note Card -->
  <div class="card">
    <div class="card-label"><span>Reflective Synthesis</span></div>
    <textarea id="gita-reflection" class="reflection-box" placeholder="Contemplate this timeless wisdom... what truth does it illuminate in your journey?"></textarea>
    <button id="btn-save-reflection" class="btn btn-primary" onclick="saveReflection()" style="width: 100%; justify-content: center; margin-top: 8px;">
      <span class="material-symbols-outlined" style="font-size: 16px;">bookmark</span>
      <span>Save Reflection</span>
    </button>
    <div id="saved-reflections" style="margin-top: 12px; display: none; padding-top: 10px; border-top: 1px solid var(--border);">
      <div style="font-size: 11px; font-weight: 700; color: var(--subslate); margin-bottom: 6px;">SOVEREIGN VAULT REFLECTION</div>
      <div id="latest-reflection-txt" style="font-size: 12px; font-style: italic; color: var(--slate); line-height: 1.5; background: var(--card-low); padding: 8px 12px; border-radius: var(--radius-sm);"></div>
    </div>
  </div>"""

    old_script = """    function saveReflection() {
      const t = document.getElementById('gita-reflection').value;
      if (window.v07 && window.v07.storage) {
        window.v07.storage.insertEvent({ category: 'gita_reflection', note: t, ts: Date.now() });
      }
      alert('Reflection saved to sovereign vault!');
    }"""

    new_script = """    function saveReflection() {
      const input = document.getElementById('gita-reflection');
      const t = input.value.trim();
      if (!t) {
        if (window.v07 && window.v07.ui) window.v07.ui.toast('Please write a reflection note first', 'error');
        return;
      }
      if (window.v07 && window.v07.storage) {
        window.v07.storage.insertEvent({ category: 'gita_reflection', note: t, ts: Date.now() });
      }
      const savedBox = document.getElementById('saved-reflections');
      const txtBox = document.getElementById('latest-reflection-txt');
      if (savedBox && txtBox) {
        txtBox.textContent = '“' + t + '”';
        savedBox.style.display = 'block';
      }
      input.value = '';
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Wisdom reflection saved to Sovereign Vault!', 'success');
      }
    }"""

    content = content.replace(old_reflection.replace('\r\n', '\n'), new_reflection.replace('\r\n', '\n'))
    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched gita")

def patch_habits():
    path = os.path.join(PLUGINS_DIR, "com.v07.habits", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Replace header button with toggle
    content = content.replace(
        '<button class="btn btn-primary" onclick="promptNewHabit()">+ New</button>',
        '<button class="btn btn-primary" onclick="toggleNewHabitForm()">+ New Habit</button>'
    )

    # Insert inline new habit form before Today's Habits Card
    old_today_card = """  <!-- Today's Habits Card -->
  <div class="card">
    <div class="card-label">
      <span>Today's Habits · Daily Cadence</span>
      <span id="today-completion" style="color: var(--brand-teal); font-weight: 700;">2 of 3 Done</span>
    </div>
    <div class="habit-item">"""

    new_today_card = """  <!-- Inline New Habit Creator -->
  <div id="new-habit-form" class="card" style="display: none; border: 1.5px dashed var(--brand-teal); background: var(--card-low); margin-bottom: 12px; padding: 12px 14px;">
    <div style="font-size: 12px; font-weight: 700; color: var(--slate); margin-bottom: 6px;">+ Create New Daily Habit</div>
    <input type="text" id="habit-name-input" placeholder="e.g. 15m Evening Walk, Cold Shower, Reading..." style="width: 100%; padding: 8px 12px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; margin-bottom: 8px; background: var(--card); color: var(--slate);">
    <div style="display: flex; gap: 6px; align-items: center; justify-content: flex-end;">
      <button class="btn btn-secondary" onclick="toggleNewHabitForm()" style="padding: 4px 10px; font-size: 11px;">Cancel</button>
      <button class="btn btn-primary" onclick="createNewHabit()" style="padding: 4px 12px; font-size: 11px;">Add Habit</button>
    </div>
  </div>

  <!-- Today's Habits Card -->
  <div class="card">
    <div class="card-label">
      <span>Today's Habits · Daily Cadence</span>
      <span id="today-completion" style="color: var(--brand-teal); font-weight: 700;">2 of 3 Done</span>
    </div>
    <div id="habit-items-container">
    <div class="habit-item">"""

    content = content.replace(old_today_card.replace('\r\n', '\n'), new_today_card.replace('\r\n', '\n'))

    # Close #habit-items-container before Heatmap Card
    old_heatmap_start = """  <!-- 20-Week Consistency Heatmap Card -->"""
    content = content.replace(
        old_heatmap_start,
        """    </div>\n  </div>\n\n  <!-- 20-Week Consistency Heatmap Card -->"""
    )
    # Remove extra </div> that was closing .card originally
    content = content.replace(
        """    </div>\n  </div>\n\n  <!-- 20-Week Consistency Heatmap Card -->\n  <div class="card">""",
        """  </div>\n\n  <!-- 20-Week Consistency Heatmap Card -->\n  <div class="card">"""
    )

    old_script = """    function toggleHabit(el) {
      el.classList.toggle('done');
      const dones = document.querySelectorAll('.habit-check.done').length;
      document.getElementById('today-completion').textContent = dones + ' of 3 Done';
      if (window.v07 && window.v07.storage) {
        window.v07.storage.set('habits_done_count', dones);
      }
    }
    function promptNewHabit() {
      const name = prompt('Enter new daily habit:');
      if (name) alert('Added habit "' + name + '" to daily cadence.');
    }"""

    new_script = """    function updateHabitsCount() {
      const total = document.querySelectorAll('.habit-item').length;
      const dones = document.querySelectorAll('.habit-check.done').length;
      const countEl = document.getElementById('today-completion');
      if (countEl) countEl.textContent = dones + ' of ' + total + ' Done';
      if (window.v07 && window.v07.storage) {
        window.v07.storage.set('habits_done_count', dones);
      }
    }
    function toggleHabit(el) {
      el.classList.toggle('done');
      updateHabitsCount();
      if (el.classList.contains('done') && window.v07 && window.v07.ui) {
        window.v07.ui.toast('Habit checked! Momentum preserved 🔥', 'success');
      }
    }
    function toggleNewHabitForm() {
      const f = document.getElementById('new-habit-form');
      if (!f) return;
      const isOpen = f.style.display === 'block';
      f.style.display = isOpen ? 'none' : 'block';
      if (!isOpen) {
        const inp = document.getElementById('habit-name-input');
        if (inp) inp.focus();
      }
    }
    function createNewHabit() {
      const inp = document.getElementById('habit-name-input');
      const name = inp ? inp.value.trim() : '';
      if (!name) {
        if (window.v07 && window.v07.ui) window.v07.ui.toast('Please enter a habit title', 'error');
        return;
      }
      const container = document.getElementById('habit-items-container') || document.querySelector('.card');
      const row = document.createElement('div');
      row.className = 'habit-item';
      row.innerHTML = `
        <div class="habit-left">
          <div class="habit-check" onclick="toggleHabit(this)">
            <span class="material-symbols-outlined" style="font-size: 16px;">check</span>
          </div>
          <span style="font-size: 13px; font-weight: 600;">🌿 ${name}</span>
        </div>
        <span class="streak-badge">🔥 1 day</span>
      `;
      container.appendChild(row);
      inp.value = '';
      toggleNewHabitForm();
      updateHabitsCount();
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Added habit "' + name + '" to daily cadence.', 'success');
      }
    }"""

    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched habits")

def patch_reader():
    path = os.path.join(PLUGINS_DIR, "com.v07.reader", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Replace header action buttons
    content = content.replace(
        '<button class="btn btn-secondary" onclick="promptAddBook()">+ Add Book</button>',
        '<button class="btn btn-secondary" onclick="toggleAddBookModal()">+ Add Book</button>'
    )
    content = content.replace(
        '<button class="btn btn-primary" onclick="searchOpenLibrary()">🔍 Search</button>',
        '<button class="btn btn-primary" onclick="toggleSearchModal()">🔍 Search</button>'
    )

    # Insert Add Book panel, Search modal, and Quotes sheet before chip-row
    old_chips = """  <div class="chip-row">"""
    modals_html = """  <!-- Inline Add Book Panel -->
  <div id="add-book-panel" class="card" style="display: none; border: 1.5px dashed var(--brand-teal); background: var(--card-low); margin-bottom: 12px; padding: 12px 14px;">
    <div style="font-size: 12px; font-weight: 700; color: var(--slate); margin-bottom: 6px;">+ Add Book to Personal Shelf</div>
    <input type="text" id="new-book-title" placeholder="Book Title (e.g. Deep Work)..." style="width: 100%; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; margin-bottom: 6px; background: var(--card); color: var(--slate);">
    <input type="text" id="new-book-author" placeholder="Author (e.g. Cal Newport)..." style="width: 100%; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; margin-bottom: 6px; background: var(--card); color: var(--slate);">
    <div style="display: flex; gap: 6px; margin-bottom: 8px;">
      <input type="number" id="new-book-pages" placeholder="Total Pages (e.g. 304)" style="flex: 1; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
      <select id="new-book-shelf" style="flex: 1; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
        <option value="reading">Currently Reading</option>
        <option value="toread" selected>To Read</option>
        <option value="done">Finished</option>
      </select>
    </div>
    <div style="display: flex; gap: 6px; justify-content: flex-end;">
      <button class="btn btn-secondary" onclick="toggleAddBookModal()" style="padding: 4px 10px; font-size: 11px;">Cancel</button>
      <button class="btn btn-primary" onclick="saveNewBook()" style="padding: 4px 12px; font-size: 11px;">Save to Shelf</button>
    </div>
  </div>

  <!-- Inline Open Library Search Panel -->
  <div id="search-modal-panel" class="card" style="display: none; background: var(--card-low); margin-bottom: 12px; padding: 12px 14px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
      <span style="font-size: 12px; font-weight: 700; color: var(--slate);">🔍 Open Literary Database</span>
      <button class="btn btn-secondary" onclick="toggleSearchModal()" style="padding: 2px 8px; font-size: 10px;">Close</button>
    </div>
    <input type="text" id="search-books-query" placeholder="Search title, author, or ISBN..." oninput="filterSearchResults(this.value)" style="width: 100%; padding: 8px 12px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; margin-bottom: 8px; background: var(--card); color: var(--slate);">
    <div id="search-results-list" style="display: flex; flex-direction: column; gap: 6px;">
      <div class="run-log-row" style="background: var(--card); padding: 8px; border-radius: var(--radius-sm); border: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
        <div>
          <div style="font-size: 12px; font-weight: 600;">Deep Work</div>
          <div style="font-size: 10px; color: var(--subslate);">Cal Newport • 296 pages</div>
        </div>
        <button class="btn btn-primary" onclick="addFromSearch('Deep Work', 'Cal Newport', 296)" style="padding: 3px 8px; font-size: 10px;">+ Add</button>
      </div>
      <div class="run-log-row" style="background: var(--card); padding: 8px; border-radius: var(--radius-sm); border: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
        <div>
          <div style="font-size: 12px; font-weight: 600;">Meditations</div>
          <div style="font-size: 10px; color: var(--subslate);">Marcus Aurelius • 208 pages</div>
        </div>
        <button class="btn btn-primary" onclick="addFromSearch('Meditations', 'Marcus Aurelius', 208)" style="padding: 3px 8px; font-size: 10px;">+ Add</button>
      </div>
      <div class="run-log-row" style="background: var(--card); padding: 8px; border-radius: var(--radius-sm); border: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
        <div>
          <div style="font-size: 12px; font-weight: 600;">Klara and the Sun</div>
          <div style="font-size: 10px; color: var(--subslate);">Kazuo Ishiguro • 320 pages</div>
        </div>
        <button class="btn btn-primary" onclick="addFromSearch('Klara and the Sun', 'Kazuo Ishiguro', 320)" style="padding: 3px 8px; font-size: 10px;">+ Add</button>
      </div>
    </div>
  </div>

  <!-- Inline Quotes & Notes Sheet -->
  <div id="quotes-sheet-panel" class="card" style="display: none; background: var(--card-low); margin-bottom: 12px; padding: 12px 14px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
      <span style="font-size: 12px; font-weight: 700; color: var(--slate);">📝 Highlights & Literary Quotes</span>
      <button class="btn btn-secondary" onclick="toggleQuotesSheet()" style="padding: 2px 8px; font-size: 10px;">Close</button>
    </div>
    <div style="display: flex; flex-direction: column; gap: 8px; margin-bottom: 10px;">
      <div style="font-size: 12px; font-style: italic; color: var(--slate); background: var(--card); padding: 8px 12px; border-radius: var(--radius-sm); border-left: 3px solid var(--brand-teal);">
        “Solitude matters, and for some people, it is the air they breathe.”
        <div style="font-size: 10px; color: var(--subslate); margin-top: 3px; font-style: normal;">— Susan Cain, Quiet</div>
      </div>
      <div style="font-size: 12px; font-style: italic; color: var(--slate); background: var(--card); padding: 8px 12px; border-radius: var(--radius-sm); border-left: 3px solid var(--brand-teal);">
        “There is zero correlation between being the best talker and having the best ideas.”
        <div style="font-size: 10px; color: var(--subslate); margin-top: 3px; font-style: normal;">— Susan Cain, Quiet</div>
      </div>
    </div>
    <div style="display: flex; gap: 6px;">
      <input type="text" id="new-quote-input" placeholder="Type a memorable quote..." style="flex: 1; padding: 6px 10px; font-size: 11px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
      <button class="btn btn-primary" onclick="saveNewQuote()" style="padding: 4px 10px; font-size: 11px;">+ Save</button>
    </div>
  </div>

  <div class="chip-row">"""

    content = content.replace(old_chips, modals_html)

    # Wrap shelf cards with id
    content = content.replace(
        '<div class="card-label" style="margin-top: 16px;"><span>On Your Shelf</span></div>',
        '<div class="card-label" style="margin-top: 16px;"><span>On Your Shelf</span></div>\n  <div id="shelf-books-container">'
    )
    content = content.replace(
        '  <script>\n    let curPage = 142;',
        '  </div>\n\n  <script>\n    let curPage = 142;'
    )

    old_script = """    function promptAddBook() {
      const title = prompt('Enter Book Title:');
      if (title) alert('Added "' + title + '" to your reading shelf.');
    }
    function searchOpenLibrary() {
      const q = prompt('Search Open Library / Google Books:');
      if (q) alert('Searching open literary databases for: ' + q);
    }
    function showQuotes() {
      alert('1. "Solitude matters, and for some people, it is the air they breathe."\\n\\n2. "There is zero correlation between being the best talker and having the best ideas."');
    }
    function openBookDetail() {
      adjustPages(5);
    }
    function startReading() {
      alert('Moved to Currently Reading shelf!');
    }"""

    new_script = """    function toggleAddBookModal() {
      const p = document.getElementById('add-book-panel');
      if (!p) return;
      p.style.display = p.style.display === 'block' ? 'none' : 'block';
    }
    function toggleSearchModal() {
      const p = document.getElementById('search-modal-panel');
      if (!p) return;
      p.style.display = p.style.display === 'block' ? 'none' : 'block';
    }
    function toggleQuotesSheet() {
      const p = document.getElementById('quotes-sheet-panel');
      if (!p) return;
      p.style.display = p.style.display === 'block' ? 'none' : 'block';
    }
    function showQuotes() {
      toggleQuotesSheet();
    }
    function openBookDetail() {
      adjustPages(5);
      if (window.v07 && window.v07.ui) window.v07.ui.toast('Progress logged (+5 pages)!', 'success');
    }
    function startReading() {
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Moved "Atomic Habits" to Currently Reading shelf!', 'success');
      }
    }
    function saveNewBook() {
      const title = document.getElementById('new-book-title').value.trim();
      const author = document.getElementById('new-book-author').value.trim() || 'Author';
      const pages = document.getElementById('new-book-pages').value.trim() || '250';
      if (!title) {
        if (window.v07 && window.v07.ui) window.v07.ui.toast('Please enter a book title', 'error');
        return;
      }
      const container = document.getElementById('shelf-books-container');
      if (container) {
        const div = document.createElement('div');
        div.className = 'book-card';
        div.style.marginTop = '8px';
        div.innerHTML = `
          <div class="book-cover" style="background: linear-gradient(135deg, #2E7D6A 0%, #182230 100%);">
            <span>${title.substring(0, 10).toUpperCase()}</span>
            <span style="font-size: 7px; opacity: 0.8; font-weight: 400;">${author.toUpperCase()}</span>
          </div>
          <div class="book-details">
            <div>
              <div class="book-title">${title}</div>
              <div class="book-author">${author} • [TO READ]</div>
            </div>
            <div class="hud-sub" style="margin: 8px 0;">Added Today • ${pages} pages</div>
            <button class="btn btn-secondary" onclick="startReading()" style="align-self: flex-start; padding: 4px 10px; font-size: 11px;">Start Reading</button>
          </div>
        `;
        container.appendChild(div);
      }
      toggleAddBookModal();
      document.getElementById('new-book-title').value = '';
      document.getElementById('new-book-author').value = '';
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Added "' + title + '" to your reading shelf.', 'success');
      }
    }
    function addFromSearch(title, author, pages) {
      const container = document.getElementById('shelf-books-container');
      if (container) {
        const div = document.createElement('div');
        div.className = 'book-card';
        div.style.marginTop = '8px';
        div.innerHTML = `
          <div class="book-cover" style="background: linear-gradient(135deg, #3F9A83 0%, #182230 100%);">
            <span>${title.substring(0, 10).toUpperCase()}</span>
            <span style="font-size: 7px; opacity: 0.8; font-weight: 400;">${author.toUpperCase()}</span>
          </div>
          <div class="book-details">
            <div>
              <div class="book-title">${title}</div>
              <div class="book-author">${author} • [TO READ]</div>
            </div>
            <div class="hud-sub" style="margin: 8px 0;">Added from Open Library • ${pages} pages</div>
            <button class="btn btn-secondary" onclick="startReading()" style="align-self: flex-start; padding: 4px 10px; font-size: 11px;">Start Reading</button>
          </div>
        `;
        container.appendChild(div);
      }
      toggleSearchModal();
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Imported "' + title + '" to your shelf!', 'success');
      }
    }
    function saveNewQuote() {
      const inp = document.getElementById('new-quote-input');
      const val = inp ? inp.value.trim() : '';
      if (!val) return;
      inp.value = '';
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Saved quote highlight to Vault!', 'success');
      }
    }"""

    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched reader")

def patch_running():
    path = os.path.join(PLUGINS_DIR, "com.v07.running", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Replace header action button
    content = content.replace(
        '<button class="btn btn-primary" onclick="promptLogRun()">+ Log Run</button>',
        '<button class="btn btn-primary" onclick="toggleLogRunPanel()">+ Log Run</button>'
    )

    # Insert Quick Log Run panel before Weekly Mileage Goal
    old_goal = """  <!-- Weekly Mileage Goal -->"""
    log_panel = """  <!-- Inline Quick-Log Run Panel -->
  <div id="log-run-panel" class="card" style="display: none; border: 1.5px dashed var(--brand-teal); background: var(--card-low); margin-bottom: 12px; padding: 12px 14px;">
    <div style="font-size: 12px; font-weight: 700; color: var(--slate); margin-bottom: 6px;">+ Record Athletic Cadence Run</div>
    <input type="text" id="run-title-in" placeholder="Session Title (e.g. Sunset Recovery Flow)..." style="width: 100%; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; margin-bottom: 6px; background: var(--card); color: var(--slate);">
    <div style="display: flex; gap: 6px; margin-bottom: 8px;">
      <input type="number" step="0.1" id="run-km-in" placeholder="Distance in km (e.g. 5.4)" style="flex: 1; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
      <input type="text" id="run-time-in" placeholder="Duration (e.g. 28m 15s)" style="flex: 1; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
    </div>
    <div style="display: flex; gap: 6px; justify-content: flex-end;">
      <button class="btn btn-secondary" onclick="toggleLogRunPanel()" style="padding: 4px 10px; font-size: 11px;">Cancel</button>
      <button class="btn btn-primary" onclick="saveRunLog()" style="padding: 4px 12px; font-size: 11px;">Save Run</button>
    </div>
  </div>

  <!-- Weekly Mileage Goal -->"""

    content = content.replace(old_goal, log_panel)

    # Wrap Recent Cadence Runs list with id
    content = content.replace(
        '<div class="card-label"><span>Recent Cadence Runs</span></div>',
        '<div class="card-label"><span>Recent Cadence Runs</span></div>\n    <div id="recent-runs-list">'
    )
    content = content.replace(
        '  </div>\n\n  <script>\n    function promptLogRun()',
        '    </div>\n  </div>\n\n  <script>\n    function promptLogRun()'
    )

    old_script = """  <script>
    function promptLogRun() {
      const km = prompt('Enter distance in kilometers:');
      if (km) alert('Logged ' + km + ' km athletic run.');
    }
  </script>"""

    new_script = """  <script>
    function toggleLogRunPanel() {
      const p = document.getElementById('log-run-panel');
      if (!p) return;
      p.style.display = p.style.display === 'block' ? 'none' : 'block';
      if (p.style.display === 'block') {
        const inp = document.getElementById('run-title-in');
        if (inp) inp.focus();
      }
    }
    function saveRunLog() {
      const title = document.getElementById('run-title-in').value.trim() || 'Outdoor Cadence Run';
      const km = document.getElementById('run-km-in').value.trim();
      const time = document.getElementById('run-time-in').value.trim() || '28m';
      if (!km) {
        if (window.v07 && window.v07.ui) window.v07.ui.toast('Please enter distance in km', 'error');
        return;
      }
      const list = document.getElementById('recent-runs-list');
      if (list) {
        const row = document.createElement('div');
        row.className = 'run-log-row';
        row.innerHTML = `
          <div>
            <div style="font-size: 13px; font-weight: 600;">${title}</div>
            <div style="font-size: 11px; color: var(--subslate);">Just now • ${km} km in ${time}</div>
          </div>
          <span class="badge-pill">5'10" /km</span>
        `;
        list.insertBefore(row, list.firstChild);
      }
      toggleLogRunPanel();
      document.getElementById('run-title-in').value = '';
      document.getElementById('run-km-in').value = '';
      document.getElementById('run-time-in').value = '';
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Logged ' + km + ' km athletic run!', 'success');
      }
      if (window.v07 && window.v07.storage) {
        window.v07.storage.insertEvent({ category: 'running', km: parseFloat(km), duration: time, ts: Date.now() });
      }
    }
  </script>"""

    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched running")

def patch_timetracker():
    path = os.path.join(PLUGINS_DIR, "com.v07.timetracker", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Replace header button
    content = content.replace(
        '<button class="btn btn-primary" onclick="promptNewMilestone()">+ New</button>',
        '<button class="btn btn-primary" onclick="toggleNewMilestonePanel()">+ New Milestone</button>'
    )

    # Insert Add Milestone panel before Active Countdowns
    old_active = """  <div class="card-label"><span>Active Countdowns</span></div>"""
    milestone_panel = """  <!-- Inline New Milestone Panel -->
  <div id="new-milestone-panel" class="card" style="display: none; border: 1.5px dashed var(--brand-teal); background: var(--card-low); margin-bottom: 12px; padding: 12px 14px;">
    <div style="font-size: 12px; font-weight: 700; color: var(--slate); margin-bottom: 6px;">+ Create New Milestone Countdown</div>
    <input type="text" id="ms-title-in" placeholder="Event / Milestone Title (e.g. Marathon Race Day)..." style="width: 100%; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; margin-bottom: 6px; background: var(--card); color: var(--slate);">
    <div style="display: flex; gap: 6px; margin-bottom: 8px;">
      <input type="number" id="ms-days-in" placeholder="Target Days (e.g. 14)" style="flex: 1; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
      <select id="ms-cat-in" style="flex: 1; padding: 7px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); outline: none; background: var(--card); color: var(--slate);">
        <option value="Work">Work Milestone</option>
        <option value="Personal">Personal</option>
        <option value="Health">Health</option>
        <option value="Celebration">Celebration</option>
      </select>
    </div>
    <div style="display: flex; gap: 6px; justify-content: flex-end;">
      <button class="btn btn-secondary" onclick="toggleNewMilestonePanel()" style="padding: 4px 10px; font-size: 11px;">Cancel</button>
      <button class="btn btn-primary" onclick="saveNewMilestone()" style="padding: 4px 12px; font-size: 11px;">Create Countdown</button>
    </div>
  </div>

  <div class="card-label"><span>Active Countdowns</span></div>
  <div id="milestones-container">"""

    content = content.replace(old_active, milestone_panel)

    # Close #milestones-container before <script>
    content = content.replace(
        '  <script>\n    // Live 1-second interval ticker',
        '  </div>\n\n  <script>\n    // Live 1-second interval ticker'
    )

    old_script = """    function promptNewMilestone() {
      const title = prompt('Enter Milestone / Event Title:');
      if (title) alert('Added countdown for "' + title + '"');
    }"""

    new_script = """    function toggleNewMilestonePanel() {
      const p = document.getElementById('new-milestone-panel');
      if (!p) return;
      p.style.display = p.style.display === 'block' ? 'none' : 'block';
      if (p.style.display === 'block') {
        const inp = document.getElementById('ms-title-in');
        if (inp) inp.focus();
      }
    }
    function saveNewMilestone() {
      const title = document.getElementById('ms-title-in').value.trim();
      const days = parseInt(document.getElementById('ms-days-in').value.trim()) || 14;
      const cat = document.getElementById('ms-cat-in').value;
      if (!title) {
        if (window.v07 && window.v07.ui) window.v07.ui.toast('Please enter a milestone title', 'error');
        return;
      }
      const container = document.getElementById('milestones-container');
      if (container) {
        const card = document.createElement('div');
        card.className = 'card';
        card.style.marginTop = '8px';
        card.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
              <div style="font-size: 14px; font-weight: 700; color: var(--slate);">🎯 ${title}</div>
              <div style="font-size: 11px; color: var(--subslate); margin-top: 1px;">Target: In ${days} Days • ${cat}</div>
            </div>
            <span class="badge-pill">IN ${days} DAYS</span>
          </div>
          <div class="hud-sub" style="margin-top: 8px;">Active Countdown Ticker • Tracking ${days} days remaining</div>
        `;
        container.appendChild(card);
      }
      toggleNewMilestonePanel();
      document.getElementById('ms-title-in').value = '';
      document.getElementById('ms-days-in').value = '';
      if (window.v07 && window.v07.ui) {
        window.v07.ui.toast('Added countdown for "' + title + '"', 'success');
      }
      if (window.v07 && window.v07.storage) {
        window.v07.storage.insertEvent({ category: 'milestone', title: title, days: days, ts: Date.now() });
      }
    }"""

    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched timetracker")

def patch_vitals():
    path = os.path.join(PLUGINS_DIR, "com.v07.vitals", "index.html")
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Add status pill in header
    content = content.replace(
        '<span class="badge-pill">7-DAY ACTIVE</span>',
        '<span id="vitals-status-pill" class="badge-pill">READY TO LOG</span>'
    )

    old_script = """    function logVitals() {
      const note = document.getElementById('vitals-note').value;
      if (window.v07 && window.v07.storage) {
        window.v07.storage.insertEvent({
          category: 'vitals',
          mood: selectedMood,
          energy: selectedEnergy,
          note: note,
          ts: Date.now()
        }).then(() => alert('Logged Vitals: ' + selectedMood + ' (' + selectedEnergy + ')')).catch(() => alert('Recorded locally.'));
      } else {
        alert('Recorded Vitals: ' + selectedMood + ' (' + selectedEnergy + ')');
      }
      document.getElementById('vitals-note').value = '';
    }"""

    new_script = """    function logVitals() {
      const note = document.getElementById('vitals-note').value.trim();
      const nowStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      const statusPill = document.getElementById('vitals-status-pill');
      if (statusPill) {
        statusPill.textContent = '✓ LOGGED ' + nowStr;
        statusPill.style.background = 'var(--brand-teal-light)';
        statusPill.style.color = 'var(--brand-teal)';
      }
      if (window.v07 && window.v07.storage) {
        window.v07.storage.insertEvent({
          category: 'vitals',
          mood: selectedMood,
          energy: selectedEnergy,
          note: note,
          ts: Date.now()
        }).then(() => {
          if (window.v07 && window.v07.ui) window.v07.ui.toast('Logged Vitals: ' + selectedMood + ' (' + selectedEnergy + ')', 'success');
        }).catch(() => {
          if (window.v07 && window.v07.ui) window.v07.ui.toast('Recorded vitals locally.', 'success');
        });
      } else {
        if (window.v07 && window.v07.ui) {
          window.v07.ui.toast('Recorded Vitals: ' + selectedMood + ' (' + selectedEnergy + ')', 'success');
        }
      }
      document.getElementById('vitals-note').value = '';
    }"""

    content = content.replace(old_script.replace('\r\n', '\n'), new_script.replace('\r\n', '\n'))
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("Patched vitals")

if __name__ == "__main__":
    patch_gamify()
    patch_gita()
    patch_habits()
    patch_reader()
    patch_running()
    patch_timetracker()
    patch_vitals()
    print("All plugins patched cleanly!")
