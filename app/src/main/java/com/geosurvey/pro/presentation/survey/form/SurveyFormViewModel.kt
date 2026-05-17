package com.geosurvey.pro.presentation.survey.form

import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.domain.model.SurveyResponse
import com.geosurvey.pro.domain.repository.AuthRepository
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.domain.repository.LocationRepository
import com.geosurvey.pro.domain.repository.StorageRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyFormViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val storageRepository: StorageRepository
) : BaseViewModel<SurveyFormState, SurveyFormEffect, SurveyFormEvent>(SurveyFormState()) {

    override fun onEvent(event: SurveyFormEvent) {
        when (event) {
            is SurveyFormEvent.LoadSurvey -> loadSurvey(event.id)
            is SurveyFormEvent.AnswerChanged -> {
                val newAnswers = uiState.value.answers.toMutableMap()
                newAnswers[event.questionId] = event.answer
                setState { copy(answers = newAnswers) }
            }
            SurveyFormEvent.Submit -> submitResponse()
        }
    }

    private fun loadSurvey(id: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val user = authRepository.currentUser.first()
            
            firestoreRepository.getSurvey(id).onSuccess { survey ->
                setState { copy(survey = survey) }
                
                // Check for existing response to pre-fill (especially if flagged)
                if (user != null) {
                    firestoreRepository.getUserResponses(user.id).first().find { it.surveyId == id }?.let { response ->
                        setState { copy(answers = response.answers) }
                    }
                }
                setState { copy(isLoading = false) }
            }.onFailure {
                setState { copy(isLoading = false, error = it.message) }
            }
        }
    }

    private fun submitResponse() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val survey = uiState.value.survey ?: return@launch
            
            setState { copy(isSubmitting = true) }

            // Geofencing Check
            val currentLocation = locationRepository.getCurrentLocation()
            if (currentLocation == null) {
                setState { copy(isSubmitting = false, error = "Unable to verify your location. Please ensure GPS is on.") }
                sendEffect(SurveyFormEffect.ShowError("Location required"))
                return@launch
            }

            val distance = locationRepository.calculateDistance(
                currentLocation.latitude,
                currentLocation.longitude,
                survey.latitude,
                survey.longitude
            )

            if (distance > survey.radius) {
                val formattedDistance = java.util.Locale.getDefault().let { locale ->
                    String.format(locale, "%.0f", distance)
                }
                val errorMessage = "You are $formattedDistance meters away from the survey location. Please get closer to submit."
                setState { copy(isSubmitting = false, error = errorMessage) }
                sendEffect(SurveyFormEffect.ShowError("Outside survey area"))
                return@launch
            }

            // Image Uploads
            val finalAnswers = uiState.value.answers.toMutableMap()
            for ((qId, answer) in uiState.value.answers) {
                if (answer is Uri) {
                    val path = "surveys/${survey.id}/responses/${user?.id ?: "anon"}/$qId.jpg"
                    val uploadResult = storageRepository.uploadFile(answer, path)
                    uploadResult.onSuccess { downloadUrl ->
                        finalAnswers[qId] = downloadUrl
                    }.onFailure {
                        setState { copy(isSubmitting = false, error = "Failed to upload image: ${it.message}") }
                        return@launch
                    }
                }
            }
            
            val response = SurveyResponse(
                id = java.util.UUID.randomUUID().toString(),
                surveyId = survey.id,
                respondentId = user?.id ?: "anonymous",
                answers = finalAnswers,
                timestamp = System.currentTimeMillis(),
                status = ResponseStatus.PENDING
            )

            firestoreRepository.submitResponse(response).onSuccess {
                setState { copy(isSubmitting = false, isSuccess = true) }
                sendEffect(SurveyFormEffect.NavigateBack)
            }.onFailure {
                setState { copy(isSubmitting = false, error = it.message) }
                sendEffect(SurveyFormEffect.ShowError(it.message ?: "Submission failed"))
            }
        }
    }
}
