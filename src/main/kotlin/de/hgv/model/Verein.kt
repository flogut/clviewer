package de.hgv.model

import org.jsoup.Jsoup
import java.net.URL

data class Verein(val name: String, val id: String) {

    val wappenURL: String? by lazy {
        val doc = Jsoup.parse(URL("http://weltfussball.de/teams/$id"), 5000)
        doc.selectFirst("div.emblem > a > img")?.attr("src")
    }

}