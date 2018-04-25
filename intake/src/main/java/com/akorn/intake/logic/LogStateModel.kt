package com.akorn.intake.logic

import com.akorn.akorn.*
import com.akorn.intake.data.*


class MealLogStateModel(actionEngine: ActionEngine = AppActionEngine) : StateModel<MealLogTree>(
        actionEngine = actionEngine,
        repos = * arrayOf(MealsRepo),
        initState = MealLogTree.Loading,
        stateReducer = mealLogReducer)

sealed class MealLogTree : StateTree {
    object Loading : MealLogTree()
    sealed class Done : MealLogTree() {
        object Blank : Done()
        sealed class Ready(val days: List<Day>) : Done() {
            class NewLog(days: List<Day>) : Ready(days)
            class MealDeleted(days: List<Day>, val newMeals: List<Meal>, val dayPosition: Int) : Ready(days)
        }

        sealed class MealLogStateError : ErrorStateTree, Done() {
            object CouldNotLoadMeals : MealLogStateError()
            object CouldNotDeleteMeal : MealLogStateError()
        }
    }
}

val mealLogReducer: StateReducer<MealLogTree> = { action, currentStateTree ->
    when (action) {
        is DaysLogResponse.AllMeals.Success -> {
            val days = mealListToDaysList(action.meals)
            if (days.isEmpty()) MealLogTree.Done.Blank else MealLogTree.Done.Ready.NewLog(days)
        }

        DaysLogResponse.AllMeals.Failed -> MealLogTree.Done.MealLogStateError.CouldNotLoadMeals

        DaysLogResponse.DeletedMeal.Failed -> MealLogTree.Done.MealLogStateError.CouldNotDeleteMeal

        is DaysLogResponse.CratedMeal.Success -> when (currentStateTree) {
            is MealLogTree.Done.Blank -> MealLogTree.Done.Ready.NewLog(mealListToDaysList(arrayListOf(action.meal)))
            is MealLogTree.Done.Ready -> MealLogTree.Done.Ready.NewLog(addMealToDays(action.meal, currentStateTree.days))
            else -> null
        }

        is DaysLogResponse.DeletedMeal.Success -> when (currentStateTree) {
            is MealLogTree.Done.Ready -> {
                val oldDays = currentStateTree.days
                val newDays = removeMealFromDays(action.meal, currentStateTree.days)

                if (newDays.isEmpty()) {
                    MealLogTree.Done.Blank
                } else {
                    val position = oldDays.indexOf(oldDays.find { it.meals.contains(action.meal) })
                    val newMeals = oldDays[position].meals.minus(action.meal)
                    MealLogTree.Done.Ready.MealDeleted(newDays, newMeals, position)
                }
            }
            else -> null
        }
        else -> null
    }
}

fun mealListToDaysList(meals: List<Meal>): List<Day> {
    val days = mutableListOf<Day>()
    val mealsMap = meals.sortedBy { it.date }.groupByTo(mutableMapOf()) { it.date.toDateOnlyString() }
    mealsMap.forEach {
        var daySize = 12f
        it.value.forEach {
            if (daySize < 40) {
                daySize += when (it.mealSize) {
                    MealSize.Light -> 1
                    MealSize.Normal -> 2
                    MealSize.Heavy -> 3
                }
            }
        }
        days.add(Day(date = it.key, meals = it.value.sortedBy { it.date }.reversed(), daySize = daySize))
    }
    return days
}

fun addMealToDays(meal: Meal, days: List<Day>): List<Day> {
    val meals = mutableListOf<Meal>()
    days.forEach { meals.addAll(it.meals) }
    meals.find { it.id == meal.id }?.let { meals.remove(it) }
    meals.add(meal)
    return mealListToDaysList(meals)
}

fun removeMealFromDays(meal: Meal, days: List<Day>): List<Day> {
    val meals = mutableListOf<Meal>()
    days.forEach { meals.addAll(it.meals) }
    meals.find { it.id == meal.id }?.let { meals.remove(it) }
    return mealListToDaysList(meals)
}