# AnixLifeTracker

**AnixLifeTracker** is an offline-first, personal life-management assistant designed to simplify finance tracking, investment monitoring, upcoming expense management, reminders, and cross-device synchronization.

Built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, it delivers a native user experience across Android, Desktop (JVM), Web (Kotlin/Wasm), and back-end cloud synchronization services.

---

## 🌟 Application Features

* **Automated Financial Data Ingestion (Android)**
  * Background parsing of incoming banking SMS messages to automatically log debit/credit transactions.
  * Interception and extraction of transaction notifications from supported banking and UPI payment applications.
  * Deterministic rule-based filtering to ignore OTPs, promotional messages, and failed transactions while deduplicating recurring records.
  * Historical SMS scanning with explicit user consent and review queue management.

* **Money & Expense Management**
  * Support for multiple accounts, credit cards, income streams, and expense tracking.
  * Category-based budget allocation, recurring expense tracking, and monthly cash flow insights.
  * Transaction history search, filtering, and manual entry capabilities.

* **Investment Portfolio Tracking**
  * Monitoring of stocks, mutual funds, SIPs, fixed deposits, and portfolio valuation.
  * Real-time metrics calculation including absolute returns, portfolio allocation, and scenario-based projections.

* **Universal Reminder Engine**
  * Unified reminder infrastructure supporting payment alerts, SIP schedules, travel dates, personal events, and custom notifications.

* **Cross-Device Cloud Sync & Authentication**
  * Secure user registration, authentication, and session management.
  * Offline-first local data persistence with automatic sync push/pull and conflict resolution when connected to the cloud server.

* **Adaptive Multiplatform UI**
  * Responsive user interface automatically adapting layout elements (Navigation Bar, Navigation Rail, or Navigation Sidebar) based on device form factor (mobile, tablet, foldable, or desktop/web).
  * Seamless Light and Dark theme toggling.

---

## 🏗️ Architecture & Frameworks

### Architectural Patterns
* **Clean Architecture**: Clear separation of concerns between UI/Presentation, Domain Business Logic, and Data Persistence layers.
* **MVVM (Model-View-ViewModel)**: State management utilizing reactive view models and observable state containers.
* **Unidirectional Data Flow (UDF)**: Deterministic user interactions flowing down through actions and state flowing back up to recompose the UI.
* **Offline-First Strategy**: Local database read/write priority ensuring complete app functionality without an active network connection.
* **Modular Multiplatform Design**: Shared cross-platform presentation, data, and logic modules with platform-specific hardware adapters.

### Frameworks & Libraries
* **UI Toolkit**: Compose Multiplatform (declarative UI shared across Android, Desktop, and Web).
* **Cross-Platform Engine**: Kotlin Multiplatform (KMP).
* **Local Data Storage**: Room KMP with bundled SQLite drivers.
* **Networking & Cloud**: Ktor Client (for multiplatform app targets) and Ktor Server (for backend sync services).
* **Asynchronous Execution**: Kotlin Coroutines and StateFlow streams.
* **Utilities**: Kotlinx Serialization and Kotlinx DateTime.

---

## 📱 User Control Flow & How to Use the App

```
 [ Launch App ]
       │
       ▼
 ┌─────────────────────────────────────────┐
 │ 1. Onboarding & Permissions Setup        │
 │    - Grant SMS/Notification Access     │
 │    - Configure Account Preferences      │
 └────────────────────┬────────────────────┘
                      │
                      ▼
 ┌─────────────────────────────────────────┐
 │ 2. Main Dashboard                      │
 │    - View Financial Health Summary     │
 │    - Check Recent Activity & Reminders  │
 └────────────────────┬────────────────────┘
                      │
         ┌────────────┴────────────┐
         │ Navigation Menu Choice   │
         └────────────┬────────────┘
                      │
    ┌─────────────────┼─────────────────┬─────────────────┐
    ▼                 ▼                 ▼                 ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Finances    │ │ Investments  │ │  Reminders   │ │  Settings &  │
│  Management  │ │  Portfolio   │ │   & Events   │ │  Cloud Sync  │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

1. **Onboarding & Configuration**:
   * On initial launch, users are presented with an overview dashboard.
   * Android users can navigate to SMS or Notification settings to enable automatic transaction ingestion with optional historical scanning.
2. **Dashboard Overview**:
   * The primary entry point displays account balances, monthly expenditure summaries, quick action buttons, and upcoming alerts.
3. **Financial Tracking & Categorization**:
   * Access the **Finances** section to view automated logs, add manual cash transactions, manage categories, or review pending items in the ingestion queue.
4. **Investment Monitoring**:
   * Navigate to **Investments** to track current asset distribution, monitor SIP schedules, and review performance metrics.
5. **Reminders & Planning**:
   * Utilize **Reminders**, **Travel**, and **Events** tabs to set upcoming payment dates, travel itineraries, or personal schedule alerts.
6. **Settings & Synchronization**:
   * Switch between Light/Dark/System themes.
   * Sign in to an account to push local changes to the cloud server or pull data across multiple personal devices.

---

## 🛠️ Project Structure

- `/app/androidApp` — Native Android application entry point.
- `/app/desktopApp` — Desktop (JVM) desktop application entry point.
- `/app/webApp` — Web application compiled to WebAssembly (Kotlin/Wasm).
- `/app/shared` — Shared Compose UI, business logic, domain models, and platform adapters.
- `/core` — Core utility helpers shared across apps and server.
- `/server` — Ktor backend server for authentication and sync services.

---

## 🚀 Running the Project

The root directory contains self-bootstrapping `kotlin` scripts.

- **Android App**: `./kotlin run -m androidApp`
- **Desktop App**: `./kotlin run -m desktopApp`
- **Web App**: `./kotlin run -m webApp`
- **Backend Server**: `./kotlin run -m server`
- **Build All**: `./kotlin build`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html), [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/), and [Ktor](https://ktor.io/).
