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
        getWindowManager().addView(topwrapper, topwrapper.windowParams);

        App.browser = Browser(this)
        browser = App.browser
        browser.listener = this
        browser.foreground.listener = this
        val initialUrl = getIntent()?.dataString ?: "https://www.google.com"
        browser.foreground.tab.loadUrl(initialUrl)
        btnTab.text = browser.tabs.count().toString()

        grpWebViewContainer.addView(browser.foreground.tab.wb,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        browser.foreground.tab.wb.requestFocus()
        registerForContextMenu(browser.foreground.tab.wb);

        btnTitle.setOnClickListener { btnTitle.maxLines = if (btnTitle.maxLines == 1) 10 else 1 }
        btnClearUrl.setOnClickListener { inputUrl.text.clear() }
        btnPasteUrl.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;
            if (!(clipboard.hasPrimaryClip())) {
                App.toaster.show("!(clipboard.hasPrimaryClip())")
            } else if (!(clipboard.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
                App.toaster.show("since the clipboard has data but it is not plain text")
            } else {
                val item = clipboard.getPrimaryClip().getItemAt(0);
                val start = inputUrl.selectionStart;
                val end = inputUrl.selectionEnd;
                inputUrl.text.replace(Math.min(start, end), Math.max(start, end), item.text);

            }
        }
        btnEnterUrl.setOnClickListener {
            val text = inputUrl.text.toString()
            if (text.isEmpty()) return@setOnClickListener
            browser.query(text)
        }
        inputUrl.setOnFocusChangeListener { view, b ->
            grpEditPanel.visibility = View.VISIBLE xor View.GONE xor grpEditPanel.visibility;
        }
        inputUrl.setOnKeyListener { view: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                browser.query(inputUrl.text.toString())
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }

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
                toolbar.visibility = View.VISIBLE
                topwrapper.visibility = View.VISIBLE;
                topwrapper.height = toolbar.height
                topwrapper.touhed = false
                val p = PopupMenu(this, btnMenu)
                popup = p
                p.menuInflater.inflate(R.menu.main_tool, p.menu)
                p.menu.findItem(R.id.menuJsEnable).setVisible(!browser.isJsEnabled)
                p.menu.findItem(R.id.menuJsDisable).setVisible(browser.isJsEnabled)
                p.menu.findItem(R.id.menuImageEnable).setVisible(!browser.isImageEnabled)
                p.menu.findItem(R.id.menuImageDisable).setVisible(browser.isImageEnabled)
                p.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menuShare -> browser.foreground.tab.shareUrl(this)
                        R.id.menuOpenInOtherBrowser -> browser.foreground.tab.openInOtherBrowser(this)
                        R.id.menuJsEnable -> browser.isJsEnabled = true
                        R.id.menuJsDisable -> browser.isJsEnabled = false
                        R.id.menuImageEnable -> browser.isImageEnabled = true
                        R.id.menuImageDisable -> browser.isImageEnabled = false
                    }
                    return@setOnMenuItemClickListener false
                }
                p.setOnDismissListener {
                    if (canHideToolBar()) {
                        toolbar.visibility = View.INVISIBLE
                        topwrapper.visibility = View.INVISIBLE
                        topwrapper.touhed = false
                    }
                }
                p.show()
            }
        }
    }

    override fun onDestroy() {
        windowManager.removeView(topwrapper)
        Log.wtf(Util.tag, "${intent?.dataString}")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_TAB) {
            if (resultCode == RESULT_OK) {
                val tabIndex = data!!.getIntExtra(TabListActivity.EXTRA_SELECTED_TAB_INDEX, 0)
                val tab = browser.tabs[tabIndex]
                browser.foreground.changeTab(tab)
            } else {
                // 戻るボタンなどで戻ってきた場合はなにもしない
            }
            // タブがひとつもない場合は新しくタブを開く
            if (browser.tabs.isEmpty()) {
                val tab = browser.addNewTab()
                tab.loadUrl(browser.homeUrl)
                browser.foreground.changeTab(tab)
            }
            return
        }

        Util.debug(Util.tag, "Unhandled Activity Result: $requestCode $resultCode")
        return super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.wtf(Util.tag, "${intent?.dataString}")
        if (intent?.dataString != null) {
            val tab = browser.addNewTab();
            tab.loadUrl(intent!!.dataString)
            browser.foreground.changeTab(tab)
        }

        setIntent(intent);
        super.onNewIntent(intent);
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (menu == null) return
        if (v == null) return

        if (v is WebView) {
            val wb = v as WebView
            val node = wb.hitTestResult
            val type = node.type
            selectedWebNode = node
            if (type == WebView.HitTestResult.UNKNOWN_TYPE ||
                    type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
                return
            }
            val title = node.extra
            menu.setHeaderTitle(title)
            menuInflater.inflate(R.menu.main_web_context, menu)
            menu.setGroupVisible(R.id.menugAnchor, type == WebView.HitTestResult.SRC_ANCHOR_TYPE
                    || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                    || type == WebView.HitTestResult.IMAGE_TYPE
            )
            menu.setGroupVisible(R.id.menugImage, type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                    || type == WebView.HitTestResult.IMAGE_TYPE
            )
            menu.setGroupVisible(R.id.menugPhone, type == WebView.HitTestResult.PHONE_TYPE)
            menu.setGroupVisible(R.id.menugMail, type == WebView.HitTestResult.EMAIL_TYPE)
            menu.setGroupVisible(R.id.menugGeo, type == WebView.HitTestResult.GEO_TYPE)

        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (item == null) return super.onContextItemSelected(item)
        if (selectedWebNode == null) return super.onContextItemSelected(item)
        val node = selectedWebNode!!
        when (item.itemId) {
            R.id.menuShare -> Util.shareUrl(this, node.extra);
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (browser.foreground.tab.wb.canGoBack()) {
                    browser.foreground.tab.back()
                    return true
                }
                // もうこれ以上戻れないならタブを閉じる。
                // 全てのタブを閉じた場合はアプリを閉じる（デフォルト動作）。
                browser.closeTab(browser.foreground.tab)
                App.toaster.show(R.string.tabClosed)
                if (!browser.tabs.isEmpty()) return true
                else return super.onKeyDown(keyCode, event)
            }
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
        registerForContextMenu(browser.foreground.tab.wb);
    }

    override fun onTitleChanged(title: String) {
        btnTitle.setText(title)
    }

    override fun onUrlChanged(url: String) {
        inputUrl.setText(url)
    }

    override fun onPageStarted() {
        val view = getCurrentFocus();
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                PixelFormat.TRANSLUCENT);
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

