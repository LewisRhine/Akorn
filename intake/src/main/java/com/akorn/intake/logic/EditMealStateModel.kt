package com.akorn.intake.logic

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.akorn.akorn.*
import com.akorn.intake.EditMealActions
import com.akorn.intake.data.*
import java.util.*


class EditMealStateModel(editExistingMeal: Boolean = false) : StateModel<EditMealTree>(
        repos = *arrayOf(MealsRepo),
        initState = if (editExistingMeal) EditMealTree.FetchingMealToEdit else EditMealTree.Ready(Meal(mealSize = MealSize.Normal, mealType = MealType.Snack)),
        stateReducer = editMealReducer)


fun editMealStateModelFactory(editExistingMeal: Boolean = false) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditMealStateModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditMealStateModel(editExistingMeal) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class EditMealTree : StateTree {
    object FetchingMealToEdit : EditMealTree()

    data class Ready(
            val meal: Meal,
            val itemTitle: String = "") : EditMealTree()

    data class MealSaved(val meal: Meal) : EditMealTree()

    sealed class Error : ErrorStateTree, EditMealTree() {
        object MealCouldNotBeFound : Error()
        object NoItemsInMeal : Error()
        object CouldNotSaveMeal : Error()
    }
}

val editMealReducer: StateReducer<EditMealTree> = { action, currentStateTree ->
    when (currentStateTree) {
        is EditMealTree.Ready -> editMealReadyReducer(action, currentStateTree)

        else -> when (action) {
            is DaysLogResponse.EditExistingMeal.Success -> EditMealTree.Ready(action.meal)
            is DaysLogResponse.EditExistingMeal.Failed -> EditMealTree.Error.MealCouldNotBeFound
            is DaysLogResponse.CratedMeal.Failed -> EditMealTree.Error.CouldNotSaveMeal
            is EditMealActions.EditNewMeal -> EditMealTree.Ready(Meal(mealSize = MealSize.Normal, mealType = MealType.Snack))

            else -> null
        }
    }
}

fun editMealReadyReducer(action: AppAction, currentStateTree: EditMealTree.Ready): EditMealTree? = when (action) {
    is DaysLogResponse.CratedMeal -> when (action.mealId == currentStateTree.meal.id) {
        true -> when (action) {
            is DaysLogResponse.CratedMeal.Success -> EditMealTree.MealSaved(action.meal)

            is DaysLogResponse.CratedMeal.Failed -> if (currentStateTree.meal.items.isEmpty()) {
                EditMealTree.Error.NoItemsInMeal
            } else {
                EditMealTree.Error.CouldNotSaveMeal
            }
        }
        else -> null
    }

    is EditMealActions.AddMealItem -> if (action.item.isNotBlank()) {
        val meal = currentStateTree.meal.copy(items = currentStateTree.meal.items.plus(action.item))
        currentStateTree.copy(meal = meal)
    } else {
        null
    }

    is EditMealActions.DeleteMealItem -> {
        val meal = currentStateTree.meal.copy(items = currentStateTree.meal.items.minus(action.item))
        currentStateTree.copy(meal = meal)
    }


    is EditMealActions.UpdateMealItem -> {
        val meal = currentStateTree.meal.copy(items = currentStateTree.meal.items.minus(action.item))
        currentStateTree.copy(meal = meal, itemTitle = action.item)
    }

    is EditMealActions.ChangeMealType -> {
        val mealType = MealTypeConverter().toMealType(action.type)
        val meal = currentStateTree.meal.copy(mealType = mealType)
        currentStateTree.copy(meal = meal)
    }

    is EditMealActions.ChangeMealSize -> {
        val meal = currentStateTree.meal.copy(mealSize = MealSizeConverter().toMealSize(action.size))
        currentStateTree.copy(meal = meal)
    }

    is EditMealActions.ChangeDate -> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentStateTree.meal.date.time
            set(action.year, action.month, action.dayOfMonth)
        }
        currentStateTree.copy(meal = currentStateTree.meal.copy(date = cal.time))
    }

    is EditMealActions.ChangeTime -> {
        val cal = Calendar.getInstance().apply {
            time = currentStateTree.meal.date
            set(Calendar.HOUR_OF_DAY, action.hour)
            set(Calendar.MINUTE, action.minute)
        }
        currentStateTree.copy(meal = currentStateTree.meal.copy(date = cal.time))
    }

    else -> null
}
