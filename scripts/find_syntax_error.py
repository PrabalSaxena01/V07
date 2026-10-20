from cdp_client import eval_js

js = """
(function() {
    const s = document.querySelectorAll('script')[3];
    if (!s) return 'script 3 not found';
    const lines = s.text.split('\\n');
    
    // Find syntax error by parsing prefix
    let low = 1;
    let high = lines.length;
    let errLine = -1;
    let errMsg = '';
    
    // Test parsing chunks
    // Let's use Babel/Acorn or simple eval on function blocks, or let's inspect where ')' is unexpected
    for (let i = 1; i <= lines.length; i++) {
        const chunk = lines.slice(0, i).join('\\n');
        try {
            // we wrap in function to test if valid or premature end
            new Function(chunk);
        } catch (e) {
            if (e.message.includes("Unexpected token ')'")) {
                errLine = i;
                errMsg = e.message;
                break;
            }
        }
    }
    
    if (errLine !== -1) {
        return {
            errLine: errLine,
            lineContent: lines[errLine - 1],
            surrounding: lines.slice(Math.max(0, errLine - 5), Math.min(lines.length, errLine + 5)),
            error: errMsg
        };
    }
    
    // If not found by prefix, let's look for suspicious lines with ')'
    const suspicious = [];
    lines.forEach((l, idx) => {
        if (l.includes('))') || l.includes('())') || l.includes(',)') || l.includes(';)') || l.includes('() => )')) {
            suspicious.push({ lineNum: idx + 1, content: l });
        }
    });
    
    return { errLine: -1, suspicious: suspicious.slice(0, 20) };
})()
"""

import json
res = eval_js(js)
print(json.dumps(res, indent=2))
