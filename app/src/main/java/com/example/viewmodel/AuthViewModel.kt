package com.example.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PortalRepository
import com.example.model.ClientAccount
import com.example.security.BiometricAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthScreenState {
    object Login : AuthScreenState()
    object ForgotPhone : AuthScreenState()
    data class ForgotOtp(val phone: String) : AuthScreenState()
    object SignUp : AuthScreenState()
}

data class AuthUiState(
    val screenState: AuthScreenState = AuthScreenState.Login,
    val isAuthenticated: Boolean = false,
    val currentAccount: ClientAccount? = null,
    val emailInput: String = "01015523142az@gmail.com",
    val passwordInput: String = "password123",
    val phoneInput: String = "+1 555-234-8921",
    val otpCodeInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val signupFullName: String = "",
    val signupEmail: String = "",
    val signupPhone: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val autoBiometricEnabled: Boolean = true
)

class AuthViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.accountFlow.collect { account ->
                _uiState.update { it.copy(currentAccount = account) }
            }
        }
        viewModelScope.launch {
            repository.autoBiometricLoginFlow.collect { autoBio ->
                _uiState.update { it.copy(autoBiometricEnabled = autoBio) }
            }
        }
    }

    fun checkBiometricAvailability(activity: FragmentActivity) {
        val available = BiometricAuthManager.isBiometricAvailable(activity)
        _uiState.update { it.copy(isBiometricAvailable = available) }
    }

    fun onEmailChanged(email: String) = _uiState.update { it.copy(emailInput = email, errorMessage = null) }
    fun onPasswordChanged(pw: String) = _uiState.update { it.copy(passwordInput = pw, errorMessage = null) }
    fun onPhoneChanged(phone: String) = _uiState.update { it.copy(phoneInput = phone, errorMessage = null) }
    fun onOtpChanged(otp: String) = _uiState.update { it.copy(otpCodeInput = otp, errorMessage = null) }
    fun onNewPasswordChanged(pw: String) = _uiState.update { it.copy(newPasswordInput = pw, errorMessage = null) }
    fun onConfirmPasswordChanged(pw: String) = _uiState.update { it.copy(confirmPasswordInput = pw, errorMessage = null) }

    fun onSignupFullNameChanged(name: String) = _uiState.update { it.copy(signupFullName = name, errorMessage = null) }
    fun onSignupEmailChanged(email: String) = _uiState.update { it.copy(signupEmail = email, errorMessage = null) }
    fun onSignupPhoneChanged(phone: String) = _uiState.update { it.copy(signupPhone = phone, errorMessage = null) }
    fun onSignupPasswordChanged(pw: String) = _uiState.update { it.copy(signupPassword = pw, errorMessage = null) }
    fun onSignupConfirmPasswordChanged(pw: String) = _uiState.update { it.copy(signupConfirmPassword = pw, errorMessage = null) }

    fun setScreenState(state: AuthScreenState) {
        _uiState.update {
            it.copy(
                screenState = state,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun signInWithPassword() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            kotlinx.coroutines.delay(600) // Smooth feedback
            repository.updateLastLogin()
            _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
        }
    }

    fun triggerBiometricAuth(activity: FragmentActivity) {
        if (!BiometricAuthManager.isBiometricAvailable(activity)) {
            _uiState.update { it.copy(errorMessage = "Biometric unlock is not configured on this device.") }
            return
        }

        BiometricAuthManager.authenticate(
            activity = activity,
            title = "Client Portal Quick Access",
            subtitle = "Authenticate with your biometric credential",
            onSuccess = {
                viewModelScope.launch {
                    repository.updateLastLogin()
                    _uiState.update { it.copy(isAuthenticated = true, errorMessage = null) }
                }
            },
            onError = { err ->
                _uiState.update { it.copy(errorMessage = err) }
            }
        )
    }

    fun sendForgotPhoneOtp() {
        val phone = _uiState.value.phoneInput.trim()
        if (phone.length < 7) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid phone number.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            kotlinx.coroutines.delay(800)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    screenState = AuthScreenState.ForgotOtp(phone),
                    successMessage = "6-digit verification code sent via SMS to $phone"
                )
            }
        }
    }

    fun verifyOtpAndResetPassword() {
        val code = _uiState.value.otpCodeInput.trim()
        val p1 = _uiState.value.newPasswordInput
        val p2 = _uiState.value.confirmPasswordInput

        if (code.length != 6) {
            _uiState.update { it.copy(errorMessage = "Please enter the 6-digit SMS code.") }
            return
        }
        if (p1.length < 8) {
            _uiState.update { it.copy(errorMessage = "New password must be at least 8 characters.") }
            return
        }
        if (p1 != p2) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            kotlinx.coroutines.delay(800)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    screenState = AuthScreenState.Login,
                    successMessage = "Password reset successfully! Please sign in with your new password.",
                    passwordInput = p1
                )
            }
        }
    }

    fun submitSignup() {
        val name = _uiState.value.signupFullName.trim()
        val email = _uiState.value.signupEmail.trim()
        val phone = _uiState.value.signupPhone.trim()
        val p1 = _uiState.value.signupPassword
        val p2 = _uiState.value.signupConfirmPassword

        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required.") }
            return
        }
        if (p1.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
            return
        }
        if (p1 != p2) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            kotlinx.coroutines.delay(1000)
            val newAccount = ClientAccount(
                id = "client_user_01",
                email = email,
                fullName = name,
                companyName = "$name Capital",
                phone = phone,
                accountType = "full_service",
                skiptraceCredits = 1000,
                creditBalanceCents = 3000
            )
            repository.saveAccount(newAccount)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    screenState = AuthScreenState.Login,
                    emailInput = email,
                    passwordInput = p1,
                    successMessage = "Account created successfully! Please sign in."
                )
            }
        }
    }

    fun signOut() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                passwordInput = "",
                errorMessage = null,
                successMessage = null,
                screenState = AuthScreenState.Login
            )
        }
    }

    fun toggleBiometricSetting(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBiometric(enabled)
        }
    }
}

class AuthViewModelFactory(private val repository: PortalRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository) as T
    }
}
