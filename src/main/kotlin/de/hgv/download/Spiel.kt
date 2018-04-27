package de.hgv.download

import java.time.LocalDate

data class Spiel(
    val daheim: Verein,
    val auswaerts: Verein,
    val datum: LocalDate,
    val toreHeim: Int,
    val toreAuswaerts: Int,
    // Spieler werden als Paar von ID und Name gespeichert, um unnötige Netzwerkanfragen zu vermeiden
    val spielerHeim: List<Pair<String, String>>,
    val spielerAuswaerts: List<Pair<String, String>>,
    val torschuetzen: List<Pair<String, String>>
)