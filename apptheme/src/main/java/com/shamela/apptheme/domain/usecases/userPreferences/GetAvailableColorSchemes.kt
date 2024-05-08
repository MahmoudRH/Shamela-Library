package com.shamela.apptheme.domain.usecases.userPreferences

import com.shamela.apptheme.domain.datasource.UserPrefsDataSource

class GetAvailableColorSchemes(private val datasource: UserPrefsDataSource) {
    operator fun invoke(): List<String> {
        return datasource.getAvailableColorSchemes()
    }
}