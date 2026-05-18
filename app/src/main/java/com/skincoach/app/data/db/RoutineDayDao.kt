package com.skincoach.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: RoutineDayEntity)

    @Query("SELECT * FROM routine_days WHERE epochDay = :epochDay")
    fun observe(epochDay: Long): Flow<RoutineDayEntity?>

    @Query("SELECT * FROM routine_days WHERE epochDay = :epochDay")
    suspend fun getOnce(epochDay: Long): RoutineDayEntity?

    @Query("SELECT epochDay FROM routine_days WHERE completed = 1 ORDER BY epochDay")
    fun observeCompletedDays(): Flow<List<Long>>
}
