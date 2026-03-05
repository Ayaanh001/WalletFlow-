package com.hussain.walletflow.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hussain.walletflow.ui.theme.*

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
    "Housing"       to CategoryHousing,
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

// ── Available icons for the custom-item picker ────────────────────────────────

val AVAILABLE_ICONS: Map<String, ImageVector> = linkedMapOf(
    // Finance
    "AccountBalance"        to Icons.Filled.AccountBalance,
    "AccountBalanceWallet"  to Icons.Filled.AccountBalanceWallet,
    "Savings"               to Icons.Filled.Savings,
    "CreditCard"            to Icons.Filled.CreditCard,
    "Payment"               to Icons.Filled.Payment,
    "Payments"              to Icons.Filled.Payments,
    "AttachMoney"           to Icons.Filled.AttachMoney,
    "MonetizationOn"        to Icons.Filled.MonetizationOn,
    "TrendingUp"            to Icons.Filled.TrendingUp,
    "TrendingDown"          to Icons.Filled.TrendingDown,
    // Food & Lifestyle
    "Restaurant"            to Icons.Filled.Restaurant,
    "LocalCafe"             to Icons.Filled.LocalCafe,
    "LocalBar"              to Icons.Filled.LocalBar,
    "Fastfood"              to Icons.Filled.Fastfood,
    "LocalPizza"            to Icons.Filled.LocalPizza,
    "LocalGroceryStore"     to Icons.Filled.LocalGroceryStore,
    // Shopping & Home
    "ShoppingBag"           to Icons.Filled.ShoppingBag,
    "ShoppingCart"          to Icons.Filled.ShoppingCart,
    "Home"                  to Icons.Filled.Home,
    "House"                 to Icons.Filled.House,
    "Apartment"             to Icons.Filled.Apartment,
    "Chair"                 to Icons.Filled.Chair,
    // Transport
    "DirectionsCar"         to Icons.Filled.DirectionsCar,
    "DirectionsBus"         to Icons.Filled.DirectionsBus,
    "Train"                 to Icons.Filled.Train,
    "Flight"                to Icons.Filled.Flight,
    "TwoWheeler"            to Icons.Filled.TwoWheeler,
    "LocalTaxi"             to Icons.Filled.LocalTaxi,
    // Health & Education
    "LocalHospital"         to Icons.Filled.LocalHospital,
    "MedicalServices"       to Icons.Filled.MedicalServices,
    "FitnessCenter"         to Icons.Filled.FitnessCenter,
    "School"                to Icons.Filled.School,
    "MenuBook"              to Icons.Filled.MenuBook,
    // Entertainment & Social
    "Movie"                 to Icons.Filled.Movie,
    "MusicNote"             to Icons.Filled.MusicNote,
    "SportsEsports"         to Icons.Filled.SportsEsports,
    "SportsSoccer"          to Icons.Filled.SportsSoccer,
    "Celebration"           to Icons.Filled.Celebration,
    "CardGiftcard"          to Icons.Filled.CardGiftcard,
    // Work & Business
    "Business"              to Icons.Filled.Business,
    "Work"                  to Icons.Filled.Work,
    "Laptop"                to Icons.Filled.Laptop,
    "Build"                 to Icons.Filled.Build,
    // Utilities & Bills
    "Receipt"               to Icons.Filled.Receipt,
    "Bolt"                  to Icons.Filled.Bolt,
    "WaterDrop"             to Icons.Filled.WaterDrop,
    "Wifi"                  to Icons.Filled.Wifi,
    "PhoneAndroid"          to Icons.Filled.PhoneAndroid,
    // Misc
    "Pets"                  to Icons.Filled.Pets,
    "ChildCare"             to Icons.Filled.ChildCare,
    "LocalLaundryService"   to Icons.Filled.LocalLaundryService,
    "Subscriptions"         to Icons.Filled.Subscriptions,
    "Replay"                to Icons.Filled.Replay,
    "QrCode"                to Icons.Filled.QrCode,
    "Category"              to Icons.Filled.Category,
    "MoreHoriz"             to Icons.Filled.MoreHoriz,
)

// ── Available colors for the custom-item color picker ────────────────────────

val AVAILABLE_COLORS: List<Color> = listOf(
    Color(0xFFF44336), // Red
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Purple
    Color(0xFF673AB7), // Deep Purple
    Color(0xFF3F51B5), // Indigo
    Color(0xFF2196F3), // Blue
    Color(0xFF03A9F4), // Light Blue
    Color(0xFF00BCD4), // Cyan
    Color(0xFF009688), // Teal
    Color(0xFF4CAF50), // Green
    Color(0xFF8BC34A), // Light Green
    Color(0xFFCDDC39), // Lime
    Color(0xFFFFEB3B), // Yellow
    Color(0xFFFFC107), // Amber
    Color(0xFFFF9800), // Orange
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Grey
    Color(0xFF1A73E8), // Google Blue
    Color(0xFF5F259F), // PhonePe Purple
)

// ── Runtime custom-item overrides (populated from DataStore) ─────────────────

private val customCategoryColors = mutableMapOf<String, Color>()
private val customCategoryIcons  = mutableMapOf<String, ImageVector>()
private val customPaymentColors  = mutableMapOf<String, Color>()
private val customPaymentIcons   = mutableMapOf<String, ImageVector>()

fun registerCustomCategories(items: List<com.hussain.walletflow.data.CustomItem>) {
    customCategoryColors.clear()
    customCategoryIcons.clear()
    items.forEach { item ->
        val color = parseHexColor(item.colorHex)
        val icon  = AVAILABLE_ICONS[item.iconKey] ?: DEFAULT_CATEGORY_ICON
        customCategoryColors[item.name] = color
        customCategoryIcons[item.name]  = icon
    }
}

fun registerCustomPaymentMethods(items: List<com.hussain.walletflow.data.CustomItem>) {
    customPaymentColors.clear()
    customPaymentIcons.clear()
    items.forEach { item ->
        val color = parseHexColor(item.colorHex)
        val icon  = AVAILABLE_ICONS[item.iconKey] ?: DEFAULT_PAYMENT_ICON
        customPaymentColors[item.name] = color
        customPaymentIcons[item.name]  = icon
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor("#$hex"))
    } catch (e: Exception) {
        DEFAULT_CATEGORY_COLOR
    }
}

fun colorToHex(color: Color): String {
    val argb = color.value.toLong().ushr(32).toInt()
    // color.value is a packed ULong; use android to convert
    return String.format("%06X", (android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red   * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue  * 255).toInt()
    ) and 0xFFFFFF))
}

// ── Public accessors ─────────────────────────────────────────────────────────

fun getCategoryIcon(category: String): ImageVector =
    customCategoryIcons[category] ?: CATEGORY_ICONS[category] ?: DEFAULT_CATEGORY_ICON

fun getCategoryColor(category: String): Color =
    customCategoryColors[category] ?: CATEGORY_COLORS[category] ?: DEFAULT_CATEGORY_COLOR

fun getPaymentIcon(method: String): ImageVector =
    customPaymentIcons[method] ?: PAYMENT_ICONS[method] ?: DEFAULT_PAYMENT_ICON

fun getPaymentChipColor(method: String): Color =
    customPaymentColors[method] ?: PAYMENT_COLORS[method] ?: DEFAULT_PAYMENT_COLOR