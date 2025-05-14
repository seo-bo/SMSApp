package com.example.smsapp.ui.thread

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.VH>() {
    private val items = mutableListOf<SmsEntity>()
    private val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    fun submit(list: List<SmsEntity>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(private val b: ItemMessageBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(m: SmsEntity) = with(b) {
            tvAddress.text = if (m.type == 1) m.address else "나"
            tvBody.text    = m.body
            tvTime.text    = fmt.format(Date(m.timestamp))
            root.alpha     = if (m.type == 1) 1f else .6f
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemMessageBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}
