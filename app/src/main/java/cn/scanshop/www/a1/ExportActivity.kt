package cn.scanshop.www.a1

import QSocket
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.export_layout.*
import org.jetbrains.anko.toast


class ExportActivity : AppCompatActivity() {

    private val HOST_KEY = "host"
    private var orderno: String? = null
    private var ajcode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.export_layout)

        initCtrl()
    }

    private fun initCtrl() {
        try {
            orderno = intent.extras.getString(SQLiteDbHelper.FN_ORDERNO)
            ajcode = intent.extras.getString(SQLiteDbHelper.FN_AJCODE)

            txthost.setText(Util.readConfig(this, HOST_KEY))

//            btnsend.setOnClickListener {
//                //                QSocket.host = "192.168.100.10"
////                QSocket.port = 60000
//                val content = txtcontent.text.toString()
//                QSocket.send(content, this)
//                Log.d("q", content)
//                Util.showMessage(this, "发送成功：${content}")
//            }
            btnsend.setOnClickListener {
                sendClick(it)
            }

            txthost.setOnKeyListener { v, i, k ->
                if (k.keyCode == KeyEvent.KEYCODE_ENTER && k.action == KeyEvent.ACTION_DOWN) {
                    sendClick(v)
                    true
                } else {
                    false
                }
            }

        } catch (e: Exception) {
            Log.e("q", e.message)
            throw e
        }

//        QSocket.receive()
    }

    private val sendClick = { v: View ->
        try {
//            Util.showMessage(this, "开始发送...")
            toast("开始发送...")

            val db = SQLiteDbHelper(this)
            val w = db.getWeight(this.orderno, this.ajcode)
            val s = db.getSeal(this.orderno, this.ajcode)
            val list = db.exportList4(this.orderno, this.ajcode, w, s)

//            MediaScannerConnection.scanFile(this, arrayOf(SQLiteDbHelper.EXPORT_FILE),null, null)

            val data = Uri.parse("file:///sdcard/a1-export.txt")
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data)) //使电脑可以刷新一下就看到新生成的文件

            val host = txthost.text.toString().replace("*", ".").trim() //用*号来输入.，因为键盘上没有.
            txthost.setText(host) //* -> . 后将文本框的内容变过来
            Util.saveConfig(this, HOST_KEY, host)

//            QSocket.send(host, list, this, showProgress)
            val name = Util.readConfig(this, "name")
            QSocket.send2(host, list, this.orderno!!, this.ajcode!!, name, this, showProgress)
        } catch (e: Exception) {
            Log.e("q", e.message)
            toast(e.message.toString())
        }
    }

    private val showProgress = { p: Int, m: Int ->
        runOnUiThread {
            title = "上传数据（${p}/${m}）"
            btnsend.isEnabled = false

            if (p == m) {
                btnsend.isEnabled = true
                lblstatus.text = "${lblstatus.text}\r\n上传完成"
                Util.showMessage(this, "上传完成")
            }
        }
    }
}