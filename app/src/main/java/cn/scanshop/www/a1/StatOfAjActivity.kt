//查看一个柜的统计信息

package cn.scanshop.www.a1

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import cn.scanshop.www.a1.Util.Companion.getBitmapFormUri
import kotlinx.android.synthetic.main.loading_statistics_layout.*
import org.jetbrains.anko.*
import java.io.File


/**
 * 单柜统计
 */
class StatOfAjActivity : AppCompatActivity() {

    companion object {
        const val TAKE_PHOTO = 1
        const val TAKE_PHOTO2 = 2
    }

    private var adapter: StatOfAjAdapter? = null
    private var orderno: String? = null
    private var ajcode: String? = null
    private var rowcount: Int = 0

    private var ajcode_orginal: String? = null //原本的柜号

//    private var ajcode_changed = ajcode //改名，或删除后的柜号

    private var photouri: Uri? = null
    private var photouri2: Uri? = null
    private var _imageview: ImageView? = null
    private var _imageview2: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_statistics_layout)

        initCtrls()
    }

    private val ORDERBY_SCAN = "scan" //按扫描顺序排
    private val ORDERBY_ORDER = "ord" //按序号排
    private fun queryStatOfAj(orderby: String) {
        this.orderno = intent.extras.getString(SQLiteDbHelper.FN_ORDERNO)
        this.ajcode = intent.extras.getString(SQLiteDbHelper.FN_AJCODE)
        this.ajcode_orginal = this.ajcode
        val db = SQLiteDbHelper(this)
        val baritemsofaj = db.getStatsOfAj(orderno, ajcode, orderby)
        this.rowcount = baritemsofaj.count()
        adapter = StatOfAjAdapter(baritemsofaj, this, this.orderno!!, this.ajcode!!)
        lvstat.adapter = adapter

        title = "$ajcode（${this.rowcount}）"
    }

    private fun initCtrls() {
        queryStatOfAj(ORDERBY_SCAN)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_loading_stat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_send -> {

                if (this.rowcount > 0) {

                    val db = SQLiteDbHelper(this)
                    val weight = db.getWeight(orderno, ajcode)
                    val seal = db.getSeal(orderno, ajcode)
                    val photo = db.getPhoto(orderno, ajcode)
                    if (!weight.isNullOrEmpty() && !seal.isNullOrEmpty() && !photo.isNullOrEmpty()) {
                        var intent = Intent()
                        intent.setClass(this, ExportActivity::class.java)
                        intent.putExtra(SQLiteDbHelper.FN_ORDERNO, this.orderno)
                        intent.putExtra(SQLiteDbHelper.FN_AJCODE, this.ajcode)


                        startActivity(intent)
                    } else {
                        toast("柜重 或 封条 或 照片 未填写，不能上传")
                    }
                } else {
                    toast("没有数据需要上传")
                }

                true
            }
            R.id.action_del -> {
                Util.inputDialog(this, "删除柜号", "", "请输入密码", null) {
                    if (it == "999") {
                        Util.confirmYesNo(this, "删除柜号吗？") {
                            if (it == Dialog.BUTTON_POSITIVE) {
                                try {
                                    SQLiteDbHelper(this).delAj(this.orderno, this.ajcode)
//
//                                    ajcode_changed = "（已删除）"
                                    ajcode = "（已删除）"

                                    title = "（已删除）"
                                } catch (e: Exception) {
                                    Log.e("q", e.message.toString())
                                    toast(e.message.toString())
                                }
                            }
                        }
                    } else {
                        this.toast("密码错误")
                    }
                }

                true
            }
            R.id.action_rename -> {
                Util.inputDialog(this, "改柜号（${this.ajcode}）", "", "新柜号", null, SQLiteDbHelper.AJCODE_PATT_NOAJABLE) {

                    Util.confirmYesNo(this, "确认修改吗？") { yesno ->
                        if (yesno == Dialog.BUTTON_POSITIVE) {

                            SQLiteDbHelper(this).renameOrder(this.orderno, this.ajcode, it)
                            this.ajcode = it

                            title = it
                        }
                    }
                }
                true
            }
            R.id.action_weight_seal -> { //柜重和封条
                try {
                    val db = SQLiteDbHelper(this)
                    val w = db.getWeight(this.orderno, this.ajcode)
                    val s = db.getSeal(this.orderno, this.ajcode)
                    val p = db.getPhoto(this.orderno, this.ajcode).toStringWithDefault("")

                    var p1 = ""
                    var p2 = ""
                    if (!p.isNullOrEmpty()) { //如果已经设置过图片就加载

                        val ps = p.split(',') //p里面有两个文件名，用逗号（,）分隔。

                        if (ps.count() == 2) {

                            p1 = ps[0]
                            val file = File(Environment.getExternalStorageDirectory(), ps[0])
                            photouri =
                                FileProvider.getUriForFile(
                                    this,
                                    "cn.scanshop.www.a1.fileprovider",
                                    file
                                )//通过FileProvider创建一个content类型的Uri

                            p2 = ps[1]
                            val file2 = File(Environment.getExternalStorageDirectory(), ps[1])
                            photouri2 =
                                FileProvider.getUriForFile(
                                    this,
                                    "cn.scanshop.www.a1.fileprovider",
                                    file2
                                )//通过FileProvider创建一个content类型的Uri
                        } else if (ps.count() == 1) {
                            p1 = ps[0]
                            val file = File(Environment.getExternalStorageDirectory(), ps[0])
                            photouri =
                                FileProvider.getUriForFile(
                                    this,
                                    "cn.scanshop.www.a1.fileprovider",
                                    file
                                )//通过FileProvider创建一个content类型的Uri
                        }
                    }

                    weightSealDialog(w, s, p1, p2) { weight, seal, uri, uri2 ->
                        val path = "$uri"
                        val path2 = "$uri2"
                        SQLiteDbHelper(this).setWeightSeal(this.orderno, this.ajcode, weight, seal, path, path2)
                    }
                } catch (e: java.lang.Exception) {
                    Log.e("q", e.message)
                    toast(e.message.toString())
                }
                true
            }
            R.id.action_by_scan -> { //按扫描顺序排
                queryStatOfAj(ORDERBY_SCAN)
                true
            }
            R.id.action_by_order -> { //按序号排
                queryStatOfAj(ORDERBY_ORDER)
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        setResult(
            Activity.RESULT_OK,
            intentFor<Any>("aj" to this.ajcode, "aj_original" to this.ajcode_orginal)
        ) //返回改名名，或删除后的柜号（“（已删除）”），以及原始的柜号
        super.onBackPressed()
    }

    /**
     * 填写柜重，封条，两张照片
     */
    fun weightSealDialog(
        currentW: String, //当前柜重
        currentS: String, //当前封条
        currentP: String, //当前照片的路径
        currentP2: String, //当前照片2的路径
        callback: (String, String, String, String) -> Unit
    ) {
        with(this) {

            alert {
                val that = this
                this.title = "输入柜重和封条"
                customView {
                    verticalLayout {

                        var weight: EditText? = null
                        var seal: EditText? = null

                        linearLayout {
                            orientation = LinearLayout.HORIZONTAL

                            textView {
                                text = "柜重"
                            }

                            weight = editText {
                                hint = "请输入柜重"
                                setText(currentW)
//                                inputType = InputType.TYPE_CLASS_NUMBER
                                inputType = InputType.TYPE_CLASS_TEXT
                                selectAll()
                                setSelectAllOnFocus(true)

//                                setOnTouchListener { v, e ->
//                                    selectAll()
//                                    true
//                                }
                            }
                        }

                        linearLayout {
                            orientation = LinearLayout.HORIZONTAL

                            textView {
                                text = "封条"
                            }

                            seal = editText {
                                hint = "请输入封条"
                                setText(currentS)
                                inputType = InputType.TYPE_CLASS_TEXT
                                selectAll()
                                setSelectAllOnFocus(true)

//                                setOnTouchListener { v, e ->
//                                    selectAll()
//                                    true
//                                }
                            }
                        }

                        linearLayout {

                            orientation = LinearLayout.HORIZONTAL

                            _imageview = imageView {
                                val file = File(Environment.getExternalStorageDirectory(), currentP)
                                if (!currentP.isNullOrEmpty() && file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        this.context,
                                        "cn.scanshop.www.a1.fileprovider",
                                        file
                                    )//通过FileProvider创建一个content类型的Uri
                                    val img = getBitmapFormUri(context, uri)
                                    setImageBitmap(img)
                                } else {
                                    setImageDrawable(resources.getDrawable(R.drawable.ic_photo_camera_black_24dp))
                                }

                                setOnClickListener {
                                    clickPhoto()
                                }
                            }.lparams(weight = 1f, width = dip(10)){
//                                minimumWidth = dip(300)

                            }

                            _imageview2 = imageView {
                                val file = File(Environment.getExternalStorageDirectory(), currentP2)
                                if (!currentP2.isNullOrEmpty() && file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        this.context,
                                        "cn.scanshop.www.a1.fileprovider",
                                        file
                                    )//通过FileProvider创建一个content类型的Uri
                                    val img = getBitmapFormUri(context, uri)
                                    setImageBitmap(img)
                                } else {
                                    setImageDrawable(resources.getDrawable(R.drawable.ic_photo_camera_black_24dp))
                                }

                                setOnClickListener {
                                    clickPhoto2()
                                }
                            }.lparams(weight = 1f, width = dip(10)){
//                                minimumWidth = dip(300)
                            }

                        }.lparams(width = ViewGroup.LayoutParams.MATCH_PARENT)

                        positiveButton("确认") {
                            if (!weight!!.text.isNullOrEmpty() && !seal!!.text.isNullOrEmpty() && photouri != null && photouri2 != null) {
                                _imageview = null //释放引用，应该这样才不会出现内存泄漏
                                callback(
                                    weight!!.text.toString(),
                                    seal!!.text.toString(),
                                    photouri!!.pathSegments.takeLast(2).joinToString("") { a -> "/${a}" },
                                    photouri2!!.pathSegments.takeLast(2).joinToString("") { a -> "/${a}" })
                            } else {
                                toast("柜重 或 封条 或 照片 未填写，不能上传")
                            }
                        }

                        negativeButton("取消") {
                            _imageview = null //释放引用，应该这样才不会出现内存泄漏
                        }

                        Keybord.openKeybord2(weight!!, context) //自动显示软键盘
                    }
                }
            }.show()
        }
    }

    fun clickPhoto() {
        val file = File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        photouri =
            FileProvider.getUriForFile(this, "cn.scanshop.www.a1.fileprovider", file)//通过FileProvider创建一个content类型的Uri
        val intent = Intent()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photouri)//将拍取的照片保存到指定URI
//            startActivityForResult(intent, 1006)
        startActivityForResult(intent, TAKE_PHOTO)
    }

    fun clickPhoto2() {
        val file = File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        photouri2 =
            FileProvider.getUriForFile(this, "cn.scanshop.www.a1.fileprovider", file)//通过FileProvider创建一个content类型的Uri
        val intent = Intent()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photouri2)//将拍取的照片保存到指定URI
//            startActivityForResult(intent, 1006)
        startActivityForResult(intent, TAKE_PHOTO2)
    }

//    fun getBitmapFormUri(uri: Uri): Bitmap? {
//        var input = contentResolver.openInputStream(uri)
//
//        //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
//        val onlyBoundsOptions = BitmapFactory.Options()
//        onlyBoundsOptions.inJustDecodeBounds = true//不加载到内存
//        onlyBoundsOptions.inDither = true//optional
//        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565//optional
//        BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
//        input!!.close()
//        val originalWidth = onlyBoundsOptions.outWidth
//        val originalHeight = onlyBoundsOptions.outHeight
//        if (originalWidth == -1 || originalHeight == -1)
//            return null
//
//        //图片分辨率以480x800为标准
//        val hh = 800f//这里设置高度为800f
//        val ww = 480f//这里设置宽度为480f
//        //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
//        var be = 1//be=1表示不缩放
//        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
//            be = (originalWidth / ww).toInt()
//        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
//            be = (originalHeight / hh).toInt()
//        }
//        if (be <= 0)
//            be = 1
//        //比例压缩
//        val bitmapOptions = BitmapFactory.Options()
//        bitmapOptions.inSampleSize = be//设置缩放比例
//        bitmapOptions.inDither = true
//        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
//        input = contentResolver.openInputStream(uri)
//        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
//        input!!.close()
//
//        return compressImage(bitmap)//再进行质量压缩
//    }
//
//    fun compressImage(image: Bitmap?): Bitmap? {
//        val baos = ByteArrayOutputStream()
//        image!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//        var options = 100
//        while (baos.toByteArray().count() / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
//            baos.reset()//重置baos即清空baos
//            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos)//这里压缩options，把压缩后的数据存放到baos中
//            options -= 10//每次都减少10
//            if (options <= 0)
//                break
//        }
//        val isBm = ByteArrayInputStream(baos.toByteArray())//把压缩后的数据baos存放到ByteArrayInputStream中
//        return BitmapFactory.decodeStream(isBm, null, null)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = getBitmapFormUri(this, photouri!!)
                    _imageview?.setImageBitmap(img)
                } else {
//                    photouri = null //拍照时点取消不再清空照片，因为应该不会想要这样操作，拍照时点取消不改变照片就可以了
//                    _imageview?.setImageDrawable(resources.getDrawable(R.drawable.ic_photo_camera_black_24dp))
                }
            }

            TAKE_PHOTO2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = getBitmapFormUri(this, photouri2!!)
                    _imageview2?.setImageBitmap(img)
                } else {
//                    photouri2 = null //拍照时点取消不再清空照片，因为应该不会想要这样操作，拍照时点取消不改变照片就可以了
//                    _imageview2?.setImageDrawable(resources.getDrawable(R.drawable.ic_photo_camera_black_24dp))
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    class StatOfAjAdapter(
        private val list: List<BarItemOfAj>,
        private val ctx: Context,
        private val orderno: String,
        private val ajcode: String
    ) : BaseAdapter() {

        override fun getCount(): Int = list.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItem(position: Int): BarItemOfAj = list[position]

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var view: View
            if (convertView == null) {
                view = View.inflate(ctx, R.layout.loading_stat_item_layout, null)
                viewHolder = ViewHolder(view, this.orderno, this.ajcode)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
            val item = getItem(position)
            if (item is BarItemOfAj) {
                viewHolder.lblord.text = item.ord.toString()
                viewHolder.lblmodel.text = item.model
                viewHolder.lblplanning.text = item.planning.toString()
                viewHolder.lbltotal.text = item.done.toString()
                viewHolder.lblunchecked.text = item.unchecked.toString() //不加as会报错，说分不清 BarItem 和 BarItemOfAj

                val mark = item.mark
                if (mark == 0) { //mark = 0：表示未标记
                    with(viewHolder) {
                        lblord.backgroundColor = Color.WHITE
                        lblmodel.backgroundColor = Color.WHITE
                        lblplanning.backgroundColor = Color.WHITE
                        lbltotal.backgroundColor = Color.WHITE
                        lblunchecked.backgroundColor = Color.WHITE

                        lblord.tag = Color.WHITE
                    }
                } else { //mark = 1：表示标记为黄色
                    with(viewHolder) {
                        lblord.backgroundColor = Color.YELLOW
                        lblmodel.backgroundColor = Color.YELLOW
                        lblplanning.backgroundColor = Color.YELLOW
                        lbltotal.backgroundColor = Color.YELLOW
                        lblunchecked.backgroundColor = Color.YELLOW

                        lblord.tag = Color.YELLOW
                    }
                }
            }

            return view
        }
    }

    class ViewHolder(viewItem: View, val orderno: String, val ajcode: String) {
        var lblord: TextView = viewItem.findViewById(R.id.lblord)
        var lblmodel: TextView = viewItem.findViewById(R.id.lblmodel)
        var lblplanning: TextView = viewItem.findViewById(R.id.lblplanning)
        var lbltotal: TextView = viewItem.findViewById(R.id.lbltotal)
        var lblunchecked: TextView = viewItem.findViewById(R.id.lblunchecked) //差异

        val click = { v: View ->

            if (lblord.tag == Color.YELLOW) {
                lblord.backgroundColor = Color.WHITE
                lblmodel.backgroundColor = Color.WHITE
                lblplanning.backgroundColor = Color.WHITE
                lbltotal.backgroundColor = Color.WHITE
                lblunchecked.backgroundColor = Color.WHITE

                lblord.tag = Color.WHITE

                val model = lblmodel.text.toString()
                val orderno = this.orderno
                val ajcode = this.ajcode
                val db = SQLiteDbHelper(v.context)
                db.mark(model, ajcode, orderno, 0) //mark = 0 表示未标记

            } else {
                lblord.backgroundColor = Color.YELLOW
                lblmodel.backgroundColor = Color.YELLOW
                lblplanning.backgroundColor = Color.YELLOW
                lbltotal.backgroundColor = Color.YELLOW
                lblunchecked.backgroundColor = Color.YELLOW

                lblord.tag = Color.YELLOW

                val model = lblmodel.text.toString()
                val orderno = this.orderno
                val ajcode = this.ajcode
                val db = SQLiteDbHelper(v.context)
                db.mark(model, ajcode, orderno, 1) //mark = 1 表示标记为黄色
            }
        }

        init {
            lblord.setOnClickListener(click)
            lblmodel.setOnClickListener(click)
            lblplanning.setOnClickListener(click)
            lbltotal.setOnClickListener(click)
            lblunchecked.setOnClickListener(click)

            lblord.tag = Color.WHITE //用序号列作为标记颜色的记录器
        }
    }
}