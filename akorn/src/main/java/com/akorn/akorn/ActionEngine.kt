package com.akorn.akorn

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

object MockActionEngine : ActionEngine()

object AppActionEngine : ActionEngine()

abstract class ActionEngine {
    private val mutableAction: MutableLiveData<AppAction> = MutableLiveData()
    val actions: LiveData<AppAction> get() = mutableAction

    fun doAction(appAction: () -> AppAction) = doAction(appAction())

    fun doAction(appAction: AppAction) {
        async(UI) {
            mutableAction.value = appAction
            delay(1)
            mutableAction.value = null
        }
    }
}

interface AppAction
data class MockAction(val mockState: StateTree) : AppAction