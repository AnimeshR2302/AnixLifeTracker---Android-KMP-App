# AnixLifeTracker - Module Structure & Source-Sets

## 1. Module Overview

AnixLifeTracker is configured as a multi-module Kotlin Multiplatform project using **JetBrains Amper** and **Kotlin Toolchain**.

```text
AnixLifeTracker/
│
├── app/
│   ├── androidApp/          # Android application entry point & Manifest
│   ├── desktopApp/          # Desktop JVM application entry point & Main window
│   ├── webApp/              # WebAssembly (WasmJS) entry point & HTML harness
│   └── shared/              # Shared Compose UI, ViewModels, and Application composition
│       ├── src/             # Common source set (AppShell, Theme, DesignSystem, ViewModels, Screens)
│       ├── src@android/     # Android platform entry point
│       ├── src@jvm/         # Desktop JVM platform entry point
│       ├── src@wasmJs/      # WasmJS platform entry point
│       └── test/            # Common multiplatform unit tests
│
├── shared/
│   ├── core/
│   │   ├── common/          # Cross-cutting utilities, dispatchers, result types
│   │   ├── model/           # Core domain models, monetary value objects
│   │   ├── domain/          # Shared use cases & repository interfaces
│   │   ├── data/            # Repository implementations, data mappers, sync engine
│   │   ├── database/        # Room/SQLite schema, entities, DAOs, migrations
│   │   ├── designsystem/    # Material 3 theme, components, tokens
│   │   ├── navigation/      # Navigation state, routes, screen destinations
│   │   └── platform/        # Platform abstraction interfaces (SMS, KeyStore, Alarms)
│   │
│   └── feature/
│       ├── dashboard/       # Overview, Net Worth, Recent Activity summary
│       ├── finance/         # Accounts, Income/Expense tracking, Budgets
│       ├── investments/     # Mutual Funds, Stocks, SIPs, Portfolio analytics
│       ├── reminders/       # Universal reminder list, scheduling, alerts
│       ├── travel/          # Travel itinerary, tickets, reminders
│       └── events/          # Life events, milestones, reminders
│
├── core/                    # Root shared core module across app and server
├── server/                  # Ktor backend server (JVM target)
├── docs/                    # Architecture and developer documentation
├── project.yaml             # Amper root project configuration
└── libs.versions.toml       # Centralized version catalog
```

---

## 2. Module Responsibilities & Target Mapping

### Application Modules (`app/`)
- **`app/androidApp`**: Android runner (`product: android/app`). Owns `MainActivity`, Android Manifest, runtime permission handlers, and Android application setup.
- **`app/desktopApp`**: Desktop runner (`product: jvm/app`). Owns `main.kt`, Swing window wrapper, and system tray integration.
- **`app/webApp`**: Web runner (`product: wasm-js/app`). Owns `main.kt`, Wasm canvas attachment, and web resource loading.
- **`app/shared`**: Shared UI library (`product: kmp/lib`). Assembles feature composables, top-level navigation graphs, root Scaffold, and global Koin dependency injection modules.

### Core Shared Modules (`shared/core/` and packages)
- **`core/common`**: Kotlin Result wrappers, Coroutine Dispatcher abstractions, Logger contracts, Currency formatting utilities.
- **`core/model`**: Domain entities (`Transaction`, `Account`, `InvestmentHolding`, `Reminder`, `Budget`).
- **`core/domain`**: Business rules, use case interdictors, financial calculation engines (XIRR, balance projections).
- **`core/data`**: Concrete implementations of repositories (`FinanceRepositoryImpl`, `InvestmentRepositoryImpl`, `ReminderRepositoryImpl`).
- **`core/database`**: Room KMP database instance, DAO interfaces, database entity converters.
- **`core/designsystem`**: Color schemes, typography, spacing tokens, custom financial widgets (charts, currency text fields, status badges).
- **`core/navigation`**: Type-safe navigation routes, navigation event bus, back-stack management.
- **`core/platform`**: `expect`/`actual` declarations for platform features (SMS reader, Secure KeyStore, Local Notifications).

### Feature Modules (`shared/feature/`)
- **`feature/dashboard`**: High-level financial overview, quick action shortcuts, combined analytics cards.
- **`feature/finance`**: Account list, transaction feed, manual transaction creation, budget progress indicators.
- **`feature/investments`**: Asset allocation breakdown, portfolio return charts, SIP tracker.
- **`feature/reminders`**: Multi-category reminder management UI, snooze/completion dialogs.
- **`feature/travel` & `feature/events`**: Specialist life tracking interfaces sharing core reminder infrastructure.

---

## 3. Allowed Dependency Rules

To preserve clean separation and prevent circular dependencies, dependencies are strictly directional:

```mermaid
graph TD
    AppAndroid[app/androidApp] --> AppShared[app/shared]
    AppDesktop[app/desktopApp] --> AppShared
    AppWeb[app/webApp] --> AppShared

    AppShared --> FeatureDashboard[feature/dashboard]
    AppShared --> FeatureFinance[feature/finance]
    AppShared --> FeatureInvestments[feature/investments]
    AppShared --> FeatureReminders[feature/reminders]

    FeatureDashboard --> CoreDomain[core/domain]
    FeatureDashboard --> CoreDesignSystem[core/designsystem]
    FeatureDashboard --> CoreNavigation[core/navigation]

    FeatureFinance --> CoreDomain
    FeatureFinance --> CoreDesignSystem

    CoreData[core/data] ..|> CoreDomain
    CoreData --> CoreDatabase[core/database]
    CoreData --> CorePlatform[core/platform]

    CoreDatabase --> CoreModel[core/model]
    CoreDomain --> CoreModel
    CoreModel --> CoreCommon[core/common]
```

### Invariants:
1. **No Feature-to-Feature Dependencies**: `feature/finance` cannot depend on `feature/investments`. Communication happens via `core/domain` shared use cases or state/navigation.
2. **Domain Has No Upward Dependencies**: `core/domain` relies solely on `core/model` and `core/common`. It must NEVER import `core/data`, `core/database`, `core/designsystem`, Compose, or Android APIs.
3. **Data Implements Domain Contracts**: `core/data` depends on `core/domain` to implement repository interfaces.

---

## 4. Source-Set Hierarchy & Organization

Each KMP module structures code across target-specific source sets:

```text
src/                         # Common Kotlin code (compiled for all targets)
├── commonMain/              # Business logic, models, shared Compose UI
└── commonTest/              # Cross-platform unit tests

src@android/                 # Android-specific code
├── androidMain/             # Android SMS/Notification listeners, Android KeyStore
└── androidTest/             # Android instrumentation / local tests

src@jvm/                     # Desktop JVM-specific code
├── jvmMain/                 # Swing integrations, Desktop Keyring, File parsers
└── jvmTest/                 # JVM JUnit 5 unit tests

src@wasmJs/                  # Kotlin/Wasm-specific code
├── wasmJsMain/              # Browser LocalStorage, Web Crypto API
└── wasmJsTest/              # Wasm browser environment tests
```

### Guidelines for Platform Code (`src@<platform>`)
- Keep platform-specific implementations minimal and focused on platform APIs.
- Always expose functionality to `commonMain` via interfaces or `expect`/`actual` declarations.
- Ensure `wasmJs` implementations provide fallback behavior when native platform hardware APIs (such as telephony/SMS) are unavailable.
