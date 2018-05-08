package de.hgv.view

import de.hgv.provider.ActiveProvider
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.TextInputDialog
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.RowConstraints
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.*
import java.net.URL
import java.time.format.DateTimeFormatter

class SpielerView: Fragment() {

    val id = params["id"] as? String ?: "joshua-kimmich"
    val spieler = ActiveProvider.getSpieler(id)


    override val root = vbox(alignment = Pos.TOP_CENTER) {
        vbox(alignment = Pos.TOP_CENTER) {
            runAsyncWithOverlay {
                val urls = listOf(
                    spieler?.details?.verein?.wappenURL,
                    "http://www.nationalflaggen.de/media/flags/flagge-${spieler?.details?.land?.toLowerCase()}.gif",
                    spieler?.details?.portraitUrl
                )

                runBlocking {
                    urls
                        .map {
                            async {
                                URL(it).openConnection().apply {
                                    setRequestProperty(
                                        "user-agent",
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0"
                                    )
                                }.getInputStream()
                            }
                        }
                        .map { it.await() }
                        .map { Image(it) }
                }
            } ui { bilder ->
                gridpane {
                    alignment = Pos.TOP_CENTER

                    imageview(bilder[0]) {
                        useMaxWidth = true
                        fitHeight = 100.0
                        isPreserveRatio = true
                        isSmooth = true
                        alignment = Pos.CENTER

                        gridpaneConstraints {
                            columnRowIndex(0, 0)
                            hAlignment = HPos.CENTER
                            vAlignment = VPos.CENTER
                        }
                    }

                    imageview(bilder[1]) {
                        useMaxWidth = true
                        fitHeight = 100.0
                        isPreserveRatio = true
                        isSmooth = true

                        gridpaneConstraints {
                            columnRowIndex(0, 1)
                            vAlignment = VPos.CENTER
                            hAlignment = HPos.CENTER
                        }
                    }

                    label("#${spieler?.details?.nummer}") {
                        font = Font(50.0)
                        textAlignment = TextAlignment.CENTER

                        gridpaneConstraints {
                            columnRowIndex(0, 2)
                            hAlignment = HPos.CENTER
                            vAlignment = VPos.CENTER
                        }
                    }

                    imageview(bilder[2]) {
                        fitHeight = 400.0
                        isPreserveRatio = true
                        isSmooth = true

                        gridpaneConstraints {
                            columnRowIndex(1, 0)
                            rowSpan = 3
                        }
                    }

                    repeat(3) {
                        rowConstraints.add(RowConstraints().apply { percentHeight = 100 / 3.0 })
                    }
                }

                label(spieler?.name.orEmpty()) {
                    font = Font(30.0)
                    textAlignment = TextAlignment.CENTER
                    useMaxWidth = true
                    alignment = Pos.CENTER
                }

                val eigenschaften = listOf(
                    "Geburtstag: " to spieler?.details?.geburtstag?.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")),
                    "Größe: " to spieler?.details?.groesse?.toString() + "cm",
                    "Starker Fuß: " to spieler?.details?.spielfuss?.capitalize(),
                    "Position" + if (spieler?.details?.positionen?.size?.compareTo(2) ?: 0 >= 0) {
                        "en"
                    } else {
                        ""
                    } + ": " to spieler?.details?.positionen?.joinToString(", ")
                )

                for (eigenschaft in eigenschaften.filterNot { it.second == null }) {
                    label(eigenschaft.first + eigenschaft.second) {
                        font = Font(20.0)
                        useMaxWidth = true
                        paddingLeft = 5.0
                    }
                }

                primaryStage.sizeToScene()
                primaryStage.centerOnScreen()
            }

            setOnKeyPressed {
                if (it.isControlDown && it.code == KeyCode.F) {
                    it.consume()
                    val dialog = TextInputDialog()
                    dialog.headerText = "Spieler auswählen"
                    dialog.title = "Spieler auswählen"

                    val result = dialog.showAndWait()
                    result.ifPresent { name ->
                        this@SpielerView.replaceWith(
                            find<SpielerView>(mapOf("id" to name.replace(" ", "-").toLowerCase())),
                            ViewTransition.Fade(15.millis)
                        )

                    }
                }
            }

            runLater {
                requestFocus()
            }
        }
    }

    init {
        primaryStage.isResizable = false

        title = spieler?.name.orEmpty()
    }
}
