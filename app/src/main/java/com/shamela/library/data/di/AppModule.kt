package com.shamela.library.data.di

import android.app.Application
import com.shamela.library.data.local.sharedPrefs.SharedPreferencesData
import com.shamela.library.domain.repo.UserPrefsDataSource
import com.shamela.library.domain.usecases.userPreferences.GetAvailableColorSchemes
import com.shamela.library.domain.usecases.userPreferences.GetAvailableFontFamilies
import com.shamela.library.domain.usecases.userPreferences.GetAvailableFontSizes
import com.shamela.library.domain.usecases.userPreferences.GetAvailableThemes
import com.shamela.library.domain.usecases.userPreferences.ReadUserPreferences
import com.shamela.library.domain.usecases.userPreferences.UpdateUserPreferences
import com.shamela.library.domain.usecases.userPreferences.UserPreferencesUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Singleton
    @Provides
    fun provideUserPreferencesDatasource(app: Application): UserPrefsDataSource {
        return SharedPreferencesData(app)
    }
    @Singleton
    @Provides
    fun provideUserPrefsUseCases(datasource:UserPrefsDataSource): UserPreferencesUseCases{
        return UserPreferencesUseCases(
            readUserPreferences = ReadUserPreferences(datasource),
            updateUserPreferences = UpdateUserPreferences(datasource),
            getAvailableFontFamilies = GetAvailableFontFamilies(datasource),
            getAvailableThemes = GetAvailableThemes(datasource),
            getAvailableColorSchemes = GetAvailableColorSchemes(datasource),
            getAvailableFontSizes = GetAvailableFontSizes(datasource)
        )
    }
}