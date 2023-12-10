package com.example.connect.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.connect.R
import com.example.connect.presentation.ui.models.PostVisibilityScope
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


    fun getPostVisibilityList(context: Context): List<PostVisibilityScope> {
        val postVisibilityScopeList = arrayListOf<PostVisibilityScope>()
        postVisibilityScopeList.add(
            PostVisibilityScope(
                0,
                context.getString(R.string.public_visibility),
                context.getString(R.string.this_post_will_be_visible_to_every_connect_user),
                R.drawable.ic_lock_open
            )
        )
        postVisibilityScopeList.add(
            PostVisibilityScope(
                1,
                context.getString(R.string.private_visibility),
                context.getString(R.string.this_post_will_only_be_visible_to_your_friends),
                R.drawable.ic_lock_close
            )
        )
        return postVisibilityScopeList
    }


    fun getExoPlayer(context: Context, uri: String): ExoPlayer {
        val exoPlayer = ExoPlayer.Builder(context)
            .build().apply {
                val defaultSourceFactory = DefaultDataSource.Factory(context)
                val dataSourceFactory = DefaultDataSource.Factory(context, defaultSourceFactory)
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
                setMediaSource(mediaSource)
                prepare()
            }
        return exoPlayer
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


    fun getCurrentTimeInMillis(): Long {
        return Date().time
    }

}