package com.teamproject2k.connect.domain.use_case.file

import android.net.Uri
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUploadFileRepository
import javax.inject.Inject

class UploadFileToRemoteUseCase @Inject constructor(private val repository: IUploadFileRepository) {
    /**
     * Invokes the upload file to remote function in the repository.
     *
     * @param uri The URI of the file to upload.
     * @param path The path to the file on the remote server.
     * @return A [ResponseState] containing the result of the upload.
     */
    suspend operator fun invoke(uri: Uri, path: String): ResponseState<String> {
        return repository.uploadFileToRemote(uri, path)
    }
}