package com.example.connect.presentation.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FunctionHelper {
    fun vibrateDevice(context: Context, vibrationDuration: Long = 200) {
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
        if (vibrator?.hasVibrator() == true) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    vibrationDuration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    fun getFormattedDate(timeInMillis: Long): String {
        val date = Date(timeInMillis)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    fun getUserId(formattedName: String, currentCount: Int): String {
        var userId = formattedName.replace(" ", "").lowercase()
        userId = "$userId@${currentCount + 1}"
        return userId
    }


    fun Context.showToast(message: String, toastLength: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, toastLength).show()
    }
}