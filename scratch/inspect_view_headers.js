const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const regex = /<div\s+id=["'](view-[^"']+)["'][^>]*>([\s\S]*?)<\/header>/gi;
let match;
while ((match = regex.exec(html)) !== null) {
  const viewId = match[1];
  const headerContent = match[2];
  console.log(`\n=== Header for ${viewId} ===`);
  const buttons = headerContent.match(/<button[\s\S]*?<\/button>/gi) || [];
  buttons.forEach(b => console.log('  Button:', b.replace(/\s+/g, ' ')));
}
