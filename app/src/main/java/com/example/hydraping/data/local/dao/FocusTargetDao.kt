package com.example.hydraping.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.hydraping.data.local.entity.FocusTarget
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusTargetDao {

    @Insert
    suspend fun insert(target: FocusTarget): Long

    @Update
    suspend fun update(target: FocusTarget)

    @Delete
    suspend fun delete(target: FocusTarget)

    @Query("SELECT * FROM focus_targets WHERE isActive = 1 ORDER BY startHour, startMinute")
    fun getActiveTargets(): Flow<List<FocusTarget>>

    @Query("SELECT * FROM focus_targets ORDER BY startHour, startMinute")
    fun getAllTargets(): Flow<List<FocusTarget>>

    @Query("SELECT * FROM focus_targets WHERE id = :id")
    suspend fun getById(id: Int): FocusTarget?

    @Query("UPDATE focus_targets SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Int, active: Boolean)
}
