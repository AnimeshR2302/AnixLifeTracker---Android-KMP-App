# Plan 8 — Intelligent Reminders and Notification Engine

> Project: AnixLifeTracker — Android, Desktop JVM and Web; iOS later.
> Prerequisites: Plans 1–7 implemented, including the revised synchronization policy.
> Current release: Reminder management and in-app due lists; outgoing notification delivery remains disabled.
> Execution: Code changes only. No builds, tests or verifications will be run while implementing this plan.

## Ready-to-use implementation prompt

Extend the existing AnixLifeTracker KMP application with a shared reminder domain, financial reminder generation, due-item/in-app inbox UI and notification-delivery extension points. This plan replaces original Plan 15. Reuse existing models, backend/authentication, repositories, shared Compose UI and Plan 7 sync code. Do not implement investments, email or statement imports.

### 1. Execution rules

Read relevant current code and architecture, schema, financial processing, forecasting, synchronization, security and implementation-status documents. Use inspection to make source changes only; follow actual module names and existing dependency choices.

Do not run builds, compilation, Gradle sync/tasks, tests, lint/static analysis, app launches/previews, benchmarks, manual verification or deployment commands. Do not add unit/integration tests. Do not claim any behavior has been verified. Implement production-path validation/error handling as source code; do not execute validation activities against the application. Finish by stating that builds, tests and verifications were not run.

### 2. Explicit interpretation of “no notifications for now”

Implement reminder CRUD, schedules, due-state evaluation, preferences and an in-app due/upcoming list. All OS/browser/remote notification delivery is disabled in this release, on every platform. Do not show system banners, issue push messages or request POST_NOTIFICATIONS/browser notification permission. Do not add FCM, APNs, web-push subscriptions, service-worker push delivery, desktop notification daemons, alarm scheduling, scheduled email or a server notification cron job.

Define a provider-neutral notification-delivery interface and a disabled implementation returning an explicit Disabled/Unsupported result. Register only that implementation in dependency injection; no live sender or secret configuration is required. Preserve extension points for later opt-in delivery without treating it as already available.

Existing Plan 6 financial NotificationListenerService is an incoming data source and must continue to work where authorized. Its notification-access settings are unrelated to outgoing-notification permission. Do not remove incoming collection or use notification-output settings to disable it.

In-app entries are stored state displayed when the app is in use, not a promise of background alerts. Do not advertise reliable reminders while the app/browser is closed. Notification enable controls should explain “Delivery is not available in this version” rather than accepting a toggle with no effect.

### 3. Preserve the sync contract from Plan 7

Use the existing backend; no direct phone-to-browser transport or live subscription. Apply the same sync behavior to reminder definitions, edits, occurrence actions and preferences as to finances:

- Android creates/edits locally and queues mutations until explicit Sync. Incoming financial events do not upload reminder changes automatically.
- Android automatically pulls backend changes when the app opens, restores a session or returns to the foreground. This is pull-only; never flush the Android outbox from a lifecycle callback.
- Desktop/Web Save persists locally and immediately attempts to write edits/actions to the backend. Offline/failed saves remain visibly Pending; bounded retries of those writes while open are allowed.
- Desktop/Web Sync publishes pending edits and retrieves the last backend data. It cannot retrieve newer unsent phone data; show last Android upload and stale-state copy.
- Android receives committed Desktop/Web edits on its next open/foreground pull or explicit Sync, subject to connectivity/authentication. No live refresh is promised while Android stays open.
- No timer, reconnect callback or new reminder scheduler may introduce automatic Android uploads or Desktop/Web polling. Reuse the Plan 7 operation-reason policy and durable queues.

Remote actions must use stable IDs, conditional server revisions, idempotency keys, deletion tombstones, owner isolation and the existing conflict UI. Never make a separate reminder synchronization engine. A pull must preserve conflicting local edits and pending occurrence actions. Clear pending status only after the corresponding mutation is acknowledged.

### 4. Shared domain and persistence

Reuse/extend existing reminder entities instead of making a second reminder database. Keep a general reminder model capable of supporting later important, travel and event reminders, but implement the financial use cases and basic manual reminders now.

Suggested domain concepts, adapted to existing code:

| Concept | Required information |
|---|---|
| Reminder definition | Stable ID, owner, title, optional notes, source type/source entity, enabled state, schedule version, timestamps and server revision |
| Schedule | One-off/recurring, local due date/time, IANA timezone, date-only policy, cadence, anchor, end date/count, lead times and missed-date policy |
| Occurrence | Deterministic occurrence ID, reminder ID, schedule version, scheduled local date/time, resolved instant, due/overdue/completed/skipped/cancelled state |
| User action | Stable operation ID, occurrence ID, action, effective time, base revision and pending/conflict state |
| Snooze | Occurrence-specific next display time; does not change the recurring anchor |
| In-app item | Linked reminder/occurrence or financial insight, severity, reason, source revision, freshness, read/dismissed status and invalidation state |
| Preferences | In-app visibility, quiet-hour preferences reserved for future delivery, timezone/display settings and future channel choices |

Use exact monetary values and ISO currency only where a financial reminder requires them. Store links to canonical obligations/transactions; do not duplicate amounts as unrelated financial truth. Keep device permission/capability state local. Never synchronize a claim that notification permission granted on Android is granted in a browser.

Use versioned local and backend migration source files. Add foreign keys, owner-scoped indexes/uniqueness and access policies consistent with Plan 7. Do not execute migrations or configure a live delivery service.

### 5. Financial reminder generation

Integrate with Plan 7 budgets, obligations, recurrence and forecasts through shared use cases/events:

1. Confirmed rent, bills, subscriptions and repayment obligations generate upcoming and due occurrences.
2. Inferred recurring patterns stay suggestions until confirmed. Low-confidence forecasts must not become definite bills automatically.
3. A conservative payment match marks or proposes the occurrence paid. Ambiguous matches require user confirmation.
4. Budget threshold crossings and unusual spending can produce one in-app insight per relevant period/rule transition; apply cooldowns and invalidate stale calculations.
5. Cash-shortfall estimates show assumptions and stale/unknown-balance limitations. Do not produce a definite low-balance alert from an unknown starting balance.
6. A manual reminder can be created independently of an automated financial obligation.

Do not implement SIP reminders, fund alerts, market news, daily online investment monitoring or opportunity recommendations. Keep source-type extension points for those later plans.

Use stable generation keys such as owner + source entity + rule + occurrence + schedule version. The backend must enforce relevant uniqueness/conditional-write rules so multiple devices cannot create duplicate logical occurrences merely because they generated different random IDs. Regeneration after app restart, sync or date change must be idempotent.

### 6. Occurrence lifecycle and recurrence rules

Keep schedule evaluation in commonMain behind a clock/timezone abstraction. Use the established portable date/time library; do not use Android Calendar or JVM-only APIs in common code.

Implement explicit behavior for:

- One-time reminders and daily/weekly/monthly/yearly recurrence supported by the existing model.
- Monthly anchors on 29/30/31: use a documented last-valid-day policy while retaining the intended anchor for future months.
- Leap dates, DST gaps/overlaps and user timezone changes: preserve fixed-zone versus follow-user-zone semantics explicitly; resolve ambiguous instants deterministically.
- Date-only bills: apply a user-visible local-time policy rather than silently treating dates as UTC midnight.
- Upcoming, due, overdue, completed, skipped, cancelled, dismissed and snoozed presentation. Reading or dismissing an inbox entry must not mark a bill paid.
- Completing one occurrence leaves the next recurrence intact. Support “this occurrence” versus “this and future occurrences” for applicable edits.
- Snooze changes when that occurrence appears again; it does not rewrite its original due date or prove the payment happened.
- Payment reversal/deletion or unlinking can reopen the associated obligation according to the ledger relationship; never undo an explicit user action silently.
- Schedule deletion/disable cancels future generation. Preserve necessary history and tombstones without exposing deleted reminders as active.

Do not materialize an infinite series. Generate a bounded horizon and page older overdue items. On reopen after a long absence, compute missed occurrences deterministically and show a compact backlog; do not create a burst of duplicate inbox entries.

### 7. Shared engine responsibilities

Create or extend equivalent contracts for:

- ReminderRepository: observe/query/create/edit/delete definitions, occurrences and actions.
- ReminderScheduleCalculator: compute due occurrences from a schedule and explicit clock/timezone.
- FinancialReminderGenerator: map confirmed obligations and selected Plan 7 insights to definitions/items.
- ReminderCoordinator: recompute affected state when local data changes or a downloaded batch commits.
- InAppInboxRepository: persist read/dismissed/invalidated state and expose filtered lists.
- NotificationDeliveryGateway: capability/status/result interface with disabled implementation only.

Use narrow invalidation: recompute affected reminders when a transaction, obligation, schedule or preference changes. Batch after sync rather than recomputing per downloaded row. Scope coroutines to the signed-in user; cancel on logout. Device clocks may determine the local due view, but must not decide authoritative sync-write ordering.

Do not mark an occurrence Sent/Delivered when only an in-app row was created or a disabled gateway was called. Persist actual user state separately from derived due state. Delivery-attempt storage, if an existing model needs it, must represent Disabled truthfully.

### 8. Reminder and inbox UI on all platforms

Reuse shared Compose navigation/components and responsive layouts. Add:

1. Upcoming, Due/Overdue and Completed views with date/source filters.
2. Create/Edit Reminder with title, source link where applicable, schedule, timezone, recurrence, end conditions and enabled state.
3. Reminder detail with linked financial record, amount/currency if known, recurrence explanation and freshness.
4. Occurrence actions: complete/mark paid when appropriate, snooze, skip, dismiss and undo where domain rules permit.
5. In-app insights with reason/evidence, assumptions, date generated and stale/invalidated states.
6. Preferences that separate incoming financial-notification tracking, in-app reminder visibility and future outgoing delivery.

Show pending backend saves, local unsent Android edits and conflicts. A desktop action such as “mark paid” may be locally visible immediately but must show Pending until backend acknowledgment. On Android open, a downloaded completion removes the stale due item after the batch commits; if Android has a competing pending action, surface a conflict rather than toggling state back and forth.

Avoid intrusive modal alerts merely because an item became due. Use accessible lists, badges and clear empty/error/offline states. Do not expose transport details or permission jargon in ordinary reminder flows.

### 9. Platform adapters and disabled delivery

- commonMain: domain, schedule calculations, generation, preferences, shared UI and interfaces.
- Android: existing foreground hook invokes pull, then local reminder reconciliation; do not register notification jobs/alarms. Incoming collectors remain isolated.
- Desktop: use existing persistence and shared due engine while open; no startup daemon or notification sender.
- Web: use existing browser persistence, shared due engine and explicit Sync; no background service-worker delivery or notification permission request. A closed browser receives no reminders.
- iOS: maintain interface boundaries; do not add an implementation/target in this plan.

If earlier code has working outgoing senders or automatic schedules, disconnect their activation safely and reflect delivery-disabled state in settings. Do not delete reminder data or disable SMS/financial notification collection as a side effect. An existing remote sender must not be invoked by new code; required external configuration changes should be documented as pending, not performed through deployment commands.

### 10. Documentation and final source-change report

Create/update docs/REMINDERS.md, docs/NOTIFICATION_ENGINE.md, docs/SYNCHRONIZATION.md, docs/DATABASE_SCHEMA.md and docs/IMPLEMENTATION_STATUS.md. Record recurrence/timezone policies, bounded generation, financial linking, occurrence actions, user conflicts, platform capabilities and the fact that all outgoing delivery remains disabled.

Report changed components/files, schema/configuration source changes requiring later application, the preserved sync behavior and limitations. Explicitly state: “No builds, tests or verifications were run, as requested.” Do not report production readiness or active background notification support. Do not start Plan 9 automatically.
