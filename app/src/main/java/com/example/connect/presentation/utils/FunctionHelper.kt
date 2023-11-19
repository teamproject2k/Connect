package com.example.connect.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    suspend fun getUsersFromName(fireStore: FirebaseFirestore, name: String): ResponseState<Int> {
        return try {
            val result = fireStore.collection(FirebaseConstants.UsersKey)
                .whereEqualTo(UserDetails::name.name, name).get().await()
            ResponseState.success(result.size())
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }
}