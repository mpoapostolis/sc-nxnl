package com.skincoach.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Insert
    suspend fun insert(scan: ScanEntity): Long

    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(): ScanEntity?

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
