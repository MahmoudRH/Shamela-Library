package com.shamela.library.presentation.screens.search


import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.folioreader.ui.activity.searchActivity.SearchActivity
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.domain.model.Category

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val searchState = viewModel.searchState.collectAsState().value
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit, block = {
//        if (searchState.allCategories.isEmpty())
            viewModel.onEvent(SearchEvent.GetAllCategories)
    })
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        SearchTextField(
            value = searchState.searchQuery,
            onValueChanged = { viewModel.onEvent(SearchEvent.OnChangeSearchQuery(it)) },
            isEnabled = searchState.selectedCategories.isNotEmpty(),
            onSearch = {
                val intent = Intent(context, SearchActivity::class.java)
                intent.apply {
                    putExtra(SearchActivity.Search_Type, SearchActivity.Search_Type_SectionsSearch)
                    putExtra(SearchActivity.Search_Query, searchState.searchQuery)
                    putExtra(SearchActivity.Search_Categories, searchState.selectedCategories.map { it.name }.toTypedArray())
                }
                context.startActivity(intent)
            },
            onClear = { viewModel.onEvent(SearchEvent.OnChangeSearchQuery("")) }
        )
        SelectedSections(
            allCategories = searchState.allCategories,
            selectedCategories = searchState.selectedCategories,
            expanded = searchState.isListExpanded,
            onExpandedChange = { viewModel.onEvent(SearchEvent.ToggleCategoriesList) },
            onDismiss = { viewModel.onEvent(SearchEvent.CloseCategoriesList) },
            onItemChecked = { category -> viewModel.onEvent(SearchEvent.ItemChecked(category)) }
        )
        AnimatedVisibility(visible = searchState.selectedCategories.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                Text(
                    "يجب اختيار قسم واحد أو عدة اقسام ليتم إجراء البحث فيها",
                    style = AppFonts.textSmallBold
                )
            }
        }

    }
    LoadingScreen(visibility = searchState.isLoading)
}

@Composable
private fun SearchTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    hint: String = "كلمة البحث",
    placeholder: String = "أدخل كلمة البحث..",
    isEnabled: Boolean = true,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp, end = 16.dp),
        value = value,
        onValueChange = onValueChanged,
        textStyle = AppFonts.textNormal,
        enabled = isEnabled,
        label = { Text(text = hint, style = AppFonts.textNormal) },
        placeholder = { Text(text = placeholder, style = AppFonts.textNormal) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch()
        }),
        trailingIcon = {
            AnimatedVisibility(
                value.isNotEmpty(),
                enter = fadeIn(), exit = fadeOut()
            ) {
                IconButton(
                    onClick = onClear
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "مسح"
                    )
                }
            }
        }
    )
}

@Composable
private fun SelectedSections(
    allCategories: List<Category>,
    selectedCategories: List<Category>,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onDismiss: () -> Unit,
    onItemChecked: (Category) -> Unit,
) {
    LazyColumn() {
        item {
            Text(
                "الأقسام",
                style = AppFonts.textLarge,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
            Divider()
            Spacer(Modifier.height(16.dp))
        }
        items(selectedCategories) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemChecked(it) }
                .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(it.name, style = AppFonts.textNormal)
                Icon(imageVector = Icons.Rounded.Cancel, contentDescription = null)
            }
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = .5f))

        }
        item {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                .clickable { onExpandedChange() }
                .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("إختر قسماً", style = AppFonts.textNormalBold)
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    }
    if (expanded)
        AlertDialog(
            title = { Text("إختر قسماً أو أكثر", style = AppFonts.textLarge) },
            onDismissRequest = { onDismiss() },
            text = {
                LazyColumn() {
                    items(allCategories) { item ->
                        val isSelected = selectedCategories.contains(item)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemChecked(item)
                                    onDismiss()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onItemChecked(item) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.name, style = AppFonts.textNormal)
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = {
                    onDismiss()
                }) {
                    Text("تم", style = AppFonts.textNormal)
                }
            },
        )
}