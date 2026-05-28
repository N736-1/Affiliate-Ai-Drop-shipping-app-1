package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val agentLogDao: AgentLogDao,
    private val storeConfigDao: StoreConfigDao
) {
    val allLogs: Flow<List<AgentLog>> = agentLogDao.getAllLogsFlow()
    val allStoreConfigs: Flow<List<StoreConfig>> = storeConfigDao.getAllStoreConfigsFlow()

    suspend fun insertLog(log: AgentLog) {
        agentLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        agentLogDao.clearAllLogs()
    }

    suspend fun insertStoreConfig(config: StoreConfig) {
        storeConfigDao.insertStoreConfig(config)
    }

    suspend fun updateProductCount(id: String, count: Int) {
        storeConfigDao.updateProductCount(id, count)
    }

    suspend fun updateStoreStatus(id: String, status: String) {
        storeConfigDao.updateStoreStatus(id, status)
    }
}
