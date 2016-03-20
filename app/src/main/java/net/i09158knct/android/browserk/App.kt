package net.i09158knct.android.browserk

import android.app.Application
import net.i09158knct.android.browserk.browser.Browser
import net.i09158knct.android.browserk.services.Toaster

class App : Application() {
    companion object {
        lateinit var instance: App
        lateinit var toaster: Toaster
        lateinit var browser: Browser
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        toaster = Toaster(this)
    }
}