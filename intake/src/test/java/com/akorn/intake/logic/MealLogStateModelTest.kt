package com.akorn.intake.logic

import com.akorn.intake.data.DaysLogResponse
import com.akorn.intake.data.Meal
import com.akorn.intake.data.MealSize
import com.akorn.intake.data.MealType
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Test

class MealLogStateModelTest {

    @Test
    fun mealLogReducer_DaysLogResponse_Test() {
        val mockMeal = Meal(id = "mock1", mealSize = MealSize.Heavy, mealType = MealType.Breakfast)

        val successBlankState = mealLogReducer(DaysLogResponse.AllMeals.Success(emptyList()), MealLogTree.Loading)
        assertThat(successBlankState, instanceOf(MealLogTree.Done.Blank::class.java))

        val successDataState = mealLogReducer(DaysLogResponse.AllMeals.Success(listOf(mockMeal)), MealLogTree.Loading)
        assertThat(successDataState, instanceOf(MealLogTree.Done.Ready.NewLog::class.java))
        assertEquals(mockMeal, (successDataState as MealLogTree.Done.Ready.NewLog).days.first().meals.first())

        val failedGettingLogState = mealLogReducer(DaysLogResponse.AllMeals.Failed, MealLogTree.Loading)
        assertThat(failedGettingLogState, instanceOf(MealLogTree.Done.MealLogStateError.CouldNotLoadMeals::class.java))

        val failedDeletingMealState = mealLogReducer(DaysLogResponse.DeletedMeal.Failed, MealLogTree.Loading)
        assertThat(failedDeletingMealState, instanceOf(MealLogTree.Done.MealLogStateError.CouldNotDeleteMeal::class.java))
    }
}