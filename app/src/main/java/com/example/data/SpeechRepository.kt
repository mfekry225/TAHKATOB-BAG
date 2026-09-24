package com.example.data

import com.example.data.local.ChildEntity
import com.example.data.local.SessionLogEntity
import com.example.data.local.SpeechDao
import kotlinx.coroutines.flow.Flow

class SpeechRepository(private val speechDao: SpeechDao) {
    val allChildren: Flow<List<ChildEntity>> = speechDao.getAllChildren()

    fun getChildById(id: Int): Flow<ChildEntity?> {
        return speechDao.getChildById(id)
    }

    suspend fun insertChild(child: ChildEntity): Long {
        return speechDao.insertChild(child)
    }

    suspend fun updateChild(child: ChildEntity) {
        speechDao.updateChild(child)
    }

    suspend fun deleteChild(child: ChildEntity) {
        speechDao.deleteChild(child)
    }

    fun getSessionLogsForChild(childId: Int): Flow<List<SessionLogEntity>> {
        return speechDao.getSessionLogsForChild(childId)
    }

    suspend fun insertSessionLog(log: SessionLogEntity): Long {
        return speechDao.insertSessionLog(log)
    }

    suspend fun deleteSessionLog(log: SessionLogEntity) {
        speechDao.deleteSessionLog(log)
    }
}
