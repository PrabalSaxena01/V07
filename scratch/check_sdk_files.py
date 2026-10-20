import os

plugin_base = 'D:/software/V07-Android/app/src/main/assets/v3/plugins'
plugins = sorted([d for d in os.listdir(plugin_base) if os.path.isdir(os.path.join(plugin_base, d)) and not d.startswith('_') and d != 'v07-template'])

for p in plugins:
    sdk_path = os.path.join(plugin_base, p, 'v07-sdk.js')
    print(f"Plugin {p:25}: v07-sdk.js exists? {os.path.exists(sdk_path)}")
