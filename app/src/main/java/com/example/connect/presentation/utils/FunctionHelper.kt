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
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.presentation.ui.models.PostVisibilityScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object FunctionHelper {
    /**
     * Vibrates the device for a specified duration.
     *
     * @param context The context of the application.
     * @param vibrationDuration The duration of the vibration in milliseconds.
     */
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

    /**
     * Formats the given time in milliseconds to a string in the format "dd MMM yyyy".
     *
     * @param timeInMillis The time in milliseconds.
     * @return The formatted date.
     */
    fun getFormattedDate(timeInMillis: Long): String {
        val date = Date(timeInMillis)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }


    /**
     * Gets the user ID for a given formatted name and current count.
     *
     * @param formattedName The formatted name of the user.
     * @param currentCount The current count of users.
     * @return The user ID.
     */
    fun getUserId(formattedName: String, currentCount: Int): String {
        // Replace all spaces in the formatted name with an empty string and then converts it to lowercase
        var userId = formattedName.replace(" ", "").lowercase()
        userId = "$userId@${currentCount + 1}"

        return userId
    }


    /**
     * Shows a toast message.
     *
     * @param message The message to show.
     * @param toastLength The length of the toast.
     */
    fun Context.showToast(message: String, toastLength: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, toastLength).show()
    }


    /**
     * Checks if the network is available.
     *
     * @return `true` if the network is available, `false` otherwise.
     */
    fun Context.isNetworkAvailable(): Boolean {
        var isNetworkAvailable = false
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities != null) {
            // Check if the network has any of the following transports:
            // - Cellular
            // - Wi-Fi
            // - Ethernet
            isNetworkAvailable = with(networkCapabilities) {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        return isNetworkAvailable
    }


    /**
     * Formats a name by capitalizing the first letter of each word.
     *
     * @param name The name to format.
     * @return The formatted name.
     */
    fun getFormattedDisplayName(name: String): String {
        // Split the name into words.
        val words = name.split(" ")
        // Join the words back together, capitalizing the first letter of each word.
        return words.joinToString(" ") { word ->
            word.replaceFirstChar {
                // Check if the first letter is lowercase.
                if (it.isLowerCase()) {
                    // If it is, capitalize it.
                    it.titlecase(
                        Locale.getDefault()
                    )
                } else {
                    // If it is not, return it as is.
                    it.toString()
                }
            }
        }
    }


    /**
     * Gets the list of post visibility scopes.
     *
     * @param context The context.
     * @return The list of post visibility scopes.
     */
    fun getPostVisibilityList(context: Context): List<PostVisibilityScope> {
        // Create a list of post visibility scopes.
        val postVisibilityScopeList = arrayListOf<PostVisibilityScope>()

        // Add the public visibility scope to the list.
        postVisibilityScopeList.add(
            PostVisibilityScope(
                0,
                context.getString(R.string.public_visibility),
                context.getString(R.string.this_post_will_be_visible_to_every_connect_user),
                R.drawable.ic_lock_open
            )
        )

        // Add the private visibility scope to the list.
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


    /**
     * Gets an ExoPlayer instance.
     *
     * @param context The context.
     * @param uri The URI of the media to play.
     * @return The ExoPlayer instance.
     */
    fun getExoPlayer(context: Context, uri: String): ExoPlayer {
        // Create an ExoPlayer instance.
        val exoPlayer = ExoPlayer.Builder(context)
            .build().apply {
                // Create a DefaultDataSourceFactory.
                val defaultSourceFactory = DefaultDataSource.Factory(context)

                // Create a DefaultDataSourceFactory with the defaultSourceFactory.
                val dataSourceFactory = DefaultDataSource.Factory(context, defaultSourceFactory)

                // Create a ProgressiveMediaSource.Factory with the dataSourceFactory.
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))

                // Set the media source to the ExoPlayer.
                setMediaSource(mediaSource)

                // Prepare the ExoPlayer.
                prepare()
            }
        return exoPlayer
    }

    /**
     * Converts a dp value to a pixel value.
     *
     * @param dp The dp value to convert.
     * @param context The context to use to get the display metrics.
     * @return The pixel value.
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    fun getCurrentTimeInMillis(): Long {
        return Date().time
    }

    fun getLowerCaseUserName(userName: String): String {
        var formattedUserName = ""
        val formattedUserNameList = userName.trim().split(" ")
        formattedUserNameList.forEach {
            if (it.isNotBlank()) {
                formattedUserName += "$it "
            }
        }
        return formattedUserName.trimEnd().lowercase()
    }

}