//
//  Bridge.js
//  FolioReader-Android
//
//  Created by Heberti Almeida on 06/05/15.
//  Copyright (c) 2015 Folio Reader. All rights reserved.
//
//  Cleaned: removed jQuery, jsface, Rangy, readium-cfi, and all dead-code functions
//  that referenced disconnected Java bridges (WebViewPager, FolioPageFragment, SSBridge,
//  LoadingView, EPUBcfi, Highlight). Only the API surface actually called from Kotlin is kept.
//

// ---------------------------------------------------------------------------
// Class manipulation utilities
// ---------------------------------------------------------------------------

function hasClass(ele, cls) {
    return !!ele.className.match(new RegExp('(\\s|^)' + cls + '(\\s|$)'));
}

function addClass(ele, cls) {
    if (!hasClass(ele, cls)) ele.className += " " + cls;
}

function removeClass(ele, cls) {
    if (hasClass(ele, cls)) {
        const reg = new RegExp('(\\s|^)' + cls + '(\\s|$)');
        ele.className = ele.className.replace(reg, ' ');
    }
}

// ---------------------------------------------------------------------------
// Media overlay (called via injected <script> tag from HtmlUtil)
// ---------------------------------------------------------------------------

function setMediaOverlayStyle(style) {
    document.documentElement.classList.remove(
        "mediaOverlayStyle0", "mediaOverlayStyle1", "mediaOverlayStyle2"
    );
    document.documentElement.classList.add(style);
}

// Called from HtmlUtil injected script: setMediaOverlayStyleColors('#C0ED72','#C0ED72')
function setMediaOverlayStyleColors(color, colorHighlight) {
    // Dynamic overlay colour rules are not applied in this build.
}

// ---------------------------------------------------------------------------
// Anchor navigation (used for in-book links)
// ---------------------------------------------------------------------------

function scrollAnchor(id) {
    window.location.hash = id;
}

// ---------------------------------------------------------------------------
// Text utilities
// ---------------------------------------------------------------------------

function getBodyText() {
    return document.body.innerText;
}

function getReadingTime() {
    const text = document.body.innerText;
    const totalWords = text.trim().split(/\s+/g).length;
    return Math.round(totalWords / 3); // 180 wpm → 3 words/sec
}

// ---------------------------------------------------------------------------
// Selection rect — called from CustomWebView via evaluateJavascript()
// Depends on RangeFix (rangefix.js) for accurate RTL bounding rect on WebKit.
// ---------------------------------------------------------------------------

function getSelectionRect(element) {
    let range;
    if (element !== undefined) {
        range = document.createRange();
        range.selectNodeContents(element);
    } else {
        range = window.getSelection().getRangeAt(0);
    }
    const rect = RangeFix.getBoundingClientRect(range);
    return {
        left:   rect.left,
        top:    rect.top,
        right:  rect.right,
        bottom: rect.bottom
    };
}

function clearSelection() {
    window.getSelection().removeAllRanges();
}

// ---------------------------------------------------------------------------
// Text selection popup actions — called from CustomWebView via loadUrl()
// ---------------------------------------------------------------------------

function onTextSelectionItemClicked(id) {
    const selectedText = window.getSelection().toString();
    FolioWebView.onTextSelectionItemClicked(id, selectedText);
}

// ---------------------------------------------------------------------------
// Tap detection — <html onclick="onClickHtml()"> injected by HtmlUtil
// ---------------------------------------------------------------------------

function onClickHtml() {
    const selection = document.getSelection().toString().trim();
    if (!selection) {
        CustomWebView.isTapped();
    }
}

// ---------------------------------------------------------------------------
// Search result highlighting — called via loadUrl() from FolioActivity
// ---------------------------------------------------------------------------

function highlightSearchLocator(text) {
    try {
        const walker = document.createTreeWalker(
            document.body, NodeFilter.SHOW_TEXT, null, false
        );
        let node;
        while ((node = walker.nextNode())) {
            const content = node.textContent;
            const start = content.indexOf(text);
            if (start !== -1) {
                const end = start + text.length;
                if (end <= content.length) {
                    const range = document.createRange();
                    range.setStart(node, start);
                    range.setEnd(node, end);
                    window.getSelection().removeAllRanges();
                    window.getSelection().addRange(range);
                    highlightSelectedText();
                    break;
                }
            }
        }
    } catch (e) {
        console.error("highlightSearchLocator: " + e);
    }
}

function highlightSelectedText() {
    const selection = window.getSelection();
    if (selection && !selection.isCollapsed) {
        const range = selection.getRangeAt(0);
        const span = document.createElement('span');
        span.style.backgroundColor = 'yellow';
        span.style.color = 'black';
        range.surroundContents(span);
        selection.removeAllRanges();
        span.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

// ---------------------------------------------------------------------------
// Selection change detection — notifies Android when user selects text
// ---------------------------------------------------------------------------

function detectSelection() {
    const selection = document.getSelection().toString();
    if (selection) {
        CustomWebView.textSelected(selection);
    }
}

function oneOffDebounce(func, wait) {
    let timeout;
    return function (...args) {
        if (timeout) clearTimeout(timeout);
        timeout = setTimeout(() => {
            timeout = null;
            func(...args);
        }, wait);
    };
}

document.addEventListener("selectionchange", oneOffDebounce(detectSelection, 1000));
