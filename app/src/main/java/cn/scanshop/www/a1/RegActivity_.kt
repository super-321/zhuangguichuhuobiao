package cn.scanshop.www.a1

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.DeviceUtils
import kotlinx.android.synthetic.main.reg_layout.*
import org.jetbrains.anko.toast
import java.math.BigInteger
import kotlin.math.sqrt

class RegActivity_ : AppCompatActivity() {

    companion object {
        val OK = 1

        val REG_KEY = "reg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mac = getMac()

        if (Util.readConfig(this, REG_KEY) == calcReg(mac)) { //已经输入过正确的注册码
            setResult(OK)
            finish()
        }

        setContentView(R.layout.reg_layout)

//        lblmac.visibility = android.view.View.GONE
//        Utils.init(application)
        lblsn.text = mac

        txtreg.inputType = InputType.TYPE_CLASS_NUMBER
        Keybord.openKeybord2(txtreg, this)
        txtreg.setOnKeyListener { v, i, k ->
            if ((k.keyCode == KeyEvent.KEYCODE_ENTER) and (k.action == KeyEvent.ACTION_DOWN)) {
                btnok.callOnClick()
                true
            }
            false
        }

        btnok.setOnClickListener {
            if (txtreg.text.toString() == calcReg(lblsn.text.toString())) {
                Util.saveConfig(this, REG_KEY, txtreg.text.toString())
                setResult(OK)
                finish()
            } else {
                toast("注册码错误")
            }
        }
    }

    private fun getMac() = DeviceUtils.getMacAddress()

    fun calcReg(sn: String): String {
        val snnum = BigInteger(sn.split(":").joinToString(""), 16)
        val result = sqrt(snnum.toDouble() * 8 / 3 / 2019).toInt().toString()
        Log.d("q", result)
        return result
    }
}
