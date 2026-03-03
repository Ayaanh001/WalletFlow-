package com.hussain.walletflow.utils

import android.content.Context
import android.net.Uri
import com.hussain.walletflow.data.TransactionType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.crypt.Decryptor
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/** Wraps the result of parsing a file. */
data class ParseResult(
        val transactions: List<ParsedTransaction>,
        val passwordRequired: Boolean = false
)

data class ParsedTransaction(
        val date: Long,
        val amount: Double,
        val type: TransactionType,
        val narration: String,
        val paymentMethod: String,
        val rawLine: String = ""
)

object FileImportParser {

    // ──────────────────────────────────────────────────────────────────────────
    // Public entry point
    // ──────────────────────────────────────────────────────────────────────────

    private fun resolveFileName(context: Context, uri: Uri): String {
        var name = ""
        context.contentResolver.query(uri, null, null, null, null)?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) name = it.getString(nameIndex).lowercase()
        }
        return name.ifEmpty { uri.lastPathSegment?.lowercase() ?: "" }
    }

    fun parseUri(context: Context, uri: Uri): ParseResult {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val fileName = resolveFileName(context, uri)
        val isXlsx = fileName.endsWith(".xlsx") || mimeType.contains("spreadsheetml")
        val isXls = fileName.endsWith(".xls") || mimeType.contains("excel")
        val isExcel = isXlsx || isXls

        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            if (isExcel) {
                parseExcel(inputStream, isXlsx)
            } else {
                ParseResult(parseCsvOrTxt(inputStream))
            }
        }
                ?: ParseResult(emptyList())
    }

    fun parseUriWithPassword(context: Context, uri: Uri, password: String): ParseResult {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val fileName = resolveFileName(context, uri)
        val isXlsx = fileName.endsWith(".xlsx") || mimeType.contains("spreadsheetml")

        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            parseExcel(inputStream, isXlsx, password)
        }
                ?: ParseResult(emptyList())
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CSV / TXT parser
    // ──────────────────────────────────────────────────────────────────────────

    private fun parseCsvOrTxt(inputStream: java.io.InputStream): List<ParsedTransaction> {
        val lines =
                BufferedReader(InputStreamReader(inputStream))
                        .readLines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

        if (lines.size < 2) return emptyList()

        // ── 1. Find the header row ───────────────────────────────────────────
        // Skip lines that look like bank statement metadata (no recognizable headers)
        val headerIndex =
                lines.indexOfFirst { line ->
                    val lower = line.lowercase()
                    lower.contains("date") ||
                            lower.contains("narration") ||
                            lower.contains("description") ||
                            lower.contains("withdrawal") ||
                            lower.contains("debit") ||
                            lower.contains("deposit") ||
                            lower.contains("credit")
                }
        if (headerIndex < 0) return emptyList()

        val headerLine = lines[headerIndex]
        val delimiter = detectDelimiter(headerLine)
        val headers = splitLine(headerLine, delimiter).map { it.trim().lowercase() }

        // ── 2. Map column indices ────────────────────────────────────────────
        val dateCol =
                headers.indexOfFirst { h ->
                    h.contains("date") || h.contains("txn date") || h.contains("value date")
                }
        val narrationCol =
                headers.indexOfFirst { h ->
                    h.contains("narration") ||
                            h.contains("description") ||
                            h.contains("particulars") ||
                            h.contains("remarks") ||
                            h.contains("details") ||
                            h.contains("Narration")
                }
        val withdrawalCol =
                headers.indexOfFirst { h ->
                    h.contains("withdrawal") ||
                            h.contains("Withdrawal Amt") ||
                            h.contains("debit") ||
                            h == "dr" ||
                            h.contains("amount") && h.contains("debit")
                }
        val depositCol =
                headers.indexOfFirst { h ->
                    h.contains("deposit") ||
                            h.contains("deposit") ||
                            h.contains("Deposit Amt") ||
                            h.contains("credit") ||
                            h == "cr" ||
                            h.contains("amount") && h.contains("credit")
                }
        // Single "amount" column fallback (positive = income, negative = expense)
        val singleAmountCol =
                if (withdrawalCol < 0 && depositCol < 0)
                        headers.indexOfFirst { h -> h == "amount" || h == "amt" }
                else -1

        // ── 3. Parse data rows ───────────────────────────────────────────────
        val results = mutableListOf<ParsedTransaction>()

        for (i in (headerIndex + 1) until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val cells = splitLine(line, delimiter).map { it.trim().removeSurrounding("\"") }
            if (cells.size <= 1) continue

            val rawDate = if (dateCol >= 0 && dateCol < cells.size) cells[dateCol] else continue
            val parsedDate = parseDate(rawDate) ?: continue

            val narration =
                    if (narrationCol >= 0 && narrationCol < cells.size) cells[narrationCol] else ""

            val (amount, type) =
                    when {
                        singleAmountCol >= 0 && singleAmountCol < cells.size -> {
                            val amt = parseAmount(cells[singleAmountCol])
                            if (amt != null) {
                                if (amt >= 0) Pair(amt, TransactionType.INCOME)
                                else Pair(-amt, TransactionType.EXPENSE)
                            } else continue
                        }
                        else -> {
                            val withdrawal =
                                    if (withdrawalCol >= 0 && withdrawalCol < cells.size)
                                            parseAmount(cells[withdrawalCol])
                                    else null
                            val deposit =
                                    if (depositCol >= 0 && depositCol < cells.size)
                                            parseAmount(cells[depositCol])
                                    else null

                            when {
                                withdrawal != null && withdrawal > 0 ->
                                        Pair(withdrawal, TransactionType.EXPENSE)
                                deposit != null && deposit > 0 ->
                                        Pair(deposit, TransactionType.INCOME)
                                else -> continue // skip rows with no amount
                            }
                        }
                    }

            if (amount <= 0) continue

            val paymentMethod = detectPaymentMethod(narration)

            results.add(
                    ParsedTransaction(
                            date = parsedDate,
                            amount = amount,
                            type = type,
                            narration = narration.ifBlank { "Imported" },
                            paymentMethod = paymentMethod,
                            rawLine = line
                    )
            )
        }

        return results
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Excel Parser (XLS / XLSX)
    // ──────────────────────────────────────────────────────────────────────────

    private fun parseExcel(
        inputStream: InputStream,
        isXlsx: Boolean,
        password: String? = null
    ): ParseResult {
        val results = mutableListOf<ParsedTransaction>()
        try {
            val workbook: Workbook = when {
                // ── Explicit password supplied ────────────────────────────────────
                password != null -> {
                    val fs = POIFSFileSystem(inputStream)
                    val encInfo = EncryptionInfo(fs)
                    val decryptor = Decryptor.getInstance(encInfo)
                    if (!decryptor.verifyPassword(password)) {
                        return ParseResult(emptyList(), passwordRequired = true)
                    }
                    XSSFWorkbook(decryptor.getDataStream(fs))
                }

                // ── XLSX (no password yet) ────────────────────────────────────────
                isXlsx -> {
                    // Buffer the stream so we can retry as OLE2 if the first
                    // attempt fails (encrypted xlsx is actually an OLE2 container)
                    val bytes = inputStream.readBytes()
                    try {
                        XSSFWorkbook(bytes.inputStream())
                    } catch (e: Exception) {
                        // If it looks like an OLE2 / encrypted file, flag it
                        try {
                            val fs = POIFSFileSystem(bytes.inputStream())
                            // Successfully parsed as OLE2 → check for encryption
                            if (fs.root.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
                                return ParseResult(emptyList(), passwordRequired = true)
                            }
                            // OLE2 but not encrypted → try as legacy XLS
                            HSSFWorkbook(fs)
                        } catch (ole2Ex: Exception) {
                            return ParseResult(emptyList(), passwordRequired = false)
                        }
                    }
                }

                // ── XLS ───────────────────────────────────────────────────────────
                else -> {
                    try {
                        HSSFWorkbook(inputStream)
                    } catch (e: Exception) {
                        val msg = e.message?.lowercase() ?: ""
                        if (msg.contains("password") || msg.contains("encrypt") ||
                            msg.contains("protected")) {
                            return ParseResult(emptyList(), passwordRequired = true)
                        }
                        return ParseResult(emptyList())
                    }
                }
            }

            // ── The rest of your existing sheet-parsing logic is UNCHANGED ────────
            val sheet = workbook.getSheetAt(0) ?: return ParseResult(emptyList())
            val evaluator = workbook.creationHelper.createFormulaEvaluator()

            var headerRowIndex = -1
            val keywordSet = setOf(
                "date", "narration", "description", "particulars", "remarks",
                "withdrawal", "debit", "deposit", "credit", "amount", "amt",
                "dr", "cr", "txn", "value"
            )

            for (r in 0 until minOf(sheet.physicalNumberOfRows, 100)) {
                val row = sheet.getRow(r) ?: continue
                var matchCount = 0
                for (c in 0 until row.lastCellNum) {
                    val cv = getCellValueAsString(row.getCell(c), evaluator).trim().lowercase()
                    if (keywordSet.any { kw -> cv.contains(kw) }) matchCount++
                }
                if (matchCount >= 2) {
                    headerRowIndex = r
                    break
                }
            }

            if (headerRowIndex == -1) return ParseResult(emptyList())

            val headerRow = sheet.getRow(headerRowIndex) ?: return ParseResult(emptyList())
            val firstCellNum = headerRow.firstCellNum.toInt().coerceAtLeast(0)
            val lastCellNum = headerRow.lastCellNum.toInt()
            val headers = (firstCellNum until lastCellNum).map { c ->
                getCellValueAsString(headerRow.getCell(c), evaluator).trim().lowercase()
            }

            val dateCol = headers.indexOfFirst { h ->
                h.contains("date") || h.contains("txn") || h.contains("value date")
            }
            val narrationCol = headers.indexOfFirst { h ->
                h.contains("narration") || h.contains("description") ||
                        h.contains("particulars") || h.contains("remarks") || h.contains("details")
            }
            val withdrawalCol = headers.indexOfFirst { h ->
                (h.contains("withdrawal") || h.contains("debit") || h == "dr") && !h.contains("credit")
            }
            val depositCol = headers.indexOfFirst { h ->
                (h.contains("deposit") || h.contains("credit") || h == "cr") && !h.contains("debit")
            }
            val singleAmountCol = if (withdrawalCol < 0 && depositCol < 0)
                headers.indexOfFirst { h -> h == "amount" || h == "amt" }
            else -1

            if (dateCol < 0) return ParseResult(emptyList())

            for (r in (headerRowIndex + 1)..sheet.lastRowNum) {
                val row = sheet.getRow(r) ?: continue

                fun cellAt(colIdx: Int): String {
                    val absCol = firstCellNum + colIdx
                    return getCellValueAsString(row.getCell(absCol), evaluator)
                }

                fun dateMsAt(colIdx: Int): Long? {
                    val absCol = firstCellNum + colIdx
                    val cell = row.getCell(absCol) ?: return null
                    if (cell.cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        return cell.dateCellValue?.time
                    }
                    return parseDate(getCellValueAsString(cell, evaluator))
                }

                val allBlank = (firstCellNum until lastCellNum).all {
                    getCellValueAsString(row.getCell(it), evaluator).isBlank()
                }
                if (allBlank) continue

                val parsedDate = dateMsAt(dateCol) ?: continue
                val narration = if (narrationCol >= 0) cellAt(narrationCol) else ""

                val (amount, type) = when {
                    singleAmountCol >= 0 -> {
                        val amt = parseAmount(cellAt(singleAmountCol)) ?: continue
                        if (amt >= 0) Pair(amt, TransactionType.INCOME)
                        else Pair(-amt, TransactionType.EXPENSE)
                    }
                    else -> {
                        val withdrawal = if (withdrawalCol >= 0) parseAmount(cellAt(withdrawalCol)) else null
                        val deposit = if (depositCol >= 0) parseAmount(cellAt(depositCol)) else null
                        when {
                            withdrawal != null && withdrawal > 0 -> Pair(withdrawal, TransactionType.EXPENSE)
                            deposit != null && deposit > 0 -> Pair(deposit, TransactionType.INCOME)
                            else -> continue
                        }
                    }
                }

                if (amount <= 0) continue

                results.add(
                    ParsedTransaction(
                        date = parsedDate,
                        amount = amount,
                        type = type,
                        narration = narration.ifBlank { "Imported" },
                        paymentMethod = detectPaymentMethod(narration),
                        rawLine = (firstCellNum until lastCellNum).joinToString("|") {
                            getCellValueAsString(row.getCell(it), evaluator)
                        }
                    )
                )
            }
            workbook.close()

        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            if (msg.contains("password") || msg.contains("encrypt") || msg.contains("protected")) {
                return ParseResult(emptyList(), passwordRequired = true)
            }
            e.printStackTrace()
        }
        return ParseResult(results)
    }

    private fun getCellValueAsString(cell: Cell?, evaluator: FormulaEvaluator? = null): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    val date = cell.dateCellValue
                    SimpleDateFormat("dd/MM/yyyy").format(date)
                } else {
                    // Handle numeric values smartly - remove trailing .0 if it's an integer
                    val num = cell.numericCellValue
                    if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    val cellValue = evaluator?.evaluate(cell)
                    when (cellValue?.cellType) {
                        CellType.STRING -> cellValue.stringValue
                        CellType.NUMERIC -> {
                            val num = cellValue.numberValue
                            if (num == num.toLong().toDouble()) num.toLong().toString()
                            else num.toString()
                        }
                        CellType.BOOLEAN -> cellValue.booleanValue.toString()
                        else -> cell.cellFormula // fallback to formula string
                    }
                } catch (e: Exception) {
                    cell.cellFormula // fallback to formula string
                }
            }
            else -> ""
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Split a line respecting quoted fields (e.g. "Smith, John",... ) */
    private fun splitLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }

    private fun detectDelimiter(headerLine: String): Char {
        val commaCount = headerLine.count { it == ',' }
        val semicolonCount = headerLine.count { it == ';' }
        val tabCount = headerLine.count { it == '\t' }
        return when {
            tabCount >= commaCount && tabCount >= semicolonCount -> '\t'
            semicolonCount > commaCount -> ';'
            else -> ','
        }
    }

    private val DATE_FORMATS =
            listOf(
                    "dd/MM/yyyy",
                    "dd-MM-yyyy",
                    "MM/dd/yyyy",
                    "yyyy-MM-dd",
                    "dd MMM yyyy",
                    "dd-MMM-yyyy",
                    "dd MMM yy",
                    "dd-MMM-yy",
                    "d/M/yy",
                    "d/M/yyyy",
                    "yyyyMMdd",
                    "MM-dd-yyyy",
                    "dd.MM.yyyy",
                    "d-MMM-yyyy",
                    "d-MMM-yy"
            )

    private fun parseDate(raw: String): Long? {
        val cleaned = raw.trim().removeSurrounding("\"")
        for (fmt in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
                sdf.isLenient = false
                val date = sdf.parse(cleaned)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun parseAmount(raw: String): Double? {
        val cleaned =
                raw.trim()
                        .removeSurrounding("\"")
                        .replace(
                                Regex("[^0-9.\\-]"),
                                ""
                        ) // Remove everything except digits, dots and minus sign
        return cleaned.toDoubleOrNull()
    }

    private fun detectPaymentMethod(narration: String): String {
        val upper = narration.uppercase()
        return when {
            upper.contains("UPI") -> "UPI"
            upper.contains("NEFT") -> "NEFT"
            upper.contains("IMPS") -> "IMPS"
            upper.contains("RTGS") -> "RTGS"
            upper.contains("ATM") -> "Cash"
            else -> ""
        }
    }
}
