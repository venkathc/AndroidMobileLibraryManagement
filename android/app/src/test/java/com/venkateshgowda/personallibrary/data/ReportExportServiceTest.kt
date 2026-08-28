package com.venkateshgowda.personallibrary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.zip.ZipInputStream

class ReportExportServiceTest {
    @Test fun reportsAndCsvIncludeExpectedValues() {
        val book = BookEntity(title = "Book, One", author = "Author", pricePaise = 12500, favourite = true, readingStatus = "Read")
        val summary = ReportExportService.summary(listOf(book), emptyList(), emptyList(), "2026-08-27")
        assertEquals(12500, summary.investmentPaise)
        assertTrue(ReportExportService.booksCsv(listOf(book)).decodeToString().contains("\"Book, One\""))
    }

    @Test fun workbookContainsBookAndWishlistSheets() {
        val archive = ReportExportService.workbook(emptyList(), emptyList())
        val names = ZipInputStream(archive.inputStream()).use { stream -> buildList { while (true) { val entry = stream.nextEntry ?: break; add(entry.name) } } }
        assertTrue("xl/worksheets/sheet1.xml" in names && "xl/worksheets/sheet2.xml" in names && "xl/worksheets/sheet3.xml" in names)
    }

    @Test fun loansCsvIncludesBookAndDerivedStatus() {
        val book = BookEntity(id = 1, title = "Loaned book", author = "Author", pricePaise = 0)
        val loan = LoanEntity(bookId = 1, borrowerName = "Reader", borrowedDate = "2026-08-01", expectedReturnDate = "2026-08-20")
        val csv = ReportExportService.loansCsv(listOf(book), listOf(loan), "2026-08-27").decodeToString()
        assertTrue(csv.contains("\"Loaned book\"") && csv.contains("\"Reader\"") && csv.contains("\"Overdue\""))
    }

    @Test fun advancedReportGroupsSpendingAndDerivedStatuses() {
        val books = listOf(
            BookEntity(title = "One", author = "Author", category = "Fiction", publisher = "Publisher", purchaseDate = "2026-08-01", pricePaise = 12000, rating = 5),
            BookEntity(title = "Two", author = "Author", category = "Fiction", publisher = "Publisher", purchaseDate = "2026-08-05", pricePaise = 8000, rating = 3)
        )
        val loans = listOf(
            LoanEntity(bookId = 1, borrowerName = "A", borrowedDate = "2026-07-20", expectedReturnDate = "2026-08-01"),
            LoanEntity(bookId = 2, borrowerName = "B", borrowedDate = "2026-07-20", actualReturnDate = "2026-08-20")
        )
        val wishlist = listOf(WishlistEntity(title = "Next", priority = "High", status = "Planned"))

        val report = ReportExportService.advancedSummary(books, loans, wishlist, "2026-08-27")

        assertEquals(20000, report.investmentByCategory.single().value)
        assertEquals("2026-08", report.investmentByPurchaseMonth.single().label)
        assertEquals(2, report.booksByAuthor.single().value)
        assertTrue(report.loansByStatus.any { it.label == "Overdue" && it.value == 1L })
        assertTrue(report.wishlistByPriority.any { it.label == "High" && it.value == 1L })
    }
}