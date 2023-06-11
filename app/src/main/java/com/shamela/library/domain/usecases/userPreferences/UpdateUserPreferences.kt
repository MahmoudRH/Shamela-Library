package com.shamela.library.domain.usecases.userPreferences

import com.shamela.library.domain.model.UserPrefs
import com.shamela.library.domain.repo.UserPrefsDataSource
import javax.inject.Inject

class UpdateUserPreferences @Inject constructor (private val datasource: UserPrefsDataSource) {
    operator fun invoke(userPreferences: UserPrefs) {
        datasource.updateUserPrefs(userPreferences)
    }
}