const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const sIdx = html.indexOf('resultsEl.innerHTML = books.map((b, idx)');
console.log(html.substring(sIdx, sIdx + 1200));
