# V07 V3: Product Identity & Truth (PRODUCT.md)

> **Durable source of truth for V07 V3: audience, goals, voice, constraints, and frontend discipline.**  
> *Initialized via `/impeccable init` according to the Impeccable frontend discipline and AGENTS.md rules.*

---

## 1. Product Identity & Core Mission

**V07 V3** is a deliberate, calm reconstruction of the personal life-logging platform. It rejects the hyperactive mechanics of enterprise productivity consoles, noisy dashboards, neon avionics HUDs, and anxiety-inducing notification feeds in favor of an **intimate, tactile, unhurried digital sanctuary**.

Guided by the tactile metaphor of **opening a finely bound personal notebook**—*Warm Paper in daylight, and Night Ink after sunset*—V07 V3 provides an unhurried space for:
- Writing quiet daily reflections and journaling without algorithmic feeds
- Tracking contemplative reading journeys (Kindle & physical shelf sync)
- Cultivating gentle daily rituals and habits without guilt-tripping streaks
- Setting uninterrupted intervals for deep focus and mindful presence
- Reviewing daily time and digital stillness with serene pace trajectories
- Contemplating timeless wisdom (e.g. Bhagavad Gita daily shlokas)
- Retaining 100% offline sovereignty over personal data and reflections

---

## 2. Target Audience & Personas

1. **The Deep Thinker & Writer**  
   Desires an unadorned, distraction-free writing plane. Values tactile typography, natural sentence-case phrasing, lined notebook textures, and zero cognitive clutter.
   
2. **The Mindful Reader & Student**  
   Tracks book progress gently across chapters and pages without social comparison or gamified leaderboards. Seeks tranquil reflection on excerpts and quotes.
   
3. **The Digital Minimalist**  
   Fatigued by infinite scroll apps, constant pings, and intrusive notifications. Seeks intentional digital stillness, healthy screen time boundaries, and grounding rituals.
   
4. **The Privacy-Conscious Sovereign User**  
   Demands complete ownership of thoughts. Rejects cloud subscriptions and remote parsing. Expects data to remain securely on-device in transparent, open markdown/SQLite formats.

---

## 3. Product Goals & Core Outcomes

| Goal | Success Criterion |
|---|---|
| **Serene Experience** | Zero jarring animations, zero loud alerts. Warm ambient transitions mimic turning heavy cardstock paper. |
| **Strict Design Fidelity** | 100% adherence to Google Stitch design systems for both **Warm Paper (Light)** and **Night Ink (Dark)** modes. |
| **Complete Data Sovereignty** | All events, sessions, habits, and journal entries are stored locally on-device in SQLite and accessible via the user's chosen local storage path. |
| **Zero Broken Interfaces** | Absolute enforcement of Impeccable principles: no text overflow, no HTML entity leaks, 44px+ touch targets, and inline SVG fallbacks. |
| **Extensible Platform** | Core host and authenticated loopback server provide a predictable, secure plugin platform for companion tools. |

---

## 4. Voice, Tone & Emotional Signature

- **Cadence:** Unhurried, grounding, protective, contemplative.
- **Language:** Strictly **Sentence Case** everywhere. We never use loud capital tracking (`ALL CAPS`) or synthetic enterprise jargon.
- **Vocabulary:**
  - *Warm greeting:* "Sunday sanctuary", "Good morning", "A little space for today."
  - *Gentle action:* "Add a gentle note", "Add ritual habit", "Preserve", "Reflect now."
  - *Compassionate recovery:* "Gently release this reflection?" instead of harsh "DELETE RECORD".
- **Color Philosophy:**
  - **No pure untinted grays.** All neutral surfaces are tinted with natural earth tones: warm stone slate (`#F3FBF5`) in daylight, or deep forest night slate (`#171B19`) in evening.
  - **Botanical Sage (`#1C6150` / `#75B7A1`):** Represents calm growth, quiet focus, and serene progress.
  - **Terracotta Clay (`#855144` / `#D09A8C`):** Represents warmth, emotional milestones, evening pauses, and human presence.
  - **No Alarmist Alert Colors:** Critical alerts use muted dried madder rose (`#BA1A1A` / `#FFB4AB`) inside gentle blush trays rather than glaring digital sirens.

---

## 5. Architectural & Technical Constraints

### A. Strict V3 Isolation (AGENTS.md Rule 1 & 2)
- **Do not modify V1/V2 legacy code.** V3 runs as a dedicated implementation:
  - Native Host: `com.v07.v3.app.V3Activity`
  - Platform Coordinator: `com.v07.v3.app.V3PlatformHost`
  - Loopback HTTP Server: strictly bound to `127.0.0.1` with `X-V07-Token` session authentication
  - Frontend Assets: `app/src/main/assets/v3/index.html`
- **Zero Legacy UI Reuse:** "DO NOT USE previous design at all." No avionics dials, no radar graphs, no flight-instrument panels.

### B. 5-Node Persistent Navigation Architecture
The core shell is anchored by exactly 5 quiet, primary nodes:
1. **Home (`spa`):** Morning flow snapshot, active focus block, unrushed daily tasks, currently reading card, atmospheric daily quote.
2. **Tools (`grid_view`):** Daily companions suite (Reading Shelf, Habits Tracker, Focus Chronometer, Gita Wisdom, Digital Stillness, Journal Vault, Mood & Energy).
3. **Journal (`edit_note`):** Lined linen notebook, instant search, hashtag filter rail (`#gratitude`, `#focus`, `#calm`, `#morning`, `#reading`), and tactile quick-writer drawer.
4. **Day (`calendar_today`):** Date offset navigation, daily summary, Serene Pace SVG visual curve (Dawn/Noon/Dusk), and chronological activity spine.
5. **You (`person_outline`):** Sanctuary profile, daily rhythm toggles, appearance theme selector (Warm Paper vs. Night Ink), local storage vault controls, and raw database inspection.

### C. Impeccable Hard Rules (Frontend Law)
1. **Zero Broken Text:** External strings, journal entries, and task titles must pass through text sanitization (`cleanText`), with strict line-clamp and ellipsis protection.
2. **Ergonomic Touch Targets:** Minimum 44px × 44px thumb target for all buttons, chips, and navigation items. Active tactile scale compression (`active:scale-[0.97]`).
3. **Graceful Asset Fallbacks:** Books, covers, and illustrations must provide immediate inline vector SVG fallbacks on error (`onerror="this.src=FALLBACK_SVG"`).
4. **Instant Theme Switching:** Toggling between Warm Paper and Night Ink must update CSS tokens instantly, persist in localStorage, and synchronize Android's native system bars via `@JavascriptInterface` `AndroidTheme.setSystemTheme(isLight)`.
5. **Safe Area Insets:** Fixed headers and floating bottom navigation docks must respect hardware display cutouts and navigation bars via `env(safe-area-inset-top)` and `env(safe-area-inset-bottom)`.

---

## 6. Companion Tools Specification

1. **Reading Companion (`#modal-reading`):**
   - Book tracking (current page, total pages, % completed)
   - Quick increment buttons (`-5p`, `+10p`, `Update page`)
   - Curated shelf sections: Reading, Finished, Saved
2. **Habits Companion (`#modal-habits`):**
   - Daily ritual checkboxes with flame streak counters
   - Weekly day-of-week cadence indicators (M T W T F S S)
   - Monthly 30-day consistency heatmap (density-coded cells from light to deep sage)
3. **Focus Chronometer (`#modal-focus`):**
   - Dual Mode: Pomodoro Mode (25m / 5m intervals) vs Custom Timer Mode
   - Circular SVG progress dial with stroke-dashoffset animation
   - Center countdown, cycle count (`Cycle 1/4`), +5m / -5m adjustments
   - Deep work soundscape presets (Forest, Rain, White Noise, Silence)
   - Backend sync with `/api/v3/sessions/*`
4. **Bhagavad Gita Wisdom (`#modal-gita`):**
   - Chapter & verse selector
   - Devanagari Sanskrit shloka with English transliteration and translation
   - Contemplative sanctuary prompt connecting ancient wisdom to modern ease
5. **Digital Stillness / Screen Time (`#modal-screentime`):**
   - Daily and weekly rhythm toggle
   - Total screen time vs daily limit threshold
   - Hourly activity bar chart (6 AM to 11 PM)
   - Top application usage breakdown (Figma, Safari, Slack, Kindle)
6. **Mood & Energy Check-in (`#modal-mood`):**
   - 5-tier mood selector: Heavy 😞, Uneasy 😕, Steady 😐, Calm 🙂, Vibrant 😄
   - Energy level scale (1 to 10)
   - Encrypted quiet reflection entry saved directly to Journal
   - Panic Vault grounding exercises (5-4-3-2-1 sensory calming and 4-4-4-4 box breathing)
7. **Journal Vault:**
   - On-device file export (JSON / Markdown)
   - Local directory path sovereignty (`/storage/emulated/0/...`)
   - Raw SQLite database inspector and diagnostic bundle generator
