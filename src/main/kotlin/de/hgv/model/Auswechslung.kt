package de.hgv.model

data class Auswechslung(
    val ein: Spieler,
    val aus: Spieler,
    val spielminute: Int
)
