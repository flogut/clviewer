package de.hgv.download

import java.time.LocalDate

data class Spieler(
    val name: String,
    val id: String,
    val verein: Verein,
    val positionen: List<String>,
    val nummer: Int?,
    val land: String?,
    val geburtstag: LocalDate?,
    val groesse: Int?,
    val spielfuss: String?,
    val portraitUrl: String?
)