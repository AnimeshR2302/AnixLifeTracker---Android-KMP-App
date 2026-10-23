# TASK: Implement AnixLifeTracker KMP Foundation and Base UI

You are implementing Plan 2 of the AnixLifeTracker project.

Plan 1 established the application's architecture.

Read the following project documents before making changes:

- docs/ARCHITECTURE.md
- docs/MODULE_STRUCTURE.md
- docs/TECH_STACK.md
- docs/AGENT_GUIDELINES.md
- docs/IMPLEMENTATION_STATUS.md

Preserve the architecture established in Plan 1.

Do not redesign the project unless an actual build or compatibility issue requires it. Document any necessary deviation.

## 1. Objective

Create a fully functional Kotlin Multiplatform application running on:

1. Android.
2. Desktop (JVM).
3. Web (WasmJS).

The project must use shared Compose Multiplatform UI and have complete navigation between its primary features.

The result must be a working application shell, not just generated empty modules.

Do not implement real financial processing or databases in this plan.

## 2. Project Configuration

Inspect the existing Gradle configuration before making changes.

Implement the structure approved in Plan 1.

Configure:

- Kotlin Multiplatform.
- Android application target.
- JVM Desktop target.
- Web WasmJS target.
- Compose Multiplatform.
- Material 3.
- Kotlin Coroutines.
- Kotlin Serialization.
- Dependency injection.
- Multiplatform navigation.
- Shared resources.
- Unit-testing dependencies.

Use:

- Gradle Kotlin DSL.
- `libs.versions.toml`.
- Compatible stable dependency versions.
- Appropriate Gradle and Kotlin compiler plugins.

Avoid unnecessary experimental APIs.

Use the existing Android SDK installation.

Do not modify unrelated files outside the project.

### Platform Requirements

**Android**
- Application ID: `com.anixlifetracker.app`
- Application name: AnixLifeTracker.
- Support modern Android versions, with the final minimum SDK chosen according to dependency compatibility.
- Configure application lifecycle and themes.
- Support edge-to-edge rendering and system back navigation.

**Desktop**
- Create a standalone JVM application entry point.
- Support Linux development.
- Structure packaging configuration so Windows and macOS distributions can be added later.
- Ensure desktop window resizing works correctly.

**Web**
- Configure a WasmJS browser application.
- Support browser resizing.
- Handle navigation and browser history.
- Provide development and production build tasks.
- Avoid JVM dependencies in web source sets.

**iOS**
- Do not create an iOS application yet.
- Keep shared code compatible with eventual iOS support.

## 3. Create the Shared Application Shell

Create a common application entry point.

Suggested responsibilities:

**App**
- Initialize theme.
- Initialize application-level dependencies.
- Set up navigation.
- Render the main application layout.

**AppViewModel**
- Manage application-level UI state where needed.
- Handle selected feature and global UI actions.
- Avoid storing state that belongs to individual screens.

**AppState**
- Track relevant application-shell state.
- Avoid using a global mutable singleton for all feature data.

**PlatformEntryPoint**
- Initialize platform-specific services.
- Provide dependencies to shared application code.

Use constructor injection wherever practical.

Avoid tightly coupling the UI to Android Activities or platform-specific APIs.

## 4. Navigation System

Implement navigation between the following sections:

### Dashboard
The application's main landing screen.

### Finances
Personal income and expense management.

### Investments
Portfolio and investment tracking.

### Reminders
General important reminders.

### Travel
Travel planning and reminders.

### Events
Event tracking and reminders.

### Settings
Application preferences and future integration settings.

Create type-safe route definitions.

Implement:

- Navigation between sections.
- Correct back navigation.
- Selected navigation item state.
- Back-stack restoration where supported.
- Browser Back/Forward integration.
- Route preservation in browser URLs.
- Responsive navigation.

Use a maintained Compose Multiplatform navigation solution verified to support the chosen targets.

Prefer Navigation Compose if its built-in browser-history support provides a more reliable implementation than Navigation 3.

Do not write a custom navigation framework unnecessarily.

## 5. Responsive Application Layout

The application must work across different screen sizes.

Implement three adaptive layouts.

### 5.1 Compact Layout — Mobile

For narrow screen widths:

- Top application bar.
- Bottom navigation.
- Scrollable content.
- Compact financial cards.
- Floating or prominent primary action where appropriate.

### 5.2 Medium Layout — Tablets and Small Windows

- Navigation rail.
- Two-column content when appropriate.
- Responsive cards.
- Adaptive padding.

### 5.3 Expanded Layout — Desktop and Web

- Persistent navigation sidebar.
- Larger content area.
- Multi-column dashboard.
- Resizable layout.
- Appropriate maximum content width.
- Desktop pointer and keyboard interactions.

Use available window size or layout constraints to determine responsiveness.

Do not rely exclusively on platform detection.

A narrow desktop window should be able to use a compact layout.

## 6. Design System

Create a reusable design system in the approved shared module.

Implement:

**Theme**
- Light theme.
- Dark theme.
- System theme selection.
- Consistent color scheme.
- Typography.
- Shapes.
- Spacing.

**Reusable Components**
- AppScaffold.
- AppTopBar.
- NavigationBar.
- NavigationRail.
- NavigationSidebar.
- SummaryCard.
- FinancialAmountText.
- SectionHeader.
- EmptyState.
- LoadingState.
- ErrorState.
- PrimaryActionButton.
- ConfirmationDialog.
- SearchInput.
- StandardFormField.

Use shared Compose Multiplatform components.

Follow Material 3 conventions.

Keep the interface clean, professional, and suitable for a financial application.

Use consistent visual indicators for income, expenses, and neutral transfers.

Colors must not be the only mechanism for distinguishing financial states.

Create accessible text labels and content descriptions.

Support scrolling, keyboard interaction, and reasonable text scaling.

## 7. Implement Dashboard UI

Create a dashboard layout containing the following sections.

### Financial Summary

Cards for:

- Total account balance.
- Monthly income.
- Monthly expenses.
- Monthly savings.
- Investment portfolio value.

### Spending Overview

Display:

- Monthly spending overview.
- Category-wise spending preview.
- Recent transactions preview.

### Investment Overview

Display:

- Portfolio value.
- Portfolio change.
- SIP summary.
- Recent investment insights.

### Upcoming Items

Display:

- Upcoming payments.
- SIP reminders.
- Important reminders.
- Relevant events.

### Quick Actions

Provide visible actions for:

- Add transaction.
- Add investment.
- Add reminder.
- View analytics.

For now, use clearly identified demonstration data.

Do not present sample financial data as real user information.

Actions may navigate to appropriate placeholder screens.

Do not pretend an operation succeeded when it has not been implemented.

## 8. Implement Feature Screens

### 8.1 Finances Screen

Create an initial layout with:

- Account summary section.
- Income and expense summary.
- Transaction list.
- Spending categories.
- Budget section.
- Add transaction action.

No real database is required yet.

### 8.2 Investments Screen

Create an initial layout with:

- Portfolio value.
- Asset allocation.
- Investment holdings.
- SIP overview.
- Wealth projection section.
- Market insights section.

Use demonstration data.

### 8.3 Reminders Screen

Create:

- Reminder list layout.
- Upcoming reminders section.
- Add reminder action.
- Empty state.

### 8.4 Travel Screen

Create:

- Upcoming trips layout.
- Travel reminders section.
- Empty state.

### 8.5 Events Screen

Create:

- Upcoming events layout.
- Event reminder section.
- Empty state.

### 8.6 Settings Screen

Create sections for:

- Appearance.
- Currency preference.
- Notification preferences.
- Privacy.
- Data import.
- Account synchronization.
- About application.

Settings that are not functional must be clearly marked as unavailable or planned.

Theme changes should work during the current application session.

Persistent settings storage will be implemented later.

## 9. UI State Management

Use MVVM.

Each screen should have:

- A dedicated ViewModel where necessary.
- Immutable UI state.
- User-intent handling.
- Clear loading, success, and error states.

Use StateFlow and Compose state collection.

Avoid putting financial business logic directly inside Composable functions.

Use mock repositories or demonstration data providers that can later be replaced by real repositories.

Do not design the UI so that it depends directly on an eventual Room database.

## 10. Application Initialization

Implement proper startup initialization for:

- Android.
- Desktop.
- Web.

Set up dependency injection at the appropriate application entry points.

Initialize only the services required by this plan.

Do not initialize SMS listeners, financial APIs, authentication services, or background schedulers yet.

## 11. Testing Requirements

Implement relevant tests for:

- Navigation route definitions.
- App-level UI state transitions.
- ViewModel behavior.
- Formatting of demonstration financial amounts.
- Basic UI responsiveness where automated testing is practical.

Manually verify:

- Android application startup.
- Desktop application startup.
- Web application startup.
- Navigation between all sections.
- Mobile and expanded layouts.
- Dark and light themes.
- Back navigation.
- Browser Back/Forward behavior.
- Window resizing.

Use Gradle tasks appropriate for the targets actually configured.

Do not claim a platform was tested if the test could not be executed.

## 12. Implementation Restrictions

- Do not implement SMS reading.
- Do not implement NotificationListenerService.
- Do not implement bank integrations.
- Do not create a production database yet.
- Do not implement cloud synchronization.
- Do not implement investment predictions.
- Do not introduce real personal financial data.
- Do not add unnecessary dependencies.
- Do not silently remove Web or Desktop support to resolve compilation problems.
- Do not replace actual implementations with empty stubs merely to make a build pass.

Any feature that is not yet implemented must be represented honestly in the UI.

## 13. Documentation Updates

Update:

- docs/MODULE_STRUCTURE.md
- docs/TECH_STACK.md
- docs/IMPLEMENTATION_STATUS.md

Document:

- Actual created modules.
- Final dependency versions.
- Application entry points.
- Navigation implementation.
- Test results.
- Known limitations.

## 14. Acceptance Criteria

Plan 2 is complete only when:

1. Android builds successfully.
2. Desktop builds successfully.
3. Web WasmJS builds successfully.
4. All primary screens are accessible.
5. Navigation works correctly.
6. The UI adapts to mobile and desktop layouts.
7. The shared design system is implemented.
8. ViewModels follow the approved architecture.
9. Demonstration data is clearly identified.
10. No feature requires an unimplemented backend or database to launch the application.

If a runtime test cannot be performed, explicitly document the blocker instead of reporting success.

## 15. Final Response

After implementation, provide:

1. Modules and files created.
2. Summary of implemented UI.
3. Dependency changes.
4. Build commands executed.
5. Build and test results for each platform.
6. Remaining issues.
7. Any deviations from Plan 1.

Update `IMPLEMENTATION_STATUS.md`.

**Completion condition:** AnixLifeTracker launches on Android, Desktop, and Web with a functional shared application shell, responsive UI, and navigation ready for real financial data integration.