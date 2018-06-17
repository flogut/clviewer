package de.hgv.app

import de.hgv.view.SpielerView
import javafx.application.Application
import tornadofx.App

/**
 * Hauptklasse der Anwendung clviewer.
 */
class CLViewer: App(SpielerView::class)

/**
 * Main-Methode der Anwendung.
 * @param args Wird ignoriert
 */
fun main(args: Array<String>) {
    Application.launch(CLViewer::class.java)
}
