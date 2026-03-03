package com.hussain.walletflow.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hussain.walletflow.ui.theme.*

// Using Maps instead of when-expressions means each lookup is O(1) and the
// compiler doesn't have to build a string-equality chain on every recomposition.

private val CATEGORY_ICONS: Map<String, ImageVector> = mapOf(
    "Food"          to Icons.Filled.Restaurant,
    "Shopping"      to Icons.Filled.ShoppingBag,
    "Transport"     to Icons.Filled.DirectionsCar,
    "Rent"          to Icons.Filled.Home,
    "Bills"         to Icons.Filled.Receipt,
    "Entertainment" to Icons.Filled.Movie,
    "Housing"       to Icons.Filled.House,
    "Healthcare"    to Icons.Filled.LocalHospital,
    "Education"     to Icons.Filled.School,
    "Other Expense" to Icons.Filled.MoreHoriz,
    "Salary"        to Icons.Filled.AccountBalance,
    "Business"      to Icons.Filled.Business,
    "Investment"    to Icons.Filled.Savings,
    "Refund"        to Icons.Filled.Replay,
    "Gift"          to Icons.Filled.CardGiftcard,
    "Other Income"  to Icons.Filled.MoreHoriz,
)
private val DEFAULT_CATEGORY_ICON: ImageVector = Icons.Filled.Category

private val CATEGORY_COLORS: Map<String, Color> = mapOf(
    "Food"          to CategoryFood,
    "Shopping"      to CategoryShopping,
    "Transport"     to CategoryTransport,
    "Rent"          to CategoryRent,
    "Bills"         to CategoryBills,
    "Entertainment" to CategoryEntertainment,
    "Healthcare"    to CategoryHealth,
    "Education"     to CategoryEducation,
    "Other Expense" to CategoryOthers,
    "Salary"        to CategorySalary,
    "Business"      to CategoryBusiness,
    "Investment"    to CategoryInvestment,
    "Refund"        to CategoryBonus,
    "Gift"          to CategoryBonus,
    "Other Income"  to CategoryOthers,
)
private val DEFAULT_CATEGORY_COLOR = Color(0xFF9E9E9E)

private val PAYMENT_ICONS: Map<String, ImageVector> = mapOf(
    "Cash"        to Icons.Filled.Payments,
    "UPI"         to Icons.Filled.QrCode,
    "GPay"        to Icons.Filled.AccountBalanceWallet,
    "PhonePe"     to Icons.Filled.AccountBalanceWallet,
    "Credit Card" to Icons.Filled.CreditCard,
    "Debit Card"  to Icons.Filled.Payment,
)
private val DEFAULT_PAYMENT_ICON: ImageVector = Icons.Filled.Payments

private val PAYMENT_COLORS: Map<String, Color> = mapOf(
    "GPay"        to Color(0xFF1A73E8),
    "PhonePe"     to Color(0xFF5F259F),
    "Paytm"       to Color(0xFF00BAF2),
    "Cash"        to Color(0xFF43A047),
    "UPI"         to Color(0xFFFF6B00),
    "Credit Card" to Color(0xFFE53935),
    "Debit Card"  to Color(0xFF1565C0),
)
private val DEFAULT_PAYMENT_COLOR = Color(0xFF9E9E9E)

fun getCategoryIcon(category: String): ImageVector =
    CATEGORY_ICONS[category] ?: DEFAULT_CATEGORY_ICON

fun getCategoryColor(category: String): Color =
    CATEGORY_COLORS[category] ?: DEFAULT_CATEGORY_COLOR

fun getPaymentIcon(method: String): ImageVector =
    PAYMENT_ICONS[method] ?: DEFAULT_PAYMENT_ICON

fun getPaymentChipColor(method: String): Color =
    PAYMENT_COLORS[method] ?: DEFAULT_PAYMENT_COLOR