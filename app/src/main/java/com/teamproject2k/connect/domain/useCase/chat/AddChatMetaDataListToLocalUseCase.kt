package com.teamproject2k.connect.domain.useCase.chat

import com.teamproject2k.connect.domain.models.ChatMetaDataBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class AddChatMetaDataListToLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(chatMetaDataList: List<ChatMetaDataBean>): LongArray {
        return repository.addChatMetaDataToLocal(chatMetaDataList)
    }
}