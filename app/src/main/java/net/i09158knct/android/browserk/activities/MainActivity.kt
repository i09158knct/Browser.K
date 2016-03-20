package net.i09158knct.android.browserk.activities

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
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

class MainActivity : AppCompatActivity()
        , Browser.IEventListener
        , ForegroundTabManager.IEventListener {

    companion object {
        const private val REQUEST_SELECT_TAB: Int = 0x001
    }

    private lateinit var browser: Browser
    private lateinit var topwrapper: TopWrapper
    private var popup: PopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.wtf(Util.tag, "${intent?.dataString}")
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
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
            if (supportActionBar!!.isShowing) {
                supportActionBar!!.hide()
            } else {
                supportActionBar!!.show()
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
                        supportActionBar!!.hide()
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
            // TODO タブがひとつもない場合
            return
        }

        Util.debug(Util.tag, "Unhandled Activity Result: $requestCode $resultCode")
        return super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        Log.wtf(Util.tag, "${intent?.dataString}")
    }

    override fun onRestart() {
        super.onRestart()
        Log.wtf(Util.tag, "${intent?.dataString}")
    }

    override fun onResume() {
        super.onResume()
        Log.wtf(Util.tag, "${intent?.dataString}")
    }

    override fun onPause() {
        Log.wtf(Util.tag, "${intent?.dataString}")
        super.onPause()
    }

    override fun onStop() {
        Log.wtf(Util.tag, "${intent?.dataString}")
        super.onStop()
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (browser.foreground.tab.wb.canGoBack()) {
                browser.foreground.tab.back()
                return false
            }
            // TODO タブのクローズ
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
    }

    override fun onTitleChanged(title: String) {
        txtTitle.setText(title)
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
        supportActionBar!!.show()
        prgLoadingProgress.progress = 0
        prgLoadingProgress.visibility = View.VISIBLE
    }

    override fun onPageFinished() {
        prgLoadingProgress.visibility = View.INVISIBLE
        if (canHideToolBar()) {
            supportActionBar!!.hide()
        }
    }

    override fun onProgressChanged(progress: Int) {
        prgLoadingProgress.progress = progress
        prgLoadingProgress.visibility =
                if (progress == 0) View.INVISIBLE
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

