package com.teamproject2k.connect.domain.repository

interface IAppLocalRepository {
    suspend fun deleteAllTables()
}