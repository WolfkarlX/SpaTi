package com.example.spaTi.data.repository

import android.util.Log
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Tag
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.FireStoreDocumentField
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Implementation of the [TagRepository] interface that uses Firestore as the data source.
 * This class provides methods to perform CRUD operations on [Tag] objects in Firestore.
 */
class TagRepositoryImpl (
    val database: FirebaseFirestore
) : TagRepository {
    /**
     * Retrieves all tags from the Firestore collection, ordered by most related to tags in descending order.
     *
     * @param result A callback function that returns a [UiState] containing a list of [Tag] objects if successful,
     * or an error message if the operation fails.
     */
    override fun getTags(result: (UiState<List<Tag>>) -> Unit) {
        database.collection(FireStoreCollection.TAG)
            .orderBy("relatedCount", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                val tags = arrayListOf<Tag>()
                for (document in it) {
                    val tag = document.toObject(Tag::class.java)
                    tags.add(tag)
                }
                result.invoke(
                    UiState.Success(tags)
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    /**
     * Adds a new tag to the Firestore collection. Generates a new document ID for the tag.
     *
     * @param tag The [Tag] object to be added to Firestore.
     * @param result A callback function that returns a [UiState] containing a pair of the added [Tag]
     * and a success message if the operation is successful, or an error message if it fails.
     */
    override fun addTag(tag: Tag, result: (UiState<Pair<Tag, String>>) -> Unit) {
        val document = database.collection(FireStoreCollection.TAG).document()
        tag.id = document.id
        document
            .set(tag)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success(Pair(
                        tag,
                        "Tag has been created successfully"
                    ))
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    /**
     * Updates an existing tag in the Firestore collection based on the tag's ID.
     *
     * @param tag The [Tag] object containing the updated data.
     * @param result A callback function that returns a [UiState] containing a success message if the operation
     * is successful, or an error message if it fails.
     */
    override fun updateTag(tag: Tag, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.TAG).document(tag.id)
        document
            .set(tag)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Tag has been update successfully")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    /**
     * Deletes an existing tag from the Firestore collection based on the tag's ID.
     *
     * @param tag The [Tag] object to be deleted.
     * @param result A callback function that returns a [UiState] containing a success message if the operation
     * is successful, or an error message if it fails.
     */
    override fun deleteTag(tag: Tag, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.TAG).document(tag.id)
            .delete()
            .addOnSuccessListener {
                result.invoke(UiState.Success("Tag successfully deleted!"))
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.message))
            }
    }

    override fun getTagById(id: String, result: (UiState<Tag?>) -> Unit) {
        database.collection(FireStoreCollection.TAG)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val tag = documentSnapshot.toObject(Tag::class.java)
                    result.invoke(UiState.Success(tag))
                } else {
                    result.invoke(UiState.Success(null))
                }
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(it.localizedMessage)
                )
            }
    }

    override fun searchTags(query: String, result: (UiState<List<Tag>>) -> Unit) {
        database.collection(FireStoreCollection.TAG)
            .orderBy("name")
            .startAt(query)
            .endAt(query + '\uf8ff')
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tags = querySnapshot.toObjects(Tag::class.java)
                result.invoke(UiState.Success(tags))
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(it.localizedMessage)
                )
            }
    }

    override fun updateTagCount(tagId: String, change: Int) {
        val tagRef = database.collection(FireStoreCollection.TAG).document(tagId)
        database.runTransaction { transaction ->
            val snapshot = transaction.get(tagRef)
            val currentCount = snapshot.getLong("relatedCount") ?: 0
            transaction.update(tagRef, "relatedCount", currentCount + change)
        }.addOnFailureListener { e ->
            Log.e("TagRepository", "Error updating tag count", e)
        }
    }
}