const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /theme|dark/gi;
let match;
const matches = [];
while ((match = regex.exec(html)) !== null) {
  const line = html.substring(Math.max(0, match.index - 40), Math.min(html.length, match.index + 80));
  if (line.includes('function') || line.includes('setTheme') || line.includes('toggleTheme') || line.includes('theme-') || line.includes('applyTheme')) {
    matches.push(line.replace(/\s+/g, ' ').trim());
  }
}
console.log('Theme related functions/lines:', matches.length);
matches.slice(0, 15).forEach(m => console.log('  -', m));
