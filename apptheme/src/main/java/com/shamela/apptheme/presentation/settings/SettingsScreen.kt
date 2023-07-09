package com.shamela.apptheme.presentation.settings


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlin.math.ceil
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    onSettingsChanged:(Int)->Unit = {}
) {
    val settingsState = viewModel.settingsState.collectAsState().value
    val isSystemDark = isSystemInDarkTheme()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SettingsSection(
            title = "تغيير الثيم",
            options = settingsState.availableThemes,
            selectedOption = settingsState.userPrefs.theme,
        ) {
            viewModel.onEvent(
                SettingsEvent.OnChangeAppTheme(
                    colorScheme = AppTheme.themeOf(
                        theme = it,
                        colorScheme = settingsState.userPrefs.colorScheme,
                        isSystemInDarkTheme = isSystemDark,
                        context = context
                    ),
                    settingsState.userPrefs.copy(theme = it)
                )
            )
            onSettingsChanged(settingsState.userPrefs.hashCode())
        }
        SettingsSection(
            title = "تغيير اللون",
            options = settingsState.availableColorSchemes,
            selectedOption = settingsState.userPrefs.colorScheme,
        ) {
            viewModel.onEvent(
                SettingsEvent.OnChangeAppTheme(
                    colorScheme = AppTheme.themeOf(
                        theme = settingsState.userPrefs.theme,
                        colorScheme = it,
                        isSystemInDarkTheme = isSystemDark,
                        context = context
                    ),
                    settingsState.userPrefs.copy(colorScheme = it)
                )
            )
            onSettingsChanged(settingsState.userPrefs.hashCode())
        }
        FontSizeSelector(
            title = "حجم الخط",
            sliderPosition = settingsState.sliderPosition,
            onSliderPositionChanged = {
                viewModel.onEvent(SettingsEvent.OnChangeSliderPosition(it))
                onSettingsChanged(settingsState.userPrefs.hashCode())
            },
            list = settingsState.availableFontSizes,
            onValueChangeFinished = {
                val sliderPosition = ceil(settingsState.sliderPosition).toInt()
                Log.e("Mah", "sliderPosition: $sliderPosition")

                viewModel.onEvent(
                    SettingsEvent.OnChangeAppFontSize(
                        settingsState.userPrefs.copy(fontSize = settingsState.availableFontSizes[sliderPosition])
                    )
                )
            }
        )


        FontsSection(
            title = "نوع الخط",
            options = settingsState.availableFontFamilies,
            selectedOption = settingsState.userPrefs.fontFamily,
        ) {
            viewModel.onEvent(
                SettingsEvent.OnChangeAppFont(
                    settingsState.userPrefs.copy(
                        fontFamily = it
                    )
                )
            )
            onSettingsChanged(settingsState.userPrefs.hashCode())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionClicked: (String) -> Unit,
) {
    Text(
        text = title, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp), style = AppFonts.textNormalBold
    )
    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
    FlowRow(modifier = Modifier.padding(top = 12.dp)) {
        options.forEach {
            val isSelected = (it == selectedOption)
            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .clickable { onOptionClicked(it) }
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.6f), CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = it,
                style = AppFonts.textNormal
            )
        }
    }
}

@Composable
private fun FontsSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionClicked: (String) -> Unit,
) {
    Text(
        text = title, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp), style = AppFonts.textNormalBold
    )
    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
    Column(modifier = Modifier.padding(top = 12.dp)) {
        options.forEach {
            val isSelected = (it == selectedOption)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(vertical = 4.dp)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOptionClicked(it) }
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(0.6f),
                        RoundedCornerShape(20.dp)
                    )
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = it,
                    style = AppFonts.textNormal.copy(fontFamily =  AppFonts.fontFamilyOf(it)),
                )
            }
        }
    }
}


@Composable
fun FontSizeSelector(
    title: String,
    sliderPosition: Float,
    list: List<Int>,
    onSliderPositionChanged: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Text(
        text = title, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp), style = AppFonts.textNormalBold
    )
    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = onSliderPositionChanged,
            valueRange = 0f..list.lastIndex.toFloat(),
            steps = ceil(list.size / 2f).toInt(),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.secondary.copy(0.7f),
                inactiveTickColor = MaterialTheme.colorScheme.tertiary,
                activeTickColor = MaterialTheme.colorScheme.secondary
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val wordsList = listOf("أصغر", "صغير", "عادي", "كبير", "أكبر")
            list.forEachIndexed { index, it ->
                val color =
                    if (sliderPosition.toInt() == index) MaterialTheme.colorScheme.primary else Color.Unspecified
                Text(
                    text = wordsList[index],
                    style = AppFonts.textNormal.copy(fontSize = (16 + it).sp, color = color)
                )
            }
        }
    }
    LaunchedEffect(sliderPosition) {
        onValueChangeFinished()
    }
}