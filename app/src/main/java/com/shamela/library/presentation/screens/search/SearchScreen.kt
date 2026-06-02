package com.shamela.library.presentation.screens.search


import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folioreader.ui.activity.searchActivity.SearchActivity
import com.shamela.apptheme.presentation.common.EmptyListScreen
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import com.shamela.library.domain.model.Category
import com.shamela.library.presentation.screens.LocalPaddingValues

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val searchState = viewModel.searchState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val localPadding = LocalPaddingValues.current
    LaunchedEffect(key1 = Unit, block = {
//        if (searchState.allCategories.isEmpty())
            viewModel.onEvent(SearchEvent.GetAllCategories)
    })
    Column(modifier = Modifier.fillMaxSize().padding(localPadding), horizontalAlignment = Alignment.CenterHorizontally) {

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
        CategoryFilterRow(
            allCategories = searchState.allCategories,
            selectedCategories = searchState.selectedCategories,
            onItemChecked = { viewModel.onEvent(SearchEvent.ItemChecked(it)) }
        )
        AnimatedVisibility(visible = searchState.selectedCategories.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = ShamelaIcons.Info, contentDescription = null)
                Text(
                    "يجب اختيار قسم واحد أو عدة اقسام ليتم إجراء البحث فيها",
                    style = AppFonts.textSmallBold
                )
            }
        }

    }
    LoadingScreen(visibility = searchState.isLoading && searchState.allCategories.isNotEmpty())
    EmptyListScreen(visibility = searchState.allCategories.isEmpty(), text = "لا بد من تحميل بعض الكتب قبل التمكن من البحث")
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
                        imageVector = ShamelaIcons.Cancel,
                        contentDescription = "مسح"
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    allCategories: List<Category>,
    selectedCategories: List<Category>,
    onItemChecked: (Category) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allCategories) { category ->
            val isSelected = selectedCategories.contains(category)
            FilterChip(
                selected = isSelected,
                onClick = { onItemChecked(category) },
                label = { Text(category.name, style = AppFonts.textSmallBold) },
                leadingIcon = if (isSelected) {
                    { Icon(ShamelaIcons.Check, contentDescription = null) }
                } else null
            )
        }
    }
}