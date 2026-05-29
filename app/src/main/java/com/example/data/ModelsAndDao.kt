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
    @PrimaryKey val storeId: String, // e.g., "greenwich", "birmingham", "mercedes_hq"
    val storeName: String,
    val affiliateLink: String,
    val productCount: Int,
    val status: String, // "CONNECTED", "SYNCING", "OFFLINE"
    val isGatewayActive: Boolean,
    val autoSyncIntervalMinutes: Int = 15
)

@Entity(tableName = "mercedes_cars")
data class MercedesCar(
    @PrimaryKey val carId: String,
    val modelName: String,
    val category: String, // "NEW", "USED", "VINTAGE", "LUXURY_PARTS"
    val priceUSD: Double,
    val commissionPercent: Double,
    val description: String,
    val affiliateUrl: String,
    val conditionDetails: String, // e.g., "Full factory warranty", "Refurbished - Certified", "Pristine Collectible"
    val modelYear: Int,
    val specifications: String // e.g., "C300, 2.0L Turbo, 255HP, Obsidian Metallic"
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val carId: String,
    val quantity: Int = 1
)

@Entity(tableName = "order_records")
data class OrderRecord(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    val itemsSummary: String,
    val customerName: String,
    val paymentGateway: String, // "JazzCash", "EasyPaisa", "Debit Card / IBAN", "PayPal", "Payoneer", "Crypto"
    val totalAmountUSD: Double,
    val commissionEarnedUSD: Double,
    val orderTime: Long = System.currentTimeMillis(),
    val trackingStatus: String // "Order Placed", "Escrow Verified", "Commission Safe-Escrowed", "Seller Dispatched"
)

@Dao
interface AgentLogDao {
    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT 150")
    fun getAllLogsFlow(): Flow<List<AgentLog>>

    @Query("SELECT COUNT(*) FROM agent_logs")
    suspend fun getLogsCount(): Int

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

    @Query("UPDATE store_configs SET status = :status WHERE storeId = :id")
    suspend fun updateStoreStatus(id: String, status: String)
}

@Dao
interface MercedesCarDao {
    @Query("SELECT * FROM mercedes_cars")
    fun getAllCarsFlow(): Flow<List<MercedesCar>>

    @Query("SELECT * FROM mercedes_cars")
    suspend fun getAllCars(): List<MercedesCar>

    @Query("SELECT * FROM mercedes_cars WHERE category = :category")
    fun getCarsByCategoryFlow(category: String): Flow<List<MercedesCar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCars(cars: List<MercedesCar>)

    @Query("SELECT * FROM mercedes_cars WHERE carId = :carId")
    suspend fun getCarById(carId: String): MercedesCar?
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items")
    suspend fun getCartItems(): List<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItem)

    @Query("DELETE FROM cart_items WHERE carId = :carId")
    suspend fun removeFromCart(carId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderRecordDao {
    @Query("SELECT * FROM order_records ORDER BY orderTime DESC")
    fun getAllOrdersFlow(): Flow<List<OrderRecord>>

    @Query("SELECT * FROM order_records ORDER BY orderTime DESC")
    suspend fun getAllOrders(): List<OrderRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderRecord)

    @Query("UPDATE order_records SET trackingStatus = :newStatus WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: String)
}
