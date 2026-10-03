const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const bRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
while ((match = bRegex.exec(html)) !== null) {
  const attrs = match[1];
  const inner = match[2].trim();
  const hasAria = /aria-label\s*=/i.test(attrs);
  const hasTitle = /title\s*=/i.test(attrs);
  const isIconOnly = /^<span[^>]*material-symbols[^>]*>[^<]+<\/span>$/i.test(inner.replace(/\s+/g, ' '));
  if (isIconOnly && !hasAria && !hasTitle) {
    const idx = match.index;
    console.log('--- EXACT MATCH ---');
    console.log(JSON.stringify(html.substring(idx, idx + 200)));
  }
}
