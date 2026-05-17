package com.geosurvey.pro.presentation.survey.form

import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyQuestion

data class SurveyFormState(
    val survey: Survey? = null,
    val answers: Map<String, Any> = emptyMap(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class SurveyFormEffect {
    object NavigateBack : SurveyFormEffect()
    data class ShowError(val message: String) : SurveyFormEffect()
}

sealed class SurveyFormEvent {
    data class LoadSurvey(val id: String) : SurveyFormEvent()
    data class AnswerChanged(val questionId: String, val answer: Any) : SurveyFormEvent()
    object Submit : SurveyFormEvent()
}
