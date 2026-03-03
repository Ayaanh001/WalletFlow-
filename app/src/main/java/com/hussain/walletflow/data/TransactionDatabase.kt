package com.hussain.walletflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: TransactionDatabase? = null

        // Migration v2 → v3: add the composite index so existing users keep
        // their data. Room cannot auto-create indexes on existing tables, so
        // we do it manually here.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_isAddedToMonthly_date` " +
                            "ON `transactions` (`isAddedToMonthly`, `date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_date` " +
                            "ON `transactions` (`date`)"
                )
            }
        }

        fun getDatabase(context: Context): TransactionDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    // Keep destructive as last-resort fallback only
                    .fallbackToDestructiveMigrationFrom(1)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}