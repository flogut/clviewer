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
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.*
import java.time.format.DateTimeFormatter

/**
 * Stellt die Details zu einem Spieler dar. Die ID des Spielers wird über die params von TornadoFX übergeben mit dem Key "id". <br>
 * TODO Statt params Konstruktor verwenden?
 *
 * @author Florian Gutekunst
 */
class SpielerView: Fragment() {

    val id = params["id"] as? String ?: "joshua-kimmich"
    val spieler = ActiveProvider.getSpieler(id)


    override val root = vbox(alignment = Pos.TOP_CENTER) {
        vbox(alignment = Pos.TOP_CENTER) {
            runAsyncWithOverlay {
                //Download der Wappen während ein Ladekreis angezeigt wird => Gibt eine Map von Verein zu Wappen zurück
                val urls = listOf(
                    spieler?.details?.verein?.wappenURL,
                    "http://www.nationalflaggen.de/media/flags/flagge-${spieler?.details?.land?.toLowerCase()}.gif",
                    spieler?.details?.portraitUrl
                )

                runBlocking {
                    urls.map { download(it) }
                        .map { it.await() }
                        //TODO NPE abfangen => Nicht-gefunden Bild anzeigen, wenn der InputStream null ist
                        .map { Image(it) }
                }
            } ui { bilder ->
                gridpane {
                    alignment = Pos.TOP_CENTER

                    // Vereinswappen
                    imageview(bilder[0]) {
                        useMaxWidth = true
                        fitHeight = 100.0
                        isPreserveRatio = true
                        isSmooth = true
                        alignment = Pos.CENTER

                        tooltip(spieler?.details?.verein?.name)

                        gridpaneConstraints {
                            columnRowIndex(0, 0)
                            hAlignment = HPos.CENTER
                            vAlignment = VPos.CENTER
                        }
                    }

                    // Flagge
                    imageview(bilder[1]) {
                        useMaxWidth = true
                        fitHeight = 100.0
                        isPreserveRatio = true
                        isSmooth = true

                        tooltip(spieler?.details?.land)

                        gridpaneConstraints {
                            columnRowIndex(0, 1)
                            vAlignment = VPos.CENTER
                            hAlignment = HPos.CENTER
                        }
                    }

                    // Rückennummer
                    label("#${spieler?.details?.nummer}") {
                        font = Font(50.0)
                        textAlignment = TextAlignment.CENTER

                        gridpaneConstraints {
                            columnRowIndex(0, 2)
                            hAlignment = HPos.CENTER
                            vAlignment = VPos.CENTER
                        }
                    }

                    // Spieler-Portrait
                    imageview(bilder[2]) {
                        fitHeight = 400.0
                        isPreserveRatio = true
                        isSmooth = true

                        gridpaneConstraints {
                            columnRowIndex(1, 0)
                            rowSpan = 3
                        }
                    }

                    // Höhe der Zeilen auf je 33% setzen
                    repeat(3) {
                        rowConstraints.add(RowConstraints().apply { percentHeight = 100 / 3.0 })
                    }
                }

                // Name
                label(spieler?.name.orEmpty()) {
                    font = Font(30.0)
                    textAlignment = TextAlignment.CENTER
                    useMaxWidth = true
                    alignment = Pos.CENTER
                }

                // Liste der Eigenschaften eines Spielers als Paar von Eigenschatfsname und Wert
                val eigenschaften = listOf(
                    "Geburtstag: " to spieler?.details?.geburtstag?.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")),
                    "Größe: " to spieler?.details?.groesse?.let { it.toString() + "cm" },
                    "Starker Fuß: " to spieler?.details?.spielfuss?.capitalize(),
                    "Position" + if (spieler?.details?.positionen?.size?.compareTo(2) ?: 0 >= 0) {
                        "en"
                    } else {
                        ""
                    } + ": " to spieler?.details?.positionen?.joinToString(", ")
                )

                // Zeigt die Eigenschaften an, deren Wert bekannt ist
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

            // TODO Entfernen (Nur für Navigation während des Debuggings)
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
