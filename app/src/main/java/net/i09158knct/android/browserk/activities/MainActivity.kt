package net.i09158knct.android.browserk.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.net.http.SslError
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.widget.Button
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_main.*
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.R
import net.i09158knct.android.browserk.browser.Browser
import net.i09158knct.android.browserk.browser.CustomWebChromeClient
import net.i09158knct.android.browserk.browser.CustomWebViewClient
import net.i09158knct.android.browserk.utils.Util

class MainActivity : AppCompatActivity()
        , CustomWebChromeClient.IEventListener
        , CustomWebViewClient.IEventListener {

    companion object {
        const private val REQUEST_SELECT_TAB: Int = 0x001
    }

    private lateinit var browser: Browser
    private lateinit var topwrapper: TopWrapper
    private var popup: PopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        topwrapper = TopWrapper()
        getWindowManager().addView(topwrapper, topwrapper.windowParams);

        App.browser = Browser(this)
        browser = App.browser
        val initialUrl = getIntent()?.dataString ?: "https://www.google.com"
        val newTab = browser.addNewTab(initialUrl)
        browser.changeCurrentTab(newTab)

        inputUrl.setOnKeyListener { view: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                browser.mainvm.loadUrl(inputUrl.text.toString())
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }

        btnBack.setOnClickListener { browser.mainvm.back() }
        btnForward.setOnClickListener { browser.mainvm.forward() }
        btnReload.setOnClickListener { browser.mainvm.reload() }
        btnStop.setOnClickListener { browser.mainvm.stopLoading() }
        btnShare.setOnClickListener { browser.mainvm.share() }
        btnBookmark.setOnClickListener { browser.mainvm.bookmark() }
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
                p.menu.findItem(R.id.menuJsEnable).setVisible(!browser.mainvm.IsJsEnabled())
                p.menu.findItem(R.id.menuJsDisable).setVisible(browser.mainvm.IsJsEnabled())
                p.menu.findItem(R.id.menuImageEnable).setVisible(!browser.mainvm.IsImageEnabled())
                p.menu.findItem(R.id.menuImageDisable).setVisible(browser.mainvm.IsImageEnabled())
                p.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menuShare -> browser.mainvm.share()
                        R.id.menuOpenInOtherBrowser -> browser.mainvm.openInOtherBrowser()
                        R.id.menuJsEnable -> browser.mainvm.switchJs(true)
                        R.id.menuJsDisable -> browser.mainvm.switchJs(false)
                        R.id.menuImageEnable -> browser.mainvm.switchImage(true)
                        R.id.menuImageDisable -> browser.mainvm.switchImage(false)
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
        // TODO タブ数表示
        // TODO 戻る進むボタン無効表示
    }

    override fun onDestroy() {
        windowManager.removeView(topwrapper)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_TAB) {
            if (resultCode == RESULT_OK) {
                val tabIndex = data!!.getIntExtra(TabListActivity.EXTRA_SELECTED_TAB_INDEX, 0)
                val tab = browser.tabs[tabIndex]
                browser.changeCurrentTab(tab)
            } else {
                // 戻るボタンなどで戻ってきた場合はなにもしない
            }
            // TODO タブがひとつもない場合
            return
        }

        Util.debug(Util.tag, "Unhandled Activity Result: $requestCode $resultCode")
        return super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(Util.tag, "${intent?.dataString}")
        if (intent?.dataString != null) {
            val tab = browser.addNewTab(intent!!.dataString);
            browser.changeCurrentTab(tab)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (browser.mainvm.canGoBack()) {
                browser.mainvm.back()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        prgLoadingProgress.visibility = View.VISIBLE
        prgLoadingProgress.progress = newProgress
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
        imgFavicon.setImageDrawable(BitmapDrawable(getResources(), icon))
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        txtTitle.setText(title)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        inputUrl.setText(url)
        inputUrl.clearFocus()
        btnReload.visibility = View.GONE
        btnStop.visibility = View.VISIBLE
        supportActionBar!!.show()
    }

    fun canHideToolBar(): Boolean {
        val notFocused = inputUrl.findFocus() == null
        val notTouched = !topwrapper.touhed
        Log.d(Util.tag, "$notFocused $notTouched")
        return notFocused && notTouched
    }

    override fun onPageFinished(view: WebView, url: String) {
        prgLoadingProgress.visibility = View.INVISIBLE
        btnReload.visibility = View.VISIBLE
        btnStop.visibility = View.GONE
        if (canHideToolBar()) {
            supportActionBar!!.hide()
        }
    }

    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
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

