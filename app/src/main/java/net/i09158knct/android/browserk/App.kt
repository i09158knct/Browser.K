package net.i09158knct.android.browserk

import android.app.Application
import net.i09158knct.android.browserk.browser.Browser
import net.i09158knct.android.browserk.services.Toaster

class App : Application() {
    companion object {
        private var _i: App? = null
        var i: App
            get() = _i!!
            private set(value) {
                _i = value
                _s = Singletons(value)
            }
        private var _s: Singletons? = null
        var s: Singletons
            get() = _s!!
            set(value) {
                _s = value
            }

        class Singletons(val app: App) {
            val toaster = Toaster(app)
            lateinit var browser: Browser
        }
    }

    override fun onCreate() {
        super.onCreate()
        i = this
    }
}