package net.i09158knct.android.browserk.browser

import android.webkit.WebView
import kotlinx.android.synthetic.main.activity_main.*
import net.i09158knct.android.browserk.activities.MainActivity
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

        fun stopLoading() {
            viewManager.currentTab?.stopLoading()
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

        fun canGoBack(): Boolean {
            return viewManager.currentTab?.wb?.canGoBack() ?: false
        }

        fun openInOtherBrowser() {
            Util.openInOtherBrowser(main, viewManager.currentTab!!.wb.url)
        }

        fun switchJs(enable: Boolean) {
            tabs.forEach { it.wb.settings.javaScriptEnabled = enable }
        }

        fun switchImage(enable: Boolean) {
            tabs.forEach { it.wb.settings.loadsImagesAutomatically = enable }
        }

        fun IsJsEnabled(): Boolean {
            return tabs[0].wb.settings.javaScriptEnabled
        }

        fun IsImageEnabled(): Boolean {
            return tabs[0].wb.settings.loadsImagesAutomatically
        }
    }

    fun addNewTab(url: String) : Tab {
        val webview = WebView(main)
        val tab = Tab(webview, chromeClient, viewClient)
        tabs.add(tab)
        tab.wb.loadUrl(url) // FIXME 正しい読み込みタイミング
        return tab
    }

    fun changeCurrentTab(tab: Tab) {
        viewManager.showTab(tab)
    }

    fun closeTab(tab: Tab) {
        tabs.remove(tab)
        tab.wb.destroy()
    }
}