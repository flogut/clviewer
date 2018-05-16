package de.hgv.provider.parsing

import de.hgv.model.Tabelle
import de.hgv.model.Verein
import org.jsoup.Jsoup
import java.net.URL

/**
 * @author Florian Gutekunst
 */
class TabelleProvider {

    /**
     * getTabelle lädt die Tabelle (Platzierung, Tordifferenz, Punktzahl) einer Gruppe aus der Gruppenphase herunter
     * @param gruppe Name der Gruppe (z.B. "Gruppe A")
     * @param saison Jahr, in dem das Finale stattfindet (z.B. 2017/2018 => 2018)
     * @return Die Tabelle, oder null, wenn ein Fehler auftritt
     */
    fun getTabelle(gruppe: String, saison: Int): Tabelle? {
        val doc = Jsoup.parse(
            URL(
                "http://www.weltfussball.de/spielplan/champions-league-${saison - 1}-$saison-${gruppe.toLowerCase().replace(
                    " ",
                    "-"
                )}/0/"
            ), 5000
        )

        val zeilen = doc.select(".box > .data > .standard_tabelle:contains(Mannschaft) > tbody > tr:not(:has(th))")
        val tabelle = mutableListOf<Tabelle.Zeile>()

        for (zeile in zeilen) {
            val platz = zeile.selectFirst("td")?.text()?.toIntOrNull() ?: continue

            val name = zeile.selectFirst("a")?.attr("title") ?: continue
            val id = zeile.selectFirst("a")?.attr("href")?.removeSurrounding("/teams/", "/") ?: continue
            val verein = Verein(name, id)

            val tordifferenz = zeile.selectFirst("td:eq(8)")?.text()?.toIntOrNull() ?: continue

            val punkte = zeile.selectFirst("td:eq(9)")?.text()?.toIntOrNull() ?: continue

            tabelle.add(Tabelle.Zeile(platz, verein, tordifferenz, punkte))
        }

        if (tabelle.size != 4) {
            return null
        }

        return Tabelle(gruppe, tabelle)
    }

}