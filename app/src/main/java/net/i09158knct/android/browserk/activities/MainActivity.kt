package net.i09158knct.android.browserk.activities

import android.app.Activity
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_main.*
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.R
import net.i09158knct.android.browserk.browser.Browser
import net.i09158knct.android.browserk.browser.ForegroundTabManager
import net.i09158knct.android.browserk.browser.Tab
import net.i09158knct.android.browserk.utils.Util

class MainActivity : Activity()
        , Browser.IEventListener
        , ForegroundTabManager.IEventListener {

    companion object {
        const private val REQUEST_SELECT_TAB: Int = 0x001
    }

    private lateinit var browser: Browser
    private lateinit var topwrapper: TopWrapper
    private var popup: PopupMenu? = null
    private var selectedWebNode: WebView.HitTestResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.wtf(Util.tag, "${intent?.dataString}")
        setContentView(R.layout.activity_main)

        topwrapper = TopWrapper()
        windowManager.addView(topwrapper, topwrapper.windowParams)

        // ブラウザを初期化する。
        App.browser = Browser(this)
        browser = App.browser
        browser.listener = this
        browser.foreground.listener = this

        // デフォルトのURLを読み込む。
        val initialUrl = getIntent()?.dataString ?: "https://www.google.com"
        browser.foreground.tab.loadUrl(initialUrl)
        btnTab.text = browser.tabs.count().toString()

        // WebViewをレイアウトにセットする。
        grpWebViewContainer.addView(browser.foreground.tab.wb,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        browser.foreground.tab.wb.requestFocus()
        registerForContextMenu(browser.foreground.tab.wb)

        // ヘッダーのイベントリスナーをセットする。
        btnTitle.setOnClickListener { btnTitle.maxLines = if (btnTitle.maxLines == 1) 10 else 1 }
        btnClearUrl.setOnClickListener {
            inputUrl.text.clear()

            inputUrl.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputUrl, InputMethodManager.SHOW_FORCED)
        }
        btnPasteUrl.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (!(clipboard.hasPrimaryClip())) {
                App.toaster.show("!(clipboard.hasPrimaryClip())")
                return@setOnClickListener
            }

            val desc = clipboard.primaryClipDescription
            if (!desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    && !desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                App.toaster.show("since the clipboard has data but it is not plain text")
                return@setOnClickListener
            }

            val item = clipboard.primaryClip.getItemAt(0)
            val start = inputUrl.selectionStart
            val end = inputUrl.selectionEnd
            inputUrl.text.replace(Math.min(start, end), Math.max(start, end), item.text)
        }
        btnEnterUrl.setOnClickListener {
            val text = inputUrl.text.toString()
            if (text.isEmpty()) return@setOnClickListener
            browser.query(text)
        }
        inputUrl.setOnFocusChangeListener { view, focused ->
            if (focused) grpEditPanel.visibility = View.VISIBLE
            else grpEditPanel.visibility = View.GONE
        }
        inputUrl.setOnKeyListener { view: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                browser.query(inputUrl.text.toString())
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }

        // フッターのイベントリスナーをセットする。
        btnBack.setOnClickListener { browser.foreground.tab.back() }
        btnForward.setOnClickListener { browser.foreground.tab.forward() }
        btnReload.setOnClickListener { browser.foreground.tab.reload() }
        btnStop.setOnClickListener { browser.foreground.tab.stopLoading() }
        btnShare.setOnClickListener { browser.foreground.tab.shareUrl(this) }
        btnBookmark.setOnClickListener { browser.foreground.tab.bookmark() }
        btnTab.setOnClickListener {
            val intent = Intent(applicationContext, TabListActivity::class.java)
            startActivityForResult(intent, REQUEST_SELECT_TAB)
        }
        btnMenu.setOnClickListener {
            if (toolbar.visibility == View.VISIBLE) {
                toolbar.visibility = View.INVISIBLE
            } else {
                grpEditPanel.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE
                topwrapper.visibility = View.VISIBLE
                toolbar.measure(0, 0)
                topwrapper.height = toolbar.measuredHeight
                topwrapper.touhed = false
                popup = PopupMenu(this, btnMenu).apply {
                    menuInflater.inflate(R.menu.main_tool, menu)
                    menu.findItem(R.id.menuJsEnable).isVisible = !browser.isJsEnabled
                    menu.findItem(R.id.menuJsDisable).isVisible = browser.isJsEnabled
                    menu.findItem(R.id.menuImageEnable).isVisible = !browser.isImageEnabled
                    menu.findItem(R.id.menuImageDisable).isVisible = browser.isImageEnabled
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.menuShare -> browser.foreground.tab.shareUrl(this@MainActivity)
                            R.id.menuOpenInOtherBrowser -> browser.foreground.tab.openInOtherBrowser(this@MainActivity)
                            R.id.menuJsEnable -> browser.isJsEnabled = true
                            R.id.menuJsDisable -> browser.isJsEnabled = false
                            R.id.menuImageEnable -> browser.isImageEnabled = true
                            R.id.menuImageDisable -> browser.isImageEnabled = false
                        }
                        return@setOnMenuItemClickListener false
                    }
                    setOnDismissListener {
                        if (canHideToolBar()) {
                            grpEditPanel.visibility = View.GONE
                            toolbar.visibility = View.INVISIBLE
                            topwrapper.visibility = View.INVISIBLE
                            topwrapper.touhed = false
                        }
                    }
                }
                popup!!.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        browser.saveState()
    }

    override fun onDestroy() {
        windowManager.removeViewImmediate(topwrapper)
        Log.wtf(Util.tag, "${intent?.dataString}")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TabListActivityから戻ってきた場合。
        if (requestCode == REQUEST_SELECT_TAB) {
            if (resultCode == RESULT_OK) {
                // 選ばれたタブをフォアグラウンドにする。
                val tabIndex = data!!.getIntExtra(TabListActivity.EXTRA_SELECTED_TAB_INDEX, 0)
                val tab = browser.tabs[tabIndex]
                browser.foreground.changeTab(tab)
            } else {
                // 戻るボタンなどで戻ってきた場合はなにもしない。
            }

            // タブがひとつもない場合は新しくタブを開く。
            if (browser.tabs.isEmpty()) {
                val tab = browser.addNewTab()
                tab.loadUrl(browser.homeUrl)
                browser.foreground.changeTab(tab)
            }
            return
        }

        // 知らない場所から戻ってきた場合。
        Util.debug(Util.tag, "Unhandled Activity Result: $requestCode $resultCode")
        return super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.wtf(Util.tag, "${intent?.dataString}")

        // 他のアプリなどからURLを渡された場合の処理。
        if (intent?.dataString != null) {
            // 新しくタブを作ってURLを読み込んで表示する。
            val tab = browser.addNewTab()
            tab.loadUrl(intent!!.dataString)
            browser.foreground.changeTab(tab)
        }

        setIntent(intent)
        super.onNewIntent(intent)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (menu == null) return
        if (v == null) return

        // WebView上の要素のコンテキストメニューの作成の場合。
        if (v is WebView) {
            val node = v.hitTestResult
            val type = node.type
            selectedWebNode = node

            // 意味のない場所やテキストボックスの場合はなにもしない。
            if (type == WebView.HitTestResult.UNKNOWN_TYPE ||
                    type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
                return
            }

            menuInflater.inflate(R.menu.main_web_context, menu)
            menu.run {
                setHeaderTitle(node.extra)
                setGroupVisible(R.id.menugAnchor, type == WebView.HitTestResult.SRC_ANCHOR_TYPE
                        || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        || type == WebView.HitTestResult.IMAGE_TYPE
                )
                setGroupVisible(R.id.menugImage, type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        || type == WebView.HitTestResult.IMAGE_TYPE
                )
                setGroupVisible(R.id.menugPhone, type == WebView.HitTestResult.PHONE_TYPE)
                setGroupVisible(R.id.menugMail, type == WebView.HitTestResult.EMAIL_TYPE)
                setGroupVisible(R.id.menugGeo, type == WebView.HitTestResult.GEO_TYPE)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (item == null) return super.onContextItemSelected(item)
        if (selectedWebNode == null) return super.onContextItemSelected(item)
        val node = selectedWebNode!!
        when (item.itemId) {
            R.id.menuShare -> Util.shareUrl(this, node.extra)
            R.id.menuCopyUrl -> {
                Util.copyToClipboard(this, node.extra)
                App.toaster.show(R.string.copied)
            }
            R.id.menuOpenInNewTab -> {
                val tab = browser.addNewTab()
                tab.loadUrl(node.extra)
                browser.foreground.changeTab(tab)
            }
            R.id.menuOpenInBackground -> {
                val tab = browser.addNewTab()
                tab.loadUrl(node.extra)
            }
            R.id.menuOpenInOtherBrowser -> Util.openInOtherBrowser(this, node.extra)
            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        // ページバック可能ならページバックする。
        if (browser.foreground.tab.wb.canGoBack()) {
            browser.foreground.tab.back()
            return
        }

        // もうこれ以上戻れないならタブを閉じる。
        browser.closeTab(browser.foreground.tab)
        App.toaster.show(R.string.tabClosed)

        // 全てのタブを閉じた場合はアプリを閉じる。
        if (browser.tabs.isEmpty()) {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
        // 音量キーの操作でPageUp/PageDownする。
            KeyEvent.KEYCODE_VOLUME_UP -> {
                browser.foreground.tab.wb.run {
                    scrollTo(scrollX, Math.max(scrollY - height / 5, 0))
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                browser.foreground.tab.wb.run {
                    // TODO ページの高さの取得
                    scrollTo(scrollX, scrollY + height / 5)
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun canHideToolBar(): Boolean {
        val notFocused = inputUrl.findFocus() == null
        val notTouched = !topwrapper.touhed
        Log.d(Util.tag, "$notFocused $notTouched")
        return notFocused && notTouched
    }

    override fun onTabCountChanged(count: Int) {
        btnTab.text = count.toString()
    }

    override fun onForegroundTabChanged(oldTab: Tab, newTab: Tab) {
        grpWebViewContainer.removeView(oldTab.wb)
        grpWebViewContainer.addView(newTab.wb,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        newTab.wb.requestFocus()
        registerForContextMenu(browser.foreground.tab.wb)
    }

    override fun onTitleChanged(title: String) {
        btnTitle.text = title
    }

    override fun onUrlChanged(url: String) {
        inputUrl.setText(url)
    }

    override fun onPageStarted() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        browser.foreground.tab.wb.requestFocus()
        toolbar.visibility = View.VISIBLE
        prgLoadingProgress.progress = 5
        prgLoadingProgress.visibility = View.VISIBLE
    }

    override fun onPageFinished() {
        prgLoadingProgress.visibility = View.INVISIBLE
        if (canHideToolBar()) {
            toolbar.visibility = View.INVISIBLE
        }
    }

    override fun onProgressChanged(progress: Int) {
        prgLoadingProgress.progress = progress
        prgLoadingProgress.visibility =
                if (progress == 100) View.INVISIBLE
                else View.VISIBLE
    }

    override fun onBackForwardStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        btnBack.isEnabled = canGoBack
        btnForward.isEnabled = canGoForward
    }

    override fun onReloadStopStateChanged(loading: Boolean) {
        btnReload.visibility = if (loading) View.GONE else View.VISIBLE
        btnStop.visibility = if (loading) View.VISIBLE else View.GONE
    }


    private inner class TopWrapper() : Button(this) {
        val windowParams = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        var touhed: Boolean = false

        init {
            windowParams.gravity = Gravity.TOP

            visibility = View.INVISIBLE
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            background.alpha = 0
        }

        override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
            visibility = View.INVISIBLE
            touhed = true
            popup?.dismiss()
            toolbar.dispatchTouchEvent(event)
            touhed = false
            return super.dispatchTouchEvent(event)
        }
    }
}

