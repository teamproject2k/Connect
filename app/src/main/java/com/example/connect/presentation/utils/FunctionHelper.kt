package com.example.connect.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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


    fun Context.isNetworkAvailable(): Boolean {
        var isNetworkAvailable = false
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities != null) {
            isNetworkAvailable = with(networkCapabilities) {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        return isNetworkAvailable
    }


    fun getFormattedDisplayName(name: String): String {
        return name.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    }


    @Composable
    fun Modifier.shimmer(
        durationMillis: Int = 1000,
    ): Modifier {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "infinite_shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis), repeatMode = RepeatMode.Restart
            ), label = "shimmer"
        )

        return then(
            Modifier
                .background(
                    Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset.Zero,
                        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
                    )
                )
        )
    }
}