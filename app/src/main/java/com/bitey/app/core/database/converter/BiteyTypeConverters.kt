package com.bitey.app.core.database.converter

import androidx.room.TypeConverter
import com.bitey.app.core.database.model.MealType

class BiteyTypeConverters {
    @TypeConverter
    fun fromMealType(mealType: MealType): String {
        return mealType.name
    }

    @TypeConverter
    fun toMealType(value: String): MealType {
        return try {
            MealType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MealType.FOOD
        }
    }
}
