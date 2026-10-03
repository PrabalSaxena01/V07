const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /function\s+escapeHtml/g;
let match;
while ((match = regex.exec(html)) !== null) {
  const start = match.index;
  console.log(html.substring(start, start + 300));
}
