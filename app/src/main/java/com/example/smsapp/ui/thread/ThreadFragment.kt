package com.example.smsapp.ui.thread

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
import com.example.smsapp.databinding.FragmentThreadBinding
import com.example.smsapp.ui.viewmodel.ThreadViewModel
import com.google.android.material.appbar.MaterialToolbar

class ThreadFragment : Fragment(R.layout.fragment_thread) {

    private var _binding: FragmentThreadBinding? = null
    private val b get() = _binding!!

    private val args: ThreadFragmentArgs by navArgs()
    private lateinit var vm: ThreadViewModel
    private lateinit var adapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentThreadBinding.bind(view)

        /* ─────────────── 1. Toolbar ─────────────── */
        val toolbar: MaterialToolbar = b.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.apply {
            title = if (args.isSpam) "스팸: ${args.address}" else args.address
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            navigationIcon?.setTint(android.graphics.Color.BLACK) // ← 검정색 화살표
            setNavigationOnClickListener {
                // ◀ 누르면 키보드 숨기고 뒤로
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val token = requireActivity().currentFocus?.windowToken ?: b.root.windowToken
                imm.hideSoftInputFromWindow(token, 0)
                findNavController().navigateUp()
            }
            isTitleCentered = true
        }

        /* ─────────────── 2. ViewModel ─────────────── */
        vm = ViewModelProvider(
            this,
            ThreadViewModel.Factory(
                requireActivity().application,
                args.address,
                args.isSpam
            )
        )[ThreadViewModel::class.java]

        /* ─────────────── 3. RecyclerView ─────────────── */
        adapter = MessageAdapter()
        b.rvMsg.layoutManager = LinearLayoutManager(requireContext())
            .apply { reverseLayout = true }
        b.rvMsg.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged()                        = b.rvMsg.scrollToPosition(0)
            override fun onItemRangeInserted(p: Int, c: Int) = onChanged()
        })

        vm.messages.observe(viewLifecycleOwner) { adapter.submit(it) }

        /* ─────────────── 4. 입력창 & 전송 ─────────────── */
        if (args.isSpam) {
            b.inputContainer.visibility = View.GONE   // 읽기 전용
        } else {
            b.btnSend.setOnClickListener {
                val text = b.etMessage.text.toString().trim()
                if (text.isNotEmpty()) {
                    vm.send(text)
                    b.etMessage.text?.clear()
                }
            }
        }

        /* ─────────────── 5. 입력창 높이만큼 패딩 ─────────────── */
        b.inputContainer.doOnLayout { bar ->
            b.rvMsg.setPadding(0, 0, 0, bar.height)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
