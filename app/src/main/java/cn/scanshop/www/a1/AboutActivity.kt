package cn.scanshop.www.a1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.DeviceUtils
import kotlinx.android.synthetic.main.about_layout.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.about_layout)

//        lblmac.visibility = android.view.View.GONE
//        Utils.init(application)
        lblmac.text = DeviceUtils.getMacAddress()
    }
}
