const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /DOMContentLoaded|window\.onload|async function init|function initApp/g;
let match;
while ((match = regex.exec(html)) !== null) {
  const start = Math.max(0, match.index - 50);
  const end = Math.min(html.length, match.index + 350);
  console.log('--- occurrence ---');
  console.log(html.substring(start, end));
}
