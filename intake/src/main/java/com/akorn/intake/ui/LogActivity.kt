package com.akorn.intake.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.akorn.akorn.*
import com.akorn.intake.EditMealActions
import com.akorn.intake.LogActions
import com.akorn.intake.R
import com.akorn.intake.getLog
import com.akorn.intake.logic.MealLogStateModel
import com.akorn.intake.logic.MealLogTree
import kotlinx.android.synthetic.main.activity_main.*


class LogActivity : AppCompatActivity() {

    private val mealLogStateModel by viewModelProviders(MealLogStateModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        subscribeTo(mealLogStateModel) {
            when (it) {
                is MealLogTree.Loading -> newMealFab.hide()
                is MealLogTree.Done.Ready, is MealLogTree.Done.Blank -> newMealFab.show()
                is MealLogTree.Done.MealLogStateError -> when (it) {
                    MealLogTree.Done.MealLogStateError.CouldNotLoadMeals -> Snackbar.make(newMealFab, R.string.error_getting_meals, Snackbar.LENGTH_INDEFINITE).setAction(R.string.error_getting_meals_action) { AppActionEngine.getLog() }.show()
                    MealLogTree.Done.MealLogStateError.CouldNotDeleteMeal -> Snackbar.make(newMealFab, R.string.error_deleting_meal, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        onAction {
            when (it) {
                is LogActions.EditMeal -> startEditMealActivity(this, it.meal.id)
                LogActions.NewMeal -> startEditMealActivity(this)
            }
        }

        newMealFab clickToAction LogActions.NewMeal

        AppActionEngine.doAction(LogActions.GetLog)
    }
}
