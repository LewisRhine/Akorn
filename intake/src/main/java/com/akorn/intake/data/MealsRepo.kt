package com.akorn.intake.data

import com.akorn.akorn.AppAction
import com.akorn.akorn.DataRepo
import com.akorn.akorn.RepoResponse
import com.akorn.intake.EditMealActions
import com.akorn.intake.LogActions
import com.akorn.intake.app

object MealsRepo : DataRepo<DaysLogResponse>() {

    override fun onAction(action: AppAction): DaysLogResponse? = when (action) {
        LogActions.GetLog -> {
            try {
                val meals = app.database.mealDao().loadAllMeals().toList()
                DaysLogResponse.AllMeals.Success(meals)
            } catch (e: Exception) {
                DaysLogResponse.AllMeals.Failed
            }
        }

        is EditMealActions.EditExistingMeal -> {
            try {
                val mealToEdit = app.database.mealDao().loadMeal(action.mealId)
                if (mealToEdit != null) {
                    DaysLogResponse.EditExistingMeal.Success(mealToEdit)
                } else {
                    DaysLogResponse.EditExistingMeal.Failed
                }
            } catch (e: Exception) {
                DaysLogResponse.EditExistingMeal.Failed
            }
        }

        is LogActions.DeleteMeal -> {
            try {
                val deletedAt = app.database.mealDao().deleteMeal(action.meal)
                if (deletedAt > 0) {
                    DaysLogResponse.DeletedMeal.Success(action.meal)
                } else {
                    DaysLogResponse.DeletedMeal.Failed
                }
            } catch (e: Exception) {
                DaysLogResponse.DeletedMeal.Failed
            }
        }

        is EditMealActions.SaveMeal -> {
            if (action.meal.items.isNotEmpty()) {
                try {
                    app.database.mealDao().insertMeal(action.meal)
                    DaysLogResponse.CratedMeal.Success(action.meal)
                } catch (e: Exception) {
                    DaysLogResponse.CratedMeal.Failed(action.meal.id)
                }
            } else {
                DaysLogResponse.CratedMeal.Failed(action.meal.id)
            }
        }

        else -> null
    }
}


sealed class DaysLogResponse : RepoResponse {
    sealed class AllMeals : DaysLogResponse() {
        data class Success(val meals: List<Meal>) : AllMeals()
        object Failed : AllMeals()
    }

    sealed class EditExistingMeal : DaysLogResponse() {
        data class Success(val meal: Meal) : EditExistingMeal()
        object Failed : EditExistingMeal()
    }

    sealed class CratedMeal(val mealId: String) : DaysLogResponse() {
        data class Success(val meal: Meal) : CratedMeal(meal.id)
        class Failed(mealId: String) : CratedMeal(mealId)
    }

    sealed class DeletedMeal : DaysLogResponse() {
        data class Success(val meal: Meal) : DeletedMeal()
        object Failed : DeletedMeal()
    }
}
