package com.example.smsapp.ui.thread

import android.view.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.R
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val onLongPress: (SmsEntity, View) -> Unit        // ← 콜백
) : RecyclerView.Adapter<MessageAdapter.VH>() {

    private val items = mutableListOf<SmsEntity>()
    private val fmt   = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    fun submit(list: List<SmsEntity>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(val b: ItemMessageBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(m: SmsEntity) {
            val ctx = b.root.context
            val parent = b.root as LinearLayout

            if (m.type == 1) {                       // 수신
                b.ivAvatar.visibility = View.VISIBLE
                b.tvAddress.visibility = View.GONE
                parent.gravity = Gravity.START
                b.layoutBubble.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_message_bubble)
            } else {                                // 발신
                b.ivAvatar.visibility = View.INVISIBLE
                b.tvAddress.visibility = View.GONE
                parent.gravity = Gravity.END
                b.layoutBubble.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_message_bubble_outgoing)
            }
            b.tvBody.text = m.body
            b.tvTime.text = fmt.format(Date(m.timestamp))

            /* ─── 롱클릭 콜백 ─── */
            b.layoutBubble.setOnLongClickListener {
                onLongPress(m, it)
                true
            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemMessageBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])

    override fun getItemCount() = items.size
}
