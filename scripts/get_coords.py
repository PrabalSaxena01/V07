from cdp_client import eval_js
import json

js = """
(function() {
    const el = document.querySelector('#app-bottom-nav a[data-tab="tools"]');
    const r = el.getBoundingClientRect();
    return {
        left: r.left,
        top: r.top,
        width: r.width,
        height: r.height,
        dpr: window.devicePixelRatio,
        tapX: Math.round((r.left + r.width / 2) * window.devicePixelRatio),
        tapY: Math.round((r.top + r.height / 2) * window.devicePixelRatio),
        activeTab: window.activeTab
    };
})()
"""

print(json.dumps(eval_js(js), indent=2))
