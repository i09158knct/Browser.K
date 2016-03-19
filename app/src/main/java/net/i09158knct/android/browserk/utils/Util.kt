package net.i09158knct.android.browserk.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.BuildConfig
import net.i09158knct.android.browserk.R
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLEncoder

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

    fun shareUrl(a: Activity, url: String) {
        val i = Intent()
        i.setAction(Intent.ACTION_SEND)
        i.setType("text/plain")
        i.putExtra(Intent.EXTRA_TEXT, url)
        a.startActivity(Intent.createChooser(i, a.getString(R.string.menuShare)))
    }

    fun openInOtherBrowser(a: Activity, url: String) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        a.startActivity(Intent.createChooser(i, a.getString(R.string.menuOpenInOtherBrowser)))
    }

    fun generateSearchUrl(searchWord: String): String {
        try {
            return "https://www.google.co.jp/search?q=" + URLEncoder.encode(searchWord, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return "https://www.google.co.jp/search?q="
    }
}