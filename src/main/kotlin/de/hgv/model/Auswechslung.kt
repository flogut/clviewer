package de.hgv.model

/**
 * @author Florian Gutekunst
 */
data class Auswechslung(
    val ein: Spieler,
    val aus: Spieler,
    val spielminute: Int
)
