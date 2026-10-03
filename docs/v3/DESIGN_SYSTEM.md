# V3 Design System: Sovereign Sanctuary (Alabaster & Obsidian)

## 1. Aesthetic Thesis & Visual Philosophy

The V3 Design System is defined by a **mindful, paper-and-ink sovereign sanctuary aesthetic**:
- **Tactile Paper & Ink Tone**: Pure, calm, organic surfaces. Replaces harsh clinical mint washes and artificial blue glare with warm alabaster stone in light mode and rich obsidian night slate in dark mode.
- **Dignified Botanical & Terracotta Accents**: Primary accents draw from deep forest spruce emerald (`#22634E` light / `#50BF8E` dark) paired with warm earthen terracotta rose (`#98594B` light / `#E29F8C` dark) for secondary actions and milestones.
- **Editorial Typography**: Modern neo-grotesque sans-serif (`Plus Jakarta Sans`, `Inter`, `system-ui`) for UI and headings paired with high-precision tabular monospace (`JetBrains Mono`, `Space Mono`) for metrics, ledger values, chrono timers, and telemetry.
- **Strict Anti-Slop Discipline**:
  - No sickly mint green washes over light backgrounds.
  - No murky, flattened olive-drab dark surfaces.
  - No generic purple gradient blobs or floating 28px pill buttons.
  - Preserved elevation hierarchy across all 5 surface container tokens (`lowest`, `low`, `base`, `high`, `highest`).
  - Zero text breakage, zero unstyled buttons, and robust empty/error states.

---

## 2. Color Palette & Semantic Tokens

### Light Mode: Warm Alabaster & Forest Spruce
Designed for relaxed, glare-free daytime reading and deep work:

```css
:root {
  /* Surfaces & Canvas */
  --surface: #F8F9F5;                      /* Warm alabaster stone baseline */
  --surface-dim: #E9ECE5;                  /* Recessed ambient gutter */
  --surface-bright: #FFFFFF;               /* Brightest paper layer */
  --surface-container-lowest: #FFFFFF;     /* Crisp white card elevations */
  --surface-container-low: #F0F3EE;        /* Subtle grouped containers */
  --surface-container: #E7ECE5;            /* Input fields & secondary panels */
  --surface-container-subtle: #EDF1EB;     /* Chip & tag backgrounds */
  --surface-container-high: #DFE5DC;       /* Dialogs, sheets, bottom nav */
  --surface-container-highest: #D4DCD1;    /* Divided segments & active tab pill */

  /* Ink & Typography */
  --on-surface: #1A211D;                   /* Deep sumi ink charcoal (15.5:1 contrast) */
  --on-surface-variant: #47534C;           /* Warm slate-moss body text (8.0:1 contrast) */
  --on-surface-muted: #728078;             /* Micro captions & timestamps (4.5:1 contrast) */

  /* Botanical Accents */
  --primary: #22634E;                      /* Forest spruce emerald (7.1:1 on white) */
  --on-primary: #FFFFFF;
  --primary-container: #D8EFE4;            /* Mint spruce wash for badges */
  --on-primary-container: #0E382A;

  /* Terracotta Secondary */
  --secondary: #98594B;                    /* Earthy terracotta earthenware (5.4:1 on white) */
  --on-secondary: #FFFFFF;
  --secondary-container: #FADFD7;
  --on-secondary-container: #451C13;

  /* Outlines & Elevation */
  --outline: #76847C;                      /* Clean geometric 1px boundaries */
  --outline-variant: #C8D2CA;              /* Subtle dividers */
  --nav-shadow: 0 10px 30px -4px rgba(26, 33, 29, 0.08), 0 3px 8px -2px rgba(26, 33, 29, 0.04);
  --card-shadow: 0 1px 4px -1px rgba(26, 33, 29, 0.06), 0 1px 2px 0 rgba(26, 33, 29, 0.03);
}
```

### Dark Mode: Obsidian Slate & Luminous Sage Jade
Engineered for evening focus, OLED power efficiency, and zero eye fatigue:

```css
html.dark {
  /* Surfaces & Canvas */
  --surface: #111513;                      /* Rich obsidian night slate */
  --surface-dim: #0D100E;                  /* Deepest recessed viewport */
  --surface-bright: #222A26;               /* Highlighted ambient surface */
  --surface-container-lowest: #151B18;     /* Recessed card wells */
  --surface-container-low: #1A211D;        /* Primary content card baseline */
  --surface-container: #202824;            /* Elevated cards, search inputs */
  --surface-container-subtle: #242E29;     /* Subtle interactive chips */
  --surface-container-high: #29342E;       /* Modals, bottom nav dock, headers */
  --surface-container-highest: #334139;    /* Pressed buttons, active tab indicators */

  /* Bone & Slate Typography */
  --on-surface: #EDF3EE;                   /* Soft bone alabaster (16.4:1 contrast) */
  --on-surface-variant: #A0B0A7;           /* Slate jade body text (7.6:1 contrast) */
  --on-surface-muted: #718278;             /* Micro stamps & inactive icons (4.6:1 contrast) */

  /* Luminous Sage Jade Accents */
  --primary: #50BF8E;                      /* Radiant jade emerald */
  --on-primary: #071E14;                   /* Deep spruce text on primary chip (7.6:1) */
  --primary-container: #1A3F2F;            /* Deep emerald container */
  --on-primary-container: #C8F2DE;

  /* Desert Rose Secondary */
  --secondary: #E29F8C;                    /* Warm desert rose ember (8.0:1 contrast) */
  --on-secondary: #2A130D;
  --secondary-container: #47271F;
  --on-secondary-container: #FAD8CF;

  /* Outlines & Elevation */
  --outline: #38473E;                      /* Crisp tactile borders */
  --outline-variant: #232D27;              /* Non-intrusive container separators */
  --nav-shadow: 0 10px 30px -4px rgba(0, 0, 0, 0.6), 0 3px 8px -2px rgba(0, 0, 0, 0.4);
  --card-shadow: 0 1px 6px -1px rgba(0, 0, 0, 0.4), 0 1px 2px 0 rgba(0, 0, 0, 0.25);
}
```

---

## 3. Typographic Scale & Hierarchy

| Role | Family | Weight | Size | Line Height | Tracking | Usage |
|---|---|---|---|---|---|---|
| Display / Header | Sans-Serif | 700 / 800 | 20px - 28px | 1.2 | `-0.02em` | View titles, major metric headings |
| Section Title | Sans-Serif / Mono | 700 | 12px - 14px | 1.3 | `0.04em` | Card group titles, uppercase tracking |
| Body Text | Sans-Serif | 400 / 500 | 13px - 14px | 1.45 | Normal | Descriptions, notes, entries, settings labels |
| Microcopy / Meta | Sans-Serif | 500 / 600 | 10px - 12px | 1.3 | `0.02em` | Timestamps, status chips, helper hints |
| Numerical / Chrono | Monospace (Tabular) | 700 | 14px - 36px | 1.0 | `-0.02em` | Timer countdown, ledger balances, stats |

---

## 4. Layout Architecture & Component Elevations

### A. Surface Container Elevation Hierarchy
V3 avoids flat monochromatic screens by strictly observing 5 levels of surface container elevation:
1. **Lowest (`--surface-container-lowest`)**: Background wells, sunken ledger summaries, recessed code blocks.
2. **Low (`--surface-container-low`)**: Standard card elevations, unselected list items.
3. **Container Base (`--surface-container`)**: Search input fields, elevated list tiles, inactive filter chips.
4. **High (`--surface-container-high`)**: Sticky bottom navigation dock, view headers, bottom sheets, alert dialogs.
5. **Highest (`--surface-container-highest`)**: Active segmented controls, floating action buttons, pressed state overlays.

### B. Navigation & Header Dock
- Pinned bottom bar with glassmorphic backdrop-blur (`backdrop-blur-md`).
- Active tab pill with smooth transition (`transition-all duration-200`).
- Safe-area insets (`env(safe-area-inset-bottom)`) respected across all edge-to-edge Android displays.

### C. Tactile Inputs & Interactive Touch Targets
- Minimum touch target: `44px x 44px` for all interactive elements.
- Active states: scale-down micro-interaction (`active:scale-[0.98] transition-transform duration-100`).
- Clear focus and selection rings (`ring-2 ring-primary/40`).

---

## 5. Instant Theme Restoration Architecture

To eliminate visual flash on cold start:
1. **Immediate Head Script**: An inline script in `<head>` executes before DOM rendering, checking `localStorage.getItem('v07_app_theme')` with fallback to `window.matchMedia('(prefers-color-scheme: dark)')`.
2. **Synchronous Class Application**: The `.dark` class is appended to `document.documentElement` immediately, preventing white/dark flash.
3. **Dual Persistence**: The `setAppTheme(theme)` function persists state both to local storage (`v07_app_theme`) and to the native Android storage bridge (`storage:set`).
