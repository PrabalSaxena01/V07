const fs = require('fs');
const path = require('path');
const vm = require('vm');

const filePath = path.resolve('app/src/main/assets/v3/index.html');
let content = fs.readFileSync(filePath, 'utf8');

const isCRLF = content.includes('\r\n');
const EOL = isCRLF ? '\r\n' : '\n';

function replaceNormalized(src, target, replacement) {
  const normTarget = target.replace(/\r\n/g, '\n');
  const normSrc = src.replace(/\r\n/g, '\n');
  if (normSrc.includes(normTarget)) {
    const normReplacement = replacement.replace(/\r\n/g, '\n');
    const replaced = normSrc.replace(normTarget, normReplacement);
    return isCRLF ? replaced.replace(/\n/g, '\r\n') : replaced;
  }
  return src;
}

// 1. Replace CSS Color Palette
const currentStylePalette = `    :root {
      /* Warm Paper (Light Mode) Palette - Calm Space System */
      --surface: #f3fbf5;
      --surface-dim: #d3dcd6;
      --surface-bright: #ffffff;
      --surface-container-lowest: #ffffff;
      --surface-container-low: #edf6ef;
      --surface-container: #e7f0e9;
      --surface-container-subtle: #edf6ef;
      --surface-container-high: #e2eae4;
      --surface-container-highest: #dce5de;
      --on-surface: #151d19;
      --on-surface-variant: #3f4945;
      --on-surface-muted: #6f7975;
      --inverse-surface: #2a322e;
      --inverse-on-surface: #eaf3ec;
      --outline: #6f7975;
      --outline-variant: #bfc9c4;
      --surface-tint: #266958;
      --primary: #1c6150;
      --on-primary: #ffffff;
      --primary-container: #397a68;
      --on-primary-container: #c6ffeb;
      --inverse-primary: #92d4be;
      --secondary: #855144;
      --on-secondary: #ffffff;
      --secondary-container: #fdb9a9;
      --on-secondary-container: #79473b;
      --secondary-fixed: #ffdad2;
      --secondary-fixed-dim: #fab6a6;
      --on-secondary-fixed: #341007;
      --tertiary: #535855;
      --tertiary-container: #6b706d;
      --tertiary-fixed: #dfe3e0;
      --tertiary-fixed-dim: #c3c7c4;
      --error: #ba1a1a;
      --on-error: #ffffff;
      --error-container: #ffdad6;
      --on-error-container: #93000a;
      --primary-fixed: #adf0da;
      --primary-fixed-dim: #92d4be;
      --on-primary-fixed: #002019;
      --surface-variant: #dce5de;
      --nav-shadow: 0 10px 30px -4px rgba(32, 40, 36, 0.1), 0 3px 8px -2px rgba(32, 40, 36, 0.05);
      --card-shadow: 0 1px 6px -1px rgba(32, 40, 36, 0.06), 0 1px 2px 0 rgba(32, 40, 36, 0.03);
    }

    html.dark {
      /* Night Ink (Dark Mode) Palette - Paper & Ink After Sunset */
      --surface: #171b19;
      --surface-dim: #101412;
      --surface-bright: #272d29;
      --surface-container-lowest: #202522;
      --surface-container-low: #202522;
      --surface-container: #202522;
      --surface-container-subtle: #272d29;
      --surface-container-high: #272d29;
      --surface-container-highest: #343c37;
      --on-surface: #e9eee9;
      --on-surface-variant: #aab5ae;
      --on-surface-muted: #7d8982;
      --inverse-surface: #e9eee9;
      --inverse-on-surface: #171b19;
      --outline: #343c37;
      --outline-variant: #272d29;
      --surface-tint: #75b7a1;
      --primary: #75b7a1;
      --on-primary: #101412;
      --primary-container: #75b7a1;
      --on-primary-container: #101412;
      --inverse-primary: #1c6150;
      --secondary: #d09a8c;
      --on-secondary: #171b19;
      --secondary-container: #3a2b28;
      --on-secondary-container: #d09a8c;
      --secondary-fixed: #3a2b28;
      --secondary-fixed-dim: #4b271d;
      --on-secondary-fixed: #d09a8c;
      --tertiary: #aab5ae;
      --tertiary-container: #272d29;
      --tertiary-fixed: #272d29;
      --tertiary-fixed-dim: #202522;
      --error: #ffb4ab;
      --on-error: #690005;
      --error-container: #93000a;
      --on-error-container: #ffdad6;
      --primary-fixed: #263d35;
      --primary-fixed-dim: #75b7a1;
      --on-primary-fixed: #e9eee9;
      --surface-variant: #272d29;
      --nav-shadow: 0 10px 30px -4px rgba(0, 0, 0, 0.45), 0 3px 8px -2px rgba(0, 0, 0, 0.3);
      --card-shadow: 0 1px 6px -1px rgba(0, 0, 0, 0.3), 0 1px 2px 0 rgba(0, 0, 0, 0.18);
    }`;

const elevatedStylePalette = `    :root {
      /* Warm Alabaster & Forest Spruce (Light Mode Sanctuary Palette) */
      --surface: #F8F9F5;
      --surface-dim: #E9ECE5;
      --surface-bright: #FFFFFF;
      --surface-container-lowest: #FFFFFF;
      --surface-container-low: #F0F3EE;
      --surface-container: #E7ECE5;
      --surface-container-subtle: #EDF1EB;
      --surface-container-high: #DFE5DC;
      --surface-container-highest: #D4DCD1;
      --on-surface: #1A211D;
      --on-surface-variant: #47534C;
      --on-surface-muted: #728078;
      --inverse-surface: #28312B;
      --inverse-on-surface: #F0F4EF;
      --outline: #76847C;
      --outline-variant: #C8D2CA;
      --surface-tint: #22634E;
      --primary: #22634E;
      --on-primary: #FFFFFF;
      --primary-container: #D8EFE4;
      --on-primary-container: #0E382A;
      --inverse-primary: #86D7B4;
      --secondary: #98594B;
      --on-secondary: #FFFFFF;
      --secondary-container: #FADFD7;
      --on-secondary-container: #451C13;
      --secondary-fixed: #FCECE7;
      --secondary-fixed-dim: #F5CEBF;
      --on-secondary-fixed: #53231A;
      --tertiary: #505E56;
      --tertiary-container: #DCE5DE;
      --tertiary-fixed: #E2EAE3;
      --tertiary-fixed-dim: #C6D2C8;
      --error: #BA2727;
      --on-error: #FFFFFF;
      --error-container: #FFDAD6;
      --on-error-container: #93000A;
      --primary-fixed: #DBEFE5;
      --primary-fixed-dim: #B5DEC9;
      --on-primary-fixed: #103B2D;
      --surface-variant: #DCE4DC;
      --nav-shadow: 0 10px 30px -4px rgba(26, 33, 29, 0.08), 0 3px 8px -2px rgba(26, 33, 29, 0.04);
      --card-shadow: 0 1px 4px -1px rgba(26, 33, 29, 0.06), 0 1px 2px 0 rgba(26, 33, 29, 0.03);
    }

    html.dark {
      /* Obsidian Slate & Luminous Sage Jade (Dark Mode Sanctuary Palette) */
      --surface: #111513;
      --surface-dim: #0D100E;
      --surface-bright: #222A26;
      --surface-container-lowest: #151B18;
      --surface-container-low: #1A211D;
      --surface-container: #202824;
      --surface-container-subtle: #242E29;
      --surface-container-high: #29342E;
      --surface-container-highest: #334139;
      --on-surface: #EDF3EE;
      --on-surface-variant: #A0B0A7;
      --on-surface-muted: #718278;
      --inverse-surface: #EDF3EE;
      --inverse-on-surface: #111513;
      --outline: #38473E;
      --outline-variant: #232D27;
      --surface-tint: #50BF8E;
      --primary: #50BF8E;
      --on-primary: #071E14;
      --primary-container: #1A3F2F;
      --on-primary-container: #C8F2DE;
      --inverse-primary: #22634E;
      --secondary: #E29F8C;
      --on-secondary: #2A130D;
      --secondary-container: #47271F;
      --on-secondary-container: #FAD8CF;
      --secondary-fixed: #3F221B;
      --secondary-fixed-dim: #562E25;
      --on-secondary-fixed: #FAD8CF;
      --tertiary: #A0B0A7;
      --tertiary-container: #29342E;
      --tertiary-fixed: #29342E;
      --tertiary-fixed-dim: #202824;
      --error: #FFB4AB;
      --on-error: #690005;
      --error-container: #93000A;
      --on-error-container: #FFDAD6;
      --primary-fixed: #1A3F2F;
      --primary-fixed-dim: #50BF8E;
      --on-primary-fixed: #E2F7EE;
      --surface-variant: #29342E;
      --nav-shadow: 0 10px 30px -4px rgba(0, 0, 0, 0.6), 0 3px 8px -2px rgba(0, 0, 0, 0.4);
      --card-shadow: 0 1px 6px -1px rgba(0, 0, 0, 0.4), 0 1px 2px 0 rgba(0, 0, 0, 0.25);
    }`;

const c1 = replaceNormalized(content, currentStylePalette, elevatedStylePalette);
if (c1 !== content) {
  content = c1;
  console.log('✓ Replaced CSS Palette with Alabaster Linen & Obsidian Sage Jade tokens');
} else {
  console.error('Failed to replace CSS style palette!');
  process.exit(1);
}

// 2. Add immediate head theme restore script to prevent theme flash
const headThemeScript = `  <script>
    (function() {
      try {
        var saved = localStorage.getItem('v07_app_theme');
        if (saved === 'dark' || (!saved && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
          document.documentElement.classList.add('dark');
        } else {
          document.documentElement.classList.remove('dark');
        }
      } catch (_) {}
    })();
  </script>`;

if (!content.includes('v07_app_theme')) {
  // Inject right after <head>
  const headIdx = content.indexOf('<head>') + 6;
  content = content.substring(0, headIdx) + EOL + headThemeScript + content.substring(headIdx);
  console.log('✓ Injected immediate head theme restore script');
}

// 3. Update FALLBACK_BOOK_COVER to match the new colors
const oldFallback = `const FALLBACK_BOOK_COVER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 140" width="100" height="140"><rect width="100" height="140" rx="8" fill="%232D3748"/><path d="M35 50h30M35 65h30M35 80h20" stroke="%23A0AEC0" stroke-width="3" stroke-linecap="round"/><text x="50" y="112" fill="%23CBD5E0" font-size="10" font-family="sans-serif" font-weight="bold" text-anchor="middle">SANCTUARY</text></svg>';`;
const newFallback = `const FALLBACK_BOOK_COVER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 140" width="100" height="140"><rect width="100" height="140" rx="8" fill="%23202824"/><path d="M35 48h30M35 62h30M35 76h20" stroke="%2350BF8E" stroke-width="2.5" stroke-linecap="round"/><text x="50" y="108" fill="%23A0B0A7" font-size="9" font-family="sans-serif" font-weight="700" letter-spacing="1" text-anchor="middle">SANCTUARY</text></svg>';`;

content = replaceNormalized(content, oldFallback, newFallback);
console.log('✓ Updated FALLBACK_BOOK_COVER SVG styling');

// 4. Update setAppTheme to store in localStorage and update card borders cleanly
const oldSetTheme = `function setAppTheme(theme) {
      const isDark = theme === 'dark';
      document.documentElement.classList.toggle('dark', isDark);

      // Update check icons in Settings
      const checkLight = document.getElementById('settings-check-light');
      const checkDark = document.getElementById('settings-check-dark');
      const cardLight = document.getElementById('settings-theme-light');
      const cardDark = document.getElementById('settings-theme-dark');

      if (checkLight && checkDark && cardLight && cardDark) {
        if (isDark) {
          checkDark.classList.remove('opacity-0');
          checkLight.classList.add('opacity-0');
          cardDark.classList.add('border-primary');
          cardDark.classList.remove('border-surface-container-high');
          cardLight.classList.remove('border-primary/50');
          cardLight.classList.add('border-surface-container-high');
        } else {
          checkLight.classList.remove('opacity-0');
          checkDark.classList.add('opacity-0');
          cardLight.classList.add('border-primary/50');
          cardLight.classList.remove('border-surface-container-high');
          cardDark.classList.remove('border-primary');
          cardDark.classList.add('border-surface-container-high');
        }
      }

      nativeInvoke('storage:set', { key: 'app_theme', value: theme });
      showToast(isDark ? 'Night Ink theme active' : 'Warm Paper theme active', 'Theme preference stored locally', 'info');
    }`;

const newSetTheme = `function setAppTheme(theme) {
      const isDark = theme === 'dark';
      document.documentElement.classList.toggle('dark', isDark);

      try {
        localStorage.setItem('v07_app_theme', theme);
      } catch (_) {}

      // Update check icons in Settings
      const checkLight = document.getElementById('settings-check-light');
      const checkDark = document.getElementById('settings-check-dark');
      const cardLight = document.getElementById('settings-theme-light');
      const cardDark = document.getElementById('settings-theme-dark');

      if (checkLight && checkDark && cardLight && cardDark) {
        if (isDark) {
          checkDark.classList.remove('opacity-0');
          checkLight.classList.add('opacity-0');
          cardDark.classList.add('border-primary', 'ring-1', 'ring-primary/40');
          cardDark.classList.remove('border-surface-container-high');
          cardLight.classList.remove('border-primary', 'ring-1', 'ring-primary/40');
          cardLight.classList.add('border-surface-container-high');
        } else {
          checkLight.classList.remove('opacity-0');
          checkDark.classList.add('opacity-0');
          cardLight.classList.add('border-primary', 'ring-1', 'ring-primary/40');
          cardLight.classList.remove('border-surface-container-high');
          cardDark.classList.remove('border-primary', 'ring-1', 'ring-primary/40');
          cardDark.classList.add('border-surface-container-high');
        }
      }

      nativeInvoke('storage:set', { key: 'app_theme', value: theme });
      showToast(isDark ? 'Night Ink theme active' : 'Warm Alabaster theme active', 'Sanctuary palette updated', 'info');
    }`;

content = replaceNormalized(content, oldSetTheme, newSetTheme);
console.log('✓ Updated setAppTheme with localStorage and refined border rings');

// 5. Update DOMContentLoaded to sync theme checkmarks
const domInitTarget = `updatePlayButtons(false);`;
const domInitReplacement = `updatePlayButtons(false);

      // Sync active theme indicator in Settings
      try {
        const curTheme = document.documentElement.classList.contains('dark') ? 'dark' : 'light';
        const chkL = document.getElementById('settings-check-light');
        const chkD = document.getElementById('settings-check-dark');
        const cdL = document.getElementById('settings-theme-light');
        const cdD = document.getElementById('settings-theme-dark');
        if (chkL && chkD && cdL && cdD) {
          if (curTheme === 'dark') {
            chkD.classList.remove('opacity-0');
            chkL.classList.add('opacity-0');
            cdD.classList.add('border-primary', 'ring-1', 'ring-primary/40');
            cdD.classList.remove('border-surface-container-high');
            cdL.classList.remove('border-primary', 'ring-1', 'ring-primary/40');
            cdL.classList.add('border-surface-container-high');
          } else {
            chkL.classList.remove('opacity-0');
            chkD.classList.add('opacity-0');
            cdL.classList.add('border-primary', 'ring-1', 'ring-primary/40');
            cdL.classList.remove('border-surface-container-high');
            cdD.classList.remove('border-primary', 'ring-1', 'ring-primary/40');
            cdD.classList.add('border-surface-container-high');
          }
        }
      } catch (_) {}`;

content = replaceNormalized(content, domInitTarget, domInitReplacement);
console.log('✓ Injected DOMContentLoaded theme indicator sync');

// 6. Validate JavaScript syntax with Node VM
console.log('\nValidating JavaScript syntax across all <script> blocks...');
const scriptBlocks = content.match(/<script\b[^>]*>([\s\S]*?)<\/script>/gi) || [];
scriptBlocks.forEach((sb, idx) => {
  const code = sb.replace(/<script\b[^>]*>/i, '').replace(/<\/script>/i, '');
  try {
    new vm.Script(code);
    console.log(`✓ Script block ${idx + 1} (${(code.length / 1024).toFixed(1)} KB) syntax is 100% VALID`);
  } catch (err) {
    console.error(`✕ Syntax error in script block ${idx + 1}:`, err.message);
    process.exit(1);
  }
});

fs.writeFileSync(filePath, content, 'utf8');
console.log(`\nSuccessfully applied elevated theme palette to ${filePath} (${(content.length / 1024).toFixed(1)} KB)`);
