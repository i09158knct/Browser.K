package net.i09158knct.android.browserk.browser

import android.widget.LinearLayout
import net.i09158knct.android.browserk.activities.MainActivity

class BrowserViewManager(val view: LinearLayout) {
    var currentTab: Tab? = null
    fun showTab(tab: Tab): Unit {
        view.removeView(currentTab?.wb)
        currentTab = tab
        view.addView(tab.wb,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        tab.wb.requestFocus()
    }
}
