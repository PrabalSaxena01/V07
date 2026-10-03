const fs = require('fs');
const path = require('path');

const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
let count = 0;
let iconButtons = [];

while ((match = buttonRegex.exec(html)) !== null) {
  count++;
  const attrs = match[1];
  const inner = match[2];
  
  // Extract visible text without html tags
  const textContent = inner.replace(/<[^>]+>/g, '').trim();
  const hasAria = /aria-label\s*=\s*["'][^"']+["']/i.test(attrs);
  const hasTitle = /title\s*=\s*["'][^"']+["']/i.test(attrs);
  const classMatch = attrs.match(/class\s*=\s*["']([^"']+)["']/i);
  const classes = classMatch ? classMatch[1] : '';
  const idMatch = attrs.match(/id\s*=\s*["']([^"']+)["']/i);
  const id = idMatch ? idMatch[1] : '';
  const onclickMatch = attrs.match(/onclick\s*=\s*["']([^"']+)["']/i);
  const onclick = onclickMatch ? onclickMatch[1] : '';

  // An icon button is one where textContent is just an icon name (material symbols glyph name like 'close', 'arrow_back', 'search', 'add', etc.)
  // or where textContent is empty / purely glyphs
  const isLikelyIcon = /^[a-z0-9_]{1,30}$/i.test(textContent) || textContent === '';
  
  if (isLikelyIcon && !hasAria && !hasTitle) {
    iconButtons.push({
      index: count,
      id,
      text: textContent,
      classes: classes.replace(/\s+/g, ' ').trim(),
      onclick: onclick.substring(0, 45)
    });
  }
}

console.log(`Total buttons found: ${count}`);
console.log(`Icon-only buttons WITHOUT aria-label or title: ${iconButtons.length}`);
iconButtons.forEach((b, i) => {
  console.log(`${i+1}. [${b.text || 'EMPTY'}] id="${b.id}" onclick="${b.onclick}" class="${b.classes.substring(0, 60)}"`);
});
