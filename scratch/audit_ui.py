from cdp_client import eval_js
import json

js = """
(function() {
    const issues = [];
    
    // Check all buttons and clickable elements
    const clickables = Array.from(document.querySelectorAll('button, a, [onclick], [role="button"]'));
    clickables.forEach((el) => {
        const rect = el.getBoundingClientRect();
        const onclick = el.getAttribute('onclick');
        const text = el.innerText ? el.innerText.trim() : '';
        
        const style = window.getComputedStyle(el);
        if (style.display !== 'none' && style.visibility !== 'hidden' && rect.width > 0) {
            if (rect.width < 36 || rect.height < 36) {
                issues.push({
                    type: 'tiny_touch_target',
                    id: el.id,
                    tag: el.tagName,
                    text: text.slice(0, 30),
                    width: Math.round(rect.width),
                    height: Math.round(rect.height),
                    onclick: onclick ? onclick.slice(0, 40) : null
                });
            }
        }
    });

    // Check text overflow (elements where scrollWidth > clientWidth)
    const allElements = Array.from(document.querySelectorAll('*'));
    allElements.forEach(el => {
        const style = window.getComputedStyle(el);
        if (style.display !== 'none' && style.visibility !== 'hidden' && !['HTML', 'BODY', 'MAIN', 'NAV', 'SECTION'].includes(el.tagName)) {
            // Check if horizontal scroll is spilling out without overflow control
            if (el.scrollWidth > el.clientWidth + 4 && el.clientWidth > 40) {
                if (style.overflowX !== 'auto' && style.overflowX !== 'scroll') {
                    issues.push({
                        type: 'overflow_spill',
                        tag: el.tagName,
                        id: el.id,
                        className: String(el.className).slice(0, 50),
                        scrollWidth: el.scrollWidth,
                        clientWidth: el.clientWidth,
                        overflowX: style.overflowX,
                        text: (el.innerText || '').slice(0, 50).replace(/\\n/g, ' ')
                    });
                }
            }
        }
    });

    return {
        totalClickables: clickables.length,
        totalIssues: issues.length,
        issues: issues
    };
})()
"""

res = eval_js(js)
print(json.dumps(res, indent=2))
