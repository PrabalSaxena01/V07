const fs = require('fs');
const path = require('path');
const vm = require('vm');

const filePath = path.resolve('app/src/main/assets/v3/index.html');
let content = fs.readFileSync(filePath, 'utf8');

// Helper for newline-agnostic replace
function replaceNormalized(src, target, replacement) {
  const normTarget = target.replace(/\r\n/g, '\n');
  const normSrc = src.replace(/\r\n/g, '\n');
  if (normSrc.includes(normTarget)) {
    const normReplacement = replacement.replace(/\r\n/g, '\n');
    const replaced = normSrc.replace(normTarget, normReplacement);
    return src.includes('\r\n') ? replaced.replace(/\n/g, '\r\n') : replaced;
  }
  return src;
}

// 1. Injected FALLBACK_BOOK_COVER and cleanText helper
const isCRLF = content.includes('\r\n');
const EOL = isCRLF ? '\r\n' : '\n';
const sIdx = content.indexOf('<script>');
if (sIdx !== -1) {
  const nextLineIdx = content.indexOf('\n', sIdx) + 1;
  content = content.substring(0, nextLineIdx) + 
    `    // --- IMPECCABLE RESILIENCE & SANITIZATION ASSETS ---${EOL}    const FALLBACK_BOOK_COVER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 140" width="100" height="140"><rect width="100" height="140" rx="8" fill="%232D3748"/><path d="M35 50h30M35 65h30M35 80h20" stroke="%23A0AEC0" stroke-width="3" stroke-linecap="round"/><text x="50" y="112" fill="%23CBD5E0" font-size="10" font-family="sans-serif" font-weight="bold" text-anchor="middle">SANCTUARY</text></svg>';${EOL}${EOL}    function cleanText(input) {${EOL}      if (input == null) return '';${EOL}      if (typeof input === 'object') {${EOL}        if (input.value) input = input.value;${EOL}        else if (Array.isArray(input)) input = input.join(', ');${EOL}        else input = '';${EOL}      }${EOL}      var str = String(input).replace(/<[^>]+>/g, ' ');${EOL}      var txt = document.createElement('textarea');${EOL}      txt.innerHTML = str;${EOL}      return txt.value.replace(/\\s+/g, ' ').trim();${EOL}    }${EOL}${EOL}` +
    content.substring(nextLineIdx);
  console.log('✓ Injected FALLBACK_BOOK_COVER and cleanText helper');
}

// 2. Add onerror to #featured-book-cover HTML tag
const featImgTarget = `<img id="featured-book-cover" class="w-full h-full object-cover" src="" alt="Book cover"/>`;
const featImgReplacement = `<img id="featured-book-cover" class="w-full h-full object-cover" src="" alt="Book cover" onerror="this.onerror=null; this.src=FALLBACK_BOOK_COVER;"/>`;
content = replaceNormalized(content, featImgTarget, featImgReplacement);

// 3. Update renderReadingView cover handling
const readingViewCoverTarget = `if (coverImg && coverContainer) {
          if (featured.coverUrl) {
            coverImg.src = featured.coverUrl;
            coverImg.classList.remove('hidden');
            coverImg.onerror = () => {
              coverImg.classList.add('hidden');
              coverContainer.setAttribute('data-fallback', (featured.title || 'BK').slice(0, 2).toUpperCase());
            };
          } else {
            coverImg.classList.add('hidden');
            coverContainer.setAttribute('data-fallback', (featured.title || 'BK').slice(0, 2).toUpperCase());
          }
        }
      } else if (featuredCard) {
        if (titleEl) titleEl.textContent = 'No book selected';
        if (authorEl) authorEl.textContent = 'Tap "+ Add book" to start your shelf';
        if (pctBadge) pctBadge.textContent = '0%';
        if (pageInfo) pageInfo.textContent = 'Tap to add or search books';
        if (bar) bar.style.width = '0%';
        if (coverImg) coverImg.classList.add('hidden');
      }`;

const readingViewCoverReplacement = `if (coverImg && coverContainer) {
          coverImg.classList.remove('hidden');
          if (featured.coverUrl) {
            coverImg.src = featured.coverUrl;
            coverImg.onerror = () => {
              coverImg.onerror = null;
              coverImg.src = FALLBACK_BOOK_COVER;
            };
          } else {
            coverImg.src = FALLBACK_BOOK_COVER;
          }
        }
      } else if (featuredCard) {
        if (titleEl) titleEl.textContent = 'No book selected';
        if (authorEl) authorEl.textContent = 'Tap "+ Add book" to start your shelf';
        if (pctBadge) pctBadge.textContent = '0%';
        if (pageInfo) pageInfo.textContent = 'Tap to add or search books';
        if (bar) bar.style.width = '0%';
        if (coverImg) {
          coverImg.src = FALLBACK_BOOK_COVER;
          coverImg.classList.remove('hidden');
        }
      }`;

content = replaceNormalized(content, readingViewCoverTarget, readingViewCoverReplacement);

// 4. Update executeLiveBookSearch: cleanText and inline cover fallback
const tSafeTarget = `const titleSafe = escapeHtml(b.title);\n        const authorSafe = escapeHtml(b.author);`;
const tSafeReplacement = `const titleSafe = escapeHtml(cleanText(b.title));\n        const authorSafe = escapeHtml(cleanText(b.author));`;
content = replaceNormalized(content, tSafeTarget, tSafeReplacement);

const bookImgTarget = `${"${b.coverUrl ? `<img src=\"${coverSafe}\" class=\"w-full h-full object-cover\" onerror=\"this.remove()\" />` : `<span class=\"font-bold text-xs text-primary\">${titleSafe.slice(0, 2).toUpperCase()}</span>`}"}`;
const bookImgReplacement = `${"${b.coverUrl ? `<img src=\"${coverSafe}\" class=\"w-full h-full object-cover\" onerror=\"this.onerror=null; this.src=FALLBACK_BOOK_COVER;\" />` : `<span class=\"font-bold text-xs text-primary\">${titleSafe.slice(0, 2).toUpperCase()}</span>`}"}`;
content = replaceNormalized(content, bookImgTarget, bookImgReplacement);

// 5. Update addBookFromSearch to sanitize with cleanText
const addBookSearchTarget = `const newBook = {
          id: 'book_' + Date.now().toString(36),
          title: bookData.title || 'Untitled Book',
          author: bookData.author || 'Unknown Author',
          currentPage: 0,
          totalPages: Math.max(1, bookData.pages || 250),
          status: 'reading',
          coverUrl: bookData.coverUrl || '',
          notes: '',
          updatedAt: Date.now()
        };`;

const addBookSearchReplacement = `const newBook = {
          id: 'book_' + Date.now().toString(36),
          title: cleanText(bookData.title) || 'Untitled Book',
          author: cleanText(bookData.author) || 'Unknown Author',
          currentPage: 0,
          totalPages: Math.max(1, parseInt(bookData.pages, 10) || 250),
          status: 'reading',
          coverUrl: bookData.coverUrl || '',
          notes: '',
          updatedAt: Date.now()
        };`;

content = replaceNormalized(content, addBookSearchTarget, addBookSearchReplacement);

// 6. Dynamic avatar fallbacks
const avatarTarget = `if (drawerAvatar) drawerAvatar.innerHTML = \`<img src="\${saved}" class="w-full h-full object-cover rounded-full" alt="Avatar"/>\`;
          if (headerAvatar) headerAvatar.innerHTML = \`<img src="\${saved}" class="w-full h-full object-cover rounded-full" alt="Avatar"/>\`;`;

const avatarReplacement = `if (drawerAvatar) drawerAvatar.innerHTML = \`<img src="\${saved}" class="w-full h-full object-cover rounded-full" alt="Avatar" onerror="this.onerror=null; this.parentElement.textContent='👤';" />\`;
          if (headerAvatar) headerAvatar.innerHTML = \`<img src="\${saved}" class="w-full h-full object-cover rounded-full" alt="Avatar" onerror="this.onerror=null; this.parentElement.textContent='👤';" />\`;`;

content = replaceNormalized(content, avatarTarget, avatarReplacement);

// 7. Day timeline empty state
const dayEmptyTarget = `if (events.length === 0) {
          eventsContainer.innerHTML = \`
            <div class="py-6 text-center text-xs text-on-surface-variant flex flex-col items-center gap-1.5 -ml-5">
              <span class="material-symbols-outlined text-[24px] opacity-40">event_busy</span>
              <span>No recorded events on this date.</span>
            </div>
          \`;
          return;
        }`;

const dayEmptyReplacement = `if (events.length === 0) {
          eventsContainer.innerHTML = \`
            <div class="py-8 px-4 text-center bg-surface-container-lowest rounded-2xl border border-surface-container-high/40 flex flex-col items-center gap-2 shadow-xs">
              <div class="w-10 h-10 rounded-2xl bg-surface-container text-on-surface-variant flex items-center justify-center">
                <span class="material-symbols-outlined text-[20px]">calendar_today</span>
              </div>
              <span class="text-xs font-bold text-on-surface">A Quiet Horizon</span>
              <span class="text-[11px] text-on-surface-variant max-w-xs">No entries or activities logged for this date.</span>
              <button onclick="navigateTo('journal')" class="mt-1 px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold active:scale-95 transition-all shadow-xs cursor-pointer">
                + Write Note
              </button>
            </div>
          \`;
          return;
        }`;

content = replaceNormalized(content, dayEmptyTarget, dayEmptyReplacement);

// 8. Day timeline event delete button
const eventDeleteTarget = `<button onclick="softDeleteItem('event', '\${ev.id}', '\${escapeHtml(ev.title).replace(/'/g, "\\\\'")}', '\${ev.category || 'event'}')" class="w-6 h-6 rounded flex items-center justify-center text-outline hover:text-error hover:bg-error/10 text-xs font-bold transition-colors cursor-pointer shrink-0" title="Move to Recycle Bin">✕</button>`;
const eventDeleteReplacement = `<button onclick="softDeleteItem('event', '\${ev.id}', '\${escapeHtml(ev.title).replace(/'/g, "\\\\'")}', '\${ev.category || 'event'}')" class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg flex items-center justify-center text-outline hover:text-error hover:bg-error/10 text-xs font-bold transition-all active:scale-90 cursor-pointer shrink-0" aria-label="Move event to Recycle Bin" title="Move to Recycle Bin">✕</button>`;
content = replaceNormalized(content, eventDeleteTarget, eventDeleteReplacement);

// 9. Task item delete button
const taskDeleteTarget = `<button onclick="event.stopPropagation(); softDeleteItem('task', '\${task.id}', '\${escapeHtml(task.title).replace(/'/g, "\\\\'")}', 'task')" class="w-6 h-6 rounded flex items-center justify-center text-outline hover:text-error hover:bg-error/10 text-xs font-bold transition-colors cursor-pointer shrink-0 ml-2" title="Move to Recycle Bin">✕</button>`;
const taskDeleteReplacement = `<button onclick="event.stopPropagation(); softDeleteItem('task', '\${task.id}', '\${escapeHtml(task.title).replace(/'/g, "\\\\'")}', 'task')" class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg flex items-center justify-center text-outline hover:text-error hover:bg-error/10 text-xs font-bold transition-all active:scale-90 cursor-pointer shrink-0 ml-2" aria-label="Move task to Recycle Bin" title="Move to Recycle Bin">✕</button>`;
content = replaceNormalized(content, taskDeleteTarget, taskDeleteReplacement);

// 10. Habits empty state
const habitEmptyTarget = `if (habits.length === 0) {
        container.innerHTML = \`
          <div class="p-6 text-center bg-surface-container-lowest rounded-2xl border border-surface-container-high/40 flex flex-col items-center gap-2">
            <span class="material-symbols-outlined text-[24px] text-on-surface-variant opacity-40">task_alt</span>
            <p class="text-xs text-on-surface-variant">No habits active right now. Add your first ritual below.</p>
          </div>
        \`;
        return;
      }`;

const habitEmptyReplacement = `if (habits.length === 0) {
        container.innerHTML = \`
          <div class="p-6 text-center bg-surface-container-lowest rounded-2xl border border-surface-container-high/40 flex flex-col items-center gap-2 shadow-xs">
            <div class="w-10 h-10 rounded-2xl bg-surface-container text-on-surface-variant flex items-center justify-center">
              <span class="material-symbols-outlined text-[22px] text-primary">task_alt</span>
            </div>
            <span class="text-xs font-bold text-on-surface">No Rituals Yet</span>
            <p class="text-[11px] text-on-surface-variant max-w-xs">Build your daily momentum by logging morning reading, meditation, or walks.</p>
            <button onclick="promptAddHabit()" class="mt-1 px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold active:scale-95 transition-all shadow-xs cursor-pointer">
              + Add First Ritual
            </button>
          </div>
        \`;
        return;
      }`;

content = replaceNormalized(content, habitEmptyTarget, habitEmptyReplacement);

// 11. Habits row action buttons (tune & close)
const habitRowActionsTarget = `<div class="flex items-center gap-1 shrink-0">
                <button onclick="promptEditHabit('\${h.id}')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Edit Habit">
                  <span class="material-symbols-outlined text-[16px]">tune</span>
                </button>
                <button onclick="softDeleteHabit('\${h.id}')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:text-error hover:bg-error/10 cursor-pointer" title="Soft Delete to Recycle Bin">
                  <span class="material-symbols-outlined text-[16px]">close</span>
                </button>
              </div>`;

const habitRowActionsReplacement = `<div class="flex items-center gap-1 shrink-0">
                <button onclick="promptEditHabit('\${h.id}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Edit habit" title="Edit Habit">
                  <span class="material-symbols-outlined text-[16px]">tune</span>
                </button>
                <button onclick="softDeleteHabit('\${h.id}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:text-error hover:bg-error/10 active:scale-95 transition-all cursor-pointer" aria-label="Delete habit" title="Soft Delete to Recycle Bin">
                  <span class="material-symbols-outlined text-[16px]">close</span>
                </button>
              </div>`;

content = replaceNormalized(content, habitRowActionsTarget, habitRowActionsReplacement);

// 12. Ledger row action buttons (edit, copy, delete)
const ledgerRowActionsTarget = `<div class="flex items-center gap-1 shrink-0">
                <button onclick="promptEditLedgerEntry('\${item.id}')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:text-primary hover:bg-surface-container cursor-pointer transition-colors" title="Edit entry">
                  <span class="material-symbols-outlined text-[15px]">edit</span>
                </button>
                <button onclick="handleCopyVaultEntry('\${item.id}')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Copy text">
                  <span class="material-symbols-outlined text-[15px]">content_copy</span>
                </button>
                <button onclick="deleteLedgerEntry('\${item.id}')" class="w-7 h-7 rounded-lg flex items-center justify-center text-outline hover:text-error hover:bg-error/10 font-bold text-xs cursor-pointer transition-colors" title="Move to Recycle Bin">
                  ✕
                </button>
              </div>`;

const ledgerRowActionsReplacement = `<div class="flex items-center gap-1 shrink-0">
                <button onclick="promptEditLedgerEntry('\${item.id}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:text-primary hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Edit entry" title="Edit entry">
                  <span class="material-symbols-outlined text-[16px]">edit</span>
                </button>
                <button onclick="handleCopyVaultEntry('\${item.id}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Copy note content" title="Copy text">
                  <span class="material-symbols-outlined text-[16px]">content_copy</span>
                </button>
                <button onclick="deleteLedgerEntry('\${item.id}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-outline hover:text-error hover:bg-error/10 font-bold text-xs active:scale-90 transition-all cursor-pointer" aria-label="Move entry to Recycle Bin" title="Move to Recycle Bin">
                  ✕
                </button>
              </div>`;

content = replaceNormalized(content, ledgerRowActionsTarget, ledgerRowActionsReplacement);

// 13. Data Center row copy and delete buttons
const dcRowActionsTarget = `<button onclick="handleCopyVaultEntry('\${item.id}')" class="w-7 h-7 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" title="Copy text">
                  <span class="material-symbols-outlined text-[15px]">content_copy</span>
                </button>
                <button onclick="handleDeleteVaultEntry('\${item.id}', false)" class="w-7 h-7 rounded-lg flex items-center justify-center text-outline hover:text-error hover:bg-error/10 font-bold text-xs cursor-pointer transition-colors" title="Delete activity">
                  ✕
                </button>`;

const dcRowActionsReplacement = `<button onclick="handleCopyVaultEntry('\${item.id}')" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Copy activity content" title="Copy text">
                  <span class="material-symbols-outlined text-[16px]">content_copy</span>
                </button>
                <button onclick="handleDeleteVaultEntry('\${item.id}', false)" class="w-8 h-8 rounded-lg flex items-center justify-center text-outline hover:text-error hover:bg-error/10 font-bold text-xs active:scale-90 transition-all cursor-pointer" aria-label="Delete activity" title="Delete activity">
                  ✕
                </button>`;

content = replaceNormalized(content, dcRowActionsTarget, dcRowActionsReplacement);

// 14. Recycle bin purge button
const purgeTarget = `<button type="button" onclick="purgeRecycleItem('\${item.recycleId}')" class="w-7 h-7 rounded-lg text-outline hover:text-error hover:bg-error/10 active:scale-95 transition-all flex items-center justify-center cursor-pointer" title="Permanently delete">
                      <span class="material-symbols-outlined text-[15px]">delete</span>
                    </button>`;

const purgeReplacement = `<button type="button" onclick="purgeRecycleItem('\${item.recycleId}')" class="w-8 h-8 rounded-lg text-outline hover:text-error hover:bg-error/10 active:scale-95 transition-all flex items-center justify-center cursor-pointer" aria-label="Permanently delete item" title="Permanently delete">
                      <span class="material-symbols-outlined text-[16px]">delete</span>
                    </button>`;

content = replaceNormalized(content, purgeTarget, purgeReplacement);

// 15. Drawer close buttons
content = content.replace(
  `<button onclick="closePluginCatalogDrawer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface cursor-pointer">`,
  `<button onclick="closePluginCatalogDrawer()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close plugin catalog" title="Close">`
);

content = content.replace(
  `<button onclick="closeProfileDrawer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface cursor-pointer">`,
  `<button onclick="closeProfileDrawer()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close profile drawer" title="Close">`
);

content = content.replace(
  `<button onclick="closeNotificationDrawer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface cursor-pointer">`,
  `<button onclick="closeNotificationDrawer()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close notifications" title="Close">`
);

content = content.replace(
  `<button onclick="closeCustomModal()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" aria-label="Close modal">`,
  `<button onclick="closeCustomModal()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Close modal" title="Close">`
);

// 16. Header buttons (notifications, profile)
content = content.replace(
  `<button onclick="openNotificationDrawer()" class="relative w-10 h-10 rounded-full bg-surface-container-low border border-surface-container-high/60 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" title="Notifications & Prompts">`,
  `<button onclick="openNotificationDrawer()" class="relative w-10 h-10 rounded-full bg-surface-container-low border border-surface-container-high/60 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" aria-label="Notifications & Prompts" title="Notifications & Prompts">`
);

content = content.replace(
  `<button onclick="openProfileDrawer()" class="relative w-10 h-10 rounded-full bg-primary-fixed text-primary border border-primary/20 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform font-bold text-sm shadow-xs" title="Sanctuary Profile">`,
  `<button onclick="openProfileDrawer()" class="relative w-10 h-10 rounded-full bg-primary-fixed text-primary border border-primary/20 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform font-bold text-sm shadow-xs" aria-label="Sanctuary Profile" title="Sanctuary Profile">`
);

// 17. Search clear buttons
content = content.replace(
  `<button onclick="clearLedgerSearch()" id="ledger-search-clear" class="absolute right-3 text-on-surface-variant hover:text-on-surface hidden cursor-pointer">`,
  `<button onclick="clearLedgerSearch()" id="ledger-search-clear" class="absolute right-3 text-on-surface-variant hover:text-on-surface hidden cursor-pointer p-1 rounded-full active:scale-95" aria-label="Clear ledger search" title="Clear">`
);

content = content.replace(
  `<button onclick="clearDcSearch()" id="dc-search-clear" class="absolute right-3 text-on-surface-variant hover:text-on-surface hidden cursor-pointer">`,
  `<button onclick="clearDcSearch()" id="dc-search-clear" class="absolute right-3 text-on-surface-variant hover:text-on-surface hidden cursor-pointer p-1 rounded-full active:scale-95" aria-label="Clear search" title="Clear">`
);

// 18. Focus target adjustment buttons (- and +)
const focusAdjustTarget = `<div class="flex items-center gap-1.5">
              <button onclick="adjustDailyFocusTarget(-15)" class="w-6 h-6 rounded-md bg-surface-container-low hover:bg-surface-container flex items-center justify-center text-on-surface-variant text-xs font-bold active:scale-90 transition-all cursor-pointer">
                -
              </button>
              <span class="text-xs font-bold text-primary" id="focus-daily-target-text">90 min</span>
              <button onclick="adjustDailyFocusTarget(15)" class="w-6 h-6 rounded-md bg-surface-container-low hover:bg-surface-container flex items-center justify-center text-on-surface-variant text-xs font-bold active:scale-90 transition-all cursor-pointer">
                +
              </button>
            </div>`;

const focusAdjustReplacement = `<div class="flex items-center gap-2">
              <button onclick="adjustDailyFocusTarget(-15)" class="w-8 h-8 rounded-lg bg-surface-container-low hover:bg-surface-container flex items-center justify-center text-on-surface-variant text-xs font-bold active:scale-90 transition-all cursor-pointer" aria-label="Decrease focus goal by 15 minutes" title="Decrease">
                -
              </button>
              <span class="text-xs font-bold text-primary px-1" id="focus-daily-target-text">90 min</span>
              <button onclick="adjustDailyFocusTarget(15)" class="w-8 h-8 rounded-lg bg-surface-container-low hover:bg-surface-container flex items-center justify-center text-on-surface-variant text-xs font-bold active:scale-90 transition-all cursor-pointer" aria-label="Increase focus goal by 15 minutes" title="Increase">
                +
              </button>
            </div>`;

content = replaceNormalized(content, focusAdjustTarget, focusAdjustReplacement);

// 19. Tool header settings buttons (add aria-label)
const toolSettingsReplacements = [
  { from: `onclick="openPluginSettingsModal('focus')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Focus Sanctuary"`,
    to: `onclick="openPluginSettingsModal('focus')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Focus Sanctuary" title="Customize Focus Sanctuary"` },
  { from: `onclick="resetFocusTimer()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Reset"`,
    to: `onclick="resetFocusTimer()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Reset focus timer" title="Reset"` },
  { from: `onclick="openPluginSettingsModal('reading')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Reading Companion"`,
    to: `onclick="openPluginSettingsModal('reading')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Reading Companion" title="Customize Reading Companion"` },
  { from: `onclick="openPluginSettingsModal('habits')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Habits"`,
    to: `onclick="openPluginSettingsModal('habits')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Habits" title="Customize Habits"` },
  { from: `onclick="openPluginSettingsModal('screentime')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Digital Stillness"`,
    to: `onclick="openPluginSettingsModal('screentime')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Digital Stillness" title="Customize Digital Stillness"` },
  { from: `onclick="refreshScreenTime()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Refresh"`,
    to: `onclick="refreshScreenTime()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Refresh Screen Time" title="Refresh"` },
  { from: `onclick="openPluginSettingsModal('mood')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Mood & Grounding"`,
    to: `onclick="openPluginSettingsModal('mood')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Mood & Grounding" title="Customize Mood & Grounding"` },
  { from: `onclick="openPluginSettingsModal('gita')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Gita Wisdom"`,
    to: `onclick="openPluginSettingsModal('gita')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Gita Wisdom" title="Customize Gita Wisdom"` },
  { from: `onclick="openPluginSettingsModal('weekly')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Weekly Digest"`,
    to: `onclick="openPluginSettingsModal('weekly')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Customize Weekly Digest" title="Customize Weekly Digest"` },
  { from: `onclick="openRecycleBinModal()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Open Recycle Bin"`,
    to: `onclick="openRecycleBinModal()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" aria-label="Open Recycle Bin" title="Open Recycle Bin"` },
  { from: `<button onclick="toggleRunningLogForm(false)" class="text-on-surface-variant hover:text-on-surface text-xs p-1">`,
    to: `<button onclick="toggleRunningLogForm(false)" class="text-on-surface-variant hover:text-on-surface text-xs p-1.5 rounded-full active:scale-95" aria-label="Close log form" title="Close">` }
];

toolSettingsReplacements.forEach(r => {
  content = replaceNormalized(content, r.from, r.to);
});

// Validate JavaScript syntax in all <script> blocks using Node VM
console.log('\nValidating JavaScript syntax across all <script> blocks...');
const scriptBlocks = content.match(/<script\b[^>]*>([\s\S]*?)<\/script>/gi) || [];
scriptBlocks.forEach((sb, idx) => {
  const code = sb.replace(/<script\b[^>]*>/i, '').replace(/<\/script>/i, '');
  try {
    new vm.Script(code);
    console.log(`✓ Script block ${idx + 1} (${(code.length / 1024).toFixed(1)} KB) syntax is 100% VALID`);
  } catch (err) {
    console.error(`✕ Syntax error in script block ${idx + 1}:`, err.message);
    process.exit(1);
  }
});

// Write changes back to file
fs.writeFileSync(filePath, content, 'utf8');
console.log(`\nSuccessfully applied Impeccable Hardening! Saved to ${filePath} (${(content.length / 1024).toFixed(1)} KB)`);
