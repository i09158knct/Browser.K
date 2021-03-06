package net.i09158knct.android.browserk.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        App.toaster.show("${tag} | ${msg}")
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

    fun generateSearchUrl(searchWord: String, searchUrl: String): String {
        try {
            return searchUrl + URLEncoder.encode(searchWord, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return "https://www.google.com/search?q="
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager;
        clipboard.primaryClip = ClipData.newPlainText(context.getString(R.string.app_name), text)
    }
}