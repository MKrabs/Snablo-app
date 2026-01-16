package de.mkrabs.snablo.app.util

import kotlin.math.abs

/**
 * Formatiert Cent-Beträge als Euro-String (z.B. 150 -> "1,50 €")
 */
fun formatPrice(cents: Int): String {
    val euros = cents / 100
    val centsPart = abs(cents % 100)
    val centsStr = if (centsPart < 10) "0$centsPart" else "$centsPart"
    return "$euros,$centsStr €"
}

/**
 * Formatiert Cent-Beträge mit Vorzeichen (z.B. 150 -> "+1,50 €", -150 -> "-1,50 €")
 */
fun formatPriceWithSign(cents: Int): String {
    val sign = if (cents >= 0) "+" else "-"
    val absCents = abs(cents)
    val euros = absCents / 100
    val centsPart = absCents % 100
    val centsStr = if (centsPart < 10) "0$centsPart" else "$centsPart"
    return "$sign$euros,$centsStr €"
}

/**
 * Formatiert Balance mit optionalem Minus (z.B. -150 -> "-1,50 €", 150 -> "1,50 €")
 */
fun formatBalance(cents: Int): String {
    val sign = if (cents < 0) "-" else ""
    val absCents = abs(cents)
    val euros = absCents / 100
    val centsPart = absCents % 100
    val centsStr = if (centsPart < 10) "0$centsPart" else "$centsPart"
    return "$sign$euros,$centsStr €"
}
