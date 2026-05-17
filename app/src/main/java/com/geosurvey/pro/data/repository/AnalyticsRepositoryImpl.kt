package com.geosurvey.pro.data.repository

import com.geosurvey.pro.domain.model.AnalyticsData
import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyResponse
import com.geosurvey.pro.domain.repository.AnalyticsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnalyticsRepository {

    override fun getAdminAnalytics(adminId: String): Flow<AnalyticsData> = callbackFlow {
        val surveysQuery = firestore.collection("surveys").whereEqualTo("creatorId", adminId)
        val responsesQuery = firestore.collection("responses")

        val listener = surveysQuery.addSnapshotListener { surveySnapshot, _ ->
            val surveys = surveySnapshot?.toObjects(Survey::class.java) ?: emptyList()
            
            // In a real app, you'd filter responses by survey IDs belonging to this admin
            responsesQuery.addSnapshotListener { responseSnapshot, _ ->
                val allResponses = responseSnapshot?.toObjects(SurveyResponse::class.java) ?: emptyList()
                val adminSurveyIds = surveys.map { it.id }.toSet()
                val adminResponses = allResponses.filter { adminSurveyIds.contains(it.surveyId) }

                val data = AnalyticsData(
                    totalSurveys = surveys.size,
                    totalResponses = adminResponses.size,
                    activeUsers = adminResponses.map { it.respondentId }.distinct().size,
                    completionRate = if (surveys.isNotEmpty()) (adminResponses.size.toFloat() / surveys.size.toFloat()) else 0f,
                    responsesBySurvey = adminResponses.groupBy { it.surveyId }.mapValues { it.value.size }
                )
                trySend(data)
            }
        }
        awaitClose { /* In a real app, handle multiple listener removals */ }
    }
}
