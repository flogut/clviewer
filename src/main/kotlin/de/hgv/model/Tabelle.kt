package de.hgv.model

/**
 * @author Florian Gutekunst
 */
data class Tabelle(
    val gruppe: String,
    val tabelle: List<Zeile>
) {

    /**
     * @author Florian Gutekunst
     */
    data class Zeile(
        val platz: Int,
        val verein: Verein,
        val tordifferenz: Int,
        val punkte: Int
    )
}