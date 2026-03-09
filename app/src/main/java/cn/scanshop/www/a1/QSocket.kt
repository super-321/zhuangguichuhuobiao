import android.content.Context
import android.os.Environment
import android.util.Log
import cn.scanshop.www.a1.SQLiteDbHelper
import cn.scanshop.www.a1.Util
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.regex.Pattern

object QSocket {
    val port: Int = 10086

    //    val timeout: Int = 20000
    val timeout: Int = 2000

    val buff_size: Int = 8000
    val ack_cmd: String = "{ACK}"
    val send_cmd: String = "{SEND}"
    val receive_cmd: String = "{RECEIVE}"
    val pause_cmd: String = "{PAUSE}"
    val receive_bars_cmd: String = "{RECEIVE_BARS}"

    var ecallback = { context: Context, e: java.lang.Exception ->
        //        Looper.prepare()
//        Util.showMessage(context, e.message.toString())
//        Looper.loop()

        context.runOnUiThread { toast(e.message.toString()) }

        Log.e("q:ecallback()", e.message)
        false
    } //初始异常回调


//    enum class Status(val status: Int) {
//        OK(0),
//        NG(1)
//    }

//    val handler = Handler { //这种方式不太好传 context 进去，所以没使用。应该在这里面使用UI是不会有问题的
//
    //        it.what == Status.NG.ordinal && throw it.obj as Throwable //从 run 中传递过来的异常对象，向外抛出
//        it.what == Status.NG.ordinal &&
//                ecallback(it.obj as Exception)

//        if (it.what == Status.NG.ordinal) {
//            throw it.obj as Throwable //从 run 中传递过来的异常对象，向外抛出
//            true
//        } else {
//            false
//        }
//    }

//    override fun run() {
//        try {
//            val socket = Socket()
//            val sa: SocketAddress = InetSocketAddress(host, port)
//            socket.connect(sa, timeout)
//            val os = socket.getOutputStream()
//            val pw = PrintWriter(os)
//            pw.print(content)
//            pw.flush()
//            pw.close()
//        } catch (e: Exception) {
//            Log.e("q", e.message)
//            //throw e //这是在另一个线程上运行的，抛出异常会收不到，
////            val msg = Message()
////            msg.obj = e
////            msg.what = Status.NG.ordinal
////            handler.sendMessage(msg)
//
//            ecallback(e)
//        }
//    }

    fun send(
        host: String,
        lines: List<String>,
        context: Context,
        progress: (p: Int, m: Int) -> Unit
    ) {
        try {
            Thread { sendThread(host, lines, context, progress) }.start()
        } catch (e: Exception) {
            Log.e("q", e.message.toString())
            Util.showMessage(context, e.message.toString())
        }
    }

    fun send2(
        host: String,
        lines: List<String>,
        orderno: String,
        ajcode: String,
        name: String,
        context: Context,
        progress: (p: Int, m: Int) -> Unit
    ) {

        GlobalScope.launch {

            try {
                val socket = Socket()
                val sa: SocketAddress = InetSocketAddress(host, port)
                socket.connect(sa, timeout)

                val os = socket.getOutputStream()
                val ins = socket.getInputStream()

                sendString(os, send_cmd) //请求发送数据
                val reply = receiveString(ins) //对方回应
                if (reply == pause_cmd) {
                    context.runOnUiThread { toast("暂停通讯，可能服务器在更新数据") }
                    return@launch
                }

//                val ajcodes = db.ajcodes.joinToString(SQLiteDbHelper.LINE_SEP)
//                sendString(os, ajcodes) //发送aj全名清单
//                receiveString(ins) //接收到回应，表示可以继续通讯

                val orderaj = "$orderno---$ajcode---$name"
                sendString(os, orderaj) //发送aj全名清单
                receiveString(ins) //接收到回应，表示可以继续通讯

                val db = SQLiteDbHelper(context)
                val weight = db.getWeight(orderno, ajcode)
                val seal = db.getSeal(orderno, ajcode)
                val ws = "$weight---$seal"
                sendString(os, ws) //发送柜重、封条
                receiveString(ins) //接收到回应，表示可以继续通讯

                //读取照片全路径
                val photo = db.getPhoto(orderno, ajcode)
                val (p1, p2) = photo.split(",") //因为操作的时候要求拍两张照片，所以这里假设是有两张照片的

                //发送照片
                val file = File(Environment.getExternalStorageDirectory(), p1) //拼出照片文件的路径
                var bytes: ByteArray = byteArrayOf()
                if (file.exists()) {
                    bytes = file.readBytes() //读取照片的字节数组
                } //else：bytes为空数组，上传逻辑不变
                sendBinary(os, bytes) //发送照片的二进制数据
                receiveString(ins) //接收到回应，表示可以继续通讯

                //发送照片2
                val file2 = File(Environment.getExternalStorageDirectory(), p2) //拼出照片文件的路径
                var bytes2: ByteArray = byteArrayOf()
                if (file2.exists()) {
                    bytes2 = file2.readBytes() //读取照片2的字节数组
                } //else：bytes为空数组，上传逻辑不变
                sendBinary(os, bytes2) //发送照片2的二进制数据
                receiveString(ins) //接收到回应，表示可以继续通讯

                sendString(os, "{${lines.count()}}") //发送即将传送的数据的行数

                for ((i, line) in lines.withIndex()) {
                    sendString(os, line)
                    Log.d("q", line)
                    progress(i + 1, lines.count())
                    sleep(100)
                }

                os.close()
                ins.close()
                socket.close()
            } catch (e: Exception) {
                Log.e("q", e.message)
                ecallback(context, e)
            }
        }
    }


    fun unplanSend2(
        host: String,
        lines: List<String>,
        orderno: String,
        ajcode: String,
        name: String,
        alias: String,
        context: Context,
        progress: (p: Int, m: Int) -> Unit
    ) {

        GlobalScope.launch {

            try {
                val socket = Socket()
                val sa: SocketAddress = InetSocketAddress(host, port)
                socket.connect(sa, timeout)

                val os = socket.getOutputStream()
                val ins = socket.getInputStream()

                sendString(os, send_cmd) //请求发送数据
                val reply = receiveString(ins) //对方回应
                if (reply == pause_cmd) {
                    context.runOnUiThread { toast("暂停通讯，可能服务器在更新数据") }
                    return@launch
                }

//                val ajcodes = db.ajcodes.joinToString(SQLiteDbHelper.LINE_SEP)
//                sendString(os, ajcodes) //发送aj全名清单
//                receiveString(ins) //接收到回应，表示可以继续通讯

                val orderaj = "$alias---$ajcode---$name"
                sendString(os, orderaj) //发送aj全名清单
                receiveString(ins) //接收到回应，表示可以继续通讯

                val db = SQLiteDbHelper(context)
                val weight = db.unplanGetWeight(orderno, ajcode)
                val seal = db.unplanGetSeal(orderno, ajcode)
                val ws = "$weight---$seal"
                sendString(os, ws) //发送柜重、封条
                receiveString(ins) //接收到回应，表示可以继续通讯

                //读取照片全路径
                val photo = db.unplanGetPhoto(orderno, ajcode)
                val (p1, p2) = photo.split(",") //因为操作的时候要求拍两张照片，所以这里假设是有两张照片的

                //发送照片
                val file = File(Environment.getExternalStorageDirectory(), p1) //拼出照片文件的路径
                var bytes: ByteArray = byteArrayOf()
                if (file.exists()) {
                    bytes = file.readBytes() //读取照片的字节数组
                } //else：bytes为空数组，上传逻辑不变
                sendBinary(os, bytes) //发送照片的二进制数据
                receiveString(ins) //接收到回应，表示可以继续通讯

                //发送照片2
                val file2 = File(Environment.getExternalStorageDirectory(), p2) //拼出照片文件的路径
                var bytes2: ByteArray = byteArrayOf()
                if (file2.exists()) {
                    bytes2 = file2.readBytes() //读取照片2的字节数组
                } //else：bytes为空数组，上传逻辑不变
                sendBinary(os, bytes2) //发送照片2的二进制数据
                receiveString(ins) //接收到回应，表示可以继续通讯

                sendString(os, "{${lines.count()}}") //发送即将传送的数据的行数

                for ((i, line) in lines.withIndex()) {
                    sendString(os, line)
                    Log.d("q", line)
                    progress(i + 1, lines.count())
                    sleep(100)
                }

                os.close()
                ins.close()
                socket.close()
            } catch (e: Exception) {
                Log.e("q", e.message)
                ecallback(context, e)
            }
        }
    }

    private val sendThread =
        { host: String, lines: List<String>, context: Context, progress: (p: Int, m: Int) -> Unit ->
            try {
                val linecount = lines.count()

                val socket = Socket()
                val sa: SocketAddress = InetSocketAddress(host, port)
                socket.connect(sa, timeout)
                val os = socket.getOutputStream()
                val pw = PrintWriter(os)

                for ((i, line) in lines.withIndex()) {
                    pw.print(line + "\r")
                    pw.flush()
                    Log.d("q", line)
                    progress(i + 1, linecount)
                    sleep(100)
                }
//            pw.print(lines)
////            pw.flush()
                pw.close()
            } catch (e: Exception) {
                Log.e("q", e.message)
                //throw e //这是在另一个线程上运行的，抛出异常会收不到，
//            val msg = Message()
//            msg.obj = e
//            msg.what = Status.NG.ordinal
//            handler.sendMessage(msg)

                ecallback(context, e)
            }
        }

    fun receive(host: String, context: Context, progress: (p: Int, result: String?) -> Unit) {
        try {
            Thread { receiveThread(host, context, progress) }.start()
        } catch (e: Exception) {
            Log.e("q", e.message.toString())
            Util.showMessage(context, e.message.toString())
        }
    }

    private fun sendInt(stream: OutputStream, value: Int) {
        stream.write(value)
    }

    /**
     * 发送字串
     */
    private fun sendString(stream: OutputStream, content: String) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val nbytes = bytes.count()
        stream.write(Util.toLH(nbytes)) //传送即将发送的字节数
//        stream.flush()
        stream.write(bytes)
//        stream.flush()
//
//        val pw = PrintWriter(stream)
//        pw.println(content)
    }

    /**
     * 发送二进制数据
     */
    private fun sendBinary(stream: OutputStream, content: ByteArray) {
        val nbytes = content.count()
        stream.write(Util.toLH(nbytes)) //即将发送的字节数
        stream.write(content)
    }

//    private fun convertFourUnSignLong(byteArray: ByteArray): Long =
//        ((byteArray[3].toInt() and 0xFF) shl 24 or (byteArray[2].toInt() and 0xFF) shl 16 or (byteArray[1].toInt() and 0xFF) shl 8 or (byteArray[0].toInt() and 0xFF)).toLong()


    private fun receiveString(stream: InputStream): String {
        val accu = ArrayList<Byte>()

        val bytes = ByteArray(buff_size)
        val nn = stream.read(bytes, 0, Int.SIZE_BYTES) //收到即将发送过来的字节数

//        val ns = String(bytes, 0, nn, Charsets.UTF_8)
//        val size = Integer.parseInt(ns)
        val size = Util.convertFourUnSignLong(bytes)

        while (true) {
            if (accu.count() < size) {
                val n = stream.read(bytes, 0, size.toInt())
                accu.addAll(bytes.take(n))
            } else {
                break
            }
        }

        return String(accu.toByteArray(), 0, accu.toByteArray().count(), charset("UTF-8"))
    }

    private fun receiveCnt(stream: InputStream): Int {
        val s = receiveString(stream)

        Log.d("q", "receiveCnt()=$s")

        val p = Pattern.compile("[{](\\d+)[}]")
        val m = p.matcher(s)
        m.find() //不调用find()后面的group()不会有结果
        val cnt = m.group(1)
        return Integer.parseInt(cnt)
    }

    private fun parseCnt(s: String): Int {
        Log.d("q", "parseCnt()=$s")

        val p = Pattern.compile("[{](\\d+)[}]")
        val m = p.matcher(s)
        m.find() //不调用find()后面的group()不会有结果
        val ns = m.group(1)
        return Integer.parseInt(ns)
    }

    private fun sendExistingOrders(stream: OutputStream, context: Context): Unit {
        val db = SQLiteDbHelper(context) //发送本机已有的回数，
        val orders = db.orders.joinToString(SQLiteDbHelper.FLD_SEP)
        sendString(stream, orders)
    }

    fun receive2(
        host: String,
        context: Context,
        progress: (p: Int, m: Int, result: ArrayList<String>) -> Unit
    ) {
        GlobalScope.launch {
            try {
                val socket = Socket()
                val sa: SocketAddress = InetSocketAddress(host, port)
                socket.connect(sa, timeout)

                val ins = socket.getInputStream()
                val ous = socket.getOutputStream()

                sendString(ous, receive_cmd) //请求接收数据

                val reply = receiveString(ins)
                if (reply == pause_cmd) { //处于暂停通讯状态，
                    context.runOnUiThread { toast("暂停通讯，可能服务器在更新数据") }
                    return@launch
                }

                sendExistingOrders(ous, context) //发送本机已有的回数，对方发过来的数据中将不会包含这些回号

                val cnt = receiveCnt(ins)
                if (cnt > 0) {
                    val accu = ArrayList<String>()
                    for (i in 1..cnt) {
                        val s = receiveString(ins)
                        accu.add(s)
                        progress(i, cnt, accu)

                        Log.d("q", "i=$i , $s")
                    }

                    ins.close()
                    ous.close()
                    socket.close()
                } else {
                    context.runOnUiThread { context.toast("下载完成") } //应该调用progress()来处理比较好，但result参数传入空值有点问题
                }
            } catch (e: Exception) {
                Log.e("q", e.message)
                ecallback(context, e)
            }
        }
    }

    fun receive3(
        host: String,
        context: Context,
        progress: (p: Int, m: Int, result: ArrayList<String>) -> Unit
    ) {
        GlobalScope.launch {
            try {
                val socket = Socket()
                val sa: SocketAddress = InetSocketAddress(host, port)
                socket.connect(sa, timeout)

                val ins = socket.getInputStream()
                val ous = socket.getOutputStream()

                sendString(ous, receive_bars_cmd) //请求接收数据

                val reply = receiveString(ins)
                if (reply == pause_cmd) { //处于暂停通讯状态，
                    context.runOnUiThread { toast("暂停通讯，可能服务器在更新数据") }
                    return@launch
                }

                val cnt = receiveCnt(ins)

                sendString(ous, ack_cmd) //回应电脑

                if (cnt > 0) {
                    val accu = ArrayList<String>()
                    for (i in 1..cnt) {
                        val s = receiveString(ins)
                        accu.add(s)
                        progress(i, cnt, accu)

                        Log.d("q", "i=$i , $s")
                    }

                    ins.close()
                    ous.close()
                    socket.close()
                } else {
                    context.runOnUiThread { context.toast("下载完成") } //应该调用progress()来处理比较好，但result参数传入空值有点问题
                }
            } catch (e: Exception) {
                Log.e("q", e.message)
                ecallback(context, e)
            }
        }
    }

    private val receiveThread =
        { host: String, context: Context, progress: (p: Int, result: String?) -> Unit ->
            try {
                val socket = Socket()
                val sa: SocketAddress = InetSocketAddress(host, port)
                socket.connect(sa, timeout)
                val ins = socket.getInputStream()

                val bytelist = ArrayList<Byte>()
                var naccu = 0
                while (true) {
                    val buff = ByteArray(8000)
                    val n = ins.read(buff)
                    if (n == -1) {
                        val received = String(
                            bytelist.toByteArray(),
                            0,
                            bytelist.toByteArray().count(),
                            charset("UTF-8")
                        )
                        progress(-1, received) //用-1调用会提示 接收结束；received是接收到的所有內容
                        break //n==-1表示没有数据了，这时跳出while()
                    }

                    bytelist.addAll(buff.copyOf(n).asList())
                    naccu += n
                    val partial = String(buff.copyOf(n), 0, n, charset("UTF-8")) //本次循环接收到的数据
                    progress(naccu, partial)

                    Log.d("qq", "n=${n} , ${String(buff, 0, n, charset("UTF-8"))}")
                }
            } catch (e: Exception) {
                Log.e("q", e.message)
                ecallback(context, e)
            }
        }
}