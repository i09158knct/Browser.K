package net.i09158knct.android.browserk.services

import android.content.Context
import android.widget.Toast

class Toaster(val context: Context) {
    fun show(text: String): Unit {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}