# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

m = re.search(r'<div id="tab-tools"[\s\S]*?(?=\s*<!-- Page Tab: Journal|\s*<div id="tab-journal")', text)
if m:
    content = m.group(0)
    print("Full tab-tools content length:", len(content))
    articles = re.findall(r'<article[\s\S]*?</article>', content)
    print(f"Total articles/cards in tab-tools: {len(articles)}")
    for i, a in enumerate(articles):
        title_m = re.search(r'<h3[^>]*>([^<]+)</h3>', a)
        btn_m = re.findall(r'<button[^>]*onclick="([^"]+)"[^>]*>([\s\S]*?)</button>', a)
        title = title_m.group(1).strip() if title_m else "No title"
        print(f"Card {i+1}: {title}")
        for fn, btext in btn_m:
            clean_btext = re.sub(r'<[^>]+>', '', btext).strip()
            print(f"   Button: onclick='{fn}' -> '{clean_btext}'")
    
    # Are there any other elements in tab-tools?
    non_articles = re.sub(r'<article[\s\S]*?</article>', '[CARD]', content)
    print("\nNon-card content in tab-tools:")
    print(non_articles[:1500])
