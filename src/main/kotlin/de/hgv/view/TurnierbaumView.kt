package de.hgv.view

import de.hgv.model.Phase
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.*
import java.time.LocalDate

/**
 * Stellt den Turnierbaum dar. Die Saison wird über die Params mit dem Key "saison" übergeben.
 *
 * @author Tobias Döttling
 */
class TurnierbaumView : Fragment() {
    val saison = params["saison"] as? Int ?: aktuelleSaison()
    private val saisonProptery = SimpleIntegerProperty(saison)
    private val jahre = (aktuelleSaison() downTo 2004).toList().observable()

    override val root = vbox {

        style += "-fx-background-image: url(\"/resources/background-image.jpg\"); -fx-background-size: cover;"
        useMaxSize = true

        menubar {
            for (gruppe in Phase.values().filter { it.bevor(Phase.ACHTELFINALE) }) {
                menu(gruppe.toString()) {
                    item(gruppe.toString()) {
                        action {
                            val view = find<GruppeDetailedView>(mapOf("phase" to gruppe, "saison" to saison))
                            val stage = view.openWindow(resizable = false)
                            view.stage = stage
                        }
                    }

                    setOnShown {
                        items[0].fire()
                    }
                }
            }
        }

        vbox {
            useMaxSize = true
            vgrow = Priority.ALWAYS

            runAsyncWithOverlay {
                buildTurnierbaum(saison) {
                    alignment = Pos.CENTER
                    useMaxSize = true
                }
            } ui { turnierbaum ->
                vbox {
                    useMaxSize = true
                    alignment = Pos.TOP_CENTER

                    label("Turnierbaum") {
                        useMaxWidth = true
                        alignment = Pos.CENTER
                        font = Font.font(font.family, FontWeight.BOLD, 30.0)
                        textFill = c("#FFFFFF")
                    }

                    hbox {
                        alignment = Pos.CENTER
                        label("Saison:  "){
                            textFill = c("#FFFFFF")
                        }

                        combobox(saisonProptery, jahre){
                            cellFormat {
                                text = "${item.toInt() - 1}/$item"
                            }
                        }
                    }
                }

                add(turnierbaum)
            }
        }

    }

    init {
        title = "Champions League Saison ${saison - 1}/$saison"

        primaryStage.isMaximized = true

        saisonProptery.onChange { neu ->
            val turnierbaumNeu = find<TurnierbaumView>(mapOf("saison" to neu))
            this.replaceWith(turnierbaumNeu, ViewTransition.Fade(1000.millis))
        }
    }


    /**
     * Gibt die aktuelle Saison zurück. Stichtag ist der 1. September, da dann die Auslosung der Gruppenphase bereits
     * stattgefunden hat.
     */
    private fun aktuelleSaison(): Int = LocalDate.now().let {
        if (it.isBefore(LocalDate.of(it.year, 9, 1))) {
            it.year
        } else {
            it.year + 1
        }
    }
}
