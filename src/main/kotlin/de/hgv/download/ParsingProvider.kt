package de.hgv.download

class ParsingProvider: Provider {

    private val spielerProvider = ParsingSpielerProvider()
    private val spielProvider = ParsingSpielProvider()

    override fun getSpieler(id: String): Spieler? = spielerProvider.getSpieler(id)

    override fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein): Spiel? =
        spielProvider.getSpiel(saison, phase, daheim, auswaerts)

    override fun getSpiel(link: String): Spiel? = spielProvider.getSpiel(link)
}