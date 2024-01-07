package com.example.connect.domain.logger

import android.util.Log
import com.example.connect.domain.logger.LoggingLevelEnum.Debug
import com.example.connect.domain.logger.LoggingLevelEnum.Error
import com.example.connect.domain.logger.LoggingLevelEnum.Info
import com.example.connect.domain.logger.LoggingLevelEnum.Warn
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object LoggingHelper {
    fun logData(
        loggingLevel: LoggingLevelEnum,
        logTag: String,
        screenName: String,
        logMessage: String
    ) {
        val message = "$screenName $logMessage"
        when (loggingLevel) {
            Error -> {
                Firebase.crashlytics.log(message)
                Log.e(logTag, message)
            }

            Info -> {
                Log.i(logTag, message)
            }

            Debug -> {
                Log.d(logTag, message)
            }

            Warn -> {
                Firebase.crashlytics.log(message)
                Log.w(logTag, message)
            }
        }

    }
}
