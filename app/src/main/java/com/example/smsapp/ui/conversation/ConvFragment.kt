package com.example.smsapp.ui.conversation

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.smsapp.R
import com.example.smsapp.databinding.FragmentConversationsBinding
import com.example.smsapp.ui.viewmodel.ConvViewModel
import com.google.android.material.appbar.MaterialToolbar

class ConvFragment : Fragment(R.layout.fragment_conversations) {

    /* ---------- 뷰 바인딩 / VM ---------- */
    private var _binding: FragmentConversationsBinding? = null
    private val b get() = _binding!!
    private lateinit var vm: ConvViewModel

    /* ---------- RecyclerView 어댑터 ---------- */
    private val adapter = ConvAdapter { convo ->
        findNavController().navigate(
            ConvFragmentDirections.actionConvFragmentToThreadFragment(convo.address)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)      // 우측 스팸 아이콘 메뉴 사용
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConversationsBinding.bind(view)
        vm      = ViewModelProvider(this)[ConvViewModel::class.java]

        /* ─────────────── 1. Toolbar ─────────────── */
        val toolbar = b.toolbar as MaterialToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        // 왼쪽: 톱니바퀴 → 설정
        toolbar.setNavigationIcon(R.drawable.ic_baseline_settings_24)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_convFragment_to_settingsFragment)
        }
        toolbar.setTitleTextColor(Color.BLACK)

        /* ─────────────── 2. Insets ─────────────── */
        ViewCompat.setOnApplyWindowInsetsListener(
            b.convRoot as CoordinatorLayout
        ) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }

        /* ─────────────── 3. RecyclerView ─────────────── */
        b.rvConv.layoutManager = LinearLayoutManager(requireContext())
        b.rvConv.adapter       = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged()                 = b.rvConv.scrollToPosition(0)
            override fun onItemRangeInserted(p: Int, c: Int) = onChanged()
        })

        /* ─────────────── 4. FAB (새 대화) ─────────────── */
        b.fabCompose.setOnClickListener { showComposeDialog() }

        /* ─────────────── 5. LiveData ─────────────── */
        vm.rooms.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.emptyContainer.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        /* ─────────────── 6. 스와이프 삭제 ─────────────── */
        attachSwipeToDelete()
    }

    /* ======================================================================
       Compose(새 대화) 다이얼로그
       ====================================================================== */
    private fun showComposeDialog() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_new_conversation, null, false
        )
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("새 대화")
            .setView(dialogView)
            .setPositiveButton("확인") { d, _ ->
                val phone = etPhone.text.toString().trim()
                if (phone.isEmpty()) {
                    Toast.makeText(requireContext(),
                        "번호를 입력하세요", Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                val action = ConvFragmentDirections
                    .actionConvFragmentToThreadFragment(phone)
                findNavController().navigate(action)
                d.dismiss()
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()

        // 배경이 흰색이라 버튼 텍스트도 보이도록 색 지정
        val accent = ContextCompat.getColor(requireContext(), R.color.secondary)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(accent)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(accent)
    }

    /* ======================================================================
       메뉴 (우측 스팸 아이콘)
       ====================================================================== */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_conv, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_spam -> {
                findNavController().navigate(R.id.spamFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    /* ======================================================================
       ItemTouchHelper (스와이프 삭제)
       ====================================================================== */
    private fun attachSwipeToDelete() {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_delete_24)!!
        val bg   = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)

        val cb = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                tgt: RecyclerView.ViewHolder) = false

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dx: Float, dy: Float, state: Int, active: Boolean
            ) {
                val item   = vh.itemView
                val margin = (item.height - icon.intrinsicHeight) / 2
                val left   = item.left
                val right  = (left + dx).toInt()

                c.clipRect(left, item.top, right, item.bottom)
                c.drawColor(bg)
                icon.setBounds(
                    left + margin, item.top + margin,
                    left + margin + icon.intrinsicWidth,
                    item.top + margin + icon.intrinsicHeight
                )
                icon.draw(c)
                super.onChildDraw(c, rv, vh, dx, dy, state, active)
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val pos  = vh.bindingAdapterPosition
                val item = adapter.currentList[pos]
                AlertDialog.Builder(requireContext())
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        vm.deleteConversation(item.address)
                    }
                    .setNegativeButton("취소") { _, _ ->
                        adapter.notifyItemChanged(pos)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
        ItemTouchHelper(cb).attachToRecyclerView(b.rvConv)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
