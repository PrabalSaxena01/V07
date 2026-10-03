const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
let count = 0;
while ((match = buttonRegex.exec(html)) !== null) {
  count++;
  const attrs = match[1];
  const inner = match[2];
  const textContent = inner.replace(/<[^>]+>/g, '').trim();
  const hasAria = /aria-label\s*=/i.test(attrs);
  const hasTitle = /title\s*=/i.test(attrs);
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
    if (count <= 31) {
      console.log(`L${lineNum}: [${textContent}] ${attrs.replace(/\s+/g, ' ').trim()}`);
    }
  }
}
