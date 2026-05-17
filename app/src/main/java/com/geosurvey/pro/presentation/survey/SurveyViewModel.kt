package com.geosurvey.pro.presentation.survey

import androidx.lifecycle.viewModelScope
import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.SurveyQuestion
import com.geosurvey.pro.domain.repository.AuthRepository
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: com.geosurvey.pro.domain.repository.LocationRepository
) : BaseViewModel<SurveyState, SurveyEffect, SurveyEvent>(SurveyState()) {

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            user?.let { admin ->
                combine(
                    firestoreRepository.getAdminSurveys(admin.id),
                    firestoreRepository.getAllAdminResponses(admin.id)
                ) { surveys, allResponses ->
                    // Filter responses that belong to admin's surveys
                    val surveyIds = surveys.map { it.id }.toSet()
                    val filteredResponses = allResponses.filter { it.surveyId in surveyIds }
                    Pair(surveys, filteredResponses)
                }.collectLatest { (surveys, filteredResponses) ->
                    setState { 
                        copy(
                            surveys = surveys,
                            allResponses = filteredResponses,
                            isLoading = false
                        ) 
                    }
                }
            }
        }
    }

    private fun loadSurvey(id: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            firestoreRepository.getSurvey(id).onSuccess { survey ->
                setState { copy(isLoading = false, currentSurvey = survey) }
            }.onFailure {
                setState { copy(isLoading = false, error = it.message) }
            }
        }
    }

    override fun onEvent(event: SurveyEvent) {
        when (event) {
            is SurveyEvent.LoadSurvey -> loadSurvey(event.id)
            is SurveyEvent.UpdateTitle -> setState { copy(currentSurvey = currentSurvey.copy(title = event.title)) }
            is SurveyEvent.UpdateDescription -> setState { copy(currentSurvey = currentSurvey.copy(description = event.desc)) }
            SurveyEvent.AddQuestion -> {
                val newList = uiState.value.currentSurvey.questions + SurveyQuestion()
                setState { copy(currentSurvey = currentSurvey.copy(questions = newList)) }
            }
            is SurveyEvent.RemoveQuestion -> {
                val newList = uiState.value.currentSurvey.questions.filter { it.id != event.id }
                setState { copy(currentSurvey = currentSurvey.copy(questions = newList)) }
            }
            is SurveyEvent.UpdateQuestion -> {
                val newList = uiState.value.currentSurvey.questions.map {
                    if (it.id == event.question.id) event.question else it
                }
                setState { copy(currentSurvey = currentSurvey.copy(questions = newList)) }
            }
            is SurveyEvent.UpdateLocation -> {
                setState { 
                    copy(
                        currentSurvey = currentSurvey.copy(
                            latitude = event.lat, 
                            longitude = event.lng, 
                            radius = event.radius,
                            locationName = event.locationName ?: currentSurvey.locationName
                        )
                    ) 
                }
            }
            SurveyEvent.SaveSurvey -> saveSurvey()
            SurveyEvent.UseCurrentLocation -> useCurrentLocation()
            is SurveyEvent.DeleteSurvey -> deleteSurvey(event.id)
            is SurveyEvent.ToggleStatus -> toggleStatus(event.id, event.isActive)
        }
    }

    private fun useCurrentLocation() {
        viewModelScope.launch {
            locationRepository.getCurrentLocation()?.let { location ->
                setState { 
                    copy(
                        currentSurvey = currentSurvey.copy(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                }
            }
        }
    }

    private fun saveSurvey() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val user = authRepository.currentUser.first()
            val survey = uiState.value.currentSurvey.copy(
                id = uiState.value.currentSurvey.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                creatorId = user?.id ?: ""
            )
            val isNew = uiState.value.currentSurvey.id.isEmpty()
            val result = if (isNew) {
                firestoreRepository.createSurvey(survey)
            } else {
                firestoreRepository.updateSurvey(survey)
            }
            result.onSuccess {
                if (isNew) {
                    firestoreRepository.broadcastNotification(
                        title = "New Survey: ${survey.title}",
                        body = "A new mapping project is available in ${survey.locationName}."
                    )
                }
                setState { copy(isLoading = false, isSuccess = true, currentSurvey = Survey()) }
                sendEffect(SurveyEffect.NavigateBack)
            }.onFailure {
                setState { copy(isLoading = false, error = it.message) }
            }
        }
    }

    private fun deleteSurvey(id: String) {
        viewModelScope.launch {
            firestoreRepository.deleteSurvey(id)
        }
    }

    private fun toggleStatus(id: String, isActive: Boolean) {
        viewModelScope.launch {
            firestoreRepository.toggleSurveyStatus(id, isActive)
        }
    }
}
