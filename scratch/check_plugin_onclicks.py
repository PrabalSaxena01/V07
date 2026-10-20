import os
import re

plugin_dir = 'D:/software/V07-Android/app/src/main/assets/v3/plugins'
plugins = [
    'com.v07.chronosynth',
    'com.v07.reader',
    'com.v07.habits',
    'com.v07.tasks',
    'com.v07.gamify',
    'com.v07.timetracker',
    'com.v07.vitals',
    'com.v07.gita',
    'com.v07.running'
]

for p in plugins:
    p_path = os.path.join(plugin_dir, p, 'index.html')
    with open(p_path, 'r', encoding='utf-8') as f:
        html = f.read()

    # Find all onclick handlers
    onclicks = re.findall(r'onclick=["\'](.*?)["\']', html)
    
    # Find all defined functions
    fn_defs = re.findall(r'function\s+([a-zA-Z0-9_$]+)\s*\(', html)
    fn_const = re.findall(r'(?:const|let|var)\s+([a-zA-Z0-9_$]+)\s*=\s*(?:function|\(.*?\)\s*=>)', html)
    all_fns = set(fn_defs + fn_const)

    print(f"\n--- Plugin: {p} ---")
    print(f"Defined functions ({len(all_fns)}): {sorted(list(all_fns))[:8]}...")
    
    # Check each onclick
    missing = []
    for oc in onclicks:
        # get function name call
        m = re.match(r'([a-zA-Z0-9_$]+)\s*\(', oc.strip())
        if m:
            called = m.group(1)
            # ignore alert, confirm, history, console, document
            if called in ['alert', 'confirm', 'prompt'] or called.startswith('console.') or called.startswith('window.'):
                continue
            if called not in all_fns:
                missing.append(called)
    if missing:
        print(f"  [POTENTIAL MISSING ONCLICK FUNCTIONS]: {set(missing)}")
    else:
        print(f"  All {len(onclicks)} onclick handlers matched defined functions!")
