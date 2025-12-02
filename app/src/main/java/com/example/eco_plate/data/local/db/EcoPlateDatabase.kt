package com.example.eco_plate.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.eco_plate.data.local.db.dao.ItemDao
import com.example.eco_plate.data.local.db.dao.StoreDao
import com.example.eco_plate.data.local.db.entity.CachedItem
import com.example.eco_plate.data.local.db.entity.CachedStore

@Database(
    entities = [CachedItem::class, CachedStore::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EcoPlateDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun storeDao(): StoreDao
    
    companion object {
        const val DATABASE_NAME = "ecoplate_db"
    }
}

