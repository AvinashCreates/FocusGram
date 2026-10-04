package com.focusgram.app

import android.webkit.WebView

object FocusInjector {

    fun install(webView: WebView, reelMode: Boolean) {
        val script = """
            (() => {
                const STYLE_ID = 'focusgram-style';
                const GUARD_ID = 'focusgram-reel-guard';

                const css = `
                    /* Navigation-level removal */
                    a[href*="/reels/"],
                    a[href^="/reels"],
                    a[href*="/explore/"],
                    a[href^="/explore"],
                    [aria-label="Reels"],
                    [aria-label="Explore"],
                    [aria-label="Reels and more"],
                    [role="link"][href*="/reels/"],
                    [role="link"][href*="/explore/"] {
                        display: none !important;
                        visibility: hidden !important;
                        pointer-events: none !important;
                    }
                `;

                function installStyle() {
                    try {
                        let style = document.getElementById(STYLE_ID);
                        if (!style) {
                            style = document.createElement('style');
                            style.id = STYLE_ID;
                            document.documentElement.appendChild(style);
                        }
                        if (style.textContent !== css) style.textContent = css;
                    } catch (_) {}
                }

                function installReelGuard(enabled) {
                    try {
                        const old = document.getElementById(GUARD_ID);
                        if (old) old.remove();

                        if (!enabled) return;

                        const script = document.createElement('script');
                        script.id = GUARD_ID;
                        script.textContent = '(' + function() {
                            if (window.__focusGramReelGuard) return;
                            window.__focusGramReelGuard = true;

                            let startY = 0;
                            let startX = 0;

                            document.addEventListener('touchstart', function(e) {
                                if (!e.touches || !e.touches[0]) return;
                                startY = e.touches[0].clientY;
                                startX = e.touches[0].clientX;
                            }, {passive: true, capture: true});

                            document.addEventListener('touchmove', function(e) {
                                if (!e.touches || !e.touches[0]) return;

                                const dy = e.touches[0].clientY - startY;
                                const dx = e.touches[0].clientX - startX;

                                if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 8) {
                                    e.preventDefault();
                                }
                            }, {passive: false, capture: true});

                            document.addEventListener('wheel', function(e) {
                                if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) {
                                    e.preventDefault();
                                }
                            }, {passive: false, capture: true});
                        }.toString() + ')();';
                        document.documentElement.appendChild(script);
                    } catch (_) {}
                }

                installStyle();
                installReelGuard(${reelMode});

                if (!window.__focusGramObserver) {
                    window.__focusGramObserver = new MutationObserver(() => {
                        installStyle();
                    });

                    try {
                        window.__focusGramObserver.observe(document.documentElement, {
                            subtree: true,
                            childList: true,
                            attributes: true
                        });
                    } catch (_) {}
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }
}
