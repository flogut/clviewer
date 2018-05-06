package de.hgv.provider.parsing

import de.hgv.model.Phase
import de.hgv.model.Spiel
import de.hgv.model.Verein
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SpielProviderTest {

    private val spielProvider = SpielProvider()

    @Nested
    inner class `getSpiel()` {

        @Nested
        inner class Link {

            @Nested
            inner class `Gültiger Input` {

                @Test
                fun `Reguläre Spielzeit`() {
                    val spiel = Spiel(
                        daheim = Verein("Borussia Dortmund", "borussia-dortmund"),
                        auswaerts = Verein("Bayern München", "bayern-muenchen"),
                        datum = LocalDate.of(2013, 5, 25),
                        toreHeim = 1,
                        toreAuswaerts = 2,
                        verlaengerung = false,
                        elfmeterschiessen = false,
                        phase = Phase.FINALE
                    )

                    assertEquals(
                        spiel,
                        spielProvider.getSpiel(
                            "http://www.weltfussball.de/spielbericht/champions-league-2012-2013-finale-borussia-dortmund-bayern-muenchen/",
                            false
                        )
                    )
                }

                @Test
                fun `Verlängerung`() {
                    val spiel = Spiel(
                        daheim = Verein("Bayern München", "bayern-muenchen"),
                        auswaerts = Verein("Juventus", "juventus"),
                        datum = LocalDate.of(2016, 3, 16),
                        toreHeim = 4,
                        toreAuswaerts = 2,
                        verlaengerung = true,
                        elfmeterschiessen = false,
                        phase = Phase.ACHTELFINALE
                    )

                    assertEquals(
                        spiel,
                        spielProvider.getSpiel(
                            "http://www.weltfussball.de/spielbericht/champions-league-2015-2016-achtelfinale-bayern-muenchen-juventus/",
                            false
                        )
                    )
                }

                @Test
                fun `Elfmeterschießen`() {
                    val spiel = Spiel(
                        daheim = Verein("Real Madrid", "real-madrid"),
                        auswaerts = Verein("Bayern München", "bayern-muenchen"),
                        datum = LocalDate.of(2012, 4, 25),
                        toreHeim = 1,
                        toreAuswaerts = 3,
                        verlaengerung = false,
                        elfmeterschiessen = true,
                        phase = Phase.HALBFINALE
                    )

                    assertEquals(
                        spiel,
                        spielProvider.getSpiel(
                            "http://www.weltfussball.de/spielbericht/champions-league-2011-2012-halbfinale-real-madrid-bayern-muenchen/",
                            false
                        )
                    )
                }

            }

            @Test
            fun `Ungültiger Input`() {
                assertNull(spielProvider.getSpiel("http://www.weltfussball.de/spielbericht/ungueltig/", false))
            }

        }

        @Nested
        inner class Parameter {

            @Test
            fun `Gültiger Input`() {
                val spiel = Spiel(
                    daheim = Verein("Borussia Dortmund", "borussia-dortmund"),
                    auswaerts = Verein("Bayern München", "bayern-muenchen"),
                    datum = LocalDate.of(2013, 5, 25),
                    toreHeim = 1,
                    toreAuswaerts = 2,
                    verlaengerung = false,
                    elfmeterschiessen = false,
                    phase = Phase.FINALE
                )


                assertEquals(
                    spiel, spielProvider.getSpiel(
                        saison = 2013,
                        phase = "Finale",
                        daheim = Verein("Borussia Dortmund", "borussia-dortmund"),
                        auswaerts = Verein("Bayern München", "bayern-muenchen"),
                        detailed = false
                    )
                )
            }

            @Nested
            inner class `Ungültiger Input` {

                @Test
                fun `Saison`() {
                    assertNull(
                        spielProvider.getSpiel(
                            saison = 2033,
                            phase = "Finale",
                            daheim = Verein("Borussia Dortmund", "borussia-dortmund"),
                            auswaerts = Verein("Bayern München", "bayern-muenchen"),
                            detailed = false
                        )
                    )
                }

                @Test
                fun `Phase`() {
                    assertNull(
                        spielProvider.getSpiel(
                            saison = 2013,
                            phase = "Ungültig",
                            daheim = Verein("Borussia Dortmund", "borussia-dortmund"),
                            auswaerts = Verein("Bayern München", "bayern-muenchen"),
                            detailed = false
                        )
                    )
                }

                @Test
                fun `Verein`() {
                    assertNull(
                        spielProvider.getSpiel(
                            saison = 2013,
                            phase = "Finale",
                            daheim = Verein("Borussia Dortmund", "tigerentenclub-dortmund"),
                            auswaerts = Verein("Bayern München", "bayern-muenchen"),
                            detailed = false
                        )
                    )
                }

            }

        }

    }

    @Nested
    inner class `getDetailsForSpiel()` {
        @Test
        fun `Gültiger Input`() {
            val spiel = spielProvider.getSpiel(
                "http://www.weltfussball.de/spielbericht/champions-league-2011-2012-halbfinale-real-madrid-bayern-muenchen/",
                false
            ) ?: throw Exception()

            val details = spielProvider.getDetailsForSpiel(spiel)

            assertEquals(11, details?.startelfHeim?.size, "Die Heim-Starterlf hat keine elf Spieler")
            assertEquals(11, details?.startelfAuswaerts?.size, "Die Auswärts-Starterlf hat keine elf Spieler")
            assertEquals(3, details?.auswechslungenHeim?.size, "Die Anzahl der Heim-Auswechslungen ist inkorrekt")
            assertEquals(
                1,
                details?.auswechslungenAuswaerts?.size,
                "Die Anzahl der Auswärts-Auswechslungen ist inkorrekt"
            )
            assertEquals(3, details?.tore?.size, "Die Anzahl der Tore ist inkorrekt")
            assertEquals(3, details?.kartenHeim?.size, "Die Anzahl der Heim-Karten ist inkorrekt")
            assertEquals(4, details?.kartenAuswaerts?.size, "Die Anzahl der Auswärts-Karten ist inkorrekt")
        }

        @Test
        fun `Ungültiger Input`() {
            val spiel = Spiel(
                daheim = Verein("Borussia Dortmund", "tigerentenclub-dortmund"),
                auswaerts = Verein("Bayern München", "bayern-muenchen"),
                datum = LocalDate.of(2001, 1, 5),
                toreHeim = 0,
                toreAuswaerts = 13,
                verlaengerung = false,
                elfmeterschiessen = false,
                phase = Phase.FINALE
            )

            assertNull(spielProvider.getDetailsForSpiel(spiel))
        }

    }
}