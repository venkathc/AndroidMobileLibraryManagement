package com.venkateshgowda.personallibrary.data

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class EncryptedArchiveTest {
    @Test fun roundTripsAuthenticatedPayload() {
        val encrypted = EncryptedArchive.encrypt("private library".toByteArray(), "passphrase".toCharArray())
        assertArrayEquals("private library".toByteArray(), EncryptedArchive.decrypt(encrypted, "passphrase".toCharArray()))
    }

    @Test(expected = Exception::class) fun rejectsWrongPassphrase() {
        val encrypted = EncryptedArchive.encrypt("private library".toByteArray(), "passphrase".toCharArray())
        EncryptedArchive.decrypt(encrypted, "wrong passphrase".toCharArray())
    }
}