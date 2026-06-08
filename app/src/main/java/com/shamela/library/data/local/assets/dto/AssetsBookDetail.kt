package com.shamela.library.data.local.assets.dto

internal data class AssetsBookDetail(
    val title: String,
    val authorDeathYear: Int? = null,
    val about: List<AssetsBookInfo>? = null,
    val description: String? = null,
    val descriptionSource: String? = null,
    val descriptionUrl: String? = null,
    val descriptionModel: String? = null,
    val topics: List<String>? = null,
)

internal data class AssetsBookInfo(
    val label: String,
    val value: String,
)
