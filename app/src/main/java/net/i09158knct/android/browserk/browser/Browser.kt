package net.i09158knct.android.browserk.browser

import android.webkit.WebView
import kotlinx.android.synthetic.main.activity_main.*
import net.i09158knct.android.browserk.MainActivity
import net.i09158knct.android.browserk.utils.Util

class Browser(val main: MainActivity) {
    val viewManager = BrowserViewManager(main.grpWebViewContainer)
    val tabs: MutableList<Tab> = mutableListOf()
    val chromeClient: CustomWebChromeClient = CustomWebChromeClient(main);
    val viewClient: CustomWebViewClient = CustomWebViewClient(main);

    val mainvm = MainViewModel()

    inner class MainViewModel {
        fun back(): Unit {
            viewManager.currentTab?.back()
        }

        fun forward() {
            viewManager.currentTab?.forward()
        }

        fun reload() {
            viewManager.currentTab?.reload()
        }

        fun share() {
            Util.shareUrl(main, viewManager.currentTab!!.wb.url)
        }

        fun bookmark() {
        }

        fun tab() {
        }

        private val validSchemaList = listOf<String>(
                "http:",
                "https:",
                "javascript:",
                "about:",
                "data:",
                "file:",
                "content:")

        fun loadUrl(query: String) {
            val valid = validSchemaList.any { query.startsWith(it) }
            val url = if (valid) query else Util.generateSearchUrl(query)
            viewManager.currentTab?.loadUrl(url)
        }
    }

    fun addNewTab(url: String) {
        val webview = WebView(main)
        val tab = Tab(webview, chromeClient, viewClient)
        tabs.add(tab)
        viewManager.showTab(tab)
        tab.wb.loadUrl(url)
    }
}