package net.i09158knct.android.browserk.browser

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import net.i09158knct.android.browserk.App
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
    val foreground: ForegroundTabManager
    var listener: IEventListener? = null

    init {
        restoreState()
        foreground = ForegroundTabManager(addNewTab())
    }


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
        if (foreground.tab == tab && !tabs.isEmpty()) {
            foreground.changeTab(tabs[Math.min(index, tabs.count() - 1)])
        }

        tab.wb.destroy()
        listener?.onTabCountChanged(tabs.count())
    }


    fun saveState() {
        // 現在のタブ一覧を保存する。
        val indexAndUrls = tabs.mapIndexed { i, tab -> "$i,${tab.wb.url}" }
        val data = indexAndUrls.toSet()
        val pref = context.getSharedPreferences("browser", Context.MODE_PRIVATE)
        pref.edit()
                .putStringSet("tabs", data)
                .commit()
        if (indexAndUrls.size != 0) Log.d(Util.tag, "saved\n" + indexAndUrls.reduce { s, acc -> "$s\n$acc" })
    }

    fun restoreState() {
        // タブ一覧を復元する。
        val pref = context.getSharedPreferences("browser", Context.MODE_PRIVATE)
        val data = pref.getStringSet("tabs", setOf())
        val urls = data
                .map { it.split(",", limit = 2) }
                .sortedBy { it[0].toInt() }
                .map { it[1] }
        if (urls.size != 0) Log.d(Util.tag, "restored\n" + urls.reduce { s, acc -> "$s\n$acc" })
        urls.forEach {
            addNewTab().apply { loadUrl(it) }
        }
    }

    interface IEventListener {
        fun onTabCountChanged(count: Int)
    }
}