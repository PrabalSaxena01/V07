# -*- coding: utf-8 -*-
from cdp_client import eval_js

script = """
(function() {
  const html = document.documentElement;
  const body = document.body;
  const appContainer = document.getElementById('app-container');
  const main = document.querySelector('main');
  
  return JSON.stringify({
    html: {
      overflow: window.getComputedStyle(html).overflow,
      overflowY: window.getComputedStyle(html).overflowY,
      height: window.getComputedStyle(html).height,
      maxHeight: window.getComputedStyle(html).maxHeight,
      scrollTop: html.scrollTop,
      scrollHeight: html.scrollHeight,
      clientHeight: html.clientHeight
    },
    body: {
      overflow: window.getComputedStyle(body).overflow,
      overflowY: window.getComputedStyle(body).overflowY,
      height: window.getComputedStyle(body).height,
      maxHeight: window.getComputedStyle(body).maxHeight,
      scrollTop: body.scrollTop,
      scrollHeight: body.scrollHeight,
      clientHeight: body.clientHeight
    },
    main: main ? {
      tag: main.tagName,
      id: main.id,
      className: main.className,
      overflow: window.getComputedStyle(main).overflow,
      overflowY: window.getComputedStyle(main).overflowY,
      height: window.getComputedStyle(main).height,
      scrollTop: main.scrollTop,
      scrollHeight: main.scrollHeight,
      clientHeight: main.clientHeight
    } : null,
    appContainer: appContainer ? {
      tag: appContainer.tagName,
      id: appContainer.id,
      className: appContainer.className,
      overflow: window.getComputedStyle(appContainer).overflow,
      overflowY: window.getComputedStyle(appContainer).overflowY,
      height: window.getComputedStyle(appContainer).height,
      scrollTop: appContainer.scrollTop,
      scrollHeight: appContainer.scrollHeight,
      clientHeight: appContainer.clientHeight
    } : null
  });
})()
"""

print(eval_js(script))
