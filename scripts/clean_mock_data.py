import re
import os

filepath = r"D:\software\V07-Android\scratch\build_perfect_v3.py"
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Clean reading card
content = content.replace("Quiet: The Power of Introverts", "The Bhagavad Gita")
content = content.replace("Susan Cain", "Daily Wisdom Passage")
content = content.replace("Chapter 4", "Daily verse")
content = content.replace("42% complete", "Wisdom companion")
content = content.replace('style="width: 42%;"', 'style="width: 25%;"')
content = content.replace("Kindle sync", "Wisdom Companion")

# 2. Clean profile name and email
content = content.replace("Sarah Jenkins", "Sanctuary User")
content = content.replace("sarah@example.com", "Local Database Active")

# 3. Clean greeting in updateGreeting()
old_greeting = """      let greeting = 'Good morning, Sarah';
      if (hour >= 12 && hour < 17) {
        greeting = 'Good afternoon, Sarah';
      } else if (hour >= 17) {
        greeting = 'Good evening, Sarah';
      }"""

new_greeting = """      let greeting = 'Good morning';
      if (hour >= 12 && hour < 17) {
        greeting = 'Good afternoon';
      } else if (hour >= 17) {
        greeting = 'Good evening';
      }
      const savedName = localStorage.getItem('v07_profile_name');
      if (savedName && savedName.trim()) {
        greeting += ', ' + cleanText(savedName).split(' ')[0];
      }"""
content = content.replace(old_greeting, new_greeting)

# 4. Clean initial tasks and thoughts
old_tasks = """    // Default Sanctuary Tasks
    let tasks = [
      { id: '1', title: 'Review morning pages', time: '7:45 am', done: true },
      { id: '2', title: 'Water the plants', time: 'Ferns and monstera by window', done: false },
      { id: '3', title: 'Read for 10 minutes', time: 'Balcony tea time', done: false }
    ];"""

new_tasks = """    // Default Sanctuary Tasks
    let tasks = [];"""
content = content.replace(old_tasks, new_tasks)

old_thoughts = """    // Default Journal Thoughts
    let thoughts = [
      {
        id: 't1',
        text: 'Morning clarity after the green tea. Finished the chapter on solitude. The quiet hours of early dawn bring an unmatched spaciousness.',
        time: '09:30 AM',
        date: 'Today',
        tags: ['calm', 'morning'],
        mood: 'wb_twilight'
      },
      {
        id: 't2',
        text: 'Thoughts on deep focus without artificial urgency. When we remove notifications, attention naturally deepens into slow flow.',
        time: '10:15 PM',
        date: 'Yesterday',
        tags: ['reading', 'focus'],
        mood: 'nightlight'
      },
      {
        id: 't3',
        text: 'Walk through the pine canopy before dusk. The air smelled of damp cedar. Gratitude for quiet evenings with no alarms.',
        time: '06:40 PM',
        date: 'Yesterday',
        tags: ['gratitude', 'calm'],
        mood: 'park'
      }
    ];"""

new_thoughts = """    // Default Journal Thoughts
    let thoughts = [];"""
content = content.replace(old_thoughts, new_thoughts)

# 5. Empty states in renderTasks()
old_render_tasks = """    function renderTasks() {
      const list = document.getElementById('home-tasks-list');
      if (!list) return;
      list.innerHTML = '';

      let doneCount = 0;"""

new_render_tasks = """    function renderTasks() {
      const list = document.getElementById('home-tasks-list');
      if (!list) return;
      list.innerHTML = '';

      if (tasks.length === 0) {
        list.innerHTML = `
          <div class="py-4 text-center flex flex-col items-center justify-center text-on-surface-variant select-none">
            <span class="material-symbols-outlined text-outline text-[24px] mb-1">spa</span>
            <p class="font-body-sm text-body-sm text-on-surface-variant font-medium">No tasks scheduled for today.</p>
            <span class="font-label-sm text-label-sm text-outline mt-0.5">Take a mindful pause or add a gentle note below.</span>
          </div>
        `;
        const badge = document.getElementById('tasks-done-badge');
        if (badge) badge.textContent = `0 done`;
        return;
      }

      let doneCount = 0;"""
content = content.replace(old_render_tasks, new_render_tasks)

# 6. Empty state in renderJournalEntries()
old_render_journal = """    function renderJournalEntries() {
      const container = document.getElementById('journal-entries-container');
      if (!container) return;
      container.innerHTML = '';

      const query = (document.getElementById('journal-search')?.value || '').toLowerCase().trim();"""

new_render_journal = """    function renderJournalEntries() {
      const container = document.getElementById('journal-entries-container');
      if (!container) return;
      container.innerHTML = '';

      const query = (document.getElementById('journal-search')?.value || '').toLowerCase().trim();

      if (thoughts.length === 0) {
        container.innerHTML = `
          <div class="py-8 text-center flex flex-col items-center justify-center text-on-surface-variant select-none">
            <span class="material-symbols-outlined text-outline text-[32px] mb-2">edit_note</span>
            <p class="font-body-md text-body-md text-on-surface-variant font-medium">No reflections recorded yet.</p>
            <span class="font-label-sm text-label-sm text-outline mt-1">Capture your first quiet thought above.</span>
          </div>
        `;
        return;
      }"""
content = content.replace(old_render_journal, new_render_journal)

# 7. Add Gita sync to syncBackendData()
old_sync_end = """      try {
        const rTasks = await apiV3('/api/v3/tasks');
        if (rTasks.ok) {
          const j = await rTasks.json();
          if (j.tasks && j.tasks.length > 0) {
            tasks = j.tasks.map(t => ({
              id: t.id,
              title: cleanText(t.title),
              time: t.due_date || t.deadline || 'Today',
              done: t.completed === true || t.status === 'done'
            }));
            renderTasks();
          }
        }
      } catch(e) {}
    }"""

new_sync_end = """      try {
        const rTasks = await apiV3('/api/v3/tasks');
        if (rTasks.ok) {
          const j = await rTasks.json();
          const serverTasks = j.tasks || [];
          tasks = serverTasks.map(t => ({
            id: t.id,
            title: cleanText(t.title),
            time: t.due_date || t.deadline || 'Today',
            done: t.completed === true || t.status === 'done' || t.status === 'completed'
          }));
          renderTasks();
        }
      } catch(e) {}

      try {
        const rGita = await apiV3('/api/v3/gita/daily');
        if (rGita.ok) {
          const g = await rGita.json();
          const bookTitle = document.getElementById('home-book-title');
          const bookAuthor = document.getElementById('home-book-author');
          const bookChapter = document.getElementById('home-book-chapter');
          const bookPercent = document.getElementById('home-book-percent');
          const bookBar = document.getElementById('home-book-bar');
          if (bookTitle) bookTitle.textContent = 'The Bhagavad Gita';
          const ch = g.chapter || 2;
          const vs = g.verse || 47;
          if (bookAuthor) bookAuthor.textContent = `Chapter ${ch} • Verse ${vs}`;
          if (bookChapter) bookChapter.textContent = `Verse ${vs}`;
          const readCount = g.read_count || 0;
          const totalVerses = g.total_verses || 701;
          const pct = totalVerses > 0 ? Math.round((readCount * 100) / totalVerses) : 0;
          if (bookPercent) bookPercent.textContent = `${readCount}/${totalVerses} read (${pct}%)`;
          if (bookBar) bookBar.style.width = `${Math.max(5, pct)}%`;
        }
      } catch(e) {}
    }"""
content = content.replace(old_sync_end, new_sync_end)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Cleaned build_perfect_v3.py successfully!")
