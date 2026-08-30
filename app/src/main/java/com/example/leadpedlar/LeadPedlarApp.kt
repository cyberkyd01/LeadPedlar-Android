package com.example.leadpedlar

import android.app.Application
import android.content.Context
import com.example.leadpedlar.data.preferences.AppPreferences
import com.example.leadpedlar.data.preferences.DialerPreferences

class LeadPedlarApp : Application() {

    lateinit var appPreferences: AppPreferences
        private set

    lateinit var dialerPreferences: DialerPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appPreferences = AppPreferences(applicationContext)
        dialerPreferences = DialerPreferences(applicationContext)
    }

    companion object {
        lateinit var instance: LeadPedlarApp
            private set

        fun getAppContext(): Context = instance.applicationContext
    }
}
