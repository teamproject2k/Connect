package com.example.connect.domain.useCase.upload_file

import android.net.Uri
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUploadFileRepository
import javax.inject.Inject

class UploadFileToRemoteUseCase @Inject constructor(private val repository: IUploadFileRepository) {

    suspend fun invoke(uri: Uri, path: String): ResponseState<String> {
        return repository.uploadFileToRemote(uri, path)
    }
}