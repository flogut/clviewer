package de.hgv.provider.parsing

import de.hgv.model.*
import de.hgv.provider.Provider
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Der ParsingProvider lädt die benötigten Daten von einer Website herunter und parst sie dann.
 * Die einzige Instanz dieser Klasse die erstellt werden soll, ist der ActiveProvider.
 *
 * @author Florian Gutekunst
 */
open class ParsingProvider: Provider {

    private val spielerProvider = SpielerProvider()
    private val spielProvider = SpielProvider()
    private val tabelleProvider = TabelleProvider()

    override fun getSpieler(id: String): Spieler? = spielerProvider.getSpieler(id)

    override fun getDetailsForSpieler(spieler: Spieler): Spieler.Details? =
        spielerProvider.getDetailsForSpieler(spieler)

    override fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein, detailed: Boolean): Spiel? =
        spielProvider.getSpiel(saison, phase, daheim, auswaerts, detailed)

    override fun getSpiel(link: String, detailed: Boolean): Spiel? = spielProvider.getSpiel(link, detailed)

    override fun getDetailsForSpiel(spiel: Spiel): Spiel.Details? = spielProvider.getDetailsForSpiel(spiel)

    override fun getSpiele(saison: Int): List<Spiel> {
        val doc =
            Jsoup.parse(URL("http://www.weltfussball.de/alle_spiele/champions-league-${saison - 1}-$saison/"), 5000)

        val tabelle = doc.selectFirst(".box > .data > table.standard_tabelle")
        val rows = tabelle.select("tr")

        val spiele = mutableListOf<Spiel>()
        var phaseString = ""
        var letztesDatum: LocalDate = LocalDate.now()

        for (row in rows) {
            if (row.`is`(":has(th)")) {
                row.selectFirst("th > a")?.text()?.let { phaseString = it }
                continue
            }

            val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val datum = row.selectFirst("td:eq(0) > a")?.text()?.let { LocalDate.parse(it, dtf) } ?: letztesDatum
            letztesDatum = datum

            val daheim = getVereinFromRow(row, 2) ?: continue
            val auswaerts = getVereinFromRow(row, 4) ?: continue

            val ergebnisText = row.selectFirst("td:eq(5) > a")?.text() ?: continue
            val ergebnis = ergebnisText.substringBefore(" ").split(":")
            val verlaengerung = ergebnisText.endsWith("n.V.")
            val elfmeterschiessen = ergebnisText.endsWith("i.E.")

            val toreHeim = ergebnis[0].toIntOrNull() ?: continue
            val toreAuswaerts = ergebnis[1].toIntOrNull() ?: continue

            val phase = Phase.getValue(phaseString)

            val spiel =
                Spiel(daheim, auswaerts, datum, toreHeim, toreAuswaerts, verlaengerung, elfmeterschiessen, phase)

            spiele.add(spiel)
        }

        return spiele
    }

    override fun getTabelle(gruppe: String, saison: Int): Tabelle? = tabelleProvider.getTabelle(gruppe, saison)

    private fun getVereinFromRow(row: Element, index: Int): Verein? {
        val link = row.selectFirst("td:eq($index) > a") ?: return null
        val name = link.attr("title")
        val id = link.attr("href").removeSurrounding("/teams/", "/")
        return Verein(name, id)
    }
}