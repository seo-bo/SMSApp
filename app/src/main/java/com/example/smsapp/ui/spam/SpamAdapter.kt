package com.example.smsapp.ui.spam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.data.SpamEntity
import com.example.smsapp.databinding.ItemSpamBinding
import java.text.SimpleDateFormat
import java.util.*

class SpamAdapter : RecyclerView.Adapter<SpamAdapter.VH>() {
    private val items = mutableListOf<SpamEntity>()
    private val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    fun submit(list: List<SpamEntity>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(val b: ItemSpamBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: SpamEntity) = with(b) {
            tvAddr.text = e.address
            tvBody.text = e.body
            tvTime.text = fmt.format(Date(e.timestamp))
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemSpamBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}
