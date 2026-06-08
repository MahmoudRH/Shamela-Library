package com.folioreader.ui.activity.folioActivity.book

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.shamela.apptheme.domain.model.DictionaryEntry
import com.shamela.apptheme.presentation.theme.AppFonts

/**
 * Stateless bottom sheet that shows the offline-dictionary definition for the
 * word the user selected in the reader. Driven entirely by [BookState]; mirrors
 * the app module's BookDetailsBottomSheet pattern (ModalBottomSheet + RTL).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeaningBottomSheet(
    word: String,
    loading: Boolean,
    dictionaryAvailable: Boolean,
    results: List<DictionaryEntry>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth()) {
                    Text(text = word, style = AppFonts.textLargeBold)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))

                    when {
                        loading -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }

                        !dictionaryAvailable -> Text(
                            text = "القاموس غير محمّل بعد. يمكنك تحميله من الإعدادات ثم إعادة المحاولة.",
                            style = AppFonts.textNormal,
                        )

                        results.isEmpty() -> Text(
                            text = "لا توجد نتائج لكلمة \"$word\".",
                            style = AppFonts.textNormal,
                        )

                        else -> results.forEachIndexed { index, entry ->
                            if (entry.root.isNotBlank() && entry.root != word) {
                                Text(text = "[${entry.root}]", style = AppFonts.textNormalBold)
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(text = entry.definition, style = AppFonts.textNormal)
                            if (index != results.lastIndex) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
