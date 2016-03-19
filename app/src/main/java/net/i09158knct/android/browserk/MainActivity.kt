package net.i09158knct.android.browserk

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.http.SslError
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import net.i09158knct.android.browserk.browser.*

class MainActivity : AppCompatActivity()
        , CustomWebChromeClient.IEventListener
        , CustomWebViewClient.IEventListener {

    var browser: Browser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        browser = Browser(this)
        val b = browser!!
        val initialUrl = getIntent()?.dataString ?: "https://www.google.com"
        b.addNewTab(initialUrl)

        inputUrl.setOnKeyListener { view: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                b.mainvm.loadUrl(inputUrl.text.toString())
                return@setOnKeyListener true
            }
            else {
                return@setOnKeyListener false
            }
        }

        btnBack.setOnClickListener { b.mainvm.back() }
        btnForward.setOnClickListener { b.mainvm.forward() }
        btnReload.setOnClickListener { b.mainvm.reload() }
        btnStop.setOnClickListener { b.mainvm.stopLoading() }
        btnShare.setOnClickListener { b.mainvm.share() }
        btnBookmark.setOnClickListener { b.mainvm.bookmark() }
        btnTab.setOnClickListener { b.mainvm.tab() }
        btnMenu.setOnClickListener {
            if (supportActionBar!!.isShowing) {
                supportActionBar!!.hide()
            }
            else {
                supportActionBar!!.show()
            }
        }
        // TODO タブ数表示
        // TODO 戻る進むボタン無効表示

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_tool, menu)
        return true
    }

    override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        menu?.findItem(R.id.menuJsEnable)?.setVisible(!browser!!.mainvm.IsJsEnabled())
        menu?.findItem(R.id.menuJsDisable)?.setVisible(browser!!.mainvm.IsJsEnabled())
        menu?.findItem(R.id.menuImageEnable)?.setVisible(!browser!!.mainvm.IsImageEnabled())
        menu?.findItem(R.id.menuImageDisable)?.setVisible(browser!!.mainvm.IsImageEnabled())
        return super.onMenuOpened(featureId, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuShare -> browser!!.mainvm.share()
            R.id.menuOpenInOtherBrowser -> browser!!.mainvm.openInOtherBrowser()
            R.id.menuJsEnable -> browser!!.mainvm.switchJs(true)
            R.id.menuJsDisable -> browser!!.mainvm.switchJs(false)
            R.id.menuImageEnable -> browser!!.mainvm.switchImage(true)
            R.id.menuImageDisable -> browser!!.mainvm.switchImage(false)
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (browser!!.mainvm.canGoBack()) {
                browser!!.mainvm.back()
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
        btnReload.visibility = View.GONE
        btnStop.visibility = View.VISIBLE
        supportActionBar!!.show()
    }

    fun canHideToolBar() = inputUrl.findFocus() == null
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

}
