package com.example.hydraping.di

import android.content.Context
import androidx.room.Room
import com.example.hydraping.data.local.HydrationDatabase
import com.example.hydraping.data.local.PreferencesDataStore
import com.example.hydraping.data.local.dao.WaterEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HydrationDatabase {
        return Room.databaseBuilder(
            context,
            HydrationDatabase::class.java,
            "hydration_database"
        ).build()
    }

    @Provides
    fun provideWaterEntryDao(database: HydrationDatabase): WaterEntryDao {
        return database.waterEntryDao()
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
