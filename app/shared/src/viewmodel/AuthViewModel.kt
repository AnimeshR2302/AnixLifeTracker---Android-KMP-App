package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.auth.AuthError
import com.anix.android.anixlifetracker.auth.AuthRepository
import com.anix.android.anixlifetracker.auth.AuthResult
import com.anix.android.anixlifetracker.auth.AuthState
import com.anix.android.anixlifetracker.auth.UserSession
import com.anix.android.anixlifetracker.sync.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthTab {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

data class AuthUiState(
    val activeTab: AuthTab = AuthTab.SIGN_IN,
    val emailInput: String = "",
    val passwordInput: String = "",
    val displayNameInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isDisclosureDialogVisible: Boolean = false,
    val authState: AuthState = AuthState.Unauthenticated
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            authRepository.observeAuthState().collect { state ->
                _uiState.value = _uiState.value.copy(
                    authState = state,
                    isLoading = state is AuthState.Loading
                )
            }
        }
    }

    fun setTab(tab: AuthTab) {
        _uiState.value = _uiState.value.copy(
            activeTab = tab,
            errorMessage = null,
            infoMessage = null
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(emailInput = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(passwordInput = password, errorMessage = null)
    }

    fun updateDisplayName(name: String) {
        _uiState.value = _uiState.value.copy(displayNameInput = name, errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            when (state.activeTab) {
                AuthTab.SIGN_IN -> {
                    val result = authRepository.signIn(state.emailInput, state.passwordInput)
                    handleResult(result)
                }
                AuthTab.SIGN_UP -> {
                    val result = authRepository.signUp(state.emailInput, state.passwordInput, state.displayNameInput)
                    handleResult(result)
                }
                AuthTab.FORGOT_PASSWORD -> {
                    val result = authRepository.sendPasswordResetEmail(state.emailInput)
                    if (result is AuthResult.Success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            infoMessage = result.message ?: "Password reset instructions sent."
                        )
                    } else if (result is AuthResult.Failure) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.error.userMessage
                        )
                    }
                }
            }
        }
    }

    private fun handleResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    passwordInput = "",
                    infoMessage = result.message ?: "Signed in successfully.",
                    isDisclosureDialogVisible = true
                )
            }
            is AuthResult.Failure -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.error.userMessage
                )
            }
        }
    }

    fun signOut() {
        scope.launch {
            authRepository.signOut()
            syncRepository.setCloudSyncEnabled(false)
            _uiState.value = _uiState.value.copy(
                infoMessage = "Signed out. Local financial data preserved."
            )
        }
    }

    fun showDisclosureDialog() {
        _uiState.value = _uiState.value.copy(isDisclosureDialogVisible = true)
    }

    fun dismissDisclosureDialog() {
        _uiState.value = _uiState.value.copy(isDisclosureDialogVisible = false)
    }

    fun confirmCloudSyncConsent() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isDisclosureDialogVisible = false)
            syncRepository.setCloudSyncEnabled(true)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }
}
