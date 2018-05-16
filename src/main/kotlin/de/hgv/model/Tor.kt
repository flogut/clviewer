package de.hgv.model

/**
 * @author Florian Gutekunst
 */
data class Tor(
    val torschuetze: Spieler,
    val vorlagengeber: Spieler?,
    val spielminute: Int,
    val eigentor: Boolean,
    val elfmeter: Boolean
)