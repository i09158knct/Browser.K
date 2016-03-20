package net.i09158knct.android.browserk.browser

import android.app.Activity
import android.content.Context
import android.webkit.WebView
import net.i09158knct.android.browserk.activities.MainActivity
import net.i09158knct.android.browserk.utils.Util

class Browser(val context: MainActivity, var homeUrl: String = "https://www.google.co.jp") {
    val tabs = mutableListOf<Tab>()
    val foreground = ForegroundTabManager(addNewTab())
    var listener: IEventListener? = null

    private val validSchemaList = listOf(
            "http:",
            "https:",
            "javascript:",
            "about:",
            "data:",
            "file:",
            "content:")

    fun query(query: String) {
        val valid = validSchemaList.any { query.startsWith(it) }
        val url = if (valid) query else Util.generateSearchUrl(query)
        foreground.tab.loadUrl(url)
    }

    var isJsEnabled: Boolean = false
        get
        set(value) {
            field = value
            tabs.forEach { it.wb.settings.javaScriptEnabled = value }
        }

    var isImageEnabled: Boolean = false
        get
        set(value) {
            field = value
            tabs.forEach { it.wb.settings.loadsImagesAutomatically = value }
        }

    fun addNewTab(): Tab {
        val webview = WebView(context)
        val tab = Tab(webview)
        tabs.add(tab)
        listener?.onTabCountChanged(tabs.count())
        return tab
    }

    fun closeTab(tab: Tab) {
        tabs.remove(tab)
        tab.wb.destroy()
    }

    interface IEventListener {
        fun onTabCountChanged(count: Int)
    }
}