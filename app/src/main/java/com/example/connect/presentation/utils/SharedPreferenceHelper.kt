package com.example.connect.presentation.utils

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val IsUserDetailEntered = "is_user_detail_entered"
        private const val DeviceId = "device_id"
    }


    /**
     * A boolean value that indicates whether the user has entered their details.
     *
     * @property get Returns the value of the user detail entered.
     * @property set Sets the value of the user detail entered.
     */
    var isUserDetailsEntered
        get() = sharedPreferences.getBoolean(IsUserDetailEntered, false)
        set(isEntered) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(IsUserDetailEntered, isEntered)
            editor.apply()
        }


    /**
     * A string value that represent the device id on which app is running
     *
     * @property get Returns the value of the device id.
     * @property set Sets the value of the device id.
     */
    var deviceId
        get() = sharedPreferences.getString(DeviceId, "")
        set(updatedDeviceId) {
            val editor = sharedPreferences.edit()
            editor.putString(DeviceId, updatedDeviceId)
            editor.apply()
        }
}