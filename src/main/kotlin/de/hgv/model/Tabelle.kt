package de.hgv.model

data class Tabelle(
    val gruppe: String,
    val tabelle: List<Zeile>
) {
    data class Zeile(
        val platz: Int,
        val verein: Verein,
        val tordifferenz: Int,
        val punkte: Int
    )
}