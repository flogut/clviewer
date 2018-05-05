package de.hgv.download

data class Karte(
    val spieler: Spieler,
    val spielminute: Int,
    val art: Kartenart
)

enum class Kartenart {
    GELB, ROT, GELBROT
}