package de.hgv.download

import java.time.LocalDate

data class Spiel(
    val daheim: Verein,
    val auswaerts: Verein,
    val datum: LocalDate,
    val toreHeim: Int,
    val toreAuswaerts: Int,
    val phase: String,
    var details: Details? = null
) {

    val saison: Int by lazy {
        if (datum.isBefore(LocalDate.parse("${datum.year}-07-01"))) {
            datum.year
        } else {
            datum.year + 1
        }
    }

    /**
     * Lädt die Details dieses Spiels herunter und setzt sie als Wert von details, wenn details null ist
     * @param provider Provider, der zum Download genutzt werden soll
     */
    fun loadDetails(provider: Provider) {
        if (details == null) {
            details = provider.getDetailsForSpiel(this)
        }
    }

    //TODO details zu einer lazy Property ändern? Problem: kein fester Provider => Provider als Singleton? (=> nur bei compilezeit festlegbar)

    data class Details(
        // Spieler werden als Paar von ID und Name gespeichert, um unnötige Netzwerkanfragen zu vermeiden
        val spielerHeim: List<Spieler>,
        val spielerAuswaerts: List<Spieler>,
        val torschuetzen: List<Spieler>
    )
}