package com.example.connect.presentation.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date


class ValidatorTest {

    @Test
    fun validMobileNumber_ReturnsTrue() {
        val mobileNumber = "9999999999"
        assertTrue(Validator.isValidMobileNumber(mobileNumber))
    }

    @Test
    fun inValidMobileNumberOnFirstDigitLessThan6_ReturnsFalse() {
        val mobileNumber = "1999999999"
        assertFalse(Validator.isValidMobileNumber(mobileNumber))
    }

    @Test
    fun inValidMobileNumberOnAlphabetInBetween_ReturnsFalse() {
        val mobileNumber = "99999a9999"
        assertFalse(Validator.isValidMobileNumber(mobileNumber))
    }

    @Test
    fun inValidMobileNumberOnLengthNotEqualTo10_ReturnsFalse() {
        val mobileNumber = "98765432109"
        assertFalse(Validator.isValidMobileNumber(mobileNumber))
    }

    @Test
    fun validOTP_ReturnsTrue() {
        val otp = "123456"
        assertTrue(Validator.isValidOTP(otp))
    }

    @Test
    fun inValidOTPOnAlphabetInBetween_ReturnsFalse() {
        val mobileNumber = "123a45"
        assertFalse(Validator.isValidOTP(mobileNumber))
    }

    @Test
    fun inValidOTPOnLengthNotEqualTo6_ReturnsFalse() {
        val otp = "1234567"
        assertFalse(Validator.isValidOTP(otp))
    }

    @Test
    fun validName_ReturnsTrue() {
        val name = "Aryan Mishra"
        assertTrue(Validator.isValidName(name))
    }

    @Test
    fun inValidNameOnBlankName_ReturnsFalse() {
        val name = ""
        assertFalse(Validator.isValidName(name))
    }

    @Test
    fun inValidNameOnNonAlphabetCharacters_ReturnsFalse() {
        val name = "Aryan5Mishra"
        assertFalse(Validator.isValidName(name))
    }

    @Test
    fun inValidNameOnLengthGreaterThan50Characters_ReturnsFalse() {
        val name = "Hi hello namaste bonjour hello namaste bonjour hi namaste hello"
        assertFalse(Validator.isValidName(name))
    }

    @Test
    fun validBio_ReturnsTrue() {
        val bio = "Proudly2000Indian"
        assertTrue(Validator.isValidBio(bio))
    }

    @Test
    fun inValidBioOnBlankBio_ReturnsFalse() {
        val bio = ""
        assertFalse(Validator.isValidBio(bio))
    }

    @Test
    fun inValidBioOnLengthGreaterThan100Characters_ReturnsFalse() {
        val bio =
            "Hi hello namaste bonjour hello namaste bonjour hi namaste hello namaste hi hello bonjour hi hello namaste"
        assertFalse(Validator.isValidBio(bio))
    }

    @Test
    fun validDOB_ReturnsTrue() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.YEAR, -2)
        assertTrue(Validator.isValidDob(calendar.timeInMillis))
    }

    @Test
    fun inValidDOBOnNoDateSelected_ReturnsFalse() {
        val dob = -1L
        assertFalse(Validator.isValidDob(dob))
    }

    @Test
    fun inValidDOBOnDateGreaterThanCurrentDate_ReturnsFalse() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.YEAR, 2)
        assertFalse(Validator.isValidDob(calendar.timeInMillis))
    }
}