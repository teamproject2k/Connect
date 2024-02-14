package com.teamproject2k.connect.data.remote

import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface IRemoteRepository {
    @POST("./messages:send")
    fun sendFcmMessage(
        @Header("Authorization")
        token: String,
        @Body data: JsonObject
    ): Call<JSONObject>
}