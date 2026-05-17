package com.geosurvey.pro.presentation.dashboard

import androidx.lifecycle.viewModelScope
import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.repository.AuthRepository
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.domain.repository.LocationRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.onStart

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository
) : BaseViewModel<DashboardState, DashboardEffect, DashboardEvent>(DashboardState()) {

    init {
        observeSurveysAndLocation()
    }

    private fun observeSurveysAndLocation() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val user = authRepository.currentUser.first()
            
            // We use onStart to emit a null location immediately so the combine doesn't hang
            // waiting for a GPS fix. This ensures surveys show up even if GPS is slow.
            val locationFlow = locationRepository.getLocationUpdates()
                .onStart { emit(com.geosurvey.pro.domain.model.LocationData(0.0, 0.0)) }

            combine(
                firestoreRepository.getSurveys(),
                locationFlow,
                if (user != null) firestoreRepository.getUserResponses(user.id) else kotlinx.coroutines.flow.flowOf(emptyList())
            ) { surveys, location, userResponses ->
                // Show all active surveys
                val activeSurveys = surveys.filter { it.isActive }
                
                val flagged = userResponses.filter { it.status == ResponseStatus.REJECTED }
                    .mapNotNull { response -> 
                        surveys.find { it.id == response.surveyId }?.let { it to response }
                    }
                
                Triple(activeSurveys, location, flagged)
            }.collect { (activeSurveys, location, flagged) ->
                // Since we need to fetch user responses separately for state (or use the one from combine)
                // Let's just use the ones from the combine directly to be faster.
                val userResponses = user?.let { firestoreRepository.getUserResponses(it.id).first() } ?: emptyList()
                
                setState { 
                    copy(
                        nearbySurveys = activeSurveys,
                        userLocation = if (location.latitude == 0.0) null else location,
                        flaggedSurveys = flagged,
                        userResponses = userResponses,
                        isLoading = false,
                        isRefreshing = false,
                        userName = user?.displayName ?: "Field Agent"
                    )
                }
            }
        }
    }

    private fun isWithinRadius(lat: Double, lng: Double, survey: Survey): Boolean {
        val distance = locationRepository.calculateDistance(lat, lng, survey.latitude, survey.longitude)
        return distance <= survey.radius
    }

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Refresh -> observeSurveysAndLocation()
            is DashboardEvent.SurveyClicked -> {
                val survey = event.survey
                val userLoc = uiState.value.userLocation
                
                if (userLoc != null && !isWithinRadius(userLoc.latitude, userLoc.longitude, survey)) {
                    val distance = locationRepository.calculateDistance(userLoc.latitude, userLoc.longitude, survey.latitude, survey.longitude)
                    sendEffect(DashboardEffect.ShowError("You are ${distance.toInt()}m away. Please get within ${survey.radius.toInt()}m to start."))
                } else {
                    sendEffect(DashboardEffect.NavigateToSurvey(survey.id))
                }
            }
        }
    }
}
