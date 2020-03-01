package de.hgv.app

import de.hgv.view.TurnierbaumView
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.control.Tooltip
import javafx.stage.Stage
import tornadofx.*

/**
 * Hauptklasse der Anwendung clviewer.
 */
class CLViewer : App(TurnierbaumView::class) {
    override fun start(stage: Stage) {
        hackTooltipStartTiming()

        super.start(stage)
    }

    /**
     * Setzt die Verzögerung, nach der das Tooltip erscheint, herab.
     * s. https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay
     */
    private fun hackTooltipStartTiming() {
        try {
            val tooltip = Tooltip()
            val fieldBehavior = tooltip::class.java.getDeclaredField("BEHAVIOR")
            fieldBehavior.isAccessible = true
            val objBehavior = fieldBehavior.get(tooltip)

            val fieldTimer = objBehavior::class.java.getDeclaredField("activationTimer")
            fieldTimer.isAccessible = true
            val objTimer = fieldTimer.get(objBehavior) as Timeline

            objTimer.keyFrames.clear()
            objTimer.keyFrames.add(KeyFrame(50.millis))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Main-Methode der Anwendung.
 * @param args Wird ignoriert
 */
fun main(args: Array<String>) {
    Application.launch(CLViewer::class.java)
}
