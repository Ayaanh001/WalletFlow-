package com.hussain.walletflow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val CURRENCY_KEY = stringPreferencesKey("currency")
        val DELETE_FROM_PASSBOOK_KEY = booleanPreferencesKey("delete_from_passbook")
        val NAME_KEY = stringPreferencesKey("user_name")
        val APP_LOCK_KEY = booleanPreferencesKey("app_lock_enabled")
        const val DEFAULT_CURRENCY = "INR"
    }
    val currencyFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[CURRENCY_KEY] ?: DEFAULT_CURRENCY
        }

    suspend fun updateCurrency(currency: String) {
        context.dataStore.edit { preferences -> preferences[CURRENCY_KEY] = currency }
    }

    val deleteFromPassbookFlow: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[DELETE_FROM_PASSBOOK_KEY] ?: true
            }

    suspend fun updateDeleteFromPassbook(delete: Boolean) {
        context.dataStore.edit { preferences -> preferences[DELETE_FROM_PASSBOOK_KEY] = delete }
    }

    val nameFlow: Flow<String> =
        context.dataStore.data.map { preferences -> preferences[NAME_KEY] ?: "" }

    suspend fun updateName(name: String) {
        context.dataStore.edit { preferences -> preferences[NAME_KEY] = name }
    }

    val appLockEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[APP_LOCK_KEY] ?: false
        }

    suspend fun updateAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[APP_LOCK_KEY] = enabled }
    }
}

data class Currency(val code: String, val symbol: String, val name: String)

object CurrencyData {
    val currencies =
            listOf(
                    Currency("INR", "₹", "Indian Rupee"),
                    Currency("USD", "$", "US Dollar"),
                    Currency("EUR", "€", "Euro"),
                    Currency("GBP", "£", "British Pound"),
                    Currency("JPY", "¥", "Japanese Yen"),
                    Currency("CNY", "¥", "Chinese Yuan"),
                    Currency("AUD", "A$", "Australian Dollar"),
                    Currency("CAD", "C$", "Canadian Dollar"),
                    Currency("CHF", "Fr", "Swiss Franc"),
                    Currency("SGD", "S$", "Singapore Dollar"),
                    Currency("NZD", "NZ$", "New Zealand Dollar"),
                    Currency("ZAR", "R", "South African Rand"),
                    Currency("HKD", "HK$", "Hong Kong Dollar"),
                    Currency("SEK", "kr", "Swedish Krona"),
                    Currency("NOK", "kr", "Norwegian Krone"),
                    Currency("DKK", "kr", "Danish Krone"),
                    Currency("MXN", "$", "Mexican Peso"),
                    Currency("BRL", "R$", "Brazilian Real"),
                    Currency("RUB", "₽", "Russian Ruble"),
                    Currency("KRW", "₩", "South Korean Won"),
                    Currency("TRY", "₺", "Turkish Lira"),
                    Currency("AED", "د.إ", "UAE Dirham"),
                    Currency("SAR", "﷼", "Saudi Riyal"),
                    Currency("THB", "฿", "Thai Baht"),
                    Currency("MYR", "RM", "Malaysian Ringgit"),
                    Currency("IDR", "Rp", "Indonesian Rupiah"),
                    Currency("PHP", "₱", "Philippine Peso"),
                    Currency("VND", "₫", "Vietnamese Dong"),
                    Currency("PKR", "₨", "Pakistani Rupee"),
                    Currency("BDT", "৳", "Bangladeshi Taka"),
                    Currency("LKR", "Rs", "Sri Lankan Rupee"),
                    Currency("NPR", "Rs", "Nepalese Rupee"),
                    Currency("ILS", "₪", "Israeli Shekel"),
                    Currency("PLN", "zł", "Polish Zloty"),
                    Currency("CZK", "Kč", "Czech Koruna"),
                    Currency("HUF", "Ft", "Hungarian Forint"),
                    Currency("RON", "lei", "Romanian Leu"),
                    Currency("BGN", "лв", "Bulgarian Lev"),
                    Currency("HRK", "kn", "Croatian Kuna"),
                    Currency("UAH", "₴", "Ukrainian Hryvnia")
            )
}
