package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AgentLog::class, StoreConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentLogDao(): AgentLogDao
    abstract fun storeConfigDao(): StoreConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omnilink_ai_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialStores(database.storeConfigDao())
                    populateInitialLogs(database.agentLogDao())
                }
            }
        }

        suspend fun populateInitialStores(dao: StoreConfigDao) {
            val initialStores = listOf(
                StoreConfig(
                    storeId = "alibaba",
                    storeName = "AliBaba Sourcing",
                    affiliateLink = "https://www.alibaba.com",
                    productCount = 1450,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "etsy",
                    storeName = "Etsy Affiliate",
                    affiliateLink = "https://www.etsy.com",
                    productCount = 380,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "ebay",
                    storeName = "eBay Connector",
                    affiliateLink = "https://www.ebay.com",
                    productCount = 720,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "cjdropshipping",
                    storeName = "CJ Dropshipping",
                    affiliateLink = "https://www.cjdropshipping.com/contactus#online",
                    productCount = 1890,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "digistore24",
                    storeName = "DigiStore24 Hub",
                    affiliateLink = "https://www.digistore24.com/redir/431152/globalwarming/",
                    productCount = 150,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "mercedes",
                    storeName = "Mercedes-Benz Birmingham",
                    affiliateLink = "https://www.mbbhm.com/finance/affiliates/",
                    productCount = 28,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "aliexpress",
                    storeName = "AliExpress China",
                    affiliateLink = "https://www.aliexpress.com",
                    productCount = 2150,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "daraz",
                    storeName = "Daraz.pk PK Direct",
                    affiliateLink = "https://www.daraz.pk",
                    productCount = 920,
                    status = "CONNECTED",
                    isGatewayActive = true
                ),
                StoreConfig(
                    storeId = "wed2c",
                    storeName = "Wed2C Emporium",
                    affiliateLink = "https://theamericanemporiu.wed2c.com",
                    productCount = 440,
                    status = "CONNECTED",
                    isGatewayActive = true
                )
            )
            dao.insertStoreConfigs(initialStores)
        }

        suspend fun populateInitialLogs(dao: AgentLogDao) {
            val initialLogs = listOf(
                AgentLog(
                    agentName = "Sourcing Agent",
                    subAgentName = "Ali Sourcing Bot",
                    actionDetails = "Successfully connected and synchronized 1,450 wholesale products from Ali Baba catalog.",
                    status = "SUCCESS"
                ),
                AgentLog(
                    agentName = "Listing & Pricing",
                    subAgentName = "Store Syncer",
                    actionDetails = "Exported 42 new active listings with dynamic prices matching profit margin parameters.",
                    status = "SUCCESS"
                ),
                AgentLog(
                    agentName = "Creative Marketer",
                    subAgentName = "Copy Generator",
                    actionDetails = "Generated TikTok/Insta ad assets for 'Hot Summer Deal' catalog & dispatched to social pipeline.",
                    status = "SUCCESS"
                ),
                AgentLog(
                    agentName = "Payment & Gateway",
                    subAgentName = "Vault Router",
                    actionDetails = "Unified Gateway validated standard API routes with 100% active operational uptime.",
                    status = "SUCCESS"
                )
            )
            initialLogs.forEach { dao.insertLog(it) }
        }
    }
}
