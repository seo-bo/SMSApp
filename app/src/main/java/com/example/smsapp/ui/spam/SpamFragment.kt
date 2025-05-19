package com.example.smsapp.ui.spam

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsapp.R
import com.example.smsapp.databinding.FragmentSpamBinding
import com.example.smsapp.ui.viewmodel.SpamViewModel

class SpamFragment : Fragment(R.layout.fragment_spam) {

    private var _b: FragmentSpamBinding? = null
    private val b get() = _b!!
    private lateinit var vm: SpamViewModel
    private val adapter = SpamAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentSpamBinding.bind(view)
        vm = ViewModelProvider(this)[SpamViewModel::class.java]

        b.rvSpam.layoutManager = LinearLayoutManager(requireContext())
        b.rvSpam.adapter = adapter

        vm.spam.observe(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
