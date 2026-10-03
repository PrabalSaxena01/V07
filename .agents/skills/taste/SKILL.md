---
name: taste
description: >-
  Use this skill to avoid generic "AI-generated" looks and AI slop in web and mobile designs. Enforces human editorial taste, brief inference, three-dial calibration (Variance, Motion, Density), negative constraints against cliches (no purple gradients, no bubbly pill buttons, no faux-skeuomorphism, no unformatted text), and authentic tactile/vector craft.
---

# Taste: Anti-Slop Frontend Skill & Editorial Craft

> Landing pages, app surfaces, widgets, portfolios, and redesigns.
> Every rule below is **contextual**. None of it fires automatically without understanding the user's intent. First read the brief, infer the right design direction, and pull only what fits.

---

## 0. BRIEF INFERENCE (Read the Room First)

Before touching code or tweaking dials, **infer what the user actually wants**. Most LLM design output is mediocre because the model jumps to a generic statistical default instead of reading the room.

### 0.A Read these signals first
1. **Page or Component kind** - dashboard widget, plugin page, SaaS landing, mobile reader, portfolio, editorial blog, public-sector tool.
2. **Vibe words** the user used - "minimalist", "calm", "Linear-style", "vector art", "brutalist", "Apple-y", "tactile", "editorial", "dark tech".
3. **Reference signals** - URLs linked, screenshots pasted, products named, design system files (`DESIGN.md`).
4. **Audience** - High-focus daily mobile user vs. B2B procurement buyer vs. casual consumer. The audience dictates the aesthetic.
5. **Brand assets that already exist** - existing color tokens, monospace fonts, vector border scales.
6. **Quiet constraints** - zero text breakage, responsive mobile scaling, low battery/CPU overhead, high contrast legibility.

### 0.B Output a one-line "Design Read" before generating
Before writing frontend code, state in one line:
> **"Reading this as: <kind> for <audience>, with a <vibe> language, leaning toward <design system or aesthetic family>."**

*Example:*
- *"Reading this as: Mobile Obsidian plugin widget for daily productivity, with a crisp vector art language, leaning toward high-contrast dark neutrals + single emerald accent + JetBrains Mono metrics."*

### 0.C Anti-Default Discipline
**Never default to the generic AI slop tropes:**
- Centered hero with floating purple/magenta blur blobs (`radial-gradient` splotches)
- Three equal bubbly rounded cards (`border-radius: 28px+`) with generic drop shadows
- Faux-skeuomorphic leather, fake stitched ribbons, plastic gradients
- Low-contrast "whisper" gray text (`#777` on `#1a1a1a`) that fails accessibility
- Unformatted raw strings or HTML tags leaking from APIs

---

## 1. THE THREE DIALS (Core Configuration)

After the design read, configure three dials. Every layout, motion, and density decision is gated by these:

* **`DESIGN_VARIANCE: 8`** — `1` = Perfect Symmetry / Grid, `10` = Artsy Asymmetry / Dynamic
* **`MOTION_INTENSITY: 6`** — `1` = Completely Static, `10` = Cinematic Physics / Micro-springs
* **`VISUAL_DENSITY: 4`** — `1` = Airy Art Gallery, `10` = Data Cockpit / High-Density

**Baseline:** `8 / 6 / 4`. (Productivity widgets usually run at `6 / 4 / 7` for information density and fast mobile responsiveness).

### 1.A Dial Inference Matrix
| Signal / Context | VARIANCE | MOTION | DENSITY |
|---|---|---|---|
| "minimalist / clean / calm / editorial / Linear-style" | 5-6 | 3-4 | 3-4 |
| "vector art / tactile / crisp dashboard / plugin" | 6-7 | 4-5 | 6-8 |
| "premium consumer / Apple-y / luxury / brand" | 7-8 | 5-7 | 3-4 |
| "experimental / wild / interactive showcase" | 9-10 | 8-10 | 3-4 |
| "trust-first / public-sector / accessibility-critical" | 3-4 | 2-3 | 5-6 |
| "redesign - preserve existing" | match | +1 | match |

---

## 2. BANNED AI CLICHÉS & STRICT NEGATIVE CONSTRAINTS

### 1. The Purple/Magenta Blur Blob (The Lila Rule)
- **BANNED**: `radial-gradient(circle, rgba(147, 51, 234, 0.4) 0%, transparent 70%)` as random background splotches.
- **Replacement**: Crisp solid surfaces (`#0D0F14`, `#151821`) with sharp 1px vector borders (`#262C3E`, `rgba(255, 255, 255, 0.08)`).
- **Rule**: If purple/violet is explicitly requested by brand, execute with intent: consistent palette, harmonized neutrals, and sharp vector geometry—never fuzzy ambient slop.

### 2. Over-Rounded Bubbly Cards
- **BANNED**: `border-radius: 28px` or `36px` on compact cards and modals.
- **Replacement**: Intentional geometric radii:
  - Chips & micro-badges: `3px` or `4px`
  - Compact cards: `6px` or `8px`
  - Sheets & modals: `10px` or `12px`

### 3. Faux-Skeuomorphic Slop
- **BANNED**: Fake leather, paper grain filters, stitched edges, simulated glossy plastic, fake embossed text.
- **Replacement**: Honest digital materials, clean vector typography, procedural SVG icons, and crisp border elevation.

### 4. Serif Discipline (Anti-Default)
- **BANNED**: Defaulting to display serifs (`Fraunces`, `Instrument Serif`) just to look "classy" or "creative".
- **Rule**: Default to high-craft sans-serif display (`Geist`, `Cabinet Grotesk`, `Satoshi`, `Inter Display`) paired with clean monospace.
- **Serif is only permitted when**: The brand explicitly demands heritage/literary/editorial aesthetics.
- **Emphasis Rule**: When emphasizing words, use italic or bold of the **same font family**. Never inject a random serif word into a sans headline.

### 5. Premium-Consumer Palette Ban
- **BANNED as default**: Warm beige/cream (`#f7f5f1`, `#ece6db`) + brass/clay/oxblood (`#b08947`, `#9a2436`) + espresso dark text.
- **Replacements (Rotate)**:
  - *Cold Luxury*: Silver-slate (`#0F172A`) + cool metallic borders (`#334155`) + crisp cyan/emerald accent.
  - *Forest Vector*: Deep pine (`#061E14`) + bone white + bright solar amber (`#FFB84D`).
  - *Dark High-Contrast*: True off-black (`#0D0F14`) + electric violet (`#8D6CFF`) or emerald (`#4DE086`).

### 6. Eyebrow Restraint
- An "eyebrow" is the small uppercase wide-tracking label sitting above headers (`SECTION TITLE`, `STATUS`).
- **Rule**: Maximum 1 eyebrow per 3 sections. Do not slap an uppercase micro-label on top of every single card or header.

### 7. Layout Rhythm & Anti-Repetition
- **ZIGZAG CAP**: Never use more than 2 consecutive alternating left-image/right-text rows. Break rhythm with bento, full-width banners, or metric decks.
- **BENTO CELL COUNT**: A bento grid has exactly as many cells as real content exists. Never leave blank empty placeholder cards.
- **BUTTON CONTRAST & SINGLE CTA INTENT**: Primary CTA must pass WCAG AA contrast (minimum 4.5:1). Button labels must fit on one line at desktop/mobile. Never duplicate synonymous CTAs on the same surface ("Get Started" + "Sign Up Free" + "Try Free" in the same view).

---

## 3. TASTE ARCHITECTURE: THE 4 PILLARS OF HIGH-CRAFT UI

### Pillar 1: High Information Density with Clean Negative Space
- Avoid bloated empty cards that display a single orphaned number.
- Combine clear summary metrics with secondary contextual breakdowns (e.g. `140/320p · 43% · ~12d remaining`).
- Align elements strictly to an 8px or 4px baseline grid.

### Pillar 2: Monospace Metrics + Clean Sans Typography
- Use monospace fonts (`JetBrains Mono`, `Space Mono`) for numbers, progress indicators, dates, and technical chips.
- Pair with clean neo-grotesque sans-serif (`Inter`, `system-ui`, `-apple-system`) for headings and reading copy.
- Never mix more than two typeface families in a single surface.

### Pillar 3: Purposeful Single-Accent Color Systems
- 90% of the UI consists of structured monochrome neutrals (`#0D0F14`, `#151821`, `#262C3E` on dark; `#FFFFFF`, `#F8FAFC`, `#E2E8F0` on light).
- Exactly **one** vivid vector accent color per module (e.g. Emerald `#4DE086`, Electric Violet `#8D6CFF`, Solar Amber `#FFB84D`, or Cyan `#5CE1E6`).
- Color consistency lock: Once chosen, lock it across the entire module.

### Pillar 4: Tactile & Procedural Vector Precision
- Render icons and motifs using procedural mathematical SVGs rather than raster PNGs.
- Add instant tactile feedback on touch/click:
  ```css
  button:active, .card:active {
    transform: scale(0.97);
    transition: transform 0.08s ease;
  }
  ```

---

## 4. PRE-FLIGHT TASTE AUDIT CHECKLIST

Before finalizing any screen, widget, or component, verify:
- [ ] **Design Read Declared**: Was the design read stated before generating code?
- [ ] **Zero Slop Elements**: Are purple blur blobs, faux leather, and bubbly pill cards completely absent?
- [ ] **Single Accent Locked**: Is there exactly one primary accent color used with purpose?
- [ ] **Contrast & Legibility**: Does all text pass WCAG AA contrast against its background?
- [ ] **Zero Broken Text**: Is all external API data sanitized (no raw HTML tags, unescaped entities, or nulls)?
- [ ] **Dual-Theme Ready**: Does the surface support both dark and light modes cleanly via CSS variables?
- [ ] **Tactile Feedback**: Do buttons and interactive cards provide immediate scale or elevation feedback on touch?
