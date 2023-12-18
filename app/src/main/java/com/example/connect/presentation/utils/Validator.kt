package com.example.connect.presentation.utils

import android.content.Context
import com.example.connect.R
import java.util.Date

object Validator {

    fun isValidMobileNumber(userMobileNumber: String): Boolean {
        return RegexHelper.PhoneRegex.toRegex().matches(userMobileNumber)
    }

    fun isValidOTP(otp: String): Boolean {
        return RegexHelper.OtpRegex.toRegex().matches(otp)
    }

    fun isValidName(userName: String): Boolean {
        return userName.isNotBlank() && userName.length <= ConstantsHelper.NAME_MAX_CHAR_LIMIT
                && Regex(RegexHelper.NameRegex).matches(userName)
    }

    fun isValidGender(gender: String, context: Context): Boolean {
        val genderList = context.resources.getStringArray(R.array.gender_list)
        return gender in genderList
    }

    fun isValidDob(dob: Long): Boolean {
        return dob != -1L && Date(dob).before(Date())
    }

    fun isValidBio(bio: String): Boolean {
        return bio.isNotBlank() && bio.length <= ConstantsHelper.BIO_MAX_CHAR_LIMIT
    }

}