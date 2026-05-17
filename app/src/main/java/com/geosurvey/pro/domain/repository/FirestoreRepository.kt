package com.geosurvey.pro.domain.repository

import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyResponse
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    fun getSurveys(): Flow<List<Survey>>
    suspend fun getSurvey(id: String): Result<Survey>
    fun getAdminSurveys(adminId: String): Flow<List<Survey>>
    suspend fun createSurvey(survey: Survey): Result<Unit>
    suspend fun updateSurvey(survey: Survey): Result<Unit>
    suspend fun deleteSurvey(surveyId: String): Result<Unit>
    suspend fun toggleSurveyStatus(surveyId: String, isActive: Boolean): Result<Unit>
    suspend fun submitResponse(response: SurveyResponse): Result<Unit>
    fun getResponses(surveyId: String): Flow<List<SurveyResponse>>
    suspend fun updateResponseStatus(responseId: String, status: ResponseStatus, feedback: String?): Result<Unit>
    fun getUserResponses(userId: String): Flow<List<SurveyResponse>>
    fun getAllAdminResponses(adminId: String): Flow<List<SurveyResponse>>
    suspend fun broadcastNotification(title: String, body: String): Result<Unit>
    fun getBroadcasts(): Flow<List<Map<String, Any>>>
}
