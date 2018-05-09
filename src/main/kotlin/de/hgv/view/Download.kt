package de.hgv.view

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

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

