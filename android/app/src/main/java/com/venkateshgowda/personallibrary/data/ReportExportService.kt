package com.venkateshgowda.personallibrary.data

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ReportSummary(
    val books: Int,
    val investmentPaise: Long,
    val favourites: Int,
    val ratedBooks: Int,
    val activeLoans: Int,
    val overdueLoans: Int,
    val plannedWishlistPaise: Long,
    val byCategory: List<Pair<String, Int>>,
    val byStatus: List<Pair<String, Int>>
)

data class ReportBreakdown(val label: String, val value: Long)

data class AdvancedReport(
    val booksByAuthor: List<ReportBreakdown>,
    val investmentByAuthor: List<ReportBreakdown>,
    val investmentByCategory: List<ReportBreakdown>,
    val investmentByPublisher: List<ReportBreakdown>,
    val investmentByPurchaseMonth: List<ReportBreakdown>,
    val investmentByPurchaseYear: List<ReportBreakdown>,
    val ratings: List<ReportBreakdown>,
    val loansByStatus: List<ReportBreakdown>,
    val wishlistByPriority: List<ReportBreakdown>,
    val wishlistByStatus: List<ReportBreakdown>
)

object ReportExportService {
    fun summary(books: List<BookEntity>, loans: List<LoanEntity>, wishlist: List<WishlistEntity>, today: String): ReportSummary = ReportSummary(
        books = books.size,
        investmentPaise = books.sumOf { it.pricePaise },
        favourites = books.count { it.favourite },
        ratedBooks = books.count { it.rating != null },
        activeLoans = loans.count { it.actualReturnDate == null },
        overdueLoans = loans.count { it.actualReturnDate == null && it.expectedReturnDate != null && it.expectedReturnDate < today },
        plannedWishlistPaise = wishlist.filter { it.status == "Planned" }.sumOf { it.expectedPricePaise },
        byCategory = books.groupingBy { it.category ?: "Uncategorized" }.eachCount().entries.sortedByDescending { it.value }.map { it.key to it.value },
        byStatus = books.groupingBy { it.readingStatus }.eachCount().entries.sortedByDescending { it.value }.map { it.key to it.value }
    )

    fun advancedSummary(books: List<BookEntity>, loans: List<LoanEntity>, wishlist: List<WishlistEntity>, today: String): AdvancedReport = AdvancedReport(
        booksByAuthor = countBreakdown(books.map { it.author }),
        investmentByAuthor = amountBreakdown(books) { it.author },
        investmentByCategory = amountBreakdown(books) { it.category ?: "Uncategorized" },
        investmentByPublisher = amountBreakdown(books) { it.publisher ?: "Unknown publisher" },
        investmentByPurchaseMonth = amountBreakdown(books.filter { !it.purchaseDate.isNullOrBlank() }) { it.purchaseDate!!.take(7) },
        investmentByPurchaseYear = amountBreakdown(books.filter { !it.purchaseDate.isNullOrBlank() }) { it.purchaseDate!!.take(4) },
        ratings = (1..5).map { rating -> ReportBreakdown("$rating stars", books.count { it.rating == rating }.toLong()) },
        loansByStatus = countBreakdown(loans.map { loan -> when {
            loan.actualReturnDate != null -> "Returned"
            loan.expectedReturnDate != null && loan.expectedReturnDate < today -> "Overdue"
            else -> "Active"
        } }),
        wishlistByPriority = countBreakdown(wishlist.map { it.priority }),
        wishlistByStatus = countBreakdown(wishlist.map { it.status })
    )

    fun booksCsv(books: List<BookEntity>): ByteArray = csv(buildList<List<String>> {
        add(listOf("Title", "Author", "Category", "Price INR", "Purchase Date", "Publisher", "ISBN", "Language", "Rating", "Reading Status", "Favourite", "Review", "Notes"))
        addAll(books.map { listOf(it.title, it.author, it.category.orEmpty(), "%.2f".format(it.pricePaise / 100.0), it.purchaseDate.orEmpty(), it.publisher.orEmpty(), it.isbn.orEmpty(), it.language.orEmpty(), it.rating?.toString().orEmpty(), it.readingStatus, it.favourite.toString(), it.personalReview.orEmpty(), it.notes.orEmpty()) })
    })

    fun wishlistCsv(items: List<WishlistEntity>): ByteArray = csv(buildList<List<String>> {
        add(listOf("Title", "Author", "Category", "Expected Price INR", "Priority", "Expected Purchase Date", "Status", "Notes"))
        addAll(items.map { listOf(it.title, it.author.orEmpty(), it.category.orEmpty(), "%.2f".format(it.expectedPricePaise / 100.0), it.priority, it.expectedPurchaseDate.orEmpty(), it.status, it.notes.orEmpty()) })
    })

    fun loansCsv(books: List<BookEntity>, loans: List<LoanEntity>, today: String): ByteArray = csv(buildList<List<String>> {
        add(listOf("Book", "Author", "Borrower", "Contact", "Borrowed Date", "Expected Return Date", "Actual Return Date", "Status", "Notes"))
        addAll(loans.map { loan ->
            val book = books.firstOrNull { it.id == loan.bookId }
            listOf(book?.title ?: "Deleted book", book?.author.orEmpty(), loan.borrowerName, loan.borrowerContact.orEmpty(), loan.borrowedDate, loan.expectedReturnDate.orEmpty(), loan.actualReturnDate.orEmpty(), loanStatus(loan, today), loan.notes.orEmpty())
        })
    })

    fun workbook(books: List<BookEntity>, wishlist: List<WishlistEntity>, loans: List<LoanEntity> = emptyList(), today: String = "9999-12-31"): ByteArray = ByteArrayOutputStream().use { bytes ->
        ZipOutputStream(bytes).use { zip ->
            entry(zip, "[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
            entry(zip, "_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            entry(zip, "xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Books" sheetId="1" r:id="rId1"/><sheet name="Wishlist" sheetId="2" r:id="rId2"/><sheet name="Loans" sheetId="3" r:id="rId3"/></sheets></workbook>""")
            entry(zip, "xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/></Relationships>""")
            entry(zip, "xl/worksheets/sheet1.xml", worksheet(buildList<List<String>> { add(listOf("Title", "Author", "Category", "Price INR", "ISBN", "Status")); addAll(books.map { listOf(it.title, it.author, it.category.orEmpty(), "%.2f".format(it.pricePaise / 100.0), it.isbn.orEmpty(), it.readingStatus) }) }))
            entry(zip, "xl/worksheets/sheet2.xml", worksheet(buildList<List<String>> { add(listOf("Title", "Author", "Expected Price INR", "Priority", "Status")); addAll(wishlist.map { listOf(it.title, it.author.orEmpty(), "%.2f".format(it.expectedPricePaise / 100.0), it.priority, it.status) }) }))
            entry(zip, "xl/worksheets/sheet3.xml", worksheet(buildList<List<String>> { add(listOf("Book", "Author", "Borrower", "Contact", "Borrowed Date", "Expected Return Date", "Actual Return Date", "Status", "Notes")); addAll(loans.map { loan -> val book = books.firstOrNull { it.id == loan.bookId }; listOf(book?.title ?: "Deleted book", book?.author.orEmpty(), loan.borrowerName, loan.borrowerContact.orEmpty(), loan.borrowedDate, loan.expectedReturnDate.orEmpty(), loan.actualReturnDate.orEmpty(), loanStatus(loan, today), loan.notes.orEmpty()) }) }))
        }
        bytes.toByteArray()
    }

    private fun csv(rows: List<List<String>>) = rows.joinToString("\n") { row -> row.joinToString(",") { value -> "\"${value.replace("\"", "\"\"")}\"" } }.toByteArray()
    private fun countBreakdown(labels: List<String>) = labels.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { ReportBreakdown(it.key, it.value.toLong()) }
    private fun amountBreakdown(books: List<BookEntity>, label: (BookEntity) -> String) = books.groupBy(label).map { (name, matchingBooks) -> ReportBreakdown(name, matchingBooks.sumOf { it.pricePaise }) }.sortedByDescending { it.value }
    private fun loanStatus(loan: LoanEntity, today: String) = when { loan.actualReturnDate != null -> "Returned"; loan.expectedReturnDate != null && loan.expectedReturnDate < today -> "Overdue"; else -> "Active" }
    private fun worksheet(rows: List<List<String>>) = """<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>${rows.mapIndexed { row, values -> "<row r=\"${row + 1}\">${values.joinToString("") { "<c t=\"inlineStr\"><is><t>${xml(it)}</t></is></c>" }}</row>" }.joinToString("")}</sheetData></worksheet>"""
    private fun xml(value: String) = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    private fun entry(zip: ZipOutputStream, name: String, contents: String) { zip.putNextEntry(ZipEntry(name)); zip.write(contents.toByteArray()); zip.closeEntry() }
}