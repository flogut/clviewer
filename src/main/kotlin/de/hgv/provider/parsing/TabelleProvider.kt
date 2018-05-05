package de.hgv.provider.parsing

import de.hgv.model.Tabelle
import de.hgv.model.Verein
import org.jsoup.Jsoup
import java.net.URL

class TabelleProvider {

    fun getTabelle(gruppe: String, saison: Int): Tabelle {
        val doc = Jsoup.parse(
            URL(
                "http://www.weltfussball.de/spielplan/champions-league-${saison - 1}-$saison-${gruppe.toLowerCase().replace(
                    " ",
                    "-"
                )}/0/"
            ), 5000
        )

        val zeilen = doc.select(".box > .data > .standard_tabelle > tbody > tr:not(:has(th))")
        val tabelle = mutableListOf<Pair<Verein, Int>>()

        for (zeile in zeilen) {
            val name = zeile.selectFirst("a")?.attr("title") ?: continue
            val id = zeile.selectFirst("a")?.attr("href")?.removeSurrounding("/teams/", "/") ?: continue
            val verein = Verein(name, id)

            val punkte = zeile.selectFirst("td:eq(9)")?.text()?.toIntOrNull() ?: continue

            tabelle.add(verein to punkte)
        }

        return Tabelle(gruppe, tabelle)
    }

}