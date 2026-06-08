package com.shamela.apptheme.presentation.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.R
import com.shamela.apptheme.domain.model.UserPrefs
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.theme.ShamelaIcons
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            item {
                Column {
                    Text(
                        text = "المظهر والتخصيص",
                        style = AppFonts.textLargeBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "قم بتخصيص واجهة القراءة بما يناسبك",
                        style = AppFonts.textNormal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Theme Selection
            item {
                GlassSection(
                    title = stringResource(R.string.change_theme),
                    icon = { Icon(ShamelaIcons.AutoMode, contentDescription = null) }
                ) {
                    ThemeSelector(
                        selected = uiState.userPrefs.theme,
                        options = uiState.availableThemes,
                        onSelected = {
                            onEvent(
                                PreferenceSettingsEvent.OnChangeAppTheme(
                                    colorScheme = AppTheme.themeOf(
                                        theme = it,
                                        colorScheme = uiState.userPrefs.colorSchemeName,
                                        isSystemInDarkTheme = isSystemDark,
                                        context = context
                                    ),
                                    uiState.userPrefs.copy(theme = it)
                                )
                            )
                        }
                    )
                }
            }

            // Color Scheme Selection
            item {
                GlassSection(
                    title = stringResource(R.string.change_color),
                    icon = { Icon(ShamelaIcons.Palette, contentDescription = null) }
                ) {
                    ColorSchemeSelector(
                        options = uiState.availableColorSchemes,
                        selected = uiState.userPrefs.colorSchemeName,
                        selectedTheme = uiState.userPrefs.theme,
                        isSystemInDarkTheme = isSystemDark,
                        context = context
                    ) { scheme, name ->
                        onEvent(
                            PreferenceSettingsEvent.OnChangeAppTheme(
                                colorScheme = scheme,
                                userPrefs = uiState.userPrefs.copy(colorSchemeName = name)
                            )
                        )
                    }
                }
            }

            // Font Size Selection
            item {
                GlassSection(
                    title = stringResource(R.string.font_size),
                    icon = { Icon(ShamelaIcons.FormatSize, contentDescription = null) }
                ) {
                    ModernFontSlider(
                        sliderPosition = uiState.sliderPosition,
                        list = uiState.availableFontSizes,
                        selectedFontFamily = AppFonts.fontFamilyOf(uiState.userPrefs.fontFamily),
                        onSliderPositionChanged = {
                            onEvent(PreferenceSettingsEvent.OnChangeSliderPosition(it))
                        },
                        onValueChangeFinished = { finalPosition ->
                            val index = ceil(finalPosition).toInt()
                            onEvent(
                                PreferenceSettingsEvent.OnChangeAppFontSize(
                                    uiState.userPrefs.copy(fontSize = uiState.availableFontSizes[index])
                                )
                            )
                        }
                    )
                }
            }

            // Font Family Selection
            item {
                GlassSection(
                    title = stringResource(R.string.font_family),
                    icon = { Icon(ShamelaIcons.TextFields, contentDescription = null) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.availableFontFamilies.forEach { font ->
                            FontPreviewCard(
                                title = font,
                                selected = uiState.userPrefs.fontFamily == font,
                                fontFamily = AppFonts.fontFamilyOf(font)
                            ) {
                                onEvent(PreferenceSettingsEvent.OnChangeAppFont(uiState.userPrefs.copy(fontFamily = font)))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassSection(
    title: String,
    icon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    // Provide the primary color to the icon
                    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(onSurface = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        icon()
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = AppFonts.textLargeBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun ThemeSelector(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val icon = when (option) {
                "فاتح" -> ShamelaIcons.LightMode
                "مظلم" -> ShamelaIcons.DarkMode
                else -> ShamelaIcons.AutoMode
            }

            val isSelected = option == selected
            val containerColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "ThemeContainerColor"
            )
            val contentColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "ThemeContentColor"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor)
                    .clickable { onSelected(option) }
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = option, style = AppFonts.textSmallBold, color = contentColor)
            }
        }
    }
}

@Composable
private fun ColorSchemeSelector(
    options: List<String>,
    selected: String,
    selectedTheme: String,
    isSystemInDarkTheme: Boolean,
    context: Context,
    onSelected: (ColorScheme, String) -> Unit
) {
    // LazyRow prevents squishing if you add more colors
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(options) { option ->
            val scheme = AppTheme.themeOf(
                theme = selectedTheme,
                colorScheme = option,
                isSystemInDarkTheme = isSystemInDarkTheme,
                context = context
            )

            val isSelected = selected == option
            val borderSize by animateDpAsState(
                targetValue = if (isSelected) 3.dp else 0.dp,
                label = "BorderSize"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary)))
                        .border(borderSize, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .clickable { onSelected(scheme, option) },
                    contentAlignment = Alignment.Center
                ) {
                    this@Column.AnimatedVisibility(
                        visible = isSelected,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),

                        ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                ShamelaIcons.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = option,
                    style = AppFonts.textSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }
}


@Composable
private fun ModernFontSlider(
    sliderPosition: Float,
    list: List<Int>,
    selectedFontFamily: FontFamily,
    onSliderPositionChanged: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
) {
    var currentValue by remember { mutableFloatStateOf(sliderPosition) }
    LaunchedEffect(sliderPosition) { currentValue = sliderPosition }

    Column {
        // Book-like Preview Card
//        Card(
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//            ),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Box(modifier = Modifier
//                .padding(24.dp)
//                .fillMaxWidth(), contentAlignment = Alignment.Center) {
//                Text(
//                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
//                    style = AppFonts.textNormal.copy(
//                        fontFamily = selectedFontFamily,
//                        fontSize = (16 + list[currentValue.toInt()]).sp // dynamic preview size!
//                    ),
//                    color = MaterialTheme.colorScheme.onSurface,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }

//        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = currentValue,
            onValueChange = {
                currentValue = it
                onSliderPositionChanged(it)
            },
            onValueChangeFinished = { onValueChangeFinished(currentValue) },
            valueRange = 0f..max(list.lastIndex.toFloat(), 0f),
            steps = max(list.size - 2, 0),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labels = listOf(
                stringResource(R.string.xSmall),
                stringResource(R.string.small),
                stringResource(R.string.normal),
                stringResource(R.string.large),
                stringResource(R.string.xLarge)
            )

            list.forEachIndexed { index, _ ->
                val isSelected = currentValue.toInt() == index
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = if (isSelected) AppFonts.textSmallBold else AppFonts.textSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FontPreviewCard(
    title: String,
    selected: Boolean,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        label = "FontCardContainer"
    )
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "FontCardBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppFonts.textSmallBold,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اللغة العربية جميلة للغاية",
                    style = AppFonts.textNormal.copy(fontFamily = fontFamily, fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(
                visible = selected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = ShamelaIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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