# TASK: Design AnixLifeTracker's Complete KMP Architecture

You are a senior Kotlin Multi-platform architect and Android engineer working inside Android Studio.

Your responsibility is to design a maintainable, scalable, production-oriented architecture for **AnixLifeTracker**.

This task is **Plan 1 of 17**. Subsequent tasks will implement features according to the architecture you establish.

## 1. Application Overview

Application name: AnixLifeTracker

Application purpose: A personal life-management assistant for automatically tracking finances, monitoring investments, identifying upcoming expenses, and managing important reminders.

Target platforms:

- Android
- Desktop (JVM)
- Web (Kotlin/Wasm)
- iOS (future)

Initial implementation focuses on finances and investments.

Future modules include:

- General reminders
- Travel reminders
- Event reminders
- Additional life-management features

The application must be modular enough to support future functionality without significant architectural changes.

## 2. Complete Feature Requirements

### 2.1 Financial Data Collection

Design support for:

1. Android banking SMS reading.
2. Android banking/UPI notification reading.
3. Email-based transaction extraction.
4. Importing PDF and CSV bank statements.
5. Future financial account API integrations.
6. Manual transaction entry.

The architecture must distinguish transaction collection from transaction processing.

Incoming financial information should follow:

Source → Extraction → Normalization → Deduplication → Categorization → Persistence → Analysis → UI

Different sources may report the same transaction.

Therefore, design a transaction ingestion system that supports multiple sources without duplicating financial records.

Do not implement actual SMS, notification, or email reading in this plan.

### 2.2 Money Management

Support future implementation of:

- Multiple bank accounts.
- Credit cards.
- Income tracking.
- Expense tracking.
- Account-to-account transfers.
- Refunds.
- Spending categories.
- Budgets.
- Recurring transactions.
- Upcoming expenses.
- Subscription tracking.
- Monthly expense analysis.
- Cash-flow predictions.
- Financial goals.

### 2.3 Investment Management

Support future implementation of:

- Mutual funds.
- SIPs.
- Stocks.
- ETFs.
- Fixed deposits.
- Recurring deposits.
- Gold.
- NPS.
- Portfolio performance.
- XIRR and absolute returns.
- Portfolio allocation.
- Wealth projections.
- Financial market news.
- Investment risk analysis.
- Relevant market opportunities.

Separate portfolio accounting, market-data retrieval, investment analytics, and AI-generated insights.

Market predictions must be represented as probabilistic or scenario-based analysis, never guaranteed outcomes.

### 2.4 Reminder Management

Design a common reminder infrastructure supporting:

- Payment reminders.
- SIP reminders.
- Important personal reminders.
- Travel reminders.
- Event reminders.
- Recurring reminders.
- AI-generated alerts.

The same reminder engine should support every feature without coupling reminders exclusively to finance.

## 3. Architecture Requirements

Use the following architectural principles:

- Clean Architecture.
- MVVM for presentation.
- Unidirectional data flow.
- Repository Pattern.
- Dependency Injection.
- SOLID principles.
- Interface-based abstractions.
- Offline-first data access.
- Kotlin Coroutines and Flow.
- Dependency inversion between business logic and external services.

Structure application logic into:

**Presentation**

- Screens and reusable components.
- ViewModels.
- UI states.
- User actions.
- Navigation.

**Domain**

- Business models.
- Repository interfaces.
- Use cases.
- Financial validation rules.
- Business calculations.

**Data**

- Repository implementations.
- Database integration.
- Local and remote data sources.
- Data mappers.
- Synchronization abstractions.

**Platform**

- Android SMS/notification integrations.
- Desktop-specific functionality.
- Browser-specific functionality.
- Platform storage and permissions.

Platform-specific dependencies must not leak into common business logic.

## 4. Required Module Structure

Design the Gradle project around the following logical modules:

```text
AnixLifeTracker/
│
├── androidApp/
├── desktopApp/
├── webApp/
│
├── shared/
│   ├── app/
│   │
│   ├── core/
│   │   ├── common/
│   │   ├── model/
│   │   ├── domain/
│   │   ├── data/
│   │   ├── database/
│   │   ├── designsystem/
│   │   ├── navigation/
│   │   └── platform/
│   │
│   └── feature/
│       ├── dashboard/
│       ├── finance/
│       ├── investments/
│       ├── reminders/
│       ├── travel/
│       └── events/
│
├── docs/
│
├── gradle/
│
└── settings.gradle.kts
```

Treat this as the architectural target.

Determine which directories need independent Gradle modules and which may remain packages to avoid unnecessary complexity.

Use the following dependency direction:

Presentation → Domain → Repository Interfaces

Repository Implementations → Domain + Database/External Data Sources

Application Composition Root → Feature Modules + Repository Implementations

The domain layer must not depend on presentation, database, or Android APIs.

Ensure there are no circular module dependencies.

## 5. Kotlin Multiplatform Strategy

Design source sets for:

- `commonMain`
- `androidMain`
- `jvmMain`
- `wasmJsMain`
- Corresponding test source sets

Shared code should include:

- Business models.
- Financial calculations.
- Validation.
- Repository contracts.
- Use cases.
- UI state management.
- Compose Multiplatform UI.
- Database definitions supported by all targets.

Platform-specific implementations should be isolated.

Plan future iOS integration without creating or building an iOS target now.

Avoid JVM-only dependencies in shared business logic.

## 6. Technology Stack

Plan around:

- Kotlin Multiplatform.
- Compose Multiplatform.
- Material 3.
- Kotlin Coroutines.
- Kotlin Flow.
- Kotlin Serialization.
- Room 3.x.
- SQLite.
- Ktor Client for future APIs.
- Koin for dependency injection if it supports all selected targets.

Prefer a maintained, compatible Compose Multiplatform navigation library with browser history and deep-link support.

Evaluate Navigation Compose and Navigation 3. Prefer the approach with reliable Android, Desktop, and Web behavior, rather than the newest API.

Select compatible stable versions and document the selected versions.

Use Gradle Kotlin DSL and a centralized version catalog.

## 7. Data Architecture

Design the following concepts:

- User profile.
- Financial account.
- Transaction.
- Transaction source.
- Transaction category.
- Budget.
- Recurring payment.
- Upcoming financial obligation.
- Investment account.
- Investment instrument.
- Investment holding.
- Investment transaction.
- SIP.
- Market price history.
- Reminder.
- Notification/alert.
- Sync metadata.

Define entity relationships, ownership, and repository boundaries.

Important financial design requirements:

1. Use exact monetary representations, not Float or Double.
2. Support currencies using ISO currency codes.
3. Support future synchronization using stable unique identifiers.
4. Preserve transaction history and provenance.
5. Distinguish transfers from expenses.
6. Distinguish investments from ordinary spending.
7. Avoid storing unnecessary sensitive information.
8. Design for schema migrations.
9. Track pending versus confirmed transactions.
10. Preserve timestamps and transaction dates correctly.

Plan 3 will implement these data structures.

## 8. Backend and Synchronization Architecture

Design a hybrid, privacy-oriented architecture.

The application should function offline, with synchronization added later.

Define architectural contracts for:

- Authentication.
- Sync requests and responses.
- Local change tracking.
- Conflict resolution.
- Device identity.
- Background synchronization.
- Secure credentials.
- Market-data retrieval.
- Daily server-side analysis.
- Push notifications.

Do not choose a cloud provider merely for convenience.

Provide a short comparison of practical backend options and recommend one for later implementation.

The backend will be implemented in Plan 4.

Do not build backend infrastructure now.

## 9. Privacy and Security Architecture

Financial data is sensitive.

Design for:

- Least-privilege Android permissions.
- Explicit user consent.
- Secure credential storage.
- Encryption strategy.
- Log redaction.
- Sensitive-data minimization.
- Secure synchronization.
- Export and deletion of user data.
- Platform-specific storage security.

Do not assume that Room or SQLite encrypts databases automatically.

Document security requirements that must be completed before production use.

## 10. Architecture Documentation

Create the following files:

```text
docs/
├── ARCHITECTURE.md
├── MODULE_STRUCTURE.md
├── TECH_STACK.md
├── DATA_FLOW.md
├── FEATURE_ROADMAP.md
├── SECURITY.md
├── AGENT_GUIDELINES.md
└── IMPLEMENTATION_STATUS.md
```

Documentation requirements:

**ARCHITECTURE.md**

- Overall system architecture.
- Component responsibilities.
- Clean Architecture boundaries.
- Local and remote data flows.
- Architecture diagrams using Mermaid.

**MODULE_STRUCTURE.md**

- Final module structure.
- Purpose of each module.
- Allowed module dependencies.
- Source-set organization.

**TECH_STACK.md**

- Selected dependencies.
- Verified versions.
- Platform compatibility.
- Reason for each significant technology choice.

**DATA_FLOW\.md**

- Transaction ingestion pipeline.
- Database flow.
- Portfolio tracking flow.
- Prediction flow.
- Notification flow.
- Synchronization flow.

**FEATURE_ROADMAP.md**

- All 17 implementation plans.
- Dependencies between plans.
- Current and future feature scope.

**SECURITY.md**

- Threat model.
- Permission requirements.
- Sensitive-data handling.
- Storage and synchronization security decisions.

**AGENT_GUIDELINES.md**

- Coding conventions.
- Naming conventions.
- Package organization.
- UI architecture rules.
- Testing standards.
- Rules for modifying existing code.
- Mandatory platform compatibility checks.

**IMPLEMENTATION_STATUS.md**

- Current implementation plan.
- Completed work.
- Pending work.
- Known limitations.
- Verification status.

Future AI-agent sessions must read these documents before making significant architectural changes.

## 11. Implementation Restrictions

For this plan:

- Inspect the existing project before designing the structure.
- Preserve existing user code and configurations.
- Do not implement feature screens, databases, or backend APIs.
- Do not create unnecessary classes or placeholder implementations.
- Do not use obsolete dependencies.
- Do not modify files outside the current project.
- Do not access personal files or secrets unrelated to the project.
- Do not introduce unnecessary paid services.
- Prefer open-source, free technologies where practical.

This is primarily an architecture and documentation task.

Actual project scaffolding will happen in Plan 2.

## 12. Expected Final Output

When complete, report:

1. Final architecture overview.
2. Complete module tree.
3. Module dependency graph.
4. Selected technology stack.
5. Platform-specific architecture.
6. Database architecture proposal.
7. Backend architecture proposal.
8. Created documentation files.
9. Architectural risks and unresolved decisions.

Update `IMPLEMENTATION_STATUS.md`.

**Completion condition:** The project has a documented architecture detailed enough for another AI agent to implement Plan 2 without redesigning fundamental decisions.