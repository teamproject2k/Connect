package com.example.connect.domain.useCase.upload_file

import android.net.Uri
import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class UploadFileToRemoteUseCase @Inject constructor(private val repository: IHomeRepository) {


    suspend fun invoke(uri: Uri,path:String): ResponseState<String> {
        return repository.uploadFileToRemote(uri, path)
    }
}