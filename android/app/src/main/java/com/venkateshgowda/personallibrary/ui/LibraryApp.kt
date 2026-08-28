package com.venkateshgowda.personallibrary.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import com.venkateshgowda.personallibrary.data.BookDao
import com.venkateshgowda.personallibrary.data.AppSettings
import com.venkateshgowda.personallibrary.data.BookEntity
import com.venkateshgowda.personallibrary.data.CatalogDao
import com.venkateshgowda.personallibrary.data.CoverStorage
import com.venkateshgowda.personallibrary.data.EncryptedArchive
import com.venkateshgowda.personallibrary.data.CollectionEntity
import com.venkateshgowda.personallibrary.data.LegacyImportService
import com.venkateshgowda.personallibrary.data.LibraryDatabase
import com.venkateshgowda.personallibrary.data.LibraryCreator
import com.venkateshgowda.personallibrary.data.LibraryDao
import com.venkateshgowda.personallibrary.data.SearchRanking
import com.venkateshgowda.personallibrary.data.ReportExportService
import com.venkateshgowda.personallibrary.data.AdvancedReport
import com.venkateshgowda.personallibrary.data.ReportBreakdown
import com.venkateshgowda.personallibrary.data.PriceValidator
import com.venkateshgowda.personallibrary.data.PasswordHasher
import com.venkateshgowda.personallibrary.data.LoanReminderScheduler
import com.venkateshgowda.personallibrary.data.LoanDao
import com.venkateshgowda.personallibrary.data.LoanEntity
import com.venkateshgowda.personallibrary.data.WishlistDao
import com.venkateshgowda.personallibrary.data.WishlistEntity
import com.venkateshgowda.personallibrary.data.TagEntity
import com.venkateshgowda.personallibrary.data.UserDao
import com.venkateshgowda.personallibrary.data.UserEntity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream

private enum class Destination(val label: String, val phoneLabel: String = label) { Dashboard("Dashboard", "Home"), Library("Library"), Search("Search"), Loans("Loans"), More("More") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryApp(bookDao: BookDao, libraryDao: LibraryDao, loanDao: LoanDao, wishlistDao: WishlistDao, catalogDao: CatalogDao, userDao: UserDao, database: LibraryDatabase, settings: AppSettings) {
    var destination by rememberSaveable { mutableStateOf(Destination.Dashboard) }
    var editorVisible by rememberSaveable { mutableStateOf(false) }
    var editedBook by rememberSaveable { mutableStateOf<BookEntity?>(null) }
    var wishlistVisible by rememberSaveable { mutableStateOf(false) }
    var catalogVisible by rememberSaveable { mutableStateOf(false) }
    var libraryManagerVisible by rememberSaveable { mutableStateOf(false) }
    var libraryCreatorVisible by rememberSaveable { mutableStateOf(false) }
    var settingsVisible by rememberSaveable { mutableStateOf(false) }
    var exitConfirmationVisible by rememberSaveable { mutableStateOf(false) }
    var signedInUser by remember { mutableStateOf<UserEntity?>(null) }
    var switchBackUser by remember { mutableStateOf<UserEntity?>(null) }
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val onboardingComplete by settings.onboardingComplete.collectAsState(false)
    val activeLibraryId by settings.activeLibraryId.collectAsState(null)
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val activeLibrary = libraries.firstOrNull { it.id == activeLibraryId }
    LaunchedEffect(libraries, activeLibraryId) {
        if (libraries.isNotEmpty() && activeLibrary == null) settings.setActiveLibraryId(libraries.first().id)
    }
    LaunchedEffect(Unit) { if (userDao.count() == 0) userDao.insert(UserEntity(username = "admin", passwordHash = PasswordHasher.hash("admin123".toCharArray()), isAdmin = true)) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 840.dp
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(activeLibrary?.name ?: "Venkatesh Gowdas Personal Library") },
                    navigationIcon = {
                        IconButton(
                            onClick = { exitConfirmationVisible = true },
                            modifier = Modifier.padding(start = 8.dp).size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.LocalLibrary, contentDescription = "Exit app", tint = Color(0xFFFFD166))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer, titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                )
            },
            bottomBar = { if (!useNavigationRail) NavigationBar { Destination.values().forEach { item -> NavigationBarItem(destination == item, { destination = item }, { Icon(iconFor(item), item.label) }, label = { Text(item.phoneLabel) }) } } }
        ) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (useNavigationRail) NavigationRail { Destination.values().forEach { item -> NavigationRailItem(destination == item, { destination = item }, { Icon(iconFor(item), item.label) }, label = { Text(item.label) }) } }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val contentModifier = Modifier.fillMaxSize().widthIn(max = 1100.dp)
                    when (destination) {
                        Destination.Dashboard -> DashboardScreen(bookDao, loanDao, wishlistDao, activeLibraryId, contentModifier)
                        Destination.Library -> LibraryScreen(bookDao, libraryDao, wishlistDao, activeLibraryId, contentModifier, { editedBook = null; editorVisible = true }, { editedBook = it; editorVisible = true }, { wishlistVisible = true }, { catalogVisible = true }, { libraryManagerVisible = true }, { libraryCreatorVisible = true })
                        Destination.Search -> SearchScreen(bookDao, catalogDao, settings, activeLibraryId, contentModifier, { editedBook = it; editorVisible = true })
                        Destination.More -> MoreScreenImproved(database, bookDao, loanDao, wishlistDao, activeLibraryId, contentModifier, { settingsVisible = true })
                        Destination.Loans -> LoansScreenImproved(bookDao, loanDao, activeLibraryId, contentModifier)
                    }
                }
            }
        }
    }
    if (editorVisible && activeLibraryId != null) BookEditor(editedBook, bookDao, catalogDao, { editorVisible = false }) { book, tagIds, collectionIds, newImagePaths -> scope.launch { val bookId = if (book.id == 0L) bookDao.insert(book.copy(libraryId = activeLibraryId!!)) else { bookDao.update(book); book.id }; val startPosition = bookDao.imagesForBook(bookId).size; val paths = newImagePaths.distinct(); if (paths.isNotEmpty()) bookDao.insertImages(paths.mapIndexed { index, path -> com.venkateshgowda.personallibrary.data.BookImageEntity(bookId = bookId, path = path, position = startPosition + index) }); catalogDao.replaceAssignments(bookId, tagIds, collectionIds); editorVisible = false } }
    if (wishlistVisible && activeLibraryId != null) WishlistDialogImproved(wishlistDao, activeLibraryId!!, { wishlistVisible = false })
    if (catalogVisible) CatalogDialogImproved(catalogDao, { catalogVisible = false })
    if (libraryManagerVisible) LibraryManagementDialog(libraryDao, activeLibraryId, { libraryId -> scope.launch { settings.setActiveLibraryId(libraryId); libraryManagerVisible = false } }, { libraryManagerVisible = false })
    if (libraryCreatorVisible) CreateLibraryDialog(libraryDao, { libraryId -> scope.launch { settings.setActiveLibraryId(libraryId); libraryCreatorVisible = false } }, { libraryCreatorVisible = false })
    if (settingsVisible && signedInUser != null) SettingsDialogImproved(
        database,
        settings,
        userDao,
        signedInUser!!,
        { settingsVisible = false },
        { signedInUser = null; settingsVisible = false; activity?.finishAndRemoveTask() },
        {
            switchBackUser = signedInUser
            signedInUser = null
            settingsVisible = false
        }
    )
    if (exitConfirmationVisible) AlertDialog(
        onDismissRequest = { exitConfirmationVisible = false },
        title = { Text("Exit app?") },
        text = { Text("Your library data is saved on this device.") },
        confirmButton = { Button(onClick = { activity?.finishAndRemoveTask() }) { Text("Exit") } },
        dismissButton = { TextButton(onClick = { exitConfirmationVisible = false }) { Text("Cancel") } }
    )
    if (!onboardingComplete) OnboardingDialog(onNewCatalogue = { scope.launch { settings.setOnboardingComplete() } }, onImport = { scope.launch { settings.setOnboardingComplete() }; destination = Destination.More })
    if (signedInUser == null) {
        SignInDialog(
            userDao = userDao,
            onSignedIn = {
                signedInUser = it
                switchBackUser = null
            },
            onCancel = {
                val previousUser = switchBackUser
                if (previousUser != null) {
                    signedInUser = previousUser
                    switchBackUser = null
                } else {
                    activity?.finishAndRemoveTask()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignInDialog(
    userDao: UserDao,
    onSignedIn: (UserEntity) -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val users by userDao.observeAll().collectAsState(emptyList())
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var resetVisible by rememberSaveable { mutableStateOf(false) }
    var usernameMenuExpanded by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Sign in") },
        text = {
            Column {
                Text("Sign in to access your personal library.")
                ExposedDropdownMenuBox(
                    expanded = usernameMenuExpanded,
                    onExpandedChange = { usernameMenuExpanded = !usernameMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Username") },
                        placeholder = { Text("Select a user") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(usernameMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = usernameMenuExpanded,
                        onDismissRequest = { usernameMenuExpanded = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.username) },
                                onClick = {
                                    username = user.username
                                    error = null
                                    usernameMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(password, { password = it; error = null }, label = { Text("Password") }, singleLine = true, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } })
                TextButton(onClick = { resetVisible = true }) { Text("Reset password") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val user = userDao.findByUsername(username.trim())
                    if (user != null && PasswordHasher.matches(password.toCharArray(), user.passwordHash)) onSignedIn(user)
                    else error = "Incorrect username or password."
                }
            }, enabled = username.isNotBlank() && password.isNotBlank()) { Text("Sign in") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
    if (resetVisible) {
        ResetPasswordDialog(
            userDao = userDao,
            onDismiss = { resetVisible = false },
            onResetSucceeded = { resetUsername ->
                username = resetUsername
                password = ""
                error = "Password reset. Enter your new password."
                resetVisible = false
            }
        )
    }
}

@Composable
private fun ResetPasswordDialog(
    userDao: UserDao,
    onDismiss: () -> Unit,
    onResetSucceeded: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var username by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var adminPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var adminPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var succeeded by rememberSaveable { mutableStateOf(false) }
    val canReset = username.isNotBlank() && newPassword.length >= 6 && adminPassword.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the administrator password to reset an account.")
                Text("New passwords must contain at least 6 characters.", style = MaterialTheme.typography.bodySmall)
                message?.let { Text(it, color = if (succeeded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                OutlinedTextField(username, { username = it; message = null; succeeded = false }, label = { Text("Account username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(newPassword, { newPassword = it; message = null; succeeded = false }, label = { Text("New password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { PasswordVisibilityToggle(newPasswordVisible) { newPasswordVisible = !newPasswordVisible } }, singleLine = true, isError = newPassword.isNotEmpty() && newPassword.length < 6)
                OutlinedTextField(adminPassword, { adminPassword = it; message = null; succeeded = false }, label = { Text("Administrator password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { PasswordVisibilityToggle(adminPasswordVisible) { adminPasswordVisible = !adminPasswordVisible } }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    runCatching {
                        val user = userDao.findByUsername(username.trim()) ?: error("User not found.")
                        val admin = userDao.findByUsername("admin")
                        require(admin != null && admin.isAdmin && PasswordHasher.matches(adminPassword.toCharArray(), admin.passwordHash)) { "Administrator password is incorrect." }
                        userDao.update(user.copy(passwordHash = PasswordHasher.hash(newPassword.toCharArray())))
                    }.onSuccess {
                        onResetSucceeded(username.trim())
                    }.onFailure {
                        message = it.message ?: "Could not reset the password."
                        succeeded = false
                    }
                }
            }, enabled = canReset) { Text("Reset") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun PasswordVisibilityToggle(isPasswordVisible: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
        )
    }
}

@Composable
private fun OnboardingDialog(onNewCatalogue: () -> Unit, onImport: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Welcome to your personal library") },
        text = { Text("Start a new catalogue or import your existing Streamlit library archive.") },
        confirmButton = { Button(onClick = onNewCatalogue) { Text("Start new catalogue") } },
        dismissButton = { TextButton(onClick = onImport) { Text("Import existing library") } }
    )
}

@Composable
private fun DashboardScreen(bookDao: BookDao, loanDao: LoanDao, wishlistDao: WishlistDao, activeLibraryId: Long?, modifier: Modifier) {
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val totalBooks by (activeLibraryId?.let { bookDao.observeCountForLibrary(it) } ?: flowOf(0)).collectAsState(0)
    val investment by (activeLibraryId?.let { bookDao.observeInvestmentPaiseForLibrary(it) } ?: flowOf(0L)).collectAsState(0L)
    val today = LocalDate.now().toString()
    val activeLoans by (activeLibraryId?.let { loanDao.observeActiveCountForLibrary(it) } ?: flowOf(0)).collectAsState(0)
    val overdueLoans by (activeLibraryId?.let { loanDao.observeOverdueCountForLibrary(it, today) } ?: flowOf(0)).collectAsState(0)
    val dueSoonLoans by (activeLibraryId?.let { loanDao.observeDueSoonCountForLibrary(it, today, LocalDate.now().plusDays(7).toString()) } ?: flowOf(0)).collectAsState(0)
    val wishlistCost by (activeLibraryId?.let { wishlistDao.observeForLibrary(it) } ?: flowOf<List<WishlistEntity>>(emptyList())).collectAsState(emptyList<WishlistEntity>())
    val plannedWishlistPaise: Long = wishlistCost.filter { it.status == "Planned" }.map { it.expectedPricePaise }.sum()
    val recentBooks = books.sortedByDescending { it.createdAtMillis }.take(5)
    val categories = books.groupingBy { it.category ?: "Uncategorized" }.eachCount().entries.sortedByDescending { it.value }.take(5).map { ReportBreakdown(it.key, it.value.toLong()) }
    val statuses = books.groupingBy { it.readingStatus }.eachCount().entries.sortedByDescending { it.value }.map { ReportBreakdown(it.key, it.value.toLong()) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Books", totalBooks.toString(), Modifier.weight(1f)); MetricCard("Invested", formatInr(investment), Modifier.weight(1f)) }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Books lent", activeLoans.toString(), Modifier.weight(1f)); MetricCard("Overdue", overdueLoans.toString(), Modifier.weight(1f)) }
        Spacer(Modifier.height(12.dp))
        MetricCard("Due within 7 days", dueSoonLoans.toString(), Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        MetricCard("Wishlist estimated cost", formatInr(plannedWishlistPaise), Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Text("Recent books", style = MaterialTheme.typography.titleMedium)
        if (recentBooks.isEmpty()) Text("Your recently added books will appear here.") else recentBooks.forEach { book ->
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (book.coverImagePath != null) AsyncImage(model = book.coverImagePath, contentDescription = "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = Modifier.height(56.dp).weight(0.2f))
                    Column(Modifier.padding(start = if (book.coverImagePath != null) 12.dp else 0.dp).weight(1f)) {
                        Text(book.title, style = MaterialTheme.typography.titleSmall)
                        Text(book.author, style = MaterialTheme.typography.bodyMedium)
                        Text("${book.readingStatus} | ${formatInr(book.pricePaise)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (categories.isEmpty()) Text("No category summary yet.") else ReportBarChart("Categories", categories)
        if (statuses.isEmpty()) Text("No reading-status summary yet.") else ReportBarChart("Reading status", statuses)
        Spacer(Modifier.height(20.dp)); Text("Import your existing catalogue from More, or add a book from Library.")
    }
}

@Composable
private fun LoansScreenImproved(bookDao: BookDao, loanDao: LoanDao, activeLibraryId: Long?, modifier: Modifier) {
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val loans by (activeLibraryId?.let { loanDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var addVisible by rememberSaveable { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var overdueOnly by rememberSaveable { mutableStateOf(false) }
    val availableBooks = books.filter { book -> loans.none { it.bookId == book.id && it.actualReturnDate == null } }
    val overdueLoans = loans.filter { it.actualReturnDate == null && it.expectedReturnDate != null && it.expectedReturnDate < LocalDate.now().toString() }
    val displayedLoans = if (overdueOnly) overdueLoans else loans
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { addVisible = true }, enabled = availableBooks.isNotEmpty()) { Text("Record loan") }
        }
        TextButton(onClick = { overdueOnly = !overdueOnly }) { Text(if (overdueOnly) "Show all loans" else "View overdue books (${overdueLoans.size})") }
        Spacer(Modifier.height(12.dp))
        when {
            books.isEmpty() -> Text("Add a book before recording a loan.")
            availableBooks.isEmpty() -> Text("All ${books.size} books currently have active loans. Mark a book returned before recording another loan.")
        }
        if (loans.isEmpty()) Text("No loan history yet.") else if (displayedLoans.isEmpty()) Text("No overdue books. All active loans are within their return dates.") else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(displayedLoans, key = { it.id }) { loan ->
                val book = books.firstOrNull { it.id == loan.bookId }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(book?.title ?: "Deleted book", style = MaterialTheme.typography.titleMedium)
                        Text("Borrower: ${loan.borrowerName}")
                        if (!loan.borrowerContact.isNullOrBlank()) Text("Contact: ${loan.borrowerContact}")
                        Text("Borrowed: ${loan.borrowedDate}")
                        Text("Due: ${loan.expectedReturnDate ?: "Not set"}")
                        Text(if (loan.actualReturnDate == null && loan.expectedReturnDate != null && loan.expectedReturnDate < LocalDate.now().toString()) "Overdue" else if (loan.actualReturnDate == null) "Lent" else "Returned on ${loan.actualReturnDate}")
                        if (loan.actualReturnDate == null) Row { TextButton(onClick = { editingLoan = loan }) { Text("Edit") }; TextButton(onClick = { scope.launch { loanDao.markReturned(loan.id, LocalDate.now().toString()) } }) { Text("Mark returned") } }
                    }
                }
            }
        }
    }
    if (addVisible) LoanEditorImproved(books, loans, onDismiss = { addVisible = false }) { loan -> scope.launch { if (loanDao.activeCountForBook(loan.bookId) == 0) { loanDao.insert(loan); addVisible = false } } }
    editingLoan?.let { loan -> LoanEditCalendarDialog(loan, onDismiss = { editingLoan = null }) { updated -> scope.launch { loanDao.update(updated); editingLoan = null } } }
}

@Composable
private fun DateSelector(label: String, value: String, onDateSelected: (String) -> Unit, allowEmpty: Boolean = false) {
    val context = LocalContext.current
    val initialDate = runCatching { LocalDate.parse(value) }.getOrElse { LocalDate.now() }
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { DatePickerDialog(context, { _, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day).toString()) }, initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth).show() }) { Text(if (value.isBlank()) "Select date" else value) }
        if (allowEmpty && value.isNotBlank()) TextButton(onClick = { onDateSelected("") }) { Text("Clear") }
    }
}

@Composable
private fun LoanEditCalendarDialog(loan: LoanEntity, onDismiss: () -> Unit, onSave: (LoanEntity) -> Unit) {
    var borrower by rememberSaveable { mutableStateOf(loan.borrowerName) }
    var contact by rememberSaveable { mutableStateOf(loan.borrowerContact.orEmpty()) }
    var borrowedDate by rememberSaveable { mutableStateOf(loan.borrowedDate) }
    var dueDate by rememberSaveable { mutableStateOf(loan.expectedReturnDate.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(loan.notes.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit loan") }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { OutlinedTextField(borrower, { borrower = it }, label = { Text("Borrower name *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(contact, { contact = it }, label = { Text("Borrower contact") }, modifier = Modifier.fillMaxWidth()); DateSelector("Borrowed date", borrowedDate, { borrowedDate = it }); DateSelector("Expected return date", dueDate, { dueDate = it }, allowEmpty = true); OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val borrowed = runCatching { LocalDate.parse(borrowedDate) }.getOrNull(); val due = dueDate.ifBlank { null }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }; error = when { borrower.isBlank() -> "Borrower name is required."; borrowed == null -> "Select a borrowed date."; due != null && due.isBefore(borrowed) -> "Expected return cannot be before the borrowed date."; else -> null }; if (error == null) onSave(loan.copy(borrowerName = borrower.trim(), borrowerContact = contact.trim().ifBlank { null }, borrowedDate = borrowedDate, expectedReturnDate = dueDate.ifBlank { null }, notes = notes.trim().ifBlank { null })) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun LoanEditDialog(loan: LoanEntity, onDismiss: () -> Unit, onSave: (LoanEntity) -> Unit) {
    var borrower by rememberSaveable { mutableStateOf(loan.borrowerName) }
    var contact by rememberSaveable { mutableStateOf(loan.borrowerContact.orEmpty()) }
    var borrowedDate by rememberSaveable { mutableStateOf(loan.borrowedDate) }
    var dueDate by rememberSaveable { mutableStateOf(loan.expectedReturnDate.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(loan.notes.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit loan") }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { OutlinedTextField(borrower, { borrower = it }, label = { Text("Borrower name *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(contact, { contact = it }, label = { Text("Borrower contact") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(borrowedDate, { borrowedDate = it }, label = { Text("Borrowed date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Expected return date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val borrowed = runCatching { LocalDate.parse(borrowedDate) }.getOrNull(); val due = dueDate.ifBlank { null }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }; error = when { borrower.isBlank() -> "Borrower name is required."; borrowed == null -> "Enter a valid borrowed date."; dueDate.isNotBlank() && due == null -> "Enter a valid expected return date."; due != null && due.isBefore(borrowed) -> "Expected return cannot be before the borrowed date."; else -> null }; if (error == null) onSave(loan.copy(borrowerName = borrower.trim(), borrowerContact = contact.trim().ifBlank { null }, borrowedDate = borrowedDate, expectedReturnDate = dueDate.ifBlank { null }, notes = notes.trim().ifBlank { null })) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun LoansScreen(bookDao: BookDao, loanDao: LoanDao, modifier: Modifier) {
    val books by bookDao.observeAll().collectAsState(emptyList())
    val loans by loanDao.observeAll().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var editorVisible by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Loans", style = MaterialTheme.typography.headlineMedium); Button(onClick = { editorVisible = true }, enabled = books.isNotEmpty()) { Text("Record loan") } }
        Spacer(Modifier.height(12.dp))
        if (books.isEmpty()) Text("Add a book before recording a loan.") else if (loans.isEmpty()) Text("No loan history yet.") else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(loans, key = { it.id }) { loan ->
                val book = books.firstOrNull { it.id == loan.bookId }
                Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(book?.title ?: "Deleted book", style = MaterialTheme.typography.titleMedium); Text("Borrower: ${loan.borrowerName}"); Text("Due: ${loan.expectedReturnDate ?: "Not set"}"); Text(if (loan.actualReturnDate == null && loan.expectedReturnDate != null && loan.expectedReturnDate < LocalDate.now().toString()) "Overdue" else if (loan.actualReturnDate == null) "Lent" else "Returned") }; if (loan.actualReturnDate == null) Button(onClick = { scope.launch { loanDao.markReturned(loan.id, LocalDate.now().toString()) } }) { Text("Return") } } }
            }
        }
    }
    if (editorVisible) LoanEditorImproved(books, loans, onDismiss = { editorVisible = false }) { loan -> scope.launch { if (loanDao.activeCountForBook(loan.bookId) == 0) { loanDao.insert(loan); editorVisible = false } } }
}

@Composable
private fun LoanEditor(books: List<BookEntity>, onDismiss: () -> Unit, onSave: (LoanEntity) -> Unit) {
    var bookId by rememberSaveable { mutableStateOf(books.firstOrNull()?.id?.toString().orEmpty()) }
    var borrower by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var borrowedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Record loan") }, text = { Column { Text("Book IDs: ${books.joinToString { "${it.id} ${it.title}" }}", style = MaterialTheme.typography.bodySmall); OutlinedTextField(bookId, { bookId = it }, label = { Text("Book ID") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(borrower, { borrower = it }, label = { Text("Borrower name *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(contact, { contact = it }, label = { Text("Borrower contact") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(borrowedDate, { borrowedDate = it }, label = { Text("Borrowed date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Expected return date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val parsedId = bookId.toLongOrNull(); val borrowed = runCatching { LocalDate.parse(borrowedDate) }.getOrNull(); val due = dueDate.ifBlank { null }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }; error = when { parsedId == null || books.none { it.id == parsedId } -> "Select a valid book ID."; borrower.isBlank() -> "Borrower name is required."; borrowed == null -> "Enter a valid borrowed date."; dueDate.isNotBlank() && due == null -> "Enter a valid expected return date."; due != null && due.isBefore(borrowed) -> "Expected return cannot be before the borrowed date."; else -> null }; if (error == null) onSave(LoanEntity(bookId = parsedId!!, borrowerName = borrower.trim(), borrowerContact = contact.trim().ifBlank { null }, borrowedDate = borrowedDate, expectedReturnDate = dueDate.ifBlank { null }, notes = notes.trim().ifBlank { null })) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun LoanEditorImproved(books: List<BookEntity>, loans: List<LoanEntity>, onDismiss: () -> Unit, onSave: (LoanEntity) -> Unit) {
    val availableBooks = books.filter { book -> loans.none { it.bookId == book.id && it.actualReturnDate == null } }
    var selectedBookId by rememberSaveable { mutableStateOf<Long?>(availableBooks.firstOrNull()?.id) }
    var pickerVisible by rememberSaveable { mutableStateOf(false) }
    var borrower by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var borrowedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedBook = availableBooks.firstOrNull { it.id == selectedBookId }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record loan") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Book", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { pickerVisible = true }, modifier = Modifier.fillMaxWidth(), enabled = availableBooks.isNotEmpty()) {
                    Text(selectedBook?.let { "${it.title} - ${it.author}" } ?: "Select a book")
                }
                if (availableBooks.isEmpty()) Text("Every book currently has an active loan.", color = MaterialTheme.colorScheme.error)
                OutlinedTextField(borrower, { borrower = it }, label = { Text("Borrower name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(contact, { contact = it }, label = { Text("Borrower contact") }, modifier = Modifier.fillMaxWidth())
                DateSelector("Borrowed date", borrowedDate, { borrowedDate = it })
                DateSelector("Expected return date", dueDate, { dueDate = it }, allowEmpty = true)
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val borrowed = runCatching { LocalDate.parse(borrowedDate) }.getOrNull()
                val due = dueDate.ifBlank { null }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                error = when {
                    selectedBook == null -> "Select a book."
                    borrower.isBlank() -> "Borrower name is required."
                    borrowed == null -> "Enter a valid borrowed date."
                    dueDate.isNotBlank() && due == null -> "Enter a valid expected return date."
                    due != null && due.isBefore(borrowed) -> "Expected return cannot be before the borrowed date."
                    else -> null
                }
                if (error == null) onSave(LoanEntity(bookId = selectedBookId!!, borrowerName = borrower.trim(), borrowerContact = contact.trim().ifBlank { null }, borrowedDate = borrowedDate, expectedReturnDate = dueDate.ifBlank { null }, notes = notes.trim().ifBlank { null }))
            }, enabled = availableBooks.isNotEmpty()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (pickerVisible) BookPickerDialog(availableBooks, selectedBookId, onSelect = { selectedBookId = it; pickerVisible = false }, onDismiss = { pickerVisible = false })
}

@Composable
private fun BookPickerDialog(books: List<BookEntity>, selectedBookId: Long?, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select book") },
        text = {
            LazyColumn(Modifier.height(360.dp)) {
                items(books, key = { it.id }) { book ->
                    TextButton(onClick = { onSelect(book.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(if (book.id == selectedBookId) "Selected: ${book.title}" else book.title)
                            Text(book.author, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun LibraryScreen(bookDao: BookDao, libraryDao: LibraryDao, wishlistDao: WishlistDao, activeLibraryId: Long?, modifier: Modifier, onAdd: () -> Unit, onEdit: (BookEntity) -> Unit, onWishlist: () -> Unit, onCatalog: () -> Unit, onManageLibraries: () -> Unit, onCreateLibrary: () -> Unit) {
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val wishlist by (activeLibraryId?.let { wishlistDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    var viewingBooks by rememberSaveable { mutableStateOf(false) }
    var viewingWishlist by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Current library", style = MaterialTheme.typography.bodyMedium)
        Row { Button(onClick = onCreateLibrary) { Text("Create new library") }; Spacer(Modifier.widthIn(min = 8.dp)); TextButton(onClick = onManageLibraries) { Text("Manage libraries (${libraries.size})") } }
        Row { TextButton(onClick = onCatalog) { Text("Organize") }; TextButton(onClick = onWishlist) { Text("Wishlist") } }
        Row { Button(onClick = { viewingBooks = !viewingBooks; if (viewingBooks) viewingWishlist = false }) { Text(if (viewingBooks) "Hide books" else "View books (${books.size})") }; Spacer(Modifier.padding(4.dp)); Button(onClick = { viewingWishlist = !viewingWishlist; if (viewingWishlist) viewingBooks = false }) { Text(if (viewingWishlist) "Hide wishlist" else "View wishlist (${wishlist.size})") } }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAdd) { Text("Add book") }
        Spacer(Modifier.height(12.dp))
        if (viewingBooks) {
            if (books.isEmpty()) Text("No books yet. Import your web catalogue or add your first book.") else BookList(books, bookDao, onEdit, Modifier.weight(1f))
        } else if (viewingWishlist) {
            if (wishlist.isEmpty()) Text("No wishlist books yet. Add one from Wishlist.") else WishlistList(wishlist, Modifier.weight(1f), wishlistDao, bookDao)
        } else Text("Choose View books or View wishlist to browse your catalogue.")
    }
}

@Composable
private fun LibraryManagementDialog(libraryDao: LibraryDao, activeLibraryId: Long?, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    var creating by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text("Libraries", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                if (libraries.isEmpty()) Text("Create a library to record its owner and details.")
                else Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                libraries.forEach { library ->
                    TextButton(onClick = { onSelect(library.id) }, enabled = library.id != activeLibraryId, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(library.name, style = MaterialTheme.typography.titleMedium)
                            Text("Owner: ${library.owner}", style = MaterialTheme.typography.bodySmall)
                            if (!library.description.isNullOrBlank()) Text(library.description, style = MaterialTheme.typography.bodySmall)
                            Text(if (library.id == activeLibraryId) "Current library" else "Tap to open", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    TextButton(onClick = { creating = true }) { Text("Create library") }
                }
            }
        }
    }
    if (creating) CreateLibraryDialog(libraryDao, { libraryId -> onSelect(libraryId) }, { creating = false })
}

@Composable
private fun CreateLibraryDialog(libraryDao: LibraryDao, onCreated: (Long) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var owner by rememberSaveable { mutableStateOf("") }
    var imagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) runCatching { CoverStorage.copy(context, uri) }
            .onSuccess { imagePath = it; error = null }
            .onFailure { error = it.message ?: "Unable to use the selected image." }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create library") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(name, { name = it }, label = { Text("Library name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(owner, { owner = it }, label = { Text("Your name (owner) *") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text(if (imagePath == null) "Add optional image" else "Change image") }
                if (imagePath != null) AsyncImage(model = imagePath, contentDescription = "Selected library image", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(140.dp))
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                runCatching { LibraryCreator.create(name, description, imagePath, owner) }
                    .onSuccess { created -> scope.launch { runCatching { libraryDao.insert(created) }.onSuccess(onCreated).onFailure { error = "A library with this name already exists." } } }
                    .onFailure { error = it.message }
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun WishlistList(items: List<WishlistEntity>, modifier: Modifier = Modifier, wishlistDao: WishlistDao, bookDao: BookDao) {
    val scope = rememberCoroutineScope()
    var editingItem by remember { mutableStateOf<WishlistEntity?>(null) }
    var pendingDeletion by remember { mutableStateOf<WishlistEntity?>(null) }
    var purchaseReview by remember { mutableStateOf<WishlistEntity?>(null) }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    if (!item.author.isNullOrBlank()) Text(item.author)
                    Text("${formatInr(item.expectedPricePaise)} | ${item.priority} | ${item.status}", style = MaterialTheme.typography.bodyMedium)
                    if (!item.expectedPurchaseDate.isNullOrBlank()) Text("Expected: ${item.expectedPurchaseDate}", style = MaterialTheme.typography.bodySmall)
                    Row { if (item.status != "Purchased") TextButton(onClick = { purchaseReview = item }) { Text("Mark completed") }; TextButton(onClick = { editingItem = item }) { Text("Edit") }; TextButton(onClick = { pendingDeletion = item }) { Text("Delete") } }
                }
            }
        }
    }
    editingItem?.let { item -> WishlistItemEditor(item, onDismiss = { editingItem = null }) { updated -> scope.launch { wishlistDao.update(updated); editingItem = null } } }
    pendingDeletion?.let { item -> AlertDialog(onDismissRequest = { pendingDeletion = null }, title = { Text("Delete wishlist item?") }, text = { Text("${item.title} will be permanently removed.") }, confirmButton = { TextButton(onClick = { scope.launch { wishlistDao.delete(item.id); pendingDeletion = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Cancel") } }) }
    purchaseReview?.let { item -> PurchaseWishlistDialog(item, onDismiss = { purchaseReview = null }) { book -> scope.launch { val bookId = bookDao.insert(book); if (bookId > 0) { wishlistDao.update(item.copy(status = "Purchased", updatedAtMillis = System.currentTimeMillis())); purchaseReview = null } } } }
}

@Composable
private fun WishlistItemEditor(item: WishlistEntity, onDismiss: () -> Unit, onSave: (WishlistEntity) -> Unit) {
    var title by rememberSaveable { mutableStateOf(item.title) }
    var author by rememberSaveable { mutableStateOf(item.author.orEmpty()) }
    var price by rememberSaveable { mutableStateOf((item.expectedPricePaise / 100.0).toString()) }
    var category by rememberSaveable { mutableStateOf(item.category.orEmpty()) }
    var priority by rememberSaveable { mutableStateOf(item.priority) }
    var status by rememberSaveable { mutableStateOf(item.status) }
    var expectedDate by rememberSaveable { mutableStateOf(item.expectedPurchaseDate.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(item.notes.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit wishlist item") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(title, { title = it }, label = { Text("Book title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(author, { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it }, label = { Text("Expected price (INR)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(priority, { priority = it }, label = { Text("Priority: High, Medium, Low") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(status, { status = it }, label = { Text("Status: Planned, Purchased, Deferred") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(expectedDate, { expectedDate = it }, label = { Text("Expected date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = {
            val paise = PriceValidator.paise(price) ?: -1
            val validDate = expectedDate.isBlank() || runCatching { LocalDate.parse(expectedDate) }.isSuccess
            error = when { title.isBlank() -> "Book title is required."; paise < 0 -> "Expected price cannot be negative."; !validDate -> "Enter a valid expected date."; else -> null }
            if (error == null) onSave(item.copy(title = title.trim(), author = author.trim().ifBlank { null }, category = category.trim().ifBlank { null }, expectedPricePaise = paise, priority = priority.trim().ifBlank { "Medium" }, status = status.trim().ifBlank { "Planned" }, expectedPurchaseDate = expectedDate.ifBlank { null }, notes = notes.trim().ifBlank { null }, updatedAtMillis = System.currentTimeMillis()))
        }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SearchScreen(bookDao: BookDao, catalogDao: CatalogDao, settings: AppSettings, activeLibraryId: Long?, modifier: Modifier, onEdit: (BookEntity) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var readingStatus by rememberSaveable { mutableStateOf("") }
    var tag by rememberSaveable { mutableStateOf("") }
    var collection by rememberSaveable { mutableStateOf("") }
    var favouritesOnly by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf("Title") }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    val allBooks by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val threshold by settings.fuzzyThreshold.collectAsState(70)
    val tagBookIds by (if (tag.isBlank()) flowOf(emptyList()) else catalogDao.observeBookIdsForTag(tag)).collectAsState(emptyList())
    val collectionBookIds by (if (collection.isBlank()) flowOf(emptyList()) else catalogDao.observeBookIdsForCollection(collection)).collectAsState(emptyList())
    val books = SearchRanking.rank(allBooks, query, threshold).asSequence()
        .filter { category.isBlank() || it.category.orEmpty().contains(category, ignoreCase = true) }
        .filter { readingStatus.isBlank() || it.readingStatus.contains(readingStatus, ignoreCase = true) }
        .filter { tag.isBlank() || it.id in tagBookIds }
        .filter { collection.isBlank() || it.id in collectionBookIds }
        .filter { !favouritesOnly || it.favourite }
        .let { results -> when (sort) { "Author" -> results.sortedBy { it.author.lowercase() }; "Newest" -> results.sortedByDescending { it.createdAtMillis }; "Rating" -> results.sortedByDescending { it.rating ?: 0 }; "Price" -> results.sortedBy { it.pricePaise }; else -> results.sortedBy { it.title.lowercase() } } }
        .toList()
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { filtersVisible = true }) { Text("Filters") }
        }
        Text("Fuzzy match threshold: $threshold%", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(query, { query = it }, label = { Text("Title, author, ISBN, or category") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Spacer(Modifier.height(8.dp))
        if (books.isEmpty()) Text("No books match these filters.") else BookList(books, bookDao, onEdit, Modifier.weight(1f))
    }
    if (filtersVisible) AlertDialog(onDismissRequest = { filtersVisible = false }, title = { Text("Search filters") }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { OutlinedTextField(category, { category = it }, label = { Text("Filter category") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(readingStatus, { readingStatus = it }, label = { Text("Filter reading status") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(tag, { tag = it }, label = { Text("Filter tag (exact name)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(collection, { collection = it }, label = { Text("Filter collection (exact name)") }, modifier = Modifier.fillMaxWidth()); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(favouritesOnly, { favouritesOnly = it }); Text("Favourites only") }; OutlinedTextField(sort, { sort = it }, label = { Text("Sort: Title, Author, Newest, Rating, Price") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { filtersVisible = false }) { Text("Apply") } }, dismissButton = { TextButton(onClick = { category = ""; readingStatus = ""; tag = ""; collection = ""; favouritesOnly = false; sort = "Title" }) { Text("Clear") } })
}

@Composable
private fun MoreScreenImproved(database: LibraryDatabase, bookDao: BookDao, loanDao: LoanDao, wishlistDao: WishlistDao, activeLibraryId: Long?, modifier: Modifier, onSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var csvPayload by remember { mutableStateOf<ByteArray?>(null) }
    var xlsxPayload by remember { mutableStateOf<ByteArray?>(null) }
    var reportsVisible by rememberSaveable { mutableStateOf(false) }
    var aboutVisible by rememberSaveable { mutableStateOf(false) }
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val loans by loanDao.observeAll().collectAsState(emptyList())
    val wishlist by (activeLibraryId?.let { wishlistDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val report = ReportExportService.summary(books, loans, wishlist, LocalDate.now().toString())
    val advancedReport = ReportExportService.advancedSummary(books, loans, wishlist, LocalDate.now().toString())
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) scope.launch { message = try { "Imported ${LegacyImportService(context, database).importArchive(uri)} books." } catch (error: Exception) { error.message ?: "Could not import archive." } } }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if (uri != null && csvPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(csvPayload) }; message = "CSV export saved." } }
    val xlsxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri -> if (uri != null && xlsxPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(xlsxPayload) }; message = "XLSX export saved." } }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Reports", style = MaterialTheme.typography.titleMedium)
        Text("Collection, spending, loans, and wishlist summaries.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { reportsVisible = true }) { Text("View reports") }
        Spacer(Modifier.height(24.dp))
        Text("Import", style = MaterialTheme.typography.titleMedium)
        Text("Select a ZIP with root-level library.db and book_covers/.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed")) }) { Text("Choose legacy ZIP") }
        Spacer(Modifier.height(24.dp))
        Text("Export", style = MaterialTheme.typography.titleMedium)
        Row { TextButton(onClick = { csvPayload = ReportExportService.booksCsv(books); csvLauncher.launch("library-books.csv") }) { Text("Books CSV") }; TextButton(onClick = { csvPayload = ReportExportService.wishlistCsv(wishlist); csvLauncher.launch("library-wishlist.csv") }) { Text("Wishlist CSV") } }
        TextButton(onClick = { csvPayload = ReportExportService.loansCsv(books, loans, LocalDate.now().toString()); csvLauncher.launch("library-loans.csv") }) { Text("Loans CSV") }
        TextButton(onClick = { xlsxPayload = ReportExportService.workbook(books, wishlist, loans, LocalDate.now().toString()); xlsxLauncher.launch("personal-library.xlsx") }) { Text("Export XLSX") }
        Spacer(Modifier.height(24.dp))
        Text("Preferences", style = MaterialTheme.typography.titleMedium)
        Button(onClick = onSettings) { Text("Settings") }
        Spacer(Modifier.height(16.dp))
        Text("About", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { aboutVisible = true }) { Text("App information") }
        if (message != null) Text(message!!, modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.error)
    }
    if (reportsVisible) ReportsDialog(report, advancedReport, onDismiss = { reportsVisible = false })
    if (aboutVisible) AboutAppDialog(onDismiss = { aboutVisible = false })
}

@Composable
private fun AboutAppDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Venkatesh Gowdas Personal Library", style = MaterialTheme.typography.titleMedium)
                Text("Version 1.0.0")
                Text("A private, offline-first library manager for books, wishlist items, and loans.")
                Text("Your catalogue is stored locally on this device. The app does not require an internet connection.")
                Text("Multi-library support keeps books, wishlist items, loans, reports, and exports separate for each library.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun ReportsDialog(report: com.venkateshgowda.personallibrary.data.ReportSummary, advancedReport: AdvancedReport, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Library reports") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Books", report.books.toString(), Modifier.weight(1f)); MetricCard("Invested", formatInr(report.investmentPaise), Modifier.weight(1f)) }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Active loans", report.activeLoans.toString(), Modifier.weight(1f)); MetricCard("Overdue", report.overdueLoans.toString(), Modifier.weight(1f)) }
                Spacer(Modifier.height(12.dp))
                MetricCard("Wishlist planned", formatInr(report.plannedWishlistPaise), Modifier.fillMaxWidth())
                ReportBarChart("Investment by category", advancedReport.investmentByCategory, true)
                ReportBarChart("Investment by author", advancedReport.investmentByAuthor, true)
                ReportBarChart("Spending by publisher", advancedReport.investmentByPublisher, true)
                ReportBarChart("Spending by purchase month", advancedReport.investmentByPurchaseMonth, true)
                ReportBarChart("Spending by purchase year", advancedReport.investmentByPurchaseYear, true)
                ReportBarChart("Books by author", advancedReport.booksByAuthor)
                ReportBarChart("Ratings", advancedReport.ratings)
                ReportBarChart("Loans", advancedReport.loansByStatus)
                ReportBarChart("Wishlist priority", advancedReport.wishlistByPriority)
                ReportBarChart("Wishlist status", advancedReport.wishlistByStatus)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun MoreScreen(database: LibraryDatabase, bookDao: BookDao, loanDao: LoanDao, wishlistDao: WishlistDao, modifier: Modifier, onSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var csvPayload by remember { mutableStateOf<ByteArray?>(null) }
    var xlsxPayload by remember { mutableStateOf<ByteArray?>(null) }
    val books by bookDao.observeAll().collectAsState(emptyList())
    val loans by loanDao.observeAll().collectAsState(emptyList())
    val wishlist by wishlistDao.observeAll().collectAsState(emptyList())
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) scope.launch { message = try { "Imported ${LegacyImportService(context, database).importArchive(uri)} books." } catch (error: Exception) { error.message ?: "Could not import archive." } } }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if (uri != null && csvPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(csvPayload) }; message = "CSV export saved." } }
    val xlsxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri -> if (uri != null && xlsxPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(xlsxPayload) }; message = "XLSX export saved." } }
    val report = ReportExportService.summary(books, loans, wishlist, LocalDate.now().toString())
    val advancedReport = ReportExportService.advancedSummary(books, loans, wishlist, LocalDate.now().toString())
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        AdvancedReportCharts(advancedReport)
        Spacer(Modifier.height(24.dp))
        Text("More", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(16.dp)); Text("Import existing web library", style = MaterialTheme.typography.titleMedium); Text("Select a ZIP with root-level library.db and book_covers/."); Spacer(Modifier.height(8.dp)); Button(onClick = { importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed")) }) { Text("Choose legacy ZIP") }
        Spacer(Modifier.height(24.dp)); Text("Reports", style = MaterialTheme.typography.titleMedium); Text("${report.books} books, ${formatInr(report.investmentPaise)} invested, ${report.favourites} favourites, ${report.ratedBooks} rated"); Text("${report.activeLoans} active loans, ${report.overdueLoans} overdue, ${formatInr(report.plannedWishlistPaise)} planned wishlist"); Text("Categories: ${report.byCategory.joinToString { "${it.first} (${it.second})" }.ifBlank { "None" }}"); Text("Reading status: ${report.byStatus.joinToString { "${it.first} (${it.second})" }.ifBlank { "None" }}"); Spacer(Modifier.height(8.dp)); Row { TextButton(onClick = { csvPayload = ReportExportService.booksCsv(books); csvLauncher.launch("library-books.csv") }) { Text("Export books CSV") }; TextButton(onClick = { csvPayload = ReportExportService.wishlistCsv(wishlist); csvLauncher.launch("library-wishlist.csv") }) { Text("Export wishlist CSV") } }; TextButton(onClick = { xlsxPayload = ReportExportService.workbook(books, wishlist); xlsxLauncher.launch("personal-library.xlsx") }) { Text("Export XLSX") }; if (message != null) Text(message!!, modifier = Modifier.padding(top = 16.dp)); Spacer(Modifier.height(24.dp)); Button(onClick = onSettings) { Text("Settings") }
    }
}

@Composable
private fun AdvancedReportCharts(report: AdvancedReport) {
    Text("Analytics", style = MaterialTheme.typography.headlineMedium)
    ReportBarChart("Investment by category", report.investmentByCategory, true)
    ReportBarChart("Investment by author", report.investmentByAuthor, true)
    ReportBarChart("Investment by publisher", report.investmentByPublisher, true)
    ReportBarChart("Spending by purchase month", report.investmentByPurchaseMonth, true)
    ReportBarChart("Spending by purchase year", report.investmentByPurchaseYear, true)
    ReportBarChart("Books by author", report.booksByAuthor)
    ReportBarChart("Ratings", report.ratings)
    ReportBarChart("Loans", report.loansByStatus)
    ReportBarChart("Wishlist priority", report.wishlistByPriority)
    ReportBarChart("Wishlist status", report.wishlistByStatus)
}

@Composable
private fun ReportBarChart(title: String, entries: List<ReportBreakdown>, showCurrency: Boolean = false) {
    if (entries.isEmpty()) return
    val maximum = entries.maxOf { it.value }.coerceAtLeast(1L)
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    entries.take(5).forEach { entry ->
        val valueText = if (showCurrency) formatInr(entry.value) else entry.value.toString()
        Text("${entry.label}: $valueText", style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(
            progress = (entry.value.toFloat() / maximum).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsDialogImproved(database: LibraryDatabase, settings: AppSettings, userDao: UserDao, signedInUser: UserEntity, onDismiss: () -> Unit, onSignOut: () -> Unit, onSwitchUser: () -> Unit) {
    val context = LocalContext.current
    val theme by settings.theme.collectAsState("System")
    val lockTimeout by settings.lockTimeout.collectAsState("5 minutes")
    val appLockEnabled by settings.appLockEnabled.collectAsState(false)
    val remindersEnabled by settings.remindersEnabled.collectAsState(false)
    val detailedReminders by settings.detailedReminders.collectAsState(false)
    val fuzzyThreshold by settings.fuzzyThreshold.collectAsState(70)
    val scope = rememberCoroutineScope()
    var backupVisible by rememberSaveable { mutableStateOf(false) }
    var restoreVisible by rememberSaveable { mutableStateOf(false) }
    var accountsVisible by rememberSaveable { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> scope.launch { settings.setRemindersEnabled(granted); if (granted) LoanReminderScheduler.schedule(context) } }
    fun setReminders(enabled: Boolean) {
        if (!enabled) scope.launch { settings.setRemindersEnabled(false); LoanReminderScheduler.cancel(context) }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) scope.launch { settings.setRemindersEnabled(true); LoanReminderScheduler.schedule(context) }
        else notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Row { listOf("Light", "Dark", "System").forEach { option -> TextButton(onClick = { scope.launch { settings.setTheme(option) } }) { Text(if (theme == option) "$option selected" else option) } } }
                Text("Fuzzy search threshold: $fuzzyThreshold%", style = MaterialTheme.typography.titleMedium)
                Slider(value = fuzzyThreshold.toFloat(), onValueChange = { value -> scope.launch { settings.setFuzzyThreshold(value.toInt()) } }, valueRange = 60f..90f, steps = 29)
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(appLockEnabled, { enabled -> scope.launch { settings.setAppLockEnabled(enabled) } }); Text("Require biometric or device credential") }
                OutlinedTextField(lockTimeout, { value -> scope.launch { settings.setLockTimeout(value) } }, label = { Text("Lock timeout: Immediate, 5 minutes, 15 minutes, Never") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(remindersEnabled, ::setReminders); Text("Enable loan reminders") }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(detailedReminders, { detailed -> scope.launch { settings.setDetailedReminders(detailed) } }, enabled = remindersEnabled); Text("Show reminder count", color = if (remindersEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
                Text("Backup", style = MaterialTheme.typography.titleMedium)
                Text("Save an encrypted copy of your library and cover images.")
                TextButton(onClick = { backupVisible = true }) { Text("Create encrypted backup") }
                TextButton(onClick = { restoreVisible = true }) { Text("Restore encrypted backup") }
                if (signedInUser.isAdmin) {
                    TextButton(onClick = { accountsVisible = true }) { Text("Manage users") }
                }
                Text("Account", style = MaterialTheme.typography.titleMedium)
                Text("Signed in as ${signedInUser.username}")
                TextButton(onClick = onSwitchUser) { Text("Switch user") }
                TextButton(onClick = onSignOut) { Text("Sign out") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
    if (backupVisible) BackupDialog(database, onDismiss = { backupVisible = false })
    if (restoreVisible) RestoreBackupDialog(database, onDismiss = { restoreVisible = false })
    if (accountsVisible) UserAccountsDialog(userDao) { accountsVisible = false }
}

@Composable
private fun UserAccountsDialog(userDao: UserDao, onDismiss: () -> Unit) {
    val users by userDao.observeAll().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage users") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Accounts", style = MaterialTheme.typography.titleMedium)
                if (users.isEmpty()) Text("No user accounts found.") else {
                    Column(Modifier.fillMaxWidth().heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                        users.forEach { user ->
                            TextButton(onClick = { username = user.username; message = "Set a new password for ${user.username}." }, modifier = Modifier.fillMaxWidth()) {
                                Text("${user.username}${if (user.isAdmin) " (admin)" else ""}", modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
                Text("Create a user or select an account above to reset its password.")
                OutlinedTextField(username, { username = it; message = null }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(password, { password = it; message = null }, label = { Text("New password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } })
                message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val existing = userDao.findByUsername(username.trim())
                    if (username.isBlank() || password.length < 6) message = "Use a username and 6-character password."
                    else {
                        if (existing == null) userDao.insert(UserEntity(username = username.trim(), passwordHash = PasswordHasher.hash(password.toCharArray())))
                        else userDao.update(existing.copy(passwordHash = PasswordHasher.hash(password.toCharArray())))
                        message = if (existing == null) "User created." else "Password reset successfully."
                        username = ""
                        password = ""
                    }
                }
            }, enabled = username.isNotBlank() && password.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun BackupDialog(database: LibraryDatabase, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var passphrase by rememberSaveable { mutableStateOf("") }
    var pendingArchive by remember { mutableStateOf<ByteArray?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val archive = pendingArchive
        if (uri != null && archive != null) {
            scope.launch {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(archive) } ?: error("Could not open the selected backup location.") }
                    .onSuccess { onDismiss() }
                    .onFailure { error = it.message ?: "Could not save the backup." }
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create encrypted backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose a passphrase you will remember. It is required to restore this backup.")
                OutlinedTextField(passphrase, { passphrase = it; error = null }, label = { Text("Backup passphrase") }, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
                                val payload = ByteArrayOutputStream().use { output ->
                                    ZipOutputStream(output).use { archive ->
                                        val databaseFile = context.getDatabasePath("library.db")
                                        archive.putNextEntry(ZipEntry("library.db"))
                                        databaseFile.inputStream().use { it.copyTo(archive) }
                                        archive.closeEntry()
                                        File(context.filesDir, "book_covers").listFiles()?.filter { it.isFile }?.forEach { cover ->
                                            archive.putNextEntry(ZipEntry("book_covers/${cover.name}"))
                                            cover.inputStream().use { it.copyTo(archive) }
                                            archive.closeEntry()
                                        }
                                    }
                                    output.toByteArray()
                                }
                                EncryptedArchive.encrypt(payload, passphrase.toCharArray())
                            }
                        }.onSuccess { archive ->
                            pendingArchive = archive
                            saveLauncher.launch("personal-library-backup.plb")
                        }.onFailure { failure -> error = failure.message ?: "Could not create the backup." }
                    }
                },
                enabled = passphrase.isNotBlank()
            ) { Text("Choose save location") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RestoreBackupDialog(database: LibraryDatabase, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var selectedBackup by rememberSaveable { mutableStateOf<Uri?>(null) }
    var passphrase by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var restoring by rememberSaveable { mutableStateOf(false) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedBackup = uri
        error = if (uri == null) "No backup file was selected." else null
    }
    AlertDialog(
        onDismissRequest = { if (!restoring) onDismiss() },
        title = { Text("Restore encrypted backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Restoring replaces this device's current catalogue and cover images.")
                TextButton(onClick = { filePicker.launch(arrayOf("application/octet-stream", "application/zip", "*/*")) }, enabled = !restoring) {
                    Text(if (selectedBackup == null) "Choose .plb backup file" else "Backup file selected")
                }
                OutlinedTextField(passphrase, { passphrase = it; error = null }, label = { Text("Backup passphrase") }, modifier = Modifier.fillMaxWidth(), enabled = !restoring)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val backup = selectedBackup ?: return@Button
                    restoring = true
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                restoreEncryptedBackup(context, database, backup, passphrase)
                            }
                        }.onSuccess {
                            context.getSharedPreferences("restore_status", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean("show_success", true)
                                .apply()
                            onDismiss()
                            activity?.recreate()
                        }.onFailure { failure ->
                            restoring = false
                            error = failure.message ?: "Could not restore the backup."
                        }
                    }
                },
                enabled = selectedBackup != null && passphrase.isNotBlank() && !restoring
            ) { Text(if (restoring) "Restoring" else "Restore") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !restoring) { Text("Cancel") } }
    )
}

private fun restoreEncryptedBackup(context: android.content.Context, database: LibraryDatabase, uri: Uri, passphrase: String) {
    val encrypted = context.contentResolver.openInputStream(uri)?.use { input ->
        ByteArrayOutputStream().use { output ->
            input.copyTo(output)
            output.toByteArray()
        }
    } ?: error("Could not read the selected backup.")
    val staging = File(context.cacheDir, "restore-${System.nanoTime()}")
    try {
        val archive = EncryptedArchive.decrypt(encrypted, passphrase.toCharArray())
        var extractedBytes = 0L
        ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name.replace('\\', '/')
                require(!entry.isDirectory && (name == "library.db" || name.startsWith("book_covers/")) && !name.contains("../")) { "Backup contains an unsupported file." }
                val target = File(staging, name)
                target.parentFile?.mkdirs()
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = zip.read(buffer)
                        if (read < 0) break
                        extractedBytes += read
                        require(extractedBytes <= 1024L * 1024 * 1024) { "Backup expands beyond 1 GB." }
                        output.write(buffer, 0, read)
                    }
                }
                zip.closeEntry()
            }
        }
        val stagedDatabase = File(staging, "library.db")
        require(stagedDatabase.isFile) { "Backup does not contain a library database." }
        SQLiteDatabase.openDatabase(stagedDatabase.path, null, SQLiteDatabase.OPEN_READONLY).use { restored ->
            require(restored.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }) { "Backup database failed its integrity check." }
        }
        database.close()
        val databaseFile = context.getDatabasePath("library.db")
        File("${databaseFile.path}-wal").delete()
        File("${databaseFile.path}-shm").delete()
        stagedDatabase.copyTo(databaseFile, overwrite = true)
        val coversDirectory = File(context.filesDir, "book_covers")
        coversDirectory.deleteRecursively()
        val stagedCovers = File(staging, "book_covers")
        if (stagedCovers.isDirectory) stagedCovers.copyRecursively(coversDirectory, overwrite = true)
    } finally {
        staging.deleteRecursively()
    }
}

@Composable
private fun SettingsDialog(settings: AppSettings, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val theme by settings.theme.collectAsState("System")
    val lockTimeout by settings.lockTimeout.collectAsState("5 minutes")
    val appLockEnabled by settings.appLockEnabled.collectAsState(false)
    val remindersEnabled by settings.remindersEnabled.collectAsState(false)
    val detailedReminders by settings.detailedReminders.collectAsState(false)
    val fuzzyThreshold by settings.fuzzyThreshold.collectAsState(70)
    val scope = rememberCoroutineScope()
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> scope.launch { settings.setRemindersEnabled(granted); if (granted) LoanReminderScheduler.schedule(context) } }
    fun setReminders(enabled: Boolean) {
        if (!enabled) { scope.launch { settings.setRemindersEnabled(false); LoanReminderScheduler.cancel(context) } }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { scope.launch { settings.setRemindersEnabled(true); LoanReminderScheduler.schedule(context) } }
        else notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Settings") }, text = { Column { Text("Theme", style = MaterialTheme.typography.titleMedium); Row { listOf("Light", "Dark", "System").forEach { option -> TextButton(onClick = { scope.launch { settings.setTheme(option) } }) { Text(if (theme == option) "$option selected" else option) } } }; Text("Fuzzy search threshold: $fuzzyThreshold%", style = MaterialTheme.typography.titleMedium); Slider(value = fuzzyThreshold.toFloat(), onValueChange = { value -> scope.launch { settings.setFuzzyThreshold(value.toInt()) } }, valueRange = 60f..90f, steps = 29); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(appLockEnabled, { enabled -> scope.launch { settings.setAppLockEnabled(enabled) } }); Text("Require biometric or device credential") }; OutlinedTextField(lockTimeout, { value -> scope.launch { settings.setLockTimeout(value) } }, label = { Text("Lock timeout: Immediate, 5 minutes, 15 minutes, Never") }, modifier = Modifier.fillMaxWidth()); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(remindersEnabled, ::setReminders); Text("Enable loan reminders") }; if (remindersEnabled) Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(detailedReminders, { detailed -> scope.launch { settings.setDetailedReminders(detailed) } }); Text("Show reminder count") } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
private fun BookList(books: List<BookEntity>, bookDao: BookDao, onEdit: (BookEntity) -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var bookPendingDeletion by remember { mutableStateOf<BookEntity?>(null) }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) { items(books, key = { it.id }) { book -> Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { if (book.coverImagePath != null) AsyncImage(model = book.coverImagePath, contentDescription = "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = Modifier.padding(end = 12.dp).height(72.dp).weight(0.22f)); Column(Modifier.weight(1f)) { Text(book.title, style = MaterialTheme.typography.titleMedium); Text(book.author); Text(formatInr(book.pricePaise), style = MaterialTheme.typography.labelMedium) }; IconButton(onClick = { onEdit(book) }) { Icon(Icons.Outlined.Edit, "Edit ${book.title}") }; IconButton(onClick = { bookPendingDeletion = book }) { Icon(Icons.Outlined.Delete, "Delete ${book.title}") } } } } }
    bookPendingDeletion?.let { book ->
        AlertDialog(
            onDismissRequest = { bookPendingDeletion = null },
            title = { Text("Delete book?") },
            text = { Text("${book.title} and its loan history will be permanently deleted.") },
            confirmButton = { TextButton(onClick = { scope.launch { bookDao.delete(book.id); CoverStorage.delete(book.coverImagePath); bookPendingDeletion = null } }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { bookPendingDeletion = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BookEditorScrollable(book: BookEntity?, bookDao: BookDao, catalogDao: CatalogDao, onDismiss: () -> Unit, onSave: (BookEntity, List<Long>, List<Long>) -> Unit) {
    var title by rememberSaveable { mutableStateOf(book?.title.orEmpty()) }
    var author by rememberSaveable { mutableStateOf(book?.author.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(((book?.pricePaise ?: 0) / 100.0).toString()) }
    var category by rememberSaveable { mutableStateOf(book?.category.orEmpty()) }
    var isbn by rememberSaveable { mutableStateOf(book?.isbn.orEmpty()) }
    var publisher by rememberSaveable { mutableStateOf(book?.publisher.orEmpty()) }
    var purchaseDate by rememberSaveable { mutableStateOf(book?.purchaseDate.orEmpty()) }
    var language by rememberSaveable { mutableStateOf(book?.language.orEmpty()) }
    var rating by rememberSaveable { mutableStateOf(book?.rating?.toString().orEmpty()) }
    var readingStatus by rememberSaveable { mutableStateOf(book?.readingStatus ?: "Unread") }
    var personalReview by rememberSaveable { mutableStateOf(book?.personalReview.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(book?.notes.orEmpty()) }
    var favourite by rememberSaveable { mutableStateOf(book?.favourite ?: false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val tags by catalogDao.observeTags().collectAsState(emptyList())
    val collections by catalogDao.observeCollections().collectAsState(emptyList())
    val assignedTags by (book?.let { catalogDao.observeTagsForBook(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val assignedCollections by (book?.let { catalogDao.observeCollectionsForBook(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    var selectedTagIds by remember(book?.id) { mutableStateOf(emptySet<Long>()) }
    var selectedCollectionIds by remember(book?.id) { mutableStateOf(emptySet<Long>()) }
    LaunchedEffect(assignedTags, assignedCollections) { selectedTagIds = assignedTags.map { it.id }.toSet(); selectedCollectionIds = assignedCollections.map { it.id }.toSet() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (book == null) "Add book" else "Edit book") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(title, { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(author, { author = it }, label = { Text("Author *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it }, label = { Text("Price (INR) *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(isbn, { isbn = it }, label = { Text("ISBN") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(publisher, { publisher = it }, label = { Text("Publisher") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(purchaseDate, { purchaseDate = it }, label = { Text("Purchase date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(language, { language = it }, label = { Text("Language") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(rating, { rating = it }, label = { Text("Rating (1-5)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(readingStatus, { readingStatus = it }, label = { Text("Reading status") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(favourite, { favourite = it }); Text("Favourite") }
                OutlinedTextField(personalReview, { personalReview = it }, label = { Text("Personal review") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (tags.isNotEmpty()) { Text("Tags", style = MaterialTheme.typography.titleSmall); tags.forEach { tag -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(tag.id in selectedTagIds, { checked -> selectedTagIds = if (checked) selectedTagIds + tag.id else selectedTagIds - tag.id }); Text(tag.name) } } }
                if (collections.isNotEmpty()) { Text("Collections", style = MaterialTheme.typography.titleSmall); collections.forEach { collection -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(collection.id in selectedCollectionIds, { checked -> selectedCollectionIds = if (checked) selectedCollectionIds + collection.id else selectedCollectionIds - collection.id }); Text(collection.name) } } }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = {
            val paise = PriceValidator.paise(price) ?: -1
            val parsedRating = rating.toIntOrNull()
            val normalizedIsbn = isbn.replace("-", "").replace(" ", "").uppercase().ifBlank { null }
            val validDate = purchaseDate.isBlank() || runCatching { LocalDate.parse(purchaseDate) }.isSuccess
            error = when { title.isBlank() -> "Title is required."; author.isBlank() -> "Author is required."; paise < 0 -> "Enter a non-negative price using digits and up to two decimal places."; !validDate -> "Enter a valid purchase date."; parsedRating != null && parsedRating !in 1..5 -> "Rating must be from 1 to 5."; else -> null }
            if (error == null) scope.launch {
                if (normalizedIsbn != null && bookDao.findOtherBookWithIsbn(normalizedIsbn, book?.id ?: 0L) != null) error = "This ISBN already belongs to another book."
                else onSave((book ?: BookEntity(title = "", author = "")).copy(title = title.trim(), author = author.trim(), pricePaise = paise, category = category.trim().ifBlank { null }, isbn = normalizedIsbn, publisher = publisher.trim().ifBlank { null }, purchaseDate = purchaseDate.ifBlank { null }, language = language.trim().ifBlank { null }, rating = parsedRating, readingStatus = readingStatus.trim().ifBlank { "Unread" }, favourite = favourite, personalReview = personalReview.trim().ifBlank { null }, notes = notes.trim().ifBlank { null }, updatedAtMillis = System.currentTimeMillis()), selectedTagIds.toList(), selectedCollectionIds.toList())
            }
        }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BookEditor(book: BookEntity?, bookDao: BookDao, catalogDao: CatalogDao, onDismiss: () -> Unit, onSave: (BookEntity, List<Long>, List<Long>, List<String>) -> Unit) {
    var title by rememberSaveable { mutableStateOf(book?.title.orEmpty()) }; var author by rememberSaveable { mutableStateOf(book?.author.orEmpty()) }; var price by rememberSaveable { mutableStateOf(((book?.pricePaise ?: 0) / 100.0).toString()) }; var category by rememberSaveable { mutableStateOf(book?.category.orEmpty()) }; var isbn by rememberSaveable { mutableStateOf(book?.isbn.orEmpty()) }
    var lastValidPrice by rememberSaveable { mutableStateOf(price) }
    var publisher by rememberSaveable { mutableStateOf(book?.publisher.orEmpty()) }; var purchaseDate by rememberSaveable { mutableStateOf(book?.purchaseDate.orEmpty()) }; var language by rememberSaveable { mutableStateOf(book?.language.orEmpty()) }; var rating by rememberSaveable { mutableStateOf(book?.rating?.toString().orEmpty()) }; var readingStatus by rememberSaveable { mutableStateOf(book?.readingStatus ?: "Unread") }; var personalReview by rememberSaveable { mutableStateOf(book?.personalReview.orEmpty()) }; var notes by rememberSaveable { mutableStateOf(book?.notes.orEmpty()) }; var favourite by rememberSaveable { mutableStateOf(book?.favourite ?: false) }
    var coverPath by rememberSaveable { mutableStateOf(book?.coverImagePath) }
    var temporaryCapturePath by rememberSaveable { mutableStateOf<String?>(null) }
    var coverError by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(price) {
        if (price.isEmpty() || price.matches(Regex("^(0|[1-9][0-9]*)\\.$"))) return@LaunchedEffect
        if (PriceValidator.paise(price) == null) {
            coverError = "Enter a non-negative price using digits and up to two decimal places."
            price = lastValidPrice
        } else {
            val priceChanged = price != lastValidPrice
            lastValidPrice = price
            if (priceChanged && coverError == "Enter a non-negative price using digits and up to two decimal places.") {
            coverError = null
            }
        }
    }
    var newImagePaths by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var existingImagePendingDeletion by remember { mutableStateOf<com.venkateshgowda.personallibrary.data.BookImageEntity?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tags by catalogDao.observeTags().collectAsState(emptyList())
    val collections by catalogDao.observeCollections().collectAsState(emptyList())
    val assignedTags by (book?.let { catalogDao.observeTagsForBook(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val assignedCollections by (book?.let { catalogDao.observeCollectionsForBook(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val existingImages by (book?.let { bookDao.observeImagesForBook(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val catalogueBooks by bookDao.observeAll().collectAsState(emptyList())
    val similarBook = if (isbn.isBlank() && title.isNotBlank() && author.isNotBlank()) catalogueBooks.firstOrNull { candidate ->
        candidate.id != (book?.id ?: 0L) && normalizeForDuplicate(candidate.title) == normalizeForDuplicate(title) && normalizeForDuplicate(candidate.author) == normalizeForDuplicate(author)
    } else null
    var selectedTagIds by remember(book?.id) { mutableStateOf(emptySet<Long>()) }
    var selectedCollectionIds by remember(book?.id) { mutableStateOf(emptySet<Long>()) }
    LaunchedEffect(assignedTags, assignedCollections) {
        selectedTagIds = assignedTags.map { it.id }.toSet()
        selectedCollectionIds = assignedCollections.map { it.id }.toSet()
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val capturePath = temporaryCapturePath
        temporaryCapturePath = null
        if (captured && capturePath != null) {
            runCatching { CoverStorage.processCaptured(context, capturePath) }
                .onSuccess { coverPath = it }
                .onFailure { coverError = it.message ?: "Could not save the captured image." }
        } else if (capturePath != null) {
            CoverStorage.delete(capturePath)
        }
    }
    val requestCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            coverError = "Camera permission is required to take a cover photo."
        } else {
            val captureFile = File(context.filesDir, "book_covers/${UUID.randomUUID()}.jpg").apply { parentFile?.mkdirs() }
            temporaryCapturePath = captureFile.absolutePath
            camera.launch(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", captureFile))
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) runCatching { CoverStorage.copy(context, uri) }
            .onSuccess { coverPath = it }
            .onFailure { coverError = it.message ?: "Could not save the selected image." }
    }
    val additionalImagesPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        val newImagesAlreadySelected = newImagePaths.size + if (book == null && coverPath != null) 1 else 0
        val capacity = (5 - newImagesAlreadySelected).coerceAtLeast(0)
        uris.take(capacity).forEach { uri -> runCatching { CoverStorage.copy(context, uri) }.onSuccess { newImagePaths = newImagePaths + it }.onFailure { coverError = it.message ?: "Could not save a selected image." } }
        if (uris.size > capacity) coverError = "A book can have up to five newly added images."
    }
    fun startCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val captureFile = File(context.filesDir, "book_covers/${UUID.randomUUID()}.jpg").apply { parentFile?.mkdirs() }
            temporaryCapturePath = captureFile.absolutePath
            camera.launch(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", captureFile))
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (book == null) "Add book" else "Edit book") }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { OutlinedTextField(title, { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(author, { author = it }, label = { Text("Author *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(price, { price = it }, label = { Text("Price (INR) *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(isbn, { isbn = it }, label = { Text("ISBN") }, modifier = Modifier.fillMaxWidth()); if (similarBook != null) Text("Similar title and author already exist. Add an ISBN to distinguish editions, or save to keep this separate record.", color = MaterialTheme.colorScheme.error); OutlinedTextField(publisher, { publisher = it }, label = { Text("Publisher") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(purchaseDate, { purchaseDate = it }, label = { Text("Purchase date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(language, { language = it }, label = { Text("Language") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(rating, { rating = it }, label = { Text("Rating (1-5)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(readingStatus, { readingStatus = it }, label = { Text("Reading status") }, modifier = Modifier.fillMaxWidth()); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = favourite, onCheckedChange = { favourite = it }); Text("Favourite") }; OutlinedTextField(personalReview, { personalReview = it }, label = { Text("Personal review") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()); if (tags.isNotEmpty()) { Text("Tags", style = MaterialTheme.typography.titleSmall); tags.forEach { tag -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = tag.id in selectedTagIds, onCheckedChange = { checked -> selectedTagIds = if (checked) selectedTagIds + tag.id else selectedTagIds - tag.id }); Text(tag.name) } } }; if (collections.isNotEmpty()) { Text("Collections", style = MaterialTheme.typography.titleSmall); collections.forEach { collection -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = collection.id in selectedCollectionIds, onCheckedChange = { checked -> selectedCollectionIds = if (checked) selectedCollectionIds + collection.id else selectedCollectionIds - collection.id }); Text(collection.name) } } }; Text("Book images", style = MaterialTheme.typography.titleSmall); existingImages.forEachIndexed { index, image -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { AsyncImage(model = image.path, contentDescription = "Book image ${index + 1}", contentScale = ContentScale.Crop, modifier = Modifier.height(72.dp).weight(1f)); TextButton(onClick = { if (book != null) scope.launch { bookDao.moveImage(book, image.id, -1) } }, enabled = index > 0) { Text("Up") }; TextButton(onClick = { if (book != null) scope.launch { bookDao.moveImage(book, image.id, 1) } }, enabled = index < existingImages.lastIndex) { Text("Down") }; TextButton(onClick = { existingImagePendingDeletion = image }) { Text("Delete") } } }; if (coverPath != null) { AsyncImage(model = coverPath, contentDescription = "Selected cover", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp)); TextButton(onClick = { coverPath = null }) { Text("Remove cover") } }; Text("Images: ${existingImages.size + newImagePaths.size} / 5 for new additions"); Row { TextButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Choose cover") }; TextButton(onClick = ::startCamera) { Text("Take photo") }; TextButton(onClick = { additionalImagesPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Add images") } }; if (coverError != null) Text(coverError!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val paise = PriceValidator.paise(price); val parsedRating = rating.toIntOrNull(); val normalizedIsbn = isbn.replace("-", "").replace(" ", "").uppercase().ifBlank { null }; val validDate = purchaseDate.isBlank() || runCatching { LocalDate.parse(purchaseDate) }.isSuccess; coverError = when { title.isBlank() -> "Title is required."; author.isBlank() -> "Author is required."; paise == null -> "Enter a non-negative price using digits and up to two decimal places."; !validDate -> "Enter a valid purchase date."; parsedRating != null && parsedRating !in 1..5 -> "Rating must be from 1 to 5."; else -> null }; if (coverError == null) scope.launch { if (normalizedIsbn != null && bookDao.findOtherBookWithIsbn(normalizedIsbn, book?.id ?: 0L) != null) coverError = "This ISBN already belongs to another book." else { val imagesToSave = newImagePaths + if (book == null) listOfNotNull(coverPath) else emptyList(); onSave((book ?: BookEntity(title = "", author = "")).copy(title = title.trim(), author = author.trim(), pricePaise = paise!!, category = category.trim().ifBlank { null }, isbn = normalizedIsbn, coverImagePath = coverPath, publisher = publisher.trim().ifBlank { null }, purchaseDate = purchaseDate.ifBlank { null }, language = language.trim().ifBlank { null }, rating = parsedRating, readingStatus = readingStatus.trim().ifBlank { "Unread" }, favourite = favourite, personalReview = personalReview.trim().ifBlank { null }, notes = notes.trim().ifBlank { null }, updatedAtMillis = System.currentTimeMillis()), selectedTagIds.toList(), selectedCollectionIds.toList(), imagesToSave) } } }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    existingImagePendingDeletion?.let { image -> AlertDialog(onDismissRequest = { existingImagePendingDeletion = null }, title = { Text("Delete image?") }, text = { Text("This image will be permanently removed from the book.") }, confirmButton = { TextButton(onClick = { if (book != null) scope.launch { bookDao.deleteImageAndUpdateBook(book, image); CoverStorage.delete(image.path); existingImagePendingDeletion = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { existingImagePendingDeletion = null }) { Text("Cancel") } }) }
}

@Composable
private fun WishlistDialogImproved(wishlistDao: WishlistDao, activeLibraryId: Long, onDismiss: () -> Unit) {
    val items by wishlistDao.observeForLibrary(activeLibraryId).collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("0") }
    var category by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("Medium") }
    var status by rememberSaveable { mutableStateOf("Planned") }
    var expectedDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    fun clearForm() { title = ""; author = ""; price = "0"; category = ""; priority = "Medium"; status = "Planned"; expectedDate = ""; notes = ""; error = null }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wishlist") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(title, { title = it }, label = { Text("Book title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(author, { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it }, label = { Text("Expected price (INR)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(priority, { priority = it }, label = { Text("Priority: High, Medium, Low") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(status, { status = it }, label = { Text("Status: Planned, Purchased, Deferred") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(expectedDate, { expectedDate = it }, label = { Text("Expected date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Text("Saved items", style = MaterialTheme.typography.titleSmall)
                items.take(10).forEach { item -> Text("${item.title} - ${item.priority} (${item.status})", style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val paise = (price.toDoubleOrNull()?.times(100))?.toLong() ?: -1
                val validDate = expectedDate.isBlank() || runCatching { LocalDate.parse(expectedDate) }.isSuccess
                error = when { title.isBlank() -> "Book title is required."; paise < 0 -> "Expected price cannot be negative."; !validDate -> "Enter a valid expected date."; else -> null }
                if (error == null) scope.launch {
                    wishlistDao.insert(WishlistEntity(libraryId = activeLibraryId, title = title.trim(), author = author.trim().ifBlank { null }, category = category.trim().ifBlank { null }, expectedPricePaise = paise, priority = priority.trim().ifBlank { "Medium" }, status = status.trim().ifBlank { "Planned" }, expectedPurchaseDate = expectedDate.ifBlank { null }, notes = notes.trim().ifBlank { null }))
                    clearForm()
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun CatalogDialogImproved(catalogDao: CatalogDao, onDismiss: () -> Unit) {
    val tags by catalogDao.observeTags().collectAsState(emptyList())
    val collections by catalogDao.observeCollections().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var tagName by rememberSaveable { mutableStateOf("") }
    var collectionName by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Organize library") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Tags", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(tagName, { tagName = it }, label = { Text("New tag") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = {
                    if (tagName.isNotBlank()) scope.launch { runCatching { catalogDao.insertTag(TagEntity(name = tagName.trim())) }.onSuccess { tagName = "" }.onFailure { message = "A tag with that name already exists." } }
                }) { Text("Add tag") }
                tags.forEach { tag -> Text(tag.name, style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(16.dp))
                Text("Collections", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(collectionName, { collectionName = it }, label = { Text("New collection") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = {
                    if (collectionName.isNotBlank()) scope.launch { runCatching { catalogDao.insertCollection(CollectionEntity(name = collectionName.trim())) }.onSuccess { collectionName = "" }.onFailure { message = "A collection with that name already exists." } }
                }) { Text("Add collection") }
                collections.forEach { collection -> Text(collection.name, style = MaterialTheme.typography.bodyMedium) }
                if (message != null) Text(message!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun WishlistDialog(wishlistDao: WishlistDao, bookDao: BookDao, onDismiss: () -> Unit) {
    val items by wishlistDao.observeAll().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("0") }
    var category by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("Medium") }
    var status by rememberSaveable { mutableStateOf("Planned") }
    var expectedDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var filterStatus by rememberSaveable { mutableStateOf("") }
    var filterPriority by rememberSaveable { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<WishlistEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<WishlistEntity?>(null) }
    var purchaseReview by remember { mutableStateOf<WishlistEntity?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = items.filter { (filterStatus.isBlank() || it.status.equals(filterStatus, true)) && (filterPriority.isBlank() || it.priority.equals(filterPriority, true)) }
    fun clearForm() { title = ""; author = ""; price = "0"; category = ""; priority = "Medium"; status = "Planned"; expectedDate = ""; notes = ""; editingItem = null; error = null }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Wishlist") }, text = { Column { OutlinedTextField(filterStatus, { filterStatus = it }, label = { Text("Filter status") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(filterPriority, { filterPriority = it }, label = { Text("Filter priority") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(title, { title = it }, label = { Text("Book title *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(author, { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(price, { price = it }, label = { Text("Expected price (INR)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(priority, { priority = it }, label = { Text("Priority: High, Medium, Low") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(status, { status = it }, label = { Text("Status: Planned, Purchased, Deferred") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(expectedDate, { expectedDate = it }, label = { Text("Expected date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp)); filtered.forEach { item -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.title); Text("${item.priority} | ${item.status} | ${item.expectedPurchaseDate ?: "No date"}", style = MaterialTheme.typography.bodySmall) }; TextButton(onClick = { editingItem = item; title = item.title; author = item.author.orEmpty(); category = item.category.orEmpty(); price = "%.2f".format(item.expectedPricePaise / 100.0); priority = item.priority; status = item.status; expectedDate = item.expectedPurchaseDate.orEmpty(); notes = item.notes.orEmpty() }) { Text("Edit") }; if (item.status == "Planned") TextButton(onClick = { purchaseReview = item }) { Text("Purchase") }; TextButton(onClick = { pendingDelete = item }) { Text("Delete") } } } } }, confirmButton = { TextButton(onClick = { val paise = (price.toDoubleOrNull()?.times(100))?.toLong() ?: -1; val validDate = expectedDate.isBlank() || runCatching { LocalDate.parse(expectedDate) }.isSuccess; error = when { title.isBlank() -> "Book title is required."; paise < 0 -> "Expected price cannot be negative."; !validDate -> "Enter a valid expected date."; else -> null }; if (error == null) scope.launch { val value = (editingItem ?: WishlistEntity(title = title.trim())).copy(title = title.trim(), author = author.trim().ifBlank { null }, category = category.trim().ifBlank { null }, expectedPricePaise = paise, priority = priority.trim().ifBlank { "Medium" }, status = status.trim().ifBlank { "Planned" }, expectedPurchaseDate = expectedDate.ifBlank { null }, notes = notes.trim().ifBlank { null }, updatedAtMillis = System.currentTimeMillis()); if (editingItem == null) wishlistDao.insert(value) else wishlistDao.update(value); clearForm() } }) { Text(if (editingItem == null) "Add" else "Save") } }, dismissButton = { TextButton(onClick = if (editingItem == null) onDismiss else ::clearForm) { Text(if (editingItem == null) "Close" else "Cancel edit") } })
    pendingDelete?.let { item -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete wishlist item?") }, text = { Text("${item.title} will be permanently removed.") }, confirmButton = { TextButton(onClick = { scope.launch { wishlistDao.delete(item.id); pendingDelete = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }) }
    purchaseReview?.let { item -> PurchaseWishlistDialog(item, onDismiss = { purchaseReview = null }) { book -> scope.launch { val id = bookDao.insert(book); if (id > 0) { wishlistDao.update(item.copy(status = "Purchased", updatedAtMillis = System.currentTimeMillis())); purchaseReview = null } } } }
}

@Composable
private fun PurchaseWishlistDialog(item: WishlistEntity, onDismiss: () -> Unit, onSave: (BookEntity) -> Unit) {
    var title by rememberSaveable { mutableStateOf(item.title) }
    var author by rememberSaveable { mutableStateOf(item.author.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(item.category.orEmpty()) }
    var price by rememberSaveable { mutableStateOf("%.2f".format(item.expectedPricePaise / 100.0)) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Review purchased book") }, text = { Column { OutlinedTextField(title, { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(author, { author = it }, label = { Text("Author *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(price, { price = it }, label = { Text("Price (INR)") }, modifier = Modifier.fillMaxWidth()); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val paise = (price.toDoubleOrNull()?.times(100))?.toLong() ?: -1; error = if (title.isBlank() || author.isBlank() || paise < 0) "Title, author, and a valid price are required." else null; if (error == null) onSave(BookEntity(title = title.trim(), author = author.trim(), category = category.trim().ifBlank { null }, pricePaise = paise)) }) { Text("Add to library") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun CatalogDialog(catalogDao: CatalogDao, onDismiss: () -> Unit) {
    val tags by catalogDao.observeTags().collectAsState(emptyList())
    val collections by catalogDao.observeCollections().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var tagName by rememberSaveable { mutableStateOf("") }
    var collectionName by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var tagPendingDeletion by remember { mutableStateOf<TagEntity?>(null) }
    var collectionPendingDeletion by remember { mutableStateOf<CollectionEntity?>(null) }
    var tagEditing by remember { mutableStateOf<TagEntity?>(null) }
    var collectionEditing by remember { mutableStateOf<CollectionEntity?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Organize library") }, text = { Column { Text("Tags", style = MaterialTheme.typography.titleMedium); OutlinedTextField(tagName, { tagName = it }, label = { Text("New tag") }, modifier = Modifier.fillMaxWidth()); TextButton(onClick = { if (tagName.isNotBlank()) scope.launch { runCatching { catalogDao.insertTag(TagEntity(name = tagName.trim())) }.onSuccess { tagName = "" }.onFailure { message = "A tag with that name already exists." } } }) { Text("Add tag") }; if (tags.isEmpty()) Text("No tags") else tags.forEach { tag -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(tag.name, Modifier.weight(1f)); TextButton(onClick = { tagEditing = tag }) { Text("Edit") }; TextButton(onClick = { tagPendingDeletion = tag }) { Text("Delete") } } }; Spacer(Modifier.height(12.dp)); Text("Collections", style = MaterialTheme.typography.titleMedium); OutlinedTextField(collectionName, { collectionName = it }, label = { Text("New collection") }, modifier = Modifier.fillMaxWidth()); TextButton(onClick = { if (collectionName.isNotBlank()) scope.launch { runCatching { catalogDao.insertCollection(CollectionEntity(name = collectionName.trim())) }.onSuccess { collectionName = "" }.onFailure { message = "A collection with that name already exists." } } }) { Text("Add collection") }; if (collections.isEmpty()) Text("No collections") else collections.forEach { collection -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(collection.name); if (!collection.description.isNullOrBlank()) Text(collection.description, style = MaterialTheme.typography.bodySmall) }; TextButton(onClick = { collectionEditing = collection }) { Text("Edit") }; TextButton(onClick = { collectionPendingDeletion = collection }) { Text("Delete") } } }; if (message != null) Text(message!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
    tagPendingDeletion?.let { tag -> AlertDialog(onDismissRequest = { tagPendingDeletion = null }, title = { Text("Delete tag?") }, text = { Text("${tag.name} will be removed from assigned books. Books will not be deleted.") }, confirmButton = { TextButton(onClick = { scope.launch { catalogDao.deleteTagAndAssignments(tag.id); tagPendingDeletion = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { tagPendingDeletion = null }) { Text("Cancel") } }) }
    collectionPendingDeletion?.let { collection -> AlertDialog(onDismissRequest = { collectionPendingDeletion = null }, title = { Text("Delete collection?") }, text = { Text("${collection.name} will be removed from assigned books. Books will not be deleted.") }, confirmButton = { TextButton(onClick = { scope.launch { catalogDao.deleteCollectionAndAssignments(collection.id); collectionPendingDeletion = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { collectionPendingDeletion = null }) { Text("Cancel") } }) }
    tagEditing?.let { tag -> CatalogNameEditor("Rename tag", tag.name, null, { tagEditing = null }) { name, _ -> scope.launch { runCatching { catalogDao.updateTag(tag.copy(name = name)) }.onSuccess { tagEditing = null }.onFailure { message = "A tag with that name already exists." } } } }
    collectionEditing?.let { collection -> CatalogNameEditor("Edit collection", collection.name, collection.description, { collectionEditing = null }) { name, description -> scope.launch { runCatching { catalogDao.updateCollection(collection.copy(name = name, description = description)) }.onSuccess { collectionEditing = null }.onFailure { message = "A collection with that name already exists." } } } }
}

@Composable
private fun CatalogNameEditor(title: String, initialName: String, initialDescription: String?, onDismiss: () -> Unit, onSave: (String, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var description by rememberSaveable { mutableStateOf(initialDescription.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth()); if (initialDescription != null) OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), description.trim().ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(16.dp)) { Text(label); Text(value, style = MaterialTheme.typography.headlineSmall) } } }

@Composable
private fun MessageScreen(title: String, message: String, modifier: Modifier) { Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) { Text(title, style = MaterialTheme.typography.headlineMedium); Text(message) } }

private fun iconFor(destination: Destination) = when (destination) { Destination.Dashboard -> Icons.Outlined.Home; Destination.Library -> Icons.Outlined.LocalLibrary; Destination.Search -> Icons.Outlined.Search; Destination.Loans -> Icons.Outlined.SwapHoriz; Destination.More -> Icons.Outlined.MoreHoriz }
private fun formatInr(paise: Long) = "INR %.2f".format(paise / 100.0)
private fun normalizeForDuplicate(value: String) = value.lowercase().filter { it.isLetterOrDigit() }