package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ListOrderEntity
import com.example.ui.components.SkeletonBuyListOrderCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BuyListScreen(
    listOrders: List<ListOrderEntity>,
    selectedSubtab: String,
    onSubtabSelected: (String) -> Unit,
    onSubmitOrder: (String, String, String, String, Int) -> Unit
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
                "request" to "Order Niche Lists",
                "your_requests" to "Order History (${listOrders.size})"
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
                        fontSize = 12.sp,
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
            "request" -> {
                OrderNicheListBuilderView(
                    onSubmit = { type, prop, market, other, count ->
                        onSubmitOrder(type, prop, market, other, count)
                        onSubtabSelected("your_requests")
                    }
                )
            }
            "your_requests" -> {
                NicheListOrderHistoryView(orders = listOrders)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderNicheListBuilderView(
    onSubmit: (String, String, String, String, Int) -> Unit
) {
    // Demographic Niche Criteria
    val availableCriteria = listOf(
        "Absentee Owner (Out of State)" to "Owners whose tax billing address is in another state",
        "High Equity (50%+)" to "Owners with 50% to 100% estimated equity in property",
        "Tired Landlord" to "Non-owner occupied properties with 5+ years of ownership",
        "Pre-Foreclosure / Auction" to "Default notices (NOD) and scheduled foreclosure auctions",
        "Tax Delinquent" to "Properties with overdue municipal or county tax liens",
        "Probate / Inherited" to "Estates undergoing probate or passed down to heirs",
        "Vacant Property" to "USPS undeliverable verified vacant single family homes",
        "Senior Owners (65+)" to "Aging homeowners likely considering downsizing",
        "Free & Clear (100% Equity)" to "Properties with zero recorded active mortgage debt",
        "Code Violations" to "Municipal city code citations & open code cases"
    )

    var selectedCriteria by remember {
        mutableStateOf(setOf("Absentee Owner (Out of State)", "High Equity (50%+)"))
    }

    // Geographic Region Criteria
    var selectedState by remember { mutableStateOf("TX - Texas") }
    var targetCounties by remember { mutableStateOf("Dallas County, Tarrant County") }
    var targetZipCodes by remember { mutableStateOf("75227, 75223, 75228, 75217") }

    // Property Type
    val propertyTypes = listOf(
        "Single Family Home (1-4)",
        "Multi-Family (2-4 Units)",
        "Townhomes & Condos",
        "Commercial / Mixed Use",
        "Vacant Land & Lots"
    )
    var selectedPropertyType by remember { mutableStateOf("Single Family Home (1-4)") }

    // Valuation & Ownership length
    var minValuation by remember { mutableStateOf("$80,000") }
    var maxValuation by remember { mutableStateOf("$450,000") }
    var minOwnershipYears by remember { mutableStateOf("3+ Years") }
    var specialInstructions by remember { mutableStateOf("") }

    // Quantity Selector
    var requestedRecordCount by remember { mutableStateOf(5000) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Live preview count calculation based on selected criteria
    val estimatedAvailableCount = remember(selectedCriteria, selectedState, targetCounties, selectedPropertyType) {
        var base = when {
            selectedState.startsWith("TX") -> 18400
            selectedState.startsWith("FL") -> 16200
            selectedState.startsWith("GA") -> 14100
            selectedState.startsWith("AZ") -> 11800
            else -> 9500
        }
        if (selectedCriteria.contains("Pre-Foreclosure / Auction")) base = (base * 0.22f).toInt()
        if (selectedCriteria.contains("Tax Delinquent")) base = (base * 0.35f).toInt()
        if (selectedCriteria.contains("Vacant Property")) base = (base * 0.40f).toInt()
        if (selectedCriteria.contains("Absentee Owner (Out of State)")) base = (base * 0.65f).toInt()
        if (selectedPropertyType.contains("Multi-Family")) base = (base * 0.30f).toInt()
        base.coerceAtLeast(850)
    }

    val priceCentsPerRecord = 4 // $0.04
    val totalPriceDollars = (requestedRecordCount * priceCentsPerRecord) / 100f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(
                                text = "Order Niche Lists",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Custom motivated seller lists with 98% phone match accuracy",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Select demographic motivations, target geographic locations, and property criteria. Our data engineering team pulls directly from Tier-1 county deed registries & credit bureaus.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Live Count & Price Preview Card (Sticky highlight)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE LIST COUNT PREVIEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,d", estimatedAvailableCount)} Verified Records",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$0.04 / Lead",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Selected Volume", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text("${String.format(Locale.US, "%,d", requestedRecordCount)} Records", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Turnaround ETA", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text("~2 Business Hours", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimated Total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text("$${String.format(Locale.US, "%.2f", totalPriceDollars)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 1. Demographic Niche Criteria Section
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "1. DEMOGRAPHIC MOTIVATION FILTERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Select all distressed or equity criteria that apply:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    availableCriteria.forEach { (criterion, desc) ->
                        val isChecked = selectedCriteria.contains(criterion)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCriteria = if (isChecked) {
                                        selectedCriteria - criterion
                                    } else {
                                        selectedCriteria + criterion
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedCriteria = if (checked) selectedCriteria + criterion else selectedCriteria - criterion
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SleekPrimary)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = criterion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Geographic Region Criteria Section
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "2. GEOGRAPHIC REGION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Text("Target State", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("TX - Texas", "GA - Georgia", "FL - Florida", "AZ - Arizona", "NC - North Carolina", "TN - Tennessee", "OH - Ohio")) { state ->
                            val isSel = selectedState == state
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedState = state },
                                label = { Text(state, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("Target Counties / Metropolitan Market", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetCounties,
                        onValueChange = { targetCounties = it },
                        placeholder = { Text("e.g. Dallas County, Tarrant County, Collin County") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(14.dp))
                    Text("Target Zip Codes (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetZipCodes,
                        onValueChange = { targetZipCodes = it },
                        placeholder = { Text("e.g. 75227, 75223, 75228, 75217") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        // 3. Property Criteria & Quantity
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "3. PROPERTY TYPE & SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Text("Property Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        propertyTypes.forEach { pType ->
                            val isSel = selectedPropertyType == pType
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPropertyType = pType }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { selectedPropertyType = pType },
                                    colors = RadioButtonDefaults.colors(selectedColor = SleekPrimary)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(pType, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minValuation,
                            onValueChange = { minValuation = it },
                            label = { Text("Min Estimated ARV", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = maxValuation,
                            onValueChange = { maxValuation = it },
                            label = { Text("Max Estimated ARV", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("Select Order Quantity (Record Count)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(1000, 2500, 5000, 10000, 20000)) { qty ->
                            val isSel = requestedRecordCount == qty
                            FilterChip(
                                selected = isSel,
                                onClick = { requestedRecordCount = qty },
                                label = { Text("${String.format(Locale.US, "%,d", qty)} records", fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        label = { Text("Additional Instructions or Filter Exclusions (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Exclude corporate buyers, require owner age > 45") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    isSubmitting = true
                    val formattedDemographics = selectedCriteria.joinToString(", ")
                    val fullMarket = "$targetCounties ($selectedState)"
                    val details = "Zips: $targetZipCodes | ARV: $minValuation - $maxValuation | $specialInstructions"
                    onSubmit(formattedDemographics, selectedPropertyType, fullMarket, details, requestedRecordCount)
                    showSuccessDialog = true
                    isSubmitting = false
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_niche_list_order_btn")
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Place Order • $${String.format(Locale.US, "%.2f", totalPriceDollars)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(36.dp)) },
            title = { Text("Order Placed Successfully!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your niche list request for ${String.format(Locale.US, "%,d", requestedRecordCount)} records in $targetCounties has been dispatched to our data engineering team. You will receive an instant notification once the verified CSV file is ready for download."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("View Order History")
                }
            }
        )
    }
}

@Composable
fun NicheListOrderHistoryView(orders: List<ListOrderEntity>) {
    val context = LocalContext.current

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("No Niche List Orders Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text("Switch to 'Order Niche Lists' to generate custom motivated seller records.", textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    text = order.typeOfLeads,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${order.propertyType} · ${order.targetMarket}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Status Chip
                            val isFulfilled = order.status.lowercase() == "fulfilled"
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFulfilled) SleekSuccessGreen.copy(alpha = 0.15f) else SleekSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isFulfilled) "Fulfilled" else "In Processing",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFulfilled) SleekSuccessGreen else SleekSecondary,
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
                                Text("Records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%,d", order.recordCount)} Records", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${String.format(Locale.US, "%.2f", order.priceCents / 100f)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Ordered On", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(order.createdAt)), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        if (order.otherDetails.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = order.otherDetails,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (order.status.lowercase() == "fulfilled") {
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.fileUrl ?: "https://example.com/data.csv"))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Download Cleaned CSV (${order.fileName ?: "list.csv"})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
