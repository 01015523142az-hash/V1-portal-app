package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClientAccount
import com.example.model.SkiptraceOrderEntity
import com.example.model.SkiptraceResultEntity
import com.example.ui.components.SkeletonSkiptraceResultCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val activeTab = if (selectedSubtab == "batch_trace" || selectedSubtab == "results" || selectedSubtab == "order_history" || selectedSubtab == "single_trace") {
        selectedSubtab
    } else {
        "single_trace"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        // Subtabs: Property Trace | Batch Processing | Results | History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "single_trace" to "Property Trace",
                "batch_trace" to "Batch Trace",
                "results" to "Results (${skiptraceResults.size})",
                "order_history" to "History (${skiptraceOrders.size})"
            ).forEach { (id, label) ->
                val isSelected = activeTab == id
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
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when (activeTab) {
            "single_trace" -> {
                SinglePropertyAddressSkiptraceView(
                    account = account,
                    onTraceSubmitted = { formattedAddress ->
                        onRunBatchSkiptrace(listOf(formattedAddress))
                    },
                    onTopUpCredits = onTopUpCredits,
                    onViewAllResults = { onSubtabSelected("results") }
                )
            }
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
                    onSwitchToBatch = { onSubtabSelected("single_trace") }
                )
            }
            "order_history" -> {
                SkiptraceHistoryListView(orders = skiptraceOrders)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePropertyAddressSkiptraceView(
    account: ClientAccount?,
    onTraceSubmitted: (String) -> Unit,
    onTopUpCredits: (Int) -> Unit,
    onViewAllResults: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form Fields
    var streetAddress by remember { mutableStateOf("") }
    var unitApt by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("TX") }
    var zipCode by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("Single Family Home (SFR)") }
    var knownOwnerName by remember { mutableStateOf("") }
    var enrichmentDepth by remember { mutableStateOf("Comprehensive Dossier (1 credit)") }

    // Touched tracking for validation errors
    var streetTouched by remember { mutableStateOf(false) }
    var cityTouched by remember { mutableStateOf(false) }
    var zipTouched by remember { mutableStateOf(false) }

    // Validation computations
    val isStreetValid = streetAddress.trim().length >= 4 && streetAddress.any { it.isDigit() }
    val isCityValid = city.trim().length >= 2 && city.all { it.isLetter() || it.isWhitespace() || it == '-' }
    val isZipValid = zipCode.trim().matches(Regex("^\\d{5}$"))
    val isFormValid = isStreetValid && isCityValid && isZipValid && state.length == 2

    // Flow State: idle, confirming, tracking, finished
    var isConfirmDialogOpen by remember { mutableStateOf(false) }
    var isTrackingActive by remember { mutableStateOf(false) }
    var trackerStage by remember { mutableStateOf(1) } // 1 to 4
    var trackerProgress by remember { mutableStateOf(0f) }
    var trackerStatusText by remember { mutableStateOf("Initializing CASS validation...") }
    var completedResult by remember { mutableStateOf<SkiptraceResultEntity?>(null) }
    var showTopUpDialog by remember { mutableStateOf(false) }

    val availableCredits = account?.skiptraceCredits ?: 2450

    val popularStates = listOf("TX", "FL", "GA", "AZ", "NC", "TN", "OH", "CA")
    val propertyTypes = listOf(
        "Single Family Home (SFR)",
        "Multi-Family (2-4 Units)",
        "Condo / Townhome",
        "Commercial / Industrial",
        "Vacant Land / Lot"
    )

    fun autofillAddress(street: String, c: String, s: String, z: String, type: String, owner: String = "") {
        streetAddress = street
        city = c
        state = s
        zipCode = z
        propertyType = type
        knownOwnerName = owner
        streetTouched = true
        cityTouched = true
        zipTouched = true
    }

    fun startSkipTracePipeline() {
        isConfirmDialogOpen = false
        isTrackingActive = true
        trackerStage = 1
        trackerProgress = 0.1f
        trackerStatusText = "Validating USPS CASS™ deliverability and ZIP+4 assignment..."
        completedResult = null

        val fullFormatted = "${streetAddress.trim()}${if (unitApt.isNotBlank()) " $unitApt" else ""}, ${city.trim()}, $state ${zipCode.trim()}"

        coroutineScope.launch {
            // Stage 1: USPS CASS (1.2s)
            delay(1200)
            trackerStage = 2
            trackerProgress = 0.38f
            trackerStatusText = "Matching County Tax Assessor & Deed Title Registry records..."

            // Stage 2: County Assessor (1.4s)
            delay(1400)
            trackerStage = 3
            trackerProgress = 0.72f
            trackerStatusText = "Scrubbing wireless telco carriers & FTC Do-Not-Call (DNC) registries..."

            // Stage 3: Telco & DNC (1.2s)
            delay(1200)
            trackerStage = 4
            trackerProgress = 1.0f
            trackerStatusText = "Dossier enriched successfully! 100% verified match found."

            val simulatedOwnerName = if (knownOwnerName.isNotBlank()) {
                val parts = knownOwnerName.trim().split(" ")
                parts.first() to (if (parts.size > 1) parts.drop(1).joinToString(" ") else "Owner")
            } else {
                "Arthur" to "Pendelton"
            }

            val result = SkiptraceResultEntity(
                id = "ST-${System.currentTimeMillis() % 100000}",
                batchId = "SINGLE-LIVE",
                inputAddressOrName = fullFormatted,
                propertyAddress = "${streetAddress.trim()}${if (unitApt.isNotBlank()) " $unitApt" else ""}",
                propertyCity = city.trim(),
                propertyState = state,
                propertyZip = zipCode.trim(),
                ownerFirstName = simulatedOwnerName.first,
                ownerLastName = simulatedOwnerName.second,
                mailingAddress = "${streetAddress.trim()}, ${city.trim()}, $state ${zipCode.trim()}",
                phone1 = "(214) 555-0184",
                phone1Type = "Mobile",
                phone1Dnc = false,
                phone2 = "(214) 555-0199",
                phone2Type = "Mobile",
                phone2Dnc = false,
                phone3 = "(972) 555-7341",
                phone3Type = "Landline",
                phone3Dnc = true,
                email1 = "${simulatedOwnerName.first.lowercase()}.${simulatedOwnerName.second.lowercase()}@gmail.com",
                email2 = "contact@${city.trim().lowercase().replace(" ", "")}property.net",
                relativeName = "Linda ${simulatedOwnerName.second} (Spouse)",
                createdAt = System.currentTimeMillis()
            )

            completedResult = result
            onTraceSubmitted(fullFormatted)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Credits Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Available Skip Credits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${String.format(Locale.US, "%,d", availableCredits)} Credits",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showTopUpDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Credits", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Active Tracker (if tracking or completed)
        if (isTrackingActive) {
            item {
                PropertySkipTraceStatusTrackerCard(
                    stage = trackerStage,
                    progress = trackerProgress,
                    statusText = trackerStatusText,
                    result = completedResult,
                    onReset = {
                        isTrackingActive = false
                        completedResult = null
                        streetAddress = ""
                        unitApt = ""
                        city = ""
                        zipCode = ""
                        knownOwnerName = ""
                        streetTouched = false
                        cityTouched = false
                        zipTouched = false
                    },
                    onViewAllResults = onViewAllResults
                )
            }
        }

        // Property Address Form Card
        item {
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
                                text = "Single Property Skip Trace",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enter address details for instant multi-carrier & deed enrichment",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Form validation pill
                        val validCount = (if (isStreetValid) 1 else 0) + (if (isCityValid) 1 else 0) + (if (isZipValid) 1 else 0) + (if (state.length == 2) 1 else 0)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFormValid) SleekSuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isFormValid) "Ready to Trace" else "$validCount/4 Validated",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFormValid) SleekSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Quick Sample Autofill Chips
                    Text("Quick Test Samples:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            SuggestionChip(
                                onClick = { autofillAddress("4812 Meadowbrook Dr", "Dallas", "TX", "75227", "Single Family Home (SFR)", "Arthur Pendelton") },
                                label = { Text("Dallas, TX (SFR)", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = { autofillAddress("720 NW 45th St", "Miami", "FL", "33127", "Multi-Family (2-4 Units)", "Carlos Mendez") },
                                label = { Text("Miami, FL (Multi)", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = { autofillAddress("1138 Cascade Rd SW", "Atlanta", "GA", "30311", "Single Family Home (SFR)", "DeAndre Johnson") },
                                label = { Text("Atlanta, GA", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = { autofillAddress("3820 E Thomas Rd", "Phoenix", "AZ", "85018", "Commercial / Industrial", "Desert West Holdings LLC") },
                                label = { Text("Phoenix, AZ", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Street Address
                    OutlinedTextField(
                        value = streetAddress,
                        onValueChange = {
                            streetAddress = it
                            streetTouched = true
                        },
                        label = { Text("Street Address *") },
                        placeholder = { Text("e.g. 4812 Meadowbrook Drive") },
                        isError = streetTouched && !isStreetValid,
                        supportingText = {
                            if (streetTouched && !isStreetValid) {
                                Text("Enter a valid street address with street number", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                            }
                        },
                        trailingIcon = {
                            if (isStreetValid) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = SleekSuccessGreen, modifier = Modifier.size(18.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skiptrace_street_input")
                    )

                    Spacer(Modifier.height(8.dp))

                    // Unit / Apt & Property Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = unitApt,
                            onValueChange = { unitApt = it },
                            label = { Text("Unit / Apt / Suite") },
                            placeholder = { Text("Apt 4B (Optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("skiptrace_unit_input")
                        )

                        OutlinedTextField(
                            value = propertyType,
                            onValueChange = { propertyType = it },
                            label = { Text("Property Type") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("skiptrace_type_input")
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // City & State & ZIP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // City
                        OutlinedTextField(
                            value = city,
                            onValueChange = {
                                city = it
                                cityTouched = true
                            },
                            label = { Text("City *") },
                            placeholder = { Text("Dallas") },
                            isError = cityTouched && !isCityValid,
                            supportingText = {
                                if (cityTouched && !isCityValid) {
                                    Text("Valid city required", color = MaterialTheme.colorScheme.error, fontSize = 9.sp)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("skiptrace_city_input")
                        )

                        // State Picker
                        OutlinedTextField(
                            value = state,
                            onValueChange = {
                                if (it.length <= 2) {
                                    state = it.uppercase()
                                }
                            },
                            label = { Text("State *") },
                            placeholder = { Text("TX") },
                            isError = state.length != 2,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("skiptrace_state_input")
                        )

                        // ZIP Code
                        OutlinedTextField(
                            value = zipCode,
                            onValueChange = {
                                if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                                    zipCode = it
                                    zipTouched = true
                                }
                            },
                            label = { Text("ZIP *") },
                            placeholder = { Text("75227") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = zipTouched && !isZipValid,
                            supportingText = {
                                if (zipTouched && !isZipValid) {
                                    Text("5 digits", color = MaterialTheme.colorScheme.error, fontSize = 9.sp)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("skiptrace_zip_input")
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Quick State Chips
                    Text("Quick State Selection:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(popularStates) { st ->
                            val isSel = state == st
                            FilterChip(
                                selected = isSel,
                                onClick = { state = st },
                                label = { Text(st, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Optional Owner Name & Depth
                    OutlinedTextField(
                        value = knownOwnerName,
                        onValueChange = { knownOwnerName = it },
                        label = { Text("Known Owner Name (Optional)") },
                        placeholder = { Text("e.g. Arthur Pendelton or leave blank for title search") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skiptrace_owner_input")
                    )

                    Spacer(Modifier.height(14.dp))

                    // Enrichment Tier Selector
                    Text("Enrichment Data Tier:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Standard (1 credit)" to "Verified Mobiles + DNC",
                            "Comprehensive (1 credit)" to "Mobiles + Relatives + Emails"
                        ).forEach { (tier, subtitle) ->
                            val isSelected = enrichmentDepth.startsWith(tier.take(5))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SleekPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { enrichmentDepth = tier }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = tier,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SleekPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Submit Action Button
                    Button(
                        onClick = {
                            streetTouched = true
                            cityTouched = true
                            zipTouched = true
                            if (isFormValid) {
                                if (availableCredits < 1) {
                                    showTopUpDialog = true
                                } else {
                                    isConfirmDialogOpen = true
                                }
                            } else {
                                Toast.makeText(context, "Please complete all required address fields correctly.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_single_skiptrace_btn")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isFormValid) "Review & Start Skip Trace (1 Credit)" else "Complete Required Fields",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Confirmation Modal Dialog
    if (isConfirmDialogOpen) {
        AlertDialog(
            onDismissRequest = { isConfirmDialogOpen = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = SleekPrimary)
                    Text("Confirm Address Skip Trace", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Please verify the target property parameters before deducting credits and executing the real-time enrichment pipeline:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Standardized Target Address:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${streetAddress.trim().uppercase()}${if (unitApt.isNotBlank()) " ${unitApt.trim().uppercase()}" else ""}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${city.trim().uppercase()}, $state ${zipCode.trim()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Property Type: $propertyType", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (knownOwnerName.isNotBlank()) {
                                Text("Owner Hint: $knownOwnerName", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Deduction Cost:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1 Skip Credit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Remaining Balance:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${availableCredits - 1} Credits", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { startSkipTracePipeline() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    modifier = Modifier.testTag("confirm_skiptrace_dialog_btn")
                ) {
                    Text("Confirm & Launch")
                }
            },
            dismissButton = {
                TextButton(onClick = { isConfirmDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Top-up Dialog
    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Add Skip Trace Credits", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose a credit refill package to continue skip tracing instantly:", fontSize = 12.sp)
                    listOf(
                        500 to "$15.00 ($0.03 / lead)",
                        2500 to "$62.50 ($0.025 / lead)",
                        10000 to "$200.00 ($0.02 / lead)"
                    ).forEach { (credits, price) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTopUpCredits(credits)
                                    showTopUpDialog = false
                                    Toast.makeText(context, "Added $credits credits!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${String.format(Locale.US, "%,d", credits)} Credits", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(price, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Refill", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PropertySkipTraceStatusTrackerCard(
    stage: Int,
    progress: Float,
    statusText: String,
    result: SkiptraceResultEntity?,
    onReset: () -> Unit,
    onViewAllResults: () -> Unit
) {
    val context = LocalContext.current
    val isCompleted = stage >= 4 && result != null

    val stagesInfo = listOf(
        1 to "USPS CASS™ Deliverability & Standardization",
        2 to "County Assessor & Title Records Matching",
        3 to "Tier-1 Telco Wireless & FTC DNC Scrubbing",
        4 to "Contact Dossier Enriched & Verified"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) SleekSuccessGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isCompleted) SleekSuccessGreen else SleekPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skiptrace_confirmation_tracker")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(22.dp))
                    } else {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(20.dp),
                            color = SleekPrimary,
                            strokeWidth = 2.5.dp
                        )
                    }
                    Text(
                        text = if (isCompleted) "Skip Trace Complete (100% Match)" else "Skip Trace in Progress...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) SleekSuccessGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) SleekSuccessGreen.copy(alpha = 0.15f) else SleekPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isCompleted) "MATCH VERIFIED" else "STAGE $stage OF 4",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) SleekSuccessGreen else SleekPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) SleekSuccessGreen else SleekPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            // Live status description
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            // Stage Checkpoint Indicators
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stagesInfo.forEach { (stepNumber, title) ->
                    val isDone = stage > stepNumber || (stage == 4 && stepNumber == 4 && isCompleted)
                    val isCurrent = stage == stepNumber && !isCompleted

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isDone -> SleekSuccessGreen
                                isCurrent -> SleekPrimary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                } else {
                                    Text(
                                        text = "$stepNumber",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent || isDone) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                isDone -> MaterialTheme.colorScheme.onSurface
                                isCurrent -> SleekPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }

            // If completed, show the full enriched dossier card!
            if (isCompleted && result != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), thickness = 1.dp)
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Enriched Owner Dossier",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${result.ownerFirstName} ${result.ownerLastName}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${result.propertyAddress}, ${result.propertyCity}, ${result.propertyState} ${result.propertyZip}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SleekSuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "DNC CLEAN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekSuccessGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Verified Phone Numbers
                        Text("Verified Contact Numbers:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))

                        // Phone 1 (Mobile)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                Text(result.phone1, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Surface(shape = RoundedCornerShape(4.dp), color = SleekPrimary.copy(alpha = 0.12f)) {
                                    Text("Mobile (Score 98)", fontSize = 9.sp, color = SleekPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${result.phone1}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = SleekSuccessGreen, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("phone", result.phone1))
                                        Toast.makeText(context, "Copied phone number", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Phone 2 (Mobile)
                        if (!result.phone2.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                    Text(result.phone2, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text("Secondary", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${result.phone2}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = SleekSuccessGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Email
                        if (!result.email1.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = SleekSecondary, modifier = Modifier.size(16.dp))
                                Text(result.email1, fontSize = 12.sp, color = SleekSecondary)
                            }
                        }

                        // Relatives
                        if (!result.relativeName.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Text("Relative: ${result.relativeName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Actions: Trace Another or View Results
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Trace Another", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onViewAllResults,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("View In Results", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
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
