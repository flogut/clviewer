package de.hgv.download

import java.time.LocalDate

data class Spieler(
    val name: String,
    val id: String
) {

    var details: Details? = null
        get() = field ?: ActiveProvider.getDetailsForSpieler(this).also { field = it }

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