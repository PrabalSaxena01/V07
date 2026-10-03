import json
from cdp_client import eval_js

js = """
(function() {
    const results = {};
    
    // Check points
    const points = [
        { name: 'nav-tools', x: window.innerWidth * 0.35, y: window.innerHeight - 30 },
        { name: 'nav-home', x: window.innerWidth * 0.15, y: window.innerHeight - 30 },
        { name: 'center', x: window.innerWidth * 0.5, y: window.innerHeight * 0.5 },
        { name: 'header', x: window.innerWidth * 0.5, y: 40 },
        { name: 'timer-btn', x: window.innerWidth * 0.5, y: 350 }
    ];
    
    results.points = points.map(p => {
        const el = document.elementFromPoint(p.x, p.y);
        let cur = el;
        const chain = [];
        while (cur && cur !== document.body && cur !== document.documentElement) {
            chain.push(cur.tagName + (cur.id ? '#' + cur.id : '') + (cur.className ? '.' + String(cur.className).replace(/\\s+/g, '.') : ''));
            cur = cur.parentElement;
        }
        return {
            name: p.name,
            x: p.x,
            y: p.y,
            tag: el ? el.tagName : null,
            id: el ? el.id : null,
            classes: el ? el.className : null,
            chain: chain.slice(0, 5)
        };
    });
    
    // Check if any overlays exist
    const allFixed = Array.from(document.querySelectorAll('*')).filter(el => {
        const style = window.getComputedStyle(el);
        return style.position === 'fixed' || style.position === 'absolute';
    }).map(el => {
        const style = window.getComputedStyle(el);
        const rect = el.getBoundingClientRect();
        return {
            id: el.id,
            tag: el.tagName,
            classes: el.className,
            display: style.display,
            visibility: style.visibility,
            opacity: style.opacity,
            pointerEvents: style.pointerEvents,
            zIndex: style.zIndex,
            rect: { top: rect.top, left: rect.left, width: rect.width, height: rect.height }
        };
    }).filter(info => {
        return info.display !== 'none' && info.rect.width > 200 && info.rect.height > 200;
    });
    
    results.largeOverlays = allFixed;
    
    // Check touch/pointer event listeners or preventDefault
    results.windowPointerEvents = window.getComputedStyle(document.body).pointerEvents;
    results.htmlPointerEvents = window.getComputedStyle(document.documentElement).pointerEvents;
    
    return results;
})()
"""

print(json.dumps(eval_js(js), indent=2))
