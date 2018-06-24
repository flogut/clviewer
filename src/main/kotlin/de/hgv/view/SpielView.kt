package de.hgv.view

import de.hgv.model.Kartenart
import de.hgv.model.Spiel
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import tornadofx.Fragment
import tornadofx.gridpane
import tornadofx.gridpaneColumnConstraints
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.imageview
import tornadofx.label
import tornadofx.pane
import tornadofx.row
import tornadofx.runAsyncWithOverlay
import tornadofx.runLater
import tornadofx.scrollpane
import tornadofx.tooltip
import tornadofx.useMaxWidth
import tornadofx.vbox
import java.time.format.DateTimeFormatter

class SpielView : Fragment() {
    val spiel = params["spiel"] as? Spiel
    var stage: Stage? = null

    override val root = scrollpane {
        if (spiel == null) return@scrollpane

        pane {
            vbox(spacing = 5.0) {
                runAsyncWithOverlay {
                    val details = spiel.details
                    val wappen = Download.downloadWappen(listOf(spiel.daheim, spiel.auswaerts))

                    details to wappen
                } ui { (details, wappen) ->
                    label(spiel.datum.format(DateTimeFormatter.ofPattern("eeee, dd. MMMM yyyy"))) {
                        alignment = Pos.CENTER
                        useMaxWidth = true

                        font = Font.font(18.0)
                    }

                    gridpane {
                        row {
                            imageview(wappen[spiel.daheim]!!) {
                                fitHeight = 100.0
                                isPreserveRatio = true
                                isSmooth = true

                                tooltip(spiel.daheim.name)

                                gridpaneColumnConstraints {
                                    percentWidth = 100 / 3.0
                                }
                            }

                            label("${spiel.toreHeim} : ${spiel.toreAuswaerts}") {
                                alignment = Pos.CENTER
                                useMaxWidth = true
                                font = Font.font(font.family, FontWeight.BOLD, 30.0)

                                gridpaneColumnConstraints {
                                    percentWidth = 100 / 3.0
                                }
                            }

                            imageview(wappen[spiel.auswaerts]!!) {
                                fitHeight = 100.0
                                isPreserveRatio = true
                                isSmooth = true

                                tooltip(spiel.auswaerts.name)

                                gridpaneColumnConstraints {
                                    percentWidth = 100 / 3.0
                                }
                            }
                        }
                    }

                    if (details != null) {
                        hbox {
                            useMaxWidth = true

                            val heim = buildAufstellung(details, Team.HEIM) {
                                gridpaneColumnConstraints {
                                    percentWidth = 50.0
                                }
                                hgrow = Priority.ALWAYS
                            }

                            add(heim)

                            val auswaerts = buildAufstellung(details, Team.AUSWAERTS) {
                                gridpaneColumnConstraints {
                                    percentWidth = 50.0
                                }

                                hgrow = Priority.ALWAYS
                            }

                            add(auswaerts)
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

    private fun buildAufstellung(spiel: Spiel.Details, team: Team, op: (VBox.() -> Unit)? = null): VBox {
        val startelf = if (team == Team.HEIM) spiel.startelfHeim else spiel.startelfAuswaerts
        val auswechslungen = if (team == Team.HEIM) spiel.auswechslungenHeim else spiel.auswechslungenAuswaerts
        val karten = if (team == Team.HEIM) {
            spiel.kartenHeim.sortedBy { it.spielminute }
        } else {
            spiel.kartenAuswaerts.sortedBy { it.spielminute }
        }
        val tore = spiel.tore
        val pos = if (team == Team.HEIM) Pos.TOP_LEFT else Pos.TOP_RIGHT

        val picHeight = 15.0
        val fontSize = 15.0

        val liste = VBox().apply {
            alignment = pos
            useMaxWidth = true

            for (spieler in startelf) {
                hbox(alignment = Pos.CENTER_LEFT, spacing = 5.0) {
                    hgrow = Priority.ALWAYS
                    setOnMouseClicked {
                        val view = tornadofx.find<SpielerView>(params = mapOf("spieler" to spieler))
                        val stage = view.openWindow(resizable = false)
                        view.stage = stage
                    }

                    label(spieler.name.trim()) {
                        font = Font.font(fontSize)
                    }

                    if (karten.any { it.spieler == spieler }) {
                        val karte = karten.findLast { it.spieler == spieler }!!
                        val image = when (karte.art) {
                            Kartenart.GELB -> resources.image("/resources/karte-gelb.png")
                            Kartenart.ROT -> resources.image("/resources/karte-rot.png")
                            Kartenart.GELBROT -> resources.image("/resources/karte-gelbrot.png")
                        }

                        imageview(image) {
                            fitHeight = picHeight
                            isSmooth = true
                            isPreserveRatio = true

                            tooltip(karte.spielminute.toString() + "'")
                        }
                    }

                    if (tore.any { it.torschuetze == spieler }) {
                        for (tor in tore.filter { it.torschuetze == spieler }) {
                            val image = when {
                                tor.eigentor -> resources.image("/resources/ball-rot.png")
                                tor.elfmeter -> resources.image("/resources/ball.png") // TODO Elfmeter kennzeichnen
                                else -> resources.image("/resources/ball.png")
                            }

                            imageview(image) {
                                fitHeight = picHeight
                                isSmooth = true
                                isPreserveRatio = true

                                tooltip(tor.spielminute.toString() + "' " + (tor.vorlagengeber?.name?.let { " ($it)" }
                                        ?: "") + (if (tor.eigentor) "Eigentor" else ""))
                            }
                        }
                    }

                    if (auswechslungen.any { it.aus == spieler }) {
                        val auswechslung = auswechslungen.find { it.aus == spieler }!!
                        val pfeil = resources.image("/resources/pfeil-rot.png")

                        imageview(pfeil) {
                            fitHeight = picHeight
                            isSmooth = true
                            isPreserveRatio = true

                            tooltip("${auswechslung.spielminute}' (${auswechslung.ein.name})")
                        }
                    }
                }
            }

            for (auswechslung in auswechslungen) {
                hbox(alignment = Pos.CENTER_LEFT, spacing = 5.0) {
                    hgrow = Priority.ALWAYS
                    setOnMouseClicked {
                        val view = tornadofx.find<SpielerView>(params = mapOf("spieler" to auswechslung.ein))
                        val stage = view.openWindow(resizable = false)
                        view.stage = stage
                    }

                    label(auswechslung.ein.name.trim()) {
                        font = Font.font(fontSize)
                    }

                    val pfeil = resources.image("/resources/pfeil-gruen.png")
                    imageview(pfeil) {
                        fitHeight = picHeight
                        isSmooth = true
                        isPreserveRatio = true

                        tooltip("${auswechslung.spielminute}' (${auswechslung.aus.name})")
                    }

                    if (karten.any { it.spieler == auswechslung.ein }) {
                        val karte = karten.findLast { it.spieler == auswechslung.ein }!!
                        val image = when (karte.art) {
                            Kartenart.GELB -> resources.image("/resources/karte-gelb.png")
                            Kartenart.ROT -> resources.image("/resources/karte-rot.png")
                            Kartenart.GELBROT -> resources.image("/resources/karte-gelbrot.png")
                        }

                        imageview(image) {
                            fitHeight = picHeight
                            isSmooth = true
                            isPreserveRatio = true

                            tooltip("${karte.spielminute}'")
                        }
                    }

                    if (tore.any { it.torschuetze == auswechslung.ein }) {
                        for (tor in tore.filter { it.torschuetze == auswechslung.ein }) {
                            val image = when {
                                tor.eigentor -> resources.image("/resources/ball-rot.png")
                                tor.elfmeter -> resources.image("/resources/ball.png") // TODO Elfmeter kennzeichnen
                                else -> resources.image("/resources/ball.png")
                            }

                            imageview(image) {
                                fitHeight = picHeight
                                isSmooth = true
                                isPreserveRatio = true

                                tooltip(tor.spielminute.toString() + "' " + (tor.vorlagengeber?.name?.let { " ($it)" }
                                        ?: "") + (if (tor.eigentor) "Eigentor" else ""))
                            }
                        }
                    }
                }
            }
        }

        op?.invoke(liste)

        return liste
    }

    init {
        if (spiel != null) {
            title = "${spiel.daheim.name} ${spiel.toreHeim} : ${spiel.toreAuswaerts} ${spiel.auswaerts.name}"
        }
    }

    private enum class Team {
        HEIM, AUSWAERTS
    }
}
