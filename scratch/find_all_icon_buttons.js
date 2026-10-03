const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
let iconOnly = [];

while ((match = buttonRegex.exec(html)) !== null) {
  const attrs = match[1];
  const inner = match[2];
  const textContent = inner.replace(/<[^>]+>/g, '').trim();
  const hasAria = /aria-label\s*=/i.test(attrs);
  const hasTitle = /title\s*=/i.test(attrs);

  // An icon button is one where textContent is just an icon name or empty or non-alphanumeric symbol
  // Examples: "close", "arrow_back", "check", "tune", "edit", "content_copy", "delete", "delete_sweep", "search", "add", "✕"
  const iconGlyphs = new Set([
    'close', 'arrow_back', 'arrow_forward', 'check', 'tune', 'edit', 'content_copy',
    'delete', 'delete_sweep', 'search', 'add', 'menu', 'settings', 'more_horiz', 'more_vert',
    'expand_more', 'expand_less', 'chevron_right', 'chevron_left', 'refresh', '✕', 'event_busy',
    'history_edu', 'sprint', 'bookmark', 'share', 'sync', 'close'
  ]);
  
  if ((iconGlyphs.has(textContent.toLowerCase()) || textContent === '') && (!hasAria || !hasTitle)) {
    let charIndex = match.index;
    let lineNum = 1;
    for (let i = 0; i < charIndex; i++) {
      if (html[i] === '\n') lineNum++;
    }
    iconOnly.push({
      lineNum,
      textContent,
      attrs: attrs.replace(/\s+/g, ' ').trim(),
      inner: inner.replace(/\s+/g, ' ').trim(),
      full: match[0]
    });
  }
}

console.log(`Found ${iconOnly.length} icon-only buttons needing accessible labels / touch target review:`);
iconOnly.forEach((b, i) => {
  console.log(`${i+1}. L${b.lineNum}: glyph="${b.textContent}" | attrs: ${b.attrs}`);
});
