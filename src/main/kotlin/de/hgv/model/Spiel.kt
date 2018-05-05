package de.hgv.model

import de.hgv.provider.ActiveProvider
import java.time.LocalDate

data class Spiel(
    val daheim: Verein,
    val auswaerts: Verein,
    val datum: LocalDate,
    val toreHeim: Int,
    val toreAuswaerts: Int,
    val verlaengerung: Boolean,
    val elfmeterschiessen: Boolean,
    val phase: Phase
) {

    val saison: Int by lazy {
        if (datum.isBefore(LocalDate.parse("${datum.year}-07-01"))) {
            datum.year
        } else {
            datum.year + 1
        }
    }

    var details: Details? = null
        get() = field ?: ActiveProvider.getDetailsForSpiel(this).also { field = it }

    data class Details(
        val startelfHeim: List<Spieler>,
        val startelfAuswaerts: List<Spieler>,
        val auswechslungenHeim: List<Auswechslung>,
        val auswechslungenAuswaerts: List<Auswechslung>,
        val tore: List<Tor>,
        val kartenHeim: List<Karte>,
        val kartenAuswaerts: List<Karte>
    )
}