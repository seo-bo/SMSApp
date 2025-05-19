package com.example.smsapp.ui.thread

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smsapp.R
import com.example.smsapp.databinding.FragmentThreadBinding
import com.example.smsapp.ui.viewmodel.ThreadViewModel

class ThreadFragment : Fragment(R.layout.fragment_thread) {

    private var _b: FragmentThreadBinding? = null
    private val b get() = _b!!

    private val args: ThreadFragmentArgs by navArgs()
    private lateinit var vm: ThreadViewModel
    private lateinit var adapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentThreadBinding.bind(view)
        
        vm = ViewModelProvider(
            this,
            ThreadViewModel.Factory(requireActivity().application, args.address)
        )[ThreadViewModel::class.java]

        adapter = MessageAdapter()
        b.rvMsg.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
        }
        b.rvMsg.adapter = adapter

        b.inputContainer.doOnLayout { bar ->
            b.rvMsg.setPadding(0, 0, 0, bar.height)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = b.rvMsg.scrollToPosition(0)
            override fun onItemRangeInserted(pos: Int, cnt: Int) = onChanged()
        })

        vm.messages.observe(viewLifecycleOwner) { list -> adapter.submit(list) }

        b.btnSend.setOnClickListener {
            val text = b.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                vm.send(text)
                b.etMessage.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
