package de.hgv.model

data class Karte(
    val spieler: Spieler,
    val spielminute: Int,
    val art: Kartenart
)

enum class Kartenart {
    GELB, ROT, GELBROT
}