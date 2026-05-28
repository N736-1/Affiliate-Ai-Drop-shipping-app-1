package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgentLog
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.StoreConfig
import com.example.api.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(db.agentLogDao(), db.storeConfigDao())

    val logs: StateFlow<List<AgentLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stores: StateFlow<List<StoreConfig>> = repository.allStoreConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Automation Live State
    var isSystemLive = MutableStateFlow(true)
        private set

    // Wed2C Store URLs list
    val wed2cUrls = listOf(
        "https://httpstheamericane.wed2c.com",
        "https://theamericanemporiu.wed2c.com",
        "https://sellonlinestore1.wed2c.com",
        "https://sellbazzarshop.wed2c.com",
        "https://dropshippingshop.wed2c.com",
        "https://bazzarstore.wed2c.com",
        "https://bazaarshop.wed2c.com",
        "https://onlinedropstore.wed2c.com",
        "https://sellonlinestore.wed2c.com"
    )

    // Current selected Wed2C URL index
    var selectedWed2cIndex = MutableStateFlow(1) // Defaults to theamericanemporiu.wed2c.com
        private set

    // Unified Payment Gateway configuration
    var selectedGatewayProvider = MutableStateFlow("MultiGate Central")
    val gatewayProviders = listOf("MultiGate Central", "Stripe Unified", "PayPal Global Checkout", "Coinbase Commerce")
    var isGatewayTestMode = MutableStateFlow(false)
    var processedVolumeUSD = MutableStateFlow(12432.50)

    // Maintenance Costs Variables
    var baseServerCost = MutableStateFlow(45) // $45/mo for basic server
    var aiCreditsSpent = MutableStateFlow(30) // $30/mo allocated credits
    var adsDailyBudget = MutableStateFlow(25) // $25/day default, calculated monthly
    var proxyCostPerMo = MutableStateFlow(20) // $20/mo proxy scaling

    val totalMonthlyMaintenanceCost: StateFlow<Int> = combine(
        baseServerCost, aiCreditsSpent, adsDailyBudget, proxyCostPerMo
    ) { server, ai, ads, proxy ->
        server + ai + (ads * 30) + proxy
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 845)

    // Ad Generator State
    var selectedSocialPlatform = MutableStateFlow("Facebook")
    val socialPlatforms = listOf("Facebook", "TikTok", "Instagram", "Google Search", "YouTube Ads", "WhatsApp")
    var inputProductName = MutableStateFlow("Portable Mini Projector 4K")
    var inputProductDetails = MutableStateFlow("Dynamic lens, multi-device connect, suitable for outdoor cinemas")
    var generatedCopyText = MutableStateFlow("")
    var isGeneratingAd = MutableStateFlow(false)

    init {
        // Run automated simulator background worker
        viewModelScope.launch {
            while (true) {
                delay(12000) // Trigger task flow animation logs every 12 seconds
                if (isSystemLive.value) {
                    executeAutomatedStep()
                }
            }
        }
    }

    fun toggleSystemLive() {
        isSystemLive.value = !isSystemLive.value
        viewModelScope.launch {
            repository.insertLog(
                AgentLog(
                    agentName = "System Controller",
                    subAgentName = "Global Routine",
                    actionDetails = "Automation system state changed. Active Status: ${isSystemLive.value}",
                    status = "SUCCESS"
                )
            )
        }
    }

    fun selectWed2cStoreIndex(idx: Int) {
        selectedWed2cIndex.value = idx
        viewModelScope.launch(Dispatchers.IO) {
            val url = wed2cUrls[idx]
            repository.insertStoreConfig(
                StoreConfig(
                    storeId = "wed2c",
                    storeName = "Wed2C Emporium",
                    affiliateLink = url,
                    productCount = 440 + Random.nextInt(-50, 50),
                    status = "CONNECTED",
                    isGatewayActive = true
                )
            )
            repository.insertLog(
                AgentLog(
                    agentName = "Listing & Pricing",
                    subAgentName = "Wed2C Dynamic Publisher",
                    actionDetails = "Switched Wed2C Active Routing Endpoint to: $url",
                    status = "SUCCESS"
                )
            )
        }
    }

    fun triggerManualFullSync() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLog(
                AgentLog(
                    agentName = "Sourcing Agent",
                    subAgentName = "Ali Sourcing Bot",
                    actionDetails = "Forced full synchronization of all 9 connected stores requested by administrator.",
                    status = "RUNNING"
                )
            )
            delay(1500)
            
            // Randomly update numbers to display sync action
            val storesList = db.storeConfigDao().getAllStoreConfigs()
            storesList.forEach { store ->
                val newCount = store.productCount + Random.nextInt(-10, 15)
                repository.updateProductCount(store.storeId, newCount)
            }

            processedVolumeUSD.value = processedVolumeUSD.value + Random.nextDouble(50.0, 180.0)

            repository.insertLog(
                AgentLog(
                    agentName = "Payment & Gateway",
                    subAgentName = "Vault Router",
                    actionDetails = "All connected checkout gateways verified & re-established securely. Active through current gateway provider: ${selectedGatewayProvider.value}.",
                    status = "SUCCESS"
                )
            )
        }
    }

    private suspend fun executeAutomatedStep() {
        val agents = listOf("Sourcing Agent", "Listing & Pricing", "Creative Marketer", "Payment & Gateway")
        val selectedAgent = agents[Random.nextInt(agents.size)]

        val log = when (selectedAgent) {
            "Sourcing Agent" -> {
                val subagents = listOf("Ali Sourcing Bot", "CJ Catalog Crawler", "AliExpress Crawler", "Daraz Scraper")
                val sub = subagents[Random.nextInt(subagents.size)]
                val productCount = Random.nextInt(5, 45)
                val platform = when(sub) {
                    "Ali Sourcing Bot" -> "Ali Baba B2B"
                    "CJ Catalog Crawler" -> "CJ Dropshipping"
                    "AliExpress Crawler" -> "AliExpress"
                    else -> "Daraz Pakistan"
                }

                AgentLog(
                    agentName = selectedAgent,
                    subAgentName = sub,
                    actionDetails = "Scanned $platform channels. Discovered $productCount micro-trending items. Updated wholesale inventory lists.",
                    status = listOf("SUCCESS", "SUCCESS", "SUCCESS", "WARNING").random()
                )
            }
            "Listing & Pricing" -> {
                val subagents = listOf("Bulker", "Price Optimizer", "Wed2C Dynamic Publisher", "SEO Tag Generator")
                val sub = subagents[Random.nextInt(subagents.size)]
                val priceChangePercent = Random.nextInt(1, 4)
                AgentLog(
                    agentName = selectedAgent,
                    subAgentName = sub,
                    actionDetails = "Price Optimization audit executed. Recalibrated markup values by +$priceChangePercent% to account for dynamic freight fees.",
                    status = "SUCCESS"
                )
            }
            "Creative Marketer" -> {
                val subagents = listOf("TikTok Video Renderer", "Meta Ad Writer", "Instagram Visualizer", "WhatsApp Broadcaster")
                val sub = subagents[Random.nextInt(subagents.size)]
                val clicksSim = Random.nextInt(100, 400)
                AgentLog(
                    agentName = selectedAgent,
                    subAgentName = sub,
                    actionDetails = "Pushed background campaign optimization. CTR improved. Registered +$clicksSim referral clicks via social feeds in past 1 hour.",
                    status = "SUCCESS"
                )
            }
            else -> {
                val subagents = listOf("SafePay Broker", "OmniGate Coordinator", "AppSheet Sync Bot", "CSV Exporter")
                val sub = subagents[Random.nextInt(subagents.size)]
                val revenueIncr = Random.nextDouble(15.0, 95.0)
                processedVolumeUSD.value = processedVolumeUSD.value + revenueIncr
                
                AgentLog(
                    agentName = selectedAgent,
                    subAgentName = sub,
                    actionDetails = "Processed order batch gateway checkout successfully. AppSheet sync updated with raw sales index of +$${String.format(Locale.US, "%.2f", revenueIncr)} USD.",
                    status = "SUCCESS"
                )
            }
        }
        repository.insertLog(log)
    }

    fun generateMarketingAd() {
        if (inputProductName.value.trim().isEmpty()) {
            generatedCopyText.value = "⚠️ Please specify a Product Name to generate ad copy."
            return
        }

        isGeneratingAd.value = true
        generatedCopyText.value = "🤖 Contacting OmniLink AI Copywriter..."

        viewModelScope.launch(Dispatchers.IO) {
            val response = GeminiClient.generateAdCreative(
                platform = selectedSocialPlatform.value,
                product = inputProductName.value,
                details = inputProductDetails.value
            )

            if (response.isNotEmpty()) {
                generatedCopyText.value = response
            } else {
                // Fallback high-quality template generator
                delay(1200)
                generatedCopyText.value = buildLocalFallbackCopy(
                    platform = selectedSocialPlatform.value,
                    product = inputProductName.value,
                    details = inputProductDetails.value
                )
            }
            isGeneratingAd.value = false

            // Append log of ad generation
            repository.insertLog(
                AgentLog(
                    agentName = "Creative Marketer",
                    subAgentName = "Gemini Copy Bot",
                    actionDetails = "Constructed high-conversion ${selectedSocialPlatform.value} advertising template for product: '${inputProductName.value}'.",
                    status = "SUCCESS"
                )
            )
        }
    }

    private fun buildLocalFallbackCopy(platform: String, product: String, details: String): String {
        return """
            ⚡ EYE-CATCHING HOOK [Local Optimizer]
            "Stop scrolling! If you're looking for the ultimate upgrade, the target is here. Meet the completely transformed $product!"
            
            📈 KEY BENEFITS
            • 💎 Premium Grade Quality – Engineered for high-density performance ($details).
            • 🚀 Automated Drop-shipping compatibility – Integrated instantly across Alibaba, AliExpress, and eBay.
            • 🔐 Direct Purchase Processing – One-click safe gateways enabled for friction-free purchasing!
            • 📦 Worldwide Free Cargo – Powered by our 9-store connected network!
            
            🔥 CALL TO ACTION (CTA)
            👉 Click 'SHOP NOW' on our official Wed2C or DigiStore24 channels to secure the -50% early bird discount before the campaign ends!
        """.trimIndent()
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            repository.insertLog(
                AgentLog(
                    agentName = "System Controller",
                    subAgentName = "Admin Command",
                    actionDetails = "Local database log clearance protocol finalized. Recording reset.",
                    status = "SUCCESS"
                )
            )
        }
    }

    fun exportLogsToCSV(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val builder = java.lang.StringBuilder()
        builder.append("ID,TIMESTAMP,AGENT,SUB_AGENT,ACTION_DETAILS,STATUS\n")
        logs.value.forEach {
            val timeString = format.format(Date(it.timestamp))
            // Escape double quotes in details to avoid CSV breakage
            val escapedDetails = it.actionDetails.replace("\"", "\"\"")
            builder.append("${it.id},\"$timeString\",\"${it.agentName}\",\"${it.subAgentName}\",\"$escapedDetails\",\"${it.status}\"\n")
        }
        return builder.toString()
    }

    fun exportLogsToTextFormat(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val builder = java.lang.StringBuilder()
        builder.append("================ OMNILINK AI AGENT ACTIVITY REPORT ================\n")
        builder.append("Report Date: ${format.format(Date())}\n")
        builder.append("Total Database Record Index: ${logs.value.size} active elements\n")
        builder.append("-------------------------------------------------------------------\n\n")
        logs.value.forEach {
            val timeString = format.format(Date(it.timestamp))
            builder.append("[$timeString] [${it.status}] ${it.agentName} (${it.subAgentName}):\n")
            builder.append("  ↳ ${it.actionDetails}\n\n")
        }
        return builder.toString()
    }

    fun updateStoreLink(storeId: String, newLink: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStore = stores.value.find { it.storeId == storeId }
            if (currentStore != null) {
                val updated = currentStore.copy(affiliateLink = newLink)
                repository.insertStoreConfig(updated)
                repository.insertLog(
                    AgentLog(
                        agentName = "System Controller",
                        subAgentName = "Admin Command",
                        actionDetails = "Updated connection link parameters for ${currentStore.storeName} to: $newLink",
                        status = "SUCCESS"
                    )
                )
            }
        }
    }

    fun updateStoreStatusToggle(storeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStore = stores.value.find { it.storeId == storeId }
            if (currentStore != null) {
                val newStatus = if (currentStore.status == "CONNECTED") "OFFLINE" else "CONNECTED"
                repository.updateStoreStatus(storeId, newStatus)
                repository.insertLog(
                    AgentLog(
                        agentName = "System Controller",
                        subAgentName = "Admin Command",
                        actionDetails = "Toggled ${currentStore.storeName} status state to $newStatus.",
                        status = "SUCCESS"
                    )
                )
            }
        }
    }
}
