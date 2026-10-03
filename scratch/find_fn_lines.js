const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const lines = html.split('\n');
lines.forEach((line, idx) => {
  if (line.includes('async function executeLiveBookSearch')) {
    console.log(`executeLiveBookSearch starts at line ${idx + 1}`);
  }
  if (line.includes('function renderReadingView')) {
    console.log(`renderReadingView starts at line ${idx + 1}`);
  }
  if (line.includes('function loadDayTimeline')) {
    console.log(`loadDayTimeline starts at line ${idx + 1}`);
  }
});
