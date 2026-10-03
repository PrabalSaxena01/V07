const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

// Find all HTML blocks that render empty states
const emptyPatterns = [
  /books-shelf-list[\s\S]*?No books/i,
  /journal-entries-stream[\s\S]*?Ledger is Quiet/i,
  /day-timeline-events[\s\S]*?No recorded events/i,
  /run-history-stream[\s\S]*?No Runs Logged/i,
  /habits-list[\s\S]*?empty/i,
  /tasks[\s\S]*?empty/i,
  /recycle-bin[\s\S]*?empty/i
];

console.log('--- Checking Empty State implementations ---');

// Let's search for container.innerHTML = `.*?(No|empty|quiet).*?` in scripts
const emptyMatches = html.match(/\.innerHTML\s*=\s*`[\s\S]*?(?:No\s+\w+|empty|Quiet)[\s\S]*?`/gi) || [];
console.log(`Found ${emptyMatches.length} innerHTML assignments with empty state text.`);

emptyMatches.forEach((m, idx) => {
  console.log(`\n[Empty State ${idx + 1}]`);
  console.log(m.substring(0, 400).replace(/\s+/g, ' '));
});
