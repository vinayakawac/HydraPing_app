package com.example.hydraping.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hydraping.data.local.entity.WaterEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {

    @Insert
    suspend fun insert(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterEntry>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getTotalForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getTotalForRange(startTime: Long, endTime: Long): Int

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteById(id: Int)
}
