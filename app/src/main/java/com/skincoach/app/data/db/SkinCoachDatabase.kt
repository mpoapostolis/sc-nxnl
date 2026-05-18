package com.skincoach.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScanEntity::class, RoutineDayEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class SkinCoachDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun routineDayDao(): RoutineDayDao

    companion object {
        /** v2 adds the routine_days table for the daily-streak feature. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `routine_days` (" +
                        "`epochDay` INTEGER NOT NULL, " +
                        "`checkedTitles` TEXT NOT NULL, " +
                        "`completed` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`epochDay`))",
                )
            }
        }

        @Volatile
        private var instance: SkinCoachDatabase? = null

        fun get(context: Context): SkinCoachDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkinCoachDatabase::class.java,
                    "skincoach.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
