package com.venkateshgowda.personallibrary.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

data class LibraryMember(val user: UserEntity, val membership: LibraryMembershipEntity)

class LibraryAccessRepository(private val database: LibraryDatabase) {
    private val membershipDao = database.membershipDao()
    private val requestDao = database.membershipRequestDao()
    private val auditDao = database.auditLogDao()
    private val userDao = database.userDao()

    fun observeMembers(libraryId: Long): Flow<List<LibraryMember>> = combine(
        userDao.observeAll(), membershipDao.observeForLibrary(libraryId)
    ) { users, memberships ->
        memberships.mapNotNull { membership -> users.firstOrNull { it.id == membership.userId }?.let { LibraryMember(it, membership) } }
    }

    fun observeRequests(libraryId: Long) = requestDao.observeForLibrary(libraryId)
    fun observeAudit(libraryId: Long) = auditDao.observeRecent(libraryId)
    fun observeUsers() = userDao.observeAll()

    suspend fun ensureMembership(libraryId: Long, user: UserEntity) {
        if (membershipDao.find(libraryId, user.id) == null) {
            membershipDao.upsert(LibraryMembershipEntity(libraryId = libraryId, userId = user.id, role = user.userRole.name))
        }
    }

    suspend fun addMember(libraryId: Long, actor: UserEntity, user: UserEntity) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to add members." }
        require(membershipDao.find(libraryId, user.id) == null) { "This user already belongs to the library." }
        membershipDao.upsert(LibraryMembershipEntity(libraryId = libraryId, userId = user.id, role = UserRole.Member.name))
        audit(libraryId, actor.id, "Member added", "${user.displayName ?: user.username} added as Member")
    }

    suspend fun createMember(libraryId: Long, actor: UserEntity, username: String, displayName: String?, password: String) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to create users." }
        require(username.isNotBlank()) { "Enter a username." }
        require(password.length >= 6) { "Use a password with at least 6 characters." }
        require(userDao.findByUsername(username.trim()) == null) { "A user with this username already exists. Choose a different username." }
        val userId = userDao.insert(
            UserEntity(
                username = username.trim(),
                displayName = displayName?.trim()?.ifBlank { null },
                passwordHash = PasswordHasher.hash(password.toCharArray()),
                isAdmin = false,
                role = UserRole.Member.name
            )
        )
        membershipDao.upsert(LibraryMembershipEntity(libraryId = libraryId, userId = userId, role = UserRole.Member.name))
        audit(libraryId, actor.id, "User created", "${displayName?.trim()?.ifBlank { null } ?: username.trim()} created as Member")
    }

    suspend fun invite(libraryId: Long, actor: UserEntity, email: String): MembershipRequestEntity {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to invite members." }
        require(email.contains("@")) { "Enter a valid email address." }
        val request = MembershipRequestEntity(libraryId = libraryId, email = email.trim(), inviteCode = UUID.randomUUID().toString().substring(0, 8).uppercase())
        requestDao.insert(request)
        audit(libraryId, actor.id, "User invited", request.email)
        return request
    }

    suspend fun updateRole(libraryId: Long, actor: UserEntity, membership: LibraryMembershipEntity, role: UserRole) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to change roles." }
        require(membership.userId != actor.id || role == UserRole.Owner) { "You cannot remove your own access." }
        require(UserRole.fromStored(membership.role, false) != UserRole.Owner) { "Transfer ownership before changing the owner's role." }
        membershipDao.update(membership.copy(role = role.name))
        audit(libraryId, actor.id, "Role changed", "Member ${membership.userId} is now ${role.label}")
    }

    suspend fun removeMember(libraryId: Long, actor: UserEntity, membership: LibraryMembershipEntity) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to remove members." }
        require(membership.userId != actor.id) { "You cannot remove yourself." }
        require(UserRole.fromStored(membership.role, false) != UserRole.Owner) { "Transfer ownership before removing the owner." }
        membershipDao.delete(membership.id)
        audit(libraryId, actor.id, "User removed", "Member ${membership.userId} removed")
    }

    suspend fun deleteUser(libraryId: Long, actor: UserEntity, user: UserEntity) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to delete users." }
        require(user.id != actor.id) { "You cannot delete the account currently signed in." }
        require(!membershipDao.hasOwnerMembership(user.id)) { "Transfer ownership before deleting an owner." }
        userDao.deleteUser(user.id)
        audit(libraryId, actor.id, "User deleted", "${user.displayName ?: user.username} deleted")
    }

    suspend fun resetPassword(libraryId: Long, actor: UserEntity, user: UserEntity, password: String) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to reset passwords." }
        require(password.length >= 6) { "Use a password with at least 6 characters." }
        userDao.update(user.copy(passwordHash = PasswordHasher.hash(password.toCharArray())))
        audit(libraryId, actor.id, "Password reset", "Password reset for ${user.displayName ?: user.username}")
    }

    suspend fun updateDisplayName(libraryId: Long, actor: UserEntity, user: UserEntity, displayName: String) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to edit users." }
        require(displayName.isNotBlank()) { "Enter a display name." }
        userDao.update(user.copy(displayName = displayName.trim()))
        audit(libraryId, actor.id, "User updated", "Display name changed for ${user.username}")
    }

    suspend fun updateAccountStatus(libraryId: Long, actor: UserEntity, user: UserEntity, status: String) {
        require(actor.userRole.can(LibraryPermission.ManageUsers)) { "You do not have permission to change account status." }
        require(user.id != actor.id) { "You cannot disable the account currently signed in." }
        require(status in setOf("Active", "Suspended")) { "Unsupported account status." }
        userDao.update(user.copy(accountStatus = status))
        audit(libraryId, actor.id, "Account status changed", "${user.username} is now $status")
    }

    suspend fun transferOwnership(libraryId: Long, actor: UserEntity, currentOwner: LibraryMembershipEntity, successor: LibraryMembershipEntity) {
        require(actor.userRole == UserRole.Owner && currentOwner.userId == actor.id) { "Only the owner can transfer ownership." }
        require(successor.status == "Active") { "Ownership can only be transferred to an active member." }
        membershipDao.update(currentOwner.copy(role = UserRole.Admin.name))
        membershipDao.update(successor.copy(role = UserRole.Owner.name))
        audit(libraryId, actor.id, "Ownership transferred", "Member ${successor.userId} is now owner")
    }

    suspend fun decideRequest(libraryId: Long, actor: UserEntity, request: MembershipRequestEntity, approved: Boolean) {
        require(actor.userRole.can(LibraryPermission.ApproveRequests)) { "You do not have permission to approve requests." }
        requestDao.update(request.copy(status = if (approved) "Approved" else "Rejected", respondedAtMillis = System.currentTimeMillis()))
        audit(libraryId, actor.id, if (approved) "Membership approved" else "Membership rejected", request.email)
    }

    private suspend fun audit(libraryId: Long, actorId: Long, action: String, detail: String) {
        auditDao.insert(AuditLogEntity(libraryId = libraryId, actorUserId = actorId, action = action, detail = detail))
    }
}

class LibraryAccessViewModel(private val repository: LibraryAccessRepository) : ViewModel() {
    fun invite(libraryId: Long, actor: UserEntity, email: String, onResult: (Result<MembershipRequestEntity>) -> Unit) = viewModelScope.launch {
        onResult(runCatching { repository.invite(libraryId, actor, email) })
    }

    fun decideRequest(libraryId: Long, actor: UserEntity, request: MembershipRequestEntity, approved: Boolean, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        onResult(runCatching { repository.decideRequest(libraryId, actor, request, approved) })
    }
}