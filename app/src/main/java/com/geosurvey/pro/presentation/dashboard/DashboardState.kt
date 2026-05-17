package com.geosurvey.pro.presentation.dashboard

import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.domain.model.LocationData
import com.geosurvey.pro.domain.model.SurveyResponse

data class DashboardState(
    val nearbySurveys: List<Survey> = emptyList(),
    val flaggedSurveys: List<Pair<Survey, SurveyResponse>> = emptyList(),
    val userLocation: LocationData? = null,
    val userName: String = "Field Agent",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

sealed class DashboardEffect {
    data class NavigateToSurvey(val surveyId: String) : DashboardEffect()
    data class ShowError(val message: String) : DashboardEffect()
}

sealed class DashboardEvent {
    object Refresh : DashboardEvent()
    data class SurveyClicked(val survey: Survey) : DashboardEvent()
}
