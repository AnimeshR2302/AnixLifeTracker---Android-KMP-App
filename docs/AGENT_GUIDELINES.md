# AnixLifeTracker - AI Agent Coding & Development Guidelines

This document provides mandatory conventions and rules for AI coding agents and human developers contributing code to **AnixLifeTracker**.

---

## 1. Core Architectural Laws

1. **Strict Money Handling**:
   - **NEVER** use `Float` or `Double` for financial values, amounts, or prices.
   - **ALWAYS** store monetary amounts as minor currency units (e.g., `amountInCents: Long` or `amountInPaise: Long`) or exact fixed-precision types.
   - Standardize ISO 4217 currency codes (e.g., `"INR"`, `"USD"`).

2. **Clean Architecture Layer Isolation**:
   - Domain layer (`core/domain` / `shared/core/domain`) MUST remain pure Kotlin with ZERO external framework dependencies (no Compose, no Android APIs, no Room, no Ktor).
   - Presentation depends on Domain contracts. Data implements Domain contracts.

3. **Multiplatform Target Verification**:
   - Before adding any new library or Java/JVM dependency, verify compatibility across ALL 3 active targets: **Android**, **Desktop JVM**, and **Web WasmJS**.
   - Do NOT introduce JVM-only dependencies (such as `java.io.File`, `java.text.SimpleDateFormat`, `java.awt.*`) into `commonMain`. Use `kotlinx.datetime`, `kotlinx.io`, and Compose Multiplatform equivalents.

4. **Unidirectional Data Flow (UDF) in Presentation**:
   - ViewModels expose state via immutable `StateFlow<UiState>`.
   - UI components trigger changes by dispatching explicit `UserAction` / `Intent` sealed classes/interfaces.
   - Use `UiState` sealed classes representing `Loading`, `Success`, `Error`, and `Empty` states.

---

## 2. Code & Naming Conventions

### File & Class Naming
- **Domain Entities**: Concise noun phrases (`Transaction`, `Account`, `InvestmentHolding`, `Reminder`).
- **Use Cases**: Verb + Object + UseCase (`GetMonthlyTransactionsUseCase`, `CalculatePortfolioXirrUseCase`).
- **Repositories**: Interface `FinanceRepository`, implementation `FinanceRepositoryImpl`.
- **ViewModels**: Screen name + ViewModel (`DashboardViewModel`, `AccountDetailsViewModel`).
- **Compose Screens**: Screen name + Screen (`DashboardScreen`, `TransactionsScreen`).

### Package Organization
```text
com.anix.android.anixlifetracker.feature.<feature_name>
├── ui/
│   ├── components/
│   ├── <Feature>Screen.kt
│   └── <Feature>ViewModel.kt
├── state/
│   ├── <Feature>UiState.kt
│   └── <Feature>UserAction.kt
```

---

## 3. State Management Template

All ViewModels in AnixLifeTracker must conform to the following UDF structure:

```kotlin
class FinanceViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FinanceUiState>(FinanceUiState.Loading)
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    fun onAction(action: FinanceUserAction) {
        when (action) {
            is FinanceUserAction.LoadTransactions -> loadTransactions()
            is FinanceUserAction.AddTransaction -> createTransaction(action.transaction)
            is FinanceUserAction.FilterByCategory -> filterByCategory(action.categoryId)
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = FinanceUiState.Loading
            getTransactionsUseCase()
                .catch { cause -> _uiState.value = FinanceUiState.Error(cause.message ?: "Unknown error") }
                .collect { list -> _uiState.value = FinanceUiState.Success(transactions = list) }
        }
    }
}
```

---

## 4. Testing Standards

- **Unit Tests (`commonTest`)**:
  - Test all Use Cases, Domain business logic, XIRR calculations, and Data Mappers in `commonTest`.
  - Use fake/mock repositories implemented in pure Kotlin.
- **ViewModel Tests**:
  - Verify UI state transitions using Coroutines test dispatchers (`runTest`, `UnconfinedTestDispatcher`).
- **Database Tests**:
  - Verify DAO queries and Room migrations against in-memory SQLite drivers.

---

## 5. Rules for Modifying Existing Code

1. **Preserve Architectural Invariants**: Never bypass a repository contract or Use Case to access a database DAO directly from a ViewModel or Composable.
2. **Backward Compatibility**: When updating data models, ensure Room database schema migrations are explicitly declared or supported.
3. **No Hidden Build Breaks**: Always verify that additions in `commonMain` do not break non-Android platform targets.
