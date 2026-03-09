package cn.scanshop.www.a1

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.unplan_loading_layout.*
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.util.regex.Pattern

class UnplanLodingActivity : AppCompatActivity() {

    private val db = SQLiteDbHelper(this)
    private var orderno: String? = null
    private var aj: String? = null
    var bar: String? = null

    private val option_base_id = 555

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unplan_loading_layout)
        initCtrls()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()

        menuInflater.inflate(R.menu.unplan_menu_loading, menu)

        val ajs = db.unplanGetAjOfOrder(this.orderno)
        for ((i, aj) in ajs.withIndex()) {
            menu?.add(0, option_base_id + i, 2, aj)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun initCtrls() {
        orderno = intent.extras.getString(SQLiteDbHelper.FN_ORDERNO)
        val checked = db.unplanGetChecked(this.orderno)
        title = "临时：${this.orderno}（$checked）"

        txtbar.inputType = InputType.TYPE_CLASS_NUMBER
        txtbar.setSelectAllOnFocus(true)

        txtaj.setOnClickListener(txtajClick)
        txtbar.setOnKeyListener(txtbarClick)
    }

    private val txtbarClick = { v: View, k: Int, e: KeyEvent ->

        if (k == KeyEvent.KEYCODE_ENTER && e.action == KeyEvent.ACTION_DOWN) {
            txtbar.selectAll()
            this.bar = txtbar.text.toString()

            try {

                if (aj.isNullOrEmpty()) {
                    throw java.lang.Exception("货柜号未填写")
                }

                db.unplanExists(this.bar) //如果没有bar，会抛出异常，被后面的 catch() 捕获。临时调货扫描只要有指定的条码即可装柜，不需要先导入回数，但要导入条码库

                var qty = 0

                val bi = db.unplanGet(this.bar, this.orderno)
                Util.inputDialog(
                    this,
                    "数量（${bi.model}）\r\n${this.bar}\r\n",
                    "",
                    "当前数量：${bi.total}",
                    InputType.TYPE_CLASS_NUMBER
                ) {
                    try {
                        qty = Integer.parseInt(it)
                        if (qty >= 1) {
                            db.unplanUpdate(this.bar, db.getAj(this.aj), qty, this.orderno)
                            refreshScreen() //刷新屏幕
                        }
                    } catch (e: java.lang.Exception) {
                        Util.showMessage(this, e.message.toString())
                        Util.alertSound(this)
                        Log.e("q", e.message)
                    } finally {
                        Keybord.closeKeybord(txtbar, this)
                    }
                }
            } catch (e: Exception) {
                Util.showMessage(this, e.message.toString())
                Util.alertSound(this)
                Log.e("q", e.message)
            }
            true
        } else {
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stat -> { //出货统计
                val intent = Intent(this, UnplanStatActivity::class.java)
                intent.putExtra(SQLiteDbHelper.FN_ORDERNO, orderno)
                startActivity(intent)
                true
            }
            R.id.action_qty -> { //修改数量
                try {
                    val bi = db.unplanGet(this.bar, this.orderno)
                    Util.inputDialog(
                        this,
                        "修改数量",
                        "",
                        "当前数量：${bi.total}",
                        InputType.TYPE_CLASS_NUMBER,
                        null
                    ) {

                        try {
                            val q = Integer.parseInt(it)
                            alterqty(q)
                        } catch (e: java.lang.Exception) {
                            Log.e("q", e.message.toString())
                            Util.showMessage(this, e.message.toString())
                            Util.alertSound(this)
                        } finally {
                            Keybord.closeKeybord(txtbar, this)
                        }
                    }
                } catch (e: java.lang.IllegalArgumentException) {
                    Log.e("q", "请先选好货柜及输入好条码")
                    Util.showMessage(this, "请先选好货柜及输入好条码")
                } catch (e: java.lang.Exception) {
                    Log.e("q", e.message)
                    Util.showMessage(this, e.message.toString())
                }

                true
            }
            R.id.action_del -> { //删除本回数
                Util.confirmYesNo(this, "删除本回数？") {

                    if (it == Dialog.BUTTON_POSITIVE) {
                        Util.inputDialog(this, "删除回数（${this.orderno}）", "", "请输入密码", null) { pwd ->
                            if (pwd == "999") {
                                if (it == Dialog.BUTTON_POSITIVE) {
                                    db.unplanResetOrder(this.orderno)
                                    title = "临时：（已删除）"
                                }
                            } else {
                                toast("密码错误")
                            }
                        }
                    }
                }
                true
            }
            else -> { //查看指定一个柜的统计信息，如：BSIU123456-B
                val aj = item.title
                val orderno = this.orderno
                val intent = Intent(this, UnplanStatOfAjActivity::class.java)
                intent.putExtra(SQLiteDbHelper.FN_ORDERNO, orderno)
                intent.putExtra(SQLiteDbHelper.FN_AJCODE, aj)
                startActivityForResult(intent, 999)
                true

//                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * 修改数量
     */
    private fun alterqty(q: Int) {
        db.unplanAlterQty(this.bar, this.aj, q, this.orderno)
        refreshScreen()
        toast("数量改为：$q")
    }

    /**
     * 点击货柜文本框
     */
    private val txtajClick = { v: View ->
        try {
            val ajs = db.unplanGetAjs(this.orderno)
            selector("请选择货柜号", ajs) { d, i ->
                val aj = ajs[i]
                if (Pattern.matches("[A-Z]{2,6}[0-9]{5,10}-[A-Z]", aj)) { //货柜号符合规则
                    txtaj.setText(ajs[i])
                    this.aj = ajs[i]
                } else { //货柜号不符合规则
                    Util.inputDialog(this, "请输入货柜号", "", "举例：BSIU9718737", null, "[A-Z]{2,6}[0-9]{5,10}") {
                        val ajpure = db.getAj(aj) //aj是aj，可能有 - 打头，getAj()将取出纯aj部分
                        db.unplanSetAj(ajpure, it, this.orderno)
//                            lblaj.setText("$it-$ajpure")
                        txtaj.setText("$it-$ajpure")
                        this.aj = "$it-$ajpure"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("q", e.message)
            toast(e.message.toString())
        }
    }


    private fun showBarItem(bi: BarItem) {
        lblmodel.text = bi.model
        lbltotal.text = bi.total.toString()
    }

    private val refreshScreen = {
        val bi = db.unplanGet(this.bar, this.orderno)
        showBarItem(bi)
        val checked = db.unplanGetChecked(this.orderno)
        title = "临时：${this.orderno}（$checked）"

        txtbar.requestFocus() //条码输入框获得输入焦点
    }
}
