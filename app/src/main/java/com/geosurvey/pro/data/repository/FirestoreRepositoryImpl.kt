package com.geosurvey.pro.data.repository

import android.util.Log
import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyResponse
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreRepository {

    private val TAG = "FirestoreRepo"
    private val surveysCollection = firestore.collection("surveys")
    private val responsesCollection = firestore.collection("responses")

    override fun getSurveys(): Flow<List<Survey>> = callbackFlow {
        val subscription = surveysCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            snapshot?.let {
                trySend(it.toObjects(Survey::class.java))
            } ?: trySend(emptyList())
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getSurvey(id: String): Result<Survey> {
        return try {
            val doc = surveysCollection.document(id).get().await()
            val survey = doc.toObject(Survey::class.java) ?: throw Exception("Survey not found")
            Result.success(survey)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting survey $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getAdminSurveys(adminId: String): Flow<List<Survey>> = callbackFlow {
        val subscription = surveysCollection.whereEqualTo("creatorId", adminId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(Survey::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createSurvey(survey: Survey): Result<Unit> {
        return try {
            val docRef = if (survey.id.isEmpty()) surveysCollection.document() else surveysCollection.document(survey.id)
            val finalSurvey = if (survey.id.isEmpty()) survey.copy(id = docRef.id) else survey
            docRef.set(finalSurvey).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSurvey(survey: Survey): Result<Unit> {
        return try {
            surveysCollection.document(survey.id).set(survey).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSurvey(surveyId: String): Result<Unit> {
        return try {
            surveysCollection.document(surveyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleSurveyStatus(surveyId: String, isActive: Boolean): Result<Unit> {
        return try {
            surveysCollection.document(surveyId).update("active", isActive).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitResponse(response: SurveyResponse): Result<Unit> {
        return try {
            // Server-side duplicate prevention (only if not already rejected)
            val existing = responsesCollection
                .whereEqualTo("surveyId", response.surveyId)
                .whereEqualTo("respondentId", response.respondentId)
                .get()
                .await()
            
            val rejectedResponse = existing.documents.firstOrNull { 
                it.toObject(SurveyResponse::class.java)?.status == ResponseStatus.REJECTED 
            }

            if (!existing.isEmpty && rejectedResponse == null) {
                return Result.failure(Exception("You have already submitted a response for this survey."))
            }

            if (rejectedResponse != null) {
                // Update the existing rejected response
                responsesCollection.document(rejectedResponse.id).set(response).await()
            } else {
                // New response
                responsesCollection.document().set(response).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getResponses(surveyId: String): Flow<List<SurveyResponse>> = callbackFlow {
        val subscription = responsesCollection.whereEqualTo("surveyId", surveyId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    trySend(it.toObjects(SurveyResponse::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateResponseStatus(responseId: String, status: ResponseStatus, feedback: String?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status.name)
            feedback?.let { updates["adminFeedback"] = it }
            
            // Find the document ID if not known, but usually we have it
            responsesCollection.whereEqualTo("id", responseId).get().await().documents.firstOrNull()?.let {
                it.reference.update(updates).await()
            } ?: responsesCollection.document(responseId).update(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserResponses(userId: String): Flow<List<SurveyResponse>> = callbackFlow {
        val subscription = responsesCollection.whereEqualTo("respondentId", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(SurveyResponse::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllAdminResponses(adminId: String): Flow<List<SurveyResponse>> = callbackFlow {
        val subscription = responsesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            snapshot?.let { trySend(it.toObjects(SurveyResponse::class.java)) } ?: trySend(emptyList())
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun broadcastNotification(title: String, body: String): Result<Unit> {
        return try {
            val broadcast = hashMapOf(
                "title" to title,
                "body" to body,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("broadcasts").document().set(broadcast).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBroadcasts(): Flow<List<Map<String, Any>>> = callbackFlow {
        val subscription = firestore.collection("broadcasts")
            .whereGreaterThan("timestamp", System.currentTimeMillis() - 60000) // Only last 1 min
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.documents.map { doc -> doc.data ?: emptyMap() }) }
            }
        awaitClose { subscription.remove() }
    }
}
