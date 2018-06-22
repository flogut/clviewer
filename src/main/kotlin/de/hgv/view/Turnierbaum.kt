package de.hgv.view

import de.hgv.model.KoSpiele
import de.hgv.model.Phase
import de.hgv.model.Spiel
import de.hgv.model.Verein
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import tornadofx.*

fun buildTurnierbaum(saison: Int) = GridPane().apply {
    hgap = 20.0

    val turnier = KoSpiele(saison).getTurnierbaum()
    val vereine = turnier[Phase.ACHTELFINALE].orEmpty().flatMap { it.map { it.daheim } }.distinct()
    val wappen = downloadWappen(vereine)

    //Achtelfinalspiele:
    for (i: Int in (0..7)) {
        vbox {
            gridpane {
                hgap = 10.0
                val paarung = turnier[Phase.ACHTELFINALE]?.get(i)

                buildSpiel(paarung, 0, wappen)
                buildSpiel(paarung, 1, wappen)
            }
            gridpaneConstraints {
                columnRowIndex((i / 4) * 6, 2 * (i % 4))
            }
        }
    }


    //Viertelfinalspiele
    for (i: Int in (0..3)) {
        vbox {
            gridpane {
                hgap = 10.0
                row {
                    imageview(wappen.getValue(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(0)?.daheim)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }

                    label(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(0)?.toreHeim.toString())

                    label(":")

                    label(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(0)?.toreAuswaerts.toString())

                    imageview(wappen.getValue(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(0)?.auswaerts)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }


                }
                row {
                    imageview(wappen.getValue(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(1)?.daheim)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }

                    label(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(1)?.toreHeim.toString())

                    label(":")

                    label(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(1)?.toreAuswaerts.toString())

                    imageview(wappen.getValue(turnier.get(Phase.VIERTELFINALE)?.get(i)?.get(1)?.auswaerts)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }


                }
            }
            gridpaneConstraints {
                columnRowIndex(4 * (i / 2) + 1, 4 * (i % 2) + 1)
            }
        }
    }

    //Halbfinalspiele
    for (i: Int in (0..1)) {
        vbox {
            gridpane {
                hgap = 10.0
                row {
                    imageview(wappen.getValue(turnier.get(Phase.HALBFINALE)?.get(i)?.get(0)?.daheim)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }

                    label(turnier.get(Phase.HALBFINALE)?.get(i)?.get(0)?.toreHeim.toString())

                    label(":")

                    label(turnier.get(Phase.HALBFINALE)?.get(i)?.get(0)?.toreAuswaerts.toString())

                    imageview(wappen.getValue(turnier.get(Phase.HALBFINALE)?.get(i)?.get(0)?.auswaerts)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }


                }
                row {
                    imageview(wappen.getValue(turnier.get(Phase.HALBFINALE)?.get(i)?.get(1)?.daheim)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }

                    label(turnier.get(Phase.HALBFINALE)?.get(i)?.get(1)?.toreHeim.toString())

                    label(":")

                    label(turnier.get(Phase.HALBFINALE)?.get(i)?.get(1)?.toreAuswaerts.toString())

                    imageview(wappen.getValue(turnier.get(Phase.HALBFINALE)?.get(i)?.get(1)?.auswaerts)) {
                        fitWidth = 50.0
                        isPreserveRatio = true
                        isSmooth = true
                    }


                }
            }
            gridpaneConstraints {
                columnRowIndex((i + 1) * 2, 3)
            }
        }
    }

    //Finalspiel
    vbox {
        gridpane {
            hgap = 10.0
            row {
                imageview(wappen.getValue(turnier.get(Phase.FINALE)?.get(0)?.get(0)?.daheim)) {
                    fitWidth = 50.0
                    isPreserveRatio = true
                    isSmooth = true
                }

                label(turnier.get(Phase.FINALE)?.get(0)?.get(0)?.toreHeim.toString())

                label(":")

                label(turnier.get(Phase.FINALE)?.get(0)?.get(0)?.toreAuswaerts.toString())

                imageview(wappen.getValue(turnier.get(Phase.FINALE)?.get(0)?.get(0)?.auswaerts)) {
                    fitWidth = 50.0
                    isPreserveRatio = true
                    isSmooth = true
                }
            }
        }
        gridpaneConstraints {
            columnRowIndex(3, 3)
            alignment = Pos.CENTER
        }
    }

    return@apply
}

private fun GridPane.buildSpiel(paarung: List<Spiel>?, index: Int, wappen: Map<Verein?, Image>) = row {
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
    }
}

