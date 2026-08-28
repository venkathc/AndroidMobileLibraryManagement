package com.venkateshgowda.personallibrary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryCreatorTest {
    @Test fun creatorIsStoredAsOwnerWithTrimmedOptionalFields() {
        val library = LibraryCreator.create("  Reading room  ", "  ", null, "  Venkatesh  ", 123L)

        assertEquals("Reading room", library.name)
        assertEquals("Venkatesh", library.owner)
        assertNull(library.description)
        assertEquals(123L, library.createdAtMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun ownerIsRequired() {
        LibraryCreator.create("Reading room", "", null, "")
    }
}