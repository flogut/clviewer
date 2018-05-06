package de.hgv.provider.parsing

import de.hgv.model.Tabelle
import de.hgv.model.Verein
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TabelleProviderTest {

    private val tabelleProvider = TabelleProvider()

    @Test
    fun `Gültiger Input`() {
        val zeilen = listOf(
            Tabelle.Zeile(1, Verein("Paris Saint-Germain", "paris-saint-germain"), 21, 15),
            Tabelle.Zeile(2, Verein("Bayern München", "bayern-muenchen"), 7, 15),
            Tabelle.Zeile(3, Verein("Celtic FC", "celtic-fc"), -13, 3),
            Tabelle.Zeile(4, Verein("RSC Anderlecht", "rsc-anderlecht"), -15, 3)
        )
        val tabelle = Tabelle("Gruppe B", zeilen)

        assertEquals(tabelle, tabelleProvider.getTabelle("Gruppe B", 2018))
    }

    @Test
    fun `Ungültige Saison`() {
        assertNull(tabelleProvider.getTabelle("Gruppe A", 2033))
    }

    @Test
    fun `Ungültige Gruppe`() {
        assertNull(tabelleProvider.getTabelle("Gruppe X", 2018))
    }
}