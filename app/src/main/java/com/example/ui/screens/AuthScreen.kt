package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.security.BiometricStatus
import com.example.ui.theme.*
import com.example.viewmodel.AuthScreenState
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel
) {
    val state by authViewModel.uiState.collectAsState()
    val activity = LocalContext.current as? FragmentActivity
    var hasAutoPromptedBiometric by remember { mutableStateOf(false) }

    LaunchedEffect(activity) {
        activity?.let { authViewModel.checkBiometricAvailability(it) }
    }

    // Auto-prompt Fingerprint / Face ID if on LaunchChallenge screen and biometric is enabled/available
    LaunchedEffect(state.screenState, state.isBiometricAvailable, state.autoBiometricEnabled, activity) {
        if (state.screenState is AuthScreenState.LaunchChallenge &&
            state.isBiometricAvailable &&
            state.autoBiometricEnabled &&
            !hasAutoPromptedBiometric &&
            activity != null
        ) {
            hasAutoPromptedBiometric = true
            authViewModel.triggerBiometricAuth(activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // PropTech AI Brand Logo Symbol
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PropTechBlue, PropTechIndigo, PropTechCyan)
                        )
                    )
                    .border(1.5.dp, PropTechCyan.copy(alpha = 0.7f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_proptech_ai_symbol),
                    contentDescription = "PropTech AI Logo",
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Client Portal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PropTechCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PropTechCyan.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "AI CORE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PropTechCyan,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Real People. AI Empowered.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            // Main Auth Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    when (val screen = state.screenState) {
                        is AuthScreenState.LaunchChallenge -> {
                            BiometricChallengeLaunchView(
                                state = state,
                                onScanBiometric = {
                                    activity?.let { authViewModel.triggerBiometricAuth(it) }
                                },
                                onUsePassword = {
                                    authViewModel.setScreenState(AuthScreenState.Login)
                                },
                                onBypassDemo = {
                                    authViewModel.bypassBiometricForDemo()
                                }
                            )
                        }

                        is AuthScreenState.Login -> {
                            LoginView(
                                state = state,
                                onEmailChange = authViewModel::onEmailChanged,
                                onPasswordChange = authViewModel::onPasswordChanged,
                                onSignIn = authViewModel::signInWithPassword,
                                onBiometricClick = {
                                    authViewModel.setScreenState(AuthScreenState.LaunchChallenge)
                                    activity?.let { authViewModel.triggerBiometricAuth(it) }
                                },
                                onForgotPassword = { authViewModel.setScreenState(AuthScreenState.ForgotPhone) },
                                onSignUp = { authViewModel.setScreenState(AuthScreenState.SignUp) }
                            )
                        }

                        is AuthScreenState.ForgotPhone -> {
                            ForgotPhoneView(
                                state = state,
                                onPhoneChange = authViewModel::onPhoneChanged,
                                onSendOtp = authViewModel::sendForgotPhoneOtp,
                                onBackToLogin = { authViewModel.setScreenState(AuthScreenState.Login) }
                            )
                        }

                        is AuthScreenState.ForgotOtp -> {
                            ForgotOtpView(
                                state = state,
                                phone = screen.phone,
                                onOtpChange = authViewModel::onOtpChanged,
                                onNewPasswordChange = authViewModel::onNewPasswordChanged,
                                onConfirmPasswordChange = authViewModel::onConfirmPasswordChanged,
                                onReset = authViewModel::verifyOtpAndResetPassword,
                                onBackToLogin = { authViewModel.setScreenState(AuthScreenState.Login) }
                            )
                        }

                        is AuthScreenState.SignUp -> {
                            SignUpView(
                                state = state,
                                onNameChange = authViewModel::onSignupFullNameChanged,
                                onEmailChange = authViewModel::onSignupEmailChanged,
                                onPhoneChange = authViewModel::onSignupPhoneChanged,
                                onPasswordChange = authViewModel::onSignupPasswordChanged,
                                onConfirmPasswordChange = authViewModel::onSignupConfirmPasswordChanged,
                                onSubmit = authViewModel::submitSignup,
                                onBackToLogin = { authViewModel.setScreenState(AuthScreenState.Login) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricChallengeLaunchView(
    state: AuthUiState,
    onScanBiometric: () -> Unit,
    onUsePassword: () -> Unit,
    onBypassDemo: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
            Text(
                text = "SECURITY CHALLENGE",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = CoralPrimary,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Biometric Verification",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Verify your fingerprint or face scan to access your real estate deals & skip traces.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(Modifier.height(18.dp))

        // Animated Scanner Ring
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CoralPrimary.copy(alpha = 0.25f),
                            CyanAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, Brush.linearGradient(listOf(CoralPrimary, CyanAccent, GoldAccent)), CircleShape)
                .clickable { onScanBiometric() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint sensor",
                    tint = CoralPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // User Account Chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        text = state.currentAccount?.fullName ?: "Enterprise User",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.currentAccount?.email ?: state.emailInput,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = SleekAlertRedDim,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SleekAlertRed, modifier = Modifier.size(16.dp))
                    Text(
                        text = state.errorMessage,
                        color = SleekAlertRed,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Primary Action: Scan Fingerprint / Face ID
        Button(
            onClick = onScanBiometric,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoralPrimary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("launch_scan_biometric_btn")
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scan Fingerprint / Face ID", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(10.dp))

        // Secondary Action: Enter Password
        OutlinedButton(
            onClick = onUsePassword,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("launch_enter_password_btn")
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sign In with Password / PIN", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        // Demo Unlock Fallback (for testing environments)
        TextButton(
            onClick = onBypassDemo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Quick Unlock (Demo / Emulator)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LoginView(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onBiometricClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Text(
        text = "Welcome to Client Portal",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Manage your leads, skip tracing and billing seamlessly.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )

    if (state.errorMessage != null) {
        Surface(
            color = SleekAlertRedDim,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(
                text = state.errorMessage,
                color = SleekAlertRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    if (state.successMessage != null) {
        Surface(
            color = SleekSuccessGreenDim,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(
                text = state.successMessage,
                color = SleekSuccessGreen,
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    OutlinedTextField(
        value = state.emailInput,
        onValueChange = onEmailChange,
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth().testTag("login_email_input")
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = state.passwordInput,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth().testTag("login_password_input")
    )

    Spacer(Modifier.height(18.dp))

    Button(
        onClick = onSignIn,
        enabled = !state.isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_submit_btn")
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        } else {
            Text("Sign in", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    Spacer(Modifier.height(12.dp))
    Button(
        onClick = onBiometricClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_biometric_btn")
    ) {
        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text("Biometric Security Scan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onForgotPassword) {
            Text("Forgot password?", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
        TextButton(onClick = onSignUp) {
            Text("New here? Sign up", fontSize = 12.sp, color = SleekSecondary)
        }
    }
}

@Composable
fun ForgotPhoneView(
    state: AuthUiState,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Text(
        text = "Reset your password",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Enter your mobile phone number on file. We will text you a 6-digit verification code.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )

    if (state.errorMessage != null) {
        Surface(
            color = SleekAlertRedDim,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(text = state.errorMessage, color = SleekAlertRed, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        }
    }

    OutlinedTextField(
        value = state.phoneInput,
        onValueChange = onPhoneChange,
        label = { Text("Mobile Phone Number") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(18.dp))

    Button(
        onClick = onSendOtp,
        enabled = !state.isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text("Send Verification Code", fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(12.dp))

    TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
        Text("← Back to Sign In", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ForgotOtpView(
    state: AuthUiState,
    phone: String,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onReset: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Text(
        text = "Enter Verification Code",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "We sent a 6-digit code to $phone. Enter it below with your new password.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )

    if (state.errorMessage != null) {
        Surface(
            color = SleekAlertRedDim,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(text = state.errorMessage, color = SleekAlertRed, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        }
    }

    OutlinedTextField(
        value = state.otpCodeInput,
        onValueChange = { if (it.length <= 6) onOtpChange(it) },
        label = { Text("6-digit Code") },
        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = state.newPasswordInput,
        onValueChange = onNewPasswordChange,
        label = { Text("New Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = state.confirmPasswordInput,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirm New Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(18.dp))

    Button(
        onClick = onReset,
        enabled = !state.isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text("Save & Reset Password", fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(12.dp))

    TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
        Text("← Back to Sign In", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SignUpView(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Text(
        text = "Create Portal Account",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Create your account to manage leads, orders, and skip tracing.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )

    if (state.errorMessage != null) {
        Surface(
            color = SleekAlertRedDim,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(text = state.errorMessage, color = SleekAlertRed, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        }
    }

    OutlinedTextField(
        value = state.signupFullName,
        onValueChange = onNameChange,
        label = { Text("Full Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(10.dp))

    OutlinedTextField(
        value = state.signupEmail,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(10.dp))

    OutlinedTextField(
        value = state.signupPhone,
        onValueChange = onPhoneChange,
        label = { Text("Mobile Phone") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(10.dp))

    OutlinedTextField(
        value = state.signupPassword,
        onValueChange = onPasswordChange,
        label = { Text("Password (min 8 chars)") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(10.dp))

    OutlinedTextField(
        value = state.signupConfirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirm Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(18.dp))

    Button(
        onClick = onSubmit,
        enabled = !state.isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text("Create Account", fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(12.dp))

    TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
        Text("← Already have an account? Sign In", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
