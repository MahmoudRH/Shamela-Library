package com.shamela.library.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import com.shamela.library.domain.util.BookSortOption

/**
 * A sort icon button that opens a dropdown letting the user pick a [BookSortOption] and toggle
 * the sort direction. Stateless: the caller owns [sortOption]/[ascending] and reacts to callbacks.
 *
 * [availableOptions] lets a screen hide options that don't apply (e.g. DOWNLOAD_TIME for remote
 * books that have no local file).
 */
@Composable
fun BookSortMenu(
    sortOption: BookSortOption,
    ascending: Boolean,
    onOptionSelected: (BookSortOption) -> Unit,
    onToggleDirection: () -> Unit,
    modifier: Modifier = Modifier,
    availableOptions: List<BookSortOption> = BookSortOption.values().toList(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = ShamelaIcons.Sort, contentDescription = "ترتيب الكتب")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Text(
                text = "ترتيب حسب",
                style = AppFonts.textNormalBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            availableOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.label, style = AppFonts.textNormal) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    leadingIcon = {
                        if (option == sortOption) {
                            Icon(
                                imageVector = ShamelaIcons.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            // Keep labels aligned with the checked row.
                            Box(Modifier.size(24.dp))
                        }
                    }
                )
            }
            HorizontalDivider()
            // Toggling direction keeps the menu open so the change is visible.
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (ascending) "تصاعدي" else "تنازلي",
                        style = AppFonts.textNormal
                    )
                },
                onClick = onToggleDirection,
                leadingIcon = {
                    Icon(
                        imageVector = if (ascending) ShamelaIcons.ArrowUpward else ShamelaIcons.ArrowDownward,
                        contentDescription = null
                    )
                }
            )
        }
    }
}