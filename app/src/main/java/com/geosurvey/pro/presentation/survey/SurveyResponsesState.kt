package com.geosurvey.pro.presentation.survey

import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyResponse

data class SurveyResponsesState(
    val survey: Survey? = null,
    val responses: List<SurveyResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SurveyResponsesEvent {
    data class LoadResponses(val surveyId: String) : SurveyResponsesEvent()
    data class RejectResponse(val responseId: String, val feedback: String) : SurveyResponsesEvent()
    data class ApproveResponse(val responseId: String) : SurveyResponsesEvent()
}
