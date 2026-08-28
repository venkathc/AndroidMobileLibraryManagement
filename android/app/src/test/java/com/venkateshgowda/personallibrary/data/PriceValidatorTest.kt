package com.venkateshgowda.personallibrary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceValidatorTest {
    @Test fun acceptsNonNegativeInrWithAtMostTwoDecimalPlaces() {
        assertEquals(0L, PriceValidator.paise("0"))
        assertEquals(12575L, PriceValidator.paise("125.75"))
        assertEquals(500L, PriceValidator.paise("5.0"))
    }

    @Test fun rejectsSpacesNegativeAndInvalidCharacters() {
        listOf("", " 5", "5 ", "-5", "+5", "5.123", "5,000", "1e3", "abc").forEach { value ->
            assertNull(value, PriceValidator.paise(value))
        }
    }
}