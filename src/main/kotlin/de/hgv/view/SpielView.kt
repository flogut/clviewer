package de.hgv.view

import de.hgv.model.Kartenart
import de.hgv.model.Spiel
import javafx.geometry.Pos
import javafx.scene.Cursor
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
import tornadofx.onHover
import tornadofx.pane
import tornadofx.row
import tornadofx.runAsyncWithOverlay
import tornadofx.runLater
import tornadofx.scrollpane
import tornadofx.tooltip
import tornadofx.useMaxWidth
import tornadofx.vbox
import java.time.format.DateTimeFormatter

/**
 * Stellt die Details zu einem Spiel dar. Das Spiel wird über die params von TornadoFX mit dem Key "spiel" übergeben.
 *
 * @author Niklas Cölle, Moritz Brandt, Florian Gutekunst (Unterstützung)
 */
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
                    // Datum
                    label(spiel.datum.format(DateTimeFormatter.ofPattern("eeee, dd. MMMM yyyy"))) {
                        alignment = Pos.CENTER
                        useMaxWidth = true

                        font = Font.font(18.0)
                    }

                    gridpane {
                        row {
                            // Wappen Heimmannschaft
                            imageview(wappen[spiel.daheim]!!) {
                                fitHeight = 100.0
                                isPreserveRatio = true
                                isSmooth = true

                                tooltip(spiel.daheim.name)

                                gridpaneColumnConstraints {
                                    percentWidth = 100 / 3.0
                                }
                            }

                            // Ergebnis
                            label("${spiel.toreHeim} : ${spiel.toreAuswaerts}") {
                                alignment = Pos.CENTER
                                useMaxWidth = true
                                font = Font.font(font.family, FontWeight.BOLD, 30.0)

                                gridpaneColumnConstraints {
                                    percentWidth = 100 / 3.0
                                }
                            }

                            // Wappen Auswärtsmannschaft
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

                            children += buildAufstellung(details, Team.HEIM) {
                                gridpaneColumnConstraints {
                                    percentWidth = 50.0
                                }

                                hgrow = Priority.ALWAYS
                            }

                            children += buildAufstellung(details, Team.AUSWAERTS) {
                                gridpaneColumnConstraints {
                                    percentWidth = 50.0
                                }

                                hgrow = Priority.ALWAYS
                            }
                        }
                    }

                    // Setzt die Größe des Fensters neu, nachdem das Laden abgeschlossen ist
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

    /**
     * Baut das UI der Aufstellung eines Teams.
     * @param spiel Spiel, dessen Aufstellung angezeigt werden soll
     * @param team Team, dessen Aufstellung angezeigt werden soll (heim oder auswärts)
     * @param op Optionale Funktion für weitere Einstellungen auf dem resultierenden UI
     */
    private fun buildAufstellung(spiel: Spiel.Details, team: Team, op: (VBox.() -> Unit)? = null): VBox {
        val startelf = if (team == Team.HEIM) spiel.startelfHeim else spiel.startelfAuswaerts
        val auswechslungen = if (team == Team.HEIM) spiel.auswechslungenHeim else spiel.auswechslungenAuswaerts
        val karten = if (team == Team.HEIM) {
            spiel.kartenHeim
        } else {
            spiel.kartenAuswaerts
        }
        val tore = spiel.tore
        val pos = if (team == Team.HEIM) Pos.TOP_LEFT else Pos.TOP_RIGHT

        val picHeight = 15.0
        val fontSize = 15.0

        val liste = VBox().apply {
            alignment = pos
            useMaxWidth = true

            // Startelf
            for (spieler in startelf) {
                hbox(alignment = Pos.CENTER_LEFT, spacing = 5.0) {
                    hgrow = Priority.ALWAYS

                    // Name
                    label(spieler.name.trim()) {
                        font = Font.font(fontSize)

                        onHover { hovering ->
                            cursor = if (hovering) {
                                Cursor.HAND
                            } else {
                                Cursor.DEFAULT
                            }
                        }

                        // Öffnet das Fenster mit den Details zu dem Spieler
                        setOnMouseClicked {
                            val view = tornadofx.find<SpielerView>(params = mapOf("spieler" to spieler))
                            val stage = view.openWindow(resizable = false)
                            view.stage = stage
                        }
                    }

                    // Zeigt Karten des Spielers an
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

                    // Zeigt Tore des Spielers an
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
                                        ?: "") + if (tor.eigentor) "Eigentor" else "")
                            }
                        }
                    }

                    // Zeigt an, ob der Spieler ausgewechselt wurde
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

            // Eingewechselte Spieler
            for (auswechslung in auswechslungen) {
                hbox(alignment = Pos.CENTER_LEFT, spacing = 5.0) {
                    hgrow = Priority.ALWAYS

                    // Name
                    label(auswechslung.ein.name.trim()) {
                        font = Font.font(fontSize)

                        onHover { hovering ->
                            cursor = if (hovering) {
                                Cursor.HAND
                            } else {
                                Cursor.DEFAULT
                            }
                        }

                        // Öffnet ein Fenster mit den Details zu dem Spieler
                        setOnMouseClicked {
                            val view = tornadofx.find<SpielerView>(params = mapOf("spieler" to auswechslung.ein))
                            val stage = view.openWindow(resizable = false)
                            view.stage = stage
                        }
                    }

                    // Zeigt den Einwechslungspfeil an
                    val pfeil = resources.image("/resources/pfeil-gruen.png")
                    imageview(pfeil) {
                        fitHeight = picHeight
                        isSmooth = true
                        isPreserveRatio = true

                        tooltip("${auswechslung.spielminute}' (${auswechslung.aus.name})")
                    }

                    // Zeigt Karten des Spielers an
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

                    // Zeigt Tore des Spielers an
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
                                        ?: "") + if (tor.eigentor) "Eigentor" else "")
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
