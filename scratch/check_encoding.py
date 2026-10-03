with open('app/src/main/assets/index.html', 'r', encoding='utf-8', errors='replace') as f:
    content = f.read()

count = content.count('\ufffd')
print(f"Occurrences of replacement character \\ufffd in index.html: {count}")
if count > 0:
    for i, line in enumerate(content.splitlines()):
        if '\ufffd' in line:
            print(f"Line {i+1}: {line.strip()}")
