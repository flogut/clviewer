package de.hgv.app

import de.hgv.view.TurnierbaumView
import javafx.application.Application
import tornadofx.App

/**
 * Hauptklasse der Anwendung clviewer.
 */
class CLViewer : App(TurnierbaumView::class)

/**
 * Main-Methode der Anwendung.
 * @param args Wird ignoriert
 */
fun main(args: Array<String>) {
    Application.launch(CLViewer::class.java)
}
