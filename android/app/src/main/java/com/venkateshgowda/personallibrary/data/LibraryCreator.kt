package com.venkateshgowda.personallibrary.data

object LibraryCreator {
    fun create(
        name: String,
        description: String,
        imagePath: String?,
        owner: String,
        createdAtMillis: Long = System.currentTimeMillis()
    ): LibraryEntity {
        require(name.isNotBlank()) { "Library name is required." }
        require(owner.isNotBlank()) { "Owner name is required." }
        return LibraryEntity(
            name = name.trim(),
            description = description.trim().ifBlank { null },
            imagePath = imagePath,
            owner = owner.trim(),
            createdAtMillis = createdAtMillis
        )
    }
}