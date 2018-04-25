package com.akorn.akorn

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.view.View

fun <T : StateTree> LifecycleOwner.subscribeTo(stateModel: StateModel<T>, onChange: (state: T?) -> Unit) = stateModel.state.observe(this, Observer {
    onChange(it)
})

fun LifecycleOwner.onAction(onChange: (action: AppAction?) -> Unit) = AppActionEngine.actions.observe(this, Observer { onChange(it) })

inline fun <reified T : AppAction> LifecycleOwner.onActionOf(crossinline onChange: (action: T) -> Unit) =
        AppActionEngine.actions.observe(this, Observer { if (it is T) onChange(it) })

infix fun View.clickToAction(action: AppAction) = setOnClickListener { AppActionEngine.doAction(action) }
