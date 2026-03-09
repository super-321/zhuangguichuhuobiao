package cn.scanshop.www.a1

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.loading_statistics_layout.*

/**
 * 当前回数的所有货柜的统计，可按扫描顺序排，也可按序号排
 */
class UnplanStatActivity : AppCompatActivity() {

    private var adapter: StatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.unplan_loading_statistics_layout)

        initCtrls()
    }

    private fun initCtrls() {
        unplanQueryStat() //查询出临时调货扫描的指定订单号（orderno）的所有产品的出货情况
    }

    private fun unplanQueryStat() {
        val orderno = intent.extras.getString(SQLiteDbHelper.FN_ORDERNO)
        val db = SQLiteDbHelper(this)
        val baritems = db.unplanGetStats(orderno)
        val rowcount = baritems.count()
        adapter = StatAdapter(baritems, this)
        lvstat.adapter = adapter
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
                view = View.inflate(ctx, R.layout.unplan_loading_stat_item_layout, null)
                viewHolder = ViewHolder(view)
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
            val item = getItem(position)
            if (item is BarItem) {
                viewHolder.lblmodel.text = item.model
                viewHolder.lbltotal.text = item.total.toString()
            }

            return view
        }
    }

    class ViewHolder(viewItem: View) {
        var lblmodel: TextView = viewItem.findViewById(R.id.lblmodel)
        var lbltotal: TextView = viewItem.findViewById(R.id.lbltotal)
    }
}