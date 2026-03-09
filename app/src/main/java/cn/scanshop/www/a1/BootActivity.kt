package cn.scanshop.www.a1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

//FIXME：应在每个Activity的Resume()中检查注册码，这样才能防止一直不退出程序而绕过注册码的有效期限制

class BootActivity : AppCompatActivity() {

    val REGACTIVITY = 1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        startActivityForResult(intentFor<RegActivity>(Pair(RegActivity.REG_REREG, false)), REGACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REGACTIVITY) {
            if (resultCode == RegActivity_.OK) {
                startActivity<MainActivity>()
                finish()
            } else {
                finish()
            }
        }
    }
}