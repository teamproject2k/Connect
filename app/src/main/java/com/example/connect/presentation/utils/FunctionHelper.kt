package com.example.connect.presentation.utils

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.database.getLongOrNull
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.connect.R
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.utils.VisibilityScopeEnum
import com.example.connect.presentation.ui.enums.StatusWithCurrentUserUiEnum
import com.example.connect.presentation.ui.models.VisibilityScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow


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
    fun getPostVisibilityList(context: Context): List<VisibilityScope> {
        // Create a list of post visibility scopes.
        val postVisibilityScopeList = arrayListOf<VisibilityScope>()

        // Add the public visibility scope to the list.
        postVisibilityScopeList.add(
            VisibilityScope(
                0,
                context.getString(R.string.public_visibility),
                VisibilityScopeEnum.Public,
                context.getString(R.string.this_post_will_be_visible_to_every_connect_user),
                R.drawable.ic_lock_open
            )
        )

        // Add the friends only visibility scope to the list.
        postVisibilityScopeList.add(
            VisibilityScope(
                1,
                context.getString(R.string.friends_only_visibility),
                VisibilityScopeEnum.FriendsOnly,
                context.getString(R.string.this_post_will_only_be_visible_to_your_friends),
                R.drawable.ic_lock_friends_only
            )
        )
        return postVisibilityScopeList
    }

    /**
     * Gets the list of gender visibility scopes.
     *
     * @param context The context.
     * @return The list of gender visibility scopes.
     */
    fun getGenderVisibilityList(context: Context): List<VisibilityScope> {
        // Create a list of gender visibility scopes.
        val genderVisibilityScopeList = arrayListOf<VisibilityScope>()

        // Add the public visibility scope to the list.
        genderVisibilityScopeList.add(
            VisibilityScope(
                0,
                context.getString(R.string.public_visibility),
                VisibilityScopeEnum.Public,
                context.getString(R.string.your_gender_will_be_visible_to_everyone),
                R.drawable.ic_lock_open
            )
        )

        // Add the friends only visibility scope to the list.
        genderVisibilityScopeList.add(
            VisibilityScope(
                1,
                context.getString(R.string.friends_only_visibility),
                VisibilityScopeEnum.FriendsOnly,
                context.getString(R.string.your_gender_will_only_be_visible_to_your_friends),
                R.drawable.ic_lock_friends_only
            )
        )

        // Add the private visibility scope to the list.
        genderVisibilityScopeList.add(
            VisibilityScope(
                2,
                context.getString(R.string.private_visibility),
                VisibilityScopeEnum.Private,
                context.getString(R.string.your_gender_will_only_be_visible_to_you),
                R.drawable.ic_lock_close
            )
        )
        return genderVisibilityScopeList
    }

    /**
     * Gets the list of dob visibility scopes.
     *
     * @param context The context.
     * @return The list of dob visibility scopes.
     */
    fun getDobVisibilityList(context: Context): List<VisibilityScope> {
        // Create a list of dob visibility scopes.
        val dobVisibilityScopeList = arrayListOf<VisibilityScope>()

        // Add the public visibility scope to the list.
        dobVisibilityScopeList.add(
            VisibilityScope(
                0,
                context.getString(R.string.public_visibility),
                VisibilityScopeEnum.Public,
                context.getString(R.string.your_dob_will_be_visible_to_everyone),
                R.drawable.ic_lock_open
            )
        )

        // Add the friends only visibility scope to the list.
        dobVisibilityScopeList.add(
            VisibilityScope(
                1,
                context.getString(R.string.friends_only_visibility),
                VisibilityScopeEnum.FriendsOnly,
                context.getString(R.string.your_dob_will_only_be_visible_to_your_friends),
                R.drawable.ic_lock_friends_only
            )
        )

        // Add the private visibility scope to the list.
        dobVisibilityScopeList.add(
            VisibilityScope(
                2,
                context.getString(R.string.private_visibility),
                VisibilityScopeEnum.Private,
                context.getString(R.string.your_dob_will_only_be_visible_to_you),
                R.drawable.ic_lock_close
            )
        )
        return dobVisibilityScopeList
    }

    /**
     * Gets the list of friendList visibility scopes.
     *
     * @param context The context.
     * @return The list of friendList visibility scopes.
     */
    fun getFriendListVisibilityList(context: Context): List<VisibilityScope> {
        // Create a list of friend list visibility scopes.
        val friendListVisibilityScopeList = arrayListOf<VisibilityScope>()

        // Add the public visibility scope to the list.
        friendListVisibilityScopeList.add(
            VisibilityScope(
                0,
                context.getString(R.string.public_visibility),
                VisibilityScopeEnum.Public,
                context.getString(R.string.your_friends_will_be_visible_to_everyone),
                R.drawable.ic_lock_open
            )
        )

        // Add the friends only visibility scope to the list.
        friendListVisibilityScopeList.add(
            VisibilityScope(
                1,
                context.getString(R.string.friends_only_visibility),
                VisibilityScopeEnum.FriendsOnly,
                context.getString(R.string.your_friends_will_only_be_visible_to_your_friends),
                R.drawable.ic_lock_friends_only
            )
        )

        // Add the private visibility scope to the list.
        friendListVisibilityScopeList.add(
            VisibilityScope(
                2,
                context.getString(R.string.private_visibility),
                VisibilityScopeEnum.Private,
                context.getString(R.string.your_friends_will_only_be_visible_to_you),
                R.drawable.ic_lock_close
            )
        )
        return friendListVisibilityScopeList
    }

    /**
     * Gets an ExoPlayer instance.
     *
     * @param context The context.
     * @param uri The URI of the media to play.
     * @return The ExoPlayer instance.
     */
    @OptIn(UnstableApi::class)
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
     * Gets the status of the current user with the requested user.
     *
     * @param currentUsersBean The current user's bean.
     * @param requiredUsersBean The required user's bean.
     * @return The status of the current user with the requested user.
     */
    fun getStatusWithCurrentUser(
        currentUsersBean: UsersBean,
        requiredUsersBean: UsersBean
    ): String {
        // Check if the required user is a friend of the current user.
        if (currentUsersBean.friendList.contains(requiredUsersBean.firebaseUserId)) {
            return StatusWithCurrentUserUiEnum.Friends.name
        }

        // Check if the required user has blocked the current user.
        if (currentUsersBean.blockedUsersList.contains(requiredUsersBean.firebaseUserId)) {
            return StatusWithCurrentUserUiEnum.BlockedByCurrentUser.name
        }

        if (requiredUsersBean.blockedUsersList.contains(currentUsersBean.firebaseUserId)) {
            return StatusWithCurrentUserUiEnum.BlockedByOtherUser.name
        }

        // Check if the required user has sent a friend request to the current user.
        if (currentUsersBean.receivedFriendRequestList.contains(requiredUsersBean.firebaseUserId)) {
            return StatusWithCurrentUserUiEnum.RequestedByOtherUser.name
        }

        // Check if the current user has sent a friend request to the required user.
        if (currentUsersBean.requestedFriendRequestList.contains(requiredUsersBean.firebaseUserId)) {
            return StatusWithCurrentUserUiEnum.RequestedByCurrentUser.name
        }

        // If none of the above conditions are met, then the required user is not a friend of the current user.
        return StatusWithCurrentUserUiEnum.NotFriends.name
    }

    /**
     * Gets the file size of a content URI.
     *
     * @param contentResolver The content resolver to use.
     * @param uri The content URI to get the file size of.
     * @return The file size in bytes.
     */
    fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
        // Get a cursor for the content URI.
        val cursor = contentResolver.query(uri, null, null, null, null)

        // Initialize the file size to 0.
        var fileSize: Long = 0

        // If the cursor is not null, move to the first row and get the file size.
        cursor?.use {
            if (it.moveToFirst()) {
                // Get the index of the SIZE column.
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

                // Get the file size in bytes.
                fileSize = it.getLongOrNull(sizeIndex) ?: 0
            }
        }

        // Return the file size.
        return fileSize
    }

    /**
     * Formats a file size in bytes to a human-readable string.
     *
     * @param size The file size in bytes.
     * @return A human-readable string representing the file size.
     */
    fun formatFileSize(size: Long): String {
        // If the file size is 0, return "0 B".
        if (size <= 0) return "0 B"

        // Create an array of units (B, KB, MB, GB, TB).
        val units = arrayOf("B", "KB", "MB", "GB", "TB")

        // Calculate the number of digit groups in the file size.
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()

        // Return a string formatted with the file size and unit.
        return String.format(
            "%.2f %s",
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}