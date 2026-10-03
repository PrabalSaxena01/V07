const fs = require('fs');
const path = require('path');

const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

// Find executeLiveBookSearch
const searchMatch = html.match(/async\s+function\s+executeLiveBookSearch[\s\S]*?(?=async\s+function|function\s+[a-zA-Z0-9_]+\s*\(|\<\/script\>)/);
if (searchMatch) {
  console.log('--- executeLiveBookSearch ---');
  console.log(searchMatch[0].substring(0, 1500));
}

// Find featured-book-cover
const featuredMatch = html.match(/id=["']featured-book-cover["'][^>]*>/);
if (featuredMatch) {
  console.log('\n--- featured-book-cover ---');
  console.log(featuredMatch[0]);
}

// Find loadUnifiedLedger
const ledgerMatch = html.match(/function\s+loadUnifiedLedger[\s\S]*?(?=function\s+[a-zA-Z0-9_]+\s*\(|\<\/script\>)/);
if (ledgerMatch) {
  console.log('\n--- loadUnifiedLedger (first 1000 chars) ---');
  console.log(ledgerMatch[0].substring(0, 1000));
}
