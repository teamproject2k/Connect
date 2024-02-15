package com.teamproject2k.connect.domain.repository

import android.net.Uri
import com.teamproject2k.connect.domain.network_utils.ResponseState

interface IUploadFileRepository {
    /**
     * Uploads a file to a remote server.
     *
     * @param url The URL of the remote server.
     * @param path The path to the file to upload.
     * @return A [ResponseState] containing the result of the upload.
     */
    suspend fun uploadFileToRemote(url: Uri, path: String): ResponseState<String>
}