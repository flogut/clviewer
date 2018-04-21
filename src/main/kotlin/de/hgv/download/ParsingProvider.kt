package de.hgv.download

class ParsingProvider: Provider {

    private val spielerProvider = ParsingSpielerProvider()

    override fun getSpieler(id: String): Spieler? = spielerProvider.getSpieler(id)

}