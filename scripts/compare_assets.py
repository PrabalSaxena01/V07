import os
import filecmp

extracted_dir = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3"
workspace_dir = r"D:/software/V07-Android/app/src/main/assets/v3"

diffs = []
for root, dirs, files in os.walk(extracted_dir):
    for f in files:
        ext_path = os.path.join(root, f)
        rel_path = os.path.relpath(ext_path, extracted_dir)
        ws_path = os.path.join(workspace_dir, rel_path)
        if not os.path.exists(ws_path):
            diffs.append(f"ONLY IN EXTRACTED APK: {rel_path}")
        elif not filecmp.cmp(ext_path, ws_path, shallow=False):
            diffs.append(f"DIFFERENT: {rel_path}")

print(f"Total diffs: {len(diffs)}")
for d in diffs[:20]:
    print(" ", d)
