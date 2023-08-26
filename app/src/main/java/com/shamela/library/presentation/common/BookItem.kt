package com.shamela.library.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryBookItem(
    modifier: Modifier,
    item: Book,
    onFavoriteIconClicked: () -> Unit,
    onSwipeOut: () -> Unit,
    highlightText: String = "",
    isSelected:Boolean = false
) {
    val scope = rememberCoroutineScope()
    val swipeState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart)
                onSwipeOut()
            true
        },
    )
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
    SwipeToDismiss(
        state = swipeState,
        background = {
            val color by animateColorAsState(
                targetValue = when (swipeState.targetValue) {
                    DismissValue.Default -> Color.LightGray
                    DismissValue.DismissedToEnd -> Color.Red
                    DismissValue.DismissedToStart -> Color.Red
                }, label = "swipe to dismiss background color"
            )
            val scale by animateFloatAsState(
                targetValue = if (swipeState.targetValue == DismissValue.Default) 0.8f else 1.2f,
                label = "swipe to dismiss icon scale"
            )
            val icon = Icons.Outlined.Delete
            val alignment = Alignment.CenterEnd

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 12.dp), contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.scale(scale)
                )
            }
        },
        directions = setOf(DismissDirection.EndToStart),
        dismissContent = {
            val cardElevation by animateDpAsState(
                targetValue = if (swipeState.dismissDirection != null) 4.dp else 0.dp,
                label = "swipe to dismiss card elevation"
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(cardElevation),
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp, end = 12.dp, top = 12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)                             ,
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = text,
                            maxLines = 1,
                            style = AppFonts.textNormalBold,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.author, style = AppFonts.textNormal)
                            Text(
                                text = " عدد الصفحات: ${item.pageCount}",
                                style = AppFonts.textNormal
                            )
                        }
                    }
                    IconButton(
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
    )
}