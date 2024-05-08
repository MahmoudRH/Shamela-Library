package com.shamela.library.data.di

import android.app.Application
import androidx.room.Room
import com.shamela.library.data.local.assets.AssetsBooksRepoImpl
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.db.BooksDao
import com.shamela.library.data.local.db.BooksDatabase
import com.shamela.library.data.local.db.QuotesDao
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.quotes.QuotesUseCases
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

    @Provides
    fun provideDatabase(app: Application): BooksDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            BooksDatabase::class.java,
            BooksDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideBooksDao(database: BooksDatabase): BooksDao {
        return database.booksDao
    }

    @Provides
    fun provideQuotesDao(database: BooksDatabase): QuotesDao {
        return database.quotesDao
    }

    @Singleton
    @Provides
    fun provideQuotesUseCases(dao: QuotesDao):QuotesUseCases{
        return QuotesUseCases(dao =dao,)
    }

    @AssetsRepoImpl
    @Singleton
    @Provides
    fun provideAssetsBooksUseCases(repo: AssetsBooksRepoImpl, dao: BooksDao): BooksUseCases {
        return BooksUseCases(
            repository = repo,
            booksDao = dao
        )
    }

    @FilesRepoImpl
    @Singleton
    @Provides
    fun provideFilesBooksUseCases(repo: FilesBooksRepoImpl, dao: BooksDao): BooksUseCases {
        return BooksUseCases(
            repository = repo,
            booksDao = dao
        )
    }
}