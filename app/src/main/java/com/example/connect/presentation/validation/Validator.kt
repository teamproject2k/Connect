package com.example.connect.presentation.validation

import android.content.Context
import com.example.connect.R
import com.example.connect.presentation.utils.ConstantsHelper
import java.util.Date

object Validator {

    /**
     *  Validates mobile number
     *  @param userMobileNumber [String] user entered mobile number
     *  @return response code of validation
     *
     *  0 -> Correct
     *
     *  1-> Empty mobile number
     *
     *  2-> Invalid mobile number
     */
    fun isValidMobileNumber(userMobileNumber: String): Int {
        var responseCode = 0
        when {
            userMobileNumber.isBlank() -> {
                responseCode = 1
            }

            !RegexHelper.PHONE_REGEX.toRegex().matches(userMobileNumber) -> {
                responseCode = 2
            }
        }
        return responseCode
    }

    /**
     * Validates otp
     * @param otp [String] user entered OTP
     * @return response code of validation
     *
     * 0-> Correct
     *
     * 1-> Empty otp
     *
     * 2-> Invalid otp
     */
    fun isValidOTP(otp: String): Int {
        var responseCode = 0
        when {
            otp.isBlank() -> {
                responseCode = 1
            }

            !RegexHelper.OTP_REGEX.toRegex().matches(otp) -> {
                responseCode = 2
            }
        }
        return responseCode
    }


    /**
     * Validates name
     * @param userName [String] user entered name
     * @return response code of validation
     *
     * 0 -> Correct
     *
     * 1->  Empty name
     *
     * 2->  Invalid name
     *
     * 3->  Max limit of name exceeded
     */
    fun isValidName(userName: String): Int {
        var responseCode = 0
        when {
            userName.isBlank() -> {
                responseCode = 1
            }

            !Regex(RegexHelper.NAME_REGEX).matches(userName) -> {
                responseCode = 2
            }

            userName.length > ConstantsHelper.NAME_MAX_CHAR_LIMIT -> {
                responseCode = 3
            }
        }
        return responseCode
    }

    /**
     * Validates gender
     *  @param gender [String] user selected gender
     *  @param context [String]
     *  @return response code of validation
     *
     *  0 -> Correct
     *
     *  1-> Empty gender
     *
     *  2-> Incorrect gender
     */
    fun isValidGender(gender: String, context: Context): Int {
        val genderList = context.resources.getStringArray(R.array.gender_list)
        var responseCode = 0
        when {
            gender.isBlank() -> {
                responseCode = 1
            }

            gender !in genderList -> {
                responseCode = 2
            }
        }
        return responseCode
    }


    /**
     * Validates DOB
     * @param dob [Long] user entered DOB
     * @return response code of validation
     *
     * 0-> Correct
     *
     * 1-> DOB not selected
     *
     * 2-> Incorrect DOB
     */
    fun isValidDob(dob: Long): Int {
        var responseCode = 0
        when {
            dob == -1L -> {
                responseCode = 1
            }

            !Date(dob).before(Date()) -> {
                responseCode = 2
            }
        }
        return responseCode
    }


    /**
     * Validates bio
     *  @param bio [String] user entered
     *  @return response code of validation
     *
     *  0 -> Correct
     *
     *  1-> Empty bio
     *
     *  2-> Max limit of bio exceeded
     */
    fun isValidBio(bio: String): Int {
        var responseCode = 0
        when {
            bio.isBlank() -> {
                responseCode = 1
            }

            bio.length > ConstantsHelper.BIO_MAX_CHAR_LIMIT -> {
                responseCode = 2
            }
        }
        return responseCode
    }

}