package de.hgv.model

import org.jsoup.Jsoup
import java.net.URL

/**
 * @author Florian Gutekunst
 */
data class Verein(val name: String, val id: String) {

    /**
     * Die wappenUrl wird beim ersten Zugriff heruntergeladen und gespeichert. Dieser dauert also potenziell etwas
     * länger.
     */
    val wappenURL: String? by lazy {
        val doc = Jsoup.parse(URL("http://weltfussball.de/teams/$id"), 5000)
        doc.selectFirst("div.emblem > a > img")?.attr("src")
    }
}
