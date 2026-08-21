package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onSaveCard: (brand: String, last4: String, expMonth: Int, expYear: Int, isDefault: Boolean) -> Unit
) {
    var rawCardNumber by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var rawExpiry by remember { mutableStateOf("") }
    var rawCvv by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var isDefaultCard by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Detect card brand dynamically
    val cardBrand = remember(rawCardNumber) {
        val clean = rawCardNumber.filter { it.isDigit() }
        when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") || clean.startsWith("54") || clean.startsWith("55") -> "Mastercard"
            clean.startsWith("34") || clean.startsWith("37") -> "Amex"
            clean.startsWith("6011") || clean.startsWith("65") -> "Discover"
            else -> "Card"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CoralPrimaryDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Add Payment Method",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Secure encrypted tokenization",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Card Preview Visual Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cardBrand.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(Icons.Default.Contactless, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                        }

                        Spacer(Modifier.height(14.dp))

                        // Card number preview
                        val displayNum = if (rawCardNumber.isBlank()) {
                            "•••• •••• •••• ••••"
                        } else {
                            rawCardNumber.take(16).chunked(4).joinToString(" ")
                        }
                        Text(
                            text = displayNum,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CARDHOLDER", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text(
                                    text = cardholderName.ifBlank { "YOUR NAME" }.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("EXPIRES", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                val expDisplay = if (rawExpiry.length >= 4) "${rawExpiry.take(2)}/${rawExpiry.drop(2).take(2)}" else if (rawExpiry.isNotEmpty()) rawExpiry else "MM/YY"
                                Text(
                                    text = expDisplay,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Cardholder Name Input
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text("Name on Card") },
                    placeholder = { Text("e.g. Alex Morgan") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("cardholder_name_input")
                )

                Spacer(Modifier.height(10.dp))

                // Card Number Input
                OutlinedTextField(
                    value = rawCardNumber,
                    onValueChange = {
                        val digits = it.filter { ch -> ch.isDigit() }.take(16)
                        rawCardNumber = digits
                    },
                    label = { Text("Card Number") },
                    placeholder = { Text("16-digit card number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Outlined.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    visualTransformation = CardNumberVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("card_number_input")
                )

                Spacer(Modifier.height(10.dp))

                // Expiry and CVV Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rawExpiry,
                        onValueChange = {
                            val digits = it.filter { ch -> ch.isDigit() }.take(4)
                            rawExpiry = digits
                        },
                        label = { Text("MM / YY") },
                        placeholder = { Text("12/28") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ExpiryVisualTransformation(),
                        modifier = Modifier.weight(1f).testTag("card_expiry_input")
                    )

                    OutlinedTextField(
                        value = rawCvv,
                        onValueChange = {
                            val digits = it.filter { ch -> ch.isDigit() }.take(4)
                            rawCvv = digits
                        },
                        label = { Text("CVV / CVC") },
                        placeholder = { Text("123") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f).testTag("card_cvv_input")
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Billing Zip Code
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = {
                        val clean = it.filter { ch -> ch.isDigit() }.take(5)
                        zipCode = clean
                    },
                    label = { Text("Billing ZIP / Postal Code") },
                    placeholder = { Text("75201") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("card_zip_input")
                )

                Spacer(Modifier.height(12.dp))

                // Set as Default Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Set as default card", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Used automatically for monthly cold caller subscriptions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isDefaultCard,
                        onCheckedChange = { isDefaultCard = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CoralPrimary
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Security Guarantee Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(14.dp))
                    Text(
                        text = "PCI-DSS Level 1 Certified • End-to-End 256-bit Tokenization",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (cardholderName.trim().length < 2) {
                                errorMessage = "Please enter the cardholder's name."
                                return@Button
                            }
                            if (rawCardNumber.length < 15) {
                                errorMessage = "Please enter a valid 15 or 16-digit card number."
                                return@Button
                            }
                            if (rawExpiry.length < 4) {
                                errorMessage = "Please enter expiry date as MM/YY."
                                return@Button
                            }
                            val month = rawExpiry.take(2).toIntOrNull() ?: 0
                            val year = 2000 + (rawExpiry.drop(2).take(2).toIntOrNull() ?: 0)
                            if (month !in 1..12) {
                                errorMessage = "Invalid expiration month (must be 01 to 12)."
                                return@Button
                            }
                            if (rawCvv.length < 3) {
                                errorMessage = "Please enter a valid 3 or 4-digit CVV."
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true
                            val last4 = rawCardNumber.takeLast(4)
                            onSaveCard(cardBrand, last4, month, year, isDefaultCard)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.weight(1f).testTag("save_card_submit_btn")
                    ) {
                        Text("Save Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if ((i == 3 || i == 7 || i == 11) && i != trimmed.lastIndex) {
                out += " "
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 && i != trimmed.lastIndex) {
                out += "/"
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
