package com.shamela.library.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import com.shamela.library.presentation.theme.AppFonts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    title: String,
    actionIcon: ImageVector? = null,
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.shadow(elevation = 8.dp),
        title = { Text(text = title, style = AppFonts.textLargeBold) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp),
        ),
        actions = {
            AnimatedVisibility(visible = actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(actionIcon!!, contentDescription = null)
                }
            }

        }
    )
}