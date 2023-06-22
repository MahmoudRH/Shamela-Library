package com.folioreader.ui.screens.tableOfContent

import org.readium.r2.shared.Link


data class TableOfContentState(
    val isLoading: Boolean = true,
    val items: List<Link> = emptyList(),
)