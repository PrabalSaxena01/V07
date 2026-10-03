const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /class=["'][^"']*\btool-view\b[^"']*["']/gi;
let match;
while ((match = regex.exec(html)) !== null) {
  const start = match.index;
  const chunk = html.substring(start, start + 700);
  const headerEnd = chunk.indexOf('</header>');
  console.log('--- TOOL VIEW HEADER ---');
  console.log(chunk.substring(0, headerEnd !== -1 ? headerEnd + 9 : 400).replace(/\s+/g, ' '));
}
