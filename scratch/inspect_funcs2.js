const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const sIdx = html.indexOf('async function executeLiveBookSearch');
console.log('=== executeLiveBookSearch 2nd half ===');
console.log(html.substring(sIdx + 1500, sIdx + 3200));

const rIdx = html.indexOf('function renderLedgerStream()');
console.log('\n=== renderLedgerStream full ===');
console.log(html.substring(rIdx, rIdx + 2500));
