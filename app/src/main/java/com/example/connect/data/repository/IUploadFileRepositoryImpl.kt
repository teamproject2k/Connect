package com.example.connect.data.repository

import android.net.Uri
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUploadFileRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IUploadRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : IUploadFileRepository {
    override suspend fun uploadFileToRemote(url: Uri, path: String): ResponseState<String> {
        // Upload the file to the specified path in Firebase Storage.
        return try {
            val downloadUrl = firebaseStorage.reference.child(path).putFile(url)
                .await().storage.downloadUrl.await()
            // Return the download URL as a success state.
            ResponseState.success(downloadUrl.toString())
        } catch (exception: Exception) {
            // Return an error state with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }
}