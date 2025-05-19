package com.example.smsapp.ui.conversation

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsapp.R
import com.example.smsapp.databinding.DialogComposeBinding
import com.example.smsapp.databinding.FragmentConversationsBinding
import com.example.smsapp.ui.viewmodel.ConvViewModel
import com.google.android.material.appbar.MaterialToolbar

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
        _binding = FragmentConversationsBinding.bind(view)
        vm = ViewModelProvider(this)[ConvViewModel::class.java]

        val toolbar = b.toolbar as MaterialToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)

        ViewCompat.setOnApplyWindowInsetsListener(
            b.convRoot as CoordinatorLayout
        ) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }

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
                    Toast.makeText(
                        requireContext(),
                        "번호와 내용을 입력하세요",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                vm.sendSms(phone, msg)
                Toast.makeText(requireContext(), "전송 완료", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
