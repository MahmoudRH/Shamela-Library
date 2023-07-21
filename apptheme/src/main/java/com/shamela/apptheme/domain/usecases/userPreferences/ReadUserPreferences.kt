package com.shamela.apptheme.domain.usecases.userPreferences

import com.shamela.apptheme.domain.datasource.UserPrefsDataSource
import com.shamela.apptheme.domain.model.UserPrefs

class ReadUserPreferences(private val datasource: UserPrefsDataSource) {
    operator fun invoke(): UserPrefs {
        return datasource.getUserPrefs()
    }
}