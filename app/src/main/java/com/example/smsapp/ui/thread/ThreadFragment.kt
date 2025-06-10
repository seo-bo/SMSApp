package com.example.smsapp.ui.thread

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.R
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.databinding.FragmentThreadBinding
import com.example.smsapp.ui.viewmodel.ThreadViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class ThreadFragment : Fragment(R.layout.fragment_thread) {

    private var _binding: FragmentThreadBinding? = null
    private val b get() = _binding!!

    private val args: ThreadFragmentArgs by navArgs()
    private lateinit var vm: ThreadViewModel
    private lateinit var adapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentThreadBinding.bind(view)

        // 툴바
        val tb: MaterialToolbar = b.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(tb)
        tb.title = args.address // 전화번호만 표시
        tb.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        tb.navigationIcon?.setTint(android.graphics.Color.BLACK)
        tb.setNavigationOnClickListener { hideKeyboardAndBack() }
        tb.isTitleCentered = false  // 왼쪽 정렬

        // ViewModel
        vm = ViewModelProvider(
            this,
            ThreadViewModel.Factory(
                requireActivity().application,
                args.address,
                args.isSpam
            )
        )[ThreadViewModel::class.java]

        // RecyclerView
        adapter = MessageAdapter(::onMessageLongPress)
        b.rvMsg.layoutManager = LinearLayoutManager(requireContext()).apply { reverseLayout = true }
        b.rvMsg.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged()                        = b.rvMsg.scrollToPosition(0)
            override fun onItemRangeInserted(p: Int, c: Int) = onChanged()
        })
        vm.messages.observe(viewLifecycleOwner) { adapter.submit(it) }

        // 입력 / 전송
        if (args.isSpam) {
            b.inputContainer.visibility = View.GONE // 읽기 전용
        } else {
            b.btnSend.setOnClickListener {
                val txt = b.etMessage.text.toString().trim()
                if (txt.isNotEmpty()) {
                    vm.send(txt)
                    b.etMessage.text?.clear()
                }
            }
        }

        // 패딩
        b.inputContainer.doOnLayout { bar ->
            b.rvMsg.setPadding(0, 0, 0, bar.height)
        }
    }

    // Copy / Delete
    private fun onMessageLongPress(msg: SmsEntity, anchor: View) {
        if (args.isSpam) return

        val sheet = BottomSheetDialog(requireContext())
        val content = layoutInflater.inflate(R.layout.sheet_message_actions, null, false)
        sheet.setContentView(content)

        val bar = content as BottomNavigationView
        bar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.actionCopy -> {
                    val cm = requireContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("sms", msg.body))
                    Snackbar.make(b.root, "Copied", Snackbar.LENGTH_SHORT).show()
                }
                R.id.actionDelete -> vm.delete(msg.id)
            }
            sheet.dismiss()
            true
        }

        sheet.show()
    }

    // 뒤로가기
    private fun hideKeyboardAndBack() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val token = requireActivity().currentFocus?.windowToken ?: b.root.windowToken
        imm.hideSoftInputFromWindow(token, 0)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
