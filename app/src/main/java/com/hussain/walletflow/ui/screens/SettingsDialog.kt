package com.hussain.walletflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hussain.walletflow.data.Currency
import com.hussain.walletflow.data.CurrencyData
import com.hussain.walletflow.data.UserPreferencesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefsRepository = remember { UserPreferencesRepository(context) }
    val selectedCurrency by prefsRepository.currencyFlow.collectAsState(initial = "INR")
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }

    val selectedCurrencyObj =
            remember(selectedCurrency) {
                CurrencyData.currencies.find { it.code == selectedCurrency }
            }

    val sortedCurrencies = remember { CurrencyData.currencies.sortedBy { it.name } }

    val filteredCurrencies =
            remember(searchQuery, sortedCurrencies) {
                if (searchQuery.isEmpty()) {
                    sortedCurrencies
                } else {
                    sortedCurrencies.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.code.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Title
            Text(
                    text = "Set currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selected currency card
            selectedCurrencyObj?.let { currency ->
                Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Symbol circle + name/code column
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                    modifier =
                                            Modifier.size(52.dp)
                                                    .background(
                                                            color =
                                                                    MaterialTheme.colorScheme
                                                                            .onPrimary.copy(
                                                                            alpha = 0.15f
                                                                    ),
                                                            shape = CircleShape
                                                    ),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        text = currency.symbol,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column {
                                Text(
                                        text = currency.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                                MaterialTheme.colorScheme.onPrimary.copy(
                                                        alpha = 0.85f
                                                )
                                )
                                Text(
                                        text = currency.code,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Selected badge
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                    text = "Selected",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Search (USD, EUR, GBP, etc)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Box(
                                    modifier =
                                            Modifier.padding(end = 4.dp)
                                                    .size(28.dp)
                                                    .background(
                                                            color =
                                                                    MaterialTheme.colorScheme
                                                                            .onSurface.copy(
                                                                            alpha = 0.08f
                                                                    ),
                                                            shape = CircleShape
                                                    )
                                                    .clickable(
                                                            interactionSource =
                                                                    remember {
                                                                        MutableInteractionSource()
                                                                    },
                                                            indication = ripple(bounded = false),
                                                            onClick = { searchQuery = "" }
                                                    ),
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Currency list
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding =
                                PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 4.dp,
                                        bottom = 52.dp
                                ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(filteredCurrencies, key = { _, c -> c.code }) { index, currency ->
                        val isFirst = index == 0
                        val isLast = index == filteredCurrencies.lastIndex

                        val shape =
                                when {
                                    filteredCurrencies.size == 1 -> RoundedCornerShape(24.dp)
                                    isFirst ->
                                            RoundedCornerShape(
                                                    topStart = 24.dp,
                                                    topEnd = 24.dp,
                                                    bottomStart = 8.dp,
                                                    bottomEnd = 8.dp
                                            )
                                    isLast ->
                                            RoundedCornerShape(
                                                    topStart = 8.dp,
                                                    topEnd = 8.dp,
                                                    bottomStart = 24.dp,
                                                    bottomEnd = 24.dp
                                            )
                                    else -> RoundedCornerShape(8.dp)
                                }

                        CurrencyItem(
                                currency = currency,
                                isSelected = currency.code == selectedCurrency,
                                shape = shape,
                                onClick = {
                                    scope.launch { prefsRepository.updateCurrency(currency.code) }
                                }
                        )
                    }
                }

                // Bottom fade gradient overlay
                Box(
                        modifier =
                                Modifier.align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .background(
                                                brush =
                                                        Brush.verticalGradient(
                                                                colors =
                                                                        listOf(
                                                                                Color.Transparent,
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surfaceContainerLow
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.9f
                                                                                        ),
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surfaceContainerLow
                                                                        )
                                                        )
                                        )
                )
            }
        }
    }
}

@Composable
fun CurrencyItem(
        currency: Currency,
        isSelected: Boolean,
        shape: RoundedCornerShape,
        onClick: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth().clip(shape).clickable(onClick = onClick),
            shape = shape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Symbol circle avatar
            Box(
                    modifier =
                            Modifier.size(44.dp)
                                    .background(
                                            color =
                                                    if (isSelected)
                                                            MaterialTheme.colorScheme.primary
                                                    else
                                                            MaterialTheme.colorScheme.onSurface
                                                                    .copy(alpha = 0.08f),
                                            shape = CircleShape
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = currency.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color =
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                )
            }

            // Name + code stacked
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = currency.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color =
                                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = currency.code,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
