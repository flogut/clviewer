package de.hgv.provider.parsing

import de.hgv.model.Spieler
import de.hgv.model.Verein
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Florian Gutekunst
 */
class SpielerProvider {

    /**
     * getSpieler parst alle Daten zu einem Spieler.
     * @param id ID des Spielers
     * @return Den Spieler, oder null, wenn die ID ungültig ist
     */
    fun getSpieler(id: String): Spieler? {
        val doc = Jsoup.parse(URL("http://www.weltfussball.de/spieler_profil/$id"), 5000)

        val name = doc.selectFirst(".sidebar > .box > .head > h2[itemprop=name]")?.text() ?: id

        val tabelle = doc.selectFirst("table.standard_tabelle.yellow") ?: return null
        val rows = tabelle.select("tr")

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY)
        val geburtstag =
            getTextFromTable(rows, "geboren am:")?.let { LocalDate.parse(it, dateTimeFormatter) }

        val land = getLand(rows)

        val groesse = getTextFromTable(rows, "Größe:")?.removeSuffix(" cm")?.toIntOrNull()

        val positionen =
            getHtmlFromTable(rows, "Position(en):")
                ?.removeSuffix("<br>")
                ?.split("<br>")
                ?.toList() ?: listOf()

        val spielfuss = getTextFromTable(rows, "Spielfuß:")

        val nummer = doc.selectFirst("table.standard_tabelle > tbody > tr:contains(#)")
            ?.getElementsByTag("td")
            ?.getOrNull(2)
            ?.text()
            ?.substring(1)
            ?.toIntOrNull()

        val vereinName =
            doc.selectFirst("table.standard_tabelle > tbody > tr:contains(#) > td:eq(1) > a")
                ?.text()

        val vereinId =
            doc.selectFirst("table.standard_tabelle > tbody > tr:contains(#) > td:eq(1) > a")
                ?.attr("href")
                ?.removeSurrounding("/teams/", "/")

        val verein = if (vereinName != null && vereinId != null) Verein(vereinName, vereinId) else Verein.UNBEKANNT

        val portraitUrl = doc.selectFirst("div.data[itemprop=image] > img")?.attr("src")

        val details =
            Spieler.Details(verein, positionen, nummer, land, geburtstag, groesse, spielfuss, portraitUrl)
        val spieler = Spieler(name, id)
        spieler.details = details

        return spieler
    }

    /**
     * getDetailsForSpieler parst die Details zu einem Spieler.
     * @param spieler Spieler, dessen Details geparst werden sollen
     * @return Die Details, oder null, wenn ein Fehler auftritt
     */
    fun getDetailsForSpieler(spieler: Spieler): Spieler.Details? = getSpieler(spieler.id)?.details

    /**
     * Parst die (Haupt-)Nation des Spielers.
     * @param rows Tablle, die die Daten des Spielers enthält
     */
    private fun getLand(rows: Elements) =
        rows.select("tr:contains(Nationalität)")
            .firstOrNull()?.select("td")
            ?.get(1)
            ?.selectFirst("span")
            ?.text()

    /**
     * Es wird eine aus zwei Spalten bestehende Tabelle mit den Zeilen rows übergegeben.
     * Gibt es nun eine Zeile, deren erste Spalte text enthält, dann gibt getTextFromTable den Text der rechten Spalte
     * dieser Zeile zurück.
     * @param rows Zeilen einer Tabelle
     * @param text Text, nach dem gesucht wird
     * @return Den Text der rechten Spalte oder null, wenn der gesuchte Text nicht gefunden wird
     */
    private fun getTextFromTable(rows: Elements, text: String): String? = rows.select("tr:contains($text)")
        .firstOrNull()
        ?.select("td")
        ?.get(1)
        ?.text()

    /**
     * Er wird eine aus zwei Spalten bestehende Tabelle mit den Zeilen rows übergeben.
     * Gibt es nun eine Zeile, deren erste Spalte text enthält, dann gibt getHtmlFromTable den HTML-Code der rechten
     * Spalte dieser Zeile zurück.
     * @param rows Zeilen einer Tabelle
     * @param text Text, nach dem gesucht wird
     * @return Den HTML-Code der rechten Spalte oder null, wenn der gesuchte Text nicht gefunden wird
     */
    private fun getHtmlFromTable(rows: Elements, text: String): String? = rows.select("tr:contains($text)")
        .firstOrNull()
        ?.select("td")
        ?.get(1)
        ?.html()
}
