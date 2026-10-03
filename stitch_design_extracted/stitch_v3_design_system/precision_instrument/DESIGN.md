---
name: Precision Instrument
colors:
  surface: '#151821'
  surface-dim: '#111318'
  surface-bright: '#37393f'
  surface-container-lowest: '#0c0e13'
  surface-container-low: '#1a1b21'
  surface-container: '#1e1f25'
  surface-container-high: '#282a2f'
  surface-container-highest: '#33353a'
  on-surface: '#e2e2e9'
  on-surface-variant: '#bdc8d1'
  inverse-surface: '#e2e2e9'
  inverse-on-surface: '#2e3036'
  outline: '#87929a'
  outline-variant: '#3e484f'
  surface-tint: '#7bd0ff'
  primary: '#8ed5ff'
  on-primary: '#00354a'
  primary-container: '#38bdf8'
  on-primary-container: '#004965'
  inverse-primary: '#00668a'
  secondary: '#ccbeff'
  on-secondary: '#350097'
  secondary-container: '#4d20bc'
  on-secondary-container: '#bba9ff'
  tertiary: '#ffc176'
  on-tertiary: '#472a00'
  tertiary-container: '#f1a02b'
  on-tertiary-container: '#613b00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c4e7ff'
  primary-fixed-dim: '#7bd0ff'
  on-primary-fixed: '#001e2c'
  on-primary-fixed-variant: '#004c69'
  secondary-fixed: '#e7deff'
  secondary-fixed-dim: '#ccbeff'
  on-secondary-fixed: '#1f0060'
  on-secondary-fixed-variant: '#4d20bc'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb960'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#111318'
  on-background: '#e2e2e9'
  surface-variant: '#33353a'
  surface-subtle: '#1D2230'
  border: '#262C3E'
  border-subtle: rgba(255, 255, 255, 0.07)
  text-primary: '#F1F5F9'
  text-secondary: '#94A3B8'
  text-muted: '#64748B'
  success: '#4DE086'
  warning: '#FFB84D'
  danger: '#EF4444'
  info: '#38BDF8'
  tasks-accent: '#5CE1E6'
  habits-accent: '#FF9F43'
  reader-accent: '#8D6CFF'
  timetracker-accent: '#A78BFA'
typography:
  display:
    fontFamily: Inter
    fontSize: 2.25rem
    fontWeight: '600'
    lineHeight: 2.75rem
    letterSpacing: -0.025em
  display-mobile:
    fontFamily: Inter
    fontSize: 1.75rem
    fontWeight: '600'
    lineHeight: 2.25rem
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 1.125rem
    fontWeight: '500'
    lineHeight: 1.5rem
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.25rem
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1rem
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 0.8125rem
    fontWeight: '500'
    lineHeight: 1.125rem
    letterSpacing: 0.02em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 0.6875rem
    fontWeight: '500'
    lineHeight: 0.875rem
    letterSpacing: 0.04em
  metric-lg:
    fontFamily: JetBrains Mono
    fontSize: 1.75rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: -0.03em
  metric-md:
    fontFamily: JetBrains Mono
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.5rem
    letterSpacing: -0.02em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

# V3 DESIGN SYSTEM
## Crisp Vector Product UI & Impeccable UX
> **Purpose:** Define the visual language, interaction principles, component rules, and UX quality bar for V3.
> This document is a design system and product-quality contract.

# 1. DESIGN PHILOSOPHY
The design language is crisp, geometric, editorial, information-dense where useful, spacious where clarity requires it, high contrast, restrained, functional, visually distinctive without being decorative.
No generic AI-generated UI patterns (no pastel gradients, no purple/blue AI glows, no glassmorphism, no floating blobs, no card soup, no pill-shaped everything).

# 3. VISUAL IDENTITY
Core Character: Precision + Utility + Calm + Intelligence. Closer to an instrument, a command center, an editorial system, or technical notebook.

# 4. COLOR SYSTEM
## Dark Theme (Primary)
background: #0D0F14
surface: #151821
surface-subtle: #1D2230
border: #262C3E
border-subtle: rgba(255,255,255,0.07)
text-primary: #F1F5F9
text-secondary: #94A3B8
text-muted: #64748B

## Semantic Accents
success: #4DE086
warning: #FFB84D
danger: #EF4444
info: #38BDF8
tasks-accent: #5CE1E6
habits-accent: #FF9F43
reader-accent: #8D6CFF
timetracker-accent: #A78BFA

# 6. TYPOGRAPHY
Primary: -apple-system, BlinkMacSystemFont, "Segoe UI", Inter, Roboto, sans-serif
Data / Metrics: "Space Mono", "JetBrains Mono", monospace

# 8. BORDERS & RADIUS
Borders: 1px solid #262C3E
Radius: restrained geometry (4px to 8px max, subtle cards, no pills for everything)
Shadows: subtle and functional, relying on border and contrast first.

# 9. COMPONENTS
shadcn/ui style geometric, composable, precise primitives.
