package de.hgv.view

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

/**
 * Lädt die Website, die unter url zu finden ist, asynchron herunter. Da manche Websites den Java User-Agent blockieren,
 * wird hier der Firefox User-Agent genutzt.
 * @param url URL der Website, die heruntergeladen werden soll
 * @return Es wird ein Deferred<InputStream> zurückgegeben. Um auf den InputStream tatsächlich zuzugreifen, muss noch
 * die await() Methode aufgerufen werden.
 *
 * @author Florian Gutekunst
 */
fun download(url: String?): Deferred<InputStream?> = async {
    try {
        val connection = URL(url).openConnection()
        connection.setRequestProperty(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0"
        )

        connection.getInputStream()
    } catch (e: MalformedURLException) {
        null
    }
}
