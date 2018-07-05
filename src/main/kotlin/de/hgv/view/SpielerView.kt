package de.hgv.view

import de.hgv.model.Spieler
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.image.Image
import javafx.scene.layout.RowConstraints
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.*
import java.time.format.DateTimeFormatter
import kotlin.collections.set

/**
 * Stellt die Details zu einem Spieler dar. Der Spielers wird über die params von TornadoFX übergeben mit dem
 * Key "spieler".
 *
 * @author Florian Gutekunst
 */
class SpielerView: Fragment() {

    val spieler = params["spieler"] as Spieler
    var stage: Stage? = null

    override val root = scrollpane {
        vbox(alignment = Pos.TOP_CENTER) {
            vbox(alignment = Pos.TOP_CENTER) {
                runAsyncWithOverlay {
                    if (spieler.details == null) {
                        return@runAsyncWithOverlay mutableMapOf<String, Image>()
                    }

                    // Download der Wappen während ein Ladekreis angezeigt wird => Gibt eine Map von Verein zu Wappen zurück
                    val urls = listOf(
                        "flagge" to "http://www.nationalflaggen.de/media/flags/flagge-${spieler.details?.getLandForLink()}.gif",
                        "portrait" to spieler.details?.portraitUrl
                    )

                    val bilder = runBlocking {
                        urls.map { it.first to Download.downloadAsync(it.second) }
                            .map { it.first to it.second.await() }
                            .filter { it.second != null }
                            .map { it.first to Image(it.second) }
                            .toMap().toMutableMap()
                    }

                    bilder["wappen"] = Download.downloadWappen(spieler.details?.verein)

                    bilder
                } ui { bilder ->
                    if (spieler.details == null) {
                        error(
                            "Informationen zum Spieler konnten nicht geladen werden",
                            "Detaillierte Informationen sind nicht verfügbar. Dieser hat vermutlich seine Karriere " +
                                    "beendet oder ist momentan vertragslos."
                        )

                        runLater {
                            stage?.close()
                        }
                    } else {
                        gridpane {
                            alignment = Pos.TOP_CENTER

                            // Vereinswappen
                            if (bilder["wappen"] != null) {
                                imageview(bilder.getValue("wappen")) {
                                    useMaxWidth = true
                                    fitHeight = 100.0
                                    isPreserveRatio = true
                                    isSmooth = true
                                    alignment = Pos.CENTER

                                    tooltip(spieler.details?.verein?.name)

                                    gridpaneConstraints {
                                        columnRowIndex(0, 0)
                                        hAlignment = HPos.CENTER
                                        vAlignment = VPos.CENTER
                                    }
                                }
                            } else {
                                label(spieler.details?.verein?.name.orEmpty()) {
                                    gridpaneConstraints {
                                        columnRowIndex(0, 0)
                                        hAlignment = HPos.CENTER
                                        vAlignment = VPos.CENTER
                                    }
                                }
                            }

                            // Flagge
                            if (bilder["flagge"] != null) {
                                imageview(bilder.getValue("flagge")) {
                                    useMaxWidth = true
                                    fitHeight = 100.0
                                    isPreserveRatio = true
                                    isSmooth = true

                                    tooltip(spieler.details?.land)

                                    gridpaneConstraints {
                                        columnRowIndex(0, 1)
                                        vAlignment = VPos.CENTER
                                        hAlignment = HPos.CENTER
                                    }
                                }
                            } else {
                                label(spieler.details?.land.orEmpty()) {
                                    gridpaneConstraints {
                                        columnRowIndex(0, 1)
                                        vAlignment = VPos.CENTER
                                        hAlignment = HPos.CENTER
                                    }
                                }
                            }

                            // Rückennummer
                            label(spieler.details?.nummer?.let { "#$it" }.orEmpty()) {
                                font = Font(50.0)
                                textAlignment = TextAlignment.CENTER

                                gridpaneConstraints {
                                    columnRowIndex(0, 2)
                                    hAlignment = HPos.CENTER
                                    vAlignment = VPos.CENTER
                                }
                            }

                            // Spieler-Portrait
                            imageview(bilder.getValue("portrait")) {
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
                        label(spieler.name) {
                            font = Font(30.0)
                            textAlignment = TextAlignment.CENTER
                            useMaxWidth = true
                            alignment = Pos.CENTER
                        }

                        // Liste der Eigenschaften eines Spielers als Paar von Eigenschatfsname und Wert
                        val eigenschaften = listOf(
                            "Geburtstag: " to spieler.details?.geburtstag?.format(
                                DateTimeFormatter.ofPattern("dd. MMMM yyyy")
                            ),
                            "Größe: " to spieler.details?.groesse?.let { it.toString() + "cm" },
                            "Starker Fuß: " to spieler.details?.spielfuss?.capitalize(),
                            "Position" + if (spieler.details?.positionen?.size?.compareTo(2) ?: 0 >= 0) {
                                "en"
                            } else {
                                ""
                            } + ": " to spieler.details?.positionen?.joinToString(", ")
                        )

                        // Zeigt die Eigenschaften an, deren Wert bekannt ist
                        for (eigenschaft in eigenschaften.filterNot { it.second == null }) {
                            label(eigenschaft.first + eigenschaft.second) {
                                font = Font(20.0)
                                useMaxWidth = true
                                paddingLeft = 5.0
                            }
                        }

                        runLater {
                            resize()
                        }
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
        title = spieler.name
    }
}
