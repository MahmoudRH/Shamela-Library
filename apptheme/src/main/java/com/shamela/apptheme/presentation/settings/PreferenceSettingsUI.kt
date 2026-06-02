package com.shamela.apptheme.presentation.settings


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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.R
import com.shamela.apptheme.domain.model.UserPrefs
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.util.ShamelaPrev
import kotlin.math.ceil
import kotlin.math.max


@Composable
fun PreferenceSettingsUI(
    modifier: Modifier = Modifier,
    uiState: PreferenceSettingsState,
    onEvent: (PreferenceSettingsEvent) -> Unit,
) {
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
            title = stringResource(R.string.change_theme),
            options = uiState.availableThemes,
            selectedOption = uiState.userPrefs.theme,
        ) {
            onEvent(
                PreferenceSettingsEvent.OnChangeAppTheme(
                    colorScheme = AppTheme.themeOf(
                        theme = it,
                        colorScheme = uiState.userPrefs.colorScheme,
                        isSystemInDarkTheme = isSystemDark,
                        context = context
                    ),
                    uiState.userPrefs.copy(theme = it)
                )
            )
        }
        SettingsSection(
            title = stringResource(R.string.change_color),
            options = uiState.availableColorSchemes,
            selectedOption = uiState.userPrefs.colorScheme,
        ) {
            onEvent(
                PreferenceSettingsEvent.OnChangeAppTheme(
                    colorScheme = AppTheme.themeOf(
                        theme = uiState.userPrefs.theme,
                        colorScheme = it,
                        isSystemInDarkTheme = isSystemDark,
                        context = context
                    ),
                    userPrefs = uiState.userPrefs.copy(colorScheme = it)
                )
            )
        }
        FontSizeSelector(
            title = stringResource(R.string.font_size),
            sliderPosition = uiState.sliderPosition,
            onSliderPositionChanged = {
                onEvent(PreferenceSettingsEvent.OnChangeSliderPosition(it))
            },
            list = uiState.availableFontSizes,
            onValueChangeFinished = {
                val sliderPosition = ceil(uiState.sliderPosition).toInt()

                onEvent(
                    PreferenceSettingsEvent.OnChangeAppFontSize(
                        uiState.userPrefs.copy(fontSize = uiState.availableFontSizes[sliderPosition])
                    )
                )
            }
        )


        FontsSection(
            title = stringResource(R.string.font_family),
            options = uiState.availableFontFamilies,
            selectedOption = uiState.userPrefs.fontFamily,
        ) {
            onEvent(
                PreferenceSettingsEvent.OnChangeAppFont(uiState.userPrefs.copy(fontFamily = it))
            )
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
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
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
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
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
                    style = AppFonts.textNormal.copy(fontFamily = AppFonts.fontFamilyOf(it)),
                )
            }
        }
    }
}


@Composable
private fun FontSizeSelector(
    title: String,
    sliderPosition: Float,
    list: List<Int>,
    onSliderPositionChanged: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp), style = AppFonts.textNormalBold
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = onSliderPositionChanged,
            valueRange = 0f..max(list.lastIndex.toFloat(),0f),
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
            val wordsList = listOf(
                stringResource(R.string.xSmall),
                stringResource(R.string.small),
                stringResource(R.string.normal),
                stringResource(R.string.large),
                stringResource(R.string.xLarge),
            )
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

@ShamelaPrev
@Composable
private fun PreferenceScreenPrev() {
    AppTheme.ShamelaLibraryTheme {
        PreferenceSettingsUI(
            uiState = PreferenceSettingsState(
                userPrefs = UserPrefs(),
                availableFontSizes = listOf(-4, -2, 0, 2, 4),
                availableFontFamilies = listOf("خط النسخ", "خط الرق"),
                availableColorSchemes = listOf("ذهبي", "ازرق"),
                availableThemes = listOf("فاتح", "مظلم", "تلقائي")
            ), onEvent = {}
        )
    }
}