package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClientAccount
import com.example.model.SkiptraceOrderEntity
import com.example.model.SkiptraceResultEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SkiptraceScreen(
    account: ClientAccount?,
    skiptraceOrders: List<SkiptraceOrderEntity>,
    skiptraceResults: List<SkiptraceResultEntity>,
    isProcessingBatch: Boolean,
    batchProgress: Float,
    batchStatusText: String,
    selectedSubtab: String,
    onSubtabSelected: (String) -> Unit,
    onRunBatchSkiptrace: (List<String>) -> Unit,
    onTopUpCredits: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        // Subtabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "batch_trace" to "Batch Processing",
                "results" to "Results (${skiptraceResults.size})",
                "order_history" to "History (${skiptraceOrders.size})"
            ).forEach { (id, label) ->
                val isSelected = selectedSubtab == id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) SleekPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSubtabSelected(id) }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when (selectedSubtab) {
            "batch_trace" -> {
                BatchSkiptraceFormView(
                    account = account,
                    isProcessing = isProcessingBatch,
                    progress = batchProgress,
                    statusText = batchStatusText,
                    onRunBatch = { inputs ->
                        onRunBatchSkiptrace(inputs)
                    },
                    onTopUpCredits = onTopUpCredits
                )
            }
            "results" -> {
                SkiptraceResultsListView(
                    results = skiptraceResults,
                    onSwitchToBatch = { onSubtabSelected("batch_trace") }
                )
            }
            "order_history" -> {
                SkiptraceHistoryListView(orders = skiptraceOrders)
            }
        }
    }
}

@Composable
fun BatchSkiptraceFormView(
    account: ClientAccount?,
    isProcessing: Boolean,
    progress: Float,
    statusText: String,
    onRunBatch: (List<String>) -> Unit,
    onTopUpCredits: (Int) -> Unit
) {
    val context = LocalContext.current
    var inputMode by remember { mutableStateOf("address") } // "address" or "name"
    var rawInputText by remember {
        mutableStateOf(
            "4812 Meadowbrook Dr, Dallas, TX 75227\n" +
            "1138 Cascade Rd SW, Atlanta, GA 30311\n" +
            "3820 E Thomas Rd, Phoenix, AZ 85018\n" +
            "720 NW 45th St, Miami, FL 33127\n" +
            "914 S Beacon St, Dallas, TX 75223"
        )
    }

    var showTopUpDialog by remember { mutableStateOf(false) }

    val detectedLines = remember(rawInputText) {
        rawInputText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
    val requiredCredits = detectedLines.size.coerceAtLeast(1)
    val availableCredits = account?.skiptraceCredits ?: 2450
    val hasEnoughCredits = availableCredits >= requiredCredits

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Wallet Bar
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Available Credits", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${String.format(Locale.US, "%,d", availableCredits)} Credits",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = { showTopUpDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Top Up", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                    }
                }
            }
        }

        // Batch Input Form
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BATCH SKIP TRACE PROCESSING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Paste or type property addresses or owner names (one record per line) to retrieve verified mobile phones, carrier info, DNC validation, and emails.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    // Input Mode Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = inputMode == "address",
                            onClick = { inputMode = "address" },
                            label = { Text("Property Addresses", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = inputMode == "name",
                            onClick = { inputMode = "name" },
                            label = { Text("Owner Names & Cities", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Quick Template Fillers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                inputMode = "address"
                                rawInputText = "4812 Meadowbrook Dr, Dallas, TX 75227\n" +
                                        "1138 Cascade Rd SW, Atlanta, GA 30311\n" +
                                        "3820 E Thomas Rd, Phoenix, AZ 85018\n" +
                                        "720 NW 45th St, Miami, FL 33127\n" +
                                        "914 S Beacon St, Dallas, TX 75223\n" +
                                        "2918 Glenfield Ave, Dallas, TX 75233\n" +
                                        "2408 Donald Lee Hollowell Pkwy, Atlanta, GA 30318"
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Sample Distressed Properties (7)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                inputMode = "name"
                                rawInputText = "Arthur Pendelton, Dallas, TX\n" +
                                        "Brenda Vaughn, Atlanta, GA\n" +
                                        "Charles Montgomery, Phoenix, AZ\n" +
                                        "Diana Kovacs, Miami, FL\n" +
                                        "Edward Ramsay, Fort Worth, TX"
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Sample Owners (5)", fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Text Input Area
                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("skiptrace_input_textarea"),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = {
                            Text(
                                if (inputMode == "address")
                                    "123 Main St, City, ST 12345\n456 Oak Ave, City, ST 12345"
                                else
                                    "John Doe, Dallas, TX\nJane Smith, Atlanta, GA"
                            )
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Parser summary banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${detectedLines.size} records detected • $requiredCredits credits ($${String.format(Locale.US, "%.2f", requiredCredits * 0.03f)})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "$0.03 / Match",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Processing Indicator
                    AnimatedVisibility(visible = isProcessing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SleekPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SleekPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Process Button
                    Button(
                        onClick = {
                            if (hasEnoughCredits && detectedLines.isNotEmpty()) {
                                onRunBatch(detectedLines)
                            } else if (!hasEnoughCredits) {
                                showTopUpDialog = true
                            }
                        },
                        enabled = !isProcessing && detectedLines.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("run_batch_skiptrace_btn")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Enriching Records via LexisNexis...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Run Batch Skip Trace (${detectedLines.size} Records)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Top Up Skip Trace Credits", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a credit bundle ($0.03 / credit with instant activation):", fontSize = 12.sp)
                    listOf(
                        1000 to "$30.00 (1,000 credits)",
                        2500 to "$75.00 (2,500 credits)",
                        5000 to "$150.00 (5,000 + 500 bonus credits)"
                    ).forEach { (credits, label) ->
                        Button(
                            onClick = {
                                onTopUpCredits(credits)
                                showTopUpDialog = false
                                Toast.makeText(context, "Added $credits credits!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopUpDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SkiptraceResultsListView(
    results: List<SkiptraceResultEntity>,
    onSwitchToBatch: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterMobileOnly by remember { mutableStateOf(false) }
    var filterExcludeDnc by remember { mutableStateOf(false) }

    val filteredResults = remember(results, searchQuery, filterMobileOnly, filterExcludeDnc) {
        results.filter { item ->
            val matchesQuery = searchQuery.isEmpty() ||
                    item.ownerFirstName.contains(searchQuery, ignoreCase = true) ||
                    item.ownerLastName.contains(searchQuery, ignoreCase = true) ||
                    item.propertyAddress.contains(searchQuery, ignoreCase = true) ||
                    item.propertyCity.contains(searchQuery, ignoreCase = true) ||
                    item.phone1.contains(searchQuery)

            val matchesMobile = !filterMobileOnly || item.phone1Type.equals("Mobile", ignoreCase = true)
            val matchesDnc = !filterExcludeDnc || !item.phone1Dnc

            matchesQuery && matchesMobile && matchesDnc
        }
    }

    if (results.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(54.dp))
                Spacer(Modifier.height(14.dp))
                Text("No Skip Trace Results Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text("Run a batch skip trace to see enriched owner contact details, carrier validation, and relatives.", textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onSwitchToBatch,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Start Batch Skip Trace")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search & Filter Header
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by owner name, address, or phone...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = filterMobileOnly,
                                    onClick = { filterMobileOnly = !filterMobileOnly },
                                    label = { Text("Mobile Only", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = filterExcludeDnc,
                                    onClick = { filterExcludeDnc = !filterExcludeDnc },
                                    label = { Text("Safe (No DNC)", fontSize = 11.sp) }
                                )
                            }

                            IconButton(
                                onClick = {
                                    val csvContent = buildString {
                                        appendLine("Owner,Age,Property Address,Mailing Address,Phone 1,Type,Carrier,DNC,Email 1,Relative")
                                        filteredResults.forEach { r ->
                                            appendLine("${r.ownerFirstName} ${r.ownerLastName},${r.age},\"${r.propertyAddress}, ${r.propertyCity}, ${r.propertyState} ${r.propertyZip}\",\"${r.mailingAddress}\",${r.phone1},${r.phone1Type},${r.phone1Carrier},${if (r.phone1Dnc) "DNC" else "CLEAN"},${r.email1 ?: ""},\"${r.relativeName ?: ""}\"")
                                        }
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Skip Trace CSV", csvContent))
                                    Toast.makeText(context, "Copied ${filteredResults.size} records as CSV to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy CSV", tint = SleekPrimary)
                            }
                        }
                    }
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHOWING ${filteredResults.size} OF ${results.size} MATCHED RECORDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cards list
            items(filteredResults) { result ->
                SkiptraceResultCard(result = result)
            }
        }
    }
}

@Composable
fun SkiptraceResultCard(result: SkiptraceResultEntity) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Owner Name & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${result.ownerFirstName.take(1)}${result.ownerLastName.take(1)}".uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${result.ownerFirstName} ${result.ownerLastName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (result.isDeceased) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text("Deceased", fontSize = 10.sp, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text(
                            text = "Age ${result.age} · Verified Deed Owner",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Confidence badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SleekSuccessGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${result.phone1Confidence}% Match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SleekSuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Property vs Mailing Address
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${result.propertyAddress}, ${result.propertyCity}, ${result.propertyState} ${result.propertyZip}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val isAbsentee = result.mailingAddress.isNotEmpty() && !result.mailingAddress.startsWith(result.propertyAddress)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MarkunreadMailbox, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Mailing: ${result.mailingAddress}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isAbsentee) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SleekSecondary.copy(alpha = 0.15f)
                        ) {
                            Text("Absentee", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SleekSecondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            // Phone 1 (Primary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${result.phone1}"))
                        context.startActivity(intent)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                    Column {
                        Text(
                            text = result.phone1,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${result.phone1Type} · ${result.phone1Carrier}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // DNC Flag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (result.phone1Dnc) MaterialTheme.colorScheme.errorContainer else SleekSuccessGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (result.phone1Dnc) "DNC Listed" else "Clean Line",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.phone1Dnc) MaterialTheme.colorScheme.error else SleekSuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Phone 2 & 3 if present
            if (result.phone2 != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${result.phone2}"))
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(
                            text = result.phone2,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "(${result.phone2Type} · ${result.phone2Carrier})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Emails & Relatives
            if (result.email1 != null || result.relativeName != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (result.email1 != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = result.email1,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (result.relativeName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = result.relativeName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkiptraceHistoryListView(orders: List<SkiptraceOrderEntity>) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("No Skip Trace Batches Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text("Your past skip trace jobs and batch download links will appear here.", textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(orders) { order ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Batch ${order.id}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(order.createdAt)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SleekSuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Completed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekSuccessGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), thickness = 1.dp)
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Submitted", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${order.recordCount} Records", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("Matched Contacts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${order.resultsCount} Matches", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekSuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Credits Charged", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${order.recordCount} Credits", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
