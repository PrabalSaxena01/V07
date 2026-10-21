import zipfile
import os

apk_path = r"D:/software/V07-Android/output/safekeeping/v07-v3-extracted.apk"
out_dir = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents"

os.makedirs(out_dir, exist_ok=True)
with zipfile.ZipFile(apk_path, 'r') as z:
    for name in z.namelist():
        if name.startswith("assets/v3/") or name == "AndroidManifest.xml":
            z.extract(name, out_dir)

print("Extracted assets/v3/ from extracted APK.")
