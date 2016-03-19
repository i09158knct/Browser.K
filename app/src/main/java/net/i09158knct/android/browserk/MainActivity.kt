package net.i09158knct.android.browserk

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.http.SslError
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val browser = Browser(this)
        val initialUrl = getIntent()?.dataString ?: "https://www.google.com"
        browser.addNewTab(initialUrl)

        inputUrl.setOnKeyListener { view: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                browser.mainvm.loadUrl(inputUrl.text.toString())
                return@setOnKeyListener true
            }
            else {
                return@setOnKeyListener false
            }
        }

        btnBack.setOnClickListener { browser.mainvm.back() }
        btnForward.setOnClickListener { browser.mainvm.forward() }
        btnReload.setOnClickListener { browser.mainvm.reload() }
        btnShare.setOnClickListener { browser.mainvm.share() }
        btnBookmark.setOnClickListener { browser.mainvm.bookmark() }
        btnTab.setOnClickListener { browser.mainvm.tab() }
        // TODO タブ数表示
        // TODO 中止ボタン
        // TODO 戻る進むボタン無効表示

    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
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
        supportActionBar?.show()
    }

    override fun onPageFinished(view: WebView, url: String) {
    }

    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
    }

}
