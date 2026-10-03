from cdp_client import eval_js
import json

js = """
(function() {
    const el = document.querySelector('header button[title="Sanctuary Profile"]');
    if (!el) return 'profile btn not found';
    const r = el.getBoundingClientRect();
    return {
        left: r.left,
        top: r.top,
        x: Math.round((r.left + r.width / 2) * window.devicePixelRatio),
        y: Math.round((r.top + r.height / 2) * window.devicePixelRatio)
    };
})()
"""

print(json.dumps(eval_js(js), indent=2))
