package com.shamela.library.data.di

import android.app.Application
import com.shamela.library.data.local.assets.AssetsBooksRepoImpl
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
    fun provideBooksRepository(app:Application): BooksRepository {
        return AssetsBooksRepoImpl(app.applicationContext)
    }

    @Singleton
    @Provides
    fun provideBooksUseCases(repo:BooksRepository): BooksUseCases{
        return  BooksUseCases(
            getAllCategories = GetAllCategories(repo),
            getAllBooks = GetAllBooks(repo),
            getBooksByCategory = GetBooksByCategory(repo),
            searchForABook = SearchForABook(repo),
            getDownloadUri = GetDownloadUri(repo)
        )
    }
}