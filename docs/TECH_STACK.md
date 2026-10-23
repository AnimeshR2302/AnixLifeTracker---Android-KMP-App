# AnixLifeTracker - Technology Stack & Dependencies

## 1. Stack Overview

AnixLifeTracker leverages a modern, 100% Kotlin Multiplatform technology stack designed for target compatibility across Android, Desktop (JVM), and Web (WasmJS).

---

## 2. Dependency & Version Matrix

Dependencies are centrally managed via `libs.versions.toml` and Amper module declarations.

| Category | Library | Version | Multiplatform Targets | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Language** | Kotlin Toolchain / KMP | `2.1.0`+ | Android, JVM, WasmJS | Core language infrastructure |
| **UI Framework** | Compose Multiplatform | `1.7.x` | Android, JVM, WasmJS | Declarative cross-platform UI rendering |
| **Design System** | Material 3 Multiplatform | `1.7.x` | Android, JVM, WasmJS | Standardized, accessible component set |
| **Concurrency** | `kotlinx.coroutines` | `1.10.x` | Android, JVM, WasmJS | Asynchronous state & reactive flows |
| **Date/Time** | `kotlinx.datetime` | `0.6.x` | Android, JVM, WasmJS | ISO date, timestamp, and timezone handling |
| **Serialization** | `kotlinx.serialization` | `1.8.x` | Android, JVM, WasmJS | Type-safe JSON parsing for DB & network |
| **Database** | Room KMP / SQLite | `2.7.x` | Android, JVM, WasmJS | Type-safe local persistence with Reactive Flow DAOs |
| **Dependency Injection** | Koin | `4.0.x` | Android, JVM, WasmJS | Lightweight, multiplatform-native DI framework |
| **Networking** | Ktor Client | `3.0.x` | Android, JVM, WasmJS | Multiplatform HTTP client for sync & market data |
| **Navigation** | Navigation Compose / KMP | `2.8.x` | Android, JVM, WasmJS | Type-safe backstack & deep link handling |
| **Logging** | Logback / Napier | `1.6.x` | Android, JVM, WasmJS | Platform-adapted structured logging |

---

## 3. Technology Rationale

### JetBrains Amper & Kotlin Toolchain
- **Why**: Replaces complex, verbose multiplatform Gradle scripts with clean declarative YAML module definitions (`module.yaml`).
- **Benefit**: Auto-configures Kotlin Multiplatform source sets, Compose compiler plugins, and native target linking without configuration churn.

### Room 3.x for Multiplatform
- **Why**: Official Android Room persistence framework now supports Kotlin Multiplatform (JVM, Android, Wasm, iOS) backed by SQLite.
- **Benefit**: Retains compile-time query verification, reactive `Flow` DAOs, migration APIs, and familiarity for Android developers.

### Koin Dependency Injection
- **Why**: Fully compatible with KMP targets without requiring code generation annotation processors (KSP/APT) across non-JVM targets like WasmJS.
- **Benefit**: Fast startup, simple DSL declarations (`module { single { ... } }`), and seamless Compose integration (`koinViewModel()`).

### Ktor Client 3.x
- **Why**: Official Kotlin multiplatform networking engine utilizing platform-native engines (Android OkHttp, JVM Java Engine, Wasm Fetch API).
- **Benefit**: Consistent request/response pipeline, automatic JSON deserialization via `kotlinx.serialization`, and built-in auth plugins.

### Navigation Compose Multiplatform
- **Why**: Standardized navigation API supporting type-safe routes, backstack restoration, and multi-pane layouts across phone, desktop window, and web browser targets.

---

## 4. Platform Compatibility Matrix

| Library / Feature | Android | Desktop (JVM) | Web (WasmJS) | iOS (Future) |
| :--- | :---: | :---: | :---: | :---: |
| Compose Multiplatform UI | ✅ | ✅ | ✅ | ✅ |
| Room KMP Persistent Database | ✅ | ✅ | ✅ | ✅ |
| Ktor HTTP Engine | ✅ | ✅ | ✅ | ✅ |
| Koin DI Engine | ✅ | ✅ | ✅ | ✅ |
| SMS Reading Service | ✅ Native | ❌ N/A | ❌ N/A | ❌ N/A |
| Notification Listener | ✅ Native | ⚠️ System Tray | ⚠️ Web Push | ⚠️ APNS |
| Local File Parsing (PDF/CSV) | ✅ | ✅ | ✅ (Browser File API) | ✅ |
