const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const lines = html.split('\n');
lines.forEach((l, idx) => {
  if (l.trim() === '<script>') {
    console.log(`Line ${idx + 1}: <script>`);
  }
});
