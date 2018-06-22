package de.hgv.view

import de.hgv.model.Verein
import javafx.scene.image.Image
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

/**
 * @author Florian Gutekunst
 */
object Download {

    private val TEMP_DIR: File = File(System.getenv("temp") + "\\clviewer").also { it.mkdirs() }

    /**
     * Lädt die Website, die unter url zu finden ist, asynchron herunter. Da manche Websites den Java User-Agent
     * blockieren, wird hier der Firefox User-Agent genutzt.
     * @param url URL der Website, die heruntergeladen werden soll
     * @return Es wird ein Deferred<InputStream> zurückgegeben. Um auf den InputStream tatsächlich zuzugreifen, muss
     * noch die await() Methode aufgerufen werden.
     */
    fun downloadAsync(url: String?): Deferred<InputStream?> = async {
        download(url)
    }

    /**
     * Lädt url herunter. Blockt währenddessen den Thread.
     * @param url URL der Website, die heruntergeladen werden soll
     */
    private fun download(url: String?): InputStream? = try {
        val connection = URL(url).openConnection()
        connection.setRequestProperty(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0"
        )

        connection.getInputStream()
    } catch (e: MalformedURLException) {
        null
    }

    /**
     * Lädt die Wappen der Vereine herunter.
     * @param vereine Liste der Vereine, deren Wappen heruntergeladen werden sollen
     */
    fun downloadWappen(vereine: List<Verein>): Map<Verein, Image> = runBlocking {
        vereine.distinct()
            .map { verein -> verein to async { downloadWappen(verein) } }
            .map { (verein, job) -> verein to job.await() }
            .toMap()
    }

    /**
     * Lädt das Wappen eines Vereins herunter und cached es.
     * @param verein Verein, dessen Wappen heruntergeladen werden soll
     */
    fun downloadWappen(verein: Verein?): Image {
        if (verein == null || verein == Verein.UNBEKANNT) {
            TODO("Eigenes Wappen zurückgeben")
        }

        if (isCached(verein)) {
            return getCachedWappen(verein)
        }

        val inputStream = download(verein.wappenURL) ?: throw Exception("Wappen konnte nicht heruntergeladen werden")

        val bytes = inputStream.readBytes()

        val wappen = Image(bytes.inputStream())

        cache(verein, bytes)

        return wappen
    }

    /**
     * Prüft, ob das Wappen eines Vereins bereits gecached wurde.
     * @param verein Verein, dessen Wappen getested werden soll
     */
    private fun isCached(verein: Verein): Boolean =
        TEMP_DIR.listFiles()?.any { it.nameWithoutExtension == verein.id } ?: false

    /**
     * Lädt das Wappen aus dem Cache.
     * @param verein Verein, dessen Wappen geladen werden soll
     * @throws IllegalArgumentException, wenn das Wappen nicht im Cache gefunden wird
     */
    private fun getCachedWappen(verein: Verein): Image {
        val file = TEMP_DIR.listFiles()?.find { it.nameWithoutExtension == verein.id }
                ?: throw IllegalArgumentException("Wappen wurde nicht gechached")

        return Image(file.inputStream())
    }

    /**
     * Speichert das Wappen eines Vereins im Cache.
     */
    private fun cache(verein: Verein, wappen: ByteArray) {
        if (isCached(verein)) {
            return
        }

        File("$TEMP_DIR\\${verein.id}.gif").writeBytes(wappen)
    }
}
