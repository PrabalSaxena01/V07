const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const lines = html.split('\n');
const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
let missingLabels = [];

while ((match = buttonRegex.exec(html)) !== null) {
  const attrs = match[1];
  const inner = match[2];
  const textContent = inner.replace(/<[^>]+>/g, '').trim();
  const hasAria = /aria-label\s*=/i.test(attrs);
  const hasTitle = /title\s*=/i.test(attrs);

  // If textContent is an icon name or empty or contains only non-word / symbol
  const isIcon = /^[a-z0-9_]{1,30}$/i.test(textContent) || textContent === '✕' || textContent === '';
  
  if (!hasAria && !hasTitle) {
    // Find line number
    const charIndex = match.index;
    let lineNum = 1;
    for (let i = 0; i < charIndex; i++) {
      if (html[i] === '\n') lineNum++;
    }
    
    missingLabels.push({
      lineNum,
      isIcon,
      textContent,
      snippet: match[0].substring(0, 120).replace(/\s+/g, ' ')
    });
  }
}

console.log(`Total buttons missing aria-label or title: ${missingLabels.length}`);
console.log(`Icon-like buttons missing label: ${missingLabels.filter(b => b.isIcon).length}`);
missingLabels.forEach((b, i) => {
  console.log(`${i+1}. L${b.lineNum} [${b.textContent}] ${b.snippet}`);
});
