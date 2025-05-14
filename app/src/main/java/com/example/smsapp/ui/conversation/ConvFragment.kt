package com.example.smsapp.ui.conversation

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsapp.R
import com.example.smsapp.databinding.DialogComposeBinding
import com.example.smsapp.databinding.FragmentConversationsBinding
import com.example.smsapp.ui.viewmodel.ConvViewModel

class ConvFragment : Fragment(R.layout.fragment_conversations) {

    private var _b: FragmentConversationsBinding? = null
    private val b get() = _b!!
    private lateinit var vm: ConvViewModel
    private val adapter = ConvAdapter { convo ->
        val action = ConvFragmentDirections
            .actionConvFragmentToThreadFragment(convo.address)
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentConversationsBinding.bind(view)
        vm = ViewModelProvider(this)[ConvViewModel::class.java]

        b.rvConv.layoutManager = LinearLayoutManager(requireContext())
        b.rvConv.adapter = adapter

        b.fabCompose.setOnClickListener { showComposeDialog() }

        vm.rooms.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.emptyContainer.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showComposeDialog() {
        val dlgB = DialogComposeBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setTitle("새 대화")
            .setView(dlgB.root)
            .setPositiveButton("보내기") { dialog, _ ->
                val phone = dlgB.etPhone.text.toString().trim()
                val msg   = dlgB.etMessage.text.toString().trim()
                if (phone.isEmpty() || msg.isEmpty()) {
                    Toast.makeText(requireContext(),
                        "번호와 내용을 입력하세요",
                        Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                try {
                    vm.sendSms(phone, msg)
                    Toast.makeText(requireContext(),
                        "전송 완료",
                        Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "전송 실패: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
