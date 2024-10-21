package com.example.spaTi.ui.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spaTi.data.models.Tag
import com.example.spaTi.data.repository.TagRepository
import com.example.spaTi.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel for managing tag-related data and operations in the UI layer.
 *
 * This ViewModel communicates with the [TagRepository] to fetch, add, update, and delete tags.
 * It exposes LiveData objects for observing the state of these operations in the UI.
 *
 * @param repository The [TagRepository] instance injected via Hilt for performing CRUD operations on tags.
 */
@HiltViewModel
class TagViewModel @Inject constructor (
    val repository: TagRepository
) : ViewModel() {
    // LiveData for observing the list of tags and its state.
    private val _tags = MutableLiveData<UiState<List<Tag>>>()
    val tag: LiveData<UiState<List<Tag>>>
        get() = _tags

    // LiveData for observing the result of adding a new tag.
    private val _addTag = MutableLiveData<UiState<Pair<Tag, String>>>()
    val addTag: LiveData<UiState<Pair<Tag, String>>>
        get() = _addTag

    // LiveData for observing the result of updating an existing tag.
    private val _updateTag = MutableLiveData<UiState<String>>()
    val updateTag: LiveData<UiState<String>>
        get() = _updateTag

    // LiveData for observing the result of deleting a tag.
    private val _deleteTag = MutableLiveData<UiState<String>>()
    val deleteTag: LiveData<UiState<String>>
        get() = _deleteTag

    // LiveData for observing the result of searching for a tag by id
    private val _getTagById = MutableLiveData<UiState<Tag?>>()
    val getTagById: LiveData<UiState<Tag?>>
        get() = _getTagById

    private val _allTagsProcessed = MutableLiveData<UiState<Boolean>>()
    val allTagsProcessed: LiveData<UiState<Boolean>> = _allTagsProcessed

    fun getTags() {
        _tags.value = UiState.Loading
        repository.getTags { _tags.value = it }
    }

    fun addTag(tag: Tag) {
        _addTag.value = UiState.Loading
        repository.addTag(tag) { _addTag.value = it }
    }

    fun updateTag(tag: Tag) {
        _updateTag.value = UiState.Loading
        repository.updateTag(tag) { _updateTag.value = it }
    }

    fun deleteTag(tag: Tag) {
        _deleteTag.value = UiState.Loading
        repository.deleteTag(tag) { _deleteTag.value = it }
    }

    fun getTagById(id: String) {
        _getTagById.value = UiState.Loading
        repository.getTagById(id) { _getTagById.value = it }
    }

    fun searchTags(query: String) {
        _tags.value = UiState.Loading
        repository.searchTags(query) { _tags.value = it }
    }
}