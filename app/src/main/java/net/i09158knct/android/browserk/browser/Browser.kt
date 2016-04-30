package net.i09158knct.android.browserk.browser

import android.app.Activity
import android.content.Context
import android.webkit.WebView
import net.i09158knct.android.browserk.activities.MainActivity
import net.i09158knct.android.browserk.utils.Util

class Browser(val context: MainActivity,
              var homeUrl: String = "https://www.google.com",
              var searchUrl: String = "https://www.google.com/search?q=") {
    var isJsEnabled: Boolean = false
        get
        set(value) {
            field = value
            tabs.forEach { it.wb.settings.javaScriptEnabled = value }
        }

    var isImageEnabled: Boolean = true
        get
        set(value) {
            field = value
            tabs.forEach { it.wb.settings.loadsImagesAutomatically = value }
        }

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
        val url = if (valid) query else Util.generateSearchUrl(query, searchUrl)
        foreground.tab.loadUrl(url)
    }

    fun addNewTab(): Tab {
        val webview = WebView(context)
        val tab = Tab(webview, isJsEnabled, isImageEnabled)
        tab.wb.setWebViewClient(ForegroundTabManager.BackgroundViewClient)
        tab.wb.setWebChromeClient(ForegroundTabManager.BackGroundChromeClient)
        tabs.add(tab)
        listener?.onTabCountChanged(tabs.count())
        return tab
    }

    fun closeTab(tab: Tab) {
        // タブをtabsから削除する。
        val index = tabs.indexOf(tab)
        tabs.remove(tab)
        // TODO タブ復元機能

        // 閉じたタブがforegroundだった場合は別のタブをforegroundにする。
        // ただし、タブが全部閉じられた場合は何もしない。
        if (foreground.tab.equals(tab) && !tabs.isEmpty()) {
            foreground.changeTab(tabs[Math.min(index, tabs.count() - 1)])
        }

       tab.wb.destroy()
       listener?.onTabCountChanged(tabs.count())
    }

    interface IEventListener {
        fun onTabCountChanged(count: Int)
    }
}