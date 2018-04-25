package com.akorn.intake.ui.adapter

import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.view.isGone
import com.akorn.intake.R
import com.akorn.intake.data.MealSize
import kotlinx.android.synthetic.main.meal_item.view.*

class MealItemsAdapter(items: List<String> = emptyList(),
                       var mealSize: MealSize = MealSize.Normal,
                       private val showClearButton: Boolean = false,
                       var onClick: (item: String) -> Unit = { _ -> }) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onClearButtonClick: (item: String) -> Unit = { _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)) {}

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder.itemView) {
            val item = items[position]
            mealItem.text = item
            mealItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, (mealSize.toFloat() - 2))
            setOnClickListener { onClick(items[position]) }

            clearButton.isGone = !showClearButton
            clearButton.setOnClickListener { onClearButtonClick(item) }
        }
    }
}