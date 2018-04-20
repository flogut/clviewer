package de.hgv.download

import java.util.*

data class Spiel(
    val daheim: Verein,
    val auswaerts: Verein,
    val datum: Date,
    val toreHeim: Int,
    val toreAuswaerts: Int,
    val spielerHeim: List<Spieler>,
    val spielerAuswaerts: List<Spieler>,
    val torschuetzen: List<Spieler>
)