package com.hussain.walletflow.ui

import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.hussain.walletflow.data.TransactionDatabase
import com.hussain.walletflow.utils.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SmsScanner : Service() {

    companion object {
        // Pass this extra as `true` when starting from the Refresh button.
        // Omitting it (or passing false) = auto-scan on first open.
        const val EXTRA_MANUAL_REFRESH = "manual_refresh"
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isManualRefresh = intent?.getBooleanExtra(EXTRA_MANUAL_REFRESH, false) ?: false
        scanSmsMessages(isManualRefresh)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun scanSmsMessages(isManualRefresh: Boolean) {
        scope.launch {
            try {
                val dao = TransactionDatabase.getDatabase(applicationContext).transactionDao()

                // Two dedup strategies depending on how the scan was triggered:
                //
                // AUTO-SCAN (first open, SmsReceiver): exclude ALL SMS already in DB
                //   → prevents any duplicates, never re-shows added messages
                //
                // MANUAL REFRESH (user tapped Refresh): exclude only SMS currently in
                //   passbook (isAddedToMonthly = 0). SMS that were moved to monthly are
                //   NOT excluded, so they reappear as fresh passbook rows.
                //   This is exactly what the user expects when they tap Refresh.
                val alreadyInDb: HashSet<String> = if (isManualRefresh) {
                    HashSet(dao.getPassbookSmsTexts())
                } else {
                    HashSet(dao.getAllExistingSmsTexts())
                }

                val uri = Uri.parse("content://sms/inbox")
                val projection = arrayOf("address", "body", "date")

                val cursor: Cursor? = contentResolver.query(
                    uri, projection, null, null, "date DESC"
                )

                val newTransactions = buildList {
                    cursor?.use {
                        val addressIdx = it.getColumnIndexOrThrow("address")
                        val bodyIdx    = it.getColumnIndexOrThrow("body")
                        val dateIdx    = it.getColumnIndexOrThrow("date")

                        while (it.moveToNext()) {
                            val sender    = it.getString(addressIdx) ?: continue
                            val body      = it.getString(bodyIdx)    ?: continue
                            val timestamp = it.getLong(dateIdx)

                            if (body in alreadyInDb) continue

                            if (SmsParser.isBankingMessage(sender, body)) {
                                SmsParser.parseTransaction(sender, body, timestamp)
                                    ?.let { tx -> add(tx) }
                            }
                        }
                    }
                }

                if (newTransactions.isNotEmpty()) {
                    dao.insertAll(newTransactions)
                    Log.d(
                        "SmsScanner",
                        "Inserted ${newTransactions.size} transactions " +
                                "(manualRefresh=$isManualRefresh)"
                    )
                } else {
                    Log.d("SmsScanner", "No new transactions found (manualRefresh=$isManualRefresh)")
                }

            } catch (e: Exception) {
                Log.e("SmsScanner", "Error scanning SMS: ${e.message}", e)
            } finally {
                stopSelf()
            }
        }
    }
}