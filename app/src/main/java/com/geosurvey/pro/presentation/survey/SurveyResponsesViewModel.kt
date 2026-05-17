package com.geosurvey.pro.presentation.survey

import androidx.lifecycle.viewModelScope
import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyResponsesViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : BaseViewModel<SurveyResponsesState, Unit, SurveyResponsesEvent>(SurveyResponsesState()) {

    override fun onEvent(event: SurveyResponsesEvent) {
        when (event) {
            is SurveyResponsesEvent.LoadResponses -> loadResponses(event.surveyId)
            is SurveyResponsesEvent.ApproveResponse -> updateStatus(event.responseId, ResponseStatus.APPROVED)
            is SurveyResponsesEvent.RejectResponse -> updateStatus(event.responseId, ResponseStatus.REJECTED, event.feedback)
        }
    }

    private fun loadResponses(surveyId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            firestoreRepository.getSurvey(surveyId).onSuccess { survey ->
                setState { copy(survey = survey) }
            }
            firestoreRepository.getResponses(surveyId).collect { responses ->
                setState { copy(responses = responses, isLoading = false) }
            }
        }
    }

    private fun updateStatus(responseId: String, status: ResponseStatus, feedback: String? = null) {
        viewModelScope.launch {
            firestoreRepository.updateResponseStatus(responseId, status, feedback)
        }
    }
}
