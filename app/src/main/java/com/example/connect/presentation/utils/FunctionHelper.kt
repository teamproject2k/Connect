package com.example.connect.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.connect.R
import com.example.connect.domain.enums.StatusWithCurrentEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.destinations.AddPostScreenDestination
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.HomeScreenDestination
import com.example.connect.presentation.ui.destinations.SearchScreenDestination
import com.example.connect.presentation.ui.models.BottomAppBarItemData
import com.example.connect.presentation.ui.models.PostVisibilityScope
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

    /**
     * Gets the lower case version of the text without extra space.
     *
     * @param stringToFormat The text to be formatted.
     * @return The formatted text without extra space.
     */
    fun getLowerCaseTextWithOutExtraSpace(stringToFormat: String): String {
        var formattedString = ""
        val formattedStringList =
            stringToFormat.trim().split(" ") // Split the user name into a list of words
        formattedStringList.forEach { // Iterate over the list of words
            if (it.isNotBlank()) { // Check if the word is not empty
                formattedString += "$it " // Add the word to the formatted user name
            }
        }
        return formattedString.trimEnd().lowercase()
    }


    /**
     * Gets the list of items to be displayed in the bottom navigation bar of home activity.
     *
     * @param context The context of the application.
     * @return The list of items to be displayed in the bottom navigation bar.
     */
    fun getBottomNavBarItemList(context: Context): ArrayList<BottomAppBarItemData> {
        val bottomNavList = arrayListOf<BottomAppBarItemData>()
        bottomNavList.add(
            BottomAppBarItemData(
                context.getString(R.string.home),
                Icons.Filled.Home,
                Icons.Outlined.Home,
                HomeScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                context.getString(R.string.search),
                Icons.Filled.Search,
                Icons.Outlined.Search,
                SearchScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                context.getString(R.string.add_post),
                Icons.Filled.AddCircle,
                Icons.Outlined.AddCircleOutline,
                AddPostScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                context.getString(R.string.chat),
                Icons.Filled.ChatBubble,
                Icons.Outlined.ChatBubbleOutline,
                AddPostScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                context.getString(R.string.profile),
                Icons.Filled.Person,
                Icons.Outlined.Person,
                CurrentUserProfileScreenDestination.route
            )
        )
        return bottomNavList
    }

    fun getStatusWithCurrentUser(
        currentUsersBean: UsersBean,
        requiredUsersBean: UsersBean
    ): String {
        return when {
            currentUsersBean.friendList.contains(requiredUsersBean.firebaseUserId) -> {
                StatusWithCurrentEnum.Friends.name
            }

            currentUsersBean.blockedUsersList.contains(requiredUsersBean.firebaseUserId) -> {
                StatusWithCurrentEnum.Blocked.name
            }

            currentUsersBean.receivedFriendRequestList.contains(requiredUsersBean.firebaseUserId) -> {
                StatusWithCurrentEnum.RequestedByOtherUser.name
            }

            currentUsersBean.requestedFriendRequestList.contains(requiredUsersBean.firebaseUserId) -> {
                StatusWithCurrentEnum.RequestedByCurrentUser.name
            }

            else -> {
                StatusWithCurrentEnum.NotFriends.name
            }
        }
    }
}