package net.i09158knct.android.browserk.utils

import android.util.Log
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.BuildConfig

object Util {
    val tag: String
        get() {
            if (BuildConfig.DEBUG) {
                val trace = Thread.currentThread().stackTrace[3]
                val shortClassName = trace.className.split('.').last();
                return "${shortClassName}.${trace.methodName}"
            }
            return "*"
        }

    fun debug(tag: String, msg: String): Unit {
        Log.d(tag, msg)
        App.s.toaster.show("${tag} | ${msg}")
    }
}