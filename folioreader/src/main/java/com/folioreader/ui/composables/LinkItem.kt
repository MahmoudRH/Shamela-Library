package com.folioreader.ui.composables


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.shamela.apptheme.presentation.theme.AppFonts
import org.readium.r2.shared.Link

@Composable
fun LinkItem(
    item: Link,
    level: Int,
    isFirstItem: Boolean = false,
    onLinkClicked: (String?, String?) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val color = when (level) {
        3 -> MaterialTheme.colorScheme.primary.copy(alpha = 1f - .6f)
        2 -> MaterialTheme.colorScheme.primary.copy(alpha = 1f - .7f)
        1 -> MaterialTheme.colorScheme.primary.copy(alpha = 1f - .8f)
        0 -> MaterialTheme.colorScheme.primary.copy(alpha = 1f - .9f)
        else -> {
            MaterialTheme.colorScheme.background
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .background(color)
            .drawBehind {
                val strokeWidth = 1 * density
                val y = size.height - strokeWidth / 2
                val borderColor = color.copy(alpha = 0.5f)

                if (isFirstItem) {
                    drawLine(
                        borderColor,
                        Offset(0f, 1f),
                        Offset(size.width, 1f),
                        strokeWidth
                    )
                }
                drawLine(
                    borderColor,
                    Offset(size.width, 0f),
                    Offset(size.width, size.height),
                    strokeWidth
                )
                drawLine(
                    borderColor,
                    Offset(0f, 0f),
                    Offset(0f, size.height),
                    strokeWidth
                )

                drawLine(
                    borderColor,
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )

            }
            .clickable { onLinkClicked(item.title, item.href)  }
            .padding(vertical = 4.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp + (12 * level).dp))
        if (item.children.isNotEmpty() && level != 3) {
            IconButton(onClick = { isExpanded = !isExpanded }) {
                if (isExpanded) {
                    Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Shrink")
                } else {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "expand")
                }
            }
        } else {
            Spacer(modifier = Modifier.width(16.dp + (12 * level).dp))
        }
        Text(text = item.title ?: "", style = AppFonts.textNormal)
    }
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {

        Column {
            item.children.forEach {
                if (level != 3) {
                    LinkItem(
                        item = it,
                        level = level + 1,
                        onLinkClicked = { title, href -> onLinkClicked(title, href) })
                }
            }
        }
    }
}


