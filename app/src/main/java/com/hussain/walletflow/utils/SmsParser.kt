package com.hussain.walletflow.utils

import com.hussain.walletflow.data.Transaction
import com.hussain.walletflow.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object SmsParser {

        // ── Sender → bank name map (O(1) lookup instead of a 30-arm when-chain) ─
        // Keys are the normalised sender fragment (uppercase, no hyphens).
        private val SENDER_BANK_MAP: List<Pair<String, String>> = listOf(
                "HDFCBK" to "HDFC",   "HDFCBN" to "HDFC Bank",
                "ICICIB" to "ICICI",   "ICICBA" to "ICICI Bank",
                "SBIIN"  to "SBI",          "CBSSBI" to "SBI",
                "SBIINB" to "SBI",          "SBIPSG" to "SBI",
                "SBIBNK" to "SBI",          "SBMSBI" to "SBI",
                "SBISMT" to "SBI",
                "AXISBK" to "Axis",    "AXBNK"  to "Axis Bank",
                "KOTAKB" to "Kotak",   "KOTAK"  to "Kotak Bank",
                "PNBSMS" to "PNB",          "PUNJNB" to "PNB",
                "BOIIND" to "BOI",
                "BOBSMS" to "Bank of Baroda","BARODA" to "Bank of Baroda",
                "UNIONB" to "Union",   "UBOI"   to "Union Bank",
                "IDFCFB" to "IDFC First","IDFCBK" to "IDFC First Bank",
                "YESBNK" to "Yes",          "YESBANK" to "Yes Bank",
                "SCBANK" to "Standard Chartered","SCBIND" to "Standard Chartered",
                "CITIBK" to "Citi",
                "HSBCIN" to "HSBC",
                "DEUTSC" to "Deutsche",
                "CANBNK" to "Canara",  "CANARA" to "Canara Bank",
                "INDBNK" to "Indian",  "INDIANB" to "Indian Bank",
                "FEDBNK" to "Federal", "FEDBK"  to "Federal Bank",
                "IDBIBK" to "IDBI",    "IDBI"   to "IDBI Bank",
                "MAHABK" to "Bank of Maharashtra","BOMAH" to "Bank of Maharashtra",
                "CENBNK" to "Central",
                "IOBIND" to "IOB",          "IOB"    to "IOB",
                "UCOBK"  to "UCO",
                "BANDHN" to "Bandhan",
                "RBLBNK" to "RBL",
                "SIBSMS" to "South Indian",
                "KVBANK" to "Karur Vysya",
                "PAYTM"  to "Paytm",
                "GOOGLEPAY" to "Google Pay","GPAY"   to "Google Pay",
                "PHONEPE" to "PhonePe",
                "BHIM"   to "BHIM",
                "UPIBNK" to "UPI",          "NPCI"   to "UPI",
        )

        // Pre-compiled regex patterns — compiled once at class load time
        private val AMOUNT_PATTERN: Pattern =
                Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([0-9,]+\\.?[0-9]*)")

        private val ACCOUNT_PATTERN: Pattern = Pattern.compile(
                "(?:[Aa]/?[Cc]|account|acct)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with)?)?\\s*[*xX]*\\s*([0-9]{3,})"
        )
        private val ACCOUNT_FALLBACK: Pattern =
                Pattern.compile("[Aa]/?[Cc]\\s*[*xX]+\\s*(\\d{3,})")
        private val ACCOUNT_BROAD: Pattern = Pattern.compile(
                "(?:a/?c|account|acct)[^\\d]*[xX*]+(\\d{3,})", Pattern.CASE_INSENSITIVE
        )
        private val CARD_PATTERN: Pattern = Pattern.compile(
                "(?:card|debit|credit)[^\\d]{0,10}[*xX-]*(\\d{3,})",
                Pattern.CASE_INSENSITIVE
        )

        private val UPI_REMARK_PATTERN: Pattern =
                Pattern.compile("(?:to|from)\\s+([a-zA-Z0-9@._-]+)")

        // Date formats — NOT thread-safe; create new instances per call
        private val DATE_FORMAT_STRINGS = listOf(
                "dd-MM-yyyy", "dd/MM/yyyy", "dd-MMM-yyyy",
                "dd MMM yyyy", "dd-MM-yy", "dd/MM/yy", "ddMMMyy"
        )

        // Keyword sets — Set.contains() is O(1), List.any { contains } is O(n·m)
        private val DEBIT_KEYWORDS: Set<String> = setOf(
                "debited", "debit", "withdrawn", "paid", "purchase",
                "spent", "payment", "transfer to", "sent", "deducted"
        )
        private val CREDIT_KEYWORDS: Set<String> = setOf(
                "credited", "credit", "deposited", "received", "refund",
                "cashback", "transfer from", "salary", "credit alert"
        )
        private val CURRENCY_SIGNALS: Set<String> = setOf("rs", "inr", "₹")
        private val ACCOUNT_REF_SIGNALS: Set<String> = setOf("a/c", "account", "acct", "upi")

        // ── Public API ───────────────────────────────────────────────────────────

        fun isBankingMessage(sender: String, message: String): Boolean {
                val normSender = sender.uppercase().replace("-", "")
                val isKnownBank = SENDER_BANK_MAP.any { (key, _) -> normSender.contains(key) }

                val lower = message.lowercase()
                val hasDebitOrCredit = DEBIT_KEYWORDS.any { lower.contains(it) }
                        || CREDIT_KEYWORDS.any { lower.contains(it) }
                val hasCurrencySignal = CURRENCY_SIGNALS.any { lower.contains(it) }
                val hasTransactionKeywords = hasDebitOrCredit && hasCurrencySignal

                if (isKnownBank && hasTransactionKeywords) return true
                if (hasTransactionKeywords && ACCOUNT_REF_SIGNALS.any { lower.contains(it) }) return true
                return false
        }

        fun parseTransaction(sender: String, message: String, timestamp: Long): Transaction? {
                return try {
                        val amount = extractAmount(message) ?: return null
                        val type = detectTransactionType(message)
                        Transaction(
                                date = timestamp,
                                amount = amount,
                                type = type,
                                category = getDefaultCategory(type, message),
                                bankName = extractBankName(sender, message),
                                accountLastFour = extractAccountNumber(message),
                                instrumentType = detectInstrumentType(message),
                                remark = generateRemark(message),
                                originalSms = message,
                                isAddedToMonthly = false
                        )
                } catch (e: Exception) {
                        e.printStackTrace()
                        null
                }
        }

        // ── Private helpers ──────────────────────────────────────────────────────

        private fun extractAmount(message: String): Double? {
                val matcher = AMOUNT_PATTERN.matcher(message)
                if (!matcher.find()) return null
                return matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        private fun detectTransactionType(message: String): TransactionType {
                val lower = message.lowercase()
                val hasDebit = DEBIT_KEYWORDS.any { lower.contains(it) }
                val hasCredit = CREDIT_KEYWORDS.any { lower.contains(it) }
                return when {
                        hasDebit && !hasCredit -> TransactionType.EXPENSE
                        hasCredit && !hasDebit -> TransactionType.INCOME
                        else -> TransactionType.UNKNOWN
                }
        }

        private fun detectInstrumentType(message: String): String {
                val lower = message.lowercase()

                return when {
                        CARD_PATTERN.matcher(message).find() -> "CARD"
                        lower.contains("upi") -> "UPI"
                        ACCOUNT_PATTERN.matcher(message).find() -> "ACCOUNT"
                        else -> "ACCOUNT"
                }
        }

        private fun extractBankName(sender: String, message: String): String {
                val normSender = sender.uppercase().replace("-", "")
                // Try sender map first
                SENDER_BANK_MAP.firstOrNull { (key, _) -> normSender.contains(key) }
                        ?.second?.let { return it }

                // Fallback to message body keywords
                val lower = message.lowercase()
                return when {
                        "hdfc" in lower              -> "HDFC Bank"
                        "icici" in lower             -> "ICICI Bank"
                        "sbi" in lower || "state bank" in lower -> "SBI"
                        "axis" in lower              -> "Axis Bank"
                        "kotak" in lower             -> "Kotak Bank"
                        "pnb" in lower || "punjab national" in lower -> "PNB"
                        "idfc" in lower              -> "IDFC First Bank"
                        "yes bank" in lower          -> "Yes Bank"
                        "canara" in lower            -> "Canara Bank"
                        "indian bank" in lower       -> "Indian Bank"
                        "federal bank" in lower      -> "Federal Bank"
                        "idbi" in lower              -> "IDBI Bank"
                        "union bank" in lower        -> "Union Bank"
                        "bob" in lower || "bank of baroda" in lower -> "Bank of Baroda"
                        else                         -> sender
                }
        }

        private fun extractAccountNumber(message: String): String {
                fun last4(s: String) = if (s.length >= 4) s.takeLast(4) else s

                ACCOUNT_PATTERN.matcher(message).let {
                        if (it.find()) return last4(it.group(1) ?: "")
                }
                ACCOUNT_FALLBACK.matcher(message).let {
                        if (it.find()) return last4(it.group(1) ?: "")
                }
                ACCOUNT_BROAD.matcher(message).let {
                        if (it.find()) return last4(it.group(1) ?: "")
                }
                CARD_PATTERN.matcher(message).let {
                        if (it.find()) return last4(it.group(1) ?: "")
                }
                return ""
        }

        private fun generateRemark(message: String): String {
                val matcher = UPI_REMARK_PATTERN.matcher(message)
                return if (matcher.find()) "Transaction with ${matcher.group(1)}"
                else message.take(50).trim()
        }

        private fun getDefaultCategory(type: TransactionType, message: String): String {
                val lower = message.lowercase()
                return when (type) {
                        TransactionType.EXPENSE -> when {
                                "swiggy" in lower || "zomato" in lower       -> "Food"
                                "amazon" in lower || "flipkart" in lower     -> "Shopping"
                                "uber" in lower || "ola" in lower            -> "Transport"
                                "electricity" in lower || "bill" in lower    -> "Bills"
                                else                                         -> "Other Expense"
                        }
                        TransactionType.INCOME -> when {
                                "salary" in lower  -> "Salary"
                                "refund" in lower  -> "Refund"
                                else               -> "Other Income"
                        }
                        TransactionType.UNKNOWN -> "Other Expense"
                }
        }
}