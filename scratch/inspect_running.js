const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const idx = html.indexOf('function renderRunningList');
if (idx !== -1) {
  console.log(html.substring(idx, idx + 1500));
} else {
  console.log('renderRunningList not found, searching running...');
  const m = html.match(/running-runs-list[\s\S]*?(?=<\/div>)/);
  if (m) console.log(m[0]);
}
