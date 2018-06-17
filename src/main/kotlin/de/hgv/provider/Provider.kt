package de.hgv.provider

import de.hgv.model.Phase
import de.hgv.model.Spiel
import de.hgv.model.Spieler
import de.hgv.model.Tabelle
import de.hgv.model.Verein

/**
 * Definiert die Methoden für den Zugriff auf die benötigten Daten.
 */
interface Provider {

    /**
     * getSpieler parst alle Daten zu einem Spieler.
     * @param id ID des Spielers
     * @return Den Spieler, oder null, wenn die ID ungültig ist
     */
    fun getSpieler(id: String): Spieler?

    /**
     * getDetailsForSpieler parst die Details zu einem Spieler.
     * @param spieler Spieler, dessen Details geparst werden sollen
     * @return Die Details, oder null, wenn ein Fehler auftritt
     */
    fun getDetailsForSpieler(spieler: Spieler): Spieler.Details?

    /**
     * getSpiel parst die Daten zu einem Spiel.
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     * @param phase Phase, in der das Spiel stattfindet (Gruppe A, ..., Gruppe F, Achtelfinale, Viertelfinale,
     * Halbfinale, Finale)
     * @param daheim Heimmannschaft
     * @param auswaerts Auswärtsmannschaft
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     * @return Das Spiel, oder null, wenn ein Fehler auftritt
     */
    fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein, detailed: Boolean): Spiel?

    /**
     * getSpiel parst die Daten zu einem Spiel.
     * @param link Letzter Teil des Links zur Spielübersicht
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     * @return Das Spiel, oder null, wenn ein Fehler auftritt
     */
    fun getSpiel(link: String, detailed: Boolean): Spiel?

    /**
     * getDetailsForSpiel parst die Details zu einem Spiel (Aufstellungen, Torschützen).
     * @param spiel Spiel, dessen Details geparst werden sollen
     * @return Die Details, oder null, wenn ein Fehler auftritt
     */
    fun getDetailsForSpiel(spiel: Spiel): Spiel.Details?

    /**
     * getSpiele lädt eine Liste aller Spiele einer Saison (ohne Aufstellungen, Torschützen => Spiel.Details).
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     */
    fun getSpiele(saison: Int): List<Spiel>

    /**
     * getSpieleInPhase lädt eine List aller Spiele einer Phase einer Saison herunter
     * (ohne Aufstellungen, Torschützen => Spiel.Details).
     * @param phase Phase der Saison
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     */
    fun getSpieleInPhase(phase: Phase, saison: Int): List<Spiel> = getSpiele(saison).filter { it.phase == phase }

    /**
     * getTabelle lädt die Tabelle (Platzierung, Tordifferenz, Punktzahl) einer Gruppe aus der Gruppenphase herunter.
     * @param gruppe Name der Gruppe (z.B. "Gruppe A")
     * @param saison Jahr, in dem das Finale stattfindet (z.B. 2017/2018 => 2018)
     * @return Die Tabelle, oder null, wenn ein Fehler auftritt
     */
    fun getTabelle(gruppe: String, saison: Int): Tabelle?
}
