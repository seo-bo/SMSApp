package com.example.smsapp.ui.conversation

import android.view.*
import androidx.recyclerview.widget.*
import com.example.smsapp.data.ConversationSummary
import com.example.smsapp.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.*

class ConvAdapter(
    private val onClick: (ConversationSummary) -> Unit
) : ListAdapter<ConversationSummary, ConvAdapter.VH>(diff) {

    private val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    inner class VH(val b: ItemConversationBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(c: ConversationSummary) = with(b) {
            tvAddress.text = c.address
            tvPreview.text = c.lastBody
            tvTime.text    = fmt.format(Date(c.lastTime))
            tvCount.text   = c.total.toString()
            root.setOnClickListener { onClick(c) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemConversationBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(getItem(i))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<ConversationSummary>() {
            override fun areItemsTheSame(a: ConversationSummary, b: ConversationSummary) =
                a.address == b.address
            override fun areContentsTheSame(a: ConversationSummary, b: ConversationSummary) = a == b
        }
    }
}
