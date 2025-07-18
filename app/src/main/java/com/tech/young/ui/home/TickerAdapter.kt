package com.tech.young.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.R

//data class StockItem(val symbol: String, val change: String, val isUp: Boolean)
//
//class TickerAdapter(private val items: List<StockItem>) :
//    RecyclerView.Adapter<TickerAdapter.TickerViewHolder>() {
//
//    inner class TickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvStock: TextView = view.findViewById(R.id.tvStock)
//        val ivArrow: ImageView = view.findViewById(R.id.ivArrow)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TickerViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticker_stock, parent, false)
//        return TickerViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: TickerViewHolder, position: Int) {
//        val item = items[position % items.size] // repeat for infinite feel
//        holder.tvStock.text = "${item.symbol} ${item.change}"
//        holder.ivArrow.setImageResource(if (item.isUp) R.drawable.arrow_up else R.drawable.arrow_down)
//        /*holder.ivArrow.setColorFilter(
//            if (item.isUp) Color.GREEN else Color.RED,
//            PorterDuff.Mode.SRC_IN
//        )*/
//    }
//
//    override fun getItemCount(): Int = Int.MAX_VALUE // large number for loop effect
//}

data class StockItem(val symbol: String, val change: String, val isUp: Boolean)

class TickerAdapter(private var items: List<StockItem>) :
    RecyclerView.Adapter<TickerAdapter.TickerViewHolder>() {

    inner class TickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val ivArrow: ImageView = view.findViewById(R.id.ivArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TickerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticker_stock, parent, false)
        return TickerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TickerViewHolder, position: Int) {
        if (items.isEmpty()) return

        val item = items[position % items.size] // looped effect
        holder.tvStock.text = "${item.symbol} ${item.change}"
        holder.ivArrow.setImageResource(if (item.isUp) R.drawable.arrow_up else R.drawable.arrow_down)
    }

    override fun getItemCount(): Int = if (items.isEmpty()) 0 else Int.MAX_VALUE

    fun setItems(newItems: List<StockItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}

