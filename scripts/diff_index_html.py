import difflib

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
ws_index = r"D:/software/V07-Android/app/src/main/assets/v3/index.html"

with open(ext_index, "r", encoding="utf-8") as f:
    ext_lines = f.readlines()
with open(ws_index, "r", encoding="utf-8") as f:
    ws_lines = f.readlines()

diff = difflib.unified_diff(ws_lines, ext_lines, fromfile="workspace_index.html", tofile="extracted_phone_index.html", n=3)
diff_text = "".join(list(diff))
print(f"Diff length: {len(diff_text)} chars")
print(diff_text[:3000])
