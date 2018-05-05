package de.hgv.download

data class Tabelle(
    val gruppe: String,
    val tabelle: List<Pair<Verein, Int>>
)