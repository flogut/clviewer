package de.hgv.view

import de.hgv.model.Phase
import de.hgv.provider.ActiveProvider
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import tornadofx.*

/**
 * Stellt die zu einer Gruppe die Tabelle und die Spiele (sortiert nach Datum) einer Saison dar. Die Gruppe und die
 * Saison werden über die params von TornadoFX mit den Keys "phase" und "saison" übergeben.
 *
 * @author Florian Gutekunst
 */
class GruppeDetailedView: Fragment() {

    val saison = params["saison"] as Int
    val phase = params["phase"] as Phase

    var stage: Stage? = null

    override val root = scrollpane {
        vbox {
            useMaxSize = true
            vgrow = Priority.ALWAYS

            vbox {
                useMaxSize = true
                vgrow = Priority.ALWAYS

                runAsyncWithOverlay {
                    val tabelle = ActiveProvider.getTabelle(phase.toString(), saison)
                    val spiele = ActiveProvider.getSpieleInPhase(phase, saison).sortedBy { it.datum }
                    val wappen = Download.downloadWappen(tabelle?.tabelle?.map { it.verein }.orEmpty())

                    Triple(tabelle, spiele, wappen)
                } ui { (tabelle, spiele, wappen) ->
                    // Überschrift Tabelle
                    label("Tabelle") {
                        useMaxWidth = true
                        textAlignment = TextAlignment.CENTER
                        alignment = Pos.TOP_CENTER
                        font = Font.font(font.family, FontWeight.BOLD, 30.0)
                    }

                    // Tabelle
                    gridpane {
                        hgap = 10.0
                        vgap = 3.0
                        alignment = Pos.TOP_CENTER

                        // Kopfzeile
                        row {
                            label("Platz")

                            label("Verein") {
                                useMaxWidth = true
                                gridpaneConstraints {
                                    columnSpan = 2
                                }
                            }

                            label("")

                            label("Diff.")

                            label("Punkte")
                        }

                        for (zeile in tabelle?.tabelle ?: listOf()) {
                            row {
                                // Platzierung
                                label(zeile.platz.toString())

                                // Wappen
                                imageview(wappen.getValue(zeile.verein)) {
                                    fitWidth = 25.0
                                    isPreserveRatio = true
                                    isSmooth = true
                                }

                                // Name
                                label(zeile.verein.name)

                                // Tordifferenz
                                label(zeile.tordifferenz.toString())

                                // Punkte
                                label(zeile.punkte.toString())
                            }
                        }
                    }

                    // Überschrift Spiele
                    label("Spiele") {
                        useMaxWidth = true
                        textAlignment = TextAlignment.CENTER
                        alignment = Pos.TOP_CENTER
                        font = Font.font(font.family, FontWeight.BOLD, 30.0)
                    }

                    // Für eine schöne Anordnung werden die Spiele wie eine Tabelle dargestellt
                    gridpane {
                        hgap = 10.0
                        vgap = 3.0
                        alignment = Pos.TOP_CENTER

                        setOnMouseClicked {
                            val spiel = spiele[(it.y / 37.5).toInt()]
                            val view = tornadofx.find<SpielView>(params = mapOf("spiel" to spiel))
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

                        for (spiel in spiele) {
                            row {
                                // Wappen Heimmannschaft
                                imageview(wappen.getValue(spiel.daheim)) {
                                    fitHeight = 35.0
                                    isPreserveRatio = true
                                    isSmooth = true
                                }

                                // Name Heimmannschaft
                                label(spiel.daheim.name)

                                // Tore Heimmannschaft
                                label(spiel.toreHeim.toString()) {
                                    paddingLeft = 10
                                }

                                label(" : ")

                                // Tore Auswärtsmannschaft
                                label(spiel.toreAuswaerts.toString()) {
                                    paddingRight = 10
                                }

                                // Name Auswärtsmannschaft
                                label(spiel.auswaerts.name) {
                                    useMaxWidth = true
                                    textAlignment = TextAlignment.RIGHT
                                    alignment = Pos.CENTER_RIGHT
                                }

                                // Wappen Auswärtsmannschaft
                                imageview(wappen.getValue(spiel.auswaerts)) {
                                    fitWidth = 35.0
                                    isPreserveRatio = true
                                    isSmooth = true
                                }
                            }
                        }
                    }

                    runLater {
                        resize()
                    }
                }
            }
        }
    }

    private fun resize() {
        stage?.sizeToScene()
        stage?.centerOnScreen()
    }

    init {
        title = "$phase (Saison ${saison - 1}/$saison)"
    }
}
