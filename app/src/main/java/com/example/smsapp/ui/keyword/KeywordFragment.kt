package com.example.smsapp.ui.keyword

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsapp.R
import com.example.smsapp.data.KeywordEntity
import com.example.smsapp.databinding.FragmentKeywordBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KeywordFragment : Fragment(R.layout.fragment_keyword) {

    private var _binding: FragmentKeywordBinding? = null
    private val b get() = _binding!!

    private val args: KeywordFragmentArgs by navArgs()
    private lateinit var vm: KeywordViewModel
    private lateinit var adapter: KeywordAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentKeywordBinding.bind(view)

        /* ─ Toolbar ─ */
        val tb: MaterialToolbar = b.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(tb)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tb.setNavigationOnClickListener { findNavController().navigateUp() }
        tb.title = if (args.isWhitelist) "화이트리스트 키워드" else "블랙리스트 키워드"
        tb.setTitleTextColor(Color.BLACK)
        tb.isTitleCentered = true

        /* ─ ViewModel ─ */
        vm = ViewModelProvider(
            this,
            KeywordViewModel.Factory(requireActivity().application, args.isWhitelist)
        )[KeywordViewModel::class.java]

        /* ─ RecyclerView ─ */
        adapter = KeywordAdapter { kw -> confirmDelete(kw) }
        b.rvKeyword.layoutManager = LinearLayoutManager(requireContext())
        b.rvKeyword.adapter = adapter
        vm.words.observe(viewLifecycleOwner) { adapter.submitList(it) }

        /* ─ 입력 + 추가 ─ */
        b.btnAdd.setOnClickListener {
            val word = b.etKeyword.text.toString().trim()
            if (word.isNotEmpty()) {
                vm.add(word)
                b.etKeyword.text?.clear()
            }
        }
        b.etKeyword.addTextChangedListener { txt ->
            b.btnAdd.isEnabled = !txt.isNullOrBlank()
        }
    }

    /* ─ 키워드 삭제 확인 ─ */
    private fun confirmDelete(kw: KeywordEntity) {
        val dlg = MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme_SMSApp)
            .setMessage("‘${kw.word}’ 키워드를 삭제할까요?")
            .setPositiveButton("삭제", null)
            .setNegativeButton("취소", null)
            .create()

        dlg.setOnShowListener {
            val ok     = dlg.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancel = dlg.getButton(AlertDialog.BUTTON_NEGATIVE)
            val black  = resources.getColor(R.color.black, null)
            ok.setTextColor(black); cancel.setTextColor(black)

            ok.setOnClickListener { vm.delete(kw); dlg.dismiss() }
            cancel.setOnClickListener { dlg.dismiss() }
        }
        dlg.show()
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
