package com.example.smsapp.ui.thread

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsapp.databinding.FragmentThreadBinding
import com.example.smsapp.ui.viewmodel.ThreadViewModel

class ThreadFragment : Fragment() {
    private val args: ThreadFragmentArgs by navArgs()
    private var _b: FragmentThreadBinding? = null
    private val b get() = _b!!
    private lateinit var vm: ThreadViewModel
    private val adapter = MessageAdapter()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentThreadBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        vm = ViewModelProvider(
            this,
            ThreadViewModel.Factory(requireActivity().application, args.address)
        )[ThreadViewModel::class.java]

        b.tvTitle.text = args.address
        b.rvMsg.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
        }
        b.rvMsg.adapter = adapter

        vm.messages.observe(viewLifecycleOwner) { adapter.submit(it) }

        b.btnSend.setOnClickListener {
            val text = b.etMessage.text.toString()
            if (text.isBlank()) return@setOnClickListener
            vm.send(text)
            b.etMessage.text.clear()
            Toast.makeText(requireContext(), "전송 완료", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
