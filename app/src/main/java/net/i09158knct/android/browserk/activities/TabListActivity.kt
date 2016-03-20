package net.i09158knct.android.browserk.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_tab_list.*
import kotlinx.android.synthetic.main.item_tab.view.*
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.R
import net.i09158knct.android.browserk.browser.Browser
import net.i09158knct.android.browserk.browser.Tab

class TabListActivity : AppCompatActivity()
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
            browser.addNewTab("")
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
: ArrayAdapter<Tab>(context, 0, tabs) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view = convertView ?: View.inflate(context, R.layout.item_tab, null)
        val tab = getItem(position)
        if (tab == null) return view

        view.txtTitle.text = tab.wb.title
        view.txtUrl.text = tab.wb.url
        view.grpTabSelectArea.setOnClickListener { listener.onClickTab(tab) }
        view.btnClose.setOnClickListener { listener.onClickTabClose(tab) }
        return view
    }

    interface IEventListener {
        fun onClickTab(tab: Tab): Unit
        fun onClickTabClose(tab: Tab): Unit
    }
}