package com.example.connect.presentation.utils

object RegexHelper {
    const val PhoneRegex = "^[6-9][0-9]{9}$"
    const val OtpRegex = "^[0-9]{${ConstantsHelper.OTPCharCount}}$"
    const val NameRegex = "^[^0-9!\$@#%^&*()_+{}\\[\\]:;<>,.?~|]*\$"
}