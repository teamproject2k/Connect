package com.example.connect.domain.repository

interface IAuthenticationRepository {
    suspend fun sendOtp(mobileNumber: String)
}