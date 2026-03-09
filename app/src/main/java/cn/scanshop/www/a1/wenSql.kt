//package cn.scanshop.www.a1
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import android.os.Handler
//import android.os.Message
//import android.util.Log
//import java.lang.Exception
//
//
////这里是写的工具类
//class wenSql(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, ver: Int) :
//    SQLiteOpenHelper(context, name, factory, ver) {
//    //ord：序号
//    //bar：条码，与 model 一一对应
//    //model：型号
//    //planning：应出货数量
//    //a~j：柜号，按最多10个柜设计
//    val Create_ =
//        "Create table list(id integer primary key autoincrement,bar text, ord int, model text, planning int, a int, b int, c int, d int, e int, f int, g int, h int, i int, j int);"//SQL语句
//    val mContext = context
//    var marks = arrayListOf<Map<String, Any>>()//存放数据
//    override fun onCreate(db: SQLiteDatabase?) {
//        try {
//            db!!.execSQL(Create_)
//            // Toast.makeText(mContext, "初始化完成！", Toast.LENGTH_SHORT).show()
//        } catch (e: NullPointerException) {
//            println(e)
//        }
//
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
//
//    fun wen_add(bar: String, aj: String, qty: Int) { //将bar装入aj柜。aj=['a'...'j']
//        try {
//            var cv = ContentValues()
//            cv.put("bar", bar)
//            cv.put(aj, qty)
//        } catch (e: Excepti   on) {
//            var msg = Message()
//            msg.what = 0
//            hand.sendMessage(msg)//通知UI线程刷新数据
//        }
//
//    }
//
//    fun wen_add(title: String, url: String, db: SQLiteDatabase, hand: Handler): Boolean {
//        try {
//            var cv = ContentValues()
//            cv.put("title", title)
//            cv.put("url", url)
//            Log.d("添加：", cv.getAsString("title"))
//            Log.d("添加：", cv.getAsString("url"))
//
//            db.insert("wen", null, cv)
//            cv.clear()
//            var msg = Message()
//            msg.what = 2
//            hand.sendMessage(msg)
//            return true
//        } catch (e: NullPointerException) {
//            var msg = Message()
//            msg.what = 0
//            hand.sendMessage(msg)//通知UI线程刷新数据
//            return false
//        }
//    }
//
//    fun wen_delete(sql: String): Boolean {
//        return false
//    }
//
//    fun wen_query(id: String, db: SQLiteDatabase, hand: Handler): Boolean {
//        var cursor = db.query("wen", null, null, null, null, null, null, null)
//        if (cursor.moveToFirst()) {
//            do {
//                var temMAp = linkedMapOf<String, Any>()
//                var title = cursor.getString(cursor.getColumnIndex("title"))
//                var url = cursor.getString(cursor.getColumnIndex("url"))
//                temMAp.put("title", title)
//                temMAp.put("url", url)
//                marks.update(temMAp)
//                // println(temMAp)
//                //temMAp.clear()
//            } while (cursor.moveToNext())
//            println(marks)
//            val msg = Message()
//            msg.what = 1
//            hand.sendMessage(msg)
//            cursor.close()
//        }
//        return false
//    }
//
//    fun wen_update(sql: String): Boolean {
//        return false
//    }
//}
