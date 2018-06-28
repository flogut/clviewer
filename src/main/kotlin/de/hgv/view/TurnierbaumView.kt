package de.hgv.view

import de.hgv.model.Phase
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.Fragment
import tornadofx.ViewTransition
import tornadofx.action
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.item
import tornadofx.label
import tornadofx.menu
import tornadofx.menubar
import tornadofx.millis
import tornadofx.observable
import tornadofx.onChange
import tornadofx.runAsyncWithOverlay
import tornadofx.useMaxSize
import tornadofx.useMaxWidth
import tornadofx.vbox
import tornadofx.vgrow
import java.time.LocalDate

/**
 * Stellt den Turnierbaum dar. Die Saison wird über die Params mit dem Key "saison" übergeben.
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

    override val root = vbox {
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

                add(turnierbaum)
            }
        }
    }

    init {
        title = "Champions League Saison ${saison - 1}/$saison"

        primaryStage.isMaximized = true

        saisonProptery.onChange { neu ->
            val turnierbaumNeu = find<TurnierbaumView>(mapOf("saison" to neu))
            this.replaceWith(turnierbaumNeu, ViewTransition.Fade(15.millis))
        }
    }
}
