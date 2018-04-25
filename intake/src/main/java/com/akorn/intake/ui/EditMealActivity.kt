package com.akorn.intake.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.view.isGone
import androidx.view.isVisible
import com.akorn.akorn.AppActionEngine
import com.akorn.akorn.subscribeTo
import com.akorn.akorn.viewModelProviders
import com.akorn.intake.EditMealActions
import com.akorn.intake.R
import com.akorn.intake.logic.EditMealStateModel
import com.akorn.intake.logic.EditMealTree
import com.akorn.intake.logic.editMealStateModelFactory
import kotlinx.android.synthetic.main.activity_edit_meal.*

const val mealIdToEditTag = "mealIdToEditTag"

fun startEditMealActivity(context: Context, mealIdToEdit: String? = null) {
    val intent = Intent(context, EditMealActivity::class.java)
    intent.putExtra(mealIdToEditTag, mealIdToEdit)
    context.startActivity(intent)
}

class EditMealActivity : AppCompatActivity() {
    private val mealIdToEdit by lazy { intent.getStringExtra(mealIdToEditTag) }

    private val editMealStateModel by viewModelProviders(EditMealStateModel::class.java) {
        editMealStateModelFactory(mealIdToEdit != null)
    }

    private var saveMealClick: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_meal)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp)
        }

        val fragment = supportFragmentManager.findFragmentByTag(editMealFragmentTag)
                ?: EditMealFragment()

        supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()

        subscribeTo(editMealStateModel) { state ->
            when (state) {
                EditMealTree.FetchingMealToEdit -> {
                    loading.isVisible = true
                }

                is EditMealTree.Ready -> {
                    loading.isGone = true

                    saveMealClick = { AppActionEngine.doAction(EditMealActions.SaveMeal(state.meal)) }
                }

                is EditMealTree.MealSaved -> finish()

                is EditMealTree.Error.CouldNotSaveMeal -> {
                    Snackbar.make(contentMain, R.string.error_saving_meal, Snackbar.LENGTH_SHORT).show()
                }

                is EditMealTree.Error.MealCouldNotBeFound -> AlertDialog.Builder(this)
                        .setMessage(R.string.error_getting_meal_message)
                        .setPositiveButton(R.string.error_getting_meal_ok) { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
            }
        }

        mealIdToEdit?.let { AppActionEngine.doAction(EditMealActions.EditExistingMeal(it)) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_meal_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveMenuButton -> {
                saveMealClick()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
