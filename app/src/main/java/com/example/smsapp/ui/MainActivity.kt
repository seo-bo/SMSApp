package com.example.smsapp.ui

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.smsapp.R
import com.example.smsapp.util.PermissionHelper
import com.example.smsapp.util.SmsImporter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var permHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        permHelper = PermissionHelper(this) {
            // 기존 SMS DB 가져오기
            val prefs = getSharedPreferences("smsapp_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("import_done", false)) {
                lifecycleScope.launch {
                    SmsImporter.importAll(applicationContext)
                    prefs.edit().putBoolean("import_done", true).apply()
                }
            }
        }
        permHelper.ensureSmsPermissions(this)

        // 기본 SMS 앱 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { }

            val rm = getSystemService(RoleManager::class.java)
            if (rm?.isRoleAvailable(RoleManager.ROLE_SMS) == true &&
                !rm.isRoleHeld(RoleManager.ROLE_SMS)
            ) {
                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
            }
        }
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.nav_host).navigateUp()
}
