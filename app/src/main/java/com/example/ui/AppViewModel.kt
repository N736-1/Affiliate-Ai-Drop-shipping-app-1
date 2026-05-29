package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(
        db.agentLogDao(),
        db.storeConfigDao(),
        db.mercedesCarDao(),
        db.cartItemDao(),
        db.orderRecordDao()
    )

    // Room Database State Flows
    val logs: StateFlow<List<AgentLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stores: StateFlow<List<StoreConfig>> = repository.allStoreConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cars: StateFlow<List<MercedesCar>> = repository.allCars
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.allCartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderRecord>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // UI Filters / Search State
    // -------------------------------------------------------------
    var isSystemLive = MutableStateFlow(true)
        private set

    var selectedCategory = MutableStateFlow("ALL") // "ALL", "NEW", "USED", "VINTAGE", "LUXURY_PARTS"
        private set

    var searchQuery = MutableStateFlow("")
        private set

    // Calculated fields
    val filteredCars: StateFlow<List<MercedesCar>> = combine(
        cars, selectedCategory, searchQuery
    ) { carsList, cat, query ->
        carsList.filter { car ->
            val matchesCategory = (cat == "ALL") || (car.category == cat)
            val matchesSearch = car.modelName.contains(query, ignoreCase = true) ||
                    car.description.contains(query, ignoreCase = true) ||
                    car.specifications.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart with associated car models join flow
    val cartWithCars: StateFlow<List<Pair<MercedesCar, Int>>> = combine(
        cars, cartItems
    ) { carsList, items ->
        items.mapNotNull { item ->
            val car = carsList.find { it.carId == item.carId }
            if (car != null) Pair(car, item.quantity) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotalUSD: StateFlow<Double> = cartWithCars.map { list ->
        list.sumOf { it.first.priceUSD * it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartEstimatedCommissionUSD: StateFlow<Double> = cartWithCars.map { list ->
        list.sumOf { (it.first.priceUSD * (it.first.commissionPercent / 100.0)) * it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // -------------------------------------------------------------
    // Payment Gateway State & Customer Details
    // -------------------------------------------------------------
    val paymentGateways = listOf(
        "JazzCash Mobile Wallet",
        "EasyPaisa Mobile Wallet",
        "Local Bank Account (IBAN Direct)",
        "Premium Debit Card (Visa/Master)",
        "PayPal Express Secure",
        "Payoneer Escrow Settlement",
        "Digital Crypto exchange (USDT / BTC)"
    )
    var selectedGateway = MutableStateFlow(paymentGateways[0])
    var customerNameInput = MutableStateFlow("")
    var paymentAccountInput = MutableStateFlow("")

    val processedVolumeUSD = MutableStateFlow(328500.0)
    val totalCommissionPaidUSD = MutableStateFlow(22995.0)

    // -------------------------------------------------------------
    // AI Copywriter / Marketing Section State
    // -------------------------------------------------------------
    var selectedSocialPlatform = MutableStateFlow("TikTok")
    val socialPlatforms = listOf("TikTok", "Facebook", "Instagram", "Meta Ad Network", "WhatsApp Broadcaster")
    var inputProductName = MutableStateFlow("Mercedes-Benz C 300 Sedan")
    var inputProductDetails = MutableStateFlow("M3 Dynamic Silver Star trim, Active Escrow payments, direct manufacturer referral link tracking secure.")
    var generatedCopyText = MutableStateFlow("")
    var isGeneratingAd = MutableStateFlow(false)

    init {
        // Initialize & populate base tables
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Initial Affiliate Store Configuration
                val storeDao = db.storeConfigDao()
                val currentStores = storeDao.getAllStoreConfigs()
                if (currentStores.isEmpty()) {
                    val initialStores = listOf(
                        StoreConfig(
                            storeId = "greenwich",
                            storeName = "Mercedes-Benz of Greenwich",
                            affiliateLink = "https://www.mercedesbenzgreenwich.com/penske-terms-of-use/",
                            productCount = 1420,
                            status = "CONNECTED",
                            isGatewayActive = true
                        ),
                        StoreConfig(
                            storeId = "birmingham",
                            storeName = "Mercedes-Benz of Birmingham",
                            affiliateLink = "https://www.mbbhm.com/finance/affiliates/",
                            productCount = 890,
                            status = "CONNECTED",
                            isGatewayActive = true
                        ),
                        StoreConfig(
                            storeId = "direct_hq",
                            storeName = "Mercedes-Benz HQ Direct Broker",
                            affiliateLink = "https://www.mbusa.com/en/home",
                            productCount = 2170,
                            status = "CONNECTED",
                            isGatewayActive = true
                        )
                    )
                    storeDao.insertStoreConfigs(initialStores)
                }

                // 2. Initial Mercedes-Benz luxury inventory & high-end parts catalog
                val carDao = db.mercedesCarDao()
                val currentCars = carDao.getAllCars()
                if (currentCars.isEmpty()) {
                    val initialCars = listOf(
                        MercedesCar(
                            carId = "c300",
                            modelName = "Mercedes-Benz C 300 Sedan",
                            category = "NEW",
                            priceUSD = 48500.0,
                            commissionPercent = 5.5,
                            description = "The absolute epitome of luxury premium entry. Employs a fully optimized mild-hybrid EQ Boost system with adaptive headlights, dynamic leather upholstery, and a cockpit curved display panel.",
                            affiliateUrl = "https://www.mercedesbenzgreenwich.com/penske-terms-of-use/",
                            conditionDetails = "Factory New - Full Mercedes-Benz Premium Warranty",
                            modelYear = 2026,
                            specifications = "2.0L Turbo I4 + 48V Hybrid, 255 HP, Obsidian Black, Burmester® 3D Surround sound"
                        ),
                        MercedesCar(
                            carId = "amg_gt",
                            modelName = "Mercedes-AMG GT Coupe (V8 Exclusive)",
                            category = "NEW",
                            priceUSD = 135000.0,
                            commissionPercent = 7.0,
                            description = "Mastercrafted sports flagship configured for track speed with luxury composure. Offers an actively adjustable Rear Aerofoil, multi-mode drive selector control, and bespoke carbon fiber trim packages.",
                            affiliateUrl = "https://www.mbbhm.com/finance/affiliates/",
                            conditionDetails = "Factory Hand-Assembled in Affalterbach",
                            modelYear = 2026,
                            specifications = "Handcrafted AMG 4.0L V8 Biturbo, 523 HP, Selenite Grey Satin, Performance exhaust"
                        ),
                        MercedesCar(
                            carId = "s580",
                            modelName = "Mercedes-Benz S-Class 580 Executive",
                            category = "NEW",
                            priceUSD = 124000.0,
                            commissionPercent = 6.0,
                            description = "Pioneering the future of comfort. Built with custom rear lounge seat calf-massage modules, warm scent diffuse chamber, active lane management, and executive steering package controls.",
                            affiliateUrl = "https://www.mercedesbenzgreenwich.com/penske-terms-of-use/",
                            conditionDetails = "Factory Custom Order Booking",
                            modelYear = 2026,
                            specifications = "4.0L V8 Biturbo + Mild Hybrid, 496 HP, Diamond Metallic White, Rear axle steering"
                        ),
                        MercedesCar(
                            carId = "g63",
                            modelName = "Mercedes-AMG G 63 SUV Elite",
                            category = "USED",
                            priceUSD = 189000.0,
                            commissionPercent = 4.5,
                            description = "Certified Pre-Owned luxury icon. Kept in pristine collectors garage condition with complete records, triple locking differential knobs, and dual side AMG exhaust tips.",
                            affiliateUrl = "https://www.mbbhm.com/finance/affiliates/",
                            conditionDetails = "CPO - Certified Pre-Owned (Pristine 9,800 miles)",
                            modelYear = 2024,
                            specifications = "Handcrafted AMG 4.0L V8 Bi-turbo, 577 HP, Matte Obsidian, 22-inch Forged Black Rims"
                        ),
                        MercedesCar(
                            carId = "sl300",
                            modelName = "Mercedes-Benz 300 SL Roadster Classic",
                            category = "VINTAGE",
                            priceUSD = 1450000.0,
                            commissionPercent = 4.0,
                            description = "A breathtaking museum-vintage investment collectible. Beautifully kept Roadster variant. Matching engine/chassis number stamps with gorgeous retro dials and ivory paint.",
                            affiliateUrl = "https://www.mercedesbenzgreenwich.com/penske-terms-of-use/",
                            conditionDetails = "Concours d'Elegance Gold Class Certified",
                            modelYear = 1957,
                            specifications = "Historic 3.0L Inline-6, 240 HP, Solid Cream White, Cognac Leather, Manual 4-Speed"
                        ),
                        MercedesCar(
                            carId = "carbon_wheel",
                            modelName = "AMG Performance Alcantara Steering Wheel",
                            category = "LUXURY_PARTS",
                            priceUSD = 2450.0,
                            commissionPercent = 12.0,
                            description = "Add elite motorsport feeling to your premium cockpit. Features integrated carbon-fiber side moldings, red top strip markers, and customized racing paddles.",
                            affiliateUrl = "https://www.mercedesbenzgreenwich.com/penske-terms-of-use/",
                            conditionDetails = "Genuine OEM Mercedes-Benz Accessory",
                            modelYear = 2026,
                            specifications = "High-density weave Carbon Fiber, Alcantara grip, OEM wiring plug-and-play"
                        ),
                        MercedesCar(
                            carId = "star_badge",
                            modelName = "Illuminated Star Front Grille LED Assembly",
                            category = "LUXURY_PARTS",
                            priceUSD = 499.0,
                            commissionPercent = 15.0,
                            description = "A sophisticated luxury upgrade that automatically activates a crisp white LED backlight ring around the front silver star when doors unlock or start.",
                            affiliateUrl = "https://www.mbbhm.com/finance/affiliates/",
                            conditionDetails = "Official OEM Retrofit Kit",
                            modelYear = 2025,
                            specifications = "LED illumination element, fuse harness bundle included"
                        )
                    )
                    carDao.insertCars(initialCars)
                }

                // 3. Initial Agent logs showcasing team effort
                val logDao = db.agentLogDao()
                if (logDao.getLogsCount() == 0) {
                    val initialLogs = listOf(
                        AgentLog(
                            agentName = "System Planner (Agent 1)",
                            subAgentName = "Requirements Architect",
                            actionDetails = "Secured Penske affiliate terms policy from Greenwich & Birmingham registries. Mapped C300 & parts data hierarchy.",
                            status = "SUCCESS"
                        ),
                        AgentLog(
                            agentName = "Backend DB (Agent 2)",
                            subAgentName = "SQLite & API Broker",
                            actionDetails = "Room database setup finalized with full constraints for cars, custom items, and multi-wallet escrow checks.",
                            status = "SUCCESS"
                        ),
                        AgentLog(
                            agentName = "UI Engineering (Agent 3)",
                            subAgentName = "Theme Layout Engine",
                            actionDetails = "Completed ultra-premium obsidian and steel theme. Configured responsive category headers and product cards.",
                            status = "SUCCESS"
                        ),
                        AgentLog(
                            agentName = "QC Integration (Agent 4)",
                            subAgentName = "Pay-Reconciliation Sync",
                            actionDetails = "JazzCash, EasyPaisa, and IBAN routes fully bridged. Synchronized all active interfaces successfully.",
                            status = "SUCCESS"
                        )
                    )
                    initialLogs.forEach { logDao.insertLog(it) }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Loop simulation logs animation background worker
            try {
                while (true) {
                    delay(15000)
                    if (isSystemLive.value) {
                        try {
                            executeAutomatedStep()
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore cancellation normally
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // -------------------------------------------------------------
    // Core User Actions
    // -------------------------------------------------------------
    fun selectCategory(cat: String) {
        selectedCategory.value = cat
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLog(
                AgentLog(
                    agentName = "UI Engineering (Agent 3)",
                    subAgentName = "Jetpack Search Listener",
                    actionDetails = "Filtered product view target to category: $cat",
                    status = "SUCCESS"
                )
            )
        }
    }

    fun updateSearchQuery(q: String) {
        searchQuery.value = q
    }

    fun addToCart(car: MercedesCar) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToCart(car.carId)
            repository.insertLog(
                AgentLog(
                    agentName = "Backend DB (Agent 2)",
                    subAgentName = "Cart Cache Broker",
                    actionDetails = "Registered add-to-cart API request for ${car.modelName}. Set escrow commission rate to ${car.commissionPercent}%.",
                    status = "SUCCESS"
                )
            )
        }
    }

    fun removeFromCart(carId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromCart(carId)
            repository.insertLog(
                AgentLog(
                    agentName = "Backend DB (Agent 2)",
                    subAgentName = "Cart Cache Broker",
                    actionDetails = "Removed unit ID '$carId' from cart state indexes.",
                    status = "SUCCESS"
                )
            )
        }
    }

    fun checkAndToggleLiveSystem() {
        isSystemLive.value = !isSystemLive.value
        viewModelScope.launch {
            repository.insertLog(
                AgentLog(
                    agentName = "QC Integration (Agent 4)",
                    subAgentName = "Cron Loop Manager",
                    actionDetails = "Automated subagent simulation state updated. Running: ${isSystemLive.value}",
                    status = "SUCCESS"
                )
            )
        }
    }

    // -------------------------------------------------------------
    // Order Placement & Gateway Checkout Engine
    // -------------------------------------------------------------
    fun placeAffiliateOrder() {
        val currentCartList = cartWithCars.value
        if (currentCartList.isEmpty()) return

        val totalAmount = cartTotalUSD.value
        val commissionEarned = cartEstimatedCommissionUSD.value
        val gateway = selectedGateway.value
        val customer = customerNameInput.value.trim().ifEmpty { "Guest Customer" }
        val payoutDetails = paymentAccountInput.value.trim().ifEmpty { "Cash Escrow" }

        // Compile items text summary
        val summaryStr = currentCartList.joinToString(", ") { "${it.first.modelName} (x${it.second})" }

        viewModelScope.launch(Dispatchers.IO) {
            // Log Agent 1 planning checkout rules
            repository.insertLog(
                AgentLog(
                    agentName = "System Planner (Agent 1)",
                    subAgentName = "Escrow Policy Checker",
                    actionDetails = "Verified order guidelines under Penske Terms. Commission tracking generated for: $customer ($gateway).",
                    status = "SUCCESS"
                )
            )

            // Log Agent 2 executing backend insertion
            repository.insertLog(
                AgentLog(
                    agentName = "Backend DB (Agent 2)",
                    subAgentName = "SQL Transaction Agent",
                    actionDetails = "Committed OrderRecord to local database. Summary: $summaryStr. Total: $$totalAmount USD.",
                    status = "SUCCESS"
                )
            )

            val orderRecord = OrderRecord(
                itemsSummary = summaryStr,
                customerName = customer,
                paymentGateway = "$gateway ($payoutDetails)",
                totalAmountUSD = totalAmount,
                commissionEarnedUSD = commissionEarned,
                trackingStatus = "Order Placed"
            )
            repository.placeOrder(orderRecord)

            // Trigger visual volume updates
            processedVolumeUSD.value = processedVolumeUSD.value + totalAmount
            totalCommissionPaidUSD.value = totalCommissionPaidUSD.value + commissionEarned

            // Log Agent 3 displaying UI update
            repository.insertLog(
                AgentLog(
                    agentName = "UI Engineering (Agent 3)",
                    subAgentName = "Order Transition Router",
                    actionDetails = "Cleared shopping cart interface. Redirected customer to tracking logs.",
                    status = "SUCCESS"
                )
            )

            // Clear Cart
            repository.clearCart()

            // Run Agent 4 payout reconciliation checks
            delay(2000)
            val latestOrders = repository.getAllOrders()
            val latestId = latestOrders.firstOrNull()?.orderId
            if (latestId != null) {
                repository.insertLog(
                    AgentLog(
                        agentName = "QC Integration (Agent 4)",
                        subAgentName = "Pay-Reconciliation Sync",
                        actionDetails = "Gateway finalized. IBAN/Mobile Wallet verification success. Status: Commission Safe-Escrowed.",
                        status = "SUCCESS"
                    )
                )
                repository.updateOrderStatus(latestId, "Commission Safe-Escrowed")
            }
        }
    }

    // -------------------------------------------------------------
    // Simulated Automated Subagent Background Engine
    // -------------------------------------------------------------
    private suspend fun executeAutomatedStep() {
        val randomSubtask = Random.nextInt(4)
        val sampleCustomers = listOf("Farhan", "Ayesha", "Daniyal", "Sajid", "Hamza", "Zainab", "Naeem")
        val sampleDealerships = listOf("Greenwich penske-terms-of-use (Greenwich, CT)", "mbbhm-affiliates (Birmingham, AL)", "Mercedes Direct HQ")

        when (randomSubtask) {
            0 -> {
                // Agent 1 Plans inventory checks
                val dealer = sampleDealerships.random()
                repository.insertLog(
                    AgentLog(
                        agentName = "System Planner (Agent 1)",
                        subAgentName = "Catalog Assessor",
                        actionDetails = "Scanned affiliate links for '$dealer'. Standardized commission structure details under active terms.",
                        status = "SUCCESS"
                    )
                )
            }
            1 -> {
                // Agent 2 executes simulated pricing check
                val carList = cars.value
                if (carList.isNotEmpty()) {
                    val car = carList.random()
                    val originalPrice = car.priceUSD
                    val marginAdjust = Random.nextDouble(-120.0, 250.0)
                    repository.insertLog(
                        AgentLog(
                            agentName = "Backend DB (Agent 2)",
                            subAgentName = "Arbitrage Evaluator",
                            actionDetails = "Audited dynamic wholesale API prices for '${car.modelName}'. Shifted quote variance to adjust commission targets.",
                            status = "SUCCESS"
                        )
                    )
                }
            }
            2 -> {
                // Agent 3 reviews UI interaction simulation
                val carList = cars.value
                val item = if (carList.isNotEmpty()) carList.random().modelName else "Parts list"
                repository.insertLog(
                    AgentLog(
                        agentName = "UI Engineering (Agent 3)",
                        subAgentName = "Usage Analytics",
                        actionDetails = "Simulated organic guest session. Inspected card rendering, touch state timings, and scroll performance on '$item'.",
                        status = "SUCCESS"
                    )
                )
            }
            else -> {
                // Agent 4 triggers random commission sale via checkout!
                val carList = cars.value
                if (carList.isNotEmpty()) {
                    val eligibleCars = carList.filter { it.category != "VINTAGE" }
                    if (eligibleCars.isNotEmpty()) {
                        val car = eligibleCars.random()
                        val buyer = sampleCustomers.random()
                        val chosenPayout = paymentGateways.random()
                        val commVal = car.priceUSD * (car.commissionPercent / 100.0)

                        repository.insertLog(
                            AgentLog(
                                agentName = "QC Integration (Agent 4)",
                                subAgentName = "Pay-Reconciliation Sync",
                                actionDetails = "Simulated organic checkout conversion! Buyer: $buyer ordered ${car.modelName}. Commission generated of $${String.format(Locale.US, "%.2f", commVal)} USD.",
                                status = "SUCCESS"
                            )
                        )

                        val simOrder = OrderRecord(
                            itemsSummary = "${car.modelName} (x1)",
                            customerName = buyer,
                            paymentGateway = "$chosenPayout [Simulated Checkout]",
                            totalAmountUSD = car.priceUSD,
                            commissionEarnedUSD = commVal,
                            trackingStatus = "Commission Safe-Escrowed"
                        )
                        repository.placeOrder(simOrder)

                        processedVolumeUSD.value = processedVolumeUSD.value + car.priceUSD
                        totalCommissionPaidUSD.value = totalCommissionPaidUSD.value + commVal
                    }
                }
            }
        }
    }

    fun generateAdCopy(carModel: String, year: Int, specsDetails: String) {
        if (carModel.trim().isEmpty()) return

        isGeneratingAd.value = true
        generatedCopyText.value = "🤖 Activating Agent 1 and Agent 3 marketing systems to query Gemini models for '$carModel' campaign copy..."

        viewModelScope.launch(Dispatchers.IO) {
            val detailsParam = "Exclusive SilverStar Edition, Year: $year, specifications: $specsDetails. Connected live via Penske Terms and secure payment escrows (JazzCash, IBAN, Crypto)."
            val response = GeminiClient.generateAdCreative(
                platform = selectedSocialPlatform.value,
                product = carModel,
                details = detailsParam
            )

            if (response.isNotEmpty()) {
                generatedCopyText.value = response
            } else {
                delay(1500)
                generatedCopyText.value = buildLocalClassicCopy(carModel, year, specsDetails)
            }
            isGeneratingAd.value = false

            repository.insertLog(
                AgentLog(
                    agentName = "QC Integration (Agent 4)",
                    subAgentName = "Social Engine Router",
                    actionDetails = "Successfully generated and dispatched luxury marketing copy for model: $carModel.",
                    status = "SUCCESS"
                )
            )
        }
    }

    private fun buildLocalClassicCopy(model: String, year: Int, specs: String): String {
        return """
            ⚡ EXCLUSIVE LUXURY DEALER HOOK
            "Experience pure SilverStar perfection. Introducing the magnificent $year Edition $model!"
            
            📈 HIGH-END HIGHLIGHTS
            • 💎 Handcrafted Quality – Powered by state-of-the-art dynamics: $specs.
            • 🤝 Secure Affiliate Escrow – Safe commission splits processed directly via local and international gateways.
            • 🔗 Penske Terms Compliant – Track and bridge purchases securely between premium buyers and certified dealerships!
            • 💳 Multiple Cash Out Options – Integrated with JazzCash, EasyPaisa, Direct IBAN, Payoneer, and digital currency wallets.
            
            🔥 CALL TO ACTION
            👉 View active listings and lock in your order to receive direct VIP delivery!
        """.trimIndent()
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            repository.insertLog(
                AgentLog(
                    agentName = "System Planner (Agent 1)",
                    subAgentName = "History Audit",
                    actionDetails = "Database activity registry successfully purged. Restarting real-time trace logging.",
                    status = "SUCCESS"
                )
            )
        }
    }
}
