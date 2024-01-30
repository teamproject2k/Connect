package com.example.connect.presentation.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.database.getLongOrNull
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.utils.VisibilityScopeEnum
import com.example.connect.presentation.ui.enums.StatusWithCurrentUserUiEnum
import com.example.connect.presentation.ui.models.VisibilityScope
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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
        // Get the vibrator service from the context.
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)

        // Check if the device has a vibrator.
        if (vibrator?.hasVibrator() == true) {
            // Create a vibration effect.
            val vibrationEffect = VibrationEffect.createOneShot(
                vibrationDuration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )

            // Vibrate the device.
            vibrator.vibrate(vibrationEffect)
        }
    }

    /**
     * Formats the given time in milliseconds to a string in the format "dd MMM yyyy".
     *
     * @param timeInMillis The time in milliseconds.
     * @return The formatted date.
     */
    fun getFormattedDate(timeInMillis: Long): String {
        // Create a Date object from the time in milliseconds.
        val date = Date(timeInMillis)

        // Create a SimpleDateFormat object to format the date.
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Format the date and return the result.
        return formatter.format(date)
    }

    /**
     * Gets the user ID for a given formatted name and current count.
     *
     * @param lowerCaseNameWithoutAnyExtraSpace The formatted name of the user.
     * @param currentCount The current count of users.
     * @return The user ID.
     */
    fun getUserId(lowerCaseNameWithoutAnyExtraSpace: String, currentCount: Int): String {
        // Replace all spaces in the formatted name with an empty string and then converts it to lowercase
        var userIdFirstPart = getConnectIdFirstPart(lowerCaseNameWithoutAnyExtraSpace)
        userIdFirstPart = "$userIdFirstPart@${currentCount + 1}"
        return userIdFirstPart
    }


    /**
     * Gets the first part of the connect ID.
     *
     * @param lowerCaseNameWithoutAnyExtraSpace The lower case name without any extra space.
     * @return The first part of the connect ID.
     */
    fun getConnectIdFirstPart(lowerCaseNameWithoutAnyExtraSpace: String): String {
        var userIdFirstPart = ""
        lowerCaseNameWithoutAnyExtraSpace.split(" ").forEach {
            // If the word is not blank, add the first letter to the user ID first part.
            if (it.isNotBlank()) {
                userIdFirstPart += it[0].lowercase()
            }
        }
        return userIdFirstPart
    }

    /**
     * Shows a toast message.
     *
     * @param message The message to show.
     * @param toastLength The length of the toast.
     */
    fun Context.showToast(message: String, toastLength: Int = Toast.LENGTH_SHORT) {
        // Create a Toast object with the given message and toast length.
        val toast = Toast.makeText(this, message, toastLength)

        // Show the Toast.
        toast.show()
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
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    fun getCurrentTimeInMillis(): Long {
        // Get the current date and time.
        val date = Date()

        /* Get the time in milliseconds since January 1, 1970.
        and return the time in milliseconds.*/
        return date.time
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


    fun getTimeAgo(timestamp: Long, context: Context, useShortNotation: Boolean = false): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val timeAgoDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val currentDateTime = LocalDateTime.now()
        val duration = Duration.between(timeAgoDateTime, currentDateTime)
        return when {
            duration.seconds < 60 -> {
                if (duration.seconds == 1L) {
                    if (useShortNotation) context.getString(R.string._1_sec)
                    else context.getString(R.string._1_sec_ago)
                } else {
                    if (useShortNotation) context.getString(R.string.duration_sec, duration.seconds)
                    else context.getString(R.string.secs_ago, duration.seconds)
                }
            }

            duration.toMinutes() < 60 -> {
                if (duration.toMinutes() == 1L) {
                    if (useShortNotation) context.getString(R.string._1_m)
                    else context.getString(R.string._1_min_ago)
                } else {
                    if (useShortNotation) context.getString(
                        R.string.duration_m,
                        duration.toMinutes()
                    )
                    else context.getString(R.string.mins_ago, duration.toMinutes())
                }
            }

            duration.toHours() < 24 -> {
                if (duration.toHours() == 1L) {
                    if (useShortNotation) context.getString(R.string._1_h)
                    else context.getString(R.string._1_hour_ago)
                } else {
                    if (useShortNotation) context.getString(
                        R.string.duration_h,
                        duration.toHours()
                    )
                    else context.getString(R.string.hours_ago, duration.toHours())
                }
            }

            duration.toDays() < 30 -> {
                if (duration.toDays() == 1L) {
                    if (useShortNotation) context.getString(R.string._1_d)
                    else context.getString(R.string._1_day_ago)
                } else {
                    if (useShortNotation) context.getString(R.string.duration_d, duration.toDays())
                    else context.getString(R.string.days_ago, duration.toDays())
                }
            }

            duration.toDays() < 365 -> {
                if (duration.toDays() / 30 == 1L) {
                    if (useShortNotation) context.getString(R.string._1_mon)
                    else context.getString(R.string._1_month_ago)
                } else {
                    if (useShortNotation) context.getString(
                        R.string.duration_mon,
                        duration.toDays() / 30
                    )
                    else context.getString(R.string.months_ago, duration.toDays() / 30)
                }
            }

            else -> {
                if (duration.toDays() / 365 == 1L) {
                    if (useShortNotation) context.getString(R.string._1_yr)
                    else context.getString(R.string._1_year_ago)
                } else {
                    if (useShortNotation) context.getString(
                        R.string.duration_yr,
                        duration.toDays() / 365
                    )
                    else context.getString(R.string.years_ago, duration.toDays() / 365)
                }
            }
        }
    }

    fun getAccessToken(context: Context): String {
        var token = ""
        try {
            val fileInputStream = context.assets.open("serviceAccountKey.json")
            val googleCredential = GoogleCredentials
                .fromStream(fileInputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredential.refreshIfExpired()
            token = googleCredential.accessToken.tokenValue
        } catch (exception: Exception) {
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ERROR_TAG,
                "getAccessToken",
                exception.localizedMessage ?: ""
            )
        }
        return if (token.isNotBlank()) "Bearer $token" else ""
    }


    fun getMediaType(contentResolver: ContentResolver, uri: Uri): String? {
        return contentResolver.getType(uri)?.substringBefore("/")
    }

    fun getVideoDuration(contentResolver: ContentResolver, uri: Uri): Long {
        val projection = arrayOf(MediaStore.Video.Media.DURATION)
        var duration: Long = 0
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            if (cursor.moveToFirst()) {
                duration = cursor.getLong(durationIndex)
            }
        }
        return duration
    }

    fun getStoryBackgroundColorList(): MutableList<MutableList<Color>> {
        val gradientColorList = mutableListOf<MutableList<Color>>()
        gradientColorList.add(mutableListOf(Color.Black, Color(0xFF262626)))
        gradientColorList.add(mutableListOf(Color(0xFF0000cc), Color(0xFF9999ff)))
        gradientColorList.add(mutableListOf(Color(0xFFcc5200), Color(0xFFFFC299)))
        gradientColorList.add(mutableListOf(Color(0xFF006600), Color(0xFFb3ffb3)))
        gradientColorList.add(mutableListOf(Color(0xFF66004d), Color(0xFFffb3ec)))
        gradientColorList.add(mutableListOf(Color(0xFF0000cc), Color.Cyan))
        return gradientColorList
    }

    fun getStoryTextColorList(): MutableList<Color> {
        return mutableListOf(
            Color(0xFFFFD700),
            Color(0xff8B4513),
            Color.White,
            Color.Black,
            Color.Red,
            Color.Blue,
            Color.Green,
            Color.Magenta,
            Color.Yellow,
            Color.Cyan
        )
    }

    fun getDefaultBackgroundGradient(): MutableList<Color> {
        return (mutableListOf(Color.Black, Color(0xFF262626)))
    }


    /**
     * Parses a string of colors into a list of [Color] objects.
     *
     * @param colorListString The string of colors, separated by the given delimiter.
     * @param delimiter The delimiter used to separate the colors.
     * @return A list of [Color] objects.
     */
    fun getColorListFromColorString(
        colorListString: String,
        delimiter: Char = ','
    ): List<Color> {
        val colorStringList = colorListString.split(delimiter)
        val colorList = arrayListOf<Color>()
        colorStringList.forEach { colorString ->
            colorList.add(getColorFromColorString(colorString))
        }
        return colorList
    }

    /**
     * Converts a color string to a [Color] object.
     *
     * The color string must be in the format "#RRGGBB", where RR, GG, and BB are the
     * hexadecimal values of the red, green, and blue components of the color, respectively.
     *
     * @param colorString The color string to convert.
     * @return The [Color] object representing the color string.
     */
    fun getColorFromColorString(colorString: String): Color {
        val colorInt = colorString.trim().toLong(radix = 16).toInt()
        return Color(colorInt)
    }

    /**
     * Converts a URI to a Bitmap.
     *
     * @param contentResolver The ContentResolver to use.
     * @param selectedFileUri The URI of the file to convert.
     * @return The Bitmap representation of the file, or null if the conversion failed.
     */
    fun uriToBitmap(contentResolver: ContentResolver, selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }
}