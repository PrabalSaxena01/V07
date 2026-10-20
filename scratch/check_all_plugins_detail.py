import os
import re

plugin_base = 'D:/software/V07-Android/app/src/main/assets/v3/plugins'
plugins = sorted([d for d in os.listdir(plugin_base) if os.path.isdir(os.path.join(plugin_base, d)) and not d.startswith('_') and d != 'v07-template'])

for p in plugins:
    p_path = os.path.join(plugin_base, p, 'index.html')
    if not os.path.exists(p_path):
        print(f"[{p}] MISSING index.html")
        continue
    with open(p_path, 'r', encoding='utf-8') as f:
        html = f.read()
    
    # Check for script errors, SDK usage, styles
    has_head = '<head>' in html or '<HEAD>' in html
    has_body = '<body' in html or '<BODY' in html
    scripts = re.findall(r'<script.*?>.*?</script>', html, re.DOTALL | re.IGNORECASE)
    print(f"\n=== Plugin: {p} ({len(html)} bytes, {len(scripts)} scripts) ===")
    
    # Check manifest.json
    m_path = os.path.join(plugin_base, p, 'manifest.json')
    if os.path.exists(m_path):
        with open(m_path, 'r', encoding='utf-8') as mf:
            m_text = mf.read()
            print(f"  Manifest: {len(m_text)} bytes")
    else:
        print(f"  Manifest: MISSING!")

    # Check for syntax errors in embedded script
    for i, s in enumerate(scripts):
        # strip script tags
        code = re.sub(r'^<script.*?>', '', s, flags=re.IGNORECASE)
        code = re.sub(r'</script>$', '', code, flags=re.IGNORECASE).strip()
        if not code or code.startswith('// external') or 'src=' in s:
            continue
        # Check basic things
        try:
            # Check unclosed braces or quotes
            open_b = code.count('{')
            close_b = code.count('}')
            open_p = code.count('(')
            close_p = code.count(')')
            if open_b != close_b:
                print(f"  [WARN] script {i} curly braces mismatch: {open_b} vs {close_b}")
            if open_p != close_p:
                print(f"  [WARN] script {i} parens mismatch: {open_p} vs {close_p}")
        except Exception as e:
            print(f"  Error checking script: {e}")
