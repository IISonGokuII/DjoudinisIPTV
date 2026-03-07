package com.djoudini.iptv.di

import android.content.Context
import androidx.room.Room
import com.djoudini.iptv.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "djoudinis_iptv_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideProviderDao(database: AppDatabase): ProviderDao = database.providerDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideChannelDao(database: AppDatabase): ChannelDao = database.channelDao()

    @Provides
    @Singleton
    fun provideVodDao(database: AppDatabase): VodDao = database.vodDao()

    @Provides
    @Singleton
    fun provideSeriesDao(database: AppDatabase): SeriesDao = database.seriesDao()

    @Provides
    @Singleton
    fun provideEpgDao(database: AppDatabase): EpgDao = database.epgDao()

    @Provides
    @Singleton
    fun provideVodProgressDao(database: AppDatabase): VodProgressDao = database.vodProgressDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()
}
