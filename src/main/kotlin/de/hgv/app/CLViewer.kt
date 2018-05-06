package de.hgv.app

import de.hgv.provider.ActiveProvider
import de.hgv.view.SpielerView
import tornadofx.*

class CLViewer: App(SpielerView::class, Styles::class)

fun main(args: Array<String>) {
    val spiele = ActiveProvider.getSpiele(2017)
    val max =
        spiele
            .asSequence()
            .flatMap { it.details?.tore?.map { it.torschuetze }?.asSequence()?.also { println(it) } ?: emptySequence() }
            .groupingBy { it }
            .eachCount()
            .maxBy { it.value }

    println(max)
}