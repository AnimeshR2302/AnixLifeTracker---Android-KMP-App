# AnixLifeTracker - Database Migration Strategy

## 1. Schema Versioning

AnixLifeTracker database migrations are managed via **Room KMP Migration API**.

- **Current Version**: `1`
- **Destructive Migrations**: **STRICTLY PROHIBITED** in production builds (`fallbackToDestructiveMigration()` is disabled for release builds).

---

## 2. Migration Guidelines

When making schema changes in subsequent plans:

1. **Increment Database Version**: Update `@Database(version = N)` in `AppDatabase.kt`.
2. **Export Schema JSON**: Ensure Room schema export is updated and committed in `schemas/`.
3. **Write Explicit Migration**: Create a versioned `Migration(M, N)` object in `database/migrations/`:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(connection: SQLiteConnection) {
           connection.execSQL("ALTER TABLE transactions ADD COLUMN tags TEXT DEFAULT NULL")
       }
   }
   ```
4. **Register Migration**: Add `addMigrations(MIGRATION_1_2)` to `getDatabaseBuilder()`.
5. **Test Migration**: Execute automated migration tests verifying database integrity and zero data loss.
