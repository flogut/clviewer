package de.hgv.download

import java.time.LocalDate

data class Spieler(
    val name: String,
    val id: String,
    var details: Details? = null
) {

    /**
     * Lädt die Details dieses Spielers herunter und setzt sie als Wert von details, wenn details null ist
     * @param provider Provider, der zum Download genutzt werden soll
     */
    fun loadDetails(provider: Provider) {
        if (details != null) {
            details = provider.getDetailsForSpieler(this)
        }
    }

    data class Details(
        val verein: Verein,
        val positionen: List<String>,
        val nummer: Int?,
        val land: String?,
        val geburtstag: LocalDate?,
        val groesse: Int?,
        val spielfuss: String?,
        val portraitUrl: String?
    )
}