package com.example.smsapp.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smsapp.R
import com.example.smsapp.databinding.FragmentSettingsBinding
import com.example.smsapp.util.AppPrefs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val b get() = _binding!!

    private lateinit var vm: SettingsViewModel

    /* ───────── Lifecycle ───────── */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        /* 1. Toolbar */
        val tb: MaterialToolbar = b.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(tb)
        tb.apply {
            title = "설정"
            setTitleTextColor(Color.BLACK)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            isTitleCentered = true
        }

        /* 2. Insets */
        ViewCompat.setOnApplyWindowInsetsListener(
            view.findViewById<CoordinatorLayout>(R.id.settingsRoot)
        ) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }

        /* 3. ViewModel & 통계 */
        vm = ViewModelProvider(this)[SettingsViewModel::class.java]
        vm.total.observe(viewLifecycleOwner) { updateStats() }
        vm.spam.observe(viewLifecycleOwner)  { updateStats() }

        /* 4. 로컬 스팸 차단 토글 */
        val swLocal = b.swLocal as SwitchMaterial
        swLocal.isChecked = AppPrefs.isLocalFilterEnabled(requireContext())
        swLocal.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setLocalFilterEnabled(requireContext(), isChecked)
        }

        /* 5. 고급 스팸 차단 – 토글만 */
        b.swAdvanced.setOnCheckedChangeListener { _, _ -> }

        /* 6. 블랙/화이트 리스트 버튼 – 미구현 */
        b.btnBlacklist.setOnClickListener {
            android.widget.Toast.makeText(requireContext(),
                "추후 구현 예정입니다", android.widget.Toast.LENGTH_SHORT).show()
        }
        b.btnWhitelist.setOnClickListener {
            android.widget.Toast.makeText(requireContext(),
                "추후 구현 예정입니다", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStats() {
        val total = vm.total.value ?: 0
        val spam  = vm.spam.value  ?: 0
        val percent = if (total == 0) 0 else (spam * 100 / total)

        b.tvTotal.text = "전체 문자 ${total}건 중"
        b.tvSpam.text  = "스팸 문자 ${spam}건을 차단했어요"
        b.tvPercent.text = "$percent%"

        val ring = b.progressRing as CircularProgressIndicator
        ring.progress = percent
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
