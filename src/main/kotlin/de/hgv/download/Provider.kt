package de.hgv.download

interface Provider {

    /**
     * getSpieler parst alle Daten zu einem Spieler
     * @param id ID des Spielers
     */
    fun getSpieler(id: String): Spieler?

    /**
     * getDetailsForSpieler parst die Details zu einem Spieler
     * @param spieler Spieler, dessen Details geparst werden sollen
     */
    fun getDetailsForSpieler(spieler: Spieler): Spieler.Details?

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     * @param phase Phase, in der das Spiel stattfindet (Gruppe A, ..., Gruppe F, Achtelfinale, Viertelfinale, Halbfinale, Finale)
     * @param daheim Heimmannschaft
     * @param auswaerts Auswärtsmannschaft
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     */
    fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein, detailed: Boolean): Spiel?

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param link Letzter Teil des Links zur Spielübersicht
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     */
    fun getSpiel(link: String, detailed: Boolean): Spiel?

    /**
     * getDetailsForSpiel parst die Details zu einem Spiel (Aufstellungen, Torschützen)
     * @param spiel Spiel, dessen Details geparst werden sollen
     */
    fun getDetailsForSpiel(spiel: Spiel): Spiel.Details?

    /**
     * getSpiele lädt eine Liste aller Spiele einer Saison (ohne Aufstellungen, Torschützen => Spiel.Details)
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     */
    fun getSpiele(saison: Int): List<Spiel>

    /**
     * getTabelle lädt die Tabelle einer Gruppe herunter
     * @param gruppe Gruppenname (z.B. "Gruppe A")
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     */
    fun getTabelle(gruppe: String, saison: Int): Tabelle

}