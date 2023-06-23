package com.folioreader.ui.composables


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.folioreader.R
import com.folioreader.model.HighlightImpl
import com.folioreader.ui.view.UnderlinedTextView
import com.folioreader.util.UiUtil
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun HighlightsItem(
    item: HighlightImpl,
    onItemClick: (HighlightImpl) -> Unit,
    onDeleteClicked: (Int) -> Unit,
) {

    Box(Modifier
        .fillMaxWidth()
        .clickable {
            onItemClick(item)
        }
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.fillMaxWidth(0.9f)) {
            Text(text = formatDate(item.date), style = AppFonts.textNormal)
            Spacer(modifier = Modifier.height(4.dp))
            HtmlText(html = item.content, item.type)
        }

        IconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = { onDeleteClicked(item.id) }) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
private fun HtmlText(html: String, type: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val typeFace = AppFonts.selectedFontTypeFace(context)
            val textView = UnderlinedTextView(context)
            textView.typeface = typeFace
            textView.textSize = AppFonts.textNormal.fontSize.value
            textView
        },
        update = {
            val isDarkTheme: Boolean = AppTheme.isDarkTheme(it.context)
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            it.setTextColor(
                ContextCompat.getColor(
                    it.context,
                    if (isDarkTheme) R.color.white else R.color.black
                )
            )
            UiUtil.setBackColorToTextView(it, type, isDarkTheme)
        }
    )
}

/**  @return date formatted into arabic day and month name */
private fun formatDate(date: Date?): String {
    return date?.let { SimpleDateFormat("dd-MMM", Locale.forLanguageTag("ar")).format(it) } ?: "-"
}


