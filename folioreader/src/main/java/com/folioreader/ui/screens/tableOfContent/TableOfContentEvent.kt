package com.folioreader.ui.screens.tableOfContent

import org.readium.r2.shared.Publication


sealed class TableOfContentEvent{
    class LoadTableOfContentsOf(val publication: Publication):TableOfContentEvent()
}