
package com.akorn.akorn

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun <T : ViewModel> Fragment.viewModelProviders(clazz: Class<T>, factoryInit: () -> ViewModelProvider.Factory? = { null }): ReadOnlyProperty<Any?, T> = object : ReadOnlyProperty<Any?, T> {
    private var viewModel: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        try {
            viewModel = Mockery.findAMockeryOf(clazz.simpleName) as T
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (viewModel == null) {
            val factory = factoryInit()
            viewModel = if (factory != null) {
                ViewModelProviders.of(this@viewModelProviders, factory).get(clazz)
            } else {
                ViewModelProviders.of(this@viewModelProviders).get(clazz)
            }
        }

        return viewModel as T
    }
}

fun <T : ViewModel> Fragment.viewModelProvidersFromActivity(clazz: Class<T>, factoryInit: () -> ViewModelProvider.Factory? = { null }): ReadOnlyProperty<Any?, T> = object : ReadOnlyProperty<Any?, T> {
    private var viewModel: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        try {
            viewModel = Mockery.findAMockeryOf(clazz.simpleName) as T
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (viewModel == null) {
            val theActivity = this@viewModelProvidersFromActivity.activity
            viewModel = if (theActivity != null) {
                val factory = factoryInit()
                if (factory != null) {
                    ViewModelProviders.of(theActivity, factory).get(clazz)
                } else {
                    ViewModelProviders.of(theActivity).get(clazz)
                }
            } else {
                ViewModelProviders.of(this@viewModelProvidersFromActivity).get(clazz)
            }
        }

        return viewModel as T
    }
}

fun <T : ViewModel> AppCompatActivity.viewModelProviders(clazz: Class<T>, factoryInit: () -> ViewModelProvider.Factory? = { null }): ReadOnlyProperty<Any?, T> = object : ReadOnlyProperty<Any?, T> {
    private var viewModel: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        try {
            viewModel = Mockery.findAMockeryOf(clazz.simpleName) as T
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (viewModel == null) {
            val factory = factoryInit()
            viewModel = if (factory != null) {
                ViewModelProviders.of(this@viewModelProviders, factory).get(clazz)
            } else {
                ViewModelProviders.of(this@viewModelProviders).get(clazz)
            }
        }

        return viewModel as T
    }
}
