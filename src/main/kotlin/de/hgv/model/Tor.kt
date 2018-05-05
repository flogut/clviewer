package de.hgv.model

data class Tor(
    val torschuetze: Spieler,
    val vorlagengeber: Spieler?,
    val spielminute: Int,
    val eigentor: Boolean,
    val elfmeter: Boolean
)