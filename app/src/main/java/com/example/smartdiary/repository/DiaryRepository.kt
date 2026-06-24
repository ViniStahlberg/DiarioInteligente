package com.smartdiary.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.smartdiary.model.DiaryEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DiaryRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    private fun entriesCollection() =
        firestore.collection("users")
            .document(currentUserId)
            .collection("entries")

    // Fluxo em tempo real dos registros do usuário
    fun getEntriesFlow(): Flow<List<DiaryEntry>> = callbackFlow {
        val listener = entriesCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiaryEntry::class.java)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getEntryById(id: String): DiaryEntry? {
        return try {
            entriesCollection()
                .document(id)
                .get()
                .await()
                .toObject(DiaryEntry::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveEntry(entry: DiaryEntry): Result<String> {
        return try {
            val docRef = if (entry.id.isEmpty()) {
                entriesCollection().document()
            } else {
                entriesCollection().document(entry.id)
            }
            val finalEntry = entry.copy(
                id = docRef.id,
                userId = currentUserId
            )
            docRef.set(finalEntry.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEntry(id: String): Result<Unit> {
        return try {
            entriesCollection().document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllEntries(): Result<Unit> {
        return try {
            val docs = entriesCollection().get().await()
            val batch = firestore.batch()
            docs.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload de foto para Firebase Storage
    suspend fun uploadPhoto(localUri: Uri): Result<String> {
        return try {
            val fileName = "photos/${currentUserId}/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            ref.putFile(localUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEntryCountFlow(): Flow<Int> = callbackFlow {
        val listener = entriesCollection()
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }
}