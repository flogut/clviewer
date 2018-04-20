package de.hgv.view

import de.hgv.controller.DataController
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

class SpielerView: View() {

    private val dataController: DataController by inject()
    private val providerProperty = dataController.providerProperty
    private var provider by providerProperty

    private val spielerProperty = SimpleObjectProperty((params["id"] as? String)?.let { provider.getSpieler(it) })
    private var spieler by spielerProperty

    private val name = spielerProperty.selectString { it?.name }
    private val nummer = spielerProperty.selectString { it?.nummer?.let { "#${it}" } }
    private val groesse = spielerProperty.selectString { it?.groesse?.let { "Größe: ${it}cm" } }
    private val spielfuss = spielerProperty.selectString { it?.spielfuss?.let { "Starker Fuß: ${it.capitalize()}" } }
    private val spielerBildUrl = spielerProperty.selectString { it?.portraitUrl }
    private val wappenUrl = spielerProperty.selectString { it?.verein?.wappenURL }
    private val flaggeUrl =
        spielerProperty.selectString { "http://www.nationalflaggen.de/media/flags/flagge-${it?.land?.toLowerCase()}.gif" }
    private val geburtstag = spielerProperty.selectString {
        it?.geburtstag?.let {
            "Geburtstag: ${it.format(DateTimeFormatter.ofPattern("dd. MMMM uuuu"))}"
        }
    }
    private val position = spielerProperty.selectString {
        it?.let {
            "Position" + (if (it.positionen.size > 1) "en" else "") + ": " + it.positionen.joinToString(", ")
        }
    }

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
            visibleWhen { spielerProperty.isNull }
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
                    spieler = provider.getSpieler(name.replace(" ", "-").toLowerCase())
                }
            }
            if (it.isControlDown && it.code == KeyCode.B) {
                it.consume()
                print("")
            }
        }
    }

    init {
        runLater {
            root.requestFocus()
        }

        spielerProperty.onChange {
            primaryStage.sizeToScene()
        }

        primaryStage.isResizable = false

        titleProperty.bind(name)

        spieler = provider.getSpieler("joshua-kimmich")
    }

    private fun <T> ObservableValue<T>.selectString(nested: (T) -> String?): Property<String> =
        this.select { SimpleStringProperty(nested(this.value) ?: "") }
}