package de.hgv.view

import de.hgv.provider.ActiveProvider
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*
import java.time.format.DateTimeFormatter

class SpielerViewOld: View() {

    private val spielerProperty = SimpleObjectProperty((params["id"] as? String)?.let { ActiveProvider.getSpieler(it) })
    private var spieler by spielerProperty

    private val name = spielerProperty.selectString { spieler -> spieler?.name }
    private val nummer =
        spielerProperty.selectString { spieler -> spieler?.details?.nummer?.let { nummer -> "#$nummer" } }
    private val spielerBildUrl = spielerProperty.selectString { spieler -> spieler?.details?.portraitUrl }
    private val wappenUrl = spielerProperty.selectString { spieler -> spieler?.details?.verein?.wappenURL }
    private val groesse = getGroesse()
    private val spielfuss = getSpielfuss()
    private val flaggeUrl = getFlaggeUrl()
    private val geburtstag = getGeburtstag()
    private val position = getPosition()

    override val root = pane {
        useMaxSize = true

        vbox {
            paddingLeft = 5
            visibleWhen { spielerProperty.isNotNull }
            //TODO Responsive layout => Geringe Auflösung der Schulrechner

            borderpane {
                left = vbox(spacing = 35.0, alignment = Pos.BASELINE_CENTER) {
                    imageview(wappenUrl, false) {
                        useMaxWidth = true
                        fitHeight = 100.0
                        isPreserveRatio = true
                        isSmooth = true
                        isCache = true
                    }

                    imageview(flaggeUrl, false) {
                        useMaxWidth = true
                        fitHeight = 90.0
                        isPreserveRatio = true
                        isSmooth = true
                        isCache = true
                        paddingAll = 10
                    }

                    label(nummer) {
                        font = Font(50.0)
                        textAlignment = TextAlignment.CENTER
                    }
                }

                center {
                    imageview(spielerBildUrl, false) {
                        fitHeight = 400.0
                        isPreserveRatio = true
                        isSmooth = true
                    }
                }
            }

            label(name) {
                font = Font(30.0)
                textAlignment = TextAlignment.CENTER
                useMaxWidth = true
                alignment = Pos.CENTER
            }


            for (property in listOf(geburtstag, groesse, spielfuss, position)) {
                label(property) {
                    font = Font(20.0)
                    useMaxWidth = true
                    removeWhen { property.isBlank() }
                }
            }
        }

        label("Spieler konnte nicht geladen werden") {
            removeWhen { spielerProperty.isNotNull }
            alignment = Pos.CENTER
            useMaxSize = true
            textAlignment = TextAlignment.CENTER
            font = Font(20.0)
        }

        setOnKeyPressed {
            if (it.isControlDown && it.code == KeyCode.F) {
                it.consume()
                val dialog = TextInputDialog()
                dialog.headerText = "Spieler auswählen"
                dialog.title = "Spieler auswählen"

                val result = dialog.showAndWait()
                result.ifPresent { name ->
                    spieler = ActiveProvider.getSpieler(name.replace(" ", "-").toLowerCase())
                }
            }
            if (it.isControlDown && it.code == KeyCode.B) {
                it.consume()
                print("")
            }
        }
    }

    init {
        primaryStage.isResizable = false
        titleProperty.bind(name)

        spielerProperty.onChange {
            primaryStage.sizeToScene()
        }

        spieler = ActiveProvider.getSpieler("joshua-kimmich")

        runLater {
            root.requestFocus()
        }
    }

    private fun getGroesse(): Property<String> =
        spielerProperty.selectString { spieler -> spieler?.details?.groesse?.let { groesse -> "Größe: ${groesse}cm" } }

    private fun getSpielfuss(): Property<String> =
        spielerProperty.selectString { spieler -> spieler?.details?.spielfuss?.let { spielfuss -> "Starker Fuß: ${spielfuss.capitalize()}" } }

    private fun getFlaggeUrl(): Property<String> =
        spielerProperty.selectString { "http://www.nationalflaggen.de/media/flags/flagge-${it?.details?.land?.toLowerCase()}.gif" }

    private fun getGeburtstag(): Property<String> = spielerProperty.selectString { spieler ->
        spieler?.details?.geburtstag?.let { geburtstag ->
            "Geburtstag: " + geburtstag.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy"))
        }
    }

    private fun getPosition() = spielerProperty.selectString { spieler ->
        spieler?.details?.positionen?.let { positionen ->
            "Position" + (if (positionen.size > 1) "en" else "") + ": " + positionen.joinToString(", ")
        }
    }

    private fun <T> ObservableValue<T>.selectString(nested: (T) -> String?): Property<String> =
        this.select { SimpleStringProperty(nested(this.value) ?: "") }
}