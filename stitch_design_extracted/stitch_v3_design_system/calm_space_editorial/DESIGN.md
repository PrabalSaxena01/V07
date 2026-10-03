---
name: Calm Space Editorial
colors:
  surface: '#f3fbf5'
  surface-dim: '#d3dcd6'
  surface-bright: '#f3fbf5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#edf6ef'
  surface-container: '#e7f0e9'
  surface-container-high: '#e2eae4'
  surface-container-highest: '#dce5de'
  on-surface: '#151d19'
  on-surface-variant: '#3f4945'
  inverse-surface: '#2a322e'
  inverse-on-surface: '#eaf3ec'
  outline: '#6f7975'
  outline-variant: '#bfc9c4'
  surface-tint: '#266958'
  primary: '#1c6150'
  on-primary: '#ffffff'
  primary-container: '#397a68'
  on-primary-container: '#c6ffeb'
  inverse-primary: '#92d4be'
  secondary: '#855144'
  on-secondary: '#ffffff'
  secondary-container: '#fdb9a9'
  on-secondary-container: '#79473b'
  tertiary: '#4d5955'
  on-tertiary: '#ffffff'
  tertiary-container: '#65716d'
  on-tertiary-container: '#e8f5f0'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#adf0da'
  primary-fixed-dim: '#92d4be'
  on-primary-fixed: '#002019'
  on-primary-fixed-variant: '#015141'
  secondary-fixed: '#ffdad2'
  secondary-fixed-dim: '#fab6a6'
  on-secondary-fixed: '#341007'
  on-secondary-fixed-variant: '#693a2e'
  tertiary-fixed: '#d8e5e0'
  tertiary-fixed-dim: '#bcc9c5'
  on-tertiary-fixed: '#121e1b'
  on-tertiary-fixed-variant: '#3d4946'
  background: '#f3fbf5'
  on-background: '#151d19'
  surface-variant: '#dce5de'
typography:
  display:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 56px
    letterSpacing: -0.025em
  display-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 34px
    fontWeight: '600'
    lineHeight: 42px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '500'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.005em
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 3rem
  margin-tablet: 2rem
  margin-mobile: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system embodies a serene, tactile journal aesthetic tailored for mindful productivity, thoughtful writing, and reflective digital environments. The target audience values visual stillness, intentional composition, and the unhurried cadence of physical print. The emotional tone is meditative, disciplined, and human. 

Drawing from modern minimalism intertwined with organic editorial publishing, the aesthetic rejects clinical SaaS patterns, hyper-dense dashboards, and mechanical tech metaphors. Zero monospaced fonts, raw code blocks, or harsh terminal artifacts appear in the interface. Layouts prioritize generous breathing room, intentional text margins, and natural sentence case over aggressive uppercase styling or high-velocity micro-interactions. The tactile quality comes from layered physical metaphors: heavy warm paper backdrops, crisp white paper stock cards, and precise, ink-grounded typography.

## Colors

The palette mimics organic stationery, pressed botanicals, and hand-bound print matter. It relies strictly on a light mode canvas to maintain the tactile paper impression.

- **Canvas & Surface:** The foundational canvas is warm paper (`#F6F7F4`), providing an unbleached, reflective backdrop. Elevated interactive panels and content containers rest on clean card surface stock (`#FFFFFF`).
- **Typography & Content:** Primary copy uses deep ink (`#202824`), a rich forest-charcoal that provides crisp readability without the harsh, digital severity of pure `#000000`. Secondary, metadata, and placeholder text use muted green-slate (`#66716B`), ensuring clear legibility while remaining visually quiet.
- **Accents:** 
  - **Botanical Sage (`#397A68`):** The primary anchor for primary interactive states, progress markers, and key focal accents. Its companion tint, soft accent background (`#E3F0EB`), serves as a gentle resting fill for highlights, tags, and selected states.
  - **Warm Cedar (`#A86F61`):** A secondary accent used for contextual emphasis, human annotations, or quiet alerts, supported by soft warm (`#F4E9E5`) for soft callouts and warning backgrounds.
- **Structural Separation:** Outlines, hairline rules, and panel divisions utilize subtle border (`#E1E7E2`), preserving structure without drawing high-contrast geometric boxes.

## Typography

Plus Jakarta Sans is utilized across all typographical tiers. Its contemporary humanist geometry, open counters, and quiet warmth mimic printed letterforms without becoming overly sterile or clinical.

- **Capitalization:** Use natural sentence case across all headlines, subheads, labels, and action triggers. Avoid all-caps styling entirely—even for tags or table headers—to sustain an unhurried, editorial voice.
- **Reading Rhythm:** Editorial body copy prefers `body-lg` (18px) set to generous line-heights (28px) with maximum line-lengths restricted to 65–75 characters per measure for sustained, fatigue-free reading.
- **Hierarchy:** Contrast is established through size differentials and selective weights (`500` Medium and `600` SemiBold) rather than hyper-bold (`800` or `900`) values, which disrupts the meditative balance.

## Layout & Spacing

The layout philosophy follows a disciplined, column-centered fixed grid inspired by book design and architectural journals. Rather than edge-to-edge fluid dashboards, views are bound to max-width editorial containers (typically 1120px for desktop workflows, 720px for dedicated reading and writing columns).

- **Desktop (1024px+):** 12-column grid, centered, flanked by a minimum canvas margin of `3rem` (`margin`). Column gutters are fixed at `1.5rem` (`gutter`). Generous white space between distinct vertical narrative sections utilizes `space-xl` and higher multiples.
- **Tablet (768px – 1023px):** 8-column layout, margins reduced to `2rem` (`margin-tablet`), preserving the centered editorial column format.
- **Mobile (< 768px):** 4-column layout with `1.25rem` (`margin-mobile`) canvas margins and `1rem` (`gutter-mobile`) gutters. Containers collapse into a single stacked column where internal card gutters default to `space-md`.
- **Rhythm:** Internal component spacing follows a 4px/8px baseline, prioritizing expansive paddings (`space-lg` to `space-xl`) inside cards to reinforce tactile uncluttered surfaces.

## Elevation & Depth

This system avoids dark, drop-shadows and synthetic 3D lighting. Depth is articulated primarily through **tonal paper layering** supported by extremely diffuse, warm-tinted ambient shadows and subtle hairline boundaries.

- **Base Layer (Ground):** The unbleached warm paper background (`#F6F7F4`) sits at the lowest plane.
- **Surface Layer (Cards & Panels):** White card surfaces (`#FFFFFF`) rest on the paper canvas. They are defined primarily by a 1px continuous border of subtle border (`#E1E7E2`), paired with an ultra-subtle ambient shadow: `0 2px 12px -2px rgba(32, 40, 36, 0.03)`.
- **Floating / Modal Layer:** Menus, dropdowns, and ephemeral popovers lift subtly off the canvas with a soft, dual-tier organic shadow tinted with charcoal ink: `0 8px 24px -4px rgba(32, 40, 36, 0.06), 0 2px 6px -1px rgba(32, 40, 36, 0.02)`, maintained alongside the 1px `#E1E7E2` perimeter stroke.
- **Zero Glassmorphism:** No backdrop blurs, specular highlights, or refractive glass overlays are permitted; all surfaces must read as opaque, tactile paper materials.

## Shapes

The design system employs a balanced, gentle geometry (Level 2: Rounded) that strikes an equilibrium between crisp bookbinding edges and organic touchability.

- **Base Components:** Inputs, buttons, chips, and small controls use a 0.5rem (8px) radius. This prevents the harshness of boxy brutalism while avoiding the overly toy-like aesthetic of full pill buttons.
- **Cards & Surface Modules:** Containers and cards utilize `rounded-lg` (1rem / 16px) to gracefully frame long-form content.
- **Dialogs & Overlays:** Large focus sheets and modal panels utilize `rounded-xl` (1.5rem / 24px) for a soft, welcoming presentation.
- **Structural Integrity:** Internal elements inside containers (such as selected rows or embedded media) match the nesting radius formula: inner radius equals outer radius minus container padding, preserving harmonious corner curves.

## Components

### Buttons
- **Primary:** Botanical Sage background (`#397A68`), `#FFFFFF` text, subtle border transparent, 0.5rem border radius. Hover introduces a subtle darkening to `#31695A`. Focus ring is 2px botanical sage offset by 2px paper `#F6F7F4`.
- **Secondary / Ghost:** Warm card surface (`#FFFFFF`) or transparent fill with a 1px `#E1E7E2` border, deep ink text (`#202824`). Hover transitions background to soft accent (`#E3F0EB`) with `#397A68` text.
- **Tertiary:** Pure text button, `#397A68`, sentence case, underlined only on hover.

### Chips & Badges
- Constructed with a 0.5rem corner radius. Soft accent background (`#E3F0EB`) with botanical sage text (`#397A68`) for standard metadata.
- Contextual/warm chips use soft warm (`#F4E9E5`) with warm cedar text (`#A86F61`).
- Padding is compact (`space-xs` vertically, `space-sm` horizontally) with `label-md` typography.

### Lists
- Editorial row lists rely on clean horizontal hairline separators (`1px solid #E1E7E2`) rather than vertical borders or isolated card capsules.
- Generous vertical cell padding (`space-md` to `space-lg`). Row hover reveals an imperceptible paper wash (`#F9FAF8`).

### Checkboxes & Radio Buttons
- Form controls use 1.5px subtle border (`#E1E7E2`) borders against a `#FFFFFF` canvas. 
- Selected checkboxes fill with botanical sage (`#397A68`) housing a white checkmark icon. Radios feature an inset botanical dot. Checkbox radius is 4px; radios are circular.

### Input Fields
- Built on a crisp `#FFFFFF` background with a 1px `#E1E7E2` boundary.
- Default text uses deep ink (`#202824`); placeholder copy is muted green-slate (`#66716B`).
- Active/focus state subtly shifts the border to botanical sage (`#397A68`) with a matching, zero-blur 1px outer ring. No aggressive blue glows.

### Cards & Narrative Panels
- Pure white surfaces (`#FFFFFF`) with 1px `#E1E7E2` borders and `1rem` corner rounding.
- Internal padding is deliberately spacious (`space-lg` to `space-xl`) to encourage calm scanning and breathing space around blocks of prose.

### Journal Callouts & Pullquotes
- Quiet editorial panels featuring soft warm (`#F4E9E5`) or soft accent (`#E3F0EB`) solid fills without borders.
- Left-accented with an optional 3px solid ink stroke in warm cedar (`#A86F61`) or botanical sage (`#397A68`), paired with `body-lg` type for thoughtful annotations.