const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const sIdx = html.indexOf('async function executeLiveBookSearch');
console.log('=== executeLiveBookSearch full ===');
console.log(html.substring(sIdx, sIdx + 2200));

const lIdx = html.indexOf('async function loadUnifiedLedger()');
console.log('\n=== loadUnifiedLedger full ===');
console.log(html.substring(lIdx, lIdx + 2500));
