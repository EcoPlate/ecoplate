package com.example.eco_plate.di

import android.content.Context
import androidx.room.Room
import com.example.eco_plate.data.local.db.EcoPlateDatabase
import com.example.eco_plate.data.local.db.dao.ItemDao
import com.example.eco_plate.data.local.db.dao.StoreDao
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
    fun provideDatabase(@ApplicationContext context: Context): EcoPlateDatabase {
        return Room.databaseBuilder(
            context,
            EcoPlateDatabase::class.java,
            EcoPlateDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    @Singleton
    fun provideItemDao(database: EcoPlateDatabase): ItemDao {
        return database.itemDao()
    }
    
    @Provides
    @Singleton
    fun provideStoreDao(database: EcoPlateDatabase): StoreDao {
        return database.storeDao()
    }
}

