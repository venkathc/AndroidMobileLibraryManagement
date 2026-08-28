package com.venkateshgowda.personallibrary.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.withTransaction
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.zip.ZipFile

class LegacyImportService(private val context: Context, private val database: LibraryDatabase) {
    suspend fun importArchive(uri: Uri): Int {
        require(database.bookDao().count() == 0) { "Legacy import is available only for an empty catalogue." }
        val archive = File.createTempFile("legacy", ".zip", context.cacheDir)
        val staging = File(context.cacheDir, "legacy-${System.nanoTime()}")
        try {
            context.contentResolver.openInputStream(uri)?.use { input -> archive.outputStream().use { output ->
                var total = 0L; val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) { val read = input.read(buffer); if (read < 0) break; total += read; require(total <= 500L * 1024 * 1024) { "Archive exceeds the 500 MB limit." }; output.write(buffer, 0, read) }
            } } ?: error("Could not read the selected archive.")
            extract(archive, staging)
            val legacyFile = File(staging, "library.db")
            require(legacyFile.isFile) { "Archive must contain root-level library.db." }
            return SQLiteDatabase.openDatabase(legacyFile.path, null, SQLiteDatabase.OPEN_READONLY).use { legacy ->
                require(legacy.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }) { "Legacy database failed its integrity check." }
                val tables = legacy.tables()
                require("books" in tables) { "Legacy database does not contain books." }
                val books = readBooks(legacy, staging, tables)
                val isbns = books.mapNotNull { it.book.isbn }; require(isbns.distinct().size == isbns.size) { "Legacy archive contains duplicate ISBNs." }
                database.withTransaction {
                    val idMap = books.associate { it.legacyId to database.bookDao().insert(it.book) }
                    database.bookDao().insertImages(books.flatMap { record -> record.images.mapIndexed { position, path -> BookImageEntity(bookId = idMap.getValue(record.legacyId), path = path, position = position) } })
                    importCatalog(legacy, tables, idMap)
                    importWishlist(legacy, tables)
                    importLoans(legacy, tables, idMap)
                }
                books.size
            }
        } finally { archive.delete(); staging.deleteRecursively() }
    }

    private fun extract(archive: File, directory: File) = ZipFile(archive).use { zip ->
        require(zip.size() <= 10_000) { "Archive has too many entries." }
        var total = 0L
        zip.entries().asSequence().forEach { entry ->
            val name = entry.name.replace('\\', '/')
            require(!name.startsWith('/') && !name.contains("../")) { "Archive contains an unsafe file path." }
            require(entry.size == -1L || entry.size <= 20L * 1024 * 1024) { "Archive entry exceeds 20 MB." }
            if (!entry.isDirectory) { val target = File(directory, name); target.parentFile?.mkdirs(); zip.getInputStream(entry).use { input -> target.outputStream().use(input::copyTo) }; total += target.length(); require(total <= 1024L * 1024 * 1024) { "Archive expands beyond 1 GB." } }
        }
    }

    private data class LegacyBook(val legacyId: Long, val book: BookEntity, val images: List<String>)
    private fun readBooks(db: SQLiteDatabase, staging: File, tables: Set<String>): List<LegacyBook> {
        val additionalImages = mutableMapOf<Long, MutableList<Pair<Int, String>>>()
        if ("book_images" in tables) db.rawQuery("SELECT * FROM book_images ORDER BY position, id", null).use { c -> while (c.moveToNext()) c.optional("image_path")?.let { path -> copyCover(path, staging)?.let { copied -> additionalImages.getOrPut(c.long("book_id")) { mutableListOf() }.add((c.integer("position")) to copied) } } }
        return db.rawQuery("SELECT * FROM books", null).use { c -> buildList { while (c.moveToNext()) {
        val title = c.text("book_name").trim(); val author = c.text("author").trim(); if (title.isBlank() || author.isBlank()) continue
        val paths = mutableListOf<String>(); c.optional("cover_image_path")?.let { copyCover(it, staging)?.let(paths::add) }
        additionalImages[c.long("id")]?.sortedBy { it.first }?.mapTo(paths) { it.second }
        val distinctPaths = paths.distinct()
        add(LegacyBook(c.long("id"), BookEntity(title = title, author = author, pricePaise = (c.number("price") * 100).toLong(), category = c.optional("category"), isbn = isbn(c.optional("isbn")), coverImagePath = distinctPaths.firstOrNull(), readingStatus = c.optional("reading_status") ?: "Unread", favourite = c.integer("is_favourite") != 0, publisher = c.optional("publisher"), purchaseDate = c.optional("purchase_date"), language = c.optional("language"), rating = c.optionalInt("rating"), personalReview = c.optional("personal_review"), notes = c.optional("notes"), createdAtMillis = time(c.optional("created_at")), updatedAtMillis = time(c.optional("updated_at"))), distinctPaths))
    } } }
    }

    private suspend fun importCatalog(db: SQLiteDatabase, tables: Set<String>, books: Map<Long, Long>) {
        val dao = database.catalogDao(); val tags = mutableMapOf<Long, Long>(); val collections = mutableMapOf<Long, Long>()
        if ("tags" in tables) db.rawQuery("SELECT * FROM tags", null).use { c -> while (c.moveToNext()) tags[c.long("id")] = dao.insertTag(TagEntity(name = c.text("name"))) }
        if ("collections" in tables) db.rawQuery("SELECT * FROM collections", null).use { c -> while (c.moveToNext()) collections[c.long("id")] = dao.insertCollection(CollectionEntity(name = c.text("name"), description = c.optional("description"))) }
        if ("book_tags" in tables) db.rawQuery("SELECT * FROM book_tags", null).use { c -> while (c.moveToNext()) { val book = books[c.long("book_id")]; val tag = tags[c.long("tag_id")]; if (book != null && tag != null) dao.insertTagAssignments(listOf(BookTagEntity(book, tag))) } }
        if ("book_collections" in tables) db.rawQuery("SELECT * FROM book_collections", null).use { c -> while (c.moveToNext()) { val book = books[c.long("book_id")]; val collection = collections[c.long("collection_id")]; if (book != null && collection != null) dao.insertCollectionAssignments(listOf(BookCollectionEntity(book, collection))) } }
    }
    private suspend fun importWishlist(db: SQLiteDatabase, tables: Set<String>) { if ("wishlist" !in tables) return; val values = db.rawQuery("SELECT * FROM wishlist", null).use { c -> buildList { while(c.moveToNext()) add(WishlistEntity(title = c.text("book_name"), author = c.optional("author"), category = c.optional("category"), expectedPricePaise = (c.number("expected_price") * 100).toLong(), priority = c.optional("priority") ?: "Medium", status = c.optional("status") ?: "Planned", notes = c.optional("notes"), expectedPurchaseDate = c.optional("expected_purchase_date"), createdAtMillis = time(c.optional("created_at")), updatedAtMillis = time(c.optional("updated_at")))) } }; database.wishlistDao().insertAll(values) }
    private suspend fun importLoans(db: SQLiteDatabase, tables: Set<String>, books: Map<Long, Long>) { if ("loans" !in tables) return; val values = db.rawQuery("SELECT * FROM loans", null).use { c -> buildList { while(c.moveToNext()) { val book = books[c.long("book_id")] ?: continue; add(LoanEntity(bookId = book, borrowerName = c.text("borrower_name"), borrowerContact = c.optional("borrower_contact"), borrowedDate = c.text("borrowed_date"), expectedReturnDate = c.optional("expected_return_date"), actualReturnDate = c.optional("actual_return_date"), notes = c.optional("notes"), createdAtMillis = time(c.optional("created_at")))) } } }; database.loanDao().insertAll(values) }
    private fun copyCover(path: String, staging: File): String? { val source = File(staging, "book_covers/${path.substringAfterLast('/')}"); if (!source.isFile) return null; val target = File(context.filesDir, "book_covers/${System.nanoTime()}-${source.name}").apply { parentFile?.mkdirs() }; source.copyTo(target); return target.absolutePath }
    private fun SQLiteDatabase.tables() = rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { c -> buildSet { while(c.moveToNext()) add(c.getString(0)) } }
    private fun Cursor.column(name: String) = getColumnIndex(name)
    private fun Cursor.text(name: String) = optional(name) ?: ""
    private fun Cursor.optional(name: String) = column(name).takeIf { it >= 0 }?.let { getString(it) }
    private fun Cursor.long(name: String) = getLong(column(name))
    private fun Cursor.integer(name: String) = column(name).takeIf { it >= 0 }?.let { getInt(it) } ?: 0
    private fun Cursor.optionalInt(name: String) = column(name).takeIf { it >= 0 && !isNull(it) }?.let { getInt(it) }
    private fun Cursor.number(name: String) = column(name).takeIf { it >= 0 && !isNull(it) }?.let { getDouble(it) } ?: 0.0
    private fun isbn(value: String?) = value?.replace("-", "")?.replace(" ", "")?.uppercase()?.ifBlank { null }
    private fun time(value: String?) = runCatching { LocalDateTime.parse(value?.replace(' ', 'T')).atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli() }.getOrDefault(System.currentTimeMillis())
}