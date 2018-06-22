package de.hgv.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.Fragment
import tornadofx.ViewTransition
import tornadofx.borderpane
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.label
import tornadofx.millis
import tornadofx.observable
import tornadofx.onChange
import tornadofx.pane
import tornadofx.runAsyncWithOverlay
import tornadofx.useMaxWidth
import tornadofx.vbox
import java.time.LocalDate

/**
 * Stellt den Turnierbaum dar. Hauptbildschirm der Anwendung.
 *
 * @author Tobias Döttling
 */
class TurnierbaumView : Fragment() {
    val saison = params["saison"] as? Int ?: LocalDate.now().let {
        if (it.isBefore(LocalDate.of(it.year, 7, 1))) {
            it.year
        } else {
            it.year + 1
        }
    }
    private val saisonProptery = SimpleIntegerProperty(saison)
    // TODO Akutelle Saison statt 2018 verwnden
    private val jahre = (2018 downTo 2004).toList().observable()

    override val root = pane {
        borderpane {
            runAsyncWithOverlay {
                buildTurnierbaum(saison)
            } ui { turnierbaum ->
                top = vbox {
                    label("Turnierbaum") {
                        useMaxWidth = true
                        alignment = Pos.CENTER
                        font = Font.font(font.family, FontWeight.BOLD, 30.0)
                    }
                    hbox {
                        alignment = Pos.CENTER
                        label("Saison:  ")

                        combobox(saisonProptery, jahre) {
                            cellFormat {
                                text = "${item.toInt() - 1}/$item"
                            }
                        }
                    }
                }

                center = turnierbaum
            }
        }
    }

    init {
        primaryStage.isMaximized = true

        saisonProptery.onChange { neu ->
            val turnierbaumNeu = find<TurnierbaumView>(mapOf("saison" to neu))
            this.replaceWith(turnierbaumNeu, ViewTransition.Fade(15.millis))
        }
    }
}
