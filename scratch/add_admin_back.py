btn = '''      <button onclick="if (window.history.length > 1) window.history.back(); else window.location.href = '/';" class="w-8 h-8 rounded-lg bg-[var(--bg-surface)] border border-[var(--border-subtle)] text-[var(--text-muted)] hover:text-white flex items-center justify-center transition btn-press shrink-0" title="Back to Sanctuary"><span class="material-symbols-outlined text-base">arrow_back</span></button>\n'''
for p in ['D:/software/V07-Android/app/src/main/assets/v3/admin.html', 'D:/software/V07-Android/app/src/main/assets/admin.html']:
    with open(p, 'r', encoding='utf-8') as f:
        c = f.read()
    c = c.replace('\r\n', '\n')
    if 'title="Back to Sanctuary"' not in c:
        target = '    <div class="flex items-center gap-3">\n      <div class="w-8 h-8 rounded-lg'
        c = c.replace(target, '    <div class="flex items-center gap-2">\n' + btn + '      <div class="w-8 h-8 rounded-lg')
        with open(p, 'w', encoding='utf-8') as f:
            f.write(c.replace('\n', '\r\n'))
        print('Updated', p)
    else:
        print('Already updated', p)
