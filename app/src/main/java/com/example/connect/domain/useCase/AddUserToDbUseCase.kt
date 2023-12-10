package com.example.connect.domain.useCase

import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class AddUserToDbUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun invoke(userDetails: UserDetails) =
        repository.addUserToLocalDb(userDetails)
}