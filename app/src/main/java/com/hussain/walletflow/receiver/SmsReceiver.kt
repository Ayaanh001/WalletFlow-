package com.hussain.walletflow.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.hussain.walletflow.data.TransactionDatabase
import com.hussain.walletflow.utils.SmsParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // goAsync() tells the system "don't kill this receiver yet, we're still working".
        // We must call pendingResult.finish() when done.
        // Without this, the BroadcastReceiver is considered done the moment onReceive()
        // returns, and the OS may kill the process before the coroutine completes.
        val pendingResult = goAsync()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val dao = TransactionDatabase.getDatabase(context).transactionDao()

                // Load existing SMS bodies so we don't double-insert if the inbox
                // scanner already picked this message up (race condition on first open)
                val alreadyInDb = HashSet(dao.getAllExistingSmsTexts())

                for (smsMessage in messages) {
                    val sender    = smsMessage.displayOriginatingAddress ?: continue
                    val body      = smsMessage.messageBody              ?: continue
                    val timestamp = smsMessage.timestampMillis

                    Log.d("SmsReceiver", "SMS from: $sender")

                    if (body in alreadyInDb) {
                        Log.d("SmsReceiver", "Already in DB, skipping")
                        continue
                    }

                    if (SmsParser.isBankingMessage(sender, body)) {
                        Log.d("SmsReceiver", "Banking SMS detected")
                        val transaction = SmsParser.parseTransaction(sender, body, timestamp)
                        if (transaction != null) {
                            dao.insert(transaction)
                            // Add to set in case multiple messages arrive in same broadcast
                            alreadyInDb.add(body)
                            Log.d("SmsReceiver", "Transaction saved: ${transaction.amount}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error saving transaction: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}