//装柜功能

package cn.scanshop.www.a1

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.loading_layout.*
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.util.regex.Pattern


class LoadingActivity : AppCompatActivity() {
    var orderno: String? = null
    var aj: String? = null
    var bar: String? = null
    val db = SQLiteDbHelper(this)

    val option_base_id = 555

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_layout)
//        setSupportActionBar(toolbar)

        initCtrls()
    }

    private fun initCtrls() {
        orderno = intent.extras.getString(SQLiteDbHelper.FN_ORDERNO)
        val unchecked = db.getUnchecked(orderno)
        title = "装柜：$orderno（$unchecked）"

        txtbar.inputType = InputType.TYPE_CLASS_NUMBER
        txtbar.setSelectAllOnFocus(true)

        txtbar.setOnKeyListener { v, k, e ->

            if (k == KeyEvent.KEYCODE_ENTER && e.action == KeyEvent.ACTION_DOWN) {
                txtbar.selectAll()
//                Util.showMessage(this, "条码：${txtbar.text}")

                this.bar = txtbar.text.toString()

                try {

                    if (aj.isNullOrEmpty()) {
                        throw java.lang.Exception("货柜号未填写")
                    }

                    db.exists(this.bar, this.orderno) //如果没有bar+ordero，会抛出异常，被后面的 catch() 捕获

                    var qty = 0

                    val unchecked = db.getUnchecked(this.orderno, this.bar)
                    if (unchecked <= 0) {
                        throw java.lang.Exception("此条码已够数")
                    }

                    val bi = db.get(this.bar, this.orderno)
                    Util.inputDialog(
                        this,
                        "数量（${bi.model}）\r\n${this.bar}",
                        "",
                        "最大剩余数量：$unchecked",
                        InputType.TYPE_CLASS_NUMBER
                    ) {
                        try {
                            qty = Integer.parseInt(it)
                            if (qty >= 1) {
                                db.update(this.bar, db.getAj(this.aj), qty, this.orderno)

//                                HideKeyboard()

                                refreshScreen()

                                if (unchecked <= 0) {
                                    Util.showMessage(this, "本订单已经出货完成")
                                }
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

//                val bi = db.get(bar, orderno)
//                showBarItem(bi) //就算没有产品，也想要刷新页面中的信息
//
//                val unchecked = db.getUnchecked(orderno)
//                title = "装柜出货：$orderno（$unchecked）"
//                refreshScreen() //放在这里会由于 Util.inputDialog() 是异步的，而出现调用时机问题，现移到 Util.inputDialog() 里面了
//
//                if (unchecked <= 0) {
//                    Util.showMessage(this, "本订单已经出货完成")
//                }

                true
            } else {
                false
            }
        }

//        txtaj.setOnKeyListener { v, i, k ->
//            if (k.action == KeyEvent.ACTION_DOWN && k.keyCode == KeyEvent.KEYCODE_ENTER) {
//                if (!txtaj.text.isNullOrEmpty()) {
//                    this.aj = txtaj.text.toString()
//                    txtbar.requestFocus()
//                    txtbar.selectAll()
//                } else {
//                    toast("货柜号不能空白")
//                    Util.alertSound(this)
//                }
//            }
//
//            true
//        }

        txtaj.setOnClickListener() {
            btnaj.callOnClick()
        }

        btnaj.setOnClickListener { v ->
            try {
                val ajs = db.getAjs(this.orderno)
                selector("请选择货柜号", ajs) { d, i ->
                    val aj = ajs[i]
                    if (Pattern.matches("[A-Z]{2,6}[0-9]{5,10}-[A-Z]", aj)) { //货柜号符合规则
                        txtaj.setText(ajs[i])
                        this.aj = ajs[i]
                    } else { //货柜号不符合规则
                        Util.inputDialog(this, "请输入货柜号", "", "举例：BSIU9718737", null, "[A-Z]{2,6}[0-9]{5,10}") {
                            val ajpure = db.getAj(aj) //aj是aj，可能有 - 打头，getAj()将取出纯aj部分
                            db.setAj(ajpure, it, this.orderno)
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

    }

    //隐藏虚拟键盘
    fun HideKeyboard() {
//        val imm = v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        if (imm.isActive) {
//            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0)
//
//        }

//        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            this@LoadingActivity.getCurrentFocus().getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    override fun onBackPressed() {
//        super.onBackPressed() //有这句直接就会退出，没有机会让你选择
        Util.confirmYesNo(this, "退回上层？") {
            if (it == Dialog.BUTTON_POSITIVE) {
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_loading, menu)

//        val ajs = db.getAjOfOrder(this.orderno)
//        for (aj in ajs) {
//            menu.add(aj)
//        }


        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        menu?.clear()

        menuInflater.inflate(R.menu.menu_loading, menu)

        val ajs = db.getAjOfOrder(this.orderno)
        for ((i, aj) in ajs.withIndex()) {
            menu?.add(0, option_base_id + i, 2, aj)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_stat -> { //出货统计
                val intent = Intent(this, StatActivity::class.java)
                intent.putExtra(SQLiteDbHelper.FN_ORDERNO, orderno)
                startActivity(intent)
                true
            }
            R.id.action_qty -> { //修改数量

                try {

                    val hint = db.getAlterHint(orderno, bar, this.aj)

                    Util.inputDialog(
                        this,
                        "修改数量",
                        "",
                        "当前：${hint.second}，最大：${hint.first}",
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

//                try {
//                    val qdview = layoutInflater.inflate(R.layout.qty_alret_layout, null)
//                    val tq = qdview.findViewById<EditText>(R.id.txtqty)
//                    val lh = qdview.findViewById<TextView>(R.id.lblhint)
//                    val hint = db.getAlterHint(orderno, bar, this.aj)
//                    lh.text = "当前：${hint.second}，最大：${hint.first}"
//
//                    val qd = AlertDialog.Builder(this)
//                        .setTitle("修改数量")
//                        .setView(qdview)
//                        .setPositiveButton("确定") { d, i ->
//                            try {
//                                val q = Integer.parseInt(tq.text.toString())
//                                alterqty(q)
//
////                                db.alterQty(this.bar, this.aj, Integer.parseInt(tq.text.toString()), this.orderno)
////
////                                val bi = db.get(bar, orderno)
////                                showBarItem(bi) //就算没有产品，也想要刷新页面中的信息
////
////                                val unchecked = db.getUnchecked(orderno)
////                                title = "装柜出货：$orderno（$unchecked）"
////
////                                Util.showMessage(this, "数量改为：${tq.text}")
//                            } catch (e: java.lang.Exception) {
//                                Log.e("q", e.message.toString())
//                                Util.showMessage(this, e.message.toString())
//                                Util.alertSound(this)
//                            }
//                        }.show()
//
//                    tq.setOnKeyListener { v, i, k ->
//                        if (k.action == KeyEvent.ACTION_DOWN && k.keyCode == KeyEvent.KEYCODE_ENTER) {
//                            try {
//                                val q = Integer.parseInt(tq.text.toString())
//                                alterqty(q)
////                                db.alterQty(this.bar, this.aj, Integer.parseInt(tq.text.toString()), this.orderno)
////
////                                val bi = db.get(bar, orderno)
////                                showBarItem(bi) //就算没有产品，也想要刷新页面中的信息
////
////                                val unchecked = db.getUnchecked(orderno)
////                                title = "装柜出货：$orderno（$unchecked）"
//
//                                qd.dismiss()
//
////                                Util.showMessage(this, "数量改为：${tq.text}")
//                            } catch (e: java.lang.Exception) {
//                                Log.e("q", e.message.toString())
//                                Util.showMessage(this, e.message.toString())
//                            }
//                            true
//                        } else {
//                            false
//                        }
//                    }
//                } catch (e: java.lang.IllegalArgumentException) {
//                    Log.e("q", "请先选好货柜及输入好条码")
//                    Util.showMessage(this, "请先选好货柜及输入好条码")
//                } catch (e: java.lang.Exception) {
//                    Log.e("q", e.message)
//                    Util.showMessage(this, e.message.toString())
//                }


                true
            }
            R.id.action_del -> { //删除本回数
                Util.confirmYesNo(this, "删除本回数？") {

                    if (it == Dialog.BUTTON_POSITIVE) {
                        Util.inputDialog(this, "删除回数（${this.orderno}）", "", "请输入密码", null) { pwd ->
                            if (pwd == "999") {
                                if (it == Dialog.BUTTON_POSITIVE) {
                                    db.delOrder(this.orderno)
                                    title = "装柜：（已删除）"
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
                val intent = Intent(this, StatOfAjActivity::class.java)
                intent.putExtra(SQLiteDbHelper.FN_ORDERNO, orderno)
                intent.putExtra(SQLiteDbHelper.FN_AJCODE, aj)
                startActivityForResult(intent, 999)

                super.onOptionsItemSelected(item)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 999) { //从查询柜号界面回来，可能改了柜名，或删除了柜，要清空柜号选择柜，使用户在输入条码时得到提醒
            val aj = data!!.getStringExtra("aj") //改名或删除后的柜号
            val ajoriginal = data!!.getStringExtra("aj_original") //原始的柜号
            if (this.aj == ajoriginal) { //如果查看了当前柜号的单柜统计信息，
                if (aj == "（已删除）") { //如果查看时删除了柜号，即当前柜号删除了
                    this.aj = ""
                    txtaj.setText("")
                } else { //否则是修改了柜号
                    this.aj = aj
                    txtaj.setText(aj)
                }
            }
        }
    }

    val alterqty = { q: Int ->
        db.alterQty(this.bar, this.aj, q, this.orderno)

//        val bi = db.get(bar, orderno)
//        showBarItem(bi) //就算没有产品，也想要刷新页面中的信息
//
//        val unchecked = db.getUnchecked(orderno)
//        title = "装柜出货：$orderno（$unchecked）"

        refreshScreen()

//        Util.showMessage(this, "数量改为：$q")
        toast("数量改为：$q")

    }

    private fun showBarItem(bi: BarItem) {
        lblord.text = bi.ord.toString()
        lblmodel.text = bi.model
        lblplanning.text = bi.planning.toString()
        lbltotal.text = bi.total.toString()
//        lbla.text = bi.a.toString()
//        lblb.text = bi.b.toString()
//        lblc.text = bi.c.toString()
//        lbld.text = bi.d.toString()
//        lble.text = bi.e.toString()
//        lblf.text = bi.f.toString()
//        lblg.text = bi.g.toString()
//        lblh.text = bi.h.toString()
//        lbli.text = bi.i.toString()
//        lblj.text = bi.j.toString()
        lblunchecked.text = bi.unchecked.toString()
    }

    private val refreshScreen = {
        val bi = db.get(this.bar, this.orderno)
        showBarItem(bi)
        val unchecked = db.getUnchecked(this.orderno)
        title = "装柜：${this.orderno}（$unchecked）"

        txtbar.requestFocus() //条码输入框获得输入焦点
    }
}