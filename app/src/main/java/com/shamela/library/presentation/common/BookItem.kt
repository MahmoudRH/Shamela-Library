package com.shamela.library.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.domain.model.Book
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
fun FavoriteBookItem(
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

@Composable
fun LibraryBookItem(
    modifier: Modifier,
    item: Book,
    onFavoriteIconClicked: () -> Unit,
    onSwipeOut: () -> Unit,
    highlightText: String = "",
) {
    val scope = rememberCoroutineScope()

    var offsetX by remember {
        mutableFloatStateOf(0f)
    }
    var alpha by remember {
        mutableFloatStateOf(0f)
    }
    val vanishOffsetRight = remember { androidx.compose.animation.core.Animatable(250f) }
    val vanishOffsetLeft = remember { androidx.compose.animation.core.Animatable(-250f) }

    fun resetOffset() {
        scope.launch {
            val resetOffset = Animatable(offsetX)
            resetOffset.animateTo(0f, tween(180)) { offsetX = value }
        }
    }

    fun swipeOut() {
        scope.launch {
            launch {
                val vanishAlpha = Animatable(0.3f)
                vanishAlpha.animateTo(1f, tween(80)) { alpha = value }
            }
            launch {
                if (offsetX > 0)
                    vanishOffsetRight.animateTo(1030f, tween(80)) { offsetX = value }
                else
                    vanishOffsetLeft.animateTo(-1050f, tween(80)) { offsetX = value }
            }
        }.invokeOnCompletion {
            onSwipeOut()
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (alpha.absoluteValue > 0) Color.Red.copy(alpha = alpha.absoluteValue.coerceIn(0f, 1f)) else Color.Transparent)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = try {
                        (offsetX) / 1000
                    } catch (e: ArithmeticException) {
                        1f
                    }
                    this.alpha = (1f - alpha.absoluteValue).coerceIn(0f, 1f)
                    this.translationX = offsetX
                }
                .draggable(
                    rememberDraggableState(onDelta = { delta ->
                        offsetX += delta
                    }),
                    onDragStopped = { velocity ->
                        if (velocity.absoluteValue < 2000) {
                            resetOffset()
                        } else {
                            swipeOut()
                        }
                    },
                    orientation = Orientation.Horizontal
                ),
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
}