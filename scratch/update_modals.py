with open('scratch/build_perfect_v3.py', 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Update close buttons to 44px (w-10 h-10) with aria-label
code = code.replace(
    '''      <button onclick="document.getElementById('modal-habits').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">''',
    '''      <button onclick="document.getElementById('modal-habits').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close habits">'''
)

code = code.replace(
    '''      <button onclick="document.getElementById('modal-reading').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">''',
    '''      <button onclick="document.getElementById('modal-reading').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close reading">'''
)

code = code.replace(
    '''      <button onclick="document.getElementById('modal-focus').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">''',
    '''      <button onclick="document.getElementById('modal-focus').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close focus">'''
)

code = code.replace(
    '''      <button onclick="document.getElementById('modal-gita').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">''',
    '''      <button onclick="document.getElementById('modal-gita').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close wisdom">'''
)

code = code.replace(
    '''      <button onclick="document.getElementById('modal-screentime').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">''',
    '''      <button onclick="document.getElementById('modal-screentime').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close screen time">'''
)

code = code.replace(
    '''      <button onclick="document.getElementById('modal-mood').close()" class="w-8 h-8 rounded-lg flex items-center justify-center text-on-surface hover:bg-surface-container transition-colors cursor-pointer">''',
    '''      <button onclick="document.getElementById('modal-mood').close()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-all cursor-pointer" aria-label="Close mood">'''
)

# 2. Update reading quick buttons to avoid text wrapping
old_reading_btns = '''            <div class="flex items-center gap-2 mt-3">
              <button onclick="adjustBookPages(-5)" class="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg font-label-sm text-[12px] transition-colors active:scale-95 cursor-pointer">
                -5 p
              </button>
              <button onclick="adjustBookPages(10)" class="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg font-label-sm text-[12px] transition-colors active:scale-95 cursor-pointer">
                +10 p
              </button>
              <button onclick="promptUpdatePage()" class="ml-auto flex items-center gap-1 px-3 py-1 bg-primary text-on-primary rounded-lg font-label-sm text-[12px] transition-opacity hover:opacity-95 cursor-pointer">
                <span class="material-symbols-outlined text-[14px]">edit</span>
                <span>Update</span>
              </button>
            </div>'''

new_reading_btns = '''            <div class="flex items-center gap-2 mt-3">
              <button onclick="adjustBookPages(-5)" class="px-3 py-1.5 min-h-[36px] bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg font-label-sm text-[12px] font-medium transition-colors active:scale-95 cursor-pointer whitespace-nowrap">
                -5 pages
              </button>
              <button onclick="adjustBookPages(10)" class="px-3 py-1.5 min-h-[36px] bg-surface-container-low hover:bg-surface-container text-on-surface rounded-lg font-label-sm text-[12px] font-medium transition-colors active:scale-95 cursor-pointer whitespace-nowrap">
                +10 pages
              </button>
              <button onclick="promptUpdatePage()" class="ml-auto flex items-center gap-1.5 px-3.5 py-1.5 min-h-[36px] bg-primary text-on-primary rounded-lg font-label-sm text-[12px] font-medium transition-opacity hover:opacity-95 cursor-pointer whitespace-nowrap">
                <span class="material-symbols-outlined text-[15px]">edit</span>
                <span>Update</span>
              </button>
            </div>'''

code = code.replace(old_reading_btns, new_reading_btns)

# 3. Add backdrop click dismissal to DOMContentLoaded
old_dom_init = '''    // Initialization
    window.addEventListener('DOMContentLoaded', () => {'''

new_dom_init = '''    // Initialization
    window.addEventListener('DOMContentLoaded', () => {
      // Backdrop click dismissal for all dialog modals
      document.querySelectorAll('dialog').forEach(modal => {
        modal.addEventListener('click', (e) => {
          const rect = modal.getBoundingClientRect();
          const isInDialog = (
            rect.top <= e.clientY && e.clientY <= rect.top + rect.height &&
            rect.left <= e.clientX && e.clientX <= rect.left + rect.width
          );
          if (!isInDialog) {
            modal.close();
          }
        });
      });'''

code = code.replace(old_dom_init, new_dom_init)

with open('scratch/build_perfect_v3.py', 'w', encoding='utf-8') as f:
    f.write(code)

print('Updated scratch/build_perfect_v3.py successfully')
