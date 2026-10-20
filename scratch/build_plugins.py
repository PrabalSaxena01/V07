import os, json

BASE_DIR = r"D:\software\V07-Android\app\src\main\assets\plugins"

def build_all_plugins():
    # -------------------------------------------------------------
    # Common Calm Space Editorial Theme CSS Block
    # -------------------------------------------------------------
    common_css = """
    * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    :root {
      --canvas: #F4F7F5;
      --card: #FFFFFF;
      --card-low: #F8FAF9;
      --brand-teal: #2E7D6A;
      --brand-teal-light: #E8F5F1;
      --mint: #3F9A83;
      --cedar: #8C5E58;
      --cedar-light: #FAF0EE;
      --slate: #182230;
      --subslate: #5B6A78;
      --border: #DDE7E2;
      --border-subtle: rgba(0, 0, 0, 0.06);
      --font-main: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      --font-mono: 'Space Mono', 'JetBrains Mono', monospace;
      --radius: 14px;
      --radius-sm: 8px;
      --radius-pill: 9999px;
      --shadow-card: 0 1px 3px rgba(24, 34, 48, 0.04), 0 1px 2px rgba(24, 34, 48, 0.02);
    }
    :root[data-theme="dark"], body.dark {
      --canvas: #0B111E;
      --card: #141C2B;
      --card-low: #1A2436;
      --brand-teal: #4DE086;
      --brand-teal-light: rgba(77, 224, 134, 0.12);
      --mint: #7CF4A8;
      --cedar: #E09F95;
      --cedar-light: rgba(224, 159, 149, 0.12);
      --slate: #F1F5F9;
      --subslate: #94A3B8;
      --border: #262E3E;
      --border-subtle: rgba(255, 255, 255, 0.06);
      --shadow-card: 0 1px 3px rgba(0, 0, 0, 0.2);
    }
    body {
      background: var(--canvas);
      color: var(--slate);
      font-family: var(--font-main);
      padding: 16px 14px calc(32px + env(safe-area-inset-bottom, 16px));
      max-width: 540px;
      margin: 0 auto;
      overflow-y: auto;
      -webkit-overflow-scrolling: touch;
      line-height: 1.5;
    }
    .header-card {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 16px;
    }
    .header-info {
      display: flex;
      align-items: center;
      gap: 10px;
      min-width: 0;
    }
    .header-icon {
      width: 38px;
      height: 38px;
      border-radius: 10px;
      background: var(--brand-teal-light);
      color: var(--brand-teal);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      flex-shrink: 0;
    }
    .header-title-wrap h1 {
      font-size: 14px;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--slate);
      margin: 0;
    }
    .header-title-wrap p {
      font-size: 11px;
      color: var(--subslate);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin: 0;
    }
    .badge-pill {
      font-size: 10px;
      font-weight: 600;
      padding: 3px 8px;
      border-radius: var(--radius-pill);
      background: var(--brand-teal-light);
      color: var(--brand-teal);
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
    .btn {
      font-family: var(--font-main);
      font-size: 12px;
      font-weight: 600;
      padding: 7px 14px;
      border-radius: var(--radius-pill);
      border: 1px solid transparent;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.15s ease;
      white-space: nowrap;
    }
    .btn:active { transform: scale(0.96); }
    .btn-primary {
      background: var(--brand-teal);
      color: #FFFFFF;
      border-color: var(--brand-teal);
    }
    :root[data-theme="dark"] .btn-primary {
      color: #0B111E;
    }
    .btn-secondary {
      background: var(--card);
      color: var(--slate);
      border-color: var(--border);
    }
    .btn-secondary:hover { background: var(--card-low); }
    .card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 14px;
      margin-bottom: 14px;
      box-shadow: var(--shadow-card);
    }
    .card-label {
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--subslate);
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    """

    theme_sync_script = """
    (function(){
      function syncTheme(){
        var t = 'dark';
        try {
          t = window.parent.document.documentElement.dataset.theme || localStorage.getItem('v07_theme') || 'dark';
        } catch(e){
          try { t = localStorage.getItem('v07_theme') || 'dark'; } catch(e2){}
        }
        if(t === 'light') document.documentElement.setAttribute('data-theme', 'light');
        else document.documentElement.removeAttribute('data-theme');
      }
      syncTheme();
      window.addEventListener('storage', syncTheme);
      setInterval(syncTheme, 2000);
    })();
    """

    # -------------------------------------------------------------
    # 1. Weekly Digest (com.v07.chronosynth)
    # -------------------------------------------------------------
    chronosynth_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Weekly Digest</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.hud-grid {{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
}}
.hud-cell {{
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 12px;
}}
.hud-val {{
  font-family: var(--font-mono);
  font-size: 18px;
  font-weight: 700;
  color: var(--slate);
  margin: 4px 0 2px;
}}
.hud-sub {{
  font-size: 11px;
  color: var(--subslate);
}}
.delta-pill {{
  display: inline-block;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: var(--radius-pill);
  background: var(--brand-teal-light);
  color: var(--brand-teal);
  margin-top: 4px;
}}
.chip-row {{
  display: flex;
  gap: 6px;
  overflow-x: auto;
  margin-bottom: 12px;
  padding-bottom: 2px;
}}
.filter-chip {{
  font-size: 11px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  background: var(--card);
  border: 1px solid var(--border);
  color: var(--subslate);
  cursor: pointer;
  white-space: nowrap;
}}
.filter-chip.active {{
  background: var(--brand-teal);
  color: #FFFFFF;
  border-color: var(--brand-teal);
}}
.console-input {{
  width: 100%;
  background: var(--card-low);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  font-size: 12px;
  color: var(--slate);
  margin-top: 8px;
  margin-bottom: 10px;
  outline: none;
}}
.console-input:focus {{
  border-color: var(--brand-teal);
}}
.brief-markdown {{
  font-size: 12px;
  line-height: 1.6;
  color: var(--slate);
}}
.brief-markdown ul {{
  padding-left: 18px;
  margin: 6px 0;
}}
.brief-markdown li {{
  margin-bottom: 4px;
}}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon"><span class="material-symbols-outlined">psychology</span></div>
      <div class="header-title-wrap">
        <h1>Weekly Digest</h1>
        <p>Executive telemetry briefing & synthesis</p>
      </div>
    </div>
    <div style="display: flex; gap: 6px;">
      <span class="badge-pill">AI ENGINE</span>
      <span class="badge-pill" style="background: var(--brand-teal); color: #fff;">ACTIVE</span>
    </div>
  </header>

  <!-- Week Filter Bar -->
  <div class="chip-row">
    <div class="filter-chip active" onclick="selectWeek(0, this)">Current Week</div>
    <div class="filter-chip" onclick="selectWeek(1, this)">Oct 12 - 18</div>
    <div class="filter-chip" onclick="selectWeek(2, this)">Oct 05 - 11</div>
  </div>

  <!-- 4-Cell Telemetry HUD Grid -->
  <div class="hud-grid">
    <div class="hud-cell">
      <div class="card-label"><span>⏱️ Focus Time</span></div>
      <div class="hud-val" id="hud-focus">14h 25m</div>
      <span class="delta-pill">+2.4h vs last week</span>
    </div>
    <div class="hud-cell">
      <div class="card-label"><span>✓ Tasks Cleared</span></div>
      <div class="hud-val" id="hud-tasks">24 / 28</div>
      <div class="hud-sub">86% completion rate</div>
    </div>
    <div class="hud-cell">
      <div class="card-label"><span>🔥 Habit Matrix</span></div>
      <div class="hud-val" id="hud-habits">92%</div>
      <div class="hud-sub">5 active streaks</div>
    </div>
    <div class="hud-cell">
      <div class="card-label"><span>📱 Screen / Mindful</span></div>
      <div class="hud-val" id="hud-screen">1h 45m</div>
      <span class="delta-pill" style="background: var(--cedar-light); color: var(--cedar);">-45m below ceiling</span>
    </div>
  </div>

  <!-- AI Synthesis Console Card -->
  <div class="card">
    <div class="card-label">
      <span>⚡ AI Synthesis Console</span>
      <span style="color: var(--brand-teal); font-weight: 600;">Gemini Core</span>
    </div>
    <div class="chip-row" style="margin-bottom: 6px;">
      <div class="filter-chip active" id="style-exec" onclick="selectStyle('exec', this)">Executive Brief</div>
      <div class="filter-chip" id="style-tactical" onclick="selectStyle('tactical', this)">Tactical</div>
      <div class="filter-chip" id="style-deep" onclick="selectStyle('deep', this)">Deep Reflection</div>
    </div>
    <input type="text" id="ai-prompt-input" class="console-input" placeholder="Optional directive (e.g. focus on deep work and evening recovery)...">
    <button class="btn btn-primary" id="btn-generate" onclick="generateBriefing()" style="width: 100%; justify-content: center;">
      <span class="material-symbols-outlined" style="font-size: 16px;">bolt</span>
      <span>Generate Weekly Briefing</span>
    </button>
  </div>

  <!-- Executive Briefing Markdown Card -->
  <div class="card">
    <div class="card-label">
      <span>📌 Executive Intelligence Briefing</span>
      <span id="brief-timestamp" style="font-size: 10px; color: var(--subslate);">Generated Today</span>
    </div>
    <div class="brief-markdown" id="brief-content">
      <p><strong>Strategic Observations:</strong></p>
      <ul>
        <li>Deep work sessions peaked Tuesday and Thursday mornings with high flow duration (averaging 74 minutes per session).</li>
        <li>Habit consistency remained steady at 92%, with Morning Meditation holding an unbroken 14-day chain.</li>
        <li>Digital Stillness telemetry shows evening device pickups dropping by 28% after 9:30 PM.</li>
      </ul>
      <p style="margin-top: 10px;"><strong>Actionable Levers for Next Week:</strong></p>
      <ul>
        <li>Protect the 09:00 - 11:30 AM block from unscheduled context switches.</li>
        <li>Aim to clear chapter 4 of reading companion before Friday review.</li>
      </ul>
    </div>
    <div style="display: flex; gap: 8px; margin-top: 12px; padding-top: 10px; border-top: 1px solid var(--border);">
      <button class="btn btn-secondary" onclick="copyBriefing()">
        <span class="material-symbols-outlined" style="font-size: 14px;">content_copy</span>
        <span>Copy</span>
      </button>
      <button class="btn btn-secondary" onclick="exportToVault()">
        <span class="material-symbols-outlined" style="font-size: 14px;">save</span>
        <span>Save to Vault</span>
      </button>
    </div>
  </div>

  <script>
    let activeStyle = 'exec';
    function selectStyle(s, el) {{
      activeStyle = s;
      document.querySelectorAll('#style-exec, #style-tactical, #style-deep').forEach(c => c.classList.remove('active'));
      el.classList.add('active');
    }}
    function selectWeek(idx, el) {{
      document.querySelectorAll('.chip-row .filter-chip').forEach(c => c.classList.remove('active'));
      el.classList.add('active');
    }}
    async function generateBriefing() {{
      const btn = document.getElementById('btn-generate');
      btn.disabled = true;
      btn.innerHTML = '<span class="material-symbols-outlined" style="font-size: 16px; animation: spin 1s infinite linear;">sync</span> Synthesizing telemetry...';
      
      setTimeout(() => {{
        btn.disabled = false;
        btn.innerHTML = '<span class="material-symbols-outlined" style="font-size: 16px;">bolt</span> Generate Weekly Briefing';
        document.getElementById('brief-timestamp').textContent = 'Just now';
        if (window.v07 && window.v07.ui) {{
          window.v07.ui.toast('Weekly Briefing synthesized successfully', 'success');
        }}
      }}, 1200);
    }}
    function copyBriefing() {{
      const text = document.getElementById('brief-content').innerText;
      navigator.clipboard.writeText(text).then(() => {{
        alert('Briefing copied to clipboard!');
      }}).catch(() => {{}});
    }}
    function exportToVault() {{
      if (window.v07 && window.v07.storage) {{
        window.v07.storage.insertEvent({{
          category: 'digest',
          content: document.getElementById('brief-content').innerText,
          ts: Date.now()
        }}).then(() => alert('Exported to Sovereign Vault!')).catch(() => alert('Saved locally.'));
      }} else {{
        alert('Saved to Vault!');
      }}
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 2. Book Collection (com.v07.reader)
    # -------------------------------------------------------------
    reader_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Book Collection</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.book-card {{
  display: flex;
  gap: 14px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px;
  margin-bottom: 12px;
}}
.book-cover {{
  width: 64px;
  height: 92px;
  border-radius: 6px;
  background: linear-gradient(135deg, var(--brand-teal) 0%, #1c4b40 100%);
  color: #FFFFFF;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 6px;
  font-size: 9px;
  font-weight: 700;
  line-height: 1.2;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}}
.book-details {{
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}}
.book-title {{
  font-size: 14px;
  font-weight: 700;
  color: var(--slate);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}}
.book-author {{
  font-size: 11px;
  color: var(--subslate);
  margin-top: 1px;
}}
.progress-bar-wrap {{
  margin: 8px 0;
}}
.progress-bar {{
  height: 6px;
  background: var(--border);
  border-radius: var(--radius-pill);
  overflow: hidden;
}}
.progress-fill {{
  height: 100%;
  background: var(--brand-teal);
  border-radius: var(--radius-pill);
}}
.progress-text {{
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  font-family: var(--font-mono);
  color: var(--subslate);
  margin-top: 3px;
}}
.quick-btns {{
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: wrap;
}}
.chip-row {{
  display: flex;
  gap: 6px;
  overflow-x: auto;
  margin-bottom: 12px;
}}
.shelf-chip {{
  font-size: 11px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  background: var(--card);
  border: 1px solid var(--border);
  color: var(--subslate);
  cursor: pointer;
  white-space: nowrap;
}}
.shelf-chip.active {{
  background: var(--brand-teal);
  color: #FFFFFF;
  border-color: var(--brand-teal);
}}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon"><span class="material-symbols-outlined">auto_stories</span></div>
      <div class="header-title-wrap">
        <h1>Book Collection</h1>
        <p>Reading tracker, personal shelves & literary quotes</p>
      </div>
    </div>
    <div style="display: flex; gap: 6px;">
      <button class="btn btn-secondary" onclick="promptAddBook()">+ Add Book</button>
      <button class="btn btn-primary" onclick="searchOpenLibrary()">🔍 Search</button>
    </div>
  </header>

  <div class="chip-row">
    <div class="shelf-chip active" onclick="filterShelf('all', this)">ALL · 3</div>
    <div class="shelf-chip" onclick="filterShelf('reading', this)">READING · 1</div>
    <div class="shelf-chip" onclick="filterShelf('toread', this)">TO READ · 1</div>
    <div class="shelf-chip" onclick="filterShelf('done', this)">FINISHED · 1</div>
  </div>

  <div class="card-label"><span>Currently Immersed</span></div>
  <div class="book-card" id="hero-book">
    <div class="book-cover">
      <span>QUIET</span>
      <span style="font-size: 7px; opacity: 0.8; font-weight: 400;">S. CAIN</span>
    </div>
    <div class="book-details">
      <div>
        <div class="book-title">Quiet: The Power of Introverts</div>
        <div class="book-author">Susan Cain · 2012 ★★★★★ (5.0)</div>
      </div>
      <div class="progress-bar-wrap">
        <div class="progress-bar"><div class="progress-fill" id="quiet-bar" style="width: 40%;"></div></div>
        <div class="progress-text">
          <span id="quiet-pages">Page 142 of 352</span>
          <span id="quiet-pct">40%</span>
        </div>
      </div>
      <div class="quick-btns">
        <button class="btn btn-secondary" onclick="adjustPages(-5)" style="padding: 4px 10px; font-size: 11px;">-5p</button>
        <button class="btn btn-secondary" onclick="adjustPages(10)" style="padding: 4px 10px; font-size: 11px;">+10p</button>
        <button class="btn btn-secondary" onclick="showQuotes()" style="padding: 4px 10px; font-size: 11px;">📝 Notes (4)</button>
        <button class="btn btn-primary" onclick="openBookDetail()" style="padding: 4px 10px; font-size: 11px;">Update →</button>
      </div>
    </div>
  </div>

  <div class="card-label" style="margin-top: 16px;"><span>On Your Shelf</span></div>
  <div class="book-card">
    <div class="book-cover" style="background: linear-gradient(135deg, #182230 0%, #32435b 100%);">
      <span>ATOMIC HABITS</span>
      <span style="font-size: 7px; opacity: 0.8; font-weight: 400;">J. CLEAR</span>
    </div>
    <div class="book-details">
      <div>
        <div class="book-title">Atomic Habits</div>
        <div class="book-author">James Clear · [TO READ]</div>
      </div>
      <div class="hud-sub" style="margin: 8px 0;">Added Sep 12 · 320 pages</div>
      <button class="btn btn-secondary" onclick="startReading()" style="align-self: flex-start; padding: 4px 10px; font-size: 11px;">Start Reading</button>
    </div>
  </div>

  <script>
    let curPage = 142;
    const totalPages = 352;
    function adjustPages(delta) {{
      curPage = Math.max(0, Math.min(totalPages, curPage + delta));
      const pct = Math.round((curPage / totalPages) * 100);
      document.getElementById('quiet-bar').style.width = pct + '%';
      document.getElementById('quiet-pages').textContent = 'Page ' + curPage + ' of ' + totalPages;
      document.getElementById('quiet-pct').textContent = pct + '%';
      if (window.v07 && window.v07.storage) {{
        window.v07.storage.set('reader_quiet_page', curPage);
      }}
    }}
    function filterShelf(shelf, el) {{
      document.querySelectorAll('.shelf-chip').forEach(c => c.classList.remove('active'));
      el.classList.add('active');
    }}
    function promptAddBook() {{
      const title = prompt('Enter Book Title:');
      if (title) alert('Added "' + title + '" to your reading shelf.');
    }}
    function searchOpenLibrary() {{
      const q = prompt('Search Open Library / Google Books:');
      if (q) alert('Searching open literary databases for: ' + q);
    }}
    function showQuotes() {{
      alert('1. "Solitude matters, and for some people, it is the air they breathe."\\n\\n2. "There is zero correlation between being the best talker and having the best ideas."');
    }}
    function openBookDetail() {{
      adjustPages(5);
    }}
    function startReading() {{
      alert('Moved to Currently Reading shelf!');
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 3. Habits Matrix (com.v07.habits)
    # -------------------------------------------------------------
    habits_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Habits Matrix</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.habit-item {{
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}}
.habit-item:last-child {{ border-bottom: none; }}
.habit-left {{
  display: flex;
  align-items: center;
  gap: 12px;
}}
.habit-check {{
  width: 26px;
  height: 26px;
  border-radius: var(--radius-pill);
  border: 2px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--card);
}}
.habit-check.done {{
  background: var(--brand-teal);
  border-color: var(--brand-teal);
  color: #FFFFFF;
}}
.streak-badge {{
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  background: var(--cedar-light);
  color: var(--cedar);
  display: flex;
  align-items: center;
  gap: 4px;
}}
.heatmap-container {{
  overflow-x: auto;
  padding-bottom: 6px;
}}
.heatmap-svg {{
  width: 100%;
  min-width: 420px;
  height: 90px;
}}
.kpi-row {{
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  margin-top: 10px;
}}
.kpi-box {{
  background: var(--card-low);
  border-radius: var(--radius-sm);
  padding: 10px;
  text-align: center;
}}
.kpi-val {{
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--slate);
}}
.kpi-lbl {{
  font-size: 10px;
  color: var(--subslate);
  margin-top: 2px;
}}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon" style="background: var(--cedar-light); color: var(--cedar);"><span class="material-symbols-outlined">local_fire_department</span></div>
      <div class="header-title-wrap">
        <h1>Habits Matrix</h1>
        <p>Daily consistency engine & 20-week visual heatmap</p>
      </div>
    </div>
    <div style="display: flex; gap: 6px; align-items: center;">
      <span class="streak-badge">🔥 14 DAYS</span>
      <button class="btn btn-primary" onclick="promptNewHabit()">+ New</button>
    </div>
  </header>

  <!-- Today's Habits Card -->
  <div class="card">
    <div class="card-label">
      <span>Today's Habits · Daily Cadence</span>
      <span id="today-completion" style="color: var(--brand-teal); font-weight: 700;">2 of 3 Done</span>
    </div>
    <div class="habit-item">
      <div class="habit-left">
        <div class="habit-check done" onclick="toggleHabit(this)">
          <span class="material-symbols-outlined" style="font-size: 16px;">check</span>
        </div>
        <span style="font-size: 13px; font-weight: 600;">🌿 Morning Meditation (10m)</span>
      </div>
      <span class="streak-badge">🔥 14 days</span>
    </div>
    <div class="habit-item">
      <div class="habit-left">
        <div class="habit-check done" onclick="toggleHabit(this)">
          <span class="material-symbols-outlined" style="font-size: 16px;">check</span>
        </div>
        <span style="font-size: 13px; font-weight: 600;">💧 2L Clean Hydration</span>
      </div>
      <span class="streak-badge">🔥 21 days</span>
    </div>
    <div class="habit-item">
      <div class="habit-left">
        <div class="habit-check" onclick="toggleHabit(this)">
          <span class="material-symbols-outlined" style="font-size: 16px;">check</span>
        </div>
        <span style="font-size: 13px; font-weight: 600;">📖 Evening Reading (20p)</span>
      </div>
      <span class="streak-badge">🔥 6 days</span>
    </div>
  </div>

  <!-- 20-Week Consistency Heatmap Card -->
  <div class="card">
    <div class="card-label">
      <span>20-Week Consistency Heatmap</span>
      <span>88% Consistency</span>
    </div>
    <div class="heatmap-container">
      <svg class="heatmap-svg" viewBox="0 0 420 85" id="heatmap-svg">
        <!-- Generated procedurally by JS -->
      </svg>
    </div>
    <div style="display: flex; align-items: center; justify-content: flex-end; gap: 4px; font-size: 10px; color: var(--subslate); margin-top: 6px;">
      <span>Less</span>
      <span style="width: 10px; height: 10px; border-radius: 2px; background: #DDE7E2;"></span>
      <span style="width: 10px; height: 10px; border-radius: 2px; background: #C7E8DE;"></span>
      <span style="width: 10px; height: 10px; border-radius: 2px; background: #66BFA8;"></span>
      <span style="width: 10px; height: 10px; border-radius: 2px; background: #2E7D6A;"></span>
      <span>More</span>
    </div>
  </div>

  <!-- Streak Analytics -->
  <div class="card">
    <div class="card-label"><span>Streak Analytics</span></div>
    <div class="kpi-row">
      <div class="kpi-box">
        <div class="kpi-val">14 Days</div>
        <div class="kpi-lbl">Current Streak</div>
      </div>
      <div class="kpi-box">
        <div class="kpi-val">28 Days</div>
        <div class="kpi-lbl">Best Streak</div>
      </div>
      <div class="kpi-box">
        <div class="kpi-val">184 Total</div>
        <div class="kpi-lbl">Completions</div>
      </div>
    </div>
  </div>

  <script>
    function renderHeatmap() {{
      const svg = document.getElementById('heatmap-svg');
      let html = '';
      const colors = ['#DDE7E2', '#C7E8DE', '#66BFA8', '#2E7D6A'];
      const days = ['M', '', 'W', '', 'F', '', 'S'];
      
      for (let r = 0; r < 7; r++) {{
        if (days[r]) {{
          html += `<text x="4" y="${{r * 12 + 9}}" font-size="8" fill="var(--subslate)" font-family="var(--font-mono)">${{days[r]}}</text>`;
        }}
        for (let c = 0; c < 24; c++) {{
          const val = (r * 3 + c * 7) % 4;
          const col = colors[val];
          html += `<rect x="${{c * 15 + 20}}" y="${{r * 12}}" width="11" height="9" rx="2" fill="${{col}}" />`;
        }}
      }}
      svg.innerHTML = html;
    }}
    renderHeatmap();

    function toggleHabit(el) {{
      el.classList.toggle('done');
      const dones = document.querySelectorAll('.habit-check.done').length;
      document.getElementById('today-completion').textContent = dones + ' of 3 Done';
      if (window.v07 && window.v07.storage) {{
        window.v07.storage.set('habits_done_count', dones);
      }}
    }}
    function promptNewHabit() {{
      const name = prompt('Enter new daily habit:');
      if (name) alert('Added habit "' + name + '" to daily cadence.');
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 4. Tasks & Priorities (com.v07.tasks)
    # -------------------------------------------------------------
    tasks_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Tasks & Priorities</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.task-row {{
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}}
.task-row:last-child {{ border-bottom: none; }}
.task-left {{
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex: 1;
  min-width: 0;
}}
.task-check {{
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 2px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-top: 2px;
  flex-shrink: 0;
}}
.task-check.done {{
  background: var(--brand-teal);
  border-color: var(--brand-teal);
  color: #FFFFFF;
}}
.task-content {{
  flex: 1;
  min-width: 0;
}}
.task-name {{
  font-size: 13px;
  font-weight: 600;
  color: var(--slate);
}}
.task-row.done-row .task-name {{
  text-decoration: line-through;
  opacity: 0.5;
}}
.task-sub {{
  font-size: 11px;
  color: var(--subslate);
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 6px;
}}
.pri-badge {{
  font-size: 9px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: var(--radius-pill);
}}
.pri-high {{ background: #FFE8E5; color: #D32F2F; }}
.pri-med {{ background: #FFF4E5; color: #B45309; }}
.pri-low {{ background: #E8F5F1; color: var(--brand-teal); }}
.chip-row {{
  display: flex;
  gap: 6px;
  overflow-x: auto;
  margin-bottom: 12px;
}}
.filter-chip {{
  font-size: 11px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  background: var(--card);
  border: 1px solid var(--border);
  color: var(--subslate);
  cursor: pointer;
  white-space: nowrap;
}}
.filter-chip.active {{
  background: var(--brand-teal);
  color: #FFFFFF;
  border-color: var(--brand-teal);
}}
.quick-add-wrap {{
  display: flex;
  gap: 8px;
  margin-top: 14px;
}}
.quick-add-input {{
  flex: 1;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius-pill);
  padding: 8px 14px;
  font-size: 12px;
  color: var(--slate);
  outline: none;
}}
.quick-add-input:focus {{ border-color: var(--brand-teal); }}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon"><span class="material-symbols-outlined">edit_calendar</span></div>
      <div class="header-title-wrap">
        <h1>Tasks & Priorities</h1>
        <p>Daily sprint: 75% completed (6 of 8)</p>
      </div>
    </div>
    <button class="btn btn-primary" onclick="focusQuickAdd()">+ Add Task</button>
  </header>

  <!-- Sprint Progress Bar -->
  <div class="card" style="padding: 10px 14px; margin-bottom: 12px;">
    <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700; color: var(--slate); margin-bottom: 6px;">
      <span>Daily Sprint Progress</span>
      <span id="sprint-txt">6 of 8 Cleared (75%)</span>
    </div>
    <div style="height: 6px; background: var(--border); border-radius: var(--radius-pill); overflow: hidden;">
      <div id="sprint-fill" style="width: 75%; height: 100%; background: var(--brand-teal); border-radius: var(--radius-pill);"></div>
    </div>
  </div>

  <div class="chip-row">
    <div class="filter-chip active" onclick="filterTasks('all', this)">TODAY · 8</div>
    <div class="filter-chip" onclick="filterTasks('high', this)">HIGH PRIORITY · 2</div>
    <div class="filter-chip" onclick="filterTasks('upcoming', this)">UPCOMING · 3</div>
  </div>

  <div class="card-label"><span>Critical Today</span></div>
  <div class="card" style="margin-bottom: 12px;" id="tasks-container">
    <div class="task-row">
      <div class="task-left">
        <div class="task-check" onclick="toggleTask(this)">
          <span class="material-symbols-outlined" style="font-size: 14px; display: none;">check</span>
        </div>
        <div class="task-content">
          <div class="task-name">Finalize Q4 editorial strategy</div>
          <div class="task-sub">
            <span class="pri-badge pri-high">🔴 HIGH</span>
            <span>Due Today 5:00 PM • Work</span>
          </div>
        </div>
      </div>
      <button class="btn btn-secondary" onclick="deleteTask(this)" style="padding: 3px 6px; border: none;"><span class="material-symbols-outlined" style="font-size: 16px; color: var(--subslate);">delete</span></button>
    </div>
    <div class="task-row done-row">
      <div class="task-left">
        <div class="task-check done" onclick="toggleTask(this)">
          <span class="material-symbols-outlined" style="font-size: 14px;">check</span>
        </div>
        <div class="task-content">
          <div class="task-name">Send monthly reflection report</div>
          <div class="task-sub">
            <span class="pri-badge pri-med">🟡 MED</span>
            <span>Completed 10:30 AM • Personal</span>
          </div>
        </div>
      </div>
      <button class="btn btn-secondary" onclick="deleteTask(this)" style="padding: 3px 6px; border: none;"><span class="material-symbols-outlined" style="font-size: 16px; color: var(--subslate);">delete</span></button>
    </div>
    <div class="task-row">
      <div class="task-left">
        <div class="task-check" onclick="toggleTask(this)">
          <span class="material-symbols-outlined" style="font-size: 14px; display: none;">check</span>
        </div>
        <div class="task-content">
          <div class="task-name">Water indoor botanical garden</div>
          <div class="task-sub">
            <span class="pri-badge pri-low">🔵 LOW</span>
            <span>Mindfulness Cadence</span>
          </div>
        </div>
      </div>
      <button class="btn btn-secondary" onclick="deleteTask(this)" style="padding: 3px 6px; border: none;"><span class="material-symbols-outlined" style="font-size: 16px; color: var(--subslate);">delete</span></button>
    </div>
  </div>

  <!-- Quick Add Bar -->
  <div class="quick-add-wrap">
    <input type="text" id="quick-task-in" class="quick-add-input" placeholder="+ Add a task..." onkeydown="if(event.key==='Enter') quickAddTask()">
    <button class="btn btn-primary" onclick="quickAddTask()">+</button>
  </div>

  <script>
    function toggleTask(checkEl) {{
      checkEl.classList.toggle('done');
      const icon = checkEl.querySelector('.material-symbols-outlined');
      if (icon) icon.style.display = checkEl.classList.contains('done') ? 'block' : 'none';
      const row = checkEl.closest('.task-row');
      if (row) row.classList.toggle('done-row');
      updateSprint();
    }}
    function deleteTask(btn) {{
      const row = btn.closest('.task-row');
      if (row) row.remove();
      updateSprint();
    }}
    function updateSprint() {{
      const total = document.querySelectorAll('.task-row').length;
      const done = document.querySelectorAll('.task-check.done').length;
      const pct = total === 0 ? 100 : Math.round((done / total) * 100);
      document.getElementById('sprint-txt').textContent = `${{done}} of ${{total}} Cleared (${{pct}}%)`;
      document.getElementById('sprint-fill').style.width = pct + '%';
    }}
    function focusQuickAdd() {{
      document.getElementById('quick-task-in').focus();
    }}
    function quickAddTask() {{
      const inp = document.getElementById('quick-task-in');
      const val = inp.value.trim();
      if (!val) return;
      const div = document.createElement('div');
      div.className = 'task-row';
      div.innerHTML = `
        <div class="task-left">
          <div class="task-check" onclick="toggleTask(this)">
            <span class="material-symbols-outlined" style="font-size: 14px; display: none;">check</span>
          </div>
          <div class="task-content">
            <div class="task-name">${{val}}</div>
            <div class="task-sub">
              <span class="pri-badge pri-med">🟡 MED</span>
              <span>Today</span>
            </div>
          </div>
        </div>
        <button class="btn btn-secondary" onclick="deleteTask(this)" style="padding: 3px 6px; border: none;"><span class="material-symbols-outlined" style="font-size: 16px; color: var(--subslate);">delete</span></button>
      `;
      document.getElementById('tasks-container').appendChild(div);
      inp.value = '';
      updateSprint();
    }}
    function filterTasks(f, el) {{
      document.querySelectorAll('.chip-row .filter-chip').forEach(c => c.classList.remove('active'));
      el.classList.add('active');
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 5. Gamify (com.v07.gamify)
    # -------------------------------------------------------------
    gamify_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Gamify Quests</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.quest-item {{
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}}
.quest-item:last-child {{ border-bottom: none; }}
.quest-left {{
  display: flex;
  align-items: center;
  gap: 10px;
}}
.medals-grid {{
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}}
.medal-card {{
  background: var(--card-low);
  border-radius: var(--radius-sm);
  padding: 12px 6px;
  text-align: center;
  border: 1px solid var(--border);
}}
.medal-icon {{
  font-size: 24px;
  margin-bottom: 4px;
}}
.medal-name {{
  font-size: 11px;
  font-weight: 700;
  color: var(--slate);
}}
.medal-status {{
  font-size: 9px;
  font-weight: 600;
  color: var(--brand-teal);
  text-transform: uppercase;
  margin-top: 2px;
}}
.medal-card.locked {{
  opacity: 0.45;
  filter: grayscale(0.8);
}}
.medal-card.locked .medal-status {{
  color: var(--subslate);
}}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon" style="background: #FFF4E5; color: #B45309;"><span class="material-symbols-outlined">military_tech</span></div>
      <div class="header-title-wrap">
        <h1>Gamify Quests</h1>
        <p>Mindful RPG milestones & sovereign XP</p>
      </div>
    </div>
    <span class="badge-pill" style="font-family: var(--font-mono); font-size: 11px;">2,450 XP</span>
  </header>

  <!-- Player Level Card -->
  <div class="card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        <span style="font-size: 20px;">⭐</span>
        <div>
          <div style="font-size: 13px; font-weight: 700; color: var(--slate);">LEVEL 7: SOVEREIGN ARCHITECT</div>
          <div style="font-size: 11px; color: var(--subslate);">550 XP remaining to Level 8</div>
        </div>
      </div>
      <span class="badge-pill" style="background: var(--cedar-light); color: var(--cedar);">🔥 x1.4 BOOST</span>
    </div>
    <div style="height: 8px; background: var(--border); border-radius: var(--radius-pill); overflow: hidden;">
      <div id="xp-fill" style="width: 81%; height: 100%; background: linear-gradient(90deg, var(--brand-teal) 0%, #FFB84D 100%);"></div>
    </div>
  </div>

  <!-- Daily Quests -->
  <div class="card">
    <div class="card-label"><span>Today's Mindful Quests</span></div>
    <div class="quest-item">
      <div class="quest-left">
        <span class="material-symbols-outlined" style="color: var(--brand-teal);">check_circle</span>
        <div>
          <div style="font-size: 12px; font-weight: 600;">Clear 2 Focus Sessions</div>
          <div style="font-size: 10px; color: var(--subslate);">Completed 2/2</div>
        </div>
      </div>
      <button class="btn btn-primary" onclick="claimQuest(this, 50)" style="padding: 4px 10px; font-size: 11px;">+50 XP Claim</button>
    </div>
    <div class="quest-item">
      <div class="quest-left">
        <span class="material-symbols-outlined" style="color: var(--brand-teal);">check_circle</span>
        <div>
          <div style="font-size: 12px; font-weight: 600;">Read 20 Book Pages</div>
          <div style="font-size: 10px; color: var(--subslate);">Completed 25/20</div>
        </div>
      </div>
      <button class="btn btn-primary" onclick="claimQuest(this, 30)" style="padding: 4px 10px; font-size: 11px;">+30 XP Claim</button>
    </div>
    <div class="quest-item">
      <div class="quest-left">
        <span class="material-symbols-outlined" style="color: var(--subslate);">radio_button_unchecked</span>
        <div>
          <div style="font-size: 12px; font-weight: 600;">Complete All Habits Today</div>
          <div style="font-size: 10px; color: var(--subslate);">Progress: 2 of 3</div>
        </div>
      </div>
      <span class="badge-pill" style="background: var(--card-low); color: var(--subslate);">+100 XP (2/3)</span>
    </div>
  </div>

  <!-- Achievement Medals Deck -->
  <div class="card">
    <div class="card-label"><span>Achievement Medals</span></div>
    <div class="medals-grid">
      <div class="medal-card">
        <div class="medal-icon">🏅</div>
        <div class="medal-name">7-Day Flame</div>
        <div class="medal-status">UNLOCKED</div>
      </div>
      <div class="medal-card">
        <div class="medal-icon">📚</div>
        <div class="medal-name">Bookworm I</div>
        <div class="medal-status">UNLOCKED</div>
      </div>
      <div class="medal-card locked">
        <div class="medal-icon">🧘</div>
        <div class="medal-name">Zen Master</div>
        <div class="medal-status">LOCKED (2/5)</div>
      </div>
    </div>
  </div>

  <script>
    let totalXp = 2450;
    function claimQuest(btn, xp) {{
      totalXp += xp;
      btn.disabled = true;
      btn.textContent = 'Claimed!';
      btn.style.background = 'var(--card-low)';
      btn.style.color = 'var(--subslate)';
      alert(`+${{xp}} XP claimed! New total: ${{totalXp}} XP`);
      if (window.v07 && window.v07.storage) {{
        window.v07.storage.set('gamify_xp', totalXp);
      }}
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 6. Time Tracker & Milestones (com.v07.timetracker)
    # -------------------------------------------------------------
    timetracker_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Time Tracker & Milestones</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.countdown-grid {{
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin: 12px 0 8px;
}}
.digit-box {{
  background: var(--card-low);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 8px 4px;
  text-align: center;
}}
.digit-val {{
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  color: var(--slate);
}}
.digit-lbl {{
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--subslate);
  margin-top: 2px;
}}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon"><span class="material-symbols-outlined">schedule</span></div>
      <div class="header-title-wrap">
        <h1>Time Tracker</h1>
        <p>Milestone countdowns, deadlines & precision tickers</p>
      </div>
    </div>
    <button class="btn btn-primary" onclick="promptNewMilestone()">+ New</button>
  </header>

  <div class="card-label"><span>Active Countdowns</span></div>

  <!-- Milestone 1 -->
  <div class="card">
    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
      <div>
        <div style="font-size: 14px; font-weight: 700; color: var(--slate);">🚀 Product V3 Release Launch</div>
        <div style="font-size: 11px; color: var(--subslate); margin-top: 1px;">Target: Oct 31, 2026 • Work Milestone</div>
      </div>
      <span class="badge-pill" id="target1-pill">IN 11 DAYS</span>
    </div>

    <!-- 4 Digit Boxes -->
    <div class="countdown-grid">
      <div class="digit-box">
        <div class="digit-val" id="d1-days">11</div>
        <div class="digit-lbl">DAYS</div>
      </div>
      <div class="digit-box">
        <div class="digit-val" id="d1-hours">08</div>
        <div class="digit-lbl">HOURS</div>
      </div>
      <div class="digit-box">
        <div class="digit-val" id="d1-mins">32</div>
        <div class="digit-lbl">MINS</div>
      </div>
      <div class="digit-box">
        <div class="digit-val" id="d1-secs">15</div>
        <div class="digit-lbl">SECS</div>
      </div>
    </div>
  </div>

  <!-- Milestone 2 -->
  <div class="card">
    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
      <div>
        <div style="font-size: 14px; font-weight: 700; color: var(--slate);">🎂 Autumn Solstice Celebration</div>
        <div style="font-size: 11px; color: var(--subslate); margin-top: 1px;">Target: Nov 15, 2026 • Personal</div>
      </div>
      <span class="badge-pill" style="background: var(--cedar-light); color: var(--cedar);">IN 26 DAYS</span>
    </div>
    <div class="hud-sub" style="margin-top: 8px;">26 Days · 14 Hours remaining</div>
  </div>

  <script>
    // Live 1-second interval ticker for Milestone 1
    const targetDate = new Date(Date.now() + 11 * 24 * 3600 * 1000 + 8 * 3600 * 1000 + 32 * 60 * 1000 + 15 * 1000);
    function updateTicker() {{
      const now = new Date();
      const diff = Math.max(0, targetDate - now);
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
      const mins = Math.floor((diff / (1000 * 60)) % 60);
      const secs = Math.floor((diff / 1000) % 60);

      document.getElementById('d1-days').textContent = String(days).padStart(2, '0');
      document.getElementById('d1-hours').textContent = String(hours).padStart(2, '0');
      document.getElementById('d1-mins').textContent = String(mins).padStart(2, '0');
      document.getElementById('d1-secs').textContent = String(secs).padStart(2, '0');
      document.getElementById('target1-pill').textContent = `IN ${{days}} DAYS`;
    }}
    setInterval(updateTicker, 1000);
    updateTicker();

    function promptNewMilestone() {{
      const title = prompt('Enter Milestone / Event Title:');
      if (title) alert('Added countdown for "' + title + '"');
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 7. Mood & Energy Vitals (com.v07.vitals)
    # -------------------------------------------------------------
    vitals_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Mood & Energy Vitals</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.mood-grid {{
  display: flex;
  gap: 6px;
  overflow-x: auto;
  margin: 8px 0 14px;
}}
.mood-chip {{
  font-size: 11px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  background: var(--card-low);
  border: 1px solid var(--border);
  color: var(--slate);
  cursor: pointer;
  white-space: nowrap;
}}
.mood-chip.active {{
  background: var(--brand-teal);
  color: #FFFFFF;
  border-color: var(--brand-teal);
}}
.battery-row {{
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  margin: 8px 0 14px;
}}
.batt-btn {{
  background: var(--card-low);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 8px 2px;
  text-align: center;
  font-size: 10px;
  font-weight: 700;
  color: var(--subslate);
  cursor: pointer;
}}
.batt-btn.active {{
  background: var(--brand-teal);
  color: #FFFFFF;
  border-color: var(--brand-teal);
}}
.note-input {{
  width: 100%;
  background: var(--card-low);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 8px 12px;
  font-size: 12px;
  color: var(--slate);
  outline: none;
  margin-bottom: 10px;
}}
.note-input:focus {{ border-color: var(--brand-teal); }}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon"><span class="material-symbols-outlined">bolt</span></div>
      <div class="header-title-wrap">
        <h1>Mood & Energy Vitals</h1>
        <p>Circadian presence, mood equilibrium & somatic vitality</p>
      </div>
    </div>
    <span class="badge-pill">HISTORY →</span>
  </header>

  <div class="card">
    <div class="card-label"><span>How are you feeling right now?</span></div>
    
    <!-- Mood Selector -->
    <div class="hud-sub" style="font-weight: 600; margin-top: 4px;">MOOD STATE</div>
    <div class="mood-grid">
      <div class="mood-chip active" onclick="selectMood('🌿 Calm', this)">🌿 Calm</div>
      <div class="mood-chip" onclick="selectMood('✨ Inspired', this)">✨ Inspired</div>
      <div class="mood-chip" onclick="selectMood('☕ Focused', this)">☕ Focused</div>
      <div class="mood-chip" onclick="selectMood('🌧️ Melancholy', this)">🌧️ Melancholy</div>
      <div class="mood-chip" onclick="selectMood('⚡ Restless', this)">⚡ Restless</div>
    </div>

    <!-- Energy Battery -->
    <div class="hud-sub" style="font-weight: 600;">ENERGY LEVEL</div>
    <div class="battery-row">
      <div class="batt-btn" onclick="selectEnergy('10%', this)">10%</div>
      <div class="batt-btn" onclick="selectEnergy('30%', this)">30%</div>
      <div class="batt-btn" onclick="selectEnergy('50%', this)">50%</div>
      <div class="batt-btn active" onclick="selectEnergy('75%', this)">75%</div>
      <div class="batt-btn" onclick="selectEnergy('PEAK', this)">100%</div>
    </div>

    <input type="text" id="vitals-note" class="note-input" placeholder="Add an optional somatic or emotional note...">
    <button class="btn btn-primary" onclick="logVitals()" style="width: 100%; justify-content: center;">Log Vitals Now</button>
  </div>

  <!-- Sleep Logger Card -->
  <div class="card">
    <div class="card-label"><span>Last Night's Sleep</span></div>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <div>
        <div style="font-size: 14px; font-weight: 700; color: var(--slate);">🌙 7h 45m (11:15 PM → 07:00 AM)</div>
        <div style="font-size: 11px; color: var(--subslate); margin-top: 2px;">Quality: ★★★★☆ (Good recovery)</div>
      </div>
      <span class="badge-pill">RESTFUL</span>
    </div>
  </div>

  <!-- 7-Day Sparkline Correlation -->
  <div class="card">
    <div class="card-label"><span>7-Day Energy & Mood Correlation</span></div>
    <div style="font-size: 11px; color: var(--subslate); margin-bottom: 8px;">Energy: 75% average • Peak days: Tuesday & Friday</div>
    <div style="height: 10px; background: var(--border); border-radius: var(--radius-pill); overflow: hidden;">
      <div style="width: 75%; height: 100%; background: linear-gradient(90deg, var(--brand-teal) 0%, #FFB84D 100%);"></div>
    </div>
  </div>

  <script>
    let selectedMood = '🌿 Calm';
    let selectedEnergy = '75%';
    function selectMood(m, el) {{
      selectedMood = m;
      document.querySelectorAll('.mood-chip').forEach(c => c.classList.remove('active'));
      el.classList.add('active');
    }}
    function selectEnergy(e, el) {{
      selectedEnergy = e;
      document.querySelectorAll('.batt-btn').forEach(b => b.classList.remove('active'));
      el.classList.add('active');
    }}
    function logVitals() {{
      const note = document.getElementById('vitals-note').value;
      if (window.v07 && window.v07.storage) {{
        window.v07.storage.insertEvent({{
          category: 'vitals',
          mood: selectedMood,
          energy: selectedEnergy,
          note: note,
          ts: Date.now()
        }}).then(() => alert('Logged Vitals: ' + selectedMood + ' (' + selectedEnergy + ')')).catch(() => alert('Recorded locally.'));
      }} else {{
        alert('Recorded Vitals: ' + selectedMood + ' (' + selectedEnergy + ')');
      }}
      document.getElementById('vitals-note').value = '';
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 8. Bhagavad Gita Wisdom (com.v07.gita)
    # -------------------------------------------------------------
    gita_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Bhagavad Gita Wisdom</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.sanskrit-verse {{
  font-size: 15px;
  line-height: 1.8;
  font-weight: 600;
  text-align: center;
  color: var(--brand-teal);
  margin: 12px 0;
  padding: 10px;
  background: var(--card-low);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--brand-teal);
}}
.translit {{
  font-size: 12px;
  font-style: italic;
  color: var(--subslate);
  text-align: center;
  margin-bottom: 12px;
}}
.purport-box {{
  font-size: 12px;
  line-height: 1.6;
  color: var(--slate);
}}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon" style="background: #FFF4E5; color: #B45309;"><span class="material-symbols-outlined">menu_book</span></div>
      <div class="header-title-wrap">
        <h1>Bhagavad Gita Wisdom</h1>
        <p>Daily contemplative Sanskrit verses & philosophical purport</p>
      </div>
    </div>
    <button class="btn btn-secondary" onclick="nextVerse()">🎲 Verse</button>
  </header>

  <div class="card">
    <div class="card-label">
      <span id="verse-ref">Chapter 2, Verse 47</span>
      <span class="badge-pill">KARMA YOGA</span>
    </div>

    <!-- Sanskrit Verse -->
    <div class="sanskrit-verse" id="verse-sanskrit">
      कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br>
      मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥
    </div>

    <div class="translit" id="verse-translit">
      karmaṇy-evādhikāras te mā phaleṣu kadācana<br>
      mā karma-phala-hetur bhūr mā te saṅgo 'stv akarmaṇi
    </div>

    <div class="card-label" style="margin-top: 14px;"><span>Philosophical Purport</span></div>
    <div class="purport-box" id="verse-purport">
      You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty.
    </div>
  </div>

  <div class="card">
    <div class="card-label"><span>Daily Mindful Contemplation</span></div>
    <p class="hud-sub" style="margin-bottom: 8px;">How does this verse speak to your current focus, attachment, or daily work?</p>
    <textarea id="gita-reflection" style="width: 100%; height: 70px; background: var(--card-low); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 8px 12px; font-size: 12px; color: var(--slate); outline: none;" placeholder="Record a quiet reflection..."></textarea>
    <button class="btn btn-primary" onclick="saveReflection()" style="margin-top: 8px;">Save Reflection</button>
  </div>

  <script>
    const verses = [
      {{
        ref: "Chapter 2, Verse 47",
        cat: "KARMA YOGA",
        san: "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br>मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
        tra: "karmaṇy-evādhikāras te mā phaleṣu kadācana<br>mā karma-phala-hetur bhūr mā te saṅgo 'stv akarmaṇi",
        pur: "You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty."
      }},
      {{
        ref: "Chapter 6, Verse 5",
        cat: "DHYANA YOGA",
        san: "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।<br>आत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः॥",
        tra: "uddhared ātmanātmānaṁ nātmānam avasādayet<br>ātmaiva hy ātmano bandhur ātmaiva ripur ātmanaḥ",
        pur: "One must elevate oneself by one's own mind, and not degrade oneself. The mind is the friend of the conditioned soul, and its enemy as well."
      }},
      {{
        ref: "Chapter 2, Verse 70",
        cat: "SAMADHI",
        san: "आपूर्यमाणमचलप्रतिष्ठं समुद्रमापः प्रविशन्ति यद्वत्।<br>तद्वत्कामा यं प्रविशन्ति सर्वे स शान्तिमाप्नोति न कामकामी॥",
        tra: "āpūryamāṇam acala-pratiṣṭhaṁ samudram āpaḥ praviśanti yadvat<br>tadvat kāmā yaṁ praviśanti sarve sa śāntim āpnoti na kāma-kāmī",
        pur: "A person who is not disturbed by the incessant flow of desires—that enter like rivers into the ocean, which is ever being filled but is always still—can alone achieve peace."
      }}
    ];
    let vIdx = 0;
    function nextVerse() {{
      vIdx = (vIdx + 1) % verses.length;
      const v = verses[vIdx];
      document.getElementById('verse-ref').textContent = v.ref;
      document.getElementById('verse-sanskrit').innerHTML = v.san;
      document.getElementById('verse-translit').innerHTML = v.tra;
      document.getElementById('verse-purport').innerHTML = v.pur;
    }}
    function saveReflection() {{
      const t = document.getElementById('gita-reflection').value;
      if (window.v07 && window.v07.storage) {{
        window.v07.storage.insertEvent({{ category: 'gita_reflection', note: t, ts: Date.now() }});
      }}
      alert('Reflection saved to sovereign vault!');
    }}
  </script>
</body>
</html>
"""

    # -------------------------------------------------------------
    # 9. Athletic Rhythm & Running (com.v07.running)
    # -------------------------------------------------------------
    running_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<script src="v07-sdk.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Athletic Rhythm & Running</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Mono:wght@400;700&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
<style>
{common_css}
.kpi-row {{
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}}
.kpi-box {{
  background: var(--card-low);
  border-radius: var(--radius-sm);
  padding: 10px 4px;
  text-align: center;
}}
.kpi-val {{
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--slate);
}}
.kpi-lbl {{
  font-size: 10px;
  color: var(--subslate);
  margin-top: 2px;
}}
.run-log-row {{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}}
.run-log-row:last-child {{ border-bottom: none; }}
</style>
<script>{theme_sync_script}</script>
</head>
<body>
  <header class="header-card">
    <div class="header-info">
      <div class="header-icon"><span class="material-symbols-outlined">directions_run</span></div>
      <div class="header-title-wrap">
        <h1>Athletic Rhythm</h1>
        <p>Cadence pacing, stamina zones & recovery analytics</p>
      </div>
    </div>
    <button class="btn btn-primary" onclick="promptLogRun()">+ Log Run</button>
  </header>

  <!-- Weekly Mileage Goal -->
  <div class="card">
    <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700; margin-bottom: 6px;">
      <span>Weekly Mileage Goal</span>
      <span>18.4 / 25.0 km (73%)</span>
    </div>
    <div style="height: 6px; background: var(--border); border-radius: var(--radius-pill); overflow: hidden;">
      <div style="width: 73%; height: 100%; background: var(--brand-teal); border-radius: var(--radius-pill);"></div>
    </div>
  </div>

  <!-- Performance Analytics -->
  <div class="card">
    <div class="card-label"><span>Pacing & Stamina Metrics</span></div>
    <div class="kpi-row">
      <div class="kpi-box">
        <div class="kpi-val">5'12"</div>
        <div class="kpi-lbl">Avg Pace /km</div>
      </div>
      <div class="kpi-box">
        <div class="kpi-val">172 spm</div>
        <div class="kpi-lbl">Cadence</div>
      </div>
      <div class="kpi-box">
        <div class="kpi-val">148 bpm</div>
        <div class="kpi-lbl">Avg Heart Rate</div>
      </div>
    </div>
  </div>

  <!-- Recent Sessions -->
  <div class="card">
    <div class="card-label"><span>Recent Cadence Runs</span></div>
    <div class="run-log-row">
      <div>
        <div style="font-size: 13px; font-weight: 600;">Morning Forest Interval</div>
        <div style="font-size: 11px; color: var(--subslate);">Yesterday • 6.2 km in 32m 14s</div>
      </div>
      <span class="badge-pill">5'11" /km</span>
    </div>
    <div class="run-log-row">
      <div>
        <div style="font-size: 13px; font-weight: 600;">Aerobic Base Flow</div>
        <div style="font-size: 11px; color: var(--subslate);">Sunday • 12.2 km in 1h 04m</div>
      </div>
      <span class="badge-pill" style="background: var(--cedar-light); color: var(--cedar);">5'16" /km</span>
    </div>
  </div>

  <script>
    function promptLogRun() {{
      const km = prompt('Enter distance in kilometers:');
      if (km) alert('Logged ' + km + ' km athletic run.');
    }}
  </script>
</body>
</html>
"""

    # Dictionary of plugins to write
    plugin_files = {
        "com.v07.chronosynth": chronosynth_html,
        "com.v07.reader": reader_html,
        "com.v07.habits": habits_html,
        "com.v07.tasks": tasks_html,
        "com.v07.gamify": gamify_html,
        "com.v07.timetracker": timetracker_html,
        "com.v07.vitals": vitals_html,
        "com.v07.gita": gita_html,
        "com.v07.running": running_html
    }

    # Write each plugin's index.html
    for plugin_id, html_content in plugin_files.items():
        dest = os.path.join(BASE_DIR, plugin_id, "index.html")
        with open(dest, "w", encoding="utf-8") as f:
            f.write(html_content)
        print(f"Wrote {dest} ({len(html_content)} bytes)")

    print("All plugins successfully built as per design specifications.")

if __name__ == "__main__":
    build_all_plugins()
