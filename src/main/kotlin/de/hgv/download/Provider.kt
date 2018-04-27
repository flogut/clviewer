package de.hgv.download

interface Provider {

    /**
     * getSpieler parst die Daten zu einem Spieler
     * @param id ID des Spielers
     */
    fun getSpieler(id: String): Spieler?

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 17/18 => 18)
     * @param phase Phase, in der das Spiel stattfindet (Gruppe A, ..., Gruppe F, Achtelfinale, Viertelfinale, Halbfinale, Finale)
     * @param daheim Heimmannschaft
     * @param auswaerts Auswärtsmannschaft
     */
    fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein): Spiel?

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param link Letzter Teil des Links zur Spielübersicht
     */
    fun getSpiel(link: String): Spiel?


}