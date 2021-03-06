package com.shortstack.hackertracker.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.shortstack.hackertracker.Constants
import com.shortstack.hackertracker.R
import javax.inject.Inject


class SharedPreferencesUtil @Inject constructor(context: Context) {

    companion object {
        private const val USER_ALLOW_PUSH = "user_allow_push_notifications"
        private const val USER_EXPIRED_EVENTS = "user_show_expired_events"
        private const val USER_ANALYTICS = "user_analytics"
        private const val SYNC_INTERVAL = "sync_interval"
    }

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val allowPushNotification: Boolean
        get() = preferences.getBoolean(USER_ALLOW_PUSH, true)

    val showExpiredEvents: Boolean
        get() = preferences.getBoolean(USER_EXPIRED_EVENTS, false)

    val allowAnalytics: Boolean
        get() = preferences.getBoolean(USER_ANALYTICS, true)

    val syncInterval: Int
        get() = preferences.getString(SYNC_INTERVAL, "6").toInt()

    val syncingDisabled: Boolean
        get() = syncInterval == 0

}
