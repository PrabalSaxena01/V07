from cdp_client import eval_js
import json

js = """
(function() {
    const tabs = Array.from(document.querySelectorAll('#app-bottom-nav a.nav-item')).map(el => {
        const r = el.getBoundingClientRect();
        return {
            tab: el.getAttribute('data-tab'),
            x: Math.round((r.left + r.width / 2) * window.devicePixelRatio),
            y: Math.round((r.top + r.height / 2) * window.devicePixelRatio)
        };
    });
    return tabs;
})()
"""

print(json.dumps(eval_js(js), indent=2))
