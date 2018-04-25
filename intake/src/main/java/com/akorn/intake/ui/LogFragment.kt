package com.akorn.intake.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.view.isGone
import androidx.view.isVisible
import com.akorn.akorn.subscribeTo
import com.akorn.akorn.viewModelProvidersFromActivity
import com.akorn.intake.R
import com.akorn.intake.logic.MealLogStateModel
import com.akorn.intake.logic.MealLogTree
import com.akorn.intake.ui.adapter.LogAdapter
import com.akorn.intake.ui.adapter.MealsAdapter
import kotlinx.android.synthetic.main.fragment_log.view.*
import kotlinx.android.synthetic.main.log_day_item.view.*


class LogFragment : Fragment() {
    private val mealLogStateModel by viewModelProvidersFromActivity(MealLogStateModel::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeTo(mealLogStateModel) {
            when (it) {
                is MealLogTree.Loading -> loadingState(view)
                is MealLogTree.Done.Ready -> readyState(view, it)
                is MealLogTree.Done.Blank -> blankState(view)
                is MealLogTree.Done.MealLogStateError -> errorState(view)
            }
        }
    }
}

fun loadingState(view: View) = with(view) {
    if (mealLog.adapter == null) {
        mealLog.isGone = true
        mealLogBlank.isGone = true
        mealLogLoading.isVisible = true
    }
}

fun errorState(view: View) = with(view) {
    mealLogLoading.isGone = true
}

fun blankState(view: View) = with(view) {
    mealLogLoading.isGone = true
    mealLogBlank.isVisible = true
    mealLog.isGone = true
}

fun readyState(view: View, state: MealLogTree.Done.Ready) = with(view) {
    mealLogLoading.isGone = true
    mealLogBlank.isGone = true
    mealLog.isVisible = true

    when (state) {
        is MealLogTree.Done.Ready.NewLog -> {
            if (mealLog.adapter == null) mealLog.adapter = LogAdapter()
            (mealLog.adapter as LogAdapter).log = state.days
        }

        is MealLogTree.Done.Ready.MealDeleted -> {
            val adapter = mealLog.findViewHolderForAdapterPosition(state.dayPosition)?.itemView?.mealList?.adapter
            if (adapter is MealsAdapter) adapter.meals = state.newMeals
        }
    }
}


