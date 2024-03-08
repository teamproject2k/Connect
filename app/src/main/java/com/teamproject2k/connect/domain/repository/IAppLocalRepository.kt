package com.teamproject2k.connect.domain.repository

interface IAppLocalRepository {
    /**
     * Deletes all tables from the local database.
     */
    suspend fun deleteAllTables()
}