# V3 Functional Inventory

This document provides a comprehensive inventory of all functional capabilities discovered in the V1/V2 reference implementations (Obsidian Vault, Python REST API, Telegram/Discord Bots, and V07-Android).

## Classification Scheme

- **KEEP**: Valid domain feature whose business logic and purpose should be retained, with minimal API cleanup.
- **REIMPLEMENT**: Necessary capability whose current code is deeply flawed, entangled, or low-quality, requiring a clean-room implementation on V3 foundations.
- **REDESIGN**: Feature whose user workflow, UX model, or architecture is fundamentally suboptimal and must be reimagined for V3.
- **DEPRECATE**: Legacy or redundant feature that adds maintenance overhead without serving V3 product goals.
- **UNCERTAIN**: Feature requiring stakeholder clarification on current usage and future relevance.

---

## 1. Core Telemetry & Event Logging

### Feature: Daily Event Stream & Quick Logging
- **Purpose**: Fast, low-friction recording of daily micro-events (Check-in, Check-out, Mood, Energy, Win, Panic, Doomscrolling, Pushups, Think, Journal, Freeform Notes).
- **Current Implementation**:
  - Web: Floating action buttons, modal dialogs, and timeline lists in `index.html` (~lines 2600–3200).
  - Android: `EventStore.kt` appends JSON objects to category-specific `.jsonl` files in `08 - System/Data/events/`.
  - Python REST: `POST /log/{category}` in `08 - System/REST API/api.py`.
  - Chat Bots: Telegram and Discord bot slash commands (`/checkin`, `/mood`, etc.).
- **User Workflow**: User opens app or triggers a shortcut -> selects event type -> enters optional value/note -> submits -> event appears in daily stream.
- **Dependencies**: Event storage, local clock, filesystem permissions.
- **Data Involved**: Timestamp (`ts`), date (`YYYY-MM-DD`), category (`cat`), value (`val`), text/note (`text`), metadata (`meta`).
- **APIs Involved**: `POST /api/events`, `GET /api/events/today`, `POST /log/{cat}`.
- **Plugin Relationship**: Emits platform events that plugins can observe if granted `events:read` permission.
- **Known Problems**: Inconsistent schemas across categories; concurrent file append race conditions in Android and Python; duration units confused (seconds vs minutes); zero offline-to-cloud synchronization guarantees.
- **V3 Recommendation**: **REIMPLEMENT**. Standardize on a unified, strongly typed Event schema backed by SQLite with an append-only WAL log, clean REST/IPC APIs, and offline queueing.

### Feature: Daily Check-in & Check-out Routine
- **Purpose**: Anchor the start and end of the productive day with an intentional mindset checkpoint.
- **Current Implementation**: Embedded buttons in `index.html` and REST `/log/checkin`, `/log/checkout` writing to `events.jsonl`.
- **User Workflow**: Morning: tap Check-in -> prompt for initial mood/intention -> Day opens. Evening: tap Check-out -> prompt for reflection/wins -> Day closes.
- **Dependencies**: Event stream, daily aggregator.
- **Data Involved**: Check-in timestamp, morning reflection, check-out timestamp, evening review.
- **APIs Involved**: `POST /api/checkin`, `POST /api/checkout`.
- **Plugin Relationship**: Triggers day-lifecycle events (`day:start`, `day:end`).
- **Known Problems**: Fragmented across UI components; no state machine ensuring check-in precedes check-out; easy to double check-in.
- **V3 Recommendation**: **REDESIGN**. Formalize into a first-class Day Lifecycle state machine with dedicated morning/evening guided flow components in the V3 shell.

---

## 2. Attention & Screen Time Telemetry

### Feature: Foreground Application Usage Tracking
- **Purpose**: Passive telemetry tracking which apps are used on the device, calculating screen time, categorize usage (Productive vs Distracting), and enforcing limits.
- **Current Implementation**:
  - Android: `ScreenTimeTracker.kt` and `V07AccessibilityService.kt` polling `UsageStatsManager` and accessibility window events.
  - Storage: `screentime.jsonl` recording session intervals `[startTime, endTime, packageName, appName]`.
  - UI: `index.html` renders screen time bar charts, stat cards, and paginated session logs.
- **User Workflow**: Passive background collection -> User reviews daily/weekly screen time HUD -> User configures app budget limits.
- **Dependencies**: Android `PACKAGE_USAGE_STATS` permission, `BIND_ACCESSIBILITY_SERVICE`.
- **Data Involved**: Package name, human-readable app label, start time, end time, duration ms, category (Productive, Neutral, Distracting).
- **APIs Involved**: `GET /api/screentime`, `GET /api/screentime/sessions`, `POST /api/screentime/limits`.
- **Plugin Relationship**: Core platform service. Plugins can request aggregated screen time metrics via `V07Platform.telemetry.getScreenTime()`.
- **Known Problems**: High battery consumption if accessibility service is unthrottled; OEM battery killers terminating background services; package names changing or unmapped.
- **V3 Recommendation**: **KEEP** with architectural hardening. Extract into an isolated native platform telemetry module (`core-platform-android`) behind an abstract `TelemetryService` interface.

### Feature: Distraction Blocker & App Limits
- **Purpose**: Warn or block the user when an app's daily usage budget is exceeded.
- **Current Implementation**: `V07AccessibilityService.kt` monitors foreground window package; triggers system overlay warning or launches home screen if limit exceeded.
- **User Workflow**: User sets limit (e.g. YouTube 45m/day) -> Threshold reached -> Overlay blocker displays remaining budget -> App minimized.
- **Dependencies**: Accessibility service, system alert window permission.
- **Data Involved**: Daily limit minutes per package, accumulated minutes today.
- **APIs Involved**: Android native shared preferences and bridge calls.
- **Plugin Relationship**: Internal core policy.
- **Known Problems**: Can be bypassed easily; overlay permissions vary across Android versions; no graceful snooze/friction options.
- **V3 Recommendation**: **REDESIGN**. Implement modern digital-wellbeing friction patterns (e.g. 60-second breathing pause, intent reflection prompt) rather than harsh abrupt kills.

---

## 3. Focus & Deep Work Timer

### Feature: Focus Stopwatch & Activity Session Recorder
- **Purpose**: Intentional time tracking for deep work, coding, reading, and study sessions.
- **Current Implementation**:
  - `index.html` countdown/stopwatch logic.
  - Android `V07OverlayService.kt` floating timer bubble.
  - Python REST `/session/start`, `/session/stop`, `/session/pause`.
  - Storage in `sessions.jsonl`.
- **User Workflow**: Select task/activity -> tap Start -> timer runs in notification & overlay -> pause/resume as needed -> tap Stop -> enter session notes -> XP awarded.
- **Dependencies**: Foreground service, notification manager, event store.
- **Data Involved**: Session ID, activity tag, start timestamp, end timestamp, elapsed seconds, pause intervals, completion note.
- **APIs Involved**: `POST /api/sessions/start`, `POST /api/sessions/stop`, `GET /api/sessions/active`.
- **Plugin Relationship**: Managed by `com.v07.timetracker` plugin.
- **Known Problems**: Split logic between web JavaScript timer, Android service notification timer, and floating overlay timer; pauses in JS can desynchronize if Android app is killed in background.
- **V3 Recommendation**: **REIMPLEMENT**. Single source of truth in backend/native service timer engine; web UI and overlay are pure reactive displays observing timer state streams.

---

## 4. Habits & Consistency Matrix

### Feature: Daily Recurring Habit Tracker & Streaks
- **Purpose**: Build long-term routines (e.g., Meditation, Reading, Hydration, Workout) with streak tracking.
- **Current Implementation**:
  - Plugin `com.v07.habits` with `widget.html`, `index.html`, `plugin.js`.
  - Stored in `08 - System/Data/events/habits.jsonl` (and `hbt.jsonl`).
  - Streak and completion calculations done client-side inside plugin iframe.
- **User Workflow**: View daily habit checklist -> tap habit pill/circle to toggle completion -> streak increments -> weekly matrix updates.
- **Dependencies**: Storage, date calculation utilities.
- **Data Involved**: Habit definition (ID, title, icon, frequency, target), logs (habitId, date, completed, timestamp).
- **APIs Involved**: `GET /api/plugins/com.v07.habits/data`, `POST /api/plugins/com.v07.habits/toggle`.
- **Plugin Relationship**: Built-in starter plugin.
- **Known Problems**: Previously had method naming discrepancies (`getHabits` vs `habits`); duplicated streak math in `WeeklyAggregator.kt` and `plugin.js`; storage category dual-writing (`hbt` vs `habits`).
- **V3 Recommendation**: **REIMPLEMENT** as clean V3 plugin conforming to the standard V3 plugin manifest and storage contract.

---

## 5. Tasks & Execution Discipline

### Feature: Priority Task Manager
- **Purpose**: Manage operational daily tasks with priority tags, status transitions, and momentum indicators.
- **Current Implementation**:
  - Plugin `com.v07.tasks` with `index.html`, `widget.html`, `plugin.js`.
  - Stored in `tasks.jsonl`.
  - Android Launcher Widget integration in `V07DashboardWidget.kt`.
- **User Workflow**: Input task text -> select priority (High/Medium/Low) -> submit -> check off task -> strike-through and done count increment.
- **Dependencies**: Storage, date formatting.
- **Data Involved**: Task ID, title, priority, status (`pending`, `completed`, `archived`), created date, completed date.
- **APIs Involved**: Plugin event storage.
- **Plugin Relationship**: Built-in starter plugin.
- **Known Problems**: No recurrence rules; no sub-tasks or project grouping; no sync with Obsidian markdown task checkboxes (`- [ ]`).
- **V3 Recommendation**: **REDESIGN**. Enhance with project groupings, clean priority sorting, bidirectional Obsidian vault Markdown task sync capability, and offline optimistic updates.

---

## 6. Book Reader & Knowledge Engine

### Feature: EPUB & Text Reader with Reading Telemetry
- **Purpose**: Distraction-free book reader that logs reading minutes, pages read, active shelf position, and connects to Google Books for metadata.
- **Current Implementation**:
  - Plugin `com.v07.reader`.
  - Web client with ePub.js / raw text renderer in `app/src/main/assets/plugins/com.v07.reader/`.
  - Stored in `08 - System/Data/events/reader.jsonl`.
- **User Workflow**: Browse library -> select book -> read chapters with bookmarking -> session automatically records reading time and pages -> update progress in dashboard.
- **Dependencies**: ePub parser, Google Books API, local file storage.
- **Data Involved**: Book metadata (ISBN, title, author, total pages), reading logs (bookId, timestamp, duration, startPage, endPage).
- **APIs Involved**: Google Books Search API, local file storage bridge.
- **Plugin Relationship**: Built-in starter plugin.
- **Known Problems**: URL entity encoding bugs (`%20`, `%27`, broken tags in search results); ePub rendering memory leaks on large files; fragmented storage between local storage and vault JSONL.
- **V3 Recommendation**: **REDESIGN**. Lightweight, robust reader with strict sanitization, decoupled ePub/PDF/Markdown adapters, and clean telemetry emission.

---

## 7. Gamification & Progression

### Feature: XP, Leveling, Ranks & Quests
- **Purpose**: Provide dopamine rewards and structured progression for real-world productivity (tasks completed, focus hours, habits done, runs logged).
- **Current Implementation**:
  - Plugin `com.v07.gamify`.
  - Hardcoded formulas for XP thresholds and rank badges (Novice, Apprentice, Veteran, Centurion, Master).
  - Stored in `gamify.jsonl`.
- **User Workflow**: Complete actions across system -> XP toast appears -> Level progress bar animates -> Ranks unlock.
- **Dependencies**: Event bus listening to all task, habit, and athletics completions.
- **Data Involved**: Total XP, current level, active rank, quest list, achievement badges.
- **APIs Involved**: Plugin event listener.
- **Plugin Relationship**: Built-in starter plugin listening to system event streams.
- **Known Problems**: Tightly coupled directly into core dashboard headers; XP formulas duplicated across `gamify/plugin.js`, `WeeklyAggregator.kt`, and `V07DashboardWidget.kt`.
- **V3 Recommendation**: **REIMPLEMENT**. Pure plugin consuming standard platform event bus events (`task:completed`, `habit:completed`, `run:logged`) via documented extension points.

---

## 8. Neurodynamics & Vitals (Mood & Energy)

### Feature: Mood & Energy Equilibrium Logging
- **Purpose**: Track psychological and physical energy states throughout the day to detect burnout or high-flow patterns.
- **Current Implementation**:
  - Plugin `com.v07.vitals` and legacy REST `/log/mood`, `/log/energy`.
  - 1-5 ratings for mood and energy, contextual notes.
  - Stored in `vitals.jsonl` (and `events.jsonl`).
- **User Workflow**: Open vitals widget/page -> tap mood score (1-5) and energy score (1-5) -> enter note -> timeline equilibrium calculated.
- **Dependencies**: Storage, date aggregators.
- **Data Involved**: Mood rating (1-5), energy rating (1-5), tag, notes, timestamp.
- **APIs Involved**: Event store.
- **Plugin Relationship**: Built-in starter plugin.
- **Known Problems**: Disconnected from sleep and nutrition; two places where mood is recorded (core checkin modal vs vitals plugin).
- **V3 Recommendation**: **REIMPLEMENT** as unified Vitals & Wellbeing plugin with standardized circadian tracking.

---

## 9. Athletics & Physical Performance

### Feature: Running & Workout Tracker
- **Purpose**: Record running distance, duration, pace, cadence, and pushup counts.
- **Current Implementation**:
  - Native Android `RunInputActivity.kt`.
  - Stored in `running.jsonl` and `pushups.jsonl`.
  - Displayed in timeline, weekly aggregator, and Android launcher widget.
- **User Workflow**: Tap Run tile -> enter distance (km) and time -> pace is calculated -> saved to event stream.
- **Dependencies**: Storage, math utilities.
- **Data Involved**: Distance km, duration sec/min, pace sec/km, pushup reps, notes.
- **APIs Involved**: `POST /api/athletics/run`, `POST /api/athletics/pushups`.
- **Plugin Relationship**: Core feature in V2, candidate for athletics plugin in V3.
- **Known Problems**: Unit mismatch bugs (seconds stored in minute fields, inverted pace checks) historically plagued V2; manual entry only (no GPS/Strava integration).
- **V3 Recommendation**: **REIMPLEMENT**. Standardize internal unit of duration strictly as ISO 8601 or integer milliseconds. Package as an Athletics plugin.

---

## 10. Wisdom & Daily Gita

### Feature: Daily Verse & Philosophical Reflection
- **Purpose**: Daily spiritual grounding with selected verses from Bhagavad Gita.
- **Current Implementation**:
  - `gita.py` in REST API; hardcoded verse tables; `/log/verse` endpoints.
- **User Workflow**: Morning notification displays daily verse -> tap to read translation -> mark as reflected.
- **Dependencies**: Text asset or API, notification manager.
- **Data Involved**: Chapter number, verse number, Sanskrit text, English translation, reflection note.
- **APIs Involved**: `GET /api/gita/daily`.
- **Plugin Relationship**: Plugin candidate.
- **Known Problems**: Hardcoded in Python backend; missing from Android native app UI except as legacy endpoints.
- **V3 Recommendation**: **REIMPLEMENT** as a standalone wisdom/quote plugin distributed via the plugin platform.

---

## 11. Multi-Metric Synthesis & AI Copilot

### Feature: Weekly Digest & AI Retrospective
- **Purpose**: Aggregate all telemetry across 7 days into cohesive multi-metric charts and feed them to an LLM for actionable coaching and summaries.
- **Current Implementation**:
  - `WeeklyAggregator.kt` in Android; `chronosynth` plugin; AI integration bridge in `index.html` (~lines 6200–6500) calling Gemini / Claude / OpenAI.
- **User Workflow**: User opens Weekly Digest -> system calculates stats (focus hrs, tasks done, habit consistency %, screen time, avg mood) -> User taps "Generate AI Analysis" -> Copilot provides deep retrospective.
- **Dependencies**: Multi-model AI API keys, network access, aggregator engine.
- **Data Involved**: Aggregated metrics JSON, user goals, prompt templates, AI output markdown.
- **APIs Involved**: `GET /api/weekly/summary`, external LLM endpoints.
- **Plugin Relationship**: `com.v07.chronosynth` plugin + core AI provider.
- **Known Problems**: Direct browser/WebView calls to AI APIs using client-stored keys without rate-limiting or prompt caching; giant prompt strings concatenated in JS; brittle JSON parsing.
- **V3 Recommendation**: **REDESIGN**. Platform-level Secure AI Gateway service with structured schema outputs, model agnostic adapters, and robust local caching.

---

## 12. System Integration & Peripheral Access

### Feature: Android Home Screen AppWidgets
- **Purpose**: Glanceable home-screen widgets for at-a-glance visibility without launching the app.
- **Current Implementation**:
  - `V07DashboardWidget.kt`, `V07WidgetConfigActivity.kt`, `V07WidgetActionReceiver.kt`.
  - Vector art layout with 4 stat tiles, module switcher, and live previews.
- **User Workflow**: Long-press home screen -> add V07 widget -> configure module (Tasks, Habits, Focus, etc.) -> widget updates automatically via broadcast receivers.
- **Dependencies**: Android AppWidget framework, RemoteViews.
- **Data Involved**: Widget preferences, aggregated module metrics.
- **APIs Involved**: Android native AppWidgetManager.
- **Plugin Relationship**: Core platform hosts widgets; plugins supply widget data via platform contract.
- **Known Problems**: RemoteViews limitations prevent dynamic custom views without native code; plugins cannot register arbitrary native widgets without core modifying its manifest.
- **V3 Recommendation**: **REDESIGN**. Core provides a declarative widget template engine (Cards, Metrics, Quick Actions) that plugins configure declaratively in their manifests.

### Feature: Android Floating Telemetry Bubble (Overlay)
- **Purpose**: Persistent on-screen bubble showing active timer, quick log shortcuts, and screen time alerts while using other apps.
- **Current Implementation**: `V07OverlayService.kt` using `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- **User Workflow**: Toggle floating bubble in settings -> movable bubble appears -> tap to expand mini dashboard -> long-press to hide.
- **Dependencies**: `SYSTEM_ALERT_WINDOW` permission.
- **Data Involved**: Active session state, quick log categories.
- **APIs Involved**: Native Android service.
- **Plugin Relationship**: Core service.
- **Known Problems**: Clutters user's screen; requires invasive permission; fragile touch handling on modern Android gesture navigation.
- **V3 Recommendation**: **KEEP** as optional power-user feature, but isolate completely into an optional Android platform extension.

### Feature: Companion Web Portal (HTTP on Port 8080)
- **Purpose**: Access V07 dashboard from PC or tablet over local Wi-Fi without installing software.
- **Current Implementation**: `companion.html` served by `V07Server.kt` on port 8080.
- **User Workflow**: Open `http://<phone-ip>:8080` on laptop browser -> view live dashboard -> log events.
- **Dependencies**: NanoHTTPD server, local network connectivity.
- **Data Involved**: Full dashboard read/write.
- **APIs Involved**: Local REST API.
- **Plugin Relationship**: Mirrors core UI.
- **Known Problems**: No authentication on local network; security risk on public Wi-Fi; duplicates HTML/JS code from `index.html`.
- **V3 Recommendation**: **REDESIGN**. V3 web application should be identical across mobile and desktop via responsive design; served over local HTTPS or token-authenticated local API.

### Feature: Telegram & Discord Remote Bots
- **Purpose**: Remote event logging and status checking via messaging apps.
- **Current Implementation**: Python scripts in `08 - System/Telegram Bot` and `08 - System/Discord Bot`.
- **User Workflow**: Send message to bot (`/log mood 4`) -> bot appends to JSONL file in synced Obsidian vault.
- **Dependencies**: Bot tokens, python-telegram-bot, discord.py, synced filesystem.
- **Data Involved**: Chat message parsing, event generation.
- **APIs Involved**: Telegram/Discord bot APIs, local file I/O.
- **Plugin Relationship**: External adapters.
- **Known Problems**: Bypasses Android app completely; directly mutates synced files causing sync conflicts (`.sync-conflict-...` files found in vault); duplicate parsing logic.
- **V3 Recommendation**: **DEPRECATE** in core. Provide a documented V3 Webhook Ingestion API that external bot scripts can call cleanly.

---

## Summary Matrix

| Feature | Category | Current Status | V3 Classification | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| Daily Event Stream | Core Telemetry | Monolithic JS + `.jsonl` | **REIMPLEMENT** | Unify schema and migrate to SQLite WAL engine |
| Check-in / Check-out | Daily Lifecycle | Split buttons & modals | **REDESIGN** | Model as explicit Day Lifecycle state machine |
| Screen Time Tracker | Telemetry | Native Accessibility/Usage | **KEEP** | Essential domain feature; isolate in platform module |
| App Limits & Blocker | Attention Mgmt | Native abrupt kill | **REDESIGN** | Modern intentional friction rather than crash-kills |
| Focus Session Timer | Time Tracking | Split JS + Service + Bubble | **REIMPLEMENT** | Single background service source of truth |
| Habits Matrix | Routines | Plugin + duplicate math | **REIMPLEMENT** | Standardize on V3 plugin contract |
| Priority Tasks | Execution | Plugin | **REDESIGN** | Add project groupings and bidirectional Markdown sync |
| Book Reader | Knowledge | Plugin + ePub.js | **REDESIGN** | Sanitize inputs, decouple reader engine, clean telemetry |
| Gamification | Progression | Plugin + hardcoded core HUD | **REIMPLEMENT** | Decouple from core shell; pure event consumer |
| Mood & Energy Vitals | Wellness | Plugin + duplicate checkin | **REIMPLEMENT** | Unify wellness tracking |
| Athletics & Running | Fitness | Native activity + units bug | **REIMPLEMENT** | Clean time units, convert to standard Athletics plugin |
| Daily Gita Verse | Wisdom | Legacy Python scripts | **REIMPLEMENT** | Convert to Wisdom plugin |
| Weekly Digest & AI | Synthesis | Client JS LLM calls | **REDESIGN** | Secure, cached AI Gateway architecture |
| Home Screen Widgets | Android Integration | Native AppWidget RemoteViews | **REDESIGN** | Declarative widget schema for plugins |
| Floating Bubble Overlay | Android Integration | Native WindowManager | **KEEP** | Optional power-user tool; isolate in Android module |
| Companion Web Portal | Cross-Device | Duplicate `companion.html` | **REDESIGN** | Unified responsive PWA served by core |
| Telegram/Discord Bots | Remote Access | Python scripts mutating files | **DEPRECATE** | Replace with standard authenticated Webhook API |
