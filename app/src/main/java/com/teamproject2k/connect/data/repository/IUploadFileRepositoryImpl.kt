package com.teamproject2k.connect.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUploadFileRepository
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