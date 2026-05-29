package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val agentLogDao: AgentLogDao,
    private val storeConfigDao: StoreConfigDao,
    private val mercedesCarDao: MercedesCarDao,
    private val cartItemDao: CartItemDao,
    private val orderRecordDao: OrderRecordDao
) {
    val allLogs: Flow<List<AgentLog>> = agentLogDao.getAllLogsFlow()
    val allStoreConfigs: Flow<List<StoreConfig>> = storeConfigDao.getAllStoreConfigsFlow()
    val allCars: Flow<List<MercedesCar>> = mercedesCarDao.getAllCarsFlow()
    val allCartItems: Flow<List<CartItem>> = cartItemDao.getCartItemsFlow()
    val allOrders: Flow<List<OrderRecord>> = orderRecordDao.getAllOrdersFlow()

    // Agent Log methods
    suspend fun insertLog(log: AgentLog) {
        agentLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        agentLogDao.clearAllLogs()
    }

    // Affiliates & dealership configuration
    suspend fun insertStoreConfig(config: StoreConfig) {
        storeConfigDao.insertStoreConfig(config)
    }

    suspend fun updateStoreStatus(id: String, status: String) {
        storeConfigDao.updateStoreStatus(id, status)
    }

    // Cars list endpoints
    suspend fun insertCars(cars: List<MercedesCar>) {
        mercedesCarDao.insertCars(cars)
    }

    suspend fun getCarById(carId: String): MercedesCar? {
        return mercedesCarDao.getCarById(carId)
    }

    // Cart operations
    suspend fun addToCart(carId: String) {
        cartItemDao.addToCart(CartItem(carId, 1))
    }

    suspend fun removeFromCart(carId: String) {
        cartItemDao.removeFromCart(carId)
    }

    suspend fun clearCart() {
        cartItemDao.clearCart()
    }

    // Orders & Tracking operations
    suspend fun placeOrder(order: OrderRecord) {
        orderRecordDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String) {
        orderRecordDao.updateOrderStatus(orderId, newStatus)
    }
    
    suspend fun getAllOrders(): List<OrderRecord> {
        return orderRecordDao.getAllOrders()
    }
}
