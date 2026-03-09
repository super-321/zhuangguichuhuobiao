package cn.scanshop.www.a1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import cn.scanshop.www.a1.Util.Companion.eb
import com.blankj.utilcode.util.DeviceUtils
import com.cxyzy.demo.AesCryptUtil
import kotlinx.android.synthetic.main.reg_layout.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class RegActivity : AppCompatActivity() {

    companion object {
        val OK = 1

        val REG_KEY = "reg"
        val REG_REREG = "rereg" //重新注册

        val PASS = "@q-soft.jan.6.2020"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val mac = getMac()
            val reg = Util.readConfig(this, REG_KEY)

            val rereg = intent.extras.getBoolean(REG_REREG)

            try {
                if (rereg){ //rereg为true时，表示主动重新注册；rereg为false时，表示程序正常启动时检查注册信息
                    throw java.lang.Exception("重新注册，故意抛出异常，跳到 catch{} 块中")
                }
                checkReg(reg)

                setResult(OK)
                finish()
            } catch (e: Exception) { //这里出现异常，是因为注册码无效，因此接着就要显示出注册窗口

                eb(this, e)

                setContentView(R.layout.reg_layout)
                lblsn.setText(mac)
//                txtreg.inputType = InputType.TYPE_CLASS_NUMBER
//                Keybord.openKeybord2(txtreg, this)
                txtreg.setOnKeyListener { v, i, k ->
                    if ((k.keyCode == KeyEvent.KEYCODE_ENTER) and (k.action == KeyEvent.ACTION_DOWN)) {
                        btnok.callOnClick()
                        true
                    }
                    false
                }

                btnok.setOnClickListener {

                    val reg = txtreg.text.toString()

                    try {
                        checkReg(reg)
                        Util.saveConfig(this, REG_KEY, reg)
                        setResult(OK)
                        finish()
                    } catch (e: java.lang.Exception) {
                        eb(this, java.lang.Exception("注册码错误（${e.message ?: e.toString()}）"))
                    }
                }

                btnfile.setOnClickListener {
                    btnfileClick()
                }

            }
        } catch (e: Exception) {
            eb(this, e)
        }
    }

    /**
     * 点击 ... 打开注册码文件，从文件里读取出注册码
     */
    private val btnfileClick = {
        val intent = Intent().also {
            it.action = Intent.ACTION_GET_CONTENT
            it.type = "text/plain"
        }
        startActivityForResult(intent, 999)
    }

    /**
     * 从uri指定位置读取出文件内容
     */
    private fun readTextFile(uri: Uri): String? {
        var reader: BufferedReader? = null
        val builder = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)))
            var line: String? = ""
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return builder.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 999 && resultCode == RESULT_OK) {
            val uri = data?.data
            val content = readTextFile(uri!!)
            txtreg.setText(content)
        }
    }

    fun checkReg(reg: String) {
        val mac = getMac()

        val sd = decrypt(reg)
        val (sn, date) = extract(sd)
        val validity = date.toDate()

        if (sn != mac) {
            throw Exception("序列号错误")
        } else if (validity < Date()) {
            throw Exception("超过有效期")
        } // else 注册码有效
    }

    private fun getMac() = DeviceUtils.getMacAddress().replace(":", "") //将 : 去掉，方便输入

//    fun calcReg(sn: String): String {
//        val snnum = BigInteger(sn.split(":").joinToString(""), 16)
//        val result = sqrt(snnum.toDouble() * 8 / 3 / 2019).toInt().toString()
//        Log.d("q", result)
//        return result
//    }

    /**
     * 将注册码解码出来
     */
    fun decrypt(reg: String): String {
        val result = AesCryptUtil.decrypt(PASS, reg)
        return result
    }

    /**
     * 将序列号+有效期提取出来
     * return: (序列号,日期)
     */
    fun extract(sndate: String): Pair<String, String> {
        val sd = sndate.split(",")
        return Pair(sd[0], sd[1])
    }

    /**
     * 用 MAC地址 + 有效期 生成注册码
     */
    fun calcReg(sn: String, date: String): String {
        val s = "$sn,${SimpleDateFormat("yyyy-M-d").format(date.toDate())}"
        return AesCryptUtil.encrypt(PASS, s)
    }
}
