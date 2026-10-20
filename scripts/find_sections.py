with open('scratch/generate_v3_stitch.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines[:1200], 1):
    if 'id="tab-' in line:
        print(f"Line {i}: {line.strip()[:80]}")
