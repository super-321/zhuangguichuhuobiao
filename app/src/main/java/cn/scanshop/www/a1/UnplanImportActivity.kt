package cn.scanshop.www.a1

import QSocket
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.export_layout.txthost
import kotlinx.android.synthetic.main.import_layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast

class UnplanImportActivity : AppCompatActivity() {

    private val HOST_KEY = "host"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.import_layout)

        initCtrl()
    }

    private fun initCtrl() {
        try {
            txthost.setText(Util.readConfig(this, HOST_KEY))

            btnreceive.setOnClickListener {
                receiveClick(it)
            }

            txthost.setOnKeyListener { v, i, k ->
                if (k.keyCode == KeyEvent.KEYCODE_ENTER && k.action == KeyEvent.ACTION_DOWN) {
                    receiveClick(v)
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

    private val receiveClick = { v: View ->
        try {
            lblreceived.text = ""

//            Util.showMessage(this, "开始接收...")
            toast("开始接收")

            val host = txthost.text.toString().replace("*", ".").trim() //用*号来输入.，因为键盘上没有.
            txthost.setText(host) //* -> . 后将文本框的内容变过来
            Util.saveConfig(this, HOST_KEY, host)

//            QSocket.receive(host, this, showProgress)
            QSocket.receive3(host, this, showProgress) //在 showProgress() 里将接收到的数据写入数据库。runBlock{}。
        } catch (e: Exception) {
            Log.e("q", e.message)
        }
    }

    private val showProgress = { p: Int, m: Int, result: ArrayList<String> ->
        runOnUiThread {
            try {
                title = "下载数据-条码库（$p/$m）"
//                lblreceived.text = "${lblreceived.text}\r\n\r\n${result.last()}"
                btnreceive.isEnabled = false

                if (p == m) {
                    val ui = this

                    lblreceived.text = "${lblreceived.text}\r\n正在写入数据，可能需要几分钟，请耐心等待..."

                    GlobalScope.launch {
                        val db = SQLiteDbHelper(ui)
                        db.importBars(result)
                        runOnUiThread {
                            btnreceive.isEnabled = true
                            lblreceived.text = "${lblreceived.text}\r\n下载完成"
                            toast("下载完成") //外层有一个GlobalScope.launch{}，可能使得内部的上下文变了，再用runOnUiThread{}设置回来，要不会跳出
                        }
//                        Util.showMessage(ui, "下载完成")
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e("q", e.message)
                Util.showMessage(this, e.message.toString())
            }
        }
    }
}