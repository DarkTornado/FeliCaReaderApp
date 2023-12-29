package com.darktornado.felicahistoryreader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.view.*
import android.widget.*
import com.darktornado.library.FeliCa
import java.util.*

class MainActivity : Activity() {

    var layout: LinearLayout? = null
    private var adapter: NfcAdapter? = null
    private var intent: PendingIntent? = null


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> startActivity(Intent(this, LicenseActivity::class.java))
            1 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DarkTornado/FeliCaReaderApp")))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "라이선스 정보")
        menu.add(0, 1, 0, "깃허브로 이동")
        return true
    }

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
            parseHistory(res)
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    private fun parseHistory(res: ByteArray) {
        val zero = 0x00;
        if (res[10] != zero.toByte()) {
            toast(getString(R.string.not_felica))
            return
        }
        val size = res[12].toInt()
        val data = ArrayList<History>()
        for (n in 0 until size) {
            val fc = FeliCa.parse(res, 13 + n * 16)
            if (fc.index > 0) data.add(History(fc))
        }
        applyResult(data.toTypedArray())
    }

    private fun applyResult(data: Array<History>) {
        val icons = arrayOfNulls<Drawable>(5)
        icons[FeliCa.TYPE_JR] = resources.getDrawable(R.drawable.metro)
        icons[FeliCa.TYPE_METRO] = icons[FeliCa.TYPE_JR]
        icons[FeliCa.TYPE_BUS] = resources.getDrawable(R.drawable.bus)
        icons[FeliCa.TYPE_GOODS] = resources.getDrawable(R.drawable.shopping)
        icons[4] = resources.getDrawable(R.drawable.charge)

        val adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return data.size
            }

            override fun getItem(pos: Int): Any {
                return data[pos]
            }

            override fun getItemId(pos: Int): Long {
                return pos.toLong()
            }

            override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
                val ctx = parent!!.context
                val layout = LinearLayout(ctx)
                layout.orientation = 0
                layout.weightSum = 4f

                val item = data[pos]
                val icon = ImageView(ctx)
                if (item.action.contains("충전") || item.action.contains("신규")) icon.setImageDrawable(icons[4])
                else icon.setImageDrawable(icons[item.type])

                val params = LinearLayout.LayoutParams(dip2px(64), dip2px(64), 1f)
                params.setMargins(dip2px(8), 0, dip2px(16), 0)
                params.gravity = Gravity.START or Gravity.CENTER
                icon.layoutParams = params
                layout.addView(icon)

                val lay2 = LinearLayout(ctx)
                lay2.orientation = 1
                lay2.layoutParams = LinearLayout.LayoutParams(-1, -2, 3f)

                val title = TextView(ctx)
                title.text = "잔액 : " + item.balance + "円"
                title.textSize = 24f
                title.layoutParams = LinearLayout.LayoutParams(-1, -1)
                title.gravity = Gravity.CENTER_VERTICAL
                lay2.addView(title)

                val subtitle = TextView(ctx)
                subtitle.setText(item.action + " / " + item.date)
                subtitle.textSize = 16f
                lay2.addView(subtitle)
                layout.addView(lay2)

                val mar = dip2px(8)
                val margin = LinearLayout.LayoutParams(-1, -2)
                margin.setMargins(mar, mar, mar, mar)
                layout.layoutParams = margin

                return layout
            }

        }

        val list = ListView(this)
        list.setAdapter(adapter)
        val pad = dip2px(16)
        list.setPadding(pad, pad, pad, pad)

        layout!!.removeAllViews()
        layout!!.addView(list)

        toast(getString(R.string.card_read))
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