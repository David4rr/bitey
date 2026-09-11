package com.bitey.app.core.database.model

import java.util.Calendar

enum class MealType(val label: String) {
    FOOD("Food"),
    DRINK("Drink"),
    OTHER("Other");

    companion object {
        fun fromTimestamp(timestamp: Long = System.currentTimeMillis()): MealType {
            return FOOD
        }
    }
}
