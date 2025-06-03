package com.example.smsapp.ui.spam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.R
import com.example.smsapp.data.SpamSummary
import com.example.smsapp.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.*

class SpamAdapter(
    private val onClick: (SpamSummary) -> Unit = {}
) : ListAdapter<SpamSummary, SpamAdapter.VH>(diff) {

    private val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    inner class VH(val b: ItemConversationBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(c: SpamSummary) = with(b) {
            tvAddress.text = c.address
            tvPreview.text = c.lastBody
            tvTime.text    = fmt.format(Date(c.lastTime))
            tvCount.text   = c.total.toString()
            ivAvatar.setImageResource(R.drawable.ic_default_profile)
            root.setOnClickListener { onClick(c) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemConversationBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(getItem(i))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<SpamSummary>() {
            override fun areItemsTheSame(a: SpamSummary, b: SpamSummary) =
                a.address == b.address
            override fun areContentsTheSame(a: SpamSummary, b: SpamSummary) = a == b
        }
    }
}
