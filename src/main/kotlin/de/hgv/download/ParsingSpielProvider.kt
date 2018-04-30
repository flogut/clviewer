package de.hgv.download

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ParsingSpielProvider {

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     * @param phase Phase, in der das Spiel stattfindet (Gruppe A, ..., Gruppe F, Achtelfinale, Viertelfinale, Halbfinale, Finale)
     * @param daheim Heimmannschaft
     * @param auswaerts Auswärtsmannschaft
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     */
    fun getSpiel(saison: Int, phase: String, daheim: Verein, auswaerts: Verein, detailed: Boolean): Spiel? {
        val linkPhase = phase.replace(" ", "-").toLowerCase()
        val link =
            "http://www.weltfussball.de/spielbericht/champions-league-${saison - 1}-$saison-$linkPhase-${daheim.id}-${auswaerts.id}"
        return getSpiel(link, detailed)
    }

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param link Letzter Teil des Links zur Spielübersicht
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     */
    fun getSpiel(link: String, detailed: Boolean): Spiel? {
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

        // \u00BB = »
        val phase =
            doc.selectFirst("#navi > .breadcrumb > h1")
                ?.text()
                ?.substringAfter("\u00BB")
                ?.substringBeforeLast("\u00BB")
                ?.trim() ?: return null

        val spiel = Spiel(daheim, auswaerts, datum, toreHeim, toreAuswaerts, phase)

        if (detailed) {
            spiel.details = getDetailsForSpiel(spiel)
        }

        return spiel
    }

    /**
     * getDetailsForSpiel parst die Details zu einem Spiel (Aufstellungen, Torschützen)
     * @param spiel Spiel, dessen Details geparst werden sollen
     */
    fun getDetailsForSpiel(spiel: Spiel): Spiel.Details? {
        val linkPhase = spiel.phase.replace(" ", "-").toLowerCase()
        val link =
            "http://www.weltfussball.de/spielbericht/champions-league-${spiel.saison - 1}-${spiel.saison}-$linkPhase-${spiel.daheim.id}-${spiel.auswaerts.id}"
        val doc = Jsoup.parse(URL(link), 5000)

        val spielerHeimTabelle = doc.selectFirst(".box .data table td:eq(0) > table.standard_tabelle") ?: return null
        val spielerHeim = getAufstellung(spielerHeimTabelle)

        val spielerAuswaertsTabelle =
            doc.selectFirst(".box .data table td:eq(1) > table.standard_tabelle") ?: return null
        val spielerAuswaerts = getAufstellung(spielerAuswaertsTabelle)

        val toreTabelle = doc.selectFirst("table.standard_tabelle:contains(Tore)") ?: return null
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

        return Spiel.Details(spielerHeim, spielerAuswaerts, torschuetzen)
    }

    /**
     * getId holt die ID eines Vereins
     * @param rows Zeilen der Tabelle mit den Vereinsnamen
     * @param index Index der Spalte, in der der Name des Vereins, dessen ID gesucht wird, steht
     */
    private fun getId(rows: Elements, index: Int): String? =
        rows.firstOrNull()
            ?.selectFirst("th:eq($index) > a")
            ?.attr("href")
            ?.removeSurrounding("/teams/", "/")

    /**
     * getAufstellung parst die Aufstellung (bestehend aus Startelf und eingewechselten Spielern)
     * @param tabelle Tabelle, in der die Aufstellung des gesuchten Vereins steht
     */
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