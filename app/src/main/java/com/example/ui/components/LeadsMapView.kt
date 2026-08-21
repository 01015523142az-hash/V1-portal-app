package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeadEntity
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun LeadsMapView(
    leads: List<LeadEntity>,
    selectedLeadId: String?,
    onLeadSelected: (String?) -> Unit,
    onOpenLeadDetail: (String) -> Unit,
    onOpenVoiceNote: (String) -> Unit,
    onLogCallMade: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var selectedStatusFilter by remember { mutableStateOf("all") }

    val filteredLeads = remember(leads, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "all" -> leads
            "qualified" -> leads.filter { it.status.lowercase() == "qualified" }
            "new" -> leads.filter { it.status.lowercase() == "new" }
            "accepted" -> leads.filter { it.status.lowercase() == "accepted" }
            else -> leads
        }
    }

    // Default center around US or first lead
    val initialCenter = remember(filteredLeads) {
        if (filteredLeads.isNotEmpty()) {
            val coords = filteredLeads.first().getResolvedCoordinates()
            LatLng(coords.first, coords.second)
        } else {
            LatLng(39.8283, -98.5795) // Center of US
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, 4.2f)
    }

    val selectedLead = remember(filteredLeads, selectedLeadId) {
        filteredLeads.find { it.id == selectedLeadId }
    }

    // Animate camera to selected lead if set
    LaunchedEffect(selectedLeadId) {
        selectedLead?.let { lead ->
            val coords = lead.getResolvedCoordinates()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(coords.first, coords.second), 14f),
                1000
            )
        }
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    val mapProperties = remember(mapType) {
        MapProperties(
            mapType = mapType,
            isMyLocationEnabled = false
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("leads_map_container")
    ) {
        // --- Interactive Google Map ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapClick = {
                onLeadSelected(null)
            }
        ) {
            filteredLeads.forEach { lead ->
                val coords = lead.getResolvedCoordinates()
                val position = LatLng(coords.first, coords.second)
                val isSelected = lead.id == selectedLeadId

                val markerHue = when {
                    lead.status.lowercase() == "qualified" -> BitmapDescriptorFactory.HUE_GREEN
                    lead.status.lowercase() == "accepted" -> BitmapDescriptorFactory.HUE_AZURE
                    lead.disputeStatus != null -> BitmapDescriptorFactory.HUE_ORANGE
                    else -> BitmapDescriptorFactory.HUE_RED
                }

                Marker(
                    state = rememberMarkerState(position = position),
                    title = "${lead.sellerName} · ${lead.askingPrice}",
                    snippet = "${lead.propertyAddress}, ${lead.propertyCity} (${lead.status.uppercase()})",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        onLeadSelected(lead.id)
                        true
                    }
                )
            }
        }

        // --- Top Map Controls Bar ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Filter Row & Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips in a card surface
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapFilterPill(
                            label = "All (${leads.size})",
                            selected = selectedStatusFilter == "all",
                            onClick = { selectedStatusFilter = "all" },
                            testTag = "map_filter_all"
                        )
                        MapFilterPill(
                            label = "New",
                            selected = selectedStatusFilter == "new",
                            onClick = { selectedStatusFilter = "new" },
                            badgeColor = CoralPrimary,
                            testTag = "map_filter_new"
                        )
                        MapFilterPill(
                            label = "Qualified",
                            selected = selectedStatusFilter == "qualified",
                            onClick = { selectedStatusFilter = "qualified" },
                            badgeColor = SleekSuccessGreen,
                            testTag = "map_filter_qualified"
                        )
                        MapFilterPill(
                            label = "Accepted",
                            selected = selectedStatusFilter == "accepted",
                            onClick = { selectedStatusFilter = "accepted" },
                            badgeColor = CobaltSecondary,
                            testTag = "map_filter_accepted"
                        )
                    }
                }

                // Lead Count Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f),
                    shadowElevation = 4.dp,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${filteredLeads.size} Pins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // --- Floating Side Tools (Recenter & Map Type) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fit All Leads / Recenter Button
            FloatingActionButton(
                onClick = {
                    if (filteredLeads.isNotEmpty()) {
                        coroutineScope.launch {
                            val builder = LatLngBounds.builder()
                            filteredLeads.forEach { lead ->
                                val coords = lead.getResolvedCoordinates()
                                builder.include(LatLng(coords.first, coords.second))
                            }
                            val bounds = builder.build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 120),
                                800
                            )
                        }
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("map_recenter_button"),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Fit all leads", modifier = Modifier.size(20.dp))
            }

            // Map Type Toggle Button
            FloatingActionButton(
                onClick = {
                    mapType = when (mapType) {
                        MapType.NORMAL -> MapType.HYBRID
                        MapType.HYBRID -> MapType.TERRAIN
                        else -> MapType.NORMAL
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("map_type_toggle_btn"),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Layers, contentDescription = "Toggle Map Type", modifier = Modifier.size(20.dp))
            }
        }

        // --- Bottom Slide-Up Lead Detail Card ---
        AnimatedVisibility(
            visible = selectedLead != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            selectedLead?.let { lead ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lead_marker_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Row: Seller Name, Status Pill, Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = lead.sellerName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // Status Badge
                                    LeadStatusBadge(status = lead.status)
                                }

                                Text(
                                    text = lead.campaignName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onLeadSelected(null) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close Preview",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Property Address & Pricing Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = lead.propertyAddress,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${lead.propertyCity}, ${lead.propertyState} ${lead.propertyZip}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Price & Value Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = lead.askingPrice,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SleekSuccessGreen
                                    )
                                    Text(
                                        text = "ARV: ${lead.marketValue}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Motivation snippet
                        if (lead.whySell.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Motivation: ${lead.whySell}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Phone Dial Button
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${lead.sellerPhone}")
                                    }
                                    context.startActivity(intent)
                                    onLogCallMade(lead.id)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("map_lead_call_button")
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp)
                            }

                            // Voice Note Button
                            OutlinedButton(
                                onClick = { onOpenVoiceNote(lead.id) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("map_lead_voice_note_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Note", tint = CoralPrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Memo", fontSize = 12.sp)
                            }

                            // Full Details Primary Button
                            Button(
                                onClick = { onOpenLeadDetail(lead.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("map_open_lead_detail_button")
                            ) {
                                Text("Open Lead", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badgeColor: Color? = null,
    testTag: String? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (badgeColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LeadStatusBadge(status: String) {
    val (bgColor, textColor, text) = when (status.lowercase()) {
        "qualified" -> Triple(SleekSuccessGreen.copy(alpha = 0.18f), SleekSuccessGreen, "QUALIFIED")
        "accepted" -> Triple(CobaltSecondary.copy(alpha = 0.18f), CobaltSecondary, "ACCEPTED")
        else -> Triple(CoralPrimary.copy(alpha = 0.18f), CoralPrimary, "NEW")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            fontFamily = FontFamily.Monospace
        )
    }
}
