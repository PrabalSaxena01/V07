const fs = require('fs');
const path = require('path');

const content = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const checks = [
  { name: 'cleanText defined', pass: content.includes('function cleanText(input)') },
  { name: 'FALLBACK_BOOK_COVER defined', pass: content.includes('const FALLBACK_BOOK_COVER =') },
  { name: 'featured-book-cover onerror in HTML', pass: content.includes('id="featured-book-cover" class="w-full h-full object-cover" src="" alt="Book cover" onerror=') },
  { name: 'renderReadingView fallback cover', pass: content.includes('coverImg.src = FALLBACK_BOOK_COVER;') },
  { name: 'book search cleanText usage', pass: content.includes('cleanText(b.title)') },
  { name: 'book search FALLBACK_BOOK_COVER usage', pass: content.includes('onerror="this.onerror=null; this.src=FALLBACK_BOOK_COVER;"') },
  { name: 'avatar onerror handler', pass: content.includes("this.parentElement.textContent='👤'") },
  { name: 'Day timeline "A Quiet Horizon" empty state', pass: content.includes('A Quiet Horizon') },
  { name: 'Habits "No Rituals Yet" empty state', pass: content.includes('No Rituals Yet') },
  { name: 'Drawer close buttons aria-label', pass: content.includes('aria-label="Close plugin catalog"') && content.includes('aria-label="Close profile drawer"') },
  { name: 'Header notification aria-label', pass: content.includes('aria-label="Notifications & Prompts"') },
  { name: 'Search clear aria-label', pass: content.includes('aria-label="Clear ledger search"') }
];

console.log('=== HARDENING VERIFICATION ===');
checks.forEach(c => {
  console.log(`${c.pass ? '✓' : '✕'} ${c.name}`);
});
