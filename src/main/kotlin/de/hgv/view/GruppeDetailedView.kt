package de.hgv.view

import de.hgv.model.Phase
import de.hgv.provider.ActiveProvider
import javafx.geometry.Pos
import javafx.scene.control.TextInputDialog
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.*
import java.time.LocalDate

/**
 * Stellt die zu einer Gruppe die Tabelle und die Spiele (sortiert nach Datum) einer Saison dar. Die Gruppe und die Saison werden über die params von TornadoFX mit den Keys "phase" und "saison" übergeben
 * TODO Statt params Konstruktor verwenden?
 *
 * @author Florian Gutekunst
 */
class GruppeDetailedView: Fragment() {

    /**
     * Wird keine Saison übergeben, wird die aktuelle Saison genutzt
     */
    val saison = params["saison"] as? Int ?: LocalDate.now().let {
        if (it.isBefore(LocalDate.of(it.year, 7, 1))) {
            it.year
        } else {
            it.year - 1
        }
    }

    val phase = params["phase"] as? Phase ?: Phase.GRUPPE_A
    val tabelle = ActiveProvider.getTabelle(phase.toString(), saison)
    val spiele = ActiveProvider.getSpieleInPhase(phase, saison).sortedBy { it.datum }

    override val root = vbox {
        useMaxSize = true
        vgrow = Priority.ALWAYS

        vbox {
            useMaxSize = true
            vgrow = Priority.ALWAYS

            runAsyncWithOverlay {
                // Download der Wappen während ein Ladekreis angezeigt wird => Gibt eine Map von Verein zu Wappen zurück
                runBlocking {
                    tabelle?.tabelle
                        ?.map { it.verein }
                        ?.map { it to download(it.wappenURL) }
                        ?.map { (verein, job) -> verein to job.await() }
                        //TODO NPE abfangen => Nicht-gefunden Bild anzeigen, wenn der InputStream null ist
                        ?.map { (verein, inputStream) -> verein to Image(inputStream) }
                        ?.toMap() ?: mapOf()
                }
            } ui { wappen ->
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

                // Für eine schöne Anordnung, werden die Spiele wie eine Tabelle dargestellt
                gridpane {
                    hgap = 10.0
                    vgap = 3.0
                    alignment = Pos.TOP_CENTER

                    for (spiel in spiele) {
                        row {
                            // Wappen Heimmannschaft
                            imageview(wappen.getValue(spiel.daheim)) {
                                fitWidth = 35.0
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
            }
        }

        //TODO Entfernen (Nur für Navigation während des Debuggings)
        setOnKeyPressed {
            if (it.isControlDown && it.code == KeyCode.F) {
                it.consume()

                val dialog = TextInputDialog()
                dialog.headerText = "Gruppe auswählen"
                dialog.title = "Gruppe auswählen"

                val result = dialog.showAndWait()
                result.ifPresent {
                    val phase = Phase.getValue(it)
                    this@GruppeDetailedView.replaceWith(
                        find<GruppeDetailedView>(mapOf("phase" to phase, "saison" to saison)),
                        ViewTransition.Fade(0.15.seconds)
                    )
                }
            } else if (it.isControlDown && it.code == KeyCode.S) {
                it.consume()

                val dialog = TextInputDialog()
                dialog.headerText = "Saison auswählen"
                dialog.title = "Saison auswählen"

                val result = dialog.showAndWait()
                result.ifPresent {
                    val saison = it.toIntOrNull()
                    this@GruppeDetailedView.replaceWith(
                        find<GruppeDetailedView>(mapOf("phase" to phase, "saison" to saison)),
                        ViewTransition.Fade(0.15.seconds)
                    )
                }
            }
        }

        runLater {
            requestFocus()
        }
    }

    init {
        primaryStage.isMaximized = true

        title = "$phase (Saison ${saison - 1}/$saison)"
    }
}