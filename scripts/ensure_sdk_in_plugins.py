import os

BASE_DIR = r"D:\software\V07-Android"
PLUGIN_ROOTS = [
    os.path.join(BASE_DIR, r"app\src\main\assets\plugins"),
    os.path.join(BASE_DIR, r"app\src\main\assets\v3\plugins")
]

for root_dir in PLUGIN_ROOTS:
    if not os.path.exists(root_dir):
        continue
    for p_folder in os.listdir(root_dir):
        folder_path = os.path.join(root_dir, p_folder)
        if not os.path.isdir(folder_path):
            continue
        for html_file in ["index.html", "widget.html", "settings.html"]:
            file_path = os.path.join(folder_path, html_file)
            if os.path.exists(file_path):
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                if "v07-sdk.js" not in content:
                    # Inject v07-sdk.js into head or before body
                    if "<head>" in content:
                        content = content.replace("<head>", "<head>\n<script src=\"v07-sdk.js\"></script>", 1)
                    elif "</head>" in content:
                        content = content.replace("</head>", "<script src=\"v07-sdk.js\"></script>\n</head>", 1)
                    else:
                        content = "<script src=\"v07-sdk.js\"></script>\n" + content
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                    print(f"Injected v07-sdk.js into {file_path}")

print("All plugin HTML files verified and updated!")
