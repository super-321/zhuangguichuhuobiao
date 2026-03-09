package cn.scanshop.www.a1

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    //    val FLAG_HOMEKEY_DISPATCHED = -0x80000000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        initCtrl()
    }

    private fun requestSdcardPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                Util.confirmYesNo(this, "允许程序读写存储器吗？") {
                    if (it == Dialog.BUTTON_POSITIVE) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ), 1
                        )
                    }
                }
            }
        }
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Util.confirmYesNo(this, "允许程序拍照吗？") {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.CAMERA
                        ), 1
                    )
                }
            }
        }
    }

    private fun initCtrl() {
//        btnloading.setOnClickListener {
//            try {
//                val db = SQLiteDbHelper(this)
//                db.checkProgress2()
//            } catch (e: Exception) {
//                Log.e("q", e.message)
//                Util.showMessage(this, e.message!!)
//            }
////            val p = getApplicationContext().getFilesDir().getAbsolutePath()
////            Toast.makeText(this, p, Toast.LENGTH_SHORT).show()
//        }

        btnloading.setOnClickListener {
            try {
                val intent = Intent(this, OrderSelectionActivity::class.java)
//                intent.setClass(this, OrderSelectionActivity::class.java)
//                startActivity(intent)
                startActivityForResult(intent, ActivityEnum.OrderSelection.ordinal)

            } catch (e: Exception) {
                Log.e("q", e.message)
                Util.showMessage(this, e.message!!)
            }
        }

        btnmock.setOnClickListener {
            btnmockClick()
        }


        btnabout.setOnClickListener {
            btnaboutClick()
        }

        btnexport.setOnClickListener {
            //这个按钮是visible=gone，所以是没用的。导出的代码里没有做导出全部数据的功能，所以如果打开这个按钮应该会出错
            btnexportClick()
        }

        btnimport.setOnClickListener {
            btnimportClick()
        }

        btnunplan.setOnClickListener {
            btnunplanClick()
        }

        btnunplanimport.setOnClickListener {
            btnunplanimportClick()
        }

        requestSdcardPermission()
        requestCameraPermission()
    }


    private val btnmockClick = {
        try {
            Util.confirmYesNo(this, "是否要清空数据库，并生成测试数据？") { yesno ->
                if (yesno == Dialog.BUTTON_POSITIVE) {
                    val db = SQLiteDbHelper(this)

                    db.mockInitList()

                    Log.d("q", "db.mockInitList() >")
                    Log.d("q", "mockInitList() ok")
                    Util.showMessage(this, "mockInitList() ok")
                }
            }
        } catch (e: Exception) {
            Log.e("q", e.message)
        }
    }

    /**
     * 临时调货扫描
     */
    private val btnunplanClick = {
        try {
            val intent = Intent(this, UnplanOrderSelectionActivity::class.java)
            startActivityForResult(intent, ActivityEnum.UnplanOrderSelecton.ordinal)
        } catch (e: Exception) {
            Log.e("q", e.message)
            Util.showMessage(this, e.message!!)
        }
    }

    private val btnexportClick = {
        try {
            val intent = Intent(this, ExportActivity::class.java)
//                intent.setClass(this, ExportActivity::class.java)
            startActivity(intent)

        } catch (e: Exception) {
            Util.showMessage(this, e.message!!)
        }
    }

    private val btnimportClick = {
        try {
            val intent = Intent(this, ImportActivity::class.java)
//                intent.setClass(this, ExportActivity::class.java)
            startActivity(intent)

        } catch (e: Exception) {
            Util.showMessage(this, e.message!!)
        }
    }

    private val btnaboutClick = {
        try {
            val intent = Intent(this, AboutActivity::class.java)
//                intent.setClass(this, ExportActivity::class.java)
            startActivity(intent)

        } catch (e: Exception) {
            Util.showMessage(this, e.message!!)
        }
    }

    private val btnunplanimportClick = {
        try {
            val intent = Intent(this, UnplanImportActivity::class.java)
//                intent.setClass(this, ExportActivity::class.java)
            startActivity(intent)

        } catch (e: Exception) {
            Util.showMessage(this, e.message!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ActivityEnum.OrderSelection.ordinal) {
            try {
                var orderno = data!!.getStringExtra(SQLiteDbHelper.FN_ORDERNO)
//                Util.showMessage(this, "选择了：${orderno!!}") //提示当前的回数

                val bundle = Bundle()
                bundle.putString(
                    SQLiteDbHelper.FN_ORDERNO,
                    orderno!!
                ) //orderno!!确保orderno不是null值，这样处理流程可能比较简单些
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtras(bundle)
                startActivityForResult(intent, ActivityEnum.Check.ordinal)

            } catch (e: KotlinNullPointerException) {
                Util.showMessage(this, "没选订单")
            } catch (e: Exception) {
                Log.v("q", e.message.toString())
                Util.showMessage(this, e.message.toString())
            }
        } else if (requestCode == ActivityEnum.UnplanOrderSelecton.ordinal) {
            try {
                var orderno = data!!.getStringExtra(SQLiteDbHelper.FN_ORDERNO)

                val bundle = Bundle()
                bundle.putString(SQLiteDbHelper.FN_ORDERNO, orderno!!)
                val intent = Intent(this, UnplanLodingActivity::class.java)
                intent.putExtras(bundle)
                startActivityForResult(intent, ActivityEnum.UnplanCheck.ordinal)
            } catch (e: KotlinNullPointerException) {
                toast(e.message.toString())
            } catch (e: java.lang.Exception) {
                Log.v("q", e.message.toString())
                toast(e.message.toString())
            }
        } else if (requestCode == ActivityEnum.Rereg.ordinal && resultCode == RegActivity.OK) {
            toast("注册成功")
        }
    }


    override fun onBackPressed() {
//        super.onBackPressed() //有这句直接就会退出，没有机会让你选择
        Util.confirmYesNo(this, "退出程序？") {
            if (it == Dialog.BUTTON_POSITIVE) {
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(cn.scanshop.www.a1.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        val handler = CoroutineExceptionHandler { _, exception ->
            //协程的异常处理器
            Log.e("q-soft", "$exception")
            toastOnUi("$exception")
        }

        return when (item.itemId) {
            R.id.action_mock -> {
                Util.inputDialog(this, "输入密码", "", "请输入密码", null, "999") {
                    if (it == "999") {
                        try {
                            Util.confirmYesNo(this@MainActivity, "是否要清空数据库，并生成测试数据？") { yesno ->
                                if (yesno == Dialog.BUTTON_POSITIVE) {
                                    indeterminateProgressDialog("正在生成测试数据...") {
                                        lifecycleScope.launch(handler + Dispatchers.IO) {
                                            val db = SQLiteDbHelper(this@MainActivity)
                                            db.mockInitList()
                                            dismiss()
                                            toastOnUi("测试数据生成完成")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("q", e.message)
                        }
                    } else {
                        toast("密码错误")
                    }
                }
                true
            }
            R.id.action_backup -> {
                Util.inputDialog(this, "备份数据", "", "请输入密码", null) {
                    if (it == "999") {
                        indeterminateProgressDialog("正在备份...") {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val backupok = SQLiteDbHelper.backup()
                                if (backupok) {
                                    val data = Uri.parse("file:///sdcard/Download/a1-backup.zip")
                                    sendBroadcast(
                                        Intent(
                                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                            data
                                        )
                                    ) //使电脑可以刷新一下就看到新生成的文件
                                    this@indeterminateProgressDialog.dismiss()
                                    toastOnUi("备份到：${SQLiteDbHelper.BACKUP_ZIP}")
                                } else {
                                    this@indeterminateProgressDialog.dismiss()
                                    toastOnUi("备份失败")
                                }
                            }
                        }.setCancelable(false)
                    } else {
                        toast("密码错误")
                    }
                }

                true
            }
            R.id.action_restore -> {
                Util.inputDialog(this, "恢复数据", "", "请输入密码", null) {
                    if (it == "999") {
                        indeterminateProgressDialog("正在恢复数据...") {
                            lifecycleScope.launch(Dispatchers.IO) {
                                if (SQLiteDbHelper.restore()) {
                                    val data =
                                        Uri.parse("file:///sdcard/restore_temp/a1-backup.bin")
                                    sendBroadcast(
                                        Intent(
                                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                            data
                                        )
                                    ) //使电脑可以刷新一下就看到新生成的文件

                                    dismiss()
                                    runOnUiThread { toast("数据已恢复") }
                                } else {
                                    dismiss()
                                    runOnUiThread { toast("恢复失败") }
                                }
                            }
                        }

                    } else {
                        toast("密码错误")
                    }
                }
                true
            }
            R.id.action_name -> {
                val name = Util.readConfig(this, "name")
                Util.inputDialog(this, "输入机器名称", name, null, null) {
                    Util.saveConfig(this, "name", it)
                }
                true
            }
            R.id.action_delphoto -> {
                Util.inputDialog(this, "清除照片", "", "请输入密码", null) {
                    if (it == "999") {
                        indeterminateProgressDialog("正在清除照片...") {
                            lifecycleScope.launch(Dispatchers.IO)
                            {
                                try {
                                    val photodir =
                                        "${Environment.getExternalStorageDirectory()}/temp/" //FIXME：应该用一个统一的对象来存放这个路径
                                    Util.deleteDirectoryFiles(photodir)
                                    toastOnUi("清除完成")
                                    dismiss()
                                } catch (e: Exception) {
                                    toastOnUi("清除出错：${e.message.toString()}")
                                    dismiss()
                                }
                            }
                        }
                    } else {
                        toast("密码错误")
                    }
                }
                true
            }
            R.id.action_reg -> {
                startActivityForResult(
                    intentFor<RegActivity>(
                        Pair(
                            RegActivity.REG_REREG,
                            true
                        )
                    ), ActivityEnum.Rereg.ordinal
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    enum class ActivityEnum(val value: Int) {
        OrderSelection(0),
        Check(1),
        UnplanOrderSelecton(2),
        UnplanCheck(3),
        Rereg(4)
    }

}