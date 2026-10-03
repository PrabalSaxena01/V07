const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const idx = html.indexOf('min-w-[760px]');
console.log(html.substring(Math.max(0, idx - 200), Math.min(html.length, idx + 400)));
