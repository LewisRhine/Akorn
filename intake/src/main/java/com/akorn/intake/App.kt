package com.akorn.intake

import android.app.Application
import android.arch.persistence.room.Room
import com.akorn.intake.data.AppDatabase


class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    val database by lazy { Room.databaseBuilder(applicationContext, AppDatabase::class.java, "intake-db").build() }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

val app: App get() = App.instance