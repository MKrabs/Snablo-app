package de.mkrabs.snablo.app.util

import kotlin.math.roundToInt

/**
 * Format a price in euros according to the rules:
 * - Use postfix euro sign: "x€"
 * - Use comma as decimal separator
 * - If the value has zero cents, show as integer ("1€")
 * - Otherwise show two decimals ("1,50€", "0,10€")
 */
fun formatPriceEu(amount: Double?): String {
    if (amount == null) return "—"
    // Work in cents to avoid floating point display issues
    val cents = (amount * 100.0).roundToInt()
    val euros = cents / 100
    val centPart = cents % 100
    return if (centPart == 0) {
        "${euros}€"
    } else {
        // pad cent part to two digits, use comma as decimal separator
        val centsStr = centPart.toString().padStart(2, '0')
        "${euros},${centsStr}€"
    }
}

/**
 * Heuristic formatter for item labels: previously this returned a short friendly
 * label for opaque ids; user requested to undo that change, so return the raw
 * trimmed string (or "—" for blank/null).
 */
fun formatItemLabel(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return raw.trim()
}
