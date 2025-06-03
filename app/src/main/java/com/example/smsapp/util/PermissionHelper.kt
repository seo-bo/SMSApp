package com.example.smsapp.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionHelper(
    caller: ActivityResultCaller,
    private val onGranted: () -> Unit
) {
    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) onGranted()
    }

    fun ensureSmsPermissions(activity: Activity) {
        val perms = arrayOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS
        )
        val ok = perms.all {
            ContextCompat.checkSelfPermission(activity, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (ok) onGranted() else launcher.launch(perms)
    }
}
