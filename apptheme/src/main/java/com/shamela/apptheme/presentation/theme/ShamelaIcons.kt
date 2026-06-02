package com.shamela.apptheme.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object ShamelaIcons {
    private fun buildIcon(name: String, pathData: String): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black)
            )
        }.build()
    }

    val Add by lazy { buildIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z") }
    val ArrowBackIosNew by lazy { buildIcon("ArrowBackIosNew", "M17.77 3.77L16 2 6 12l10 10 1.77-1.77L9.54 12z") }
    val ArrowBackIos by lazy { ArrowForwardIos }
    val ArrowForwardIos by lazy { buildIcon("ArrowForwardIos", "M6.23 20.23L8 22l10-10L8 2 6.23 3.77 14.46 12z") }
    val Book by lazy { buildIcon("Book", "M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4zm12 16H6v-4h12v4zm0-5H6V4h12v11z") }
    val Cancel by lazy { buildIcon("Cancel", "M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z") }
    val Check by lazy { buildIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z") }
    val Delete by lazy { buildIcon("Delete", "M16 9v10H8V9h8m-1.5-6h-5l-1 1H5v2h14V4h-3.5l-1-1zM18 7H6v12c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7z") }
    val Favorite by lazy { buildIcon("Favorite", "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z") }
    val FavoriteBorder by lazy { buildIcon("FavoriteBorder", "M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z") }
    val FileDownload by lazy { buildIcon("FileDownload", "M18 15v3H6v-3H4v3c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-3h-2zm-1-4l-1.41-1.41L13 12.17V4h-2v8.17L8.41 9.59 7 11l5 5 5-5z") }
    val FormatListBulleted by lazy { buildIcon("FormatListBulleted", "M4 10.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5zm0-6c-.83 0-1.5.67-1.5 1.5S3.17 7.5 4 7.5 5.5 6.83 5.5 6 4.83 4.5 4 4.5zm0 12c-.83 0-1.5.68-1.5 1.5s.68 1.5 1.5 1.5 1.5-.68 1.5-1.5-.67-1.5-1.5-1.5zM7 19h14v-2H7v2zm0-6h14v-2H7v2zm0-8v2h14V5H7z") }
    val Info by lazy { buildIcon("Info", "M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z") }
    val KeyboardArrowLeft by lazy { buildIcon("KeyboardArrowLeft", "M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z") }
    val KeyboardArrowRight by lazy { buildIcon("KeyboardArrowRight", "M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z") }
    val LocalLibrary by lazy { buildIcon("LocalLibrary", "M12 11.55C9.64 9.35 6.48 8 3 8v11c3.48 0 6.64 1.35 9 3.55 2.36-2.19 5.52-3.55 9-3.55V8c-3.48 0-6.64 1.35-9 3.55zM10.82 19.34C8.94 18.28 6.78 17.65 4.5 17.47V10c2.25.17 4.38.79 6.25 1.76l.07.03v7.55zM19.5 17.47c-2.28.18-4.44.81-6.32 1.87v-7.55l.07-.03c1.87-.97 4-1.59 6.25-1.76v7.47z") }
    val MenuBook by lazy { buildIcon("MenuBook", "M21 5c-1.11-.35-2.33-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zM21 18.5c-1.1-.35-2.3-.5-3.5-.5-1.7 0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z") }
    val MoreVert by lazy { buildIcon("MoreVert", "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z") }
    val Person by lazy { buildIcon("Person", "M12 6c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2m0 10c2.7 0 5.8 1.29 6 2H6c.23-.72 3.31-2 6-2m0-12C9.79 4 8 5.79 8 8s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 9c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z") }
    val Remove by lazy { buildIcon("Remove", "M19 13H5v-2h14v2z") }
    val Search by lazy { buildIcon("Search", "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z") }
    val Settings by lazy { buildIcon("Settings", "M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.06-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.73,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.06,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.43-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.49-0.12-0.61L19.14,12.94z M12,15.6c-1.93,0-3.5-1.57-3.5-3.5 s1.57-3.5,3.5-3.5s3.5,1.57,3.5,3.5S13.93,15.6,12,15.6z") }
    val AutoStories by lazy { buildIcon("AutoStories", "M19 1L14 3.5 9 1 4 3.5V21l5-2.5 5 2.5 5-2.5V3.5zM17 17.5l-3 1.5V6l3-1.5v13zM12 17.5l-3-1.5V6l3 1.5v13z") }
    val LibraryBooks by lazy { buildIcon("LibraryBooks", "M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9H9V9h10v2zm-4 4H9v-2h6v2zm4-8H9V5h10v2z") }
    val Palette by lazy { buildIcon("Palette", "M12 3C7.03 3 3 6.58 3 11c0 3.87 3.13 7 7 7h1c.55 0 1 .45 1 1s.45 1 1 1h1c4.42 0 8-4.03 8-9s-4.03-8-9-8zm-4 9c-.83 0-1.5-.67-1.5-1.5S7.17 9 8 9s1.5.67 1.5 1.5S8.83 12 8 12zm3-4c-.83 0-1.5-.67-1.5-1.5S10.17 5 11 5s1.5.67 1.5 1.5S11.83 8 11 8zm4 0c-.83 0-1.5-.67-1.5-1.5S14.17 5 15 5s1.5.67 1.5 1.5S15.83 8 15 8zm3 4c-.83 0-1.5-.67-1.5-1.5S17.17 9 18 9s1.5.67 1.5 1.5S18.83 12 18 12z") }
    val FormatSize by lazy { buildIcon("FormatSize", "M9 4v3h5v12h3V7h5V4H9zM2 12v3h3v7h3v-7h3v-3H2z") }
    val TextFields by lazy { buildIcon("TextFields", "M2.5 4v3h5v12h3V7h5V4h-13zm9 8v3h3v7h3v-7h3v-3h-9z") }
    val LightMode by lazy { buildIcon("LightMode", "M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.8 1.42-1.42zm10.45-1.79l-1.79 1.79 1.41 1.42 1.8-1.8-1.42-1.41zM12 4V1h-1v3h1zm0 19v-3h-1v3h1zm8-11h3v-1h-3v1zM1 12h3v-1H1v1zm15.24 6.16l1.79 1.8 1.42-1.42-1.8-1.79-1.41 1.41zM4.96 19.96l1.8-1.8-1.42-1.41-1.79 1.79 1.41 1.42zM12 6a6 6 0 100 12 6 6 0 000-12z") }
    val DarkMode by lazy { buildIcon("DarkMode", "M9.37 5.51A7 7 0 0018.49 14.63 9 9 0 1112 3c-.89 0-1.74.13-2.63.51z") }
    val AutoMode by lazy { buildIcon("AutoMode", "M19.03 7.39L20.45 6c-1.48-1.34-3.43-2.2-5.57-2.45V1h-2v2.55C8.64 4.05 5 7.7 5 12c0 4.42 3.58 8 8 8 3.73 0 6.84-2.55 7.73-6h-2.08A6.003 6.003 0 0113 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.58 0 3.01.61 4.08 1.61l-1.58 1.58H21V3.5l-1.97 1.97z") }}