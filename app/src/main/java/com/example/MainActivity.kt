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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.data.AgentLog
import com.example.data.StoreConfig
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val isLive by viewModel.isSystemLive.collectAsState()

    var currentTab by remember { mutableStateOf("Monitor") } // "Monitor", "Agents", "Stores", "Admin"
    var showReportDialog by remember { mutableStateOf<String?>(null) } // "CSV" or "TEXT" or null
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OmniBg,
        bottomBar = {
            // High Density Bottom Navigation matching Design HTML layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(OmniNavBg)
                    .border(1.dp, OmniBorder, RectangleShape)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Monitor Tab
                NavigationItem(
                    icon = "🏠",
                    label = "Monitor",
                    isSelected = currentTab == "Monitor",
                    onClick = { currentTab = "Monitor" },
                    modifier = Modifier.testTag("nav_monitor")
                )
                // Agents Tab
                NavigationItem(
                    icon = "🤖",
                    label = "Agents",
                    isSelected = currentTab == "Agents",
                    onClick = { currentTab = "Agents" },
                    modifier = Modifier.testTag("nav_agents")
                )
                // Stores Tab
                NavigationItem(
                    icon = "🏬",
                    label = "Stores",
                    isSelected = currentTab == "Stores",
                    onClick = { currentTab = "Stores" },
                    modifier = Modifier.testTag("nav_stores")
                )
                // Admin Settings Tab
                NavigationItem(
                    icon = "⚙️",
                    label = "Admin",
                    isSelected = currentTab == "Admin",
                    onClick = { currentTab = "Admin" },
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header component matching HTML Styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OmniLink AI",
                        color = OmniText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "9 Stores • 4 Primary Agents",
                        color = OmniMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Pulsing glowing status badge
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(OmniBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.toggleSystemLive() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLive) OmniLiveGreen.copy(alpha = pulseAlpha)
                                else Color.Red.copy(alpha = pulseAlpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLive) "SYSTEM LIVE" else "SYSTEM IDLE",
                        color = if (isLive) OmniLiveGreen else OmniMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Divider(color = OmniBorder, thickness = 1.dp)

            // Switch content based on chosen tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentTab) {
                    "Monitor" -> MonitorTabContent(
                        viewModel = viewModel,
                        logs = logs,
                        stores = stores,
                        onOpenReports = { showReportDialog = "TEXT" }
                    )
                    "Agents" -> AgentsTabContent(
                        viewModel = viewModel,
                        logs = logs,
                        onShowCSV = { showReportDialog = "CSV" },
                        onShowText = { showReportDialog = "TEXT" }
                    )
                    "Stores" -> StoresTabContent(
                        viewModel = viewModel,
                        stores = stores
                    )
                    "Admin" -> AdminTabContent(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Modal dialogs showing export data
    if (showReportDialog != null) {
        val title = if (showReportDialog == "CSV") "AppSheet CSV Export" else "Standard Agent Logs Report"
        val exportText = if (showReportDialog == "CSV") {
            viewModel.exportLogsToCSV()
        } else {
            viewModel.exportLogsToTextFormat()
        }

        AlertDialog(
            onDismissRequest = { showReportDialog = null },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary, contentColor = Color(0xFF381E72)),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportText))
                        Toast.makeText(context, "Copied report to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReportDialog = null }
                ) {
                    Text("Close", color = OmniText)
                }
            },
            containerColor = OmniCardBg,
            title = {
                Text(
                    text = title,
                    color = OmniText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "This structured output is extracted live from the local Room database SQLite cursor. Ready to load into AppSheet, Google Sheets, or local text databases.",
                        color = OmniMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(OmniBg)
                            .border(1.dp, OmniBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = OmniText
                        )
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
    Column(
        modifier = modifier
            .testTag("nav_item_${isSelected}")
            .width(72.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFE8DEF8), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
        } else {
            Text(text = icon, fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) OmniText else OmniMuted.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// -------------------------------------------------------------
// TAB 1: MONITOR CONTENT
// -------------------------------------------------------------
@Composable
fun MonitorTabContent(
    viewModel: AppViewModel,
    logs: List<AgentLog>,
    stores: List<StoreConfig>,
    onOpenReports: () -> Unit
) {
    val volumeUSD by viewModel.processedVolumeUSD.collectAsState()
    val isLive by viewModel.isSystemLive.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // High Density Grid consisting of 3 metrics from theme HTML spec
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Processed Revenue",
                value = "$${String.format(Locale.US, "%,.2f", volumeUSD)}",
                modifier = Modifier.weight(1.5f)
            )
            MetricCard(
                title = "Total Orders",
                value = "${(volumeUSD / 14.77).toInt()}",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Avg ROI",
                value = "4.2x",
                modifier = Modifier.weight(0.9f)
            )
        }

        // AI Agent Network Section with segregated lists
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(14.dp)
                                .background(OmniPrimary, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Agent Network Status",
                            color = OmniText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "Auto-Scaling Active",
                        color = OmniLiveGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 grid representing the 4 agents
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AgentStatusBlock(
                        title = "Sourcing",
                        meta = "AliBaba / CJ Sync",
                        percentageText = "98%",
                        progressSegments = 2,
                        modifier = Modifier.weight(1f)
                    )
                    AgentStatusBlock(
                        title = "Listing",
                        meta = "Etsy / eBay / Wed2C",
                        percentageText = "Synced",
                        progressSegments = 3,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AgentStatusBlock(
                        title = "Creative",
                        meta = "Video Ads (TikTok)",
                        percentageText = if (isLive) "Rendering" else "Idle",
                        progressSegments = if (isLive) 1 else 0,
                        modifier = Modifier.weight(1f)
                    )
                    AgentStatusBlock(
                        title = "Payment",
                        meta = "Unified Gateway",
                        percentageText = "Secured",
                        progressSegments = 3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active Store Connectors Section (Visual quick tags)
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Affiliate Connectors",
                        color = OmniText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(OmniMetricBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        val activeStoresCount = stores.count { it.status == "CONNECTED" }
                        Text(
                            text = "$activeStoresCount/9 Online",
                            color = OmniPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Map of custom stores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("eBay", "AliX", "Etsy", "CJ", "D24").forEach { tag ->
                        val isOnline = stores.firstOrNull { it.storeId.contains(tag, ignoreCase = true) }?.status != "OFFLINE"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OmniBg)
                                .border(
                                    1.dp,
                                    if (isOnline) OmniPrimary.copy(alpha = 0.5f) else OmniBorder,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when(tag) {
                                        "eBay" -> "🛍️"
                                        "AliX" -> "🌏"
                                        "Etsy" -> "🎨"
                                        "CJ" -> "📦"
                                        else -> "💰"
                                    },
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tag,
                                    color = if (isOnline) OmniText else OmniMuted.copy(alpha = 0.5f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Social Campaign Feed & Reports Button Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(OmniMetricBg, RoundedCornerShape(16.dp))
                    .border(1.dp, OmniBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(OmniBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("IG", color = OmniPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Marketing Run", color = OmniMuted, fontSize = 9.sp)
                    Text("Insta/TikTok Ads", color = OmniText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(OmniPrimary)
                    .clickable { onOpenReports() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "REPORTS",
                    color = Color(0xFF381E72),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
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
        shape = RoundedCornerShape(16.dp),
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
                color = OmniText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AgentStatusBlock(
    title: String,
    meta: String,
    percentageText: String,
    progressSegments: Int, // 0 to 3
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(OmniBg, RoundedCornerShape(16.dp))
            .border(1.dp, OmniBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = OmniText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = percentageText, color = OmniLiveGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(text = meta, color = OmniMuted, fontSize = 8.sp, modifier = Modifier.padding(top = 1.dp))

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar subdivided in segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i <= progressSegments) OmniPrimary else OmniBorder
                        )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: AGENT CONSOLE & CONTROLS
// -------------------------------------------------------------
@Composable
fun AgentsTabContent(
    viewModel: AppViewModel,
    logs: List<AgentLog>,
    onShowCSV: () -> Unit,
    onShowText: () -> Unit
) {
    val context = LocalContext.current
    val agentCategories = listOf(
        AgentMeta(
            "Sourcing Agent",
            "Ali Baba, AliExpress, CJ Dropshipping, Daraz.pk catalog integration.",
            "📡",
            listOf("Ali Sourcing Bot", "CJ Catalog Crawler", "AliExpress Crawler", "Daraz Scraper")
        ),
        AgentMeta(
            "Listing & Pricing",
            "Multi-channel inventory publications, SEO tagging, markup calibration.",
            "🏷️",
            listOf("Bulker", "Price Optimizer", "Wed2C Dynamic Publisher", "SEO Tag Generator")
        ),
        AgentMeta(
            "Creative Marketer",
            "Continuous TikTok ad scheduling, Facebook campaigns, YouTube visual buffers.",
            "🎨",
            listOf("TikTok Video Renderer", "Meta Ad Writer", "Instagram Visualizer", "WhatsApp Broadcaster")
        ),
        AgentMeta(
            "Payment & Gateway",
            "Consolidated checkout coordination, fraud auditing, AppSheet log syncing.",
            "🔐",
            listOf("SafePay Broker", "OmniGate Coordinator", "AppSheet Sync Bot", "CSV Exporter")
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Exporter Controls Row
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sync Local AppSheet:",
                    color = OmniText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = OmniMetricBg, contentColor = OmniPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp),
                    onClick = onShowCSV
                ) {
                    Text("AppSheet CSV", fontSize = 9.sp)
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary, contentColor = Color(0xFF381E72)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp),
                    onClick = onShowText
                ) {
                    Text("Plain Text", fontSize = 9.sp)
                }
            }
        }

        // Active Terminal Log Box (height bound)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
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
                            text = "LIVE TRANSACTION CONSOLE",
                            color = OmniLiveGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    Text(
                        text = "[CLEAR]",
                        color = Color.Red,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
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
                            text = "No log indices. Actively waiting for automation loop cycles...",
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

        Divider(color = OmniBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

        // Manual Task Trigger Panel (horizontal scroll)
        Text(
            text = "TRIGGER WORKFORCE RUNS",
            color = OmniPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agentCategories.forEach { meta ->
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OmniCardBg)
                        .border(1.dp, OmniBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            Toast
                                .makeText(
                                    context,
                                    "Dispatched instruction to ${meta.name}!",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            viewModel.triggerManualFullSync()
                        }
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = meta.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = meta.name,
                                color = OmniText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Subagents: ${meta.subagents.joinToString(", ")}",
                            color = OmniMuted,
                            fontSize = 8.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.height(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(OmniMetricBg, RoundedCornerShape(4.dp))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RUN ROUTINE", color = OmniPrimary, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

data class AgentMeta(
    val name: String,
    val description: String,
    val icon: String,
    val subagents: List<String>
)

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

// -------------------------------------------------------------
// TAB 3: STORES INTEGRATION
// -------------------------------------------------------------
@Composable
fun StoresTabContent(
    viewModel: AppViewModel,
    stores: List<StoreConfig>
) {
    val currentWed2cIdx by viewModel.selectedWed2cIndex.collectAsState()
    val wed2cList = viewModel.wed2cUrls
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Special Wed2C Store Links routing picker card
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniMetricBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Wed2C DROPSHIPPING MULTI-STORE CHANNEL ROUTER",
                    color = OmniPrimary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wed2C platforms dynamic selector. Direct the autonomous AI agent routine to any of your 9 specified regional storefronts instantly:",
                    color = OmniMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniBg, RoundedCornerShape(8.dp))
                            .border(1.dp, OmniBorder, RoundedCornerShape(8.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wed2cList[currentWed2cIdx],
                            color = OmniText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "▼", color = OmniPrimary, fontSize = 10.sp)
                    }

                    // Simple standard material scroll menu
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(OmniCardBg)
                            .border(1.dp, OmniBorder)
                    ) {
                        wed2cList.forEachIndexed { index, url ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = url,
                                        color = OmniText,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                },
                                onClick = {
                                    viewModel.selectWed2cStoreIndex(index)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // List of all 9 connected stores
        Text(
            text = "CONNECTED AFFILIATE & DROPSHIPPING STORES (9 TOTAL)",
            color = OmniMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (stores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OmniPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stores) { config ->
                    StoreConfigCard(config = config, onToggleOnline = {
                        viewModel.updateStoreStatusToggle(config.storeId)
                    }, onUrlChange = { newUrl ->
                        viewModel.updateStoreLink(config.storeId, newUrl)
                    })
                }
            }
        }
    }
}

@Composable
fun StoreConfigCard(
    config: StoreConfig,
    onToggleOnline: () -> Unit,
    onUrlChange: (String) -> Unit
) {
    var editedUrl by remember(config.affiliateLink) { mutableStateOf(config.affiliateLink) }
    val isOnline = config.status == "CONNECTED"

    Card(
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        border = BorderStroke(1.dp, OmniBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Store Initial graphic icon placeholder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OmniBg)
                        .border(1.dp, OmniBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (config.storeId) {
                            "alibaba" -> "🏮"
                            "etsy" -> "🎨"
                            "ebay" -> "📦"
                            "cjdropshipping" -> "🌍"
                            "digistore24" -> "💲"
                            "mercedes" -> "🚗"
                            "aliexpress" -> "🛍️"
                            "daraz" -> "🛒"
                            else -> "⚡"
                        },
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.storeName,
                        color = OmniText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${config.productCount} active listings synced",
                        color = OmniMuted,
                        fontSize = 9.sp
                    )
                }

                // Status Badge online / offline
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOnline) OmniLiveGreen.copy(alpha = 0.15f) else OmniBorder)
                        .clickable { onToggleOnline() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) OmniLiveGreen else OmniMuted)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOnline) "ACTIVE" else "OFFLINE",
                            color = if (isOnline) OmniLiveGreen else OmniMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Affiliate / Sourcing url config
            Text(
                text = "Target Integration / Affiliate Referral Link:",
                color = OmniMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = editedUrl,
                    onValueChange = { newVal -> editedUrl = newVal },
                    textStyle = TextStyle(color = OmniText, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OmniPrimary,
                        unfocusedBorderColor = OmniBorder,
                        focusedContainerColor = OmniBg,
                        unfocusedContainerColor = OmniBg
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OmniPrimary)
                        .clickable { onUrlChange(editedUrl) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SAVE",
                        color = Color(0xFF381E72),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: ADMIN GATEWAY & COSTS
// -------------------------------------------------------------
@Composable
fun AdminTabContent(viewModel: AppViewModel) {
    val totalMaintenance by viewModel.totalMonthlyMaintenanceCost.collectAsState()
    val baseServer by viewModel.baseServerCost.collectAsState()
    val aiCredits by viewModel.aiCreditsSpent.collectAsState()
    val adsBudget by viewModel.adsDailyBudget.collectAsState()
    val proxyCost by viewModel.proxyCostPerMo.collectAsState()

    val gatewayMethod by viewModel.selectedGatewayProvider.collectAsState()
    val isTestMode by viewModel.isGatewayTestMode.collectAsState()
    val volumeUSD by viewModel.processedVolumeUSD.collectAsState()

    val adPlatform by viewModel.selectedSocialPlatform.collectAsState()
    val pName by viewModel.inputProductName.collectAsState()
    val pDetails by viewModel.inputProductDetails.collectAsState()
    val generatedAdText by viewModel.generatedCopyText.collectAsState()
    val isGeneratingAd by viewModel.isGeneratingAd.collectAsState()

    var gatewayExpanded by remember { mutableStateOf(false) }
    var adPlatformExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Unified Payment Gateway configuration (combining all site transactions)
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unified Storefront Payment Gateway",
                        color = OmniText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A single, consolidated digital terminal handling online checkouts across all 9 stores (eBay, AliExpress, Wed2C, etc.) concurrently.",
                    color = OmniMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select merchant gateway provider
                Text("Select Primary Processing Vault:", color = OmniMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniBg, RoundedCornerShape(8.dp))
                            .border(1.dp, OmniBorder, RoundedCornerShape(8.dp))
                            .clickable { gatewayExpanded = true }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = gatewayMethod, color = OmniPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "▼", color = OmniPrimary, fontSize = 10.sp)
                    }

                    DropdownMenu(
                        expanded = gatewayExpanded,
                        onDismissRequest = { gatewayExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(OmniCardBg)
                            .border(1.dp, OmniBorder)
                    ) {
                        viewModel.gatewayProviders.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item, color = OmniText, fontSize = 11.sp) },
                                onClick = {
                                    viewModel.selectedGatewayProvider.value = item
                                    gatewayExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Route Modes and active checkout status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OmniBg, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTestMode) "GATEWAY LINE TEST_MODE" else "GATEWAY SECURED ACTIVE",
                            color = if (isTestMode) Color.Yellow else OmniLiveGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTestMode) "Sandbox routes enabled for diagnostics" else "Synchronized SSL tunnel operational on all sites",
                            color = OmniMuted,
                            fontSize = 8.sp
                        )
                    }

                    Switch(
                        checked = isTestMode,
                        onCheckedChange = { viewModel.isGatewayTestMode.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Yellow,
                            checkedTrackColor = Color.Yellow.copy(alpha = 0.3f),
                            uncheckedThumbColor = OmniMuted,
                            uncheckedTrackColor = OmniBorder
                        )
                    )
                }
            }
        }

        // Monthly Maintenance Cost Estimator Sheet
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Total Maintenance cost for 1 Month",
                        color = OmniText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A dynamic ledger reflecting overall infrastructure margins, calculated in real-time as you adjust your ad-spent or agent models:",
                    color = OmniMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Itemized Cost sliders
                InteractiveCostSlider(
                    label = "Server Scale hosting capacity",
                    value = baseServer,
                    units = "$/mo",
                    rangeMin = 15f,
                    rangeMax = 150f,
                    onValueChange = { newVal -> viewModel.baseServerCost.value = newVal.toInt() }
                )

                InteractiveCostSlider(
                    label = "AI Agent Credits (M3/GPT tokens)",
                    value = aiCredits,
                    units = "$/mo",
                    rangeMin = 10f,
                    rangeMax = 100f,
                    onValueChange = { newVal -> viewModel.aiCreditsSpent.value = newVal.toInt() }
                )

                InteractiveCostSlider(
                    label = "Social Ads Daily Spend Tracker",
                    value = adsBudget,
                    units = "$/day",
                    rangeMin = 5f,
                    rangeMax = 200f,
                    onValueChange = { newVal -> viewModel.adsDailyBudget.value = newVal.toInt() }
                )

                InteractiveCostSlider(
                    label = "Proxy network limits (for scanners)",
                    value = proxyCost,
                    units = "$/mo",
                    rangeMin = 5f,
                    rangeMax = 80f,
                    onValueChange = { newVal -> viewModel.proxyCostPerMo.value = newVal.toInt() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Aggregate Cost calculation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(OmniPrimary)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL ANTICIPATED MONTHLY OUTLAY",
                            color = Color(0xFF381E72),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "$$totalMaintenance.00 USD",
                            color = Color(0xFF381E72),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Approx. $${String.format(Locale.getDefault(), "%.2f", totalMaintenance / 30f)} daily upkeep cost",
                            color = Color(0xFF381E72).copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Gemini Copywriting Generator Console (Marketing Products through social feeds)
        Card(
            colors = CardDefaults.cardColors(containerColor = OmniCardBg),
            border = BorderStroke(1.dp, OmniBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Campaign Generator (Social Feeds)",
                        color = OmniText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Craft viral converting ad texts for Facebook, TikTok, YouTube, WhatsApp, and Instagram using the connected Gemini models.",
                    color = OmniMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Social Channel Dropdown
                Text("Select Social Channel:", color = OmniMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(3.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniBg, RoundedCornerShape(8.dp))
                            .border(1.dp, OmniBorder, RoundedCornerShape(8.dp))
                            .clickable { adPlatformExpanded = true }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = adPlatform, color = OmniPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "▼", color = OmniPrimary, fontSize = 10.sp)
                    }

                    DropdownMenu(
                        expanded = adPlatformExpanded,
                        onDismissRequest = { adPlatformExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(OmniCardBg)
                            .border(1.dp, OmniBorder)
                    ) {
                        viewModel.socialPlatforms.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item, color = OmniText, fontSize = 11.sp) },
                                onClick = {
                                    viewModel.selectedSocialPlatform.value = item
                                    adPlatformExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Input
                Text("Product Name:", color = OmniMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = pName,
                    onValueChange = { viewModel.inputProductName.value = it },
                    textStyle = TextStyle(color = OmniText, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OmniPrimary,
                        unfocusedBorderColor = OmniBorder,
                        focusedContainerColor = OmniBg,
                        unfocusedContainerColor = OmniBg
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product details Input
                Text("Product Sells Hooks & Features details:", color = OmniMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = pDetails,
                    onValueChange = { viewModel.inputProductDetails.value = it },
                    textStyle = TextStyle(color = OmniText, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OmniPrimary,
                        unfocusedBorderColor = OmniBorder,
                        focusedContainerColor = OmniBg,
                        unfocusedContainerColor = OmniBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Generate trigger button
                Button(
                    onClick = { viewModel.generateMarketingAd() },
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary, contentColor = Color(0xFF381E72)),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGeneratingAd,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGeneratingAd) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF381E72))
                    } else {
                        Text("GENERATE VIRAL SOCIAL AD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Output text
                if (generatedAdText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Generated Draft Campaign Copy:", color = OmniPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OmniBg, RoundedCornerShape(12.dp))
                            .border(1.dp, OmniBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = generatedAdText,
                                color = OmniText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = OmniBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            // Copy creative button
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = OmniMetricBg, contentColor = OmniPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(26.dp),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedAdText))
                                    Toast.makeText(context, "Creative ad copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("COPY TEXT", fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveCostSlider(
    label: String,
    value: Int,
    units: String,
    rangeMin: Float,
    rangeMax: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = OmniText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text(
                text = if (units == "$/day") "$value $/day ($${value * 30}/mo)" else "$value $units",
                color = OmniPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = rangeMin..rangeMax,
            colors = SliderDefaults.colors(
                thumbColor = OmniPrimary,
                activeTrackColor = OmniPrimary,
                inactiveTrackColor = OmniBorder
            )
        )
    }
}

// Simple alignment helper for header
object RowArrangement {
    val KeepWith = Arrangement.SpaceBetween
}

// Small Typealias or helpers
typealias TextStyle = androidx.compose.ui.text.TextStyle
