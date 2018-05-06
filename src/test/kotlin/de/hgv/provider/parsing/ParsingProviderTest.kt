package de.hgv.provider.parsing

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ParsingProviderTest {

    private val parsingProvider = ParsingProvider()

    @Nested
    inner class `getSpiele` {

        @Test
        fun `Gültiger Input`() {
            assertEquals(125, parsingProvider.getSpiele(2017).size, "Die Anzahl der Spiele ist inkorrekt")
        }

        @Test
        fun `Ungültiger Input`() {
            assertEquals(0, parsingProvider.getSpiele(2033).size, "Die Anzahl der Spiele ist inkorrekt")
        }

    }

}