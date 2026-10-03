import os

files = ['app/src/main/assets/v3/index.html', 'app/src/main/assets/index.html']

for fpath in files:
    with open(fpath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Infinity symbol in focus preset
    content = content.replace(
        '<span class="material-symbols-outlined text-[13px]">hourglass_empty</span>\n            <span>infinity Open</span>',
        '<span class="material-symbols-outlined text-[14px]">all_inclusive</span>\n            <span>Open</span>'
    )
    content = content.replace(
        '<span class="material-symbols-outlined text-[13px]">hourglass_empty</span>\r\n            <span>infinity Open</span>',
        '<span class="material-symbols-outlined text-[14px]">all_inclusive</span>\r\n            <span>Open</span>'
    )

    # 2. Add Tool button text in kit header to fit cleanly without wrapping or overflow
    content = content.replace(
        '<span class="material-symbols-outlined text-[16px]">add</span>\n            <span>Add Tool</span>',
        '<span class="material-symbols-outlined text-[16px]">add</span>\n            <span>Add</span>'
    )
    content = content.replace(
        '<span class="material-symbols-outlined text-[16px]">add</span>\r\n            <span>Add Tool</span>',
        '<span class="material-symbols-outlined text-[16px]">add</span>\r\n            <span>Add</span>'
    )

    # 3. Status preview for reading companion in kit tile
    content = content.replace(
        "case 'reading':\n          return 'Atomic Habits • p. 42 / 320';",
        "case 'reading':\n          return 'Atomic Habits • 42/320p';"
    )
    content = content.replace(
        "case 'reading':\r\n          return 'Atomic Habits • p. 42 / 320';",
        "case 'reading':\r\n          return 'Atomic Habits • 42/320p';"
    )

    # 4. Habits header subtitle spacing
    content = content.replace(
        '<p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you do regularly</p>',
        '<p class="text-[11px] text-on-surface-variant mt-0.5">Keep track of what you do regularly</p>'
    )

    with open(fpath, 'w', encoding='utf-8') as f:
        f.write(content)

print("Applied micro-refinements to index.html files!")
