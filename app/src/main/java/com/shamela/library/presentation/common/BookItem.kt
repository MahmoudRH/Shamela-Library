package com.shamela.library.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.search.BookSearchMatcher

private fun buildHighlightedString(text: String, query: String): AnnotatedString =
    buildAnnotatedString {
        val range = BookSearchMatcher.findHighlightRange(text, query)
        if (range == null) {
            append(text)
        } else {
            append(text.substring(0, range.first))
            withStyle(SpanStyle(
                fontSize = AppFonts.textNormal.fontSize.value.sp,
                background = Color(0xfff8ff00),
                color = Color.Black
            )) { append(text.substring(range)) }
            append(text.substring(range.last + 1))
        }
    }

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
        Icon(imageVector = ShamelaIcons.ArrowBackIosNew, contentDescription = null)
    },
    item: Book,
    highlightText: String = "",
    onInfoClick: (() -> Unit)? = null,
) {
    val text = buildHighlightedString(item.title, highlightText)
    val authorText = buildHighlightedString(item.author, highlightText)
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
                Text(text = authorText, style = AppFonts.textNormal)
                Text(text = " عدد الصفحات: ${item.pageCount}", style = AppFonts.textNormal)
            }
        }
        InfoIconButton(onInfoClick)
        icon()
    }
}

@Composable
private fun InfoIconButton(onInfoClick: (() -> Unit)?) {
    if (onInfoClick != null) {
        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = ShamelaIcons.Info,
                contentDescription = "عن الكتاب",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
fun FavoriteBookItem(
    modifier: Modifier,
    item: Book,
    onFavoriteIconClicked: () -> Unit,
    highlightText: String = "",
    onInfoClick: (() -> Unit)? = null,
) {
    val text = buildHighlightedString(item.title, highlightText)
    val authorText = buildHighlightedString(item.author, highlightText)
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
                Text(text = authorText, style = AppFonts.textNormal)
                Text(text = " عدد الصفحات: ${item.pageCount}", style = AppFonts.textNormal)
            }
        }
        InfoIconButton(onInfoClick)
        IconButton(
//            modifier = Modifier.size(55.dp),
            onClick = onFavoriteIconClicked
        ) {
            val tintColor by animateColorAsState(
                targetValue = if (item.isFavorite) Color(0xff8B0000) else LocalContentColor.current,
                label = ""
            )
            Icon(
                imageVector = if (item.isFavorite) ShamelaIcons.Favorite else ShamelaIcons.FavoriteBorder,
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
    isSelected: Boolean = false,
    onInfoClick: (() -> Unit)? = null,
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onSwipeOut()
            }
            // Don't dismiss the item; let the confirmation dialog decide.
            // The card snaps back, and the actual removal happens via state update on confirm.
            false
        },
    )
    val text = buildHighlightedString(item.title, highlightText)
    val authorText = buildHighlightedString(item.author, highlightText)
    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (swipeState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> Color.Red
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }, label = "swipe to dismiss background color"
            )
            val scale by animateFloatAsState(
                targetValue = if (swipeState.targetValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
                label = "swipe to dismiss icon scale"
            )
            val icon = ShamelaIcons.Delete
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
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        content = {
            val cardElevation by animateDpAsState(
                targetValue = if (swipeState.dismissDirection != SwipeToDismissBoxValue.Settled) 4.dp else 0.dp,
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
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = ShamelaIcons.Check,
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
                            Text(text = authorText, style = AppFonts.textNormal)
                            Text(
                                text = " عدد الصفحات: ${item.pageCount}",
                                style = AppFonts.textNormal
                            )
                        }
                    }
                    InfoIconButton(onInfoClick)
                    IconButton(
                        onClick = onFavoriteIconClicked
                    ) {
                        val tintColor by animateColorAsState(
                            targetValue = if (item.isFavorite) Color(0xff8B0000) else LocalContentColor.current,
                            label = ""
                        )
                        Icon(
                            imageVector = if (item.isFavorite) ShamelaIcons.Favorite else ShamelaIcons.FavoriteBorder,
                            contentDescription = null,
                            tint = tintColor
                        )
                    }
                }
            }
        }
    )
}