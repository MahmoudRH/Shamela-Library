package com.shamela.library.domain.usecases.userPreferences

import com.shamela.library.domain.repo.UserPrefsDataSource
import javax.inject.Inject

class GetAvailableFontSizes @Inject constructor (private val datasource: UserPrefsDataSource) {
    operator fun invoke(): List<String> {
        return datasource.getAvailableFontSizes()
    }
}