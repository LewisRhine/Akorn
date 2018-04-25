package com.akorn.akorn

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

interface StateTree
interface ErrorStateTree : StateTree

typealias StateReducer<S> = (action: AppAction, currentStateTree: S) -> S?

abstract class StateModel<T : StateTree>(
        private val actionEngine: ActionEngine = AppActionEngine,
        vararg repos: DataRepo<*> = emptyArray(),
        initState: T,
        private val stateReducer: StateReducer<T>) : ViewModel() {


    val key: String get() = this::class.java.simpleName

    private var currentState: T = initState
    private var previousState: T? = null

    private val mediatorState: StateLiveDate<T> = StateLiveDate<T>().apply { async(UI) { value = currentState } }

    val state: LiveData<T> get() = mediatorState

    init {
        async {
            mediatorState.addSource(actionEngine.actions) {
                it?.let {
                    if (it is MockAction) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            setState(it.mockState as T)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        stateReducer(it, currentState)?.let { setState(it) }
                    }
                }
            }
            mediatorState.doOnActive = { onActivated() }
            if (actionEngine !== MockActionEngine) {
                repos.forEach { mediatorState.addSource(it.response) { it?.let { stateReducer(it, currentState)?.let { setState(it) } } } }
            }
        }
    }

    private fun setState(stateTree: T) {
        async(UI) {
            if (stateTree is ErrorStateTree) {
                mediatorState.value = stateTree
                delay(1)
                mediatorState.value = currentState
            } else {
                if (currentState != stateTree) {
                    currentState = stateTree
                    mediatorState.value = currentState
                }
            }
        }
    }

    open fun onActivated() {}
}

class StateLiveDate<T> : MediatorLiveData<T>() {
    var doOnActive: () -> Unit = {}
    override fun onActive() {
        super.onActive()
        doOnActive()
    }
}