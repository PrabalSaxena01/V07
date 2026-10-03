---
name: awesome-design-md
description: >-
  Use this skill when creating, structuring, or adhering to DESIGN.md design system specifications. Formats color tokens, typography scales, component constraints, and aesthetic rules in plain markdown for AI agents and developers.
---

# awesome-design.md: Markdown Design System Protocol

> `DESIGN.md` is a plain-text markdown specification format that captures the complete visual language and design system of a project. Placed at the root of a project, it gives AI coding agents and human developers an unambiguous, tokenized blueprint to build pixel-perfect, consistent interfaces without needing Figma files or heavy UI frameworks.

---

## 1. THE ANATOMY OF A WORLD-CLASS `DESIGN.md`

Every `DESIGN.md` file should be structured with the following core sections:

```markdown
# DESIGN SYSTEM: [PROJECT NAME]

## 1. Visual Identity & Design Philosophy
- Core visual metaphor (e.g. "Crisp Vector Monolith", "Minimal Monospace Cockpit", "Editorial Paper & Ink")
- Foundational principles (e.g. Taste, Impeccable UX, Zero Bloat, Zero Broken Text)
- Dial Settings: VARIANCE (1-10), MOTION (1-10), DENSITY (1-10)

## 2. Color Palette & Theming (CSS Variable Tokens)
### Dark Mode (:root)
- `--bg`: Main canvas background
- `--surface`: Card and container background
- `--surface-alt`: Elevated surface or secondary container
- `--border`: Crisp 1px vector border
- `--text`: Primary high-contrast text
- `--text-muted`: Secondary metadata text
- `--accent`: Primary vivid vector accent
- `--accent-dim`: Accent with subtle transparency for badges

### Light Mode (:root[data-theme="light"])
- Complete mirror of dark tokens calibrated for light surfaces

## 3. Typography Hierarchy
- Primary UI Typeface: (e.g. Geist, Inter, system-ui)
- Technical / Numeric Typeface: (e.g. JetBrains Mono, Space Mono)
- Scale:
  - Display / Hero: 24px - 32px (weight: 700/800, tracking: -0.03em)
  - Section Header: 15px - 18px (weight: 600, tracking: -0.01em)
  - Body Text: 13px - 14px (weight: 400, line-height: 1.5)
  - Caption / Mono Meta: 10px - 12px (weight: 500, tabular numbers)
  - Micro Tag: 8px - 9px (weight: 700, uppercase, tracking: 0.05em)

## 4. Spacing, Radii & Elevation Tokens
- Grid Baseline: 4px / 8px scale
- Spacing Tokens: 4px, 8px, 12px, 16px, 20px, 24px
- Border Radii:
  - Micro / Badges: 3px - 4px
  - Cards & Buttons: 6px - 8px
  - Sheets & Modals: 10px - 12px
- Elevation: Crisp 1px borders preferred over fuzzy drop shadows.

## 5. Component Patterns & Ergonomics
- Badges & Status Chips: Compact, high-contrast, monospace numbers
- Cards: Single 1px border, no nested card nesting
- Buttons: Primary, Secondary, Danger, Icon-only (with aria-labels)
- Progress Bars & Gauges: Smooth transitions, high-contrast track and fill
- Modals & Bottom Sheets: Clean scrim backdrop, physics-based entry transition

## 6. Strict Negative Constraints (Forbidden Tropes)
- Enumerate all forbidden elements (no purple blur gradients, no fake leather, no unformatted text)
```

---

## 2. BEST PRACTICES FOR CODING WITH `DESIGN.md`

### 1. Token Reusability via CSS Variables
Always implement the design system tokens directly in CSS:
```css
:root {
  --bg: #0D0F14;
  --surface: #151821;
  --surface-alt: #1C212E;
  --border: #262C3E;
  --text: #F1F5F9;
  --text-muted: #94A3B8;
  --accent: #4DE086;
  --accent-dim: rgba(77, 224, 134, 0.12);
  --font-ui: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --font-mono: "JetBrains Mono", "Space Mono", monospace;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
}

:root[data-theme="light"] {
  --bg: #F8FAFC;
  --surface: #FFFFFF;
  --surface-alt: #F1F5F9;
  --border: #E2E8F0;
  --text: #0F172A;
  --text-muted: #64748B;
  --accent: #16A34A;
  --accent-dim: rgba(22, 163, 74, 0.1);
}
```

### 2. Never Hardcode Arbitrary Hex Values
In application markup and component styles, always bind to token variables:
```css
/* CORRECT */
background: var(--surface);
border: 1px solid var(--border);
color: var(--text);

/* BANNED */
background: #1a1a1a;
border: 1px solid #333333;
```

### 3. Agent Protocol & Autonomous Validation
When working in a codebase:
1. Check for `DESIGN.md` at the project root before writing or refactoring any UI component.
2. Read the defined tokens and typography scale.
3. Validate that generated HTML/CSS complies with the documented component rules and negative constraints.
