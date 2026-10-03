const fs = require('fs');
const path = require('path');
const vm = require('vm');

const filePath = path.resolve('app/src/main/assets/v3/index.html');
let content = fs.readFileSync(filePath, 'utf8');

const t1 = `<button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">`;
const r1 = `<button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close Weekly Digest" title="Close">`;

if (content.includes(t1)) {
  content = content.replace(t1, r1);
  console.log('✓ Replaced weekly close button');
}

const t2 = `<button onclick="this.parentElement.remove(); showToast('Thought Dismissed', '', 'info')" class="w-6 h-6 rounded flex items-center justify-center text-on-surface-variant hover:text-error cursor-pointer">`;
const r2 = `<button onclick="this.parentElement.remove(); showToast('Thought Dismissed', '', 'info')" class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:text-error hover:bg-error/10 active:scale-90 transition-all cursor-pointer" aria-label="Dismiss thought" title="Dismiss thought">`;

if (content.includes(t2)) {
  content = content.replace(t2, r2);
  console.log('✓ Replaced thought dismiss button');
}

// Verify VM
const scriptBlocks = content.match(/<script\b[^>]*>([\s\S]*?)<\/script>/gi) || [];
scriptBlocks.forEach((sb, idx) => {
  const code = sb.replace(/<script\b[^>]*>/i, '').replace(/<\/script>/i, '');
  new vm.Script(code);
});

fs.writeFileSync(filePath, content, 'utf8');
console.log('✓ Saved changes to', filePath);
