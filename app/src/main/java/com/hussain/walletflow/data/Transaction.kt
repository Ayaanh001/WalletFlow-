package com.hussain.walletflow.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Composite index on (isAddedToMonthly, date) means Room's date-range and
// filter queries skip a full-table scan.  The index on date alone speeds up
// ORDER BY date DESC on the passbook/monthly flows.
@Entity(
        tableName = "transactions",
        indices = [
                Index(value = ["isAddedToMonthly", "date"]),
                Index(value = ["date"])
        ]
)
data class Transaction(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val date: Long,
        val amount: Double,
        val type: TransactionType,
        val category: String,
        val bankName: String,
        val accountLastFour: String,
        val instrumentType: String = "ACCOUNT",
        val remark: String,
        val originalSms: String,
        val paymentMethod: String = "",
        val isAddedToMonthly: Boolean = false,
        val createdAt: Long = System.currentTimeMillis()
)

enum class TransactionType { INCOME, EXPENSE, UNKNOWN }

object TransactionCategories {
        val EXPENSE_CATEGORIES = listOf(
                "Food", "Shopping", "Transport", "Rent",
                "Bills", "Entertainment", "Housing", "Healthcare", "Education", "Other Expense"
        )
        val INCOME_CATEGORIES = listOf(
                "Salary", "Business", "Investment", "Refund", "Gift", "Other Income"
        )
        val PAYMENT_METHODS = listOf(
                "Cash", "UPI", "GPay", "PhonePe", "Credit Card", "Debit Card"
        )
}