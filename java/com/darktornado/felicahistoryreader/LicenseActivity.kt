package com.darktornado.felicahistoryreader

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import com.darktornado.library.LicenseView

class LicenseActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = 1

        val lice = LicenseView(this)
        lice.setTitle("Material Design")
        lice.setSubtitle("by Google")
        lice.setLicense("Apache License 2.0", "icon.txt")
        layout.addView(lice)

        val pad = dip2px(16)
        layout.setPadding(pad, pad, pad, pad);

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun dip2px(dips: Int) = Math.ceil((dips * this.resources.displayMetrics.density).toDouble()).toInt()

}