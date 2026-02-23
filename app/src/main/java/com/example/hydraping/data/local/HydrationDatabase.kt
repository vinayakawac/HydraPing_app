package com.example.hydraping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hydraping.data.local.dao.WaterEntryDao
import com.example.hydraping.data.local.entity.WaterEntry

@Database(entities = [WaterEntry::class], version = 1, exportSchema = true)
abstract class HydrationDatabase : RoomDatabase() {
    abstract fun waterEntryDao(): WaterEntryDao
}
