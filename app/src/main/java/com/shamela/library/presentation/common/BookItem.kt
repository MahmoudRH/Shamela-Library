package com.shamela.library.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.library.domain.model.Book

//@Composable
//fun BookItem(
//    modifier: Modifier,
//    icon: @Composable () -> Unit = {
//        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = null)
//    },
//    item: Book,
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Column(Modifier.fillMaxWidth(0.9f)) {
//            Text(
//                text = item.title,
//                maxLines = 1,
//                style = AppFonts.textNormalBold,
//                overflow = TextOverflow.Ellipsis
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Text(text = item.author, style = AppFonts.textNormal)
//                Text(text = " عدد الصفحات: ${item.pageCount}", style = AppFonts.textNormal)
//            }
//        }
//        icon()
//    }
//}

@Composable
fun BookItem(
    modifier: Modifier,
    icon: @Composable () -> Unit = {
        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = null)
    },
    item: Book,
    highlightText: String = "",
) {
    val text = buildAnnotatedString {
        item.title.run {
            val textBefore = substring(0, indexOf(highlightText))
            val textAfter = substring(indexOf(highlightText) + highlightText.length)
            append(textBefore)
            withStyle(
                style = SpanStyle(
                    fontSize = (AppFonts.textNormal.fontSize.value).sp,
                    background = Color(0xfff8ff00),
                    color = Color.Black
                )
            ) {
                append(highlightText)
            }
            append(textAfter)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.fillMaxWidth(0.9f)) {
            Text(
                text = text,
                maxLines = 1,
                style = AppFonts.textNormalBold,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.author, style = AppFonts.textNormal)
                Text(text = " عدد الصفحات: ${item.pageCount}", style = AppFonts.textNormal)
            }
        }
        icon()
    }
}

@Composable
fun LocalBookItem(
    modifier: Modifier,
    item: Book,
    onFavoriteIconClicked: () -> Unit,
    highlightText: String = "",
) {
    val text = buildAnnotatedString {
        item.title.run {
            val textBefore = substring(0, indexOf(highlightText))
            val textAfter = substring(indexOf(highlightText) + highlightText.length)
            append(textBefore)
            withStyle(
                style = SpanStyle(
                    fontSize = (AppFonts.textNormal.fontSize.value).sp,
                    background = Color(0xfff8ff00),
                    color = Color.Black
                )
            ) {
                append(highlightText)
            }
            append(textAfter)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = text,
                maxLines = 1,
                style = AppFonts.textNormalBold,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.author, style = AppFonts.textNormal)
                Text(text = " عدد الصفحات: ${item.pageCount}", style = AppFonts.textNormal)
            }
        }
        IconButton(
//            modifier = Modifier.size(55.dp),
            onClick = onFavoriteIconClicked
        ) {
            val tintColor by animateColorAsState(
                targetValue = if (item.isFavorite) Color(0xff8B0000) else LocalContentColor.current,
                label = ""
            )
            Icon(
                imageVector = if (item.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = tintColor
            )
        }
    }
}