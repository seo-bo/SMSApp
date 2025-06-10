package com.example.smsapp.ui.spam

import android.graphics.Canvas
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.R
import com.example.smsapp.databinding.FragmentSpamBinding
import com.example.smsapp.ui.viewmodel.SpamViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SpamFragment : Fragment(R.layout.fragment_spam) {

    private var _binding: FragmentSpamBinding? = null
    private val b get() = _binding!!
    private lateinit var vm: SpamViewModel
    private val adapter = SpamAdapter { s ->
        val action = SpamFragmentDirections
            .actionSpamFragmentToThreadFragment(s.address, true)
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpamBinding.bind(view)

        // 툴바
        val tb = b.toolbar as MaterialToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(tb)
        tb.title = "스팸함"
        tb.setTitleTextColor(Color.BLACK)
        (requireActivity() as AppCompatActivity)
            .supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // insets
        val root = view.findViewById<CoordinatorLayout>(R.id.spamRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, ins ->
            val sys = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // RecyclerView
        vm = ViewModelProvider(this)[SpamViewModel::class.java]
        b.rvSpam.layoutManager = LinearLayoutManager(requireContext())
        b.rvSpam.adapter = adapter
        vm.rooms.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            b.rvSpam.scrollToPosition(0)
        }

        attachSwipeToDelete()
    }

    // 스와이프 삭제
    private fun attachSwipeToDelete() {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_delete_24)!!
        val bg   = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, tgt: RecyclerView.ViewHolder) = false

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dx: Float, dy: Float, state: Int, active: Boolean
            ) {
                val item   = vh.itemView
                val margin = (item.height - icon.intrinsicHeight) / 2
                c.save()
                c.clipRect(item.left, item.top, item.left + dx.toInt(), item.bottom)
                c.drawColor(bg)
                icon.setBounds(
                    item.left + margin, item.top + margin,
                    item.left + margin + icon.intrinsicWidth,
                    item.top  + margin + icon.intrinsicHeight
                )
                icon.draw(c)
                c.restore()
                super.onChildDraw(c, rv, vh, dx, dy, state, active)
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val pos     = vh.bindingAdapterPosition
                val summary = adapter.currentList[pos]

                val dlg = MaterialAlertDialogBuilder(requireContext())
                    .setMessage("이 대화를 정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", null)
                    .setNegativeButton("취소", null)
                    .setCancelable(false)
                    .create()

                dlg.setOnShowListener {
                    val black = ContextCompat.getColor(requireContext(), R.color.black)
                    dlg.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                        setTextColor(black)
                        setOnClickListener {
                            vm.deleteConversation(summary.address)
                            dlg.dismiss()
                        }
                    }
                    dlg.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                        setTextColor(black)
                        setOnClickListener {
                            adapter.notifyItemChanged(pos)  // 복원
                            dlg.dismiss()
                        }
                    }
                }
                dlg.show()
            }
        }).attachToRecyclerView(b.rvSpam)
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
