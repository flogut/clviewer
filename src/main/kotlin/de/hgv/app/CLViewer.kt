package de.hgv.app

import de.hgv.view.SpielerView
import javafx.application.Application
import tornadofx.*

class CLViewer: App(SpielerView::class, Styles::class)

fun main(args: Array<String>) {
    Application.launch(CLViewer::class.java)
}