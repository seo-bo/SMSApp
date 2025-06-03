package com.example.smsapp.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smsapp.R
import com.google.android.material.appbar.MaterialToolbar

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* ─────────────── Toolbar ─────────────── */
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.apply {
            title = "설정"                     // 가운데 정렬용 → XML과 무관하게 수동 세팅
            setTitleTextColor(Color.BLACK)
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            navigationIcon?.setTint(Color.BLACK)
            setNavigationOnClickListener { findNavController().navigateUp() }
            // MaterialToolbar 1.4+ : 타이틀 가운데 정렬
            isTitleCentered = true
        }

        /* ─────────────── Insets ─────────────── */
        val root = view.findViewById<CoordinatorLayout>(R.id.settingsRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
