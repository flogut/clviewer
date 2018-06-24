package de.hgv.model

import de.hgv.provider.ActiveProvider
import java.time.LocalDate

/**
 * @author Florian Gutekunst
 */
data class Spieler(
    val name: String,
    val id: String
) {

    /**
     * Die Details des Spielers werden beim ersten Zugriff heruntergeladen und gespeichert. Dieser dauert also
     * potenziell etwas länger.
     */
    var details: Details? = null
        get() = field ?: ActiveProvider.getDetailsForSpieler(this).also { field = it }

    /**
     * @author Florian Gutekunst
     */
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Spieler

        if (id != other.id) return false

        return true
    }
}
