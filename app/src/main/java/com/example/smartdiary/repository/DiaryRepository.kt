package com.smartdiary.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.smartdiary.model.DiaryEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class DiaryRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    private fun entriesCollection() =
        firestore.collection("users")
            .document(currentUserId)
            .collection("entries")

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

    // Converte a foto para Base64 e salva direto no Firestore (sem Firebase Storage)
    suspend fun uploadPhoto(localUri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(localUri)
                ?: return Result.failure(Exception("Não foi possível abrir a imagem"))

            // Reduz o tamanho da imagem antes de converter (evita documento gigante no Firestore)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val scaled = Bitmap.createScaledBitmap(original, 600, 600, true)

            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()

            val base64String = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)

            Result.success(base64String)
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
