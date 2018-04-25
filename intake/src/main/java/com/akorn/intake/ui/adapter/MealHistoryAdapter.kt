package com.akorn.intake.ui.adapter

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akorn.intake.R
import com.akorn.intake.data.Meal
import com.akorn.intake.data.toDateOnlyString
import kotlinx.android.synthetic.main.meal_history_item.view.*

class MealHistoryAdapter(meals: List<Meal> = emptyList(), private var onMealClicked: (meal: Meal) -> Unit = {}) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var meals: List<Meal> = meals
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedAt: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.meal_history_item, parent, false)) {}

    override fun getItemCount(): Int = meals.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = with(holder.itemView) {
        val meal = meals[position]


        if (selectedAt == position) {
            mealCard.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            mealCard.setBackgroundColor(ContextCompat.getColor(context, R.color.cardview_light_background))
        }

        setOnClickListener { onMealClicked(meal) }

        @SuppressLint("SetTextI18n")
        mealType.text = meal.date.toDateOnlyString()
    }
}