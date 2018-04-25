package com.akorn.intake.data

import android.arch.persistence.room.*
import java.text.SimpleDateFormat
import java.util.*


@Database(entities = [(Meal::class)], version = 1)
@TypeConverters(MealSizeConverter::class, MealTypeConverter::class, MealItemsConverter::class, DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeal(vararg meals: Meal)

    @Query("SELECT * FROM meal WHERE id = :mealId")
    fun loadMeal(mealId: String): Meal?

    @Query("SELECT * FROM meal")
    fun loadAllMeals(): Array<Meal>

    @Delete
    fun deleteMeal(vararg meals: Meal): Int
}

@Entity
data class Meal(
        @PrimaryKey
        val id: String = "${UUID.randomUUID()}",
        @TypeConverters(DateTypeConverter::class)
        val date: Date = Date(),
        @TypeConverters(MealSizeConverter::class)
        val mealSize: MealSize,
        @TypeConverters(MealTypeConverter::class)
        val mealType: MealType,
        @TypeConverters(MealItemsConverter::class)
        val items: List<String> = emptyList())

enum class MealType { Snack, Drink, Breakfast, Lunch, Dinner }
enum class MealSize {
    Light {
        override fun toFloat(): Float = 14f // 2
    },
    Normal {
        override fun toFloat(): Float = 18f // 4
    },
    Heavy {
        override fun toFloat(): Float = 20f //6
    };

    abstract fun toFloat(): Float
}


class DateTypeConverter {
    @TypeConverter
    fun toDate(value: Long): Date = Date(value)

    @TypeConverter
    fun toLong(value: Date): Long = value.time
}

class MealSizeConverter {
    @TypeConverter
    fun toMealSize(size: String): MealSize = when (size) {
        "Light" -> MealSize.Light
        "Heavy" -> MealSize.Heavy
        else -> MealSize.Normal
    }

    @TypeConverter
    fun toString(mealSize: MealSize): String = mealSize.name
}

class MealTypeConverter {
    @TypeConverter
    fun toMealType(type: String): MealType = when (type) {
        "Drink" -> MealType.Drink
        "Breakfast" -> MealType.Breakfast
        "Lunch" -> MealType.Lunch
        "Dinner" -> MealType.Dinner
        else -> MealType.Snack
    }

    @TypeConverter
    fun toString(mealType: MealType): String = mealType.name
}

class MealItemsConverter {
    @TypeConverter
    fun toItems(items: String): List<String> = items.split(",").map { it.trim() }

    @TypeConverter
    fun toString(items: List<String>): String = items.joinToString(",")
}

data class Day(val date: String = Date().toDateOnlyString(), val meals: List<Meal> = emptyList(), val daySize: Float)

fun Date.toDateOnlyString(): String = SimpleDateFormat("MMMM d - EEEE", Locale.getDefault()).format(this)
fun Date.toTimeOnlyString(): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(this)