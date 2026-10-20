with open('scratch/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

count = text.count('\ufffd')
print(f"Total \\ufffd in generator script: {count}")

for i, line in enumerate(text.splitlines()):
    if '\ufffd' in line:
        # print line number and safe repr
        safe = line.replace('\ufffd', '[REPLACEMENT_CHAR]')
        print(f"L{i+1}: {safe}")
