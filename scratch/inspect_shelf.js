const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const idx = html.indexOf('function renderReadingShelf');
if (idx !== -1) {
  console.log(html.substring(idx, idx + 2500));
} else {
  console.log('renderReadingShelf not found, searching shelf...');
  const m = html.match(/reading-shelf-grid[\s\S]*?(?=<\/div>)/);
  if (m) console.log(m[0]);
}
