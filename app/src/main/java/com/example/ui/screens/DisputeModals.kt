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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeModalSheet(
    leadId: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, explanation: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Not the Homeowner / Wrong Number") }
    var explanation by remember { mutableStateOf("") }

    val reasons = listOf(
        "Not the Homeowner / Wrong Number",
        "Property Already Listed on MLS",
        "Asking Price Exceeds Market ARV Rules",
        "Seller Stated No Intent to Sell",
        "Audio Recording Missing / Quality Issue"
    )

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
                    Text("File Lead Dispute", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                    Text("Dispute Window: Within 2 US Business Days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Select Dispute Reason:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            reasons.forEach { r ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReason = r }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (selectedReason == r),
                        onClick = { selectedReason = r },
                        colors = RadioButtonDefaults.colors(selectedColor = DangerRed)
                    )
                    Text(r, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Detailed Explanation for Quality Review") },
                placeholder = { Text("e.g., Spoke to seller and they stated they are not selling.", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (explanation.isNotBlank()) {
                        onSubmit(selectedReason, explanation)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("submit_dispute_btn")
            ) {
                Text("Submit Dispute to Quality Team", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackModalSheet(
    leadId: String,
    onDismiss: () -> Unit,
    onSubmit: (feedback: String) -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }

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
                Text("Leave Campaign Feedback", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Help us refine caller training or qualification details for future leads.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Your Feedback") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (feedbackText.isNotBlank()) {
                        onSubmit(feedbackText)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Send Feedback", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
