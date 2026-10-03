const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.resolve('app/src/main/assets/v3/index.html'), 'utf8');

const headIdx = html.indexOf('<head>');
const headEnd = html.indexOf('</head>');
console.log('--- HEAD CONTENT ---');
console.log(html.substring(headIdx, headEnd + 7));
