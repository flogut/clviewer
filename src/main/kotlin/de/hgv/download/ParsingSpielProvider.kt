package de.hgv.download

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ParsingSpielProvider {

    fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein): Spiel? {
        val linkPhase = phase.replace(" ", "-").toLowerCase()
        val link =
            "http://www.weltfussball.de/spielbericht/champions-league-${saison - 1}-$saison-$linkPhase-${daheim.id}-${auswaerts.id}"
        return getSpiel(link)
    }

    fun getSpiel(link: String): Spiel? {
        val doc = Jsoup.parse(URL(link), 5000)

        val tabelle = doc.selectFirst(".box > .data > .standard_tabelle") ?: return null
        val rows = tabelle.select("tr")

        val daheimName = rows.firstOrNull()?.selectFirst("th > a")?.text()
        val daheimId = getId(rows, 0)
        if (daheimName == null || daheimId == null) {
            return null
        }
        val daheim = Verein(daheimName, daheimId)

        val auswaertsName = rows.firstOrNull()?.select("th > a")?.getOrNull(1)?.text()
        val auswaertsId = getId(rows, 2)
        if (auswaertsName == null || auswaertsId == null) {
            return null
        }
        val auswaerts = Verein(auswaertsName, auswaertsId)

        val dtf = DateTimeFormatter.ofPattern("d. MMMM yyyy")
        val datumText =
            rows.firstOrNull()
                ?.selectFirst("th:eq(1)")
                ?.html()
                ?.substringBefore("<br>")
                ?.substringAfter(", ")
        val datum = datumText?.let { LocalDate.parse(it, dtf) } ?: return null

        val ergebnis = rows.getOrNull(1)?.selectFirst("td > .resultat")?.text()?.split(":") ?: return null
        val toreHeim = ergebnis[0].toIntOrNull() ?: return null
        val toreAuswaerts = ergebnis[1].toIntOrNull() ?: return null

        val spielerHeimTabelle = doc.selectFirst(".box .data table td:eq(0) > table.standard_tabelle")
        val spielerHeim = getAufstellung(spielerHeimTabelle)

        val spielerAuswaertsTabelle = doc.selectFirst(".box .data table td:eq(1) > table.standard_tabelle")
        val spielerAuswaerts = getAufstellung(spielerAuswaertsTabelle)

        val toreTabelle = doc.selectFirst("table.standard_tabelle:contains(Tore)")
        val torschuetzen =
            toreTabelle
                .select("tr:gt(0) > td:eq(1) > a:eq(0)").asSequence()
                .map { it.attr("href") to it.attr("title") }
                .map { (link, name) -> link.removeSurrounding("/spieler_profil/", "/") to name }
                .toList()

        //TODO Zu Toren Zeitpunkt und Vorlagengeber speichern
        //TODO Auswechslungen explizit mit Zeitpunkt speichern
        //TODO Karten speichern
        //TODO Anzeige als Timeline?

        return Spiel(
            daheim = daheim,
            auswaerts = auswaerts,
            datum = datum,
            toreHeim = toreHeim,
            toreAuswaerts = toreAuswaerts,
            spielerHeim = spielerHeim,
            spielerAuswaerts = spielerAuswaerts,
            torschuetzen = torschuetzen
        )
    }

    private fun getId(rows: Elements, index: Int): String? =
        rows.firstOrNull()
            ?.selectFirst("th:eq($index) > a")
            ?.attr("href")
            ?.removeSurrounding("/teams/", "/")

    private fun getAufstellung(tabelle: Element): List<Pair<String, String>> {
        val spieler = mutableListOf<Pair<String, String>>()

        val startelfLinks = tabelle.select("tr:lt(11) > td:eq(1) > a")
        startelfLinks.asSequence()
            .map { it.attr("href") to it.attr("title") }
            .map { (link, name) -> link.removeSurrounding("/spieler_profil/", "/") to name }
            .toCollection(spieler)

        val reserve = tabelle.select("tr:gt(11)")
        reserve.asSequence()
            .filter { it.selectFirst("td:eq(2)")?.text()?.isNotEmpty() == true }
            .map { it.selectFirst("td:eq(1) > a") }
            .map { it.attr("href") to it.attr("title") }
            .map { (link, name) -> link.removeSurrounding("/spieler_profil/", "/") to name }
            .toCollection(spieler)

        return spieler
    }

}