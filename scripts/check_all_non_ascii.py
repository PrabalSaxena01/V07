with open('scratch/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

non_ascii = {}
for i, line in enumerate(lines):
    for c in line:
        if ord(c) > 127:
            # check what it is
            non_ascii.setdefault(c, []).append(i + 1)

for c, l_nums in non_ascii.items():
    print(f"Char: {repr(c)}, ord: {ord(c)}, hex: {hex(ord(c))}, count: {len(l_nums)}, lines: {l_nums[:5]}")
