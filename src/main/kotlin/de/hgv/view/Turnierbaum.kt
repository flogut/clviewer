package de.hgv.view

import de.hgv.model.KoSpiele
import de.hgv.model.Phase
import de.hgv.model.Spiel
import de.hgv.model.Verein
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import tornadofx.gridpane
import tornadofx.gridpaneConstraints
import tornadofx.imageview
import tornadofx.label
import tornadofx.row
import tornadofx.tooltip
import tornadofx.vbox

/**
 * Baut das UI des Turnierbaums.
 * @param saison Saison, deren Verlauf dargestellt werden soll
 */
fun buildTurnierbaum(saison: Int, op: (GridPane.() -> Unit)? = null) = GridPane().apply {
    hgap = 20.0

    val turnier = KoSpiele(saison).getTurnierbaum()
    val vereine = turnier[Phase.ACHTELFINALE].orEmpty().flatMap { it.map { it.daheim } }.distinct()
    val wappen = Download.downloadWappen(vereine)

    // Achtelfinalspiele:
    for (i: Int in 0..7) {
        vbox {
            gridpane {
                hgap = 10.0
                val paarung = turnier[Phase.ACHTELFINALE]?.get(i)

                buildSpiel(paarung, 0, wappen)
                buildSpiel(paarung, 1, wappen)
            }
            gridpaneConstraints {
                columnRowIndex(i / 4 * 6, 2 * (i % 4))
            }
        }
    }

    // Viertelfinalspiele
    for (i: Int in 0..3) {
        vbox {
            gridpane {
                hgap = 10.0
                val paarung = turnier[Phase.VIERTELFINALE]?.get(i)

                buildSpiel(paarung, 0, wappen)
                buildSpiel(paarung, 1, wappen)
            }
            gridpaneConstraints {
                columnRowIndex(4 * (i / 2) + 1, 4 * (i % 2) + 1)
            }
        }
    }

    // Halbfinalspiele
    for (i: Int in 0..1) {
        vbox {
            gridpane {
                hgap = 10.0
                val paarung = turnier[Phase.HALBFINALE]?.get(i)

                buildSpiel(paarung, 0, wappen)
                buildSpiel(paarung, 1, wappen)
            }
            gridpaneConstraints {
                columnRowIndex((i + 1) * 2, 3)
            }
        }
    }

    // Finalspiel
    vbox {
        gridpane {
            hgap = 10.0
            buildSpiel(turnier[Phase.FINALE]?.get(0), 0, wappen)
        }
        gridpaneConstraints {
            columnRowIndex(3, 3)
            alignment = Pos.CENTER
        }
    }

    op?.invoke(this)
}

/**
 * Baut die Darstellung eines Spiels.
 * @param paarung Paarung, in der das Spiel ist
 * @param index Index des Spiels
 * @param wappen Wappen der Vereine, die in den KO-Spielen vertreten sind
 */
private fun GridPane.buildSpiel(paarung: List<Spiel>?, index: Int, wappen: Map<Verein, Image>) = row {
    val daheim = paarung?.get(index)?.daheim ?: Verein.UNBEKANNT
    val auswaerts = paarung?.get(index)?.auswaerts ?: Verein.UNBEKANNT

    imageview(wappen.getValue(daheim)) {
        fitWidth = 50.0
        isPreserveRatio = true
        isSmooth = true

        tooltip(daheim.name)
    }

    label(paarung?.get(index)?.toreHeim.toString())

    label(":")

    label(paarung?.get(index)?.toreAuswaerts.toString())

    imageview(wappen.getValue(auswaerts)) {
        fitWidth = 50.0
        isPreserveRatio = true
        isSmooth = true

        tooltip(auswaerts.name)
    }
}
