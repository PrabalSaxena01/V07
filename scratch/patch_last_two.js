const fs = require('fs');
const path = require('path');
const vm = require('vm');

const filePath = path.resolve('app/src/main/assets/v3/index.html');
let content = fs.readFileSync(filePath, 'utf8');

// 1. Weekly digest close button
const weeklyCloseTarget = `<button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>`;

const weeklyCloseReplacement = `<button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close Weekly Digest" title="Close">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>`;

function replaceNorm(src, t, r) {
  const normT = t.replace(/\r\n/g, '\n');
  const normS = src.replace(/\r\n/g, '\n');
  if (normS.includes(normT)) {
    const normR = r.replace(/\r\n/g, '\n');
    const replaced = normS.replace(normT, normR);
    return src.includes('\r\n') ? replaced.replace(/\n/g, '\r\n') : replaced;
  }
  return src;
}

content = replaceNorm(content, weeklyCloseTarget, weeklyCloseReplacement);

// 2. Parking thought dismiss button
const thoughtTarget = `<button onclick="this.parentElement.remove(); showToast('Thought Dismissed', '', 'info')" class="w-6 h-6 rounded flex items-center justify-center text-on-surface-variant hover:text-error cursor-pointer">
                  <span class="material-symbols-outlined text-[16px]">check</span>
                </button>`;

const thoughtReplacement = `<button onclick="this.parentElement.remove(); showToast('Thought Dismissed', '', 'info')" class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:text-error hover:bg-error/10 active:scale-90 transition-all cursor-pointer" aria-label="Dismiss thought" title="Dismiss thought">
                  <span class="material-symbols-outlined text-[16px]">check</span>
                </button>`;

content = replaceNorm(content, thoughtTarget, thoughtReplacement);

// Verify VM
const scriptBlocks = content.match(/<script\b[^>]*>([\s\S]*?)<\/script>/gi) || [];
scriptBlocks.forEach((sb, idx) => {
  const code = sb.replace(/<script\b[^>]*>/i, '').replace(/<\/script>/i, '');
  new vm.Script(code);
});

fs.writeFileSync(filePath, content, 'utf8');
console.log('✓ Successfully patched remaining 2 buttons and verified syntax!');
