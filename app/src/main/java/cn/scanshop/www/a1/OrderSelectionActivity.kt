package cn.scanshop.www.a1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.order_selection_layout.*

class OrderSelectionActivity : AppCompatActivity() {
    private var adapter: MyAdapter? = null
    private var selectedOrderno: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_selection_layout)

        initCtrl()
    }

    private fun initCtrl() {
//        adapter = StatAdapter(
//            listOf(
//                "order-1",
//                "order-2",
//                "order-3",
//                "order-4",
//                "order-5",
//                "order-6",
//                "order-7",
//                "order-8",
//                "order-9",
//                "order-10"
//            ), this
//        )

//        val orders = listOf("order-1", "order-2", "order-3")
        val orders = SQLiteDbHelper(this).orders  //orders 是 getOrders() 的一种简单写法，kotlin建议这样写

        var beans = ArrayList<Bean>()
        orders.forEach {
            beans.add(Bean(it))
        }

//        val orders = listOf(Bean("order-1"), Bean("order-2"), Bean("order-3"))
        adapter = MyAdapter(beans, this)
        lvorders.adapter = adapter

//        findViewById<TextView>(R.id.txtorderno).setOnClickListener { //点击 订单号 时的功能
//            Util.showMessage(this, "(it as TextView).text.toString()")
//        }

        findViewById<ListView>(R.id.lvorders).setOnItemClickListener { parent, view, position, id ->
            //            val element = adapter.getItemAtPosition(position) // The item that was clicked
//            val intent = Intent(this, BookDetailActivity::class.java)
//            startActivity(intent)

            for (it in beans) {
                it.checked = false
            }

            val bean = parent.getItemAtPosition(position) as Bean
            bean.checked = true
            selectedOrderno = bean.orderno
            (adapter as MyAdapter).notifyDataSetChanged()

//            Util.showMessage(this, bean.orderno) //提示所选择的回数

//            val orderno = parent.getItemAtPosition(position) as String
//            Util.showMessage(this, orderno)
//
//            val c = findViewById<CheckBox>(R.id.chkorderno)
//            c.setChecked(!c.isChecked)
        }

//        findViewById<Button>(R.id.txtorderno).setOnClickListener { //会跳出
//            Util.showMessage(this, "orderno")
//        }

        btnOk.setOnClickListener {

            var intent = Intent()
            intent.putExtra(SQLiteDbHelper.FN_ORDERNO, selectedOrderno)
            setResult(Activity.RESULT_OK, intent)
            finish() //不finish()是不是就会从上层回到这里？可能不是这样的

//            Util.showMessage(this, "选择了：${selectedOrderno}")
        }
    }

    //    class StatAdapter(private val list: List<String>, private val ctx: Context) : BaseAdapter() {
//
//        override fun getCount(): Int = list.size
//
//        override fun getItemId(position: Int): Long = position.toLong()
//
//        override fun getItem(position: Int): Any = list.get(position)
//
//        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//            var viewHolder: ViewHolder
//            var view: View
//            if (convertView == null) {
//                view = View.inflate(ctx, R.layout.order_item_layout, null)
//                viewHolder = ViewHolder(view)
//                view.tag = viewHolder
//            } else {
//                view = convertView
//                viewHolder = view.tag as ViewHolder
//            }
//            val item = getItem(position)
//            if (item is String) {
//                /**
//                 *直接通过view.text设置文本信息
//                 */
//                viewHolder.tv.text = item
//            }
//            return view!!
//        }
//    }

    class MyAdapter(private val list: List<Bean>, private val ctx: Context) : BaseAdapter() {

        override fun getCount(): Int = list.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItem(position: Int): Bean = list.get(position)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var view: View
            if (convertView == null) {
                view = View.inflate(ctx, R.layout.order_item_layout, null)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
            val item = getItem(position)
            if (item is Bean) {
                /**
                 *直接通过view.text设置文本信息
                 */
                viewHolder.tv.text = item.orderno
            }

            viewHolder.cb.isChecked = item.checked

            return view!!
        }
    }

    class ViewHolder(viewItem: View) {
        var tv: TextView = viewItem.findViewById(R.id.txtorderno)
        var cb: CheckBox = viewItem.findViewById(R.id.chkorderno)
    }

    class Bean(val orderno: String, var checked: Boolean = false)
}
