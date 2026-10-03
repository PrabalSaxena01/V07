const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const idx = html.indexOf("const coverImg = document.getElementById('featured-book-cover');");
console.log(html.substring(idx - 200, idx + 1500));
