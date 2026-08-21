package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubscriptionSheet(
    subscriptionId: String,
    onDismiss: () -> Unit,
    onAddCaller: (tier: String, dialer: String, data: String) -> Unit,
    onRequestPause: () -> Unit,
    onRequestCancel: () -> Unit
) {
    var selectedAction by remember { mutableStateOf("add_caller") }
    var newCallerTier by remember { mutableStateOf("full_time") }
    var dialerChoice by remember { mutableStateOf("ours") }
    var dataChoice by remember { mutableStateOf("ours") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Manage Campaign Staffing", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Add callers or request pause/cancellation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action Selection Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "add_caller" to "Add Caller",
                    "pause" to "Pause Campaign",
                    "cancel" to "Cancel"
                ).forEach { (id, label) ->
                    val isSelected = selectedAction == id
                    OutlinedButton(
                        onClick = { selectedAction = id },
                        shape = RoundedCornerShape(8.dp),
                        colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedAction) {
                "add_caller" -> {
                    Text("Select Additional Caller Tier:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    listOf(
                        "full_time" to "Full-Time Caller ($900/month)",
                        "part_time" to "Part-Time Caller ($500/month)"
                    ).forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newCallerTier = key }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = newCallerTier == key, onClick = { newCallerTier = key })
                            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onAddCaller(newCallerTier, dialerChoice, dataChoice)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Confirm & Add Caller", fontWeight = FontWeight.Bold)
                    }
                }

                "pause" -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarningOrangeDim,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("14-Day Notice Required", fontWeight = FontWeight.Bold, color = WarningOrange, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Per contract terms, pause requests take effect 14 days from today to honor caller scheduling commitments.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onRequestPause()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Submit Pause Request", fontWeight = FontWeight.Bold)
                    }
                }

                "cancel" -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRedDim,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("14-Day Cancellation Notice", fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Cancellation will take effect 14 days after submission. Active calling will continue until the effective date.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onRequestCancel()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Submit Cancellation Request", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
