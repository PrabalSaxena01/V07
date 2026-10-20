from cdp_client import eval_js
import json

js = """
(function() {
    const results = [];
    const tabs = ['home', 'tools', 'journal', 'day', 'settings'];
    
    tabs.forEach(tab => {
        try {
            switchTab(tab);
        } catch (e) {
            results.push({ tab: tab, error: 'switchTab failed: ' + e.message });
            return;
        }
        
        const container = document.getElementById('tab-' + tab);
        if (!container) return;
        
        const buttons = Array.from(container.querySelectorAll('button, a[onclick], [role="button"]'));
        buttons.forEach(btn => {
            const onclick = btn.getAttribute('onclick') || '';
            const text = (btn.innerText || btn.getAttribute('title') || btn.getAttribute('aria-label') || '').slice(0, 30).trim();
            
            // Test if onclick references an undefined function
            const fnMatches = onclick.match(/([a-zA-Z0-9_$]+)\\s*\\(/g) || [];
            fnMatches.forEach(m => {
                const fnName = m.replace('(', '').trim();
                if (['void', 'alert', 'confirm', 'prompt', 'parseInt', 'parseFloat', 'encodeURIComponent', 'decodeURIComponent', 'closest'].includes(fnName)) return;
                if (typeof window[fnName] !== 'function' && typeof eval(fnName) !== 'function') {
                    results.push({
                        tab: tab,
                        button: text,
                        onclick: onclick,
                        missingFunction: fnName
                    });
                }
            });
        });
    });

    switchTab('home');
    return results;
})()
"""

res = eval_js(js)
print(json.dumps(res, indent=2))
