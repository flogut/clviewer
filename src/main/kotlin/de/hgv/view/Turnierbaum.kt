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

private val wappenGroesse =
    if (GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds.height - 116 - 50 * 14 < 0) {
        (GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds.height - 116) / 14
    } else {
        50
    }

/**
 * Baut das UI des Turnierbaums.
 * @param saison Saison, deren Verlauf dargestellt werden soll
 */
fun buildTurnierbaum(saison: Int, op: (GridPane.() -> Unit)? = null) = GridPane().apply {
    hgap = 20.0

    val turnier: Map<Phase, List<List<Spiel>>> = KoSpiele(saison).getTurnierbaum()
    val vereine: List<Verein> =
        turnier[Phase.ACHTELFINALE].orEmpty().flatMap { it.map { it.daheim } + it.map { it.auswaerts } }.distinct()
    val wappen: Map<Verein, Image> = Download.downloadWappen(vereine)

    // Achtelfinalspiele:
    for (i: Int in 0..7) {
        vbox {
            gridpane {
                hgap = 10.0
                val paarung = turnier[Phase.ACHTELFINALE]?.get(i)

                buildSpiel(paarung?.getOrNull(0), wappen)
                buildSpiel(paarung?.getOrNull(1), wappen)

                if (paarung != null) {
                    setOnMouseClicked {
                        val spiel = if (it.y < wappenGroesse) {
                            paarung[0]
                        } else {
                            paarung[1]
                        }
                        val view = find<SpielView>(params = mapOf("spiel" to spiel))
                        val stage = view.openWindow(resizable = false)
                        view.stage = stage
                    }

                    onHover { hovering ->
                        cursor = if (hovering) {
                            Cursor.HAND
                        } else {
                            Cursor.DEFAULT
                        }
                    }
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

                buildSpiel(paarung?.getOrNull(0), wappen)
                buildSpiel(paarung?.getOrNull(1), wappen)

                if (paarung != null) {
                    setOnMouseClicked {
                        val spiel = if (it.y < wappenGroesse) {
                            paarung[0]
                        } else {
                            paarung[1]
                        }
                        val view = find<SpielView>(params = mapOf("spiel" to spiel))
                        val stage = view.openWindow(resizable = false)
                        view.stage = stage
                    }

                    onHover { hovering ->
                        cursor = if (hovering) {
                            Cursor.HAND
                        } else {
                            Cursor.DEFAULT
                        }
                    }
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

                buildSpiel(paarung?.getOrNull(0), wappen)
                buildSpiel(paarung?.getOrNull(1), wappen)

                if (paarung != null) {
                    setOnMouseClicked {
                        val spiel = if (it.y < wappenGroesse) {
                            paarung[0]
                        } else {
                            paarung[1]
                        }
                        val view = find<SpielView>(params = mapOf("spiel" to spiel))
                        val stage = view.openWindow(resizable = false)
                        view.stage = stage
                    }

                    onHover { hovering ->
                        cursor = if (hovering) {
                            Cursor.HAND
                        } else {
                            Cursor.DEFAULT
                        }
                    }
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
            val paarung = turnier[Phase.FINALE]
            buildSpiel(paarung?.getOrNull(0)?.getOrNull(0), wappen)

            if (paarung != null) {
                setOnMouseClicked {
                    val spiel = paarung[0][0]
                    val view = find<SpielView>(params = mapOf("spiel" to spiel))
                    val stage = view.openWindow(resizable = false)
                    view.stage = stage
                }

                onHover { hovering ->
                    cursor = if (hovering) {
                        Cursor.HAND
                    } else {
                        Cursor.DEFAULT
                    }
                }
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
 * @param spiel Spiel, das angezeigt werden soll, null wenn das Spiel noch nicht ausgelost oder gespielt wurde
 * @param wappen Wappen der Vereine, die in den KO-Spielen vertreten sind
 */
private fun GridPane.buildSpiel(
    spiel: Spiel?,
    wappen: Map<Verein, Image>
) = row {
    val daheim = if (spiel?.daheim in wappen) spiel!!.daheim else Verein.UNBEKANNT
    val auswaerts = if (spiel?.auswaerts in wappen) spiel!!.auswaerts else Verein.UNBEKANNT

    imageview(wappen.getValue(daheim)) {
        fitWidth = wappenGroesse.toDouble()
        isPreserveRatio = true
        isSmooth = true

        tooltip(daheim.name)
    }

    label(spiel?.toreHeim?.toString() ?: "?") {
        textFill = c("#FFFFFF")
    }

    label(":") {
        textFill = c("#FFFFFF")
    }

    label(spiel?.toreAuswaerts?.toString() ?: "?") {
        textFill = c("#FFFFFF")
    }

    imageview(wappen.getValue(auswaerts)) {
        fitWidth = wappenGroesse.toDouble()
        isPreserveRatio = true
        isSmooth = true

        tooltip(auswaerts.name)
    }
}
