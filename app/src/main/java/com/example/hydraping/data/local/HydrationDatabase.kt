package com.example.hydraping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hydraping.data.local.dao.FocusTargetDao
import com.example.hydraping.data.local.dao.WaterEntryDao
import com.example.hydraping.data.local.entity.FocusTarget
import com.example.hydraping.data.local.entity.WaterEntry

@Database(
    entities = [WaterEntry::class, FocusTarget::class],
    version = 2,
    exportSchema = true
)
abstract class HydrationDatabase : RoomDatabase() {
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun focusTargetDao(): FocusTargetDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `focus_targets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `startHour` INTEGER NOT NULL,
                        `startMinute` INTEGER NOT NULL,
                        `endHour` INTEGER NOT NULL,
                        `endMinute` INTEGER NOT NULL,
                        `targetAmountMl` INTEGER NOT NULL,
                        `repeatMode` TEXT NOT NULL,
                        `customDays` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
