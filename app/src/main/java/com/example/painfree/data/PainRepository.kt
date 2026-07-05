package com.example.painfree.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.painfree.core.Constants
import com.google.firebase.firestore.toObject

/**
 * Domain model used by the UI.
 */
data class PainData(
    val mainTitle: String,
    val gifUrls: List<String>,
    val instructions: List<String>,
    val pageTitles: List<String>,
    val aspectRatios: List<Float?>
)

/**
 * Firestore DTO (Data Transfer Object) matching the database structure.
 * Standard practice is to provide default null values for automatic deserialization.
 */
data class GifEntry(
    val url: String? = null,
    val instructions: List<String>? = null,
    val name: String? = null,
    val title: String? = null,
    val dimensions: String? = null
)

data class PainDoc(
    val title: String? = null,
    val name: String? = null,
    val gifs: List<GifEntry>? = null
)

class PainRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getPainDetails(painId: String): PainData {
        val snapshot = db.collection(Constants.COLLECTION_PAINS).document(painId).get().await()
        
        // Standard Firebase practice: toObject() handles the mapping automatically
        val doc = snapshot.toObject<PainDoc>()

        // Map the DTO to the Domain Model with business logic (like formatting)
        val mainTitle = doc?.title ?: doc?.name 
            ?: (Constants.PAIN_FALLBACK_TITLES[painId] ?: "Recovery Pain")

        val gifUrls = doc?.gifs?.mapNotNull { it.url } ?: emptyList()
        
        val instructionsList = doc?.gifs?.map { entry ->
            entry.instructions?.mapIndexed { index, s -> "${index + 1}. $s" }?.joinToString("\n") ?: ""
        } ?: emptyList()

        val pageTitles = doc?.gifs?.map { it.name ?: it.title ?: "" } ?: emptyList()

        val aspectRatios = doc?.gifs?.map { entry ->
            entry.dimensions?.let { dims ->
                try {
                    val parts = dims.split("x")
                    if (parts.size == 2) {
                        val w = parts[0].trim().toFloat()
                        val h = parts[1].trim().toFloat()
                        if (h > 0) w / h else null
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
        } ?: emptyList()
        
        return PainData(mainTitle, gifUrls, instructionsList, pageTitles, aspectRatios)
    }
}
