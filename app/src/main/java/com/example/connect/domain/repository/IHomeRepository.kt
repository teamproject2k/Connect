package com.example.connect.domain.repository

import android.net.Uri
import com.example.connect.common.ResponseState

interface IHomeRepository {


    /**
     * Uploads a file to a remote server.
     *
     * @param url The URL of the remote server.
     * @param path The path to the file to be uploaded.
     * @return A [ResponseState] containing the result of the upload.
     */
    suspend fun uploadFileToRemote(url: Uri, path: String): ResponseState<String>

    suspend fun updateUserDetailsOnServer(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?>

    suspend fun getUsersFromName(name: Any): ResponseState<Int>

    suspend fun updateImageOnRemoteStorage(
        imageUri: Uri?,
        firebaseUserId: String,
        parameterToUpdate: String
    ): ResponseState<String>

    suspend fun updateUserDetailsOnLocal(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long


}