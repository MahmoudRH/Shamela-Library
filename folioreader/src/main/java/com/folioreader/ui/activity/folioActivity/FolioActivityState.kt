package com.folioreader.ui.activity.folioActivity

import org.readium.r2.shared.Publication

data class FolioActivityState(
    val isTopActivity: Boolean = true,
    val taskImportance: Int = 0,
    val publication: Publication? = null,
    val isLoading: Boolean = true,
)