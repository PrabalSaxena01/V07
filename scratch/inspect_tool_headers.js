const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /class=["'][^"']*\btool-view\b[^"']*["']/gi;
let match;
let i = 0;
while ((match = regex.exec(html)) !== null) {
  i++;
  const start = match.index;
  const chunk = html.substring(start, start + 1200);
  const headerEnd = chunk.indexOf('</header>');
  const header = chunk.substring(0, headerEnd + 9);
  console.log(`\n[Tool View ${i}]`);
  console.log(header.replace(/\s+/g, ' '));
}
