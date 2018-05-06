package de.hgv.provider.parsing

import de.hgv.model.Spieler
import de.hgv.model.Verein
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SpielerProviderTest {

    private val spielerProvider = SpielerProvider()

    @Nested
    inner class `getSpieler()` {

        @Test
        fun `Gültiger Input`() {
            val spieler = Spieler("Thiago", "thiago_17")

            assertEquals(spieler, spielerProvider.getSpieler("thiago_17"))
        }

        @Test
        fun `Unbenutze ID`() {
            assertNull(spielerProvider.getSpieler("ungültige ID"))
        }

    }

    @Nested
    inner class `getDetailsForSpieler()` {

        @Nested
        inner class `Gültiger Input` {

            @Test
            fun `Vollständige Daten`() {
                val spieler = Spieler("Thiago", "thiago_17")
                val details = Spieler.Details(
                    verein = Verein("Bayern München", "bayern-muenchen"),
                    positionen = listOf("Offensives Mittelfeld", "Defensives Mittelfeld"),
                    nummer = 6,
                    land = "Spanien",
                    geburtstag = LocalDate.of(1991, 4, 11),
                    groesse = 174,
                    spielfuss = "rechts",
                    portraitUrl = "https://s.hs-data.com/bilder/spieler/gross/142540.jpg"
                )

                assertEquals(details, spielerProvider.getDetailsForSpieler(spieler))
            }

            @Test
            fun `Unvollständige Daten`() {
                val spieler = Spieler("Giannis Andreou", "giannis-andreou_2")
                val details = Spieler.Details(
                    verein = Verein("APOEL Nikosia", "apoel-nikosia"),
                    positionen = emptyList(),
                    nummer = 34,
                    land = "Zypern",
                    geburtstag = LocalDate.of(2000, 1, 4),
                    groesse = null,
                    spielfuss = null,
                    portraitUrl = null
                )

                assertEquals(details, spielerProvider.getDetailsForSpieler(spieler))
            }

        }

        @Test
        fun `Unbenutzte ID`() {
            val spieler = Spieler("Ungültige ID", "ungültige ID")
            assertNull(spielerProvider.getDetailsForSpieler(spieler))
        }

    }
}