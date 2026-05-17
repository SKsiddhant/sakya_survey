package com.geosurvey.pro.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Survey(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var questions: List<SurveyQuestion> = emptyList(),
    
    @get:PropertyName("active")
    @set:PropertyName("active")
    var isActive: Boolean = true,
    
    var expiryDate: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var radius: Double = 500.0,
    var locationName: String = "",
    var creatorId: String = "",
    var createdAt: Long = System.currentTimeMillis()
)

enum class ResponseStatus {
    PENDING, APPROVED, REJECTED
}

data class SurveyResponse(
    var id: String = "",
    var surveyId: String = "",
    var respondentId: String = "",
    var answers: Map<String, Any> = emptyMap(),
    var timestamp: Long = System.currentTimeMillis(),
    var status: ResponseStatus = ResponseStatus.PENDING,
    var adminFeedback: String? = null
)
