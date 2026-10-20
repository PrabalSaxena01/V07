import sys
import os
import json
import time

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from cdp_client import eval_js

def run_tests():
    print("Testing document.title...")
    title = eval_js("document.title")
    print("Title:", title)

    print("\n--- 1. Testing Journal Tab ---")
    res_journal = eval_js("""
        (() => {
            try {
                if (typeof switchTab !== 'function') return { error: 'switchTab not defined' };
                switchTab('journal');
                const tab = document.getElementById('tab-journal');
                const container = document.getElementById('journal-entries-container');
                return {
                    success: true,
                    tabVisible: tab && !tab.classList.contains('hidden'),
                    containerExists: !!container,
                    containerChildCount: container ? container.children.length : 0
                };
            } catch(e) {
                return { error: e.toString() };
            }
        })()
    """)
    print("Journal Tab Result:", json.dumps(res_journal, indent=2))

    print("\n--- 2. Testing Screen Time Modal ---")
    res_screentime = eval_js("""
        (() => {
            try {
                if (typeof openScreenTimeModal !== 'function') return { error: 'openScreenTimeModal not defined' };
                openScreenTimeModal();
                const modal = document.getElementById('modal-screentime');
                const isOpen = modal && modal.classList.contains('translate-y-0') && modal.classList.contains('opacity-100');
                closeScreenTimeModal();
                const isClosed = modal && modal.classList.contains('translate-y-full');
                return {
                    success: true,
                    modalExists: !!modal,
                    isOpen,
                    isClosed
                };
            } catch(e) {
                return { error: e.toString() };
            }
        })()
    """)
    print("Screen Time Modal Result:", json.dumps(res_screentime, indent=2))

    print("\n--- 3. Testing Open Timer (Stopwatch 0 -> Infinity) ---")
    res_timer = eval_js("""
        (() => {
            try {
                if (typeof openFocusModal !== 'function') return { error: 'openFocusModal not defined' };
                if (typeof setFocusMode !== 'function') return { error: 'setFocusMode not defined' };
                
                openFocusModal();
                setFocusMode('timer');
                
                const modal = document.getElementById('modal-focus');
                const isOpen = modal && modal.classList.contains('translate-y-0');
                const mode = window.focusCurrentMode;
                const secStart = window.focusModalSeconds;
                const timeTextStart = document.getElementById('focus-modal-time')?.textContent;
                const targetDuration = document.getElementById('focus-target-duration')?.textContent;

                // Simulate ticking up
                window.focusModalSeconds += 3;
                renderFocusModal();
                const secTicked = window.focusModalSeconds;
                const timeTextTicked = document.getElementById('focus-modal-time')?.textContent;

                // Reset
                resetFocusTimer();
                const secReset = window.focusModalSeconds;
                const timeTextReset = document.getElementById('focus-modal-time')?.textContent;

                closeFocusModal();

                return {
                    success: true,
                    isOpen,
                    mode,
                    secStart,
                    timeTextStart,
                    targetDuration,
                    secTicked,
                    timeTextTicked,
                    secReset,
                    timeTextReset
                };
            } catch(e) {
                return { error: e.toString() };
            }
        })()
    """)
    print("Open Timer Result:", json.dumps(res_timer, indent=2))

if __name__ == '__main__':
    run_tests()
