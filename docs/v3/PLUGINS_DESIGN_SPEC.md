# V07 Calm Space: Comprehensive Plugin Design Specification
> **Audience**: Graphic Designers, UI/UX Designers, AI Coding Agents, and Frontend Engineers  
> **Scope**: Exhaustive page layouts, component inventories, visual hierarchies, and state matrices for the full-page `index.html` of all 7 core plugins.  
> **Theme Integration**: All plugins inherit the **Calm Space Editorial** design system:
> - Canvas `#F4F7F5` | Card `#FFFFFF` | Brand Teal `#2E7D6A` | Mint `#3F9A83` | Cedar `#8C5E58` | Slate `#182230` | Subslate `#5B6A78` | Border `#DDE7E2`
> - Primary UI: `Plus Jakarta Sans` | Technical / Monospace: `JetBrains Mono` / `Space Mono`
> - Full viewport hosting inside `#pluginFramePage` (`width: 100vw; height: 100%; top: 0; bottom: 0; z-index: 200`).

---

## 1. Global Plugin Architecture & Shell Integration

### 1.1 Plugin Host Viewport Contract
- **Container**: Each plugin runs inside a sandboxed `<iframe>` within `#pluginFramePage`.
- **Dimensions**: 100% viewport width (`100vw`), 100% remaining viewport height below the 48px plugin header.
- **Top Shell Header**:
  - `[←]` Back Button (40px touch target, returns to Tool Kit catalog).
  - Center: Plugin Name in uppercase bold monospace (`font-size: 13px; letter-spacing: 0.06em`).
  - Right: `[CLOSE]` pill button (`font-size: 11px; padding: 4px 12px; border-radius: 9999px; background: #FAF0EE; color: #8C5E58`).
- **Scroll Container**: Internal plugin `<body>` has `padding: 16px 14px calc(32px + env(safe-area-inset-bottom, 16px))`, with `max-width: 540px; margin: 0 auto; overflow-y: auto; -webkit-overflow-scrolling: touch`.

---

## 2. Plugin 1: Weekly Digest (`com.v07.chronosynth`)

### Purpose & User Goal
Aggregates the user's entire week of focus sessions, completed tasks, habit consistency, screen telemetry, and mental state into an executive AI briefing.

```
┌────────────────────────────────────────────────────────┐
│  [🧠] WEEKLY DIGEST            [AI ENGINE] [ACTIVE]    │  <-- Header
│  Executive telemetry briefing & synthesis              │
├────────────────────────────────────────────────────────┤
│  ( Current Week )  ( Sep 15 - 21 )  ( Sep 08 - 14 )    │  <-- Week Chips
│                                                        │
│  WEEKLY TELEMETRY HUD                                  │
│  ┌───────────────────────┐  ┌────────────────────────┐ │  <-- 4-Cell Grid
│  │ ⏱️ FOCUS TIME          │  │ ✓ TASKS CLEARED        │ │
│  │ 14h 25m               │  │ 24 / 28                │ │
│  │ +2.4h vs last week    │  │ 86% completion rate    │ │
│  └───────────────────────┘  └────────────────────────┘ │
│  ┌───────────────────────┐  ┌────────────────────────┐ │
│  │ 🔥 HABIT CONSISTENCY  │  │ 📱 SCREEN / MINDFUL    │ │
│  │ 92%                   │  │ 1h 45m / day avg       │ │
│  │ 5 active streaks      │  │ 45m below daily ceiling│ │
│  └───────────────────────┘  └────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ⚡ AI SYNTHESIS CONSOLE                           │  │  <-- AI Console
│  │ Style: [ Executive Brief ]  [ Tactical ]  [ Deep ]│  │
│  │ [ Focus on balance, solitude, and screen pacing ]│  │
│  │ [ ⚡ GENERATE WEEKLY BRIEFING                   ]│  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  EXECUTIVE BRIEFING                                    │  <-- Briefing Card
│  ┌──────────────────────────────────────────────────┐  │
│  │ 📌 Key Takeaways                                 │  │
│  │ • Focus sessions peaked Tuesday morning (3.5 hrs) │  │
│  │ • Reading habit hit 14 consecutive days          │  │
│  │ • Screen time dropped 18% below quota            │  │
│  │                                                  │  │
│  │ 🎯 Next Week's Strategic Levers                  │  │
│  │ • Protect 9am-11am deep work window              │  │
│  │ • Complete Chapter 5 of Quiet before Friday      │  │
│  │ [ 📋 Copy Briefing ]  [ 💾 Export to Vault ]     │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Header & Status Banner**:
   - 28px square badge with brain icon (`psychology`), title "WEEKLY DIGEST", status pills ("AI ENGINE", "ONLINE").
2. **Week Filter Bar**:
   - Horizontally scrolling chips for current and past weeks. Active chip has `#2E7D6A` background and white text.
3. **4-Cell Telemetry HUD Grid**:
   - 2-column on mobile, cards in `#FFFFFF` with `1px solid #DDE7E2`, radius `14px`.
   - Card 1: Focus hours + delta pill vs last week.
   - Card 2: Tasks cleared ratio + progress bar.
   - Card 3: Habit streak consistency percentage.
   - Card 4: Screen time daily average vs quota.
4. **AI Synthesis Console Card**:
   - Style chips (`Executive Brief`, `Tactical`, `Deep Reflection`).
   - Custom prompt input field with placeholder "What would you like the AI to analyze?".
   - Primary action button: `[ ⚡ GENERATE WEEKLY BRIEFING ]` in Brand Teal with glow shadow.
5. **Executive Briefing Markdown Card**:
   - Structured typography with bold bullets, quotes, and action items.
   - Footer buttons: Copy to Clipboard and Export Markdown to Vault.

### State Matrix:
- **Default State**: HUD loaded with calculated telemetry from `sessions.jsonl`, `checklist.jsonl`, `events.jsonl`; cached briefing displayed.
- **Generating / Loading State**: Console button displays animated spinning vector indicator with text "Synthesizing week...", card pulses with subtle shimmer skeleton.
- **Empty Week State**: "No telemetry recorded for this week yet. Log focus blocks, tasks, or habits to generate your briefing."
- **AI Offline / API Error State**: Red border card: "Gemini API unavailable or offline. Showing local statistical rollup without AI commentary. [Retry]".

---

## 3. Plugin 2: Book Collection (`com.v07.reader`)

### Purpose & User Goal
Manage physical and digital book shelves, log daily reading sessions, record meaningful quotes, and search open literary databases.

```
┌────────────────────────────────────────────────────────┐
│  [📖] BOOK COLLECTION         [+ ADD BOOK] [🔍 SEARCH] │  <-- Header
│  Reading tracker, personal shelves & literary quotes   │
├────────────────────────────────────────────────────────┤
│  ( ALL · 12 )  ( READING · 2 )  ( TO READ )  ( DONE )  │  <-- Shelf Chips
│                                                        │
│  CURRENTLY IMMERSED                                    │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ┌──────┐  Quiet: The Power of Introverts         │  │  <-- Book Card
│  │ │ 📕   │  Susan Cain · 2012                      │  │
│  │ │ COVER│  ★★★★★ (5.0)                           │  │
│  │ └──────┘  Page 142 of 352  (40%)                 │  │
│  │ [██████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░]   │  │
│  │ [ -5p ]  [ +10p ]  [ 📝 Notes (4) ]  [ UPDATE → ] │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  ON YOUR SHELF                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ┌──────┐  Atomic Habits                          │  │
│  │ │ 📘   │  James Clear · [TO READ]                │  │
│  │ └──────┘  Added Sep 12 · 320 pages               │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Header**: Badge icon (`auto_stories`), title "BOOK COLLECTION", "+ ADD BOOK" button (manual entry), "SEARCH" button (Open Library database).
2. **Shelf Tab Bar**: Scrollable pills (`ALL`, `READING`, `TO READ`, `FINISHED`, `FAVORITES`). Active shelf shows count pill.
3. **Book Cards**:
   - Left: Book cover image (or procedural vector spine with author and title typography).
   - Middle: Book title (15px bold `#182230`), Author (12px `#5B6A78`), Star rating (1-5).
   - Reading Progress Bar: Dual-tone track (`#E8EFEB`) with mint-to-teal gradient fill, page numbers (`142 / 352`), and percentage.
   - Quick Page Increment Buttons: `[ -5p ]` and `[ +10p ]` for instant 1-tap logging.
4. **Book Detail & Quote Sheet Modal**:
   - Full cover art, synopsis, page counter slider, quotes/highlights list with "+ Add Highlight", and shelf selector.
5. **Open Library / Google Books Search Modal**:
   - Live query input, instant search result cards with cover thumbnail, title, author, and `[ + ADD TO SHELF ]` button.

### State Matrix:
- **Default State**: Shelf populated with books; currently reading book elevated at the top.
- **Empty Shelf State**: "No books in this shelf yet. Search open libraries to find your next great read." with large `[ 🔍 Search Books ]` CTA.
- **Searching State**: Loading skeletons in search modal; debounced API query.
- **Search Error / Offline State**: "Could not reach book database. You can still add books manually. [Add Manually]".

---

## 4. Plugin 3: Habits Matrix (`com.v07.habits`)

### Purpose & User Goal
Build unbroken daily habits using interactive checklist pills, a GitHub-style 20-week calendar heatmap, and streak analytics.

```
┌────────────────────────────────────────────────────────┐
│  [🔥] HABIT MATRIX            [+ NEW HABIT] [STREAK: 14│  <-- Header
│  Daily consistency engine & 20-week visual heatmap     │
├────────────────────────────────────────────────────────┤
│  TODAY'S HABITS · FRI, SEP 25                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ [✓] 🌿 Morning Meditation (10m)    [🔥 14 days]  │  │  <-- Habit Row 1
│  ├──────────────────────────────────────────────────┤  │
│  │ [✓] 💧 2L Clean Water              [🔥 21 days]  │  │  <-- Habit Row 2
│  ├──────────────────────────────────────────────────┤  │
│  │ [ ] 📖 Evening Reading (20p)       [🔥 6 days]   │  │  <-- Habit Row 3
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  20-WEEK CONSISTENCY HEATMAP                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │ M  ■ ■ ■ □ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■       │  │  <-- Heatmap Grid
│  │ W  ■ ■ □ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■       │  │
│  │ F  ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■       │  │
│  │ Less  □ ▨ ▩ ■  More     (88% total consistency)  │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  STREAK ANALYTICS                                      │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │
│  │ 14 Days       │ │ 28 Days       │ │ 184 Total     │ │
│  │ Current Streak│ │ Best Streak   │ │ Completions   │ │
│  └───────────────┘ └───────────────┘ └───────────────┘ │
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Header**: Fire badge (`local_fire_department`), "HABIT MATRIX", "+ NEW HABIT" button, active streak counter pill.
2. **Daily Habits Checklist**:
   - Checkbox: 24px circular pill. When tapped: immediate check animation with confetti/ripple, streak increments, event logged to `hbt`.
   - Habit Name + Color dot.
   - Streak Pill: Warm cedar container (`#FAF0EE`) with fire icon and bold days count.
3. **20-Week GitHub-Style Heatmap Calendar**:
   - Horizontal SVG grid of 7 rows (days of week) × 20 columns (weeks).
   - Color scale: 0 = `#E8EFEB`, 1-2 = `#C7E8DE`, 3-4 = `#66BFA8`, 5+ = `#2E7D6A`.
   - Hover / Tap Tooltip: Displays "Sep 22: 4 of 4 habits completed".
4. **Streak Analytics Cards**: 3-column KPI tiles for Current Streak, Best Streak, and All-Time Completions.
5. **New Habit Modal**: Name input, category selector, palette color picker (8 soothing pastel colors), and weekly target days.

### State Matrix:
- **Default State**: Checklist shows today's completion status; heatmap rendered from historical records.
- **Habit Checked State**: Row turns subtly muted, pill background turns teal, streak badge increments dynamically.
- **Empty State**: Friendly illustration: "No habits tracked yet. Start small: drink water, read 5 pages, or take a breath." with `[ + Create First Habit ]`.
- **Missed Day Warning**: If a streak is broken, soft encouragement appears: "Streaks reset, but momentum remains. Check off today to begin again."

---

## 5. Plugin 4: Tasks & Priorities (`com.v07.tasks`)

### Purpose & User Goal
Structured task management with daily progress bar, priority tagging (High, Medium, Low), deadlines, and Eisenhower Matrix grouping.

```
┌────────────────────────────────────────────────────────┐
│  [📝] TASKS & PRIORITIES                   [+ ADD TASK]│  <-- Header
│  Daily sprint progress: 75% completed (6 of 8)         │
├────────────────────────────────────────────────────────┤
│  [████████████████████████░░░░░░░░]  (6/8 Cleared)     │  <-- Progress Bar
│                                                        │
│  ( TODAY · 8 )  ( HIGH PRIORITY · 2 )  ( UPCOMING )    │  <-- Filters
│                                                        │
│  CRITICAL TODAY                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ [ ] Finalize Q4 editorial strategy  [🔴 HIGH]    │  │  <-- Task Item
│  │     Due Today 5:00 PM  •  [Work]  •  [ 🗑️ ]       │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ [✓] Send monthly reflection report  [🟡 MED]     │  │
│  │     Completed 10:30 AM  •  [Personal]            │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  GENTLE PRIORITIES                                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │ [ ] Water indoor garden             [🔵 LOW]     │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  [ + Add a task...                             [+] ]   │  <-- Quick Add Bar
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Header & Sprint Progress Bar**:
   - Header title, sprint completion percentage, 10px progress bar with teal gradient fill.
2. **Filter Chips**: `TODAY`, `HIGH PRIORITY`, `UPCOMING`, `ALL`, `ARCHIVE`.
3. **Priority-Grouped Task Lists**:
   - High Priority (Crimson badge `[🔴 HIGH]`), Medium Priority (Amber badge `[🟡 MED]`), Low Priority (Slate badge `[🔵 LOW]`).
   - Checkbox with tactile strikethrough animation.
   - Due date chip (turns Cedar Red if overdue).
4. **Quick Add Bar**: Floating or bottom-pinned input with priority selector dropdown and submit button.

### State Matrix:
- **Default State**: Populated lists grouped by priority; completed tasks collapsed at bottom.
- **Task Cleared State**: Task row crosses out, moves smoothly to completed section, progress bar advances.
- **All Complete State**: Celebratory card appears: "All tasks cleared for today ✨ Time to rest."
- **Empty State**: "No open tasks. Enjoy the quiet spaciousness or plan ahead."

---

## 6. Plugin 5: Gamify (`com.v07.gamify`)

### Purpose & User Goal
Turns mindful living, journaling, reading, and habit consistency into a calming RPG experience with XP, levels, daily quests, and badges.

```
┌────────────────────────────────────────────────────────┐
│  [⭐] LEVEL 7: SOVEREIGN ARCHITECT        [XP: 2,450]  │  <-- Player Header
│  2,450 / 3,000 XP to Level 8  (550 XP remaining)       │
│  [████─────────────────────────────────────────────]   │
├────────────────────────────────────────────────────────┤
│  DAILY STREAK BONUS: 🔥 14 DAYS (x1.4 XP Multiplier)   │  <-- Multiplier Pill
│                                                        │
│  DAILY QUESTS                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ [✓] Clear 2 Focus Sessions          [+50 XP] [CLAIM]│  <-- Quest 1
│  ├──────────────────────────────────────────────────┤  │
│  │ [✓] Read 20 Book Pages              [+30 XP] [CLAIM]│  <-- Quest 2
│  ├──────────────────────────────────────────────────┤  │
│  │ [ ] Complete All Habits Today       [+100 XP] [ 0/3]│  <-- Quest 3
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  ACHIEVEMENT MEDALS                                    │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │
│  │ 🏅 7-Day Flame│ │ 📚 Bookworm I │ │ 🧘 Zen Master │ │  <-- Medals Grid
│  │ UNLOCKED      │ │ UNLOCKED      │ │ LOCKED (2/5)  │ │
│  └───────────────┘ └───────────────┘ └───────────────┘ │
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Player Level Card**: Avatar with glowing rank frame, Level number ("LEVEL 7"), Title ("Sovereign Architect"), XP progress bar.
2. **Streak Multiplier Banner**: Amber pill with active multiplier (`x1.4 XP Boost`).
3. **Daily Quests List**: Quests tied to real actions (focus timer, reading, tasks, habits). Claim button triggers XP particle burst.
4. **Badges & Medals Deck**: 3-column grid of circular vector badges with unlocked (colored) vs locked (monochrome opacity 0.4) states.

### State Matrix:
- **Default State**: Live XP bar, daily quest progress synced with today's event log.
- **XP Claimed State**: Button pulses green, "+50 XP" floats up and fades, progress bar fills smoothly.
- **Level-Up State**: Fullscreen overlay celebration modal: "LEVEL UP! You are now Level 8: Serene Sage." with confetti.
- **All Quests Complete State**: "Perfect Day bonus unlocked! +150 XP bonus awarded."

---

## 7. Plugin 6: Time Tracker & Milestones (`com.v07.timetracker`)

### Purpose & User Goal
Track important milestone countdowns, event timers, project deadlines, and live focus stopwatches.

```
┌────────────────────────────────────────────────────────┐
│  [⏱️] TIME TRACKER                   [+ NEW MILESTONE] │  <-- Header
│  Milestone countdowns, deadlines & focus stopwatches   │
├────────────────────────────────────────────────────────┤
│  ACTIVE COUNTDOWNS                                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🚀 Product V3 Release Launch        [IN 14 DAYS] │  │  <-- Milestone Card
│  │ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐          │  │
│  │ │  14   │ │  08   │ │  32   │ │  15   │          │  │
│  │ │ DAYS  │ │ HOURS │ │ MINS  │ │ SECS  │          │  │
│  │ └───────┘ └───────┘ └───────┘ └───────┘          │  │
│  │ Target: Oct 09, 2026  •  [Work]  •  [ ✏️ ]       │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🎂 Sarah's Birthday                 [IN 42 DAYS] │  │
│  │ 42 Days · 16 Hours remaining                     │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Header**: Clock badge (`schedule`), title "TIME TRACKER", "+ NEW MILESTONE" button.
2. **Milestone Countdown Cards**:
   - Hero title + Category tag.
   - 4 Digit Boxes: Days, Hours, Minutes, Seconds with real-time 1-second interval ticker.
   - Target date metadata and progress toward date.
3. **Add Milestone Modal**: Title input, target date & time picker, category selector, color badge picker.

### State Matrix:
- **Default State**: Active tickers updating every second in tabular monospace font.
- **Milestone Reached / Due Today State**: Card glows with celebratory Cedar Light border: "TODAY IS THE DAY! 🎉".
- **Passed Event State**: Card moves to past events section with "Passed 3 days ago" pill.
- **Empty State**: "No countdowns tracked. Add a deadline, birthday, launch, or vacation."

---

## 8. Plugin 7: Mood & Energy Vitals (`com.v07.vitals`)

### Purpose & User Goal
Mindful daily logging of emotional mood, physical energy, sleep duration, and wellness vitals.

```
┌────────────────────────────────────────────────────────┐
│  [⚡] MOOD & ENERGY VITALS                [HISTORY →]  │  <-- Header
│  Daily physical energy, mental clarity & sleep tracking│
├────────────────────────────────────────────────────────┤
│  HOW ARE YOU FEELING RIGHT NOW?                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ MOOD:  ( 🌿 Calm )  ( ✨ Inspired )  ( ☕ Focused )│  │  <-- Mood Selector
│  │        ( 🌧️ Melancholy )  ( ⚡ Restless )         │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ ENERGY LEVEL:                                    │  │  <-- Energy Battery
│  │ [ 10% ] [ 30% ] [ 50% ] [ 75% ] [ 100% PEAK ]    │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ [ 💭 Add a gentle note or observation...       ]│  │
│  │ [ LOG VITALS NOW                                ]│  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  LAST NIGHT'S SLEEP                                    │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🌙 7h 45m  (11:15 PM → 07:00 AM)    [RESTFUL]    │  │  <-- Sleep Card
│  │ Quality: ★★★★☆ (Good recovery)                  │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  7-DAY ENERGY & MOOD CORRELATION                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Energy: 75% avg  •  Peak days: Tuesday & Friday   │  │  <-- Chart
│  │ [░░░░████████████████████████░░░░░░░░░░░░░░░░░░]   │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Components & Layout:
1. **Mood Selector**: Tactile chip row with emotional state labels and subtle emoji indicators. Active selection gains Brand Teal outline.
2. **Energy Battery Level**: 5 segment buttons (Depleted, Low, Moderate, High, Peak).
3. **Sleep Logger**: Bedtime and wake time inputs, automated duration calculation, quality star rating.
4. **Correlation Graph**: 7-day visual sparkline linking focus hours to energy ratings.

### State Matrix:
- **Default State**: Quick-log form ready; shows today's logged state if already completed.
- **Logged State**: Displays "Vitals recorded at 9:30 AM" with an option to "Update Log".
- **Extreme Fatigue Alert**: If Energy is logged as "Depleted", a gentle suggestion appears: "Low energy detected. Consider scheduling a quiet focus block or a restorative walk."
