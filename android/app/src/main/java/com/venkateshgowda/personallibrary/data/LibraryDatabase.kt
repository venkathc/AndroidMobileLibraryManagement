package com.venkateshgowda.personallibrary.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Entity(tableName = "books", indices = [Index(value = ["isbn"], unique = true), Index(value = ["libraryId"])])
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "1") val libraryId: Long = 1,
    val title: String,
    val author: String,
    val pricePaise: Long = 0,
    val category: String? = null,
    val isbn: String? = null,
    val barcode: String? = null,
    val coverImagePath: String? = null,
    val readingStatus: String = "Unread",
    val favourite: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val publisher: String? = null,
    val purchaseDate: String? = null,
    val language: String? = null,
    val rating: Int? = null,
    val personalReview: String? = null,
    val notes: String? = null
)

@Entity(tableName = "libraries", indices = [Index(value = ["name"], unique = true)])
data class LibraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val imagePath: String? = null,
    val owner: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String? = null,
    val email: String? = null,
    val avatarPath: String? = null,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val role: String = UserRole.Member.name,
    val accountStatus: String = "Active",
    val forcePasswordReset: Boolean = false,
    val lastLoginAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class UserRole(val label: String, val permissions: Set<LibraryPermission>) {
    Owner("Owner", LibraryPermission.values().toSet()),
    Admin("Admin", setOf(LibraryPermission.ViewBooks, LibraryPermission.SearchBooks, LibraryPermission.AddBooks, LibraryPermission.EditBooks, LibraryPermission.DeleteBooks, LibraryPermission.ManageCategories, LibraryPermission.ManageTags, LibraryPermission.ManageUsers, LibraryPermission.ApproveRequests, LibraryPermission.ViewReports)),
    Librarian("Librarian", setOf(LibraryPermission.ViewBooks, LibraryPermission.SearchBooks, LibraryPermission.AddBooks, LibraryPermission.EditBooks, LibraryPermission.ManageLoans, LibraryPermission.ViewReports)),
    Member("Member", setOf(LibraryPermission.ViewBooks, LibraryPermission.SearchBooks, LibraryPermission.AddPersonalNotes, LibraryPermission.AddRatings, LibraryPermission.ManageWishlist, LibraryPermission.BorrowBooks)),
    Guest("Guest", setOf(LibraryPermission.ViewBooks, LibraryPermission.SearchBooks));

    companion object {
        fun fromStored(value: String, isAdmin: Boolean) = when (value) {
            "Administrator" -> Owner
            "User" -> Member
            else -> values().firstOrNull { it.name == value } ?: if (isAdmin) Owner else Member
        }
    }
}

val UserEntity.userRole: UserRole
    get() = UserRole.fromStored(role, isAdmin)

enum class LibraryPermission {
    ViewBooks, SearchBooks, AddBooks, EditBooks, DeleteBooks, ManageCategories, ManageTags,
    ManageLoans, BorrowBooks, ManageWishlist, AddPersonalNotes, AddRatings, ManageUsers,
    ApproveRequests, ManageLibrarySettings, ImportExport, DeleteLibrary, TransferOwnership,
    ViewReports
}

fun UserRole.can(permission: LibraryPermission) = permission in permissions

@Entity(tableName = "roles")
data class RoleEntity(@PrimaryKey val id: String, val displayName: String, val isSystemRole: Boolean = true)

@Entity(tableName = "permissions")
data class PermissionEntity(@PrimaryKey val id: String, val displayName: String)

@Entity(tableName = "library_memberships", indices = [Index(value = ["libraryId"]), Index(value = ["userId"]), Index(value = ["libraryId", "userId"], unique = true)])
data class LibraryMembershipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libraryId: Long,
    val userId: Long,
    val role: String,
    val status: String = "Active",
    val joinedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "membership_requests", indices = [Index(value = ["libraryId"]), Index(value = ["email"])])
data class MembershipRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libraryId: Long,
    val email: String,
    val inviteCode: String,
    val status: String = "Pending",
    val requestedAtMillis: Long = System.currentTimeMillis(),
    val respondedAtMillis: Long? = null
)

@Entity(tableName = "audit_logs", indices = [Index(value = ["libraryId"]), Index(value = ["createdAtMillis"])])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libraryId: Long,
    val actorUserId: Long,
    val action: String,
    val detail: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

object PasswordHasher {
    private const val Iterations = 210_000
    private const val KeyBits = 256

    fun hash(password: CharArray): String {
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derive(password, salt))
    }

    fun matches(password: CharArray, stored: String): Boolean = runCatching {
        val parts = stored.split(":")
        parts.size == 2 && derive(password, Base64.getDecoder().decode(parts[0])).contentEquals(Base64.getDecoder().decode(parts[1]))
    }.getOrDefault(false)

    private fun derive(password: CharArray, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password, salt, Iterations, KeyBits))
            .encoded
}

@Entity(
    tableName = "book_images",
    indices = [Index(value = ["bookId"])],
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class BookImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val path: String,
    val position: Int
)

@Entity(
    tableName = "loans",
    indices = [Index(value = ["bookId"])],
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val borrowerName: String,
    val borrowerContact: String? = null,
    val borrowedDate: String,
    val expectedReturnDate: String? = null,
    val actualReturnDate: String? = null,
    val notes: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist", indices = [Index(value = ["libraryId"])])
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "1") val libraryId: Long = 1,
    val title: String,
    val author: String? = null,
    val category: String? = null,
    val expectedPricePaise: Long = 0,
    val priority: String = "Medium",
    val status: String = "Planned",
    val notes: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val expectedPurchaseDate: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

@Entity(tableName = "collections", indices = [Index(value = ["name"], unique = true)])
data class CollectionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val description: String? = null)

@Entity(tableName = "library_categories", indices = [Index(value = ["libraryId", "name"], unique = true)])
data class LibraryCategoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val libraryId: Long, val name: String)

@Entity(tableName = "library_languages", indices = [Index(value = ["libraryId", "name"], unique = true)])
data class LibraryLanguageEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val libraryId: Long, val name: String)

@Entity(tableName = "book_tags", primaryKeys = ["bookId", "tagId"])
data class BookTagEntity(val bookId: Long, val tagId: Long)

@Entity(tableName = "book_collections", primaryKeys = ["bookId", "collectionId"])
data class BookCollectionEntity(val bookId: Long, val collectionId: Long)

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: BookEntity): Long

    @Insert
    suspend fun insertAll(books: List<BookEntity>)

    @Query("SELECT * FROM books")
    suspend fun allBooks(): List<BookEntity>

    @Update
    suspend fun update(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: Long)

    @Query("DELETE FROM book_tags WHERE bookId = :bookId")
    suspend fun deleteTagAssignments(bookId: Long)

    @Query("DELETE FROM book_collections WHERE bookId = :bookId")
    suspend fun deleteCollectionAssignments(bookId: Long)

    @Transaction
    suspend fun delete(bookId: Long) {
        deleteTagAssignments(bookId)
        deleteCollectionAssignments(bookId)
        deleteBook(bookId)
    }

    @Query("SELECT * FROM books ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE libraryId = :libraryId ORDER BY createdAtMillis DESC")
    fun observeForLibrary(libraryId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE lower(title) LIKE '%' || lower(:query) || '%' OR lower(author) LIKE '%' || lower(:query) || '%' OR lower(COALESCE(isbn, '')) LIKE '%' || lower(:query) || '%' ORDER BY title COLLATE NOCASE")
    fun observeMatching(query: String): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE libraryId = :libraryId")
    fun observeCountForLibrary(libraryId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(pricePaise), 0) FROM books")
    fun observeInvestmentPaise(): Flow<Long>

    @Query("SELECT COALESCE(SUM(pricePaise), 0) FROM books WHERE libraryId = :libraryId")
    fun observeInvestmentPaiseForLibrary(libraryId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int

    @Query("SELECT id FROM books WHERE isbn = :isbn AND id != :excludedBookId LIMIT 1")
    suspend fun findOtherBookWithIsbn(isbn: String, excludedBookId: Long): Long?

    @Insert
    suspend fun insertImages(images: List<BookImageEntity>)

    @Query("SELECT * FROM book_images WHERE bookId = :bookId ORDER BY position, id")
    fun observeImagesForBook(bookId: Long): Flow<List<BookImageEntity>>

    @Query("SELECT * FROM book_images WHERE bookId = :bookId ORDER BY position, id")
    suspend fun imagesForBook(bookId: Long): List<BookImageEntity>

    @Query("DELETE FROM book_images WHERE id = :imageId")
    suspend fun deleteImage(imageId: Long)

    @Update
    suspend fun updateImages(images: List<BookImageEntity>)

    @Transaction
    suspend fun moveImage(book: BookEntity, imageId: Long, direction: Int) {
        val images = imagesForBook(book.id).toMutableList()
        val index = images.indexOfFirst { it.id == imageId }
        val target = index + direction
        if (index < 0 || target !in images.indices) return
        val moved = images.removeAt(index)
        images.add(target, moved)
        val reordered = images.mapIndexed { position, image -> image.copy(position = position) }
        updateImages(reordered)
        update(book.copy(coverImagePath = reordered.firstOrNull()?.path, updatedAtMillis = System.currentTimeMillis()))
    }

    @Transaction
    suspend fun deleteImageAndUpdateBook(book: BookEntity, image: BookImageEntity) {
        deleteImage(image.id)
        val remaining = imagesForBook(book.id)
        update(book.copy(coverImagePath = remaining.firstOrNull()?.path, updatedAtMillis = System.currentTimeMillis()))
    }
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun findById(userId: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM library_memberships WHERE userId = :userId")
    suspend fun deleteMembershipsForUser(userId: Long)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserRecord(userId: Long)

    @Transaction
    suspend fun deleteUser(userId: Long) {
        deleteMembershipsForUser(userId)
        deleteUserRecord(userId)
    }

    @Query("SELECT * FROM users ORDER BY username COLLATE NOCASE")
    fun observeAll(): Flow<List<UserEntity>>
}

@Dao
interface LibraryDao {
    @Insert
    suspend fun insert(library: LibraryEntity): Long

    @Update
    suspend fun update(library: LibraryEntity)

    @Query("DELETE FROM book_tags WHERE bookId IN (SELECT id FROM books WHERE libraryId = :libraryId)")
    suspend fun deleteBookTagsForLibrary(libraryId: Long)

    @Query("DELETE FROM book_collections WHERE bookId IN (SELECT id FROM books WHERE libraryId = :libraryId)")
    suspend fun deleteBookCollectionsForLibrary(libraryId: Long)

    @Query("DELETE FROM books WHERE libraryId = :libraryId")
    suspend fun deleteBooksForLibrary(libraryId: Long)

    @Query("DELETE FROM wishlist WHERE libraryId = :libraryId")
    suspend fun deleteWishlistForLibrary(libraryId: Long)

    @Query("DELETE FROM library_categories WHERE libraryId = :libraryId")
    suspend fun deleteCategoriesForLibrary(libraryId: Long)

    @Query("DELETE FROM library_languages WHERE libraryId = :libraryId")
    suspend fun deleteLanguagesForLibrary(libraryId: Long)

    @Query("DELETE FROM libraries WHERE id = :libraryId")
    suspend fun deleteLibraryRecord(libraryId: Long)

    @Transaction
    suspend fun deleteLibrary(libraryId: Long) {
        deleteBookTagsForLibrary(libraryId)
        deleteBookCollectionsForLibrary(libraryId)
        deleteBooksForLibrary(libraryId)
        deleteWishlistForLibrary(libraryId)
        deleteCategoriesForLibrary(libraryId)
        deleteLanguagesForLibrary(libraryId)
        deleteLibraryRecord(libraryId)
    }

    @Query("SELECT * FROM libraries ORDER BY createdAtMillis DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<LibraryEntity>>
}

@Dao
interface MembershipDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsert(membership: LibraryMembershipEntity): Long

    @Update
    suspend fun update(membership: LibraryMembershipEntity)

    @Query("SELECT * FROM library_memberships WHERE libraryId = :libraryId ORDER BY joinedAtMillis")
    fun observeForLibrary(libraryId: Long): Flow<List<LibraryMembershipEntity>>

    @Query("SELECT * FROM library_memberships WHERE libraryId = :libraryId AND userId = :userId LIMIT 1")
    suspend fun find(libraryId: Long, userId: Long): LibraryMembershipEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM library_memberships WHERE userId = :userId AND role = 'Owner')")
    suspend fun hasOwnerMembership(userId: Long): Boolean

    @Query("DELETE FROM library_memberships WHERE id = :membershipId")
    suspend fun delete(membershipId: Long)
}

@Dao
interface MembershipRequestDao {
    @Insert
    suspend fun insert(request: MembershipRequestEntity): Long

    @Update
    suspend fun update(request: MembershipRequestEntity)

    @Query("SELECT * FROM membership_requests WHERE libraryId = :libraryId ORDER BY requestedAtMillis DESC")
    fun observeForLibrary(libraryId: Long): Flow<List<MembershipRequestEntity>>
}

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insert(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs WHERE libraryId = :libraryId ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecent(libraryId: Long, limit: Int = 50): Flow<List<AuditLogEntity>>
}

@Dao
interface LoanDao {
    @Insert
    suspend fun insert(loan: LoanEntity): Long

    @Insert
    suspend fun insertAll(loans: List<LoanEntity>)

    @Update
    suspend fun update(loan: LoanEntity)

    @Query("SELECT * FROM loans")
    suspend fun allLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans ORDER BY actualReturnDate IS NULL DESC, expectedReturnDate ASC")
    fun observeAll(): Flow<List<LoanEntity>>

    @Query("SELECT loans.* FROM loans INNER JOIN books ON books.id = loans.bookId WHERE books.libraryId = :libraryId ORDER BY loans.actualReturnDate IS NULL DESC, loans.expectedReturnDate ASC")
    fun observeForLibrary(libraryId: Long): Flow<List<LoanEntity>>

    @Query("SELECT COUNT(*) FROM loans WHERE actualReturnDate IS NULL")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans INNER JOIN books ON books.id = loans.bookId WHERE books.libraryId = :libraryId AND loans.actualReturnDate IS NULL")
    fun observeActiveCountForLibrary(libraryId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans WHERE actualReturnDate IS NULL AND expectedReturnDate < :today")
    fun observeOverdueCount(today: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans INNER JOIN books ON books.id = loans.bookId WHERE books.libraryId = :libraryId AND loans.actualReturnDate IS NULL AND loans.expectedReturnDate < :today")
    fun observeOverdueCountForLibrary(libraryId: Long, today: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans WHERE actualReturnDate IS NULL AND expectedReturnDate BETWEEN :today AND :dueDate")
    fun observeDueSoonCount(today: String, dueDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans INNER JOIN books ON books.id = loans.bookId WHERE books.libraryId = :libraryId AND loans.actualReturnDate IS NULL AND loans.expectedReturnDate BETWEEN :today AND :dueDate")
    fun observeDueSoonCountForLibrary(libraryId: Long, today: String, dueDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans WHERE bookId = :bookId AND actualReturnDate IS NULL")
    suspend fun activeCountForBook(bookId: Long): Int

    @Query("UPDATE loans SET actualReturnDate = :returnedDate WHERE id = :loanId AND actualReturnDate IS NULL")
    suspend fun markReturned(loanId: Long, returnedDate: String): Int
}

@Dao
interface WishlistDao {
    @Insert
    suspend fun insert(item: WishlistEntity): Long

    @Insert
    suspend fun insertAll(items: List<WishlistEntity>)

    @Query("SELECT * FROM wishlist")
    suspend fun allItems(): List<WishlistEntity>

    @Update
    suspend fun update(item: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE id = :itemId")
    suspend fun delete(itemId: Long)

    @Query("SELECT * FROM wishlist ORDER BY CASE priority WHEN 'High' THEN 0 WHEN 'Medium' THEN 1 ELSE 2 END, title COLLATE NOCASE")
    fun observeAll(): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist WHERE libraryId = :libraryId ORDER BY CASE priority WHEN 'High' THEN 0 WHEN 'Medium' THEN 1 ELSE 2 END, title COLLATE NOCASE")
    fun observeForLibrary(libraryId: Long): Flow<List<WishlistEntity>>

    @Query("SELECT COALESCE(SUM(expectedPricePaise), 0) FROM wishlist WHERE status = 'Planned'")
    fun observePlannedCost(): Flow<Long>
}

@Dao
interface CatalogDao {
    @Insert
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Insert
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Insert
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Insert
    suspend fun insertCollections(collections: List<CollectionEntity>): List<Long>

    @Insert
    suspend fun insertCategory(category: LibraryCategoryEntity): Long

    @Insert
    suspend fun insertLanguage(language: LibraryLanguageEntity): Long

    @Update
    suspend fun updateCategory(category: LibraryCategoryEntity)

    @Update
    suspend fun updateLanguage(language: LibraryLanguageEntity)

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM collections ORDER BY name COLLATE NOCASE")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM library_categories WHERE libraryId = :libraryId ORDER BY name COLLATE NOCASE")
    fun observeCategories(libraryId: Long): Flow<List<LibraryCategoryEntity>>

    @Query("SELECT * FROM library_languages WHERE libraryId = :libraryId ORDER BY name COLLATE NOCASE")
    fun observeLanguages(libraryId: Long): Flow<List<LibraryLanguageEntity>>

    @Query("SELECT tags.* FROM tags INNER JOIN book_tags ON tags.id = book_tags.tagId WHERE book_tags.bookId = :bookId ORDER BY tags.name COLLATE NOCASE")
    fun observeTagsForBook(bookId: Long): Flow<List<TagEntity>>

    @Query("SELECT collections.* FROM collections INNER JOIN book_collections ON collections.id = book_collections.collectionId WHERE book_collections.bookId = :bookId ORDER BY collections.name COLLATE NOCASE")
    fun observeCollectionsForBook(bookId: Long): Flow<List<CollectionEntity>>

    @Query("SELECT book_tags.bookId FROM book_tags INNER JOIN tags ON tags.id = book_tags.tagId WHERE lower(tags.name) = lower(:name)")
    fun observeBookIdsForTag(name: String): Flow<List<Long>>

    @Query("SELECT book_collections.bookId FROM book_collections INNER JOIN collections ON collections.id = book_collections.collectionId WHERE lower(collections.name) = lower(:name)")
    fun observeBookIdsForCollection(name: String): Flow<List<Long>>

    @Query("DELETE FROM book_tags WHERE bookId = :bookId")
    suspend fun clearTagAssignments(bookId: Long)

    @Query("DELETE FROM book_collections WHERE bookId = :bookId")
    suspend fun clearCollectionAssignments(bookId: Long)

    @Query("DELETE FROM book_tags WHERE tagId = :tagId")
    suspend fun clearAssignmentsForTag(tagId: Long)

    @Query("DELETE FROM book_collections WHERE collectionId = :collectionId")
    suspend fun clearAssignmentsForCollection(collectionId: Long)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: Long)

    @Query("DELETE FROM collections WHERE id = :collectionId")
    suspend fun deleteCollection(collectionId: Long)

    @Query("DELETE FROM library_categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @Query("DELETE FROM library_languages WHERE id = :languageId")
    suspend fun deleteLanguage(languageId: Long)

    @Insert
    suspend fun insertTagAssignments(assignments: List<BookTagEntity>)

    @Insert
    suspend fun insertCollectionAssignments(assignments: List<BookCollectionEntity>)

    @Transaction
    suspend fun replaceAssignments(bookId: Long, tagIds: List<Long>, collectionIds: List<Long>) {
        clearTagAssignments(bookId)
        clearCollectionAssignments(bookId)
        if (tagIds.isNotEmpty()) insertTagAssignments(tagIds.distinct().map { BookTagEntity(bookId, it) })
        if (collectionIds.isNotEmpty()) insertCollectionAssignments(collectionIds.distinct().map { BookCollectionEntity(bookId, it) })
    }

    @Transaction
    suspend fun deleteTagAndAssignments(tagId: Long) {
        clearAssignmentsForTag(tagId)
        deleteTag(tagId)
    }

    @Transaction
    suspend fun deleteCollectionAndAssignments(collectionId: Long) {
        clearAssignmentsForCollection(collectionId)
        deleteCollection(collectionId)
    }
}

@Database(entities = [BookEntity::class, BookImageEntity::class, LoanEntity::class, WishlistEntity::class, TagEntity::class, CollectionEntity::class, LibraryCategoryEntity::class, LibraryLanguageEntity::class, BookTagEntity::class, BookCollectionEntity::class, LibraryEntity::class, UserEntity::class, RoleEntity::class, PermissionEntity::class, LibraryMembershipEntity::class, MembershipRequestEntity::class, AuditLogEntity::class], version = 19, exportSchema = false)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun libraryDao(): LibraryDao
    abstract fun loanDao(): LoanDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun catalogDao(): CatalogDao
    abstract fun userDao(): UserDao
    abstract fun membershipDao(): MembershipDao
    abstract fun membershipRequestDao(): MembershipRequestDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE books ADD COLUMN publisher TEXT")
                database.execSQL("ALTER TABLE books ADD COLUMN purchaseDate TEXT")
                database.execSQL("ALTER TABLE books ADD COLUMN language TEXT")
                database.execSQL("ALTER TABLE books ADD COLUMN rating INTEGER")
                database.execSQL("ALTER TABLE books ADD COLUMN personalReview TEXT")
                database.execSQL("ALTER TABLE books ADD COLUMN notes TEXT")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_collections_name ON collections(name)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_books_isbn ON books(isbn)")
                database.execSQL("CREATE TABLE IF NOT EXISTS book_images (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, bookId INTEGER NOT NULL, path TEXT NOT NULL, position INTEGER NOT NULL, FOREIGN KEY(bookId) REFERENCES books(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_book_images_bookId ON book_images(bookId)")
                database.execSQL("INSERT INTO book_images (bookId, path, position) SELECT id, coverImagePath, 0 FROM books WHERE coverImagePath IS NOT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE wishlist ADD COLUMN expectedPurchaseDate TEXT")
                database.execSQL("ALTER TABLE wishlist ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS libraries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT, imagePath TEXT, owner TEXT NOT NULL, createdAtMillis INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_libraries_name ON libraries(name)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("INSERT OR IGNORE INTO libraries (id, name, description, imagePath, owner, createdAtMillis) VALUES (1, 'Venkatesh Gowdas Library', NULL, NULL, 'Owner', 0)")
                database.execSQL("ALTER TABLE books ADD COLUMN libraryId INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE wishlist ADD COLUMN libraryId INTEGER NOT NULL DEFAULT 1")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_books_libraryId ON books(libraryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_wishlist_libraryId ON wishlist(libraryId)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE libraries SET name = 'Venkatesh Gowdas Library' WHERE id = 1 AND owner = 'Owner' AND name = 'Personal Library'")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, username TEXT NOT NULL, passwordHash TEXT NOT NULL, isAdmin INTEGER NOT NULL, createdAtMillis INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users(username)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN displayName TEXT")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'User'")
                database.execSQL("UPDATE users SET role = 'Administrator' WHERE isAdmin = 1")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN email TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN avatarPath TEXT")
                database.execSQL("UPDATE users SET role = 'Owner' WHERE role = 'Administrator'")
                database.execSQL("UPDATE users SET role = 'Member' WHERE role = 'User'")
                database.execSQL("CREATE TABLE IF NOT EXISTS roles (id TEXT NOT NULL, displayName TEXT NOT NULL, isSystemRole INTEGER NOT NULL, PRIMARY KEY(id))")
                database.execSQL("CREATE TABLE IF NOT EXISTS permissions (id TEXT NOT NULL, displayName TEXT NOT NULL, PRIMARY KEY(id))")
                database.execSQL("CREATE TABLE IF NOT EXISTS library_memberships (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, libraryId INTEGER NOT NULL, userId INTEGER NOT NULL, role TEXT NOT NULL, status TEXT NOT NULL, joinedAtMillis INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_library_memberships_libraryId_userId ON library_memberships(libraryId, userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_library_memberships_libraryId ON library_memberships(libraryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_library_memberships_userId ON library_memberships(userId)")
                database.execSQL("CREATE TABLE IF NOT EXISTS membership_requests (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, libraryId INTEGER NOT NULL, email TEXT NOT NULL, inviteCode TEXT NOT NULL, status TEXT NOT NULL, requestedAtMillis INTEGER NOT NULL, respondedAtMillis INTEGER)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_membership_requests_libraryId ON membership_requests(libraryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_membership_requests_email ON membership_requests(email)")
                database.execSQL("CREATE TABLE IF NOT EXISTS audit_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, libraryId INTEGER NOT NULL, actorUserId INTEGER NOT NULL, action TEXT NOT NULL, detail TEXT NOT NULL, createdAtMillis INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_libraryId ON audit_logs(libraryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_createdAtMillis ON audit_logs(createdAtMillis)")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS library_categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, libraryId INTEGER NOT NULL, name TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_library_categories_libraryId_name ON library_categories(libraryId, name)")
                database.execSQL("CREATE TABLE IF NOT EXISTS library_languages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, libraryId INTEGER NOT NULL, name TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_library_languages_libraryId_name ON library_languages(libraryId, name)")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN accountStatus TEXT NOT NULL DEFAULT 'Active'")
                database.execSQL("ALTER TABLE users ADD COLUMN forcePasswordReset INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN lastLoginAtMillis INTEGER")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE books ADD COLUMN barcode TEXT")
            }
        }
    }
}