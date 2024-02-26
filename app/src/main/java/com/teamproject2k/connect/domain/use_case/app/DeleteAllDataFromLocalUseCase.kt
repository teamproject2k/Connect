package com.teamproject2k.connect.domain.use_case.app

import com.teamproject2k.connect.domain.repository.IAppLocalRepository
import javax.inject.Inject

class DeleteAllDataFromLocalUseCase @Inject constructor(private val repository: IAppLocalRepository) {
    suspend operator fun invoke() {
        repository.deleteAllTables()
    }
}