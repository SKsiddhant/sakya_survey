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
            
            combine(
                firestoreRepository.getSurveys(),
                locationRepository.getLocationUpdates(),
                if (user != null) firestoreRepository.getUserResponses(user.id) else kotlinx.coroutines.flow.flowOf(emptyList())
            ) { surveys, location, userResponses ->
                val nearby = surveys.filter { survey ->
                    survey.isActive && isWithinRadius(location.latitude, location.longitude, survey)
                }
                
                val flagged = userResponses.filter { it.status == ResponseStatus.REJECTED }
                    .mapNotNull { response -> 
                        surveys.find { it.id == response.surveyId }?.let { it to response }
                    }
                
                Triple(nearby, location, flagged)
            }.collect { (nearby, location, flagged) ->
                setState { 
                    copy(
                        nearbySurveys = nearby,
                        userLocation = location,
                        flaggedSurveys = flagged,
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
                sendEffect(DashboardEffect.NavigateToSurvey(event.survey.id))
            }
        }
    }
}
