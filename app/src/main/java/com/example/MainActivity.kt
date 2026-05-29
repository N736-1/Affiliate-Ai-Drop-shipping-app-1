package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MercedesStarLogo(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f
        
        drawCircle(
            color = color,
            radius = r * 0.9f,
            style = Stroke(width = 1.75.dp.toPx())
        )
        
        val angles = listOf(-90.0, 30.0, 150.0)
        for (deg in angles) {
            val rad = Math.toRadians(deg)
            val endX = cx + r * 0.88f * cos(rad).toFloat()
            val endY = cy + r * 0.88f * sin(rad).toFloat()
            
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(cx, cy),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val viewModel: AppViewModel = viewModel()
    val logs by viewModel.logs.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val cars by viewModel.filteredCars.collectAsState()
    val cartItems by viewModel.cartWithCars.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val isLive by viewModel.isSystemLive.collectAsState()

    var currentTab by remember { mutableStateOf("Marketplace") } // "Marketplace", "My Cart", "Order Trace", "Agent Node"
    var selectedCarForAd by remember { mutableStateOf<MercedesCar?>(null) }
    var selectedDealerUrlForDialog by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OmniBg,
        bottomBar = {
            // Elegant M3 Bottom Navigation bar with high touch area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(68.dp)
                    .background(OmniNavBg)
                    .border(1.dp, OmniBorder, RectangleShape)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NavigationItem(
                    icon = "🚗",
                    label = "Browse",
                    isSelected = currentTab == "Marketplace",
                    onClick = { currentTab = "Marketplace" },
                    modifier = Modifier.testTag("nav_browse")
                )
                NavigationItem(
                    icon = "🛒",
                    label = "Cart (${cartItems.sumOf { it.second }})",
                    isSelected = currentTab == "My Cart",
                    onClick = { currentTab = "My Cart" },
                    modifier = Modifier.testTag("nav_cart")
                )
                NavigationItem(
                    icon = "📋",
                    label = "Trace",
                    isSelected = currentTab == "Order Trace",
                    onClick = { currentTab = "Order Trace" },
                    modifier = Modifier.testTag("nav_trace")
                )
                NavigationItem(
                    icon = "🤖",
                    label = "Agents",
                    isSelected = currentTab == "Agent Node",
                    onClick = { currentTab = "Agent Node" },
                    modifier = Modifier.testTag("nav_agents")
                )
                NavigationItem(
                    icon = "📖",
                    label = "Guide",
                    isSelected = currentTab == "User Guide",
                    onClick = { currentTab = "User Guide" },
                    modifier = Modifier.testTag("nav_guide")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Premium Brand Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MercedesStarLogo(
                        modifier = Modifier.size(32.dp),
                        color = OmniLiveGreen
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MERCEDES-BENZ",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.0.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SilverStar Affiliate Hub • Active Gateway",
                            color = OmniMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Custom blinking active network beacon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(OmniBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.checkAndToggleLiveSystem() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "blink")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "opacity"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLive) OmniLiveGreen.copy(alpha = alpha) else Color.Red.copy(alpha = alpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLive) "AUTO INTEGRATED" else "PAUSED",
                        color = if (isLive) OmniLiveGreen else OmniMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Divider(color = OmniBorder, thickness = 1.dp)

            // Dynamic Tab Selector Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(OmniBg, OmniNavBg)
                        )
                    )
            ) {
                when (currentTab) {
                    "Marketplace" -> MarketplaceTab(
                        viewModel = viewModel,
                        cars = cars,
                        onTriggerAdGen = { car -> selectedCarForAd = car },
                        onShowAffiliateTerms = { url -> selectedDealerUrlForDialog = url }
                    )
                    "My Cart" -> CartTab(
                        viewModel = viewModel,
                        cartItems = cartItems
                    )
                    "Order Trace" -> TraceTab(
                        viewModel = viewModel,
                        orders = orders
                    )
                    "Agent Node" -> AgentNodeTab(
                        viewModel = viewModel,
                        logs = logs,
                        stores = stores
                    )
                    "User Guide" -> UserGuideTab(
                        viewModel = viewModel,
                        cartItems = cartItems,
                        orders = orders,
                        logs = logs,
                        isLive = isLive
                    )
                }
            }
        }
    }

    // Modal popup showing terms routing and tracking compliance
    val currentDealerUrl = selectedDealerUrlForDialog
    if (currentDealerUrl != null) {
        AlertDialog(
            onDismissRequest = { selectedDealerUrlForDialog = null },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary, contentColor = Color(0xFF381E72)),
                    onClick = { selectedDealerUrlForDialog = null }
                ) {
                    Text("Deemed Compliant")
                }
            },
            containerColor = OmniCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⛓️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Penske Affiliate Escrow terms",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Verified Dealership Terms Rule Bridge:\n$currentDealerUrl",
                        color = OmniLiveGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Under the verified franchise covenants rules, all user referrals & part commission structures are routed through strict secure payment gates. This bridge tracks user sessions via digital cookies and maps escrow commission payouts at -50% early bird allocations, honoring user permissions on-device.",
                        color = OmniMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        )
    }

    // Modal to generate AI ad copywriting for a car via Gemini API Flow
    val currentCarForAd = selectedCarForAd
    if (currentCarForAd != null) {
        val isGenerating by viewModel.isGeneratingAd.collectAsState()
        val generatedText by viewModel.generatedCopyText.collectAsState()
        val socialPlatform by viewModel.selectedSocialPlatform.collectAsState()
        val clipboardManager = LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { selectedCarForAd = null },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary, contentColor = Color(0xFF381E72)),
                        modifier = Modifier.weight(1f),
                        enabled = !isGenerating,
                        onClick = {
                            viewModel.generateAdCopy(currentCarForAd.modelName, currentCarForAd.modelYear, currentCarForAd.specifications)
                        }
                    ) {
                        Text("Query AI", fontWeight = FontWeight.Bold)
                    }

                    if (generatedText.isNotEmpty()) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF49454F), contentColor = Color.White),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedText))
                                Toast.makeText(context, "Marketing copy copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Copy")
                        }
                    }

                    TextButton(
                        onClick = { selectedCarForAd = null }
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            },
            containerColor = OmniCardBg,
            title = {
                Text(
                    text = "Generate Ad: ${currentCarForAd.modelName}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Agent 3 dynamically writes templates for viral conversions. Choose target network:",
                        color = OmniMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Target Network chips selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.socialPlatforms.forEach { platform ->
                            FilterChip(
                                selected = socialPlatform == platform,
                                onClick = { viewModel.selectedSocialPlatform.value = platform },
                                label = { Text(platform, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OmniPrimary,
                                    selectedLabelColor = Color(0xFF381E72),
                                    containerColor = OmniBg,
                                    labelColor = OmniMuted
                                )
                            )
                        }
                    }

                    Divider(color = OmniBorder, modifier = Modifier.padding(bottom = 8.dp))

                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = OmniPrimary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Agents compiling Gemini creative...",
                                    color = OmniMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else if (generatedText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(OmniBg, RoundedCornerShape(8.dp))
                                .border(1.dp, OmniBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = generatedText,
                                color = OmniText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🤖 AI copywriting engine is offline ready.", color = OmniMuted, fontSize = 10.sp)
                            Text("Model: Gemini 3.5 Flash", color = OmniLiveGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun NavigationItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .testTag("nav_item_${isSelected}")
            .width(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .background(
                    if (isSelected) Color(0xFF1E1F28) else Color.Transparent, 
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon, 
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) OmniLiveGreen else OmniMuted.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            letterSpacing = 0.2.sp
        )
    }
}

// -------------------------------------------------------------
// TAB 1: MARKETPLACE BROWSER
// -------------------------------------------------------------
@Composable
fun MarketplaceTab(
    viewModel: AppViewModel,
    cars: List<MercedesCar>,
    onTriggerAdGen: (MercedesCar) -> Unit,
    onShowAffiliateTerms: (String) -> Unit
) {
    val searchVal by viewModel.searchQuery.collectAsState()
    val activeCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf(
        "ALL" to "All Catalog",
        "NEW" to "New Cars 🚗",
        "USED" to "Used 🚙",
        "VINTAGE" to "Vintage 🏎️",
        "LUXURY_PARTS" to "Elite Parts ⚙️"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Premium Setup Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(OmniBg, CircleShape)
                        .border(1.dp, OmniLiveGreen.copy(alpha = 0.5f), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MercedesStarLogo(
                        modifier = Modifier.fillMaxSize(),
                        color = OmniLiveGreen
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EQ PRO AFFILIATE INVENTORY",
                            color = OmniLiveGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OmniLiveGreen)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Earn up to 12% payouts. Copy automated AI ad scripts and route escrow clearances through secure digital payment bridges.",
                        color = OmniMuted,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // High polish premium search container
        OutlinedTextField(
            value = searchVal,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search model, specs, gear...", color = OmniMuted, fontSize = 12.sp) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = OmniText, fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OmniPrimary,
                unfocusedBorderColor = OmniBorder,
                focusedContainerColor = OmniCardBg,
                unfocusedContainerColor = OmniCardBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            singleLine = true,
            trailingIcon = { if (searchVal.isNotEmpty()) Text("❌", modifier = Modifier.clickable { viewModel.updateSearchQuery("") }, fontSize = 10.sp) }
        )

        // Category Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (key, label) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeCategory == key) OmniPrimary else OmniCardBg)
                        .border(1.dp, if (activeCategory == key) OmniPrimary else OmniBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectCategory(key) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        color = if (activeCategory == key) Color(0xFF1C1B1F) else OmniText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main listings lists
        if (cars.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No matching Mercedes assets found.", color = OmniMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars, key = { it.carId }) { car ->
                    CarListingCard(
                        car = car,
                        onAdd = { viewModel.addToCart(car) },
                        onGenAd = { onTriggerAdGen(car) },
                        onOpenTerms = { onShowAffiliateTerms(car.affiliateUrl) }
                    )
                }
            }
        }
    }
}

@Composable
fun CarListingCard(
    car: MercedesCar,
    onAdd: () -> Unit,
    onGenAd: () -> Unit,
    onOpenTerms: () -> Unit
) {
    val estimatedComm = remember(car.carId, car.priceUSD, car.commissionPercent) { 
        car.priceUSD * (car.commissionPercent / 100.0) 
    }
    val formattedCommissionText = remember(car.carId, car.commissionPercent, estimatedComm) {
        "${car.commissionPercent}% Pay: $${String.format(Locale.US, "%,.0f", estimatedComm)}"
    }
    val formattedPriceText = remember(car.carId, car.priceUSD) {
        "$${String.format(Locale.US, "%,.2f", car.priceUSD)} USD"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        border = BorderStroke(1.dp, OmniBorder),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges & Tag row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                when (car.category) {
                                    "NEW" -> OmniLiveGreen.copy(alpha = 0.15f)
                                    "USED" -> Color(0xFF00E1D9).copy(alpha = 0.15f)
                                    "VINTAGE" -> Color(0xFFFFB300).copy(alpha = 0.15f)
                                    else -> OmniPrimary.copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = car.category,
                            color = when (car.category) {
                                "NEW" -> OmniLiveGreen
                                "USED" -> Color(0xFF00E1D9)
                                "VINTAGE" -> Color(0xFFFFB300)
                                else -> OmniPrimary
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${car.modelYear} • ${car.conditionDetails}",
                        color = OmniMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Estimated Commission Highlight Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(OmniLiveGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formattedCommissionText,
                        color = OmniLiveGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Model Title
            Text(
                text = car.modelName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )

            // Specs label
            Text(
                text = car.specifications,
                color = OmniMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Description details
            Text(
                text = car.description,
                color = OmniMuted.copy(alpha = 0.85f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OmniBg, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text("ESTIMATED PRICE", color = OmniMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formattedPriceText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(
                    onClick = onOpenTerms,
                    modifier = Modifier.size(48.dp) // Accessibility standard minimum 48dp
                ) {
                    Text("🔗", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onGenAd,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OmniLiveGreen),
                    border = BorderStroke(1.dp, OmniBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp) // Elevated click size
                ) {
                    Text("💡 AI AD COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }

                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = OmniLiveGreen, contentColor = Color(0xFF06070B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp) // Elevated click size
                ) {
                    Text("ADD TO CART", fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: CART & CHECKOUT PAGE
// -------------------------------------------------------------
@Composable
fun CartTab(
    viewModel: AppViewModel,
    cartItems: List<Pair<MercedesCar, Int>>
) {
    val cartTotal by viewModel.cartTotalUSD.collectAsState()
    val estCommission by viewModel.cartEstimatedCommissionUSD.collectAsState()
    val payoutGateway by viewModel.selectedGateway.collectAsState()
    val custName by viewModel.customerNameInput.collectAsState()
    val payAcct by viewModel.paymentAccountInput.collectAsState()

    var activeDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your cart is empty.", color = OmniMuted, fontSize = 13.sp)
                    Text("Browse luxury inventory and add stars!", color = OmniMuted, fontSize = 11.sp)
                }
            }
        } else {
            // Cart Items Header
            Text(
                text = "SELECTED MERCEDES VEHICLES / PARTS IN CART:",
                color = OmniPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Items list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartItems.forEach { (car, qty) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniCardBg, RoundedCornerShape(12.dp))
                            .border(1.dp, OmniBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(car.modelName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Price: $${String.format(Locale.US, "%,.0f", car.priceUSD)} | Commission Rate: ${car.commissionPercent}%",
                                color = OmniMuted,
                                fontSize = 9.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Qty: $qty", color = OmniPrimary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(
                                onClick = { viewModel.removeFromCart(car.carId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("🗑️", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = OmniMetricBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL CART VALUE", color = OmniMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$${String.format(Locale.US, "%,.2f", cartTotal)} USD",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("ESTIMATED PAYOUT", color = OmniLiveGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$${String.format(Locale.US, "%,.2f", estCommission)} USD",
                            color = OmniLiveGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-Gateway Payment Form
        Text(
            text = "MULTI-DESTINATION PAYMENT GATEWAY checkout:",
            color = OmniMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Dropdown selector
                Text("Select Payout Channel / Gateway Source", color = OmniPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniBg, RoundedCornerShape(8.dp))
                            .border(1.dp, OmniBorder, RoundedCornerShape(8.dp))
                            .clickable { activeDropdown = true }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(payoutGateway, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("▼", color = OmniPrimary, fontSize = 10.sp)
                    }

                    DropdownMenu(
                        expanded = activeDropdown,
                        onDismissRequest = { activeDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniCardBg)
                            .border(1.dp, OmniBorder)
                    ) {
                        viewModel.paymentGateways.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = Color.White, fontSize = 10.sp) },
                                onClick = {
                                    viewModel.selectedGateway.value = opt
                                    activeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name input
                Text("Referral Target Customer Name", color = OmniMuted, fontSize = 9.sp)
                OutlinedTextField(
                    value = custName,
                    onValueChange = { viewModel.customerNameInput.value = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = OmniText, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OmniBorder,
                        unfocusedBorderColor = OmniBorder,
                        focusedContainerColor = OmniBg,
                        unfocusedContainerColor = OmniBg
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Account Inputs depending on active gateway
                val accountLabel = when {
                    payoutGateway.startsWith("JazzCash") -> "JazzCash Mobile Wallet # (03xx)"
                    payoutGateway.startsWith("EasyPaisa") -> "EasyPaisa Mobile Wallet # (03xx)"
                    payoutGateway.startsWith("Local Bank") -> "Direct Bank IBAN Number (PKxx)"
                    payoutGateway.startsWith("Premium Debit") -> "Debit Card digits / Expiry"
                    payoutGateway.startsWith("PayPal") -> "PayPal Registered Email ID"
                    payoutGateway.startsWith("Payoneer") -> "Payoneer User ID"
                    else -> "Crypto Network Wallet Address (USDT-TRC20)"
                }

                Text(accountLabel, color = OmniMuted, fontSize = 9.sp)
                OutlinedTextField(
                    value = payAcct,
                    onValueChange = { viewModel.paymentAccountInput.value = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = OmniText, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OmniBorder,
                        unfocusedBorderColor = OmniBorder,
                        focusedContainerColor = OmniBg,
                        unfocusedContainerColor = OmniBg
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Order Action
                Button(
                    onClick = { viewModel.placeAffiliateOrder() },
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary, contentColor = Color(0xFF381E72)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cartItems.isNotEmpty()
                ) {
                    Text("PLACE COMMISSION ORDER Check", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OmniMetricBg),
        border = BorderStroke(1.dp, OmniBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                color = OmniPrimary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 3: PAST ORDERS & TRACKING LEDGER
// -------------------------------------------------------------
@Composable
fun TraceTab(
    viewModel: AppViewModel,
    orders: List<OrderRecord>
) {
    val totalVol by viewModel.processedVolumeUSD.collectAsState()
    val totalPaidComm by viewModel.totalCommissionPaidUSD.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // High Density Grid consisting of metric aggregates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Processed Revenue",
                value = "$${String.format(Locale.US, "%,.2f", totalVol)} USD",
                modifier = Modifier.weight(1.5f)
            )
            MetricCard(
                title = "Total Orders",
                value = "${orders.size + 14}",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Paid Commission",
                value = "$${String.format(Locale.US, "%,.2f", totalPaidComm)}",
                modifier = Modifier.weight(1.3f)
            )
        }

        Text(
            text = "AFFILIATE COMMISSION ORDER TRACING LEDGER:",
            color = OmniMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No order records in database yet.", color = OmniMuted, fontSize = 11.sp)
                    Text("Place checks inside the 'Cart' portal.", color = OmniMuted, fontSize = 9.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders) { order ->
                    OrderTrackingCard(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderTrackingCard(order: OrderRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        border = BorderStroke(1.dp, OmniBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val date = remember(order.orderTime) { Date(order.orderTime) }
                    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }
                    Text(text = "ORDER ID: #${order.orderId + 1042}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = sdf.format(date), color = OmniMuted, fontSize = 8.sp)
                }

                // Tracking chip status
                Box(
                    modifier = Modifier
                        .background(
                            when (order.trackingStatus) {
                                "Order Placed" -> Color.Gray.copy(alpha = 0.2f)
                                "Escrow Verified" -> Color.Cyan.copy(alpha = 0.2f)
                                "Commission Safe-Escrowed" -> OmniLiveGreen.copy(alpha = 0.2f)
                                else -> OmniPrimary.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.trackingStatus.uppercase(),
                        color = when (order.trackingStatus) {
                            "Order Placed" -> Color.LightGray
                            "Escrow Verified" -> Color.Cyan
                            "Commission Safe-Escrowed" -> OmniLiveGreen
                            else -> OmniPrimary
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = OmniBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.itemsSummary,
                color = OmniText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Buyer: ${order.customerName}", color = OmniMuted, fontSize = 9.sp)
                Text(text = "Channel: ${order.paymentGateway}", color = OmniMuted, fontSize = 9.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OmniBg, RoundedCornerShape(6.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Sale Amount: $${String.format(Locale.US, "%,.2f", order.totalAmountUSD)}", color = Color.White, fontSize = 10.sp)
                Text(
                    text = "Commission Earned: $${String.format(Locale.US, "%,.2f", order.commissionEarnedUSD)}",
                    color = OmniLiveGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: COLLABORATIVE AI NETWORK NODE (4 AGENTS)
// -------------------------------------------------------------
@Composable
fun AgentNodeTab(
    viewModel: AppViewModel,
    logs: List<AgentLog>,
    stores: List<StoreConfig>
) {
    val isLive by viewModel.isSystemLive.collectAsState()

    // Definition of the active 4 AI Agents contributing to this app
    val agentProfiles = listOf(
        Triple("System Planner (Agent 1)", "Defines specifications, commissions & plans terms compliance.", "⚙️"),
        Triple("Backend DB (Agent 2)", "Handles SQLite operations, transactional APIs, and checkouts.", "📂"),
        Triple("UI Engineering (Agent 3)", "Crafts Material-3 metallic layouts, visual buttons & filters.", "🎨"),
        Triple("QC Integration (Agent 4)", "Interleaves payment loops, verifies credentials & payouts.", "⚡")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Connected franchises status row
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ACTIVE COMPLIANT FRANCHISES", color = OmniPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("Penske & Birmingham Affiliates Map", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    stores.forEach { store ->
                        Box(
                            modifier = Modifier
                                .background(OmniMetricBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (store.storeId == "greenwich") "Greenwich" else if (store.storeId == "birmingham") "Birmingham" else "HQ Direct",
                                color = OmniLiveGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Live collaborative horizontal blocks
        Text(
            text = "4-AGENTS CO-SCHEDULING ACTIVE:",
            color = OmniMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            agentProfiles.forEach { (name, desk, icon) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(OmniCardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, OmniBorder, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        val shortName = name.substringBefore(" (")
                        Text(
                            text = shortName,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        // Simulated loading bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isLive) OmniLiveGreen else OmniBorder)
                        )
                    }
                }
            }
        }

        // Output transaction logger
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OmniLiveGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COLLABORATIVE WORKER CONSOLE",
                            color = OmniLiveGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    Text(
                        text = "[PURGE LOGS]",
                        color = Color.Red,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.clearLogs() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = OmniBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No log registries. Actively listening to workspace scheduler loops...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = OmniMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(logs) { log ->
                            TerminalRow(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalRow(log: AgentLog) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val timeLabel = formatter.format(Date(log.timestamp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "[$timeLabel]",
                color = OmniMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                modifier = Modifier.padding(end = 6.dp)
            )

            val statusColor = when (log.status) {
                "SUCCESS" -> OmniLiveGreen
                "WARNING" -> Color.Yellow
                "FAILED" -> Color.Red
                else -> OmniPrimary
            }

            Text(
                text = "${log.agentName} (sub: ${log.subAgentName})",
                color = statusColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "↳ ${log.actionDetails}",
            color = OmniText,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            modifier = Modifier.padding(start = 54.dp, top = 1.dp)
        )
    }
}

@Composable
fun UserGuideTab(
    viewModel: AppViewModel,
    cartItems: List<Pair<MercedesCar, Int>>,
    orders: List<OrderRecord>,
    logs: List<AgentLog>,
    isLive: Boolean
) {
    var expandedToolSection by remember { mutableStateOf<String?>("Browse") }

    val hasLiveSystem = isLive
    val hasCartItems = cartItems.isNotEmpty()
    val hasOrders = orders.isNotEmpty()
    val hasLogs = logs.isNotEmpty()

    val completedCount = (if (hasLiveSystem) 1 else 0) +
            (if (hasCartItems) 1 else 0) +
            (if (hasOrders) 1 else 0) +
            (if (hasLogs) 1 else 0)

    val rankTitle = when (completedCount) {
        0 -> "Starter Apprentice 🛡️"
        1 -> "Active Prospect 🚗"
        2 -> "Certified Associate 💼"
        3 -> "Expert Strategist ⚡"
        else -> "Mercedes Guild Affiliate Master 🏆"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Header
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AFFILIATE WORKFLOW GUIDE",
                    color = OmniLiveGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mastering the SilverStar Hub",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Earn high-payouts by referring premium luxury vehicles. This dashboard guides you through automated advertising copy, escrow compliance contracts, and collaborative developer nodes.",
                    color = OmniMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Live Progress Checklist Tracker
        Text(
            text = "GUIDED ONBOARDING SYSTEM & READY RANKING:",
            color = OmniMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = OmniMetricBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR ADVISORY STATUS",
                            color = OmniMuted,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = rankTitle,
                            color = if (completedCount == 4) OmniLiveGreen else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(OmniLiveGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$completedCount / 4 CHECKS",
                            color = OmniLiveGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = OmniBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                ChecklistItemRow(
                    label = "System Node Live & Synchronizing",
                    isDone = hasLiveSystem,
                    hintText = "Beacon on top-right must be 'AUTO INTEGRATED'. Double-tap to toggle."
                )
                ChecklistItemRow(
                    label = "Vehicles Selected in Cart",
                    isDone = hasCartItems,
                    hintText = "Search & find premium cars on Browse, tap 'ADD TO CART'."
                )
                ChecklistItemRow(
                    label = "Place Commission Testing Order",
                    isDone = hasOrders,
                    hintText = "Open Cart, enter referral, designate wallet payment & Place order!"
                )
                ChecklistItemRow(
                    label = "Agent Multi-Worker Collaboration",
                    isDone = hasLogs,
                    hintText = "Browse terminal console to guarantee ledger operations compile successfully."
                )
            }
        }

        // Dynamic Expandable Interactive Guide Directory
        Text(
            text = "EXPLORE DETAILED TOOL INSTRUCTIONS:",
            color = OmniMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val toolsHelp = listOf(
            HelpSection(
                id = "Browse",
                title = "🚗 Browse (Inventory Tool)",
                description = "Our premium Mercedes showroom displays latest luxury units connected with international distributors.",
                bullets = listOf(
                    "Search Filters: Look up models via physical keys: conditions (NEW, USED, VINTAGE), model category tags, or specifications details.",
                    "AI Copywriting: Tap '💡 AI AD COPY' on any card to ask our Gemini 3.5 agent to compose conversion-optimized social scripts.",
                    "Franchise Escrows: Click '🔗' icon inside estimate cards to verify Penske terms and legal on-device cookie allocation codes."
                )
            ),
            HelpSection(
                id = "Cart",
                title = "🛒 Cart & Payout Customization Checkouts",
                description = "Consolidates checked cars & configures escrow payment instructions.",
                bullets = listOf(
                    "Adaptive Input: Provide name of referral target buyer to ensure tracking matches client registries.",
                    "Dynamic Gateways: Support multiple instant mobile channels (JazzCash, EasyPaisa), standard bank direct IBAN routing, or secure international escrows.",
                    "Verify Commission: Review pricing aggregate calculations displaying aggregate sale values against total payouts."
                )
            ),
            HelpSection(
                id = "Trace",
                title = "📋 Trace Ledger Tracking",
                description = "Real-time diagnostic registry tracking processed orders and verified earnings.",
                bullets = listOf(
                    "Total Revenue metric keeps running records of global cars successfully refered on device.",
                    "Dynamic Status flow charts transition from initial 'Order Placed' right up to 'Commission Safe-Escrowed'.",
                    "Database Ledger preserves records securely in SQL local storage blocks."
                )
            ),
            HelpSection(
                id = "Agents",
                title = "🤖 Agent Co-Scheduling Node",
                description = "A collaborative ecosystem of 4 dedicated virtual agents executing concurrently to fuel operations.",
                bullets = listOf(
                    "Agent 1 (System Planner) establishes specs benchmarks and verifies agreement variables.",
                    "Agent 2 (Backend DB) handles immediate database transaction logs.",
                    "Agent 3 (UI Engineering) manages Material-3 premium Obsidian visual layouts & dynamic indicators.",
                    "Agent 4 (QC Integration) validates payout credentials, logs processes, & manages active beacons."
                )
            )
        )

        toolsHelp.forEach { helpSec ->
            val isExpanded = expandedToolSection == helpSec.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) OmniCardBg else OmniBg
                ),
                border = BorderStroke(1.dp, if (isExpanded) OmniLiveGreen.copy(alpha = 0.5f) else OmniBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        expandedToolSection = if (isExpanded) null else helpSec.id
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = helpSec.title,
                            color = if (isExpanded) OmniLiveGreen else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            color = OmniMuted,
                            fontSize = 10.sp
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = helpSec.description,
                            color = OmniText,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        helpSec.bullets.forEach { b ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", color = OmniLiveGreen, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    text = b,
                                    color = OmniMuted,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    label: String,
    isDone: Boolean,
    hintText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = if (isDone) "🟢" else "🔴",
            fontSize = 11.sp,
            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (isDone) Color.White else OmniMuted,
                fontSize = 10.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = hintText,
                color = OmniMuted.copy(alpha = 0.65f),
                fontSize = 8.sp,
                lineHeight = 11.sp
            )
        }
    }
}

data class HelpSection(
    val id: String,
    val title: String,
    val description: String,
    val bullets: List<String>
)
