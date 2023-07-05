package com.shamela.apptheme.domain.usecases.userPreferences

import com.shamela.apptheme.domain.model.UserPrefs
import com.shamela.apptheme.domain.datasource.UserPrefsDataSource

class UpdateUserPreferences(private val datasource: UserPrefsDataSource) {
    operator fun invoke(userPreferences: UserPrefs) {
        datasource.updateUserPrefs(userPreferences)
    }
}