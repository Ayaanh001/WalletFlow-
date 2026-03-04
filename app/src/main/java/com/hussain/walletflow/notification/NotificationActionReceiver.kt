package com.hussain.walletflow.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.hussain.walletflow.data.Transaction
import com.hussain.walletflow.data.TransactionType
import com.hussain.walletflow.data.TransactionDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActionReceiver : BroadcastReceiver() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra(SmsNotificationHelper.EXTRA_NOTIFICATION_ID, -1)

        when (intent.action) {
            SmsNotificationHelper.ACTION_ADD_DIRECT -> {
                val txId = intent.getLongExtra(SmsNotificationHelper.EXTRA_TRANSACTION_ID, -1L)
                if (txId == -1L) return

                val result = goAsync()
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val dao = TransactionDatabase.getDatabase(context).transactionDao()
                        val existing = dao.getTransactionById(txId)

                        if (existing != null) {
                            // Real SMS transaction — just mark it
                            dao.markAsAddedToMonthly(txId)
                        } else {
                            // Test transaction or race condition — rebuild from intent and insert
                            val tx = buildTransactionFromIntent(intent)
                            if (tx != null) {
                                dao.insert(tx.copy(isAddedToMonthly = true))
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Added to this month", Toast.LENGTH_SHORT).show()
                            SmsNotificationHelper.cancel(context, notifId)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to add transaction", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        result.finish()
                    }
                }
            }

            SmsNotificationHelper.ACTION_DISMISS -> {
                SmsNotificationHelper.cancel(context, notifId)
            }
        }
    }

    private fun buildTransactionFromIntent(intent: Intent): Transaction? {
        val txId = intent.getLongExtra(SmsNotificationHelper.EXTRA_TRANSACTION_ID, -1L)
        if (txId == -1L) return null
        return Transaction(
            id               = txId,
            date             = System.currentTimeMillis(),
            amount           = intent.getDoubleExtra("tx_amount", 0.0),
            type             = TransactionType.valueOf(intent.getStringExtra("tx_type") ?: "EXPENSE"),
            bankName         = intent.getStringExtra("tx_bank") ?: "",
            accountLastFour  = intent.getStringExtra("tx_account") ?: "",
            instrumentType   = intent.getStringExtra("tx_instrument") ?: "UPI",
            remark           = intent.getStringExtra("tx_remark") ?: "",
            category         = intent.getStringExtra("tx_category") ?: "",
            paymentMethod    = intent.getStringExtra("tx_payment") ?: "",
            originalSms      = "",
            isAddedToMonthly = false
        )
    }
}