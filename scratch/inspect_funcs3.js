const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const rIdx = html.indexOf('function renderLedgerStream()');
console.log(html.substring(rIdx + 1900, rIdx + 3500));
