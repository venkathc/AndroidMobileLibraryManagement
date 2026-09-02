package com.venkateshgowda.personallibrary.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.venkateshgowda.personallibrary.data.LibraryCategoryEntity
import com.venkateshgowda.personallibrary.data.LibraryDao
import com.venkateshgowda.personallibrary.data.LibraryEntity
import com.venkateshgowda.personallibrary.data.LibraryLanguageEntity
import com.venkateshgowda.personallibrary.data.LibraryAccessRepository
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
import com.venkateshgowda.personallibrary.data.UserRole
import com.venkateshgowda.personallibrary.data.can
import com.venkateshgowda.personallibrary.data.userRole
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Date
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream

private enum class Destination(val label: String, val phoneLabel: String = label) { Dashboard("Dashboard", "Home"), Books("Books"), Library("Library"), Search("Search"), Loans("Loans"), More("More") }

private enum class CatalogItemKind { Tag, Collection, Category, Language }

private data class CatalogItem(val id: Long, val name: String, val kind: CatalogItemKind)

private data class DashboardMetric(val label: String, val value: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

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
    var logoMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
    var signedInUser by remember { mutableStateOf<UserEntity?>(null) }
    var savedSignedInUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var switchBackUser by remember { mutableStateOf<UserEntity?>(null) }
    var sessionRestored by remember { mutableStateOf(false) }
    val accessRepository = remember(database) { LibraryAccessRepository(database) }
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val onboardingComplete by settings.onboardingComplete.collectAsState(false)
    val activeLibraryId by settings.activeLibraryId.collectAsState(null)
    val activeMemberships by (activeLibraryId?.let { libraryId -> database.membershipDao().observeForLibrary(libraryId) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val userRole = signedInUser?.let { user -> activeMemberships.firstOrNull { it.userId == user.id }?.let { UserRole.fromStored(it.role, false) } ?: UserRole.Guest } ?: UserRole.Guest
    val canModify = userRole.can(com.venkateshgowda.personallibrary.data.LibraryPermission.AddBooks) || userRole.can(com.venkateshgowda.personallibrary.data.LibraryPermission.EditBooks)
    val canManageWishlist = userRole.can(com.venkateshgowda.personallibrary.data.LibraryPermission.ManageWishlist)
    val canManageLoans = userRole.can(com.venkateshgowda.personallibrary.data.LibraryPermission.ManageLoans)
    val canManageSettings = userRole.can(com.venkateshgowda.personallibrary.data.LibraryPermission.ManageLibrarySettings)
    val canDelete = userRole.can(com.venkateshgowda.personallibrary.data.LibraryPermission.DeleteBooks)
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val activeLibrary = libraries.firstOrNull { it.id == activeLibraryId }
    LaunchedEffect(libraries, activeLibraryId) {
        if (libraries.isNotEmpty() && activeLibrary == null) settings.setActiveLibraryId(libraries.first().id)
    }
    LaunchedEffect(activeLibraryId, signedInUser?.id) {
        val libraryId = activeLibraryId
        val user = signedInUser
        if (libraryId != null && user != null) accessRepository.ensureMembership(libraryId, user)
    }
    LaunchedEffect(savedSignedInUserId) {
        signedInUser = savedSignedInUserId?.let { userDao.findById(it) }
        sessionRestored = true
    }
    LaunchedEffect(Unit) { settings.clearSignedInUser() }
    LaunchedEffect(Unit) {
        val defaultPasswordHash = PasswordHasher.hash("admin123".toCharArray())
        val admin = userDao.findByUsername("admin")
        if (admin == null) {
            userDao.insert(UserEntity(username = "admin", passwordHash = defaultPasswordHash, isAdmin = true, role = UserRole.Owner.name))
        } else if (!PasswordHasher.matches("admin123".toCharArray(), admin.passwordHash) || admin.userRole != UserRole.Owner) {
            userDao.update(admin.copy(passwordHash = defaultPasswordHash, isAdmin = true, role = UserRole.Owner.name))
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 840.dp
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Library Management") },
                    navigationIcon = {
                        Box {
                            IconButton(
                                onClick = { logoMenuExpanded = true },
                                modifier = Modifier.padding(start = 8.dp).size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.LocalLibrary, contentDescription = "Account and app actions", tint = Color(0xFFFFD166))
                                }
                            }
                            DropdownMenu(expanded = logoMenuExpanded, onDismissRequest = { logoMenuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Switch user") },
                                    onClick = {
                                        logoMenuExpanded = false
                                        switchBackUser = signedInUser
                                        signedInUser = null
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sign out") },
                                    onClick = {
                                        logoMenuExpanded = false
                                        switchBackUser = null
                                        signedInUser = null
                                        savedSignedInUserId = null
                                        scope.launch { settings.clearSignedInUser() }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Exit") },
                                    onClick = {
                                        logoMenuExpanded = false
                                        exitConfirmationVisible = true
                                    }
                                )
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
                        Destination.Dashboard -> DashboardScreen(
                            bookDao, loanDao, wishlistDao, activeLibraryId, contentModifier,
                            displayName = signedInUser?.displayName?.takeIf { it.isNotBlank() } ?: signedInUser?.username.orEmpty(),
                            libraryName = activeLibrary?.name ?: "Personal Library",
                            libraryCount = libraries.size,
                            canModify = canModify,
                            onAddBook = { editedBook = null; editorVisible = true },
                            onAddLibrary = { libraryCreatorVisible = true },
                            onNavigate = { destination = it },
                            onSearch = { query -> homeSearchQuery = query; destination = Destination.Search }
                        )
                        Destination.Books -> BooksScreen(bookDao, libraryDao, contentModifier, canModify, canDelete, { editedBook = it; editorVisible = true })
                        Destination.Library -> LibraryScreen(bookDao, libraryDao, wishlistDao, activeLibraryId, activeLibrary?.name ?: "Personal Library", libraries.size, contentModifier, canModify, canManageWishlist, canDelete, { editedBook = null; editorVisible = true }, { editedBook = it; editorVisible = true }, { wishlistVisible = true }, { catalogVisible = true }, { libraryManagerVisible = true }, { libraryCreatorVisible = true })
                        Destination.Search -> SearchScreen(bookDao, catalogDao, libraryDao, settings, activeLibraryId, homeSearchQuery, contentModifier, canModify, canDelete, { editedBook = it; editorVisible = true })
                        Destination.More -> MoreScreenImproved(database, bookDao, libraryDao, loanDao, wishlistDao, userDao, activeLibraryId, signedInUser, contentModifier, canManageSettings, { settingsVisible = true })
                        Destination.Loans -> LoansScreenImproved(bookDao, loanDao, activeLibraryId, contentModifier, canManageLoans)
                    }
                }
            }
        }
    }
    if (editorVisible && activeLibraryId != null && canModify) BookEditor(editedBook, bookDao, catalogDao, activeLibraryId!!, canDelete, { editorVisible = false }) { book, tagIds, collectionIds, newImagePaths -> scope.launch { val bookId = if (book.id == 0L) bookDao.insert(book.copy(libraryId = activeLibraryId!!)) else { bookDao.update(book); book.id }; val startPosition = bookDao.imagesForBook(bookId).size; val paths = newImagePaths.distinct(); if (paths.isNotEmpty()) bookDao.insertImages(paths.mapIndexed { index, path -> com.venkateshgowda.personallibrary.data.BookImageEntity(bookId = bookId, path = path, position = startPosition + index) }); catalogDao.replaceAssignments(bookId, tagIds, collectionIds); editorVisible = false } }
    if (wishlistVisible && activeLibraryId != null && canManageWishlist) WishlistDialogImproved(wishlistDao, activeLibraryId!!, { wishlistVisible = false })
    if (catalogVisible && canModify && activeLibraryId != null) CatalogDialogImproved(catalogDao, activeLibraryId!!, { catalogVisible = false })
    if (libraryManagerVisible) LibraryManagementDialog(libraryDao, activeLibraryId, canModify, canDelete, { libraryId -> scope.launch { settings.setActiveLibraryId(libraryId); libraryManagerVisible = false } }, { libraryManagerVisible = false })
    if (libraryCreatorVisible && canModify) CreateLibraryDialog(libraryDao, { libraryId -> scope.launch { settings.setActiveLibraryId(libraryId); libraryCreatorVisible = false } }, { libraryCreatorVisible = false })
    if (settingsVisible && signedInUser != null) SettingsDialogImproved(
        database,
        settings,
        signedInUser!!,
        { settingsVisible = false },
        { signedInUser = null; savedSignedInUserId = null; settingsVisible = false; scope.launch { settings.clearSignedInUser(); activity?.finishAndRemoveTask() } },
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
        confirmButton = { Button(onClick = { signedInUser = null; savedSignedInUserId = null; scope.launch { settings.clearSignedInUser(); activity?.finishAndRemoveTask() } }) { Text("Exit") } },
        dismissButton = { TextButton(onClick = { exitConfirmationVisible = false }) { Text("Cancel") } }
    )
    if (!onboardingComplete) OnboardingDialog(onNewCatalogue = { scope.launch { settings.setOnboardingComplete() } }, onImport = { scope.launch { settings.setOnboardingComplete() }; destination = Destination.More })
    if (sessionRestored && signedInUser == null) {
        SignInDialog(
            userDao = userDao,
            onSignedIn = {
                signedInUser = it
                savedSignedInUserId = it.id
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
    var username by rememberSaveable { mutableStateOf("admin") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var resetVisible by rememberSaveable { mutableStateOf(false) }
    var usernameMenuExpanded by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        text = {
            Column {
                Text("Sign in to access your personal library.")
                ExposedDropdownMenuBox(
                    expanded = usernameMenuExpanded,
                    onExpandedChange = { usernameMenuExpanded = !usernameMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; error = null },
                        readOnly = false,
                        label = { Text("Username") },
                        placeholder = { Text("Enter or select a user") },
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
                    val normalizedUsername = username.trim()
                    val normalizedPassword = password.trim()
                    val user = userDao.findByUsername(normalizedUsername)
                    if (normalizedUsername.equals("admin", ignoreCase = true) && normalizedPassword == "admin123") {
                        val defaultHash = PasswordHasher.hash("admin123".toCharArray())
                        val admin = if (user == null) {
                            val newUser = UserEntity(username = "admin", passwordHash = defaultHash, isAdmin = true, role = UserRole.Owner.name)
                            newUser.copy(id = userDao.insert(newUser))
                        } else {
                            val repairedUser = user.copy(passwordHash = defaultHash, isAdmin = true, role = UserRole.Owner.name)
                            userDao.update(repairedUser)
                            repairedUser
                        }
                        val signedInAdmin = admin.copy(lastLoginAtMillis = System.currentTimeMillis())
                        userDao.update(signedInAdmin)
                        onSignedIn(signedInAdmin)
                    } else if (user != null && PasswordHasher.matches(normalizedPassword.toCharArray(), user.passwordHash)) {
                        if (user.accountStatus == "Suspended") error = "This account is suspended. Contact a library administrator."
                        else {
                            val signedInUser = user.copy(lastLoginAtMillis = System.currentTimeMillis())
                            userDao.update(signedInUser)
                            onSignedIn(signedInUser)
                        }
                    } else error = "Incorrect username or password."
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
private fun DashboardScreen(
    bookDao: BookDao,
    loanDao: LoanDao,
    wishlistDao: WishlistDao,
    activeLibraryId: Long?,
    modifier: Modifier,
    onAddBook: () -> Unit,
    onAddLibrary: () -> Unit,
    onNavigate: (Destination) -> Unit,
    onSearch: (String) -> Unit,
    displayName: String,
    libraryName: String,
    libraryCount: Int,
    canModify: Boolean
) {
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val totalBooks by (activeLibraryId?.let { bookDao.observeCountForLibrary(it) } ?: flowOf(0)).collectAsState(0)
    val loans by (activeLibraryId?.let { loanDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val wishlistCost by (activeLibraryId?.let { wishlistDao.observeForLibrary(it) } ?: flowOf<List<WishlistEntity>>(emptyList())).collectAsState(emptyList<WishlistEntity>())
    val recentBooks = books.sortedByDescending { it.createdAtMillis }.take(5)
    val categories = books.groupingBy { it.category ?: "Uncategorized" }.eachCount().entries.sortedByDescending { it.value }.take(5).map { ReportBreakdown(it.key, it.value.toLong()) }
    val uniqueAuthors = books.map { it.author.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().size
    val categoryCount = books.map { it.category?.trim().orEmpty() }.filter { it.isNotEmpty() }.distinct().size
    val greeting = when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
    val favouriteBook = books.filter { it.favourite }.maxByOrNull { it.rating ?: 0 }
    val mostValuableBook = books.maxByOrNull { it.pricePaise }
    val readBooks = books.count { it.readingStatus.equals("Read", ignoreCase = true) || it.readingStatus.equals("Finished", ignoreCase = true) }
    val inProgressBooks = books.filter { it.readingStatus.equals("Reading", true) || it.readingStatus.equals("In progress", true) }.take(5)
    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year
    val booksThisMonth = books.count { book -> book.purchaseDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }?.let { it.year == currentYear && it.monthValue == currentMonth } == true }
    val completedThisYear = books.count { book -> (book.readingStatus.equals("Read", true) || book.readingStatus.equals("Finished", true)) && book.updatedAtMillis >= LocalDate.of(currentYear, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }
    val topCategory = categories.firstOrNull()?.label ?: "No category yet"
    val favouriteAuthor = books.filter { it.favourite }.groupingBy { it.author }.eachCount().maxByOrNull { it.value }?.key ?: "No favourite author yet"
    val averageRating = books.mapNotNull { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0
    val annualGoal = 24
    val continueReadingState = rememberLazyListState()
    val recentBooksState = rememberLazyListState()
    val categoryState = rememberLazyListState()
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { ScreenHeader("Home", "Your library at a glance") {} }
        item { DashboardWelcome(greeting, displayName, libraryName, canModify, onAddBook, onSearch) }
        item { DashboardMetricGrid(totalBooks, uniqueAuthors, categoryCount, libraryCount, readBooks, wishlistCost.size) }
        item { DashboardRailHeader("Continue reading", "Your current reads", Icons.Outlined.Bookmark, continueReadingState) }
        item { if (inProgressBooks.isEmpty()) EmptyDashboardState(canModify, onAddBook) else LazyRow(state = continueReadingState, horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(inProgressBooks, key = { it.id }) { ContinueReadingCard(it) { onNavigate(Destination.Library) } } } }
        item { DashboardRailHeader("Recently added", "Fresh on your shelf", Icons.Outlined.Add, recentBooksState) }
        item { if (recentBooks.isEmpty()) EmptyDashboardState(canModify, onAddBook) else LazyRow(state = recentBooksState, horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(recentBooks, key = { it.id }) { RecentBookCarouselCard(it) } } }
        item { DashboardSectionHeader("Reading insights", "A quiet look at your library", Icons.Outlined.Assessment) }
        item { ReadingInsightsGrid(booksThisMonth, completedThisYear, topCategory, favouriteAuthor, averageRating) }
        item { DashboardSectionHeader("Quick actions", "Keep your library moving", Icons.Outlined.Settings) }
        item { DashboardQuickActions(canModify, onAddBook, onAddLibrary, onNavigate) }
        item { DashboardRailHeader("Books by category", "Your shelves at a glance", Icons.Outlined.Bookmark, categoryState) }
        item { if (categories.isEmpty()) EmptyContentState("No categories yet", "Add a category to see your collection take shape.") else LazyRow(state = categoryState, horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(categories, key = { it.label }) { CategoryCard(it) } } }
        item { ReadingGoalCard(readBooks, annualGoal) }
        item { DashboardSectionHeader("Recent activity", "The latest changes in your library", Icons.Outlined.SwapHoriz) }
        item { RecentActivityCard(recentBooks, loans, books) }
        item { SmartInsightsCard(books, favouriteAuthor, topCategory, mostValuableBook, favouriteBook) }
    }
}

@Composable
private fun CollectionHero(greeting: String, displayName: String, totalBooks: Int, investment: Long, wishlistPaise: Long, onAddBook: () -> Unit) {
    val animatedBookCount by animateIntAsState(totalBooks, label = "book count")
    Card(
        Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            Modifier.background(Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFF0F766E)))).padding(22.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(greeting.uppercase(), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.78f), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Welcome back, $displayName", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Your library is ready whenever you are.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.82f))
                }
                IconButton(onClick = onAddBook, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f))) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add book", tint = Color.White)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HeroDetail("$animatedBookCount", if (totalBooks == 1) "book" else "books")
                HeroDetail(formatInr(investment), "invested")
                HeroDetail(formatInr(wishlistPaise), "wishlist")
            }
        }
    }
}

@Composable
private fun HeroDetail(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
    }
}

@Composable
private fun DashboardWelcome(greeting: String, displayName: String, libraryName: String, canModify: Boolean, onAddBook: () -> Unit, onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
    Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFF0F766E)))).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                    Text(displayName.firstOrNull()?.uppercase() ?: "U", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(greeting, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.78f))
                    Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(libraryName, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currentDate, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (canModify) IconButton(onClick = onAddBook, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f))) { Icon(Icons.Outlined.Add, "Add book", tint = Color.White) }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search your books") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { IconButton(onClick = { onSearch(query) }) { Icon(Icons.Outlined.Search, "Search library") } },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                    focusedLeadingIconColor = Color.White,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.85f),
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.85f),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.65f),
                    cursorColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DashboardMetricGrid(totalBooks: Int, authors: Int, categories: Int, libraries: Int, read: Int, wishlist: Int) {
    val metrics = listOf(
        DashboardMetric("Total books", totalBooks.toString(), MaterialTheme.colorScheme.primaryContainer, Icons.Outlined.LocalLibrary),
        DashboardMetric("Authors", authors.toString(), MaterialTheme.colorScheme.secondaryContainer, Icons.Outlined.People),
        DashboardMetric("Categories", categories.toString(), MaterialTheme.colorScheme.tertiaryContainer, Icons.Outlined.Bookmark),
        DashboardMetric("Libraries", libraries.toString(), MaterialTheme.colorScheme.surfaceVariant, Icons.Outlined.LocalLibrary),
        DashboardMetric("Books read", read.toString(), Color(0xFFE0F2E9), Icons.Outlined.Bookmark),
        DashboardMetric("Wishlist", wishlist.toString(), Color(0xFFFFE8B6), Icons.Outlined.Bookmark)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowMetrics.forEach { metric -> DashboardMetricCard(metric.label, metric.value, metric.color, metric.icon, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DashboardSectionHeader(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.padding(start = 10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DashboardRailHeader(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, state: androidx.compose.foundation.lazy.LazyListState) {
    val scope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { scope.launch { state.animateScrollToItem((state.firstVisibleItemIndex - 2).coerceAtLeast(0)) } }, enabled = state.canScrollBackward) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Scroll $title left")
        }
        IconButton(onClick = { scope.launch { state.animateScrollToItem((state.firstVisibleItemIndex + 2).coerceAtMost((state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))) } }, enabled = state.canScrollForward) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Scroll $title right")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContinueReadingCard(book: BookEntity, onContinue: () -> Unit) {
    Card(onClick = onContinue, modifier = Modifier.widthIn(min = 272.dp, max = 296.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(10.dp).height(106.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(book, Modifier.size(76.dp))
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.Center) {
                Text(book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(progress = 0.5f, modifier = Modifier.fillMaxWidth())
                Text("Continue reading", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun RecentBookCarouselCard(book: BookEntity) {
    Card(Modifier.widthIn(min = 216.dp, max = 236.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(10.dp).height(86.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(book, Modifier.size(62.dp))
            Column(Modifier.padding(start = 10.dp).weight(1f), verticalArrangement = Arrangement.Center) {
                Text(book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.purchaseDate ?: "Recently added", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun BookCover(book: BookEntity, modifier: Modifier) {
    if (book.coverImagePath != null) AsyncImage(book.coverImagePath, "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = modifier.clip(RoundedCornerShape(12.dp)))
    else Box(modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.LocalLibrary, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(34.dp)) }
}

@Composable
private fun ReadingInsightsGrid(monthly: Int, completed: Int, category: String, author: String, rating: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HighlightCard("Added this month", monthly.toString(), "New books", Modifier.weight(1f))
            HighlightCard("Completed this year", completed.toString(), "Finished reads", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HighlightCard("Most read category", category, "Your top shelf", Modifier.weight(1f))
            HighlightCard("Favourite author", author, if (rating == 0.0) "No ratings yet" else "Average rating %.1f / 5".format(rating), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DashboardQuickActions(canModify: Boolean, onAddBook: () -> Unit, onAddLibrary: () -> Unit, onNavigate: (Destination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (canModify) DashboardAction("Add book", Icons.Outlined.Add, Modifier.weight(1f), onAddBook)
            if (canModify) DashboardAction("Add library", Icons.Outlined.LocalLibrary, Modifier.weight(1f), onAddLibrary)
            DashboardAction("Search ISBN", Icons.Outlined.Search, Modifier.weight(1f)) { onNavigate(Destination.Search) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardAction("Import", Icons.Outlined.UploadFile, Modifier.weight(1f)) { onNavigate(Destination.More) }
            DashboardAction("Export", Icons.Outlined.Download, Modifier.weight(1f)) { onNavigate(Destination.More) }
            DashboardAction("Insights", Icons.Outlined.Assessment, Modifier.weight(1f)) { onNavigate(Destination.More) }
        }
    }
}

@Composable
private fun CategoryCard(category: ReportBreakdown) {
    Card(Modifier.widthIn(min = 130.dp, max = 150.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.padding(14.dp)) {
            Text(category.value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(category.label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReadingGoalCard(completed: Int, goal: Int) {
    val progress = (completed.toFloat() / goal).coerceIn(0f, 1f)
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(18.dp)) {
            Text("2026 reading goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$completed of $goal books completed", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp))
            Text("${(goal - completed).coerceAtLeast(0)} books remaining", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun RecentActivityCard(recentBooks: List<BookEntity>, loans: List<LoanEntity>, books: List<BookEntity>) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            recentBooks.take(2).forEach { ActivityRow("Added", it.title) }
            books.filter { it.rating != null }.sortedByDescending { it.updatedAtMillis }.take(1).forEach { ActivityRow("Rating updated", it.title) }
            books.filter { !it.notes.isNullOrBlank() }.sortedByDescending { it.updatedAtMillis }.take(1).forEach { ActivityRow("Note added", it.title) }
            loans.filter { it.actualReturnDate != null }.take(1).forEach { loan -> ActivityRow("Book returned", books.firstOrNull { it.id == loan.bookId }?.title ?: "Deleted book") }
            if (recentBooks.isEmpty() && loans.isEmpty()) Text("Activity will appear as you add, rate, and lend books.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActivityRow(event: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Column(Modifier.padding(start = 10.dp)) { Text(event, style = MaterialTheme.typography.labelLarge); Text(detail, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun SmartInsightsCard(books: List<BookEntity>, favouriteAuthor: String, category: String, mostValuable: BookEntity?, favourite: BookEntity?) {
    val unread = books.filter { it.readingStatus.equals("Unread", true) }.take(1).firstOrNull()
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF14532D)))).padding(18.dp)) {
            Text("Smart library insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Private recommendations from your on-device catalogue", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f))
            Spacer(Modifier.height(12.dp))
            Text("Favourite genre: $category", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text("Most purchased author: $favouriteAuthor", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text("Suggested next read: ${unread?.title ?: favourite?.title ?: mostValuable?.title ?: "Add a book to begin"}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DashboardMetricCard(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier.heightIn(min = 104.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
            Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.heightIn(min = 82.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RecentBookCard(book: BookEntity) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (book.coverImagePath != null) {
                AsyncImage(model = book.coverImagePath, contentDescription = "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = Modifier.size(58.dp, 76.dp).clip(RoundedCornerShape(10.dp)))
            } else {
                Box(Modifier.size(58.dp, 76.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LocalLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
                }
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${book.readingStatus}  |  ${formatInr(book.pricePaise)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun HighlightCard(label: String, title: String, detail: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DashboardActivityPanel(title: String, loans: List<LoanEntity>, books: List<BookEntity>, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (loans.isEmpty()) Text("No activity yet.", style = MaterialTheme.typography.bodySmall)
            else loans.forEach { loan ->
                val bookTitle = books.firstOrNull { it.id == loan.bookId }?.title ?: "Deleted book"
                Text(bookTitle, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (loan.actualReturnDate == null) "To ${loan.borrowerName}" else "Returned ${loan.actualReturnDate}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun EmptyDashboardState(canModify: Boolean, onAddBook: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(18.dp)) {
            Text("Start your next chapter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Add your first book to bring your collection to life.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            if (canModify) TextButton(onClick = onAddBook) { Icon(Icons.Outlined.Add, contentDescription = null); Text(" Add a book") }
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryHubCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.heightIn(min = 112.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmptyContentState(title: String, message: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreActionCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LoansScreenImproved(bookDao: BookDao, loanDao: LoanDao, activeLibraryId: Long?, modifier: Modifier, canModify: Boolean) {
    val context = LocalContext.current
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val loans by (activeLibraryId?.let { loanDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var addVisible by rememberSaveable { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var loanFilter by rememberSaveable { mutableStateOf("All") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOrder by rememberSaveable { mutableStateOf("Due date") }
    var loanPage by rememberSaveable { mutableStateOf(0) }
    val today = LocalDate.now()
    val availableBooks = books.filter { book -> loans.none { it.bookId == book.id && it.actualReturnDate == null } }
    val overdueLoans = loans.filter { it.actualReturnDate == null && it.expectedReturnDate != null && it.expectedReturnDate < today.toString() }
    val activeLoans = loans.filter { it.actualReturnDate == null }
    val dueSoonLoans = activeLoans.filter { loan ->
        loan.expectedReturnDate?.let { dueDate -> runCatching { LocalDate.parse(dueDate) }.getOrNull()?.let { !it.isBefore(today) && !it.isAfter(today.plusDays(7)) } } == true
    }
    val displayedLoans = loans.asSequence()
        .filter { loan ->
            when (loanFilter) {
                "Active" -> loan.actualReturnDate == null
                "Overdue" -> loan in overdueLoans
                "Due soon" -> loan in dueSoonLoans
                "Returned" -> loan.actualReturnDate != null
                else -> true
            }
        }
        .filter { loan ->
            val book = books.firstOrNull { it.id == loan.bookId }
            searchQuery.isBlank() || listOfNotNull(book?.title, book?.author, loan.borrowerName, loan.borrowerContact, loan.borrowedDate, loan.expectedReturnDate)
                .any { it.contains(searchQuery, ignoreCase = true) }
        }
        .sortedWith(when (sortOrder) {
            "Borrower" -> compareBy<LoanEntity> { it.borrowerName.lowercase() }
            "Borrowed date" -> compareByDescending { it.borrowedDate }
            "Newest" -> compareByDescending { it.createdAtMillis }
            else -> compareBy<LoanEntity> { it.expectedReturnDate ?: "9999-12-31" }
        })
        .toList()
    val loanPageCount = (displayedLoans.size + 9) / 10
    val pageLoans = displayedLoans.drop(loanPage * 10).take(10)
    Column(modifier.fillMaxSize().padding(20.dp)) {
        ScreenHeader("Loans", "Keep every borrowed book in view") {
            if (canModify) IconButton(onClick = { addVisible = true }, enabled = availableBooks.isNotEmpty(), modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Outlined.Add, contentDescription = "Record loan", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (overdueLoans.isNotEmpty()) Color(0xFFFFE5E1) else MaterialTheme.colorScheme.secondaryContainer)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (overdueLoans.isEmpty()) "Everything is on track" else "Action needed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${activeLoans.size} active  |  ${overdueLoans.size} overdue  |  ${dueSoonLoans.size} due this week  |  ${loans.count { it.actualReturnDate != null }} returned", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            books.isEmpty() -> Text("Add a book before recording a loan.")
            availableBooks.isEmpty() -> Text("All ${books.size} books currently have active loans. Mark a book returned before recording another loan.")
        }
        if (loans.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("All", "Active", "Overdue").forEach { filter ->
                    Button(onClick = { loanFilter = filter; loanPage = 0 }, modifier = Modifier.weight(1f), colors = if (loanFilter == filter) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text(filter, maxLines = 1) }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Due soon", "Returned").forEach { filter ->
                    Button(onClick = { loanFilter = filter; loanPage = 0 }, modifier = Modifier.weight(1f), colors = if (loanFilter == filter) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text(filter, maxLines = 1) }
                }
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(searchQuery, { searchQuery = it; loanPage = 0 }, label = { Text("Search books, borrowers, or dates") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            ManagedCatalogDropdown("Sort by", sortOrder, listOf("Due date", "Borrowed date", "Borrower", "Newest")) { sortOrder = it; loanPage = 0 }
            Spacer(Modifier.height(10.dp))
        }
        if (loans.isEmpty()) Text("No loan history yet.") else if (displayedLoans.isEmpty()) Text("No loans match the selected filter.") else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pageLoans, key = { it.id }) { loan ->
                val book = books.firstOrNull { it.id == loan.bookId }
                val dueDate = loan.expectedReturnDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val dueLabel = when {
                    loan.actualReturnDate != null -> "Returned on ${loan.actualReturnDate}"
                    dueDate == null -> "No due date"
                    dueDate.isBefore(today) -> "${java.time.temporal.ChronoUnit.DAYS.between(dueDate, today)} days overdue"
                    dueDate == today -> "Due today"
                    dueDate == today.plusDays(1) -> "Due tomorrow"
                    else -> "Due in ${java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)} days"
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(book?.title ?: "Deleted book", style = MaterialTheme.typography.titleMedium)
                        Text("Borrower: ${loan.borrowerName}")
                        if (!loan.borrowerContact.isNullOrBlank()) Text("Contact: ${loan.borrowerContact}")
                        Text("Borrowed: ${loan.borrowedDate}")
                        Text("Due: ${loan.expectedReturnDate ?: "Not set"}")
                        Text(dueLabel, color = if (dueDate?.isBefore(today) == true && loan.actualReturnDate == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        if (loan.actualReturnDate == null && canModify) Row {
                            TextButton(onClick = { editingLoan = loan }) { Text("Edit") }
                            TextButton(onClick = { scope.launch { loanDao.markReturned(loan.id, today.toString()) } }) { Text("Mark returned") }
                            if (!loan.borrowerContact.isNullOrBlank()) TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(loan.borrowerContact)}"))) }) { Text("Contact") }
                        }
                    }
                }
            }
            if (loanPageCount > 1) item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { loanPage -= 1 }, enabled = loanPage > 0) { Text("Previous") }
                    Text("Page ${loanPage + 1} of $loanPageCount")
                    TextButton(onClick = { loanPage += 1 }, enabled = loanPage < loanPageCount - 1) { Text("Next") }
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
    var query by rememberSaveable { mutableStateOf("") }
    val selectedBook = books.firstOrNull { it.id == selectedBookId }
    val matchingBooks = books.asSequence()
        .filter { book ->
            query.isBlank() || listOf(book.title, book.author, book.category.orEmpty())
                .any { it.contains(query, ignoreCase = true) }
        }
        .sortedWith(compareBy<BookEntity> { it.title.lowercase() }.thenBy { it.author.lowercase() })
        .toList()
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Select book", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Choose an available book for this loan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "Close book selection") }
                }
                Spacer(Modifier.height(16.dp))
                Text("${books.size} available books", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selectedBook != null) {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(selectedBook.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(selectedBook.author, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(query, { query = it }, label = { Text("Search title, author, or category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                if (matchingBooks.isEmpty()) Text("No available books match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(matchingBooks, key = { it.id }) { book ->
                        val isSelected = book.id == selectedBookId
                        Card(modifier = Modifier.fillMaxWidth().clickable { onSelect(book.id) }, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    book.category?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                                if (isSelected) Text("Selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(bookDao: BookDao, libraryDao: LibraryDao, wishlistDao: WishlistDao, activeLibraryId: Long?, libraryName: String, libraryCount: Int, modifier: Modifier, canModify: Boolean, canManageWishlist: Boolean, canDelete: Boolean, onAdd: () -> Unit, onEdit: (BookEntity) -> Unit, onWishlist: () -> Unit, onCatalog: () -> Unit, onManageLibraries: () -> Unit, onCreateLibrary: () -> Unit) {
    val books by (activeLibraryId?.let { bookDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val wishlist by (activeLibraryId?.let { wishlistDao.observeForLibrary(it) } ?: flowOf(emptyList())).collectAsState(emptyList())
    var viewingBooks by rememberSaveable { mutableStateOf(false) }
    var viewingWishlist by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        ScreenHeader("Library", "Build a collection that feels like yours") {
            if (canModify) IconButton(onClick = onAdd, modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Outlined.Add, contentDescription = "Add book", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(16.dp))
        MoreProfileCard(libraryName, books.size, libraryCount)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LibraryHubCard("Books", books.size.toString(), Icons.Outlined.LocalLibrary, Modifier.weight(1f)) { viewingBooks = true; viewingWishlist = false }
            LibraryHubCard("Wishlist", wishlist.size.toString(), Icons.Outlined.Bookmark, Modifier.weight(1f)) { viewingWishlist = true; viewingBooks = false }
        }
        Spacer(Modifier.height(10.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${libraries.size} libraries", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onManageLibraries) { Text("Manage") }
                if (canModify) TextButton(onClick = onCreateLibrary) { Text("New") }
                if (canModify) TextButton(onClick = onCatalog) { Text("Organize") }
            }
        }
        if (canManageWishlist) TextButton(onClick = onWishlist) { Text("Manage wishlist") }
        Spacer(Modifier.height(16.dp))
        if (viewingBooks) {
            if (books.isEmpty()) Text("No books yet. Import your web catalogue or add your first book.") else BookList(books, bookDao, onEdit, canModify, canDelete, Modifier.weight(1f))
        } else if (viewingWishlist) {
            if (wishlist.isEmpty()) Text("No wishlist books yet. Add one from Wishlist.") else WishlistList(wishlist, Modifier.weight(1f), wishlistDao, bookDao, canManageWishlist, canDelete)
        } else EmptyContentState("Your shelves are ready", "Choose Books or Wishlist to explore this library.")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BooksScreen(bookDao: BookDao, libraryDao: LibraryDao, modifier: Modifier, canModify: Boolean, canDelete: Boolean, onEdit: (BookEntity) -> Unit) {
    val allBooks by bookDao.observeAll().collectAsState(emptyList())
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var refreshing by rememberSaveable { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf("Recently Added") }
    var libraryFilter by rememberSaveable { mutableStateOf(0L) }
    var categoryFilter by rememberSaveable { mutableStateOf("") }
    var authorFilter by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf("") }
    var favouritesOnly by rememberSaveable { mutableStateOf(false) }
    var summaryFilter by rememberSaveable { mutableStateOf("Total") }
    var bookPendingDeletion by remember { mutableStateOf<BookEntity?>(null) }
    val listState = rememberLazyListState()
    val normalizedQuery = query.trim().lowercase()
    val summaryBooks = allBooks.filter { book -> libraryFilter == 0L || book.libraryId == libraryFilter }
    val books = allBooks.asSequence()
        .filter { book -> libraryFilter == 0L || book.libraryId == libraryFilter }
        .filter { book -> categoryFilter.isBlank() || book.category.orEmpty().equals(categoryFilter, ignoreCase = true) }
        .filter { book -> authorFilter.isBlank() || book.author.equals(authorFilter, ignoreCase = true) }
        .filter { book -> statusFilter.isBlank() || book.readingStatus.equals(statusFilter, ignoreCase = true) }
        .filter { book -> !favouritesOnly || book.favourite }
        .filter { book -> when (summaryFilter) {
            "Read" -> book.readingStatus.equals("Read", true) || book.readingStatus.equals("Completed", true)
            "Reading" -> book.readingStatus.equals("Reading", true)
            "Unread" -> book.readingStatus.equals("Unread", true)
            "Favourites" -> book.favourite
            else -> true
        } }
        .filter { book -> normalizedQuery.isBlank() || listOf(book.title, book.author, book.category.orEmpty(), book.publisher.orEmpty(), book.isbn.orEmpty()).any { value -> value.lowercase().contains(normalizedQuery) } }
        .let { results -> when (sort) {
            "Oldest Added" -> results.sortedBy { it.createdAtMillis }
            "Title A-Z" -> results.sortedBy { it.title.lowercase() }
            "Title Z-A" -> results.sortedByDescending { it.title.lowercase() }
            "Author Name" -> results.sortedBy { it.author.lowercase() }
            "Purchase Date" -> results.sortedByDescending { it.purchaseDate.orEmpty() }
            "Rating" -> results.sortedByDescending { it.rating ?: 0 }
            "Price" -> results.sortedByDescending { it.pricePaise }
            else -> results.sortedByDescending { it.createdAtMillis }
        } }
        .toList()
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = { refreshing = true })
    LaunchedEffect(refreshing) {
        if (refreshing) {
            bookDao.allBooks()
            delay(300)
            refreshing = false
        }
    }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        ScreenHeader("Your Books", "Browse and manage your complete collection") {
            Row {
                IconButton(onClick = { filtersVisible = true }) { Icon(Icons.Outlined.Settings, contentDescription = "Filter and sort books") }
                IconButton(onClick = { refreshing = true }) { Icon(Icons.Outlined.Refresh, contentDescription = "Refresh books") }
            }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(query, { query = it }, label = { Text("Search title, author, category, publisher, or ISBN") }, leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { BooksSummaryChip("Total", summaryBooks.size, summaryFilter == "Total") { summaryFilter = if (summaryFilter == "Total") "" else "Total" } }
            item { BooksSummaryChip("Read", summaryBooks.count { it.readingStatus.equals("Read", true) || it.readingStatus.equals("Completed", true) }, summaryFilter == "Read") { summaryFilter = if (summaryFilter == "Read") "Total" else "Read" } }
            item { BooksSummaryChip("Reading", summaryBooks.count { it.readingStatus.equals("Reading", true) }, summaryFilter == "Reading") { summaryFilter = if (summaryFilter == "Reading") "Total" else "Reading" } }
            item { BooksSummaryChip("Unread", summaryBooks.count { it.readingStatus.equals("Unread", true) }, summaryFilter == "Unread") { summaryFilter = if (summaryFilter == "Unread") "Total" else "Unread" } }
            item { BooksSummaryChip("Favourites", summaryBooks.count { it.favourite }, summaryFilter == "Favourites") { summaryFilter = if (summaryFilter == "Favourites") "Total" else "Favourites" } }
        }
        Spacer(Modifier.height(10.dp))
        Text("${books.size} ${if (books.size == 1) "book" else "books"} | $sort", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.weight(1f).fillMaxWidth().pullRefresh(pullRefreshState)) {
            if (allBooks.isEmpty()) EmptyContentState("No books yet", if (canModify) "Add your first book from Library." else "No books have been added to this library yet.")
            else if (books.isEmpty()) EmptyContentState("No books found", "Try another title, author, category, publisher, or ISBN.")
            else LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) { items(books, key = { it.id }) { book -> YourBookCard(book, libraries.firstOrNull { it.id == book.libraryId }?.name ?: "Unknown library", canModify, canDelete, onDetails = { selectedBook = book }, onEdit = { onEdit(book) }, onDelete = { bookPendingDeletion = book }, onFavourite = { updated -> scope.launch { bookDao.update(updated) } }, onStatus = { updated -> scope.launch { bookDao.update(updated) } }) } }
            PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
    selectedBook?.let { book -> BookInformationDialog(book, libraries.firstOrNull { it.id == book.libraryId }?.name ?: "Unknown library", canModify, onDismiss = { selectedBook = null }, onEdit = { selectedBook = null; onEdit(book) }) }
    bookPendingDeletion?.let { book -> AlertDialog(onDismissRequest = { bookPendingDeletion = null }, title = { Text("Delete book?") }, text = { Text("${book.title} will be permanently deleted.") }, confirmButton = { Button(onClick = { scope.launch { bookDao.delete(book.id); CoverStorage.delete(book.coverImagePath); bookPendingDeletion = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { bookPendingDeletion = null }) { Text("Cancel") } }) }
    if (filtersVisible) BooksFilterDialog(libraries, allBooks, sort, libraryFilter, categoryFilter, authorFilter, statusFilter, favouritesOnly, onApply = { newSort, newLibrary, newCategory, newAuthor, newStatus, newFavouritesOnly -> sort = newSort; libraryFilter = newLibrary; categoryFilter = newCategory; authorFilter = newAuthor; statusFilter = newStatus; favouritesOnly = newFavouritesOnly; filtersVisible = false }, onDismiss = { filtersVisible = false })
}

@Composable
private fun BooksSummaryChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val foreground = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = background) {
        Text("$label $count", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, color = foreground)
    }
}

@Composable
private fun YourBookCard(book: BookEntity, libraryName: String, canModify: Boolean, canDelete: Boolean, onDetails: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onFavourite: (BookEntity) -> Unit, onStatus: (BookEntity) -> Unit) {
    var actionsVisible by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().clickable(onClick = onDetails), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (book.coverImagePath != null) AsyncImage(model = book.coverImagePath, contentDescription = "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = Modifier.size(64.dp, 88.dp).clip(RoundedCornerShape(8.dp)))
            else Box(Modifier.size(64.dp, 88.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.LocalLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.category ?: "Uncategorized", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(libraryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                book.purchaseDate?.let { Text("Purchased $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(book.readingStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    book.rating?.let { rating -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFF0A500)); Text(rating.toString(), style = MaterialTheme.typography.labelSmall) } }
                    if (book.favourite) Icon(Icons.Outlined.Favorite, contentDescription = "Favourite", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Box {
                IconButton(onClick = { actionsVisible = true }) { Icon(Icons.Outlined.MoreHoriz, contentDescription = "Actions for ${book.title}") }
                DropdownMenu(expanded = actionsVisible, onDismissRequest = { actionsVisible = false }) {
                    DropdownMenuItem(text = { Text("View details") }, onClick = { actionsVisible = false; onDetails() })
                    if (canModify) {
                        DropdownMenuItem(text = { Text("Edit book") }, onClick = { actionsVisible = false; onEdit() })
                        DropdownMenuItem(text = { Text(if (book.favourite) "Remove favourite" else "Mark as favourite") }, onClick = { actionsVisible = false; onFavourite(book.copy(favourite = !book.favourite, updatedAtMillis = System.currentTimeMillis())) })
                        DropdownMenuItem(text = { Text("Update reading status") }, onClick = { actionsVisible = false; onStatus(book.copy(readingStatus = nextReadingStatus(book.readingStatus), updatedAtMillis = System.currentTimeMillis())) })
                    }
                    if (canDelete) DropdownMenuItem(text = { Text("Delete book", color = MaterialTheme.colorScheme.error) }, onClick = { actionsVisible = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun BookInformationDialog(book: BookEntity, libraryName: String, canModify: Boolean, onDismiss: () -> Unit, onEdit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book information") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (book.coverImagePath != null) AsyncImage(model = book.coverImagePath, contentDescription = "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(8.dp)))
                Text(book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(book.author, style = MaterialTheme.typography.titleMedium)
                Text("Category: ${book.category ?: "Uncategorized"}")
                Text("Library: $libraryName")
                Text("Price: ${formatInr(book.pricePaise)}")
                book.purchaseDate?.let { Text("Purchase date: $it") }
                Text("Reading status: ${book.readingStatus}")
                Text("Rating: ${book.rating?.toString() ?: "Not rated"}")
                Text("Favourite: ${if (book.favourite) "Yes" else "No"}")
                book.publisher?.let { Text("Publisher: $it") }
                book.isbn?.let { Text("ISBN: $it") }
                book.barcode?.let { Text("Book barcode: $it") }
                book.language?.let { Text("Language: $it") }
            }
        },
        confirmButton = { if (canModify) TextButton(onClick = onEdit) { Text("Edit") } else TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = { if (canModify) TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private fun nextReadingStatus(status: String) = when (status.lowercase()) { "unread" -> "Reading"; "reading", "in progress" -> "Completed"; else -> "Unread" }

@Composable
private fun BooksFilterDialog(libraries: List<LibraryEntity>, books: List<BookEntity>, initialSort: String, initialLibrary: Long, initialCategory: String, initialAuthor: String, initialStatus: String, initialFavouritesOnly: Boolean, onApply: (String, Long, String, String, String, Boolean) -> Unit, onDismiss: () -> Unit) {
    var sort by rememberSaveable { mutableStateOf(initialSort) }
    var libraryId by rememberSaveable { mutableStateOf(initialLibrary) }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var author by rememberSaveable { mutableStateOf(initialAuthor) }
    var status by rememberSaveable { mutableStateOf(initialStatus) }
    var favouritesOnly by rememberSaveable { mutableStateOf(initialFavouritesOnly) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Sort and filter") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ManagedCatalogDropdown("Sort", sort, listOf("Recently Added", "Oldest Added", "Title A-Z", "Title Z-A", "Author Name", "Purchase Date", "Rating", "Price")) { sort = it }
        ManagedCatalogDropdown("Library", libraries.firstOrNull { it.id == libraryId }?.name.orEmpty(), listOf("All libraries") + libraries.map { it.name }) { selected -> libraryId = libraries.firstOrNull { it.name == selected }?.id ?: 0L }
        ManagedCatalogDropdown("Category", category, books.mapNotNull { it.category }.distinct().sorted()) { category = it }
        ManagedCatalogDropdown("Author", author, books.map { it.author }.distinct().sorted()) { author = it }
        ManagedCatalogDropdown("Reading status", status, listOf("Unread", "Reading", "Completed")) { status = it }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(favouritesOnly, { favouritesOnly = it }); Text("Favourite books only") }
    } }, confirmButton = { TextButton(onClick = { onApply(sort, libraryId, category, author, status, favouritesOnly) }) { Text("Apply") } }, dismissButton = { TextButton(onClick = { onApply("Recently Added", 0L, "", "", "", false) }) { Text("Clear") } })
}

@Composable
private fun LibraryManagementDialog(libraryDao: LibraryDao, activeLibraryId: Long?, canModify: Boolean, canDelete: Boolean, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var creating by rememberSaveable { mutableStateOf(false) }
    var editingLibrary by remember { mutableStateOf<LibraryEntity?>(null) }
    var libraryPendingDeletion by remember { mutableStateOf<LibraryEntity?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text("Libraries", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                if (libraries.isEmpty()) Text("Create a library to record its owner and details.")
                else LazyColumn(Modifier.heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(libraries, key = { it.id }) { library ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onSelect(library.id) }, enabled = library.id != activeLibraryId, modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxWidth()) {
                            Text(library.name, style = MaterialTheme.typography.titleMedium)
                            Text("Owner: ${library.owner}", style = MaterialTheme.typography.bodySmall)
                            if (!library.description.isNullOrBlank()) Text(library.description, style = MaterialTheme.typography.bodySmall)
                            Text(if (library.id == activeLibraryId) "Current library" else "Tap to open", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (canModify) TextButton(onClick = { editingLibrary = library }) { Text("Edit") }
                        if (canDelete) TextButton(onClick = { libraryPendingDeletion = library }, enabled = libraries.size > 1) { Text("Delete") }
                    }
                }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    if (canModify) TextButton(onClick = { creating = true }) { Text("Create library") }
                }
            }
        }
    }
    if (creating) CreateLibraryDialog(libraryDao, { libraryId -> onSelect(libraryId) }, { creating = false })
    if (editingLibrary != null) EditLibraryDialog(editingLibrary!!, libraryDao, { editingLibrary = null })
    if (libraryPendingDeletion != null) AlertDialog(
        onDismissRequest = { libraryPendingDeletion = null },
        title = { Text("Delete ${libraryPendingDeletion!!.name}?") },
        text = { Text("This permanently deletes its books, loans, and wishlist items.") },
        confirmButton = {
            Button(onClick = {
                val library = libraryPendingDeletion!!
                val replacementId = libraries.first { it.id != library.id }.id
                scope.launch {
                    libraryDao.deleteLibrary(library.id)
                    libraryPendingDeletion = null
                    if (library.id == activeLibraryId) onSelect(replacementId)
                }
            }) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = { libraryPendingDeletion = null }) { Text("Cancel") } }
    )
}

@Composable
private fun EditLibraryDialog(library: LibraryEntity, libraryDao: LibraryDao, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable(library.id) { mutableStateOf(library.name) }
    var description by rememberSaveable(library.id) { mutableStateOf(library.description.orEmpty()) }
    var owner by rememberSaveable(library.id) { mutableStateOf(library.owner) }
    var error by rememberSaveable(library.id) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit library") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it; error = null }, label = { Text("Library name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(owner, { owner = it; error = null }, label = { Text("Owner *") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedName = name.trim()
                val trimmedOwner = owner.trim()
                if (trimmedName.isBlank() || trimmedOwner.isBlank()) {
                    error = "Library name and owner are required."
                } else {
                    scope.launch {
                        runCatching { libraryDao.update(library.copy(name = trimmedName, description = description.trim().ifBlank { null }, owner = trimmedOwner)) }
                            .onSuccess { onDismiss() }
                            .onFailure { error = "A library with this name already exists." }
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
private fun WishlistList(items: List<WishlistEntity>, modifier: Modifier = Modifier, wishlistDao: WishlistDao, bookDao: BookDao, canModify: Boolean, canDelete: Boolean) {
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
                    if (canModify) Row { if (item.status != "Purchased") TextButton(onClick = { purchaseReview = item }) { Text("Mark completed") }; TextButton(onClick = { editingItem = item }) { Text("Edit") }; if (canDelete) TextButton(onClick = { pendingDeletion = item }) { Text("Delete") } }
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
private fun SearchScreen(bookDao: BookDao, catalogDao: CatalogDao, libraryDao: LibraryDao, settings: AppSettings, activeLibraryId: Long?, initialQuery: String, modifier: Modifier, canModify: Boolean, canDelete: Boolean, onEdit: (BookEntity) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var readingStatus by rememberSaveable { mutableStateOf("") }
    var tag by rememberSaveable { mutableStateOf("") }
    var collection by rememberSaveable { mutableStateOf("") }
    var favouritesOnly by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf("Recently Added") }
    var libraryFilter by rememberSaveable { mutableStateOf(activeLibraryId ?: 0L) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(initialQuery) { if (initialQuery.isNotBlank()) query = initialQuery }
    val allBooks by bookDao.observeAll().collectAsState(emptyList())
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val tags by catalogDao.observeTags().collectAsState(emptyList())
    val collections by catalogDao.observeCollections().collectAsState(emptyList())
    val threshold by settings.fuzzyThreshold.collectAsState(70)
    val tagBookIds by (if (tag.isBlank()) flowOf(emptyList()) else catalogDao.observeBookIdsForTag(tag)).collectAsState(emptyList())
    val collectionBookIds by (if (collection.isBlank()) flowOf(emptyList()) else catalogDao.observeBookIdsForCollection(collection)).collectAsState(emptyList())
    val books = SearchRanking.rank(allBooks, query, threshold).asSequence()
        .filter { libraryFilter == 0L || it.libraryId == libraryFilter }
        .filter { category.isBlank() || it.category.orEmpty().equals(category, ignoreCase = true) }
        .filter { author.isBlank() || it.author.equals(author, ignoreCase = true) }
        .filter { readingStatus.isBlank() || it.readingStatus.equals(readingStatus, ignoreCase = true) }
        .filter { tag.isBlank() || it.id in tagBookIds }
        .filter { collection.isBlank() || it.id in collectionBookIds }
        .filter { !favouritesOnly || it.favourite }
        .let { results -> when (sort) {
            "Oldest Added" -> results.sortedBy { it.createdAtMillis }
            "Title A-Z" -> results.sortedBy { it.title.lowercase() }
            "Title Z-A" -> results.sortedByDescending { it.title.lowercase() }
            "Author Name" -> results.sortedBy { it.author.lowercase() }
            "Purchase Date" -> results.sortedByDescending { it.purchaseDate.orEmpty() }
            "Rating" -> results.sortedByDescending { it.rating ?: 0 }
            "Price" -> results.sortedByDescending { it.pricePaise }
            else -> results.sortedByDescending { it.createdAtMillis }
        } }
        .toList()
    Column(modifier.fillMaxSize().padding(20.dp)) {
        ScreenHeader("Search", "Find the next book on your shelf") {
            TextButton(onClick = { filtersVisible = true }) { Text("Filters") }
        }
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(query, { query = it }, label = { Text("Search title, author, ISBN, or category") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) })
                Text("Smart matching at $threshold% similarity", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        if (books.isEmpty()) EmptyContentState("No books found", "Try another title or adjust your filters.") else BookList(books, bookDao, onEdit, canModify, canDelete, Modifier.weight(1f))
    }
    if (filtersVisible) AlertDialog(onDismissRequest = { filtersVisible = false }, title = { Text("Sort and filter") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ManagedCatalogDropdown("Sort", sort, listOf("Recently Added", "Oldest Added", "Title A-Z", "Title Z-A", "Author Name", "Purchase Date", "Rating", "Price")) { sort = it }
        ManagedCatalogDropdown("Library", libraries.firstOrNull { it.id == libraryFilter }?.name.orEmpty(), listOf("All libraries") + libraries.map { it.name }) { selected -> libraryFilter = libraries.firstOrNull { it.name == selected }?.id ?: 0L }
        ManagedCatalogDropdown("Category", category, allBooks.mapNotNull { it.category }.distinct().sorted()) { category = it }
        ManagedCatalogDropdown("Author", author, allBooks.map { it.author }.distinct().sorted()) { author = it }
        ManagedCatalogDropdown("Reading status", readingStatus, listOf("Unread", "Reading", "Completed")) { readingStatus = it }
        ManagedCatalogDropdown("Tag", tag, tags.map { it.name }.sorted()) { tag = it }
        ManagedCatalogDropdown("Collection", collection, collections.map { it.name }.sorted()) { collection = it }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(favouritesOnly, { favouritesOnly = it }); Text("Favourite books only") }
    } }, confirmButton = { TextButton(onClick = { filtersVisible = false }) { Text("Apply") } }, dismissButton = { TextButton(onClick = { libraryFilter = activeLibraryId ?: 0L; category = ""; author = ""; readingStatus = ""; tag = ""; collection = ""; favouritesOnly = false; sort = "Recently Added" }) { Text("Clear") } })
}

@Composable
private fun MoreScreenImproved(database: LibraryDatabase, bookDao: BookDao, libraryDao: LibraryDao, loanDao: LoanDao, wishlistDao: WishlistDao, userDao: UserDao, activeLibraryId: Long?, signedInUser: UserEntity?, modifier: Modifier, canModify: Boolean, onSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accessRepository = remember(database) { LibraryAccessRepository(database) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var csvPayload by remember { mutableStateOf<ByteArray?>(null) }
    var xlsxPayload by remember { mutableStateOf<ByteArray?>(null) }
    var pdfPayload by remember { mutableStateOf<ByteArray?>(null) }
    var reportsVisible by rememberSaveable { mutableStateOf(false) }
    var aboutVisible by rememberSaveable { mutableStateOf(false) }
    var backupVisible by rememberSaveable { mutableStateOf(false) }
    var usersVisible by rememberSaveable { mutableStateOf(false) }
    val books by bookDao.observeAll().collectAsState(emptyList())
    val libraries by libraryDao.observeAll().collectAsState(emptyList())
    val users by userDao.observeAll().collectAsState(emptyList())
    val loans by loanDao.observeAll().collectAsState(emptyList())
    val wishlist by wishlistDao.observeAll().collectAsState(emptyList())
    val canViewReports = signedInUser?.userRole?.can(com.venkateshgowda.personallibrary.data.LibraryPermission.ViewReports) == true
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) scope.launch { message = try { "Imported ${LegacyImportService(context, database).importArchive(uri)} books." } catch (error: Exception) { error.message ?: "Could not import archive." } } }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if (uri != null && csvPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(csvPayload) }; message = "CSV export saved." } }
    val xlsxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri -> if (uri != null && xlsxPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(xlsxPayload) }; message = "XLSX export saved." } }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri -> if (uri != null && pdfPayload != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(pdfPayload) }; message = "PDF report saved." } }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        ScreenHeader("More", "Your library command centre") {}
        Spacer(Modifier.height(16.dp))
        Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (canViewReports) {
            ManagementCard("Reports", "View reading trends and book statistics", Icons.Outlined.Assessment, { ReportPreviewChips() }) { reportsVisible = true }
            Spacer(Modifier.height(10.dp))
        }
        if (canModify) ManagementCard("Settings", "Configure preferences, privacy, and backups", Icons.Outlined.Settings) { onSettings() }
        if (signedInUser?.userRole?.can(com.venkateshgowda.personallibrary.data.LibraryPermission.ManageUsers) == true) {
            Spacer(Modifier.height(10.dp))
            ManagementCard("Manage users", "Add, edit, and manage library members", Icons.Outlined.People) { usersVisible = true }
        }
        Spacer(Modifier.height(22.dp))
        Text("Library health", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        LibraryHealthCard(books, "Not recorded yet")
        Spacer(Modifier.height(22.dp))
        ManagementCard("About", "Version 1.0.0  |  Built by Venkatesh Gowda", Icons.Outlined.MoreHoriz) { aboutVisible = true }
        Spacer(Modifier.height(18.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Export your records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    TextButton(onClick = { csvPayload = ReportExportService.booksCsv(books); csvLauncher.launch("library-books.csv") }) { Text("Books CSV") }
                    TextButton(onClick = { csvPayload = ReportExportService.wishlistCsv(wishlist); csvLauncher.launch("library-wishlist.csv") }) { Text("Wishlist CSV") }
                    TextButton(onClick = { xlsxPayload = ReportExportService.workbook(books, wishlist, loans, LocalDate.now().toString()); xlsxLauncher.launch("personal-library.xlsx") }) { Text("XLSX") }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BottomQuickAction("Export", Icons.Outlined.Download, Modifier.weight(1f)) { csvPayload = ReportExportService.booksCsv(books); csvLauncher.launch("library-books.csv") }
            if (canModify) BottomQuickAction("Backup", Icons.Outlined.Backup, Modifier.weight(1f)) { backupVisible = true }
            if (canModify) BottomQuickAction("Import", Icons.Outlined.UploadFile, Modifier.weight(1f)) { importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed")) }
        }
        if (message != null) Text(message!!, modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.error)
    }
    if (reportsVisible && canViewReports) ReportsDashboardDialog(
        books, libraries, users, loans, wishlist,
        onDismiss = { reportsVisible = false },
        onExportExcel = { filteredBooks, filteredLoans, filteredWishlist -> xlsxPayload = ReportExportService.workbook(filteredBooks, filteredWishlist, filteredLoans, LocalDate.now().toString()); xlsxLauncher.launch("library-report.xlsx") },
        onExportPdf = { filteredBooks, filteredLoans -> pdfPayload = createReportPdf(filteredBooks, libraries, filteredLoans); pdfLauncher.launch("library-report.pdf") },
        onShare = { filteredBooks, filteredLoans ->
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, reportShareText(filteredBooks, libraries, filteredLoans)), "Share library report"))
        }
    )
    if (aboutVisible) AboutAppDialog(onDismiss = { aboutVisible = false })
    if (backupVisible) BackupDialog(database, onDismiss = { backupVisible = false })
    if (usersVisible && activeLibraryId != null && signedInUser != null) UserRolesDialog(activeLibraryId, signedInUser, accessRepository, onDismiss = { usersVisible = false })
}

@Composable
private fun MoreProfileCard(libraryName: String, bookCount: Int, libraryCount: Int) {
    Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Row(Modifier.background(Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFF0F766E)))).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.LocalLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(libraryName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("$bookCount Books  |  $libraryCount Libraries", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun MoreInsight(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier.heightIn(min = 106.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagementCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, preview: @Composable (() -> Unit)? = null, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
                preview?.invoke()
            }
        }
    }
}

@Composable
private fun ReportPreviewChips() {
    Row(Modifier.padding(top = 10.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("By category", "Monthly purchases", "Reading trends").forEach { label ->
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
            }
        }
    }
}

@Composable
private fun LibraryHealthCard(books: List<BookEntity>, lastBackup: String) {
    val duplicateBooks = books.groupingBy { "${it.title.trim().lowercase()}|${it.author.trim().lowercase()}" }.eachCount().count { it.value > 1 }
    val coverCount = books.count { !it.coverImagePath.isNullOrBlank() }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HealthRow("Recently added", "${books.sortedByDescending { it.createdAtMillis }.take(7).size} books in the latest seven")
            HealthRow("Last backup", lastBackup)
            HealthRow("Potential duplicates", "$duplicateBooks title and author matches")
            HealthRow("Cover storage", "$coverCount of ${books.size} books have cover images")
        }
    }
}

@Composable
private fun HealthRow(label: String, detail: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomQuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.heightIn(min = 76.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun AboutAppDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var privacyVisible by rememberSaveable { mutableStateOf(false) }
    var feedbackVisible by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Venkatesh Gowdas Personal Library", style = MaterialTheme.typography.titleMedium)
                Text("Version 1.0.0")
                Text("Developed by Venkatesh Gowda")
                Text("A private, offline-first library manager for books, wishlist items, and loans.")
                Text("Your catalogue is stored locally on this device. The app does not require an internet connection.")
                Text("Multi-library support keeps books, wishlist items, loans, reports, and exports separate for each library.")
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    TextButton(onClick = { feedbackVisible = true }) { Text("Rate app") }
                    TextButton(onClick = { privacyVisible = true }) { Text("Privacy policy") }
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@personallibrary.local?subject=Personal%20Library%20Support")))
                    }) { Text("Contact support") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
    if (privacyVisible) AlertDialog(onDismissRequest = { privacyVisible = false }, title = { Text("Privacy policy") }, text = { Text("This app stores its catalogue only on this device. It has no internet permission, analytics, cloud synchronization, or remote diagnostics. Backups are created only when you choose a destination and passphrase.") }, confirmButton = { TextButton(onClick = { privacyVisible = false }) { Text("Close") } })
    if (feedbackVisible) AlertDialog(onDismissRequest = { feedbackVisible = false }, title = { Text("Rate app") }, text = { Text("This is a privately installed app and is not published in an app store. You can share feedback through Contact support.") }, confirmButton = { TextButton(onClick = { feedbackVisible = false }) { Text("Close") } })
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
private fun ReportsDashboardDialog(allBooks: List<BookEntity>, libraries: List<LibraryEntity>, users: List<UserEntity>, loans: List<LoanEntity>, wishlist: List<WishlistEntity>, onDismiss: () -> Unit, onExportExcel: (List<BookEntity>, List<LoanEntity>, List<WishlistEntity>) -> Unit, onExportPdf: (List<BookEntity>, List<LoanEntity>) -> Unit, onShare: (List<BookEntity>, List<LoanEntity>) -> Unit) {
    val today = LocalDate.now()
    var libraryFilter by rememberSaveable { mutableStateOf(0L) }
    var categoryFilter by rememberSaveable { mutableStateOf("") }
    var authorFilter by rememberSaveable { mutableStateOf("") }
    var languageFilter by rememberSaveable { mutableStateOf("") }
    var publisherFilter by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf("") }
    var ratingFilter by rememberSaveable { mutableStateOf("") }
    var loanStatusFilter by rememberSaveable { mutableStateOf("") }
    var wishlistStatusFilter by rememberSaveable { mutableStateOf("") }
    var dateRange by rememberSaveable { mutableStateOf("All time") }
    var minimumPrice by rememberSaveable { mutableStateOf("") }
    var maximumPrice by rememberSaveable { mutableStateOf("") }
    var reportType by rememberSaveable { mutableStateOf("All Books") }
    var reportGenerated by rememberSaveable { mutableStateOf(false) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    var resultsView by rememberSaveable { mutableStateOf("List") }
    var resultQuery by rememberSaveable { mutableStateOf("") }
    var resultPage by rememberSaveable { mutableStateOf(0) }
    val startDateMillis = when (dateRange) {
        "Last 7 days" -> today.minusDays(7)
        "Last 30 days" -> today.minusDays(30)
        "This year" -> today.withDayOfYear(1)
        else -> null
    }?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    val books = allBooks.asSequence()
        .filter { libraryFilter == 0L || it.libraryId == libraryFilter }
        .filter { categoryFilter.isBlank() || it.category.orEmpty().equals(categoryFilter, ignoreCase = true) }
        .filter { authorFilter.isBlank() || it.author.equals(authorFilter, ignoreCase = true) }
        .filter { languageFilter.isBlank() || it.language.orEmpty().equals(languageFilter, ignoreCase = true) }
        .filter { publisherFilter.isBlank() || it.publisher.orEmpty().equals(publisherFilter, ignoreCase = true) }
        .filter { statusFilter.isBlank() || it.readingStatus.equals(statusFilter, ignoreCase = true) }
        .filter { ratingFilter.isBlank() || it.rating?.toString() == ratingFilter }
        .filter { startDateMillis == null || it.createdAtMillis >= startDateMillis }
        .filter { minimumPrice.toLongOrNull()?.let { minimum -> it.pricePaise >= minimum * 100 } ?: true }
        .filter { maximumPrice.toLongOrNull()?.let { maximum -> it.pricePaise <= maximum * 100 } ?: true }
        .toList()
    val filteredBookIds = books.map { it.id }.toSet()
    val filteredLoans = loans.filter { loan ->
        loan.bookId in filteredBookIds && when (loanStatusFilter) {
            "Active" -> loan.actualReturnDate == null
            "Returned" -> loan.actualReturnDate != null
            "Overdue" -> loan.actualReturnDate == null && !loan.expectedReturnDate.isNullOrBlank() && loan.expectedReturnDate < today.toString()
            else -> true
        }
    }
    val filteredWishlist = wishlist.filter { item ->
        (libraryFilter == 0L || item.libraryId == libraryFilter) &&
            (wishlistStatusFilter.isBlank() || item.status.equals(wishlistStatusFilter, ignoreCase = true))
    }
    val activeLoans = filteredLoans.filter { it.actualReturnDate == null }
    val overdue = activeLoans.filter { !it.expectedReturnDate.isNullOrBlank() && it.expectedReturnDate < today.toString() }
    val reportLoans = when (reportType) {
        "Loaned Books" -> filteredLoans.filter { it.actualReturnDate == null }
        "Returned Books" -> filteredLoans.filter { it.actualReturnDate != null }
        "Overdue Books" -> overdue
        "Loan History" -> filteredLoans
        else -> filteredLoans
    }
    val reportWishlist = if (reportType == "Wishlist Books" || reportType == "Wishlist by Library") filteredWishlist else emptyList()
    val libraryBookCounts = libraries
        .filter { libraryFilter == 0L || it.id == libraryFilter }
        .map { library -> library to books.count { it.libraryId == library.id } }
        .filter { resultQuery.isBlank() || it.first.name.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Pair<LibraryEntity, Int>> { it.second }.thenBy { it.first.name.lowercase() })
    val wishlistByLibrary = libraries
        .asSequence()
        .filter { libraryFilter == 0L || it.id == libraryFilter }
        .map { library ->
            val libraryWishlist = filteredWishlist.filter { it.libraryId == library.id }
            Triple(library, libraryWishlist.size, libraryWishlist.sumOf { it.expectedPricePaise })
        }
        .filter { resultQuery.isBlank() || it.first.name.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<LibraryEntity, Int, Long>> { it.second }.thenBy { it.first.name.lowercase() })
        .toList()
    val loanFilteredBooks = if (loanStatusFilter.isBlank()) books else books.filter { book -> filteredLoans.any { it.bookId == book.id } }
    val authorBookCounts = loanFilteredBooks
        .groupBy { it.author.trim().ifBlank { "Unknown author" } }
        .map { (author, authorBooks) -> Triple(author, authorBooks.size, authorBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.second }.thenBy { it.first.lowercase() })
    val categoryBookCounts = loanFilteredBooks
        .groupBy { it.category?.trim().orEmpty().ifBlank { "Uncategorized" } }
        .map { (category, categoryBooks) -> Triple(category, categoryBooks.size, categoryBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.second }.thenBy { it.first.lowercase() })
    val languageBookCounts = loanFilteredBooks
        .groupBy { it.language?.trim().orEmpty().ifBlank { "Unspecified" } }
        .map { (language, languageBooks) -> Triple(language, languageBooks.size, languageBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.second }.thenBy { it.first.lowercase() })
    val publisherBookCounts = loanFilteredBooks
        .groupBy { it.publisher?.trim().orEmpty().ifBlank { "Unknown publisher" } }
        .map { (publisher, publisherBooks) -> Triple(publisher, publisherBooks.size, publisherBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.second }.thenBy { it.first.lowercase() })
    val ratingBookCounts = loanFilteredBooks
        .groupBy { it.rating?.let { rating -> "$rating stars" } ?: "Unrated" }
        .map { (rating, ratingBooks) -> Triple(rating, ratingBooks.size, ratingBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.first.removeSuffix(" stars").toIntOrNull() ?: 0 }
            .thenBy { it.first })
    val readingStatusBookCounts = loanFilteredBooks
        .groupBy { it.readingStatus.trim().ifBlank { "Unspecified" } }
        .map { (status, statusBooks) -> Triple(status, statusBooks.size, statusBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.second }.thenBy { it.first.lowercase() })
    val spendingByCategory = loanFilteredBooks
        .groupBy { it.category?.trim().orEmpty().ifBlank { "Uncategorized" } }
        .map { (category, categoryBooks) -> Triple(category, categoryBooks.size, categoryBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedWith(compareByDescending<Triple<String, Int, Long>> { it.third }.thenBy { it.first.lowercase() })
    val recentlyAddedStart = today.minusDays(30).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val recentlyCreatedLibraries = libraries
        .asSequence()
        .filter { libraryFilter == 0L || it.id == libraryFilter }
        .filter { it.createdAtMillis >= recentlyAddedStart }
        .filter { resultQuery.isBlank() || it.name.contains(resultQuery, ignoreCase = true) }
        .sortedByDescending { it.createdAtMillis }
        .toList()
    val purchasedBooks = loanFilteredBooks
        .filter { !it.purchaseDate.isNullOrBlank() }
        .sortedByDescending { it.purchaseDate }
    val monthlyPurchases = purchasedBooks
        .groupBy { it.purchaseDate!!.take(7) }
        .map { (period, periodBooks) -> Triple(period, periodBooks.size, periodBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedByDescending { it.first }
    val yearlyPurchases = purchasedBooks
        .groupBy { it.purchaseDate!!.take(4) }
        .map { (period, periodBooks) -> Triple(period, periodBooks.size, periodBooks.sumOf { it.pricePaise }) }
        .filter { resultQuery.isBlank() || it.first.contains(resultQuery, ignoreCase = true) }
        .sortedByDescending { it.first }
    val reportBooks = when (reportType) {
        "Loaned Books", "Returned Books", "Overdue Books", "Loan History" -> loanFilteredBooks.filter { book -> reportLoans.any { it.bookId == book.id } }
        "Recently Added Books" -> loanFilteredBooks.filter { it.createdAtMillis >= recentlyAddedStart }.sortedByDescending { it.createdAtMillis }
        "My Library Books" -> if (libraryFilter == 0L) emptyList() else loanFilteredBooks
        "Books by Reading Status" -> loanFilteredBooks.sortedBy { it.readingStatus }
        "Books by Rating" -> loanFilteredBooks.sortedByDescending { it.rating ?: 0 }
        "All Purchases", "Monthly Purchase Report", "Yearly Purchase Report" -> purchasedBooks
        else -> loanFilteredBooks
    }.filter { book -> resultQuery.isBlank() || listOf(book.title, book.author, book.category.orEmpty(), book.publisher.orEmpty()).any { it.contains(resultQuery, ignoreCase = true) } }
    val totalPages = (reportBooks.size + 9) / 10
    val pageBooks = reportBooks.drop(resultPage * 10).take(10)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Library reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Local collection insights", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.MoreHoriz, contentDescription = "Close reports") }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    item { ReportSection("Quick reports") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("All Books", "Loaned Books", "Wishlist Books", "My Library Books")) { quickReport ->
                                Button(onClick = { reportType = quickReport; reportGenerated = true; resultPage = 0 }, enabled = reportType != quickReport) { Text(quickReport) }
                            }
                        }
                    } }
                    item { ReportSection("Report generator") {
                        ManagedCatalogDropdown("Report category", reportType, listOf("All Books", "Books by Library", "Books by Category", "Books by Language", "Books by Author", "Books by Publisher", "Books by Rating", "Books by Reading Status", "Recently Added Books", "All Libraries", "Library-wise Book Count", "Recently Created Libraries", "All Users", "Loaned Books", "Returned Books", "Overdue Books", "Loan History", "Wishlist Books", "Wishlist by Library", "All Purchases", "Monthly Purchase Report", "Yearly Purchase Report", "Spending by Category")) { reportType = it; reportGenerated = false; resultPage = 0 }
                        TextButton(onClick = { filtersVisible = !filtersVisible }) { Text(if (filtersVisible) "Hide custom filters" else "Custom report builder") }
                    } }
                    if (filtersVisible) item { ReportSection("Custom filters") {
                        ManagedCatalogDropdown("Library", libraries.firstOrNull { it.id == libraryFilter }?.name.orEmpty(), listOf("All libraries") + libraries.map { it.name }) { selected -> libraryFilter = libraries.firstOrNull { it.name == selected }?.id ?: 0L }
                        ManagedCatalogDropdown("Category", categoryFilter, allBooks.mapNotNull { it.category }.distinct().sorted()) { categoryFilter = it }
                        ManagedCatalogDropdown("Author", authorFilter, allBooks.map { it.author }.distinct().sorted()) { authorFilter = it }
                        ManagedCatalogDropdown("Language", languageFilter, allBooks.mapNotNull { it.language }.distinct().sorted()) { languageFilter = it }
                        ManagedCatalogDropdown("Publisher", publisherFilter, allBooks.mapNotNull { it.publisher }.distinct().sorted()) { publisherFilter = it }
                        ManagedCatalogDropdown("Reading status", statusFilter, listOf("Unread", "Reading", "Completed")) { statusFilter = it }
                        ManagedCatalogDropdown("Rating", ratingFilter, (1..5).map { it.toString() }) { ratingFilter = it }
                        ManagedCatalogDropdown("Loan status", loanStatusFilter, listOf("Active", "Returned", "Overdue")) { loanStatusFilter = it }
                        ManagedCatalogDropdown("Wishlist status", wishlistStatusFilter, wishlist.map { it.status }.distinct().sorted()) { wishlistStatusFilter = it }
                        ManagedCatalogDropdown("Added", dateRange, listOf("All time", "Last 7 days", "Last 30 days", "This year")) { dateRange = it.ifBlank { "All time" } }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(minimumPrice, { minimumPrice = it.filter(Char::isDigit) }, label = { Text("Min INR") }, singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(maximumPrice, { maximumPrice = it.filter(Char::isDigit) }, label = { Text("Max INR") }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                        TextButton(onClick = { libraryFilter = 0L; categoryFilter = ""; authorFilter = ""; languageFilter = ""; publisherFilter = ""; statusFilter = ""; ratingFilter = ""; loanStatusFilter = ""; wishlistStatusFilter = ""; dateRange = "All time"; minimumPrice = ""; maximumPrice = "" }) { Text("Clear filters") }
                    } }
                    item { Button(onClick = { reportGenerated = true; resultPage = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Generate report") } }
                    if (reportGenerated) item { ReportSection(reportType) {
                        ReportMetricGrid(listOf("Records" to when (reportType) { "Books by Library", "Library-wise Book Count" -> libraryBookCounts.size.toString(); "Books by Author" -> authorBookCounts.size.toString(); "Books by Category" -> categoryBookCounts.size.toString(); "Books by Language" -> languageBookCounts.size.toString(); "Books by Publisher" -> publisherBookCounts.size.toString(); "Books by Rating" -> ratingBookCounts.size.toString(); "Books by Reading Status" -> readingStatusBookCounts.size.toString(); "Recently Created Libraries" -> recentlyCreatedLibraries.size.toString(); "Monthly Purchase Report" -> monthlyPurchases.size.toString(); "Yearly Purchase Report" -> yearlyPurchases.size.toString(); "Wishlist by Library" -> wishlistByLibrary.size.toString(); "Wishlist Books" -> reportWishlist.size.toString(); "Spending by Category" -> spendingByCategory.size.toString(); "All Users" -> users.size.toString(); "Loaned Books", "Returned Books", "Overdue Books", "Loan History" -> reportLoans.size.toString(); else -> reportBooks.size.toString() }, "Investment" to formatInr(reportBooks.sumOf { it.pricePaise }), "Active loans" to activeLoans.size.toString(), "Overdue" to overdue.size.toString()))
                        Text("Applied filters: ${listOfNotNull(libraries.firstOrNull { it.id == libraryFilter }?.name, categoryFilter.ifBlank { null }, authorFilter.ifBlank { null }, languageFilter.ifBlank { null }, publisherFilter.ifBlank { null }, statusFilter.ifBlank { null }, ratingFilter.takeIf { it.isNotBlank() }?.let { "$it stars" }, loanStatusFilter.ifBlank { null }, wishlistStatusFilter.ifBlank { null }, dateRange.takeUnless { it == "All time" }, minimumPrice.takeIf { it.isNotBlank() }?.let { "Min INR $it" }, maximumPrice.takeIf { it.isNotBlank() }?.let { "Max INR $it" }).ifEmpty { listOf("None") }.joinToString()}", style = MaterialTheme.typography.bodySmall)
                        if (reportType == "Books by Library" || reportType == "Library-wise Book Count") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search libraries") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            libraryBookCounts.forEach { (library, count) -> ReportInsight(library.name, "$count ${if (count == 1) "book" else "books"} | Created ${library.createdAtMillis}") }
                        }
                        else if (reportType == "Books by Author") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search authors") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (authorBookCounts.isEmpty()) Text("No authors match the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else authorBookCounts.forEach { (author, count, investment) -> ReportInsight(author, "$count ${if (count == 1) "book" else "books"} | ${formatInr(investment)}") }
                        }
                        else if (reportType == "Books by Category" || reportType == "Books by Language") {
                            val breakdowns = if (reportType == "Books by Category") categoryBookCounts else languageBookCounts
                            val label = if (reportType == "Books by Category") "Search categories" else "Search languages"
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (breakdowns.isEmpty()) Text("No matching groups for the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else breakdowns.forEach { (name, count, investment) -> ReportInsight(name, "$count ${if (count == 1) "book" else "books"} | ${formatInr(investment)}") }
                        }
                        else if (reportType == "Books by Publisher" || reportType == "Books by Rating") {
                            val breakdowns = if (reportType == "Books by Publisher") publisherBookCounts else ratingBookCounts
                            val label = if (reportType == "Books by Publisher") "Search publishers" else "Search ratings"
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (breakdowns.isEmpty()) Text("No matching groups for the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else breakdowns.forEach { (name, count, investment) -> ReportInsight(name, "$count ${if (count == 1) "book" else "books"} | ${formatInr(investment)}") }
                        }
                        else if (reportType == "Books by Reading Status") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search reading statuses") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (readingStatusBookCounts.isEmpty()) Text("No reading statuses match the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else readingStatusBookCounts.forEach { (status, count, investment) -> ReportInsight(status, "$count ${if (count == 1) "book" else "books"} | ${formatInr(investment)}") }
                        }
                        else if (reportType == "Spending by Category") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search spending categories") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (spendingByCategory.isEmpty()) Text("No spending matches the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else spendingByCategory.forEach { (category, count, investment) -> ReportInsight(category, "${formatInr(investment)} | $count ${if (count == 1) "book" else "books"}") }
                        }
                        else if (reportType == "Recently Created Libraries") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search recently created libraries") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (recentlyCreatedLibraries.isEmpty()) Text("No libraries were created in the last 30 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else recentlyCreatedLibraries.forEach { library ->
                                ReportInsight(library.name, "Created ${java.time.Instant.ofEpochMilli(library.createdAtMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM uuuu"))}")
                            }
                        }
                        else if (reportType == "Recently Added Books") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search recently added books") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (pageBooks.isEmpty()) Text("No books were added in the last 30 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else pageBooks.forEach { book ->
                                val addedDate = java.time.Instant.ofEpochMilli(book.createdAtMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM uuuu"))
                                ReportInsight(book.title, "${book.author} | Added $addedDate")
                            }
                            if (totalPages > 1) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = { resultPage -= 1 }, enabled = resultPage > 0) { Text("Previous") }; Text("Page ${resultPage + 1} of $totalPages"); TextButton(onClick = { resultPage += 1 }, enabled = resultPage < totalPages - 1) { Text("Next") } }
                        }
                        else if (reportType == "All Purchases") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search purchases") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (pageBooks.isEmpty()) Text("No purchases match the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else pageBooks.forEach { book ->
                                ReportInsight(book.title, "${book.author} | Purchased ${book.purchaseDate} | ${formatInr(book.pricePaise)}")
                            }
                            if (totalPages > 1) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = { resultPage -= 1 }, enabled = resultPage > 0) { Text("Previous") }; Text("Page ${resultPage + 1} of $totalPages"); TextButton(onClick = { resultPage += 1 }, enabled = resultPage < totalPages - 1) { Text("Next") } }
                        }
                        else if (reportType == "Monthly Purchase Report" || reportType == "Yearly Purchase Report") {
                            val purchases = if (reportType == "Monthly Purchase Report") monthlyPurchases else yearlyPurchases
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search purchase period") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (purchases.isEmpty()) Text("No purchases match the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else purchases.forEach { (period, count, investment) -> ReportInsight(period, "$count ${if (count == 1) "book" else "books"} | ${formatInr(investment)}") }
                        }
                        else if (reportType in listOf("Loaned Books", "Returned Books", "Overdue Books", "Loan History")) {
                            val visibleLoans = reportLoans.filter { loan ->
                                val book = allBooks.firstOrNull { it.id == loan.bookId }
                                resultQuery.isBlank() || listOfNotNull(book?.title, book?.author, loan.borrowerName, loan.borrowedDate, loan.expectedReturnDate)
                                    .any { it.contains(resultQuery, ignoreCase = true) }
                            }
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search books, borrowers, or dates") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (visibleLoans.isEmpty()) Text("No loans match the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else visibleLoans.forEach { loan ->
                                val bookTitle = allBooks.firstOrNull { it.id == loan.bookId }?.title ?: "Deleted book"
                                val loanState = when {
                                    loan.actualReturnDate != null -> "Returned ${loan.actualReturnDate}"
                                    !loan.expectedReturnDate.isNullOrBlank() && loan.expectedReturnDate < today.toString() -> "Overdue since ${loan.expectedReturnDate}"
                                    !loan.expectedReturnDate.isNullOrBlank() -> "Due ${loan.expectedReturnDate}"
                                    else -> "No due date"
                                }
                                ReportInsight(bookTitle, "Borrower: ${loan.borrowerName} | Borrowed ${loan.borrowedDate} | $loanState")
                            }
                        }
                        else if (reportType == "Wishlist by Library") {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search libraries") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            if (wishlistByLibrary.isEmpty()) Text("No libraries match the selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else wishlistByLibrary.forEach { (library, count, expectedCost) -> ReportInsight(library.name, "$count ${if (count == 1) "item" else "items"} | Expected ${formatInr(expectedCost)}") }
                        }
                        else if (reportType == "Wishlist Books") reportWishlist.forEach { item -> ReportInsight(item.title, "${item.author ?: "Unknown author"} | ${item.status}") }
                        else if (reportType in listOf("All Users")) users.forEach { user -> ReportInsight(user.displayName ?: user.username, user.role) }
                        else {
                            OutlinedTextField(resultQuery, { resultQuery = it; resultPage = 0 }, label = { Text("Search within report") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { resultsView = "List" }) { Text("List") }; TextButton(onClick = { resultsView = "Table" }) { Text("Table") } }
                            pageBooks.forEach { book -> ReportInsight(book.title, if (resultsView == "Table") "${book.author} | ${book.category ?: "-"} | ${formatInr(book.pricePaise)} | ${book.readingStatus}" else "${book.author} | ${book.readingStatus}") }
                            if (totalPages > 1) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = { resultPage -= 1 }, enabled = resultPage > 0) { Text("Previous") }; Text("Page ${resultPage + 1} of $totalPages"); TextButton(onClick = { resultPage += 1 }, enabled = resultPage < totalPages - 1) { Text("Next") } }
                        }
                    } }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { onShare(reportBooks, reportLoans) }) { Text("Share") }; TextButton(onClick = { onExportPdf(reportBooks, reportLoans) }) { Text("PDF") }; Button(onClick = { onExportExcel(reportBooks, reportLoans, reportWishlist) }) { Text("Excel") } }
            }
        }
    }
}

@Composable
private fun ReportMetricGrid(metrics: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { metrics.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { (label, value) -> MetricCard(label, value, Modifier.weight(1f)) }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } }
}

@Composable
private fun ReportSection(title: String, content: @Composable () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); content() } }

@Composable
private fun ReportInsight(label: String, value: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(8.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) { Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.46f).padding(end = 12.dp)); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(0.54f)) } } }

@Composable
private fun ReportBookList(title: String, books: List<BookEntity>, showPrice: Boolean = false) { if (books.isNotEmpty()) { Text(title, style = MaterialTheme.typography.titleMedium); books.forEach { book -> ReportInsight(book.title, if (showPrice) formatInr(book.pricePaise) else "${book.author}${book.rating?.let { " | $it / 5" }.orEmpty()}") } } }

private fun reportCount(books: List<BookEntity>, label: (BookEntity) -> String) = books.groupingBy(label).eachCount().entries.sortedByDescending { it.value }.map { ReportBreakdown(it.key, it.value.toLong()) }
private fun reportAmount(books: List<BookEntity>, label: (BookEntity) -> String) = books.groupBy(label).map { (name, matching) -> ReportBreakdown(name, matching.sumOf { it.pricePaise }) }.sortedByDescending { it.value }
private fun reportLoanCount(loans: List<LoanEntity>, label: (LoanEntity) -> String) = loans.groupingBy(label).eachCount().entries.sortedByDescending { it.value }.map { ReportBreakdown(it.key, it.value.toLong()) }

private fun reportShareText(books: List<BookEntity>, libraries: List<LibraryEntity>, loans: List<LoanEntity>) = "Library report\n${books.size} books across ${libraries.size} libraries\n${books.count { it.favourite }} favourites\n${loans.count { it.actualReturnDate == null }} active loans\nInvestment: ${formatInr(books.sumOf { it.pricePaise })}"

private fun createReportPdf(books: List<BookEntity>, libraries: List<LibraryEntity>, loans: List<LoanEntity>): ByteArray = ByteArrayOutputStream().use { output ->
    val document = PdfDocument()
    val reportLines = buildList {
        add("Library Report")
        add("Generated: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM uuuu"))}")
        add("")
        add("Summary")
        add("Total books: ${books.size}")
        add("Libraries represented: ${books.map { it.libraryId }.distinct().size} of ${libraries.size}")
        add("Favourite books: ${books.count { it.favourite }}")
        add("Active loans: ${loans.count { it.actualReturnDate == null }}")
        add("Overdue loans: ${loans.count { it.actualReturnDate == null && !it.expectedReturnDate.isNullOrBlank() && it.expectedReturnDate < LocalDate.now().toString() }}")
        add("Total investment: ${formatInr(books.sumOf { it.pricePaise })}")
        add("Average price: ${formatInr(if (books.isEmpty()) 0 else books.sumOf { it.pricePaise } / books.size)}")
        add("")
        add("Books")
        if (books.isEmpty()) add("No books match the selected report filters.")
        else {
            add("Title | Author | Category | Price | Status | Rating")
            books.sortedBy { it.title.lowercase() }.forEach { book ->
                add("${book.title} | ${book.author} | ${book.category ?: "Uncategorized"} | ${formatInr(book.pricePaise)} | ${book.readingStatus} | ${book.rating?.toString() ?: "-"}")
            }
        }
        add("")
        add("Loans")
        if (loans.isEmpty()) add("No loans match the selected report filters.")
        else {
            add("Book | Borrower | Borrowed | Due | Status")
            loans.forEach { loan ->
                val bookTitle = books.firstOrNull { it.id == loan.bookId }?.title ?: "Deleted book"
                val status = when {
                    loan.actualReturnDate != null -> "Returned"
                    !loan.expectedReturnDate.isNullOrBlank() && loan.expectedReturnDate < LocalDate.now().toString() -> "Overdue"
                    else -> "Active"
                }
                add("$bookTitle | ${loan.borrowerName} | ${loan.borrowedDate} | ${loan.expectedReturnDate ?: "-"} | $status")
            }
        }
    }
    val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
    val bodyPaint = Paint().apply { textSize = 9f }
    val headingPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
    var y = 52f
    reportLines.forEach { line ->
        if (y > 790f) {
            page.canvas.drawText("Page $pageNumber", 500f, 816f, bodyPaint)
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            y = 52f
        }
        val paint = when (line) { "Library Report" -> titlePaint; "Summary", "Books", "Loans" -> headingPaint; else -> bodyPaint }
        page.canvas.drawText(line.take(112), 42f, y, paint)
        y += if (paint == titlePaint) 30f else 16f
    }
    page.canvas.drawText("Page $pageNumber", 500f, 816f, bodyPaint)
    document.finishPage(page)
    document.writeTo(output)
    document.close()
    output.toByteArray()
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(entry.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(end = 12.dp))
            Text(valueText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
        }
        LinearProgressIndicator(
            progress = (entry.value.toFloat() / maximum).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsDialogImproved(database: LibraryDatabase, settings: AppSettings, signedInUser: UserEntity, onDismiss: () -> Unit, onSignOut: () -> Unit, onSwitchUser: () -> Unit) {
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
private fun BookList(books: List<BookEntity>, bookDao: BookDao, onEdit: (BookEntity) -> Unit, canModify: Boolean, canDelete: Boolean, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var bookPendingDeletion by remember { mutableStateOf<BookEntity?>(null) }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) { items(books, key = { it.id }) { book -> Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { if (book.coverImagePath != null) AsyncImage(model = book.coverImagePath, contentDescription = "Cover for ${book.title}", contentScale = ContentScale.Crop, modifier = Modifier.padding(end = 12.dp).height(72.dp).weight(0.22f)); Column(Modifier.weight(1f)) { Text(book.title, style = MaterialTheme.typography.titleMedium); Text(book.author); Text(formatInr(book.pricePaise), style = MaterialTheme.typography.labelMedium) }; if (canModify) IconButton(onClick = { onEdit(book) }) { Icon(Icons.Outlined.Edit, "Edit ${book.title}") }; if (canDelete) IconButton(onClick = { bookPendingDeletion = book }) { Icon(Icons.Outlined.Delete, "Delete ${book.title}") } } } } }
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
private fun BookEditor(book: BookEntity?, bookDao: BookDao, catalogDao: CatalogDao, libraryId: Long, canDelete: Boolean, onDismiss: () -> Unit, onSave: (BookEntity, List<Long>, List<Long>, List<String>) -> Unit) {
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
    val categories by catalogDao.observeCategories(libraryId).collectAsState(emptyList())
    val languages by catalogDao.observeLanguages(libraryId).collectAsState(emptyList())
    if (book == null) {
        ManagedAddBookDialog(categories.map { it.name }, languages.map { it.name }, bookDao, onDismiss) { newBook ->
            onSave(newBook, emptyList(), emptyList(), emptyList())
        }
        return
    }
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
    if (canDelete) existingImagePendingDeletion?.let { image -> AlertDialog(onDismissRequest = { existingImagePendingDeletion = null }, title = { Text("Delete image?") }, text = { Text("This image will be permanently removed from the book.") }, confirmButton = { TextButton(onClick = { if (book != null) scope.launch { bookDao.deleteImageAndUpdateBook(book, image); CoverStorage.delete(image.path); existingImagePendingDeletion = null } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { existingImagePendingDeletion = null }) { Text("Cancel") } }) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagedCatalogDropdown(label: String, value: String, options: List<String>, onValueChanged: (String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select $label") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onValueChanged(""); expanded = false })
            options.distinct().forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onValueChanged(option); expanded = false })
            }
        }
    }
}

@Composable
private fun ManagedAddBookDialog(categories: List<String>, languages: List<String>, bookDao: BookDao, onDismiss: () -> Unit, onSave: (BookEntity) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("0") }
    var category by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("") }
    var isbn by rememberSaveable { mutableStateOf("") }
    var barcode by rememberSaveable { mutableStateOf("") }
    var publisher by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var scanVisible by rememberSaveable { mutableStateOf(false) }
    var scanMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add book") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scanVisible = true }, modifier = Modifier.fillMaxWidth()) { Text("Scan book barcode") }
                scanMessage?.let { Text(it, color = if (it.startsWith("ISBN already")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
                OutlinedTextField(title, { title = it; error = null }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(author, { author = it; error = null }, label = { Text("Author *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it; error = null }, label = { Text("Price (INR) *") }, modifier = Modifier.fillMaxWidth())
                ManagedCatalogDropdown("Category", category, categories) { category = it }
                ManagedCatalogDropdown("Language", language, languages) { language = it }
                OutlinedTextField(isbn, { isbn = it }, label = { Text("ISBN") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(barcode, { barcode = it }, label = { Text("Book barcode") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(publisher, { publisher = it }, label = { Text("Publisher") }, modifier = Modifier.fillMaxWidth())
                if (categories.isEmpty() || languages.isEmpty()) Text("Add categories and languages from Organize library to populate these lists.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val paise = PriceValidator.paise(price)
                val normalizedIsbn = normalizeIsbn(isbn)
                error = when {
                    title.isBlank() -> "Title is required."
                    author.isBlank() -> "Author is required."
                    paise == null -> "Enter a valid non-negative price."
                    isbn.isNotBlank() && normalizedIsbn == null -> "Enter a valid ISBN-10 or ISBN-13."
                    else -> null
                }
                if (error == null) scope.launch {
                    if (normalizedIsbn != null && bookDao.findOtherBookWithIsbn(normalizedIsbn, 0L) != null) error = "This ISBN already exists in your library."
                    else onSave(BookEntity(title = title.trim(), author = author.trim(), pricePaise = paise!!, category = category.ifBlank { null }, language = language.ifBlank { null }, isbn = normalizedIsbn, barcode = normalizeBarcode(barcode), publisher = publisher.trim().ifBlank { null }))
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (scanVisible) BarcodeScannerDialog(onDismiss = { scanVisible = false }) { scannedCode ->
        scanVisible = false
        val scannedIsbn = normalizeIsbn(scannedCode)
        if (scannedIsbn == null) barcode = scannedCode else isbn = scannedIsbn
        scope.launch {
            scanMessage = if (scannedIsbn != null && bookDao.findOtherBookWithIsbn(scannedIsbn, 0L) != null) {
                "ISBN already exists in this library. Review the existing book before saving."
            } else if (scannedIsbn == null) {
                "Book barcode scanned successfully. Enter the remaining details to add the book."
            } else {
                "ISBN scanned successfully. Enter the remaining details to add the book."
            }
        }
    }
}

@Composable
private fun BarcodeScannerDialog(onDismiss: () -> Unit, onScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProvider = remember { ProcessCameraProvider.getInstance(context) }
    val handled = remember { AtomicBoolean(false) }
    var permissionGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var scanError by rememberSaveable { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> permissionGranted = granted }
    LaunchedEffect(Unit) { if (!permissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA) }
    DisposableEffect(permissionGranted, lifecycleOwner) {
        if (!permissionGranted) return@DisposableEffect onDispose { }
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E, Barcode.FORMAT_CODE_128, Barcode.FORMAT_ITF).build()
        )
        cameraProvider.addListener({
            runCatching {
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { useCase ->
                    useCase.setAnalyzer(analysisExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage == null) imageProxy.close()
                        else scanner.process(InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees))
                            .addOnSuccessListener { codes ->
                                val barcode = codes.firstNotNullOfOrNull { code -> normalizeBarcode(code.rawValue) }
                                if (barcode != null && handled.compareAndSet(false, true)) ContextCompat.getMainExecutor(context).execute { onScanned(barcode) }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                }
                val provider = cameraProvider.get()
                provider.unbindAll()
                camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            }.onFailure { scanError = "Unable to start the camera. Please try again." }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            scanner.close()
            analysisExecutor.shutdown()
            runCatching { cameraProvider.get().unbindAll() }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan book barcode") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (permissionGranted) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(300.dp))
                    Text("Point the camera at the book barcode. ISBNs and library inventory codes are supported.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { torchOn = !torchOn; camera?.cameraControl?.enableTorch(torchOn) }) { Text(if (torchOn) "Turn flashlight off" else "Turn flashlight on") }
                } else {
                    Text("Camera permission is required to scan a book barcode.")
                    TextButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Allow camera") }
                }
                scanError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Enter manually") } }
    )
}

private fun normalizeBarcode(rawValue: String?): String? {
    return rawValue.orEmpty().uppercase().filter(Char::isLetterOrDigit).takeIf { it.isNotBlank() }
}

private fun normalizeIsbn(rawValue: String?): String? {
    val isbn = rawValue.orEmpty().uppercase().filter { it.isDigit() || it == 'X' }
    return when {
        isbn.length == 13 && isbn.all(Char::isDigit) && (isbn.startsWith("978") || isbn.startsWith("979")) && isbn.map { it.digitToInt() }.mapIndexed { index, digit -> if (index % 2 == 0) digit else digit * 3 }.sum() % 10 == 0 -> isbn
        isbn.length == 10 && isbn.dropLast(1).all(Char::isDigit) && (isbn.last().isDigit() || isbn.last() == 'X') && isbn.mapIndexed { index, character -> (if (character == 'X') 10 else character.digitToInt()) * (10 - index) }.sum() % 11 == 0 -> isbn
        else -> null
    }
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
private fun CatalogDialogImproved(catalogDao: CatalogDao, libraryId: Long, onDismiss: () -> Unit) {
    val tags by catalogDao.observeTags().collectAsState(emptyList())
    val collections by catalogDao.observeCollections().collectAsState(emptyList())
    val categories by catalogDao.observeCategories(libraryId).collectAsState(emptyList())
    val languages by catalogDao.observeLanguages(libraryId).collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var tagName by rememberSaveable { mutableStateOf("") }
    var collectionName by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf("") }
    var languageName by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var editingItem by remember { mutableStateOf<CatalogItem?>(null) }
    var deletingItem by remember { mutableStateOf<CatalogItem?>(null) }
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
                tags.forEach { tag -> CatalogItemRow(tag.name, { editingItem = CatalogItem(tag.id, tag.name, CatalogItemKind.Tag) }, { deletingItem = CatalogItem(tag.id, tag.name, CatalogItemKind.Tag) }) }
                Spacer(Modifier.height(16.dp))
                Text("Collections", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(collectionName, { collectionName = it }, label = { Text("New collection") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = {
                    if (collectionName.isNotBlank()) scope.launch { runCatching { catalogDao.insertCollection(CollectionEntity(name = collectionName.trim())) }.onSuccess { collectionName = "" }.onFailure { message = "A collection with that name already exists." } }
                }) { Text("Add collection") }
                collections.forEach { collection -> CatalogItemRow(collection.name, { editingItem = CatalogItem(collection.id, collection.name, CatalogItemKind.Collection) }, { deletingItem = CatalogItem(collection.id, collection.name, CatalogItemKind.Collection) }) }
                if (message != null) Text(message!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Text("Categories", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(categoryName, { categoryName = it }, label = { Text("New category") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = {
                    if (categoryName.isNotBlank()) scope.launch { runCatching { catalogDao.insertCategory(LibraryCategoryEntity(libraryId = libraryId, name = categoryName.trim())) }.onSuccess { categoryName = "" }.onFailure { message = "That category already exists in this library." } }
                }) { Text("Add category") }
                categories.forEach { category -> CatalogItemRow(category.name, { editingItem = CatalogItem(category.id, category.name, CatalogItemKind.Category) }, { deletingItem = CatalogItem(category.id, category.name, CatalogItemKind.Category) }) }
                Spacer(Modifier.height(16.dp))
                Text("Languages", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(languageName, { languageName = it }, label = { Text("New language") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = {
                    if (languageName.isNotBlank()) scope.launch { runCatching { catalogDao.insertLanguage(LibraryLanguageEntity(libraryId = libraryId, name = languageName.trim())) }.onSuccess { languageName = "" }.onFailure { message = "That language already exists in this library." } }
                }) { Text("Add language") }
                languages.forEach { language -> CatalogItemRow(language.name, { editingItem = CatalogItem(language.id, language.name, CatalogItemKind.Language) }, { deletingItem = CatalogItem(language.id, language.name, CatalogItemKind.Language) }) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
    editingItem?.let { item ->
        CatalogItemNameDialog("Edit ${item.kind.name.lowercase()}", item.name, onDismiss = { editingItem = null }) { name ->
            scope.launch {
                runCatching {
                    when (item.kind) {
                        CatalogItemKind.Tag -> catalogDao.updateTag(TagEntity(item.id, name))
                        CatalogItemKind.Collection -> catalogDao.updateCollection(CollectionEntity(item.id, name))
                        CatalogItemKind.Category -> catalogDao.updateCategory(LibraryCategoryEntity(item.id, libraryId, name))
                        CatalogItemKind.Language -> catalogDao.updateLanguage(LibraryLanguageEntity(item.id, libraryId, name))
                    }
                }.onSuccess { editingItem = null }.onFailure { message = "That ${item.kind.name.lowercase()} already exists in this library." }
            }
        }
    }
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Delete ${item.kind.name.lowercase()}?") },
            text = { Text("${item.name} will be removed from the organized library list.") },
            confirmButton = { TextButton(onClick = { scope.launch { when (item.kind) { CatalogItemKind.Tag -> catalogDao.deleteTagAndAssignments(item.id); CatalogItemKind.Collection -> catalogDao.deleteCollectionAndAssignments(item.id); CatalogItemKind.Category -> catalogDao.deleteCategory(item.id); CatalogItemKind.Language -> catalogDao.deleteLanguage(item.id) }; deletingItem = null } }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deletingItem = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CatalogItemRow(name: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onEdit) { Text("Edit") }
        TextButton(onClick = onDelete) { Text("Delete") }
    }
}

@Composable
private fun CatalogItemNameDialog(title: String, initialName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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

private fun iconFor(destination: Destination) = when (destination) { Destination.Dashboard -> Icons.Outlined.Home; Destination.Books -> Icons.Outlined.LocalLibrary; Destination.Library -> Icons.Outlined.LocalLibrary; Destination.Search -> Icons.Outlined.Search; Destination.Loans -> Icons.Outlined.SwapHoriz; Destination.More -> Icons.Outlined.MoreHoriz }
private fun formatInr(paise: Long) = "INR %.2f".format(paise / 100.0)
private fun normalizeForDuplicate(value: String) = value.lowercase().filter { it.isLetterOrDigit() }