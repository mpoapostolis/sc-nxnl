package com.skincoach.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanEntity::class], version = 1, exportSchema = false)
abstract class SkinCoachDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile
        private var instance: SkinCoachDatabase? = null

        fun get(context: Context): SkinCoachDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkinCoachDatabase::class.java,
                    "skincoach.db",
                ).build().also { instance = it }
            }
    }
}
