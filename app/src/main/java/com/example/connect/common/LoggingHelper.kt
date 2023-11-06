package com.example.connect.common

import android.util.Log
import com.example.connect.common.LoggingLevelEnum.Debug
import com.example.connect.common.LoggingLevelEnum.Error
import com.example.connect.common.LoggingLevelEnum.Info
import com.example.connect.common.LoggingLevelEnum.Warn

object LoggingHelper {
    fun logData(loggingLevel: LoggingLevelEnum, logTag: String, screenName: String, logMessage: String) {
        val message = "$screenName $logMessage"
        when (loggingLevel) {
            Error -> {
                Log.e(logTag, message)
            }

            Info -> {
                Log.i(logTag, message)
            }

            Debug -> {
                Log.d(logTag, message)
            }

            Warn -> {
                Log.w(logTag, message)
            }
        }

    }
}
