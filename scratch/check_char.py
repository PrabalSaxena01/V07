with open('scratch/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    for i, line in enumerate(f):
        if 'Local journal active' in line:
            print(f"Line {i+1}:")
            for c in line:
                if ord(c) > 127:
                    print(f"Char: {repr(c)}, ord: {ord(c)}, hex: {hex(ord(c))}")
