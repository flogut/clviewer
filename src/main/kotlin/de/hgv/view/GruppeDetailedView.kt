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
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.*
import java.net.URL

class GruppeDetailedView: Fragment() {

    val phase = params["phase"] as? Phase ?: Phase.GRUPPE_A
    val tabelle = ActiveProvider.getTabelle(phase.toString(), 2018)
    val spiele = ActiveProvider.getSpiele(2018).filter { it.phase == phase }.sortedBy { it.datum }.observable()

    override val root = vbox {
        useMaxSize = true
        vgrow = Priority.ALWAYS

        vbox {
            useMaxSize = true
            vgrow = Priority.ALWAYS

            runAsyncWithOverlay {
                runBlocking {
                    tabelle?.tabelle
                        ?.map { it.verein }
                        ?.map {
                            it to async {
                                URL(it.wappenURL).openConnection().apply {
                                    setRequestProperty(
                                        "user-agent",
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0"
                                    )
                                }.getInputStream()
                            }
                        }
                        ?.map { (verein, job) -> verein to job.await() }
                        ?.map { (verein, inputStream) -> verein to Image(inputStream) }
                        ?.toMap() ?: mapOf()
                }
            } ui { wappen ->
                label("Tabelle") {
                    useMaxWidth = true
                    textAlignment = TextAlignment.CENTER
                    alignment = Pos.TOP_CENTER
                    font = Font.font(font.family, FontWeight.BOLD, 30.0)
                }

                gridpane {
                    hgap = 10.0
                    vgap = 3.0
                    alignment = Pos.TOP_CENTER

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
                            label(zeile.platz.toString())

                            imageview(wappen.getValue(zeile.verein)) {
                                fitWidth = 25.0
                                isPreserveRatio = true
                                isSmooth = true
                            }

                            label(zeile.verein.name)

                            label(zeile.tordifferenz.toString())

                            label(zeile.punkte.toString())
                        }
                    }
                }

                label("Spiele") {
                    useMaxWidth = true
                    textAlignment = TextAlignment.CENTER
                    alignment = Pos.TOP_CENTER
                    font = Font.font(font.family, FontWeight.BOLD, 30.0)
                }

                gridpane {
                    hgap = 10.0
                    vgap = 3.0
                    alignment = Pos.TOP_CENTER

                    for (spiel in spiele) {
                        row {
                            imageview(wappen.getValue(spiel.daheim)) {
                                fitWidth = 35.0
                                isPreserveRatio = true
                                isSmooth = true
                            }

                            label(spiel.daheim.name)

                            label(spiel.toreHeim.toString()) {
                                paddingLeft = 10
                            }

                            label(" : ")

                            label(spiel.toreAuswaerts.toString()) {
                                paddingRight = 10
                            }

                            label(spiel.auswaerts.name) {
                                useMaxWidth = true
                                textAlignment = TextAlignment.RIGHT
                                alignment = Pos.CENTER_RIGHT
                            }

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
                        find<GruppeDetailedView>(mapOf("phase" to phase)),
                        ViewTransition.Fade(0.15.seconds)
                    )
                }
            }
        }
    }

    init {
        primaryStage.isMaximized = true

        runLater {
            root.requestFocus()
        }

        title = phase.toString()
    }
}