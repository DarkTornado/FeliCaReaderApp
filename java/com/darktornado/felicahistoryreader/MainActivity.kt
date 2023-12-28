package com.darktornado.felicahistoryreader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.darktornado.library.FeliCa
import java.util.*

class MainActivity : Activity() {

    var layout: LinearLayout? = null
    private var adapter: NfcAdapter? = null
    private var intent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = LinearLayout(this)
        layout!!.orientation = 1

        val txt = TextView(this)
        txt.setText(R.string.tag_card)
        txt.gravity = Gravity.CENTER
        layout!!.addView(txt)

        setContentView(layout)

        adapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        this.intent = PendingIntent.getActivity(this, 0, intent, 0)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        try {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return

            val id = tag.id ?: return
            val fc = NfcF.get(tag) ?: return

            fc.connect()
            val req = FeliCa.readWithoutEncryption(id, 10)
            val res = fc.transceive(req)
            fc.close()
//            parseHistory(res)
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null) adapter!!.enableForegroundDispatch(this, intent, null, null)
    }

    override fun onPause() {
        super.onPause()
        if (adapter != null) adapter!!.disableForegroundDispatch(this)
    }

    private fun dip2px(dips: Int) = Math.ceil((dips * this.resources.displayMetrics.density).toDouble()).toInt()

    fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

}
