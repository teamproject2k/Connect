package com.example.connect.presentation.validation

import com.example.connect.presentation.utils.ConstantsHelper

object RegexHelper {
    const val PHONE_REGEX = "^[6-9][0-9]{9}$"
    const val OTP_REGEX = "^[0-9]{${ConstantsHelper.OTP_CHAR_COUNT}}$"
    const val NAME_REGEX = "^[^0-9!\$@#%^&*()_+{}\\[\\]:;<>,.?~|]*\$"
}