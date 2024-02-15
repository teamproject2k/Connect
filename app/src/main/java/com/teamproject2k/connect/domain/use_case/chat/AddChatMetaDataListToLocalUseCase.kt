package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatMetaDataBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class AddChatMetaDataListToLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Adds a list of chat metadata to the local repository and returns an array of generated IDs.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param chatMetaDataList The list of chat metadata to be added to the local repository.
     * @return An array of generated IDs corresponding to the added chat metadata.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(chatMetaDataList: List<ChatMetaDataBean>): LongArray {
        return repository.addChatMetaDataToLocal(chatMetaDataList)
    }
}