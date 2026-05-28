package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "agent_logs")
data class AgentLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agentName: String,
    val subAgentName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionDetails: String,
    val status: String // "SUCCESS", "RUNNING", "WARNING", "FAILED"
)

@Entity(tableName = "store_configs")
data class StoreConfig(
    @PrimaryKey val storeId: String, // unique key e.g., "ebay", "aliexpress", etc.
    val storeName: String,
    val affiliateLink: String,
    val productCount: Int,
    val status: String, // "CONNECTED", "SYNCING", "OFFLINE"
    val isGatewayActive: Boolean,
    val autoSyncIntervalMinutes: Int = 15
)

@Dao
interface AgentLogDao {
    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT 150")
    fun getAllLogsFlow(): Flow<List<AgentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AgentLog)

    @Query("DELETE FROM agent_logs")
    suspend fun clearAllLogs()
}

@Dao
interface StoreConfigDao {
    @Query("SELECT * FROM store_configs")
    fun getAllStoreConfigsFlow(): Flow<List<StoreConfig>>

    @Query("SELECT * FROM store_configs")
    suspend fun getAllStoreConfigs(): List<StoreConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreConfig(config: StoreConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreConfigs(configs: List<StoreConfig>)

    @Query("UPDATE store_configs SET productCount = :count WHERE storeId = :id")
    suspend fun updateProductCount(id: String, count: Int)

    @Query("UPDATE store_configs SET status = :status WHERE storeId = :id")
    suspend fun updateStoreStatus(id: String, status: String)
}
