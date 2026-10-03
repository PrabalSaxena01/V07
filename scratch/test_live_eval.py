from cdp_client import eval_js

with open('scratch/script_0.js', 'r', encoding='utf-8') as f:
    js_code = f.read()

# Wrap in an eval
test_script = f"""
(function() {{
    try {{
        window.eval({repr(js_code)});
        return 'Script executed successfully! switchTab is: ' + typeof window.switchTab;
    }} catch(e) {{
        return 'Error: ' + e.message + '\\n' + e.stack;
    }}
}})()
"""

print(eval_js(test_script))
