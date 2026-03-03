package com.hussain.walletflow.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hussain.walletflow.data.CurrencyData
import com.hussain.walletflow.data.UserPreferencesRepository
import com.hussain.walletflow.utils.FileImportParser
import com.hussain.walletflow.viewmodel.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        onBack: () -> Unit,
        viewModel: TransactionViewModel,
        onNavigateToImport: () -> Unit
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val prefsRepository = remember { UserPreferencesRepository(context) }
        val selectedCurrency by
                prefsRepository.currencyFlow.collectAsState(
                        initial = UserPreferencesRepository.DEFAULT_CURRENCY
                )
        val userName by prefsRepository.nameFlow.collectAsState(initial = "")
        var nameInput by
                remember(userName) {
                        mutableStateOf(TextFieldValue(userName, TextRange(userName.length)))
                }
        var isEditingName by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val deleteFromPassbook by
                prefsRepository.deleteFromPassbookFlow.collectAsState(initial = true)

        var showCurrencyDialog by remember { mutableStateOf(false) }
        var isImporting by remember { mutableStateOf(false) }
        var showPasswordDialog by remember { mutableStateOf(false) }
        var passwordInput by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var passwordError by remember { mutableStateOf(false) }
        var pendingFileUri by remember { mutableStateOf<android.net.Uri?>(null) }

        val appLockEnabled by prefsRepository.appLockEnabledFlow.collectAsState(initial = false)

        // File picker for import
        val filePickerLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                        if (uri != null) {
                                isImporting = true
                                scope.launch {
                                        val result =
                                                withContext(Dispatchers.IO) {
                                                        FileImportParser.parseUri(context, uri)
                                                }
                                        isImporting = false
                                        when {
                                                result.passwordRequired -> {
                                                        pendingFileUri = uri
                                                        passwordInput = ""
                                                        passwordError = false
                                                        showPasswordDialog = true
                                                }
                                                result.transactions.isNotEmpty() -> {
                                                        viewModel.setPendingImport(
                                                                result.transactions
                                                        )
                                                        onNavigateToImport()
                                                }
                                                else -> {
                                                        viewModel.setPendingImport(emptyList())
                                                        onNavigateToImport()
                                                }
                                        }
                                }
                        }
                }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                        FilledIconButton(
                                                modifier = Modifier.padding(12.dp),
                                                onClick = onBack,
                                                colors =
                                                        IconButtonDefaults.filledIconButtonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceContainerLow,
                                                                contentColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                        ) {
                                                Icon(
                                                        Icons.Default.ArrowBack,
                                                        contentDescription = "Back",
                                                        modifier = Modifier.size(24.dp)
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.onSurface
                                        )
                        )
                }
        ) { innerPadding ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(innerPadding)
                                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        // ── Profile ─────────────────────────────────────────────────
                        Text(
                                text = "Profile",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )

                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                                .copy(alpha = 0.4f)
                                        ),
                                shape = RoundedCornerShape(16.dp)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.1f),
                                                        modifier = Modifier.size(40.dp)
                                                ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                        Icons.Default.Person,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                        }
                                                }

                                                if (isEditingName) {
                                                        OutlinedTextField(
                                                                value = nameInput,
                                                                onValueChange = { nameInput = it },
                                                                placeholder = { Text("User name") },
                                                                singleLine = true,
                                                                modifier =
                                                                        Modifier.weight(1f)
                                                                                .focusRequester(
                                                                                        focusRequester
                                                                                ),
                                                                shape = RoundedCornerShape(12.dp),
                                                                keyboardOptions =
                                                                        KeyboardOptions(
                                                                                capitalization =
                                                                                        KeyboardCapitalization
                                                                                                .Words,
                                                                                imeAction =
                                                                                        ImeAction
                                                                                                .Done
                                                                        ),
                                                                keyboardActions =
                                                                        KeyboardActions(
                                                                                onDone = {
                                                                                        scope
                                                                                                .launch {
                                                                                                        prefsRepository
                                                                                                                .updateName(
                                                                                                                        nameInput
                                                                                                                                .text
                                                                                                                                .trim()
                                                                                                                )
                                                                                                }
                                                                                        isEditingName =
                                                                                                false
                                                                                }
                                                                        )
                                                        )
                                                        LaunchedEffect(isEditingName) {
                                                                if (isEditingName)
                                                                        focusRequester
                                                                                .requestFocus()
                                                        }
                                                } else {
                                                        Text(
                                                                text =
                                                                        if (userName.isNotEmpty())
                                                                                userName
                                                                        else "Not set",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                                }
                                        }

                                        if (isEditingName) {
                                                IconButton(
                                                        onClick = {
                                                                scope.launch {
                                                                        prefsRepository.updateName(
                                                                                nameInput.text
                                                                                        .trim()
                                                                        )
                                                                }
                                                                isEditingName = false
                                                        }
                                                ) {
                                                        Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Save",
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                }
                                        } else {
                                                IconButton(onClick = { isEditingName = true }) {
                                                        Icon(
                                                                Icons.Default.Edit,
                                                                contentDescription = "Edit name",
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                                }
                                        }
                                }
                        }

                        // ── Preferences ──────────────────────────────────────────────
                        Text(
                                text = "Preferences",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )

                        // Currency selection card
                        Card(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { showCurrencyDialog = true },
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                                .copy(alpha = 0.4f)
                                        )
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.1f),
                                                        modifier = Modifier.size(40.dp)
                                                ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                        Icons.Default.Payments,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                        }
                                                }
                                                Text(
                                                        text = "Set currency",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                )
                                        }

                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                val currencyObj =
                                                        remember(selectedCurrency) {
                                                                CurrencyData.currencies.find {
                                                                        it.code == selectedCurrency
                                                                }
                                                        }
                                                Text(
                                                        text =
                                                                currencyObj?.let {
                                                                        "${it.symbol}  ${it.code}"
                                                                }
                                                                        ?: selectedCurrency,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                )
                                                Icon(
                                                        imageVector =
                                                                Icons.Default.KeyboardArrowRight,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                )
                                        }
                                }
                        }

                        // Delete from Passbook toggle card
                        Card(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                                .copy(alpha = 0.4f)
                                        )
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color =
                                                                MaterialTheme.colorScheme.error
                                                                        .copy(alpha = 0.1f),
                                                        modifier = Modifier.size(40.dp)
                                                ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                        Icons.Default.Delete,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .error,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                        }
                                                }
                                                Column {
                                                        Text(
                                                                text = "Delete from Passbook",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge,
                                                                fontWeight = FontWeight.Medium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                                        Text(
                                                                text =
                                                                        if (deleteFromPassbook)
                                                                                "Transactions are removed after adding"
                                                                        else
                                                                                "Transactions are kept after adding",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }

                                        HapticSwitch(
                                                checked = deleteFromPassbook,
                                                onCheckedChange = { scope.launch { prefsRepository.updateDeleteFromPassbook(it) } },
                                                thumbContent = {
                                                        Icon(
                                                                imageVector = if (deleteFromPassbook) Icons.Default.Check else Icons.Default.Close,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                                        )
                                                }
                                        )                                }
                        }
                        // App Lock toggle card
                        Card(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                                        modifier = Modifier.size(40.dp)
                                                ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                        Icons.Default.Fingerprint,
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colorScheme.secondary,
                                                                        modifier = Modifier.size(20.dp)
                                                                )
                                                        }
                                                }
                                                Column {
                                                        Text(
                                                                text = "App Lock",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                                text = if (appLockEnabled) "Biometric lock enabled" else "Unlock with biometrics",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                }
                                        }
                                        HapticSwitch(
                                                checked = appLockEnabled,
                                                onCheckedChange = { scope.launch { prefsRepository.updateAppLockEnabled(it) } },
                                                thumbContent = {
                                                        Icon(
                                                                imageVector = if (appLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                                        )
                                                }
                                        )
                                }
                        }

                        // ── Data ─────────────────────────────────────────────────────
                        Text(
                                text = "Data",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
                        )

                        // Import Transactions card
                        Card(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable(enabled = !isImporting) {
                                                        filePickerLauncher.launch(
                                                                arrayOf(
                                                                        "text/csv",
                                                                        "text/comma-separated-values",
                                                                        "text/plain",
                                                                        "application/vnd.ms-excel",
                                                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                                        "application/octet-stream"
                                                                )
                                                        )
                                                },
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                                .copy(alpha = 0.4f)
                                        )
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color =
                                                                MaterialTheme.colorScheme.tertiary
                                                                        .copy(alpha = 0.1f),
                                                        modifier = Modifier.size(40.dp)
                                                ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                                if (isImporting) {
                                                                        CircularProgressIndicator(
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                20.dp
                                                                                        ),
                                                                                strokeWidth = 2.dp,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .tertiary
                                                                        )
                                                                } else {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .FileUpload,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .tertiary,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                20.dp
                                                                                        )
                                                                        )
                                                                }
                                                        }
                                                }
                                                Column {
                                                        Text(
                                                                text = "Import Transactions",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge,
                                                                fontWeight = FontWeight.Medium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                                        Text(
                                                                text =
                                                                        if (isImporting)
                                                                                "Parsing file…"
                                                                        else
                                                                                "Import CSV, TXT or Excel (XLS/XLSX)",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }
                                        if (!isImporting) {
                                                Icon(
                                                        imageVector =
                                                                Icons.Default.KeyboardArrowRight,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                )
                                        }
                                }
                        }
                }
        }

        if (showCurrencyDialog) {
                SettingsDialog(onDismiss = { showCurrencyDialog = false })
        }

        if (showPasswordDialog) {
                AlertDialog(
                        onDismissRequest = {
                                showPasswordDialog = false
                                pendingFileUri = null
                                passwordVisible = false
                        },
                        icon = {
                                Icon(
                                        Icons.Default.FileUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                )
                        },
                        title = { Text("Password Protected File") },
                        text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                                "This file is password protected. Please enter the password to continue.",
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                        OutlinedTextField(
                                                value = passwordInput,
                                                onValueChange = {
                                                        passwordInput = it
                                                        passwordError = false
                                                },
                                                label = { Text("Password") },
                                                singleLine = true,
                                                isError = passwordError,
                                                supportingText = if (passwordError) {
                                                        { Text("Incorrect password. Please try again.") }
                                                } else null,
                                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                                else PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Password,
                                                        imeAction = ImeAction.Done
                                                ),
                                                trailingIcon = {
                                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                                Icon(
                                                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                                                        else Icons.Default.Visibility,
                                                                        contentDescription = if (passwordVisible) "Hide password"
                                                                        else "Show password"
                                                                )
                                                        }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                        )                                }
                        },
                        confirmButton = {
                                Button(
                                        enabled = passwordInput.isNotEmpty() && !isImporting,
                                        onClick = {
                                                val uri = pendingFileUri ?: return@Button
                                                isImporting = true
                                                scope.launch {
                                                        val result =
                                                                withContext(Dispatchers.IO) {
                                                                        FileImportParser
                                                                                .parseUriWithPassword(
                                                                                        context,
                                                                                        uri,
                                                                                        passwordInput
                                                                                )
                                                                }
                                                        isImporting = false
                                                        when {
                                                                result.passwordRequired -> {
                                                                        passwordError = true
                                                                }
                                                                else -> {
                                                                        showPasswordDialog = false
                                                                        pendingFileUri = null
                                                                        passwordVisible = false
                                                                        viewModel.setPendingImport(
                                                                                result.transactions
                                                                        )
                                                                        onNavigateToImport()
                                                                }
                                                        }
                                                }
                                        }
                                ) {
                                        if (isImporting) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                )
                                        } else {
                                                Text("Unlock")
                                        }
                                }
                        },
                        dismissButton = {
                                TextButton(
                                        onClick = {
                                                showPasswordDialog = false
                                                pendingFileUri = null
                                        }
                                ) { Text("Cancel") }
                        }
                )



        }
}

@Composable
fun HapticSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        thumbContent: (@Composable () -> Unit)? = null
) {
        val haptic = LocalHapticFeedback.current
        Switch(
                checked = checked,
                onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCheckedChange(it)
                },
                thumbContent = thumbContent
        )
}
