const fs = require('fs');
const path = require('path');
const vm = require('vm');

// Contrast ratio calculator
function hexToRgb(hex) {
  const clean = hex.replace('#', '');
  const r = parseInt(clean.substring(0, 2), 16);
  const g = parseInt(clean.substring(2, 4), 16);
  const b = parseInt(clean.substring(4, 6), 16);
  return [r, g, b];
}

function luminance(r, g, b) {
  const a = [r, g, b].map(v => {
    v /= 255;
    return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
  });
  return a[0] * 0.2126 + a[1] * 0.7152 + a[2] * 0.0722;
}

function contrastRatio(hex1, hex2) {
  const [r1, g1, b1] = hexToRgb(hex1);
  const [r2, g2, b2] = hexToRgb(hex2);
  const l1 = luminance(r1, g1, b1) + 0.05;
  const l2 = luminance(r2, g2, b2) + 0.05;
  return l1 > l2 ? l1 / l2 : l2 / l1;
}

console.log('=== THEME CONTRAST RATIO AUDIT ===');

const lightTests = [
  { name: 'Light Surface vs On-Surface', bg: '#F8F9F5', fg: '#1A211D' },
  { name: 'Light Container-Lowest (Card) vs On-Surface', bg: '#FFFFFF', fg: '#1A211D' },
  { name: 'Light Container-Lowest (Card) vs On-Surface-Variant', bg: '#FFFFFF', fg: '#47534C' },
  { name: 'Light Primary Button vs On-Primary (White)', bg: '#22634E', fg: '#FFFFFF' },
  { name: 'Light Primary-Container vs On-Primary-Container', bg: '#D8EFE4', fg: '#0E382A' },
  { name: 'Light Secondary vs On-Secondary (White)', bg: '#98594B', fg: '#FFFFFF' }
];

console.log('\n--- Light Theme ("Warm Alabaster & Forest Spruce") ---');
lightTests.forEach(t => {
  const cr = contrastRatio(t.bg, t.fg);
  console.log(`${cr >= 4.5 ? '✓' : '✕'} ${t.name}: ${cr.toFixed(2)}:1 (Min 4.5:1)`);
});

const darkTests = [
  { name: 'Dark Surface vs On-Surface', bg: '#111513', fg: '#EDF3EE' },
  { name: 'Dark Container-Low (Card) vs On-Surface', bg: '#1A211D', fg: '#EDF3EE' },
  { name: 'Dark Container-Low (Card) vs On-Surface-Variant', bg: '#1A211D', fg: '#A0B0A7' },
  { name: 'Dark Primary Button vs On-Primary', bg: '#50BF8E', fg: '#071E14' },
  { name: 'Dark Primary-Container vs On-Primary-Container', bg: '#1A3F2F', fg: '#C8F2DE' },
  { name: 'Dark Secondary vs On-Secondary', bg: '#E29F8C', fg: '#2A130D' }
];

console.log('\n--- Dark Theme ("Obsidian Slate & Luminous Sage Jade") ---');
darkTests.forEach(t => {
  const cr = contrastRatio(t.bg, t.fg);
  console.log(`${cr >= 4.5 ? '✓' : '✕'} ${t.name}: ${cr.toFixed(2)}:1 (Min 4.5:1)`);
});
