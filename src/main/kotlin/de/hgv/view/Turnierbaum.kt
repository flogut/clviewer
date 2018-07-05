package de.hgv.view

import de.hgv.model.KoSpiele
import de.hgv.model.Phase
import de.hgv.model.Spiel
import de.hgv.model.Verein
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import tornadofx.*
import java.awt.GraphicsEnvironment

private val wappenGroesse = if (GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds.height -116-50*14 <0) {
    (GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds.height - 116)/14
}
else {
    50
}

/**
 * Baut das UI des Turnierbaums.
 * @param saison Saison, deren Verlauf dargestellt werden soll
 */
fun buildTurnierbaum(saison: Int, op: (GridPane.() -> Unit)? = null) = GridPane().apply {
    hgap = 20.0

    val turnier: Map<Phase, List<List<Spiel>>> = KoSpiele(saison).getTurnierbaum()
    val vereine: List<Verein> = turnier[Phase.ACHTELFINALE].orEmpty().flatMap { it.map { it.daheim } }.distinct()
    val wappen: Map<Verein, Image> = Download.downloadWappen(vereine)

    // Achtelfinalspiele:
    for (i: Int in 0..7) {
        vbox {
            gridpane {
                hgap = 10.0
                val paarung = turnier[Phase.ACHTELFINALE]?.get(i)

                buildSpiel(paarung, 0, wappen)
                buildSpiel(paarung, 1, wappen)

                setOnMouseClicked {
                    val spiel = if (it.y < wappenGroesse) {
                        paarung?.get(0)
                    } else {
                        paarung?.get(1)
                    }
                    val view = find<SpielView>(params = mapOf("spiel" to spiel))
                    val stage = view.openWindow(resizable = false)
                    view.stage = stage
                }

                onHover { hovering ->
                    cursor = if (hovering) { Cursor.HAND } else { Cursor.DEFAULT }
                }
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

                setOnMouseClicked {
                    val spiel = if (it.y < wappenGroesse) {
                        paarung?.get(0)
                    } else {
                        paarung?.get(1)
                    }
                    val view = find<SpielView>(params = mapOf("spiel" to spiel))
                    val stage = view.openWindow(resizable = false)
                    view.stage = stage
                }

                onHover { hovering ->
                    cursor = if (hovering) { Cursor.HAND } else { Cursor.DEFAULT }
                }
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

                setOnMouseClicked {
                    val spiel = if (it.y < wappenGroesse) {
                        paarung?.get(0)
                    } else {
                        paarung?.get(1)
                    }
                    val view = find<SpielView>(params = mapOf("spiel" to spiel))
                    val stage = view.openWindow(resizable = false)
                    view.stage = stage
                }

                onHover { hovering ->
                    cursor = if (hovering) { Cursor.HAND } else { Cursor.DEFAULT }
                }
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

            setOnMouseClicked {
                val spiel = turnier[Phase.FINALE]?.get(0)?.get(0)
                val view = find<SpielView>(params = mapOf("spiel" to spiel))
                val stage = view.openWindow(resizable = false)
                view.stage = stage
            }

            onHover { hovering ->
                cursor = if (hovering) { Cursor.HAND } else { Cursor.DEFAULT }
            }
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
        fitWidth = wappenGroesse.toDouble()
        isPreserveRatio = true
        isSmooth = true

        tooltip(daheim.name)
    }

    label(paarung?.get(index)?.toreHeim.toString()){
        textFill = c("#FFFFFF")
    }

    label(":"){
        textFill = c("#FFFFFF")
    }

    label(paarung?.get(index)?.toreAuswaerts.toString()){
        textFill = c("#FFFFFF")
    }

    imageview(wappen.getValue(auswaerts)) {
        fitWidth = wappenGroesse.toDouble()
        isPreserveRatio = true
        isSmooth = true

        tooltip(auswaerts.name)
    }
}
