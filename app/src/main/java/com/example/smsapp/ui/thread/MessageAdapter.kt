package com.example.smsapp.ui.thread

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
        fun bind(m: SmsEntity) {
            val parent = b.root as LinearLayout
            if (m.type == 1) {
                b.ivAvatar.visibility = View.VISIBLE
                b.tvAddress.visibility = View.VISIBLE
                b.tvAddress.text = m.address
                parent.gravity = Gravity.START
            } else {
                b.ivAvatar.visibility = View.INVISIBLE
                b.tvAddress.visibility = View.GONE
                parent.gravity = Gravity.END
            }
            b.tvBody.text = m.body
            b.tvTime.text = fmt.format(Date(m.timestamp))
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemMessageBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}