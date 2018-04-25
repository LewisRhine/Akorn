package com.akorn.intake.ui


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.akorn.akorn.AppActionEngine
import com.akorn.akorn.subscribeTo
import com.akorn.akorn.viewModelProvidersFromActivity
import com.akorn.intake.*
import com.akorn.intake.data.MealSize
import com.akorn.intake.data.MealType
import com.akorn.intake.data.toDateOnlyString
import com.akorn.intake.data.toTimeOnlyString
import com.akorn.intake.logic.EditMealStateModel
import com.akorn.intake.logic.EditMealTree
import com.akorn.intake.ui.adapter.MealItemsAdapter
import kotlinx.android.synthetic.main.fragment_edit_meal.*
import kotlinx.android.synthetic.main.fragment_edit_meal.view.*
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

const val editMealFragmentTag = "EditMealFragmentTag"

class EditMealFragment : Fragment() {
    private val editMealStateModel by viewModelProvidersFromActivity(EditMealStateModel::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_edit_meal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemTitle.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                saveItem();true
            } else {
                false
            }
        }

        addItem.setOnClickListener { saveItem() }

        subscribeTo(editMealStateModel) { state ->
            when (state) {
                is EditMealTree.Ready -> editMealTreeReady(view, state)
                is EditMealTree.Error.NoItemsInMeal -> Snackbar.make(itemsList, R.string.error_no_times, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveItem() {
        AppActionEngine.saveMealItem("${itemTitle.text}")
        itemTitle.setText("")
    }

    fun editMealTreeReady(view: View, state: EditMealTree.Ready) = with(view) {
        sizeGroup.setOnCheckedChangeListener(null)
        sizeGroup.setOnCheckedChangeListener(null)

        renderMealSize(view, state.meal.mealSize)
        renderMealType(view, state.meal.mealType)
        renderItemList(view, state.meal.items)

        if (state.itemTitle.isNotBlank()) itemTitle.setText(state.itemTitle)

        dateText.text = state.meal.date.toDateOnlyString()
        val myCalendar = Calendar.getInstance()

        dateText.setOnClickListener {
            DatePickerDialog(context,
                    { _, year, month, dayOfMonth -> AppActionEngine.changeDate(year, month, dayOfMonth) },
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }

        timeText.setOnClickListener {
            TimePickerDialog(context,
                    { _, hour, minute -> AppActionEngine.changeTime(hour, minute) },
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    false)
                    .show()
        }

        timeText.text = state.meal.date.toTimeOnlyString()

        typeGroup.setOnCheckedChangeListener { _, id ->
            val radioButton = view.findViewById<AppCompatRadioButton>(id)
            AppActionEngine.changeMealType("${radioButton.text}")
        }

        sizeGroup.setOnCheckedChangeListener { _, id ->
            val radioButton = view.findViewById<AppCompatRadioButton>(id)
            AppActionEngine.changeMealSize("${radioButton.text}")
        }
    }

    fun renderMealSize(view: View, mealSize: MealSize) {
        when (mealSize) {
            MealSize.Light -> radioLight.isChecked = true
            MealSize.Normal -> radioNormal.isChecked = true
            MealSize.Heavy -> radioHeavy.isChecked = true
        }
    }

    fun renderMealType(view: View, mealType: MealType) = with(view) {
        when (mealType) {
            MealType.Snack -> radioSnack.isChecked = true
            MealType.Drink -> radioDrink.isChecked = true
            MealType.Breakfast -> radioBreakfast.isChecked = true
            MealType.Lunch -> radioLunch.isChecked = true
            MealType.Dinner -> radioDinner.isChecked = true
        }
    }

    fun renderItemList(view: View, items: List<String>) = with(view) {
        if (itemsList.adapter == null) itemsList.adapter = MealItemsAdapter(showClearButton = true).apply {
            onClick = { item -> AppActionEngine.updateMealItem(item) }
            onClearButtonClick = { item -> AppActionEngine.deleteMealItem(item) }
        }

        (itemsList.adapter as MealItemsAdapter).items = items.reversed()
    }

    fun <T> propRender(initValue: T, render: (T) -> Unit) = object : ReadWriteProperty<Any?, T> {
        var prop: T = initValue

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = prop
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (value != prop) {
                prop = value
                render(prop)
            }
        }
    }
}
