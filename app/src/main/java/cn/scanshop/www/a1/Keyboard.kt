package cn.scanshop.www.a1

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.jetbrains.anko.runOnUiThread
import java.util.*
import kotlin.concurrent.timerTask

public class Keybord {
    companion object {

        fun openKeybord(mEditText: EditText, mContext: Context): Unit {
            val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            );
        }

        fun openKeybord2(mEditText: EditText, mContext: Context): Unit {

            val timer = Timer()
            timer.schedule(timerTask {

                mContext.runOnUiThread {
                    mEditText.isFocusable = true
                    mEditText.isFocusableInTouchMode = true
                    mEditText.requestFocus()
                    val inputManager =
                        mEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.showSoftInput(mEditText, 0)
                }
            }, 200)

//                mEditText.setFocusable(true) //不用runOnUiThread()包裹容易跳出
//                mEditText.setFocusableInTouchMode(true)
//                mEditText.requestFocus()
//                val inputManager =
//                    mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                inputManager.showSoftInput(mEditText, 0)
//            }, 100)


//            mEditText.setFocusable(true);
//            mEditText.setFocusableInTouchMode(true);
//            mEditText.requestFocus();
//            val inputManager =mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
//            inputManager.showSoftInput(mEditText, 0);
        }


        fun closeKeybord(mEditText: EditText, mContext: Context) {

            val timer = Timer()
            timer.schedule(timerTask {
                                mContext.runOnUiThread {
                    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(mEditText.windowToken, 0)
                }
            }, 100)

//            val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager; //不用runOnUiThread()包裹容易跳出
//            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }

        fun isSoftInputShow(activity: Activity): Boolean {
            val view = activity.window.peekDecorView();
            if (view != null) {
                // 隐藏虚拟键盘
                val inputmanger = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                return inputmanger.isActive && activity.window.currentFocus != null;
            }
            return false;
        }
    }
}
