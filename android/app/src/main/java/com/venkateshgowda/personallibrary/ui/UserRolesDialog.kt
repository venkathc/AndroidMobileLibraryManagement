package com.venkateshgowda.personallibrary.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.venkateshgowda.personallibrary.data.LibraryAccessRepository
import com.venkateshgowda.personallibrary.data.LibraryMember
import com.venkateshgowda.personallibrary.data.LibraryPermission
import com.venkateshgowda.personallibrary.data.LibraryMembershipEntity
import com.venkateshgowda.personallibrary.data.MembershipRequestEntity
import com.venkateshgowda.personallibrary.data.UserEntity
import com.venkateshgowda.personallibrary.data.UserRole
import com.venkateshgowda.personallibrary.data.can
import com.venkateshgowda.personallibrary.data.userRole
import kotlinx.coroutines.launch

@Composable
fun UserRolesDialog(libraryId: Long, currentUser: UserEntity, repository: LibraryAccessRepository, onDismiss: () -> Unit) {
    val members by repository.observeMembers(libraryId).collectAsState(emptyList())
    val users by repository.observeUsers().collectAsState(emptyList())
    val requests by repository.observeRequests(libraryId).collectAsState(emptyList())
    val audit by repository.observeAudit(libraryId).collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf("Members") }
    var query by rememberSaveable { mutableStateOf("") }
    var roleFilter by rememberSaveable { mutableStateOf("All") }
    var statusFilter by rememberSaveable { mutableStateOf("All") }
    var inviteVisible by rememberSaveable { mutableStateOf(false) }
    var addMemberVisible by rememberSaveable { mutableStateOf(false) }
    var createUserVisible by rememberSaveable { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<LibraryMember?>(null) }
    var memberPendingRemoval by remember { mutableStateOf<LibraryMember?>(null) }
    var userPendingDeletion by remember { mutableStateOf<UserEntity?>(null) }
    var passwordResetUser by remember { mutableStateOf<UserEntity?>(null) }
    var displayNameUser by remember { mutableStateOf<UserEntity?>(null) }
    var statusUser by remember { mutableStateOf<UserEntity?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val canManage = currentUser.userRole.can(LibraryPermission.ManageUsers)
    val filteredMembers = members.filter { member ->
        (roleFilter == "All" || member.membership.role == roleFilter) &&
            (member.user.username.contains(query, true) || member.user.displayName.orEmpty().contains(query, true) || member.user.email.orEmpty().contains(query, true))
    }.sortedBy { it.user.displayName ?: it.user.username }
    val availableUsers = users.filter { user -> members.none { it.user.id == user.id } }
    val filteredUsers = users.filter { user ->
        (statusFilter == "All" || user.accountStatus == statusFilter) &&
            (user.username.contains(query, true) || user.displayName.orEmpty().contains(query, true) || user.email.orEmpty().contains(query, true))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)) {
            Column(Modifier.fillMaxHeight().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Users & permissions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${members.size} members in this library", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (canManage) {
                        Column(horizontalAlignment = Alignment.End) {
                            Button(onClick = { createUserVisible = true }) { Text("Create user") }
                            TextButton(onClick = { addMemberVisible = true }) { Text("Add existing") }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserMetricCard("Users", users.size, Color(0xFFCDE8FF))
                    UserMetricCard("Active", users.count { it.accountStatus == "Active" }, Color(0xFFCDEFE2))
                    UserMetricCard("Admins", members.count { it.membership.role == UserRole.Admin.name || it.membership.role == UserRole.Owner.name }, Color(0xFFFFE2A8))
                    UserMetricCard("Pending", requests.count { it.status == "Pending" }, Color(0xFFE8E0FF))
                    UserMetricCard("Suspended", users.count { it.accountStatus == "Suspended" }, Color(0xFFFFDAD6))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Members", "Users", "Permissions", "Requests", "Activity").forEach { item ->
                        TextButton(onClick = { tab = item }) { Text(item, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (tab == item) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                when (tab) {
                    "Members" -> {
                        OutlinedTextField(query, { query = it }, label = { Text("Search members") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (listOf("All") + UserRole.values().map { it.name }).forEach { filter -> TextButton(onClick = { roleFilter = filter }) { Text(if (filter == roleFilter) "${if (filter == "All") "All" else UserRole.fromStored(filter, false).label} selected" else if (filter == "All") "All" else UserRole.fromStored(filter, false).label) } }
                        }
                        if (filteredMembers.isEmpty()) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (members.isEmpty()) "No members have been added yet." else "No members match this filter.", style = MaterialTheme.typography.titleSmall)
                                if (members.isEmpty() && canManage) Text("Create a user or add an existing local account to give access to this library.", style = MaterialTheme.typography.bodySmall)
                                if (roleFilter != "All" || query.isNotBlank()) TextButton(onClick = { roleFilter = "All"; query = "" }) { Text("Clear filters") }
                            }
                        } else {
                            LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredMembers, key = { it.membership.id }) { member -> MemberCard(member, canManage, currentUser.id, { selectedMember = member }, { memberPendingRemoval = member }, { userPendingDeletion = member.user }, { passwordResetUser = member.user }, { displayNameUser = member.user }) }
                            }
                        }
                    }
                    "Users" -> {
                        Text("All local accounts", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(query, { query = it }, label = { Text("Search by name or email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("All", "Active", "Suspended").forEach { status -> TextButton(onClick = { statusFilter = status }) { Text(if (status == statusFilter) "$status selected" else status) } }
                        }
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredUsers, key = { it.id }) { user ->
                                LocalUserCard(
                                    user = user,
                                    canManage = canManage,
                                    isCurrentUser = user.id == currentUser.id,
                                    isOwner = members.any { it.user.id == user.id && it.membership.role == UserRole.Owner.name },
                                    onDelete = { userPendingDeletion = user },
                                    onResetPassword = { passwordResetUser = user },
                                    onEditDisplayName = { displayNameUser = user },
                                    onChangeStatus = { statusUser = user }
                                )
                            }
                        }
                    }
                    "Permissions" -> LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(UserRole.values().toList(), key = { it.name }) { role ->
                            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = roleColor(role))) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(role.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(role.permissions.joinToString { it.label() }, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    "Requests" -> LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(requests, key = { it.id }) { request -> RequestCard(request, canManage && request.status == "Pending", onApprove = { scope.launch { runCatching { repository.decideRequest(libraryId, currentUser, request, true) }.onFailure { message = it.message } } }, onReject = { scope.launch { runCatching { repository.decideRequest(libraryId, currentUser, request, false) }.onFailure { message = it.message } } }) }
                    }
                    else -> LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(audit, key = { it.id }) { entry -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(entry.action, fontWeight = FontWeight.Bold); Text(entry.detail, style = MaterialTheme.typography.bodySmall) } } }
                    }
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close") }
            }
        }
    }
    if (addMemberVisible) AddLibraryMemberDialog(availableUsers, onDismiss = { addMemberVisible = false }) { user ->
        scope.launch {
            runCatching { repository.addMember(libraryId, currentUser, user) }
                .onSuccess { addMemberVisible = false }
                .onFailure { message = it.message }
        }
    }
    if (createUserVisible) CreateLibraryUserDialog(libraryId, currentUser, repository, onDismiss = { createUserVisible = false })
    if (inviteVisible) InviteMemberDialog(libraryId, currentUser, repository, onDismiss = { inviteVisible = false })
    selectedMember?.let { member -> RoleEditor(member, onDismiss = { selectedMember = null }) { role -> scope.launch { runCatching { repository.updateRole(libraryId, currentUser, member.membership, role) }.onSuccess { selectedMember = null }.onFailure { message = it.message } } } }
    memberPendingRemoval?.let { member -> AlertDialog(onDismissRequest = { memberPendingRemoval = null }, title = { Text("Remove ${member.user.displayName ?: member.user.username}?") }, text = { Text("This member will lose access to this library.") }, confirmButton = { Button(onClick = { scope.launch { runCatching { repository.removeMember(libraryId, currentUser, member.membership) }.onSuccess { memberPendingRemoval = null }.onFailure { message = it.message } } }) { Text("Remove") } }, dismissButton = { TextButton(onClick = { memberPendingRemoval = null }) { Text("Cancel") } }) }
    userPendingDeletion?.let { user -> AlertDialog(onDismissRequest = { userPendingDeletion = null }, title = { Text("Delete ${user.displayName ?: user.username}?") }, text = { Text("This permanently deletes the local account and removes access to every library. This cannot be undone.") }, confirmButton = { Button(onClick = { scope.launch { runCatching { repository.deleteUser(libraryId, currentUser, user) }.onSuccess { userPendingDeletion = null }.onFailure { message = it.message } } }) { Text("Delete user") } }, dismissButton = { TextButton(onClick = { userPendingDeletion = null }) { Text("Cancel") } }) }
    passwordResetUser?.let { user -> ResetPasswordDialog(user, onDismiss = { passwordResetUser = null }) { password -> scope.launch { runCatching { repository.resetPassword(libraryId, currentUser, user, password) }.onSuccess { passwordResetUser = null }.onFailure { message = it.message } } } }
    displayNameUser?.let { user -> EditDisplayNameDialog(user, onDismiss = { displayNameUser = null }) { displayName -> scope.launch { runCatching { repository.updateDisplayName(libraryId, currentUser, user, displayName) }.onSuccess { displayNameUser = null }.onFailure { message = it.message } } } }
    statusUser?.let { user -> AlertDialog(onDismissRequest = { statusUser = null }, title = { Text(if (user.accountStatus == "Suspended") "Activate ${user.username}?" else "Suspend ${user.username}?") }, text = { Text(if (user.accountStatus == "Suspended") "This user will be allowed to sign in." else "This user will not be allowed to sign in until reactivated.") }, confirmButton = { Button(onClick = { scope.launch { runCatching { repository.updateAccountStatus(libraryId, currentUser, user, if (user.accountStatus == "Suspended") "Active" else "Suspended") }.onSuccess { statusUser = null }.onFailure { message = it.message } } }) { Text(if (user.accountStatus == "Suspended") "Activate" else "Suspend") } }, dismissButton = { TextButton(onClick = { statusUser = null }) { Text("Cancel") } }) }
}

@Composable private fun UserMetricCard(label: String, value: Int, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color)) { Column(Modifier.padding(10.dp)) { Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelMedium) } }
}

@Composable private fun LocalUserCard(user: UserEntity, canManage: Boolean, isCurrentUser: Boolean, isOwner: Boolean, onDelete: () -> Unit, onResetPassword: () -> Unit, onEditDisplayName: () -> Unit, onChangeStatus: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(roleColor(if (isOwner) UserRole.Owner else user.userRole), CircleShape), contentAlignment = Alignment.Center) {
                    Text((user.displayName ?: user.username).first().uppercase(), fontWeight = FontWeight.Bold)
                }
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(user.displayName ?: user.username, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(user.email ?: user.username, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (isOwner) "Owner account | ${user.accountStatus}" else "Local account | ${user.accountStatus}", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (canManage) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onEditDisplayName) { Text("Edit name") }
                    if (!isCurrentUser) TextButton(onClick = onResetPassword) { Text("Reset password") }
                }
                if (!isCurrentUser) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = onChangeStatus) { Text(if (user.accountStatus == "Suspended") "Activate" else "Suspend") }
                        if (!isOwner) TextButton(onClick = onDelete) { Text("Delete user", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}

@Composable private fun MemberCard(member: LibraryMember, canManage: Boolean, currentUserId: Long, onRole: () -> Unit, onRemove: () -> Unit, onDeleteUser: () -> Unit, onResetPassword: () -> Unit, onEditDisplayName: () -> Unit) {
    val isProtected = member.membership.userId == currentUserId || member.membership.role == UserRole.Owner.name
    var moreActionsVisible by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(roleColor(UserRole.fromStored(member.membership.role, false)), CircleShape), contentAlignment = Alignment.Center) {
                    Text((member.user.displayName ?: member.user.username).first().uppercase(), fontWeight = FontWeight.Bold)
                }
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(member.user.displayName ?: member.user.username, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(member.user.email ?: member.user.username, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(UserRole.fromStored(member.membership.role, false).label, style = MaterialTheme.typography.labelMedium)
                }
            }
            if (canManage && !isProtected) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onRole) { Text("Manage role") }
                    TextButton(onClick = onResetPassword) { Text("Reset password") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onEditDisplayName) { Text("Edit display name") }
                    Box {
                        TextButton(onClick = { moreActionsVisible = true }) { Text("More actions") }
                        DropdownMenu(expanded = moreActionsVisible, onDismissRequest = { moreActionsVisible = false }) {
                            DropdownMenuItem(text = { Text("Remove access") }, onClick = { moreActionsVisible = false; onRemove() })
                            DropdownMenuItem(text = { Text("Delete user", color = MaterialTheme.colorScheme.error) }, onClick = { moreActionsVisible = false; onDeleteUser() })
                        }
                    }
                }
            } else if (isProtected) {
                if (canManage) TextButton(onClick = onEditDisplayName, modifier = Modifier.align(Alignment.End)) { Text("Edit display name") }
                Text("Owner access is protected", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable private fun ResetPasswordDialog(user: UserEntity, onDismiss: () -> Unit, onReset: (String) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Set a new password for ${user.displayName ?: user.username}.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(password, { password = it; error = null }, label = { Text("New password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(confirmation, { confirmation = it; error = null }, label = { Text("Confirm new password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = when {
                    password.length < 6 -> "Use a password with at least 6 characters."
                    password != confirmation -> "Passwords do not match."
                    else -> null
                }
                if (error == null) onReset(password)
            }) { Text("Reset password") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable private fun CreateLibraryUserDialog(libraryId: Long, currentUser: UserEntity, repository: LibraryAccessRepository, onDismiss: () -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var duplicateUserError by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create library user") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("The new account will be added to this library as a Member. You can assign a different role afterward.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(username, { username = it; error = null }, label = { Text("Username *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(displayName, { displayName = it; error = null }, label = { Text("Display name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(password, { password = it; error = null }, label = { Text("Password *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = when {
                    username.isBlank() -> "Username is mandatory."
                    displayName.isBlank() -> "Display name is mandatory."
                    password.isBlank() -> "Password is mandatory."
                    password.length < 6 -> "Password must contain at least 6 characters."
                    else -> null
                }
                if (error == null) scope.launch {
                    runCatching { repository.createMember(libraryId, currentUser, username, displayName, password) }
                        .onSuccess { onDismiss() }
                        .onFailure { failure ->
                            error = if (failure.message?.contains("username", ignoreCase = true) == true) {
                                duplicateUserError = true
                                "A user with this username already exists. Choose a different username."
                            } else {
                                failure.message ?: "Could not create the user."
                            }
                        }
                }
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (duplicateUserError) {
        AlertDialog(
            onDismissRequest = { duplicateUserError = false },
            title = { Text("User already exists") },
            text = { Text("A user with the username \"${username.trim()}\" already exists. Choose a different username.") },
            confirmButton = { Button(onClick = { duplicateUserError = false }) { Text("OK") } }
        )
    }
}

@Composable private fun EditDisplayNameDialog(user: UserEntity, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var displayName by rememberSaveable { mutableStateOf(user.displayName.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit display name") }, text = { Column { OutlinedTextField(displayName, { displayName = it; error = null }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth(), singleLine = true); error?.let { Text(it, color = MaterialTheme.colorScheme.error) } } }, confirmButton = { Button(onClick = { if (displayName.isBlank()) error = "Enter a display name." else onSave(displayName) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AddLibraryMemberDialog(users: List<UserEntity>, onDismiss: () -> Unit, onAdd: (UserEntity) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add library member") },
        text = {
            if (users.isEmpty()) Text("All local user accounts already belong to this library. Create another account from Settings > Manage users first.")
            else LazyColumn(Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users, key = { it.id }) { user ->
                    Card(onClick = { onAdd(user) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(user.displayName?.takeIf { it.isNotBlank() } ?: user.username, fontWeight = FontWeight.Bold)
                            Text(user.email ?: user.username, style = MaterialTheme.typography.bodySmall)
                            Text("Add as Member", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable private fun RoleEditor(member: LibraryMember, onDismiss: () -> Unit, onSave: (UserRole) -> Unit) { var role by rememberSaveable { mutableStateOf(member.membership.role) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Change role") }, text = { Column { UserRole.values().filter { it != UserRole.Owner }.forEach { option -> TextButton(onClick = { role = option.name }, modifier = Modifier.fillMaxWidth()) { Text(if (role == option.name) "${option.label} selected" else option.label) } } } }, confirmButton = { Button(onClick = { onSave(UserRole.fromStored(role, false)) }) { Text("Confirm") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

@Composable private fun RequestCard(request: MembershipRequestEntity, canDecide: Boolean, onApprove: () -> Unit, onReject: () -> Unit) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(request.email, fontWeight = FontWeight.Bold); Text("${request.status} | Code: ${request.inviteCode}", style = MaterialTheme.typography.bodySmall) }; if (canDecide) { TextButton(onClick = onApprove) { Text("Approve") }; TextButton(onClick = onReject) { Text("Reject") } } } } }

@Composable private fun InviteMemberDialog(libraryId: Long, user: UserEntity, repository: LibraryAccessRepository, onDismiss: () -> Unit) { val context = LocalContext.current; val scope = rememberCoroutineScope(); var email by rememberSaveable { mutableStateOf("") }; var invite by remember { mutableStateOf<MembershipRequestEntity?>(null) }; var error by rememberSaveable { mutableStateOf<String?>(null) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Invite member") }, text = { Column { OutlinedTextField(email, { email = it; error = null }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth()); invite?.let { Text("Invite code: ${it.inviteCode}\npersonallibrary://invite/${it.inviteCode}", modifier = Modifier.padding(top = 12.dp)); TextButton(onClick = { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "Join my Personal Library: personallibrary://invite/${it.inviteCode}"), "Share invitation")) }) { Text("Share link or code") }; TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${it.email}?subject=Library invitation&body=Use invite code ${it.inviteCode}"))) }) { Text("Email invitation") } }; error?.let { Text(it, color = MaterialTheme.colorScheme.error) } } }, confirmButton = { if (invite == null) Button(onClick = { scope.launch { runCatching { repository.invite(libraryId, user, email) }.onSuccess { invite = it }.onFailure { error = it.message } } }) { Text("Create invite") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }) }

private fun roleColor(role: UserRole) = when (role) { UserRole.Owner -> Color(0xFFFFE2A8); UserRole.Admin -> Color(0xFFCDE8FF); UserRole.Librarian -> Color(0xFFCDEFE2); UserRole.Member -> Color(0xFFE8E0FF); UserRole.Guest -> Color(0xFFE5E7EB) }
private fun LibraryPermission.label() = name.replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")