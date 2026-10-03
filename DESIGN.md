# DESIGN SYSTEM: V07 2.0

> **Specification Protocol**: Governed by `/awesome-design-md` and the `taste` & `impeccable` frontend design standards.
> **Scope**: V07 2.0 Android Mobile Application & Web Shell.

---

## 1. Visual Identity & Design Philosophy

V07 2.0 is a deliberate, calm, and inspired personal operating sanctuary. It is designed to inspire clarity, deep focus, and effortless daily cadence.

### Core Visual Metaphor: "The Inspired Editorial Sanctuary"
- **Calm Clarity**: Generous negative space, soft ambient surfaces, and razor-sharp typographic alignment.
- **Instrument Precision**: Functional controls that react with instantaneous tactile feedback (scale 0.97 on press).
- **Zero Terminal/Hacker Clichés**: Complete elimination of monospace dumping grounds, green matrix code, scanlines, CRT flicker, CLI prompts (`> `, `$ `), and terminal window styling.
- **Consistent Hierarchy**: One unified typographic voice across every tab, modal, and widget in the entire application.

### The Three Dial Settings
- **`DESIGN_VARIANCE: 6`** — Cohesive structure with intentional rhythmic asymmetry for cards and banners.
- **`MOTION_INTENSITY: 4`** — Subtle, physics-based micro-interactions (`cubic-bezier(0.16, 1, 0.3, 1)`), zero bouncy or cartoonish easing.
- **`VISUAL_DENSITY: 6`** — High information utility balanced with breathing room; comfortable mobile thumb targets.

---

## 2. Color Palette & Theming (CSS Variable Tokens)

Colors are represented as strict semantic CSS custom properties. Raw, untokenized hex codes in component markup are prohibited.

### Dark Mode (`:root` / `.dark`)
```css
:root, .dark {
  /* Canvas & Surfaces */
  --bg: #0E1217;
  --surface: #161B22;
  --surface-alt: #1C222B;
  --surface-hover: #242B36;
  --surface-active: #2D3643;
  --surface-glass: rgba(22, 27, 34, 0.85);

  /* Borders & Dividers */
  --border: #28303E;
  --border-subtle: rgba(255, 255, 255, 0.08);
  --border-focus: #4F8DF5;
  --divider: #1E2530;

  /* Typography */
  --text: #F0F6FC;
  --text-secondary: #94A3B8;
  --text-muted: #64748B;
  --text-inverse: #0E1217;

  /* Primary Brand & Accents */
  --primary: #4F8DF5;
  --primary-hover: #3B7AD9;
  --primary-dim: rgba(79, 141, 245, 0.14);
  --primary-text: #FFFFFF;

  /* Semantic State Tokens */
  --success: #34D399;
  --success-dim: rgba(52, 211, 153, 0.14);
  --warning: #FBBF24;
  --warning-dim: rgba(251, 191, 36, 0.14);
  --danger: #F87171;
  --danger-dim: rgba(248, 113, 113, 0.14);
  --info: #38BDF8;
  --info-dim: rgba(56, 189, 248, 0.14);

  /* Plugin Identity Accents */
  --accent-reader: #818CF8;
  --accent-habits: #F59E0B;
  --accent-chrono: #34D399;
  --accent-gamify: #FBBF24;
  --accent-vitals: #38BDF8;

  /* Elevation Shadows */
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 14px rgba(0, 0, 0, 0.35);
  --shadow-lg: 0 10px 28px rgba(0, 0, 0, 0.45);
  --shadow-nav: 0 -4px 20px rgba(0, 0, 0, 0.3);
}
```

### Light Mode (`:root[data-theme="light"]`)
```css
:root[data-theme="light"] {
  /* Canvas & Surfaces */
  --bg: #F8FAFC;
  --surface: #FFFFFF;
  --surface-alt: #F1F5F9;
  --surface-hover: #E2E8F0;
  --surface-active: #CBD5E1;
  --surface-glass: rgba(255, 255, 255, 0.9);

  /* Borders & Dividers */
  --border: #E2E8F0;
  --border-subtle: rgba(0, 0, 0, 0.06);
  --border-focus: #2563EB;
  --divider: #EDF2F7;

  /* Typography */
  --text: #0F172A;
  --text-secondary: #475569;
  --text-muted: #94A3B8;
  --text-inverse: #FFFFFF;

  /* Primary Brand & Accents */
  --primary: #2563EB;
  --primary-hover: #1D4ED8;
  --primary-dim: rgba(37, 99, 235, 0.08);
  --primary-text: #FFFFFF;

  /* Semantic State Tokens */
  --success: #10B981;
  --success-dim: rgba(16, 185, 129, 0.1);
  --warning: #F59E0B;
  --warning-dim: rgba(245, 158, 11, 0.1);
  --danger: #EF4444;
  --danger-dim: rgba(239, 68, 68, 0.1);
  --info: #0284C7;
  --info-dim: rgba(2, 132, 199, 0.1);

  /* Plugin Identity Accents */
  --accent-reader: #6366F1;
  --accent-habits: #D97706;
  --accent-chrono: #10B981;
  --accent-gamify: #F59E0B;
  --accent-vitals: #0284C7;

  /* Elevation Shadows */
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 14px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 10px 28px rgba(0, 0, 0, 0.12);
  --shadow-nav: 0 -4px 20px rgba(0, 0, 0, 0.05);
}
```

---

## 3. Typography Hierarchy

Consistent, refined human typography is the cornerstone of V07 2.0. Monospace is **strictly forbidden** for UI labels, titles, buttons, forms, and general content.

### Font Families
- **Primary Interface Font**: `Plus Jakarta Sans`, `Inter`, `-apple-system`, `BlinkMacSystemFont`, `sans-serif`.
- **Numerical Tabular Display**: Same sans family configured with `font-variant-numeric: tabular-nums` for seamless countdowns and timers without glyph jitter.
- **Code Snippets Only**: Monospace (`ui-monospace`, `SFMono-Regular`, `monospace`) is reserved exclusively for explicit code blocks or database query displays.

### Typographic Scale & Tracking
| Level | Font Size | Weight | Line Height | Tracking | Application |
|---|---|---|---|---|---|
| **Display Timer** | 44px–52px | 700 / 800 | 1.1 | -0.03em | Focus cockpit timer digits |
| **Hero / Page Title**| 22px–24px | 700 | 1.25 | -0.02em | Section main headers |
| **Section Heading** | 16px–18px | 600 | 1.35 | -0.01em | Card and group headers |
| **Subhead / Body Lead** | 14px–15px | 600 | 1.4 | 0 | Task titles, metric values |
| **Body Regular** | 13px–14px | 400 / 500 | 1.5 | 0 | Standard text, descriptions, inputs |
| **Caption / Meta** | 11px–12px | 500 | 1.4 | 0.01em | Timestamps, secondary telemetry |
| **Micro Badge** | 10px–11px | 600 / 700 | 1.2 | 0.04em | Status tags, category chips |

---

## 4. Spacing, Radii & Elevation Tokens

### Baseline 4px / 8px Spacing Scale
- `--space-1`: 4px
- `--space-2`: 8px
- `--space-3`: 12px
- `--space-4`: 16px
- `--space-5`: 20px
- `--space-6`: 24px
- `--space-8`: 32px

### Border Radii
- **Micro Badges & Tags**: `4px`
- **Inputs, Buttons & Chips**: `8px`
- **Cards, Panels & Deck Items**: `12px` or `14px`
- **Modals, Floating Toasts & Bottom Sheets**: `16px`
- **Circular Indicators**: `50%` (`9999px`)
- **Strict Rule**: No bubbly pill buttons (`border-radius: 28px+`) or blob shapes.

---

## 5. Component Patterns & Ergonomics

### 1. Top App Bar HUD
- Height: `60px` with safe area inset padding.
- Left: Circular status beacon + clean bold wordmark `V07 2.0`.
- Right: Minimalist HUD chips displaying active timer status and today's total screen time.

### 2. Focus Cockpit
- Centered circular progress gauge with smooth SVG stroke-dashoffset transition.
- Large, legible sans digits with tabular numbers.
- Large touch-target buttons (min 44px height): Start, Pause, Finish, Discard.
- Category chips row with active selection highlighting.

### 3. Metric Cards & Stat Grids
- 2-column or 3-column responsive grid.
- Each card: Subtle hairline border (`1px solid var(--border)`), subtle colored icon indicator, primary value, and contextual label.

### 4. Daily Timeline & Day Tracker
- Date tuner bar with simple previous/next day arrows and "Today" quick reset.
- Chronological vertical activity stream with colored beacon dots connected by a continuous 2px hairline guide.
- Rich event cards displaying focus sessions, task completions, and scheduled countdowns.

### 5. Tasks & Obligations
- Input field with instant add action and priority selection chips (High, Medium, Low).
- Tab switcher: `Pending` vs. `Completed`.
- Task items: Smooth checkbox toggle, single-line truncation, subtle strikethrough upon completion.
- Checklist.design empty state with purposeful SVG illustration and action CTA.

### 6. Bottom Navigation Bar
- Height: `58px + env(safe-area-inset-bottom)`.
- 5 or 6 clear navigation items with vector icons: Home, Timeline, Focus, Tasks, Notes, System.
- Active tab indicated by primary accent color and crisp top highlight indicator.

---

## 6. Strict Negative Constraints (Forbidden Tropes)

The following design tropes are strictly forbidden across all V07 2.0 code:

1. **NO TERMINAL CLICHÉS**:
   - No green/amber phosphor monochrome text on pure black.
   - No CLI prompt symbols (`> `, `$ `, `root@...`).
   - No blinking terminal cursor underlines (`_`).
   - No monospace fonts on buttons, labels, inputs, navigation, or prose.
   - No ASCII art or command-line help texts.
2. **NO CRT SCANLINES OR FLICKER**:
   - All scanline CSS animations and overlay divs are banned and removed.
3. **NO ARTIFICIAL LETTER-SPACING SPAM**:
   - Do not apply `letter-spacing: 0.1em` or `letter-spacing: 0.2em` indiscriminately.
4. **NO AI SLOP & PURPLE BLUR BLOBS**:
   - No ambient radial gradient blur splotches.
   - No bubbly over-rounded pill buttons.
   - No fake glassmorphism blur that degrades WebView rendering performance.
5. **ZERO BROKEN TEXT & ZERO BROKEN IMAGES**:
   - All text from data stores or APIs must be sanitized (no raw HTML tags or unescaped entities).
   - Dynamic text containers must have explicit single-line or multi-line truncation guards.
