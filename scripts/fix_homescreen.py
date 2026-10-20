#!/usr/bin/env python3
import re

p = r"D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\HomeScreen.kt"
with open(p, "r", encoding="utf-8") as f:
    lines = f.readlines()

# Collect line indices of `    val lifecycleOwner = LocalLifecycleOwner.current`
indices = [i for i, line in enumerate(lines) if "val lifecycleOwner = LocalLifecycleOwner.current" in line]
print(f"Indices: {indices}")

# We want to keep the 5th occurrence (0-indexed: index 4, which is line ~388) and drop all others (0, 1, 2, 3, 5)
indices_to_drop = [indices[0], indices[1], indices[2], indices[3], indices[5]]

# For each index to drop, find how many lines to drop (from `val lifecycleOwner` down to `onDispose { ... } \n }`)
lines_to_delete = set()
for start_idx in indices_to_drop:
    # search forward until closing brace of DisposableEffect
    end_idx = start_idx
    for j in range(start_idx, min(start_idx + 40, len(lines))):
        if "lifecycleOwner.lifecycle.removeObserver(observer)" in lines[j]:
            # Next two lines are `}` and `}`
            end_idx = j + 2
            break
    print(f"Dropping block from line {start_idx + 1} to {end_idx + 1}")
    for k in range(start_idx, end_idx + 1):
        lines_to_delete.add(k)

new_lines = [line for i, line in enumerate(lines) if i not in lines_to_delete]

with open(p, "w", encoding="utf-8", newline="\n") as f:
    f.writelines(new_lines)

print(f"Cleaned HomeScreen.kt. Total lines before: {len(lines)}, after: {len(new_lines)}")
