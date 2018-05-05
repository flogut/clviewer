package de.hgv.model

data class Tabelle(
    val gruppe: String,
    val tabelle: List<Pair<Verein, Int>>
)