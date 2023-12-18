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
     *  0 -> Correct
     *  1-> empty mobile number
     *  2-> invalid mobile number
     */
    fun isValidMobileNumber(userMobileNumber: String): Int {
        var responseCode = 0
        when {
            userMobileNumber.isBlank() -> {
                responseCode = 1
            }

            !RegexHelper.PhoneRegex.toRegex().matches(userMobileNumber) -> {
                responseCode = 2
            }
        }
        return responseCode
    }

    /**
     * Validates otp
     * @param otp [String] user entered OTP
     * @return response code of validation
     * 0 -> Correct
     * 1-> empty otp
     * 2-> invalid otp
     */
    fun isValidOTP(otp: String): Int {
        var responseCode = 0
        when {
            otp.isBlank() -> {
                responseCode = 1
            }

            !RegexHelper.OtpRegex.toRegex().matches(otp) -> {
                responseCode = 2
            }
        }
        return responseCode
    }


    /**
     * Validates name
     * @param userName [String] user entered name
     * @return response code of validation
     * 0 -> Correct
     * 1->  empty name
     * 2->  invalid name
     * 3->  max limit of name exceeded
     */
    fun isValidName(userName: String): Int {
        var responseCode = 0
        when {
            userName.isBlank() -> {
                responseCode = 1
            }

            !Regex(RegexHelper.NameRegex).matches(userName) -> {
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
     *  0 -> Correct
     *  1-> empty gender
     *  2-> incorrect gender
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
     * Validates dob
     * @param dob [Long] user entered dob
     * @return response code of validation
     * 0-> correct
     * 1->dob not selected
     * 2-> incorrect dob
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
     *  0 -> Correct
     *  1-> empty bio
     *  2-> max limit of bio exceeded
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