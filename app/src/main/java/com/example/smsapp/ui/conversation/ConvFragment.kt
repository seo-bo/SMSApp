package com.example.smsapp.ui.conversation

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog as AppAlert
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.smsapp.R
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.databinding.FragmentConversationsBinding
import com.example.smsapp.ui.viewmodel.ConvViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ConvFragment : Fragment(R.layout.fragment_conversations) {

    private var _binding: FragmentConversationsBinding? = null
    private val b get() = _binding!!

    private lateinit var vm: ConvViewModel
    private val adapter = ConvAdapter { convo ->
        findNavController().navigate(
            ConvFragmentDirections.actionConvFragmentToThreadFragment(convo.address)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConversationsBinding.bind(view)
        vm = ViewModelProvider(this)[ConvViewModel::class.java]

        /* ─ Toolbar ─ */
        val tb: MaterialToolbar = b.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(tb)
        tb.setNavigationIcon(R.drawable.ic_baseline_settings_24)
        tb.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_convFragment_to_settingsFragment)
        }

        /* ─ Insets ─ */
        ViewCompat.setOnApplyWindowInsetsListener(
            b.convRoot as CoordinatorLayout
        ) { v, ins ->
            val sys = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }

        /* ─ RecyclerView ─ */
        b.rvConv.layoutManager = LinearLayoutManager(requireContext())
        b.rvConv.adapter = adapter
        DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
            ContextCompat.getDrawable(requireContext(), R.drawable.divider_conv)?.let { setDrawable(it) }
            b.rvConv.addItemDecoration(this)
        }
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged()                        = b.rvConv.scrollToPosition(0)
            override fun onItemRangeInserted(p: Int, c: Int) = onChanged()
        })

        vm.rooms.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.emptyContainer.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        /* ─ FAB ─ */
        b.fabCompose.setOnClickListener { showComposeDialog() }

        /* ─ Swipe 삭제 ─ */
        attachSwipeToDelete()
    }

    /* ───────── 새 대화 다이얼로그 ───────── */
    private fun showComposeDialog() {
        val dialogV = layoutInflater.inflate(R.layout.dialog_start_conversation, null, false)
        val et = dialogV.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone)

        val dlg = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogV)
            .setPositiveButton("확인", null)
            .setNegativeButton("취소", null)
            .create()

        dlg.setOnShowListener {
            val black = ContextCompat.getColor(requireContext(), R.color.black)
            dlg.getButton(AppAlert.BUTTON_POSITIVE).apply {
                setTextColor(black)
                setOnClickListener {
                    val phone = et.text.toString().trim()
                    if (phone.isEmpty()) {
                        et.error = "번호를 입력하세요"
                    } else {
                        findNavController().navigate(
                            ConvFragmentDirections.actionConvFragmentToThreadFragment(phone)
                        )
                        dlg.dismiss()
                    }
                }
            }
            dlg.getButton(AppAlert.BUTTON_NEGATIVE).apply {
                setTextColor(black)
                setOnClickListener { dlg.cancel() }
            }
        }

        /* ─ 모든 종료 경로에서 키보드 강제 숨김 ─ */
        val onCancel = DialogInterface.OnCancelListener { forceHideKeyboard() }
        val onDismiss = DialogInterface.OnDismissListener { forceHideKeyboard() }

        dlg.setOnCancelListener(onCancel)
        dlg.setOnDismissListener(onDismiss)

        dlg.show()
    }

    /* ───────── 키보드 완전 강제 숨김 ───────── */
    private fun forceHideKeyboard() {
        hideIme()
        Handler(Looper.getMainLooper()).postDelayed({ hideIme() }, 50)
    }
    private fun hideIme() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    /* ───────── Swipe to delete ───────── */
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
                val pos   = vh.bindingAdapterPosition
                val convo = adapter.currentList[pos]

                val dlg = MaterialAlertDialogBuilder(requireContext())
                    .setMessage("이 대화를 정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", null)
                    .setNegativeButton("취소", null)
                    .setCancelable(false)
                    .create()

                dlg.setOnShowListener {
                    val black = ContextCompat.getColor(requireContext(), R.color.black)
                    dlg.getButton(AppAlert.BUTTON_POSITIVE).apply {
                        setTextColor(black)
                        setOnClickListener {
                            vm.deleteConversation(convo.address)
                            Snackbar.make(b.root, "대화방 삭제됨", Snackbar.LENGTH_LONG)
                                .setAction("Undo") {
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        vm.addNormal(
                                            SmsEntity(
                                                address = convo.address,
                                                body    = "",
                                                type    = 1
                                            )
                                        )
                                    }
                                }.show()
                            dlg.dismiss()
                        }
                    }
                    dlg.getButton(AppAlert.BUTTON_NEGATIVE).apply {
                        setTextColor(black)
                        setOnClickListener {
                            adapter.notifyItemChanged(pos)   // 복원
                            dlg.dismiss()
                        }
                    }
                }
                dlg.show()
            }
        }).attachToRecyclerView(b.rvConv)
    }

    /* 메뉴 (스팸함) */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_conv, menu)

    override fun onOptionsItemSelected(item: MenuItem) =
        if (item.itemId == R.id.menu_spam) {
            findNavController().navigate(R.id.spamFragment); true
        } else super.onOptionsItemSelected(item)

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
