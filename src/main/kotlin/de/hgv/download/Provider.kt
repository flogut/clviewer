package de.hgv.download

interface Provider {

    fun getSpieler(id: String): Spieler?

}