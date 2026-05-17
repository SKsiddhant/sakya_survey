package com.geosurvey.pro.presentation.survey

import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyQuestion
import com.geosurvey.pro.domain.model.SurveyResponse

data class SurveyState(
    val surveys: List<Survey> = emptyList(),
    val allResponses: List<SurveyResponse> = emptyList(),
    val currentSurvey: Survey = Survey(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class SurveyEffect {
    object NavigateBack : SurveyEffect()
    data class ShowError(val message: String) : SurveyEffect()
}

sealed class SurveyEvent {
    data class LoadSurvey(val id: String) : SurveyEvent()
    data class UpdateTitle(val title: String) : SurveyEvent()
    data class UpdateDescription(val desc: String) : SurveyEvent()
    object AddQuestion : SurveyEvent()
    data class RemoveQuestion(val id: String) : SurveyEvent()
    data class UpdateQuestion(val question: SurveyQuestion) : SurveyEvent()
    data class UpdateLocation(val lat: Double, val lng: Double, val radius: Double, val locationName: String? = null) : SurveyEvent()
    object SaveSurvey : SurveyEvent()
    object UseCurrentLocation : SurveyEvent()
    data class DeleteSurvey(val id: String) : SurveyEvent()
    data class ToggleStatus(val id: String, val isActive: Boolean) : SurveyEvent()
}
