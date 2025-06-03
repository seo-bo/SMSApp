package com.example.smsapp.ui.keyword

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.data.KeywordEntity
import com.example.smsapp.databinding.ItemKeywordBinding

class KeywordAdapter(
    private val onLongPress: (KeywordEntity) -> Unit
) : ListAdapter<KeywordEntity, KeywordAdapter.VH>(diff) {

    inner class VH(val b: ItemKeywordBinding)
        : RecyclerView.ViewHolder(b.root) {
        fun bind(e: KeywordEntity) = with(b) {
            tvWord.text = e.word
            root.setOnLongClickListener { onLongPress(e); true }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemKeywordBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(getItem(i))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<KeywordEntity>() {
            override fun areItemsTheSame(a: KeywordEntity, b: KeywordEntity) = a.id == b.id
            override fun areContentsTheSame(a: KeywordEntity, b: KeywordEntity) = a == b
        }
    }
}
