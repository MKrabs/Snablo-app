package de.mkrabs.snablo.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val label: String,
    val order: Int? = null
)

@Serializable
data class CategoryListResponse(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<Category>
)
