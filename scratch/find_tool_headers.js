const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /<div\s+id=["'](?:tool-view-|tool-)[^"']*["']/gi;
let match;
while ((match = regex.exec(html)) !== null) {
  const start = match.index;
  const chunk = html.substring(start, start + 600);
  console.log('--- TOOL VIEW HEADER ---');
  console.log(chunk.substring(0, chunk.indexOf('</header>') + 9 || 400).replace(/\s+/g, ' '));
}
