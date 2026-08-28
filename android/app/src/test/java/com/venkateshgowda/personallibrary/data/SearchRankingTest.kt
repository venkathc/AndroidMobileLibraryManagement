package com.venkateshgowda.personallibrary.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchRankingTest {
    @Test fun exactAndPartialMatchesPrecedeFuzzyMatches() {
        val direct = BookEntity(id = 1, title = "Clean Code", author = "Robert Martin")
        val fuzzy = BookEntity(id = 2, title = "Clear Code", author = "Other")
        assertEquals(listOf(1L, 2L), SearchRanking.rank(listOf(fuzzy, direct), "clean").map { it.id })
    }

    @Test fun shortQueriesDoNotUseFuzzyMatching() {
        val book = BookEntity(id = 1, title = "Clean Code", author = "Robert Martin")
        assertEquals(emptyList<Long>(), SearchRanking.rank(listOf(book), "zz").map { it.id })
    }
}