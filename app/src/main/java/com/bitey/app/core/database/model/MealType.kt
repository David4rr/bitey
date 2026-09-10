package com.bitey.app.core.database.model

import java.util.Calendar

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    LATE_NIGHT;

    companion object {
        fun fromTimestamp(timestamp: Long): MealType {
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..10 -> BREAKFAST
                in 11..14 -> LUNCH
                in 15..17 -> SNACK
                in 18..21 -> DINNER
                else -> LATE_NIGHT
            }
        }
    }
}
