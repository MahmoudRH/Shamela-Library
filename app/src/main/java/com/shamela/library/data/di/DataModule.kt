package com.shamela.library.data.di

import android.app.Application
import com.shamela.library.data.local.assets.AssetsBooksRepoImpl
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.data.remote.BooksRepoImpl
import com.shamela.library.domain.repo.BooksRepository
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.books.GetAllBooks
import com.shamela.library.domain.usecases.books.GetAllCategories
import com.shamela.library.domain.usecases.books.GetBooksByCategory
import com.shamela.library.domain.usecases.books.GetDownloadUri
import com.shamela.library.domain.usecases.books.SearchForABook
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Singleton
    @Provides
    fun provideAssetsBooksRepository(app: Application): AssetsBooksRepoImpl {
        return AssetsBooksRepoImpl(app.applicationContext)
    }

    @Singleton
    @Provides
    fun provideFilesBooksRepository(): FilesBooksRepoImpl {
        return FilesBooksRepoImpl
    }

    @AssetsRepoImpl
    @Singleton
    @Provides
    fun provideAssetsBooksUseCases(repo: AssetsBooksRepoImpl): BooksUseCases {
        return BooksUseCases(
            repository = repo
        )
    }

    @FilesRepoImpl
    @Singleton
    @Provides
    fun provideFilesBooksUseCases(repo: FilesBooksRepoImpl): BooksUseCases {
        return BooksUseCases(
            repository = repo
        )
    }
}
/*

    @FilesRepoImpl
    @Singleton
    @Provides
    fun provideBooksUseCases(): BooksUseCases {
        return BooksUseCases(repository = FilesBooksRepoImpl)
    }

    @AssetsRepoImpl
    @Singleton
    @Provides
    fun provideBooksUseCases(repo: AssetsBooksRepoImpl): BooksUseCases {
        return BooksUseCases(
            repository = repo
        )
    }
 */