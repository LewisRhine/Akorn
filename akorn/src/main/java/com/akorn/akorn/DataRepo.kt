package com.akorn.akorn

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

interface RepoResponse : AppAction

abstract class DataRepo<T : RepoResponse>(actionEngine: ActionEngine = AppActionEngine) {
    private val mediatorResponse = MediatorLiveData<T>()
    val response: LiveData<T> get() = mediatorResponse

    init {
        mediatorResponse.addSource(actionEngine.actions) {
            async {
                it?.let {
                    onAction(it)?.let { send(it) }
                }
            }
        }
    }

    private fun send(response: T) {
        async(UI) {
            mediatorResponse.value = response
        }
    }

    abstract fun onAction(action: AppAction): T?
}