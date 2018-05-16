package de.hgv.provider.parsing

import de.hgv.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @author Florian Gutekunst
 */
class SpielProvider {

    /**
     * getSpiel parst die Daten zu einem Spiel
     * @param saison Jahr, in dem das Finale stattfindet (z.B. Saison 2017/2018 => 2018)
     * @param phase Phase, in der das Spiel stattfindet (Gruppe A, ..., Gruppe F, Achtelfinale, Viertelfinale, Halbfinale, Finale)
     * @param daheim Heimmannschaft
     * @param auswaerts Auswärtsmannschaft
     * @param detailed Falls true werden die Details des Spiels geparst (Aufstellungen, Torschützen)
     * @return Das Spiel, oder null, wenn ein Fehler auftritt
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
     * @return Das Spiel, oder null, wenn ein Fehler auftritt
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

        var ergebnisString = rows.getOrNull(1)?.selectFirst("td > .resultat")?.text() ?: return null
        var verlaengerung = false
        var elfmeterschiessen = false
        if (ergebnisString.endsWith("n.V.")) {
            verlaengerung = true
            ergebnisString = ergebnisString.removeSuffix("n.V.").trim()
        } else if (ergebnisString.endsWith("i.E.")) {
            elfmeterschiessen = true
            ergebnisString = ergebnisString.removeSuffix("i.E.").trim()
        }
        val ergebnis = ergebnisString.split(":")
        val toreHeim = ergebnis[0].toIntOrNull() ?: return null
        val toreAuswaerts = ergebnis[1].toIntOrNull() ?: return null

        // \u00BB = »
        val phaseString =
            doc.selectFirst("#navi > .breadcrumb > h1")
                ?.text()
                ?.substringAfter("\u00BB")
                ?.substringBeforeLast("\u00BB")
                ?.trim() ?: return null
        val phase = Phase.getValue(phaseString)

        val spiel = Spiel(daheim, auswaerts, datum, toreHeim, toreAuswaerts, verlaengerung, elfmeterschiessen, phase)

        if (detailed) {
            spiel.details = getDetailsForSpiel(spiel)
        }

        return spiel
    }

    /**
     * getDetailsForSpiel parst die Details zu einem Spiel (Aufstellungen, Torschützen)
     * @param spiel Spiel, dessen Details geparst werden sollen
     * @return Die Details, oder null, wenn ein Fehler auftritt
     */
    fun getDetailsForSpiel(spiel: Spiel): Spiel.Details? {
        val linkPhase = spiel.phase.toLink()
        val link =
            "http://www.weltfussball.de/spielbericht/champions-league-${spiel.saison - 1}-${spiel.saison}-$linkPhase-${spiel.daheim.id}-${spiel.auswaerts.id}"
        val doc = Jsoup.parse(URL(link), 5000)

        val spielerHeimTabelle = doc.selectFirst(".box .data table td:eq(0) > table.standard_tabelle") ?: return null
        val spielerHeim = getAufstellung(spielerHeimTabelle)

        val spielerAuswaertsTabelle =
            doc.selectFirst(".box .data table td:eq(1) > table.standard_tabelle") ?: return null
        val spielerAuswaerts = getAufstellung(spielerAuswaertsTabelle)

        val toreTabelle = doc.selectFirst("table.standard_tabelle:contains(Tore)") ?: return null
        val tore = mutableListOf<Tor>()
        for (tor in toreTabelle.select("tr:gt(0) > td:eq(1)")) {
            val torschuetze = tor.selectFirst("a")
                ?.let {
                    Spieler(
                        it.attr("title"),
                        it.attr("href").removeSurrounding("/spieler_profil/", "/")
                    )
                }

            val vorlagengeber = tor.selectFirst("a:gt(0)")
                ?.let {
                    Spieler(
                        it.attr("title"),
                        it.attr("href").removeSurrounding("/spieler_profil/", "/")
                    )
                }

            val spielminute = tor.ownText().substringBefore(". /").trim().toIntOrNull()

            val eigentor = tor.ownText().contains("Eigentor")

            val elfmeter = tor.ownText().contains("Elfmeter")

            if (torschuetze == null || spielminute == null) {
                continue
            }

            tore.add(Tor(torschuetze, vorlagengeber, spielminute, eigentor, elfmeter))
        }

        val auswechslungenHeim = getAuswechslungen(spielerHeimTabelle)

        val auswechslungenAuswaerts = getAuswechslungen(spielerAuswaertsTabelle)

        val kartenHeim = getKarten(spielerHeimTabelle)
        val kartenAuswaerts = getKarten(spielerAuswaertsTabelle)

        return Spiel.Details(
            spielerHeim,
            spielerAuswaerts,
            auswechslungenHeim,
            auswechslungenAuswaerts,
            tore,
            kartenHeim,
            kartenAuswaerts
        )
    }

    /**
     * getId holt die ID eines Vereins
     * @param rows Zeilen der Tabelle mit den Vereinsnamen
     * @param index Index der Spalte, in der der Name des Vereins, dessen ID gesucht wird, steht
     * @return Die ID, oder null, wenn die ID nicht gefunden wird
     */
    private fun getId(rows: Elements, index: Int): String? =
        rows.firstOrNull()
            ?.selectFirst("th:eq($index) > a")
            ?.attr("href")
            ?.removeSurrounding("/teams/", "/")

    /**
     * getAufstellung parst die Startelf
     * @param tabelle Tabelle, in der die Aufstellung des gesuchten Vereins steht
     */
    private fun getAufstellung(tabelle: Element): List<Spieler> {
        val spieler = mutableListOf<Spieler>()

        val startelfLinks = tabelle.select("tr:lt(11) > td:eq(1) > a")
        startelfLinks.asSequence()
            .map { it.attr("href") to it.attr("title") }
            .map { (link, name) -> link.removeSurrounding("/spieler_profil/", "/") to name }
            .map { (id, name) -> Spieler(name, id) }
            .toCollection(spieler)

        return spieler
    }

    /**
     * getAuswechslungen parst die Auswechslungen
     * @param tabelle Tabelle, in der die Aufstellung des gesuchten Vereins steht
     * @return Liste aller Auswechslungen, aufsteigend sortiert nach Spielminute
     */
    private fun getAuswechslungen(tabelle: Element): List<Auswechslung> {
        val ausgewechseltTabelle =
            tabelle.select("tr:lt(11)").filter { it.selectFirst("td:eq(2)")?.text()?.isNotEmpty() == true }
        val ausgewechselt = mutableListOf<Pair<Spieler, Int>>()
        for (zeile in ausgewechseltTabelle) {
            val link = zeile.selectFirst("td:eq(1) > a") ?: continue
            val name = link.attr("title")
            val id = link.attr("href").removeSurrounding("/spieler_profil/", "/")

            val spielminute = zeile.selectFirst("td:eq(2)")?.text()?.removeSuffix("'")?.toIntOrNull() ?: continue

            ausgewechselt.add(Spieler(name, id) to spielminute)
        }

        val eingewechseltTabelle =
            tabelle.select("tr:gt(11)").filter { it.selectFirst("td:eq(2)")?.text()?.isNotEmpty() == true }
        val eingewechselt = mutableListOf<Pair<Spieler, Int>>()
        for (zeile in eingewechseltTabelle) {
            val link = zeile.selectFirst("td:eq(1) > a") ?: continue
            val name = link.attr("title")
            val id = link.attr("href").removeSurrounding("/spieler_profil/", "/")

            val spielminute = zeile.selectFirst("td:eq(2)")?.text()?.removeSuffix("'")?.toIntOrNull() ?: continue

            eingewechselt.add(Spieler(name, id) to spielminute)
        }

        val auswechslungen = mutableListOf<Auswechslung>()
        for (aus in ausgewechselt) {
            val ein = eingewechselt.firstOrNull { it.second == aus.second } ?: continue
            eingewechselt.remove(ein)

            auswechslungen.add(Auswechslung(ein.first, aus.first, ein.second))
        }

        return auswechslungen.sortedBy { it.spielminute }
    }

    /**
     * getKarten parst die Karten aus der Aufstellungstabelle einer Mannschaft
     * @param tabelle Ausfstellung des gesuchten Vereins
     */
    private fun getKarten(tabelle: Element): List<Karte> {
        val zeilen = tabelle.select("td:has(img)")

        val karten = mutableListOf<Karte>()

        for (zeile in zeilen) {
            val name = zeile.selectFirst("a")?.attr("title") ?: continue
            val id = zeile.selectFirst("a")?.attr("href")?.removeSurrounding("/spieler_profil/", "/") ?: continue
            val spieler = Spieler(name, id)

            val spielminute = zeile.selectFirst("span")?.text()?.removeSuffix("'")?.toIntOrNull() ?: continue

            val art = when (zeile.selectFirst("img").attr("alt")) {
                "gelb" -> Kartenart.GELB
                "rot" -> Kartenart.ROT
                "gelb-rot" -> Kartenart.GELBROT
                else -> null
            } ?: continue

            val karte = Karte(spieler, spielminute, art)
            karten.add(karte)
        }

        return karten
    }

}