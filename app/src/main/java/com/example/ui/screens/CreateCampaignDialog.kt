package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var campaignName by remember { mutableStateOf("Miami Distressed Acquisitions") }
    var targetLocation by remember { mutableStateOf("Miami-Dade County, FL") }
    var callerTier by remember { mutableStateOf("full_time") }
    var dialerChoice by remember { mutableStateOf("ours") }
    var dataChoice by remember { mutableStateOf("ours") }
    var qualificationNotes by remember { mutableStateOf("25% discount off ARV. Exclude HOA condos.") }

    val callerCost = if (callerTier == "full_time") 900 else 500
    val dialerCost = if (dialerChoice == "ours") 200 else 0
    val dataCost = if (dataChoice == "ours") 400 else 0
    val totalMonthly = callerCost + dialerCost + dataCost

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Launch New Campaign", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Dedicated caller onboarding & dialer provisioning", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = campaignName,
                onValueChange = { campaignName = it },
                label = { Text("Campaign Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = targetLocation,
                onValueChange = { targetLocation = it },
                label = { Text("Target Market / Counties") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            Text("1. Caller Staffing Tier", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            listOf(
                "full_time" to "Full-Time Dedicated Caller (40 hrs/wk) — $900/mo",
                "part_time" to "Part-Time Dedicated Caller (20 hrs/wk) — $500/mo"
            ).forEach { (key, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { callerTier = key }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(selected = callerTier == key, onClick = { callerTier = key })
                    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("2. Multi-Line Dialer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            listOf(
                "ours" to "Use Proptech Managed Dialer ($200/mo)",
                "own" to "Provide My Own Dialer Account (CallTools/ReadyMode - $0)"
            ).forEach { (key, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dialerChoice = key }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(selected = dialerChoice == key, onClick = { dialerChoice = key })
                    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("3. Cold Calling Data & Records", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            listOf(
                "ours" to "Proptech Curated & Skip-Traced Data ($400/mo)",
                "own" to "I Will Upload My Own Skip-Traced Lists ($0)"
            ).forEach { (key, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dataChoice = key }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(selected = dataChoice == key, onClick = { dataChoice = key })
                    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = qualificationNotes,
                onValueChange = { qualificationNotes = it },
                label = { Text("Lead Qualification Guidelines") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Pricing Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Estimated Monthly Rate", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$$totalMonthly / month", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
                    }

                    Button(
                        onClick = {
                            if (campaignName.isNotBlank()) {
                                onSubmit(campaignName, callerTier, dialerChoice, dataChoice)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.testTag("launch_campaign_submit_btn")
                    ) {
                        Text("Launch Campaign", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
