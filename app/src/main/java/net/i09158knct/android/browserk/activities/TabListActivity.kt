package net.i09158knct.android.browserk.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_tab_list.*
import kotlinx.android.synthetic.main.item_tab.view.*
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.R
import net.i09158knct.android.browserk.browser.Browser
import net.i09158knct.android.browserk.browser.Tab

class TabListActivity : Activity()
        , TabListAdapter.IEventListener {

    companion object {
        const val EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX"
    }

    lateinit var browser: Browser
    lateinit var adapter: TabListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_list)
        browser = App.browser
        adapter = TabListAdapter(this, browser.tabs, this)
        lstTab.adapter = adapter

        btnAddNewTab.setOnClickListener {
            val tab = browser.addNewTab()
            tab.loadUrl(browser.homeUrl)
            adapter.notifyDataSetChanged()
        }

        btnRestoreClosedTab.setOnClickListener {
            // TODO
            App.toaster.show("TODO")
        }
    }

    override fun onClickTabClose(tab: Tab) {
        browser.closeTab(tab)
        adapter.notifyDataSetChanged()
    }

    override fun onClickTab(tab: Tab) {
        val intent = Intent()
        val pos = browser.tabs.indexOf(tab)
        intent.putExtra(EXTRA_SELECTED_TAB_INDEX, pos)
        setResult(RESULT_OK, intent)
        finish()
    }
}

class TabListAdapter(context: Context, val tabs: List<Tab>, val listener: IEventListener)
: ArrayAdapter<Tab>(context, R.layout.item_tab, 0, tabs) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view = convertView ?: View.inflate(context, R.layout.item_tab, null)
        val tab = getItem(position) ?: return null

        return view.apply {
            txtTitle.text = tab.wb.title
            txtUrl.text = tab.wb.url
            grpTabSelectArea.setOnClickListener { listener.onClickTab(tab) }
            btnClose.setOnClickListener { listener.onClickTabClose(tab) }
        }
    }

    interface IEventListener {
        fun onClickTab(tab: Tab): Unit
        fun onClickTabClose(tab: Tab): Unit
    }
}