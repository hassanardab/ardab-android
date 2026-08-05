package com.mamo15.ardab.util

import java.util.Locale

/**
 * Formats a Double as a currency string with thousands separators and two decimal places.
 * Example: 1234567.89 -> "1,234,567.89"
 */
fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)}