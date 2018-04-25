package com.akorn.intake

import com.akorn.akorn.AppAction
import com.akorn.akorn.AppActionEngine
import com.akorn.intake.data.Meal


fun AppActionEngine.getLog() = doAction(LogActions.GetLog)
fun AppActionEngine.editMeal(meal: Meal) = doAction(LogActions.EditMeal(meal))
fun AppActionEngine.deleteMeal(meal: Meal) = doAction(LogActions.DeleteMeal(meal))

sealed class LogActions : AppAction {
    object GetLog : LogActions()
    object NewMeal : LogActions()
    data class EditMeal(val meal: Meal) : LogActions()
    data class DeleteMeal(val meal: Meal) : LogActions()
}

fun AppActionEngine.saveMealItem(itemString: String) = doAction(EditMealActions.AddMealItem(itemString))
fun AppActionEngine.changeMealType(type: String) = doAction(EditMealActions.ChangeMealType(type))
fun AppActionEngine.changeMealSize(size: String) = doAction(EditMealActions.ChangeMealSize(size))
fun AppActionEngine.deleteMealItem(item: String) = doAction(EditMealActions.DeleteMealItem(item))
fun AppActionEngine.updateMealItem(item: String) = doAction(EditMealActions.UpdateMealItem(item))
fun AppActionEngine.changeDate(year: Int, month: Int, dayOfMonth: Int) = doAction(EditMealActions.ChangeDate(year, month, dayOfMonth))
fun AppActionEngine.changeTime(hour: Int, minute: Int) = doAction(EditMealActions.ChangeTime(hour, minute))

sealed class EditMealActions : AppAction {
    data class EditExistingMeal(val mealId: String) : EditMealActions()
    object EditNewMeal : EditMealActions()
    data class AddMealItem(val item: String) : EditMealActions()
    data class ChangeMealType(val type: String) : EditMealActions()
    data class ChangeMealSize(val size: String) : EditMealActions()
    data class DeleteMealItem(val item: String) : EditMealActions()
    data class UpdateMealItem(val item: String) : EditMealActions()
    data class SaveMeal(val meal: Meal) : EditMealActions()
    data class ChangeDate(val year: Int, val month: Int, val dayOfMonth: Int) : EditMealActions()
    data class ChangeTime(val hour: Int, val minute: Int) : EditMealActions()
}
