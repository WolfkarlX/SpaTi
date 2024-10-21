package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Tag
import com.example.spaTi.util.UiState


/**
 * Interface representing the repository for managing tags.
 * This interface defines the contract for CRUD (Create, Read, Update, Delete) operations
 * on [Tag] objects, facilitating database interactions. The repository functions
 * asynchronously using callback functions that return different types of UiState
 * to indicate the state of the operation (loading, success, or failure).
 *
 * @see [TagRepositoryImpl] For the implementation of this interface.
 */
interface TagRepository {
    fun getTags(result: (UiState<List<Tag>>) -> Unit)
    fun addTag(tag: Tag, result: (UiState<Pair<Tag, String>>) -> Unit)
    fun updateTag(tag: Tag, result: (UiState<String>) -> Unit)
    fun deleteTag(tag: Tag, result: (UiState<String>) -> Unit)

    fun getTagById(id: String, result: (UiState<Tag?>) -> Unit)
    fun searchTags(query: String, result: (UiState<List<Tag>>) -> Unit)
    fun updateTagCount(tagId: String, change: Int)
}