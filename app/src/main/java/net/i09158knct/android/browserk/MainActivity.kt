package net.i09158knct.android.browserk

import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import net.i09158knct.android.browserk.browser.CustomWebChromeClient
import net.i09158knct.android.browserk.browser.CustomWebViewClient
import net.i09158knct.android.browserk.services.Toaster

class MainActivity : AppCompatActivity()
        , CustomWebChromeClient.IEventListener
        , CustomWebViewClient.IEventListener {

    var webview: WebView? = null
    val chromeClient: CustomWebChromeClient = CustomWebChromeClient(this);
    var viewClient: CustomWebViewClient = CustomWebViewClient(this);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnBack.setOnClickListener { App.s.toaster.show("btnBack") }
        btnForward.setOnClickListener { App.s.toaster.show("btnForward") }
        btnReload.setOnClickListener { App.s.toaster.show("btnReload") }
        btnShare.setOnClickListener { App.s.toaster.show("btnShare") }
        btnBookmark.setOnClickListener { App.s.toaster.show("btnBookmark") }
        btnTab.setOnClickListener { App.s.toaster.show("btnTab") }
        // TODO タブ数表示
        // TODO 中止ボタン
        // TODO 戻る進むボタン無効表示

        webview = WebView(applicationContext
        )
        webview!!.setWebChromeClient(chromeClient)
        webview!!.setWebViewClient(viewClient)
        grpWebViewContainer.addView(webview
                , LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.MATCH_PARENT
        )
        webview!!.requestFocus();

        val initialUrl = getIntent()?.dataString ?: "https://www.google.com"
        webview!!.loadUrl(initialUrl)
    }


    override fun onProgressChanged(view: WebView, newProgress: Int) {
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
    }

    override fun onReceivedTitle(view: WebView, title: String) {
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
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
