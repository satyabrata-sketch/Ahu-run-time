package com.example.data

import kotlinx.coroutines.flow.Flow

class AhuRepository(private val ahuDao: AhuDao) {
    val allEntries: Flow<List<AhuEntry>> = ahuDao.getAllEntries()

    fun getEntriesForUser(userId: String): Flow<List<AhuEntry>> {
        return ahuDao.getEntriesForUser(userId)
    }

    suspend fun insertEntry(entry: AhuEntry) {
        ahuDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: AhuEntry) {
        ahuDao.deleteEntry(entry)
    }

    suspend fun deleteEntryById(id: Int) {
        ahuDao.deleteEntryById(id)
    }

    suspend fun clearAll() {
        ahuDao.clearAll()
    }
}
