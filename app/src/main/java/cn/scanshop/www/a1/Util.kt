package cn.scanshop.www.a1

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.blankj.utilcode.util.FileUtils
import org.jetbrains.anko.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class Util {
    companion object {
        fun showMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun confirmYesNo(context: Context, title: String, callback: (Int) -> Unit) {
            AlertDialog.Builder(context).setTitle(title)
                .setPositiveButton("是") { _, _ ->
                    callback(Dialog.BUTTON_POSITIVE)
                }
                .setNegativeButton("否") { _, _ ->
                    callback(Dialog.BUTTON_NEGATIVE)
                }.show()
        }

        fun inputDialog(
            context: Context,
            title: String,
            message: String?,
            hint: String?,
            inputMethod: Int?,
            pattern: String? = null,
            callback: (String) -> Unit
        ) {
            with(context) {

                alert {
                    val that = this
                    this.title = title
                    customView {
                        verticalLayout {

                            val pass = editText {
                                this.hint = hint
                                setText(message ?: "")
                                padding = dip(20)

                                if (inputMethod != null) {
                                    inputType = inputMethod
                                }
//                                requestFocus()
//                                Keybord.openKeybord2(this, context) //自动显示软键盘
                            }

//                            pass.requestFocus()


                            positiveButton("确认") {

                                if (!pattern.isNullOrEmpty() && Pattern.matches(
                                        pattern,
                                        pass.text.toString()
                                    ) || pattern.isNullOrEmpty()
                                ) { //输入的内容符合格式要求，或没有格式要求
                                    callback(pass.text.toString())
                                } else { //有格式要求，且输入的内容不符合格式要求
                                    toast("输入格式不正确")
                                }
                            }

                            that.onKeyPressed { dialog, keyCode, e ->
                                if (keyCode == KeyEvent.KEYCODE_ENTER && e.action == KeyEvent.ACTION_DOWN) {
                                    if (!pattern.isNullOrEmpty() && Pattern.matches(
                                            pattern,
                                            pass.text.toString()
                                        ) || pattern.isNullOrEmpty()
                                    ) { //输入的内容符合格式要求，或没有格式要求
                                        callback(pass.text.toString())
                                        dialog.dismiss()
                                    } else { //有格式要求，且输入的内容不符合格式要求
                                        toast("输入格式不正确")
                                    }

                                    true
                                } else {
                                    false
                                }
//                                true
                            }


                            Keybord.openKeybord2(pass, context) //自动显示软键盘

//                            val timer = Timer() //用timer有时会跳出，可能是因为超过了show()的线程，到了其他线程中时才执行openKeyboard2()，反而不用timer比较正常
//                            timer.schedule(timerTask {
//                                Keybord.openKeybord2(pass, context) //自动显示软键盘
//                            }, 300)


                        }
                    }


                }.show()
            }
        }

        fun alertSound(context: Context) {
            val soundPool = SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
            val id = soundPool.load(context, R.raw.alert, 1);
//            soundPool.play( //这样写会没有声音，因为load()是个异步函数，所以没有加载完就会来到这里
//                id,
//                1F,
//                1F,
//                1,
//                1,
//                1F
//            );

            soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
                //用这种回调的方式就可以
                soundPool.play(
                    sampleId, 1f, 1f, 1, 0, 1f
                )
            }
        }

        fun saveConfig(context: Context, key: String, value: String) {
            val sps = context.getSharedPreferences("share", Context.MODE_PRIVATE)
            val editor = sps.edit()
            editor.putString(key, value)
            editor.commit()
        }

        fun readConfig(context: Context, key: String): String {
            val sps = context.getSharedPreferences("share", Context.MODE_PRIVATE)
            return sps.getString(key, "")
        }

        fun joinString(strs: List<String>, sep: String): String {
            return strs.joinToString(separator = sep)
        }

        fun int2Byte(intValue: Int): ByteArray {
            val b = ByteArray(4)
            for (i in 0..3) {
                b[i] = (intValue shr 8 * (3 - i) and 0xFF).toByte()
                //System.out.print(Integer.toBinaryString(b[i])+" ");
                //System.out.print((b[i] & 0xFF) + " ");
            }
            return b
        }

        fun convertFourUnSignLong(byteArray: ByteArray): Long =
            ((byteArray[3].toInt() and 0xFF) shl 24 or (byteArray[2].toInt() and 0xFF) shl 16 or (byteArray[1].toInt() and 0xFF) shl 8 or (byteArray[0].toInt() and 0xFF)).toLong()


        public fun toHH(n: Int): ByteArray {
            val b = ByteArray(4)
            b[3] = (n and 0xFF).toByte()
            b[2] = (n shr 8 and 0xff).toByte()
            b[1] = (n shr 16 and 0xff).toByte()
            b[0] = (n shr 24 and 0xff).toByte()
            return b;
        }

        public fun toLH(n: Int): ByteArray {
            val b = ByteArray(4)
            b[0] = (n and 0xFF).toByte()
            b[1] = (n shr 8 and 0xff).toByte()
            b[2] = (n shr 16 and 0xff).toByte()
            b[3] = (n shr 24 and 0xff).toByte()
            return b;
        }

        fun overwriteToFile(lines: List<String>, destination: String) {
            val dfile = File(destination)
            dfile.delete()
            dfile.createNewFile()
            for (line in lines) {
                dfile.appendText("$line\r", Charsets.UTF_8)
            }
        }

        /**
         * 异常提示
         */
        fun eb(context: Context, e: java.lang.Exception) {
            context.runOnUiThread {
                val msg = e.message ?: e.toString()
                toast(msg)
                Log.d("q-soft", msg)
            }
        }

        fun getBitmapFormUri(context: Context, uri: Uri): Bitmap? {
            var input = context.contentResolver.openInputStream(uri)

            //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true//不加载到内存
            onlyBoundsOptions.inDither = true//optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
            input!!.close()
            val originalWidth = onlyBoundsOptions.outWidth
            val originalHeight = onlyBoundsOptions.outHeight
            if (originalWidth == -1 || originalHeight == -1)
                return null

            //图片分辨率以480x800为标准
            val hh = 800f//这里设置高度为800f
            val ww = 480f//这里设置宽度为480f
            //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            var be = 1//be=1表示不缩放
            if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (originalWidth / ww).toInt()
            } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (originalHeight / hh).toInt()
            }
            if (be <= 0)
                be = 1
            //比例压缩
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inSampleSize = be//设置缩放比例
            bitmapOptions.inDither = true
            bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
            input = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
            input!!.close()

            return compressImage(bitmap)//再进行质量压缩
        }

        fun compressImage(image: Bitmap?): Bitmap? {
            val baos = ByteArrayOutputStream()
            image!!.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                baos
            )//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            var options = 100
            while (baos.toByteArray().count() / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset()//重置baos即清空baos
                //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
                image.compress(
                    Bitmap.CompressFormat.JPEG,
                    options,
                    baos
                )//这里压缩options，把压缩后的数据存放到baos中
                options -= 10//每次都减少10
                if (options <= 0)
                    break
            }
            val isBm = ByteArrayInputStream(baos.toByteArray())//把压缩后的数据baos存放到ByteArrayInputStream中
            return BitmapFactory.decodeStream(isBm, null, null)
        }

        /**
         * 遍历删除指定文件或文件夹下面的文件
         */
        fun deleteDirectoryFiles(directory: String) {
            try {
                val dir = File(directory)
                if (!dir.exists() || !dir.isDirectory) {
                    val msg = "目录不存在，或参数不是目录"
                    Log.d("soft", "目录不存在")
                    throw Exception(msg)
                }
                if (dir.exists() && dir.isDirectory) {
                    for (listFile in dir.listFiles()) {
                        if (listFile.isFile) {
                            listFile.delete()
                        } else if (listFile.isDirectory) {
                            deleteDirectoryFiles(listFile.name)
                        }
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * 压缩文件和文件夹
         *
         * @param srcFileString 要压缩的文件或文件夹
         * @param zipFileString 压缩完成的Zip路径
         * @throws Exception
         */
        @Throws(Exception::class)
        fun ZipFolder(srcFileString: String, zipFileString: String) {
            //创建ZIP
            val outZip = ZipOutputStream(FileOutputStream(zipFileString))
            outZip.use {
                //创建文件
                val file = File(srcFileString)
                //压缩
                Log.d("q-soft", "---->+${file.parent}===${file.absolutePath}")
                ZipFiles(file.parent + File.separator, file.name, outZip)
                //完成和关闭
                outZip.finish()
            }
        }

        /**
         * 压缩文件
         *
         * @param folderString
         * @param fileString
         * @param zipOutputSteam
         * @throws Exception
         */
        @Throws(Exception::class)
        private fun ZipFiles(
            folderString: String,
            fileString: String,
            zipOutputSteam: ZipOutputStream?
        ) {

            Log.d(
                "q-soft",
                "folderString:$folderString\nfileString:$fileString\n=========================="
            )
            if (zipOutputSteam == null)
                return
            val file = File(folderString + fileString)
            if (file.isFile) {
                val zipEntry = ZipEntry(fileString)
                val inputStream = FileInputStream(file)
                zipOutputSteam!!.putNextEntry(zipEntry)
                val buffer = ByteArray(4096)
                var len = inputStream.read(buffer)
                while (len != -1) {
                    zipOutputSteam!!.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
                zipOutputSteam!!.closeEntry()
            } else {
                //文件夹
                val fileList = file.list()
                //没有子文件和压缩
                if (fileList.isEmpty()) {
                    val zipEntry = ZipEntry(fileString + File.separator)
                    zipOutputSteam!!.putNextEntry(zipEntry)
                    zipOutputSteam!!.closeEntry()
                }
                //子文件和递归
                for (i in fileList.indices) {
                    ZipFiles("$folderString$fileString/", fileList[i], zipOutputSteam)
                }
            }
        }

        public fun unpackZip(path: String, zipname: String): Boolean {
            val `is`: InputStream
            val zis: ZipInputStream
            try {
                var filename: String
                `is` = FileInputStream(zipname)
                zis = ZipInputStream(BufferedInputStream(`is`))
                var ze: ZipEntry
                val buffer = ByteArray(1024)
                var count: Int
                while (true) {
                    val ze = zis.getNextEntry()
                    if (ze != null) {
                        filename = ze.name

                        // Need to create directories if not exists, or
                        // it will generate an Exception...
                        if (ze.isDirectory) {
                            val fmd = File(filename)
                            fmd.mkdirs()
                            continue
                        }

                        val outpath = combinePath(path, filename)
                        val fout = FileOutputStream(outpath)

                        while (true) {
                            val count = zis.read(buffer)
                            if (count != -1) {
                                fout.write(buffer, 0, count)
                            } else {
                                break //跳出while()
                            }
                        }
                        fout.close()
                        zis.closeEntry()
                    } else {
                        break //跳出while()
                    }
                }
                zis.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }

        /**
         * 将路径的片段连接成一个完整的路径
         */
        fun CombinPath(vararg pathSegments: String): String {
            return "/" + pathSegments.joinToString("/") {
                it.trimStart('/').trimEnd('/')
            } //头部总是加上 / 符号
        }
    }
}

/**
 * 字串转换为日期
 */
fun String.toDate(): Date {
    return SimpleDateFormat("yyyy-M-d").parse(this)
}

/**
 * 非空就输出字串，空白就输出空白
 */
fun String?.toStringWithDefault(def: String): String {
    return if (this.isNullOrEmpty()) def else this //else this不需要写为：else this!!，是因为前面的 if 已经处理了 null 的情况，后面不可能是 null 了，所以就省略了 !!，这是kotlin的厉害之处
}

/**
 * 自动在UI线程上执行toast()
 */
fun Context.toastOnUi(message: String) {
    runOnUiThread { toast(message) }
}

/**
 * 连接两个路径，组合为一个完整的路径
 */
fun combinePath(path: String, file: String): String {
    return File(path, file).path
}
