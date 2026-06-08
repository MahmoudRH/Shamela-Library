package com.folioreader.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.readium.r2.shared.Link

/**
 * Renders a flat list of book "الموضوعات" (chapter titles) using the reader's [LinkItem] row, so the
 * look matches the in-reader table of contents ("أقسام الكتاب"). Takes plain strings, so callers in
 * other modules need no readium dependency — the [Link] wrapping stays here in :folioreader.
 *
 * v1 is non-tappable; a downloaded book could later swap this for the live, tappable EPUB TOC.
 */
@Composable
fun TopicsList(
    topics: List<String>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(topics) { index, topic ->
            LinkItem(
                item = Link().apply { title = topic; href = "" },
                level = 0,
                isFirstItem = index == 0,
                onLinkClicked = { _, _ -> },
            )
        }
    }
}
