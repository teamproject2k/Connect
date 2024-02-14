package com.teamproject2k.connect.domain.logger

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum.Debug
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum.Error
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum.Info
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum.Warn

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
                Firebase.crashlytics.log(message)
                Log.i(logTag, message)
            }

            Debug -> {
                Firebase.crashlytics.log(message)
                Log.d(logTag, message)
            }

            Warn -> {
                Firebase.crashlytics.log(message)
                Log.w(logTag, message)
            }
        }

    }
}
