package com.teamproject2k.connect.presentation.utils

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val IS_USER_DETAILS_ENTERED = "is_user_detail_entered"
        private const val DEVICE_ID = "device_id"
        private const val MOBILE_NUMBER = "mobile_number"
        private const val IS_CHAT_DETAIL_SCREEN_OPEN = "is_chat_detail_screen_open"
    }

    /**
     * A boolean value that indicates whether the user has entered their details.
     *
     * @property get Returns the value of whether the user details are entered.
     * @property set Sets the value of the user detail entered.
     */
    var isUserDetailsEntered
        get() = sharedPreferences.getBoolean(IS_USER_DETAILS_ENTERED, false)
        set(isEntered) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(IS_USER_DETAILS_ENTERED, isEntered)
            editor.apply()
        }

    /**
     * A string value that represent the device id on which app is running
     *
     * @property get Returns the value of the device id.
     * @property set Sets the value of the device id.
     */
    var deviceId
        get() = sharedPreferences.getString(DEVICE_ID, "")
        set(updatedDeviceId) {
            val editor = sharedPreferences.edit()
            editor.putString(DEVICE_ID, updatedDeviceId)
            editor.apply()
        }

    /**
     * A string value that represent the mobile number of user
     *
     * @property get Returns the value of the mobile number.
     * @property set Sets the value of the mobile number.
     */
    var mobileNumber
        get() = sharedPreferences.getString(MOBILE_NUMBER, "") ?: ""
        set(mobileNumber) {
            val editor = sharedPreferences.edit()
            editor.putString(MOBILE_NUMBER, mobileNumber)
            editor.apply()
        }

    /**
     * A boolean value that indicates whether the ChatDetailsScreen is open.
     *
     * @property get Returns the value of whether ChatDetailsScreen is opened.
     * @property set Sets the value of the user detail entered.
     */
    var isChatDetailScreenOpen: Boolean
        get() = sharedPreferences.getBoolean(IS_CHAT_DETAIL_SCREEN_OPEN, false)
        set(updatedValue) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(IS_CHAT_DETAIL_SCREEN_OPEN, updatedValue)
            editor.apply()
        }
}