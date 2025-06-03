package com.example.smsapp.util

import android.content.Context
import android.content.SharedPreferences

/** 앱 전역 설정 값을 보관 – SharedPreferences 래퍼  */
object AppPrefs {
    private const val PREF_NAME = "app_settings"
    private const val KEY_LOCAL_FILTER = "local_filter_enabled"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /* ─ Local spam filter ─ */
    fun isLocalFilterEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_LOCAL_FILTER, true)        // 기본값 = ON

    fun setLocalFilterEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_LOCAL_FILTER, enabled).apply()
    }
}
