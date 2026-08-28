package com.venkateshgowda.personallibrary.data

object SearchRanking {
    fun rank(books: List<BookEntity>, query: String, threshold: Int = 70): List<BookEntity> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) return books
        val direct = books.filter { book ->
            listOf(book.title, book.author, book.isbn.orEmpty(), book.category.orEmpty()).any { it.lowercase().contains(normalizedQuery) }
        }
        if (normalizedQuery.length < 3) return direct.sortedBy { it.title.lowercase() }
        val directIds = direct.map { it.id }.toSet()
        val fuzzy = books.asSequence()
            .filterNot { it.id in directIds }
            .map { it to maxOf(wordSimilarity(normalizedQuery, it.title), wordSimilarity(normalizedQuery, it.author)) }
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
            .map { it.first }
            .toList()
        return direct.sortedBy { it.title.lowercase() } + fuzzy
    }

    private fun wordSimilarity(query: String, text: String) = text.lowercase()
        .split(Regex("[^a-z0-9]+"))
        .filter { it.isNotBlank() }
        .maxOfOrNull { similarity(query, it) } ?: 0

    private fun similarity(left: String, right: String): Int {
        if (left == right) return 100
        val matrix = Array(left.length + 1) { IntArray(right.length + 1) }
        for (index in left.indices) matrix[index + 1][0] = index + 1
        for (index in right.indices) matrix[0][index + 1] = index + 1
        for (leftIndex in left.indices) for (rightIndex in right.indices) {
            matrix[leftIndex + 1][rightIndex + 1] = minOf(
                matrix[leftIndex][rightIndex + 1] + 1,
                matrix[leftIndex + 1][rightIndex] + 1,
                matrix[leftIndex][rightIndex] + if (left[leftIndex] == right[rightIndex]) 0 else 1
            )
        }
        return ((1.0 - matrix[left.length][right.length].toDouble() / maxOf(left.length, right.length)) * 100).toInt()
    }
}