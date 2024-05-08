package com.shamela.apptheme.domain.usecases.userPreferences

import com.shamela.apptheme.domain.datasource.UserPrefsDataSource
import com.shamela.apptheme.domain.model.UserPrefs

class UpdateUserPreferences(private val datasource: UserPrefsDataSource) {
    operator fun invoke(userPreferences: UserPrefs) {
        datasource.updateUserPrefs(userPreferences)
    }
}