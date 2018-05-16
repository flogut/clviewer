package de.hgv.model

/**
 * @author Florian Gutekunst
 */
data class Karte(
    val spieler: Spieler,
    val spielminute: Int,
    val art: Kartenart
)

/**
 * @author Florian Gutekunst
 */
enum class Kartenart {
    GELB, ROT, GELBROT
}