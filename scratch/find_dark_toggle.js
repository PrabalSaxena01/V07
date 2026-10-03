const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /classList\.(?:add|toggle|remove)\(['"]dark['"]\)/g;
let match;
while ((match = regex.exec(html)) !== null) {
  const start = Math.max(0, match.index - 100);
  const end = Math.min(html.length, match.index + 200);
  console.log('--- occurrence of dark toggle ---');
  console.log(html.substring(start, end));
}
