---
name: Paper and Ink After Sunset
colors:
  surface: '#101412'
  surface-dim: '#101412'
  surface-bright: '#363a38'
  surface-container-lowest: '#0b0f0d'
  surface-container-low: '#181c1a'
  surface-container: '#1c201e'
  surface-container-high: '#272b29'
  surface-container-highest: '#323633'
  on-surface: '#e0e3e0'
  on-surface-variant: '#bfc9c3'
  inverse-surface: '#e0e3e0'
  inverse-on-surface: '#2d312f'
  outline: '#89938e'
  outline-variant: '#3f4945'
  surface-tint: '#91d4bd'
  primary: '#91d4bd'
  on-primary: '#00382b'
  primary-container: '#75b7a1'
  on-primary-container: '#004838'
  inverse-primary: '#256957'
  secondary: '#f3b9aa'
  on-secondary: '#4b271d'
  secondary-container: '#653c32'
  on-secondary-container: '#e0a89a'
  tertiary: '#ffb3b0'
  on-tertiary: '#5d1618'
  tertiary-container: '#f58c89'
  on-tertiary-container: '#702525'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#acf0d8'
  primary-fixed-dim: '#91d4bd'
  on-primary-fixed: '#002018'
  on-primary-fixed-variant: '#005140'
  secondary-fixed: '#ffdbd1'
  secondary-fixed-dim: '#f3b9aa'
  on-secondary-fixed: '#31120a'
  on-secondary-fixed-variant: '#653c32'
  tertiary-fixed: '#ffdad8'
  tertiary-fixed-dim: '#ffb3b0'
  on-tertiary-fixed: '#400106'
  on-tertiary-fixed-variant: '#7b2d2d'
  background: '#101412'
  on-background: '#e0e3e0'
  surface-variant: '#323633'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: -0.005em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 30px
    letterSpacing: 0em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.01em
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
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.25rem
  gutter-mobile: 0.75rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.25rem
  space-2xl: 3.5rem
---

## Brand & Style

This design system embodies the intimacy of journaling late in the evening. Guided by the metaphor of "paper and ink after sunset," it provides a serene, distraction-free environment that encourages self-reflection, mindfulness, and calm.

The personality balances organic tranquility with refined structure. Visual cues evoke deep forest night air, tactile uncoated paper, and steady earthen pigments. Interfaces feel protective, warm, and restorative rather than sterile or high-tech. There are no harsh contrasts, no jarring notifications, and no artificial neon glows. Transitions mimic the gentle tempo of turning a heavy paper page.

## Colors

The palette avoids cold grays and pitch black (`#000000`) in favor of earthy, warm-tinted forest tones that reduce eye strain during evening reflection.

- **Background / Canvas (`#171B19`):** Deep, warm green-slate providing the base atmosphere.
- **Surfaces:** Layered tiers build tactile depth. Container surfaces use `#202522`, with lifted cards and inset containers using `#272D29`.
- **Text & Editorial Elements:** Primary text utilizes `#E9EEE9` (creamy off-white), secondary content uses `#AAB5AE`, and subtle structural metadata or timestamps rest at `#7D8982`.
- **Primary Accent (`#75B7A1` & Soft `#263D35`):** A muted botanical sage used for focal actions, active dates, and serene confirmation cues.
- **Warm Complementary Accent (`#D09A8C` & Soft `#3A2B28`):** Earthy terracotta for personal milestones, prompts, emotional warmth, and journaling streaks.
- **Borders & Dividers:** Defined softly with `#343C37` for structural containment and `#2B322E` for quiet internal separation.
- **Alert / Danger (`#E07B78` & Soft `#402827`):** Gentle brick-rose for destructive actions and validation without disrupting the meditative space.

## Typography

Plus Jakarta Sans brings soft geometry, wide counters, and quiet humanism across all contexts. 

- **Editorial Rhythm:** Generous line heights (`30px` on `18px` body text) allow journaling and long-form prose to breathe without visual crowding.
- **Headlines:** Display and headline levels rely on semi-bold weights (`600`) with subtle negative tracking, creating an intimate, composed cadence.
- **Labels & Microcopy:** Clean medium-to-semibold weights maintain legibility at low brightness without overpowering the content hierarchy.

## Layout & Spacing

The layout is built upon an intentional, quiet reading column that avoids excessive width:

- **Max Content Width:** Journaling canvas caps at `720px` to maintain an ideal reading line length of 65–75 characters. Dashboard and browsing views adapt to a centered `1080px` maximum frame.
- **Grid Structure:** A fluid 8-column layout on desktop stepping down to a single-column journal stream on mobile.
- **Vertical Spacing:** Generous element margins evoke blank journal pages. Large section dividers utilize `space-2xl` (`56px`), while component groupings stick to a strict 4px/8px modular cadence.
- **Margins & Safe Zones:** Canvas borders feature cushioned outer margins (`margin` at `2rem`, scaling to `1rem` on mobile) so touch interactions never hug device perimeters.

## Elevation & Depth

Visual hierarchy relies entirely on warm tonal layering and soft ghost borders rather than drop shadows or artificial glows:

- **Layer 0 (Canvas):** Base background `#171B19`.
- **Layer 1 (Cards & Insets):** Inset entry lists and default cards rest on `#202522`, bound by a crisp hairline border (`1px solid #343C37`).
- **Layer 2 (Floating Surfaces & Drawers):** Modals, sticky toolbars, and active writing cards step forward onto `#272D29`, outlined with `1px solid #343C37` and an ultra-diffused atmospheric tint (`box-shadow: 0 16px 36px -8px rgba(10, 13, 11, 0.45)`).
- **No Harsh Skeuomorphism or Neon:** No drop-shadows on buttons, no colored outer glows, and no bright white halos. Depth feels matte and tactile, mimicking stacked cardstock under warm ambient light.

## Shapes

The interface balances organic softness and structural stability using smooth, pill-influenced contours:

- **Base Corner Radius:** Surfaces, cards, and modal sheets use a continuous curvature radius of `20px` (`1.25rem`), creating a soft, welcoming, pebble-like silhouette.
- **Interactive Elements:** Buttons, text fields, and chips adopt rounded forms scaling from `12px` (`0.75rem`) for compact tags up to full pill shapes (`9999px`) for primary actions and mood selectors.
- **Dividers:** Internal rules and section splits are `1px` high with soft fading endcaps where appropriate.

## Components

- **Buttons:**
  - *Primary:* Botanical sage fill (`#75B7A1`) paired with deep green text (`#171B19`), using full pill or `14px` corner radii. No drop shadow. Hover gently dims to `opacity: 0.92`.
  - *Secondary / Ghost:* Background `#272D29`, border `1px solid #343C37`, text `#E9EEE9`.
  - *Warm Highlight Action:* Background `#3A2B28`, text `#D09A8C`, border `1px solid rgba(208, 154, 140, 0.2)`.

- **Cards & Journal Tiles:**
  - Base fill `#202522`, wrapped in a subtle `1px solid #343C37` stroke, with a continuous `20px` corner radius.
  - Generous internal padding (`space-lg` / `24px`).
  - Active or hovered cards transition subtly to `#272D29` without physical lift.

- **Input Fields & Writing Areas:**
  - Input background `#202522` with border `#343C37`. Focused states transition the border softly to `#75B7A1` without glowing box-shadow rings.
  - Placeholder text styled in `#7D8982`.
  - Main free-form journaling view strips borders completely, floating bare on the canvas with a tranquil blinking sage caret.

- **Chips & Mood Selectors:**
  - Inactive chips feature `#272D29` background with `#AAB5AE` label.
  - Selected chips take either `#263D35` with `#75B7A1` text (calm botanical state) or `#3A2B28` with `#D09A8C` text (warm emotional state).
  - Pill-shaped (`9999px` radius) with `8px 16px` padding.

- **Checkboxes & Radios:**
  - Unchecked: `#202522` fill with `1.5px solid #343C37`.
  - Checked: Solid `#75B7A1` fill with `#171B19` checkmark icon. Smooth 150ms ease-in-out transition.

- **Lists & Dividers:**
  - List items are padded comfortably (`16px 0`). Dividers use `#2B322E` hairline borders.

- **Prompts & Quote Callouts:**
  - Grounded containers using `#272D29` with a gentle left accent strip (`3px solid #D09A8C`), providing warm grounding for daily journaling prompts.