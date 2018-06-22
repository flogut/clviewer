package de.hgv.model

import de.hgv.provider.ActiveProvider
import tornadofx.*
import kotlin.collections.set

/**
 * @author Tobias Döttling, Florian Gutekunst
 */
class KoSpiele(val saison:Int) {

    private val alleSpiele = ActiveProvider.getSpiele(saison).groupBy { it.phase }
    private val turnierbaum = mutableMapOf<Phase, List<List<Spiel>>>()

    /**
     * Baut den Turnierbaum.
     * @return Den Turnierbaum als Map, in der jeder Phase eine Liste von Listen von Spielen zugeordnet wird, die je
     * die Spiele einer Paarung enthalten.
     */
    fun getTurnierbaum(): Map<Phase, List<List<Spiel>>> {
        getTurnierbaum(Phase.ACHTELFINALE, mutableListOf())

        return turnierbaum
    }

    //rekursive Methode, die eine MutableList des Tunierbaums zurückgibt
    private fun getTurnierbaum(phase: Phase?, letztePhase: MutableList<List<Spiel>>): MutableList<List<Spiel>> {

        if (phase == null) {
            return letztePhase
        }

        val spiele = alleSpiele[phase] ?: return letztePhase

        //Spiele in Paarungen aufteilen
        var paarungen = mutableListOf<List<Spiel>>()
        for (spiel in spiele) {
            if (paarungen.any { spiel in it }) {
                continue
            }

            val rueckspiel = spiele.find { it.daheim == spiel.auswaerts }
            paarungen.add(listOfNotNull(spiel, rueckspiel))
        }

        if (paarungen.isNotEmpty()) {
            //rekursiver Aufruf:
            paarungen = getTurnierbaum(phase.naechste(), paarungen)
        }

        //Vertauschen der Spiele, damit die Spiele in der korrekten Reihenfolge angezeigt werden
        if (letztePhase.isNotEmpty()) {
            paarungen.forEachIndexed { index, paarung ->
                val vereine = paarung[0].daheim to paarung[0].auswaerts

                letztePhase.swap(2 * index, letztePhase.indexOfFirst { it.any { it.daheim == vereine.first } })
                letztePhase.swap(2 * index + 1, letztePhase.indexOfFirst { it.any { it.daheim == vereine.second } })
            }
        }
        turnierbaum[phase] = paarungen

        return letztePhase
    }
}
