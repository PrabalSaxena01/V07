const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const lines = html.split('\n');
console.log('--- Lines 650 - 750 ---');
console.log(lines.slice(650, 750).join('\n'));
