const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const navIdx = html.indexOf('<nav');
console.log('--- BOTTOM NAV ---');
console.log(html.substring(navIdx, navIdx + 1500));
