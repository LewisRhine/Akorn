package com.akorn.akorn

object Mockery {
    private val mocks = mutableMapOf<String, StateModel<*>>()

    fun makeAMockeryOf(mockStateModel: StateModel<*>) {
        mocks[mockStateModel.key] = mockStateModel
    }

    fun findAMockeryOf(key: String): StateModel<*>? = mocks[key]
}