# V3 UX Principles

## 1. Product Mission & Philosophy

V3 is a personal operating system designed to elevate self-mastery, attention discipline, and deep work. The interface must act as an invisible, tactile instrument—not an attention-seeking distraction.

Every screen and interaction in V3 is governed by five foundational UX tenets:

### Tenet 1: Frictionless Micro-Capture (Sub-2-Second Rule)
Any recurring daily action (logging mood, energy, checking off a habit, starting a timer, or recording a quick win) must be accomplishable in **under 2 seconds and 2 taps**. The UI must never force the user through deep modal labyrinths or sluggish transitions for routine telemetry.

### Tenet 2: Zero "AI Slop" & High Information Density
Avoid generic, decorative filler: no purple gradients, no bubbly floating pill buttons, no empty rounded cards with huge wasted margins, and no unformatted raw text dumps. Prefer crisp, tactile, high-density vector geometry, monospace typography for telemetry, and purposeful color signals.

### Tenet 3: Ambient Glanceability
Status should be understood at a glance. The user should glance at the home screen widget or the main dashboard HUD and immediately know:
- Day rhythm state (Checked-in vs Checked-out)
- Focus state (Active timer vs Idle)
- Habit consistency (Today's remaining habits vs streak unbroken)
- Attention load (Screen time vs daily budget)

### Tenet 4: Intentional Friction for Distraction
While productive logging must have near-zero friction, destructive or distracting behaviors (e.g. doomscrolling, breaking an app limit) must encounter **intentional, humane friction** (a 60-second mindfulness pause or reflection prompt) rather than harsh, disruptive application crashes.

### Tenet 5: Resilient Offline First & Optimistic Updates
The application must feel instantaneous regardless of network state. All UI state mutations are applied optimistically, with immediate haptic/visual feedback. If an operation fails, the system rolls back cleanly with a clear, non-intrusive recovery path.

---

## 2. Interaction Design & Feedback States

Every user-facing feature in V3 must provide explicit handling for all seven fundamental interface states:

1. **Initial / First-Use State**:
   - Clean, instructional placeholders. Never show an empty black box or broken undefined labels.
   - Example: Habits empty state prompts "No habits defined yet. Tap [+] to add your first daily anchor."

2. **Loading State**:
   - Skeleton layouts using subtle pulse animations matching exact card dimensions. Avoid jarring full-screen spinner wheels.

3. **Active / Success State**:
   - Immediate tactile visual response (e.g., custom SVG checkmark draw-in, subtle 1.02x scale bounce, crisp high-contrast border glow).
   - Dismissible, non-blocking toast notifications.

4. **Error State**:
   - Clear, human-readable error descriptions. Never display raw Java stack traces, HTTP 500 codes, or `[object Object]`.
   - Provide an actionable recovery button (e.g. `[RETRY]` or `[EDIT]`).

5. **Disabled State**:
   - 40% opacity, desaturated borders, distinct cursor/touch feedback, with clear contextual tooltip or status explaining *why* it is disabled.

6. **Offline / Unconnected State**:
   - Clear offline indicator chip in header HUD. All local actions continue to queue and execute smoothly in local SQLite storage.

7. **Destructive Action Confirmation**:
   - Two-step confirmation for irreversible actions (e.g. deleting a habit streak or resetting logs), with an undo buffer where practical.

---

## 3. Ergonomics & Mobile-First Hierarchy

### Thumb-Zone Ergonomics
- **Bottom Navigation & Actions**: Primary triggers, quick logging floating menus, and tabs sit within natural thumb reach on 6.5"+ Android displays.
- **Top Glance HUD**: Read-only metrics (level, XP, day date, battery/connectivity indicators) stay pinned at the top where eyes naturally land without finger obstruction.

### Haptic Design
- Subtle 10–15ms haptic tick on habit toggle, task completion, and stopwatch start/stop.
- Distinct double-tick haptic pattern on milestone unlocks and rank upgrades.

---

## 4. Accessibility & Readability

- **Contrast Ratios**: All text elements adhere strictly to WCAG AA (minimum 4.5:1 for body copy, 7:1 for numeric stat metrics).
- **Zero Text Truncation Bugs**: Dynamic font scaling safeguards against line breaks or ugly overflow; labels truncate cleanly with ellipsis (`text-overflow: ellipsis`) and full text viewable on press.
- **Touch Target Minimums**: Every interactive touch target has a minimum tap area of 44x44 dp, even if the visual icon is smaller (e.g. 18x18 dp with 14 dp transparent padding).
