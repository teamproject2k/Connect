package com.example.connect.domain.repository

import android.net.Uri
import com.example.connect.domain.network_request_response.ResponseState

interface IUploadFileRepository {

    suspend fun uploadFileToRemote(url: Uri, path: String): ResponseState<String>
}