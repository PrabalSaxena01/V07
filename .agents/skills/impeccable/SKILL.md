---
name: impeccable
description: >-
  Use this skill to audit, polish, and harden frontend user experience (UX). Eliminates friction, enforces zero text breakage, guarantees robust input sanitization, responsive layout scaling, touch-target ergonomics, 24 design commands, and purposeful micro-interactions.
---

# Impeccable: High-Precision Frontend UX & Hardening

> Impeccable is the frontend discipline of eliminating rough edges, broken edge cases, and clunky interactions. It transforms rough code into production-grade, delightfully smooth interfaces.

---

## 1. THE 24 IMPECCABLE DESIGN COMMANDS

When evaluating or refining frontend components, invoke or apply these commands:

| Command | Action & Purpose |
|---|---|
| `/impeccable craft` | Full shape-then-build flow with iterative visual refinement. |
| `/impeccable init` | Record durable product truth into `PRODUCT.md` (audience, goals, voice, constraints). |
| `/impeccable document` | Generate root `DESIGN.md` from existing codebase tokens and styles. |
| `/impeccable extract` | Pull reusable components, variables, and tokens into the design system. |
| `/impeccable shape` | Plan UX/UI structure, information hierarchy, and layout before writing code. |
| `/impeccable critique` | UX review: evaluate visual hierarchy, clarity, scanability, and emotional resonance. |
| `/impeccable audit` | Run technical quality checks (WCAG AA contrast, performance, responsive layout). |
| `/impeccable polish` | Final pass: align tokens, tighten spacing, refine 1px borders, verify shipping readiness. |
| `/impeccable bolder` | Amplify quiet or boring designs with deliberate contrast and scale. |
| `/impeccable quieter` | Tone down noisy, over-decorated, or cluttered designs. |
| `/impeccable distill` | Strip non-essential elements down to the pure functional core. |
| `/impeccable harden` | Error handling, text overflow guards, API sanitization, extreme input boundary checks. |
| `/impeccable onboard` | Design first-run flows, intuitive empty states, and user activation paths. |
| `/impeccable animate` | Add purposeful, physics-based motion with appropriate easing curves. |
| `/impeccable colorize` | Introduce strategic accent colors with strict functional meaning. |
| `/impeccable typeset` | Calibrate typeface pairings, font scales, line-heights, and tracking. |
| `/impeccable layout` | Optimize layout grids, margin consistency, and visual rhythm. |
| `/impeccable delight` | Inject tasteful, satisfying micro-interactions and tactile moments. |
| `/impeccable overdrive` | Add technically extraordinary procedural or shader effects where appropriate. |
| `/impeccable clarify` | Rewrite ambiguous UX copy, micro-labels, and error messages. |
| `/impeccable adapt` | Re-architect surfaces for extreme viewport sizes (smartphones to ultrawide). |
| `/impeccable optimize` | Eliminate layout shifts, reduce DOM depth, and optimize render cycles. |
| `/impeccable live` | Visual variant mode: iterate and evaluate design alternatives in realtime. |
| `/impeccable generate` | Generate clean programmatic UI variants for selection. |

---

## 2. THE IMPECCABLE COMMANDMENTS (HARD RULES)

### 1. Zero Broken Text (Absolute Production Law)
External API data (e.g. Open Library, Google Books, LLMs, RSS feeds) is notorious for dirty formatting:
- Raw HTML tags (`<p>`, `<b>`, `<i>`, `<br>`, `<span>`)
- Unescaped HTML entities (`&quot;`, `&#39;`, `&amp;`, `&hellip;`)
- Nested objects (`{ value: "..." }`) or `null` / `undefined` coerced to strings (`"null"`)

**Mandatory Sanitization Implementation**:
```javascript
function cleanText(input) {
  if (input == null) return '';
  if (typeof input === 'object') {
    if (input.value) input = input.value;
    else if (Array.isArray(input)) input = input.join(', ');
    else input = '';
  }
  var str = String(input).replace(/<[^>]+>/g, ' ');
  var txt = document.createElement('textarea');
  txt.innerHTML = str;
  return txt.value.replace(/\s+/g, ' ').trim();
}
```

**Text Container Overflow Discipline**:
Every container displaying dynamic text liable to vary in length must explicitly define truncation:
```css
/* Single-line truncation */
.truncate-single {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Multi-line clamp */
.truncate-multi {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
```

### 2. Ergonomic Touch Targets (Mobile & Touch First)
- Minimum tap target: **44px × 44px** (or minimum `8px 14px` padding).
- Reset mobile touch callouts:
  ```css
  -webkit-tap-highlight-color: transparent;
  user-select: none; /* on interactive buttons and chips */
  ```
- Instant tactile feedback on touch:
  ```css
  .btn:active, .card-press:active {
    transform: scale(0.97);
    transition: transform 0.08s ease;
  }
  ```

### 3. Graceful Asset Fallbacks (Zero Broken Images)
- Never allow a broken image icon to render.
- Always provide an inline vector SVG fallback:
  ```javascript
  const FALLBACK_SVG = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 140" fill="%23151821"><rect width="100" height="140" rx="4" stroke="%23262C3E"/><text x="50" y="70" fill="%2364748B" font-size="10" text-anchor="middle">NO COVER</text></svg>';
  ```
  ```html
  <img src="cover.jpg" onerror="this.onerror=null; this.src=FALLBACK_SVG;">
  ```

### 4. Optimistic UI Updates & Responsive Feedback
- Trigger state updates immediately when user acts (e.g. page counts, toggles, task completion).
- Sync with persistent storage or backend asynchronously.
- Provide clear, non-intrusive feedback toasts (auto-dismissing in `1800ms` - `2400ms`).

---

## 3. THE 4 OPERATIONAL MODES

### Mode A: Audit
1. **WCAG AA Contrast**: Minimum `4.5:1` for body text, `3.0:1` for large text (18px+).
2. **Viewport Resiliency**: Test layout at `320px` (small phone), `390px` (standard phone), `768px` (tablet), and desktop.
3. **Empty States**: Never display a blank, empty card. Provide purposeful empty states with clear iconography and a direct CTA ("Search for a book", "Add a habit").

### Mode B: Polish
1. **Optical Centering**: Ensure icon glyphs and text labels are aligned optically (`display: flex; align-items: center; gap: 6px`).
2. **Spacing Grid Scale**: Constrain all padding and margins to standard tokens (`4px`, `8px`, `12px`, `16px`, `24px`).
3. **Refined Borders**: Replace heavy, intrusive borders with crisp `1px` lines using subtle rgba opacity (`rgba(255, 255, 255, 0.08)` on dark; `rgba(0, 0, 0, 0.08)` on light).

### Mode C: Harden
1. **Extreme Bounds**: Test title with 300 characters, page count of 0, negative values, and non-numeric inputs.
2. **Network Resilience**: Wrap all network calls in `try/catch` with a fallback UI state if offline or timed out.
3. **Storage Quota & Parsing**: Always wrap `localStorage.getItem()` in `JSON.parse` try/catch blocks and guard `setItem()` against quota exhaustion.

### Mode D: Delight
1. **Physics Easing**: Use cubic bezier curves (`cubic-bezier(0.16, 1, 0.3, 1)`) for sheets and modals. Avoid dated bouncy elastic easing.
2. **Smooth Progress**: Ensure meter and progress bars transition smoothly (`transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1)`).

---

## 4. ANTI-PATTERN DETECTOR RULES

- **No Overused Fonts**: Reject default un-curated typefaces.
- **No Untinted Pure Grays**: Always tint grays slightly toward the background temperature (cool slate or warm stone).
- **No Gray Text on Colored Backgrounds**: On saturated colored buttons/badges, text must be solid white or solid black to maintain contrast.
- **No Nested Card Hell**: Never nest cards inside cards inside cards. Use subtle hairlines (`border-t`), divider lines, or negative space for grouping.
- **No Unlabeled Icon Buttons**: All icon-only buttons must include an accessible `title` and `aria-label`.
