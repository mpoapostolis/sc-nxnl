package com.skincoach.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One day's routine check-in. [epochDay] is days since the Unix epoch
 * (`LocalDate.toEpochDay()`), which makes streak maths simple integer steps.
 */
@Entity(tableName = "routine_days")
data class RoutineDayEntity(
    @PrimaryKey val epochDay: Long,
    /** Newline-joined titles of the steps checked off that day. */
    val checkedTitles: String,
    /** True once every daily-essential step was checked that day. */
    val completed: Boolean,
)
