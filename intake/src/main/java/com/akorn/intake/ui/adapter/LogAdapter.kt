package com.akorn.intake.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.view.isGone
import androidx.view.isVisible
import com.akorn.akorn.AppActionEngine
import com.akorn.intake.R
import com.akorn.intake.data.Day
import com.akorn.intake.data.Meal
import com.akorn.intake.data.toTimeOnlyString
import com.akorn.intake.deleteMeal
import com.akorn.intake.editMeal
import kotlinx.android.synthetic.main.day_meal_item.view.*
import kotlinx.android.synthetic.main.log_day_item.view.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class LogAdapter(log: List<Day> = emptyList()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var recyclerView: RecyclerView? = null

    var log: List<Day> by diffUtil(log) { oldItem, newItem -> newItem.date == oldItem.date && newItem.meals == oldItem.meals }

    var selectedAt: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val expandedDays = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.log_day_item, parent, false)) {}

    override fun getItemCount() = log.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = with(holder.itemView) {
        val day = log[position]
        val expanded = expandedDays.getOrPut(position) { false }

        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, day.daySize)
        date.text = day.date

        if (expanded) {
            mealList.isVisible = true
        } else {
            mealList.isGone = true
        }

        if (selectedAt == position) {
            dayContentMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            dayContentMain.setBackgroundColor(ContextCompat.getColor(context, R.color.cardview_light_background))
        }

        setOnClickListener {
            expandedDays[position] = mealList.isGone
            notifyItemChanged(position)
            if (expandedDays[position] == true && position == itemCount) {
                recyclerView?.smoothScrollToPosition(itemCount)
            }
        }

        if (mealList.adapter == null) mealList.adapter = MealsAdapter()

        with(mealList.adapter as MealsAdapter) {
            meals = day.meals
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
}

class MealsAdapter(meals: List<Meal> = emptyList())
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var meals: List<Meal>  by diffUtil(meals) { oldItem, newItem -> oldItem.id == newItem.id }

    private var editMode = false

    private fun flipEditMode() {
        editMode = !editMode
        meals.forEachIndexed { index, _ -> notifyItemChanged(index) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.day_meal_item, parent, false)) {}

    override fun getItemCount(): Int = meals.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = with(holder.itemView) {
        val meal = meals[position]

        mealsItems.adapter = MealItemsAdapter(meal.items, meal.mealSize)

        setOnLongClickListener { flipEditMode();true }

        mealTime.text = meal.date.toTimeOnlyString()
        mealTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, meal.mealSize.toFloat())

        mealType.text = meal.mealType.name
        mealType.setTextSize(TypedValue.COMPLEX_UNIT_SP, meal.mealSize.toFloat())

        if (editMode) {
            editMealButton.isVisible = true
            deleteMealButton.isVisible = true

            editMealButton.setOnClickListener { AppActionEngine.editMeal(meal) }

            deleteMealButton.setOnClickListener {
                AlertDialog.Builder(context)
                        .setTitle(R.string.delete_meal_title)
                        .setMessage(R.string.delete_meal_message)
                        .setPositiveButton(R.string.delete_meal_yes) { _, _ -> AppActionEngine.deleteMeal(meal) }
                        .setNegativeButton(R.string.delete_meal_no, null)
                        .show()
            }
        } else {
            editMealButton.isGone = true
            deleteMealButton.isGone = true
        }
    }
}


fun <T> diffUtil(initList: List<T> = emptyList(), areItemsTheSame: (oldItem: T, newItem: T) -> Boolean) =
        object : ReadWriteProperty<RecyclerView.Adapter<*>, List<T>> {
            private var list = initList

            override fun getValue(thisRef: RecyclerView.Adapter<*>, property: KProperty<*>): List<T> = list

            override fun setValue(thisRef: RecyclerView.Adapter<*>, property: KProperty<*>, value: List<T>) {
                val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getNewListSize() = value.size
                    override fun getOldListSize() = list.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                            areItemsTheSame(list[oldItemPosition], value[newItemPosition])

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                            list[oldItemPosition] == value[newItemPosition]
                })

                list = value
                result.dispatchUpdatesTo(thisRef)
            }
        }
