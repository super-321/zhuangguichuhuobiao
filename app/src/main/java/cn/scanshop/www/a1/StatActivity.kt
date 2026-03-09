package cn.scanshop.www.a1

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.loading_statistics_layout.*

/**
 * 当前回数的所有货柜的统计，可按扫描顺序排，也可按序号排
 */
class StatActivity : AppCompatActivity() {

    /**
     * 创建右上角菜单
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_stat, menu)

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * 点击右上角菜单时的功能
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_by_scan -> {
//                toast("按扫描顺序排")
                queryStat(ORDERBY_SCAN) //开始时按扫描顺序排
                true
            }
            R.id.action_by_order -> {
//                toast("按序号排")
                queryStat(ORDERBY_ORDER) //开始时按扫描顺序排
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private var adapter: StatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_statistics_layout)

        initCtrls()
    }

    private val ORDERBY_SCAN = "scan" //按扫描时间排
    private val ORDERBY_ORDER = "ord" //按序号排

    private fun initCtrls() {
        queryStat(ORDERBY_ORDER) //开始时按扫描顺序排
    }

    private fun queryStat(orderby: String) {
        val orderno = intent.extras.getString(SQLiteDbHelper.FN_ORDERNO)
        val db = SQLiteDbHelper(this)
        val baritems = db.getStats(orderno, orderby)
        val rowcount = baritems.count()
        adapter = StatAdapter(baritems, this)
        lvstat.adapter = adapter

//        if (title.isBlank()) {
//            title = "$title（$rowcount）"
//        }
        title = "出货统计（$rowcount）"

    }

    class StatAdapter(private val list: List<BarItem>, private val ctx: Context) : BaseAdapter() {

        override fun getCount(): Int = list.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItem(position: Int): BarItem = list.get(position)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var view: View
            if (convertView == null) {
                view = View.inflate(ctx, R.layout.loading_stat_item_layout, null)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
            val item = getItem(position)
            if (item is BarItem) {
                viewHolder.lblord.text = item.ord.toString()
                viewHolder.lblmodel.text = item.model
                viewHolder.lblplanning.text = item.planning.toString()
                viewHolder.lbltotal.text = item.total.toString()
                viewHolder.lblunchecked.text = item.unchecked.toString()
            }

            return view
        }
    }

    class ViewHolder(viewItem: View) {
        var lblord: TextView = viewItem.findViewById(R.id.lblord)
        var lblmodel: TextView = viewItem.findViewById(R.id.lblmodel)
        var lblplanning: TextView = viewItem.findViewById(R.id.lblplanning)
        var lbltotal: TextView = viewItem.findViewById(R.id.lbltotal)
        var lblunchecked: TextView = viewItem.findViewById(R.id.lblunchecked)

//        总查询不需要加黄标记功能
//        val click = { _: View ->
//
//            if (lblord.tag == Color.YELLOW) {
//                lblord.backgroundColor = Color.WHITE
//                lblmodel.backgroundColor = Color.WHITE
//                lblplanning.backgroundColor = Color.WHITE
//                lbltotal.backgroundColor = Color.WHITE
//                lblunchecked.backgroundColor = Color.WHITE
//
//                lblord.tag = Color.WHITE
//
//            }else{
//                lblord.backgroundColor = Color.YELLOW
//                lblmodel.backgroundColor = Color.YELLOW
//                lblplanning.backgroundColor = Color.YELLOW
//                lbltotal.backgroundColor = Color.YELLOW
//                lblunchecked.backgroundColor = Color.YELLOW
//
//                lblord.tag = Color.YELLOW
//            }
//        }
//
//        init {
//            lblord.setOnClickListener(click)
//            lblmodel.setOnClickListener(click)
//            lblplanning.setOnClickListener(click)
//            lbltotal.setOnClickListener(click)
//            lblunchecked.setOnClickListener(click)
//
//            lblord.tag = Color.WHITE //用序号列作为标记颜色的记录器
//        }
    }
}