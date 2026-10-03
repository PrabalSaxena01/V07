const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

// Let's inspect the buttons that have <span class="material-symbols-outlined">...</span> and NO aria-label
const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
let iconButtons = [];

while ((match = buttonRegex.exec(html)) !== null) {
  const attrs = match[1];
  const inner = match[2];
  const hasAria = /aria-label\s*=/i.test(attrs);
  const isIcon = /<span\b[^>]*material-symbols[^>]*>([a-z0-9_]+)<\/span>/i.test(inner);
  const isCross = /<span\b[^>]*>✕<\/span>|^\s*✕\s*$/i.test(inner);

  if ((isIcon || isCross) && !hasAria) {
    const iconName = isIcon ? inner.match(/<span\b[^>]*material-symbols[^>]*>([a-z0-9_]+)<\/span>/i)[1] : 'close';
    const textWithoutTags = inner.replace(/<[^>]+>/g, '').replace(iconName, '').trim();
    if (textWithoutTags.length === 0) {
      // Truly icon only!
      iconButtons.push({
        full: match[0],
        attrs,
        iconName,
        index: match.index
      });
    }
  }
}

console.log(`Found ${iconButtons.length} truly icon-only buttons without aria-label:`);
iconButtons.forEach((b, i) => {
  console.log(`${i+1}. icon: "${b.iconName}" -> ${b.full.substring(0, 100).replace(/\s+/g, ' ')}`);
});
