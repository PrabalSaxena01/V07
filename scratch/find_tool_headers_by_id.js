const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /<div\s+id=["'](tool-[^"']+)["']/gi;
let match;
while ((match = regex.exec(html)) !== null) {
  const id = match[1];
  const start = match.index;
  const chunk = html.substring(start, start + 1200);
  const headerMatch = chunk.match(/<header\b[^>]*>([\s\S]*?)<\/header>/i);
  console.log(`\nTool view: ${id}`);
  if (headerMatch) {
    console.log(headerMatch[0].replace(/\s+/g, ' '));
  } else {
    console.log('No <header> tag found directly in first 1200 chars');
  }
}
