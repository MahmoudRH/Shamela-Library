package com.shamela.library.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import com.shamela.library.BuildConfig
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.BookInfoItem

/**
 * "About the book" sheet. Shows the basic fields from [book] plus the metadata extracted from the
 * EPUB (publisher, editor, edition, author death year, …) loaded on demand from book-details assets.
 * Renders gracefully for books without extra metadata (e.g. the محاضرات مفرغة category).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsBottomSheet(
    book: Book,
    onDismiss: () -> Unit,
    viewModel: BookDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(book.id) { viewModel.load(book.categoryName, book.title) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(text = book.title, style = AppFonts.textLargeBold)
                Spacer(Modifier.height(8.dp))

                val authorLine = buildString {
                    append(book.author)
                    state.details?.authorDeathYear?.let { append(" (ت $it هـ)") }
                }
                IconText(icon = ShamelaIcons.Person, text = authorLine)
                Spacer(Modifier.height(4.dp))
                IconText(icon = ShamelaIcons.Book, text = "عدد الصفحات: ${book.pageCount}")

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.4f))
                Spacer(Modifier.height(12.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                        )
                    }
                } else {
                    val details = state.details
                    val description = details?.description
                    if (!description.isNullOrBlank()) {
                        Text(
                            text = "نبذة عن الكتاب",
                            style = AppFonts.textNormalBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(text = description, style = AppFonts.textNormal)
                        // Web-sourced descriptions carry a source; a sourceless one is AI-generated.
                        val src = details.descriptionSource?.takeIf { it.isNotBlank() }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (src != null) "المصدر: $src" else "تم توليده بالذكاء الصناعي",
                            style = AppFonts.textSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        // DEBUG-only QA label: which AI model produced this نبذة. Never shown in release.
                        if (BuildConfig.DEBUG && src == null) {
                            val model = details.descriptionModel?.takeIf { it.isNotBlank() }
                                ?: "غير معروف"
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "🛠 النموذج: $model",
                                style = AppFonts.textSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    val about = details?.about.orEmpty()
                    about.forEach { info ->
                        InfoRow(info)
                        Spacer(Modifier.height(10.dp))
                    }

                    val topics = details?.topics.orEmpty()
                    if (topics.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "الموضوعات",
                            style = AppFonts.textNormalBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        topics.forEach { topic ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "•  ",
                                    style = AppFonts.textNormal,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(text = topic, style = AppFonts.textNormal)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    if (description.isNullOrBlank() && about.isEmpty() && topics.isEmpty()) {
                        Text(
                            text = "لا تتوفر معلومات إضافية عن هذا الكتاب.",
                            style = AppFonts.textNormal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = AppFonts.textNormal)
    }
}

@Composable
private fun InfoRow(info: BookInfoItem) {
    if (info.label.isBlank()) {
        // Bracketed note lines (e.g. "[ترقيم الكتاب موافق للمطبوع]") have no label.
        Text(
            text = info.value,
            style = AppFonts.textSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(
                text = "${info.label}: ",
                style = AppFonts.textNormalBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = info.value,
                style = AppFonts.textNormal,
                modifier = Modifier.weight(1f, fill = false),
                textAlign = TextAlign.Start,
            )
        }
    }
}
