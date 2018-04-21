package de.hgv.download

import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ParsingSpielerProvider {

    fun getSpieler(id: String): Spieler? {
        val doc = Jsoup.parse(URL("http://www.weltfussball.de/spieler_profil/$id"), 5000)

        val name = doc.selectFirst(".sidebar > .box > .head > h2[itemprop=name]")?.text()

        val tabelle = doc.selectFirst("table.standard_tabelle.yellow") ?: return null
        val rows = tabelle.select("tr")

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val geburtstag = getTextFromTable(rows, "geboren am:")?.let { LocalDate.parse(it, dateTimeFormatter) }

        val land = getTextFromTable(rows, "Nationalität:")?.split(" ")?.firstOrNull()

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
            doc.selectFirst("table.standard_tabelle > tbody > tr:contains(#) > td:eq(1) > a")?.text() ?: return null

        val vereinId =
            doc.selectFirst("table.standard_tabelle > tbody > tr:contains(#) > td:eq(1) > a")
                ?.attr("href")
                ?.removeSurrounding("/teams/", "/") ?: return null

        val verein = Verein(vereinName, vereinId)

        val portraitUrl = doc.selectFirst("div.data[itemprop=image] > img")?.attr("src")

        return Spieler(
            name = name ?: id,
            id = id,
            verein = verein,
            positionen = positionen,
            nummer = nummer,
            land = land,
            geburtstag = geburtstag,
            groesse = groesse,
            spielfuss = spielfuss,
            portraitUrl = portraitUrl
        )
    }

    private fun getTextFromTable(rows: Elements, text: String): String? = rows.select("tr:contains($text)")
        .firstOrNull()
        ?.select("td")
        ?.get(1)
        ?.text()

    private fun getHtmlFromTable(rows: Elements, text: String): String? = rows.select("tr:contains($text)")
        .firstOrNull()
        ?.select("td")
        ?.get(1)
        ?.html()

}