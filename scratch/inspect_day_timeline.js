const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const idx = html.indexOf('function loadDayTimeline');
console.log(html.substring(idx, idx + 2500));
