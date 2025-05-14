package com.example.smsapp.ui

import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import com.example.smsapp.R
import com.example.smsapp.util.PermissionHelper

class MainActivity : AppCompatActivity() {

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /*미작성*/ }

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /*미작성*/ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        if (!PermissionHelper.hasAll(this))
            permLauncher.launch(PermissionHelper.REQUIRED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm?.isRoleAvailable(RoleManager.ROLE_SMS) == true &&
                !rm.isRoleHeld(RoleManager.ROLE_SMS)
            ) roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
        }
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.nav_host).navigateUp()
}
